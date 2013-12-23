.. _shutting-down:

*************
Shutting down
*************

.. rubric::
    WireMock can be shut down remotely. This can be useful to terminate a standalone WireMock instance created from a
    language other than Java.

To shutdown the server, either call ``WireMock.shutdownServer()`` or post a request with an empty body to
``http://<host>:<port>/__admin/shutdown``.

When using WireMock in Java, use of this feature is not typically necessary. See :ref:`getting-started` for more details.

Note that this feature is not available when running WireMock from a servlet container.