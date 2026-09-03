package net.danshin.danshinnoticeandroidphone.services;

import android.content.Context;

import net.danshin.danshinnoticeandroidphone.NetworkUtils;
import net.danshin.danshinnoticeandroidphone.R;
import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.SettingsResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GenealogyDatesSettingService {
    private final Context context;

    public GenealogyDatesSettingService(Context context) {
        this.context = context;
    }

    public SettingsResponse getSetting(String token) {
        CountDownLatch latch = new CountDownLatch(1);
        final SettingsResponse[] result = {null};

        fetchSetting(token,
                settings -> {
                    result[0] = settings;
                    latch.countDown();
                },
                _ -> latch.countDown()
        );

        try {
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                return null;
            }
        } catch (InterruptedException _) {
            return null;
        }

        return result[0];
    }

    public void fetchSetting(String token, Consumer<SettingsResponse> onSuccess, Consumer<String> onError) {
        String url = BuildConfig.getGenealogyDateSettingUrl();
        NetworkUtils.getJson(url, token, new NetworkUtils.Callback() {
            @Override
            public void onSuccess(int code, String responseBody) {
                if (code == 200) {
                    try {
                        SettingsResponse settings = SettingsResponse.fromJson(responseBody);
                        onSuccess.accept(settings);
                    } catch (Exception e) {
                        onError.accept(context.getString(R.string.message_error_handle));
                    }
                } else if (code == 401) {
                    onError.accept("401");
                } else {
                    onError.accept(context.getString(R.string.message_error_load) + ": " + code);
                }
            }

            @Override
            public void onError(Exception e) {
                onError.accept(context.getString(R.string.message_error_network));
            }
        });
    }
}
