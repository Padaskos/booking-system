package com.poab.booking_system;

import com.poab.booking_system.auth.UnauthorizedCredentials;
import com.poab.booking_system.booking.BookingNotFoundException;
import com.poab.booking_system.booking.UserNotAllowedException;
import com.poab.booking_system.resource.InvalidRequestException;
import com.poab.booking_system.resource.ResourceNotFoundException;
import com.poab.booking_system.resource.ResourceUnavailableException;
import com.poab.booking_system.user.EmailAlreadyExistsConflict;
import com.poab.booking_system.user.UserNotFoundException;
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
