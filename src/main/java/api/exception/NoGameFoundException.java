package api.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class NoGameFoundException extends RestException {
    public NoGameFoundException() {
        super("No game found!", HttpStatus.NOT_FOUND);
    }
}
