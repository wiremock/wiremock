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
package com.github.tomakehurst.wiremock.admin.model;

public class HealthCheck {
    private String version;
    private String upTime;
    private String responseTime;

    /**
     * get version
     */
    public String getVersion() {
        return version;
    }

    /**
     * set version
     * @param version application version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * get uptime
     */
    public String getUpTime() {
        return upTime;
    }

    /**
     * set uptime
     * @param upTime application up time
     */
    public void setUpTime(String upTime) {
        this.upTime = upTime;
    }

    /**
     * get response time
     */
    public String getResponseTime() {
        return responseTime;
    }

    /**
     * set response time
     * @param responseTime application response time
     */
    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }
}
