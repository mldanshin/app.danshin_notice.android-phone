package net.danshin.danshinnoticeandroidphone.receivers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import net.danshin.danshinnoticeandroidphone.App;
import net.danshin.danshinnoticeandroidphone.MainActivity;
import net.danshin.danshinnoticeandroidphone.NetworkUtils;
import net.danshin.danshinnoticeandroidphone.R;
import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenRequest;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenResponse;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.Message;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.SettingsResponse;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.UpcomingResponse;
import net.danshin.danshinnoticeandroidphone.services.AuthService;
import net.danshin.danshinnoticeandroidphone.services.GenealogyDatesSettingService;
import net.danshin.danshinnoticeandroidphone.services.GenealogyDatesUpcomingService;
import net.danshin.danshinnoticeandroidphone.services.MessageFormatterService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "genealogy_events";
    private static final int NOTIFICATION_ID = 1001;
    private static final int NOTIFICATION_MAX_LINES = 4;

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                doWork(context, intent);
            } catch (Exception e) {
                Log.e("NotificationReceiver", "Error in receiver", e);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private void doWork(Context context, Intent intent) {
        AuthService authService = new AuthService(context);
        String token = authService.getToken();
        if (token == null) return;

        try {
            if (authService.needsRefresh()) {
                Log.d("NotificationReceiver", "Token is stale, refreshing...");
                RefreshTokenRequest request = authService.createRefreshRequest(token);
                if (request == null) return;

                final CountDownLatch latch = new CountDownLatch(1);
                final boolean[] refreshSuccess = {false};

                NetworkUtils.postJson(BuildConfig.getRefreshTokenUrl(), request.toJson(), new NetworkUtils.Callback() {
                    @Override
                    public void onSuccess(int code, String responseBody) {
                        if (code == 200) {
                            try {
                                RefreshTokenResponse response = RefreshTokenResponse.fromJson(responseBody);
                                if (authService.hasAllowedRole(response.roles())) {
                                    authService.updateToken(response);
                                    refreshSuccess[0] = true;
                                    Log.d("NotificationReceiver", "Token refreshed successfully");
                                }
                            } catch (Exception e) {
                                Log.e("NotificationReceiver", "Failed to parse new token", e);
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("NotificationReceiver", "Network error while refreshing token", e);
                        latch.countDown();
                    }
                });

                if (!latch.await(10, TimeUnit.SECONDS)) {
                    Log.e("NotificationReceiver", "Token refresh timed out");
                    return;
                }

                if (!refreshSuccess[0]) {
                    Log.e("NotificationReceiver", "Failed to refresh token, aborting");
                    return;
                }

                token = authService.getToken();
                if (token == null) return;
            }

            boolean manual = intent.getBooleanExtra("manual", false);
            String debugDate = intent.getStringExtra("debugDate");

            GenealogyDatesSettingService service = new GenealogyDatesSettingService(context);
            SettingsResponse settings = service.getSetting(token);
            if (settings == null) return;

            LocalDate date;
            if (debugDate != null) {
                date = LocalDate.parse(debugDate);
            } else {
                ZoneId zone = ZoneId.of(settings.timezone());
                date = LocalDate.now(zone);
                if (!manual) {
                    LocalTime now = LocalTime.now(zone);
                    LocalTime sendTime = settings.sendTime();
                    if (now.isBefore(sendTime.minusMinutes(5)) || now.isAfter(sendTime.plusMinutes(5))) {
                        App.scheduleNext(context, sendTime, zone);
                        return;
                    }
                }
            }

            GenealogyDatesUpcomingService upcomingService = new GenealogyDatesUpcomingService();
            UpcomingResponse upcoming = upcomingService.getGenealogyData(token, date, settings);

            MessageFormatterService formatter = new MessageFormatterService(context);
            Message message = formatter.getMessage(upcoming);

            if (!message.isEmpty()) {
                sendNotification(context, message.text());
            }

            if (!manual) {
                App.scheduleNext(context, settings.sendTime(), ZoneId.of(settings.timezone()));
            }
        } catch (Exception e) {
            Log.e("NotificationReceiver", "doWork failed", e);
        }
    }

    private void sendNotification(Context context, String message) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "genealogy_events",
                NotificationManager.IMPORTANCE_HIGH
        );
        nm.createNotificationChannel(channel);

        String notificationText = limitForNotification(context, message);

        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        flags |= PendingIntent.FLAG_IMMUTABLE;
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, openAppIntent, flags);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(notificationText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        nm.notify(NOTIFICATION_ID, builder.build());
    }

    private String limitForNotification(Context context, String message) {
        if (message == null) return "";

        String[] lines = message.split("\n");
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : lines) {
            if (count >= NOTIFICATION_MAX_LINES) {
                sb.append("…\n");
                break;
            }
            if (sb.length() > 0) sb.append("\n");
            sb.append(line);
            count++;
        }
        sb.append("\n\n").append(context.getString(R.string.gen_dates_notification_more));
        return sb.toString();
    }
}