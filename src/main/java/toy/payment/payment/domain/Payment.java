package toy.payment.payment.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import toy.payment.user.Member;

import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;
import static toy.payment.payment.domain.PaymentComplete.COMPLETE;
import static toy.payment.payment.domain.PaymentComplete.NOT_COMPLETE;
import static toy.payment.payment.domain.PaymentType.RECEIVE;
import static toy.payment.payment.domain.PaymentType.SEND;

@Entity
@NoArgsConstructor(access = PROTECTED)
@Getter
@Slf4j
@ToString
@EqualsAndHashCode
public class Payment {
    @Id
    @GeneratedValue
    private Long id;

    private String traceId;

    private Long movePoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="MEMBER_ID")
    private Member member;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentComplete paymentComplete;

    //==생성자 메서드==//
    public static Payment createSender(Long movePoint){
        Payment payment = new Payment();
        payment.traceId = UUID.randomUUID().toString();
        payment.paymentType = SEND;
        payment.movePoint = movePoint;
        payment.paymentComplete = NOT_COMPLETE;
        return payment;
    }

    public static Payment createReceiver(Long movePoint, String traceId){
        Payment payment = new Payment();
        payment.traceId = traceId;
        payment.paymentType = RECEIVE;
        payment.movePoint = movePoint;
        payment.paymentComplete = NOT_COMPLETE;
        return payment;
    }

    public void completeTrade(){
        this.paymentComplete = COMPLETE;
    }

    //==연관관계 편의 메서드==//
    public void setMember(Member member){
        member.getPaymentList().add(this);
        this.member = member;
    }
}
