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
    public ResponseEntity<String> handleUserNotAllowed(UserNotAllowedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<String> handleBookingNotFound(BookingNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ResourceUnavailableException.class)
    public ResponseEntity<String> handleResourceUnavailable(ResourceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());

    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedCredentials.class)
    public ResponseEntity<String> handleUnauthorizedCredentials(UnauthorizedCredentials ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }
}
