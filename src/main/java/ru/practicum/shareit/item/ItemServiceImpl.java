package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getItemsByOwnerId(Long userId) {
        return repository.getItemsByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return ItemMapper.toItemDto(repository.getItem(itemId));
    }


    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        userRepository.getUserById(userId);
        Item newItem = ItemMapper.toItem(itemDto, null, null, userId);
        return ItemMapper.toItemDto(repository.createItem(newItem));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return repository.searchItems(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }


    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId) {
        Item updatedItem = ItemMapper.toItem(itemDto, itemDto.getId(), null, userId);
        return ItemMapper.toItemDto(repository.updateItem(updatedItem));
    }


    @Override
    public void deleteItem(Long userId, Long itemId) {
        repository.deleteItem(userId, itemId);
    }

}