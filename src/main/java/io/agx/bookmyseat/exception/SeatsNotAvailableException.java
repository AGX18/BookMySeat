package io.agx.bookmyseat.exception;

public class SeatsNotAvailableException extends RuntimeException {
    public SeatsNotAvailableException() {
        super("One or more seats are not available");
    }
}