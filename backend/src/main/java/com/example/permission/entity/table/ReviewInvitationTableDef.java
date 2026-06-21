package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewInvitationTableDef extends TableDef {

    public static final ReviewInvitationTableDef REVIEW_INVITATION = new ReviewInvitationTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn CUSTOMER_PHONE = new QueryColumn(this, "customer_phone");
    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");
    public final QueryColumn ROOM_TYPE_NAME = new QueryColumn(this, "room_type_name");
    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");
    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");
    public final QueryColumn REVIEW_CODE = new QueryColumn(this, "review_code");
    public final QueryColumn REVIEW_LINK = new QueryColumn(this, "review_link");
    public final QueryColumn REVIEW_STATUS = new QueryColumn(this, "review_status");
    public final QueryColumn REVIEW_TIME = new QueryColumn(this, "review_time");
    public final QueryColumn IS_SENT = new QueryColumn(this, "is_sent");
    public final QueryColumn SEND_TIME = new QueryColumn(this, "send_time");
    public final QueryColumn SEND_METHOD = new QueryColumn(this, "send_method");
    public final QueryColumn EXPIRE_TIME = new QueryColumn(this, "expire_time");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_IN_ID, CHECK_IN_NO, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_PHONE, ROOM_TYPE_ID, ROOM_TYPE_NAME, CHECK_IN_DATE, CHECK_OUT_DATE, REVIEW_CODE, REVIEW_LINK, REVIEW_STATUS, REVIEW_TIME, IS_SENT, SEND_TIME, SEND_METHOD, EXPIRE_TIME, CREATE_TIME, UPDATE_TIME, DELETED};

    public ReviewInvitationTableDef() {
        super("", "review_invitation");
    }
}
