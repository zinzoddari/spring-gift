package gift.auth.service;

import gift.infra.kakao.KakaoLoginAdapter;
import gift.infra.kakao.KakaoLoginProperties;
import gift.member.domain.Member;
import gift.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoAuthService {
    private final KakaoLoginProperties properties;
    private final KakaoLoginAdapter kakaoLoginAdapter;
    private final MemberRepository memberRepository;

    public KakaoAuthService(
        final KakaoLoginProperties properties,
        final KakaoLoginAdapter kakaoLoginAdapter,
        final MemberRepository memberRepository
    ) {
        this.properties = properties;
        this.kakaoLoginAdapter = kakaoLoginAdapter;
        this.memberRepository = memberRepository;
    }

    public String loginUrl() {
        return UriComponentsBuilder.fromUriString(properties.authBaseUrl() + "/oauth/authorize")
            .queryParam("response_type", "code")
            .queryParam("client_id", properties.clientId())
            .queryParam("redirect_uri", properties.redirectUri())
            .queryParam("scope", "account_email,talk_message")
            .build()
            .toUriString();
    }

    @Transactional
    public String login(final String code) {
        final KakaoLoginAdapter.KakaoTokenResponse kakaoToken = kakaoLoginAdapter.requestAccessToken(code);
        final KakaoLoginAdapter.KakaoUserResponse kakaoUser = kakaoLoginAdapter.requestUserInfo(kakaoToken.accessToken());

        Member member = memberRepository.findByEmail(kakaoUser.email())
            .orElseGet(() -> Member.withEmail(kakaoUser.email()));
        member.applyKakaoToken(kakaoToken.accessToken());

        memberRepository.save(member);

        return member.getEmail();
    }
}
