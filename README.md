# Microservices Demonstration Project 

_NOTE:_ This project is based on Vert.x Example Maven Project.  However, it is mostly stripped of those original files.

Example project for creating a Vert.x module with a Maven build.

Inspired by _Project Euler_ [Number Letter Counts](https://projecteuler.net/problem=17).

## TL;DR
**Netbeans / IntelliJ-IDEA**
1. Locate the `pom.xml` file
1. Right-click the `pom.xml` file, choose `Run Maven > Goals...`.
1. In the `Goals` dialog type `package` in the `Goals:` field.
1. Click `OK`.
1. Open a `Terminal/Command Prompt`.
1. Navigate to the project folder, `target` subfolder.
1. Enter the command `java -jar number-letter-count-3.4.1-fat.jar` and press `Enter`.
1. Verify the application is running without errors by monitoring the output in the terminal/command window.

Now that the application is running you can test it.  Using cUrl, Postman, Fiddler, or your favorite utility, enter one of the following commands.

#### "numtowords" endpoint
`curl http://localhost:8080/numtowords/1234`

#### "lettercount" endpoint
`curl -X POST -d "three thousand" http://localhost:8080/countLetters`

#### "range" endpoint
`curl -X POST -d "from=11&to=223" http://localhost:8080/range/`

## D33TZ

