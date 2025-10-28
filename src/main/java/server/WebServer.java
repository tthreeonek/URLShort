package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import service.UrlShortenerService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {
    private final UrlShortenerService urlShortenerService;
    private HttpServer server;
    private final int port;

    public WebServer(UrlShortenerService urlShortenerService, int port) {
        this.urlShortenerService = urlShortenerService;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RedirectHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("✅ Веб-сервер запущен: http://localhost:" + port);
        System.out.println("🔗 Формат коротких ссылок: http://localhost:" + port + "/КОРОТКИЙ_КОД");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class RedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String shortCode = path.substring(1); // Убираем начальный слэш

            String response;
            int statusCode;

            try {
                if (shortCode.isEmpty()) {
                    // Главная страница
                    response = createWelcomePage();
                    statusCode = 200;
                } else {
                    // Редирект по короткой ссылке
                    String originalUrl = urlShortenerService.redirect(shortCode);

                    // 302 Redirect
                    exchange.getResponseHeaders().set("Location", originalUrl);
                    exchange.sendResponseHeaders(302, -1);
                    return;
                }
            } catch (Exception e) {
                response = createErrorPage(e.getMessage());
                statusCode = 404;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }

        private String createWelcomePage() {
            return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>🔗 URL Shortener Service</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .container { 
                            background: white; 
                            padding: 40px; 
                            border-radius: 20px;
                            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                            max-width: 600px;
                            width: 90%;
                        }
                        h1 { 
                            color: #333; 
                            margin-bottom: 20px;
                            text-align: center;
                            font-size: 2.5em;
                        }
                        .subtitle {
                            color: #666;
                            text-align: center;
                            margin-bottom: 30px;
                            font-size: 1.2em;
                        }
                        .url-example { 
                            background: #f8f9fa; 
                            padding: 20px; 
                            border-left: 4px solid #667eea; 
                            margin: 20px 0; 
                            border-radius: 8px;
                            font-family: 'Courier New', monospace;
                        }
                        .features {
                            margin: 30px 0;
                        }
                        .feature {
                            display: flex;
                            align-items: center;
                            margin: 10px 0;
                            color: #555;
                        }
                        .feature-icon {
                            margin-right: 10px;
                            font-size: 1.2em;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🔗 Сокращатель Ссылок</h1>
                        <div class="subtitle">Локальный сервис сокращения URL</div>
                        
                        <div class="features">
                            <div class="feature"><span class="feature-icon">⏰</span> Ссылки действуют 24 часа</div>
                            <div class="feature"><span class="feature-icon">🔢</span> Лимит переходов: 100 по умолчанию</div>
                            <div class="feature"><span class="feature-icon">🆔</span> Автоматическая идентификация пользователя</div>
                        </div>
                        
                        <h3>📝 Пример использования:</h3>
                        <div class="url-example">
                            <strong>Короткая ссылка:</strong> http://localhost:%d/ABC123<br>
                            <strong>Ведет на:</strong> https://www.example.com
                        </div>
                        
                        <p style="text-align: center; color: #777; margin-top: 30px;">
                            Используйте консольное приложение для создания новых коротких ссылок
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(port);
        }

        private String createErrorPage(String error) {
            return """
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>❌ Ошибка - URL Shortener</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
                            min-height: 100vh;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }
                        .error { 
                            background: white; 
                            padding: 40px; 
                            border-radius: 20px;
                            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                            max-width: 500px;
                            width: 90%;
                            text-align: center;
                        }
                        h1 { color: #e74c3c; margin-bottom: 20px; }
                        .back-link {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 10px 20px;
                            background: #3498db;
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                            transition: background 0.3s;
                        }
                        .back-link:hover {
                            background: #2980b9;
                        }
                    </style>
                </head>
                <body>
                    <div class="error">
                        <h1>❌ Ошибка</h1>
                        <p><strong>%s</strong></p>
                        <a href="/" class="back-link">← На главную</a>
                    </div>
                </body>
                </html>
                """.formatted(error);
        }
    }
}