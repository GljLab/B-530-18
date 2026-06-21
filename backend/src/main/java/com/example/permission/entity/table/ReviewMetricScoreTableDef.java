package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ReviewMetricScoreTableDef extends TableDef {

    public static final ReviewMetricScoreTableDef REVIEW_METRIC_SCORE = new ReviewMetricScoreTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn REVIEW_RECORD_ID = new QueryColumn(this, "review_record_id");
    public final QueryColumn METRIC_ID = new QueryColumn(this, "metric_id");
    public final QueryColumn METRIC_NAME = new QueryColumn(this, "metric_name");
    public final QueryColumn SCORE = new QueryColumn(this, "score");
    public final QueryColumn WEIGHT = new QueryColumn(this, "weight");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, REVIEW_RECORD_ID, METRIC_ID, METRIC_NAME, SCORE, WEIGHT, CREATE_TIME};

    public ReviewMetricScoreTableDef() {
        super("", "review_metric_score");
    }
}
