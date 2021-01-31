package api.service;

import api.domain.Game;
import api.domain.GameInformation;
import api.domain.Play;
import api.domain.Player;
import api.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class GameOfThreeService implements GameOfThreeServiceInterface {
    private final Map<String, Game> allGamesMap = new HashMap<>();
    private Game currentGame;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Value("${message.topic.name}")
    private String topicName;

    @Override
    public SseEmitter startGame() throws IOException {
        if (currentGame == null) {
            currentGame = new Game("GAME " + (allGamesMap.size() + 1));
            currentGame.setNextPlayer(Player.PLAYER_ONE.getPlayerNumber());

            allGamesMap.put(currentGame.getGameNumber(), currentGame);

            GameInformation gameInformation =
                    new GameInformation(currentGame.getGameNumber(), Player.PLAYER_ONE.getPlayerNumber());
            currentGame.getSseEmitterPlayerOne().send(gameInformation);

            System.out.println(gameInformation.getGameNumber() + " " + gameInformation.getPlayerNumber());

            return currentGame.getSseEmitterPlayerOne();
        } else {
            Game game = currentGame;
            currentGame = null;

            GameInformation gameInformation =
                    new GameInformation(game.getGameNumber(), Player.PLAYER_TWO.getPlayerNumber());

            game.getSseEmitterPlayerTwo().send(gameInformation);

            System.out.println(gameInformation.getGameNumber() + " " + gameInformation.getPlayerNumber());

            return game.getSseEmitterPlayerTwo();
        }
    }

    @Override
    public Play manualPlay(String gameNumber, String playerNumber, Integer number) throws GameFinishedException,
                                                                                          InvalidInputException,
                                                                                          NoGameFoundException,
                                                                                          WrongPlayerTurnException,
                                                                                          InvalidPlayerException,
                                                                                          IOException {
        currentGame = getCurrentGame(gameNumber);

        Play play = makePlay(currentGame, playerNumber, number);

        if (atLeastOneAutomaticPlayerExists(currentGame)) {
            sendGameAndNextPlayerInformationToProducer(currentGame);
        }

        return play;
    }

    @Override
    public void automaticPlay(String gameNumber, String playerNumber) throws GameFinishedException,
                                                                             NoGameFoundException,
                                                                             WrongPlayerTurnException,
                                                                             InvalidInputException,
                                                                             InvalidPlayerException,
                                                                             IOException {
        currentGame = getCurrentGame(gameNumber);

        if (!isPlayerTurn(currentGame, playerNumber)) {
            return;
        }

        makePlayerPlayAutomatically(playerNumber);

        int number = generateNextNumberToBePlayed();

        makePlay(currentGame, playerNumber, number);

        if (bothPlayersAreAutomatic()) {
            sendGameAndNextPlayerInformationToProducer(currentGame);
        }
    }

    private Play makePlay(Game currentGame, String playerNumber, Integer number) throws GameFinishedException,
                                                                                        InvalidInputException,
                                                                                        NoGameFoundException,
                                                                                        WrongPlayerTurnException,
                                                                                        InvalidPlayerException,
                                                                                        IOException {
        if (!isValidPlayerNumber(playerNumber)) {
            System.out.println(new InvalidPlayerException().getMessage());
            throw new InvalidPlayerException();
        }

        if (currentGame == null) {
            System.out.println(new NoGameFoundException().getMessage());
            throw new NoGameFoundException();
        }

        if (currentGame.isGameFinished()) {
            System.out.println(new GameFinishedException().getMessage());
            throw new GameFinishedException();
        }

        if (!isPlayerTurn(currentGame, playerNumber)) {
            System.out.println(new WrongPlayerTurnException().getMessage());
            throw new WrongPlayerTurnException();
        }

        if (!isFirstPlay(currentGame) && !isValidPlay(currentGame, number)) {
            System.out.println(new InvalidInputException().getMessage());
            throw new InvalidInputException();
        }

        Play play = calculateNewPlay(currentGame, playerNumber, number);

        if (playerHasWon(play)) {
            return declareWinner(currentGame, play);
        }

        updateNextPlayer(currentGame, playerNumber);

        sendSseToPlayers(currentGame, play);

        System.out.println("Player: " + play.getPlayerNumber() + ", " +
                           "Number: " + play.getNumber() + ", " +
                           "Resulting number: " + play.getResultingNumber() + ", " +
                           "Added number: " + play.getAddedNumber());

        return play;
    }

    private Game getCurrentGame(String gameNumber) throws NoGameFoundException {
        currentGame = allGamesMap.get("GAME " + gameNumber);

        if (currentGame == null) {
            System.out.println(new NoGameFoundException().getMessage());
            throw new NoGameFoundException();
        }

        return currentGame;
    }

    private boolean isValidPlayerNumber(String playerNumber) {
        return ("PLAYER " + playerNumber).equals(Player.PLAYER_ONE.getPlayerNumber()) ||
               ("PLAYER " + playerNumber).equals(Player.PLAYER_TWO.getPlayerNumber());
    }

    private boolean isPlayerTurn(Game currentGame, String playerNumber) {
        return ("PLAYER " + playerNumber).equals(currentGame.getNextPlayer());
    }

    private boolean isFirstPlay(Game currentGame) {
        return currentGame.getLastPlay() == null;
    }

    private boolean isValidPlay(Game currentGame, Integer number) {
        if (number != (currentGame.getLastPlay() + 1)
            && number != (currentGame.getLastPlay() - 1)
            && !number.equals(currentGame.getLastPlay())) {
            return false;
        }

        return number % 3 == 0;
    }

    private Play calculateNewPlay(Game currentGame, String playerNumber, Integer number) {
        if (isFirstPlay(currentGame)) {
            Integer resultingNumber = number;
            Integer addedNumber = 0;

            updateLastPlay(currentGame, number);

            return new Play(playerNumber, number, resultingNumber, addedNumber);
        } else {
            Integer resultingNumber = number / 3;
            Integer addedNumber = number - currentGame.getLastPlay();

            updateLastPlay(currentGame, resultingNumber);

            return new Play(playerNumber, number, resultingNumber, addedNumber);
        }
    }

    private void updateLastPlay(Game currentGame, Integer number) {
        currentGame.setLastPlay(number);
    }

    private boolean playerHasWon(Play play) {
        return play.getResultingNumber() == 1;
    }

    private Play declareWinner(Game currentGame, Play play) throws IOException {
        currentGame.setGameFinished();

        Play finalPlay = new Play(play.getPlayerNumber(), play.getNumber(), play.getResultingNumber(),
                                  play.getAddedNumber(), "Player " + play.getPlayerNumber() + " is the winner!");

        sendSseToPlayers(currentGame, finalPlay);
        closeSseEmitters(currentGame);

        System.out.println("Player: " + finalPlay.getPlayerNumber() + ", " +
                           "Number: " + finalPlay.getNumber() + ", " +
                           "Resulting number: " + finalPlay.getResultingNumber() + ", " +
                           "Added number: " + finalPlay.getAddedNumber() + ", " +
                           finalPlay.getInformation());

        return finalPlay;
    }

    private void updateNextPlayer(Game currentGame, String playerNumber) {
        if (("PLAYER " + playerNumber).equals(Player.PLAYER_TWO.getPlayerNumber())) {
            currentGame.setNextPlayer(Player.PLAYER_ONE.getPlayerNumber());
        } else {
            currentGame.setNextPlayer(Player.PLAYER_TWO.getPlayerNumber());
        }
    }

    private boolean atLeastOneAutomaticPlayerExists(Game currentGame) {
        return !currentGame.isGameFinished() && (currentGame.isPlayerOneAutomatic() || currentGame
                .isPlayerTwoAutomatic());
    }

    private void sendGameAndNextPlayerInformationToProducer(Game currentGame) {
        String gameNumberInformation = currentGame.getGameNumber().split(" ")[1];
        String nextPlayerNumberInformation = currentGame.getNextPlayer().split(" ")[1];
        String gameAndNextPlayerInformation = gameNumberInformation + " " + nextPlayerNumberInformation;

        automaticPlayProducer(gameAndNextPlayerInformation);
    }

    private void makePlayerPlayAutomatically(String playerNumber) {
        if (!currentGame.isPlayerOneAutomatic() && ("PLAYER " + playerNumber)
                .equals(Player.PLAYER_ONE.getPlayerNumber())) {
            currentGame.setPlayerOneAutomatic();
        } else if (!currentGame.isPlayerTwoAutomatic() && ("PLAYER " + playerNumber)
                .equals(Player.PLAYER_TWO.getPlayerNumber())) {
            currentGame.setPlayerTwoAutomatic();
        }
    }

    private int generateNextNumberToBePlayed() {
        int number;

        if (isFirstPlay(currentGame)) {
            number = new Random().ints(3, 1000).findFirst().getAsInt();
        } else {
            if ((currentGame.getLastPlay() + 1) % 3 == 0) {
                number = currentGame.getLastPlay() + 1;
            } else if ((currentGame.getLastPlay() - 1) % 3 == 0) {
                number = currentGame.getLastPlay() - 1;
            } else {
                number = currentGame.getLastPlay();
            }
        }

        return number;
    }

    private boolean bothPlayersAreAutomatic() {
        return !currentGame.isGameFinished() && currentGame.isPlayerOneAutomatic() && currentGame
                .isPlayerTwoAutomatic();
    }

    private void sendSseToPlayers(Game currentGame, Play play) throws IOException {
        currentGame.getSseEmitterPlayerOne().send(play);
        currentGame.getSseEmitterPlayerTwo().send(play);
    }

    private void closeSseEmitters(Game currentGame) {
        currentGame.getSseEmitterPlayerOne().complete();
        currentGame.getSseEmitterPlayerTwo().complete();
    }

    private void automaticPlayProducer(String gameAndPlayerInformation) {
        kafkaTemplate.send(topicName, gameAndPlayerInformation);
    }

    @KafkaListener(topics = "${message.topic.name}", groupId = "${group.id}")
    private void automaticPlayConsumer(String gameAndPlayerInformation) throws WrongPlayerTurnException,
                                                                               InvalidPlayerException,
                                                                               InvalidInputException,
                                                                               IOException,
                                                                               GameFinishedException,
                                                                               NoGameFoundException {
        String gameNumber = gameAndPlayerInformation.split(" ")[0];
        String playerNumber = gameAndPlayerInformation.split(" ")[1];

        automaticPlay(gameNumber, playerNumber);
    }
}
