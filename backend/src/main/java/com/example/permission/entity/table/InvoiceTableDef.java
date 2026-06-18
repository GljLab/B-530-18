package com.example.permission.entity.table;

import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.table.TableDef;

public class InvoiceTableDef extends TableDef {

    public static final InvoiceTableDef INVOICE = new InvoiceTableDef();

    public final QueryColumn ID = new QueryColumn(this, "id");
    public final QueryColumn INVOICE_NO = new QueryColumn(this, "invoice_no");
    public final QueryColumn RELATED_ORDER_TYPE = new QueryColumn(this, "related_order_type");
    public final QueryColumn RELATED_ORDER_ID = new QueryColumn(this, "related_order_id");
    public final QueryColumn RELATED_ORDER_NO = new QueryColumn(this, "related_order_no");
    public final QueryColumn CUSTOMER_ID = new QueryColumn(this, "customer_id");
    public final QueryColumn CUSTOMER_NAME = new QueryColumn(this, "customer_name");
    public final QueryColumn INVOICE_TYPE = new QueryColumn(this, "invoice_type");
    public final QueryColumn TITLE_TYPE = new QueryColumn(this, "title_type");
    public final QueryColumn INVOICE_TITLE = new QueryColumn(this, "invoice_title");
    public final QueryColumn TAX_NO = new QueryColumn(this, "tax_no");
    public final QueryColumn COMPANY_ADDRESS = new QueryColumn(this, "company_address");
    public final QueryColumn BANK_ACCOUNT = new QueryColumn(this, "bank_account");
    public final QueryColumn INVOICE_AMOUNT = new QueryColumn(this, "invoice_amount");
    public final QueryColumn ORDER_AMOUNT = new QueryColumn(this, "order_amount");
    public final QueryColumn INVOICE_ITEM = new QueryColumn(this, "invoice_item");
    public final QueryColumn CONTACT_PHONE = new QueryColumn(this, "contact_phone");
    public final QueryColumn CONTACT_EMAIL = new QueryColumn(this, "contact_email");
    public final QueryColumn INVOICE_CODE = new QueryColumn(this, "invoice_code");
    public final QueryColumn INVOICE_NUMBER = new QueryColumn(this, "invoice_number");
    public final QueryColumn INVOICE_PDF = new QueryColumn(this, "invoice_pdf");
    public final QueryColumn IS_ELECTRONIC = new QueryColumn(this, "is_electronic");
    public final QueryColumn EMAIL_SENT = new QueryColumn(this, "email_sent");
    public final QueryColumn MAIL_RECIPIENT = new QueryColumn(this, "mail_recipient");
    public final QueryColumn MAIL_ADDRESS = new QueryColumn(this, "mail_address");
    public final QueryColumn MAIL_COMPANY = new QueryColumn(this, "mail_company");
    public final QueryColumn MAIL_TRACKING_NO = new QueryColumn(this, "mail_tracking_no");
    public final QueryColumn MAIL_TIME = new QueryColumn(this, "mail_time");
    public final QueryColumn APPLICANT_ID = new QueryColumn(this, "applicant_id");
    public final QueryColumn APPLICANT_NAME = new QueryColumn(this, "applicant_name");
    public final QueryColumn APPLY_TIME = new QueryColumn(this, "apply_time");
    public final QueryColumn PROCESSOR_ID = new QueryColumn(this, "processor_id");
    public final QueryColumn PROCESSOR_NAME = new QueryColumn(this, "processor_name");
    public final QueryColumn PROCESS_TIME = new QueryColumn(this, "process_time");
    public final QueryColumn STATUS = new QueryColumn(this, "status");
    public final QueryColumn REMARK = new QueryColumn(this, "remark");
    public final QueryColumn CREATE_TIME = new QueryColumn(this, "create_time");
    public final QueryColumn UPDATE_TIME = new QueryColumn(this, "update_time");
    public final QueryColumn DELETED = new QueryColumn(this, "deleted");

    public final QueryColumn ALL_COLUMNS = new QueryColumn(this, "*");
    public final QueryColumn[] DEFAULT_COLUMNS = new QueryColumn[]{ID, INVOICE_NO, RELATED_ORDER_TYPE, RELATED_ORDER_ID, RELATED_ORDER_NO, CUSTOMER_ID, CUSTOMER_NAME, INVOICE_TYPE, TITLE_TYPE, INVOICE_TITLE, TAX_NO, COMPANY_ADDRESS, BANK_ACCOUNT, INVOICE_AMOUNT, ORDER_AMOUNT, INVOICE_ITEM, CONTACT_PHONE, CONTACT_EMAIL, INVOICE_CODE, INVOICE_NUMBER, INVOICE_PDF, IS_ELECTRONIC, EMAIL_SENT, MAIL_RECIPIENT, MAIL_ADDRESS, MAIL_COMPANY, MAIL_TRACKING_NO, MAIL_TIME, APPLICANT_ID, APPLICANT_NAME, APPLY_TIME, PROCESSOR_ID, PROCESSOR_NAME, PROCESS_TIME, STATUS, REMARK, CREATE_TIME, UPDATE_TIME, DELETED};

    public InvoiceTableDef() {
        super("", "invoice");
    }
}
