WireMock with GUI
======================================================
Extends WireMock with a graphical user interface

Key Features
------------
-	Mappings
     - A paginated list of all mapping
     - Search for mappings
     - Add / Edit / Remove new mappings 
     - Add templates directly into mapping (Response Templating, Proxying, etc.)
     - Save / Reset / Remove all mappings
     - Reset all scenarios
     - Separated view improve readability
-	Matched
     - A paginated list of all requests which could be handled by Wiremock
     - Copy cUrl
     - Reset Journal
-	Unmatched
     - A paginated list of all requests which could not be handled by Wiremock
     - Copy cUrl
     - Copy request to clipboard for new mapping creation
     - Copy SOAP to clipboard which understands not matched SOAP requests and help during creation
     - Reset Journal
-	StateMachine (experimental)
     - A paginated list of all mappings which are part of a scenario
     - States are calculated automatically
     - Mappings are represented as links
     - Button on links allows to show mapping details
-	Record/playback of stubs
     - Help to start recording or snapshot
-	Auto refresh when mappings or requests changes

Where do I find the GUI?
------------
The gui is part of the Wiremock admin interface. Therefore, just open the following URL: 

\<Wiremock baseUrl\>/__admin/webapp

Features except gui
------------
Custom Handlebars helpers

| Name          | Parameter     | Description   |
| ------------- | ------------- | ------------- |
| JWT           | algo          | Signing algorithm. Check jsonwebtoken for SignatureAlgorithm. E.g. RS256 |
|               | key           | Base64 encoded key to use. Kind of key depends on used algo |
|               | claims        | a json string which describes the claims to use. Must not be null in case no payload is defined |
|               | payload       | a json string which contains the content of the jwt. Must not be null in case no claims are defined |
|               | header        | a json string which contains the header to use |

Images
------------
[Mappings](./images/mappings.png)

[Separated View](./images/mappings-separated.png)

[StateMachine](./images/state-machine.png)