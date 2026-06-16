package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerMergeLogTableDef extends TableDef {

    public static final CustomerMergeLogTableDef CUSTOMER_MERGE_LOG = new CustomerMergeLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn MERGE_TIME = new QueryColumn(this, "merge_time");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn MERGE_DETAILS = new QueryColumn(this, "merge_details");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn SOURCE_CUSTOMER_ID = new QueryColumn(this, "source_customer_id");

    public final QueryColumn TARGET_CUSTOMER_ID = new QueryColumn(this, "target_customer_id");

    public final QueryColumn SOURCE_CUSTOMER_NAME = new QueryColumn(this, "source_customer_name");

    public final QueryColumn TARGET_CUSTOMER_NAME = new QueryColumn(this, "target_customer_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, MERGE_TIME, OPERATOR_ID, MERGE_DETAILS, OPERATOR_NAME, SOURCE_CUSTOMER_ID, TARGET_CUSTOMER_ID, SOURCE_CUSTOMER_NAME, TARGET_CUSTOMER_NAME};

    public CustomerMergeLogTableDef() {
        super("", "customer_merge_log");
    }

}
