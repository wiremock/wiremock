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
package com.github.tomakehurst.wiremock.admin.tasks;

import com.github.tomakehurst.wiremock.admin.AdminTask;
import com.github.tomakehurst.wiremock.admin.model.HealthCheck;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.TimeUtil;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.io.InputStream;
import java.util.Properties;

/**
 * This class is for getting HealthCheck information
 */
public class GetHealthCheckTask implements AdminTask {

    private final Properties properties = new Properties();
    private static final String resourceName = "application-build.properties";
    /**
     * Override execute for returning health check response
     * @param admin woremock admin
     * @param request wiremock request
     * @param pathParams path parameters
     * @return json format Response
     */
    @Override
    public ResponseDefinition execute(Admin admin, Request request, PathParams pathParams) {

        HealthCheck result = new HealthCheck();
        long startTime = System.nanoTime();
        String version = getVersion(resourceName);
        long upTime = getUpTime();

        result.setVersion(version);
        result.setUpTime(upTime + " ms");

        long endTime = System.nanoTime();
        long responseTime = getResponseTime(startTime, endTime);
        result.setResponseTime(responseTime +" ns");

        return ResponseDefinitionBuilder.jsonResponse(result);
    }
    /**
     * This method is for getting wiremock version
     * @param resourceName source file which stores application's version
     * @return application version
     */
    public String getVersion(String resourceName){

        try (InputStream in = GetHealthCheckTask.class.getClassLoader()
                .getResourceAsStream(resourceName)){
            properties.load(in);
        }catch (Exception e) {
            throw new IllegalStateException(String.format("Failed to load version source file %s", resourceName),e);
        }
        return properties.getProperty("build.version");
    }

    /**
     * This method is for calculating application up time
     * @return application upTime
     */
    public long getUpTime(){
        return System.currentTimeMillis() - TimeUtil.getStartTime();
    }

    /**
     * This method is for calculating the amount of time
     * spent handling the health check request
     * @param startTime application starts to proceed request time
     * @param endTime application return response time
     * @return  application response time
     */
    public long getResponseTime(long startTime, long endTime){
        return endTime - startTime;
    }

}
