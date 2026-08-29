package com.myvitrine.api.repository;

import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.model.enums.ProfileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AffiliateLinkRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AffiliateLinkRepository affiliateLinkRepository;

    private Product persistProduct() {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persist(storeUser);
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        entityManager.persist(store);
        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        entityManager.persist(product);
        return product;
    }

    private AffiliateProfile persistAffiliate() {
        User affiliateUser = new User(UUID.randomUUID(), "Joao", "joao@example.com", "hash",
                ProfileType.AFFILIATE, LocalDateTime.now());
        entityManager.persist(affiliateUser);
        AffiliateProfile affiliate = new AffiliateProfile(affiliateUser, null, null);
        entityManager.persist(affiliate);
        return affiliate;
    }

    @Test
    void shouldFindAffiliateLinkByCode() {
        AffiliateLink link = new AffiliateLink(UUID.randomUUID(), persistAffiliate(), persistProduct(),
                "ABC12345", AffiliateLinkType.LINK, LocalDateTime.now());
        entityManager.persistAndFlush(link);

        Optional<AffiliateLink> found = affiliateLinkRepository.findByCode("ABC12345");

        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(AffiliateLinkType.LINK);
    }

    @Test
    void shouldEnforceUniqueCodeConstraint() {
        AffiliateProfile affiliate = persistAffiliate();
        Product product = persistProduct();

        AffiliateLink first = new AffiliateLink(UUID.randomUUID(), affiliate, product, "DUPLICADO",
                AffiliateLinkType.LINK, LocalDateTime.now());
        entityManager.persistAndFlush(first);

        AffiliateLink duplicate = new AffiliateLink(UUID.randomUUID(), affiliate, product, "DUPLICADO",
                AffiliateLinkType.COUPON, LocalDateTime.now());

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
