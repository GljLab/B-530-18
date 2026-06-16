package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SysMenuTableDef extends TableDef {

    /**
     * 系统菜单实体
     */
    public static final SysMenuTableDef SYS_MENU = new SysMenuTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 菜单图标
     */
    public final QueryColumn ICON = new QueryColumn(this, "icon");

    /**
     * 路由地址
     */
    public final QueryColumn PATH = new QueryColumn(this, "path");

    /**
     * 权限标识
     */
    public final QueryColumn PERMS = new QueryColumn(this, "perms");

    /**
     * 状态：0-禁用，1-启用
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 是否显示：0-隐藏，1-显示
     */
    public final QueryColumn VISIBLE = new QueryColumn(this, "visible");

    /**
     * 菜单名称
     */
    public final QueryColumn MENU_NAME = new QueryColumn(this, "menu_name");

    /**
     * 菜单类型：0-目录，1-菜单，2-按钮
     */
    public final QueryColumn MENU_TYPE = new QueryColumn(this, "menu_type");

    /**
     * 显示顺序
     */
    public final QueryColumn ORDER_NUM = new QueryColumn(this, "order_num");

    /**
     * 父菜单ID
     */
    public final QueryColumn PARENT_ID = new QueryColumn(this, "parent_id");

    /**
     * 组件路径
     */
    public final QueryColumn COMPONENT = new QueryColumn(this, "component");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ICON, PATH, PERMS, STATUS, VISIBLE, MENU_NAME, MENU_TYPE, ORDER_NUM, PARENT_ID, COMPONENT, CREATE_TIME, UPDATE_TIME};

    public SysMenuTableDef() {
        super("", "sys_menu");
    }

}
