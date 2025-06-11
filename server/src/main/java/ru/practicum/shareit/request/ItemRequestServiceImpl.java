package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDto createItemRequest(Long userId, ItemRequestSaveDto itemRequestSaveDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User = " + userId + " was not found"));
        ItemRequest itemRequest = itemRequestMapper.toRequest(itemRequestSaveDto);
        itemRequest.setRequester(user);
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        return itemRequestMapper.toRequestDto(savedItemRequest);
    }

    @Override
    public List<ItemRequestDto> getAllUserItemRequestsWithItems(Long userId) {
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId);
        return itemRequestMapper.toRequestDto(itemRequests);

    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId) {
        List<ItemRequest> itemRequests = itemRequestRepository
                .findAllByRequesterIdNotInOrderByCreatedDesc(List.of(userId));
        return itemRequestMapper.toRequestDto(itemRequests);
    }

    @Override
    public ItemRequestDto getItemRequest(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(
                        () -> new NotFoundException("Request id " + requestId + " was not found"));
        List<Item> items = itemRepository.findAllByRequestId(requestId);
        itemRequest.setItems(items);
        return itemRequestMapper.toRequestDto(itemRequest);
    }
}