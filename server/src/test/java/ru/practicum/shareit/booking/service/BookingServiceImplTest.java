package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import ru.practicum.shareit.booking.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private User user1;
    private User user2;
    private User user;
    private Item item;
    private Item item1;
    private Item item2;
    private LocalDateTime now;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingCreateDto bookingCreateDto;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        start = now.plusDays(1);
        end = now.plusDays(2);

        user1 = new User(1L, "User1", "user1@email.com");
        user2 = new User(2L, "User2", "user2@email.com");
        user = new User(3L, "User", "user@email.com");

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        item1 = Item.builder()
                .id(2L)
                .name("Item1")
                .description("Description1")
                .available(true)
                .owner(user1)
                .build();

        item2 = Item.builder()
                .id(3L)
                .name("Item2")
                .description("Description2")
                .available(false)
                .owner(user1)
                .build();

        bookingCreateDto = new BookingCreateDto(item1.getId(), start, end);
        booking = new Booking(1L, start, end, item1, user2, BookingStatus.WAITING);
        bookingDto = new BookingDto(1L, start, end, item1, user2, BookingStatus.WAITING);
    }

    @Test
    void testCreateBookingReturnBookingDtoWhenBookingIsSuccessful() {
        item1.setOwner(user2);

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingMapper.toBooking(bookingCreateDto, item1, user1)).thenReturn(booking);
        Mockito.when(bookingRepository.save(booking)).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.createBooking(user1.getId(), bookingCreateDto);

        assertEquals(bookingDto.getId(), result.getId());
        assertEquals(bookingDto.getStart(), result.getStart());
        assertEquals(bookingDto.getEnd(), result.getEnd());
        assertEquals(bookingDto.getItem().getId(), result.getItem().getId());
        assertEquals(bookingDto.getBooker().getId(), result.getBooker().getId());
    }

    @Test
    void testCreateBookingThrowNotFoundExceptionWhenBookerTriesToBookOwnItem() {
        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user1.getId(), bookingCreateDto));

        assertEquals("You can not rent your item", exception.getMessage());
    }

    @Test
    void testCreateBookingThrowIllegalStateExceptionWhenItemIsNotAvailable() {
        BookingCreateDto dto = new BookingCreateDto(item2.getId(), start, end);

        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item2.getId())).thenReturn(Optional.of(item2));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(user2.getId(), dto));

        assertEquals("Item is not available", exception.getMessage());
    }

    @Test
    void testCreateBookingThrowNotFoundExceptionWhenStartTimeIsAfterEndTime() {
        BookingCreateDto dto = new BookingCreateDto(item1.getId(), end, start);

        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingMapper.toBooking(dto, item1, user2))
                .thenReturn(new Booking(null, dto.getStart(), dto.getEnd(), item1, user2, null));

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user2.getId(), dto));

        assertEquals("Booking end date can not be before start date", ex.getMessage());
    }

    @Test
    void testCreateBookingThrowNotFoundExceptionWhenBookingTimesOverlap() {
        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingMapper.toBooking(bookingCreateDto, item1, user2)).thenReturn(booking);
        Mockito.when(bookingRepository.existsByItemIdAndStatusAndEndAfterAndStartBefore(
                item1.getId(), BookingStatus.APPROVED, start, end)).thenReturn(true);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user2.getId(), bookingCreateDto));

        assertEquals("Overlapping", ex.getMessage());
    }

    @Test
    void testUpdateBookingStatusThrowIllegalStateExceptionWhenUserIsNotOwner() {
        item1.setOwner(user2);

        Booking testBooking = new Booking(1L, start, end, item1, user1, BookingStatus.WAITING);
        Mockito.when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bookingService.updateBookingStatus(user1.getId(), testBooking.getId(), true));

        assertEquals("Only item owner can approve or reject booking", exception.getMessage());
    }

    @Test
    void testUpdateBookingStatusThrowNotFoundExceptionWhenBookingNotFound() {
        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(item1.getOwner().getId(), 99L, true));
    }

    @Test
    void testGetBookingByIdReturnBookingDtoWhenUserHasAccess() {
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(bookingMapper.toBookingDto(Mockito.any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(user2.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(bookingDto.getId(), result.getId());
    }

    @Test
    void testGetBookingsByStateReturnBookingsWhenStateIsAll() {
        List<Booking> bookings = List.of(booking);

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findAllByBookerIdOrderByStartDesc(user1.getId())).thenReturn(bookings);
        Mockito.when(bookingMapper.toBookingDto(Mockito.any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsByState(user1.getId(), BookingState.ALL);

        assertEquals(1, result.size());
        assertEquals(bookingDto.getId(), result.get(0).getId());
    }

    @Test
    void testGetBookingsForOwnerReturnBookingsWhenStateIsFuture() {
        List<Booking> bookings = List.of(booking);

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1, item2));
        Mockito.when(bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(Mockito.anyList(), Mockito.any()))
                .thenReturn(bookings);
        Mockito.when(bookingMapper.toBookingDto(Mockito.any())).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getBookingsForOwner(user1.getId(), BookingState.FUTURE);

        assertEquals(1, result.size());
        assertEquals(bookingDto.getId(), result.get(0).getId());
    }

    @Test
    void testGetBookingsForOwnerReturnEmptyListWhenNoItemsExist() {
        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(Collections.emptyList());

        List<BookingDto> result = bookingService.getBookingsForOwner(user1.getId(), BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        Mockito.verifyNoInteractions(bookingRepository);
    }

    @Test
    void testReturnBookingWhenExists() {
        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(bookingMapper.toBookingDto(Mockito.any())).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(booking.getBooker().getId(), booking.getId());
        assertNotNull(result);
    }

    @Test
    void testThrowExceptionWhenBookingNotFound() {
        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(booking.getBooker().getId(), booking.getId()));
    }

    @Test
    void testGetAllUserBookingsWaiting() {
        Booking waitingBooking = new Booking(1L, now, now.plusHours(1), item, user, BookingStatus.WAITING);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(user.getId(), BookingStatus.WAITING))
                .thenReturn(List.of(waitingBooking));
        Mockito.when(bookingMapper.toBookingDto(Mockito.any())).thenReturn(
                new BookingDto(waitingBooking.getId(), waitingBooking.getStart(), waitingBooking.getEnd(),
                        waitingBooking.getItem(), waitingBooking.getBooker(), waitingBooking.getStatus()));

        List<BookingDto> bookings = bookingService.getBookingsByState(user.getId(), BookingState.WAITING);

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
    }

    @Test
    void testGetAllUserBookingsCurrent() {
        Long userId = 20L;
        BookingState state = BookingState.CURRENT;

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User(userId, "User", "email@example.com")));

        List<BookingDto> bookings = bookingService.getBookingsByState(userId, state);

        bookings.forEach(booking -> assertThat(booking, allOf(
                hasProperty("booker", hasProperty("id", equalTo(userId))),
                hasProperty("status", notNullValue())
        )));
    }
}
