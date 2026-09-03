package net.danshin.danshinnoticeandroidphone.models.auth;

import org.json.JSONObject;

public class RefreshTokenRequest {
    private final String token;
    private final Long expirationMs;

    public RefreshTokenRequest(String token, Long expirationMs) {
        this.token = token;
        this.expirationMs = expirationMs;
    }

    public String toJson() throws Exception {
        JSONObject json = new JSONObject();
        json.put("token", token);
        json.put("expirationMs", expirationMs);
        return json.toString();
    }
}
