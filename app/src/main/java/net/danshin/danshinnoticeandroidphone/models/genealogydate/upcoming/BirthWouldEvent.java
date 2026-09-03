package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONException;
import org.json.JSONObject;

public record BirthWouldEvent(
        Date date,
        Person person,
        Age age
) {
    public static BirthWouldEvent fromJson(JSONObject obj) throws JSONException {
        JSONObject dateObj = obj.optJSONObject("date");
        JSONObject personObj = obj.optJSONObject("person");
        JSONObject ageObj = obj.optJSONObject("age");

        return new BirthWouldEvent(
                Date.fromJson(dateObj != null ? dateObj : new JSONObject()),
                Person.fromJson(personObj != null ? personObj : new JSONObject()),
                Age.fromJson(ageObj != null ? ageObj : new JSONObject())
        );
    }
}
