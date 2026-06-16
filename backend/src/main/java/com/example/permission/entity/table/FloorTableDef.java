package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class FloorTableDef extends TableDef {

    public static final FloorTableDef FLOOR = new FloorTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn FEATURES = new QueryColumn(this, "features");

    public final QueryColumn FLOOR_NAME = new QueryColumn(this, "floor_name");

    public final QueryColumn ROOM_COUNT = new QueryColumn(this, "room_count");

    public final QueryColumn BUILDING_ID = new QueryColumn(this, "building_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn FLOOR_NUMBER = new QueryColumn(this, "floor_number");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STATUS, DELETED, FEATURES, FLOOR_NAME, ROOM_COUNT, BUILDING_ID, CREATE_TIME, UPDATE_TIME, FLOOR_NUMBER};

    public FloorTableDef() {
        super("", "floor");
    }

}
