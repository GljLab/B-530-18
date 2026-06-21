package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewRecordTableDef extends TableDef {

    public static final ReviewRecordTableDef REVIEW_RECORD = new ReviewRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn INVITATION_ID = new QueryColumn(this, "invitation_id");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn CUSTOMER_PHONE = new QueryColumn(this, "customer_phone");
    public final QueryColumn MEMBER_ID = new QueryColumn(this, "member_id");
    public final QueryColumn MEMBER_NO = new QueryColumn(this, "member_no");
    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");
    public final QueryColumn ROOM_TYPE_NAME = new QueryColumn(this, "room_type_name");
    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");
    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");
    public final QueryColumn OVERALL_SCORE = new QueryColumn(this, "overall_score");
    public final QueryColumn REVIEW_CONTENT = new QueryColumn(this, "review_content");
    public final QueryColumn SELECTED_TAGS = new QueryColumn(this, "selected_tags");
    public final QueryColumn IS_ANONYMOUS = new QueryColumn(this, "is_anonymous");
    public final QueryColumn REVIEW_STATUS = new QueryColumn(this, "review_status");
    public final QueryColumn AUDIT_TIME = new QueryColumn(this, "audit_time");
    public final QueryColumn AUDITOR_ID = new QueryColumn(this, "auditor_id");
    public final QueryColumn AUDITOR_NAME = new QueryColumn(this, "auditor_name");
    public final QueryColumn AUDIT_REMARK = new QueryColumn(this, "audit_remark");
    public final QueryColumn IS_TOP = new QueryColumn(this, "is_top");
    public final QueryColumn TOP_TIME = new QueryColumn(this, "top_time");
    public final QueryColumn REPLY_CONTENT = new QueryColumn(this, "reply_content");
    public final QueryColumn REPLY_TIME = new QueryColumn(this, "reply_time");
    public final QueryColumn REPLIED_BY = new QueryColumn(this, "replied_by");
    public final QueryColumn REPLIED_NAME = new QueryColumn(this, "replied_name");
    public final QueryColumn LIKE_COUNT = new QueryColumn(this, "like_count");
    public final QueryColumn REVIEW_TIME = new QueryColumn(this, "review_time");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_IN_ID, CHECK_IN_NO, INVITATION_ID, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_PHONE, MEMBER_ID, MEMBER_NO, ROOM_TYPE_ID, ROOM_TYPE_NAME, CHECK_IN_DATE, CHECK_OUT_DATE, OVERALL_SCORE, REVIEW_CONTENT, SELECTED_TAGS, IS_ANONYMOUS, REVIEW_STATUS, AUDIT_TIME, AUDITOR_ID, AUDITOR_NAME, AUDIT_REMARK, IS_TOP, TOP_TIME, REPLY_CONTENT, REPLY_TIME, REPLIED_BY, REPLIED_NAME, LIKE_COUNT, REVIEW_TIME, CREATE_TIME, UPDATE_TIME, DELETED};

    public ReviewRecordTableDef() {
        super("", "review_record");
    }
}
