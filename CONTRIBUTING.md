# Contributing to WireMock

WireMock exists and continues to thrive due to the efforts of over 150 contributors, and we continue to welcome contributions
to it evoltion.


## Ways to contribute
You can help make WireMock a better tool in a number of ways:

* Write or improve [documentation](#writing-documentation).
* Join the [community Slack channel](https://join.slack.com/t/wiremock-community/shared_invite/zt-1mkbo0zlx-gxeZdTJ15Kchdt888Fn_1A), help other WireMock users and share tips. 
* Raise an issue if you discover a bug.
* Contribute bug fixes, new features or enhancements.

## Before opening a PR
When proposing new features or enhancements, we strongly recommend opening an issue first so that the problem being solved
and the implementation design can be discussed. This helps to avoid time being invested in code that is never eventually
merged, and also promotes better designs by involving the community more widely.

For straightforward bug fixes where the issue is clear and can be illustrated via a failing unit or acceptance test, please
just open a PR.


## Code style
WireMock uses the [Google Java style guide](https://google.github.io/styleguide/javaguide.html) and this is enforced in
the build via the Gradle [Spotless plugin](https://github.com/diffplug/spotless).

When running pre-commit checks, if there are any formatting failures the Spotless plugin can fix them for you:

```bash
./gradlew spotlessApply
```

There's also an [IntelliJ plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format) for the same purpose.


## Testing
WireMock has a fairly comprehensive test suite which ensures it remains robust and correct as the codebase envolves.

In particular, there are acceptance tests for almost all features, where a full WireMock server is started up and tested
via its public APIs.

New features should by default come with acceptance tests covering the major positive and negative cases. Unit tests
should also be used judiciously where non-trivial logic would benefit from finer-grained checking. 

When making performance enhancements a representative benchmark test should be developed using an appropriate tool, and
the results before and after applying the change attached to the associated PR.

## Writing documentation
WireMock's documentation is publised on wiremock.org, which is a static website built using Jekyll.

Documentation can be added or modified by cloning 
[https://github.com/wiremock/wiremock.org-sources](https://github.com/wiremock/wiremock.org-sources), working with the
Markdown documents under the `_docs` directory, then raising a PR when ready to have you changes published.
