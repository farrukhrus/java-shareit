package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingDto> createBooking(@RequestHeader(HEADER_USER_ID) Long userId,
                                    @RequestBody BookingCreateDto bcd) {
        log.debug("Create booking for item {} by user {}", bcd.getItemId(), userId);
        BookingDto bd = bookingService.createBooking(userId, bcd);
        return ResponseEntity.ok(bd);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                          @PathVariable Long bookingId,
                                          @RequestParam boolean approved) {
        log.debug("Update booking {} by owner id {}", bookingId, ownerId);
        BookingDto bd = bookingService.updateBookingStatus(ownerId, bookingId, approved);
        return ResponseEntity.ok(bd);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@RequestHeader(HEADER_USER_ID) Long userId,
                                     @PathVariable Long bookingId) {
        log.debug("Get booking by booking id {}", bookingId);
        BookingDto bd =bookingService.getBookingById(userId, bookingId);
        return ResponseEntity.ok(bd);
    }


    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsByState(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @RequestParam(defaultValue = "ALL") BookingState state) {
        log.debug("Get booking of user id {} by state {}", userId, state.name());
        List<BookingDto> bd = bookingService.getBookingsByState(userId, state);
        return ResponseEntity.ok(bd);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsForOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                @RequestParam(defaultValue = "ALL") BookingState state) {
        log.debug("Get booking for item owner id {} with status {}", ownerId, state.name());
        List<BookingDto> bd = bookingService.getBookingsForOwner(ownerId, state);
        return ResponseEntity.ok(bd);
    }
}