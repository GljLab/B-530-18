package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.example.permission.security.LoginUser;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.permission.entity.table.ReviewMetricTableDef.REVIEW_METRIC;
import static com.example.permission.entity.table.ReviewTagTableDef.REVIEW_TAG;
import static com.example.permission.entity.table.ReviewQuickCommentTableDef.REVIEW_QUICK_COMMENT;
import static com.example.permission.entity.table.HotelInfoTableDef.HOTEL_INFO;
import static com.example.permission.entity.table.ReviewInvitationTableDef.REVIEW_INVITATION;
import static com.example.permission.entity.table.ReviewRecordTableDef.REVIEW_RECORD;
import static com.example.permission.entity.table.ReviewMetricScoreTableDef.REVIEW_METRIC_SCORE;
import static com.example.permission.entity.table.ReviewImageTableDef.REVIEW_IMAGE;

@Service
public class ReviewSubmissionService {

    @Autowired
    private ReviewInvitationMapper reviewInvitationMapper;

    @Autowired
    private ReviewRecordMapper reviewRecordMapper;

    @Autowired
    private ReviewMetricScoreMapper reviewMetricScoreMapper;

    @Autowired
    private ReviewImageMapper reviewImageMapper;

    @Autowired
    private ReviewMetricMapper reviewMetricMapper;

    @Autowired
    private ReviewTagMapper reviewTagMapper;

    @Autowired
    private ReviewQuickCommentMapper reviewQuickCommentMapper;

    @Autowired
    private HotelInfoMapper hotelInfoMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private PointRuleService pointRuleService;

