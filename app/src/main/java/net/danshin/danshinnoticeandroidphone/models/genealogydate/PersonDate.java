package net.danshin.danshinnoticeandroidphone.models.genealogydate;

import org.json.JSONObject;

public record PersonDate(int personId, DateType dateType) {
    public static PersonDate fromJson(JSONObject obj) {
        int personId = obj.optInt("personId");
        String dateTypeStr = obj.optString("dateType");
        DateType dateType = DateType.fromString(dateTypeStr);
        return new PersonDate(personId, dateType);
    }
}
