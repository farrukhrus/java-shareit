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
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemSaveDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ru.practicum.shareit.util.Constants.HEADER_USER_ID;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private final ItemService itemService;

    private ItemDto expectedItem;
    private CommentDto expectedComment;
    private ItemSaveDto itemSaveDto;
    private String itemSaveDtoJson;
    private String itemDtoExpectedJson;
    private CommentCreateDto commentCreateDto;
    private String commentCreateDtoJson;
    private Long userId;

    @BeforeEach
    public void testInit() throws Exception {
        userId = 1L;

        expectedItem = new ItemDto();
        expectedItem.setId(1L);
        expectedItem.setName("item");
        expectedItem.setDescription("description");
        expectedItem.setAvailable("false");

        itemSaveDto = new ItemSaveDto();
        itemSaveDto.setName("item");
        itemSaveDto.setDescription("description");
        itemSaveDto.setAvailable(false);

        itemSaveDtoJson = objectMapper.writeValueAsString(itemSaveDto);
        itemDtoExpectedJson = objectMapper.writeValueAsString(expectedItem);

        expectedComment = new CommentDto();
        expectedComment.setId(1L);
        expectedComment.setText("comment");
        expectedComment.setAuthorName("user1");
        expectedComment.setCreated(null);

        commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Text");
        commentCreateDtoJson = objectMapper.writeValueAsString(commentCreateDto);
    }

    @Test
    void testCreateItem() throws Exception {
        when(itemService.createItem(any(ItemSaveDto.class), any(Long.class))).thenReturn(expectedItem);

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
        when(itemService.addComment(anyLong(), anyLong(), any(CommentCreateDto.class))).thenReturn(expectedComment);

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, userId)
                        .content(commentCreateDtoJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("comment"));

        verify(itemService, times(1)).addComment(anyLong(), anyLong(), any(CommentCreateDto.class));
    }

    @Test
    void testUpdateItem() throws Exception {
        final ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);

        when(itemService.updateItem(any(ItemDto.class), anyLong())).thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_USER_ID, userId)
                        .content("{\"name\": \"Updated item\", \"description\": \"Updated description\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()));

        verify(itemService, times(1)).updateItem(any(ItemDto.class), anyLong());
    }
}
