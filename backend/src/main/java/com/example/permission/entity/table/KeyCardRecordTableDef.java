package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class KeyCardRecordTableDef extends TableDef {

    public static final KeyCardRecordTableDef KEY_CARD_RECORD = new KeyCardRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");
    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");
    public final QueryColumn CARD_NO = new QueryColumn(this, "card_no");
    public final QueryColumn CARD_TYPE = new QueryColumn(this, "card_type");
    public final QueryColumn ISSUE_TIME = new QueryColumn(this, "issue_time");
    public final QueryColumn EXPIRE_TIME = new QueryColumn(this, "expire_time");
    public final QueryColumn RETURN_TIME = new QueryColumn(this, "return_time");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn RETURN_OPERATOR_ID = new QueryColumn(this, "return_operator_id");
    public final QueryColumn RETURN_OPERATOR_NAME = new QueryColumn(this, "return_operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_IN_ID, CHECK_IN_NO, ROOM_ID, ROOM_NUMBER, CARD_NO, CARD_TYPE, ISSUE_TIME, EXPIRE_TIME, RETURN_TIME, STATUS, OPERATOR_ID, OPERATOR_NAME, RETURN_OPERATOR_ID, RETURN_OPERATOR_NAME, REMARK, CREATE_TIME, UPDATE_TIME};

    public KeyCardRecordTableDef() {
        super("", "key_card_record");
    }
}
