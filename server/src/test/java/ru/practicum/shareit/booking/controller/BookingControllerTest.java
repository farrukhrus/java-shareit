package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ru.practicum.shareit.util.Constants.HEADER_USER_ID;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private ItemMapper itemMapper;
    private UserMapper userMapper;

    @MockBean
    private final BookingService service;
    private BookingDto bookingExpected;
    private Long userId;
    private Long bookingId;

    @BeforeEach
    public void testInit() {
        userId = 1L;
        bookingId = 1L;

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(1);

        Item item = new Item();
        item.setId(1L);
        item.setName("piano");
        item.setDescription("Description");
        item.setAvailable(true);

        User booker = new User();
        booker.setId(1L);
        booker.setName("Booker");
        booker.setEmail("booker@email.com");

        bookingExpected = new BookingDto();
        bookingExpected.setId(1L);
        bookingExpected.setStart(start);
        bookingExpected.setEnd(end);
        bookingExpected.setStatus(BookingStatus.WAITING);
        bookingExpected.setItem(item);
        bookingExpected.setBooker(booker);

    }

    @Test
    void testCreateBooking() throws Exception {
        BookingCreateDto bookingSaveDto = new BookingCreateDto();
        bookingSaveDto.setItemId(1L);
        bookingSaveDto.setStart(LocalDateTime.now());
        bookingSaveDto.setEnd(LocalDateTime.now().plusMinutes(1));
        String bookingSaveDtoJson = objectMapper.writeValueAsString(bookingSaveDto);
        String bookingExpectedJson = objectMapper.writeValueAsString(bookingExpected);

        when(service.createBooking(eq(userId), any(BookingCreateDto.class)))
                .thenReturn(bookingExpected);
        mockMvc.perform(post("/bookings")
                        .header(HEADER_USER_ID, String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookingSaveDtoJson))
                .andExpect(status().isCreated())
                .andExpect(content().json(bookingExpectedJson));

        verify(service, times(1)).createBooking(eq(userId), any(BookingCreateDto.class));
    }

    @Test
    void testUpdateBooking() throws Exception {
        boolean approved = true;
        String path = "/bookings/" + bookingId;

        when(service.updateBookingStatus(eq(userId), eq(bookingId), eq(approved)))
                .thenAnswer(invocationOnMock -> {
                    bookingExpected.setStatus(BookingStatus.APPROVED);
                    return bookingExpected;
                });

        mockMvc.perform(patch(path)
                        .header(HEADER_USER_ID, String.valueOf(userId))
                        .param("approved", String.valueOf(approved))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(BookingStatus.APPROVED.name()));

        verify(service, times(1)).updateBookingStatus(eq(userId), eq(bookingId), eq(approved));
    }

    @Test
    void testGetBookingById() throws Exception {
        String path = "/bookings/" + bookingId;

        when(service.getBookingById(eq(userId), eq(bookingId)))
                .thenReturn(bookingExpected);

        mockMvc.perform(get(path)
                        .header(HEADER_USER_ID, String.valueOf(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(bookingExpected)));

        verify(service, times(1)).getBookingById(eq(userId), eq(bookingId));
    }

    @Test
    void testGetBookingsByState() throws Exception {
        BookingState state = BookingState.REJECTED;

        when(service.getBookingsByState(eq(userId), eq(state)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("state", String.valueOf(state))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(service, times(1)).getBookingsByState(eq(userId), eq(state));
    }

    @Test
    void testGetBookingsForOwner() throws Exception {
        BookingState state = BookingState.WAITING;
        String path = "/bookings" + "/owner";
        List<BookingDto> expectedBookings = List.of(bookingExpected);
        String expectedBookingsJson = objectMapper.writeValueAsString(expectedBookings);

        when(service.getBookingsForOwner(eq(userId), eq(state)))
                .thenReturn(expectedBookings);
        mockMvc.perform(get(path)
                        .header(HEADER_USER_ID, userId)
                        .param("state", String.valueOf(state))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(content().json(expectedBookingsJson));

        verify(service, times(1)).getBookingsForOwner(eq(userId), eq(state));
    }
}