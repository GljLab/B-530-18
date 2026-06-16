package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerTagTableDef extends TableDef {

    public static final CustomerTagTableDef CUSTOMER_TAG = new CustomerTagTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn TAG_NAME = new QueryColumn(this, "tag_name");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, DELETED, TAG_NAME, CREATE_TIME, UPDATE_TIME};

    public CustomerTagTableDef() {
        super("", "customer_tag");
    }

}
