package io.agx.bookmyseat.entity;

public enum Role {
    USER, // can browse movies, book seats, cancel their own bookings
    ADMIN //  can create movies, theaters, screens, showtimes, delete them etc.
}