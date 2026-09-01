package com.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The JSON body of POST /api/urls:
 *
 *   { "url": "https://example.com/some/long/path" }
 *
 * A "record" is a short way of writing a class that only holds values - Java
 * writes the constructor and the getters for you. You read the value with
 * request.url(), not request.getUrl().
 *
 * The annotations are checked by Spring BEFORE the controller method runs, so
 * bad input never reaches our own code.
 */
public record CreateUrlRequest(

        @NotBlank(message = "URL is required")
        @Size(max = 2048, message = "URL is too long")
        @Pattern(regexp = "^https?://.+",
                message = "URL must start with http:// or https://")
        String url
) {
}
