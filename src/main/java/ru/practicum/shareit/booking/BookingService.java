package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(Long userId, BookingCreateDto bco);

    BookingDto updateBookingStatus(Long ownerId, Long bookingId, boolean approved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getBookingsByState(Long userId, BookingState state);

    List<BookingDto> getBookingsForOwner(Long ownerId, BookingState state);
}
