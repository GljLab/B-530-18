package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class MemberPointLogTableDef extends TableDef {

    public static final MemberPointLogTableDef MEMBER_POINT_LOG = new MemberPointLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn MEMBER_ID = new QueryColumn(this, "member_id");

    public final QueryColumn MEMBER_NO = new QueryColumn(this, "member_no");

    public final QueryColumn POINT_TYPE = new QueryColumn(this, "point_type");

    public final QueryColumn POINTS = new QueryColumn(this, "points");

    public final QueryColumn BALANCE_BEFORE = new QueryColumn(this, "balance_before");

    public final QueryColumn BALANCE_AFTER = new QueryColumn(this, "balance_after");

    public final QueryColumn REASON_TYPE = new QueryColumn(this, "reason_type");

    public final QueryColumn REASON = new QueryColumn(this, "reason");

    public final QueryColumn DETAIL = new QueryColumn(this, "detail");

    public final QueryColumn RELATED_ORDER_TYPE = new QueryColumn(this, "related_order_type");

    public final QueryColumn RELATED_ORDER_ID = new QueryColumn(this, "related_order_id");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, MEMBER_ID, MEMBER_NO, POINT_TYPE, POINTS, BALANCE_BEFORE, BALANCE_AFTER, REASON_TYPE, REASON, DETAIL, RELATED_ORDER_TYPE, RELATED_ORDER_ID, OPERATOR_ID, OPERATOR_NAME, CREATE_TIME};

    public MemberPointLogTableDef() {
        super("", "member_point_log");
    }

}
