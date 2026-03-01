package io.agx.bookmyseat.exception;

public class BookingAlreadyCancelledException extends RuntimeException {
    public BookingAlreadyCancelledException() {
        super("Booking is already cancelled");
    }
}