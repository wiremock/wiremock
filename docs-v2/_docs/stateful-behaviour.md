---
layout: docs
title: Stateful Behaviour
toc_rank: 90
redirect_from: "/stateful-behaviour.html"
description: Mimicking stateful behaviour by returning different HTTP responses for the same request given a scenario's state.
---

**Most web services tend to have some state, which changes as you and
others interact with it. So it's pretty useful to be able to simulate
this when you've swapped a real service for a test double.**

## Scenarios

WireMock supports state via the notion of scenarios. A scenario is
essentially a state machine whose states can be arbitrarily assigned. It
starting state is always `Scenario.STARTED`. Stub mappings can be
configured to match on scenario state, such that stub A can be returned
initially, then stub B once the next scenario state has been triggered.

For example, suppose we're writing a to-do list application consisting
of a rich client of some kind talking to a REST service. We want to test
that our UI can read the to-do list, add an item and refresh itself,
showing the updated list.

In Java this could be set up like this:

```java
@Test
public void toDoListScenario() {
    stubFor(get(urlEqualTo("/todo/items")).inScenario("To do list")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse()
                    .withBody("<items>" +
                            "   <item>Buy milk</item>" +
                            "</items>")));

    stubFor(post(urlEqualTo("/todo/items")).inScenario("To do list")
            .whenScenarioStateIs(STARTED)
            .withRequestBody(containing("Cancel newspaper subscription"))
            .willReturn(aResponse().withStatus(201))
            .willSetStateTo("Cancel newspaper item added"));

    stubFor(get(urlEqualTo("/todo/items")).inScenario("To do list")
            .whenScenarioStateIs("Cancel newspaper item added")
            .willReturn(aResponse()
                    .withBody("<items>" +
                            "   <item>Buy milk</item>" +
                            "   <item>Cancel newspaper subscription</item>" +
                            "</items>")));

    WireMockResponse response = testClient.get("/todo/items");
    assertThat(response.content(), containsString("Buy milk"));
    assertThat(response.content(), not(containsString("Cancel newspaper subscription")));

    response = testClient.postWithBody("/todo/items", "Cancel newspaper subscription", "text/plain", "UTF-8");
    assertThat(response.statusCode(), is(201));

    response = testClient.get("/todo/items");
    assertThat(response.content(), containsString("Buy milk"));
    assertThat(response.content(), containsString("Cancel newspaper subscription"));
}
```

The JSON equivalent for the above three stubs is:

```json
{
    "scenarioName": "To do list",
    "requiredScenarioState": "Started",
    "request": {
        "method": "GET",
        "url": "/todo/items"
    },
    "response": {
        "status": 200,
        "body" : "<items><item>Buy milk</item></items>"
    }
}

{
    "scenarioName": "To do list",
    "requiredScenarioState": "Started",
    "newScenarioState": "Cancel newspaper item added",
    "request": {
        "method": "POST",
        "url": "/todo/items",
        "bodyPatterns": [
            { "contains": "Cancel newspaper subscription" }
         ]
    },
    "response": {
        "status": 201
    }
}

{
    "scenarioName": "To do list",
    "requiredScenarioState": "Cancel newspaper item added",
    "request": {
        "method": "GET",
        "url": "/todo/items"
    },
    "response": {
        "status": 200,
        "body" : "<items><item>Buy milk</item><item>Cancel newspaper subscription</item></items>"
    }
}
```

## Getting scenario state

The names, current state and possible states of all scenarios can be fetched.

Java:

```java
List<Scenario> allScenarios = getAllScenarios();
```


JSON:

```json
GET /__admin/scenarios
{
  "scenarios" : [ {
    "id" : "c8d249ec-d86d-48b1-88a8-a660e6848042",
    "name" : "my_scenario",
    "state" : "Started",
    "possibleStates" : [ "Started", "state_2", "state_3" ]
  } ]
}
```


## Resetting scenarios

The state of all configured scenarios can be reset back to
`Scenario.START` either by calling `WireMock.resetAllScenarios()` in
Java, or posting an empty request to `http://<host>:<port>/__admin/scenarios/reset`.





