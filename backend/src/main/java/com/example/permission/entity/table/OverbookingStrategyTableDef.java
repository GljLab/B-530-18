package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class OverbookingStrategyTableDef extends TableDef {

    public static final OverbookingStrategyTableDef OVERBOOKING_STRATEGY = new OverbookingStrategyTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");

    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");

    public final QueryColumn OVERBOOKING_RATIO = new QueryColumn(this, "overbooking_ratio");

    public final QueryColumn MAX_OVERBOOKING = new QueryColumn(this, "max_overbooking");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ROOM_TYPE_ID, ENABLED, OVERBOOKING_RATIO, MAX_OVERBOOKING, CREATE_TIME, UPDATE_TIME};

    public OverbookingStrategyTableDef() {
        super("", "overbooking_strategy");
    }
}