    private LoginUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException("用户未登录");
    }

    public Map<String, Object> getReviewPageData(String checkInNo, String code) {
        ReviewInvitation invitation = validateLink(checkInNo, code);

        Map<String, Object> result = new HashMap<>();

        QueryWrapper hotelQuery = QueryWrapper.create()
                .from(HotelInfo.class)
                .where(HOTEL_INFO.DELETED.eq(0))
                .limit(1);
        HotelInfo hotelInfo = hotelInfoMapper.selectOneByQuery(hotelQuery);
        result.put("hotelInfo", hotelInfo);

        result.put("invitation", invitation);

        QueryWrapper metricQuery = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc(), REVIEW_METRIC.ID.asc());
        List<ReviewMetric> metrics = reviewMetricMapper.selectListByQuery(metricQuery);
        result.put("metrics", metrics);

        QueryWrapper tagQuery = QueryWrapper.create()
                .from(ReviewTag.class)
                .where(REVIEW_TAG.DELETED.eq(0))
                .and(REVIEW_TAG.STATUS.eq(1))
                .orderBy(REVIEW_TAG.SORT_ORDER.asc(), REVIEW_TAG.ID.asc());
        List<ReviewTag> tags = reviewTagMapper.selectListByQuery(tagQuery);
        result.put("tags", tags);

        QueryWrapper quickCommentQuery = QueryWrapper.create()
                .from(ReviewQuickComment.class)
                .where(REVIEW_QUICK_COMMENT.DELETED.eq(0))
                .and(REVIEW_QUICK_COMMENT.STATUS.eq(1))
                .orderBy(REVIEW_QUICK_COMMENT.SORT_ORDER.asc(), REVIEW_QUICK_COMMENT.ID.asc());
        List<ReviewQuickComment> quickComments = reviewQuickCommentMapper.selectListByQuery(quickCommentQuery);
        result.put("quickComments", quickComments);

        return result;
    }

    @Transactional
    public Long submitReview(Map<String, Object> reviewData) {
        String checkInNo = (String) reviewData.get("checkInNo");
        String code = (String) reviewData.get("code");

        ReviewInvitation invitation = validateLink(checkInNo, code);

        List<Map<String, Object>> metricScores = (List<Map<String, Object>>) reviewData.get("metricScores");
        if (metricScores == null || metricScores.isEmpty()) {
            throw new BusinessException("请完成评价指标评分");
        }

        QueryWrapper metricQuery = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc());
        List<ReviewMetric> metrics = reviewMetricMapper.selectListByQuery(metricQuery);

        for (ReviewMetric metric : metrics) {
            if (metric.getIsRequired() != null && metric.getIsRequired() == 1) {
                boolean found = false;
                for (Map<String, Object> scoreMap : metricScores) {
                    Long metricId = Long.valueOf(scoreMap.get("metricId").toString());
                    if (metricId.equals(metric.getId())) {
                        found = true;
                        Object scoreObj = scoreMap.get("score");
                        if (scoreObj == null) {
                            throw new BusinessException("必评指标【" + metric.getMetricName() + "】必须评分");
                        }
                        break;
                    }
                }
                if (!found) {
                    throw new BusinessException("必评指标【" + metric.getMetricName() + "】必须评分");
                }
            }
        }

        String reviewContent = (String) reviewData.get("reviewContent");
        if (reviewContent != null && reviewContent.length() > 500) {
            throw new BusinessException("评价内容不能超过500字");
        }

        BigDecimal overallScore = calculateOverallScore(metricScores, metrics);

        ReviewRecord record = new ReviewRecord();
        record.setCheckInId(invitation.getCheckInId());
        record.setCheckInNo(invitation.getCheckInNo());
        record.setInvitationId(invitation.getId());
        record.setCustomerId(invitation.getCustomerId());
        record.setCustomerName(invitation.getCustomerName());
        record.setCustomerPhone(invitation.getCustomerPhone());

        if (invitation.getCustomerId() != null) {
            Member member = memberService.getByCustomerId(invitation.getCustomerId());
            if (member != null) {
                record.setMemberId(member.getId());
                record.setMemberNo(member.getMemberNo());
            }
        }

        record.setRoomTypeId(invitation.getRoomTypeId());
        record.setRoomTypeName(invitation.getRoomTypeName());
        record.setCheckInDate(invitation.getCheckInDate());
        record.setCheckOutDate(invitation.getCheckOutDate());
        record.setOverallScore(overallScore);
        record.setReviewContent(reviewContent != null ? reviewContent.trim() : null);

        List<String> selectedTags = (List<String>) reviewData.get("selectedTags");
        if (selectedTags != null && !selectedTags.isEmpty()) {
            record.setSelectedTags(String.join(",", selectedTags));
        }

        Integer isAnonymous = (Integer) reviewData.get("isAnonymous");
        record.setIsAnonymous(isAnonymous != null ? isAnonymous : 0);
        record.setReviewStatus(0);
        record.setIsTop(0);
        record.setLikeCount(0);
        record.setReviewTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setDeleted(0);

        reviewRecordMapper.insert(record);

        for (Map<String, Object> scoreMap : metricScores) {
            Long metricId = Long.valueOf(scoreMap.get("metricId").toString());
            Integer score = Integer.valueOf(scoreMap.get("score").toString());

            ReviewMetric metric = null;
            for (ReviewMetric m : metrics) {
                if (m.getId().equals(metricId)) {
                    metric = m;
                    break;
                }
            }

            if (metric != null) {
                ReviewMetricScore metricScore = new ReviewMetricScore();
                metricScore.setReviewRecordId(record.getId());
                metricScore.setMetricId(metricId);
                metricScore.setMetricName(metric.getMetricName());
                metricScore.setScore(score);
                metricScore.setWeight(metric.getWeight());
                metricScore.setCreateTime(LocalDateTime.now());
                reviewMetricScoreMapper.insert(metricScore);
            }
        }

        List<Map<String, Object>> images = (List<Map<String, Object>>) reviewData.get("images");
        if (images != null && !images.isEmpty()) {
            int sortOrder = 0;
            for (Map<String, Object> imageMap : images) {
                ReviewImage image = new ReviewImage();
                image.setReviewRecordId(record.getId());
                image.setImageUrl((String) imageMap.get("imageUrl"));
                image.setImageName((String) imageMap.get("imageName"));
                image.setSortOrder(sortOrder++);
                image.setCreateTime(LocalDateTime.now());
                reviewImageMapper.insert(image);
            }
        }

        invitation.setReviewStatus(1);
        invitation.setReviewTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());
        reviewInvitationMapper.update(invitation);

        if (record.getMemberId() != null) {
            PointEarnRule reviewRule = pointRuleService.getEnabledEarnRuleByType(3);
            if (reviewRule != null) {
                BigDecimal reviewPoints = reviewRule.getPointAmount();
                if (reviewPoints != null && reviewPoints.compareTo(BigDecimal.ZERO) > 0) {
                    LoginUser currentUser = null;
                    try {
                        currentUser = getCurrentUser();
                    } catch (Exception e) {
                    }
                    memberService.addPoints(
                            record.getMemberId(),
                            reviewPoints,
                            3,
                            "评价赠送",
                            "入住单" + record.getCheckInNo() + "评价获得" + reviewPoints + "积分",
                            currentUser != null ? currentUser.getUserId() : null,
                            currentUser != null ? (currentUser.getUser().getNickname() != null ? currentUser.getUser().getNickname() : currentUser.getUsername()) : "系统"
                    );
                }
            }
        }

        return record.getId();
    }

    public Map<String, Object> getReviewSuccessData(Long reviewRecordId) {
        if (reviewRecordId == null) {
            throw new BusinessException("评价记录ID不能为空");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.ID.eq(reviewRecordId))
                .and(REVIEW_RECORD.DELETED.eq(0));
        ReviewRecord record = reviewRecordMapper.selectOneByQuery(query);

        if (record == null) {
            throw new BusinessException("评价记录不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("review", record);
        result.put("isMember", record.getMemberId() != null);

        if (record.getMemberId() != null) {
            PointEarnRule reviewRule = pointRuleService.getEnabledEarnRuleByType(3);
            if (reviewRule != null) {
                result.put("earnedPoints", reviewRule.getPointAmount());
            } else {
                result.put("earnedPoints", BigDecimal.ZERO);
            }
        } else {
            result.put("earnedPoints", BigDecimal.ZERO);
        }

        return result;
    }

    private ReviewInvitation validateLink(String checkInNo, String code) {
        if (checkInNo == null || checkInNo.trim().isEmpty()) {
            throw new BusinessException("入住单号不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("验证码不能为空");
        }

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewInvitation.class)
                .where(REVIEW_INVITATION.CHECK_IN_NO.eq(checkInNo.trim()))
                .and(REVIEW_INVITATION.REVIEW_CODE.eq(code.trim()))
                .and(REVIEW_INVITATION.DELETED.eq(0));
        ReviewInvitation invitation = reviewInvitationMapper.selectOneByQuery(query);

        if (invitation == null) {
            throw new BusinessException("评价链接无效");
        }

        if (invitation.getReviewStatus() != null && invitation.getReviewStatus() == 1) {
            throw new BusinessException("该入住单已完成评价");
        }

        if (invitation.getExpireTime() != null && invitation.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("评价链接已过期");
        }

        QueryWrapper recordQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.CHECK_IN_ID.eq(invitation.getCheckInId()))
                .and(REVIEW_RECORD.DELETED.eq(0));
        long recordCount = reviewRecordMapper.selectCountByQuery(recordQuery);
        if (recordCount > 0) {
            throw new BusinessException("该入住单已完成评价");
        }

        return invitation;
    }

    private BigDecimal calculateOverallScore(List<Map<String, Object>> metricScores, List<ReviewMetric> metrics) {
        if (metricScores == null || metricScores.isEmpty() || metrics == null || metrics.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int totalWeight = 0;
        BigDecimal weightedSum = BigDecimal.ZERO;

        for (ReviewMetric metric : metrics) {
            totalWeight += metric.getWeight() != null ? metric.getWeight() : 0;

            for (Map<String, Object> scoreMap : metricScores) {
                Long metricId = Long.valueOf(scoreMap.get("metricId").toString());
                if (metricId.equals(metric.getId())) {
                    Integer score = Integer.valueOf(scoreMap.get("score").toString());
                    int weight = metric.getWeight() != null ? metric.getWeight() : 0;
                    weightedSum = weightedSum.add(BigDecimal.valueOf(score * weight));
                    break;
                }
            }
        }

        if (totalWeight == 0) {
            return BigDecimal.ZERO;
        }

        return weightedSum.divide(BigDecimal.valueOf(totalWeight), 1, BigDecimal.ROUND_HALF_UP);
    }

    public ReviewRecord getById(Long id) {
        if (id == null) {
            throw new BusinessException("ID不能为空");
        }
        ReviewRecord record = reviewRecordMapper.selectOneById(id);
        if (record == null || record.getDeleted() == 1) {
            throw new BusinessException("评价记录不存在");
        }
        fillRelationData(record);
        return record;
    }

    public ReviewRecord getByCheckInId(Long checkInId) {
        if (checkInId == null) {
            throw new BusinessException("入住单ID不能为空");
        }
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.CHECK_IN_ID.eq(checkInId))
                .and(REVIEW_RECORD.DELETED.eq(0));
        ReviewRecord record = reviewRecordMapper.selectOneByQuery(query);
        if (record != null) {
            fillRelationData(record);
        }
        return record;
    }

    private void fillRelationData(ReviewRecord record) {
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
}
