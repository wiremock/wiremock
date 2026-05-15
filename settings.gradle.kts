plugins {
    id("com.autonomousapps.build-health") version "3.12.0"
}

rootProject.name = "wiremock"

include("wiremock-core")
include("wiremock-core:certificate-generator")
include("wiremock-standalone")
include("wiremock-junit4")
include("wiremock-junit5")
include("wiremock-jetty")
include("wiremock-httpclient-apache5")
include("test-extension")
include("wiremock-url:wiremock-string-parser")
include("wiremock-url:wiremock-string-parser-jackson2")
include("wiremock-url:wiremock-string-parser-jackson3")
include("wiremock-url:wiremock-url")
include("wiremock-url:wiremock-url-jackson2")
include("wiremock-url:wiremock-url-jackson3")
