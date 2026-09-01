package com.shortener.dto;

/**
 * What we send back to the client:
 *
 *   {
 *     "code": "a3Xf9",
 *     "shortUrl": "http://localhost:9090/a3Xf9",
 *     "originalUrl": "https://example.com/some/long/path",
 *     "clickCount": 0
 *   }
 *
 * We return this instead of the ShortUrl entity itself. Returning entities
 * straight from a controller is how internal fields end up leaking into JSON
 * responses by accident.
 */
public record UrlResponse(
        String code,
        String shortUrl,
        String originalUrl,
        long clickCount
) {
}
