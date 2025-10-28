package service;

import config.AppConfig;
import model.ShortUrl;
import model.User;
import util.UrlValidator;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UrlShortenerService {
    private final Map<String, ShortUrl> shortCodeToUrlMap;
    private final UserService userService;
    private final ScheduledExecutorService scheduler;
    private final int serverPort;
    private final AppConfig config;

    public UrlShortenerService(UserService userService, AppConfig config) {
        this.shortCodeToUrlMap = new HashMap<>();
        this.userService = userService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.config = config;
        this.serverPort = config.getServerPort();
        startCleanupTask();
    }

    public String createShortUrl(String originalUrl, UUID userId) {
        return createShortUrlWithCustomLimit(originalUrl, userId, config.getDefaultClickLimit());
    }

    public String createShortUrlWithCustomLimit(String originalUrl, UUID userId, int clickLimit) {
        if (!UrlValidator.isValidUrl(originalUrl)) {
            throw new IllegalArgumentException("❌ Неверный формат URL");
        }

        User user = userService.getUser(userId);
        String shortCode = generateUniqueShortCode(originalUrl, userId);

        // Время жизни - из конфига
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(config.getDefaultTtlHours());

        ShortUrl shortUrl = new ShortUrl(originalUrl, shortCode, userId, clickLimit, expiresAt);
        user.addShortUrl(shortUrl);
        shortCodeToUrlMap.put(shortCode, shortUrl);

        System.out.println("✅ Создана короткая ссылка: " + shortCode + " → " + originalUrl);
        return "http://localhost:" + serverPort + "/" + shortCode;
    }

    public String redirect(String shortCode) {
        ShortUrl shortUrl = shortCodeToUrlMap.get(shortCode);

        if (shortUrl == null) {
            throw new RuntimeException("🔗 Короткая ссылка не найдена: " + shortCode);
        }

        if (!shortUrl.isValid()) {
            if (shortUrl.isExpired()) {
                throw new RuntimeException("⏰ Срок действия ссылки истек");
            } else if (shortUrl.isClickLimitExceeded()) {
                throw new RuntimeException("🚫 Лимит переходов исчерпан (" +
                        shortUrl.getClickCount() + "/" + shortUrl.getClickLimit() + ")");
            } else {
                throw new RuntimeException("❌ Ссылка неактивна");
            }
        }

        shortUrl.incrementClickCount();
        System.out.println("🔗 Переход по ссылке: " + shortCode + " → " + shortUrl.getOriginalUrl() +
                " (переходы: " + shortUrl.getClickCount() + "/" + shortUrl.getClickLimit() + ")");
        return shortUrl.getOriginalUrl();
    }

    public String getUrlStats(String shortCode, UUID userId) {
        ShortUrl shortUrl = shortCodeToUrlMap.get(shortCode);

        if (shortUrl == null) {
            throw new RuntimeException("🔗 Короткая ссылка не найдена");
        }

        if (!shortUrl.getUserId().equals(userId)) {
            throw new RuntimeException("🚫 Доступ запрещен");
        }

        return String.format(
                """
                📊 Статистика ссылки:
                🔗 Оригинальный URL: %s
                📎 Короткая ссылка: http://localhost:%d/%s
                👆 Переходы: %d/%d
                🕐 Создана: %s
                ⏰ Истекает: %s
                📊 Статус: %s
                """,
                shortUrl.getOriginalUrl(),
                serverPort,
                shortUrl.getShortCode(),
                shortUrl.getClickCount(),
                shortUrl.getClickLimit(),
                shortUrl.getCreatedAt(),
                shortUrl.getExpiresAt(),
                shortUrl.isValid() ? "✅ Активна" : "❌ Неактивна"
        );
    }

    public boolean deleteShortUrl(String shortCode, UUID userId) {
        ShortUrl shortUrl = shortCodeToUrlMap.get(shortCode);

        if (shortUrl != null && shortUrl.getUserId().equals(userId)) {
            shortCodeToUrlMap.remove(shortCode);
            User user = userService.getUser(userId);
            user.removeShortUrl(shortUrl.getId());
            System.out.println("🗑️ Удалена короткая ссылка: " + shortCode);
            return true;
        }

        return false;
    }

    private String generateUniqueShortCode(String originalUrl, UUID userId) {
        // Генерация уникального кода из конфига
        String baseString = originalUrl + userId.toString() + System.currentTimeMillis();
        int hashCode = Math.abs(baseString.hashCode());
        return Integer.toString(hashCode, 36).substring(0, config.getShortCodeLength()).toUpperCase();
    }

    private void startCleanupTask() {
        // Очистка просроченных ссылок из конфига
        scheduler.scheduleAtFixedRate(() -> {
            cleanUpExpiredUrls();
        }, config.getCleanupInitialDelayMinutes(), config.getCleanupIntervalHours(), TimeUnit.HOURS);
    }

    private void cleanUpExpiredUrls() {
        int removedCount = 0;
        for (var iterator = shortCodeToUrlMap.entrySet().iterator(); iterator.hasNext();) {
            var entry = iterator.next();
            ShortUrl url = entry.getValue();
            if (url.isExpired()) {
                User user = userService.getUser(url.getUserId());
                user.removeShortUrl(url.getId());
                iterator.remove();
                removedCount++;
            }
        }
        if (removedCount > 0) {
            System.out.println("🧹 Удалено просроченных ссылок: " + removedCount);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}