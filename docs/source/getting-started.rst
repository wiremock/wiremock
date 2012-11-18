.. _getting-started:

Getting Started
===============

Maven
-----
To add WireMock to your Java project, put the following in the dependencies section of your POM:

.. code-block:: xml

    <dependency>
        <groupId>com.github.tomakehurst</groupId>
        <artifactId>wiremock</artifactId>
        <version>1.25</version>

        <!-- Include this if you have dependency conflicts for Guava, Jetty, Jackson or Apache HTTP Client -->
        <classifier>standalone</classifier>
    </dependency>

JUnit 4.x
---------

