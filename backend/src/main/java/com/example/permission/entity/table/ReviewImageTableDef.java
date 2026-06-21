package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewImageTableDef extends TableDef {

    public static final ReviewImageTableDef REVIEW_IMAGE = new ReviewImageTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn REVIEW_RECORD_ID = new QueryColumn(this, "review_record_id");
    public final QueryColumn IMAGE_URL = new QueryColumn(this, "image_url");
    public final QueryColumn IMAGE_NAME = new QueryColumn(this, "image_name");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REVIEW_RECORD_ID, IMAGE_URL, IMAGE_NAME, SORT_ORDER, CREATE_TIME};

    public ReviewImageTableDef() {
        super("", "review_image");
    }
}
