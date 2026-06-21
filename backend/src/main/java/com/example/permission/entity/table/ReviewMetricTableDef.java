package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewMetricTableDef extends TableDef {

    public static final ReviewMetricTableDef REVIEW_METRIC = new ReviewMetricTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn METRIC_NAME = new QueryColumn(this, "metric_name");
    public final QueryColumn METRIC_DESC = new QueryColumn(this, "metric_desc");
    public final QueryColumn SCORE_MIN = new QueryColumn(this, "score_min");
    public final QueryColumn SCORE_MAX = new QueryColumn(this, "score_max");
    public final QueryColumn WEIGHT = new QueryColumn(this, "weight");
    public final QueryColumn IS_REQUIRED = new QueryColumn(this, "is_required");
    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, METRIC_NAME, METRIC_DESC, SCORE_MIN, SCORE_MAX, WEIGHT, IS_REQUIRED, SORT_ORDER, STATUS, CREATE_TIME, UPDATE_TIME, DELETED};

    public ReviewMetricTableDef() {
        super("", "review_metric");
    }
}
