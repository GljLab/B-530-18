package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class MemberLevelTableDef extends TableDef {

    public static final MemberLevelTableDef MEMBER_LEVEL = new MemberLevelTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn LEVEL_NAME = new QueryColumn(this, "level_name");

    public final QueryColumn LEVEL_CODE = new QueryColumn(this, "level_code");

    public final QueryColumn LEVEL_ICON = new QueryColumn(this, "level_icon");

    public final QueryColumn LEVEL_COLOR = new QueryColumn(this, "level_color");

    public final QueryColumn SORT_ORDER = new QueryColumn(this, "sort_order");

    public final QueryColumn UPGRADE_TYPE = new QueryColumn(this, "upgrade_type");

    public final QueryColumn UPGRADE_CONDITION = new QueryColumn(this, "upgrade_condition");

    public final QueryColumn KEEP_CONDITION = new QueryColumn(this, "keep_condition");

    public final QueryColumn ROOM_DISCOUNT = new QueryColumn(this, "room_discount");

    public final QueryColumn DINING_DISCOUNT = new QueryColumn(this, "dining_discount");

    public final QueryColumn POINT_RATE = new QueryColumn(this, "point_rate");

    public final QueryColumn DEPOSIT_REDUCTION = new QueryColumn(this, "deposit_reduction");

    public final QueryColumn SERVICES = new QueryColumn(this, "services");

    public final QueryColumn OTHER_BENEFITS = new QueryColumn(this, "other_benefits");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, LEVEL_NAME, LEVEL_CODE, LEVEL_ICON, LEVEL_COLOR, SORT_ORDER, UPGRADE_TYPE, UPGRADE_CONDITION, KEEP_CONDITION, ROOM_DISCOUNT, DINING_DISCOUNT, POINT_RATE, DEPOSIT_REDUCTION, SERVICES, OTHER_BENEFITS, STATUS, CREATE_TIME, UPDATE_TIME, DELETED};

    public MemberLevelTableDef() {
        super("", "member_level");
    }

}
