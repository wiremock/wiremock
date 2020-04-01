#!/bin/bash

set -eo pipefail

export JAVA7_HOME=~/.sdkman/candidates/java/7.0.181-zulu
export JAVA8_HOME=~/.sdkman/candidates/java/8.0.181-zulu
export JAVA11_HOME=~/.sdkman/candidates/java/11.0.1-open

help() {
    echo -e "Usage: go <command>"
    echo -e
    echo -e "    help           Print this help"
    echo -e
    echo -e "Common commands: "
    echo -e "    test           Run all tests against Java 7 and 8"
    echo -e "    test-java11    Run all tests against Java 11"
    echo -e "    release        Release to Maven Central (via Sonatype)"
    echo -e "    release-local  Release to ~/.m2/repository"
    echo ""
    exit 0
}

use-java7() {
    export JAVA_HOME=$JAVA7_HOME
    ./gradlew --version | grep JVM
}

use-java8() {
    export JAVA_HOME=$JAVA8_HOME
    ./gradlew --version | grep JVM
}

use-java11() {
    export JAVA_HOME=$JAVA11_HOME
    ./gradlew --version | grep JVM
}

test() {
    use-java7
    ./gradlew -c release-settings.gradle :java7:test --rerun-tasks  -x generateApiDocs

    use-java8
    ./gradlew -c release-settings.gradle :java8:test --rerun-tasks  -x generateApiDocs
}

test-java11() {
    use-java11
    ./gradlew -c release-settings.gradle :java8:test --rerun-tasks  -x generateApiDocs
}

release() {
    use-java7
    ./gradlew -c release-settings.gradle clean :java7:release --stacktrace

    use-java8
    ./gradlew -c release-settings.gradle clean :java8:release --stacktrace
}

release-local() {
    use-java7
    ./gradlew -c release-settings.gradle clean :java7:release -DLOCAL_PUBLISH=true

    use-java8
    ./gradlew -c release-settings.gradle clean :java8:release -DLOCAL_PUBLISH=true
}


if [[ $1 =~ ^(help|test|test-java11|release|release-local|use-java7|use-java8)$ ]]; then
  COMMAND=$1
  shift
  $COMMAND "$@"
else
  help
  exit 1
fi