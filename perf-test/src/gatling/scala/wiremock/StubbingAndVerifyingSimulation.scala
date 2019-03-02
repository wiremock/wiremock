package wiremock

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class StubbingAndVerifyingSimulation extends Simulation {

  val loadTestConfiguration = LoadTestConfiguration.fromEnvironment()

  val random = scala.util.Random

  before {
    loadTestConfiguration.before()
//    loadTestConfiguration.mixed100StubScenario()
//    loadTestConfiguration.onlyGet6000StubScenario()
    loadTestConfiguration.getLargeStubScenario()
  }

  after {
    loadTestConfiguration.after()
  }

  val httpConf = http
    .baseURL(loadTestConfiguration.getBaseUrl)

  val mixed100StubScenario = {

    scenario("Mixed 100")
          .repeat(1) {
            exec(http("GETs")
                .get(session => s"load-test/${random.nextInt(49) + 1}")
                .header("Accept", "text/plain+stuff")
                .check(status.is(200)))
          }
          .exec(http("JSON equality POSTs")
              .post("load-test/json")
              .header("Accept", "text/plain")
              .header("Content-Type", "application/json")
              .body(StringBody(LoadTestConfiguration.POSTED_JSON))
              .check(status.is(200)))
          .exec(http("JSONPath POSTs")
              .post("load-test/jsonpath")
              .header("Content-Type", "application/json")
              .body(StringBody(LoadTestConfiguration.JSON_FOR_JSON_PATH_MATCH))
              .check(status.is(201)))
//          .exec(http("XML equality POSTs")
//              .post("load-test/xml")
//              .header("Content-Type", "application/xml")
//              .body(StringBody(LoadTestConfiguration.POSTED_XML.replace("$1", "2")))
//              .check(status.is(200)))
          .exec(http("XPath POSTs")
              .post("load-test/xpath")
              .header("Content-Type", "application/xml")
              .body(StringBody(LoadTestConfiguration.POSTED_XML.replace("$1", String.valueOf(random.nextInt(10)))))
              .check(status.is(200)))
          .exec(http("Text POSTs")
              .post("load-test/text")
              .header("Content-Type", "text/plain")
              .body(StringBody("JSON Web Token (JWT) is a compact, URL-safe means of representing claims to be transferred between two parties. The claims in a JWT are encoded as a JSON object that is used as the payload of a JSON Web Signature (JWS) structure or as the plaintext of a JSON Web Encryption (JWE) structure, enabling the claims to be digitally signed or 12345 integrity protected with a Message Authentication Code (MAC) and/or encrypted.\n"))
              .check(status.is(200)))
          .exec(http("Response templating")
              .put("load-test/templated")
              .header("MyDate", "2018-05-16T01:02:03Z")
              .body(StringBody("{\n    \"outer\": {\n        \"inner\": [1, 2, 3, 4]\n    }\n}"))
              .check(status.is(200)))
  }

  val onlyGet6000StubScenario = {
    scenario("6000 GETs")
      .repeat(1) {
        exec(http("GETs")
          .get(session => s"load-test/${random.nextInt(5999) + 1}")
          .header("Accept", "text/plain+stuff")
          .check(status.is(200)))
        .exec(http("Not founds")
          .get(session => s"load-test/${random.nextInt(5999) + 7000}")
          .header("Accept", "text/plain+stuff")
          .check(status.is(404)))
      }
  }

  val getLargeStubsScenario = {
    scenario("100 large GETs")
      .repeat(1) {
        exec(http("GETs")
          .get(session => s"load-test/${random.nextInt(99) + 1}")
          .header("Accept", "text/plain+stuff")
          .check(status.is(200)))
      }
  }

  setUp(
//    mixed100StubScenario.inject(constantUsersPerSec(loadTestConfiguration.getRate) during(loadTestConfiguration.getDurationSeconds seconds))
//    onlyGet6000StubScenario.inject(constantUsersPerSec(loadTestConfiguration.getRate) during(loadTestConfiguration.getDurationSeconds seconds))
    getLargeStubsScenario.inject(constantUsersPerSec(loadTestConfiguration.getRate) during(loadTestConfiguration.getDurationSeconds seconds))
  ).protocols(httpConf)

}