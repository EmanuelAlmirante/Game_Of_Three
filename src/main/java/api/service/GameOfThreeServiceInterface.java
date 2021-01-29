package api.service;

import api.domain.Play;
import api.exception.GameFinishedException;
import api.exception.InvalidInputException;
import api.exception.NoGameFoundException;
import api.exception.WrongPlayerTurnException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface GameOfThreeServiceInterface {
    SseEmitter startGame() throws IOException;

    Play play(String gameNumber, String playerNumber, Integer number)
            throws GameFinishedException, InvalidInputException, NoGameFoundException, WrongPlayerTurnException,
                   IOException;

    void automaticPlay(String gameNumber, String playerNumber)
            throws GameFinishedException, NoGameFoundException, WrongPlayerTurnException, InvalidInputException,
                   IOException;
}
