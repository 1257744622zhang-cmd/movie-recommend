package com.movie.recommend.service.impl;

import com.movie.movie.dto.MovieVO;
import com.movie.movie.service.MovieService;
import com.movie.recommend.dto.SVRFillResultDTO;
import com.movie.recommend.entity.Rating;
import com.movie.recommend.repository.RatingRepository;
import com.movie.user.entity.UserBehaviorLog;
import com.movie.user.service.UserBehaviorLogService;
import org.springframework.stereotype.Service;
import weka.classifiers.functions.SMOreg;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SVRMatrixFiller {
    private final RatingRepository ratingRepository;
    private final MovieService movieService;
    private final UserBehaviorLogService userBehaviorLogService;
    private SMOreg model;
    private Instances dataset;
    private Map<String, Integer> genreMap;
    private Map<Long, Map<Long, Double>> filledMatrix;
    private SVRFillResultDTO lastFillResult;

    public SVRMatrixFiller(RatingRepository ratingRepository, MovieService movieService, 
                         UserBehaviorLogService userBehaviorLogService) {
        this.ratingRepository = ratingRepository;
        this.movieService = movieService;
        this.userBehaviorLogService = userBehaviorLogService;
        this.genreMap = new HashMap<>();
        this.filledMatrix = new HashMap<>();
    }

    /**
     * 填充评分矩阵
     */
    public SVRFillResultDTO fillRatingMatrix() {
        long startTime = System.currentTimeMillis();
        System.out.println("========== 开始SVR评分矩阵填充 ==========");
        
        try {
            // 1. 构建初始评分矩阵
            Map<Long, Map<Long, Double>> originalMatrix = buildOriginalMatrix();
            
            // 2. 统计原始矩阵信息
            List<MovieVO> allMovies = movieService.getAllMovies();
            int originalUserCount = originalMatrix.size();
            int originalMovieCount = allMovies.size();
            int originalRatingCount = originalMatrix.values().stream()
                    .mapToInt(Map::size).sum();
            double originalSparsity = calculateSparsity(originalUserCount, originalMovieCount, originalRatingCount);
            
            System.out.println("原始矩阵统计: 用户数=" + originalUserCount + ", 电影数=" + originalMovieCount + 
                             ", 评分数=" + originalRatingCount + ", 稀疏度=" + String.format("%.2f%%", originalSparsity * 100));
            
            // 3. 准备训练数据
            prepareTrainingData();
            
            // 4. 训练SVR模型
            trainSVRModel();
            
            // 5. 填充矩阵
            fillMatrix(originalMatrix);
            
            // 6. 统计填充后矩阵信息
            int filledUserCount = filledMatrix.size();
            int filledRatingCount = filledMatrix.values().stream()
                    .mapToInt(Map::size).sum();
            double filledSparsity = calculateSparsity(filledUserCount, originalMovieCount, filledRatingCount);
            int newPredictedCount = filledRatingCount - originalRatingCount;
            
            System.out.println("填充后矩阵统计: 用户数=" + filledUserCount + ", 电影数=" + originalMovieCount + 
                             ", 评分数=" + filledRatingCount + ", 新预测数=" + newPredictedCount + 
                             ", 稀疏度=" + String.format("%.2f%%", filledSparsity * 100));
            
            double executionTime = System.currentTimeMillis() - startTime;
            System.out.println("========== SVR评分矩阵填充完成，耗时: " + executionTime + "ms ==========");
            
            // 7. 构建返回结果
            SVRFillResultDTO result = new SVRFillResultDTO(
                originalUserCount,
                originalMovieCount,
                originalRatingCount,
                originalSparsity,
                filledUserCount,
                originalMovieCount,
                filledRatingCount,
                filledSparsity,
                newPredictedCount,
                executionTime,
                "success"
            );
            this.lastFillResult = result;
            
            return result;
            
        } catch (Exception e) {
            System.err.println("SVR填充过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            double executionTime = System.currentTimeMillis() - startTime;
            
            SVRFillResultDTO errorResult = new SVRFillResultDTO();
            errorResult.setExecutionTimeMs(executionTime);
            errorResult.setStatus("error: " + e.getMessage());
            
            return errorResult;
        }
    }

    /**
     * 计算稀疏度
     */
    private double calculateSparsity(int userCount, int movieCount, int ratingCount) {
        if (userCount == 0 || movieCount == 0) {
            return 1.0;
        }
        long totalPossible = (long) userCount * movieCount;
        return 1.0 - (double) ratingCount / totalPossible;
    }

    /**
     * 构建原始评分矩阵
     */
    private Map<Long, Map<Long, Double>> buildOriginalMatrix() {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        List<MovieVO> allMovies = movieService.getAllMovies();
        
        for (MovieVO movie : allMovies) {
            List<Rating> ratings = ratingRepository.findByMovieId(movie.getId());
            
            for (Rating rating : ratings) {
                matrix.computeIfAbsent(rating.getUserId(), k -> new HashMap<>())
                      .put(rating.getMovieId(), rating.getRating());
            }
        }
        
        return matrix;
    }

    /**
     * 准备训练数据
     */
    private void prepareTrainingData() {
        // 收集所有电影类型
        List<MovieVO> allMovies = movieService.getAllMovies();
        int genreIndex = 0;
        for (MovieVO movie : allMovies) {
            if (movie.getGenre() != null) {
                String[] genres = movie.getGenre().split("\\|");
                for (String genre : genres) {
                    if (!genreMap.containsKey(genre)) {
                        genreMap.put(genre, genreIndex++);
                    }
                }
            }
        }
        
        // 创建属性
        List<Attribute> attributes = new ArrayList<>();
        
        // 用户特征：平均评分、评分数量、行为次数
        attributes.add(new Attribute("userAvgRating"));
        attributes.add(new Attribute("userRatingCount"));
        attributes.add(new Attribute("userBehaviorCount"));
        
        // 电影特征：类型（one-hot编码）
        for (String genre : genreMap.keySet()) {
            attributes.add(new Attribute("genre_" + genre));
        }
        
        // 目标变量：评分
        attributes.add(new Attribute("rating"));
        
        // 创建数据集
        dataset = new Instances("RatingData", new java.util.ArrayList<>(attributes), 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);
        
        // 填充数据
        fillDataset();
    }

    /**
     * 填充数据集
     */
    private void fillDataset() {
        List<MovieVO> allMovies = movieService.getAllMovies();
        
        for (MovieVO movie : allMovies) {
            List<Rating> ratings = ratingRepository.findByMovieId(movie.getId());
            
            for (Rating rating : ratings) {
                double[] instanceValues = createInstanceValues(rating.getUserId(), movie);
                instanceValues[instanceValues.length - 1] = rating.getRating();
                dataset.add(new DenseInstance(1.0, instanceValues));
            }
        }
    }

    /**
     * 创建实例值
     */
    private double[] createInstanceValues(Long userId, MovieVO movie) {
        int numAttributes = 3 + genreMap.size() + 1; // 用户特征(3) + 类型特征 + 目标变量
        double[] values = new double[numAttributes];
        
        // 用户特征
        List<Rating> userRatings = ratingRepository.findByUserId(userId);
        values[0] = userRatings.stream().mapToDouble(Rating::getRating).average().orElse(5.0);
        values[1] = userRatings.size();
        
        List<UserBehaviorLog> userLogs = userBehaviorLogService.getUserLogs(userId);
        values[2] = userLogs.size();
        
        // 电影类型特征（one-hot编码）
        int genreStartIndex = 3;
        for (int i = 0; i < genreMap.size(); i++) {
            values[genreStartIndex + i] = 0.0;
        }
        
        if (movie.getGenre() != null) {
            String[] genres = movie.getGenre().split("\\|");
            for (String genre : genres) {
                if (genreMap.containsKey(genre)) {
                    values[genreStartIndex + genreMap.get(genre)] = 1.0;
                }
            }
        }
        
        return values;
    }

    /**
     * 训练SVR模型
     */
    private void trainSVRModel() {
        try {
            model = new SMOreg();
            // 设置参数：使用RBF核函数
            String[] options = new String[]{
                "-C", "1.0", // 正则化参数
                "-N", "0", // epsilon-insensitive loss function
                "-K", "weka.classifiers.functions.supportVector.RBFKernel", // RBF核
                "-G", "0.01" // 核函数参数
            };
            model.setOptions(options);
            model.buildClassifier(dataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 填充矩阵
     */
    private void fillMatrix(Map<Long, Map<Long, Double>> originalMatrix) {
        // 复制原始矩阵
        for (Map.Entry<Long, Map<Long, Double>> entry : originalMatrix.entrySet()) {
            filledMatrix.put(entry.getKey(), new HashMap<>(entry.getValue()));
        }
        
        // 获取所有用户和电影
        Set<Long> allUsers = originalMatrix.keySet();
        List<MovieVO> allMovies = movieService.getAllMovies();
        
        // 预测并填充缺失的评分
        for (Long userId : allUsers) {
            Map<Long, Double> userRatings = filledMatrix.get(userId);
            
            for (MovieVO movie : allMovies) {
                if (!userRatings.containsKey(movie.getId())) {
                    // 预测评分
                    double predictedRating = predictRating(userId, movie);
                    // 确保评分在0-10之间
                    predictedRating = Math.max(0.0, Math.min(10.0, predictedRating));
                    userRatings.put(movie.getId(), predictedRating);
                }
            }
        }
    }

    /**
     * 预测评分
     */
    private double predictRating(Long userId, MovieVO movie) {
        try {
            double[] instanceValues = createInstanceValues(userId, movie);
            DenseInstance instance = new DenseInstance(1.0, instanceValues);
            instance.setDataset(dataset);
            return model.classifyInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
            return 5.0; // 默认评分
        }
    }

    /**
     * 获取填充后的评分矩阵
     */
    public Map<Long, Map<Long, Double>> getFilledMatrix() {
        if (filledMatrix.isEmpty()) {
            fillRatingMatrix();
        }
        return filledMatrix;
    }
}
