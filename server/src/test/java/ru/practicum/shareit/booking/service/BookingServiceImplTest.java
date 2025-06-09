package ru.practicum.shareit.booking.service;

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

    private final User user1 = new User(1L, "User1", "user1@email.com");
    private final User user2 = new User(2L, "User2", "user2@email.com");
    private final User user = new User(1L, "User", "user@email.com");
    private final Item item = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .build();
    private final Item item1 = Item.builder()
            .id(1L)
            .name("Item")
            .description("Description")
            .available(true)
            .owner(user1)
            .build();
    private final Item item2 = Item.builder()
            .id(1L)
            .name("Item2")
            .description("Description2")
            .available(false)
            .owner(user1)
            .build();

    @Test
    void testCreateBookingReturnBookingDtoWhenBookingIsSuccessful() {
        BookingCreateDto dto = new BookingCreateDto(item1.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        item1.setOwner(user2);

        Booking booking = new Booking(1L, dto.getStart(), dto.getEnd(), item1, user1, BookingStatus.WAITING);
        BookingDto expected = new BookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
                booking.getItem(), booking.getBooker(), booking.getStatus());

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingMapper.toBooking(dto, item1, user1)).thenReturn(booking);
        Mockito.when(bookingRepository.save(booking)).thenReturn(booking);
        Mockito.when(bookingMapper.toBookingDto(booking)).thenReturn(expected);

        BookingDto result = bookingService.createBooking(user1.getId(), dto);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getStart(), result.getStart());
        assertEquals(expected.getEnd(), result.getEnd());
        assertEquals(expected.getItem().getId(), result.getItem().getId());
        assertEquals(expected.getBooker().getId(), result.getBooker().getId());
    }

    @Test
    void testCreateBookingThrowNotFoundExceptionWhenBookerTriesToBookOwnItem() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto(item1.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));

        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(user1.getId(), bookingCreateDto));

        assertEquals("You can not rent your item", exception.getMessage());
    }

    @Test
    void testCreateBookingThrowIllegalStateExceptionWhenItemIsNotAvailable() {
        BookingCreateDto bookingCreateDto = new BookingCreateDto(item2.getId(), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));


        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item2.getId())).thenReturn(Optional.of(item2));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                bookingService.createBooking(user2.getId(), bookingCreateDto));

        assertEquals("Item is not available", exception.getMessage());
    }

    @Test
    void testCreateBookingThrowNotFoundExceptionWhenStartTimeIsAfterEndTime() {
        BookingCreateDto dto = new BookingCreateDto(item1.getId(),
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        Booking booking = new Booking(null, dto.getStart(), dto.getEnd(), item1, user2, null);

        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingMapper.toBooking(dto, item1, user2)).thenReturn(booking);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user2.getId(), dto));

        assertEquals("Booking end date can not be before start date", ex.getMessage());
    }

    @Test
    void testCreateBookingThrowNotFoundExceptionWhenBookingTimesOverlap() {
        BookingCreateDto dto = new BookingCreateDto(item1.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        Booking booking = new Booking(null, dto.getStart(), dto.getEnd(), item1, user2, BookingStatus.WAITING);

        Mockito.when(userRepository.findById(user2.getId())).thenReturn(Optional.of(user2));
        Mockito.when(itemRepository.findById(item1.getId())).thenReturn(Optional.of(item1));
        Mockito.when(bookingMapper.toBooking(dto, item1, user2)).thenReturn(booking);
        Mockito.when(bookingRepository.existsByItemIdAndStatusAndEndAfterAndStartBefore(
                        item1.getId(), BookingStatus.APPROVED, dto.getStart(), dto.getEnd()))
                .thenReturn(true);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(user2.getId(), dto));

        assertEquals("Overlapping", ex.getMessage());
    }

    @Test
    void testUpdateBookingStatusThrowIllegalStateExceptionWhenUserIsNotOwner() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);

        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bookingService.updateBookingStatus(user2.getId(), booking.getId(), true)
        );

        assertEquals("Only item owner can approve or reject booking", exception.getMessage());
        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }

    @Test
    void testUpdateBookingStatusThrowNotFoundExceptionWhenBookingNotFound() {
        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> bookingService.updateBookingStatus(item1.getOwner().getId(), 99L, true)
        );

        Mockito.verify(bookingRepository, Mockito.never()).save(Mockito.any(Booking.class));
    }


    @Test
    void testGetBookingByIdReturnBookingDtoWhenUserHasAccess() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);
        BookingDto expectedBookingDto = new BookingDto(
                1L, booking.getStart(), booking.getEnd(), item, user, BookingStatus.WAITING);

        Mockito.when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        Mockito.when(bookingMapper.toBookingDto(Mockito.any(Booking.class)))
                .thenReturn(expectedBookingDto);

        BookingDto result = bookingService.getBookingById(user1.getId(), booking.getId());

        assertNotNull(result);
        assertEquals(expectedBookingDto.getId(), result.getId());
        assertEquals(expectedBookingDto.getStart(), result.getStart());
        assertEquals(expectedBookingDto.getEnd(), result.getEnd());
        assertEquals(expectedBookingDto.getStatus(), result.getStatus());
        assertEquals(expectedBookingDto.getBooker().getId(), result.getBooker().getId());
        assertEquals(expectedBookingDto.getItem().getId(), result.getItem().getId());

        Mockito.verify(bookingRepository).findById(booking.getId());
    }

    @Test
    void testGetBookingsByStateReturnBookingsWhenStateIsAll() {
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING),
                new Booking(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), item2, user1, BookingStatus.APPROVED)
        );

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(bookingRepository.findAllByBookerIdOrderByStartDesc(user1.getId())).thenReturn(bookings);
        Mockito.when(bookingMapper.toBookingDto(Mockito.any()))
                .thenAnswer(inv -> {
                    Booking b = inv.getArgument(0);
                    return new BookingDto(b.getId(), b.getStart(), b.getEnd(), b.getItem(), b.getBooker(), b.getStatus());
                });

        List<BookingDto> result = bookingService.getBookingsByState(user1.getId(), BookingState.ALL);

        assertEquals(2, result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
    }

    @Test
    void testGetBookingsForOwnerReturnBookingsWhenStateIsFuture() {
        List<Booking> bookings = List.of(
                new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user2, BookingStatus.WAITING),
                new Booking(2L, LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(4), item2, user2, BookingStatus.APPROVED)
        );

        Mockito.when(userRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        Mockito.when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(List.of(item1, item2));
        Mockito.when(bookingRepository.findAllByItemIdInAndStartAfterOrderByStartDesc(Mockito.anyList(), Mockito.any()))
                .thenReturn(bookings);
        Mockito.when(bookingMapper.toBookingDto(Mockito.any()))
                .thenAnswer(inv -> {
                    Booking b = inv.getArgument(0);
                    return new BookingDto(b.getId(), b.getStart(), b.getEnd(), b.getItem(), b.getBooker(), b.getStatus());
                });

        List<BookingDto> result = bookingService.getBookingsForOwner(user1.getId(), BookingState.FUTURE);

        assertEquals(2, result.size());
        assertEquals(bookings.get(0).getId(), result.get(0).getId());
        assertEquals(bookings.get(1).getId(), result.get(1).getId());
    }

    @Test
    void testGetBookingsForOwnerReturnEmptyListWhenNoItemsExist() {
        Mockito.when(userRepository.findById(user1.getId()))
                .thenReturn(Optional.of(user1));

        Mockito.when(itemRepository.findAllByOwnerId(user1.getId())).thenReturn(Collections.emptyList());

        List<BookingDto> result = bookingService.getBookingsForOwner(user1.getId(), BookingState.ALL);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        Mockito.verify(userRepository).findById(user1.getId());
        Mockito.verify(itemRepository).findAllByOwnerId(user1.getId());
        Mockito.verifyNoInteractions(bookingRepository);
    }

    @Test
    void testReturnBookingWhenExists() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);

        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(booking));
        Mockito.when(bookingMapper.toBookingDto(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> {
                    Booking b = invocation.getArgument(0);
                    return new BookingDto(b.getId(), b.getStart(), b.getEnd(), b.getItem(), b.getBooker(), b.getStatus());
                });

        BookingDto result = bookingService.getBookingById(booking.getBooker().getId(), booking.getId());
        assertNotNull(result);
    }

    @Test
    void testThrowExceptionWhenBookingNotFound() {
        Booking booking = new Booking(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), item1, user1, BookingStatus.WAITING);

        Mockito.when(bookingRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(booking.getBooker().getId(), booking.getId()));
    }

    @Test
    void testGetAllUserBookingsWaiting() {
        Booking booking = new Booking(1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), item, user, BookingStatus.WAITING);

        Mockito.when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        Mockito.when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(user.getId(), BookingStatus.WAITING))
                .thenReturn(List.of(booking));
        Mockito.when(bookingMapper.toBookingDto(Mockito.any(Booking.class)))
                .thenAnswer(inv -> {
                    Booking b = inv.getArgument(0);
                    return new BookingDto(b.getId(), b.getStart(), b.getEnd(), b.getItem(), b.getBooker(), b.getStatus());
                });

        List<BookingDto> bookings = bookingService.getBookingsByState(user.getId(), BookingState.WAITING);

        assertEquals(1, bookings.size());
        assertEquals(BookingStatus.WAITING, bookings.get(0).getStatus());
        assertEquals(user.getId(), bookings.get(0).getBooker().getId());
    }

    @Test
    void testGetAllUserBookingsCurrent() {
        Long userId = 20L;
        BookingState state = BookingState.CURRENT;

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User(userId, "User", "email@example.com")));

        List<BookingDto> bookings = bookingService.getBookingsByState(userId, state);

        bookings.forEach(booking -> assertThat(booking, allOf(
                hasProperty("booker", allOf(
                        hasProperty("id", equalTo(userId))
                )),
                hasProperty("status", notNullValue()))
        ));
    }
}