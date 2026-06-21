package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewTagTableDef extends TableDef {

    public static final ReviewTagTableDef REVIEW_TAG = new ReviewTagTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn TAG_TYPE = new QueryColumn(this, "tag_type");
    public final QueryColumn TAG_TEXT = new QueryColumn(this, "tag_text");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, TAG_TYPE, TAG_TEXT, SORT_ORDER, STATUS, CREATE_TIME, UPDATE_TIME, DELETED};

    public ReviewTagTableDef() {
        super("", "review_tag");
    }
}
