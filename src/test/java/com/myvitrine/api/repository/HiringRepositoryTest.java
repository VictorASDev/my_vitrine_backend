package com.myvitrine.api.repository;

import com.myvitrine.api.model.CreatorProfile;
import com.myvitrine.api.model.Hiring;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.HiringStatus;
import com.myvitrine.api.model.enums.ProfileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class HiringRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HiringRepository hiringRepository;

    @Test
    void shouldFindHiringsByStoreAndByCreator() {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persist(storeUser);
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        entityManager.persist(store);

        User creatorUser = new User(UUID.randomUUID(), "Bia", "bia@example.com", "hash",
                ProfileType.CREATOR, LocalDateTime.now());
        entityManager.persist(creatorUser);
        CreatorProfile creator = new CreatorProfile(creatorUser, null, null);
        entityManager.persist(creator);

        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        entityManager.persist(product);

        Hiring hiring = new Hiring(UUID.randomUUID(), store, creator, product, HiringStatus.REQUESTED, LocalDateTime.now());
        entityManager.persistAndFlush(hiring);

        List<Hiring> byStore = hiringRepository.findByStoreUserId(store.getUserId());
        List<Hiring> byCreator = hiringRepository.findByCreatorUserId(creator.getUserId());

        assertThat(byStore).hasSize(1);
        assertThat(byCreator).hasSize(1);
        assertThat(byStore.get(0).getId()).isEqualTo(hiring.getId());
    }
}
