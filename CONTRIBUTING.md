# Contributing to WireMock

[![Docs](https://img.shields.io/static/v1?label=Documentation&message=public&color=green)](https://wiremock.org/docs/)
[![a](https://img.shields.io/badge/slack-%23wiremock%5Fjava-brightgreen?style=flat&logo=slack)](https://slack.wiremock.org/)
[![a](https://img.shields.io/badge/Public-Roadmap-brightgreen?style=flat)](https://github.com/orgs/wiremock/projects/4)
[![Participate](https://img.shields.io/static/v1?label=Contributing&message=guide&color=blue)](https://github.com/wiremock/wiremock/blob/master/CONTRIBUTING.md)

WireMock exists and continues to thrive due to the efforts of over 150 contributors,
and we continue to welcome contributions to its evolution.
Regardless of your expertise and time you could dedicate,
there're opportunities to participate and help the project!

## Ways to contribute

This guide is for contributing to WireMock, also known as _WireMock Java_ or _WireMock Core_.
There are many other repositories waiting for contributors,
check out the [Contributor Guide](https://github.com/wiremock/wiremock/blob/master/CONTRIBUTING.md)
for the references and details.

## Getting started

If you want to contribute to WireMock Java codebase, do the following:

* Join the [community Slack channel](http://slack.wiremock.org/),
  especially the `#help-contributing` and `#wiremock-java` channels.
  The latter is used to coordinate development of this repository.
* Read the guidelines below
* Start contributing by creating issues, submitting patches via pull requests, and helping others!

## Building WireMock locally

To run all of WireMock's tests:

```bash
./gradlew clean test
```

To build both JARs (thin and standalone), the JARs will be placed under ``build/libs``.:

```bash
./gradlew jar shadowJar 
```

To publish both JARs to your local Maven repository:

```bash
./gradlew publishToMavenLocal
```

## Contributing Code

Please be mindful of the
following guidelines:

* All changes should include suitable tests, whether to demonstrate the bug or exercise and document the new feature.
* Please make one change per pull request.
* If the new feature is significantly large/complex/breaks existing behaviour, please first post a summary of your idea
on the GitHub Issue to generate a discussion. This will avoid significant amounts of coding time spent on changes that ultimately get rejected.
* Try to avoid reformats of files that change the indentation, tabs to spaces etc., as this makes reviewing diffs much
more difficult.
* Abide by [the Architecture Rules](https://github.com/wiremock/wiremock/tree/master/src/test/java/com/github/tomakehurst/wiremock/archunit) enforced by ArchUnit.

### Before opening a PR

When proposing new features or enhancements, we strongly recommend opening an issue first so that the problem being solved
and the implementation design can be discussed. This helps to avoid time being invested in code that is never eventually
merged, and also promotes better designs by involving the community more widely.

For straightforward bug fixes where the issue is clear and can be illustrated via a failing unit or acceptance test, please
just open a PR.

### Code style

WireMock uses the [Google Java style guide](https://google.github.io/styleguide/javaguide.html) and this is enforced in
the build via the Gradle [Spotless plugin](https://github.com/diffplug/spotless).

When running pre-commit checks, if there are any formatting failures the Spotless plugin can fix them for you:

```bash
./gradlew spotlessApply
```

There's also an [IntelliJ plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format) for the same purpose.

## Testing

WireMock has a fairly comprehensive test suite which ensures it remains robust and correct as the codebase evolves.

In particular, there are acceptance tests for almost all features, where a full WireMock server is started up and tested
via its public APIs.

New features should by default come with acceptance tests covering the major positive and negative cases. Unit tests
should also be used judiciously where non-trivial logic would benefit from finer-grained checking.

When making performance enhancements a representative benchmark test should be developed using an appropriate tool, and
the results before and after applying the change attached to the associated PR.

## Writing documentation

It is expected that all new features and enhancements are documented properly,
in most cases before the patches are merged.

Most of WireMock's documentation is published on `wiremock.org`,
which is a static website built using Jekyll.
The website sources are located here: [wiremock/wiremock.org](https://github.com/wiremock/wiremock.org).
All the documentation is located under the `_docs` directory as Markdown files,
and it can be edited with all modern text editors and IDEs.
See the repository's contributor guide for more information.
