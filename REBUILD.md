# Rebuild this yourself

The implementation is gone. The tests are not.

`src/main/java` has only `UrlShortenerApplication.java` left. Everything else —
the model, the repository, the DTOs, the service, the controller — you write.

## The rule

**Do not paste.** Not from the old code, not from anywhere. When you are stuck,
open the original on `main`, read it until you understand it, close the file,
then type it from memory. Retyping while looking is copying with extra steps and
teaches you nothing.

## How you know you are done

```bash
mvn test
```

Five tests, all green:

1. Shortening a valid URL returns 201 and a code
2. A URL without `http://` is rejected with 400
3. Visiting a short link returns 302 with the right `Location` header
4. Visiting a link twice moves the click count to 2
5. An unknown code returns 404 on both endpoints

The tests are the specification. Read `src/test/java/com/shortener/UrlShortenerTest.java`
first — it tells you every class and method you need to create.

## Suggested order

1. `model/ShortUrl` — the entity. One row per link.
2. `repo/ShortUrlRepository` — extend `JpaRepository`, add `findByCode`.
3. `dto/CreateUrlRequest` and `dto/UrlResponse` — in and out.
4. `service/ShortUrlService` — code generation, collision retry, click counting.
5. `controller/ShortUrlController` — three endpoints.

Expect the first pass to take a few days and to feel bad. That is the work
happening, not a sign you cannot do it.

## Then break it

Once green, prove you understand it by making it fail on purpose:

- Take the validation off and POST `"banana"`. Watch it save.
- Hardcode the generator to always return `"aaaaaa"`, create two links, watch
  the unique constraint blow up.
- Change the redirect from 302 to 301, click the link a few times, and watch
  the click count stop rising because the browser cached it.
- Remove `@Transactional` from the click increment and see whether the count
  still persists.

Put each one back after you have seen it break.

## Then extend it

Add something that was never there — custom aliases, or link expiry. If you can
build a feature with no reference, you understand this. If you can only
reproduce what was here, you memorised it.

## Passes

- Pass 1: a few days. Painful.
- Pass 2 (delete it all, do it again): an afternoon.
- Pass 3: under an hour, cold, no lookups. **That is when you own it.**
