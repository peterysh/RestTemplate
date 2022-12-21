package toy.payment.payment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.jta.ManagedTransactionAdapter;
import toy.payment.exception.ExternalError;
import toy.payment.exception.MinusPointException;
import toy.payment.payment.domain.Payment;
import toy.payment.payment.domain.PaymentComplete;
import toy.payment.retry.Retry;
import toy.payment.user.Member;
import toy.payment.user.MemberRepository;
import toy.payment.user.MemberService;

import java.util.List;

import static toy.payment.payment.domain.PaymentComplete.COMPLETE;

@Slf4j
@Service
//@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentService {

    @PersistenceContext
    private final EntityManager em;
    private final PaymentRepository paymentRepository;

    private final MemberService memberService;


    @Transactional
    public void tradingWithOptions(Long senderId, Long receiverId, Long movePoints,
                                   int millis, boolean systemOut, Payment send, Payment receive){
        Member sender = memberService.findMemberById(senderId);
        Member receiver = memberService.findMemberById(receiverId);

        if(isDuplicateReq(send, receive, sender, receiver)){
            log.info("중복된 요청");
            return;
        }

        tracePoint(movePoints, sender, receiver, millis, systemOut, send, receive);
    }

    private boolean isDuplicateReq(Payment send, Payment receive, Member sender, Member receiver) {
        return receive.getPaymentComplete() == COMPLETE && send.getPaymentComplete() == COMPLETE
                && send.getTraceId() == receive.getTraceId() && sender.getPaymentList().contains(send)
                && receiver.getPaymentList().contains(receive);
    }

    public void tracePoint(Long movePoints, Member sender, Member receiver, int millis, boolean systemOut
                            ,Payment send, Payment receive) {
        send.setMember(sender);
        receive.setMember(receiver);

        checkSendercanSend(movePoints, sender);

        sleep(millis);//지연 시간

        log.info("거래 시작");
        log.info("{} 송금 시작. 거래 전: {}", sender.getId(), sender.getPoints());
        paymentRepository.pushPayment(send);
        sender.subPoint(movePoints);

        if(systemOut) {//외부적인 요인으로 시스템 다운
            log.info("외부적인 요인으로 시스템 다운");
            throw new ExternalError();
        }
        log.info("{} 수신 시작. 거래 전: {}", receiver.getId(), receiver.getPoints());
        paymentRepository.pushPayment(receive);
        receiver.addPoint(movePoints);

        log.info("거래 완료. 송신자: {}, 수신자: {}", sender.getPoints(), receiver.getPoints());

        send.completeTrade();
        receive.completeTrade();
        em.flush();
    }

    private void checkSendercanSend(Long movePoints, Member sender) {
        log.info("송신자: {} 가 송금 할 수 있는지?", sender.getId());
        if(!sender.checkWhetherSend(movePoints)){
            log.info("송신자 {}의 금액이 부족합니다",sender.getId());
            throw new MinusPointException();
        }
        log.info("송금 가능");
    }
    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
