package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class ExtendStayRecordTableDef extends TableDef {

    public static final ExtendStayRecordTableDef EXTEND_STAY_RECORD = new ExtendStayRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn EXTEND_NO = new QueryColumn(this, "extend_no");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");
    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");
    public final QueryColumn ORIGINAL_CHECK_OUT_DATE = new QueryColumn(this, "original_check_out_date");
    public final QueryColumn NEW_CHECK_OUT_DATE = new QueryColumn(this, "new_check_out_date");
    public final QueryColumn EXTEND_DAYS = new QueryColumn(this, "extend_days");
    public final QueryColumn ROOM_PRICE = new QueryColumn(this, "room_price");
    public final QueryColumn EXTEND_AMOUNT = new QueryColumn(this, "extend_amount");
    public final QueryColumn REASON = new QueryColumn(this, "reason");
    public final QueryColumn EXTEND_TIME = new QueryColumn(this, "extend_time");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, EXTEND_NO, CHECK_IN_ID, CHECK_IN_NO, ROOM_ID, ROOM_NUMBER, ORIGINAL_CHECK_OUT_DATE, NEW_CHECK_OUT_DATE, EXTEND_DAYS, ROOM_PRICE, EXTEND_AMOUNT, REASON, EXTEND_TIME, OPERATOR_ID, OPERATOR_NAME, REMARK, CREATE_TIME};

    public ExtendStayRecordTableDef() {
        super("", "extend_stay_record");
    }
}
