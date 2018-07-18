FROM openjdk:8


USER root

RUN set -o errexit -o nounset \
    && mkdir /home/wiremock \
    && cd /home/wiremock \
    && echo "git: Clone repo"\
    && git clone https://github.com/holomekc/wiremock.git .\
    && echo "git: Switch branch"\
    && git checkout new-gui


CMD ["gradle"]

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
	&& echo "Adding gradle user and group" \
	&& groupadd --system --gid 1000 gradle \
	&& useradd --system --gid gradle --uid 1000 --shell /bin/bash --create-home gradle \
	&& mkdir /home/wiremock/.gradle \
	&& chown --recursive gradle:gradle /home/wiremock \
	\
	&& echo "Symlinking root Gradle cache to gradle Gradle cache" \
	&& ln -s /home/wiremock/.gradle /root/.gradle

ENV DEBIAN_FRONTEND noninteractive

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
#VOLUME "/home/wiremock/.gradle"


WORKDIR /home/wiremock

RUN set -o errexit -o nounset \
	&& echo "build: Build wiremock with ui" \
	&& cd /home/wiremock \
	&& gradle --stop \
	&& gradle clean jar shadowJar \
	&& echo "build: copy file to root dir"\
	&& cd build/libs/ \
    && ls \
    && cp /home/wiremock/build/libs/wiremock-standalone-2.18.0.jar /docker/wiremock.jar

WORKDIR /

# Copy the current directory contents into the container at /wiremock
ADD wiremock.jar wiremock.jar

# Make port 443 available to the world outside this container
EXPOSE 443
EXPOSE 80

# Deploy the app
CMD java -jar wiremock.jar
