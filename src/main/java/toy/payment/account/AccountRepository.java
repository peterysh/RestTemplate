package toy.payment.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    //@Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    public Account findByAccountId(long accountId);
}
