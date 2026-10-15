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
public class UserProfileBuilder {
    private final RatingRepository ratingRepository;
    private final MovieService movieService;
    private final UserBehaviorLogService userBehaviorLogService;
    private Map<Long, Map<String, Double>> userProfiles;
    private Map<Long, Integer> userClusters;
    private List<double[]> clusterCentroids;
    private static final int K = 5; // 聚类数量
    private static final int MAX_ITERATIONS = 100; // 最大迭代次数

    public UserProfileBuilder(RatingRepository ratingRepository, MovieService movieService, 
                           UserBehaviorLogService userBehaviorLogService) {
        this.ratingRepository = ratingRepository;
        this.movieService = movieService;
        this.userBehaviorLogService = userBehaviorLogService;
        this.userProfiles = new HashMap<>();
        this.userClusters = new HashMap<>();
        this.clusterCentroids = new ArrayList<>();
    }

    /**
     * 构建用户画像
     */
    public void buildUserProfiles() {
        // 1. 提取用户兴趣特征（TF-IDF）
        extractUserInterestFeatures();
        
        // 2. K-means聚类
        performKMeansClustering();
    }

    /**
     * 提取用户兴趣特征（TF-IDF）
     */
    private void extractUserInterestFeatures() {
        // 获取所有电影
        List<MovieVO> allMovies = movieService.getAllMovies();
        
        // 构建语料库（所有电影类型）
        Set<String> corpus = new HashSet<>();
        for (MovieVO movie : allMovies) {
            if (movie.getGenre() != null) {
                String[] genres = movie.getGenre().split("\\|");
                corpus.addAll(Arrays.asList(genres));
            }
        }
        
        // 计算DF（文档频率）
        Map<String, Integer> dfMap = new HashMap<>();
        for (String genre : corpus) {
            int count = 0;
            for (MovieVO movie : allMovies) {
                if (movie.getGenre() != null && movie.getGenre().contains(genre)) {
                    count++;
                }
            }
            dfMap.put(genre, count);
        }
        
        // 计算TF-IDF
        int N = allMovies.size(); // 文档总数
        List<Long> allUsers = getAllUsers();
        
        for (Long userId : allUsers) {
            // 获取用户行为
            List<UserBehaviorLog> userLogs = userBehaviorLogService.getUserLogs(userId);
            List<Rating> userRatings = ratingRepository.findByUserId(userId);
            
            // 统计用户对各类型的行为次数
            Map<String, Integer> genreCounts = new HashMap<>();
            int totalBehaviors = 0;
            
            // 处理行为日志
            for (UserBehaviorLog log : userLogs) {
                if (log.getTargetId() != null) {
                    MovieVO movie = movieService.getMovieById(log.getTargetId());
                    if (movie != null && movie.getGenre() != null) {
                        String[] genres = movie.getGenre().split("\\|");
                        for (String genre : genres) {
                            genreCounts.merge(genre, 1, Integer::sum);
                            totalBehaviors++;
                        }
                    }
                }
            }
            
            // 处理评分
            for (Rating rating : userRatings) {
                MovieVO movie = movieService.getMovieById(rating.getMovieId());
                if (movie != null && movie.getGenre() != null) {
                    String[] genres = movie.getGenre().split("\\|");
                    for (String genre : genres) {
                        // 评分越高，权重越大
                        int weight = (int) Math.round(rating.getRating() / 2); // 0-5权重
                        genreCounts.merge(genre, weight, Integer::sum);
                        totalBehaviors += weight;
                    }
                }
            }
            
            // 计算TF-IDF
            Map<String, Double> tfIdfMap = new HashMap<>();
            for (String genre : corpus) {
                // TF: 词频 = 类型出现次数 / 总行为次数
                double tf = totalBehaviors > 0 ? (double) genreCounts.getOrDefault(genre, 0) / totalBehaviors : 0.0;
                
                // IDF: 逆文档频率 = log(N / (DF + 1))
                double idf = Math.log((double) N / (dfMap.getOrDefault(genre, 0) + 1));
                
                // TF-IDF = TF * IDF
                double tfIdf = tf * idf;
                tfIdfMap.put(genre, tfIdf);
            }
            
            userProfiles.put(userId, tfIdfMap);
        }
    }

    /**
     * 执行K-means聚类
     */
    private void performKMeansClustering() {
        if (userProfiles.isEmpty()) {
            return;
        }
        
        // 初始化聚类中心
        initializeCentroids();
        
        boolean converged = false;
        int iterations = 0;
        
        while (!converged && iterations < MAX_ITERATIONS) {
            // 分配样本到最近的聚类中心
            assignToClusters();
            
            // 更新聚类中心
            List<double[]> newCentroids = updateCentroids();
            
            // 检查收敛
            converged = checkConvergence(newCentroids);
            clusterCentroids = newCentroids;
            iterations++;
        }
    }

