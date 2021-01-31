# Game Of Three


### Information:

When testing this exercise with Postman, events (SSE) did not show in real-time to the players. This meant that the information about the game was not available to the players and it would be impossible for them to play. For this reason I decided to print the information about the game to the console, so that the players could play the game. This is not a very clean and efficient solution. For that I would have to build a frontend that would consume the events and print the information about the game everytime a new event occurred. 

The game allows both players to play manually or to have one or both players play in an automatic way. 

Only unit tests of the service layer were created. For a more correct solution, tests to the controller layer should have also been created.


### Tech Stack:

- Java 11
- Spring Boot
- Swagger
- JUnit5
- Docker
- Apache Kafka


### Setup:

#### Without Docker:

- The first step is to install Apache Kafka (these are the necessary steps to do it in Linux Ubuntu):
   - Download the latest version of Apache Kafka - https://www.apache.org/dyn/closer.cgi?path=/kafka/2.7.0/kafka_2.13-2.7.0.tgz
   - Open a terminal in the folder to where the file was downloaded
   - Extract it _tar -xzf kafka_2.13-2.7.0.tgz_
   - Change the directory _cd kafka_2.13-2.7.0_
   - Start the ZooKeeper service _bin/zookeeper-server-start.sh config/zookeeper.properties_
   - Open other terminal in the Apache Kafka directory and start the Kafka broker service _bin/kafka-server-start.sh config/server.properties_
   - Open other terminal in the Apache Kafka directory and create the necessary topic _bin/kafka-topics.sh --create --topic automatic-play --bootstrap-server localhost:9092_  
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
- Run the command _docker-compose up -d_ to launch the containers with the necessary services to run Apache Kafka 
- Run the command _mvn spring-boot:run_ to launch the application


### Endpoints:

The documentation of this API can be found at _http://localhost:8080/swagger-ui.html_ (Note: you need to initialise the application to access this link).

The flow of the game is the following:

- Players register by making a _POST_ request to _http://localhost:8080/api/takeaway/game/start_;
- Players make a play by making a _POST_ request to _http://localhost:8080/api/takeaway/game/manualPlay/{gameNumber}/{playerNumber}/{number}_, where _gameNumber_ corresponds to the number of the game being played, _playerNumber_ corresponds to the player that is going to make the play, and _number_ corresponds to the number to be played;
- If a player wants to play automatically, it should make a _POST_ request to _http://localhost:8080/api/takeaway/game/automaticPlay/{gameNumber}/{playerNumber}, where _gameNumber_ corresponds to the number of the game being played, and _playerNumber_ corresponds to the player that is going to make the play.
