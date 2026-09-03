package net.danshin.danshinnoticeandroidphone.services;

import android.content.Context;
import android.content.SharedPreferences;

import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenRequest;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenResponse;

import java.util.List;

public class AuthService {

    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_UUID = "user_uuid";
    private static final String KEY_LAST_REFRESH = "last_refresh_time";
    private static final String KEY_ROLES = "user_roles";

    private static final long SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000;
    private static final long EIGHT_DAYS_MS = 8L * 24 * 60 * 60 * 1000;

    private static final List<String> ALLOWED_ROLES = List.of("ADMIN", "TRUSTED", "RELATIVE");

    private final SharedPreferences prefs;

    public AuthService(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void updateToken(RefreshTokenResponse response) {
        prefs.edit()
                .putString(KEY_TOKEN, response.token())
                .putString(KEY_EMAIL, response.email())
                .putString(KEY_UUID, response.uuid())
                .putString(KEY_ROLES, String.join(",", response.roles()))
                .putLong(KEY_LAST_REFRESH, System.currentTimeMillis())
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean needsRefresh() {
        long lastRefresh = prefs.getLong(KEY_LAST_REFRESH, 0);
        return System.currentTimeMillis() - lastRefresh >= SEVEN_DAYS_MS;
    }

    public boolean hasAllowedRole(List<String> roles) {
        for (String role : roles) {
            if (ALLOWED_ROLES.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllowedRole() {
        String rolesStr = prefs.getString(KEY_ROLES, "");
        if (rolesStr.isEmpty()) {
            return false;
        }
        List<String> roles = List.of(rolesStr.split(","));
        return hasAllowedRole(roles);
    }

    public void clearAuth() {
        prefs.edit().clear().apply();
    }

    public RefreshTokenRequest createRefreshRequest() {
        String token = getToken();
        if (token == null) {
            return null;
        }
        return new RefreshTokenRequest(token, EIGHT_DAYS_MS);
    }

    public RefreshTokenRequest createRefreshRequest(String token) {
        if (token == null) {
            return null;
        }
        return new RefreshTokenRequest(token, EIGHT_DAYS_MS);
    }
}