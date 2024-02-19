# Checklist for releasing WireMock

- [ ] Bump version number
- [ ] Run the release
- [ ] Publish the release note
- [ ] Update the version on wiremock.org
- [ ] Release the Docker image
- [ ] Announce on the WireMock Community Slack
- [ ] Announce on social

## Pre-release - bump version number
Make sure the version number has been updated. Do this either via

```
./gradlew bump-minor-version
```

or

```
./gradlew bump-patch-version
```

Commit and push the changes made by this command.

## Release
Manually trigger the [Release](https://github.com/wiremock/wiremock/actions/workflows/release.yml) workflow from the master branch.

## Publish the release note
Release drafter should have created a draft release note called "next". Check it for sanity and edit it to add any additional information and then set the tag
to the version you've just released and publish it.

## Update the version on wiremock.org
https://github.com/wiremock/wiremock.org

Publish the changes by merging to the `live-publish` branch and manually triggering the "Deploy Jekyll site to Pages" workflow.

## Release the Docker image
Wait for the JAR version you just published to be synced to Maven Central. You can check here:
https://repo1.maven.org/maven2/org/wiremock/wiremock/

Run the Release workflow:
https://github.com/wiremock/wiremock-docker/actions/workflows/release.yml

Then update the README manually on Docker Hub (until we get around to automating it).


## Post an announcement on the WireMock Community Slack
Announce in the #announcments channel then link to the message from #general.

## Shout about it on as many social media platforms as possible
You know the drill.