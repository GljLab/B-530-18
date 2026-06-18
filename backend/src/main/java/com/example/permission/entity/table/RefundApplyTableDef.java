package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class RefundApplyTableDef extends TableDef {

    public static final RefundApplyTableDef REFUND_APPLY = new RefundApplyTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn REFUND_NO = new QueryColumn(this, "refund_no");
    public final QueryColumn RELATED_ORDER_TYPE = new QueryColumn(this, "related_order_type");
    public final QueryColumn RELATED_ORDER_ID = new QueryColumn(this, "related_order_id");
    public final QueryColumn RELATED_ORDER_NO = new QueryColumn(this, "related_order_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn CUSTOMER_PHONE = new QueryColumn(this, "customer_phone");
    public final QueryColumn REFUND_TYPE = new QueryColumn(this, "refund_type");
    public final QueryColumn REFUND_AMOUNT = new QueryColumn(this, "refund_amount");
    public final QueryColumn APPROVED_AMOUNT = new QueryColumn(this, "approved_amount");
    public final QueryColumn REFUND_REASON = new QueryColumn(this, "refund_reason");
    public final QueryColumn PROOF_MATERIALS = new QueryColumn(this, "proof_materials");
    public final QueryColumn APPLICANT_ID = new QueryColumn(this, "applicant_id");
    public final QueryColumn APPLICANT_NAME = new QueryColumn(this, "applicant_name");
    public final QueryColumn APPLY_TIME = new QueryColumn(this, "apply_time");
    public final QueryColumn APPROVER_ID = new QueryColumn(this, "approver_id");
    public final QueryColumn APPROVER_NAME = new QueryColumn(this, "approver_name");
    public final QueryColumn APPROVE_TIME = new QueryColumn(this, "approve_time");
    public final QueryColumn APPROVE_REMARK = new QueryColumn(this, "approve_remark");
    public final QueryColumn REJECT_REASON = new QueryColumn(this, "reject_reason");
    public final QueryColumn REFUND_METHOD = new QueryColumn(this, "refund_method");
    public final QueryColumn REFUND_VOUCHER_NO = new QueryColumn(this, "refund_voucher_no");
    public final QueryColumn REFUND_TIME = new QueryColumn(this, "refund_time");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REFUND_NO, RELATED_ORDER_TYPE, RELATED_ORDER_ID, RELATED_ORDER_NO, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_PHONE, REFUND_TYPE, REFUND_AMOUNT, APPROVED_AMOUNT, REFUND_REASON, PROOF_MATERIALS, APPLICANT_ID, APPLICANT_NAME, APPLY_TIME, APPROVER_ID, APPROVER_NAME, APPROVE_TIME, APPROVE_REMARK, REJECT_REASON, REFUND_METHOD, REFUND_VOUCHER_NO, REFUND_TIME, STATUS, CREATE_TIME, UPDATE_TIME};

    public RefundApplyTableDef() {
        super("", "refund_apply");
    }
}
