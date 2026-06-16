package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class BuildingTableDef extends TableDef {

    public static final BuildingTableDef BUILDING = new BuildingTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    public final QueryColumn TOTAL_FLOORS = new QueryColumn(this, "total_floors");

    public final QueryColumn BUILDING_CODE = new QueryColumn(this, "building_code");

    public final QueryColumn BUILDING_NAME = new QueryColumn(this, "building_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STATUS, DELETED, CREATE_TIME, UPDATE_TIME, DESCRIPTION, TOTAL_FLOORS, BUILDING_CODE, BUILDING_NAME};

    public BuildingTableDef() {
        super("", "building");
    }

}
