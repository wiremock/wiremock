package com.tomakehurst.wiremock.verification;

import com.tomakehurst.wiremock.mapping.RequestPattern;

public interface RequestJournal {

	int countRequestsMatching(RequestPattern requestPattern);
}
