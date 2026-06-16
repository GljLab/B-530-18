package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class HotelFacilityTableDef extends TableDef {

    public static final HotelFacilityTableDef HOTEL_FACILITY = new HotelFacilityTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn HOTEL_ID = new QueryColumn(this, "hotel_id");

    public final QueryColumn OPEN_TIME = new QueryColumn(this, "open_time");

    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    public final QueryColumn FACILITY_ICON = new QueryColumn(this, "facility_icon");

    public final QueryColumn FACILITY_NAME = new QueryColumn(this, "facility_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, HOTEL_ID, OPEN_TIME, SORT_ORDER, CREATE_TIME, UPDATE_TIME, DESCRIPTION, FACILITY_ICON, FACILITY_NAME};

    public HotelFacilityTableDef() {
        super("", "hotel_facility");
    }

}
