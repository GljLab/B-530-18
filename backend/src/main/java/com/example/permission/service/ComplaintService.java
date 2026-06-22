package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.permission.entity.table.ComplaintTableDef.COMPLAINT;
import static com.example.permission.entity.table.ComplaintImageTableDef.COMPLAINT_IMAGE;
import static com.example.permission.entity.table.ComplaintVisitTableDef.COMPLAINT_VISIT;
import static com.example.permission.entity.table.SysUserTableDef.SYS_USER;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintMapper complaintMapper;

    @Autowired
    private ComplaintImageMapper complaintImageMapper;

    @Autowired
    private ComplaintVisitMapper complaintVisitMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    private LoginUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException("用户未登录");
    }

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

    private Long safeToLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Number) return ((Number) obj).longValue();
        if (obj instanceof String) return Long.parseLong((String) obj);
        return Long.parseLong(obj.toString());
    }

    public PageResult<Complaint> getComplaintPage(
            Integer complaintStatus,
            String keyword,
            Integer complaintType,
            LocalDate startDate,
            LocalDate endDate,
            Boolean myTask,
            Long pageNum,
            Long pageSize) {

        LoginUser currentUser = getCurrentUser();

        QueryWrapper query = QueryWrapper.create()
                .where(COMPLAINT.DELETED.eq(0));

        if (complaintStatus != null) {
            query.and(COMPLAINT.COMPLAINT_STATUS.eq(complaintStatus));
        }

        if (complaintType != null) {
            query.and(COMPLAINT.COMPLAINT_TYPE.eq(complaintType));
        }

        if (keyword != null && !keyword.isEmpty()) {
            query.and(
                    COMPLAINT.CUSTOMER_NAME.like("%" + keyword + "%")
                            .or(COMPLAINT.CUSTOMER_PHONE.like("%" + keyword + "%"))
                            .or(COMPLAINT.COMPLAINT_NO.like("%" + keyword + "%"))
                            .or(COMPLAINT.CHECK_IN_NO.like("%" + keyword + "%"))
            );
        }

        if (startDate != null) {
            query.and(COMPLAINT.COMPLAINT_TIME.ge(startDate.atStartOfDay()));
        }

        if (endDate != null) {
            query.and(COMPLAINT.COMPLAINT_TIME.le(endDate.atTime(23, 59, 59)));
        }

        if (myTask != null && myTask) {
            query.and(COMPLAINT.ASSIGN_USER_ID.eq(currentUser.getUserId()));
        }

        query.orderBy(COMPLAINT.COMPLAINT_TIME.desc());

        Page<Complaint> page = complaintMapper.paginate(pageNum, pageSize, query);

        for (Complaint complaint : page.getRecords()) {
            loadComplaintRelations(complaint);
        }

        return new PageResult<>(page.getTotalRow(), page.getRecords(), page.getPageNumber(), page.getPageSize());
    }

    private void loadComplaintRelations(Complaint complaint) {
        if (complaint == null || complaint.getId() == null) return;

        QueryWrapper imageQuery = QueryWrapper.create()
                .where(COMPLAINT_IMAGE.COMPLAINT_ID.eq(complaint.getId()))
                .and(COMPLAINT_IMAGE.DELETED.eq(0))
                .orderBy(COMPLAINT_IMAGE.SORT_ORDER.asc());
        complaint.setImages(complaintImageMapper.selectListByQuery(imageQuery));

        QueryWrapper visitQuery = QueryWrapper.create()
                .where(COMPLAINT_VISIT.COMPLAINT_ID.eq(complaint.getId()))
                .and(COMPLAINT_VISIT.DELETED.eq(0))
                .orderBy(COMPLAINT_VISIT.VISIT_TIME.desc());
        complaint.setVisits(complaintVisitMapper.selectListByQuery(visitQuery));
    }

    public Complaint getComplaintDetail(Long id) {
        Complaint complaint = complaintMapper.selectOneById(id);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }
        loadComplaintRelations(complaint);
        return complaint;
    }

    public List<SysUser> getAvailableStaff() {
        QueryWrapper query = QueryWrapper.create()
                .where(SYS_USER.STATUS.eq(1))
                .and(SYS_USER.DELETED.eq(0))
                .orderBy(SYS_USER.USERNAME.asc());
        return sysUserMapper.selectListByQuery(query);
    }

    @Transactional
    public void acceptComplaint(Long id, Long assignUserId, String acceptRemark) {
        LoginUser currentUser = getCurrentUser();
        Complaint complaint = complaintMapper.selectOneById(id);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }
        if (complaint.getComplaintStatus() != 1) {
            throw new BusinessException("只有待处理的投诉才能受理");
        }
        if (assignUserId == null) {
            throw new BusinessException("请分配责任人");
        }
        if (acceptRemark == null || acceptRemark.isEmpty()) {
            throw new BusinessException("请填写受理意见");
        }
        if (acceptRemark.length() > 200) {
            throw new BusinessException("受理意见不能超过200字");
        }

        SysUser assignUser = sysUserMapper.selectOneById(assignUserId);
        if (assignUser == null) {
            throw new BusinessException("责任人不存在");
        }

        complaint.setComplaintStatus(2);
        complaint.setAcceptUserId(currentUser.getUserId());
        complaint.setAcceptUserName(currentUser.getUser().getNickname() != null ? currentUser.getUser().getNickname() : currentUser.getUsername());
        complaint.setAcceptTime(LocalDateTime.now());
        complaint.setAcceptRemark(acceptRemark);
        complaint.setAssignUserId(assignUserId);
        complaint.setAssignUserName(assignUser.getNickname() != null ? assignUser.getNickname() : assignUser.getUsername());
        complaintMapper.update(complaint);
    }

    @Transactional
    public void rejectComplaint(Long id, Integer rejectReason, String rejectRemark) {
        LoginUser currentUser = getCurrentUser();
        Complaint complaint = complaintMapper.selectOneById(id);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }
        if (complaint.getComplaintStatus() != 1) {
            throw new BusinessException("只有待处理的投诉才能驳回");
        }
        if (rejectReason == null) {
            throw new BusinessException("请选择驳回原因");
        }
        if (rejectRemark == null || rejectRemark.isEmpty()) {
            throw new BusinessException("请填写详细说明");
        }
        if (rejectRemark.length() > 200) {
            throw new BusinessException("详细说明不能超过200字");
        }

        complaint.setComplaintStatus(4);
        complaint.setRejectReason(rejectReason);
        complaint.setRejectRemark(rejectRemark);
        complaint.setRejectUserId(currentUser.getUserId());
        complaint.setRejectUserName(currentUser.getUser().getNickname() != null ? currentUser.getUser().getNickname() : currentUser.getUsername());
        complaint.setRejectTime(LocalDateTime.now());
        complaintMapper.update(complaint);
    }

    @Transactional
    public void handleComplaint(Long id, String handleSolution, Integer handleResult,
                                String compensationPlan, String handleRemark) {
        LoginUser currentUser = getCurrentUser();
        Complaint complaint = complaintMapper.selectOneById(id);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }
        if (complaint.getComplaintStatus() != 2) {
            throw new BusinessException("只有处理中的投诉才能处理");
        }
        if (!Objects.equals(complaint.getAssignUserId(), currentUser.getUserId())) {
            throw new BusinessException("只有分配的责任人才能处理该投诉");
        }
        if (handleSolution == null || handleSolution.isEmpty()) {
            throw new BusinessException("请填写处理方案");
        }
        if (handleSolution.length() > 500) {
            throw new BusinessException("处理方案不能超过500字");
        }
        if (handleResult == null) {
            throw new BusinessException("请选择处理结果");
        }
        if (compensationPlan != null && compensationPlan.length() > 100) {
            throw new BusinessException("补偿方案不能超过100字");
        }
        if (handleRemark != null && handleRemark.length() > 200) {
            throw new BusinessException("处理备注不能超过200字");
        }

        complaint.setComplaintStatus(3);
        complaint.setHandleSolution(handleSolution);
        complaint.setHandleResult(handleResult);
        complaint.setCompensationPlan(compensationPlan);
        complaint.setHandleRemark(handleRemark);
        complaint.setHandleUserId(currentUser.getUserId());
        complaint.setHandleUserName(currentUser.getUser().getNickname() != null ? currentUser.getUser().getNickname() : currentUser.getUsername());
        complaint.setHandleTime(LocalDateTime.now());
        complaint.setNeedReprocess(0);
        complaintMapper.update(complaint);
    }

    @Transactional
    public void addVisit(Long id, LocalDateTime visitTime, Integer visitMethod,
                         Integer satisfaction, String visitRemark, Integer needReprocess) {
        LoginUser currentUser = getCurrentUser();
        Complaint complaint = complaintMapper.selectOneById(id);
        if (complaint == null) {
            throw new BusinessException("投诉记录不存在");
        }
        if (complaint.getComplaintStatus() != 3) {
            throw new BusinessException("只有已处理的投诉才能回访");
        }
        if (visitTime == null) {
            visitTime = LocalDateTime.now();
        }
        if (visitMethod == null) {
            throw new BusinessException("请选择回访方式");
        }
        if (satisfaction == null) {
            throw new BusinessException("请选择客人满意度");
        }
        if (visitRemark == null || visitRemark.isEmpty()) {
            throw new BusinessException("请填写回访备注");
        }
        if (visitRemark.length() > 200) {
            throw new BusinessException("回访备注不能超过200字");
        }

        ComplaintVisit visit = new ComplaintVisit();
        visit.setComplaintId(id);
        visit.setVisitTime(visitTime);
        visit.setVisitMethod(visitMethod);
        visit.setSatisfaction(satisfaction);
        visit.setVisitRemark(visitRemark);
        visit.setVisitUserId(currentUser.getUserId());
        visit.setVisitUserName(currentUser.getUser().getNickname() != null ? currentUser.getUser().getNickname() : currentUser.getUsername());
        visit.setNeedReprocess(needReprocess != null ? needReprocess : 0);
        visit.setDeleted(0);
        complaintVisitMapper.insert(visit);

        if (needReprocess != null && needReprocess == 1) {
            complaint.setNeedReprocess(1);
            complaint.setComplaintStatus(2);
            complaintMapper.update(complaint);
        }
    }

    public List<ComplaintVisit> getVisitsByComplaintId(Long complaintId) {
        QueryWrapper query = QueryWrapper.create()
                .where(COMPLAINT_VISIT.COMPLAINT_ID.eq(complaintId))
                .and(COMPLAINT_VISIT.DELETED.eq(0))
                .orderBy(COMPLAINT_VISIT.VISIT_TIME.desc());
        return complaintVisitMapper.selectListByQuery(query);
    }
}
