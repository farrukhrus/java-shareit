package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    private final User user1 = new User(1L, "User1", "user1@example.com");
    private final UserDto userDto1 = new UserDto(1L, "User1", "user1@example.com");
    private final UserDto userSaveDto1 = new UserDto(null, "User1", "user1@example.com");
    private final User user2 = new User(2L, "User2", "user2@example.com");
    private final UserDto userDto2 = new UserDto(2L, "User2", "user2@example.com");

    @Test
    void testGetAllUsers() {

        Mockito.when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        Mockito.when(userMapper.toUserDto(user1)).thenReturn(userDto1);
        Mockito.when(userMapper.toUserDto(user2)).thenReturn(userDto2);

        List<UserDto> users = userService.getAll();

        assertEquals(users, List.of(userDto1, userDto2));
    }

    @Test
    void testGetUserById() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(userMapper.toUserDto(user1)).thenReturn(userDto1);

        var result = userService.getUserById(1L);

        assertEquals(userDto1, result);
    }

    @Test
    void testCreateUser() {
        Mockito.when(userMapper.toUser(userSaveDto1)).thenReturn(user1);
        Mockito.when(userRepository.save(user1)).thenReturn(user1);
        Mockito.when(userMapper.toUserDto(user1)).thenReturn(userDto1);

        var result = userService.createUser(userSaveDto1);

        assertEquals(userDto1, result);
    }

    @Test
    void testUpdateUser() {
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(user1));
        UserDto updateUserAfterDto = new UserDto(1L, "updateUser", "updateUser@example.com");
        UserDto updateUserDto = new UserDto(null, "updateUser", "updateUser@example.com");

        Mockito.when(userMapper.toUserDto(Mockito.any(User.class)))
                .thenReturn(updateUserAfterDto);

        UserDto result = userService.updateUser(1L, updateUserDto);

        assertEquals(updateUserAfterDto, result);
    }

    @Test
    void testUpdateUserName() {
        UserDto updateUserDto = new UserDto(null, null, "updatedUser@example.com");
        UserDto updatedDto = new UserDto(1L, null, "updatedUser@example.com");

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        Mockito.when(userMapper.toUserDto(Mockito.any(User.class))).thenReturn(updatedDto);

        var result = userService.updateUser(1L, updateUserDto);

        assertEquals(updatedDto, result);
    }

    @Test
    void testUpdateUserEmail() {
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        UserDto updateUserDto = new UserDto(null, "updateUser", null);
        UserDto updateUserAfterDto = new UserDto(1L, "updateUser", null);

        Mockito.when(userMapper.toUserDto(Mockito.any(User.class))).thenReturn(updateUserAfterDto);

        UserDto result = userService.updateUser(1L, updateUserDto);

        assertEquals(updateUserAfterDto, result);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(1L);
        Mockito.verify(userRepository).deleteById(1L);
    }
}