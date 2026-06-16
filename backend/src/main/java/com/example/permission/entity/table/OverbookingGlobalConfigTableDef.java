package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class OverbookingGlobalConfigTableDef extends TableDef {

    public static final OverbookingGlobalConfigTableDef OVERBOOKING_GLOBAL_CONFIG = new OverbookingGlobalConfigTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn ENABLED = new QueryColumn(this, "enabled");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ENABLED, CREATE_TIME, UPDATE_TIME};

    public OverbookingGlobalConfigTableDef() {
        super("", "overbooking_global_config");
    }
}
