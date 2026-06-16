package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class RoomChangeLogTableDef extends TableDef {

    public static final RoomChangeLogTableDef ROOM_CHANGE_LOG = new RoomChangeLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");

    public final QueryColumn NEW_VALUE = new QueryColumn(this, "new_value");

    public final QueryColumn OLD_VALUE = new QueryColumn(this, "old_value");

    public final QueryColumn OPERATOR = new QueryColumn(this, "operator");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");

    public final QueryColumn TERMINAL_IP = new QueryColumn(this, "terminal_ip");

    public final QueryColumn CHANGE_FIELD = new QueryColumn(this, "change_field");

    public final QueryColumn CHANGE_REASON = new QueryColumn(this, "change_reason");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn OPERATOR_ROLE = new QueryColumn(this, "operator_role");

    public final QueryColumn TERMINAL_TYPE = new QueryColumn(this, "terminal_type");

    public final QueryColumn OPERATION_TYPE = new QueryColumn(this, "operation_type");

    public final QueryColumn RELATED_ORDER_ID = new QueryColumn(this, "related_order_id");

    public final QueryColumn RELATED_ORDER_NO = new QueryColumn(this, "related_order_no");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ROOM_ID, NEW_VALUE, OLD_VALUE, OPERATOR, CREATE_TIME, OPERATOR_ID, ROOM_NUMBER, TERMINAL_IP, CHANGE_FIELD, CHANGE_REASON, OPERATOR_NAME, OPERATOR_ROLE, TERMINAL_TYPE, OPERATION_TYPE, RELATED_ORDER_ID, RELATED_ORDER_NO};

    public RoomChangeLogTableDef() {
        super("", "room_change_log");
    }

}
