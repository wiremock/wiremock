---
layout: splash
title: External Resources
description: Extensions, integrations, blog posts and videos about WireMock.
---

# External Resources

Code, articles and videos related to WireMock from around the web.

## Integrations

Spring Contract Verifier (previously called Accurest) is a consumer driven contracts tool that generates WireMock stub mappings as
examples for client testing.
[http://codearte.github.io/accurest/](http://codearte.github.io/accurest/)

A Spring REST Docs integration for WireMock that generates WireMock stub mappings from your test cases:<br>
[https://github.com/epages-de/restdocs-wiremock](https://github.com/epages-de/restdocs-wiremock)


## Extensions

Some folks at Open Table have written a response transformer for injecting data from the
request body into the response:<br>
[https://github.com/opentable/wiremock-body-transformer](https://github.com/opentable/wiremock-body-transformer)

In a similar vein, Adam York has written a response transformer utilising Velocity templates:<br>
[https://github.com/adamyork/wiremock-velocity-transformer](https://github.com/adamyork/wiremock-velocity-transformer)


## Other languages

Rowan Hill has written a PHP binding:<br>
[https://github.com/rowanhill/wiremock-php](https://github.com/rowanhill/wiremock-php)

Jeffres S. Morgan has written a Ruby wrapper:<br>
[https://rubygems.org/gems/service_mock](https://rubygems.org/gems/service_mock)

Tom Jankes has written a Groovy binding:<br>
[https://github.com/tomjankes/wiremock-groovy](https://github.com/tomjankes/wiremock-groovy)


## Articles

Sam Edwards has been hugely helpful in getting WireMock onto the Android platform and helping others do so. Here is his blog post explaining
how to write an Espresso test using WireMock as your app's back-end:<br>
[http://handstandsam.com/2016/01/30/running-wiremock-on-android/](http://handstandsam.com/2016/01/30/running-wiremock-on-android/)

Dusan Dević at Yenlo wrote a useful guide to testing error conditions in the WSO2 ESB using Wiremock:<br>
[https://www.yenlo.com/blog/wso2torial-error-handling-in-wso2-esb-with-wiremock](https://www.yenlo.com/blog/wso2torial-error-handling-in-wso2-esb-with-wiremock)

Phill Barber has written a couple of interesting posts about practical testing scenarios with WireMock:<br>
[http://phillbarber.blogspot.co.uk/2015/05/how-to-write-end-to-end-tests-for-nginx.html](http://phillbarber.blogspot.co.uk/2015/05/how-to-write-end-to-end-tests-for-nginx.html)<br>
[http://phillbarber.blogspot.co.uk/2015/02/how-to-test-for-connection-leaks.html](http://phillbarber.blogspot.co.uk/2015/02/how-to-test-for-connection-leaks.html)

## Videos

Chris Batey did an excellent talk at Skillsmatter in London about building fault tolerant microservices. He showed some practical
failure testing strategies using WireMock and Saboteur he'd used for real while working at Sky:<br>
[https://skillsmatter.com/skillscasts/5810-building-fault-tolerant-microservices](https://skillsmatter.com/skillscasts/5810-building-fault-tolerant-microservices)

Daniel Bryant's excellent QCon presentation "The Seven Deadly Sins of Microservices" covers the full gamut of microservice anti-patterns seen in the wild, with some sound advice on how to fix them. WireMock and Saboteur get an honourable mention in the testing discussion:<br>
[https://www.infoq.com/presentations/7-sins-microservices](https://www.infoq.com/presentations/7-sins-microservices)

The folks at Intuit have built a very impressive and ambitious testing setup, using WireMock to isolate individual services. Here's a talk they did at AWS:Reinvent:<br>
[https://www.youtube.com/watch?list=PLhr1KZpdzuke5pqzTvI2ZxwP8-NwLACuU&v=sUsh3EnzKKk](https://www.youtube.com/watch?list=PLhr1KZpdzuke5pqzTvI2ZxwP8-NwLACuU&v=sUsh3EnzKKk)

Michael Bailey was the first person to publicly demonstrate the possibility of running WireMock on Android. Here's his presentation at Google's GTAC conference on
the testing setup used by his team at Amex:<br>
[https://www.youtube.com/watch?v=-xQCNf_5NNM](https://www.youtube.com/watch?v=-xQCNf_5NNM)

Tom and Rob Elliot gave a join talk at Skillsmatter about patterns for readable and scalable tests with WireMock, and an approach for unit testing a
CDN:<br>
[https://skillsmatter.com/skillscasts/6853-scalable-management-of-test-data-making-tests-readable](https://skillsmatter.com/skillscasts/6853-scalable-management-of-test-data-making-tests-readable)
