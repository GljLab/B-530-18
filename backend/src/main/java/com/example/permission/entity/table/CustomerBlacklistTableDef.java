package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerBlacklistTableDef extends TableDef {

    public static final CustomerBlacklistTableDef CUSTOMER_BLACKLIST = new CustomerBlacklistTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REASON = new QueryColumn(this, "reason");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn APPLY_TIME = new QueryColumn(this, "apply_time");

    public final QueryColumn APPROVER_ID = new QueryColumn(this, "approver_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn EXPIRE_TIME = new QueryColumn(this, "expire_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn APPLICANT_ID = new QueryColumn(this, "applicant_id");

    public final QueryColumn APPROVE_TIME = new QueryColumn(this, "approve_time");

    public final QueryColumn APPROVER_NAME = new QueryColumn(this, "approver_name");

    public final QueryColumn REJECT_REASON = new QueryColumn(this, "reject_reason");

    public final QueryColumn REMOVE_REASON = new QueryColumn(this, "remove_reason");

    public final QueryColumn APPLICANT_NAME = new QueryColumn(this, "applicant_name");

    public final QueryColumn BLACKLIST_TYPE = new QueryColumn(this, "blacklist_type");

    public final QueryColumn APPROVE_OPINION = new QueryColumn(this, "approve_opinion");

    public final QueryColumn REMOVE_APPLY_TIME = new QueryColumn(this, "remove_apply_time");

    public final QueryColumn REMOVE_APPROVER_ID = new QueryColumn(this, "remove_approver_id");

    public final QueryColumn DETAIL_DESCRIPTION = new QueryColumn(this, "detail_description");

    public final QueryColumn EVIDENCE_MATERIALS = new QueryColumn(this, "evidence_materials");

    public final QueryColumn REMOVE_APPLICANT_ID = new QueryColumn(this, "remove_applicant_id");

    public final QueryColumn REMOVE_APPROVE_TIME = new QueryColumn(this, "remove_approve_time");

    public final QueryColumn REMOVE_APPROVER_NAME = new QueryColumn(this, "remove_approver_name");

    public final QueryColumn REMOVE_APPLICANT_NAME = new QueryColumn(this, "remove_applicant_name");

    public final QueryColumn REMOVE_APPROVE_OPINION = new QueryColumn(this, "remove_approve_opinion");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REASON, STATUS, APPLY_TIME, APPROVER_ID, CREATE_TIME, CUSTOMER_ID, EXPIRE_TIME, UPDATE_TIME, APPLICANT_ID, APPROVE_TIME, APPROVER_NAME, REJECT_REASON, REMOVE_REASON, APPLICANT_NAME, BLACKLIST_TYPE, APPROVE_OPINION, REMOVE_APPLY_TIME, REMOVE_APPROVER_ID, DETAIL_DESCRIPTION, EVIDENCE_MATERIALS, REMOVE_APPLICANT_ID, REMOVE_APPROVE_TIME, REMOVE_APPROVER_NAME, REMOVE_APPLICANT_NAME, REMOVE_APPROVE_OPINION};

    public CustomerBlacklistTableDef() {
        super("", "customer_blacklist");
    }

}
