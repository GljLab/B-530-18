package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class MaintenanceOrderTableDef extends TableDef {

    public static final MaintenanceOrderTableDef MAINTENANCE_ORDER = new MaintenanceOrderTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ORDER_NO = new QueryColumn(this, "order_no");

    public final QueryColumn PRIORITY = new QueryColumn(this, "priority");

    public final QueryColumn USED_PARTS = new QueryColumn(this, "used_parts");

    public final QueryColumn ACCEPT_TIME = new QueryColumn(this, "accept_time");

    public final QueryColumn ASSIGN_TIME = new QueryColumn(this, "assign_time");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn FINISH_TIME = new QueryColumn(this, "finish_time");

    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ACTUAL_HOURS = new QueryColumn(this, "actual_hours");

    public final QueryColumn INSPECT_TIME = new QueryColumn(this, "inspect_time");

    public final QueryColumn INSPECTOR_ID = new QueryColumn(this, "inspector_id");

    public final QueryColumn CREATE_USER_ID = new QueryColumn(this, "create_user_id");

    public final QueryColumn INSPECT_RESULT = new QueryColumn(this, "inspect_result");

    public final QueryColumn INSPECTOR_NAME = new QueryColumn(this, "inspector_name");

    public final QueryColumn SPECIAL_REMARK = new QueryColumn(this, "special_remark");

    public final QueryColumn ASSIGNED_USER_ID = new QueryColumn(this, "assigned_user_id");

    public final QueryColumn CREATE_USER_NAME = new QueryColumn(this, "create_user_name");

    public final QueryColumn INSPECT_OPINION = new QueryColumn(this, "inspect_opinion");

    public final QueryColumn MAINTENANCE_COST = new QueryColumn(this, "maintenance_cost");

    public final QueryColumn MAINTENANCE_TYPE = new QueryColumn(this, "maintenance_type");

    public final QueryColumn ASSIGNED_USER_NAME = new QueryColumn(this, "assigned_user_name");

    public final QueryColumn EXPECTED_FINISH_TIME = new QueryColumn(this, "expected_finish_time");

    public final QueryColumn PROBLEM_DESCRIPTION = new QueryColumn(this, "problem_description");

    public final QueryColumn MAINTENANCE_DESCRIPTION = new QueryColumn(this, "maintenance_description");

    public final QueryColumn RECTIFICATION_REQUIREMENT = new QueryColumn(this, "rectification_requirement");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ROOM_ID, STATUS, DELETED, ORDER_NO, PRIORITY, USED_PARTS, ACCEPT_TIME, ASSIGN_TIME, CREATE_TIME, FINISH_TIME, ROOM_NUMBER, UPDATE_TIME, ACTUAL_HOURS, INSPECT_TIME, INSPECTOR_ID, CREATE_USER_ID, INSPECT_RESULT, INSPECTOR_NAME, SPECIAL_REMARK, ASSIGNED_USER_ID, CREATE_USER_NAME, INSPECT_OPINION, MAINTENANCE_COST, MAINTENANCE_TYPE, ASSIGNED_USER_NAME, EXPECTED_FINISH_TIME, PROBLEM_DESCRIPTION, MAINTENANCE_DESCRIPTION, RECTIFICATION_REQUIREMENT};

    public MaintenanceOrderTableDef() {
        super("", "maintenance_order");
    }

}
