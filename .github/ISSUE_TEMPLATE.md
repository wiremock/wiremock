# Issue guidelines

If you're asking a question, rather than reporting a bug or requesting a feature, **please post your question on [our Slack](https://join.slack.com/t/wiremock-community/shared_invite/zt-1mkbo0zlx-gxeZdTJ15Kchdt888Fn_1A) the [mailing list](https://groups.google.com/forum/#!forum/wiremock-user), and do not open an issue**.

Please do not log bugs regarding classpath issues (typically manifesting as 'NoClassDefFoundException' or 'ClassNotFoundException').
These are not WireMock bugs, and need to be diagnosed for your project using your build tool. Plenty has already been written in WireMock's issues and mailing list about how to resolve these issues
so please search these sources before asking for help.

## Bug reports

We're a lot more likely to look at and fix bugs that are clearly described and reproduceable. Please including the following details when reporting a bug:

- [ ] Which version of WireMock you're using
- [ ] How you're starting and configuring WireMock, including configuration or CLI the command line
- [ ] A failing test case that demonstrates the problem
- [ ] Profiler data if the issue is performance related   

## Feature requests

Please include details of the use case and motivation for a feature when suggesting it.
