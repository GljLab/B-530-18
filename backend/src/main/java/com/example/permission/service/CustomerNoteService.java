package com.example.permission.service;

import com.example.permission.entity.CustomerNote;
import com.example.permission.entity.CustomerOperationLog;
import com.example.permission.mapper.CustomerNoteMapper;
import com.example.permission.mapper.CustomerOperationLogMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.CustomerNoteTableDef.CUSTOMER_NOTE;

@Service
public class CustomerNoteService {

    @Autowired
    private CustomerNoteMapper customerNoteMapper;

    @Autowired
    private CustomerOperationLogMapper customerOperationLogMapper;

    public List<CustomerNote> listByCustomerId(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerNote.class)
                .where(CUSTOMER_NOTE.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_NOTE.DELETED.eq(0))
                .orderBy(CUSTOMER_NOTE.IS_PINNED.desc(), CUSTOMER_NOTE.CREATE_TIME.desc());
        return customerNoteMapper.selectListByQuery(query);
    }

    @Transactional
    public CustomerNote addNote(CustomerNote note, Long operatorId, String operatorName) {
        note.setOperatorId(operatorId);
        note.setOperatorName(operatorName);
        note.setDeleted(0);
        customerNoteMapper.insert(note);

        saveOperationLog(note.getCustomerId(), 8, operatorId, operatorName, "添加备注");
        return note;
    }

    @Transactional
    public void deleteNote(Long id, Long operatorId, String operatorName) {
        CustomerNote note = customerNoteMapper.selectOneById(id);
        if (note == null || note.getDeleted() == 1) {
            return;
        }
        note.setDeleted(1);
        note.setUpdateTime(LocalDateTime.now());
        customerNoteMapper.update(note);

        saveOperationLog(note.getCustomerId(), 8, operatorId, operatorName, "删除备注");
    }

    @Transactional
    public void pinNote(Long id, Long operatorId, String operatorName) {
        CustomerNote note = customerNoteMapper.selectOneById(id);
        if (note == null) {
            return;
        }
        note.setIsPinned(1);
        note.setUpdateTime(LocalDateTime.now());
        customerNoteMapper.update(note);

        saveOperationLog(note.getCustomerId(), 8, operatorId, operatorName, "置顶备注");
    }

    @Transactional
    public void unpinNote(Long id, Long operatorId, String operatorName) {
        CustomerNote note = customerNoteMapper.selectOneById(id);
        if (note == null) {
            return;
        }
        note.setIsPinned(0);
        note.setUpdateTime(LocalDateTime.now());
        customerNoteMapper.update(note);

        saveOperationLog(note.getCustomerId(), 8, operatorId, operatorName, "取消置顶备注");
    }

    public boolean hasImportantNotes(Long customerId) {
        QueryWrapper query = QueryWrapper.create()
                .from(CustomerNote.class)
                .where(CUSTOMER_NOTE.CUSTOMER_ID.eq(customerId))
                .and(CUSTOMER_NOTE.DELETED.eq(0))
                .and(CUSTOMER_NOTE.IMPORTANCE.ge(2));
        return customerNoteMapper.selectCountByQuery(query) > 0;
    }

    private void saveOperationLog(Long customerId, Integer operationType, Long operatorId, String operatorName, String remark) {
        CustomerOperationLog log = new CustomerOperationLog();
        log.setCustomerId(customerId);
        log.setOperationType(operationType);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        customerOperationLogMapper.insert(log);
    }
}
