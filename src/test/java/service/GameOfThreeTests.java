package service;

import api.domain.Play;
import api.exception.GameFinishedException;
import api.exception.InvalidInputException;
import api.exception.NoGameFoundException;
import api.exception.WrongPlayerTurnException;
import api.service.GameOfThreeService;
import api.service.GameOfThreeServiceInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

public class GameOfThreeTests {
    private final GameOfThreeServiceInterface gameOfThreeServiceInterface = new GameOfThreeService();
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void playerOneStartGameSuccessfully() throws IOException {
        // Arrange & Act
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();

        // Assert
        assertNotNull(sseEmitterPlayerOne);

        assertEquals("GAME 1 PLAYER 1", outputStreamCaptor.toString().trim());
    }

    @Test
    public void playerTwoJoinGameSuccessfully() throws IOException {
        // Arrange
        gameOfThreeServiceInterface.startGame();

        // Act
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        // Assert
        assertNotNull(sseEmitterPlayerTwo);

        assertEquals("GAME 1 PLAYER 1\nGAME 1 PLAYER 2", outputStreamCaptor.toString().trim());
    }

    @Test
    public void playerOneMakesFirstPlaySuccessfully()
            throws IOException, GameFinishedException, NoGameFoundException, WrongPlayerTurnException,
                   InvalidInputException {
        // Arrange
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        String gameNumber = "1";
        String playerNumber = "1";
        Integer number = 10;

        // Act
        Play play = gameOfThreeServiceInterface.play(gameNumber, playerNumber, number);

        // Assert
        assertNotNull(sseEmitterPlayerOne);
        assertNotNull(sseEmitterPlayerTwo);

        assertEquals(playerNumber, play.getPlayerNumber());
        assertEquals(number, play.getNumber());
        assertEquals(number, play.getResultingNumber());
        assertEquals(0, play.getAddedNumber());

        assertEquals("GAME 1 PLAYER 1\nGAME 1 PLAYER 2\nPlayer: 1, Number: 10, Resulting number: 10, Added number: 0",
                     outputStreamCaptor.toString().trim());
    }

    @Test
    public void playerMakesPlayAndWinsGameSuccessfully()
            throws IOException, GameFinishedException, NoGameFoundException, WrongPlayerTurnException,
                   InvalidInputException {
        // Arrange
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        String gameNumber = "1";
        String playerOneNumber = "1";
        String playerTwoNumber = "2";
        Integer numberPlayOne = 10;
        Integer numberPlayTwo = 9;
        Integer numberPlayThree = 3;

        // Act
        Play playOne = gameOfThreeServiceInterface.play(gameNumber, playerOneNumber, numberPlayOne);
        Play playTwo = gameOfThreeServiceInterface.play(gameNumber, playerTwoNumber, numberPlayTwo);
        Play playThree = gameOfThreeServiceInterface.play(gameNumber, playerOneNumber, numberPlayThree);


        // Assert
        assertNotNull(sseEmitterPlayerOne);
        assertNotNull(sseEmitterPlayerTwo);

        assertEquals(playerOneNumber, playOne.getPlayerNumber());
        assertEquals(numberPlayOne, playOne.getNumber());
        assertEquals(numberPlayOne, playOne.getResultingNumber());
        assertEquals(0, playOne.getAddedNumber());

        assertEquals(playerTwoNumber, playTwo.getPlayerNumber());
        assertEquals(numberPlayTwo, playTwo.getNumber());
        assertEquals(3, playTwo.getResultingNumber());
        assertEquals(-1, playTwo.getAddedNumber());

        assertEquals(playerOneNumber, playThree.getPlayerNumber());
        assertEquals(numberPlayThree, playThree.getNumber());
        assertEquals(1, playThree.getResultingNumber());
        assertEquals(0, playThree.getAddedNumber());

        assertEquals(
                "GAME 1 PLAYER 1\nGAME 1 PLAYER 2\nPlayer: 1, Number: 10, Resulting number: 10, Added number: 0\n"
                + "Player: 2, Number: 9, Resulting number: 3, Added number: -1\n"
                + "Player: 1, Number: 3, Resulting number: 1, Added number: 0, Player 1 is the winner!",
                outputStreamCaptor.toString().trim());
    }

    @Test
    public void playerMakesPlayInNonExistingGameFails() throws IOException {
        // Arrange
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        String gameNumber = "2";
        String playerNumber = "1";
        Integer number = 10;

        // Act & Assert
        assertNotNull(sseEmitterPlayerOne);
        assertNotNull(sseEmitterPlayerTwo);

        assertThrows(NoGameFoundException.class, () -> {
            gameOfThreeServiceInterface.play(gameNumber, playerNumber, number);
        });
    }

    @Test
    public void playerMakesPlayInWrongTurnFails() throws IOException {
        // Arrange
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        String gameNumber = "1";
        String playerNumber = "2";
        Integer number = 10;

        // Act & Assert
        assertNotNull(sseEmitterPlayerOne);
        assertNotNull(sseEmitterPlayerTwo);

        assertThrows(WrongPlayerTurnException.class, () -> {
            gameOfThreeServiceInterface.play(gameNumber, playerNumber, number);
        });
    }

    @Test
    public void playerMakesPlayWhenGameIsFinishedFails()
            throws IOException, GameFinishedException, NoGameFoundException, WrongPlayerTurnException,
                   InvalidInputException {
        // Arrange
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        String gameNumber = "1";
        String playerOneNumber = "1";
        String playerTwoNumber = "2";
        Integer numberPlayOne = 3;
        Integer numberPlayTwo = 3;
        Integer numberPlayThree = 1;

        // Act
        gameOfThreeServiceInterface.play(gameNumber, playerOneNumber, numberPlayOne);
        gameOfThreeServiceInterface.play(gameNumber, playerTwoNumber, numberPlayTwo);

        // Assert
        assertNotNull(sseEmitterPlayerOne);
        assertNotNull(sseEmitterPlayerTwo);

        assertThrows(GameFinishedException.class, () -> {
            gameOfThreeServiceInterface.play(gameNumber, playerOneNumber, numberPlayThree);
        });
    }

    @Test
    public void playerMakesPlayWithInvalidInputFails()
            throws IOException, GameFinishedException, NoGameFoundException, WrongPlayerTurnException,
                   InvalidInputException {
        // Arrange
        SseEmitter sseEmitterPlayerOne = gameOfThreeServiceInterface.startGame();
        SseEmitter sseEmitterPlayerTwo = gameOfThreeServiceInterface.startGame();

        String gameNumber = "1";
        String playerOneNumber = "1";
        String playerTwoNumber = "2";
        Integer numberPlayOne = 3;
        Integer numberPlayTwo = 5;

        // Act
        gameOfThreeServiceInterface.play(gameNumber, playerOneNumber, numberPlayOne);

        // Assert
        assertNotNull(sseEmitterPlayerOne);
        assertNotNull(sseEmitterPlayerTwo);

        assertThrows(InvalidInputException.class, () -> {
            gameOfThreeServiceInterface.play(gameNumber, playerTwoNumber, numberPlayTwo);
        });
    }
}
