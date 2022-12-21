package toy.payment.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import toy.payment.account.Account;
import toy.payment.exception.MinusPointException;
import toy.payment.payment.domain.Payment;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;


@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="MEMBER_ID")
    private Long id;

    @Min(0l)
    private Long points;

    @Override
    public String toString() {
        return id + " " + points;
    }

    @Version
    private Long version;

    @OneToMany(mappedBy = "member")
    private List<Payment> paymentList = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Account> accountList = new ArrayList<>();

    //==생성자 매서드==//
    public static Member createMember(){
        Member member = new Member();
        return member;
    }

    public static Member createMemberWithPoint(Long point){
        Member member = new Member();
        member.points =point;
        return member;
    }
    //==비즈니스 로직==//
    public Long addPoint(Long points){
        this.points += points;
        return this.points;
    }

    public Long subPoint(Long points){
        this.points -= points;
        return this.points;
    }

    public boolean checkWhetherSend(Long points){
        if(this.points - points < 0) {
            return false;
        }
        return true;
    }
}
