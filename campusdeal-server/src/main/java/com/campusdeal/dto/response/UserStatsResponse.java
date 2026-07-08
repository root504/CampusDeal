package com.campusdeal.dto.response;

public class UserStatsResponse {

    private long publishedCount;
    private long soldCount;
    private int creditScore;

    public UserStatsResponse() {}

    public UserStatsResponse(long publishedCount, long soldCount, int creditScore) {
        this.publishedCount = publishedCount;
        this.soldCount = soldCount;
        this.creditScore = creditScore;
    }

    public long getPublishedCount() { return publishedCount; }
    public void setPublishedCount(long publishedCount) { this.publishedCount = publishedCount; }
    public long getSoldCount() { return soldCount; }
    public void setSoldCount(long soldCount) { this.soldCount = soldCount; }
    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }
}
