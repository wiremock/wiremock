plugins {
    id("wiremock.common-conventions")
}

dependencies {
    api(libs.apache.http5.client)
    api(libs.apache.http5.core)
    api(libs.commons.fileupload)
    api(libs.guava) {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
    }
    api(libs.handlebars) {
        exclude(group = "org.mozilla", module = "rhino")
    }

    api(platform(libs.jackson.bom))
    api(libs.jackson.annotations)
    api(libs.jackson.core)
    api(libs.jackson.databind)

    api(libs.json.schema.validator)
    api(libs.json.unit.core)

    api(platform(libs.junit.bom))
    api(libs.junit.jupiter.api)

    api(libs.xmlunit.core)

    implementation(libs.handlebars.helpers) {
        exclude(group = "org.mozilla", module = "rhino")
        exclude(group = "org.apache.commons", module = "commons-lang3")
    }
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jopt.simple)
    implementation(libs.json.path) {
        exclude(group = "org.ow2.asm", module = "asm")
    }
    implementation(libs.junit.platform.commons)
    implementation(libs.slf4j.api)
    implementation(libs.xmlunit.legacy) {
        exclude(group = "junit", module = "junit")
    }
    implementation(libs.xmlunit.placeholders)

    compileOnly(libs.junit4) {
        exclude(group = "org.hamcrest", module = "hamcrest-core")
    }

    modules {
        module("org.apache.logging.log4j:log4j-core") {
            replacedBy("org.apache.logging.log4j:log4j-to-slf4j")
        }
        module("commons-logging:commons-logging") {
            replacedBy("org.slf4j:jcl-over-slf4j")
        }
        module("log4j:log4j") {
            replacedBy("org.slf4j:log4j-over-slf4j")
        }
        module("org.hamcrest:hamcrest-core") {
            replacedBy("org.hamcrest:hamcrest")
        }
        module("org.hamcrest:hamcrest-library") {
            replacedBy("org.hamcrest:hamcrest")
        }
    }
}
