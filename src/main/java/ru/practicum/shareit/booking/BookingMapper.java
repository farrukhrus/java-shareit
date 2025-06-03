package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booker", source = "user")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "status", expression = "java(BookingStatus.WAITING)")
    Booking toBooking(BookingCreateDto bcd, Item item, User user);
}