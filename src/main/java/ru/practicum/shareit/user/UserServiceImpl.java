package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAll() {
        return repository.findAll().stream().map(userMapper::toUserDto).toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toUser(userDto);
        User createdUser = repository.save(user);
        return userMapper.toUserDto(createdUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.toUserDto(repository.getById(id));
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User oldUser = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User wit id %s is not found", id)));
        User user = userMapper.toUser(userDto);
        user.setId(id);

        if (user.getEmail() == null || oldUser.getEmail().isBlank()) {
            user.setEmail(oldUser.getEmail());
        }

        if (user.getName() == null || oldUser.getName().isBlank()) {
            user.setName(oldUser.getName());
        }

        return userMapper.toUserDto(repository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

}