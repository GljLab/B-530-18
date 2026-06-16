package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerTagRelationTableDef extends TableDef {

    public static final CustomerTagRelationTableDef CUSTOMER_TAG_RELATION = new CustomerTagRelationTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn TAG_ID = new QueryColumn(this, "tag_id");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TAG_ID, CREATE_TIME, CUSTOMER_ID};

    public CustomerTagRelationTableDef() {
        super("", "customer_tag_relation");
    }

}
