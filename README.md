Money Transfer Service
======================

Minimalistic REST-service for transfer money between two accounts.

Technical desicions:
- Money datatype is ```long``` (assumes to scale values), in real project decimal may be better.
- I decided to not use any complex REST and DI frameworks (as far as Spring Boot is deprecated).
- As in-memory database - H2.
- There is no any logging (in serious system logging is necessary).
- Simple jdbc used for SQL-queries, jOOQ or Spring-jdbc can significantly simplify the code.

There are three types of tests:
- standard unit-tests for service layer
- simple rest-assured test for http-transport
- property-based test (also used for check concurrency correctness)
