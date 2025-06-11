package ru.practicum.shareit.user.controller;

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
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static ru.practicum.shareit.util.Constants.HEADER_USER_ID;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private final UserService service;

    private UserDto userExpected;
    private UserDto userSaveDto;
    private String userSaveDtoJson;
    private String userDtoExpectedJson;
    private Long userId;

    @BeforeEach
    void testInit() throws Exception {
        userExpected = new UserDto();
        userExpected.setId(1L);
        userExpected.setName("user1");
        userExpected.setEmail("user1@example.com");

        userSaveDto = new UserDto();
        userSaveDto.setName(userExpected.getName());
        userSaveDto.setEmail(userExpected.getEmail());

        userSaveDtoJson = objectMapper.writeValueAsString(userSaveDto);
        userDtoExpectedJson = objectMapper.writeValueAsString(userExpected);

        userId = userExpected.getId();
    }

    @Test
    void testCreateUser() throws Exception {
        when(service.createUser(any(UserDto.class))).thenReturn(userExpected);

        mockMvc.perform(post("/users")
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(userSaveDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(userDtoExpectedJson));

        verify(service, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void testGetUserById() throws Exception {
        String path = "/users/" + userId;

        when(service.getUserById(eq(userId))).thenReturn(userExpected);

        mockMvc.perform(get(path)
                        .header(HEADER_USER_ID, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(userDtoExpectedJson));

        verify(service, times(1)).getUserById(eq(userId));
    }

    @Test
    void testUpdateUser() throws Exception {
        String nameUpdated = "user2";
        String emailUpdated = "user2@example.com";

        UserDto userSaveDtoForUpdate = new UserDto();
        userSaveDtoForUpdate.setName(nameUpdated);
        userSaveDtoForUpdate.setEmail(emailUpdated);
        String userSaveDtoForUpdateJson = objectMapper.writeValueAsString(userSaveDtoForUpdate);
        String path = "/users/" + userId;

        when(service.updateUser(eq(userId), eq(userSaveDtoForUpdate)))
                .thenAnswer(invocationOnMock -> {
                    userExpected.setName(nameUpdated);
                    userExpected.setEmail(emailUpdated);
                    return userExpected;
                });

        mockMvc.perform(patch(path)
                        .header(HEADER_USER_ID, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(userSaveDtoForUpdateJson))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userExpected)));

        verify(service, times(1)).updateUser(eq(userId), eq(userSaveDtoForUpdate));
    }

    @Test
    void testDeleteUser() throws Exception {
        String path = "/users/" + userId;

        mockMvc.perform(delete(path)
                        .header(HEADER_USER_ID, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service, times(1)).deleteUser(eq(userId));
    }
}
