FROM openjdk:8

RUN useradd -ms /bin/bash wiremock

USER wiremock

WORKDIR /home/wiremock

RUN  curl -L "https://github.com/holomekc/wiremock/releases/download/2.29.1/wiremock-standalone-2.29.1.jar" -o /home/wiremock/wiremock.jar

CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /home/wiremock/wiremock.jar


