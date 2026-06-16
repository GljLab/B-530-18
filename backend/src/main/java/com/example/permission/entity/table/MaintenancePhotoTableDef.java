package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class MaintenancePhotoTableDef extends TableDef {

    public static final MaintenancePhotoTableDef MAINTENANCE_PHOTO = new MaintenancePhotoTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn ORDER_ID = new QueryColumn(this, "order_id");

    public final QueryColumn PHOTO_URL = new QueryColumn(this, "photo_url");

    public final QueryColumn PHOTO_TYPE = new QueryColumn(this, "photo_type");

    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ORDER_ID, PHOTO_URL, PHOTO_TYPE, SORT_ORDER, CREATE_TIME};

    public MaintenancePhotoTableDef() {
        super("", "maintenance_photo");
    }

}
