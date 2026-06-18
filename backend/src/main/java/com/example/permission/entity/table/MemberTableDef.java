package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class MemberTableDef extends TableDef {

    public static final MemberTableDef MEMBER = new MemberTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn MEMBER_NO = new QueryColumn(this, "member_no");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");

    public final QueryColumn PHONE = new QueryColumn(this, "phone");

    public final QueryColumn LEVEL_ID = new QueryColumn(this, "level_id");

    public final QueryColumn LEVEL_NAME = new QueryColumn(this, "level_name");

    public final QueryColumn REGISTER_SOURCE = new QueryColumn(this, "register_source");

    public final QueryColumn REFERRER_ID = new QueryColumn(this, "referrer_id");

    public final QueryColumn REFERRER_NO = new QueryColumn(this, "referrer_no");

    public final QueryColumn TOTAL_POINTS = new QueryColumn(this, "total_points");

    public final QueryColumn CURRENT_POINTS = new QueryColumn(this, "current_points");

    public final QueryColumn TOTAL_SPENT = new QueryColumn(this, "total_spent");

    public final QueryColumn YEARLY_SPENT = new QueryColumn(this, "yearly_spent");

    public final QueryColumn STAY_COUNT = new QueryColumn(this, "stay_count");

    public final QueryColumn LAST_STAY_TIME = new QueryColumn(this, "last_stay_time");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn FREEZE_REASON = new QueryColumn(this, "freeze_reason");

    public final QueryColumn FREEZE_TIME = new QueryColumn(this, "freeze_time");

    public final QueryColumn FREEZE_OPERATOR_ID = new QueryColumn(this, "freeze_operator_id");

    public final QueryColumn FREEZE_OPERATOR_NAME = new QueryColumn(this, "freeze_operator_name");

    public final QueryColumn REGISTER_TIME = new QueryColumn(this, "register_time");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, MEMBER_NO, CUSTOMER_ID, CUSTOMER_NAME, PHONE, LEVEL_ID, LEVEL_NAME, REGISTER_SOURCE, REFERRER_ID, REFERRER_NO, TOTAL_POINTS, CURRENT_POINTS, TOTAL_SPENT, YEARLY_SPENT, STAY_COUNT, LAST_STAY_TIME, STATUS, FREEZE_REASON, FREEZE_TIME, FREEZE_OPERATOR_ID, FREEZE_OPERATOR_NAME, REGISTER_TIME, CREATE_TIME, UPDATE_TIME, DELETED};

    public MemberTableDef() {
        super("", "member");
    }

}
