package toy.payment.user;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import toy.payment.payment.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    Member member;
    Long memberId;

    @Test
    @DisplayName("멤버 추가 테스트")
    public void setUpDB(){
        long points = 3000l;
        memberId = memberService.saveMember(points);
        member = memberService.findMemberById(memberId);

        assertThat(member.getId()).isEqualTo(memberId);
        assertThat(member.getPoints()).isEqualTo(points);
    }

    @Test
    public void lazyTest() throws Exception{
        //given//when
        setUpDB();
        //then
        Member referenceById = memberService.getReferenceById(memberId);
        System.out.println(referenceById.getPoints());
    }
}