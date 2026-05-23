package gift.infra.kakao;

enum KakaoPath {
    OAUTH_TOKEN("/oauth/token"),
    USER_ME("/v2/user/me"),
    SEND_MESSAGE("/v2/api/talk/memo/default/send");

    private final String path;

    KakaoPath(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
