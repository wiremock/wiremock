---
layout: docs
title: Record and Playback
toc_rank: 70
redirect_from: "/record-playback.html"
description: Recording HTTP exchanges with other APIs and playing them back as stubs.
---

**WireMock has the ability to create stub mappings by recording them
while you send requests. This can be used to quickly capture a
collection of responses from a real service then use them offline in
your tests.**

## Recording

Recording is done by starting the standalone runner like this:

Once it's started you send requests to it as if it was the remote
service:

Now if you look under `mappings` and `__files` (under the current
directory you're running WireMock in) you'll see that a new file has
been one created under each, the former for the stub mapping and the
latter for the body content.

### Capturing request headers


Optionally, you can record request headers so that your stub mappings
will match on those in addition to URL, method and body (if a POST or
PUT). This is done by adding an extra parameter to the command line e.g.
`--match-headers="Accept,Content-Type"`

> **note**
>
> The recorder will ignore any request matching one it has already
> captured.

## Playback


If you start the standalone runner again without the extra commandline
options it will automatically load the newly created stub mappings and
start serving them.

Alternatively you can copy the files created under
`src/test/resources/mappings` and `src/test/resources/__files` in your
Java project, or the equivalents under `WEB-INF/wiremock` in your WAR
project.
