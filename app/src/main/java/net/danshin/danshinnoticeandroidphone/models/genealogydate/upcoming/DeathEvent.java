package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONException;
import org.json.JSONObject;

public record DeathEvent(
        Date date,
        Person person,
        Age age,
        Interval interval
) {
    public static DeathEvent fromJson(JSONObject obj) throws JSONException {
        JSONObject dateObj = obj.optJSONObject("date");
        JSONObject personObj = obj.optJSONObject("person");
        JSONObject ageObj = obj.optJSONObject("age");
        JSONObject intervalObj = obj.optJSONObject("interval");

        return new DeathEvent(
                Date.fromJson(dateObj != null ? dateObj : new JSONObject()),
                Person.fromJson(personObj != null ? personObj : new JSONObject()),
                Age.fromJson(ageObj != null ? ageObj : new JSONObject()),
                (intervalObj != null ? Interval.fromJson(intervalObj) : null)
        );
    }
}
