package toy.payment.user;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    public Long saveMember(Long point){
        Member member = Member.createMemberWithPoint(point);
        return memberRepository.save(member).getId();
    }
    @Transactional(readOnly = true)
    public Member findMemberById(Long id){
        log.info("find Member: {}", id);
        return memberRepository.findMemberByIdLock(id);
    }

    public Long addPoints(Long memberId, Long plusPoint){
        Member member = memberRepository.findById(memberId).get();
        return member.addPoint(plusPoint);
    }

    public Long subPoints(Long memberId, Long minusPoint){
        Member member = memberRepository.findById(memberId).get();
        return member.subPoint(minusPoint);
    }

    public Member getReferenceById(Long id){
        Member referenceById = memberRepository.getReferenceById(id);
        referenceById.getPoints();
        return referenceById;
    }
}
