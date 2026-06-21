package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class MemberLevelLogTableDef extends TableDef {

    public static final MemberLevelLogTableDef MEMBER_LEVEL_LOG = new MemberLevelLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn MEMBER_ID = new QueryColumn(this, "member_id");

    public final QueryColumn MEMBER_NO = new QueryColumn(this, "member_no");

    public final QueryColumn CHANGE_TYPE = new QueryColumn(this, "change_type");

    public final QueryColumn OLD_LEVEL_ID = new QueryColumn(this, "old_level_id");

    public final QueryColumn OLD_LEVEL_NAME = new QueryColumn(this, "old_level_name");

    public final QueryColumn NEW_LEVEL_ID = new QueryColumn(this, "new_level_id");

    public final QueryColumn NEW_LEVEL_NAME = new QueryColumn(this, "new_level_name");

    public final QueryColumn REASON = new QueryColumn(this, "reason");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn TRIGGER_TYPE = new QueryColumn(this, "trigger_type");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, MEMBER_ID, MEMBER_NO, CHANGE_TYPE, OLD_LEVEL_ID, OLD_LEVEL_NAME, NEW_LEVEL_ID, NEW_LEVEL_NAME, REASON, OPERATOR_ID, OPERATOR_NAME, TRIGGER_TYPE, CREATE_TIME};

    public MemberLevelLogTableDef() {
        super("", "member_level_log");
    }

}
