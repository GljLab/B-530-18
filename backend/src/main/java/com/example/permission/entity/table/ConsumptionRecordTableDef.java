package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ConsumptionRecordTableDef extends TableDef {

    public static final ConsumptionRecordTableDef CONSUMPTION_RECORD = new ConsumptionRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CONSUMPTION_NO = new QueryColumn(this, "consumption_no");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");
    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn CONSUMPTION_TYPE = new QueryColumn(this, "consumption_type");
    public final QueryColumn ITEM_NAME = new QueryColumn(this, "item_name");
    public final QueryColumn QUANTITY = new QueryColumn(this, "quantity");
    public final QueryColumn UNIT_PRICE = new QueryColumn(this, "unit_price");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn CONSUMPTION_TIME = new QueryColumn(this, "consumption_time");
    public final QueryColumn BILLING_METHOD = new QueryColumn(this, "billing_method");
    public final QueryColumn IS_SETTLED = new QueryColumn(this, "is_settled");
    public final QueryColumn SETTLE_TIME = new QueryColumn(this, "settle_time");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CONSUMPTION_NO, CHECK_IN_ID, CHECK_IN_NO, ROOM_ID, ROOM_NUMBER, CUSTOMER_ID, CUSTOMER_NAME, CONSUMPTION_TYPE, ITEM_NAME, QUANTITY, UNIT_PRICE, TOTAL_AMOUNT, CONSUMPTION_TIME, BILLING_METHOD, IS_SETTLED, SETTLE_TIME, OPERATOR_ID, OPERATOR_NAME, REMARK, CREATE_TIME, UPDATE_TIME};

    public ConsumptionRecordTableDef() {
        super("", "consumption_record");
    }
}
