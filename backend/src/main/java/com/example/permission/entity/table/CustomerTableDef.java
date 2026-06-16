package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerTableDef extends TableDef {

    public static final CustomerTableDef CUSTOMER = new CustomerTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn NAME = new QueryColumn(this, "name");

    public final QueryColumn EMAIL = new QueryColumn(this, "email");

    public final QueryColumn PHONE = new QueryColumn(this, "phone");

    public final QueryColumn AVATAR = new QueryColumn(this, "avatar");

    public final QueryColumn GENDER = new QueryColumn(this, "gender");

    public final QueryColumn ID_TYPE = new QueryColumn(this, "id_type");

    public final QueryColumn STATUS = new QueryColumn(this, "status");

    public final QueryColumn WECHAT = new QueryColumn(this, "wechat");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ID_NUMBER = new QueryColumn(this, "id_number");

    public final QueryColumn BIRTH_DATE = new QueryColumn(this, "birth_date");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn FREEZE_TIME = new QueryColumn(this, "freeze_time");

    public final QueryColumn IMPORTANCE = new QueryColumn(this, "importance");

    public final QueryColumn REFERRER_ID = new QueryColumn(this, "referrer_id");

    public final QueryColumn TOTAL_SPENT = new QueryColumn(this, "total_spent");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn BACKUP_PHONE = new QueryColumn(this, "backup_phone");

    public final QueryColumn ID_PHOTO_BACK = new QueryColumn(this, "id_photo_back");

    public final QueryColumn NATIONALITY = new QueryColumn(this, "nationality");

    public final QueryColumn TOTAL_ORDERS = new QueryColumn(this, "total_orders");

    public final QueryColumn CUSTOMER_TYPE = new QueryColumn(this, "customer_type");

    public final QueryColumn FREEZE_REASON = new QueryColumn(this, "freeze_reason");

    public final QueryColumn ID_EXPIRY_DATE = new QueryColumn(this, "id_expiry_date");

    public final QueryColumn ID_PHOTO_FRONT = new QueryColumn(this, "id_photo_front");

    public final QueryColumn UNFREEZE_TIME = new QueryColumn(this, "unfreeze_time");

    public final QueryColumn LAST_ORDER_TIME = new QueryColumn(this, "last_order_time");

    public final QueryColumn CUSTOMER_SOURCE = new QueryColumn(this, "customer_source");

    public final QueryColumn FIRST_ORDER_TIME = new QueryColumn(this, "first_order_time");

    public final QueryColumn UNFREEZE_REASON = new QueryColumn(this, "unfreeze_reason");

    public final QueryColumn FREEZE_OPERATOR_ID = new QueryColumn(this, "freeze_operator_id");

    public final QueryColumn FREEZE_OPERATOR_NAME = new QueryColumn(this, "freeze_operator_name");

    public final QueryColumn UNFREEZE_OPERATOR_ID = new QueryColumn(this, "unfreeze_operator_id");

    public final QueryColumn EMERGENCY_CONTACT_NAME = new QueryColumn(this, "emergency_contact_name");

    public final QueryColumn UNFREEZE_OPERATOR_NAME = new QueryColumn(this, "unfreeze_operator_name");

    public final QueryColumn EMERGENCY_CONTACT_PHONE = new QueryColumn(this, "emergency_contact_phone");

    public final QueryColumn EMERGENCY_CONTACT_RELATION = new QueryColumn(this, "emergency_contact_relation");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, NAME, EMAIL, PHONE, AVATAR, GENDER, ID_TYPE, STATUS, WECHAT, DELETED, ID_NUMBER, BIRTH_DATE, CREATE_TIME, FREEZE_TIME, IMPORTANCE, REFERRER_ID, TOTAL_SPENT, UPDATE_TIME, BACKUP_PHONE, ID_PHOTO_BACK, NATIONALITY, TOTAL_ORDERS, CUSTOMER_TYPE, FREEZE_REASON, ID_EXPIRY_DATE, ID_PHOTO_FRONT, UNFREEZE_TIME, LAST_ORDER_TIME, CUSTOMER_SOURCE, FIRST_ORDER_TIME, UNFREEZE_REASON, FREEZE_OPERATOR_ID, FREEZE_OPERATOR_NAME, UNFREEZE_OPERATOR_ID, EMERGENCY_CONTACT_NAME, UNFREEZE_OPERATOR_NAME, EMERGENCY_CONTACT_PHONE, EMERGENCY_CONTACT_RELATION};

    public CustomerTableDef() {
        super("", "customer");
    }

}
