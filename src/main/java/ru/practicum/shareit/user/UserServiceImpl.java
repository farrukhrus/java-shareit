package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        return repository.getAll().stream().map(userMapper::toUserDto).toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        User createdUser = repository.createUser(user);
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(repository.getUserById(id));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userMapper.toUser(userDto);
        user.setId(id);
        User updatedUser = repository.updateUser(user);
        return userMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteUser(id);
    }
}