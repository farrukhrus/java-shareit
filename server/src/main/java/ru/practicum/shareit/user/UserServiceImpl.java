package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.ValidationException;

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
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User wit id %s is not found", id)));

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (repository.existsByEmailIgnoreCase(userDto.getEmail())) {
                throw new ValidationException("Email already exists: " + userDto.getEmail());
            }
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        return userMapper.toUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}