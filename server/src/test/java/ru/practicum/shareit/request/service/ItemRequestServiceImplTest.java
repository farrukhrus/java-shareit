package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestSaveDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private final User user = new User(1L, "User", "user@email.com");
    private final UserDto userDto = new UserDto(1L, "User", "user@email.com");
    private final User user2 = new User(2L, "User2", "user2@email.com");
    private final UserDto userDto2 = new UserDto(2L, "User2", "user@email.com");
    private final ItemRequest itemRequest = ItemRequest.builder()
            .id(1L)
            .requester(user)
            .description("description")
            .build();
    private final ItemRequestSaveDto itemRequestSaveDto = ItemRequestSaveDto.builder()
            .description("New item request")
            .build();
    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(itemRequest.getId())
            .description(itemRequest.getDescription())
            .requester(user)
            .items(Collections.emptyList())
            .build();

    private final ItemRequestDto itemRequestDto2 = ItemRequestDto.builder()
            .id(itemRequest.getId())
            .description(itemRequest.getDescription())
            .requester(user2)
            .items(Collections.emptyList())
            .build();

    @Test
    void testGetAllUserItemRequestsWithItemsReturnItemRequests() {
        List<ItemRequest> itemRequests = List.of(itemRequest);
        Mockito.when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(user.getId()))
                .thenReturn(itemRequests);
        Mockito.when(itemRequestMapper.toRequestDto(itemRequests))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> result = itemRequestService.getAllUserItemRequestsWithItems(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(user.getId(), result.get(0).getRequester().getId());
        Mockito.verify(itemRequestRepository, Mockito.times(1)).findAllByRequesterIdOrderByCreatedDesc(user.getId());
    }

    @Test
    void testGetAllItemRequestsReturnItemRequests() {
        itemRequest.setRequester(user2);
        List<ItemRequest> itemRequests = List.of(itemRequest);

        Mockito.when(itemRequestRepository.findAllByRequesterIdNotInOrderByCreatedDesc(List.of(user.getId())))
                .thenReturn(itemRequests);
        Mockito.when(itemRequestMapper.toRequestDto(itemRequests))
                .thenReturn(List.of(itemRequestDto2));

        List<ItemRequestDto> result = itemRequestService.getAllItemRequests(user.getId());

        assertEquals(1, result.size());
        assertNotEquals(user.getId(), result.get(0).getRequester().getId());
    }

    @Test
    void testGetItemRequestReturnItemRequestDto() {
        Item item = Item.builder()
                .id(1L)
                .request(itemRequest)
                .owner(user2)
                .build();

        List<Item> items = List.of(item);

        Mockito.when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));

        Mockito.when(itemRepository.findAllByRequestId(itemRequest.getId()))
                .thenReturn(items);

        Mockito.when(itemRequestMapper.toRequestDto(itemRequest))
                .thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.getItemRequest(itemRequest.getId());

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        Mockito.verify(itemRequestRepository).findById(itemRequest.getId());
        Mockito.verify(itemRepository).findAllByRequestId(itemRequest.getId());
        Mockito.verify(itemRequestMapper).toRequestDto(itemRequest);
    }

    @Test
    void testCreateItemRequestReturnItemRequestDto() {
        ItemRequest savedItemRequest = ItemRequest.builder()
                .id(1L)
                .requester(user)
                .description(itemRequestSaveDto.getDescription())
                .build();

        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        Mockito.when(itemRequestMapper.toRequest(itemRequestSaveDto))
                .thenReturn(savedItemRequest);

        Mockito.when(itemRequestRepository.save(savedItemRequest))
                .thenReturn(savedItemRequest);

        Mockito.when(itemRequestMapper.toRequestDto(savedItemRequest))
                .thenReturn(itemRequestDto);

        ItemRequestDto result = itemRequestService.createItemRequest(user.getId(), itemRequestSaveDto);

        assertNotNull(result);
        assertEquals(itemRequestDto.getId(), result.getId());
        assertEquals(itemRequestDto.getDescription(), result.getDescription());
        assertEquals(itemRequestDto.getRequester().getId(), result.getRequester().getId());

        Mockito.verify(userRepository).findById(user.getId());
        Mockito.verify(itemRequestMapper).toRequest(itemRequestSaveDto);
        Mockito.verify(itemRequestRepository).save(savedItemRequest);
        Mockito.verify(itemRequestMapper).toRequestDto(savedItemRequest);
    }
}