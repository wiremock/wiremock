FROM openjdk:8






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
	&& mkdir /home/gradle/.gradle \
	&& chown --recursive gradle:gradle /home/gradle \
	\
	&& echo "Symlinking root Gradle cache to gradle Gradle cache" \
	&& ln -s /home/gradle/.gradle /root/.gradle

# Create Gradle volume
USER gradle
VOLUME "/home/gradle/.gradle"
VOLUME "/home/wiremock"

WORKDIR /home/wiremock

RUN set -o errexit -o nounset \
    && echo "Clone repo and switch branch"\
    && git clone https://github.com/holomekc/wiremock.git .\
    && git checkout new-gui



RUN su set -o errexit -o nounset \
	&& echo "Build wiremock with ui" \
	&& gradle clean jar shadowJar

WORKDIR /home/wiremock/build/

ENV FILE *-standalone-*.jar

# Copy the current directory contents into the container at /wiremock
ADD ${FILE} /wiremock

# Make port 443 available to the world outside this container
EXPOSE 443

# Deploy the app
CMD java -jar ${FILE}
