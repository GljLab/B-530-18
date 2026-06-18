package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CheckInOperationLogTableDef extends TableDef {

    public static final CheckInOperationLogTableDef CHECK_IN_OPERATION_LOG = new CheckInOperationLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn OPERATION_TYPE = new QueryColumn(this, "operation_type");
    public final QueryColumn OPERATION_DESC = new QueryColumn(this, "operation_desc");
    public final QueryColumn DETAIL = new QueryColumn(this, "detail");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn OPERATOR_ROLE = new QueryColumn(this, "operator_role");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_IN_ID, CHECK_IN_NO, OPERATION_TYPE, OPERATION_DESC, DETAIL, OPERATOR_ID, OPERATOR_NAME, OPERATOR_ROLE, CREATE_TIME};

    public CheckInOperationLogTableDef() {
        super("", "check_in_operation_log");
    }
}
