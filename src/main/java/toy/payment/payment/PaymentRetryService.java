package toy.payment.payment;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import toy.payment.payment.PaymentService;
import toy.payment.payment.domain.Payment;
import toy.payment.retry.Retry;

@Service
@RequiredArgsConstructor
public class PaymentRetryService {
    private final PaymentService paymentService;

    @Retry
    public void try3Time(Long senderId, Long receiverId, Long movePoints,
                         int millis, boolean systemOut) {
        Payment send = Payment.createSender(movePoints);
        Payment receive = Payment.createReceiver(movePoints, send.getTraceId());
        paymentService.tradingWithOptions(senderId, receiverId, movePoints, millis, systemOut
        , send, receive);
    }

    public void tryManyTime(Long senderId, Long receiverId, Long movePoints,
                            int millis, boolean systemOut) {
        Payment send = Payment.createSender(movePoints);
        Payment receive = Payment.createReceiver(movePoints, send.getTraceId());
        for(int loop = 1;loop<=3;loop++){
            paymentService.tradingWithOptions(senderId, receiverId, movePoints, millis, systemOut
                    , send, receive);
        }
    }
}
