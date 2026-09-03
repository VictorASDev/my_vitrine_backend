package com.myvitrine.api.repository;

import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.ProfileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User(UUID.randomUUID(), "Ana Lima", "ana@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("ana@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Ana Lima");
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("inexistente@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = new User(UUID.randomUUID(), "Ana Lima", "ana2@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persistAndFlush(user);

        assertThat(userRepository.existsByEmail("ana2@example.com")).isTrue();
    }
}
