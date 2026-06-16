package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ChannelInventoryTableDef extends TableDef {

    public static final ChannelInventoryTableDef CHANNEL_INVENTORY = new ChannelInventoryTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn CHANNEL_ID = new QueryColumn(this, "channel_id");

    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");

    public final QueryColumn DATE = new QueryColumn(this, "date");

    public final QueryColumn ALLOCATED_ROOMS = new QueryColumn(this, "allocated_rooms");

    public final QueryColumn USED_ROOMS = new QueryColumn(this, "used_rooms");

    public final QueryColumn SHARE_MODE = new QueryColumn(this, "share_mode");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHANNEL_ID, ROOM_TYPE_ID, DATE, ALLOCATED_ROOMS, USED_ROOMS, SHARE_MODE, CREATE_TIME, UPDATE_TIME};

    public ChannelInventoryTableDef() {
        super("", "channel_inventory");
    }
}
