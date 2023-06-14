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

## Merge process

Merges to this repository can be performed by the WireMock maintainer ([Tom Akehurst](https://github.com/tomakehurst))
and by _co-maintainers_ assigned by him.
This is a [community role](https://github.com/wiremock/community/blob/main/governance/README.md)
designed for WireMock itself and other key repositories,
specifically to facilitate review and changes while WireMock 3 is in beta
and receives a lot of incremental patches.

- The maintainers are responsible to verify the pull request readiness
  in accordance with contributing guidelines (e.g. code quality, test automation, documentation, etc.).
  The pull request can only be approved if these requirements are met
- In the beginning, a review by one co-maintainer is required for the merge,
  unless there are negative reviews and unaddressed comments by other contributors
- After the approval, it is generally recommended to give at least 24 hours for reviews before merging

Beyond WireMock 3.x Beta releases, the scope of responsibilities for co-maintainers
is yet to be determined based on the experiences with this role for the Beta versions.

### What can be merged by co-maintainers

While WireMock 3.x is in Beta, co-maintainers can merge the following pull requests:

- Minor features and improvements that do not impact the WireMock architecture
- Refactorings, including the major ones, e.g. Guava replacement
- Test Automation
- Non-production repository changes: documentation (including Javadoc), GitHub Actions, bots and automation
- Dependency updates for shaded dependencies, patch/minor versions for projects following the Semantic Versioning notation

### What CANNOT be merged by co-maintainers without BDFL’s approval

The following changes need a review by Tom Akehurst before being merged.

- Any compatibility breaking changes, including binary API and REST API,
  unless pre-approved by the BDFL in the associated GitHub issue
- New request matchers (patterns)
- Substantial changes to WireMock Architecture and API.
  Examples: New REST API end-points, major features like GraphQL fetching
- Inclusion of new libraries, even if shaded
- Major version Dependency updates, e.g. Jetty 11 => 12
- Changes in the deliverable artefacts, e.g. new modules