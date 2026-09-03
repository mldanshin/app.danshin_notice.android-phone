package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONObject;

public record Date(
        String year,
        String month,
        String day
) {
    public static Date fromJson(JSONObject obj) {
        return new Date(
                obj.optString("year"),
                obj.optString("month"),
                obj.optString("day")
        );
    }
}
