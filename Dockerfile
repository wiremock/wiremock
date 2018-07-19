FROM openjdk:8

USER root

# Define working directory.
RUN mkdir -p /data/app
WORKDIR /data/app

VOLUME ["/root/.gradle/caches/"]

## Install Java (Open JDK)
#RUN \
#    apt-get update && \
#    apt-get -y install unzip openjdk-8-jdk

# Download and install Gradle
RUN \
    cd /usr/local && \
    curl -L https://services.gradle.org/distributions/gradle-2.6-bin.zip -o gradle-2.6-bin.zip && \
    unzip gradle-2.6-bin.zip && \
    rm gradle-2.6-bin.zip


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

RUN set -o errexit -o nounset \
    && echo "git: Clone repo"\
    && git clone https://github.com/holomekc/wiremock.git .\
    && echo "git: Switch branch"\
    && git checkout new-gui

RUN useradd -ms /bin/bash wiremock


# Export some environment variables
ENV GRADLE_HOME=/usr/local/gradle-2.6
ENV PATH=$PATH:$GRADLE_HOME/bin JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

RUN set -o errexit -o nounset \
	&& echo "build: Build wiremock with ui" \
	&& gradle clean jar shadowJar \
	&& echo "build: copy file to docker dir"\
	&& cp build/libs/wiremock-standalone-*.jar /home/wiremock/wiremock.jar

#ADD data/app/build/libs/wiremock.jar wiremock.jar
EXPOSE 443
EXPOSE 80

USER wiremock


# Deploy the app
CMD java -jar /home/wiremock/wiremock.jar


