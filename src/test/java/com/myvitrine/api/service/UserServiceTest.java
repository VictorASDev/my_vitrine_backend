package com.myvitrine.api.service;

import com.myvitrine.api.dto.request.UserRequest;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.exception.ResourceConflictException;
import com.myvitrine.api.exception.ResourceNotFoundException;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import com.myvitrine.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRequest request;

    @BeforeEach
    void setUp() {
        request = new UserRequest("Ana Lima", "ana@example.com", "senha1234");
    }

    @Test
    void shouldCreateUserWhenEmailIsNotTaken() {
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("hash-bcrypt-simulado");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(request);

        assertThat(response.name()).isEqualTo("Ana Lima");
        assertThat(response.email()).isEqualTo("ana@example.com");
        assertThat(response.profileType()).isEqualTo(ProfileType.STORE);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash-bcrypt-simulado");
    }

    @Test
    void shouldThrowConflictWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnUserWhenFound() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "Ana Lima", "ana@example.com", "hash", ProfileType.STORE, LocalDateTime.now());
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(id);

        assertThat(response.id()).isEqualTo(id);
    }
}
