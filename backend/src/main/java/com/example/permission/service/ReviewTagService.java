package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.ReviewTag;
import com.example.permission.mapper.ReviewTagMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ReviewTagTableDef.REVIEW_TAG;

@Service
public class ReviewTagService {

    @Autowired
    private ReviewTagMapper reviewTagMapper;

    public List<ReviewTag> listByType(Integer tagType) {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewTag.class)
                .where(REVIEW_TAG.DELETED.eq(0));
        if (tagType != null) {
            query.and(REVIEW_TAG.TAG_TYPE.eq(tagType));
        }
        query.orderBy(REVIEW_TAG.SORT_ORDER.asc(), REVIEW_TAG.ID.asc());
        return reviewTagMapper.selectListByQuery(query);
    }

    public ReviewTag getById(Long id) {
        ReviewTag tag = reviewTagMapper.selectOneById(id);
        if (tag == null || tag.getDeleted() == 1) {
            throw new BusinessException("评价标签不存在");
        }
        return tag;
    }

    public ReviewTag add(ReviewTag tag) {
        validateTag(tag);

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewTag.class)
                .where(REVIEW_TAG.TAG_TEXT.eq(tag.getTagText().trim()))
                .and(REVIEW_TAG.TAG_TYPE.eq(tag.getTagType()))
                .and(REVIEW_TAG.DELETED.eq(0));
        long count = reviewTagMapper.selectCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("标签已存在");
        }

        tag.setTagText(tag.getTagText().trim());
        if (tag.getSortOrder() == null) tag.setSortOrder(0);
        if (tag.getStatus() == null) tag.setStatus(1);
        tag.setDeleted(0);
        tag.setCreateTime(LocalDateTime.now());
        reviewTagMapper.insert(tag);
        return tag;
    }

    public void update(ReviewTag tag) {
        ReviewTag existing = reviewTagMapper.selectOneById(tag.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("评价标签不存在");
        }
        validateTag(tag);

        if (!existing.getTagText().equals(tag.getTagText().trim()) || !existing.getTagType().equals(tag.getTagType())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(ReviewTag.class)
                    .where(REVIEW_TAG.TAG_TEXT.eq(tag.getTagText().trim()))
                    .and(REVIEW_TAG.TAG_TYPE.eq(tag.getTagType()))
                    .and(REVIEW_TAG.DELETED.eq(0))
                    .and(REVIEW_TAG.ID.ne(tag.getId()));
            long count = reviewTagMapper.selectCountByQuery(query);
            if (count > 0) {
                throw new BusinessException("标签已存在");
            }
        }

        existing.setTagText(tag.getTagText().trim());
        existing.setTagType(tag.getTagType());
        existing.setSortOrder(tag.getSortOrder() != null ? tag.getSortOrder() : existing.getSortOrder());
        existing.setUpdateTime(LocalDateTime.now());
        reviewTagMapper.update(existing);
    }

    public void delete(Long id) {
        ReviewTag existing = reviewTagMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("评价标签不存在");
        }
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        reviewTagMapper.update(existing);
    }

    public void updateSort(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            ReviewTag tag = reviewTagMapper.selectOneById(ids.get(i));
            if (tag != null && tag.getDeleted() == 0) {
                tag.setSortOrder(i);
                tag.setUpdateTime(LocalDateTime.now());
                reviewTagMapper.update(tag);
            }
        }
    }

    private void validateTag(ReviewTag tag) {
        if (tag.getTagType() == null || (tag.getTagType() != 1 && tag.getTagType() != 2)) {
            throw new BusinessException("标签类型不正确");
        }
        if (tag.getTagText() == null || tag.getTagText().trim().isEmpty()) {
            throw new BusinessException("标签文本不能为空");
        }
        int len = tag.getTagText().trim().length();
        if (len < 2 || len > 10) {
            throw new BusinessException("标签长度必须在2-10个字之间");
        }
    }
}