    /**
     * 初始化聚类中心
     */
    private void initializeCentroids() {
        List<Long> userIds = new ArrayList<>(userProfiles.keySet());
        Random random = new Random();
        
        for (int i = 0; i < K && i < userIds.size(); i++) {
            int randomIndex = random.nextInt(userIds.size());
            Long userId = userIds.get(randomIndex);
            userIds.remove(randomIndex);
            
            // 将用户特征转换为向量
            double[] centroid = convertToVector(userProfiles.get(userId));
            clusterCentroids.add(centroid);
        }
    }

    /**
     * 将用户特征转换为向量
     */
    private double[] convertToVector(Map<String, Double> features) {
        List<String> genres = new ArrayList<>(features.keySet());
        double[] vector = new double[genres.size()];
        
        for (int i = 0; i < genres.size(); i++) {
            vector[i] = features.get(genres.get(i));
        }
        
        return vector;
    }

    /**
     * 分配样本到最近的聚类中心
     */
    private void assignToClusters() {
        for (Long userId : userProfiles.keySet()) {
            double[] userVector = convertToVector(userProfiles.get(userId));
            int closestCluster = findClosestCluster(userVector);
            userClusters.put(userId, closestCluster);
        }
    }

    /**
     * 找到最近的聚类中心
     */
    private int findClosestCluster(double[] vector) {
        int closest = 0;
        double minDistance = Double.MAX_VALUE;
        
        for (int i = 0; i < clusterCentroids.size(); i++) {
            double distance = calculateDistance(vector, clusterCentroids.get(i));
            if (distance < minDistance) {
                minDistance = distance;
                closest = i;
            }
        }
        
        return closest;
    }

    /**
     * 计算欧氏距离
     */
    private double calculateDistance(double[] vector1, double[] vector2) {
        double sum = 0.0;
        for (int i = 0; i < vector1.length && i < vector2.length; i++) {
            sum += Math.pow(vector1[i] - vector2[i], 2);
        }
        return Math.sqrt(sum);
    }

    /**
     * 更新聚类中心
     */
    private List<double[]> updateCentroids() {
        List<double[]> newCentroids = new ArrayList<>();
        
        for (int i = 0; i < K; i++) {
            List<double[]> clusterVectors = new ArrayList<>();
            
            for (Map.Entry<Long, Integer> entry : userClusters.entrySet()) {
                if (entry.getValue() == i) {
                    clusterVectors.add(convertToVector(userProfiles.get(entry.getKey())));
                }
            }
            
            if (!clusterVectors.isEmpty()) {
                double[] centroid = calculateCentroid(clusterVectors);
                newCentroids.add(centroid);
            } else {
                // 如果聚类为空，保留原中心
                newCentroids.add(clusterCentroids.get(i));
            }
        }
        
        return newCentroids;
    }

    /**
     * 计算聚类中心
     */
    private double[] calculateCentroid(List<double[]> vectors) {
        if (vectors.isEmpty()) {
            return new double[0];
        }
        
        int dimension = vectors.get(0).length;
        double[] centroid = new double[dimension];
        
        for (double[] vector : vectors) {
            for (int i = 0; i < dimension; i++) {
                centroid[i] += vector[i];
            }
        }
        
        for (int i = 0; i < dimension; i++) {
            centroid[i] /= vectors.size();
        }
        
        return centroid;
    }

    /**
     * 检查收敛
     */
    private boolean checkConvergence(List<double[]> newCentroids) {
        for (int i = 0; i < clusterCentroids.size() && i < newCentroids.size(); i++) {
            double distance = calculateDistance(clusterCentroids.get(i), newCentroids.get(i));
            if (distance > 0.001) { // 阈值
                return false;
            }
        }
        return true;
    }

    /**
     * 获取所有用户ID
     */
    private List<Long> getAllUsers() {
        Set<Long> userIds = new HashSet<>();
        
        // 1. 从评分中获取用户
        List<Rating> allRatings = ratingRepository.findAll();
        for (Rating rating : allRatings) {
            userIds.add(rating.getUserId());
        }
        
        // 2. 从行为日志中获取用户
        // 这里我们通过获取所有电影的行为日志来间接获取所有用户
        List<MovieVO> allMovies = movieService.getAllMovies();
        Set<Long> behaviorUserIds = new HashSet<>();
        for (MovieVO movie : allMovies) {
            // 假设我们可以通过电影ID获取相关的行为日志
            // 这里需要实现一个方法来获取所有行为日志
            // 暂时注释掉这部分，因为UserBehaviorLogService没有提供getAllLogs方法
        }
        
        return new ArrayList<>(userIds);
    }

    /**
     * 获取用户画像
     */
    public Map<String, Double> getUserProfile(Long userId) {
        if (userProfiles.isEmpty()) {
            buildUserProfiles();
        }
        return userProfiles.getOrDefault(userId, new HashMap<>());
    }

    /**
     * 获取用户聚类
     */
    public int getUserCluster(Long userId) {
        if (userClusters.isEmpty()) {
            buildUserProfiles();
        }
        return userClusters.getOrDefault(userId, 0);
    }

    /**
     * 获取聚类中心
     */
    public List<double[]> getClusterCentroids() {
        if (clusterCentroids.isEmpty()) {
            buildUserProfiles();
        }
        return clusterCentroids;
    }
}
