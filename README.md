# Revolut backend test - Money transfer
***
### Run
Navigate to project root directory and run:
```
gradle clean build run
```
App is reachable at localhost:8000
### API documentation
[RAML](./src/main/raml/api.html)

### Technology stack
Java 8 - language used for implementation;
JUnit 5 - testing framework;
Jetty - HTTP server and servlet container;
Jersey - JAX-RS implementation;
Lombok - boilerplate code reduction framework;
H2 - in-memory database;
Hibernate - ORM framework
Mockito - mocking framework used in unit tests;
RestAssured - library for REST tests. Used for e2e testing in the project;