package com.example.permission.service;

import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.ReviewRecordTableDef.REVIEW_RECORD;
import static com.example.permission.entity.table.ReviewMetricScoreTableDef.REVIEW_METRIC_SCORE;
import static com.example.permission.entity.table.ReviewTagTableDef.REVIEW_TAG;
import static com.example.permission.entity.table.ReviewMetricTableDef.REVIEW_METRIC;
import static com.example.permission.entity.table.RoomTypeTableDef.ROOM_TYPE;

@Service
public class ReviewAnalyticsService {

    @Autowired
    private ReviewRecordMapper reviewRecordMapper;

    @Autowired
    private ReviewMetricScoreMapper reviewMetricScoreMapper;

    @Autowired
    private ReviewTagMapper reviewTagMapper;

    @Autowired
    private ReviewMetricMapper reviewMetricMapper;

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper approvedQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1));

        long totalCount = reviewRecordMapper.selectCountByQuery(approvedQuery);
        result.put("totalCount", totalCount);

        LocalDate firstDayOfMonth = LocalDate.now().withDayOfMonth(1);
        QueryWrapper thisMonthQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                .and(REVIEW_RECORD.REVIEW_TIME.ge(firstDayOfMonth.atStartOfDay()));
        long thisMonthCount = reviewRecordMapper.selectCountByQuery(thisMonthQuery);
        result.put("thisMonthCount", thisMonthCount);

        List<ReviewRecord> allApproved = reviewRecordMapper.selectListByQuery(approvedQuery);
        BigDecimal avgScore = BigDecimal.ZERO;
        long goodCount = 0;
        long badCount = 0;
        long repliedCount = 0;
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
                if (score.compareTo(BigDecimal.valueOf(2)) <= 0) {
                    badCount++;
                }
                if (r.getReplyContent() != null && !r.getReplyContent().trim().isEmpty()) {
                    repliedCount++;
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
        result.put("badRate", totalCount > 0 ?
                BigDecimal.valueOf(badCount * 100).divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP).intValue() : 0);
        result.put("replyRate", totalCount > 0 ?
                BigDecimal.valueOf(repliedCount * 100).divide(BigDecimal.valueOf(totalCount), 0, RoundingMode.HALF_UP).intValue() : 0);

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

    public Map<String, Object> getQuantityTrend(int months) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i).withDayOfMonth(1);
            YearMonth ym = YearMonth.from(monthDate);
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            QueryWrapper query = QueryWrapper.create()
                    .from(ReviewRecord.class)
                    .where(REVIEW_RECORD.DELETED.eq(0))
                    .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                    .and(REVIEW_RECORD.REVIEW_TIME.ge(startDate.atStartOfDay()))
                    .and(REVIEW_RECORD.REVIEW_TIME.lt(endDate.plusDays(1).atStartOfDay()));

            long count = reviewRecordMapper.selectCountByQuery(query);

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("count", count);
            trendData.add(item);
        }

        result.put("trendData", trendData);

        if (trendData.size() >= 2) {
            long lastMonth = (long) trendData.get(trendData.size() - 1).get("count");
            long prevMonth = (long) trendData.get(trendData.size() - 2).get("count");
            String trend = "stable";
            if (lastMonth > prevMonth * 1.1) trend = "rising";
            else if (lastMonth < prevMonth * 0.9) trend = "falling";
            result.put("trend", trend);
        }

        return result;
    }

    public Map<String, Object> getScoreTrend(int months) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i).withDayOfMonth(1);
            YearMonth ym = YearMonth.from(monthDate);
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            QueryWrapper query = QueryWrapper.create()
                    .from(ReviewRecord.class)
                    .where(REVIEW_RECORD.DELETED.eq(0))
                    .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                    .and(REVIEW_RECORD.REVIEW_TIME.ge(startDate.atStartOfDay()))
                    .and(REVIEW_RECORD.REVIEW_TIME.lt(endDate.plusDays(1).atStartOfDay()));

            List<ReviewRecord> records = reviewRecordMapper.selectListByQuery(query);
            BigDecimal avgScore = BigDecimal.ZERO;
            int goodRate = 0;

            if (!records.isEmpty()) {
                BigDecimal totalScore = BigDecimal.ZERO;
                long goodCount = 0;
                for (ReviewRecord r : records) {
                    BigDecimal score = r.getOverallScore() != null ? r.getOverallScore() : BigDecimal.ZERO;
                    totalScore = totalScore.add(score);
                    if (score.compareTo(BigDecimal.valueOf(4)) >= 0) {
                        goodCount++;
                    }
                }
                avgScore = totalScore.divide(BigDecimal.valueOf(records.size()), 1, RoundingMode.HALF_UP);
                goodRate = BigDecimal.valueOf(goodCount * 100).divide(BigDecimal.valueOf(records.size()), 0, RoundingMode.HALF_UP).intValue();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("avgScore", avgScore);
            item.put("goodRate", goodRate);
            trendData.add(item);
        }

        result.put("trendData", trendData);

        if (trendData.size() >= 2) {
            BigDecimal lastScore = (BigDecimal) trendData.get(trendData.size() - 1).get("avgScore");
            BigDecimal prevScore = (BigDecimal) trendData.get(trendData.size() - 2).get("avgScore");
            String trend = "stable";
            if (lastScore.compareTo(prevScore.add(BigDecimal.valueOf(0.1))) > 0) trend = "rising";
            else if (lastScore.compareTo(prevScore.subtract(BigDecimal.valueOf(0.1))) < 0) trend = "falling";
            result.put("trend", trend);
            result.put("changeAmount", lastScore.subtract(prevScore));
        }

        return result;
    }

    public Map<String, Object> getMetricRadar() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper metricQuery = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc());
        List<ReviewMetric> metrics = reviewMetricMapper.selectListByQuery(metricQuery);

        List<Map<String, Object>> metricData = new ArrayList<>();
        BigDecimal highestScore = BigDecimal.ZERO;
        BigDecimal lowestScore = new BigDecimal("5");
        String strongestMetric = "";
        String weakestMetric = "";

        for (ReviewMetric metric : metrics) {
            QueryWrapper scoreQuery = QueryWrapper.create()
                    .from(ReviewMetricScore.class)
                    .where(REVIEW_METRIC_SCORE.METRIC_ID.eq(metric.getId()));
            List<ReviewMetricScore> scores = reviewMetricScoreMapper.selectListByQuery(scoreQuery);

            BigDecimal avgScore = BigDecimal.ZERO;
            BigDecimal maxScore = BigDecimal.ZERO;
            BigDecimal minScore = new BigDecimal("5");
            BigDecimal variance = BigDecimal.ZERO;

            if (!scores.isEmpty()) {
                BigDecimal totalScore = BigDecimal.ZERO;
                for (ReviewMetricScore s : scores) {
                    BigDecimal score = BigDecimal.valueOf(s.getScore() != null ? s.getScore() : 0);
                    totalScore = totalScore.add(score);
                    if (score.compareTo(maxScore) > 0) maxScore = score;
                    if (score.compareTo(minScore) < 0) minScore = score;
                }
                avgScore = totalScore.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);

                BigDecimal sumSqDiff = BigDecimal.ZERO;
                for (ReviewMetricScore s : scores) {
                    BigDecimal score = BigDecimal.valueOf(s.getScore() != null ? s.getScore() : 0);
                    BigDecimal diff = score.subtract(avgScore);
                    sumSqDiff = sumSqDiff.add(diff.multiply(diff));
                }
                variance = sumSqDiff.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            }

            BigDecimal stdDev = new BigDecimal(Math.sqrt(variance.doubleValue()));
            stdDev = stdDev.setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> item = new HashMap<>();
            item.put("metricId", metric.getId());
            item.put("metricName", metric.getMetricName());
            item.put("avgScore", avgScore);
            item.put("maxScore", maxScore);
            item.put("minScore", minScore);
            item.put("stdDev", stdDev);
            item.put("count", scores.size());
            metricData.add(item);

            if (avgScore.compareTo(highestScore) > 0) {
                highestScore = avgScore;
                strongestMetric = metric.getMetricName();
            }
            if (avgScore.compareTo(lowestScore) < 0) {
                lowestScore = avgScore;
                weakestMetric = metric.getMetricName();
            }
        }

        metricData.sort((a, b) -> ((BigDecimal) b.get("avgScore")).compareTo((BigDecimal) a.get("avgScore")));
        for (int i = 0; i < metricData.size(); i++) {
            metricData.get(i).put("rank", i + 1);
        }

        result.put("metrics", metricData);
        result.put("strongestMetric", strongestMetric);
        result.put("weakestMetric", weakestMetric);

        return result;
    }

    public Map<String, Object> getMetricComparison(String periodType) {
        Map<String, Object> result = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDate currentStart, currentEnd, previousStart, previousEnd;

        if ("quarter".equals(periodType)) {
            int currentQuarter = (today.getMonthValue() - 1) / 3;
            currentStart = today.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1);
            currentEnd = currentStart.plusMonths(3).minusDays(1);
            previousStart = currentStart.minusMonths(3);
            previousEnd = currentStart.minusDays(1);
        } else {
            currentStart = today.withDayOfMonth(1);
            currentEnd = today;
            previousStart = currentStart.minusMonths(1);
            previousEnd = currentStart.minusDays(1);
        }

        QueryWrapper metricQuery = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc());
        List<ReviewMetric> metrics = reviewMetricMapper.selectListByQuery(metricQuery);

        List<Map<String, Object>> comparisonData = new ArrayList<>();
        List<String> improvedMetrics = new ArrayList<>();
        List<String> declinedMetrics = new ArrayList<>();

        for (ReviewMetric metric : metrics) {
            BigDecimal currentAvg = calculateMetricAverage(metric.getId(), currentStart, currentEnd);
            BigDecimal previousAvg = calculateMetricAverage(metric.getId(), previousStart, previousEnd);
            BigDecimal change = currentAvg.subtract(previousAvg);

            String trend = "stable";
            if (change.compareTo(BigDecimal.valueOf(0.1)) > 0) {
                trend = "rising";
                improvedMetrics.add(metric.getMetricName());
            } else if (change.compareTo(BigDecimal.valueOf(-0.1)) < 0) {
                trend = "falling";
                declinedMetrics.add(metric.getMetricName());
            }

            Map<String, Object> item = new HashMap<>();
            item.put("metricId", metric.getId());
            item.put("metricName", metric.getMetricName());
            item.put("previousScore", previousAvg);
            item.put("currentScore", currentAvg);
            item.put("change", change);
            item.put("trend", trend);
            comparisonData.add(item);
        }

        result.put("comparisonData", comparisonData);
        result.put("improvedMetrics", improvedMetrics);
        result.put("declinedMetrics", declinedMetrics);

        return result;
    }

    private BigDecimal calculateMetricAverage(Long metricId, LocalDate startDate, LocalDate endDate) {
        QueryWrapper recordQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .select(REVIEW_RECORD.ID)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                .and(REVIEW_RECORD.REVIEW_TIME.ge(startDate.atStartOfDay()))
                .and(REVIEW_RECORD.REVIEW_TIME.lt(endDate.plusDays(1).atStartOfDay()));
        List<ReviewRecord> records = reviewRecordMapper.selectListByQuery(recordQuery);
        List<Long> recordIds = records.stream().map(ReviewRecord::getId).collect(Collectors.toList());

        if (recordIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        QueryWrapper scoreQuery = QueryWrapper.create()
                .from(ReviewMetricScore.class)
                .where(REVIEW_METRIC_SCORE.METRIC_ID.eq(metricId))
                .and(REVIEW_METRIC_SCORE.REVIEW_RECORD_ID.in(recordIds));
        List<ReviewMetricScore> scores = reviewMetricScoreMapper.selectListByQuery(scoreQuery);

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        for (ReviewMetricScore s : scores) {
            totalScore = totalScore.add(BigDecimal.valueOf(s.getScore() != null ? s.getScore() : 0));
        }
        return totalScore.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);
    }

    public Map<String, Object> getTagStatistics() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper approvedQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .select(REVIEW_RECORD.SELECTED_TAGS)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1));
        List<ReviewRecord> records = reviewRecordMapper.selectListByQuery(approvedQuery);

        Map<Long, Integer> tagCountMap = new HashMap<>();
        for (ReviewRecord r : records) {
            String selectedTags = r.getSelectedTags();
            if (selectedTags != null && !selectedTags.isEmpty()) {
                String[] tagIds = selectedTags.split(",");
                for (String tid : tagIds) {
                    try {
                        Long tagId = Long.parseLong(tid.trim());
                        tagCountMap.put(tagId, tagCountMap.getOrDefault(tagId, 0) + 1);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        QueryWrapper tagQuery = QueryWrapper.create()
                .from(ReviewTag.class)
                .where(REVIEW_TAG.DELETED.eq(0))
                .and(REVIEW_TAG.STATUS.eq(1));
        List<ReviewTag> tags = reviewTagMapper.selectListByQuery(tagQuery);
        Map<Long, ReviewTag> tagMap = tags.stream()
                .collect(Collectors.toMap(ReviewTag::getId, t -> t));

        List<Map<String, Object>> positiveTags = new ArrayList<>();
        List<Map<String, Object>> negativeTags = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : tagCountMap.entrySet()) {
            ReviewTag tag = tagMap.get(entry.getKey());
            if (tag != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("tagId", tag.getId());
                item.put("tagText", tag.getTagText());
                item.put("count", entry.getValue());

                if (tag.getTagType() == 1) {
                    positiveTags.add(item);
                } else {
                    negativeTags.add(item);
                }
            }
        }

        positiveTags.sort((a, b) -> (int) b.get("count") - (int) a.get("count"));
        negativeTags.sort((a, b) -> (int) b.get("count") - (int) a.get("count"));

        result.put("positiveTags", positiveTags.size() > 10 ? positiveTags.subList(0, 10) : positiveTags);
        result.put("negativeTags", negativeTags.size() > 10 ? negativeTags.subList(0, 10) : negativeTags);

        return result;
    }

    public Map<String, Object> getTagTrend(Long tagId, int months) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trendData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = today.minusMonths(i).withDayOfMonth(1);
            YearMonth ym = YearMonth.from(monthDate);
            String monthStr = ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            LocalDate startDate = ym.atDay(1);
            LocalDate endDate = ym.atEndOfMonth();

            QueryWrapper recordQuery = QueryWrapper.create()
                    .from(ReviewRecord.class)
                    .select(REVIEW_RECORD.SELECTED_TAGS)
                    .where(REVIEW_RECORD.DELETED.eq(0))
                    .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                    .and(REVIEW_RECORD.REVIEW_TIME.ge(startDate.atStartOfDay()))
                    .and(REVIEW_RECORD.REVIEW_TIME.lt(endDate.plusDays(1).atStartOfDay()));
            List<ReviewRecord> records = reviewRecordMapper.selectListByQuery(recordQuery);

            int count = 0;
            String tagIdStr = String.valueOf(tagId);
            for (ReviewRecord r : records) {
                String selectedTags = r.getSelectedTags();
                if (selectedTags != null && selectedTags.contains(tagIdStr)) {
                    String[] tagIds = selectedTags.split(",");
                    for (String tid : tagIds) {
                        try {
                            if (Long.parseLong(tid.trim()) == tagId) {
                                count++;
                                break;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }

            Map<String, Object> item = new HashMap<>();
            item.put("month", monthStr);
            item.put("count", count);
            trendData.add(item);
        }

        result.put("trendData", trendData);

        if (trendData.size() >= 2) {
            int lastMonth = (int) trendData.get(trendData.size() - 1).get("count");
            int prevMonth = (int) trendData.get(trendData.size() - 2).get("count");
            String trend = "stable";
            if (lastMonth > prevMonth * 1.1) trend = "rising";
            else if (lastMonth < prevMonth * 0.9) trend = "falling";
            result.put("trend", trend);
        }

        return result;
    }

    public Map<String, Object> getRoomTypeAnalysis() {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper roomTypeQuery = QueryWrapper.create()
                .from(RoomType.class)
                .where(ROOM_TYPE.DELETED.eq(0))
                .orderBy(ROOM_TYPE.ID.asc());
        List<RoomType> roomTypes = roomTypeMapper.selectListByQuery(roomTypeQuery);

        List<Map<String, Object>> roomTypeData = new ArrayList<>();
        BigDecimal highestScore = BigDecimal.ZERO;
        BigDecimal lowestScore = new BigDecimal("5");
        String bestRoomType = "";
        String worstRoomType = "";

        for (RoomType rt : roomTypes) {
            QueryWrapper query = QueryWrapper.create()
                    .from(ReviewRecord.class)
                    .where(REVIEW_RECORD.DELETED.eq(0))
                    .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                    .and(REVIEW_RECORD.ROOM_TYPE_ID.eq(rt.getId()));
            List<ReviewRecord> records = reviewRecordMapper.selectListByQuery(query);

            BigDecimal avgScore = BigDecimal.ZERO;
            int goodRate = 0;
            int badRate = 0;

            if (!records.isEmpty()) {
                BigDecimal totalScore = BigDecimal.ZERO;
                long goodCount = 0;
                long badCount = 0;
                for (ReviewRecord r : records) {
                    BigDecimal score = r.getOverallScore() != null ? r.getOverallScore() : BigDecimal.ZERO;
                    totalScore = totalScore.add(score);
                    if (score.compareTo(BigDecimal.valueOf(4)) >= 0) goodCount++;
                    if (score.compareTo(BigDecimal.valueOf(2)) <= 0) badCount++;
                }
                avgScore = totalScore.divide(BigDecimal.valueOf(records.size()), 1, RoundingMode.HALF_UP);
                goodRate = BigDecimal.valueOf(goodCount * 100).divide(BigDecimal.valueOf(records.size()), 0, RoundingMode.HALF_UP).intValue();
                badRate = BigDecimal.valueOf(badCount * 100).divide(BigDecimal.valueOf(records.size()), 0, RoundingMode.HALF_UP).intValue();
            }

            Map<String, Object> item = new HashMap<>();
            item.put("roomTypeId", rt.getId());
            item.put("roomTypeName", rt.getTypeName());
            item.put("reviewCount", records.size());
            item.put("avgScore", avgScore);
            item.put("goodRate", goodRate);
            item.put("badRate", badRate);
            roomTypeData.add(item);

            if (avgScore.compareTo(highestScore) > 0 && records.size() > 0) {
                highestScore = avgScore;
                bestRoomType = rt.getTypeName();
            }
            if (avgScore.compareTo(lowestScore) < 0 && records.size() > 0) {
                lowestScore = avgScore;
                worstRoomType = rt.getTypeName();
            }
        }

        roomTypeData.sort((a, b) -> ((BigDecimal) b.get("avgScore")).compareTo((BigDecimal) a.get("avgScore")));
        for (int i = 0; i < roomTypeData.size(); i++) {
            roomTypeData.get(i).put("rank", i + 1);
        }

        result.put("roomTypes", roomTypeData);
        result.put("bestRoomType", bestRoomType);
        result.put("worstRoomType", worstRoomType);

        return result;
    }

    public Map<String, Object> getRoomTypeMetrics(Long roomTypeId) {
        Map<String, Object> result = new HashMap<>();

        QueryWrapper metricQuery = QueryWrapper.create()
                .from(ReviewMetric.class)
                .where(REVIEW_METRIC.DELETED.eq(0))
                .and(REVIEW_METRIC.STATUS.eq(1))
                .orderBy(REVIEW_METRIC.SORT_ORDER.asc());
        List<ReviewMetric> metrics = reviewMetricMapper.selectListByQuery(metricQuery);

        QueryWrapper recordQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .select(REVIEW_RECORD.ID)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                .and(REVIEW_RECORD.ROOM_TYPE_ID.eq(roomTypeId));
        List<ReviewRecord> roomTypeRecords = reviewRecordMapper.selectListByQuery(recordQuery);
        List<Long> roomTypeRecordIds = roomTypeRecords.stream().map(ReviewRecord::getId).collect(Collectors.toList());

        QueryWrapper allRecordQuery = QueryWrapper.create()
                .from(ReviewRecord.class)
                .select(REVIEW_RECORD.ID)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1));
        List<ReviewRecord> allRecords = reviewRecordMapper.selectListByQuery(allRecordQuery);
        List<Long> allRecordIds = allRecords.stream().map(ReviewRecord::getId).collect(Collectors.toList());

        List<Map<String, Object>> metricData = new ArrayList<>();

        for (ReviewMetric metric : metrics) {
            BigDecimal roomTypeAvg = calculateMetricAverageForRecords(metric.getId(), roomTypeRecordIds);
            BigDecimal overallAvg = calculateMetricAverageForRecords(metric.getId(), allRecordIds);

            Map<String, Object> item = new HashMap<>();
            item.put("metricId", metric.getId());
            item.put("metricName", metric.getMetricName());
            item.put("roomTypeScore", roomTypeAvg);
            item.put("overallScore", overallAvg);
            item.put("difference", roomTypeAvg.subtract(overallAvg));
            metricData.add(item);
        }

        result.put("metrics", metricData);

        return result;
    }

    private BigDecimal calculateMetricAverageForRecords(Long metricId, List<Long> recordIds) {
        if (recordIds.isEmpty()) {
            return BigDecimal.ZERO;
        }

        QueryWrapper scoreQuery = QueryWrapper.create()
                .from(ReviewMetricScore.class)
                .where(REVIEW_METRIC_SCORE.METRIC_ID.eq(metricId))
                .and(REVIEW_METRIC_SCORE.REVIEW_RECORD_ID.in(recordIds));
        List<ReviewMetricScore> scores = reviewMetricScoreMapper.selectListByQuery(scoreQuery);

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        for (ReviewMetricScore s : scores) {
            totalScore = totalScore.add(BigDecimal.valueOf(s.getScore() != null ? s.getScore() : 0));
        }
        return totalScore.divide(BigDecimal.valueOf(scores.size()), 1, RoundingMode.HALF_UP);
    }

    public PageResult<ReviewRecord> getReviewsByScore(Integer score, Long pageNum, Long pageSize) {
        BigDecimal minScore = BigDecimal.valueOf(score - 0.5);
        BigDecimal maxScore = BigDecimal.valueOf(score + 0.5);

        QueryWrapper query = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                .and(REVIEW_RECORD.OVERALL_SCORE.ge(minScore))
                .and(REVIEW_RECORD.OVERALL_SCORE.lt(maxScore))
                .orderBy(REVIEW_RECORD.REVIEW_TIME.desc());

        Page<ReviewRecord> page = reviewRecordMapper.paginate(
                Page.of(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10),
                query
        );

        return new PageResult<>(page.getTotalRow(), page.getRecords(), page.getPageNumber(), page.getPageSize());
    }

    public PageResult<ReviewRecord> getReviewsByRoomType(Long roomTypeId, Long pageNum, Long pageSize) {
        QueryWrapper query = QueryWrapper.create()
                .from(ReviewRecord.class)
                .where(REVIEW_RECORD.DELETED.eq(0))
                .and(REVIEW_RECORD.REVIEW_STATUS.eq(1))
                .and(REVIEW_RECORD.ROOM_TYPE_ID.eq(roomTypeId))
                .orderBy(REVIEW_RECORD.REVIEW_TIME.desc());

        Page<ReviewRecord> page = reviewRecordMapper.paginate(
                Page.of(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10),
                query
        );

        return new PageResult<>(page.getTotalRow(), page.getRecords(), page.getPageNumber(), page.getPageSize());
    }
}
