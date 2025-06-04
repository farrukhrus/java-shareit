package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public List<ItemDto> getItemsByOwnerId(Long userId) {
        List<Item> items = repository.findAllByOwnerId(userId);
        return items.stream().map(itemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> getBookings(Long ownerId) {
        List<Item> items = repository.findAllByOwnerId(ownerId);

        if (items.isEmpty()) {
            return List.of();
        }

        LocalDateTime now = LocalDateTime.now();

        Map<Long, Booking> lastBookings = bookingRepository.findLastBookingsByOwner(ownerId, now)
                .stream().collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));
        Map<Long, Booking> nextBookings = bookingRepository.findNextBookingsByOwner(ownerId, now)
                .stream().collect(Collectors.toMap(b -> b.getItem().getId(), b -> b));

        List<Comment> comments = commentRepository.findByItemOwnerId(ownerId);

        Map<Long, List<CommentDto>> commentsByItemId = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId(),
                        Collectors.mapping(commentMapper::toCommentDto, Collectors.toList())));

        return items.stream()
                .map(item -> {
                    Long itemId = item.getId();
                    List<CommentDto> commentsForItem = commentsByItemId.getOrDefault(itemId, List.of());
                    return itemMapper.toItemDto(item, lastBookings.get(itemId), nextBookings.get(itemId), commentsForItem);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItem(Long itemId) {
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());;
        Item item = repository.findById(itemId).orElseThrow(() ->
                new ValidationException("Item not found"));
        ItemDto itemDto = itemMapper.toItemDto(item);
        itemDto.setComments(comments);

        return itemDto;
    }

    @Override
    public ItemDto createItem(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item newItem = itemMapper.toItem(itemDto, null, null, owner);
        return itemMapper.toItemDto(repository.save(newItem));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return repository.searchAvailableItemsByText(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }


    @Override
    public ItemDto updateItem(ItemDto itemDto, Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Item oldItem = repository.findById(itemDto.getId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (itemDto.getName() != null && !itemDto.getName().equals(oldItem.getName())) {
            oldItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().equals(oldItem.getDescription())) {
            oldItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null && Boolean.parseBoolean(itemDto.getAvailable()) != oldItem.isAvailable()) {
            oldItem.setAvailable(Boolean.parseBoolean(itemDto.getAvailable()));
        }
        return itemMapper.toItemDto(oldItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = repository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Item not found"));
        repository.delete(item);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentCreateDto createCommentDto) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Item not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User not found"));

        Booking booking = bookingRepository
                .findByItemIdAndBookerIdAndStatusAndEndIsBefore(itemId, userId, BookingStatus.APPROVED, LocalDateTime.now())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ValidationException("Booking is not found"));

        Comment comment = commentMapper.toComment(createCommentDto, user, item, LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toCommentDto(savedComment);
    }
}