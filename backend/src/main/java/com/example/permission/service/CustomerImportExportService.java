package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Customer;
import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.entity.CustomerPreference;
import com.example.permission.entity.CustomerTag;
import com.example.permission.entity.CustomerTagRelation;
import com.example.permission.mapper.CustomerMapper;
import com.example.permission.mapper.CustomerOperationLogMapper;
import com.example.permission.mapper.CustomerPreferenceMapper;
import com.example.permission.mapper.CustomerTagMapper;
import com.example.permission.mapper.CustomerTagRelationMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.CustomerTableDef.CUSTOMER;

@Service
public class CustomerImportExportService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private CustomerTagRelationMapper customerTagRelationMapper;

    @Autowired
    private CustomerTagMapper customerTagMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    @Autowired
    private CustomerPreferenceMapper customerPreferenceMapper;

    private static final String[] GENDER_OPTIONS = {"男", "女"};
    private static final String[] ID_TYPE_OPTIONS = {"身份证", "护照", "港澳通行证", "台胞证", "驾驶证"};
    private static final String[] CUSTOMER_TYPE_OPTIONS = {"散客", "企业客户", "协议客户", "VIP客户"};
    private static final String[] CUSTOMER_SOURCE_OPTIONS = {"官网", "OTA平台", "企业协议", "电话预订", "前台登记", "老客户推荐"};
    private static final String[] IMPORTANCE_OPTIONS = {"普通", "重要", "VIP"};

    private static final Map<String, Integer> GENDER_MAP = new HashMap<>();
    private static final Map<String, Integer> ID_TYPE_MAP = new HashMap<>();
    private static final Map<String, Integer> CUSTOMER_TYPE_MAP = new HashMap<>();
    private static final Map<String, Integer> CUSTOMER_SOURCE_MAP = new HashMap<>();
    private static final Map<String, Integer> IMPORTANCE_MAP = new HashMap<>();

    static {
        GENDER_MAP.put("男", 1);
        GENDER_MAP.put("女", 2);
        ID_TYPE_MAP.put("身份证", 1);
        ID_TYPE_MAP.put("护照", 2);
        ID_TYPE_MAP.put("港澳通行证", 3);
        ID_TYPE_MAP.put("台胞证", 4);
        ID_TYPE_MAP.put("驾驶证", 5);
        CUSTOMER_TYPE_MAP.put("散客", 1);
        CUSTOMER_TYPE_MAP.put("企业客户", 2);
        CUSTOMER_TYPE_MAP.put("协议客户", 3);
        CUSTOMER_TYPE_MAP.put("VIP客户", 4);
        CUSTOMER_SOURCE_MAP.put("官网", 1);
        CUSTOMER_SOURCE_MAP.put("OTA平台", 2);
        CUSTOMER_SOURCE_MAP.put("企业协议", 3);
        CUSTOMER_SOURCE_MAP.put("电话预订", 4);
        CUSTOMER_SOURCE_MAP.put("前台登记", 5);
        CUSTOMER_SOURCE_MAP.put("老客户推荐", 6);
        IMPORTANCE_MAP.put("普通", 1);
        IMPORTANCE_MAP.put("重要", 2);
        IMPORTANCE_MAP.put("VIP", 3);
    }

    private static final Map<Integer, String> GENDER_TEXT = new HashMap<>();
    private static final Map<Integer, String> ID_TYPE_TEXT = new HashMap<>();
    private static final Map<Integer, String> CUSTOMER_TYPE_TEXT = new HashMap<>();
    private static final Map<Integer, String> CUSTOMER_SOURCE_TEXT = new HashMap<>();
    private static final Map<Integer, String> IMPORTANCE_TEXT = new HashMap<>();

    static {
        GENDER_TEXT.put(1, "男");
        GENDER_TEXT.put(2, "女");
        ID_TYPE_TEXT.put(1, "身份证");
        ID_TYPE_TEXT.put(2, "护照");
        ID_TYPE_TEXT.put(3, "港澳通行证");
        ID_TYPE_TEXT.put(4, "台胞证");
        ID_TYPE_TEXT.put(5, "驾驶证");
        CUSTOMER_TYPE_TEXT.put(1, "散客");
        CUSTOMER_TYPE_TEXT.put(2, "企业客户");
        CUSTOMER_TYPE_TEXT.put(3, "协议客户");
        CUSTOMER_TYPE_TEXT.put(4, "VIP客户");
        CUSTOMER_SOURCE_TEXT.put(1, "官网");
        CUSTOMER_SOURCE_TEXT.put(2, "OTA平台");
        CUSTOMER_SOURCE_TEXT.put(3, "企业协议");
        CUSTOMER_SOURCE_TEXT.put(4, "电话预订");
        CUSTOMER_SOURCE_TEXT.put(5, "前台登记");
        CUSTOMER_SOURCE_TEXT.put(6, "老客户推荐");
        IMPORTANCE_TEXT.put(1, "普通");
        IMPORTANCE_TEXT.put(2, "重要");
        IMPORTANCE_TEXT.put(3, "VIP");
    }

    public byte[] getImportTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户导入模板");
            CellStyle headerStyle = createHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"姓名", "性别(男/女)", "手机号", "备用手机号", "邮箱", "微信",
                    "证件类型(身份证/护照/港澳通行证/台胞证/驾驶证)", "证件号码", "国籍",
                    "客户类型(散客/企业客户/协议客户/VIP客户)",
                    "客户来源(官网/OTA平台/企业协议/电话预订/前台登记/老客户推荐)",
                    "重要程度(普通/重要/VIP)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 3000) width = 3000;
                if (width > 15000) width = 15000;
                sheet.setColumnWidth(i, width);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成导入模板失败：" + e.getMessage(), e);
        }
    }

    @Transactional
    public Map<String, Object> importCustomers(byte[] fileData, Long operatorId, String operatorName) {
        int successCount = 0;
        int failCount = 0;
        List<Map<String, Object>> failDetails = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new java.io.ByteArrayInputStream(fileData))) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int rowNum = i + 1;
                try {
                    String name = getCellStringValue(row, 0);
                    String genderStr = getCellStringValue(row, 1);
                    String phone = getCellStringValue(row, 2);
                    String backupPhone = getCellStringValue(row, 3);
                    String email = getCellStringValue(row, 4);
                    String wechat = getCellStringValue(row, 5);
                    String idTypeStr = getCellStringValue(row, 6);
                    String idNumber = getCellStringValue(row, 7);
                    String nationality = getCellStringValue(row, 8);
                    String customerTypeStr = getCellStringValue(row, 9);
                    String customerSourceStr = getCellStringValue(row, 10);
                    String importanceStr = getCellStringValue(row, 11);

                    if (!StringUtils.hasText(name)) {
                        failCount++;
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("row", rowNum);
                        detail.put("reason", "姓名不能为空");
                        failDetails.add(detail);
                        continue;
                    }

                    if (!StringUtils.hasText(phone)) {
                        failCount++;
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("row", rowNum);
                        detail.put("reason", "手机号不能为空");
                        failDetails.add(detail);
                        continue;
                    }

                    if (!phone.matches("^1[3-9]\\d{9}$")) {
                        failCount++;
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("row", rowNum);
                        detail.put("reason", "手机号格式不正确");
                        failDetails.add(detail);
                        continue;
                    }

                    if (StringUtils.hasText(email) && !email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                        failCount++;
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("row", rowNum);
                        detail.put("reason", "邮箱格式不正确");
                        failDetails.add(detail);
                        continue;
                    }

                    if (StringUtils.hasText(idNumber)) {
                        Integer idTypeVal = ID_TYPE_MAP.getOrDefault(idTypeStr, 1);
                        if (idTypeVal == 1 && !idNumber.matches("^(\\d{15}|\\d{17}[\\dXx])$")) {
                            failCount++;
                            Map<String, Object> detail = new HashMap<>();
                            detail.put("row", rowNum);
                            detail.put("reason", "身份证号格式不正确");
                            failDetails.add(detail);
                            continue;
                        }
                        if (idTypeVal == 2 && !idNumber.matches("^[A-Za-z0-9]{5,20}$")) {
                            failCount++;
                            Map<String, Object> detail = new HashMap<>();
                            detail.put("row", rowNum);
                            detail.put("reason", "护照号格式不正确");
                            failDetails.add(detail);
                            continue;
                        }
                    }

                    QueryWrapper phoneCheck = QueryWrapper.create()
                            .from(Customer.class)
                            .where(CUSTOMER.PHONE.eq(phone))
                            .and(CUSTOMER.DELETED.eq(0));
                    if (customerMapper.selectCountByQuery(phoneCheck) > 0) {
                        failCount++;
                        Map<String, Object> detail = new HashMap<>();
                        detail.put("row", rowNum);
                        detail.put("reason", "手机号已存在");
                        failDetails.add(detail);
                        continue;
                    }

                    if (StringUtils.hasText(idNumber)) {
                        Integer idTypeVal = ID_TYPE_MAP.getOrDefault(idTypeStr, 1);
                        QueryWrapper idCheck = QueryWrapper.create()
                                .from(Customer.class)
                                .where(CUSTOMER.ID_NUMBER.eq(idNumber))
                                .and(CUSTOMER.ID_TYPE.eq(idTypeVal))
                                .and(CUSTOMER.DELETED.eq(0));
                        if (customerMapper.selectCountByQuery(idCheck) > 0) {
                            failCount++;
                            Map<String, Object> detail = new HashMap<>();
                            detail.put("row", rowNum);
                            detail.put("reason", "证件号码已存在");
                            failDetails.add(detail);
                            continue;
                        }
                    }

                    Customer customer = new Customer();
                    customer.setName(name);
                    customer.setGender(GENDER_MAP.getOrDefault(genderStr, null));
                    customer.setPhone(phone);
                    customer.setBackupPhone(StringUtils.hasText(backupPhone) ? backupPhone : null);
                    customer.setEmail(StringUtils.hasText(email) ? email : null);
                    customer.setWechat(StringUtils.hasText(wechat) ? wechat : null);
                    customer.setIdType(ID_TYPE_MAP.getOrDefault(idTypeStr, null));
                    customer.setIdNumber(StringUtils.hasText(idNumber) ? idNumber : null);
                    customer.setNationality(StringUtils.hasText(nationality) ? nationality : null);
                    customer.setCustomerType(CUSTOMER_TYPE_MAP.getOrDefault(customerTypeStr, null));
                    customer.setCustomerSource(CUSTOMER_SOURCE_MAP.getOrDefault(customerSourceStr, null));
                    customer.setImportance(IMPORTANCE_MAP.getOrDefault(importanceStr, null));
                    customer.setStatus(1);
                    customer.setDeleted(0);
                    customer.setTotalOrders(0);
                    customer.setTotalSpent(BigDecimal.ZERO);

                    customerMapper.insert(customer);
                    successCount++;

                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("row", rowNum);
                    detail.put("reason", e.getMessage() != null ? e.getMessage() : "未知错误");
                    failDetails.add(detail);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("解析Excel文件失败：" + e.getMessage(), e);
        }

        saveOperationLog(null, "批量导入", 12, operatorId, operatorName,
                "导入客户：成功" + successCount + "条，失败" + failCount + "条");

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failDetails", failDetails);
        return result;
    }

    @Transactional
    public byte[] exportCustomers(List<Long> customerIds, String exportRange, List<String> exportFields,
                                   Long operatorId, String operatorName) {
        List<Customer> customers;

        if ("all".equals(exportRange)) {
            QueryWrapper query = QueryWrapper.create()
                    .from(Customer.class)
                    .where(CUSTOMER.DELETED.eq(0))
                    .orderBy(CUSTOMER.CREATE_TIME.desc());
            customers = customerMapper.selectListByQuery(query);
        } else if ("custom".equals(exportRange) && customerIds != null && !customerIds.isEmpty()) {
            QueryWrapper query = QueryWrapper.create()
                    .from(Customer.class)
                    .where(CUSTOMER.ID.in(customerIds))
                    .and(CUSTOMER.DELETED.eq(0))
                    .orderBy(CUSTOMER.CREATE_TIME.desc());
            customers = customerMapper.selectListByQuery(query);
        } else {
            if (customerIds != null && !customerIds.isEmpty()) {
                QueryWrapper query = QueryWrapper.create()
                        .from(Customer.class)
                        .where(CUSTOMER.ID.in(customerIds))
                        .and(CUSTOMER.DELETED.eq(0))
                        .orderBy(CUSTOMER.CREATE_TIME.desc());
                customers = customerMapper.selectListByQuery(query);
            } else {
                QueryWrapper query = QueryWrapper.create()
                        .from(Customer.class)
                        .where(CUSTOMER.DELETED.eq(0))
                        .orderBy(CUSTOMER.CREATE_TIME.desc());
                customers = customerMapper.selectListByQuery(query);
            }
        }

        Map<Long, String> customerTagNames = loadCustomerTagNames();
        Map<Long, String> customerPreferenceSummary = loadCustomerPreferenceSummary();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("客户数据");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle infoStyle = createInfoStyle(workbook);

            int rowIdx = 0;

            Row infoRow1 = sheet.createRow(rowIdx++);
            Cell cell1 = infoRow1.createCell(0);
            cell1.setCellValue("导出时间：" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            cell1.setCellStyle(infoStyle);

            Row infoRow2 = sheet.createRow(rowIdx++);
            Cell cell2 = infoRow2.createCell(0);
            cell2.setCellValue("导出人：" + operatorName);
            cell2.setCellStyle(infoStyle);

            String filterDesc;
            if ("all".equals(exportRange)) {
                filterDesc = "全部客户";
            } else if ("custom".equals(exportRange)) {
                filterDesc = "自定义选择" + (customerIds != null ? customerIds.size() : 0) + "条";
            } else {
                filterDesc = "当前页" + (customerIds != null ? customerIds.size() : 0) + "条";
            }
            Row infoRow3 = sheet.createRow(rowIdx++);
            Cell cell3 = infoRow3.createCell(0);
            cell3.setCellValue("筛选条件：" + filterDesc);
            cell3.setCellStyle(infoStyle);

            rowIdx++;

            Row headerRow = sheet.createRow(rowIdx++);
            int colIdx = 0;
            Map<String, Integer> fieldIndex = new LinkedHashMap<>();

            if (exportFields != null && exportFields.contains("basic")) {
                String[] basicHeaders = {"姓名", "性别", "生日", "国籍"};
                for (String h : basicHeaders) {
                    Cell c = headerRow.createCell(colIdx);
                    c.setCellValue(h);
                    c.setCellStyle(headerStyle);
                    fieldIndex.put("basic_" + h, colIdx++);
                }
            }
            if (exportFields != null && exportFields.contains("idcard")) {
                String[] idcardHeaders = {"证件类型", "证件号码", "证件有效期"};
                for (String h : idcardHeaders) {
                    Cell c = headerRow.createCell(colIdx);
                    c.setCellValue(h);
                    c.setCellStyle(headerStyle);
                    fieldIndex.put("idcard_" + h, colIdx++);
                }
            }
            if (exportFields != null && exportFields.contains("contact")) {
                String[] contactHeaders = {"手机号", "备用手机号", "邮箱", "微信"};
                for (String h : contactHeaders) {
                    Cell c = headerRow.createCell(colIdx);
                    c.setCellValue(h);
                    c.setCellStyle(headerStyle);
                    fieldIndex.put("contact_" + h, colIdx++);
                }
            }
            if (exportFields != null && exportFields.contains("classification")) {
                String[] classHeaders = {"客户类型", "客户来源", "重要程度"};
                for (String h : classHeaders) {
                    Cell c = headerRow.createCell(colIdx);
                    c.setCellValue(h);
                    c.setCellStyle(headerStyle);
                    fieldIndex.put("classification_" + h, colIdx++);
                }
            }
            if (exportFields != null && exportFields.contains("statistics")) {
                String[] statHeaders = {"订单数", "消费额", "平均消费"};
                for (String h : statHeaders) {
                    Cell c = headerRow.createCell(colIdx);
                    c.setCellValue(h);
                    c.setCellStyle(headerStyle);
                    fieldIndex.put("statistics_" + h, colIdx++);
                }
            }
            if (exportFields != null && exportFields.contains("tags")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("标签列表");
                c.setCellStyle(headerStyle);
                fieldIndex.put("tags", colIdx++);
            }
            if (exportFields != null && exportFields.contains("preference")) {
                Cell c = headerRow.createCell(colIdx);
                c.setCellValue("偏好摘要");
                c.setCellStyle(headerStyle);
                fieldIndex.put("preference", colIdx++);
            }

            for (Customer customer : customers) {
                Row dataRow = sheet.createRow(rowIdx++);

                if (fieldIndex.containsKey("basic_姓名")) {
                    dataRow.createCell(fieldIndex.get("basic_姓名")).setCellValue(customer.getName() != null ? customer.getName() : "");
                }
                if (fieldIndex.containsKey("basic_性别")) {
                    dataRow.createCell(fieldIndex.get("basic_性别")).setCellValue(GENDER_TEXT.getOrDefault(customer.getGender(), ""));
                }
                if (fieldIndex.containsKey("basic_生日")) {
                    dataRow.createCell(fieldIndex.get("basic_生日")).setCellValue(customer.getBirthDate() != null ? customer.getBirthDate().toString() : "");
                }
                if (fieldIndex.containsKey("basic_国籍")) {
                    dataRow.createCell(fieldIndex.get("basic_国籍")).setCellValue(customer.getNationality() != null ? customer.getNationality() : "");
                }

                if (fieldIndex.containsKey("idcard_证件类型")) {
                    dataRow.createCell(fieldIndex.get("idcard_证件类型")).setCellValue(ID_TYPE_TEXT.getOrDefault(customer.getIdType(), ""));
                }
                if (fieldIndex.containsKey("idcard_证件号码")) {
                    dataRow.createCell(fieldIndex.get("idcard_证件号码")).setCellValue(customer.getIdNumber() != null ? customer.getIdNumber() : "");
                }
                if (fieldIndex.containsKey("idcard_证件有效期")) {
                    dataRow.createCell(fieldIndex.get("idcard_证件有效期")).setCellValue(customer.getIdExpiryDate() != null ? customer.getIdExpiryDate().toString() : "");
                }

                if (fieldIndex.containsKey("contact_手机号")) {
                    dataRow.createCell(fieldIndex.get("contact_手机号")).setCellValue(customer.getPhone() != null ? customer.getPhone() : "");
                }
                if (fieldIndex.containsKey("contact_备用手机号")) {
                    dataRow.createCell(fieldIndex.get("contact_备用手机号")).setCellValue(customer.getBackupPhone() != null ? customer.getBackupPhone() : "");
                }
                if (fieldIndex.containsKey("contact_邮箱")) {
                    dataRow.createCell(fieldIndex.get("contact_邮箱")).setCellValue(customer.getEmail() != null ? customer.getEmail() : "");
                }
                if (fieldIndex.containsKey("contact_微信")) {
                    dataRow.createCell(fieldIndex.get("contact_微信")).setCellValue(customer.getWechat() != null ? customer.getWechat() : "");
                }

                if (fieldIndex.containsKey("classification_客户类型")) {
                    dataRow.createCell(fieldIndex.get("classification_客户类型")).setCellValue(CUSTOMER_TYPE_TEXT.getOrDefault(customer.getCustomerType(), ""));
                }
                if (fieldIndex.containsKey("classification_客户来源")) {
                    dataRow.createCell(fieldIndex.get("classification_客户来源")).setCellValue(CUSTOMER_SOURCE_TEXT.getOrDefault(customer.getCustomerSource(), ""));
                }
                if (fieldIndex.containsKey("classification_重要程度")) {
                    dataRow.createCell(fieldIndex.get("classification_重要程度")).setCellValue(IMPORTANCE_TEXT.getOrDefault(customer.getImportance(), ""));
                }

                if (fieldIndex.containsKey("statistics_订单数")) {
                    dataRow.createCell(fieldIndex.get("statistics_订单数")).setCellValue(customer.getTotalOrders() != null ? customer.getTotalOrders() : 0);
                }
                if (fieldIndex.containsKey("statistics_消费额")) {
                    dataRow.createCell(fieldIndex.get("statistics_消费额")).setCellValue(customer.getTotalSpent() != null ? customer.getTotalSpent().doubleValue() : 0);
                }
                if (fieldIndex.containsKey("statistics_平均消费")) {
                    double avgSpent = 0;
                    if (customer.getTotalOrders() != null && customer.getTotalOrders() > 0 && customer.getTotalSpent() != null) {
                        avgSpent = customer.getTotalSpent().divide(BigDecimal.valueOf(customer.getTotalOrders()), 2, java.math.RoundingMode.HALF_UP).doubleValue();
                    }
                    dataRow.createCell(fieldIndex.get("statistics_平均消费")).setCellValue(avgSpent);
                }

                if (fieldIndex.containsKey("tags")) {
                    dataRow.createCell(fieldIndex.get("tags")).setCellValue(customerTagNames.getOrDefault(customer.getId(), ""));
                }

                if (fieldIndex.containsKey("preference")) {
                    dataRow.createCell(fieldIndex.get("preference")).setCellValue(customerPreferenceSummary.getOrDefault(customer.getId(), ""));
                }
            }

            for (int i = 0; i < colIdx; i++) {
                sheet.autoSizeColumn(i);
                int width = sheet.getColumnWidth(i);
                if (width < 3000) width = 3000;
                if (width > 15000) width = 15000;
                sheet.setColumnWidth(i, width);
            }

            saveOperationLog(null, "批量导出", 13, operatorId, operatorName,
                    "导出客户：" + filterDesc + "，共" + customers.size() + "条");

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出客户Excel失败：" + e.getMessage(), e);
        }
    }

    private Map<Long, String> loadCustomerTagNames() {
        Map<Long, String> result = new HashMap<>();

        List<CustomerTag> allTags = customerTagMapper.selectListByQuery(
                QueryWrapper.create().from(CustomerTag.class));
        Map<Long, String> tagIdToName = allTags.stream()
                .collect(Collectors.toMap(CustomerTag::getId, CustomerTag::getTagName, (a, b) -> a));

        List<CustomerTagRelation> allRelations = customerTagRelationMapper.selectListByQuery(
                QueryWrapper.create().from(CustomerTagRelation.class));

        Map<Long, List<Long>> customerToTagIds = new HashMap<>();
        for (CustomerTagRelation rel : allRelations) {
            customerToTagIds.computeIfAbsent(rel.getCustomerId(), k -> new ArrayList<>()).add(rel.getTagId());
        }

        for (Map.Entry<Long, List<Long>> entry : customerToTagIds.entrySet()) {
            List<String> tagNames = entry.getValue().stream()
                    .map(tagIdToName::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            result.put(entry.getKey(), String.join(",", tagNames));
        }

        return result;
    }

    private Map<Long, String> loadCustomerPreferenceSummary() {
        Map<Long, String> result = new HashMap<>();

        List<CustomerPreference> allPrefs = customerPreferenceMapper.selectListByQuery(
                QueryWrapper.create().from(CustomerPreference.class));

        for (CustomerPreference pref : allPrefs) {
            List<String> parts = new ArrayList<>();
            if (StringUtils.hasText(pref.getPreferredRoomType())) parts.add("房型:" + pref.getPreferredRoomType());
            if (StringUtils.hasText(pref.getPreferredFloor())) parts.add("楼层:" + pref.getPreferredFloor());
            if (StringUtils.hasText(pref.getPreferredBedType())) parts.add("床型:" + pref.getPreferredBedType());
            if (StringUtils.hasText(pref.getSpecialNeeds())) parts.add("特殊需求:" + pref.getSpecialNeeds());
            if (pref.getDietVegetarian() != null && pref.getDietVegetarian() == 1) parts.add("素食");
            if (pref.getDietHalal() != null && pref.getDietHalal() == 1) parts.add("清真");
            if (pref.getDietSeafoodAllergy() != null && pref.getDietSeafoodAllergy() == 1) parts.add("海鲜过敏");
            if (pref.getDietNoSpicy() != null && pref.getDietNoSpicy() == 1) parts.add("忌辣");

            if (!parts.isEmpty()) {
                result.put(pref.getCustomerId(), String.join(", ", parts));
            }
        }

        return result;
    }

    private String getCellStringValue(Row row, int colIdx) {
        Cell cell = row.getCell(colIdx);
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createInfoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private void saveOperationLog(Long customerId, String customerName, Integer operationType,
                                   Long operatorId, String operatorName, String remark) {
        CustomerOperationLog log = new CustomerOperationLog();
        log.setCustomerId(customerId);
        log.setCustomerName(customerName);
        log.setOperationType(operationType);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        customerOperationLogMapper.insert(log);
    }
}
