package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class BookingDetailTableDef extends TableDef {

    public static final BookingDetailTableDef BOOKING_DETAIL = new BookingDetailTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn PRICE = new QueryColumn(this, "price");

    public final QueryColumn DAY_TYPE = new QueryColumn(this, "day_type");

    public final QueryColumn STAY_DATE = new QueryColumn(this, "stay_date");

    public final QueryColumn BOOKING_ID = new QueryColumn(this, "booking_id");

    public final QueryColumn BOOKING_NO = new QueryColumn(this, "booking_no");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, PRICE, DAY_TYPE, STAY_DATE, BOOKING_ID, BOOKING_NO, CREATE_TIME};

    public BookingDetailTableDef() {
        super("", "booking_detail");
    }

}
