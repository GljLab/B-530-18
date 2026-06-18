package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ShiftReconciliationTableDef extends TableDef {

    public static final ShiftReconciliationTableDef SHIFT_RECONCILIATION = new ShiftReconciliationTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn SHIFT_DATE = new QueryColumn(this, "shift_date");
    public final QueryColumn SHIFT_TYPE = new QueryColumn(this, "shift_type");
    public final QueryColumn HANDOVER_USER_ID = new QueryColumn(this, "handover_user_id");
    public final QueryColumn HANDOVER_USER_NAME = new QueryColumn(this, "handover_user_name");
    public final QueryColumn TAKEOVER_USER_ID = new QueryColumn(this, "takeover_user_id");
    public final QueryColumn TAKEOVER_USER_NAME = new QueryColumn(this, "takeover_user_name");
    public final QueryColumn CASH_TOTAL = new QueryColumn(this, "cash_total");
    public final QueryColumn CARD_TOTAL = new QueryColumn(this, "card_total");
    public final QueryColumn MOBILE_TOTAL = new QueryColumn(this, "mobile_total");
    public final QueryColumn CREDIT_TOTAL = new QueryColumn(this, "credit_total");
    public final QueryColumn RECEIVABLE_TOTAL = new QueryColumn(this, "receivable_total");
    public final QueryColumn ACTUAL_CASH = new QueryColumn(this, "actual_cash");
    public final QueryColumn ACTUAL_CARD = new QueryColumn(this, "actual_card");
    public final QueryColumn ACTUAL_MOBILE = new QueryColumn(this, "actual_mobile");
    public final QueryColumn ACTUAL_TOTAL = new QueryColumn(this, "actual_total");
    public final QueryColumn DIFFERENCE = new QueryColumn(this, "difference");
    public final QueryColumn DIFFERENCE_REASON = new QueryColumn(this, "difference_reason");
    public final QueryColumn HANDOVER_REMARK = new QueryColumn(this, "handover_remark");
    public final QueryColumn TAKEOVER_REMARK = new QueryColumn(this, "takeover_remark");
    public final QueryColumn TAKEOVER_CONFIRMED = new QueryColumn(this, "takeover_confirmed");
    public final QueryColumn TAKEOVER_CONFIRM_TIME = new QueryColumn(this, "takeover_confirm_time");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, SHIFT_DATE, SHIFT_TYPE, HANDOVER_USER_ID, HANDOVER_USER_NAME, TAKEOVER_USER_ID, TAKEOVER_USER_NAME, CASH_TOTAL, CARD_TOTAL, MOBILE_TOTAL, CREDIT_TOTAL, RECEIVABLE_TOTAL, ACTUAL_CASH, ACTUAL_CARD, ACTUAL_MOBILE, ACTUAL_TOTAL, DIFFERENCE, DIFFERENCE_REASON, HANDOVER_REMARK, TAKEOVER_REMARK, TAKEOVER_CONFIRMED, TAKEOVER_CONFIRM_TIME, STATUS, CREATE_TIME, UPDATE_TIME};

    public ShiftReconciliationTableDef() {
        super("", "shift_reconciliation");
    }
}
