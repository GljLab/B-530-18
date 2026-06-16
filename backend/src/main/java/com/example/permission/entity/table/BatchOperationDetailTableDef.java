package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class BatchOperationDetailTableDef extends TableDef {

    public static final BatchOperationDetailTableDef BATCH_OPERATION_DETAIL = new BatchOperationDetailTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn BATCH_ID = new QueryColumn(this, "batch_id");

    public final QueryColumn BATCH_NO = new QueryColumn(this, "batch_no");

    public final QueryColumn NEW_VALUE = new QueryColumn(this, "new_value");

    public final QueryColumn OLD_VALUE = new QueryColumn(this, "old_value");

    public final QueryColumn TARGET_ID = new QueryColumn(this, "target_id");

    public final QueryColumn TARGET_NO = new QueryColumn(this, "target_no");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn FAIL_REASON = new QueryColumn(this, "fail_reason");

    public final QueryColumn TARGET_TYPE = new QueryColumn(this, "target_type");

    public final QueryColumn RESULT_STATUS = new QueryColumn(this, "result_status");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, BATCH_ID, BATCH_NO, NEW_VALUE, OLD_VALUE, TARGET_ID, TARGET_NO, CREATE_TIME, FAIL_REASON, TARGET_TYPE, RESULT_STATUS};

    public BatchOperationDetailTableDef() {
        super("", "batch_operation_detail");
    }

}
