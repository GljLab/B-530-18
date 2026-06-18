package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Invoice;
import com.example.permission.mapper.InvoiceMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.example.permission.entity.table.InvoiceTableDef.INVOICE;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceMapper invoiceMapper;

    public Map<String, Object> getInvoicePage(String keyword, Integer invoiceType, Integer status, String startDate, String endDate, int pageNum, int pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(Invoice.class)
                .where(INVOICE.DELETED.eq(0));
        if (StringUtils.hasText(keyword)) {
            query.and(INVOICE.CUSTOMER_NAME.like(keyword));
        }
        if (invoiceType != null) {
            query.and(INVOICE.INVOICE_TYPE.eq(invoiceType));
        }
        if (status != null) {
            query.and(INVOICE.STATUS.eq(status));
        }
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(INVOICE.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(INVOICE.CREATE_TIME.le(endDateTime));
        }
        query.orderBy(INVOICE.CREATE_TIME.desc());
        Page<Invoice> page = invoiceMapper.paginate(Page.of(pageNum, pageSize), query);
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotalRow());
        return result;
    }

    public Invoice getInvoiceById(Long id) {
        QueryWrapper query = QueryWrapper.create()
                .from(Invoice.class)
                .where(INVOICE.ID.eq(id))
                .and(INVOICE.DELETED.eq(0));
        Invoice invoice = invoiceMapper.selectOneByQuery(query);
        if (invoice == null) {
            throw new BusinessException("发票不存在");
        }
        return invoice;
    }

    @Transactional(rollbackFor = Exception.class)
    public Invoice createInvoice(Invoice invoice) {
        if (invoice.getInvoiceAmount() != null && invoice.getOrderAmount() != null
                && invoice.getInvoiceAmount().compareTo(invoice.getOrderAmount()) > 0) {
            throw new BusinessException("开票金额不能大于订单金额");
        }
        invoice.setInvoiceNo(generateInvoiceNo());
        invoice.setStatus(0);
        invoice.setApplyTime(LocalDateTime.now());
        invoice.setCreateTime(LocalDateTime.now());
        invoice.setUpdateTime(LocalDateTime.now());
        invoice.setDeleted(0);
        invoiceMapper.insert(invoice);
        return invoice;
    }

    @Transactional(rollbackFor = Exception.class)
    public Invoice processInvoice(Long id, String invoiceCode, String invoiceNumber, String invoicePdf, Long processorId, String processorName) {
        Invoice invoice = getInvoiceById(id);
        if (invoice.getStatus() != 0) {
            throw new BusinessException("该发票已处理，不可重复操作");
        }
        invoice.setInvoiceCode(invoiceCode);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoicePdf(invoicePdf);
        invoice.setProcessorId(processorId);
        invoice.setProcessorName(processorName);
        invoice.setProcessTime(LocalDateTime.now());
        if (invoice.getIsElectronic() != null && invoice.getIsElectronic() == 1
                && invoice.getContactEmail() != null && !invoice.getContactEmail().isEmpty()) {
            invoice.setStatus(1);
            invoice.setEmailSent(1);
        } else if (invoice.getIsElectronic() != null && invoice.getIsElectronic() == 0) {
            invoice.setStatus(2);
        } else {
            invoice.setStatus(1);
        }
        invoice.setUpdateTime(LocalDateTime.now());
        invoiceMapper.update(invoice);
        return invoice;
    }

    @Transactional(rollbackFor = Exception.class)
    public Invoice mailInvoice(Long id, String mailRecipient, String mailAddress, String mailCompany, String mailTrackingNo, Long operatorId) {
        Invoice invoice = getInvoiceById(id);
        if (invoice.getStatus() != 2) {
            throw new BusinessException("只有待邮寄状态的发票才能邮寄");
        }
        invoice.setMailRecipient(mailRecipient);
        invoice.setMailAddress(mailAddress);
        invoice.setMailCompany(mailCompany);
        invoice.setMailTrackingNo(mailTrackingNo);
        invoice.setMailTime(LocalDateTime.now());
        invoice.setStatus(3);
        invoice.setUpdateTime(LocalDateTime.now());
        invoiceMapper.update(invoice);
        return invoice;
    }

    public Map<String, Object> getInvoiceStatistics(String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(Invoice.class)
                .where(INVOICE.DELETED.eq(0));
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(INVOICE.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(INVOICE.CREATE_TIME.le(endDateTime));
        }
        List<Invoice> invoices = invoiceMapper.selectListByQuery(query);

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Integer, BigDecimal> byType = new HashMap<>();
        Map<Integer, BigDecimal> byItem = new HashMap<>();
        for (Invoice invoice : invoices) {
            BigDecimal amount = invoice.getInvoiceAmount() != null ? invoice.getInvoiceAmount() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amount);
            if (invoice.getInvoiceType() != null) {
                byType.merge(invoice.getInvoiceType(), amount, BigDecimal::add);
            }
            if (invoice.getInvoiceItem() != null) {
                byItem.merge(invoice.getInvoiceItem(), amount, BigDecimal::add);
            }
        }

        List<Map<String, Object>> byTypeList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : byType.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("invoiceType", entry.getKey());
            item.put("amount", entry.getValue());
            byTypeList.add(item);
        }

        List<Map<String, Object>> byItemList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : byItem.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("invoiceItem", entry.getKey());
            item.put("amount", entry.getValue());
            byItemList.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmount", totalAmount);
        result.put("totalCount", invoices.size());
        result.put("byType", byTypeList);
        result.put("byItem", byItemList);
        return result;
    }

    public List<Invoice> exportInvoices(String keyword, Integer invoiceType, Integer status, String startDate, String endDate) {
        QueryWrapper query = QueryWrapper.create()
                .from(Invoice.class)
                .where(INVOICE.DELETED.eq(0));
        if (StringUtils.hasText(keyword)) {
            query.and(INVOICE.CUSTOMER_NAME.like(keyword));
        }
        if (invoiceType != null) {
            query.and(INVOICE.INVOICE_TYPE.eq(invoiceType));
        }
        if (status != null) {
            query.and(INVOICE.STATUS.eq(status));
        }
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
            query.and(INVOICE.CREATE_TIME.ge(startDateTime));
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
            query.and(INVOICE.CREATE_TIME.le(endDateTime));
        }
        query.orderBy(INVOICE.CREATE_TIME.desc());
        return invoiceMapper.selectListByQuery(query);
    }

    private String generateInvoiceNo() {
        String prefix = "INV" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Random random = new Random();
        String suffix = String.format("%04d", random.nextInt(10000));
        String invoiceNo = prefix + suffix;
        QueryWrapper checkQuery = QueryWrapper.create()
                .from(Invoice.class)
                .where(INVOICE.INVOICE_NO.eq(invoiceNo));
        long count = invoiceMapper.selectCountByQuery(checkQuery);
        if (count > 0) {
            return generateInvoiceNo();
        }
        return invoiceNo;
    }
}
