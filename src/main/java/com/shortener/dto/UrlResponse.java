package com.shortener.dto;

/**
 * What we send back to the client:
 *
 *   {
 *     "code": "a3Xf9Q",
 *     "shortUrl": "http://localhost:9090/a3Xf9Q",
 *     "originalUrl": "https://example.com/some/long/path",
 *     "clickCount": 0
 *   }
 *
 * We return this instead of the ShortUrl entity itself. Returning entities
 * straight from a controller is how internal fields end up leaking into JSON
 * responses by accident.
 */
public class UrlResponse {

    private String code;
    private String shortUrl;
    private String originalUrl;
    private long clickCount;

    public UrlResponse(String code, String shortUrl, String originalUrl, long clickCount) {
        this.code = code;
        this.shortUrl = shortUrl;
        this.originalUrl = originalUrl;
        this.clickCount = clickCount;
    }

    public String getCode() {
        return code;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public long getClickCount() {
        return clickCount;
    }
}
