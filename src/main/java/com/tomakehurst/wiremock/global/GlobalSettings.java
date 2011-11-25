package com.tomakehurst.wiremock.global;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize(include=Inclusion.NON_NULL)
public class GlobalSettings {

	private Integer fixedDelay;

	public Integer getFixedDelay() {
		return fixedDelay;
	}

	public void setFixedDelay(Integer fixedDelay) {
		this.fixedDelay = fixedDelay;
	}
}
