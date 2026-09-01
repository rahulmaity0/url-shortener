package com.shortener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the URL shortener.
 *
 * @SpringBootTest          starts the whole application
 * @AutoConfigureMockMvc    gives us MockMvc, which sends fake HTTP requests
 *                          without needing a real server or a real port
 * @ActiveProfiles("test")  use application-test.properties, so we get the
 *                          in-memory database instead of the real one
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UrlShortenerTest {

    private static final String LONG_URL = "https://github.com/rahulmaity0";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    /**
     * Every MockMvc test has the same three parts:
     *   1. perform(...)     send a request
     *   2. andExpect(...)   say what the response should look like
     *   3. if an expectation does not hold, the test fails
     */
    @Test
    @DisplayName("shortens a valid URL and returns a code")
    void shortensAValidUrl() throws Exception {

        String body = "{\"url\": \"" + LONG_URL + "\"}";

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                // 201, because we created something new
                .andExpect(status().isCreated())

                // the response should have a "code" field with something in it
                .andExpect(jsonPath("$.code").isNotEmpty())

                // and it should echo back the URL we sent
                .andExpect(jsonPath("$.originalUrl").value(LONG_URL));
    }

    @Test
    @DisplayName("rejects a URL that does not start with http")
    void rejectsUrlWithoutProtocol() throws Exception {

        // "github.com" is missing the https:// part. The @Pattern rule on
        // CreateUrlRequest should catch this before the controller runs.
        String body = "{\"url\": \"github.com/rahulmaity0\"}";

        mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))

                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("redirects to the original URL with a 302 and a Location header")
    void redirectsToOriginalUrl() throws Exception {

        String code = shortenAndGetCode(LONG_URL);

        mockMvc.perform(get("/" + code))

                // 302 FOUND - "go somewhere else"
                .andExpect(status().isFound())

                // and this header is where the browser is told to go
                .andExpect(header().string("Location", LONG_URL));
    }

    @Test
    @DisplayName("counts a click each time the short link is visited")
    void countsClicks() throws Exception {

        String code = shortenAndGetCode(LONG_URL);

        // a brand new link has never been visited
        mockMvc.perform(get("/api/urls/" + code))
                .andExpect(jsonPath("$.clickCount").value(0));

        // visit it twice
        mockMvc.perform(get("/" + code));
        mockMvc.perform(get("/" + code));

        // the counter should have moved, which also proves JPA saved it
        // without us ever calling repository.save()
        mockMvc.perform(get("/api/urls/" + code))
                .andExpect(jsonPath("$.clickCount").value(2));
    }

    @Test
    @DisplayName("returns 404 for a code that does not exist")
    void unknownCodeIsNotFound() throws Exception {

        mockMvc.perform(get("/api/urls/zzzzzz"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/zzzzzz"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    /**
     * Shortens a URL and digs the generated code out of the response, so the
     * tests above can use it. We cannot hard-code a code, because it is
     * random every time.
     */
    private String shortenAndGetCode(String url) throws Exception {

        String body = "{\"url\": \"" + url + "\"}";

        MvcResult result = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String json = result.getResponse().getContentAsString();

        JsonNode tree = objectMapper.readTree(json);

        String code = tree.get("code").asText();

        return code;
    }
}
