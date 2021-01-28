package api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@NoArgsConstructor
public class Game {
    @Setter
    @Getter
    private String gameNumber;
    @Setter
    @Getter
    private SseEmitter sseEmitterPlayerOne;
    @Setter
    @Getter
    private SseEmitter sseEmitterPlayerTwo;
    @Setter
    @Getter
    private String nextPlayer;
    @Setter
    @Getter
    private Integer lastPlay;
    @Getter
    private boolean playerOneAutomatic;
    @Getter
    private boolean playerTwoAutomatic;
    @Getter
    private boolean gameFinished;

    public Game(String gameNumber) {
        this.gameNumber = gameNumber;
        this.setSseEmitterPlayerOne(new SseEmitter(-1L));
        this.setSseEmitterPlayerTwo(new SseEmitter(-1L));
    }

    public void setGameFinished() {
        this.gameFinished = true;
    }

    public void setPlayerOneAutomatic() {
        this.playerOneAutomatic = true;
    }

    public void setPlayerTwoAutomatic() {
        this.playerTwoAutomatic = true;
    }
}
