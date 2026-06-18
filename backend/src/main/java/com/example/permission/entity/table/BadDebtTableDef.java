package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BadDebtTableDef extends TableDef {

    public static final BadDebtTableDef BAD_DEBT = new BadDebtTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn DEBT_NO = new QueryColumn(this, "debt_no");
    public final QueryColumn RELATED_ORDER_TYPE = new QueryColumn(this, "related_order_type");
    public final QueryColumn RELATED_ORDER_ID = new QueryColumn(this, "related_order_id");
    public final QueryColumn RELATED_ORDER_NO = new QueryColumn(this, "related_order_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn DEBT_TYPE = new QueryColumn(this, "debt_type");
    public final QueryColumn ORIGINAL_AMOUNT = new QueryColumn(this, "original_amount");
    public final QueryColumn RECOVERED_AMOUNT = new QueryColumn(this, "recovered_amount");
    public final QueryColumn REMAINING_AMOUNT = new QueryColumn(this, "remaining_amount");
    public final QueryColumn DEBT_TIME = new QueryColumn(this, "debt_time");
    public final QueryColumn OVERDUE_DAYS = new QueryColumn(this, "overdue_days");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn WRITE_OFF_REASON = new QueryColumn(this, "write_off_reason");
    public final QueryColumn WRITE_OFF_PROOF = new QueryColumn(this, "write_off_proof");
    public final QueryColumn WRITE_OFF_APPROVER_ID = new QueryColumn(this, "write_off_approver_id");
    public final QueryColumn WRITE_OFF_APPROVER_NAME = new QueryColumn(this, "write_off_approver_name");
    public final QueryColumn WRITE_OFF_APPROVE_TIME = new QueryColumn(this, "write_off_approve_time");
    public final QueryColumn LEGAL_STATUS = new QueryColumn(this, "legal_status");
    public final QueryColumn LEGAL_INFO = new QueryColumn(this, "legal_info");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, DEBT_NO, RELATED_ORDER_TYPE, RELATED_ORDER_ID, RELATED_ORDER_NO, CUSTOMER_ID, CUSTOMER_NAME, DEBT_TYPE, ORIGINAL_AMOUNT, RECOVERED_AMOUNT, REMAINING_AMOUNT, DEBT_TIME, OVERDUE_DAYS, STATUS, WRITE_OFF_REASON, WRITE_OFF_PROOF, WRITE_OFF_APPROVER_ID, WRITE_OFF_APPROVER_NAME, WRITE_OFF_APPROVE_TIME, LEGAL_STATUS, LEGAL_INFO, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public BadDebtTableDef() {
        super("", "bad_debt");
    }
}
