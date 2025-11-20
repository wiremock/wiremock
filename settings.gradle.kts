plugins {
    id("com.autonomousapps.build-health") version "3.5.0"
}

rootProject.name = "wiremock"

include("wiremock-core")
include("wiremock-junit4")
include("wiremock-junit5")
include("wiremock-jetty")
include("test-extension")
