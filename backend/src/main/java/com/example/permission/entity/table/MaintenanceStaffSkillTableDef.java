package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class MaintenanceStaffSkillTableDef extends TableDef {

    public static final MaintenanceStaffSkillTableDef MAINTENANCE_STAFF_SKILL = new MaintenanceStaffSkillTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    public final QueryColumn SKILL_TYPE = new QueryColumn(this, "skill_type");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn SKILL_LEVEL = new QueryColumn(this, "skill_level");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, USER_ID, SKILL_TYPE, CREATE_TIME, SKILL_LEVEL};

    public MaintenanceStaffSkillTableDef() {
        super("", "maintenance_staff_skill");
    }

}
