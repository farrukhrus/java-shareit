package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.model.Item;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private long idGenerator = 0;

    @Override
    public List<Item> findByUserId(Long userId) {
        return itemStorage.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> getItemsByOwnerId(Long userId) {
        return findByUserId(userId);
    }

    @Override
    public Item getItem(Long itemId) {
        if (!itemStorage.containsKey(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + itemId + " is not found");
        }
        return itemStorage.get(itemId);
    }


    @Override
    public Item createItem(Item item) {
        item.setId(generateId());
        itemStorage.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> searchItems(String text) {
        String lowerCaseText = text.toLowerCase();
        return itemStorage.values().stream()
                .filter(item -> item.isAvailable()
                        && (item.getName().toLowerCase().contains(lowerCaseText)
                        || item.getDescription().toLowerCase().contains(lowerCaseText)))
                .toList();
    }


    @Override
    public Item updateItem(Item item) {
        Item existingItem = validateItemOwnership(item.getOwnerId(), item.getId());
        updateFields(existingItem, item);
        itemStorage.put(existingItem.getId(), existingItem);
        return existingItem;
    }

    private void updateFields(Item existingItem, Item newItem) {
        if (newItem.getName() != null && !existingItem.getName().equals(newItem.getName())) {
            existingItem.setName(newItem.getName());
        }
        if (newItem.getDescription() != null && !existingItem.getDescription().equals(newItem.getDescription())) {
            existingItem.setDescription(newItem.getDescription());
        }
        if (newItem.isAvailable() != existingItem.isAvailable()) {
            existingItem.setAvailable(newItem.isAvailable());
        }
    }

    private Item validateItemOwnership(Long ownerId, Long itemId) {
        if (!itemStorage.containsKey(itemId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item with id " + itemId + " is already exist");
        }

        Item item = itemStorage.get(itemId);
        if (!item.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You are not the owner of item with id " + itemId);
        }
        return item;
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        validateItemOwnership(userId, itemId);
        itemStorage.remove(itemId);
    }

    private Long generateId() {
        return idGenerator++;
    }
}