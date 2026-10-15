package com.movie.recommend.service.impl;

import com.movie.movie.dto.MovieVO;
import com.movie.movie.service.MovieService;
import com.movie.recommend.entity.Rating;
import com.movie.recommend.service.RatingService;
import com.movie.user.entity.UserBehaviorLog;
import com.movie.user.service.UserBehaviorLogService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AutoencoderRecommender {
    private final RatingService ratingService;
    private final MovieService movieService;
    private final UserBehaviorLogService userBehaviorLogService;
    private Map<Long, Map<Long, Double>> userItemMatrix;
    private List<Long> userIds;
    private List<Long> movieIds;
    private int inputSize;
    private int hiddenSize = 32; // 隐层大小
    private double[][] weights1; // 输入层到隐层的权重
    private double[] bias1; // 隐层偏置
    private double[][] weights2; // 隐层到输出层的权重
    private double[] bias2; // 输出层偏置
    private static final double LEARNING_RATE = 0.01;
    private static final int EPOCHS = 100;
    private static final int TOP_N = 20;

    public AutoencoderRecommender(RatingService ratingService, MovieService movieService, UserBehaviorLogService userBehaviorLogService) {
        this.ratingService = ratingService;
        this.movieService = movieService;
        this.userBehaviorLogService = userBehaviorLogService;
        this.userItemMatrix = new HashMap<>();
        this.userIds = new ArrayList<>();
        this.movieIds = new ArrayList<>();
    }

    /**
     * 训练自编码器模型
     */
    public void trainModel() {
        // 1. 构建用户-物品矩阵
        buildUserItemMatrix();
        
        // 2. 初始化模型参数
        initializeParameters();
        
        // 3. 训练模型
        train();
    }

    /**
     * 构建用户-物品矩阵，同时使用评分数据和行为日志数据
     */
    private void buildUserItemMatrix() {
        List<MovieVO> allMovies = movieService.getAllMovies();
        Set<Long> movieIdSet = new HashSet<>();
        Set<Long> userIdSet = new HashSet<>();
        
        // 收集所有电影ID
        for (MovieVO movie : allMovies) {
            movieIdSet.add(movie.getId());
        }
        movieIds = new ArrayList<>(movieIdSet);
        inputSize = movieIds.size();
        
        // 1. 使用评分数据构建基础矩阵
        for (Long movieId : movieIds) {
            List<Rating> ratings = ratingService.getMovieRatings(movieId)
                    .stream()
                    .map(ratingDTO -> {
                        Rating rating = new Rating();
                        rating.setUserId(ratingDTO.getUserId());
                        rating.setMovieId(ratingDTO.getMovieId());
                        rating.setRating(ratingDTO.getRating());
                        return rating;
                    })
                    .collect(Collectors.toList());
            
            for (Rating rating : ratings) {
                userItemMatrix.computeIfAbsent(rating.getUserId(), k -> new HashMap<>())
                      .put(rating.getMovieId(), rating.getRating());
                userIdSet.add(rating.getUserId());
            }
        }
        
        // 2. 使用行为日志数据增强矩阵
        // 行为类型权重映射
        Map<String, Double> behaviorWeights = new HashMap<>();
        behaviorWeights.put("COLLECT", 8.0);      // 收藏行为权重
        behaviorWeights.put("WATCHED", 7.0);      // 已看行为权重
        behaviorWeights.put("RATING", 6.0);       // 评分行为权重
        behaviorWeights.put("WATCHING", 5.0);     // 在看行为权重
        behaviorWeights.put("WATCHLIST", 4.0);    // 想看行为权重
        behaviorWeights.put("VIEW", 2.0);         // 浏览行为权重
        behaviorWeights.put("NOTE", 3.0);         // 笔记行为权重
        
        // 处理行为日志
        // 由于UserBehaviorLogService没有提供getAllLogs()方法，我们通过用户ID获取行为日志
        // 先获取所有用户ID
        Set<Long> usersFromRatings = new HashSet<>(userItemMatrix.keySet());
        
        for (Long userId : usersFromRatings) {
            List<UserBehaviorLog> userLogs = userBehaviorLogService.getUserLogs(userId);
            for (UserBehaviorLog log : userLogs) {
                if (log.getTargetId() != null) {
                    Long movieId = log.getTargetId();
                    String behaviorType = log.getBehaviorType();
                    
                    // 只有当用户还没有对该电影评分时，才使用行为日志
                    if (movieIdSet.contains(movieId) && (!userItemMatrix.containsKey(userId) || !userItemMatrix.get(userId).containsKey(movieId))) {
                        Double weight = behaviorWeights.getOrDefault(behaviorType, 1.0);
                        userItemMatrix.computeIfAbsent(userId, k -> new HashMap<>())
                              .put(movieId, weight);
                        userIdSet.add(userId);
                    }
                }
            }
        }
        
        userIds = new ArrayList<>(userIdSet);
    }

    /**
     * 初始化模型参数
     */
    private void initializeParameters() {
        Random random = new Random();
        
        // 输入层到隐层的权重
        weights1 = new double[inputSize][hiddenSize];
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights1[i][j] = random.nextGaussian() * 0.1;
            }
        }
        
        // 隐层偏置
        bias1 = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            bias1[i] = 0.0;
        }
        
        // 隐层到输出层的权重
        weights2 = new double[hiddenSize][inputSize];
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights2[i][j] = random.nextGaussian() * 0.1;
            }
        }
        
        // 输出层偏置
        bias2 = new double[inputSize];
        for (int i = 0; i < inputSize; i++) {
            bias2[i] = 0.0;
        }
    }

    /**
     * 训练模型
     */
    private void train() {
        for (int epoch = 0; epoch < EPOCHS; epoch++) {
            double totalLoss = 0.0;
            
            for (Long userId : userIds) {
                // 构建输入向量
                double[] input = buildInputVector(userId);
                
                // 前向传播
                double[] hidden = forwardPass(input);
                double[] output = backwardPass(hidden);
                
                // 计算损失
                double loss = calculateLoss(input, output);
                totalLoss += loss;
                
                // 反向传播
                backpropagate(input, hidden, output);
            }
            
            if (epoch % 10 == 0) {
                System.out.println("Epoch " + epoch + ", Loss: " + totalLoss / userIds.size());
            }
        }
    }

    /**
     * 构建输入向量
     */
    private double[] buildInputVector(Long userId) {
        double[] input = new double[inputSize];
        Map<Long, Double> userRatings = userItemMatrix.get(userId);
        
        for (int i = 0; i < movieIds.size(); i++) {
            Long movieId = movieIds.get(i);
            input[i] = userRatings != null ? userRatings.getOrDefault(movieId, 0.0) : 0.0;
        }
        
        return input;
    }

    /**
     * 前向传播（编码器）
     * h = σ(W₁x + b₁)
     */
    private double[] forwardPass(double[] input) {
        double[] hidden = new double[hiddenSize];
        
        for (int i = 0; i < hiddenSize; i++) {
            double sum = bias1[i];
            for (int j = 0; j < inputSize; j++) {
                sum += input[j] * weights1[j][i];
            }
            hidden[i] = sigmoid(sum);
        }
        
        return hidden;
    }

    /**
     * 反向传播（解码器）
     * x̂ = σ(W₂h + b₂)
     */
    private double[] backwardPass(double[] hidden) {
        double[] output = new double[inputSize];
        
        for (int i = 0; i < inputSize; i++) {
            double sum = bias2[i];
            for (int j = 0; j < hiddenSize; j++) {
                sum += hidden[j] * weights2[j][i];
            }
            output[i] = sigmoid(sum);
        }
        
        return output;
    }

    /**
     * Sigmoid激活函数
     */
    private double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    /**
     * 计算均方误差损失
     * L(x, x̂) = (1/n)∑||x_i - x̂_i||²
     */
    private double calculateLoss(double[] input, double[] output) {
        double loss = 0.0;
        for (int i = 0; i < inputSize; i++) {
            loss += Math.pow(input[i] - output[i], 2);
        }
        return loss / inputSize;
    }

    /**
     * 反向传播更新参数
     */
    private void backpropagate(double[] input, double[] hidden, double[] output) {
        // 计算输出层误差
        double[] outputError = new double[inputSize];
        for (int i = 0; i < inputSize; i++) {
            outputError[i] = (input[i] - output[i]) * output[i] * (1 - output[i]);
        }
        
        // 计算隐层误差
        double[] hiddenError = new double[hiddenSize];
        for (int i = 0; i < hiddenSize; i++) {
            double error = 0.0;
            for (int j = 0; j < inputSize; j++) {
                error += outputError[j] * weights2[i][j];
            }
            hiddenError[i] = error * hidden[i] * (1 - hidden[i]);
        }
        
        // 更新权重和偏置
        // 更新输出层权重和偏置
        for (int i = 0; i < hiddenSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                weights2[i][j] += LEARNING_RATE * outputError[j] * hidden[i];
            }
        }
        
        for (int i = 0; i < inputSize; i++) {
            bias2[i] += LEARNING_RATE * outputError[i];
        }
        
        // 更新输入层权重和偏置
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < hiddenSize; j++) {
                weights1[i][j] += LEARNING_RATE * hiddenError[j] * input[i];
            }
        }
        
        for (int i = 0; i < hiddenSize; i++) {
            bias1[i] += LEARNING_RATE * hiddenError[i];
        }
    }

    /**
     * 获取推荐
     */
    public List<MovieVO> getRecommendations(Long userId) {
        if (userItemMatrix.isEmpty()) {
            trainModel();
        }
        
        // 构建输入向量
        double[] input = buildInputVector(userId);
        
        // 前向传播获取隐层特征
        double[] hidden = forwardPass(input);
        
        // 反向传播获取预测评分
        double[] predicted = backwardPass(hidden);
        
        // 生成推荐列表
        Map<Long, Double> predictedRatings = new HashMap<>();
        Map<Long, Double> userRatings = userItemMatrix.get(userId);
        
        for (int i = 0; i < movieIds.size(); i++) {
            Long movieId = movieIds.get(i);
            // 只推荐未评分的电影
            if (userRatings == null || !userRatings.containsKey(movieId)) {
                // 将sigmoid输出转换为0-10的评分
                double rating = predicted[i] * 10.0;
                predictedRatings.put(movieId, rating);
            }
        }
        
        // 按预测评分排序并返回Top-N
        return predictedRatings.entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(TOP_N)
                .map(entry -> movieService.getMovieById(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户隐式特征
     */
    public double[] getUserFeatures(Long userId) {
        if (userItemMatrix.isEmpty()) {
            trainModel();
        }
        
        double[] input = buildInputVector(userId);
        return forwardPass(input);
    }
}
