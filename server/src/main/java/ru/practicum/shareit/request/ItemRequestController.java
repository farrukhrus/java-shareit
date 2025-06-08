package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestHeader(HEADER_USER_ID) Long userId,
                                            @RequestBody ItemRequestSaveDto itemRequestSaveDto) {
        log.info("Create item by user = {}", userId);
        return itemRequestService.createItemRequest(userId, itemRequestSaveDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllUserItemRequest(@RequestHeader(HEADER_USER_ID) Long userId) {
        log.info("Get requests by user = {}", userId);
        return itemRequestService.getAllUserItemRequestsWithItems(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@PathVariable Long requestId) {
        log.info("Get data by requestId = {}", requestId);
        return itemRequestService.getItemRequest(requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(
            @RequestHeader(HEADER_USER_ID) Long userId
    ) {
        return itemRequestService.getAllItemRequests(userId);
    }
}