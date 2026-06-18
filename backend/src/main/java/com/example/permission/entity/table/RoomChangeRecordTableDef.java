package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class RoomChangeRecordTableDef extends TableDef {

    public static final RoomChangeRecordTableDef ROOM_CHANGE_RECORD = new RoomChangeRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHANGE_NO = new QueryColumn(this, "change_no");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn OLD_ROOM_ID = new QueryColumn(this, "old_room_id");
    public final QueryColumn OLD_ROOM_NUMBER = new QueryColumn(this, "old_room_number");
    public final QueryColumn OLD_ROOM_TYPE_ID = new QueryColumn(this, "old_room_type_id");
    public final QueryColumn OLD_ROOM_TYPE_NAME = new QueryColumn(this, "old_room_type_name");
    public final QueryColumn OLD_ROOM_PRICE = new QueryColumn(this, "old_room_price");
    public final QueryColumn NEW_ROOM_ID = new QueryColumn(this, "new_room_id");
    public final QueryColumn NEW_ROOM_NUMBER = new QueryColumn(this, "new_room_number");
    public final QueryColumn NEW_ROOM_TYPE_ID = new QueryColumn(this, "new_room_type_id");
    public final QueryColumn NEW_ROOM_TYPE_NAME = new QueryColumn(this, "new_room_type_name");
    public final QueryColumn NEW_ROOM_PRICE = new QueryColumn(this, "new_room_price");
    public final QueryColumn PRICE_DIFF = new QueryColumn(this, "price_diff");
    public final QueryColumn CHANGE_REASON = new QueryColumn(this, "change_reason");
    public final QueryColumn CHANGE_DETAIL = new QueryColumn(this, "change_detail");
    public final QueryColumn CHANGE_TIME = new QueryColumn(this, "change_time");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHANGE_NO, CHECK_IN_ID, CHECK_IN_NO, OLD_ROOM_ID, OLD_ROOM_NUMBER, OLD_ROOM_TYPE_ID, OLD_ROOM_TYPE_NAME, OLD_ROOM_PRICE, NEW_ROOM_ID, NEW_ROOM_NUMBER, NEW_ROOM_TYPE_ID, NEW_ROOM_TYPE_NAME, NEW_ROOM_PRICE, PRICE_DIFF, CHANGE_REASON, CHANGE_DETAIL, CHANGE_TIME, OPERATOR_ID, OPERATOR_NAME, REMARK, CREATE_TIME};

    public RoomChangeRecordTableDef() {
        super("", "room_change_record");
    }
}
