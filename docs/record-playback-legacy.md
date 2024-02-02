---
description: >
    Quickly capture stub mappings and response body content using WireMock's record and playback.
---

# Record and Playback (Legacy)

You can quickly capture a collection of stub mappings and response body content using WireMock's record and playback.

## Recording

To start recording, start the standalone runner as in the following:

```bash
$ java -jar wiremock-standalone-{{ versions.wiremock_version }}.jar --proxy-all="http://search.twitter.com" --record-mappings --verbose
```

Once it's started, send requests to it as if it was the remote
service:

```bash
$ curl "http://localhost:8080/search.json?q=from:sirbonar&result_type=recent&rpp=1"
```

Now if you look under `mappings` and `__files` (under the current
directory you're running WireMock in) you'll see that a new file has
been one created under each, the former for the stub mapping and the
latter for the body content.

### Capturing request headers

Optionally, you can record request headers so that your stub mappings
match on those in addition to URL, method and body (if a POST or
PUT). This is done by adding an extra parameter to the command line, like 
`--match-headers="Accept,Content-Type"`

!!! note

    The recorder ignores any request matching one it has already captured.

## Playback

If you start the standalone runner again without the extra command line
options it will automatically load the newly created stub mappings and
start serving them.

Alternatively you can copy the files created under
`src/test/resources/mappings` and `src/test/resources/__files` in your
Java project, or the equivalents under `WEB-INF/wiremock` in your WAR
project.
