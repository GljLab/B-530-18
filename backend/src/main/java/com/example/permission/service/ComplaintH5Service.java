package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.permission.entity.table.ComplaintTableDef.COMPLAINT;
import static com.example.permission.entity.table.ComplaintImageTableDef.COMPLAINT_IMAGE;
import static com.example.permission.entity.table.CheckInTableDef.CHECK_IN;

@Service
public class ComplaintH5Service {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private ComplaintImageMapper complaintImageMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private HotelInfoMapper hotelInfoMapper;

    private String safeToString(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String) return (String) obj;
        return obj.toString();
    }

    private Integer safeToInteger(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return (Integer) obj;
        if (obj instanceof Number) return ((Number) obj).intValue();
        if (obj instanceof String) return Integer.parseInt((String) obj);
        return Integer.parseInt(obj.toString());
    }

    private String generateComplaintNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "CP" + datePart;
        QueryWrapper query = QueryWrapper.create()
                .select(COMPLAINT.COMPLAINT_NO)
                .where(COMPLAINT.COMPLAINT_NO.like(prefix + "%"))
                .orderBy(COMPLAINT.COMPLAINT_NO.desc())
                .limit(1);
        Complaint last = complaintMapper.selectOneByQuery(query);
        int seq = 1;
        if (last != null && last.getComplaintNo() != null) {
            String lastSeq = last.getComplaintNo().substring(prefix.length());
            try {
                seq = Integer.parseInt(lastSeq) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String generateVerifyCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public Map<String, Object> getComplaintPageData(String checkInNo, String code) {
        Map<String, Object> result = new HashMap<>();

        if (checkInNo == null || checkInNo.isEmpty() || code == null || code.isEmpty()) {
            throw new BusinessException("参数不完整");
        }

        QueryWrapper checkInQuery = QueryWrapper.create()
                .where(CHECK_IN.CHECK_IN_NO.eq(checkInNo))
                .and(CHECK_IN.DELETED.eq(0));
        CheckIn checkIn = checkInMapper.selectOneByQuery(checkInQuery);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }

        List<HotelInfo> hotelInfos = hotelInfoMapper.selectAll();
        HotelInfo hotelInfo = hotelInfos.isEmpty() ? null : hotelInfos.get(0);

        result.put("hotelInfo", hotelInfo);
        result.put("checkIn", checkIn);
        result.put("code", code);
        return result;
    }

    @Transactional
    public Long submitComplaint(Map<String, Object> data) {
        String checkInNo = safeToString(data.get("checkInNo"));
        String code = safeToString(data.get("code"));
        Integer complaintType = safeToInteger(data.get("complaintType"));
        String complaintContent = safeToString(data.get("complaintContent"));
        String expectedSolution = safeToString(data.get("expectedSolution"));
        String customerPhone = safeToString(data.get("customerPhone"));
        String customerEmail = safeToString(data.get("customerEmail"));
        String customerName = safeToString(data.get("customerName"));
        List<Map<String, Object>> images = (List<Map<String, Object>>) data.get("images");

        if (checkInNo == null || checkInNo.isEmpty()) {
            throw new BusinessException("入住单号不能为空");
        }
        if (complaintType == null) {
            throw new BusinessException("请选择投诉类型");
        }
        if (complaintContent == null || complaintContent.isEmpty()) {
            throw new BusinessException("请填写投诉内容");
        }
        if (complaintContent.length() > 500) {
            throw new BusinessException("投诉内容不能超过500字");
        }
        if (expectedSolution != null && expectedSolution.length() > 100) {
            throw new BusinessException("期望解决方案不能超过100字");
        }
        if (customerPhone == null || customerPhone.isEmpty()) {
            throw new BusinessException("请填写手机号");
        }
        if (!customerPhone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        if (images != null && images.size() > 6) {
            throw new BusinessException("最多上传6张图片");
        }

        QueryWrapper checkInQuery = QueryWrapper.create()
                .where(CHECK_IN.CHECK_IN_NO.eq(checkInNo))
                .and(CHECK_IN.DELETED.eq(0));
        CheckIn checkIn = checkInMapper.selectOneByQuery(checkInQuery);
        if (checkIn == null) {
            throw new BusinessException("入住单不存在");
        }

        Complaint complaint = new Complaint();
        complaint.setComplaintNo(generateComplaintNo());
        complaint.setCheckInId(checkIn.getId());
        complaint.setCheckInNo(checkInNo);
        complaint.setCustomerId(checkIn.getCustomerId());
        complaint.setCustomerName(customerName != null ? customerName : checkIn.getCustomerName());
        complaint.setCustomerPhone(customerPhone);
        complaint.setCustomerEmail(customerEmail);
        complaint.setRoomTypeId(checkIn.getRoomTypeId());
        complaint.setRoomTypeName(checkIn.getRoomTypeName());
        complaint.setCheckInDate(checkIn.getCheckInDate());
        complaint.setCheckOutDate(checkIn.getCheckOutDate());
        complaint.setComplaintType(complaintType);
        complaint.setComplaintContent(complaintContent);
        complaint.setExpectedSolution(expectedSolution);
        complaint.setComplaintStatus(1);
        complaint.setVerifyCode(generateVerifyCode());
        complaint.setNeedReprocess(0);
        complaint.setComplaintTime(LocalDateTime.now());
        complaint.setDeleted(0);
        complaintMapper.insert(complaint);

        if (images != null && !images.isEmpty()) {
            int sortOrder = 0;
            for (Map<String, Object> img : images) {
                ComplaintImage ci = new ComplaintImage();
                ci.setComplaintId(complaint.getId());
                ci.setImageUrl(safeToString(img.get("imageUrl")));
                ci.setImageName(safeToString(img.get("imageName")));
                ci.setSortOrder(sortOrder++);
                ci.setDeleted(0);
                complaintImageMapper.insert(ci);
            }
        }

        return complaint.getId();
    }

    public Map<String, Object> getComplaintSuccessData(Long id) {
        Map<String, Object> result = new HashMap<>();
        Complaint complaint = complaintMapper.selectOneById(id);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }
        result.put("complaintNo", complaint.getComplaintNo());
        result.put("complaintTime", complaint.getComplaintTime());
        return result;
    }
}
