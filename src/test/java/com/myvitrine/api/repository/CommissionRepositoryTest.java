package com.myvitrine.api.repository;

import com.myvitrine.api.model.AffiliateLink;
import com.myvitrine.api.model.AffiliateProfile;
import com.myvitrine.api.model.Commission;
import com.myvitrine.api.model.Product;
import com.myvitrine.api.model.Sale;
import com.myvitrine.api.model.StoreProfile;
import com.myvitrine.api.model.User;
import com.myvitrine.api.model.enums.AffiliateLinkType;
import com.myvitrine.api.model.enums.PaymentStatus;
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
class CommissionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommissionRepository commissionRepository;

    private Sale persistSale() {
        User storeUser = new User(UUID.randomUUID(), "Loja X", "loja@example.com", "hash",
                ProfileType.STORE, LocalDateTime.now());
        entityManager.persist(storeUser);
        StoreProfile store = new StoreProfile(storeUser, "Loja X", null);
        entityManager.persist(store);
        Product product = new Product(UUID.randomUUID(), store, "Camiseta", new BigDecimal("50.00"),
                new BigDecimal("10.00"), null, true, LocalDateTime.now());
        entityManager.persist(product);
        User affiliateUser = new User(UUID.randomUUID(), "Joao", "joao@example.com", "hash",
                ProfileType.AFFILIATE, LocalDateTime.now());
        entityManager.persist(affiliateUser);
        AffiliateProfile affiliate = new AffiliateProfile(affiliateUser, null, null);
        entityManager.persist(affiliate);
        AffiliateLink link = new AffiliateLink(UUID.randomUUID(), affiliate, product, "ABC12345",
                AffiliateLinkType.COUPON, LocalDateTime.now());
        entityManager.persist(link);
        Sale sale = new Sale(UUID.randomUUID(), link, new BigDecimal("200.00"), LocalDateTime.now());
        entityManager.persist(sale);
        return sale;
    }

    @Test
    void shouldFindCommissionBySaleId() {
        Sale sale = persistSale();
        Commission commission = new Commission(UUID.randomUUID(), sale, new BigDecimal("20.00"),
                new BigDecimal("4.00"), PaymentStatus.PENDING);
        entityManager.persistAndFlush(commission);

        Optional<Commission> found = commissionRepository.findBySaleId(sale.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCommissionAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldEnforceOneToOneUniqueConstraintWithSale() {
        Sale sale = persistSale();
        Commission first = new Commission(UUID.randomUUID(), sale, new BigDecimal("20.00"),
                new BigDecimal("4.00"), PaymentStatus.PENDING);
        entityManager.persistAndFlush(first);

        Commission duplicate = new Commission(UUID.randomUUID(), sale, new BigDecimal("20.00"),
                new BigDecimal("4.00"), PaymentStatus.PENDING);

        assertThatThrownBy(() -> entityManager.persistAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
