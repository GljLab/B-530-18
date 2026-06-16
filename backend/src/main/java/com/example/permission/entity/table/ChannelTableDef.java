package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ChannelTableDef extends TableDef {

    public static final ChannelTableDef CHANNEL = new ChannelTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn CHANNEL_NAME = new QueryColumn(this, "channel_name");

    public final QueryColumn CHANNEL_CODE = new QueryColumn(this, "channel_code");

    public final QueryColumn CHANNEL_TYPE = new QueryColumn(this, "channel_type");

    public final QueryColumn COOPERATION_STATUS = new QueryColumn(this, "cooperation_status");

    public final QueryColumn COMMISSION_RATE = new QueryColumn(this, "commission_rate");

    public final QueryColumn SETTLEMENT_CYCLE = new QueryColumn(this, "settlement_cycle");

    public final QueryColumn CONTACT_PERSON = new QueryColumn(this, "contact_person");

    public final QueryColumn CONTACT_PHONE = new QueryColumn(this, "contact_phone");

    public final QueryColumn REMARK = new QueryColumn(this, "remark");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHANNEL_NAME, CHANNEL_CODE, CHANNEL_TYPE, COOPERATION_STATUS, COMMISSION_RATE, SETTLEMENT_CYCLE, CONTACT_PERSON, CONTACT_PHONE, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public ChannelTableDef() {
        super("", "channel");
    }
}
