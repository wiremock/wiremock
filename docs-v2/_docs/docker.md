---
layout: docs
title: Running in Docker
toc_rank: 45
description: Configuring and running WireMock in Docker
---

<div class="mocklab-callout"> 
  <p class="mocklab-callout__text">
    Configuring servers can be a major distraction from building great software. <strong>MockLab</strong> provides a hosted, 100% WireMock compatible mocking service, freeing you from the hassles of SSL, DNS and server configuration.    
  </p>
  <a href="http://get.mocklab.io/?utm_source=wiremock.org&utm_medium=docs-callout&utm_campaign=running-in-docker" title="Learn more" class="mocklab-callout__learn-more-button">Learn more</a>
</div>

From version 2.31.0 WireMock has an [official Docker image](https://hub.docker.com/r/wiremock/wiremock).

## Getting started

### Start a single WireWock container with default configuration

```sh
docker run -it --rm
  -p 8080:8080 \
  --name wiremock \
  wiremock/wiremock:{{ site.wiremock_version }}
```

> Access [http://localhost:8080/__admin/mappings](http://localhost:8080/__admin/mappings) to display the mappings (empty set)

### Start with command line arguments

The Docker image supports exactly the same set of command line arguments as the [standalone version](/docs/running-standalone/#command-line-options).
These can be passed to the container by appending them to the end of the command e.g.:

```sh
docker run -it --rm
  -p 8443:8443 \
  --name wiremock \
  wiremock/wiremock:{{ site.wiremock_version }} \
  --https-port 8443 --verbose
```


### Mounting stub mapping files

Inside the container, the WireMock uses `/home/wiremock` as the root from which it reads the `mappings` and `__files` directories.
This means you can mount a directory containing these from your host machine into Docker and WireMock will load the stub mappings.

To mount the current directory use `-v $PWD:/home/wiremock` e.g.:

```sh
docker run -it --rm 
  -p 8080:8080 \
  --name wiremock \
  -v $PWD:/home/wiremock \
  wiremock/wiremock:{{ site.wiremock_version }}
```

### Running with extensions

[WireMock extensions](/docs/extending-wiremock/) are packaged as JAR files. In order to use them they need to be made
available at runtime and WireMock must be configured to enable them.

For example, to use the [Webhooks extension](/docs/webhooks-and-callbacks/) we would first download [wiremock-webhooks-extension-{{ site.wiremock_version }}.jar](https://repo1.maven.org/maven2/org/wiremock/wiremock-webhooks-extension/{{ site.wiremock_version }}/wiremock-webhooks-extension-{{ site.wiremock_version }}.jar)
into the `extensions` directory under our working directory.

Then when starting Docker we would mount the extensions directory to `/var/wiremock/extensions` and enable the webhooks extension
via a CLI parameter:

```sh
docker run -it --rm
  -p 8080:8080 \
  --name wiremock \
  -v $PWD/extensions:/var/wiremock/extensions \
  wiremock/wiremock \
    --extensions org.wiremock.webhooks.Webhooks
```