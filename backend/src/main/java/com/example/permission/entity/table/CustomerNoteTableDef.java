package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerNoteTableDef extends TableDef {

    public static final CustomerNoteTableDef CUSTOMER_NOTE = new CustomerNoteTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn CONTENT = new QueryColumn(this, "content");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn IS_PINNED = new QueryColumn(this, "is_pinned");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn IMPORTANCE = new QueryColumn(this, "importance");

    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ATTACHMENTS = new QueryColumn(this, "attachments");

    public final QueryColumn MENTION_ROLES = new QueryColumn(this, "mention_roles");

    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CONTENT, DELETED, IS_PINNED, CREATE_TIME, CUSTOMER_ID, IMPORTANCE, OPERATOR_ID, UPDATE_TIME, ATTACHMENTS, MENTION_ROLES, OPERATOR_NAME};

    public CustomerNoteTableDef() {
        super("", "customer_note");
    }

}
