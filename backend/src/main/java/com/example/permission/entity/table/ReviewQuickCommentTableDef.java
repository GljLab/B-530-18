package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewQuickCommentTableDef extends TableDef {

    public static final ReviewQuickCommentTableDef REVIEW_QUICK_COMMENT = new ReviewQuickCommentTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn COMMENT_CONTENT = new QueryColumn(this, "comment_content");
    public final QueryColumn COMMENT_TYPE = new QueryColumn(this, "comment_type");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, COMMENT_CONTENT, COMMENT_TYPE, SORT_ORDER, STATUS, CREATE_TIME, UPDATE_TIME, DELETED};

    public ReviewQuickCommentTableDef() {
        super("", "review_quick_comment");
    }
}
