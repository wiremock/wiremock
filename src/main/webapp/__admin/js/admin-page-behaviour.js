$(document).ready(function(){
	$('#tabs').tabs();
	
	$('#addButton').button();
	$('#addButton').click(function() {
		var method = $('#requestMethod').val();
		var urlIsExact = $('urlSpecType').val() === 'Exact';
		var url;
		var urlPattern;
		if (urlIsExact) {
			url = $('#url').val();
		} else {
			urlPattern = $('#url').val(); 
		}
		
		var status = $('#status').val();
		var body = $('#body').val();
		
		var mappingSpec = {
			request: {
				method: method,
				url: url,
				urlPattern: urlPattern
			},
			response: {
				status: status,
				body: body
			}
		};
		
		WireMock.addMapping(mappingSpec, 
			function() {
			
			},
			function() {
				
			});
	});
});