package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class BookingRuleExemptionTableDef extends TableDef {

    public static final BookingRuleExemptionTableDef BOOKING_RULE_EXEMPTION = new BookingRuleExemptionTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");

    public final QueryColumn BOOKING_ID = new QueryColumn(this, "booking_id");

    public final QueryColumn RULE_ID = new QueryColumn(this, "rule_id");

    public final QueryColumn RULE_NAME = new QueryColumn(this, "rule_name");

    public final QueryColumn EXEMPTION_REASON = new QueryColumn(this, "exemption_reason");

    public final QueryColumn EXEMPTED_BY_ID = new QueryColumn(this, "exempted_by_id");

    public final QueryColumn EXEMPTED_BY_NAME = new QueryColumn(this, "exempted_by_name");

    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");

    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, BOOKING_ID, RULE_ID, RULE_NAME, EXEMPTION_REASON, EXEMPTED_BY_ID, EXEMPTED_BY_NAME, CREATE_TIME};

    public BookingRuleExemptionTableDef() {
        super("", "booking_rule_exemption");
    }
}
