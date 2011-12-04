var WireMock = {}

WireMock.addMapping = function(mappingSpec, successHandler, errorHandler) {
	$.ajax({
		url: "/__admin/mappings/new",
		type: "POST",
		success: successHandler,
		error: errorHandler,
		dataType: "json",
		data: JSON.stringify(mappingSpec)	
	});
}