package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CheckInGuestTableDef extends TableDef {

    public static final CheckInGuestTableDef CHECK_IN_GUEST = new CheckInGuestTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn IS_MAIN = new QueryColumn(this, "is_main");
    public final QueryColumn NAME = new QueryColumn(this, "name");
    public final QueryColumn GENDER = new QueryColumn(this, "gender");
    public final QueryColumn ID_TYPE = new QueryColumn(this, "id_type");
    public final QueryColumn ID_NUMBER = new QueryColumn(this, "id_number");
    public final QueryColumn ID_EXPIRY_DATE = new QueryColumn(this, "id_expiry_date");
    public final QueryColumn ID_PHOTO_FRONT = new QueryColumn(this, "id_photo_front");
    public final QueryColumn ID_PHOTO_BACK = new QueryColumn(this, "id_photo_back");
    public final QueryColumn PHONE = new QueryColumn(this, "phone");
    public final QueryColumn NATIONALITY = new QueryColumn(this, "nationality");
    public final QueryColumn ADDRESS = new QueryColumn(this, "address");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_IN_ID, CHECK_IN_NO, IS_MAIN, NAME, GENDER, ID_TYPE, ID_NUMBER, ID_EXPIRY_DATE, ID_PHOTO_FRONT, ID_PHOTO_BACK, PHONE, NATIONALITY, ADDRESS, REMARK, CREATE_TIME, UPDATE_TIME};

    public CheckInGuestTableDef() {
        super("", "check_in_guest");
    }
}
