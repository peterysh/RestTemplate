package toy.payment.account;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import toy.payment.user.Member;

@Service
@RequiredArgsConstructor
public class AccountService{

    private final AccountRepository accountRepository;

    @Transactional
    public long deposit(long accountId, long amount, Member member) {
        Account account = accountRepository.findByAccountId(accountId);
        long currBalance = account.getBalance();
        account.setBalance(currBalance + amount);
        accountRepository.save(account);
        return currBalance + amount;
    }

    @Transactional
    public void withdraw(long accountId, long amount) {
        Account account = accountRepository.findByAccountId(accountId);
        long currBalance = account.getBalance();
        if (currBalance - amount < 0) {
            throw new IllegalArgumentException("잔액이 부족합니다");
        }
        account.setBalance(currBalance - amount);

        accountRepository.save(account);
    }
}