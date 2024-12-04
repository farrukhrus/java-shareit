package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;

@RestController("userController")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        log.debug("Getting list of all users");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        log.debug("Getting user by id");
        return userService.getUserById(id);
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto user) {
        log.debug("Creating user");
        return userService.createUser(user);
    }


    @PatchMapping("/{id}")
    public UserDto update(@PathVariable Long id, @RequestBody UserDto user) {
        log.debug("Updating user with id {}", user.getId());
        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.debug("Deleting user with id {}", id);
        userService.deleteUser(id);
    }
}