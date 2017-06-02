# Vert.x Example Maven Project

Example project for creating a Vert.x module with a Gradle build.

By default this module contains a simple Java verticle which listens on the event bus and responds to `ping!`
messages with `pong!`.

This example also shows you how to write tests in Java, Groovy, Ruby and Python


"numtowords" endpoint
curl http://localhost:8080/numtowords/1234

"lettercount" endpoint
curl -X POST -d "three thousand" http://localhost:8080/countLetters

"range" endpoint
curl -X POST -d "from=11&to=223" http://localhost:8080/range/
