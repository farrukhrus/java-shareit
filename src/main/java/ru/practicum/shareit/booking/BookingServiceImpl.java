package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingCreateDto bco) {
        User booker = findUserById(userId);
        Item item = itemRepository.findById(bco.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("You can not rent your item");
        }

        if (!item.isAvailable()) {
            throw new IllegalStateException("Item is not available");
        }

        Booking booking = bookingMapper.toBooking(bco, item, booker);
        if (!booking.getStart().isBefore(booking.getEnd())) {
            throw new NotFoundException("Booking end date can not be before start date");
        }

        boolean hasOverlap = bookingRepository.existsByItemIdAndStatusAndEndAfterAndStartBefore(
                item.getId(),
                BookingStatus.APPROVED,
                booking.getStart(),
                booking.getEnd()
        );

        if (hasOverlap) {
            throw new NotFoundException("Overlapping");
        }

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto updateBookingStatus(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Only item owner can approve or reject booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new NotFoundException("Booking state must be WAITING");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        return bookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = findBookingById(bookingId);
        validateBookingAccess(userId, booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByState(Long userId, BookingState state) {
        User user = findUserById(userId);
        return findBookings(userId, state).stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsForOwner(Long ownerId, BookingState state) {
        findUserById(ownerId);
        List<Long> itemIds = itemRepository.findAllByOwnerId(ownerId).stream()
                .map(Item::getId)
                .toList();
        if (itemIds.isEmpty()) {
            return Collections.emptyList();
        }

        return findBookingsForOwner(itemIds, state).stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }


    private List<Booking> findBookings(Long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                return bookingRepository.findCurrentBookingsByBooker(userId, now);
            case PAST:
                return bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE:
                return bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING:
                return bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findBookingsByBookerAndStatus(userId, BookingStatus.REJECTED);
            case ALL:
                return bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            default:
                throw new IllegalArgumentException("Item has unknown state");
        }
    }

    private List<Booking> findBookingsForOwner(List<Long> itemIds, BookingState state) {
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                return bookingRepository.findCurrentBookingsByOwner(itemIds, now);
            case PAST:
                return bookingRepository.findAllByItemIdInAndEndBeforeOrderByStartDesc(itemIds, now);
            case FUTURE:
                return bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(itemIds, now);
            case WAITING:
                return bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.WAITING);
            case REJECTED:
                return bookingRepository.findAllByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.REJECTED);
            case ALL:
                return bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds);
            default:
                throw new IllegalArgumentException("Item has unknown state");
        }
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Booking findBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
    }

    private void validateBookingAccess(Long userId, Booking booking) {
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("User has no access to booking");
        }
    }
}