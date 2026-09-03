package net.danshin.danshinnoticeandroidphone.models.genealogydate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public record SettingsResponse(
        String uuid,
        LocalTime sendTime,
        String timezone,
        int dayBefore,
        int dayAfter,
        List<PersonDate> dates
) {
    public static SettingsResponse fromJson(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        String uuid = obj.optString("uuid");
        String sendTimeStr = obj.optString("sendTime");
        String timezone = obj.optString("timezone");
        int dayBefore = obj.optInt("dayBefore");
        int dayAfter = obj.optInt("dayAfter");

        LocalTime sendTime = null;
        if (!sendTimeStr.isEmpty()) {
            sendTime = LocalTime.parse(sendTimeStr);
        }

        List<PersonDate> dates = new ArrayList<>();
        JSONArray datesArray = obj.optJSONArray("dates");
        if (datesArray != null) {
            for (int i = 0; i < datesArray.length(); i++) {
                dates.add(PersonDate.fromJson(datesArray.getJSONObject(i)));
            }
        }

        return new SettingsResponse(uuid, sendTime, timezone, dayBefore, dayAfter, dates);
    }
}
