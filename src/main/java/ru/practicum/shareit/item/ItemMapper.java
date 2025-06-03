package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import org.mapstruct.*;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toItemDto(Item item);

    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "lastBooking", source = "lastBooking.start")
    @Mapping(target = "nextBooking", source = "nextBooking.start")
    @Mapping(target = "comments", source = "comments")
    ItemDto toItemDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "name", source = "dto.name")
    Item toItem(ItemDto dto, Long id, ItemRequest request, User owner);
}