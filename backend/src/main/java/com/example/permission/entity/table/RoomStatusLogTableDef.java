package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class RoomStatusLogTableDef extends TableDef {

    public static final RoomStatusLogTableDef ROOM_STATUS_LOG = new RoomStatusLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");

    public final QueryColumn OPERATOR = new QueryColumn(this, "operator");

    public final QueryColumn NEW_STATUS = new QueryColumn(this, "new_status");

    public final QueryColumn OLD_STATUS = new QueryColumn(this, "old_status");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, ROOM_ID, OPERATOR, NEW_STATUS, OLD_STATUS, CREATE_TIME, OPERATOR_ID, ROOM_NUMBER};

    public RoomStatusLogTableDef() {
        super("", "room_status_log");
    }

}
