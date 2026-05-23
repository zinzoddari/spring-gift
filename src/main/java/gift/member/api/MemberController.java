package gift.member.api;

import gift.auth.dto.TokenResponse;
import gift.member.dto.MemberRequest;
import gift.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
class MemberController {
    private final MemberService memberService;

    MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody final MemberRequest request) {
        final String token = memberService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenResponse(token));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody final MemberRequest request) {
        return new TokenResponse(memberService.login(request));
    }
}
