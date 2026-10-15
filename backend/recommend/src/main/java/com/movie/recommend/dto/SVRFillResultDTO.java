package com.movie.recommend.dto;

public class SVRFillResultDTO {
    private int originalUserCount;
    private int originalMovieCount;
    private int originalRatingCount;
    private double originalSparsity;
    private int filledUserCount;
    private int filledMovieCount;
    private int filledRatingCount;
    private double filledSparsity;
    private int newPredictedCount;
    private double executionTimeMs;
    private String status;

    public SVRFillResultDTO() {
    }

    public SVRFillResultDTO(int originalUserCount, int originalMovieCount, int originalRatingCount, double originalSparsity, 
                           int filledUserCount, int filledMovieCount, int filledRatingCount, double filledSparsity, 
                           int newPredictedCount, double executionTimeMs, String status) {
        this.originalUserCount = originalUserCount;
        this.originalMovieCount = originalMovieCount;
        this.originalRatingCount = originalRatingCount;
        this.originalSparsity = originalSparsity;
        this.filledUserCount = filledUserCount;
        this.filledMovieCount = filledMovieCount;
        this.filledRatingCount = filledRatingCount;
        this.filledSparsity = filledSparsity;
        this.newPredictedCount = newPredictedCount;
        this.executionTimeMs = executionTimeMs;
        this.status = status;
    }

    public int getOriginalUserCount() {
        return originalUserCount;
    }

    public void setOriginalUserCount(int originalUserCount) {
        this.originalUserCount = originalUserCount;
    }

    public int getOriginalMovieCount() {
        return originalMovieCount;
    }

    public void setOriginalMovieCount(int originalMovieCount) {
        this.originalMovieCount = originalMovieCount;
    }

    public int getOriginalRatingCount() {
        return originalRatingCount;
    }

    public void setOriginalRatingCount(int originalRatingCount) {
        this.originalRatingCount = originalRatingCount;
    }

    public double getOriginalSparsity() {
        return originalSparsity;
    }

    public void setOriginalSparsity(double originalSparsity) {
        this.originalSparsity = originalSparsity;
    }

    public int getFilledUserCount() {
        return filledUserCount;
    }

    public void setFilledUserCount(int filledUserCount) {
        this.filledUserCount = filledUserCount;
    }

    public int getFilledMovieCount() {
        return filledMovieCount;
    }

    public void setFilledMovieCount(int filledMovieCount) {
        this.filledMovieCount = filledMovieCount;
    }

    public int getFilledRatingCount() {
        return filledRatingCount;
    }

    public void setFilledRatingCount(int filledRatingCount) {
        this.filledRatingCount = filledRatingCount;
    }

    public double getFilledSparsity() {
        return filledSparsity;
    }

    public void setFilledSparsity(double filledSparsity) {
        this.filledSparsity = filledSparsity;
    }

    public int getNewPredictedCount() {
        return newPredictedCount;
    }

    public void setNewPredictedCount(int newPredictedCount) {
        this.newPredictedCount = newPredictedCount;
    }

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(double executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
