FROM openjdk:8

USER wiremock

RUN  curl -L "https://github.com/holomekc/wiremock/releases/download/2.18.0-ui/wiremock-standalone-2.18.0.jar" -o wiremock.jar

CMD java -jar wiremock.jar


