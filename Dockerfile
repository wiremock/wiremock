FROM openjdk:8

USER root

ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 4.8.1

ARG GRADLE_DOWNLOAD_SHA256=af334d994b5e69e439ab55b5d2b7d086da5ea6763d78054f49f147b06370ed71
RUN set -o errexit -o nounset \
	&& echo "Downloading Gradle" \
	&& wget --no-verbose --output-document=gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
	\
	&& echo "Checking download hash" \
	&& echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum --check - \
	\
	&& echo "Installing Gradle" \
	&& unzip gradle.zip \
	&& rm gradle.zip \
	&& mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
	&& ln --symbolic "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
	\
#	&& echo "Adding gradle user and group" \
#	&& groupadd --system --gid 1000 gradle \
#	&& useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle \
	&& mkdir .gradle \
#	&& chown --recursive gradle:gradle .gradle \
	&& echo "Symlinking root Gradle cache to gradle Gradle cache" \
	&& ln -s .gradle /root/.gradle

RUN set -o errexit -o nounset \
    && echo "node: update" \
    && apt-get update -y \
    && echo "node: get install file"\
    && apt-get install curl \
    && curl -sL https://deb.nodesource.com/setup_8.x | bash \
    && echo "node: install nodejs"\
    && apt-get install nodejs \
    && echo "node: install build-essentials"\
    && apt-get install build-essential -y\
    && echo "node: update npm"\
    && npm install -g npm@latest

# Create Gradle volume
#USER gradle
VOLUME ".gradle"


RUN set -o errexit -o nounset \
	&& echo "build: Build wiremock with ui" \
	&& gradle -g gradle
	&& gradle --stop  --debug\
	&& gradle clean jar shadowJar --debug \
	&& echo "build: copy file to docker dir"\
	&& cp wiremock/build/libs/wiremock-standalone-*.jar wiremock/build/libs/wiremock.jar
#	&& cd build/libs/ \
#    && ls \
#    && pwd \
#    && mkdir /wiremock \
#    && cp /home/wiremock/build/libs/wiremock-standalone-*.jar /wiremock/wiremock.jar


# Copy the current directory contents into the container at /wiremock
ADD wiremock/build/libs/wiremock.jar /wiremock

# Make port 443 available to the world outside this container
EXPOSE 443
EXPOSE 80

# Deploy the app
CMD java -jar wiremock.jar
