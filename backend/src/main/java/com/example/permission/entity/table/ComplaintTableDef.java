package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ComplaintTableDef extends TableDef {

    public static final ComplaintTableDef COMPLAINT = new ComplaintTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn COMPLAINT_NO = new QueryColumn(this, "complaint_no");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn CUSTOMER_PHONE = new QueryColumn(this, "customer_phone");
    public final QueryColumn CUSTOMER_EMAIL = new QueryColumn(this, "customer_email");
    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");
    public final QueryColumn ROOM_TYPE_NAME = new QueryColumn(this, "room_type_name");
    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");
    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");
    public final QueryColumn COMPLAINT_TYPE = new QueryColumn(this, "complaint_type");
    public final QueryColumn COMPLAINT_CONTENT = new QueryColumn(this, "complaint_content");
    public final QueryColumn EXPECTED_SOLUTION = new QueryColumn(this, "expected_solution");
    public final QueryColumn COMPLAINT_STATUS = new QueryColumn(this, "complaint_status");
    public final QueryColumn VERIFY_CODE = new QueryColumn(this, "verify_code");
    public final QueryColumn ACCEPT_USER_ID = new QueryColumn(this, "accept_user_id");
    public final QueryColumn ACCEPT_USER_NAME = new QueryColumn(this, "accept_user_name");
    public final QueryColumn ACCEPT_TIME = new QueryColumn(this, "accept_time");
    public final QueryColumn ACCEPT_REMARK = new QueryColumn(this, "accept_remark");
    public final QueryColumn ASSIGN_USER_ID = new QueryColumn(this, "assign_user_id");
    public final QueryColumn ASSIGN_USER_NAME = new QueryColumn(this, "assign_user_name");
    public final QueryColumn REJECT_REASON = new QueryColumn(this, "reject_reason");
    public final QueryColumn REJECT_REMARK = new QueryColumn(this, "reject_remark");
    public final QueryColumn REJECT_USER_ID = new QueryColumn(this, "reject_user_id");
    public final QueryColumn REJECT_USER_NAME = new QueryColumn(this, "reject_user_name");
    public final QueryColumn REJECT_TIME = new QueryColumn(this, "reject_time");
    public final QueryColumn HANDLE_SOLUTION = new QueryColumn(this, "handle_solution");
    public final QueryColumn HANDLE_RESULT = new QueryColumn(this, "handle_result");
    public final QueryColumn COMPENSATION_PLAN = new QueryColumn(this, "compensation_plan");
    public final QueryColumn HANDLE_REMARK = new QueryColumn(this, "handle_remark");
    public final QueryColumn HANDLE_USER_ID = new QueryColumn(this, "handle_user_id");
    public final QueryColumn HANDLE_USER_NAME = new QueryColumn(this, "handle_user_name");
    public final QueryColumn HANDLE_TIME = new QueryColumn(this, "handle_time");
    public final QueryColumn NEED_REPROCESS = new QueryColumn(this, "need_reprocess");
    public final QueryColumn COMPLAINT_TIME = new QueryColumn(this, "complaint_time");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, COMPLAINT_NO, CHECK_IN_ID, CHECK_IN_NO, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_PHONE, CUSTOMER_EMAIL, ROOM_TYPE_ID, ROOM_TYPE_NAME, CHECK_IN_DATE, CHECK_OUT_DATE, COMPLAINT_TYPE, COMPLAINT_CONTENT, EXPECTED_SOLUTION, COMPLAINT_STATUS, VERIFY_CODE, ACCEPT_USER_ID, ACCEPT_USER_NAME, ACCEPT_TIME, ACCEPT_REMARK, ASSIGN_USER_ID, ASSIGN_USER_NAME, REJECT_REASON, REJECT_REMARK, REJECT_USER_ID, REJECT_USER_NAME, REJECT_TIME, HANDLE_SOLUTION, HANDLE_RESULT, COMPENSATION_PLAN, HANDLE_REMARK, HANDLE_USER_ID, HANDLE_USER_NAME, HANDLE_TIME, NEED_REPROCESS, COMPLAINT_TIME, CREATE_TIME, UPDATE_TIME, DELETED};

    public ComplaintTableDef() {
        super("", "complaint");
    }
}
