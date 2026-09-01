package com.shortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * One shortened link.
 *
 * Example row:
 *   id          = 1
 *   code        = "a3Xf9"
 *   originalUrl = "https://github.com/rahulmaity0/some/very/long/path"
 *   createdAt   = 2026-09-01T10:14:37Z
 *   clickCount  = 12
 */
@Entity
@Table(name = "short_urls")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The short code, e.g. "a3Xf9".
     *
     * unique = true is the important one here. Two rows with the same code
     * would mean a link points at two different places, and the lookup could
     * return the wrong one. The database rule guarantees that cannot happen.
     */
    @Column(nullable = false, unique = true, length = 10)
    private String code;

    /** The long URL we redirect to. Generous length, because URLs can be long. */
    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    /** How many times this link has been visited. */
    @Column(nullable = false)
    private long clickCount = 0;

    /** JPA needs a no-argument constructor. */
    protected ShortUrl() {
    }

    public ShortUrl(String code, String originalUrl) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.createdAt = Instant.now();
        this.clickCount = 0;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getClickCount() {
        return clickCount;
    }

    public void setClickCount(long clickCount) {
        this.clickCount = clickCount;
    }
}
