package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class HotelImageTableDef extends TableDef {

    public static final HotelImageTableDef HOTEL_IMAGE = new HotelImageTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn REF_ID = new QueryColumn(this, "ref_id");

    public final QueryColumn IS_MAIN = new QueryColumn(this, "is_main");

    public final QueryColumn REF_TYPE = new QueryColumn(this, "ref_type");

    public final QueryColumn IMAGE_URL = new QueryColumn(this, "image_url");

    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REF_ID, IS_MAIN, REF_TYPE, IMAGE_URL, SORT_ORDER, CREATE_TIME};

    public HotelImageTableDef() {
        super("", "hotel_image");
    }

}
