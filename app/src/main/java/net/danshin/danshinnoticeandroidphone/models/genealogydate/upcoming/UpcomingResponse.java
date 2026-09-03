package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record UpcomingResponse(
        @Nullable List<BirthEvent> beforeBirth,
        @Nullable List<BirthWouldEvent> beforeBirthWould,
        @Nullable List<DeathEvent> beforeDeath,
        @Nullable List<BirthEvent> todayBirth,
        @Nullable List<BirthWouldEvent> todayBirthWould,
        @Nullable List<DeathEvent> todayDeath,
        @Nullable List<BirthEvent> afterBirth,
        @Nullable List<BirthWouldEvent> afterBirthWould,
        @Nullable List<DeathEvent> afterDeath,
        boolean isEmpty
) {
    private UpcomingResponse(JSONObject obj) {
        this(
                parseList(obj.optJSONArray("beforeBirth"), BirthEvent::fromJson),
                parseList(obj.optJSONArray("beforeBirthWould"), BirthWouldEvent::fromJson),
                parseList(obj.optJSONArray("beforeDeath"), DeathEvent::fromJson),
                parseList(obj.optJSONArray("todayBirth"), BirthEvent::fromJson),
                parseList(obj.optJSONArray("todayBirthWould"), BirthWouldEvent::fromJson),
                parseList(obj.optJSONArray("todayDeath"), DeathEvent::fromJson),
                parseList(obj.optJSONArray("afterBirth"), BirthEvent::fromJson),
                parseList(obj.optJSONArray("afterBirthWould"), BirthWouldEvent::fromJson),
                parseList(obj.optJSONArray("afterDeath"), DeathEvent::fromJson),
                obj.optBoolean("isEmpty", false)
        );
    }

    public static UpcomingResponse fromJson(String json) throws JSONException {
        if (json == null || json.isEmpty()) {
            return new UpcomingResponse(new JSONObject());
        }
        return new UpcomingResponse(new JSONObject(json));
    }

    private static <T> List<T> parseList(JSONArray array, JsonMapper<T> mapper) {
        if (array == null) return List.of();
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                T item = mapper.map(array.getJSONObject(i));
                if (item != null) {
                    list.add(item);
                }
            } catch (JSONException ignored) {
            }
        }
        return list;
    }

    @FunctionalInterface
    private interface JsonMapper<T> {
        T map(JSONObject obj) throws JSONException;
    }
}
