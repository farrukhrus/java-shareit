package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;
import ru.practicum.shareit.item.ItemService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private static final String HEADER_USER_ID = "X-Sharer-User-Id";
    @MockBean
    private final ItemService itemService;
    private ItemDto expectedItem;
    private CommentDto expectedComment;
    private Long userId;

    @BeforeEach
    public void testInit() {
        userId = 1L;

        expectedItem = new ItemDto();
        expectedItem.setId(1L);
        expectedItem.setName("item");
        expectedItem.setDescription("description");
        expectedItem.setAvailable("false");

        expectedComment = new CommentDto();
        expectedComment.setId(1L);
        expectedComment.setText("comment");
        expectedComment.setAuthorName("user1");
        expectedComment.setCreated(null);
    }

    @Test
    void testCreateItem() throws Exception {
        ItemSaveDto itemSaveDto = new ItemSaveDto();
        itemSaveDto.setName("item");
        itemSaveDto.setDescription("description");
        itemSaveDto.setAvailable(false);
        String itemSaveDtoJson = objectMapper.writeValueAsString(itemSaveDto);
        String itemDtoExpectedJson = objectMapper.writeValueAsString(expectedItem);

        when(itemService.createItem(any(ItemSaveDto.class), any(Long.class)))
                .thenReturn(expectedItem);

        mockMvc.perform(post("/items")
                        .header(HEADER_USER_ID, String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(itemSaveDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(itemDtoExpectedJson));

        verify(itemService, times(1)).createItem(any(ItemSaveDto.class), any(Long.class));

    }

    @Test
    void testAddComment() throws Exception {
        final CommentDto commentInfoDto = new CommentDto();
        commentInfoDto.setText("Text");

        when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class))).thenReturn(commentInfoDto);

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 1)
                        .content("{\"text\": \"Text\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Text"));

        verify(itemService, times(1)).addComment(anyLong(), anyLong(), any(CommentCreateDto.class));
    }

    @Test
    void testUpdateItem() throws Exception {
        final ItemDto itemDto = new ItemDto();

        when(itemService.updateItem(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, 1L)
                        .content("{\"name\": \"Updated item\", \"description\": \"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));

        verify(itemService, times(1)).updateItem(any(ItemDto.class), anyLong());
    }
}