package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;

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
        log.info("Create booking for item {} by user {}", bcd.getItemId(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(userId, bcd));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> updateBookingStatus(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                          @PathVariable Long bookingId,
                                          @RequestParam boolean approved) {
        log.info("Update booking {} by owner id {}", bookingId, ownerId);
        return ResponseEntity.ok(bookingService.updateBookingStatus(ownerId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@RequestHeader(HEADER_USER_ID) Long userId,
                                     @PathVariable Long bookingId) {
        log.info("Get booking by booking id {}", bookingId);
        return ResponseEntity.ok(bookingService.getBookingById(userId, bookingId));
    }


    @GetMapping
    public ResponseEntity<List<BookingDto>> getBookingsByState(@RequestHeader(HEADER_USER_ID) Long userId,
                                               @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Get booking of user id {} by state {}", userId, state.name());
        return ResponseEntity.ok(bookingService.getBookingsByState(userId, state));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getBookingsForOwner(@RequestHeader(HEADER_USER_ID) Long ownerId,
                                                @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Get booking for item owner id {} with status {}", ownerId, state.name());
        return ResponseEntity.ok(bookingService.getBookingsForOwner(ownerId, state));
    }
}