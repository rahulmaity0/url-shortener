package com.shortener.repo;

import com.shortener.model.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring writes the implementation of this at startup. We only declare the
 * method names, and Spring works out the SQL from them.
 */
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    /**
     * Used by the redirect. Somebody visits /a3Xf9, and we need the row for
     * the code "a3Xf9".
     *
     * SQL: SELECT * FROM short_urls WHERE code = ?
     */
    Optional<ShortUrl> findByCode(String code);

    /**
     * Used when generating a new code, to make sure we do not hand out one
     * that is already taken.
     *
     * SQL: SELECT COUNT(*) > 0 FROM short_urls WHERE code = ?
     */
    boolean existsByCode(String code);
}
