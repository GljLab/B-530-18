package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class BookingTableDef extends TableDef {

    public static final BookingTableDef BOOKING = new BookingTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn DAYS = new QueryColumn(this, "days");

    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn DISCOUNT = new QueryColumn(this, "discount");

    public final QueryColumn OTHER_FEE = new QueryColumn(this, "other_fee");

    public final QueryColumn BOOKING_NO = new QueryColumn(this, "booking_no");

    public final QueryColumn ROOM_PRICE = new QueryColumn(this, "room_price");

    public final QueryColumn ROOM_TOTAL = new QueryColumn(this, "room_total");

    public final QueryColumn CANCEL_TIME = new QueryColumn(this, "cancel_time");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn GUEST_COUNT = new QueryColumn(this, "guest_count");

    public final QueryColumn GUEST_NAMES = new QueryColumn(this, "guest_names");

    public final QueryColumn GUEST_PHONE = new QueryColumn(this, "guest_phone");

    public final QueryColumn PAID_AMOUNT = new QueryColumn(this, "paid_amount");

    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");

    public final QueryColumn ROOM_TYPE_ID = new QueryColumn(this, "room_type_id");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");

    public final QueryColumn CHECK_IN_TIME = new QueryColumn(this, "check_in_time");

    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");

    public final QueryColumn CANCEL_DETAIL = new QueryColumn(this, "cancel_detail");

    public final QueryColumn CANCEL_REASON = new QueryColumn(this, "cancel_reason");

    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");

    public final QueryColumn CHECK_OUT_TIME = new QueryColumn(this, "check_out_time");

    public final QueryColumn CREATE_USER_ID = new QueryColumn(this, "create_user_id");

    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");

    public final QueryColumn ROOM_TYPE_NAME = new QueryColumn(this, "room_type_name");

    public final QueryColumn SOURCE_REMARK = new QueryColumn(this, "source_remark");

    public final QueryColumn BOOKING_SOURCE = new QueryColumn(this, "booking_source");

    public final QueryColumn CANCEL_PENALTY = new QueryColumn(this, "cancel_penalty");

    public final QueryColumn CUSTOMER_PHONE = new QueryColumn(this, "customer_phone");

    public final QueryColumn EXTRA_BED_COUNT = new QueryColumn(this, "extra_bed_count");

    public final QueryColumn EXTRA_BED_PRICE = new QueryColumn(this, "extra_bed_price");

    public final QueryColumn EXTRA_BED_TOTAL = new QueryColumn(this, "extra_bed_total");

    public final QueryColumn GUARANTEE_TYPE = new QueryColumn(this, "guarantee_type");

    public final QueryColumn CREATE_USER_NAME = new QueryColumn(this, "create_user_name");

    public final QueryColumn CANCEL_OPERATOR_ID = new QueryColumn(this, "cancel_operator_id");

    public final QueryColumn CANCEL_OPERATOR_NAME = new QueryColumn(this, "cancel_operator_name");

    public final QueryColumn EXPECTED_ARRIVAL_TIME = new QueryColumn(this, "expected_arrival_time");

    public final QueryColumn SPECIAL_REQUIREMENTS = new QueryColumn(this, "special_requirements");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, DAYS, ROOM_ID, STATUS, DELETED, DISCOUNT, OTHER_FEE, BOOKING_NO, ROOM_PRICE, ROOM_TOTAL, CANCEL_TIME, CREATE_TIME, CUSTOMER_ID, GUEST_COUNT, GUEST_NAMES, GUEST_PHONE, PAID_AMOUNT, ROOM_NUMBER, ROOM_TYPE_ID, UPDATE_TIME, CHECK_IN_DATE, CHECK_IN_TIME, TOTAL_AMOUNT, CANCEL_DETAIL, CANCEL_REASON, CHECK_OUT_DATE, CHECK_OUT_TIME, CREATE_USER_ID, CUSTOMER_NAME, ROOM_TYPE_NAME, SOURCE_REMARK, BOOKING_SOURCE, CANCEL_PENALTY, CUSTOMER_PHONE, EXTRA_BED_COUNT, EXTRA_BED_PRICE, EXTRA_BED_TOTAL, GUARANTEE_TYPE, CREATE_USER_NAME, CANCEL_OPERATOR_ID, CANCEL_OPERATOR_NAME, EXPECTED_ARRIVAL_TIME, SPECIAL_REQUIREMENTS};

    public BookingTableDef() {
        super("", "booking");
    }

}
