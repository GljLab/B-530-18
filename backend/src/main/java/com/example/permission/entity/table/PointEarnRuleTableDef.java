package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class PointEarnRuleTableDef extends TableDef {

    public static final PointEarnRuleTableDef POINT_EARN_RULE = new PointEarnRuleTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn RULE_NAME = new QueryColumn(this, "rule_name");

    public final QueryColumn RULE_TYPE = new QueryColumn(this, "rule_type");

    public final QueryColumn POINT_VALUE = new QueryColumn(this, "point_value");

    public final QueryColumn POINT_AMOUNT = new QueryColumn(this, "point_amount");

    public final QueryColumn POINT_RATE = new QueryColumn(this, "point_rate");

    public final QueryColumn START_TIME = new QueryColumn(this, "start_time");

    public final QueryColumn END_TIME = new QueryColumn(this, "end_time");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, RULE_NAME, RULE_TYPE, POINT_VALUE, POINT_AMOUNT, POINT_RATE, START_TIME, END_TIME, STATUS, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public PointEarnRuleTableDef() {
        super("", "point_earn_rule");
    }

}
