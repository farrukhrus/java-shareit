package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;


@RestController("itemController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/items")
public class ItemController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemDto>> getItemsByOwnerId(@RequestHeader(HEADER_USER_ID) Long userId) {
        List<ItemDto> items = itemService.getItemsByOwnerId(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItem(@RequestHeader(HEADER_USER_ID) Long userId,
                                           @PathVariable Long itemId) {
        ItemDto item = itemService.getItem(itemId);
        return ResponseEntity.ok(item);
    }


    @PostMapping
    public ResponseEntity<ItemDto> create(
            @RequestHeader(HEADER_USER_ID) Long userId,
            @Valid @RequestBody ItemDto itemDto) {
        ItemDto createdItem = itemService.createItem(itemDto, userId);
        return ResponseEntity.ok(createdItem);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(@RequestParam String text) {
        List<ItemDto> searchResults = itemService.searchItems(text);
        return ResponseEntity.ok(searchResults);
    }


    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(@RequestHeader(HEADER_USER_ID) Long userId,
                                          @RequestBody ItemDto itemDto,
                                          @PathVariable Long itemId) {
        itemDto.setId(itemId);
        ItemDto updatedItem = itemService.updateItem(itemDto, userId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@RequestHeader(HEADER_USER_ID) Long userId,
                                       @PathVariable Long itemId) {
        itemService.deleteItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }
}