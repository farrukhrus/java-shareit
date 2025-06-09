package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    UserService userService;

    @Mock
    ItemRequestRepository itemRequestRepository;

    private final User user = new User(1L, "User", "user@email.com");

    private final ItemRequest request = ItemRequest.builder()
            .id(1L)
            .description("description")
            .requester(user)
            .items(new ArrayList<>())
            .build();
    private final Item item = Item.builder()
            .id(1L)
            .name("ItemName")
            .description("description")
            .available(true)
            .owner(user)
            .request(request)
            .build();
    ItemDto mappedDto;
    Item mappedItem;
    ItemRequest itemRequest1;
    User user1;
    ItemSaveDto itemDto1;

    @BeforeEach
    public void set() {
        itemDto1 = new ItemSaveDto();
        itemDto1.setName("Sock");
        itemDto1.setDescription("Black");
        itemDto1.setAvailable(false);
        itemDto1.setRequestId(1L);

        user1 = new User();
        user1.setId(1L);
        user1.setName("Petya");
        user1.setEmail("pupkin@example.com");

        itemRequest1 = new ItemRequest();
        itemRequest1.setId(1L);
        itemRequest1.setDescription("New sock");
        itemRequest1.setRequester(user1);
        itemRequest1.setCreated(LocalDateTime.now());

        mappedItem = Item.builder()
                .id(1L)
                .name(itemDto1.getName())
                .description(itemDto1.getDescription())
                .available(itemDto1.getAvailable())
                .owner(user1)
                .request(itemRequest1)
                .build();

        mappedDto = ItemDto.builder()
                .id(1L)
                .name(itemDto1.getName())
                .description(itemDto1.getDescription())
                .available("false")
                .build();
    }

    @Test
    void testCreate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest1));
        when(itemMapper.toItem(eq(itemDto1), isNull(), eq(itemRequest1), eq(user1)))
                .thenReturn(mappedItem);
        when(itemRepository.save(any(Item.class))).thenReturn(mappedItem);
        when(itemMapper.toItemDto(mappedItem)).thenReturn(mappedDto);

        ItemDto createdItemDto = itemService.createItem(itemDto1, 1L);

        assertEquals("Sock", createdItemDto.getName());
        assertEquals("false", createdItemDto.getAvailable());
    }
}