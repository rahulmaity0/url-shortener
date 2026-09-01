# URL Shortener

A small REST API that turns long URLs into short ones. You POST a URL, get back
a 6-character code, and visiting that code redirects you to the original link.
It also keeps a count of how many times each link has been clicked.

Built with Spring Boot while learning how redirects and JPA actually work under
the hood.

## Tech

- Java 17
- Spring Boot 3.5
- Spring Data JPA / Hibernate
- H2 (file-based, so links survive a restart)
- Maven

## Running it

You need JDK 17 or newer.

```bash
mvn spring-boot:run
```

It starts on port 9090. The database file goes in `data/` and is git-ignored.

## Endpoints

### Shorten a URL

```bash
curl -X POST http://localhost:9090/api/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://github.com/rahulmaity0"}'
```

On Windows PowerShell, use this instead - `curl` is an alias for something
else there, and PowerShell strips the quotes out of the JSON body:

```powershell
Invoke-RestMethod -Uri http://localhost:9090/api/urls -Method Post `
  -ContentType "application/json" `
  -Body '{"url":"https://github.com/rahulmaity0"}'
```

```json
{
  "code": "a3Xf9Q",
  "shortUrl": "http://localhost:9090/a3Xf9Q",
  "originalUrl": "https://github.com/rahulmaity0",
  "clickCount": 0
}
```

### Use the short link

Open `http://localhost:9090/a3Xf9Q` in a browser and you land on the original
URL. The response is a 302 with a `Location` header - the browser follows it
by itself.

Every visit adds one to the click count.

### Check the stats

```bash
curl http://localhost:9090/api/urls/a3Xf9Q
```

Returns the same JSON as above, with the current click count. This does not
count as a click.

## How the code generation works

Codes are 6 characters picked at random from a-z, A-Z and 0-9. That gives
62^6, or roughly 56 billion possible codes.

Random codes can collide, though, and the `code` column is unique - so a clash
would fail the insert. To avoid that, the service checks the database before
using a code and generates a new one if it is already taken. It gives up after
10 attempts rather than looping forever, since that many collisions in a row
would mean something is badly wrong.

## Project structure

```
model/       ShortUrl - one row per link
repo/        two lookup methods: by code, and does-this-code-exist
dto/         the request and response shapes
service/     code generation, click counting, lookups
controller/  the three endpoints
```

## Things it does not do yet

- No authentication, so anyone can create links
- Shortening the same URL twice gives you two different codes
- Links never expire
- No custom aliases
- No rate limiting
- No tests yet

## If I took this further

Swap H2 for PostgreSQL, cache the code-to-URL lookup in Redis since redirects
are read-heavy, and move click counting off the request path so it does not
slow down the redirect.
