package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class RefundRecordTableDef extends TableDef {

    public static final RefundRecordTableDef REFUND_RECORD = new RefundRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn REFUND_NO = new QueryColumn(this, "refund_no");

    public final QueryColumn BOOKING_ID = new QueryColumn(this, "booking_id");

    public final QueryColumn BOOKING_NO = new QueryColumn(this, "booking_no");

    public final QueryColumn APPROVER_ID = new QueryColumn(this, "approver_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn PAID_AMOUNT = new QueryColumn(this, "paid_amount");

    public final QueryColumn REFUND_TIME = new QueryColumn(this, "refund_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn APPLICANT_ID = new QueryColumn(this, "applicant_id");

    public final QueryColumn APPROVE_TIME = new QueryColumn(this, "approve_time");

    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");

    public final QueryColumn APPROVER_NAME = new QueryColumn(this, "approver_name");

    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");

    public final QueryColumn REFUND_METHOD = new QueryColumn(this, "refund_method");

    public final QueryColumn REFUND_REASON = new QueryColumn(this, "refund_reason");

    public final QueryColumn APPLICANT_NAME = new QueryColumn(this, "applicant_name");

    public final QueryColumn APPROVE_REMARK = new QueryColumn(this, "approve_remark");

    public final QueryColumn DEDUCTION_AMOUNT = new QueryColumn(this, "deduction_amount");

    public final QueryColumn REFUNDABLE_AMOUNT = new QueryColumn(this, "refundable_amount");

    public final QueryColumn ACTUAL_REFUND_AMOUNT = new QueryColumn(this, "actual_refund_amount");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STATUS, REFUND_NO, BOOKING_ID, BOOKING_NO, APPROVER_ID, CREATE_TIME, CUSTOMER_ID, PAID_AMOUNT, REFUND_TIME, UPDATE_TIME, APPLICANT_ID, APPROVE_TIME, TOTAL_AMOUNT, APPROVER_NAME, CUSTOMER_NAME, REFUND_METHOD, REFUND_REASON, APPLICANT_NAME, APPROVE_REMARK, DEDUCTION_AMOUNT, REFUNDABLE_AMOUNT, ACTUAL_REFUND_AMOUNT};

    public RefundRecordTableDef() {
        super("", "refund_record");
    }

}
