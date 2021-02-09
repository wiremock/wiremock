FROM openjdk:8

RUN useradd -ms /bin/bash wiremock

USER wiremock

WORKDIR /home/wiremock

RUN  curl -L "https://github.com/holomekc/wiremock/releases/download/2.27.2-ui/wiremock-standalone-2.27.2.jar" -o /home/wiremock/wiremock.jar

CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /home/wiremock/wiremock.jar


