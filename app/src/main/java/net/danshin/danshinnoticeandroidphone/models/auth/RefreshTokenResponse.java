package net.danshin.danshinnoticeandroidphone.models.auth;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public record RefreshTokenResponse(String email, List<String> roles, String uuid, String token) {
    public static RefreshTokenResponse fromJson(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        String email = obj.optString("email");
        String uuid = obj.optString("uuid");
        String token = obj.optString("token");

        List<String> roles = new ArrayList<>();
        JSONArray rolesArray = obj.optJSONArray("roles");
        if (rolesArray != null) {
            for (int i = 0; i < rolesArray.length(); i++) {
                roles.add(rolesArray.getString(i));
            }
        }

        return new RefreshTokenResponse(email, roles, uuid, token);
    }
}
