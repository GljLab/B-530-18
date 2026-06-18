package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.RefundApply;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.CheckInMapper;
import com.example.permission.mapper.RefundApplyMapper;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import static com.example.permission.entity.table.RefundApplyTableDef.REFUND_APPLY;

@Service
public class RefundApplyService {

    @Autowired
    private RefundApplyMapper refundApplyMapper;

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private CheckInMapper checkInMapper;

    public PageResult<RefundApply> getPage(Integer pageNum, Integer pageSize, Integer status, Integer refundType, String keyword) {
        QueryWrapper query = QueryWrapper.create()
                .from(RefundApply.class)
                .where(REFUND_APPLY.ID.isNotNull());
        if (status != null) {
            query.and(REFUND_APPLY.STATUS.eq(status));
        }
        if (refundType != null) {
            query.and(REFUND_APPLY.REFUND_TYPE.eq(refundType));
        }
        if (StringUtils.hasText(keyword)) {
            query.and(REFUND_APPLY.CUSTOMER_NAME.like(keyword)
                    .or(REFUND_APPLY.CUSTOMER_PHONE.like(keyword))
                    .or(REFUND_APPLY.RELATED_ORDER_NO.like(keyword)));
        }
        query.orderBy(REFUND_APPLY.CREATE_TIME.desc());
        Page<RefundApply> page = refundApplyMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public RefundApply getById(Long id) {
        RefundApply refundApply = refundApplyMapper.selectOneById(id);
        if (refundApply == null) {
            throw new BusinessException("退款申请不存在");
        }
        return refundApply;
    }

    @Transactional(rollbackFor = Exception.class)
    public void apply(RefundApply refundApply, LoginUser loginUser) {
        if (refundApply.getRefundAmount() == null || refundApply.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("退款金额必须大于0");
        }
        String refundNo = generateRefundNo();
        refundApply.setRefundNo(refundNo);
        refundApply.setApplicantId(loginUser.getUserId());
        refundApply.setApplicantName(loginUser.getUser().getNickname() != null ? loginUser.getUser().getNickname() : loginUser.getUsername());
        refundApply.setApplyTime(LocalDateTime.now());
        refundApply.setStatus(0);
        refundApply.setCreateTime(LocalDateTime.now());
        refundApplyMapper.insert(refundApply);
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, Boolean approved, String approveRemark, BigDecimal approvedAmount, LoginUser loginUser) {
        RefundApply refundApply = getById(id);
        if (refundApply.getStatus() != 0) {
            throw new BusinessException("该退款申请已处理，不可重复审批");
        }
        refundApply.setApproverId(loginUser.getUserId());
        refundApply.setApproverName(loginUser.getUser().getNickname() != null ? loginUser.getUser().getNickname() : loginUser.getUsername());
        refundApply.setApproveTime(LocalDateTime.now());
        refundApply.setApproveRemark(approveRemark);
        if (approved) {
            refundApply.setStatus(1);
            refundApply.setApprovedAmount(approvedAmount != null ? approvedAmount : refundApply.getRefundAmount());
        } else {
            refundApply.setStatus(2);
            refundApply.setRejectReason(approveRemark);
        }
        refundApply.setUpdateTime(LocalDateTime.now());
        refundApplyMapper.update(refundApply);
    }

    @Transactional(rollbackFor = Exception.class)
    public void executeRefund(Long id, Integer refundMethod, String refundVoucherNo, LoginUser loginUser) {
        RefundApply refundApply = getById(id);
        if (refundApply.getStatus() != 1) {
            throw new BusinessException("只有审批通过的退款申请才能执行退款");
        }
        refundApply.setRefundMethod(refundMethod);
        refundApply.setRefundVoucherNo(refundVoucherNo);
        refundApply.setRefundTime(LocalDateTime.now());
        refundApply.setStatus(3);
        refundApply.setUpdateTime(LocalDateTime.now());
        refundApplyMapper.update(refundApply);
    }

    public String generateRefundNo() {
        String prefix = "RA" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        String suffix = String.format("%04d", random.nextInt(10000));
        String refundNo = prefix + suffix;
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(RefundApply.class)
                .where(REFUND_APPLY.REFUND_NO.eq(refundNo));
        long count = refundApplyMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            return generateRefundNo();
        }
        return refundNo;
    }
}
