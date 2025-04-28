plugins {
    id("com.autonomousapps.build-health") version "2.16.0"
}

rootProject.name = "wiremock"

include("wiremock-common")
include("wiremock-jetty-12")
