package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SysRoleTableDef extends TableDef {

    /**
     * 系统角色实体
     */
    public static final SysRoleTableDef SYS_ROLE = new SysRoleTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 备注
     */
    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    /**
     * 状态：0-禁用，1-启用
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    /**
     * 角色标识
     */
    public final QueryColumn ROLE_KEY = new QueryColumn(this, "role_key");

    /**
     * 显示顺序
     */
    public final QueryColumn ORDER_NUM = new QueryColumn(this, "order_num");

    /**
     * 角色名称
     */
    public final QueryColumn ROLE_NAME = new QueryColumn(this, "role_name");

    /**
     * 创建时间
     */
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 更新时间
     */
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, STATUS, DELETED, ROLE_KEY, ORDER_NUM, ROLE_NAME, CREATE_TIME, UPDATE_TIME};

    public SysRoleTableDef() {
        super("", "sys_role");
    }

}
