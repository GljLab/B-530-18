package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.Booking;
import com.example.permission.entity.RefundRecord;
import com.example.permission.mapper.BookingMapper;
import com.example.permission.mapper.RefundRecordMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomTypeMapper;
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

import static com.example.permission.entity.table.BookingTableDef.BOOKING;
import static com.example.permission.entity.table.RefundRecordTableDef.REFUND_RECORD;

@Service
public class RefundRecordService {

    @Autowired
    private BookingMapper bookingMapper;

    @Autowired
    private RefundRecordMapper refundRecordMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    public PageResult<RefundRecord> getRefundPage(Integer pageNum, Integer pageSize, Integer status,
                                                   String bookingNo, String customerName) {
        QueryWrapper query = QueryWrapper.create()
                .from(RefundRecord.class)
                .where(REFUND_RECORD.STATUS.isNotNull());
        if (status != null) {
            query.and(REFUND_RECORD.STATUS.eq(status));
        }
        if (StringUtils.hasText(bookingNo)) {
            query.and(REFUND_RECORD.BOOKING_NO.like(bookingNo));
        }
        if (StringUtils.hasText(customerName)) {
            query.and(REFUND_RECORD.CUSTOMER_NAME.like(customerName));
        }
        query.orderBy(REFUND_RECORD.CREATE_TIME.desc());
        Page<RefundRecord> page = refundRecordMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public RefundRecord getRefundDetail(Long id) {
        RefundRecord refund = refundRecordMapper.selectOneById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        return refund;
    }

    @Transactional(rollbackFor = Exception.class)
    public void approveRefund(Long id, Boolean approved, String approveRemark, LoginUser loginUser) {
        RefundRecord refund = refundRecordMapper.selectOneById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (refund.getStatus() != 0) {
            throw new BusinessException("该退款申请已处理，不可重复审批");
        }
        refund.setApproverId(loginUser.getUserId());
        refund.setApproverName(loginUser.getUser().getNickname() != null ? loginUser.getUser().getNickname() : loginUser.getUsername());
        refund.setApproveTime(LocalDateTime.now());
        refund.setApproveRemark(approveRemark);
        refund.setUpdateTime(LocalDateTime.now());
        if (approved) {
            refund.setStatus(1);
        } else {
            refund.setStatus(2);
            refund.setActualRefundAmount(BigDecimal.ZERO);
        }
        refundRecordMapper.update(refund);
    }

    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long id, LoginUser loginUser) {
        RefundRecord refund = refundRecordMapper.selectOneById(id);
        if (refund == null) {
            throw new BusinessException("退款记录不存在");
        }
        if (refund.getStatus() != 1) {
            throw new BusinessException("只有审批通过的退款申请才能执行退款");
        }
        refund.setStatus(3);
        refund.setRefundTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        refundRecordMapper.update(refund);
        Booking booking = bookingMapper.selectOneById(refund.getBookingId());
        if (booking != null) {
            booking.setStatus(7);
            booking.setUpdateTime(LocalDateTime.now());
            bookingMapper.update(booking);
        }
    }

    public String generateRefundNo() {
        String prefix = "RF" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Random random = new Random();
        String suffix = String.format("%04d", random.nextInt(10000));
        String refundNo = prefix + suffix;
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(RefundRecord.class)
                .where(REFUND_RECORD.REFUND_NO.eq(refundNo));
        long count = refundRecordMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            return generateRefundNo();
        }
        return refundNo;
    }

    public BigDecimal calculateRefundAmount(Booking booking, String refundReason) {
        if (booking == null) {
            throw new BusinessException("预订信息不存在");
        }
        BigDecimal paidAmount = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal cancelPenalty = booking.getCancelPenalty() != null ? booking.getCancelPenalty() : BigDecimal.ZERO;
        if ("取消预订".equals(refundReason)) {
            return paidAmount.subtract(cancelPenalty);
        }
        if ("提前退房".equals(refundReason)) {
            return paidAmount.multiply(new BigDecimal("0.8"));
        }
        return paidAmount;
    }
}
