package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static ru.practicum.shareit.util.Constants.HEADER_USER_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        return itemClient.create(userId, itemDto);
    }

    @GetMapping("/{item-id}")
    public ResponseEntity<Object> getByItemId(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                              @PathVariable("item-id") Long itemId) {
        return itemClient.getById(userId, itemId);
    }


    @PatchMapping("/{item-id}")
    public ResponseEntity<Object> update(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                         @PathVariable("item-id") Long itemId,
                                         @RequestBody ItemDto itemDto) {

        return itemClient.update(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable @NotNull Long itemId) {

        return itemClient.delete(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader(HEADER_USER_ID) @NotNull Long userId) {

        return itemClient.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(HEADER_USER_ID) @NotNull Long userId,
                                         @RequestParam @NotBlank String text) {
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComments(@RequestHeader(HEADER_USER_ID) Long userId,
                                              @Valid @RequestBody CommentDto commentDto,
                                              @PathVariable @NotNull Long itemId) {
        return itemClient.addComments(userId, itemId, commentDto);
    }
}