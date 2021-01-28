package api.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class InvalidInputException extends RestException {
    public InvalidInputException() {
        super("Your input is invalid.", HttpStatus.BAD_REQUEST);
    }
}
