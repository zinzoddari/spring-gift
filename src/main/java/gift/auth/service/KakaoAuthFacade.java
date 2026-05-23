package gift.auth.service;

import gift.auth.JwtProvider;
import gift.member.Member;
import org.springframework.stereotype.Service;

@Service
public class KakaoAuthFacade {
    private final KakaoAuthService kakaoAuthService;
    private final JwtProvider jwtProvider;

    public KakaoAuthFacade(KakaoAuthService kakaoAuthService, JwtProvider jwtProvider) {
        this.kakaoAuthService = kakaoAuthService;
        this.jwtProvider = jwtProvider;
    }

    /**
     * Kakao 로그인 페이지로 리다이렉트하기 위한 URL을 반환합니다.
     * KakaoAuthService에서 생성된 URL을 그대로 전달합니다.
     */
    public String loginUrl() {
        return kakaoAuthService.loginUrl();
    }

    public String login(String code) {
        final Member member = kakaoAuthService.login(code);
        return jwtProvider.createToken(member.getEmail());
    }
}
