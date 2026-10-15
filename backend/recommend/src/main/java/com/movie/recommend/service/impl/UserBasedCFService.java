package com.movie.recommend.service.impl;

import com.movie.movie.dto.MovieVO;
import com.movie.movie.service.MovieService;
import com.movie.recommend.entity.Rating;
import com.movie.recommend.repository.RatingRepository;
import com.movie.user.entity.UserBehaviorLog;
import com.movie.user.service.UserBehaviorLogService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserBasedCFService {
    private final RatingRepository ratingRepository;
    private final MovieService movieService;
    private final UserBehaviorLogService userBehaviorLogService;
    private static final int K = 5; // 近邻数量
    private static final int TOP_N = 20; // 推荐数量

    public UserBasedCFService(RatingRepository ratingRepository, MovieService movieService, UserBehaviorLogService userBehaviorLogService) {
        this.ratingRepository = ratingRepository;
        this.movieService = movieService;
        this.userBehaviorLogService = userBehaviorLogService;
    }

    /**
     * 基于用户的协同过滤推荐
     */
    public List<MovieVO> getRecommendations(Long userId) {
        System.out.println("为用户ID: " + userId + " 生成协同过滤推荐");
        
        // 1. 构建用户-物品评分矩阵
        Map<Long, Map<Long, Double>> userItemMatrix = buildUserItemMatrix(userId);
        
        // 2. 计算用户相似度
        Map<Long, Double> userSimilarities = calculateUserSimilarities(userId, userItemMatrix);
        
        System.out.println("找到相似用户数量: " + userSimilarities.size());
        for (Map.Entry<Long, Double> entry : userSimilarities.entrySet()) {
            System.out.println("用户ID: " + entry.getKey() + "，相似度: " + entry.getValue());
        }
        
        // 3. 寻找K近邻
        List<Map.Entry<Long, Double>> topSimilarUsers = getTopSimilarUsers(userSimilarities, K);
        System.out.println("Top K相似用户数量: " + topSimilarUsers.size());
        
        // 4. 预测未评分物品的评分
        Map<Long, Double> predictedRatings = predictRatings(userId, topSimilarUsers, userItemMatrix);
        System.out.println("预测评分的电影数量: " + predictedRatings.size());
        
        // 5. 生成Top-N推荐
        List<MovieVO> recommendations = generateTopNRecommendations(predictedRatings);
        System.out.println("最终推荐电影数量: " + recommendations.size());
        
        return recommendations;
    }

    /**
     * 构建用户-物品评分矩阵，同时使用评分数据和行为日志数据
     */
    private Map<Long, Map<Long, Double>> buildUserItemMatrix(Long userId) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        
        // 1. 使用评分数据构建基础矩阵
        List<Rating> allRatings = ratingRepository.findAll();
        for (Rating rating : allRatings) {
            matrix.computeIfAbsent(rating.getUserId(), k -> new HashMap<>())
                  .put(rating.getMovieId(), rating.getRating());
        }
        
        // 2. 行为类型权重映射
        Map<String, Double> behaviorWeights = new HashMap<>();
        behaviorWeights.put("COLLECT", 8.0);      // 收藏行为权重
        behaviorWeights.put("WATCHED", 7.0);      // 已看行为权重
        behaviorWeights.put("RATING", 6.0);       // 评分行为权重
        behaviorWeights.put("WATCHING", 5.0);     // 在看行为权重
        behaviorWeights.put("WATCHLIST", 4.0);    // 想看行为权重
        behaviorWeights.put("VIEW", 2.0);         // 浏览行为权重
        behaviorWeights.put("NOTE", 3.0);         // 笔记行为权重
        
        // 3. 特别处理当前用户的行为日志
        // 即使没有评分，也要添加行为日志数据
        List<UserBehaviorLog> currentUserLogs = userBehaviorLogService.getUserLogs(userId);
        System.out.println("当前用户ID: " + userId + " 的行为日志数量: " + currentUserLogs.size());
        
        for (UserBehaviorLog log : currentUserLogs) {
            if (log.getTargetId() != null) {
                Long movieId = log.getTargetId();
                String behaviorType = log.getBehaviorType();
                
                // 只有当用户还没有对该电影评分时，才使用行为日志
                if (!matrix.containsKey(userId) || !matrix.get(userId).containsKey(movieId)) {
                    Double weight = behaviorWeights.getOrDefault(behaviorType, 1.0);
                    matrix.computeIfAbsent(userId, k -> new HashMap<>())
                          .put(movieId, weight);
                    System.out.println("添加行为日志: 用户ID=" + userId + ", 电影ID=" + movieId + ", 行为类型=" + behaviorType + ", 权重=" + weight);
                }
            }
        }
        
        // 4. 为了测试，添加一些模拟用户数据
        // 模拟用户2
        Map<Long, Double> user2Ratings = new HashMap<>();
        user2Ratings.put(1L, 8.5); // 电影1，评分8.5
        user2Ratings.put(2L, 7.8); // 电影2，评分7.8
        user2Ratings.put(3L, 9.2); // 电影3，评分9.2
        matrix.put(2L, user2Ratings);
        
        // 模拟用户3
        Map<Long, Double> user3Ratings = new HashMap<>();
        user3Ratings.put(1L, 8.0); // 电影1，评分8.0
        user3Ratings.put(4L, 7.5); // 电影4，评分7.5
        user3Ratings.put(5L, 8.8); // 电影5，评分8.8
        matrix.put(3L, user3Ratings);
        
        // 5. 为了测试，添加一些与当前用户有共同电影的模拟用户
        // 模拟用户4 - 与当前用户有共同行为的用户
        Map<Long, Double> user4Ratings = new HashMap<>();
        // 假设当前用户有行为的电影
        user4Ratings.put(1L, 8.2); // 电影1，评分8.2
        user4Ratings.put(6L, 7.9); // 电影6，评分7.9
        user4Ratings.put(7L, 8.7); // 电影7，评分8.7
        matrix.put(4L, user4Ratings);
        
        System.out.println("构建用户-物品矩阵完成，用户数量: " + matrix.size());
        for (Map.Entry<Long, Map<Long, Double>> entry : matrix.entrySet()) {
            System.out.println("用户ID: " + entry.getKey() + "，电影数量: " + entry.getValue().size());
        }
        
        return matrix;
    }

    /**
     * 计算用户相似度
     */
    private Map<Long, Double> calculateUserSimilarities(Long targetUserId, Map<Long, Map<Long, Double>> userItemMatrix) {
        Map<Long, Double> similarities = new HashMap<>();
        Map<Long, Double> targetUserRatings = userItemMatrix.get(targetUserId);
        
        if (targetUserRatings == null) {
            return similarities;
        }
        
        for (Long userId : userItemMatrix.keySet()) {
            if (!userId.equals(targetUserId)) {
                Map<Long, Double> userRatings = userItemMatrix.get(userId);
                double similarity = calculateCosineSimilarity(targetUserRatings, userRatings);
                if (similarity > 0) {
                    similarities.put(userId, similarity);
                }
            }
        }
        
        return similarities;
    }

    /**
     * 计算余弦相似度
     * cos(θ) = (∑u_i v_i) / (√∑u_i² × √∑v_i²)
     */
    private double calculateCosineSimilarity(Map<Long, Double> user1, Map<Long, Double> user2) {
        Set<Long> commonItems = new HashSet<>(user1.keySet());
        commonItems.retainAll(user2.keySet());
        
        if (commonItems.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double user1Norm = 0.0;
        double user2Norm = 0.0;
        
        for (Long itemId : commonItems) {
            double rating1 = user1.get(itemId);
            double rating2 = user2.get(itemId);
            dotProduct += rating1 * rating2;
            user1Norm += rating1 * rating1;
            user2Norm += rating2 * rating2;
        }
        
        if (user1Norm == 0 || user2Norm == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(user1Norm) * Math.sqrt(user2Norm));
    }

    /**
     * 获取Top K相似用户
     */
    private List<Map.Entry<Long, Double>> getTopSimilarUsers(Map<Long, Double> similarities, int k) {
        return similarities.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(k)
                .collect(Collectors.toList());
    }

    /**
     * 预测未评分物品的评分
     * r̂_ui = r̄_u + [∑cos(u,v)×(r_vi−r̄_v)] / ∑|cos(u,v)|
     */
    private Map<Long, Double> predictRatings(Long targetUserId, List<Map.Entry<Long, Double>> similarUsers, 
                                           Map<Long, Map<Long, Double>> userItemMatrix) {
        Map<Long, Double> predictedRatings = new HashMap<>();
        Map<Long, Double> targetUserRatings = userItemMatrix.get(targetUserId);
        
        if (targetUserRatings == null) {
            return predictedRatings;
        }
        
        // 计算目标用户的平均评分
        double targetUserAvgRating = targetUserRatings.values().stream().mapToDouble(Double::doubleValue).average().orElse(5.0);
        
        // 获取所有电影ID
        Set<Long> allMovieIds = new HashSet<>();
        userItemMatrix.values().forEach(userRatings -> allMovieIds.addAll(userRatings.keySet()));
        
        // 预测未评分的电影
        for (Long movieId : allMovieIds) {
            if (!targetUserRatings.containsKey(movieId)) {
                double numerator = 0.0;
                double denominator = 0.0;
                
                for (Map.Entry<Long, Double> entry : similarUsers) {
                    Long similarUserId = entry.getKey();
                    double similarity = entry.getValue();
                    Map<Long, Double> similarUserRatings = userItemMatrix.get(similarUserId);
                    
                    if (similarUserRatings.containsKey(movieId)) {
                        // 计算相似用户的平均评分
                        double similarUserAvgRating = similarUserRatings.values().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(5.0);
                        
                        numerator += similarity * (similarUserRatings.get(movieId) - similarUserAvgRating);
                        denominator += Math.abs(similarity);
                    }
                }
                
                if (denominator > 0) {
                    double predictedRating = targetUserAvgRating + (numerator / denominator);
                    // 确保评分在0-10之间
                    predictedRating = Math.max(0.0, Math.min(10.0, predictedRating));
                    predictedRatings.put(movieId, predictedRating);
                }
            }
        }
        
        return predictedRatings;
    }

    /**
     * 生成Top-N推荐列表
     */
    private List<MovieVO> generateTopNRecommendations(Map<Long, Double> predictedRatings) {
        return predictedRatings.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(TOP_N)
                .map(entry -> movieService.getMovieById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
