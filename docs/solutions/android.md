---
layout: solution
title: Running on Android
meta_title: Running WireMock on the Android platform | WireMock
toc_rank: 115
description: With some effort it is now possible to run WireMock on Android. Please see Sam Edwards’ excellent blog post for instructions.
logo: /images/logos/technology/android.svg
---

## Guide by Sam Edwards

As documented by Sam Edwards in 2016,
with some effort it is now possible to run WireMock 2.x on Android.
Please see
[this blog post](https://handstandsam.com/2016/01/30/running-wiremock-on-android/) for instructions.
This guide is likely no longer applicable to the recent versions.

References:

- [Android Http Mocking Examples](https://github.com/handstandsam/AndroidHttpMockingExamples)
- [Shopping App Demo](https://github.com/handstandsam/ShoppingApp) application with API mocking in test automation
  - Now it is based on Ktor, but there is WireMock Edition in the commit history

<!-- TODO: Talk to Sam and have a fork repo/branch -->

## Presentation by Michael Bailey

Michael Bailey was the first person to publicly demonstrate the possibility of running WireMock on Android. Here's his presentation at Google's GTAC conference on the testing setup used by his team at Amex:
[https://www.youtube.com/watch?v=-xQCNf_5NNM](https://www.youtube.com/watch?v=-xQCNf_5NNM)

## Useful pages

- [WireMock and Kotlin](./kotlin.md) - Android ecosystem embraces Kotlin as a development language,
  and there are some additional tooling available
- [WireMock on Java and JVM](./jvm.md) - Some of JVM generic solutions are applicable to Android development too
