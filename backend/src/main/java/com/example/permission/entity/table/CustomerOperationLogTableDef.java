package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerOperationLogTableDef extends TableDef {

    public static final CustomerOperationLogTableDef CUSTOMER_OPERATION_LOG = new CustomerOperationLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn NEW_VALUE = new QueryColumn(this, "new_value");

    public final QueryColumn OLD_VALUE = new QueryColumn(this, "old_value");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn OPERATOR_IP = new QueryColumn(this, "operator_ip");

    public final QueryColumn CHANGE_FIELD = new QueryColumn(this, "change_field");

    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    public final QueryColumn OPERATION_TYPE = new QueryColumn(this, "operation_type");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, NEW_VALUE, OLD_VALUE, CREATE_TIME, CUSTOMER_ID, OPERATOR_ID, OPERATOR_IP, CHANGE_FIELD, CUSTOMER_NAME, OPERATOR_NAME, OPERATION_TYPE};

    public CustomerOperationLogTableDef() {
        super("", "customer_operation_log");
    }

}
