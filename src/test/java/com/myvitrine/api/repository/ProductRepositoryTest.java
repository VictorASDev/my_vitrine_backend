package com.myvitrine.api.repository;

import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
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
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldFindProductsByStoreId() {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persist(storeUser);
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        entityManager.persist(store);

        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        entityManager.persistAndFlush(product);

        List<Product> products = productRepository.findByStoreUserId(store.getUserId());

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Camiseta");
    }

    @Test
    void shouldFindOnlyActiveProducts() {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja2@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persist(storeUser);
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        entityManager.persist(store);

        Product active = new Product(UUID.randomUUID(), store, "Ativo", new BigDecimal("10.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        Product inactive = new Product(UUID.randomUUID(), store, "Inativo", new BigDecimal("10.00"),
                new BigDecimal("10.00"), null, false, LocalDateTime.now());
        entityManager.persist(active);
        entityManager.persistAndFlush(inactive);

        List<Product> activeProducts = productRepository.findByActiveTrue();

        assertThat(activeProducts).extracting(Product::getName).containsExactly("Ativo");
    }
}
