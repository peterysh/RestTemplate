package toy.payment.retry;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import toy.payment.payment.PaymentService;
import toy.payment.user.Member;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
class RetryAspectTest {

    @Autowired
    PaymentService paymentService;
    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();

    Method paymentMethod;
    @BeforeEach
    public void init() throws NoSuchMethodException {
        paymentMethod = PaymentService.class.getMethod("tradingWithOptions",
                Long.class, Long.class, Long.class, int.class, boolean.class);
    }

    @Test
    void success(){
        log.info("paymentService Proxy={}", paymentService.getClass());
        pointcut.setExpression("@annotation(toy.payment.retry.Retry)");
        assertThat(pointcut.matches(paymentMethod, PaymentService.class)).isTrue();
    }
}