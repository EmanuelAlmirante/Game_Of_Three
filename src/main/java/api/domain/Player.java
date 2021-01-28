package api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Player {
    PLAYER_ONE("PLAYER 1"),
    PLAYER_TWO("PLAYER 2");

    private final String playerNumber;
}
