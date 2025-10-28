import config.AppConfig;
import server.WebServer;
import service.UrlShortenerService;
import service.UserService;

import java.awt.*;
import java.net.URI;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static UrlShortenerService urlShortenerService;
    private static UserService userService;
    private static WebServer webServer;
    private static UUID currentUserId;
    private static final int SERVER_PORT = 8080;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            initializeServices();
            showWelcomeMessage();

            boolean running = true;
            while (running) {
                showMenu();
                String choice = scanner.nextLine();

                try {
                    switch (choice) {
                        case "1":
                            createShortUrl();
                            break;
                        case "2":
                            createShortUrlWithCustomLimit();
                            break;
                        case "3":
                            redirectToUrl();
                            break;
                        case "4":
                            showUrlStats();
                            break;
                        case "5":
                            deleteShortUrl();
                            break;
                        case "6":
                            showUserInfo();
                            break;
                        case "0":
                            running = false;
                            break;
                        default:
                            System.out.println("❌ Неверный выбор. Попробуйте снова.");
                    }
                } catch (Exception e) {
                    System.out.println("❌ Ошибка: " + e.getMessage());
                }
            }

            shutdownServices();
            System.out.println("👋 Спасибо за использование URL Shortener!");

        } catch (Exception e) {
            System.err.println("❌ Не удалось запустить приложение: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void initializeServices() throws Exception {
        AppConfig config = new AppConfig();

        userService = new UserService();
        currentUserId = userService.createUser();
        urlShortenerService = new UrlShortenerService(userService, config);
        webServer = new WebServer(urlShortenerService, config.getServerPort());
        webServer.start();
    }

    private static void showWelcomeMessage() {
        System.out.println("\n" +
                "╔══════════════════════════════════════════════════╗\n" +
                "║              🔗 СОКРАЩАТЕЛЬ ССЫЛОК              ║\n" +
                "║               Локальная версия                  ║\n" +
                "╚══════════════════════════════════════════════════╝\n");
        System.out.println("👤 Ваш User ID: " + currentUserId);
        System.out.println("🌐 Сервер запущен: http://localhost:" + SERVER_PORT);
        System.out.println();
    }

    private static void showMenu() {
        System.out.println("\n📋 Выберите действие:");
        System.out.println("1. Создать короткую ссылку (лимит: 100 переходов)");
        System.out.println("2. Создать короткую ссылку с кастомным лимитом");
        System.out.println("3. Перейти по короткой ссылке (откроет браузер)");
        System.out.println("4. Показать статистику ссылки");
        System.out.println("5. Удалить короткую ссылку");
        System.out.println("6. Показать информацию о пользователе");
        System.out.println("0. Выход");
        System.out.print("➡️  Ваш выбор: ");
    }

    private static void createShortUrl() {
        System.out.print("🔗 Введите длинный URL для сокращения: ");
        String longUrl = scanner.nextLine();

        try {
            String shortUrl = urlShortenerService.createShortUrl(longUrl, currentUserId);
            System.out.println("✅ Короткая ссылка создана: " + shortUrl);
            System.out.println("⏰ Ссылка действительна 24 часа");
            System.out.println("🔢 Лимит переходов: 100");
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void createShortUrlWithCustomLimit() {
        System.out.print("🔗 Введите длинный URL для сокращения: ");
        String longUrl = scanner.nextLine();

        System.out.print("🔢 Введите лимит переходов: ");
        try {
            int clickLimit = Integer.parseInt(scanner.nextLine());

            if (clickLimit <= 0) {
                System.out.println("❌ Лимит должен быть положительным числом");
                return;
            }

            String shortUrl = urlShortenerService.createShortUrlWithCustomLimit(longUrl, currentUserId, clickLimit);
            System.out.println("✅ Короткая ссылка создана: " + shortUrl);
            System.out.println("⏰ Ссылка действительна 24 часа");
            System.out.println("🔢 Лимит переходов: " + clickLimit);
        } catch (NumberFormatException e) {
            System.out.println("❌ Неверный формат числа");
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void redirectToUrl() {
        System.out.print("🔗 Введите короткий код (часть после /): ");
        String shortCode = scanner.nextLine();

        try {
            String originalUrl = urlShortenerService.redirect(shortCode);
            System.out.println("🌐 Перенаправление на: " + originalUrl);

            // Автоматическое открытие в браузере
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(originalUrl));
                System.out.println("✅ Браузер открыт");
            } else {
                System.out.println("❌ Не удалось открыть браузер автоматически");
                System.out.println("🔗 Перейдите по ссылке вручную: " + originalUrl);
            }
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void showUrlStats() {
        System.out.print("🔗 Введите короткий код: ");
        String shortCode = scanner.nextLine();

        try {
            String stats = urlShortenerService.getUrlStats(shortCode, currentUserId);
            System.out.println(stats);
        } catch (Exception e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void deleteShortUrl() {
        System.out.print("🔗 Введите короткий код для удаления: ");
        String shortCode = scanner.nextLine();

        boolean deleted = urlShortenerService.deleteShortUrl(shortCode, currentUserId);
        if (deleted) {
            System.out.println("✅ Короткая ссылка удалена");
        } else {
            System.out.println("❌ Не удалось удалить короткую ссылку. Возможно, её не существует или у вас нет прав.");
        }
    }

    private static void showUserInfo() {
        System.out.println("👤 Информация о пользователе:");
        System.out.println("🆔 User ID: " + currentUserId);
        System.out.println("💡 Сохраните этот ID для доступа к вашим ссылкам из разных сессий");
    }

    private static void shutdownServices() {
        if (webServer != null) {
            webServer.stop();
        }
        if (urlShortenerService != null) {
            urlShortenerService.shutdown();
        }
    }
}