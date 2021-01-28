package api.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class GameFinishedException extends RestException {
    public GameFinishedException() {
        super("This game is finished.", HttpStatus.BAD_REQUEST);
    }
}
