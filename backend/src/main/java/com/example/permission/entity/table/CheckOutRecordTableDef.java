package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class CheckOutRecordTableDef extends TableDef {

    public static final CheckOutRecordTableDef CHECK_OUT_RECORD = new CheckOutRecordTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn CHECK_OUT_NO = new QueryColumn(this, "check_out_no");
    public final QueryColumn CHECK_IN_ID = new QueryColumn(this, "check_in_id");
    public final QueryColumn CHECK_IN_NO = new QueryColumn(this, "check_in_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn ROOM_ID = new QueryColumn(this, "room_id");
    public final QueryColumn ROOM_NUMBER = new QueryColumn(this, "room_number");
    public final QueryColumn CHECK_IN_DATE = new QueryColumn(this, "check_in_date");
    public final QueryColumn CHECK_OUT_DATE = new QueryColumn(this, "check_out_date");
    public final QueryColumn ACTUAL_CHECK_OUT_TIME = new QueryColumn(this, "actual_check_out_time");
    public final QueryColumn STAYED_DAYS = new QueryColumn(this, "stayed_days");
    public final QueryColumn ROOM_TOTAL = new QueryColumn(this, "room_total");
    public final QueryColumn EXTRA_BED_TOTAL = new QueryColumn(this, "extra_bed_total");
    public final QueryColumn OTHER_FEE = new QueryColumn(this, "other_fee");
    public final QueryColumn DAMAGE_COMPENSATION = new QueryColumn(this, "damage_compensation");
    public final QueryColumn DISCOUNT = new QueryColumn(this, "discount");
    public final QueryColumn TOTAL_AMOUNT = new QueryColumn(this, "total_amount");
    public final QueryColumn PAID_AMOUNT = new QueryColumn(this, "paid_amount");
    public final QueryColumn DEPOSIT_AMOUNT = new QueryColumn(this, "deposit_amount");
    public final QueryColumn DEPOSIT_DEDUCTED = new QueryColumn(this, "deposit_deducted");
    public final QueryColumn DEPOSIT_REFUND = new QueryColumn(this, "deposit_refund");
    public final QueryColumn ADDITIONAL_PAYMENT = new QueryColumn(this, "additional_payment");
    public final QueryColumn REFUND_AMOUNT = new QueryColumn(this, "refund_amount");
    public final QueryColumn PAYABLE_AMOUNT = new QueryColumn(this, "payable_amount");
    public final QueryColumn PAYMENT_METHOD = new QueryColumn(this, "payment_method");
    public final QueryColumn PAYMENT_VOUCHER_NO = new QueryColumn(this, "payment_voucher_no");
    public final QueryColumn DEPOSIT_METHOD = new QueryColumn(this, "deposit_method");
    public final QueryColumn KEY_CARD_RETURNED = new QueryColumn(this, "key_card_returned");
    public final QueryColumn KEY_CARD_LOST = new QueryColumn(this, "key_card_lost");
    public final QueryColumn ROOM_CHECKED = new QueryColumn(this, "room_checked");
    public final QueryColumn ROOM_CHECK_RESULT = new QueryColumn(this, "room_check_result");
    public final QueryColumn DAMAGE_ITEMS = new QueryColumn(this, "damage_items");
    public final QueryColumn DAMAGE_DESCRIPTION = new QueryColumn(this, "damage_description");
    public final QueryColumn DAMAGE_PHOTOS = new QueryColumn(this, "damage_photos");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn OPERATOR_ID = new QueryColumn(this, "operator_id");
    public final QueryColumn OPERATOR_NAME = new QueryColumn(this, "operator_name");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CHECK_OUT_NO, CHECK_IN_ID, CHECK_IN_NO, CUSTOMER_ID, CUSTOMER_NAME, ROOM_ID, ROOM_NUMBER, CHECK_IN_DATE, CHECK_OUT_DATE, ACTUAL_CHECK_OUT_TIME, STAYED_DAYS, ROOM_TOTAL, EXTRA_BED_TOTAL, OTHER_FEE, DAMAGE_COMPENSATION, DISCOUNT, TOTAL_AMOUNT, PAID_AMOUNT, DEPOSIT_AMOUNT, DEPOSIT_DEDUCTED, DEPOSIT_REFUND, ADDITIONAL_PAYMENT, REFUND_AMOUNT, PAYABLE_AMOUNT, PAYMENT_METHOD, PAYMENT_VOUCHER_NO, DEPOSIT_METHOD, KEY_CARD_RETURNED, KEY_CARD_LOST, ROOM_CHECKED, ROOM_CHECK_RESULT, DAMAGE_ITEMS, DAMAGE_DESCRIPTION, DAMAGE_PHOTOS, STATUS, OPERATOR_ID, OPERATOR_NAME, REMARK, CREATE_TIME};

    public CheckOutRecordTableDef() {
        super("", "check_out_record");
    }
}
