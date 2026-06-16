package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class HotelInfoTableDef extends TableDef {

    public static final HotelInfoTableDef HOTEL_INFO = new HotelInfoTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn BRAND = new QueryColumn(this, "brand");

    public final QueryColumn ADDRESS = new QueryColumn(this, "address");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn HOTEL_NAME = new QueryColumn(this, "hotel_name");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn STAR_RATING = new QueryColumn(this, "star_rating");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DESCRIPTION = new QueryColumn(this, "description");

    public final QueryColumn OPENING_DATE = new QueryColumn(this, "opening_date");

    public final QueryColumn CONTACT_PHONE = new QueryColumn(this, "contact_phone");

    public final QueryColumn LICENSE_NUMBER = new QueryColumn(this, "license_number");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, BRAND, ADDRESS, DELETED, HOTEL_NAME, CREATE_TIME, STAR_RATING, UPDATE_TIME, DESCRIPTION, OPENING_DATE, CONTACT_PHONE, LICENSE_NUMBER};

    public HotelInfoTableDef() {
        super("", "hotel_info");
    }

}
