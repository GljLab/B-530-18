package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerAddressTableDef extends TableDef {

    public static final CustomerAddressTableDef CUSTOMER_ADDRESS = new CustomerAddressTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn CITY = new QueryColumn(this, "city");

    public final QueryColumn DETAIL = new QueryColumn(this, "detail");

    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn DISTRICT = new QueryColumn(this, "district");

    public final QueryColumn PROVINCE = new QueryColumn(this, "province");

    public final QueryColumn IS_DEFAULT = new QueryColumn(this, "is_default");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn POSTAL_CODE = new QueryColumn(this, "postal_code");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn ADDRESS_TYPE = new QueryColumn(this, "address_type");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, CITY, DETAIL, DELETED, DISTRICT, PROVINCE, IS_DEFAULT, CREATE_TIME, CUSTOMER_ID, POSTAL_CODE, UPDATE_TIME, ADDRESS_TYPE};

    public CustomerAddressTableDef() {
        super("", "customer_address");
    }

}
