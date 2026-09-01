package com.shortener.controller;

import com.shortener.dto.CreateUrlRequest;
import com.shortener.dto.UrlResponse;
import com.shortener.service.ShortUrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * The three endpoints.
 *
 * Controllers stay thin: take the request, call the service, return the
 * answer. No logic lives here.
 */
@RestController
public class ShortUrlController {

    private final ShortUrlService service;

    public ShortUrlController(ShortUrlService service) {
        this.service = service;
    }

    /**
     * POST /api/urls - shorten a URL.
     *
     * @Valid is what makes the rules on CreateUrlRequest actually run.
     * Without it they are silently ignored.
     */
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> shorten(@Valid @RequestBody CreateUrlRequest request) {

        String originalUrl = request.url();

        UrlResponse response = service.shorten(originalUrl);

        // 201 CREATED is the right status when something new was made.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /a3Xf9 - the actual redirect.
     *
     * We reply with status 302 and a "Location" header. The browser sees that
     * and goes to the address in the header by itself. Nothing is rendered.
     */
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {

        String destination = service.resolveAndCount(code);

        URI target = URI.create(destination);

        // Built one step at a time. Each call hands back the builder, ready
        // for the next instruction.
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.FOUND);

        builder = builder.location(target);

        ResponseEntity<Void> response = builder.build();

        return response;
    }

    /** GET /api/urls/a3Xf9 - stats, without counting as a click. */
    @GetMapping("/api/urls/{code}")
    public ResponseEntity<UrlResponse> stats(@PathVariable String code) {

        UrlResponse response = service.getStats(code);

        return ResponseEntity.ok(response);
    }
}
