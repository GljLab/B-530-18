package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ComplaintVisitTableDef extends TableDef {

    public static final ComplaintVisitTableDef COMPLAINT_VISIT = new ComplaintVisitTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn COMPLAINT_ID = new QueryColumn(this, "complaint_id");
    public final QueryColumn VISIT_TIME = new QueryColumn(this, "visit_time");
    public final QueryColumn VISIT_METHOD = new QueryColumn(this, "visit_method");
    public final QueryColumn SATISFACTION = new QueryColumn(this, "satisfaction");
    public final QueryColumn VISIT_REMARK = new QueryColumn(this, "visit_remark");
    public final QueryColumn VISIT_USER_ID = new QueryColumn(this, "visit_user_id");
    public final QueryColumn VISIT_USER_NAME = new QueryColumn(this, "visit_user_name");
    public final QueryColumn NEED_REPROCESS = new QueryColumn(this, "need_reprocess");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, COMPLAINT_ID, VISIT_TIME, VISIT_METHOD, SATISFACTION, VISIT_REMARK, VISIT_USER_ID, VISIT_USER_NAME, NEED_REPROCESS, CREATE_TIME, DELETED};

    public ComplaintVisitTableDef() {
        super("", "complaint_visit");
    }
}
