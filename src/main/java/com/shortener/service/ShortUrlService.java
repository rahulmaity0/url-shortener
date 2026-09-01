package com.shortener.service;

import com.shortener.dto.UrlResponse;
import com.shortener.model.ShortUrl;
import com.shortener.repo.ShortUrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * All the thinking in this application happens here.
 */
@Service
public class ShortUrlService {

    /**
     * The characters a code can be made of.
     *
     * Note there is no "l", "1", "O" or "0" problem to worry about here
     * because we include them all - but a real product might drop the
     * confusing ones so codes are easier to read aloud.
     */
    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /** How many characters each code has. 62^6 is about 56 billion codes. */
    private static final int CODE_LENGTH = 6;

    /** How many times we retry if a generated code is already taken. */
    private static final int MAX_ATTEMPTS = 10;

    private final ShortUrlRepository repository;
    private final String baseUrl;
    private final SecureRandom random = new SecureRandom();

    public ShortUrlService(ShortUrlRepository repository,
                           @Value("${app.base-url}") String baseUrl) {
        this.repository = repository;
        this.baseUrl = baseUrl;
    }

    /**
     * Turns a long URL into a short one.
     */
    @Transactional
    public UrlResponse shorten(String originalUrl) {

        // ---- Step 1: pick a code nobody is using yet -------------------
        String code = generateUniqueCode();

        // ---- Step 2: build the row and save it -------------------------
        ShortUrl shortUrl = new ShortUrl(code, originalUrl);

        ShortUrl saved = repository.save(shortUrl);

        // ---- Step 3: turn it into the response shape -------------------
        UrlResponse response = toResponse(saved);

        return response;
    }

    /**
     * Looks up a code and returns where it points.
     *
     * Also bumps the click counter, which is why this method changes data
     * even though it looks like a read.
     */
    @Transactional
    public String resolveAndCount(String code) {

        // ---- Step 1: find the row --------------------------------------
        ShortUrl shortUrl = requireByCode(code);

        // ---- Step 2: add one to the click count ------------------------
        long currentCount = shortUrl.getClickCount();
        long newCount = currentCount + 1;

        shortUrl.setClickCount(newCount);

        // We do not need to call repository.save() here. Inside a
        // @Transactional method, JPA notices the object changed and writes the
        // update automatically when the method finishes.

        // ---- Step 3: hand back the destination -------------------------
        String destination = shortUrl.getOriginalUrl();

        return destination;
    }

    /** Returns the details of one short link, without counting a click. */
    @Transactional(readOnly = true)
    public UrlResponse getStats(String code) {

        ShortUrl shortUrl = requireByCode(code);

        UrlResponse response = toResponse(shortUrl);

        return response;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Finds a row by its code, or fails with a 404.
     */
    private ShortUrl requireByCode(String code) {

        Optional<ShortUrl> maybeUrl = repository.findByCode(code);

        if (maybeUrl.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short URL not found");
        }

        ShortUrl shortUrl = maybeUrl.get();

        return shortUrl;
    }

    /**
     * Makes a random code that is not already in the database.
     *
     * Why a loop? Because random codes can collide. With 56 billion
     * possibilities a clash is very unlikely, but "very unlikely" is not
     * "impossible" - and the code column is unique, so a clash would blow up
     * the insert. Checking first, and retrying, avoids that.
     *
     * If ten attempts in a row all collide, something is badly wrong (most
     * likely the table is nearly full), so we give up rather than loop forever.
     */
    private String generateUniqueCode() {

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {

            String candidate = randomCode();

            boolean alreadyTaken = repository.existsByCode(candidate);

            if (!alreadyTaken) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Could not generate an unused code after " + MAX_ATTEMPTS + " attempts");
    }

    /**
     * Builds one random code, e.g. "a3Xf9Q".
     *
     * StringBuilder is used instead of joining Strings with "+" in a loop.
     * Strings in Java cannot be changed once made, so "+" inside a loop
     * quietly creates a brand new String every time round.
     */
    private String randomCode() {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {

            int position = random.nextInt(CHARACTERS.length());

            char character = CHARACTERS.charAt(position);

            builder.append(character);
        }

        String code = builder.toString();

        return code;
    }

    /** Converts a database row into the shape we send back. */
    private UrlResponse toResponse(ShortUrl shortUrl) {

        String code = shortUrl.getCode();
        String fullShortUrl = baseUrl + "/" + code;
        String originalUrl = shortUrl.getOriginalUrl();
        long clickCount = shortUrl.getClickCount();

        UrlResponse response = new UrlResponse(code, fullShortUrl, originalUrl, clickCount);

        return response;
    }
}
