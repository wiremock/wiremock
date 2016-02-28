package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.common.Dates;

import java.util.Date;

public class CookiePattern {

    private final String path;
    private final Boolean secure;
    private final ValuePattern valuePattern;
    private final Date expiresAt;
    private final Date expiresBefore;
    private final Date expiresAfter;

    public CookiePattern(@JsonProperty("path") String path,
                         @JsonProperty("secure") Boolean secure,
                         @JsonProperty("valuePattern") ValuePattern valuePattern,
                         @JsonProperty("expiresAt") String expiresAt,
                         @JsonProperty("expiresBefore") String expiresBefore,
                         @JsonProperty("expiresAfter") String expiresAfter) {
        
        this(path, secure, valuePattern, Dates.parse(expiresAt), Dates.parse(expiresBefore), Dates.parse(expiresAfter));
    }

    CookiePattern(String path, Boolean secure, ValuePattern valuePattern, Date expiresAt, Date expiresBefore, Date expiresAfter) {
        this.path = path;
        this.secure = secure;
        this.valuePattern = valuePattern;
        this.expiresAt = expiresAt;
        this.expiresBefore = expiresBefore;
        this.expiresAfter = expiresAfter;
    }

    public static Builder cookiePattern() {
        return new Builder();
    }

    public String getPath() {
        return path;
    }

    public Boolean getSecure() {
        return secure;
    }

    public ValuePattern getValuePattern() {
        return valuePattern;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public Date getExpiresBefore() {
        return expiresBefore;
    }

    public Date getExpiresAfter() {
        return expiresAfter;
    }

    public boolean isMatchFor(Cookie cookie) {
        return false;
    }

    public static class Builder {
        private String path;
        private Boolean secure;
        private ValuePattern valuePattern;
        private Date expiresAt;
        private Date expiresBefore;
        private Date expiresAfter;

        public Builder withPath(String path) {
            this.path = path;
            return this;
        }

        public Builder withSecure(Boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder withValue(ValuePattern valuePattern) {
            this.valuePattern = valuePattern;
            return this;
        }

        public Builder withExpiresAt(Date expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder withExpiresAt(String expiresAt) {
            this.expiresAt = Dates.parse(expiresAt);
            return this;
        }

        public Builder withExpiresBefore(Date expiresBefore) {
            this.expiresBefore = expiresBefore;
            return this;
        }

        public Builder withExpiresBefore(String expiresBefore) {
            this.expiresBefore = Dates.parse(expiresBefore);
            return this;
        }

        public Builder withExpiresAfter(Date expiresAfter) {
            this.expiresAfter = expiresAfter;
            return this;
        }

        public Builder withExpiresAfter(String expiresAfter) {
            this.expiresAfter = Dates.parse(expiresAfter);
            return this;
        }

        public CookiePattern build() {
            return new CookiePattern(path, secure, valuePattern, expiresAt, expiresBefore, expiresAfter);
        }
    }
}
