package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BookingRuleTableDef extends TableDef {

    public static final BookingRuleTableDef BOOKING_RULE = new BookingRuleTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn RULE_NAME = new QueryColumn(this, "rule_name");

    public final QueryColumn RULE_TYPE = new QueryColumn(this, "rule_type");

    public final QueryColumn RULE_PARAMS = new QueryColumn(this, "rule_params");

    public final QueryColumn PRIORITY = new QueryColumn(this, "priority");

    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");

    public final QueryColumn APPLY_ROOM_TYPES = new QueryColumn(this, "apply_room_types");

    public final QueryColumn APPLY_DATE_START = new QueryColumn(this, "apply_date_start");

    public final QueryColumn APPLY_DATE_END = new QueryColumn(this, "apply_date_end");

    public final QueryColumn APPLY_SOURCES = new QueryColumn(this, "apply_sources");

    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    public final QueryColumn CREATE_USER_ID = new QueryColumn(this, "create_user_id");

    public final QueryColumn CREATE_USER_NAME = new QueryColumn(this, "create_user_name");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, RULE_NAME, RULE_TYPE, RULE_PARAMS, PRIORITY, ENABLED, APPLY_ROOM_TYPES, APPLY_DATE_START, APPLY_DATE_END, APPLY_SOURCES, DESCRIPTION, CREATE_USER_ID, CREATE_USER_NAME, CREATE_TIME, UPDATE_TIME, DELETED};

    public BookingRuleTableDef() {
        super("", "booking_rule");
    }
}
