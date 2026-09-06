package com.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * The JSON body of POST /api/urls:
 *
 *   { "url": "https://example.com/some/long/path" }
 *
 * Jackson needs the empty constructor and the setter so it can create this
 * object and fill it from the request body.
 *
 * The annotations are checked by Spring BEFORE the controller method runs, so
 * bad input never reaches our own code.
 */
public class CreateUrlRequest {

    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL is too long")
    @Pattern(regexp = "^https?://.+",
            message = "URL must start with http:// or https://")
    private String url;

    public CreateUrlRequest() {
    }

    public CreateUrlRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
