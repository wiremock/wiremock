plugins {
    id("com.autonomousapps.build-health") version "3.5.1"
}

rootProject.name = "wiremock"

include("wiremock-core")
include("wiremock-junit4")
include("wiremock-junit5")
include("wiremock-jetty")
include("wiremock-httpclient-apache5")
include("wiremock-string-parser")
include("wiremock-string-parser-jackson2")
include("wiremock-string-parser-jackson3")
include("wiremock-url")
include("wiremock-url-jackson2")
include("wiremock-url-jackson3")
include("test-extension")
