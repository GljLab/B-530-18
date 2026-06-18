package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class DailyReconciliationTableDef extends TableDef {

    public static final DailyReconciliationTableDef DAILY_RECONCILIATION = new DailyReconciliationTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn RECONCILE_DATE = new QueryColumn(this, "reconcile_date");
    public final QueryColumn CASH_ROOM = new QueryColumn(this, "cash_room");
    public final QueryColumn CASH_DEPOSIT = new QueryColumn(this, "cash_deposit");
    public final QueryColumn CASH_OTHER = new QueryColumn(this, "cash_other");
    public final QueryColumn CASH_TOTAL = new QueryColumn(this, "cash_total");
    public final QueryColumn CARD_ROOM = new QueryColumn(this, "card_room");
    public final QueryColumn CARD_DEPOSIT = new QueryColumn(this, "card_deposit");
    public final QueryColumn CARD_OTHER = new QueryColumn(this, "card_other");
    public final QueryColumn CARD_TOTAL = new QueryColumn(this, "card_total");
    public final QueryColumn MOBILE_ALIPAY = new QueryColumn(this, "mobile_alipay");
    public final QueryColumn MOBILE_WECHAT = new QueryColumn(this, "mobile_wechat");
    public final QueryColumn MOBILE_TOTAL = new QueryColumn(this, "mobile_total");
    public final QueryColumn CREDIT_AMOUNT = new QueryColumn(this, "credit_amount");
    public final QueryColumn PREPAID_AMOUNT = new QueryColumn(this, "prepaid_amount");
    public final QueryColumn REFUND_AMOUNT = new QueryColumn(this, "refund_amount");
    public final QueryColumn RECEIVABLE_TOTAL = new QueryColumn(this, "receivable_total");
    public final QueryColumn ACTUAL_CASH = new QueryColumn(this, "actual_cash");
    public final QueryColumn ACTUAL_CARD = new QueryColumn(this, "actual_card");
    public final QueryColumn ACTUAL_MOBILE = new QueryColumn(this, "actual_mobile");
    public final QueryColumn ACTUAL_TOTAL = new QueryColumn(this, "actual_total");
    public final QueryColumn DIFFERENCE = new QueryColumn(this, "difference");
    public final QueryColumn DIFFERENCE_REASON = new QueryColumn(this, "difference_reason");
    public final QueryColumn DIFFERENCE_PROOF = new QueryColumn(this, "difference_proof");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, RECONCILE_DATE, CASH_ROOM, CASH_DEPOSIT, CASH_OTHER, CASH_TOTAL, CARD_ROOM, CARD_DEPOSIT, CARD_OTHER, CARD_TOTAL, MOBILE_ALIPAY, MOBILE_WECHAT, MOBILE_TOTAL, CREDIT_AMOUNT, PREPAID_AMOUNT, REFUND_AMOUNT, RECEIVABLE_TOTAL, ACTUAL_CASH, ACTUAL_CARD, ACTUAL_MOBILE, ACTUAL_TOTAL, DIFFERENCE, DIFFERENCE_REASON, DIFFERENCE_PROOF, OPERATOR_ID, OPERATOR_NAME, STATUS, CREATE_TIME, UPDATE_TIME};

    public DailyReconciliationTableDef() {
        super("", "daily_reconciliation");
    }
}
