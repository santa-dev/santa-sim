
Fragments below this point are part of a Junit test framework for
the Santa classes.  These are narrow-scope tests of individual method functionality.
These are not meant to be tests of complex interactions
between classes, although those certainly can be tested under carefully
controlled conditions.

A good tutorial on writing and running tests with JUnit can be found
at http://www.vogella.com/tutorials/JUnit/article.html


To write a test with the JUnit 4.x framework you annotate a method
with the @org.junit.Test annotation.  Technically JUnit tests classes
do not need follow a particular naming rule, but a widespread
convention is to use the name of the class under test and to add the
"Test" suffix to the test class.  Each test method should be annotated
with a `@Test`

## Running JUnit tests

The Eclipse IDE provides support for executing your tests
interactively.

To run a test, select the class which contains the tests, right-click
and select Run-as → JUnit Test. This starts JUnit and executes
all test methods in this class.  JUnit tests can be run under the
debugger by selecting Debug-as → JUnit Test.


