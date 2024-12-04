package ru.practicum.shareit.user;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final Map<Long, User> userStorage = new HashMap<>();
    private long idGenerator = 1;

    @Override
    public List<User> getAll() {
        return new ArrayList<>(userStorage.values());
    }

    @Override
    public User getUserById(Long id) {
        validateUserExists(id);
        return userStorage.get(id);
    }

    @Override
    public User createUser(User user) {
        validateEmailExist(user);
        user.setId(generatedId());
        userStorage.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        validateUserExists(user.getId());
        validateEmailExist(user);
        User userToUpdate = userStorage.get(user.getId());
        updateUser(userToUpdate, user);
        return userToUpdate;
    }


    @Override
    public void deleteUser(Long id) {
        validateUserExists(id);
        userStorage.remove(id);
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.containsKey(userId)) {
            log.error("User with id {} is not exist.", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + userId + " is not exist.");
        }
    }

    private void validateEmailExist(User user) {
        if (userStorage.values().stream()
                .anyMatch(existingUser ->
                        existingUser.getEmail() != null
                                && existingUser.getEmail().equalsIgnoreCase(user.getEmail()))) {
            log.error("Email {} is already exist", user.getEmail());
            throw new ValidationException("Email is already exist");
        }
    }

    private Long generatedId() {
        return idGenerator++;
    }

    private void updateUser(User userToUpdate, User newUser) {
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            userToUpdate.setEmail(newUser.getEmail());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            userToUpdate.setName(newUser.getName());
        }
    }
}