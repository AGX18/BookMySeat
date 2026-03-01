package io.agx.bookmyseat.exception;

public class ShowtimeAlreadyStartedException extends RuntimeException {
    public ShowtimeAlreadyStartedException() {
        super("Cannot perform this action on a showtime that has already started");
    }
}