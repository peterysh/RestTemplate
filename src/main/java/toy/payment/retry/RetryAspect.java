package toy.payment.retry;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.StaleObjectStateException;
import org.springframework.core.annotation.Order;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import toy.payment.payment.PaymentService;

@Slf4j
@Component
@Aspect
@RequiredArgsConstructor
public class RetryAspect {
    private final PaymentService paymentService;

    @Around("@annotation(retry)")
    public Object doRetry(ProceedingJoinPoint joinPoint, Retry retry) throws Throwable {
        int maxTry = retry.value();

        Exception exceptionHolder = null;
        for(int retryCount=1;retryCount <= maxTry;retryCount++){
            try{
                log.info("{} 시도",retryCount);
                return joinPoint.proceed();
            }catch(OptimisticLockException | ObjectOptimisticLockingFailureException e){
                exceptionHolder = e;
                log.info("lock 잠금 발생");
            }catch(Exception e){
                exceptionHolder = e;
                log.info("lock 이외의 오류 발생");
                break;
            }finally{
                log.info("시도 종료");
            }
        }
        throw exceptionHolder;
    }
}
