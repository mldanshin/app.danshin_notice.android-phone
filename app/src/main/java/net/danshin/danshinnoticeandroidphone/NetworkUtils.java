package net.danshin.danshinnoticeandroidphone;

import androidx.annotation.NonNull;

import net.danshin.danshinnoticeandroidphone.config.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkUtils {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void getJson(String url, String token, Callback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL apiUrl = new URL(url);
                connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                if (BuildConfig.isDebug()) {
                    connection.setRequestProperty("Host", BuildConfig.getGenealogySiteBaseUrl());
                }

                if (token != null) {
                    connection.setRequestProperty("Authorization", "Bearer " + token);
                }

                int responseCode = connection.getResponseCode();
                StringBuilder response = getBuilder(connection);
                callback.onSuccess(responseCode, response.toString());
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    public static void postJson(String url, String jsonBody, Callback callback) {
        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL apiUrl = new URL(url);
                connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);

                if (jsonBody != null) {
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                }

                int responseCode = connection.getResponseCode();
                StringBuilder response = getBuilder(connection);
                callback.onSuccess(responseCode, response.toString());
            } catch (Exception e) {
                callback.onError(e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @NonNull
    private static StringBuilder getBuilder(HttpURLConnection connection) throws IOException {
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
        }
        return response;
    }

    public interface Callback {
        void onSuccess(int code, String responseBody);

        void onError(Exception e);
    }
}