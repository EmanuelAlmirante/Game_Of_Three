package api.exception;

import org.springframework.http.HttpStatus;

public class InvalidPlayerException extends RestException {
    public InvalidPlayerException() {
        super("This player is invalid.", HttpStatus.BAD_REQUEST);
    }
}
