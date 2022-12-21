package toy.payment.account;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import toy.payment.user.Member;
import toy.payment.user.MemberRepository;
import toy.payment.user.MemberService;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountServiceTest {

    private static final ExecutorService service = Executors.newFixedThreadPool(100);

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MemberService memberService;

    Member testUser;
    private long accountId;

    @BeforeEach
    public void setUp() {
        log.info("set up");
        Long userId = memberService.saveMember(0l);
        testUser = memberService.findMemberById(userId);
        Account account = new Account("우리 1002", testUser);
        account = accountRepository.save(account);
        accountId = account.getAccountId();
    }

    @Test
    public void SimultaneousDepositPassWithNoRaceCondition() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(100);
        for (int i=0; i < 10; i++) {
            service.execute(() -> {
                accountService.deposit(accountId, 10, testUser);
                latch.countDown();
            });
        }
        latch.await();
        Account richAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(richAccount.getBalance()).isEqualTo(10 * 10);
    }
    @Test
    public void SimultaneousDepositPassWithNoRaceConditionAndVersion() throws InterruptedException {
        Account account = new Account("신한 S20");
        account.setBalance(1000l);
        account = accountRepository.save(account);
        long accountId = account.getAccountId();
        int iter = 10;
        CountDownLatch latch = new CountDownLatch(iter);
        for (int i=0; i < iter; i++) {
            service.execute(() -> {
                while(true) {
                    try {
                        accountService.deposit(accountId, 10, null);
                        break;
                    } catch (Exception e) {
                        log.info("retry");
                    }
                }
                latch.countDown();
            });
        }
        latch.await();
        Account richAccount = accountRepository.findById(accountId).orElseThrow();
        assertThat(richAccount.getBalance()).isEqualTo(1000 + 10 * iter);
    }
}