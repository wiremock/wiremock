var AdminPage = {};
AdminPage.StubMappingTab = {};

var ADD_STUB_BUTTON = '#newStubMappingForm #addButton';

AdminPage.StubMappingTab.setFormToPostingState = function() {
	if ($(ADD_STUB_BUTTON).button('option', 'label') === 'Adding...') {
		return;
	}
	$(ADD_STUB_BUTTON).button('option', 'disabled', true);
	$(ADD_STUB_BUTTON).button('option', 'label', 'Adding...');
}

AdminPage.StubMappingTab.setFormToEditingState = function() {
	if ($(ADD_STUB_BUTTON).button('option', 'label') === 'Add') {
		return;
	}
	$(ADD_STUB_BUTTON).button('option', 'disabled', false);
	$(ADD_STUB_BUTTON).button('option', 'label', 'Add');
}


$(document).ready(function(){
	$('#tabs').tabs();
	
	$(ADD_STUB_BUTTON).button();
	$(ADD_STUB_BUTTON).click(function() {
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
		
		AdminPage.StubMappingTab.setFormToPostingState();
		WireMock.addMapping(mappingSpec, 
			function() {
				AdminPage.StubMappingTab.setFormToEditingState();
			},
			function() {
				$( "#stubMappingErrorDialog" ).dialog({
					modal: true,
					buttons: {
						Ok: function() {
							$( this ).dialog("close");
						}
					}
				});
				AdminPage.StubMappingTab.setFormToEditingState();
			});
	});
	
	$('#newStubMappingForm :input').change(function() {
		AdminPage.StubMappingTab.setFormToEditingState();
	});
});