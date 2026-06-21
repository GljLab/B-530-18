package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class MemberLevelTaskLogTableDef extends TableDef {

    public static final MemberLevelTaskLogTableDef MEMBER_LEVEL_TASK_LOG = new MemberLevelTaskLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn TASK_TYPE = new QueryColumn(this, "task_type");

    public final QueryColumn TASK_NAME = new QueryColumn(this, "task_name");

    public final QueryColumn EXECUTE_TIME = new QueryColumn(this, "execute_time");

    public final QueryColumn EXECUTE_RESULT = new QueryColumn(this, "execute_result");

    public final QueryColumn PROCESS_COUNT = new QueryColumn(this, "process_count");

    public final QueryColumn UPGRADE_COUNT = new QueryColumn(this, "upgrade_count");

    public final QueryColumn DOWNGRADE_COUNT = new QueryColumn(this, "downgrade_count");

    public final QueryColumn RESET_COUNT = new QueryColumn(this, "reset_count");

    public final QueryColumn ERROR_MSG = new QueryColumn(this, "error_msg");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn TRIGGER_TYPE = new QueryColumn(this, "trigger_type");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TASK_TYPE, TASK_NAME, EXECUTE_TIME, EXECUTE_RESULT, PROCESS_COUNT, UPGRADE_COUNT, DOWNGRADE_COUNT, RESET_COUNT, ERROR_MSG, OPERATOR_ID, OPERATOR_NAME, TRIGGER_TYPE, CREATE_TIME};

    public MemberLevelTaskLogTableDef() {
        super("", "member_level_task_log");
    }

}
