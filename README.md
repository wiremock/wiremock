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

Images
------------
[Mappings](./images/mappings.png)

[Separated View](./images/mappings-separated.png)

[StateMachine](./images/state-machine.png)