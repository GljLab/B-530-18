package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ChannelPriceTableDef extends TableDef {

    public static final ChannelPriceTableDef CHANNEL_PRICE = new ChannelPriceTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn CHANNEL_ID = new QueryColumn(this, "channel_id");

    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");

    public final QueryColumn DATE = new QueryColumn(this, "date");

    public final QueryColumn PRICE_MODE = new QueryColumn(this, "price_mode");

    public final QueryColumn PRICE_VALUE = new QueryColumn(this, "price_value");

    public final QueryColumn FINAL_PRICE = new QueryColumn(this, "final_price");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHANNEL_ID, ROOM_TYPE_ID, DATE, PRICE_MODE, PRICE_VALUE, FINAL_PRICE, CREATE_TIME, UPDATE_TIME};

    public ChannelPriceTableDef() {
        super("", "channel_price");
    }
}
