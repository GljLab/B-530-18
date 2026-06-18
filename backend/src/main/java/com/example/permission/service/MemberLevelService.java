package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.Member;
import com.example.permission.entity.MemberLevel;
import com.example.permission.mapper.MemberLevelMapper;
import com.example.permission.mapper.MemberMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.permission.entity.table.MemberLevelTableDef.MEMBER_LEVEL;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;

@Service
public class MemberLevelService {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private MemberMapper memberMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<MemberLevel> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc());
        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(query);
        for (MemberLevel level : levels) {
            parseJsonFields(level);
            level.setMemberCount(countMembersByLevel(level.getId()));
        }
        return levels;
    }

    public List<MemberLevel> listEnabled() {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .and(MEMBER_LEVEL.STATUS.eq(1))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc());
        List<MemberLevel> levels = memberLevelMapper.selectListByQuery(query);
        for (MemberLevel level : levels) {
            parseJsonFields(level);
        }
        return levels;
    }

    public MemberLevel getById(Long id) {
        MemberLevel level = memberLevelMapper.selectOneById(id);
        if (level == null || level.getDeleted() == 1) {
            throw new BusinessException("会员等级不存在");
        }
        parseJsonFields(level);
        level.setMemberCount(countMembersByLevel(id));
        return level;
    }

    public MemberLevel getLowestLevel() {
        QueryWrapper query = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.DELETED.eq(0))
                .and(MEMBER_LEVEL.STATUS.eq(1))
                .orderBy(MEMBER_LEVEL.SORT_ORDER.asc())
                .limit(1);
        MemberLevel level = memberLevelMapper.selectOneByQuery(query);
        if (level == null) {
            throw new BusinessException("没有可用的会员等级");
        }
        parseJsonFields(level);
        return level;
    }

    @Transactional
    public MemberLevel addLevel(MemberLevel level) {
        QueryWrapper codeQuery = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.LEVEL_CODE.eq(level.getLevelCode()))
                .and(MEMBER_LEVEL.DELETED.eq(0));
        long codeCount = memberLevelMapper.selectCountByQuery(codeQuery);
        if (codeCount > 0) {
            throw new BusinessException("等级编码已存在");
        }

        QueryWrapper nameQuery = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.LEVEL_NAME.eq(level.getLevelName()))
                .and(MEMBER_LEVEL.DELETED.eq(0));
        long nameCount = memberLevelMapper.selectCountByQuery(nameQuery);
        if (nameCount > 0) {
            throw new BusinessException("等级名称已存在");
        }

        QueryWrapper sortQuery = QueryWrapper.create()
                .from(MemberLevel.class)
                .where(MEMBER_LEVEL.SORT_ORDER.eq(level.getSortOrder()))
                .and(MEMBER_LEVEL.DELETED.eq(0));
        long sortCount = memberLevelMapper.selectCountByQuery(sortQuery);
        if (sortCount > 0) {
            throw new BusinessException("等级排序已存在");
        }

        serializeJsonFields(level);
        level.setDeleted(0);
        level.setCreateTime(LocalDateTime.now());
        memberLevelMapper.insert(level);
        return level;
    }

    @Transactional
    public void updateLevel(MemberLevel level) {
        MemberLevel existing = memberLevelMapper.selectOneById(level.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("会员等级不存在");
        }

        if (!existing.getLevelCode().equals(level.getLevelCode())) {
            QueryWrapper codeQuery = QueryWrapper.create()
                    .from(MemberLevel.class)
                    .where(MEMBER_LEVEL.LEVEL_CODE.eq(level.getLevelCode()))
                    .and(MEMBER_LEVEL.DELETED.eq(0))
                    .and(MEMBER_LEVEL.ID.ne(level.getId()));
            long codeCount = memberLevelMapper.selectCountByQuery(codeQuery);
            if (codeCount > 0) {
                throw new BusinessException("等级编码已存在");
            }
        }

        if (!existing.getLevelName().equals(level.getLevelName())) {
            QueryWrapper nameQuery = QueryWrapper.create()
                    .from(MemberLevel.class)
                    .where(MEMBER_LEVEL.LEVEL_NAME.eq(level.getLevelName()))
                    .and(MEMBER_LEVEL.DELETED.eq(0))
                    .and(MEMBER_LEVEL.ID.ne(level.getId()));
            long nameCount = memberLevelMapper.selectCountByQuery(nameQuery);
            if (nameCount > 0) {
                throw new BusinessException("等级名称已存在");
            }
        }

        if (!existing.getSortOrder().equals(level.getSortOrder())) {
            QueryWrapper sortQuery = QueryWrapper.create()
                    .from(MemberLevel.class)
                    .where(MEMBER_LEVEL.SORT_ORDER.eq(level.getSortOrder()))
                    .and(MEMBER_LEVEL.DELETED.eq(0))
                    .and(MEMBER_LEVEL.ID.ne(level.getId()));
            long sortCount = memberLevelMapper.selectCountByQuery(sortQuery);
            if (sortCount > 0) {
                throw new BusinessException("等级排序已存在");
            }
        }

        serializeJsonFields(level);
        level.setUpdateTime(LocalDateTime.now());
        memberLevelMapper.update(level);
    }

    @Transactional
    public void deleteLevel(Long id) {
        MemberLevel existing = memberLevelMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("会员等级不存在");
        }

        long memberCount = countMembersByLevel(id);
        if (memberCount > 0) {
            throw new BusinessException("该等级下还有会员，无法删除");
        }

        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        memberLevelMapper.update(existing);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        MemberLevel existing = memberLevelMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("会员等级不存在");
        }
        existing.setStatus(status);
        existing.setUpdateTime(LocalDateTime.now());
        memberLevelMapper.update(existing);
    }

    private Long countMembersByLevel(Long levelId) {
        QueryWrapper query = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.LEVEL_ID.eq(levelId))
                .and(MEMBER.DELETED.eq(0));
        return memberMapper.selectCountByQuery(query);
    }

    private void parseJsonFields(MemberLevel level) {
        try {
            if (level.getServices() != null && !level.getServices().isEmpty()) {
                level.setServiceList(objectMapper.readValue(level.getServices(), new TypeReference<List<String>>() {}));
            } else {
                level.setServiceList(new ArrayList<>());
            }
            if (level.getOtherBenefits() != null && !level.getOtherBenefits().isEmpty()) {
                level.setOtherBenefitList(objectMapper.readValue(level.getOtherBenefits(), new TypeReference<List<String>>() {}));
            } else {
                level.setOtherBenefitList(new ArrayList<>());
            }
        } catch (Exception e) {
            level.setServiceList(new ArrayList<>());
            level.setOtherBenefitList(new ArrayList<>());
        }
    }

    private void serializeJsonFields(MemberLevel level) {
        try {
            if (level.getServiceList() != null) {
                level.setServices(objectMapper.writeValueAsString(level.getServiceList()));
            }
            if (level.getOtherBenefitList() != null) {
                level.setOtherBenefits(objectMapper.writeValueAsString(level.getOtherBenefitList()));
            }
        } catch (Exception e) {
            throw new BusinessException("JSON序列化失败");
        }
    }
}
