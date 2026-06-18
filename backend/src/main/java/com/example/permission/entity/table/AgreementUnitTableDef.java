package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class AgreementUnitTableDef extends TableDef {

    public static final AgreementUnitTableDef AGREEMENT_UNIT = new AgreementUnitTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn UNIT_NAME = new QueryColumn(this, "unit_name");
    public final QueryColumn CREDIT_CODE = new QueryColumn(this, "credit_code");
    public final QueryColumn ADDRESS = new QueryColumn(this, "address");
    public final QueryColumn CONTACT_PHONE = new QueryColumn(this, "contact_phone");
    public final QueryColumn CONTACT_PERSON = new QueryColumn(this, "contact_person");
    public final QueryColumn CONTACT_POSITION = new QueryColumn(this, "contact_position");
    public final QueryColumn CONTACT_MOBILE = new QueryColumn(this, "contact_mobile");
    public final QueryColumn CONTACT_EMAIL = new QueryColumn(this, "contact_email");
    public final QueryColumn COOPERATION_TYPE = new QueryColumn(this, "cooperation_type");
    public final QueryColumn SIGN_DATE = new QueryColumn(this, "sign_date");
    public final QueryColumn VALID_UNTIL = new QueryColumn(this, "valid_until");
    public final QueryColumn SETTLEMENT_CYCLE = new QueryColumn(this, "settlement_cycle");
    public final QueryColumn CREDIT_LIMIT = new QueryColumn(this, "credit_limit");
    public final QueryColumn CURRENT_DEBT = new QueryColumn(this, "current_debt");
    public final QueryColumn DISCOUNT_RATE = new QueryColumn(this, "discount_rate");
    public final QueryColumn AGREEMENT_FILE = new QueryColumn(this, "agreement_file");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, UNIT_NAME, CREDIT_CODE, ADDRESS, CONTACT_PHONE, CONTACT_PERSON, CONTACT_POSITION, CONTACT_MOBILE, CONTACT_EMAIL, COOPERATION_TYPE, SIGN_DATE, VALID_UNTIL, SETTLEMENT_CYCLE, CREDIT_LIMIT, CURRENT_DEBT, DISCOUNT_RATE, AGREEMENT_FILE, STATUS, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public AgreementUnitTableDef() {
        super("", "agreement_unit");
    }
}
