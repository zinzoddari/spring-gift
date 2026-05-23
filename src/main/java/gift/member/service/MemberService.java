package gift.member.service;

import gift.infra.jwt.JwtProvider;
import gift.member.domain.Member;
import gift.member.dto.MemberRequest;
import gift.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public MemberService(final MemberRepository memberRepository, final JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
    }

    @Transactional
    public String register(final MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        final Member member = memberRepository.save(Member.withCredentials(request.email(), request.password()));

        return jwtProvider.createToken(member.getEmail());
    }

    @Transactional(readOnly = true)
    public String login(final MemberRequest request) {
        final Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!member.matchesPassword(request.password())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return jwtProvider.createToken(member.getEmail());
    }
}
