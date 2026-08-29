package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.UserRequest;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.User;
import com.myvitrine.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("Ja existe um usuario cadastrado com o e-mail " + request.email());
        }
        User user = new User(UUID.randomUUID(), request.name(), request.email(),
               passwordEncoder.encode(request.password()), request.profileType(), LocalDateTime.now());
        return UserResponse.from(userRepository.save(user));
    }

    public UserResponse findById(UUID id) {
        return UserResponse.from(getUserOrThrow(id));
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = getUserOrThrow(id);
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("Ja existe um usuario cadastrado com o e-mail " + request.email());
        }
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return UserResponse.from(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = getUserOrThrow(id);
        userRepository.delete(user);
    }

    User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado: " + id));
    }
}
