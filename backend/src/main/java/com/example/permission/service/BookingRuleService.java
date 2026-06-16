package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.BookingRule;
import com.example.permission.entity.BookingRuleExemption;
import com.example.permission.entity.BookingRuleValidationLog;
import com.example.permission.mapper.BookingRuleExemptionMapper;
import com.example.permission.mapper.BookingRuleMapper;
import com.example.permission.mapper.BookingRuleValidationLogMapper;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.BookingRuleExemptionTableDef.BOOKING_RULE_EXEMPTION;
import static com.example.permission.entity.table.BookingRuleTableDef.BOOKING_RULE;
import static com.example.permission.entity.table.BookingRuleValidationLogTableDef.BOOKING_RULE_VALIDATION_LOG;

@Service
public class BookingRuleService {

    @Autowired
    private BookingRuleMapper bookingRuleMapper;

    @Autowired
    private BookingRuleValidationLogMapper bookingRuleValidationLogMapper;

    @Autowired
    private BookingRuleExemptionMapper bookingRuleExemptionMapper;

    public PageResult<BookingRule> getRulePage(Integer pageNum, Integer pageSize, String ruleName, Integer ruleType, Integer enabled) {
        QueryWrapper query = QueryWrapper.create()
                .from(BookingRule.class)
                .where(BOOKING_RULE.DELETED.eq(0));

        if (StringUtils.hasText(ruleName)) {
            query.and(BOOKING_RULE.RULE_NAME.like(ruleName));
        }
        if (ruleType != null) {
            query.and(BOOKING_RULE.RULE_TYPE.eq(ruleType));
        }
        if (enabled != null) {
            query.and(BOOKING_RULE.ENABLED.eq(enabled));
        }
        query.orderBy(BOOKING_RULE.PRIORITY.asc());

        Page<BookingRule> page = bookingRuleMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public BookingRule getRuleById(Long id) {
        if (id == null) {
            throw new BusinessException("规则ID不能为空");
        }
        BookingRule rule = bookingRuleMapper.selectOneById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        return rule;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createRule(BookingRule rule, LoginUser loginUser) {
        if (rule == null) {
            throw new BusinessException("规则信息不能为空");
        }
        if (!StringUtils.hasText(rule.getRuleName())) {
            throw new BusinessException("规则名称不能为空");
        }
        if (rule.getRuleType() == null) {
            throw new BusinessException("规则类型不能为空");
        }
        if (rule.getPriority() == null) {
            throw new BusinessException("规则优先级不能为空");
        }

        QueryWrapper priorityQuery = QueryWrapper.create()
                .from(BookingRule.class)
                .where(BOOKING_RULE.PRIORITY.eq(rule.getPriority()))
                .and(BOOKING_RULE.DELETED.eq(0));
        if (bookingRuleMapper.selectCountByQuery(priorityQuery) > 0) {
            throw new BusinessException("优先级已存在，请使用其他优先级");
        }

        rule.setCreateUserId(loginUser.getUserId());
        rule.setCreateUserName(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        rule.setDeleted(0);
        bookingRuleMapper.insert(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRule(BookingRule rule) {
        if (rule == null || rule.getId() == null) {
            throw new BusinessException("规则ID不能为空");
        }
        BookingRule existing = bookingRuleMapper.selectOneById(rule.getId());
        if (existing == null) {
            throw new BusinessException("规则不存在");
        }
        rule.setUpdateTime(LocalDateTime.now());
        bookingRuleMapper.update(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        if (id == null) {
            throw new BusinessException("规则ID不能为空");
        }
        BookingRule rule = bookingRuleMapper.selectOneById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        rule.setDeleted(1);
        rule.setUpdateTime(LocalDateTime.now());
        bookingRuleMapper.update(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void toggleRule(Long id, Integer enabled) {
        if (id == null) {
            throw new BusinessException("规则ID不能为空");
        }
        BookingRule rule = bookingRuleMapper.selectOneById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        rule.setEnabled(enabled);
        rule.setUpdateTime(LocalDateTime.now());
        bookingRuleMapper.update(rule);
    }

    public List<Map> validateBooking(Map<String, Object> bookingParams, LoginUser loginUser) {
        QueryWrapper ruleQuery = QueryWrapper.create()
                .from(BookingRule.class)
                .where(BOOKING_RULE.ENABLED.eq(1))
                .and(BOOKING_RULE.DELETED.eq(0))
                .orderBy(BOOKING_RULE.PRIORITY.asc());
        List<BookingRule> rules = bookingRuleMapper.selectListByQuery(ruleQuery);

        List<Map> results = new ArrayList<>();
        for (BookingRule rule : rules) {
            if (!isRuleApplicable(rule, bookingParams)) {
                continue;
            }
            Map<String, Object> validationResult = validateRule(rule, bookingParams);
            results.add(validationResult);

            BookingRuleValidationLog log = new BookingRuleValidationLog();
            log.setBookingId(bookingParams.get("bookingId") != null ? Long.valueOf(bookingParams.get("bookingId").toString()) : null);
            log.setRuleId(rule.getId());
            log.setRuleName(rule.getRuleName());
            log.setRuleType(rule.getRuleType());
            log.setValidationResult((Integer) validationResult.get("result"));
            log.setValidationMessage((String) validationResult.get("message"));
            log.setExempted(0);
            log.setOperatorId(loginUser.getUserId());
            log.setOperatorName(loginUser.getUser().getNickname() != null ?
                    loginUser.getUser().getNickname() : loginUser.getUsername());
            log.setCreateTime(LocalDateTime.now());
            bookingRuleValidationLogMapper.insert(log);
        }
        return results;
    }

    private boolean isRuleApplicable(BookingRule rule, Map<String, Object> bookingParams) {
        if (StringUtils.hasText(rule.getApplyRoomTypes())) {
            Object roomTypeId = bookingParams.get("roomTypeId");
            if (roomTypeId == null) {
                return false;
            }
            List<String> applyTypes = Arrays.asList(rule.getApplyRoomTypes().split(","));
            if (!applyTypes.contains(roomTypeId.toString())) {
                return false;
            }
        }
        if (rule.getApplyDateStart() != null && rule.getApplyDateEnd() != null) {
            Object checkInDate = bookingParams.get("checkInDate");
            if (checkInDate == null) {
                return false;
            }
            LocalDate checkIn = checkInDate instanceof LocalDate ? (LocalDate) checkInDate : LocalDate.parse(checkInDate.toString());
            if (checkIn.isBefore(rule.getApplyDateStart()) || checkIn.isAfter(rule.getApplyDateEnd())) {
                return false;
            }
        }
        if (StringUtils.hasText(rule.getApplySources())) {
            Object source = bookingParams.get("bookingSource");
            if (source == null) {
                return false;
            }
            List<String> applySources = Arrays.asList(rule.getApplySources().split(","));
            if (!applySources.contains(source.toString())) {
                return false;
            }
        }
        return true;
    }

    private Map<String, Object> validateRule(BookingRule rule, Map<String, Object> bookingParams) {
        Map<String, Object> result = new HashMap<>();
        result.put("ruleId", rule.getId());
        result.put("ruleName", rule.getRuleName());
        result.put("ruleType", rule.getRuleType());

        String ruleParams = rule.getRuleParams();
        Map<String, Object> params = parseRuleParams(ruleParams);

        Object checkInObj = bookingParams.get("checkInDate");
        Object checkOutObj = bookingParams.get("checkOutDate");
        LocalDate checkInDate = checkInObj instanceof LocalDate ? (LocalDate) checkInObj : (checkInObj != null ? LocalDate.parse(checkInObj.toString()) : null);
        LocalDate checkOutDate = checkOutObj instanceof LocalDate ? (LocalDate) checkOutObj : (checkOutObj != null ? LocalDate.parse(checkOutObj.toString()) : null);

        long days = 0;
        if (checkInDate != null && checkOutDate != null) {
            days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }

        boolean passed = true;
        String message = "";

        switch (rule.getRuleType()) {
            case 1: {
                int minDays = params.get("minDays") != null ? Integer.parseInt(params.get("minDays").toString()) : 1;
                if (days < minDays) {
                    passed = false;
                    message = "入住天数不足" + minDays + "晚";
                }
                break;
            }
            case 2: {
                int maxDays = params.get("maxDays") != null ? Integer.parseInt(params.get("maxDays").toString()) : 30;
                if (days > maxDays) {
                    passed = false;
                    message = "入住天数超过" + maxDays + "晚限制";
                }
                break;
            }
            case 3: {
                int advanceDays = params.get("advanceDays") != null ? Integer.parseInt(params.get("advanceDays").toString()) : 0;
                if (checkInDate != null) {
                    long daysToCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), checkInDate);
                    if (daysToCheckIn < advanceDays) {
                        passed = false;
                        message = "需提前" + advanceDays + "天预订";
                    }
                }
                break;
            }
            case 4: {
                String latestTime = params.get("latestTime") != null ? params.get("latestTime").toString() : "23:59";
                if (checkInDate != null && checkInDate.equals(LocalDate.now())) {
                    LocalTime limit = LocalTime.parse(latestTime, DateTimeFormatter.ofPattern("HH:mm"));
                    if (LocalTime.now().isAfter(limit)) {
                        passed = false;
                        message = "当日预订截止时间为" + latestTime;
                    }
                }
                break;
            }
            case 5: {
                String restrictedDates = params.get("restrictedDates") != null ? params.get("restrictedDates").toString() : "";
                if (StringUtils.hasText(restrictedDates) && checkInDate != null && checkOutDate != null) {
                    List<String> dates = Arrays.asList(restrictedDates.split(","));
                    LocalDate current = checkInDate;
                    while (current.isBefore(checkOutDate)) {
                        if (dates.contains(current.toString())) {
                            passed = false;
                            message = "入住期间包含限制日期" + current;
                            break;
                        }
                        current = current.plusDays(1);
                    }
                }
                break;
            }
            case 6: {
                String restrictedRoomTypes = params.get("restrictedRoomTypes") != null ? params.get("restrictedRoomTypes").toString() : "";
                Object roomTypeId = bookingParams.get("roomTypeId");
                if (StringUtils.hasText(restrictedRoomTypes) && roomTypeId != null) {
                    List<String> restricted = Arrays.asList(restrictedRoomTypes.split(","));
                    if (restricted.contains(roomTypeId.toString())) {
                        passed = false;
                        message = "该房型当前受限制";
                    }
                }
                break;
            }
            case 7: {
                String allowedCustomerTypes = params.get("allowedCustomerTypes") != null ? params.get("allowedCustomerTypes").toString() : "";
                Object customerType = bookingParams.get("customerType");
                if (StringUtils.hasText(allowedCustomerTypes) && customerType != null) {
                    List<String> allowed = Arrays.asList(allowedCustomerTypes.split(","));
                    if (!allowed.contains(customerType.toString())) {
                        passed = false;
                        message = "该客户类型不允许预订";
                    }
                }
                break;
            }
            default:
                break;
        }

        result.put("result", passed ? 1 : 0);
        result.put("status", passed ? "pass" : "block");
        result.put("message", message);
        return result;
    }

    private Map<String, Object> parseRuleParams(String ruleParams) {
        Map<String, Object> params = new HashMap<>();
        if (!StringUtils.hasText(ruleParams)) {
            return params;
        }
        if (ruleParams.trim().startsWith("{")) {
            ruleParams = ruleParams.trim();
            String content = ruleParams.substring(1, ruleParams.length() - 1);
            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim().replace("\"", "");
                    String value = kv[1].trim().replace("\"", "");
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    @Transactional(rollbackFor = Exception.class)
    public void exemptRule(Long bookingId, Long ruleId, String exemptionReason, LoginUser loginUser) {
        if (bookingId == null || ruleId == null) {
            throw new BusinessException("预订ID和规则ID不能为空");
        }
        if (!StringUtils.hasText(exemptionReason)) {
            throw new BusinessException("豁免原因不能为空");
        }

        List<String> allowedRoles = Arrays.asList("admin", "hotel_admin", "frontdesk_manager");
        boolean hasPermission = false;
        if (loginUser.getPermissions() != null) {
            for (String perm : loginUser.getPermissions()) {
                if (allowedRoles.contains(perm)) {
                    hasPermission = true;
                    break;
                }
            }
        }
        if (!hasPermission) {
            throw new BusinessException("无权限豁免规则");
        }

        BookingRule rule = bookingRuleMapper.selectOneById(ruleId);

        BookingRuleExemption exemption = new BookingRuleExemption();
        exemption.setBookingId(bookingId);
        exemption.setRuleId(ruleId);
        exemption.setRuleName(rule != null ? rule.getRuleName() : "");
        exemption.setExemptionReason(exemptionReason);
        exemption.setExemptedById(loginUser.getUserId());
        exemption.setExemptedByName(loginUser.getUser().getNickname() != null ?
                loginUser.getUser().getNickname() : loginUser.getUsername());
        exemption.setCreateTime(LocalDateTime.now());
        bookingRuleExemptionMapper.insert(exemption);

        QueryWrapper logQuery = QueryWrapper.create()
                .from(BookingRuleValidationLog.class)
                .where(BOOKING_RULE_VALIDATION_LOG.BOOKING_ID.eq(bookingId))
                .and(BOOKING_RULE_VALIDATION_LOG.RULE_ID.eq(ruleId));
        List<BookingRuleValidationLog> logs = bookingRuleValidationLogMapper.selectListByQuery(logQuery);
        for (BookingRuleValidationLog log : logs) {
            log.setExempted(1);
            bookingRuleValidationLogMapper.update(log);
        }
    }

    public List<BookingRuleExemption> getExemptionsByBooking(Long bookingId) {
        if (bookingId == null) {
            throw new BusinessException("预订ID不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(BookingRuleExemption.class)
                .where(BOOKING_RULE_EXEMPTION.BOOKING_ID.eq(bookingId))
                .orderBy(BOOKING_RULE_EXEMPTION.CREATE_TIME.desc());
        return bookingRuleExemptionMapper.selectListByQuery(query);
    }

    public List<BookingRuleValidationLog> getValidationLogs(Long bookingId) {
        if (bookingId == null) {
            throw new BusinessException("预订ID不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(BookingRuleValidationLog.class)
                .where(BOOKING_RULE_VALIDATION_LOG.BOOKING_ID.eq(bookingId))
                .orderBy(BOOKING_RULE_VALIDATION_LOG.CREATE_TIME.desc());
        return bookingRuleValidationLogMapper.selectListByQuery(query);
    }
}
