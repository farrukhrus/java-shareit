package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    ItemDto toItemDto(Item item);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "description", source = "dto.description")
    Item toItem(ItemDto dto, Long id, ItemRequest request, Long ownerId);
}