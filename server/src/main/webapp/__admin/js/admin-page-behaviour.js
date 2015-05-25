var AdminPage = {};
AdminPage.StubMappingTab = {};

var ADD_STUB_BUTTON = '#newStubMappingForm #addButton';
var NEW_REQUEST_HEADER_BUTTON = '#newStubMappingForm #addRequestHeaderButton';
var NEW_RESPONSE_HEADER_BUTTON = '#newStubMappingForm #addResponseHeaderButton';

AdminPage.StubMappingTab.setFormToPostingState = function() {
	if ($(ADD_STUB_BUTTON).button('option', 'label') === 'Adding...') {
		return;
	}
	$(ADD_STUB_BUTTON).button('option', 'disabled', true);
	$(ADD_STUB_BUTTON).button('option', 'label', 'Adding...');
};

AdminPage.StubMappingTab.setFormToEditingState = function() {
	if ($(ADD_STUB_BUTTON).button('option', 'label') === 'Add') {
		return;
	}
	$(ADD_STUB_BUTTON).button('option', 'disabled', false);
	$(ADD_STUB_BUTTON).button('option', 'label', 'Add');
};

AdminPage.StubMappingTab.addMapping = function() {
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
	
	return false;
};

AdminPage.StubMappingTab.addNewRequestHeaderFields = function() {
	var content = '<div class="requestHeaderFields">													\
		<label for="requestHeaderName">Key: </label>													\
		<input class="requestHeaderName" name="requestHeaderName" type="text" size="20"/> 				\
		<select name="requestHeaderOperator" class="requestHeaderOperator">								\
			<option value="equalTo">equals</option>														\
			<option value="matches">matches</option>													\
			<option value="doesNotMatch">doesn\'t match</option>										\
		</select>																						\
		<input class="requestHeaderValue" name="requestHeaderValue" type="text" size="20"/>				\
		</div>';
	
	$('#requestHeaders').append(content);
};

AdminPage.StubMappingTab.addNewResponseHeaderFields = function() {
	var content = '<div class="responseHeaderFields">													\
		<label for="responseHeaderName">Key: </label>													\
		<input class="responseHeaderName" name="responseHeaderName" type="text" size="20"/> 			\
		<span class="midFormRowText">=</span> 															\
		<input class="responseHeaderValue" name="responseHeaderValue" type="text" size="20"/>			\
		</div>';
	
	$('#responseHeaders').append(content);
};


$(document).ready(function(){
	$('#tabs').tabs();
	$('#newStubMappingForm').submit(function(e) {
		e.preventDefault();
		return false;
	});
	
	$(NEW_REQUEST_HEADER_BUTTON).button({ icons: { primary: 'ui-icon-plusthick' } });
	$(NEW_REQUEST_HEADER_BUTTON).click(AdminPage.StubMappingTab.addNewRequestHeaderFields);
	
	$(NEW_RESPONSE_HEADER_BUTTON).button({ icons: { primary: 'ui-icon-plusthick' } });
	$(NEW_RESPONSE_HEADER_BUTTON).click(AdminPage.StubMappingTab.addNewResponseHeaderFields);
	
	$(ADD_STUB_BUTTON).button();
	$(ADD_STUB_BUTTON).click(AdminPage.StubMappingTab.addMapping);
	
	$('#newStubMappingForm :input').change(AdminPage.StubMappingTab.setFormToEditingState());
});