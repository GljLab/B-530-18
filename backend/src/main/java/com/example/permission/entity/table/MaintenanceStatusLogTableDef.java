package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class MaintenanceStatusLogTableDef extends TableDef {

    public static final MaintenanceStatusLogTableDef MAINTENANCE_STATUS_LOG = new MaintenanceStatusLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn ORDER_ID = new QueryColumn(this, "order_id");

    public final QueryColumn ORDER_NO = new QueryColumn(this, "order_no");

    public final QueryColumn NEW_STATUS = new QueryColumn(this, "new_status");

    public final QueryColumn OLD_STATUS = new QueryColumn(this, "old_status");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, ORDER_ID, ORDER_NO, NEW_STATUS, OLD_STATUS, CREATE_TIME, OPERATOR_ID, OPERATOR_NAME};

    public MaintenanceStatusLogTableDef() {
        super("", "maintenance_status_log");
    }

}
