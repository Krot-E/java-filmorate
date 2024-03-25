package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private void checkId(Long id) {
        if (!userStorage.contains(id)) {
            log.error("Указан id несуществующего пользователя.");
            throw new NotFoundException("Пользователя с таким id не существует.");
        }
    }

    public void addFriend(Long userId, Long friendId) {
        checkId(userId);
        checkId(friendId);

        log.debug("Получен запрос добавления в друзья.");

        Set<Long> userFriends = friends.get(userId);
        userFriends.add(friendId);

        Set<Long> friendFriends = friends.get(friendId);
        friendFriends.add(userId);
    }

    public User deleteFriend(Long userId, Long friendId) {
        checkId(userId);
        checkId(friendId);

        log.debug("Получен запрос удаления из друзей.");

        Set<Long> userFriends = friends.get(userId);
        userFriends.remove(friendId);

        Set<Long> friendFriends = friends.get(friendId);
        friendFriends.remove(userId);

        return userStorage.getUserById(friendId);
    }

    public List<User> getAllFriends(Long id) {
        checkId(id);

        log.debug("Получен запрос получения списка друзей.");

        List<User> userFriends = new ArrayList<>();
        for (Long friendId : friends.get(id)) {
            User friend = userStorage.getUserById(friendId);
            userFriends.add(friend);
        }
        return userFriends;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        checkId(userId);
        checkId(otherId);

        log.debug("Получен запрос получения списка общих друзей.");

        Set<Long> userFriends = new HashSet<>(friends.get(userId));
        userFriends.retainAll(friends.get(otherId));
        List<User> commonFriends = new ArrayList<>();
        for (Long id : userFriends) {
            commonFriends.add(userStorage.getUserById(id));
        }
        return commonFriends;
    }

    public User createUser(User user) {
        User newUser = userStorage.create(user);
        friends.put(newUser.getId(), new HashSet<>());
        return newUser;
    }

    public User updateUser(User user) {
        return userStorage.update(user);
    }

    public User deleteUser(Long id) {
        User delUser = userStorage.delete(id);
        for (Long friendId : friends.get(id)) {
            friends.get(friendId).remove(id);
        }
        return delUser;
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public List<User> findAllUsers() {
        return userStorage.findAllUsers();
    }
}
