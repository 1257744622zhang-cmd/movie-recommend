package com.movie.service;

import com.movie.entity.Movie;
import com.movie.entity.Rating;
import com.movie.entity.UserTag;
import com.movie.repository.MovieRepository;
import com.movie.repository.RatingRepository;
import com.movie.service.RecommendFeedbackService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendService {
    private final MovieRepository movieRepository;
    private final RatingRepository ratingRepository;
    private final MovieService movieService;
    private final UserTagService userTagService;
    private final RecommendFeedbackService feedbackService;

    public RecommendService(MovieRepository movieRepository, RatingRepository ratingRepository,
                            MovieService movieService, UserTagService userTagService,
                            RecommendFeedbackService feedbackService) {
        this.movieRepository = movieRepository;
        this.ratingRepository = ratingRepository;
        this.movieService = movieService;
        this.userTagService = userTagService;
        this.feedbackService = feedbackService;
    }

    public List<Movie> recommendForNewUser() {
        List<Movie> result = new ArrayList<>(movieService.getPopularTop30());
        String[] genres = {"剧情", "喜剧", "动作", "爱情", "科幻", "悬疑", "恐怖", "动画", "纪录片", "战争"};
        for (String g : genres) {
            List<Movie> byGenre = movieService.getByGenre(g, 3);
            for (Movie m : byGenre) {
                if (!result.stream().anyMatch(x -> x.getId().equals(m.getId()))) {
                    result.add(m);
                }
            }
        }
        // 随机打乱结果，支持刷新功能
        Collections.shuffle(result);
        return result.size() > 20 ? result.subList(0, 20) : result;
    }
    
    // 为新用户推荐电影，过滤掉不喜欢的电影
    public List<Movie> recommendForNewUser(Long userId) {
        List<Movie> result = new ArrayList<>(movieService.getPopularTop30());
        String[] genres = {"剧情", "喜剧", "动作", "爱情", "科幻", "悬疑", "恐怖", "动画", "纪录片", "战争"};
        for (String g : genres) {
            List<Movie> byGenre = movieService.getByGenre(g, 3);
            for (Movie m : byGenre) {
                if (!result.stream().anyMatch(x -> x.getId().equals(m.getId()))) {
                    result.add(m);
                }
            }
        }
        // 过滤掉不喜欢的电影
        final Set<Long> dislikedIds;
        if (userId != null) {
            dislikedIds = new HashSet<>(feedbackService.getDislikedMovieIds(userId));
            result = result.stream()
                    .filter(m -> !dislikedIds.contains(m.getId()))
                    .collect(Collectors.toList());
        } else {
            dislikedIds = new HashSet<>();
        }
        // 如果推荐数量不足20，从热门电影中补充
        if (result.size() < 20) {
            List<Movie> popularMovies = movieService.getPopularTop30();
            final Set<Long> existingIds = result.stream().map(Movie::getId).collect(Collectors.toSet());
            List<Movie> additionalMovies = popularMovies.stream()
                    .filter(m -> !dislikedIds.contains(m.getId()) && !existingIds.contains(m.getId()))
                    .limit(20 - result.size())
                    .collect(Collectors.toList());
            result.addAll(additionalMovies);
        }
        // 随机打乱结果，支持刷新功能
        Collections.shuffle(result);
        return result.size() > 20 ? result.subList(0, 20) : result;
    }

    public List<Movie> recommendForUser(Long userId) {
        List<Movie> recommendations = calculateRecommendations(userId);
        // 确保推荐数量固定为20个
        if (recommendations.size() < 20) {
            List<Movie> popularMovies = movieService.getPopularTop30();
            Set<Long> existingIds = recommendations.stream().map(Movie::getId).collect(Collectors.toSet());
            Set<Long> dislikedIds = new HashSet<>(feedbackService.getDislikedMovieIds(userId));
            List<Movie> additionalMovies = popularMovies.stream()
                    .filter(m -> !existingIds.contains(m.getId()) && !dislikedIds.contains(m.getId()))
                    .limit(20 - recommendations.size())
                    .collect(Collectors.toList());
            recommendations.addAll(additionalMovies);
        }
        // 随机打乱结果，支持刷新功能
        Collections.shuffle(recommendations);
        return recommendations.size() > 20 ? recommendations.subList(0, 20) : recommendations;
    }

    private List<Movie> calculateRecommendations(Long userId) {
        List<Rating> myRatings = ratingRepository.findByUserId(userId);
        Set<Long> myRatedIds = myRatings.stream().map(Rating::getMovieId).collect(Collectors.toSet());
        // 获取用户不喜欢的电影ID
        Set<Long> dislikedIds = new HashSet<>(feedbackService.getDislikedMovieIds(userId));

        if (myRatedIds.size() < 2) {
            List<Movie> newUserRecommend = recommendForNewUser();
            // 过滤掉不喜欢的电影
            List<Movie> filtered = newUserRecommend.stream()
                    .filter(m -> !dislikedIds.contains(m.getId()))
                    .collect(Collectors.toList());
            // 确保推荐数量固定为20个
            if (filtered.size() < 20) {
                List<Movie> popularMovies = movieService.getPopularTop30();
                Set<Long> existingIds = filtered.stream().map(Movie::getId).collect(Collectors.toSet());
                List<Movie> additionalMovies = popularMovies.stream()
                        .filter(m -> !dislikedIds.contains(m.getId()) && !existingIds.contains(m.getId()))
                        .limit(20 - filtered.size())
                        .collect(Collectors.toList());
                filtered.addAll(additionalMovies);
            }
            return filtered.size() > 20 ? filtered.subList(0, 20) : filtered;
        }

        Map<Long, Double> myScores = new HashMap<>();
        for (Rating r : myRatings) myScores.put(r.getMovieId(), r.getScore());

        List<Long> otherUserIds = ratingRepository.findAll().stream()
                .map(Rating::getUserId).distinct().filter(id -> !id.equals(userId)).collect(Collectors.toList());

        Map<Long, Double> simScores = new HashMap<>();
        for (Long otherId : otherUserIds) {
            List<Rating> otherRatings = ratingRepository.findByUserId(otherId);
            double sim = cosineSimilarityWithTags(userId, otherId, myScores, otherRatings);
            if (sim > 0) {
                for (Rating r : otherRatings) {
                    if (!myRatedIds.contains(r.getMovieId()) && !dislikedIds.contains(r.getMovieId()) && r.getScore() >= 7) {
                        simScores.merge(r.getMovieId(), sim * r.getScore(), Double::sum);
                    }
                }
            }
        }

        List<Long> sortedIds = simScores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(20).map(Map.Entry::getKey).collect(Collectors.toList());

        if (sortedIds.isEmpty()) {
            List<Movie> newUserRecommend = recommendForNewUser();
            // 过滤掉不喜欢的电影
            List<Movie> filtered = newUserRecommend.stream()
                    .filter(m -> !dislikedIds.contains(m.getId()))
                    .collect(Collectors.toList());
            // 确保推荐数量固定为20个
            if (filtered.size() < 20) {
                List<Movie> popularMovies = movieService.getPopularTop30();
                Set<Long> existingIds = filtered.stream().map(Movie::getId).collect(Collectors.toSet());
                List<Movie> additionalMovies = popularMovies.stream()
                        .filter(m -> !dislikedIds.contains(m.getId()) && !existingIds.contains(m.getId()))
                        .limit(20 - filtered.size())
                        .collect(Collectors.toList());
                filtered.addAll(additionalMovies);
            }
            return filtered.size() > 20 ? filtered.subList(0, 20) : filtered;
        }

        List<Movie> movies = movieRepository.findAllById(sortedIds);
        Map<Long, Movie> map = movies.stream().collect(Collectors.toMap(Movie::getId, m -> m));
        List<Movie> result = sortedIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        
        // 如果推荐数量不足20，从热门电影中补充
        if (result.size() < 20) {
            List<Movie> popularMovies = movieService.getPopularTop30();
            Set<Long> existingIds = result.stream().map(Movie::getId).collect(Collectors.toSet());
            List<Movie> additionalMovies = popularMovies.stream()
                    .filter(m -> !myRatedIds.contains(m.getId()) && !dislikedIds.contains(m.getId()) && !existingIds.contains(m.getId()))
                    .limit(20 - result.size())
                    .collect(Collectors.toList());
            result.addAll(additionalMovies);
        }
        
        return result;
    }

    public void handleDislike(Long userId, Long movieId) {
        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie != null && movie.getGenre() != null) {
            String genre = movie.getGenre();
            List<UserTag> userTags = userTagService.getUserTags(userId);
            for (UserTag tag : userTags) {
                if (tag.getTagName().equals(genre)) {
                    double newWeight = Math.max(0.1, tag.getWeight() * 0.8);
                    userTagService.adjustTagWeight(userId, genre, newWeight);
                    break;
                }
            }
        }
    }

    public boolean isDisliked(Long userId, Long movieId) {
        return feedbackService.isDisliked(userId, movieId);
    }

    private double cosineSimilarityWithTags(Long userId, Long otherId, Map<Long, Double> myScores, List<Rating> otherRatings) {
        double ratingSimilarity = cosineSimilarity(myScores, otherRatings);
        
        List<UserTag> myTags = userTagService.getUserTags(userId);
        List<UserTag> otherTags = userTagService.getUserTags(otherId);
        
        Map<String, Double> myTagWeights = new HashMap<>();
        for (UserTag tag : myTags) {
            myTagWeights.put(tag.getTagName(), tag.getWeight());
        }
        
        Map<String, Double> otherTagWeights = new HashMap<>();
        for (UserTag tag : otherTags) {
            otherTagWeights.put(tag.getTagName(), tag.getWeight());
        }
        
        double tagSimilarity = calculateTagSimilarity(myTagWeights, otherTagWeights);
        
        return ratingSimilarity * 0.7 + tagSimilarity * 0.3;
    }

    private double calculateTagSimilarity(Map<String, Double> tags1, Map<String, Double> tags2) {
        Set<String> commonTags = new HashSet<>(tags1.keySet());
        commonTags.retainAll(tags2.keySet());
        
        if (commonTags.isEmpty()) {
            return 0;
        }
        
        double dot = 0, norm1 = 0, norm2 = 0;
        
        for (String tag : commonTags) {
            double w1 = tags1.get(tag);
            double w2 = tags2.get(tag);
            dot += w1 * w2;
            norm1 += w1 * w1;
            norm2 += w2 * w2;
        }
        
        for (String tag : tags1.keySet()) {
            if (!commonTags.contains(tag)) {
                norm1 += tags1.get(tag) * tags1.get(tag);
            }
        }
        
        for (String tag : tags2.keySet()) {
            if (!commonTags.contains(tag)) {
                norm2 += tags2.get(tag) * tags2.get(tag);
            }
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0;
        }
        
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double cosineSimilarity(Map<Long, Double> a, List<Rating> bList) {
        Map<Long, Double> b = new HashMap<>();
        for (Rating r : bList) b.put(r.getMovieId(), r.getScore());
        Set<Long> common = new HashSet<>(a.keySet());
        common.retainAll(b.keySet());
        if (common.size() < 1) return 0;
        double dot = 0, na = 0, nb = 0;
        for (Long k : common) {
            double va = a.get(k), vb = b.get(k);
            dot += va * vb;
            na += va * va;
            nb += vb * vb;
        }
        for (Long k : a.keySet()) { if (!common.contains(k)) na += a.get(k) * a.get(k); }
        for (Long k : b.keySet()) { if (!common.contains(k)) nb += b.get(k) * b.get(k); }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
