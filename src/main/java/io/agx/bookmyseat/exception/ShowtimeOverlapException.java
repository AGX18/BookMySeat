package io.agx.bookmyseat.exception;

public class ShowtimeOverlapException extends RuntimeException {
    public ShowtimeOverlapException() {
        super("Showtime overlaps with an existing showtime on this screen");
    }
}