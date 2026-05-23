package gift.member.dto;

import gift.member.domain.Member;

public record AdminMemberResponse(
    Long id,
    String email,
    String password,
    int point
) {
    public static AdminMemberResponse from(final Member member) {
        return new AdminMemberResponse(member.getId(), member.getEmail(), member.getPassword(), member.getPoint());
    }
}
