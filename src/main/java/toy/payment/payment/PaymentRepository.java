package toy.payment.payment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import toy.payment.payment.domain.Payment;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    @PersistenceContext
    private final EntityManager em;

    public void pushPayment(Payment payment) {
        log.info("trade member: {}", payment.getMember().getId());
        em.persist(payment);
    }
}
