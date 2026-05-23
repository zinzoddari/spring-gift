package gift.auth;

import gift.infra.exception.UnauthorizedException;
import gift.member.domain.Member;
import gift.member.dto.MemberInfo;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class MemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthenticationResolver authenticationResolver;

    public MemberArgumentResolver(final AuthenticationResolver authenticationResolver) {
        this.authenticationResolver = authenticationResolver;
    }

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(MemberInfo.class);
    }

    @Override
    public Object resolveArgument(
        final MethodParameter parameter,
        final ModelAndViewContainer mavContainer,
        final NativeWebRequest webRequest,
        final WebDataBinderFactory binderFactory
    ) {
        final String authorization = webRequest.getHeader("Authorization");
        final Member member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            throw new UnauthorizedException();
        }
        return MemberInfo.from(member.getId());
    }
}
