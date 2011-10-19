package com.tomakehurst.wiremock.standalone;

import com.tomakehurst.wiremock.mapping.Mappings;

public interface MappingsLoader {

	void loadMappingsInto(Mappings mappings);

}