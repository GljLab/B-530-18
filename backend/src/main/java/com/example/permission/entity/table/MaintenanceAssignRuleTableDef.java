package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class MaintenanceAssignRuleTableDef extends TableDef {

    public static final MaintenanceAssignRuleTableDef MAINTENANCE_ASSIGN_RULE = new MaintenanceAssignRuleTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn PRIORITY = new QueryColumn(this, "priority");

    public final QueryColumn RULE_NAME = new QueryColumn(this, "rule_name");

    public final QueryColumn RULE_TYPE = new QueryColumn(this, "rule_type");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn TARGET_USER_ID = new QueryColumn(this, "target_user_id");

    public final QueryColumn TARGET_ROLE_KEY = new QueryColumn(this, "target_role_key");

    public final QueryColumn CONDITION_VALUE = new QueryColumn(this, "condition_value");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, STATUS, PRIORITY, RULE_NAME, RULE_TYPE, CREATE_TIME, UPDATE_TIME, TARGET_USER_ID, TARGET_ROLE_KEY, CONDITION_VALUE};

    public MaintenanceAssignRuleTableDef() {
        super("", "maintenance_assign_rule");
    }

}
