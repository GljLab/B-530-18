package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.paginate.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.ReviewRecordTableDef.REVIEW_RECORD;
import static com.example.permission.entity.table.ReviewMetricScoreTableDef.REVIEW_METRIC_SCORE;
import static com.example.permission.entity.table.ReviewImageTableDef.REVIEW_IMAGE;
import static com.example.permission.entity.table.MemberTableDef.MEMBER;
import static com.example.permission.entity.table.ReviewTagTableDef.REVIEW_TAG;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class ReviewAuditService {

    @Autowired
    private ReviewRecordMapper reviewRecordMapper;

    @Autowired
    private ReviewMetricScoreMapper reviewMetricScoreMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private ReviewTagMapper reviewTagMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    private LoginUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException("用户未登录");
    }

    public PageResult<ReviewRecord> getAuditPage(Integer reviewStatus, String keyword, Integer scoreFilter,
                                                 LocalDate startDate, LocalDate endDate,
                                                 Long pageNum, Long pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0));

        if (reviewStatus != null) {
            query.and(REVIEW_RECORD.REVIEW_STATUS.eq(reviewStatus));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim() + "%";
            query.and(REVIEW_RECORD.CUSTOMER_NAME.like(kw)
                    .or(REVIEW_RECORD.CUSTOMER_PHONE.like(kw)));
        }

        if (scoreFilter != null && scoreFilter >= 1 && scoreFilter <= 5) {
            BigDecimal minScore = BigDecimal.valueOf(scoreFilter - 0.5);
            BigDecimal maxScore = BigDecimal.valueOf(scoreFilter + 0.5);
            query.and(REVIEW_RECORD.OVERALL_SCORE.ge(minScore))
                    .and(REVIEW_RECORD.OVERALL_SCORE.lt(maxScore));
        }

        if (startDate != null) {
            query.and(REVIEW_RECORD.REVIEW_TIME.ge(startDate.atStartOfDay()));
        }
        if (endDate != null) {
            query.and(REVIEW_RECORD.REVIEW_TIME.lt(endDate.plusDays(1).atStartOfDay()));
        }

        query.orderBy(REVIEW_RECORD.REVIEW_TIME.desc(), REVIEW_RECORD.ID.desc());

        Page<ReviewRecord> page = reviewRecordMapper.paginate(
                Page.of(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10),
                query
        );

        List<ReviewRecord> list = page.getRecords();
        for (ReviewRecord record : list) {
            fillSimpleRelationData(record);
        }

        return new PageResult<>(page.getTotalRow(), list, page.getPageNumber(), page.getPageSize());
    }

    public ReviewRecord getDetail(Long id) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        ReviewRecord record = reviewRecordMapper.selectOneById(id);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("评价记录不存在");
        }
        fillFullRelationData(record);
        return record;
    }

    @Transactional
    public void approve(Long id) {
        LoginUser currentUser = getCurrentUser();
        ReviewRecord record = getRecordForUpdate(id);
        record.setReviewStatus(1);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditorId(currentUser.getUserId());
        record.setAuditorName(getDisplayName(currentUser));
        record.setAuditRemark(null);
        record.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.update(record);
    }

    @Transactional
    public void reject(Long id, String reasonType, String remark) {
        if (remark == null || remark.trim().isEmpty()) {
            throw new BusinessException("请填写详细说明");
        }
        LoginUser currentUser = getCurrentUser();
        ReviewRecord record = getRecordForUpdate(id);
        record.setReviewStatus(2);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditorId(currentUser.getUserId());
        record.setAuditorName(getDisplayName(currentUser));
        String fullRemark = buildAuditRemark(reasonType, remark);
        record.setAuditRemark(fullRemark);
        record.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.update(record);
    }

    @Transactional
    public void hide(Long id, String remark) {
        if (remark == null || remark.trim().isEmpty()) {
            throw new BusinessException("请填写隐藏原因");
        }
        LoginUser currentUser = getCurrentUser();
        ReviewRecord record = getRecordForUpdate(id);
        record.setReviewStatus(3);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditorId(currentUser.getUserId());
        record.setAuditorName(getDisplayName(currentUser));
        record.setAuditRemark(remark.trim());
        record.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.update(record);
    }

    @Transactional
    public void reapprove(Long id) {
        LoginUser currentUser = getCurrentUser();
        ReviewRecord record = getRecordForUpdate(id);
        if (record.getReviewStatus() != 3) {
            throw new BusinessException("只有已隐藏的评价才能重新审核通过");
        }
        record.setReviewStatus(1);
        record.setAuditTime(LocalDateTime.now());
        record.setAuditorId(currentUser.getUserId());
        record.setAuditorName(getDisplayName(currentUser));
        record.setAuditRemark(null);
        record.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.update(record);
    }

    @Transactional
    public void reply(Long id, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new BusinessException("请填写回复内容");
        }
        if (replyContent.length() > 300) {
            throw new BusinessException("回复内容不能超过300字");
        }
        LoginUser currentUser = getCurrentUser();
        ReviewRecord record = getRecordForUpdate(id);
        if (record.getReplyContent() != null && !record.getReplyContent().trim().isEmpty()) {
            throw new BusinessException("该评价已回复，如需修改请使用编辑功能");
        }
        record.setReplyContent(replyContent.trim());
        record.setReplyTime(LocalDateTime.now());
        record.setRepliedBy(currentUser.getUserId());
        record.setRepliedName(getDisplayName(currentUser));
        record.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.update(record);
    }

    @Transactional
    public void editReply(Long id, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new BusinessException("请填写回复内容");
        }
        if (replyContent.length() > 300) {
            throw new BusinessException("回复内容不能超过300字");
        }
        LoginUser currentUser = getCurrentUser();
        ReviewRecord record = getRecordForUpdate(id);
        if (record.getReplyContent() == null || record.getReplyContent().trim().isEmpty()) {
            throw new BusinessException("该评价尚未回复，无法编辑");
        }
        record.setReplyContent(replyContent.trim());
        record.setReplyTime(LocalDateTime.now());
        record.setRepliedBy(currentUser.getUserId());
        record.setRepliedName(getDisplayName(currentUser));
        record.setUpdateTime(LocalDateTime.now());
        reviewRecordMapper.update(record);
    }

    public Map<String, Object> getPublicStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper approvedQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1));
        long totalCount = reviewRecordMapper.selectCountByQuery(approvedQuery);
        result.put("totalCount", totalCount);

        List<ReviewRecord> allApproved = reviewRecordMapper.selectListByQuery(approvedQuery);
        BigDecimal avgScore = BigDecimal.ZERO;
        long goodCount = 0;
        Map<Integer, Long> scoreDistribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            scoreDistribution.put(i, 0L);
        }

        if (totalCount > 0) {
            BigDecimal totalScore = BigDecimal.ZERO;
            for (ReviewRecord r : allApproved) {
                BigDecimal score = r.getOverallScore() != null ? r.getOverallScore() : BigDecimal.ZERO;
                totalScore = totalScore.add(score);
                if (score.compareTo(BigDecimal.valueOf(4)) >= 0) {
                    goodCount++;
                }
                int bucket = score.setScale(0, RoundingMode.HALF_UP).intValue();
                bucket = Math.max(1, Math.min(5, bucket));
                scoreDistribution.put(bucket, scoreDistribution.getOrDefault(bucket, 0L) + 1);
            }
            avgScore = totalScore.divide(BigDecimal.valueOf(totalCount), 1, RoundingMode.HALF_UP);
        }

        result.put("avgScore", avgScore);
        result.put("goodRate", totalCount > 0 ?
                BigDecimal.valueOf(goodCount * 100).divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP).intValue() : 0);

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            Map<String, Object> item = new HashMap<>();
            long count = scoreDistribution.get(i);
            item.put("score", i);
            item.put("count", count);
            item.put("percent", totalCount > 0 ?
                    BigDecimal.valueOf(count * 100).divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP).intValue() : 0);
            distribution.add(item);
        }
        result.put("scoreDistribution", distribution);

        return result;
    }

    public PageResult<ReviewRecord> getPublicPage(Integer scoreFilter, Long roomTypeId, Boolean hasImage,
                                                  String sortType, Long pageNum, Long pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1));

        if (scoreFilter != null && scoreFilter >= 1 && scoreFilter <= 5) {
            BigDecimal minScore = BigDecimal.valueOf(scoreFilter - 0.5);
            BigDecimal maxScore = BigDecimal.valueOf(scoreFilter + 0.5);
            query.and(REVIEW_RECORD.OVERALL_SCORE.ge(minScore))
                    .and(REVIEW_RECORD.OVERALL_SCORE.lt(maxScore));
        }

        if (roomTypeId != null) {
            query.and(REVIEW_RECORD.ROOM_TYPE_ID.eq(roomTypeId));
        }

        if (Boolean.TRUE.equals(hasImage)) {
            query.and("id IN (SELECT review_record_id FROM review_image)");
        }

        if ("score_desc".equals(sortType)) {
            query.orderBy(REVIEW_RECORD.OVERALL_SCORE.desc(), REVIEW_RECORD.REVIEW_TIME.desc());
        } else if ("score_asc".equals(sortType)) {
            query.orderBy(REVIEW_RECORD.OVERALL_SCORE.asc(), REVIEW_RECORD.REVIEW_TIME.desc());
        } else {
            query.orderBy(REVIEW_RECORD.REVIEW_TIME.desc(), REVIEW_RECORD.ID.desc());
        }

        Page<ReviewRecord> page = reviewRecordMapper.paginate(
                Page.of(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10),
                query
        );

        List<ReviewRecord> list = page.getRecords();
        for (ReviewRecord record : list) {
            fillFullRelationData(record);
        }

        return new PageResult<>(page.getTotalRow(), list, page.getPageNumber(), page.getPageSize());
    }

    public List<RoomType> getRoomTypeList() {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0))
                .orderBy(ROOM_TYPE.ID.asc());
        return roomTypeMapper.selectListByQuery(query);
    }

    private void sortList(List<ReviewRecord> list, String sortType) {
        if ("score_desc".equals(sortType)) {
            list.sort((a, b) -> {
                BigDecimal sa = a.getOverallScore() != null ? a.getOverallScore() : BigDecimal.ZERO;
                BigDecimal sb = b.getOverallScore() != null ? b.getOverallScore() : BigDecimal.ZERO;
                return sb.compareTo(sa);
            });
        } else if ("score_asc".equals(sortType)) {
            list.sort((a, b) -> {
                BigDecimal sa = a.getOverallScore() != null ? a.getOverallScore() : BigDecimal.ZERO;
                BigDecimal sb = b.getOverallScore() != null ? b.getOverallScore() : BigDecimal.ZERO;
                return sa.compareTo(sb);
            });
        } else {
            list.sort((a, b) -> {
                LocalDateTime ta = a.getReviewTime() != null ? a.getReviewTime() : LocalDateTime.MIN;
                LocalDateTime tb = b.getReviewTime() != null ? b.getReviewTime() : LocalDateTime.MIN;
                return tb.compareTo(ta);
            });
        }
    }

    private ReviewRecord getRecordForUpdate(Long id) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        ReviewRecord record = reviewRecordMapper.selectOneById(id);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("评价记录不存在");
        }
        return record;
    }

    private String getDisplayName(LoginUser user) {
        if (user.getUser() != null && user.getUser().getNickname() != null && !user.getUser().getNickname().isEmpty()) {
            return user.getUser().getNickname();
        }
        return user.getUsername();
    }

    private String buildAuditRemark(String reasonType, String remark) {
        String typeText = "";
        if ("1".equals(reasonType)) typeText = "广告信息";
        else if ("2".equals(reasonType)) typeText = "恶意差评";
        else if ("3".equals(reasonType)) typeText = "违规内容";
        else if ("4".equals(reasonType)) typeText = "其他";
        if (!typeText.isEmpty()) {
            return "【" + typeText + "】" + remark.trim();
        }
        return remark.trim();
    }

    private void fillSimpleRelationData(ReviewRecord record) {
        QueryWrapper imageQuery = QueryWrapper.create()
                .from(ReviewImage.class)
                .where(REVIEW_IMAGE.REVIEW_RECORD_ID.eq(record.getId()));
        long imageCount = reviewImageMapper.selectCountByQuery(imageQuery);
        record.setImages(imageCount > 0 ? Collections.singletonList(new ReviewImage()) : null);
    }

    private void fillFullRelationData(ReviewRecord record) {
        QueryWrapper metricScoreQuery = QueryWrapper.create()
                .from(ReviewMetricScore.class)
                .where(REVIEW_METRIC_SCORE.REVIEW_RECORD_ID.eq(record.getId()))
                .orderBy(REVIEW_METRIC_SCORE.ID.asc());
        List<ReviewMetricScore> metricScores = reviewMetricScoreMapper.selectListByQuery(metricScoreQuery);
        record.setMetricScores(metricScores);

        QueryWrapper imageQuery = QueryWrapper.create()
                .from(ReviewImage.class)
                .where(REVIEW_IMAGE.REVIEW_RECORD_ID.eq(record.getId()))
                .orderBy(REVIEW_IMAGE.SORT_ORDER.asc(), REVIEW_IMAGE.ID.asc());
        List<ReviewImage> images = reviewImageMapper.selectListByQuery(imageQuery);
        record.setImages(images);
    }

    public List<ReviewTag> getTagDetailsByIds(String selectedTags) {
        if (selectedTags == null || selectedTags.isEmpty()) {
            return new ArrayList<>();
        }
        String[] tagIds = selectedTags.split(",");
        List<Long> tagIdList = new ArrayList<>();
        for (String tid : tagIds) {
            try {
                tagIdList.add(Long.parseLong(tid.trim()));
            } catch (Exception ignored) {
            }
        }
        if (tagIdList.isEmpty()) {
            return new ArrayList<>();
        }
        QueryWrapper tagQuery = QueryWrapper.create()
                .from(ReviewTag.class)
                .where(REVIEW_TAG.ID.in(tagIdList))
                .and(REVIEW_TAG.DELETED.eq(0));
        List<ReviewTag> tags = reviewTagMapper.selectListByQuery(tagQuery);
        Map<Long, ReviewTag> tagMap = tags.stream()
                .collect(Collectors.toMap(ReviewTag::getId, t -> t));
        List<ReviewTag> orderedTags = new ArrayList<>();
        for (Long tid : tagIdList) {
            ReviewTag t = tagMap.get(tid);
            if (t != null) orderedTags.add(t);
        }
        return orderedTags;
    }

    public Member getMemberInfo(Long memberId) {
        if (memberId == null) return null;
        QueryWrapper memberQuery = QueryWrapper.create()
                .from(Member.class)
                .where(MEMBER.ID.eq(memberId))
                .and(MEMBER.DELETED.eq(0))
                .limit(1);
        return memberMapper.selectOneByQuery(memberQuery);
    }
}
