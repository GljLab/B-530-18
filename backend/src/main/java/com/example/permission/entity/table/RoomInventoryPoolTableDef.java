package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class RoomInventoryPoolTableDef extends TableDef {

    public static final RoomInventoryPoolTableDef ROOM_INVENTORY_POOL = new RoomInventoryPoolTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");

    public final QueryColumn DATE = new QueryColumn(this, "date");

    public final QueryColumn TOTAL_ROOMS = new QueryColumn(this, "total_rooms");

    public final QueryColumn AVAILABLE_ROOMS = new QueryColumn(this, "available_rooms");

    public final QueryColumn RESERVED_ROOMS = new QueryColumn(this, "reserved_rooms");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, ROOM_TYPE_ID, DATE, TOTAL_ROOMS, AVAILABLE_ROOMS, RESERVED_ROOMS, CREATE_TIME, UPDATE_TIME};

    public RoomInventoryPoolTableDef() {
        super("", "room_inventory_pool");
    }
}
