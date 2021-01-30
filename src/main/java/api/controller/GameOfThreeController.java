package api.controller;

import api.domain.Play;
import api.exception.*;
import api.service.GameOfThreeServiceInterface;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping(value = "api/takeaway/game")
public class GameOfThreeController {
    private final GameOfThreeServiceInterface gameOfThreeServiceInterface;

    public GameOfThreeController(GameOfThreeServiceInterface gameOfThreeServiceInterface) {
        this.gameOfThreeServiceInterface = gameOfThreeServiceInterface;
    }

    @PostMapping(path = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public SseEmitter startGame() throws IOException {
        return gameOfThreeServiceInterface.startGame();
    }

    @PostMapping("/makePlay/{gameNumber}/{playerNumber}/{number}")
    @ResponseStatus(HttpStatus.OK)
    public Play makePlay(@PathVariable @NonNull String gameNumber,
                         @PathVariable @NonNull String playerNumber,
                         @PathVariable @NonNull Integer number) throws GameFinishedException,
                                                                       InvalidInputException,
                                                                       NoGameFoundException,
                                                                       WrongPlayerTurnException,
                                                                       InvalidPlayerException,
                                                                       IOException {
        return gameOfThreeServiceInterface.play(gameNumber, playerNumber, number);
    }

    @PostMapping("/automaticPlay/{gameNumber}/{playerNumber}")
    public void automaticPlay(@PathVariable @NonNull String gameNumber,
                              @PathVariable @NonNull String playerNumber) throws GameFinishedException,
                                                                                 NoGameFoundException,
                                                                                 WrongPlayerTurnException,
                                                                                 InvalidInputException,
                                                                                 InvalidPlayerException,
                                                                                 IOException {
        gameOfThreeServiceInterface.automaticPlay(gameNumber, playerNumber);
    }
}
