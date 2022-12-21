package toy.payment.payment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import toy.payment.exception.MinusPointException;
import toy.payment.user.Member;
import toy.payment.user.MemberService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRetryService paymentRetryService;
    @PersistenceContext
    EntityManager em;

    Member memberA;
    Member memberB;
    Member target;

    Long APoint = 0l;
    Long BPoint = 0l;
    Long targetPoint = 5000l;

    @Before
    public void setUpDB(){
        Long memberAId = memberService.saveMember(APoint);
        Long memberBId = memberService.saveMember(BPoint);
        Long targetId = memberService.saveMember(targetPoint);
        memberA = memberService.findMemberById(memberAId);
        memberB = memberService.findMemberById(memberBId);
        target = memberService.findMemberById(targetId);
    }

    @Test
    @DisplayName("5000원인 사용자에게 2000원과 4000원을 동시에 빼감.")
    @Rollback(false)
    public void concurrencyTest() throws InterruptedException {
        Runnable txA = () ->{
            /**
             * target has 5000
             * target -> A (2000).
             */
            paymentRetryService.try3Time(target.getId(),memberA.getId()
                    ,2000l,10,false);
        };
        Runnable txB = () ->{
            /**
             * target has 5000
             * target -> B(4000)
             */
            assertThrows(MinusPointException.class, () -> {
            paymentRetryService.try3Time(target.getId(), memberB.getId(), 4000l
                        , 1000, false);
            });
        };

        runTransactions(txA, txB);
        assertEquals(memberService.findMemberById(memberA.getId()).getPoints(), 2000l);
        assertEquals(memberService.findMemberById(memberB.getId()).getPoints(), 0l);
    }

    @Test
    @DisplayName("5000원인 사용자에게 2000원과 4000원을 동시에 빼가는 도중 2000원 트랜잭션이 강제 종료 됨.")
    @Rollback(false)
    public void concurrencyExitTest() throws InterruptedException {
        Runnable txA = () ->{
            /**
             * target: 5000
             * target -> A(2000) but system error(ex, shutdonwn computer)
             */
            paymentRetryService.try3Time(target.getId(),memberA.getId()
                    ,2000l,10,true);
        };
        Runnable txB = () ->{
            /**
             * target: 5000
             * target -> B(4000)
             */
            paymentRetryService.try3Time(target.getId(), memberB.getId(), 4000l
                    , 1000, false);
        };

        runTransactions(txA, txB);
    }

    @Test
    @DisplayName("5000원인 사용자에게 2000원을 제공 후 3000원을 제공하여 1000원이 됨")
    @Rollback(false)
    public void doublePlusTest() throws InterruptedException {
        Runnable txA = () ->{
            /**
             * target: 5000
             * A -> target(2000)
             */
            memberService.addPoints(memberA.getId(),2000l);
            paymentRetryService.try3Time(memberA.getId(),target.getId()
                    ,2000l,0,false);
        };
        Runnable txB = () ->{
            /**
             * target: 5000
             * B -> target(3000)
             */
            memberService.addPoints(memberB.getId(), 3000l);
            paymentRetryService.try3Time(memberB.getId(),target.getId(),3000l
                    , 0, false);
        };

        runTransactions(txA, txB);
        Member plusMember = memberService.findMemberById(target.getId());
        /**
         * thanks to @Retry, this test codes are passed!
         */
        assertEquals(plusMember.getPoints(), 10000l);
    }

    @Test
    @DisplayName("5000원인 사용자가 3000원 인출이 성공했음에도 여러번 요청할 경우. 멱등성 테스트")
    @Rollback(false)
    public void duplicateRequestAfterSuccess() throws InterruptedException {
        Runnable txA = () ->{
            /**
             * target: 5000
             * target -> A(3000) try many times, but there should be one trade. (idempotent test)
             */
            paymentRetryService.tryManyTime(target.getId(), memberA.getId()
                    , 2000l, 0, false);
        };
        Runnable txB = () ->{
            /**
             * doNothing
             */
        };

        runTransactions(txA, txB);
        Member plusMember = memberService.findMemberById(target.getId());
        assertEquals(plusMember.getPoints(), 3000l);
    }

    private  void runTransactions(Runnable... tx) throws InterruptedException {
        Thread threadA = new Thread(tx[0]);
        threadA.setName("thread-A");
        Thread threadB = new Thread(tx[1]);
        threadB.setName("thread-B");

        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
    }


    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}