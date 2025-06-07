package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;


@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {


    private final ItemClient itemClient;


    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
                                         @Valid @RequestBody ItemDto itemDto) {
        return itemClient.create(userId, itemDto);
    }

    @GetMapping("/{item-id}")
    public ResponseEntity<Object> getByItemId(@RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
                                              @PathVariable("item-id") Long itemId) {
        return itemClient.getById(userId, itemId);
    }


    @PatchMapping("/{item-id}")

    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
                                         @PathVariable("item-id") Long itemId,
                                         @RequestBody ItemDto itemDto) {

        return itemClient.update(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable @NotNull Long itemId) {

        return itemClient.delete(itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") @NotNull Long userId) {

        return itemClient.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader("X-Sharer-User-Id") @NotNull Long userId,
                                         @RequestParam @NotBlank String text) {
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComments(@RequestHeader("X-Sharer-User-Id") Long userId,
                                              @Valid @RequestBody CommentDto commentDto,
                                              @PathVariable @NotNull Long itemId) {
        return itemClient.addComments(userId, itemId, commentDto);
    }
}