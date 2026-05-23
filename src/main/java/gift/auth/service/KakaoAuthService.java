package gift.auth.service;

import gift.auth.JwtProvider;
import gift.infra.kakao.KakaoLoginAdapter;
import gift.infra.kakao.KakaoLoginProperties;
import gift.member.Member;
import gift.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoAuthService {
    private final KakaoLoginProperties properties;
    private final KakaoLoginAdapter kakaoLoginAdapter;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    public KakaoAuthService(
        KakaoLoginProperties properties,
        KakaoLoginAdapter kakaoLoginAdapter,
        MemberRepository memberRepository,
        JwtProvider jwtProvider
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

    public String login(String code) {
        KakaoLoginAdapter.KakaoTokenResponse kakaoToken = kakaoLoginAdapter.requestAccessToken(code);
        KakaoLoginAdapter.KakaoUserResponse kakaoUser = kakaoLoginAdapter.requestUserInfo(kakaoToken.accessToken());

        Member member = memberRepository.findByEmail(kakaoUser.email())
            .orElseGet(() -> new Member(kakaoUser.email()));
        member.updateKakaoAccessToken(kakaoToken.accessToken());
        memberRepository.save(member);

        return jwtProvider.createToken(member.getEmail());
    }
}
