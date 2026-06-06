package gift.auth.service;

import gift.infra.client.kakao.KakaoLoginAdapter;
import gift.infra.client.kakao.KakaoLoginProperties;
import gift.infra.jwt.JwtProvider;
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
    private final JwtProvider jwtProvider;

    public KakaoAuthService(
        final KakaoLoginProperties properties,
        final KakaoLoginAdapter kakaoLoginAdapter,
        final MemberRepository memberRepository,
        final JwtProvider jwtProvider
    ) {
        this.properties = properties;
        this.kakaoLoginAdapter = kakaoLoginAdapter;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
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

        return jwtProvider.createToken(member.getEmail());
    }
}
