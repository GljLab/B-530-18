package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BadDebtActionTableDef extends TableDef {

    public static final BadDebtActionTableDef BAD_DEBT_ACTION = new BadDebtActionTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn BAD_DEBT_ID = new QueryColumn(this, "bad_debt_id");
    public final QueryColumn ACTION_TYPE = new QueryColumn(this, "action_type");
    public final QueryColumn ACTION_TIME = new QueryColumn(this, "action_time");
    public final QueryColumn ACTION_METHOD = new QueryColumn(this, "action_method");
    public final QueryColumn ACTION_RESULT = new QueryColumn(this, "action_result");
    public final QueryColumn RECOVERED_AMOUNT = new QueryColumn(this, "recovered_amount");
    public final QueryColumn PAYMENT_METHOD = new QueryColumn(this, "payment_method");
    public final QueryColumn PAYMENT_TIME = new QueryColumn(this, "payment_time");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, BAD_DEBT_ID, ACTION_TYPE, ACTION_TIME, ACTION_METHOD, ACTION_RESULT, RECOVERED_AMOUNT, PAYMENT_METHOD, PAYMENT_TIME, OPERATOR_ID, OPERATOR_NAME, REMARK, CREATE_TIME};

    public BadDebtActionTableDef() {
        super("", "bad_debt_action");
    }
}
