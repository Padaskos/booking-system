package io.github.padaskos.bookingsystem;

import io.github.padaskos.bookingsystem.auth.UnauthorizedCredentials;
import io.github.padaskos.bookingsystem.booking.BookingNotFoundException;
import io.github.padaskos.bookingsystem.booking.UserNotAllowedException;
import io.github.padaskos.bookingsystem.resource.InvalidRequestException;
import io.github.padaskos.bookingsystem.resource.ResourceNotFoundException;
import io.github.padaskos.bookingsystem.resource.ResourceUnavailableException;
import io.github.padaskos.bookingsystem.user.EmailAlreadyExistsConflict;
import io.github.padaskos.bookingsystem.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<String> handleInvalidRequest(InvalidRequestException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsConflict.class)
    public ResponseEntity<String> handleEmailAlreadyExistConflict(EmailAlreadyExistsConflict ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotAllowedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleUserNotAllowed() {}

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleBookingNotFound() {}

    @ExceptionHandler(ResourceUnavailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleResourceUnavailable() {}

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleUserNotFound() {}

    @ExceptionHandler(UnauthorizedCredentials.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleUnauthorizedCredentials() {}
}
