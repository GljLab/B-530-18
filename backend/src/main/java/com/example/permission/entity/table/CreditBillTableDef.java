package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CreditBillTableDef extends TableDef {

    public static final CreditBillTableDef CREDIT_BILL = new CreditBillTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn BILL_NO = new QueryColumn(this, "bill_no");
    public final QueryColumn AGREEMENT_UNIT_ID = new QueryColumn(this, "agreement_unit_id");
    public final QueryColumn AGREEMENT_UNIT_NAME = new QueryColumn(this, "agreement_unit_name");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");
    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");
    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");
    public final QueryColumn ROOM_FEE = new QueryColumn(this, "room_fee");
    public final QueryColumn EXTRA_FEE = new QueryColumn(this, "extra_fee");
    public final QueryColumn DISCOUNT_AMOUNT = new QueryColumn(this, "discount_amount");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn BILL_TIME = new QueryColumn(this, "bill_time");
    public final QueryColumn SETTLEMENT_ID = new QueryColumn(this, "settlement_id");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, BILL_NO, AGREEMENT_UNIT_ID, AGREEMENT_UNIT_NAME, CHECK_IN_ID, CHECK_IN_NO, CUSTOMER_NAME, ROOM_NUMBER, CHECK_IN_DATE, CHECK_OUT_DATE, ROOM_FEE, EXTRA_FEE, DISCOUNT_AMOUNT, TOTAL_AMOUNT, BILL_TIME, SETTLEMENT_ID, STATUS, CREATE_TIME, UPDATE_TIME};

    public CreditBillTableDef() {
        super("", "credit_bill");
    }
}
