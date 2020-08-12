package am.itspace.springdemo.service;

import am.itspace.springdemo.model.User;
import am.itspace.springdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public User getOne(int id) {
        try {
            return userRepository.getOne(id);
        } catch (EntityNotFoundException e) {
            log.error("User with {} id does not exists", id);
            return null;
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

}
