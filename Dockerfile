FROM openjdk:8

RUN useradd -ms /bin/bash wiremock

USER wiremock

RUN  curl -L "https://github.com/holomekc/wiremock/releases/download/2.18.0-ui/wiremock-standalone-2.18.0.jar" -o /home/wiremock/wiremock.jar

CMD java -jar /home/wiremock/wiremock.jar


