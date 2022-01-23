FROM eclipse-temurin:17-jre-alpine

RUN apk update && apk upgrade
RUN apk add curl

RUN adduser -u 1000 -G users -h /home/wiremock -D wiremock

USER wiremock

WORKDIR /home/wiremock

RUN  curl -L "https://github.com/holomekc/wiremock/releases/download/$WIREMOCK_VERSION-ui/wiremock-jre8-standalone-$WIREMOCK_VERSION.jar" -o /home/wiremock/wiremock.jar

CMD java -XX:+PrintFlagsFinal $JAVA_OPTIONS -jar /home/wiremock/wiremock.jar


