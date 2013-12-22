.. _record-playback:

*******************
Record and Playback
*******************

.. rubric::
    WireMock has the ability to create stub mappings by recording them while you send requests. This can be used to
    quickly capture a collection of responses from a real service then use them offline in your tests.

.. _record-playback-recording:

Recording
=========

Recording is done by starting the standalone runner like this:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar --proxy-all="http://search.twitter.com" --record-mappings --verbose

Once it's started you send requests to it as if it was the remote service:

.. parsed-literal::

    $ curl "http://localhost:8080/search.json?q=from:sirbonar&result_type=recent&rpp=1"

Now if you look under ``mappings`` and ``__files`` (under the current directory you're running WireMock in)
you'll see that a new file has been one created under each, the former for the stub mapping and the latter
for the body content.

.. note::
    The WireMock recorder will ignore a request with a method and URL identical to those of a stub already recorded.

GZip decompressing
------------------

WireMock can be configured to decompress any gzipped responses it receives when recording, making editing the resulting
body files much easier. WireMock also removes the ``Content-Encoding: gzip`` header when playing back the response. To
enable gzip decompression, add the ``--ungzip-recorded-responses`` argument when starting the standalone runner, e.g.:

.. parsed-literal::

    $ java -jar wiremock-|version|-standalone.jar --proxy-all="http://search.twitter.com" --record-mappings --ungzip-recorded-responses --verbose

Playback
========

If you start the standlone runner again without the extra commandline options it will automatically load the newly
created stub mappings and start serving them.

Alternatively you can copy the files created under ``src/test/resources/mappings`` and ``src/test/resources/__files``
in your Java project, or the equivalents under ``WEB-INF/wiremock`` in your WAR project.
