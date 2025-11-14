plugins {
    id("com.autonomousapps.build-health") version "3.4.1"
}

rootProject.name = "wiremock"

include("wiremock-common")
include("wiremock-junit4")
include("wiremock-junit5")
include("wiremock-jetty")
