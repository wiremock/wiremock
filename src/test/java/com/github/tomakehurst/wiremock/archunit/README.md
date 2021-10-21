Architecture Rules enforced by ArchUnit
=======================================

[ArchUnit](https://archunit.org) is used to enforce a variety of Architecture Rules, guiding the project towards
gradual conformance.

Examples of enforced rules are:
 - the gradual adoption of JUnit Jupiter
 - preventing unused classes and methods

More rules are to come as we refine our goal architecture.
See the individual test classes for further details.

In rare circumstances a ArchRule might result in a detected false positive or tolerated violation.
In those cases we've defined a
[violation store](https://www.archunit.org/userguide/html/000_Index.html#_freezing_arch_rules)
that can be updated to reflect the accepted violation.
