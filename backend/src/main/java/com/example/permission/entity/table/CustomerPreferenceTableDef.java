package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

// Auto generate by mybatis-flex, do not modify it.
public class CustomerPreferenceTableDef extends TableDef {

    public static final CustomerPreferenceTableDef CUSTOMER_PREFERENCE = new CustomerPreferenceTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn DIET_HALAL = new QueryColumn(this, "diet_halal");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");

    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");

    public final QueryColumn DIET_NO_SPICY = new QueryColumn(this, "diet_no_spicy");

    public final QueryColumn SPECIAL_NEEDS = new QueryColumn(this, "special_needs");

    public final QueryColumn PREFERRED_VIEW = new QueryColumn(this, "preferred_view");

    public final QueryColumn DIET_VEGETARIAN = new QueryColumn(this, "diet_vegetarian");

    public final QueryColumn PREFERRED_FLOOR = new QueryColumn(this, "preferred_floor");

    public final QueryColumn DIET_OTHER_ALLERGY = new QueryColumn(this, "diet_other_allergy");

    public final QueryColumn PREFERRED_BED_TYPE = new QueryColumn(this, "preferred_bed_type");

    public final QueryColumn PREFERRED_ROOM_TYPE = new QueryColumn(this, "preferred_room_type");

    public final QueryColumn SERVICE_PREFERENCE = new QueryColumn(this, "service_preference");

    public final QueryColumn DIET_SEAFOOD_ALLERGY = new QueryColumn(this, "diet_seafood_allergy");

    public final QueryColumn PREFERRED_ORIENTATION = new QueryColumn(this, "preferred_orientation");

    /**
     * 所有字段。
     */
    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    /**
     * 默认字段，不包含逻辑删除或者 large 等字段。
     */
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, DIET_HALAL, CREATE_TIME, CUSTOMER_ID, UPDATE_TIME, DIET_NO_SPICY, SPECIAL_NEEDS, PREFERRED_VIEW, DIET_VEGETARIAN, PREFERRED_FLOOR, DIET_OTHER_ALLERGY, PREFERRED_BED_TYPE, PREFERRED_ROOM_TYPE, SERVICE_PREFERENCE, DIET_SEAFOOD_ALLERGY, PREFERRED_ORIENTATION};

    public CustomerPreferenceTableDef() {
        super("", "customer_preference");
    }

}
