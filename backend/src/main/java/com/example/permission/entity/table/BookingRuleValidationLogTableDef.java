package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BookingRuleValidationLogTableDef extends TableDef {

    public static final BookingRuleValidationLogTableDef BOOKING_RULE_VALIDATION_LOG = new BookingRuleValidationLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn BOOKING_ID = new QueryColumn(this, "booking_id");

    public final QueryColumn RULE_ID = new QueryColumn(this, "rule_id");

    public final QueryColumn RULE_NAME = new QueryColumn(this, "rule_name");

    public final QueryColumn RULE_TYPE = new QueryColumn(this, "rule_type");

    public final QueryColumn VALIDATION_RESULT = new QueryColumn(this, "validation_result");

    public final QueryColumn VALIDATION_MESSAGE = new QueryColumn(this, "validation_message");

    public final QueryColumn EXEMPTED = new QueryColumn(this, "exempted");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, BOOKING_ID, RULE_ID, RULE_NAME, RULE_TYPE, VALIDATION_RESULT, VALIDATION_MESSAGE, EXEMPTED, OPERATOR_ID, OPERATOR_NAME, CREATE_TIME};

    public BookingRuleValidationLogTableDef() {
        super("", "booking_rule_validation_log");
    }
}
