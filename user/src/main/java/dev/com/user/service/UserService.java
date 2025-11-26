package dev.com.user.service;

import dev.com.user.entities.UserEntity;
import dev.com.user.producer.UserProducer;
import dev.com.user.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProducer userProducer;

    @Transactional
    public UserEntity saveAndPublish(UserEntity user) {
        var userEntity = userRepository.save(user);
        userProducer.sendMenssage(user);
        return userEntity;
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.deleteById(id);
    }

}
