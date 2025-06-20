plugins {
    id("com.autonomousapps.build-health") version "2.18.0"
}

rootProject.name = "wiremock"

include("wiremock-common")
include("wiremock-jetty")
