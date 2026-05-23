package gift.member.dto;

public record MemberInfo(Long id) {

    public static MemberInfo from(final Long id) {
        return new MemberInfo(id);
    }
}
