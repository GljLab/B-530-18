package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.ReviewQuickComment;
import com.example.permission.mapper.ReviewQuickCommentMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.ReviewQuickCommentTableDef.REVIEW_QUICK_COMMENT;

@Service
public class ReviewQuickCommentService {

    @Autowired
    private ReviewQuickCommentMapper reviewQuickCommentMapper;

    public List<ReviewQuickComment> listByType(Integer commentType) {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewQuickComment.class)
                .where(REVIEW_QUICK_COMMENT.DELETED.eq(0));
        if (commentType != null) {
            query.and(REVIEW_QUICK_COMMENT.COMMENT_TYPE.eq(commentType));
        }
        query.orderBy(REVIEW_QUICK_COMMENT.COMMENT_TYPE.asc(), REVIEW_QUICK_COMMENT.SORT_ORDER.asc(), REVIEW_QUICK_COMMENT.ID.asc());
        return reviewQuickCommentMapper.selectListByQuery(query);
    }

    public List<ReviewQuickComment> listAll() {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewQuickComment.class)
                .where(REVIEW_QUICK_COMMENT.DELETED.eq(0))
                .orderBy(REVIEW_QUICK_COMMENT.COMMENT_TYPE.asc(), REVIEW_QUICK_COMMENT.SORT_ORDER.asc(), REVIEW_QUICK_COMMENT.ID.asc());
        return reviewQuickCommentMapper.selectListByQuery(query);
    }

    public ReviewQuickComment getById(Long id) {
        ReviewQuickComment comment = reviewQuickCommentMapper.selectOneById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException("快捷评语不存在");
        }
        return comment;
    }

    public ReviewQuickComment add(ReviewQuickComment comment) {
        validateComment(comment);

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewQuickComment.class)
                .where(REVIEW_QUICK_COMMENT.COMMENT_CONTENT.eq(comment.getCommentContent().trim()))
                .and(REVIEW_QUICK_COMMENT.DELETED.eq(0));
        long count = reviewQuickCommentMapper.selectCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("该评语已存在");
        }

        comment.setCommentContent(comment.getCommentContent().trim());
        if (comment.getSortOrder() == null) comment.setSortOrder(0);
        if (comment.getStatus() == null) comment.setStatus(1);
        comment.setDeleted(0);
        comment.setCreateTime(LocalDateTime.now());
        reviewQuickCommentMapper.insert(comment);
        return comment;
    }

    public void update(ReviewQuickComment comment) {
        ReviewQuickComment existing = reviewQuickCommentMapper.selectOneById(comment.getId());
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("快捷评语不存在");
        }
        validateComment(comment);

        if (!existing.getCommentContent().equals(comment.getCommentContent().trim())) {
            QueryWrapper query = QueryWrapper.create()
                    .from(ReviewQuickComment.class)
                    .where(REVIEW_QUICK_COMMENT.COMMENT_CONTENT.eq(comment.getCommentContent().trim()))
                    .and(REVIEW_QUICK_COMMENT.DELETED.eq(0))
                    .and(REVIEW_QUICK_COMMENT.ID.ne(comment.getId()));
            long count = reviewQuickCommentMapper.selectCountByQuery(query);
            if (count > 0) {
                throw new BusinessException("该评语已存在");
            }
        }

        existing.setCommentContent(comment.getCommentContent().trim());
        existing.setCommentType(comment.getCommentType());
        existing.setSortOrder(comment.getSortOrder() != null ? comment.getSortOrder() : existing.getSortOrder());
        existing.setUpdateTime(LocalDateTime.now());
        reviewQuickCommentMapper.update(existing);
    }

    public void delete(Long id) {
        ReviewQuickComment existing = reviewQuickCommentMapper.selectOneById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("快捷评语不存在");
        }
        existing.setDeleted(1);
        existing.setUpdateTime(LocalDateTime.now());
        reviewQuickCommentMapper.update(existing);
    }

    private void validateComment(ReviewQuickComment comment) {
        if (comment.getCommentType() == null || (comment.getCommentType() != 1 && comment.getCommentType() != 2 && comment.getCommentType() != 3)) {
            throw new BusinessException("评语分类不正确");
        }
        if (comment.getCommentContent() == null || comment.getCommentContent().trim().isEmpty()) {
            throw new BusinessException("评语内容不能为空");
        }
        int len = comment.getCommentContent().trim().length();
        if (len < 5 || len > 100) {
            throw new BusinessException("评语长度必须在5-100个字之间");
        }
    }
}
