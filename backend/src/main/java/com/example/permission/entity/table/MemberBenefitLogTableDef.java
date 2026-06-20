package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class MemberBenefitLogTableDef extends TableDef {

    public static final MemberBenefitLogTableDef MEMBER_BENEFIT_LOG = new MemberBenefitLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn MEMBER_ID = new QueryColumn(this, "member_id");
    public final QueryColumn MEMBER_NO = new QueryColumn(this, "member_no");
    public final QueryColumn MEMBER_NAME = new QueryColumn(this, "member_name");
    public final QueryColumn BENEFIT_TYPE = new QueryColumn(this, "benefit_type");
    public final QueryColumn BENEFIT_TYPE_NAME = new QueryColumn(this, "benefit_type_name");
    public final QueryColumn RELATED_ORDER_TYPE = new QueryColumn(this, "related_order_type");
    public final QueryColumn RELATED_ORDER_ID = new QueryColumn(this, "related_order_id");
    public final QueryColumn RELATED_ORDER_NO = new QueryColumn(this, "related_order_no");
    public final QueryColumn BENEFIT_CONTENT = new QueryColumn(this, "benefit_content");
    public final QueryColumn ORIGINAL_AMOUNT = new QueryColumn(this, "original_amount");
    public final QueryColumn BENEFIT_AMOUNT = new QueryColumn(this, "benefit_amount");
    public final QueryColumn ACTUAL_AMOUNT = new QueryColumn(this, "actual_amount");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public MemberBenefitLogTableDef() {
        super("", "member_benefit_log");
    }
}
