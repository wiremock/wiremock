/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.stubbing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;

@JsonSerialize(include=Inclusion.NON_NULL)
@JsonPropertyOrder({ "request", "response" })
public class StubMapping {
	
	public static final int DEFAULT_PRIORITY = 5; 

	private RequestPattern request;
	private ResponseDefinition response;
	private Integer priority;
	private String scenarioName;
	private String requiredScenarioState;
	private String newScenarioState;
	private Scenario scenario;

	private long insertionIndex;
    private boolean isTransient = true;

	public StubMapping(RequestPattern requestPattern, ResponseDefinition response) {
		this.request = requestPattern;
		this.response = response;
	}
	
	public StubMapping() {
		//Concession to Jackson
	}
	
	public static final StubMapping NOT_CONFIGURED =
	    new StubMapping(new RequestPattern(), ResponseDefinition.notConfigured());

    public static StubMapping buildFrom(String mappingSpecJson) {
        return Json.read(mappingSpecJson, StubMapping.class);
    }

    public static String buildJsonStringFor(StubMapping mapping) {
		return Json.write(mapping);
	}

    public RequestPattern getRequest() {
		return request;
	}
	
	public ResponseDefinition getResponse() {
		return response;
	}
	
	public void setRequest(RequestPattern request) {
		this.request = request;
	}

	public void setResponse(ResponseDefinition response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return Json.write(this);
	}

	@JsonIgnore
	public long getInsertionIndex() {
		return insertionIndex;
	}

	@JsonIgnore
	public void setInsertionIndex(long insertionIndex) {
		this.insertionIndex = insertionIndex;
	}

    /**
     * @return True if this StubMapping is not persisted to the file system, false otherwise.
     */
    @JsonIgnore
    public boolean isTransient() {
        return isTransient;
    }

    @JsonIgnore
    public void setTransient(boolean isTransient) {
        this.isTransient = isTransient;
    }

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public String getScenarioName() {
		return scenarioName;
	}

	public void setScenarioName(String scenarioName) {
		this.scenarioName = scenarioName;
	}

	public String getRequiredScenarioState() {
		return requiredScenarioState;
	}

	public void setRequiredScenarioState(String requiredScenarioState) {
		this.requiredScenarioState = requiredScenarioState;
	}

	public String getNewScenarioState() {
		return newScenarioState;
	}

	public void setNewScenarioState(String newScenarioState) {
		this.newScenarioState = newScenarioState;
	}
	
	public void updateScenarioStateIfRequired() {
		if (isInScenario() && modifiesScenarioState()) {
			scenario.setState(newScenarioState);
		}
	}
	
	@JsonIgnore
	public Scenario getScenario() {
		return scenario;
	}

	@JsonIgnore
	public void setScenario(Scenario scenario) {
		this.scenario = scenario;
	}

	@JsonIgnore
	public boolean isInScenario() {
		return scenarioName != null;
	}
	
	@JsonIgnore
	public boolean modifiesScenarioState() {
		return newScenarioState != null;
	}
	
	@JsonIgnore
	public boolean isIndependentOfScenarioState() {
		return !isInScenario() || requiredScenarioState == null;
	}
	
	@JsonIgnore
	public boolean requiresCurrentScenarioState() {
		return !isIndependentOfScenarioState() && requiredScenarioState.equals(scenario.getState());
	}
	
	public int comparePriorityWith(StubMapping otherMapping) {
		int thisPriority = priority != null ? priority : DEFAULT_PRIORITY;
		int otherPriority = otherMapping.priority != null ? otherMapping.priority : DEFAULT_PRIORITY;
		return thisPriority - otherPriority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (insertionIndex ^ (insertionIndex >>> 32));
		result = prime
				* result
				+ ((newScenarioState == null) ? 0 : newScenarioState.hashCode());
		result = prime * result
				+ ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((request == null) ? 0 : request.hashCode());
		result = prime
				* result
				+ ((requiredScenarioState == null) ? 0 : requiredScenarioState
						.hashCode());
		result = prime * result
				+ ((response == null) ? 0 : response.hashCode());
		result = prime * result
				+ ((scenarioName == null) ? 0 : scenarioName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StubMapping other = (StubMapping) obj;
		if (insertionIndex != other.insertionIndex) {
			return false;
		}
		if (newScenarioState == null) {
			if (other.newScenarioState != null) {
				return false;
			}
		} else if (!newScenarioState.equals(other.newScenarioState)) {
			return false;
		}
		if (priority == null) {
			if (other.priority != null) {
				return false;
			}
		} else if (!priority.equals(other.priority)) {
			return false;
		}
		if (request == null) {
			if (other.request != null) {
				return false;
			}
		} else if (!request.equals(other.request)) {
			return false;
		}
		if (requiredScenarioState == null) {
			if (other.requiredScenarioState != null) {
				return false;
			}
		} else if (!requiredScenarioState.equals(other.requiredScenarioState)) {
			return false;
		}
		if (response == null) {
			if (other.response != null) {
				return false;
			}
		} else if (!response.equals(other.response)) {
			return false;
		}
		if (scenarioName == null) {
			if (other.scenarioName != null) {
				return false;
			}
		} else if (!scenarioName.equals(other.scenarioName)) {
			return false;
		}
		return true;
	}

	
	
}
