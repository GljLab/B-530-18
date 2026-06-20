package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class PointConsumeRuleTableDef extends TableDef {

    public static final PointConsumeRuleTableDef POINT_CONSUME_RULE = new PointConsumeRuleTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn RULE_NAME = new QueryColumn(this, "rule_name");

    public final QueryColumn RULE_TYPE = new QueryColumn(this, "rule_type");

    public final QueryColumn EXCHANGE_POINTS = new QueryColumn(this, "exchange_points");

    public final QueryColumn EXCHANGE_AMOUNT = new QueryColumn(this, "exchange_amount");

    public final QueryColumn MAX_POINTS_PER_USE = new QueryColumn(this, "max_points_per_use");

    public final QueryColumn MIN_ORDER_AMOUNT = new QueryColumn(this, "min_order_amount");

    public final QueryColumn DEDUCTION_CAP = new QueryColumn(this, "deduction_cap");

    public final QueryColumn APPLICABLE_LEVELS = new QueryColumn(this, "applicable_levels");

    public final QueryColumn START_TIME = new QueryColumn(this, "start_time");

    public final QueryColumn END_TIME = new QueryColumn(this, "end_time");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, RULE_NAME, RULE_TYPE, EXCHANGE_POINTS, EXCHANGE_AMOUNT, MAX_POINTS_PER_USE, MIN_ORDER_AMOUNT, DEDUCTION_CAP, APPLICABLE_LEVELS, START_TIME, END_TIME, STATUS, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public PointConsumeRuleTableDef() {
        super("", "point_consume_rule");
    }

}
