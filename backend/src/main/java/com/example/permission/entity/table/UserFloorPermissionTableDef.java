package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class UserFloorPermissionTableDef extends TableDef {

    public static final UserFloorPermissionTableDef USER_FLOOR_PERMISSION = new UserFloorPermissionTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    public final QueryColumn FLOOR_ID = new QueryColumn(this, "floor_id");

    public final QueryColumn BUILDING_ID = new QueryColumn(this, "building_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, USER_ID, FLOOR_ID, BUILDING_ID, CREATE_TIME};

    public UserFloorPermissionTableDef() {
        super("", "user_floor_permission");
    }

}
