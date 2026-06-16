package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class RoomTypeTableDef extends TableDef {

    public static final RoomTypeTableDef ROOM_TYPE = new RoomTypeTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn AREA = new QueryColumn(this, "area");

    public final QueryColumn BED_TYPE = new QueryColumn(this, "bed_type");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn TYPE_CODE = new QueryColumn(this, "type_code");

    public final QueryColumn TYPE_NAME = new QueryColumn(this, "type_name");

    public final QueryColumn BASE_PRICE = new QueryColumn(this, "base_price");

    public final QueryColumn COST_PRICE = new QueryColumn(this, "cost_price");

    public final QueryColumn ROOM_COUNT = new QueryColumn(this, "room_count");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn FACILITIES = new QueryColumn(this, "facilities");

    public final QueryColumn SALE_STATUS = new QueryColumn(this, "sale_status");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    public final QueryColumn MAX_OCCUPANCY = new QueryColumn(this, "max_occupancy");

    public final QueryColumn WEEKEND_PRICE = new QueryColumn(this, "weekend_price");

    public final QueryColumn EXTRA_BED_POLICY = new QueryColumn(this, "extra_bed_policy");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, AREA, BED_TYPE, DELETED, TYPE_CODE, TYPE_NAME, BASE_PRICE, COST_PRICE, ROOM_COUNT, CREATE_TIME, FACILITIES, SALE_STATUS, UPDATE_TIME, DESCRIPTION, MAX_OCCUPANCY, WEEKEND_PRICE, EXTRA_BED_POLICY};

    public RoomTypeTableDef() {
        super("", "room_type");
    }

}
