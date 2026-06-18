package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CheckInTableDef extends TableDef {

    public static final CheckInTableDef CHECK_IN = new CheckInTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn BOOKING_ID = new QueryColumn(this, "booking_id");
    public final QueryColumn BOOKING_NO = new QueryColumn(this, "booking_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn CUSTOMER_PHONE = new QueryColumn(this, "customer_phone");
    public final QueryColumn CUSTOMER_TYPE = new QueryColumn(this, "customer_type");
    public final QueryColumn MEMBER_LEVEL = new QueryColumn(this, "member_level");
    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");
    public final QueryColumn ROOM_TYPE_NAME = new QueryColumn(this, "room_type_name");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");
    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");
    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");
    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");
    public final QueryColumn ACTUAL_CHECK_IN_TIME = new QueryColumn(this, "actual_check_in_time");
    public final QueryColumn ACTUAL_CHECK_OUT_TIME = new QueryColumn(this, "actual_check_out_time");
    public final QueryColumn DAYS = new QueryColumn(this, "days");
    public final QueryColumn STAYED_DAYS = new QueryColumn(this, "stayed_days");
    public final QueryColumn ROOM_PRICE = new QueryColumn(this, "room_price");
    public final QueryColumn ROOM_TOTAL = new QueryColumn(this, "room_total");
    public final QueryColumn EXTRA_BED_COUNT = new QueryColumn(this, "extra_bed_count");
    public final QueryColumn EXTRA_BED_PRICE = new QueryColumn(this, "extra_bed_price");
    public final QueryColumn EXTRA_BED_TOTAL = new QueryColumn(this, "extra_bed_total");
    public final QueryColumn OTHER_FEE = new QueryColumn(this, "other_fee");
    public final QueryColumn DISCOUNT = new QueryColumn(this, "discount");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn PAID_AMOUNT = new QueryColumn(this, "paid_amount");
    public final QueryColumn PAYABLE_AMOUNT = new QueryColumn(this, "payable_amount");
    public final QueryColumn DEPOSIT_AMOUNT = new QueryColumn(this, "deposit_amount");
    public final QueryColumn DEPOSIT_METHOD = new QueryColumn(this, "deposit_method");
    public final QueryColumn DEPOSIT_VOUCHER_NO = new QueryColumn(this, "deposit_voucher_no");
    public final QueryColumn DEPOSIT_AUTHORIZED_BY = new QueryColumn(this, "deposit_authorized_by");
    public final QueryColumn DEPOSIT_AUTHORIZED_NAME = new QueryColumn(this, "deposit_authorized_name");
    public final QueryColumn KEY_CARD_COUNT = new QueryColumn(this, "key_card_count");
    public final QueryColumn KEY_CARD_RETURNED = new QueryColumn(this, "key_card_returned");
    public final QueryColumn GUEST_COUNT = new QueryColumn(this, "guest_count");
    public final QueryColumn SPECIAL_REQUIREMENTS = new QueryColumn(this, "special_requirements");
    public final QueryColumn BOOKING_SOURCE = new QueryColumn(this, "booking_source");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn IS_OVERDUE = new QueryColumn(this, "is_overdue");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn CHECK_OUT_OPERATOR_ID = new QueryColumn(this, "check_out_operator_id");
    public final QueryColumn CHECK_OUT_OPERATOR_NAME = new QueryColumn(this, "check_out_operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_IN_NO, BOOKING_ID, BOOKING_NO, CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_PHONE, CUSTOMER_TYPE, MEMBER_LEVEL, ROOM_TYPE_ID, ROOM_TYPE_NAME, ROOM_ID, ROOM_NUMBER, CHECK_IN_DATE, CHECK_OUT_DATE, ACTUAL_CHECK_IN_TIME, ACTUAL_CHECK_OUT_TIME, DAYS, STAYED_DAYS, ROOM_PRICE, ROOM_TOTAL, EXTRA_BED_COUNT, EXTRA_BED_PRICE, EXTRA_BED_TOTAL, OTHER_FEE, DISCOUNT, TOTAL_AMOUNT, PAID_AMOUNT, PAYABLE_AMOUNT, DEPOSIT_AMOUNT, DEPOSIT_METHOD, DEPOSIT_VOUCHER_NO, DEPOSIT_AUTHORIZED_BY, DEPOSIT_AUTHORIZED_NAME, KEY_CARD_COUNT, KEY_CARD_RETURNED, GUEST_COUNT, SPECIAL_REQUIREMENTS, BOOKING_SOURCE, STATUS, IS_OVERDUE, OPERATOR_ID, OPERATOR_NAME, CHECK_OUT_OPERATOR_ID, CHECK_OUT_OPERATOR_NAME, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public CheckInTableDef() {
        super("", "check_in");
    }
}
