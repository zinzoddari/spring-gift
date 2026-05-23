package gift.member.service;

import gift.member.domain.Member;
import gift.member.dto.AdminMemberResponse;
import gift.member.repository.MemberRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public AdminMemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminMemberResponse> getMembers() {
        return memberRepository.findAll().stream()
            .map(AdminMemberResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public AdminMemberResponse getMember(final Long id) {
        return AdminMemberResponse.from(findMember(id));
    }

    @Transactional
    public void createMember(final String email, final String password) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        memberRepository.save(new Member(email, password));
    }

    @Transactional
    public void updateMember(final Long id, final String email, final String password) {
        findMember(id).update(email, password);
    }

    @Transactional
    public void chargePoint(final Long id, final int amount) {
        findMember(id).chargePoint(amount);
    }

    @Transactional
    public void deleteMember(final Long id) {
        memberRepository.deleteById(id);
    }

    private Member findMember(final Long id) {
        return memberRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Member not found. id=" + id));
    }
}
