package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SysDataPermissionTableDef extends TableDef {

    /**
     * 数据权限实体
     */
    public static final SysDataPermissionTableDef SYS_DATA_PERMISSION = new SysDataPermissionTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 角色ID
     */
    public final QueryColumn ROLE_ID = new QueryColumn(this, "role_id");

    /**
     * 权限范围类型：1-全部数据，2-自定义数据，3-本部门数据，4-本部门及以下数据，5-仅本人数据
     */
    public final QueryColumn SCOPE_TYPE = new QueryColumn(this, "scope_type");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 自定义部门ID列表（逗号分隔）
     */
    public final QueryColumn CUSTOM_DEPT_IDS = new QueryColumn(this, "custom_dept_ids");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ROLE_ID, SCOPE_TYPE, CREATE_TIME, UPDATE_TIME, CUSTOM_DEPT_IDS};

    public SysDataPermissionTableDef() {
        super("", "sys_data_permission");
    }

}
