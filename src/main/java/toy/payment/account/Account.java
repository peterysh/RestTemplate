package toy.payment.account;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import toy.payment.user.Member;


@Getter
@Setter
@Entity(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long accountId;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    //@Version
    private Long balance;

    @Version
    private Long version;

    public Account() {}

    public Account(String name, Member member) {
        this.name = name;
        this.balance = 0l;
        this.member = member;
    }

    public Account(String name) {
        this.name = name;
        this.balance = 0l;
    }
}
