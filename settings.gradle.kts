plugins {
    id("com.autonomousapps.build-health") version "2.19.0"
}

rootProject.name = "wiremock"

include("wiremock-common")
include("wiremock-jetty")
