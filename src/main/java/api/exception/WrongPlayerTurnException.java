package api.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class WrongPlayerTurnException extends RestException {
    public WrongPlayerTurnException() {
        super("It is not your turn.", HttpStatus.FORBIDDEN);
    }
}
