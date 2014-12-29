.. _running-standalone:

*******************************
Running as a Standalone Process
*******************************

The WireMock server can be run in its own process, and configured via the Java API, JSON over HTTP or JSON files.

This will start the server on port 8080:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar

You can `download the standalone JAR from here <http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.52/wiremock-1.52-standalone.jar>`_.

Supported command line options are:

``--port``:
Set the HTTP port number e.g. ``--port 9999``

``--https-port``:
If specified, enables HTTPS on the supplied port.

``--https-keystore``:
Path to a keystore file containing an SSL certificate to use with HTTPS. The keystore must have a password of "password".
This option will only work if ``--https-port`` is specified. If this option isn't used WireMock will default to its
own self-signed certificate.

``--verbose``:
Turn on verbose logging to stdout

``--root-dir``:
Sets the root directory, under which ``mappings`` and ``__files`` reside. This defaults to the current directory.

``--record-mappings``:
Record incoming requests as stub mappings. See :ref:`record-playback`.

``--match-headers``:
When in record mode, capture request headers with the keys specified. See :ref:`record-playback`.

``--proxy-all``:
Proxy all requests through to another base URL e.g. ``--proxy-all="http://api.someservice.com"``
Typically used in conjunction with ``--record-mappings`` such that a session on another service can be recorded.

``--preserve-host-header``: When in proxy mode, it passes the Host header as it comes from the client through to the
proxied service. When this option is not present, the Host header value is deducted from the proxy URL. This option is
only available if the ``--proxy-all`` option is specified.

``--proxy-via``:
When proxying requests (either by using --proxy-all or by creating stub mappings that proxy to other hosts), route via
another proxy server (useful when inside a corporate network that only permits internet access via an opaque proxy).
e.g.
``--proxy-via webproxy.mycorp.com`` (defaults to port 80)
or
``--proxy-via webproxy.mycorp.com:8080``

``--enable-browser-proxying``:
Run as a browser proxy. See :ref:`browser-proxying`.

``--no-request-journal``:
Disable the request journal, which records incoming requests for later verification. This allows WireMock to be run
(and serve stubs) for long periods (without resetting) without exhausting the heap. The ``--record-mappings`` option isn't
available if this one is specified.

``--container-threads``:
The number of threads created for incoming requests. Defaults to 200.

``--help``:
Show command line help


File serving
------------

When running standalone files placed under the ``__files`` directory will be served up as if from under the docroot,
except if stub mapping matching the URL exists. For example if a file exists ``__files/things/myfile.html`` and
no stub mapping will match ``/things/myfile.html`` then hitting ``http://<host>:<port>/things/myfile.html`` will
serve the file.


Configuring via JSON
--------------------

Once the server has started you can give it a spin by setting up a stub mapping via the JSON API:

.. code-block:: console

    $ curl -X POST --data '{ "request": { "url": "/get/this", "method": "GET" }, "response": { "status": 200, "body": "Here it is!\n" }}' http://localhost:8080/__admin/mappings/new

Then fetching it back:

.. code-block:: console

    $ curl http://localhost:8080/get/this
    Here it is!


You can also use the JSON API via files. When the WireMock server starts it creates two directories under the current one:
``mappings`` and ``__files``.

To create a stub like the one above by this method, drop a file with a ``.json`` extension under ``mappings``
with the following content:

.. code-block:: javascript

   {
       "request": {
           "method": "GET",
           "url": "/api/mytest"
       },
       "response": {
           "status": 200,
           "body": "More content\n"
       }
   }

After restarting the server you should be able to do this:

.. code-block:: console

    $ curl http://localhost:8080/api/mytest
    More content


See :ref:`stubbing` and :ref:`verifying` for more on the JSON API.


Shutting Down
=============

To shutdown the server, either call ``WireMock.shutdownServer()`` or post a request with an empty body to
``http://<host>:<port>/__admin/shutdown``.