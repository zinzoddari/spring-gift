package gift.auth.service;

import gift.auth.JwtProvider;
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

    /**
     * 주어진 인증 코드를 사용하여 Kakao 로그인 프로세스를 수행하고 JWT 토큰을 생성합니다.
     * KakaoAuthService를 통해 사용자 이메일을 조회한 후, JwtProvider를 이용하여 JWT를 반환합니다.
     */
    public String login(final String code) {
        return jwtProvider.createToken(kakaoAuthService.login(code));
    }
}
