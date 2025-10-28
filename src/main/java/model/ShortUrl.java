package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortUrl {
    private UUID id;
    private String originalUrl;
    private String shortCode;
    private UUID userId;
    private int clickLimit;
    private int clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isActive;

    public ShortUrl(String originalUrl, String shortCode, UUID userId, int clickLimit, LocalDateTime expiresAt) {
        this.id = UUID.randomUUID();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.userId = userId;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.isActive = true;
    }

    // Getters and setters
    public UUID getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public UUID getUserId() { return userId; }
    public int getClickLimit() { return clickLimit; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isActive() { return isActive; }

    public void incrementClickCount() { this.clickCount++; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isClickLimitExceeded() {
        return clickCount >= clickLimit;
    }

    public boolean isValid() {
        return isActive && !isExpired() && !isClickLimitExceeded();
    }
}