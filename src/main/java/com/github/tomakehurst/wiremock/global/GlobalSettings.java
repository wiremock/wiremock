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
package com.github.tomakehurst.wiremock.global;

import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.DelayDistribution;

import java.util.Objects;

public class GlobalSettings {

    private Integer fixedDelay;
    private DelayDistribution delayDistribution;
    private Parameters extended;

    public Integer getFixedDelay() {
        return fixedDelay;
    }

    public void setFixedDelay(Integer fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    public DelayDistribution getDelayDistribution() {
        return delayDistribution;
    }

    public void setDelayDistribution(DelayDistribution distribution) {
        delayDistribution = distribution;
    }

    public Parameters getExtended() {
        return extended;
    }

    public void setExtended(Parameters extended) {
        this.extended = extended;
    }

    public GlobalSettings copy() {
        GlobalSettings newSettings = new GlobalSettings();
        newSettings.setFixedDelay(fixedDelay);
        newSettings.setDelayDistribution(delayDistribution);
        return newSettings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GlobalSettings that = (GlobalSettings) o;
        return Objects.equals(getFixedDelay(), that.getFixedDelay()) &&
                Objects.equals(getDelayDistribution(), that.getDelayDistribution()) &&
                Objects.equals(getExtended(), that.getExtended());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFixedDelay(), getDelayDistribution(), getExtended());
    }
}
