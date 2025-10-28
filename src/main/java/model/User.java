package model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private UUID id;
    private List<ShortUrl> shortUrls;

    public User() {
        this.id = UUID.randomUUID();
        this.shortUrls = new ArrayList<>();
    }

    public UUID getId() { return id; }
    public List<ShortUrl> getShortUrls() { return shortUrls; }

    public void addShortUrl(ShortUrl shortUrl) {
        shortUrls.add(shortUrl);
    }

    public boolean removeShortUrl(UUID shortUrlId) {
        return shortUrls.removeIf(url -> url.getId().equals(shortUrlId));
    }

    public ShortUrl findShortUrlById(UUID shortUrlId) {
        return shortUrls.stream()
                .filter(url -> url.getId().equals(shortUrlId))
                .findFirst()
                .orElse(null);
    }
}