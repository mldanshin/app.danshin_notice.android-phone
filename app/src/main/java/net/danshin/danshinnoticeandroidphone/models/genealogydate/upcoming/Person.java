package net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record Person(
        int id,
        @Nullable String surname,
        @Nullable List<String> oldSurname,
        @Nullable String name,
        @Nullable String patronymic
) {
    public static Person fromJson(JSONObject obj) {
        return new Person(
                obj.optInt("id"),
                obj.optString("surname"),
                parseList(obj.optJSONArray("oldSurname")),
                obj.optString("name"),
                obj.optString("patronymic")
        );
    }

    private static List<String> parseList(JSONArray arr) {
        if (arr == null) return List.of();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            list.add(arr.optString(i));
        }
        return list;
    }
}
