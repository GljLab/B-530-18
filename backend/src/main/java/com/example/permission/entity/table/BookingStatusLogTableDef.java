package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class BookingStatusLogTableDef extends TableDef {

    public static final BookingStatusLogTableDef BOOKING_STATUS_LOG = new BookingStatusLogTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn BOOKING_ID = new QueryColumn(this, "booking_id");

    public final QueryColumn BOOKING_NO = new QueryColumn(this, "booking_no");

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
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REMARK, BOOKING_ID, BOOKING_NO, NEW_STATUS, OLD_STATUS, CREATE_TIME, OPERATOR_ID, OPERATOR_NAME};

    public BookingStatusLogTableDef() {
        super("", "booking_status_log");
    }

}
