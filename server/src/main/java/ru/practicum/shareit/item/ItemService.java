package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;

import java.util.List;


public interface ItemService {
    List<ItemDto> getItemsByOwnerId(Long userId);

    List<ItemDto> getBookings(Long ownerId);

    ItemDto getItem(Long itemId);

    ItemDto createItem(ItemSaveDto itemSaveDto, Long userId);

    List<ItemDto> searchItems(String text);

    ItemDto updateItem(ItemDto itemDto, Long userId);

    void deleteItem(Long userId, Long itemId);

    CommentDto addComment(Long itemId, Long userId, CommentCreateDto commentDto);
}