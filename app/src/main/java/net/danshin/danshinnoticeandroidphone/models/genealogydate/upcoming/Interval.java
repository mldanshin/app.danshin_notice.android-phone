package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONObject;

public record Interval(
        int y,
        int m,
        int d
) {
    public static Interval fromJson(JSONObject obj) {
        return new Interval(
                obj.optInt("y"),
                obj.optInt("m"),
                obj.optInt("d")
        );
    }
}