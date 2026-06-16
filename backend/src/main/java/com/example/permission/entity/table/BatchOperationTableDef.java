package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class BatchOperationTableDef extends TableDef {

    public static final BatchOperationTableDef BATCH_OPERATION = new BatchOperationTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn BATCH_NO = new QueryColumn(this, "batch_no");

    public final QueryColumn ATTR_MODE = new QueryColumn(this, "attr_mode");

    public final QueryColumn ATTR_TYPE = new QueryColumn(this, "attr_type");

    public final QueryColumn ATTR_VALUE = new QueryColumn(this, "attr_value");

    public final QueryColumn FAIL_COUNT = new QueryColumn(this, "fail_count");

    public final QueryColumn SKIP_COUNT = new QueryColumn(this, "skip_count");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn FINISH_TIME = new QueryColumn(this, "finish_time");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn TOTAL_COUNT = new QueryColumn(this, "total_count");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn SUCCESS_COUNT = new QueryColumn(this, "success_count");

    public final QueryColumn TARGET_STATUS = new QueryColumn(this, "target_status");

    public final QueryColumn OPERATION_TYPE = new QueryColumn(this, "operation_type");

    public final QueryColumn OPERATION_REASON = new QueryColumn(this, "operation_reason");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, STATUS, BATCH_NO, ATTR_MODE, ATTR_TYPE, ATTR_VALUE, FAIL_COUNT, SKIP_COUNT, CREATE_TIME, FINISH_TIME, OPERATOR_ID, TOTAL_COUNT, OPERATOR_NAME, SUCCESS_COUNT, TARGET_STATUS, OPERATION_TYPE, OPERATION_REASON};

    public BatchOperationTableDef() {
        super("", "batch_operation");
    }

}
