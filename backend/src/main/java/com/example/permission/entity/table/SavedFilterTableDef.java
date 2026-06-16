package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class SavedFilterTableDef extends TableDef {

    public static final SavedFilterTableDef SAVED_FILTER = new SavedFilterTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn USER_ID = new QueryColumn(this, "user_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn FILTER_NAME = new QueryColumn(this, "filter_name");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn FILTER_CONFIG = new QueryColumn(this, "filter_config");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, USER_ID, CREATE_TIME, FILTER_NAME, UPDATE_TIME, FILTER_CONFIG};

    public SavedFilterTableDef() {
        super("", "saved_filter");
    }

}
