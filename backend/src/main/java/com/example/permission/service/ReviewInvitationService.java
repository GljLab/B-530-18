package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.CheckIn;
import com.example.permission.entity.ReviewInvitation;
import com.example.permission.entity.ReviewRecord;
import com.example.permission.mapper.ReviewInvitationMapper;
import com.example.permission.mapper.ReviewRecordMapper;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

import static com.example.permission.entity.table.ReviewInvitationTableDef.REVIEW_INVITATION;
import static com.example.permission.entity.table.ReviewRecordTableDef.REVIEW_RECORD;

@Service
public class ReviewInvitationService {

    @Autowired
    private ReviewInvitationMapper reviewInvitationMapper;

    @Autowired
    private ReviewRecordMapper reviewRecordMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointRuleService pointRuleService;

    private LoginUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException("用户未登录");
    }

    private String generateReviewCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public ReviewInvitation createInvitation(CheckIn checkIn) {
        if (checkIn == null || checkIn.getId() == null) {
            throw new BusinessException("入住单信息不能为空");
        }

        QueryWrapper existQuery = QueryWrapper.create()
                .from(ReviewInvitation.class)
                .where(REVIEW_INVITATION.CHECK_IN_ID.eq(checkIn.getId()))
                .and(REVIEW_INVITATION.DELETED.eq(0));
        long existCount = reviewInvitationMapper.selectCountByQuery(existQuery);
        if (existCount > 0) {
            throw new BusinessException("该入住单已存在评价邀请");
        }

        String reviewCode = generateReviewCode();
        String reviewLink = "/h5/review?order=" + checkIn.getCheckInNo() + "&code=" + reviewCode;

        ReviewInvitation invitation = new ReviewInvitation();
        invitation.setCheckInId(checkIn.getId());
        invitation.setCheckInNo(checkIn.getCheckInNo());
        invitation.setCustomerId(checkIn.getCustomerId());
        invitation.setCustomerName(checkIn.getCustomerName());
        invitation.setCustomerPhone(checkIn.getCustomerPhone());
        invitation.setRoomTypeId(checkIn.getRoomTypeId());
        invitation.setRoomTypeName(checkIn.getRoomTypeName());
        invitation.setCheckInDate(checkIn.getCheckInDate());
        invitation.setCheckOutDate(checkIn.getCheckOutDate());
        invitation.setReviewCode(reviewCode);
        invitation.setReviewLink(reviewLink);
        invitation.setReviewStatus(0);
        invitation.setIsSent(0);
        invitation.setExpireTime(LocalDateTime.now().plusDays(7));
        invitation.setCreateTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());
        invitation.setDeleted(0);

        reviewInvitationMapper.insert(invitation);
        return invitation;
    }

    public PageResult<ReviewInvitation> list(Map<String, Object> params, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewInvitation.class)
                .where(REVIEW_INVITATION.DELETED.eq(0));

        if (params != null) {
            String keyword = (String) params.get("keyword");
            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordTrim = keyword.trim();
                query.and(REVIEW_INVITATION.CUSTOMER_NAME.like(keywordTrim)
                        .or(REVIEW_INVITATION.CUSTOMER_PHONE.like(keywordTrim))
                        .or(REVIEW_INVITATION.CHECK_IN_NO.like(keywordTrim)));
            }

            Integer reviewStatus = (Integer) params.get("reviewStatus");
            if (reviewStatus != null) {
                query.and(REVIEW_INVITATION.REVIEW_STATUS.eq(reviewStatus));
            }

            String startDate = (String) params.get("startDate");
            if (startDate != null && !startDate.trim().isEmpty()) {
                query.and(REVIEW_INVITATION.CREATE_TIME.ge(LocalDateTime.parse(startDate.trim() + " 00:00:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            }

            String endDate = (String) params.get("endDate");
            if (endDate != null && !endDate.trim().isEmpty()) {
                query.and(REVIEW_INVITATION.CREATE_TIME.le(LocalDateTime.parse(endDate.trim() + " 23:59:59",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            }
        }

        query.orderBy(REVIEW_INVITATION.CREATE_TIME.desc());

        Page<ReviewInvitation> page = reviewInvitationMapper.paginate(pageNum, pageSize, query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(), (long) pageNum, (long) pageSize);
    }

    public ReviewInvitation getById(Long id) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        ReviewInvitation invitation = reviewInvitationMapper.selectOneById(id);
        if (invitation == null || invitation.getDeleted() == 1) {
            throw new BusinessException("评价邀请不存在");
        }
        return invitation;
    }

    public ReviewInvitation getByCheckInId(Long checkInId) {
        if (checkInId == null) {
            throw new BusinessException("入住单ID不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewInvitation.class)
                .where(REVIEW_INVITATION.CHECK_IN_ID.eq(checkInId))
                .and(REVIEW_INVITATION.DELETED.eq(0));
        return reviewInvitationMapper.selectOneByQuery(query);
    }

    @Transactional
    public void sendInvitation(Long id, Integer sendMethod) {
        ReviewInvitation invitation = getById(id);
        if (invitation.getIsSent() != null && invitation.getIsSent() == 1) {
            throw new BusinessException("该邀请已发送");
        }
        if (invitation.getReviewStatus() != null && invitation.getReviewStatus() == 1) {
            throw new BusinessException("该入住单已完成评价");
        }

        invitation.setIsSent(1);
        invitation.setSendMethod(sendMethod);
        invitation.setSendTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());
        reviewInvitationMapper.update(invitation);
    }

    public String getReviewLink(Long id) {
        ReviewInvitation invitation = getById(id);
        return invitation.getReviewLink();
    }

    public ReviewInvitation validateLink(String checkInNo, String code) {
        if (checkInNo == null || checkInNo.trim().isEmpty()) {
            throw new BusinessException("入住单号不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewInvitation.class)
                .where(REVIEW_INVITATION.CHECK_IN_NO.eq(checkInNo.trim()))
                .and(REVIEW_INVITATION.REVIEW_CODE.eq(code.trim()))
                .and(REVIEW_INVITATION.DELETED.eq(0));
        ReviewInvitation invitation = reviewInvitationMapper.selectOneByQuery(query);

        if (invitation == null) {
            throw new BusinessException("评价链接无效");
        }

        if (invitation.getReviewStatus() != null && invitation.getReviewStatus() == 1) {
            throw new BusinessException("该入住单已完成评价");
        }

        if (invitation.getExpireTime() != null && invitation.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("评价链接已过期");
        }

        QueryWrapper recordQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.CHECK_IN_ID.eq(invitation.getCheckInId()))
                .and(REVIEW_RECORD.DELETED.eq(0));
        long recordCount = reviewRecordMapper.selectCountByQuery(recordQuery);
        if (recordCount > 0) {
            throw new BusinessException("该入住单已完成评价");
        }

        return invitation;
    }
}
