---
description: >
    mocking states for Web services.
---

# Simulating Stateful Behavior for Testing 

WireMock supports simulations of stateful behaviors of Web services. You can  make use of states that change during the time that 
users interact with them.  

## Scenarios

You manage mocking of stateful behavior using scenarios. A scenario is essentially a state machine whose states can be arbitrarily assigned. 

When working with scenarios: 

- the starting state is always `Scenario.STARTED`. 
- stub mappings can be configured to match a scenario state, and run sequentially.
-- for example, stub A can be returned initially, then stub B once the next scenario state has been triggered.

The following examples illustrate this concept, using a to-do list application that is a 
rich client of some kind and sends requests to a REST service. This tests
that the UI can read the to-do list, add an item, and refresh itself,
showing the updated list:

=== "Java"

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

=== "JSON"

    ```json
    {
        "mappings": [
            {
                "scenarioName": "To do list",
                "requiredScenarioState": "Started",
                "request": {
                    "method": "GET",
                    "url": "/todo/items"
                },
                "response": {
                    "status": 200,
                    "body": "<items><item>Buy milk</item></items>"
                }
            },
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
            },
            {
                "scenarioName": "To do list",
                "requiredScenarioState": "Cancel newspaper item added",
                "request": {
                    "method": "GET",
                    "url": "/todo/items"
                },
                "response": {
                    "status": 200,
                    "body": "<items><item>Buy milk</item><item>Cancel newspaper subscription</item></items>"
                }
            }
        ]
    }
    ```

## Getting scenario state

The names, current state and possible states of all scenarios can be fetched.

=== "Java"

    ```java
    List<Scenario> allScenarios = getAllScenarios();
    ```

=== "JSON"

    ```json
    GET /__admin/scenarios
    {
      "scenarios" : [ {
        "id" : "my_scenario",
        "name" : "my_scenario",
        "state" : "Started",
        "possibleStates" : [ "Started", "state_2", "state_3" ]
      } ]
    }
    ```

## Resetting scenarios

The state of all configured scenarios can be reset back to
`Scenario.START` either by calling

Java:

```java
WireMock.resetAllScenarios()
```

To do the equivalent via the HTTP API, send an empty `POST` request to `/__admin/scenarios/reset`.


## Resetting a single scenario

You can reset the state of an individual scenario.

Java:

```java
WireMock.resetScenario("my_scenario");
```

The do the equivalent via the HTTP API, send an empty `PUT` to `/__admin/scenarios/my_scenario/state`.


## Setting the state of an individual scenario

You can also set the state of an individual scenario to a specific value:

=== "Java"

    ```java
    WireMock.setScenarioState("my_scenario", "state_2");
    ```

=== "HTTP"

    ```json
    PUT /__admin/scenarios/my_scenario/state
    {
        "state": "state_2"
    }
    ```
