package gift.auth.api;

import gift.auth.dto.TokenResponse;
import gift.auth.service.KakaoAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth/kakao")
class KakaoAuthController {
    private final KakaoAuthService kakaoAuthService;

    public KakaoAuthController(final KakaoAuthService kakaoAuthService) {
        this.kakaoAuthService = kakaoAuthService;
    }

    @GetMapping(path = "/login")
    public ResponseEntity<Void> login() {
        return ResponseEntity.status(HttpStatus.FOUND)
            .header(HttpHeaders.LOCATION, kakaoAuthService.loginUrl())
            .build();
    }

    @GetMapping(path = "/callback")
    public TokenResponse callback(@RequestParam("code") final String code) {
        return new TokenResponse(kakaoAuthService.login(code));
    }
}
