package api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Play {
    private String playerNumber;
    private Integer number;
    private Integer resultingNumber;
    private Integer addedNumber;
    private String information;

    public Play(String playerNumber, Integer number, Integer resultingNumber, Integer addedNumber) {
        this.playerNumber = playerNumber;
        this.number = number;
        this.resultingNumber = resultingNumber;
        this.addedNumber = addedNumber;
    }
}
