package nl.radiantrealm.library.net.http.cookie;

public record SetCookie(
        Cookie cookie,
        String path,
        String domain,
        long maxAge,
        boolean httpOnly,
        boolean secure,
        String sameSite
) {
    public SetCookie(Cookie cookie) {
        this(
                cookie,
                null,
                null,
                -1,
                true,
                true,
                "Lax"
        );
    }

    public SetCookie(String name, String value) {
        this(new Cookie(name, value));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(cookie.toString());

        if (path != null) {
            builder.append("; Path=").append(path);
        }

        if (domain != null) {
            builder.append("; Domain=").append(domain);
        }

        if (maxAge >= 0) {
            builder.append("; Max-Age").append(maxAge);
        }

        if (httpOnly) {
            builder.append("; HttpOnly");
        }

        if (secure) {
            builder.append("; Secure");
        }

        if (sameSite != null) {
            builder.append("; SameSite=").append(sameSite);
        }

        return builder.toString();
    }
}
