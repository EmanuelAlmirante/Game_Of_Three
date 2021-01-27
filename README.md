# Game Of Three

### Tech Stack:

- Java 11
- Spring Boot
- Swagger
- JUnit
- Mockito


### Setup:

- Clone/extract project to a folder
- Run the application with:
  - _mvn clean install_
  - _mvn spring-boot:run_
- Test the application with:
  - _mvn test_ -> run all tests
  - _mvn -Dtest=TestClass test_ -> run a single test class
  - _mvn -Dtest=TestClass1,TestClass2 test_ -> run multiple test classes
- Package the application with _mvn package_


#### With Docker:

- Install Docker on your machine
- Launch Docker
- Run the command _sudo systemctl status docker_ to confirm Docker is running
- Open terminal in the project folder
- Run the command _sudo docker build -t [NAME_OF_IMAGE] ._ to create the Docker image. Replace _NAME_OF_IMAGE_ with a name for that image like, for example, _game-of-three-solution_
- Run the command _sudo docker run -p 8080:8080 [NAME_OF_IMAGE]_ to launch the application


### Endpoints:

The documentation of this API can be found at _http://localhost:8080/swagger-ui.html_ (Note: you need to initialise the application to access this link).
