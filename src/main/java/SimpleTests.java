import service.UrlShortenerService;
import service.UserService;
import config.AppConfig;
import util.UrlValidator;
import model.ShortUrl;

import java.time.LocalDateTime;
import java.util.UUID;

public class SimpleTests {

    public static void main(String[] args) {
        System.out.println("🚀 ПРОСТЫЕ ТЕСТЫ URL SHORTENER\n");

        testUrlValidator();
        testUserService();
        testShortUrlModel();
        testUrlShortenerService();
        testIntegration();

        System.out.println("\n🎉 ТЕСТИРОВАНИЕ ЗАВЕРШЕНО!");
    }

    public static void testUrlValidator() {
        System.out.println("\n=== ТЕСТЫ URL VALIDATOR ===");

        // Тест 1: Валидный HTTP URL
        if (UrlValidator.isValidUrl("http://example.com")) {
            System.out.println("✅ testValidUrl_Http - PASSED");
        } else {
            System.out.println("❌ testValidUrl_Http - FAILED");
        }

        // Тест 2: Валидный HTTPS URL
        if (UrlValidator.isValidUrl("https://google.com")) {
            System.out.println("✅ testValidUrl_Https - PASSED");
        } else {
            System.out.println("❌ testValidUrl_Https - FAILED");
        }

        // Тест 3: Невалидный URL
        if (!UrlValidator.isValidUrl("not-a-url")) {
            System.out.println("✅ testInvalidUrl - PASSED");
        } else {
            System.out.println("❌ testInvalidUrl - FAILED");
        }
    }

    public static void testUserService() {
        System.out.println("\n=== ТЕСТЫ USER SERVICE ===");

        UserService userService = new UserService();

        // Тест 1: Создание пользователей
        UUID user1 = userService.createUser();
        UUID user2 = userService.createUser();

        if (user1 != null && user2 != null && !user1.equals(user2)) {
            System.out.println("✅ testCreateUser_UniqueIds - PASSED");
        } else {
            System.out.println("❌ testCreateUser_UniqueIds - FAILED");
        }

        // Тест 2: Получение пользователя
        if (userService.getUser(user1) != null) {
            System.out.println("✅ testGetUser_Exists - PASSED");
        } else {
            System.out.println("❌ testGetUser_Exists - FAILED");
        }
    }

    public static void testShortUrlModel() {
        System.out.println("\n=== ТЕСТЫ SHORT URL MODEL ===");

        // Тест 1: Создание ShortUrl
        ShortUrl shortUrl = new ShortUrl(
                "https://example.com",
                "TEST123",
                UUID.randomUUID(),
                5,
                LocalDateTime.now().plusHours(24)
        );

        if (shortUrl.getOriginalUrl().equals("https://example.com")) {
            System.out.println("✅ testShortUrlCreation - PASSED");
        } else {
            System.out.println("❌ testShortUrlCreation - FAILED");
        }

        // Тест 2: Подсчет кликов
        shortUrl.incrementClickCount();
        shortUrl.incrementClickCount();
        if (shortUrl.getClickCount() == 2) {
            System.out.println("✅ testClickCounting - PASSED");
        } else {
            System.out.println("❌ testClickCounting - FAILED");
        }

        // Тест 3: Лимит переходов
        for (int i = 0; i < 5; i++) {
            shortUrl.incrementClickCount();
        }
        if (shortUrl.isClickLimitExceeded()) {
            System.out.println("✅ testClickLimit - PASSED");
        } else {
            System.out.println("❌ testClickLimit - FAILED");
        }
    }

    public static void testUrlShortenerService() {
        System.out.println("\n=== ТЕСТЫ URL SHORTENER SERVICE ===");

        try {
            UserService userService = new UserService();
            UUID userId = userService.createUser();
            AppConfig config = new AppConfig();
            UrlShortenerService service = new UrlShortenerService(userService, config);

            // Тест 1: Создание короткой ссылки
            String shortUrl = service.createShortUrl("https://www.google.com", userId);
            if (shortUrl != null && shortUrl.contains("http://localhost:")) {
                System.out.println("✅ testCreateShortUrl - PASSED");
            } else {
                System.out.println("❌ testCreateShortUrl - FAILED");
            }

            // Тест 2: Невалидный URL
            try {
                service.createShortUrl("invalid-url", userId);
                System.out.println("❌ testInvalidUrl_Exception - FAILED");
            } catch (IllegalArgumentException e) {
                System.out.println("✅ testInvalidUrl_Exception - PASSED");
            }

            // Тест 3: Уникальность для разных пользователей
            UUID user2 = userService.createUser();
            String url1 = service.createShortUrl("https://example.com", userId);
            String url2 = service.createShortUrl("https://example.com", user2);

            if (!url1.equals(url2)) {
                System.out.println("✅ testUniqueUrlsPerUser - PASSED");
            } else {
                System.out.println("❌ testUniqueUrlsPerUser - FAILED");
            }

        } catch (Exception e) {
            System.out.println("❌ Service tests FAILED: " + e.getMessage());
        }
    }

    public static void testIntegration() {
        System.out.println("\n=== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ===");

        try {
            UserService userService = new UserService();
            UUID user1 = userService.createUser();
            UUID user2 = userService.createUser();
            AppConfig config = new AppConfig();
            UrlShortenerService service = new UrlShortenerService(userService, config);

            // Тест 1: Лимит переходов (из ТЗ)
            String shortUrl = service.createShortUrlWithCustomLimit("https://google.com", user1, 2);
            String shortCode = shortUrl.substring(shortUrl.lastIndexOf("/") + 1);

            // Первые два перехода
            service.redirect(shortCode);
            service.redirect(shortCode);

            // Третий должен быть заблокирован
            try {
                service.redirect(shortCode);
                System.out.println("❌ testClickLimitBlocking - FAILED");
            } catch (RuntimeException e) {
                System.out.println("✅ testClickLimitBlocking - PASSED");
            }

            // Тест 2: Контроль доступа по UUID (из ТЗ)
            String user1Url = service.createShortUrl("https://example.com", user1);
            String user1Code = user1Url.substring(user1Url.lastIndexOf("/") + 1);

            // user2 не может получить статистику чужой ссылки
            try {
                service.getUrlStats(user1Code, user2);
                System.out.println("❌ testUserAccessControl - FAILED");
            } catch (RuntimeException e) {
                System.out.println("✅ testUserAccessControl - PASSED");
            }

        } catch (Exception e) {
            System.out.println("❌ Integration tests FAILED: " + e.getMessage());
        }
    }
}