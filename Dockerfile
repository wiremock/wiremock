FROM java:7-jre

ADD http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.57/wiremock-1.57-standalone.jar .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "./wiremock-1.57-standalone.jar"]
