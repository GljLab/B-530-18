package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class RoomTableDef extends TableDef {

    public static final RoomTableDef ROOM = new RoomTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn FLOOR_ID = new QueryColumn(this, "floor_id");

    public final QueryColumn VIEW_TYPE = new QueryColumn(this, "view_type");

    public final QueryColumn BUILDING_ID = new QueryColumn(this, "building_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");

    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ORIENTATION = new QueryColumn(this, "orientation");

    public final QueryColumn SPECIAL_TAGS = new QueryColumn(this, "special_tags");

    public final QueryColumn LOCATION_FEATURES = new QueryColumn(this, "location_features");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, STATUS, DELETED, FLOOR_ID, VIEW_TYPE, BUILDING_ID, CREATE_TIME, ROOM_NUMBER, ROOM_TYPE_ID, UPDATE_TIME, ORIENTATION, SPECIAL_TAGS, LOCATION_FEATURES};

    public RoomTableDef() {
        super("", "room");
    }

}
