package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CreditSettlementTableDef extends TableDef {

    public static final CreditSettlementTableDef CREDIT_SETTLEMENT = new CreditSettlementTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn SETTLEMENT_NO = new QueryColumn(this, "settlement_no");
    public final QueryColumn AGREEMENT_UNIT_ID = new QueryColumn(this, "agreement_unit_id");
    public final QueryColumn AGREEMENT_UNIT_NAME = new QueryColumn(this, "agreement_unit_name");
    public final QueryColumn PERIOD_START = new QueryColumn(this, "period_start");
    public final QueryColumn PERIOD_END = new QueryColumn(this, "period_end");
    public final QueryColumn BILL_COUNT = new QueryColumn(this, "bill_count");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn SETTLEMENT_METHOD = new QueryColumn(this, "settlement_method");
    public final QueryColumn SETTLEMENT_DATE = new QueryColumn(this, "settlement_date");
    public final QueryColumn VOUCHER_NO = new QueryColumn(this, "voucher_no");
    public final QueryColumn INVOICE_NO = new QueryColumn(this, "invoice_no");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, SETTLEMENT_NO, AGREEMENT_UNIT_ID, AGREEMENT_UNIT_NAME, PERIOD_START, PERIOD_END, BILL_COUNT, TOTAL_AMOUNT, SETTLEMENT_METHOD, SETTLEMENT_DATE, VOUCHER_NO, INVOICE_NO, OPERATOR_ID, OPERATOR_NAME, STATUS, REMARK, CREATE_TIME, UPDATE_TIME};

    public CreditSettlementTableDef() {
        super("", "credit_settlement");
    }
}
