package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static ru.practicum.shareit.util.Constants.HEADER_USER_ID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        log.info("Create item by userId={}", userId);
        return itemClient.create(userId, itemDto);
    }

    @GetMapping("/{item-id}")
    public ResponseEntity<Object> getByItemId(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                              @PathVariable("item-id") Long itemId) {
        log.info("Get item by id = {}", itemId);
        return itemClient.getById(userId, itemId);
    }


    @PatchMapping("/{item-id}")
    public ResponseEntity<Object> update(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                         @PathVariable("item-id") Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Update item with id = {}", itemId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable @NotNull Long itemId) {
        log.info("Delete item with id = {}", itemId);
        return itemClient.delete(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader(HEADER_USER_ID) @NotNull Long userId) {
        log.info("Get items by user = {}", userId);
        return itemClient.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                         @RequestParam @NotBlank String text) {
        log.info("Search items by user = {} containing {}", userId, text);
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComments(@RequestHeader(HEADER_USER_ID) Long userId,
                                              @Valid @RequestBody CommentDto commentDto,
                                              @PathVariable @NotNull Long itemId) {
        log.info("Add comment to item = {} by user {}", itemId, userId);
        return itemClient.addComments(userId, itemId, commentDto);
    }
}