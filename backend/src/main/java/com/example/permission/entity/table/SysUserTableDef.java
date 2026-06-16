package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SysUserTableDef extends TableDef {

    /**
     * 系统用户实体
     */
    public static final SysUserTableDef SYS_USER = new SysUserTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    /**
     * 邮箱
     */
    public final QueryColumn EMAIL = new QueryColumn(this, "email");

    /**
     * 手机号
     */
    public final QueryColumn PHONE = new QueryColumn(this, "phone");

    /**
     * 头像URL
     */
    public final QueryColumn AVATAR = new QueryColumn(this, "avatar");

    /**
     * 状态：0-禁用，1-启用
     */
    public final QueryColumn STATUS = new QueryColumn(this, "status");

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    /**
     * 昵称
     */
    public final QueryColumn NICKNAME = new QueryColumn(this, "nickname");

    /**
     * 密码
     */
    public final QueryColumn PASSWORD = new QueryColumn(this, "password");

    /**
     * 用户名
     */
    public final QueryColumn USERNAME = new QueryColumn(this, "username");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, EMAIL, PHONE, AVATAR, STATUS, DELETED, NICKNAME, PASSWORD, USERNAME, CREATE_TIME, UPDATE_TIME};

    public SysUserTableDef() {
        super("", "sys_user");
    }

}
