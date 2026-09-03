package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONObject;

public record Age(
        int y,
        int m,
        int d
) {
    public static Age fromJson(JSONObject obj) {
        return new Age(
                obj.optInt("y"),
                obj.optInt("m"),
                obj.optInt("d")
        );
    }
}
