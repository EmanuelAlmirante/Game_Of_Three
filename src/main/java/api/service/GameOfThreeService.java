package api.service;

import api.domain.Game;
import api.domain.GameInformation;
import api.domain.Play;
import api.domain.Player;
import api.exception.GameFinishedException;
import api.exception.InvalidInputException;
import api.exception.NoGameFoundException;
import api.exception.WrongPlayerTurnException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class GameOfThreeService implements GameOfThreeServiceInterface {
    private final Map<String, Game> allGamesMap = new HashMap<>();
    private Game currentGame;

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
    public Play play(String gameNumber, String playerNumber, Integer number)
            throws GameFinishedException, InvalidInputException, NoGameFoundException, WrongPlayerTurnException,
                   IOException {
        Game currentGame = allGamesMap.get("GAME " + gameNumber);

        if (currentGame != null) {
            if (!currentGame.isGameFinished()) {
                if (!("PLAYER " + playerNumber).equals(currentGame.getNextPlayer())) {
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

                sendEventToPlayers(currentGame, play);

                System.out.println("Player: " + play.getPlayerNumber() + ", " +
                                   "Number: " + play.getNumber() + ", " +
                                   "Resulting number: " + play.getResultingNumber() + ", " +
                                   "Added number: " + play.getAddedNumber());

                return play;
            } else {
                System.out.println(new GameFinishedException().getMessage());
                throw new GameFinishedException();
            }
        }
        System.out.println(new NoGameFoundException().getMessage());
        throw new NoGameFoundException();
    }

    private boolean isFirstPlay(Game currentGame) {
        return currentGame.getLastPlay() == null;
    }

    private boolean isValidPlay(Game currentGame, Integer number) {
        if (number != (currentGame.getLastPlay() + 1)
            && number != (currentGame.getLastPlay() - 1)
            && number != (currentGame.getLastPlay())) {
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

        sendEventToPlayers(currentGame, finalPlay);
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

    private void sendEventToPlayers(Game currentGame, Play play) throws IOException {
        currentGame.getSseEmitterPlayerOne().send(play);
        currentGame.getSseEmitterPlayerTwo().send(play);
    }

    private void closeSseEmitters(Game currentGame) {
        currentGame.getSseEmitterPlayerOne().complete();
        currentGame.getSseEmitterPlayerTwo().complete();
    }
}
