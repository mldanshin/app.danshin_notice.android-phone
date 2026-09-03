package net.danshin.danshinnoticeandroidphone;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.receivers.NotificationReceiver;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class App extends Application {
    public static final int REQUEST_CODE = 1001;

    public static void scheduleNext(Context context, LocalTime sendTime, ZoneId zone) {
        try {
            LocalDateTime now = LocalDateTime.now(zone);
            LocalDateTime next = now.toLocalDate().atTime(sendTime);
            if (!next.isAfter(now)) {
                next = next.plusDays(1);
            }

            long triggerAtMillis = next
                    .atZone(zone)
                    .toInstant()
                    .toEpochMilli();

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pi = buildPendingIntent(context, false, null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
                } else {
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
                }
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            }
        } catch (Exception e) {
        }

    }

    public static void triggerNow(Context context, String debugDate) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = buildPendingIntent(context, true, debugDate);
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pi);
    }

    private static PendingIntent buildPendingIntent(Context context, boolean manual, String debugDate) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("manual", manual);
        if (debugDate != null) intent.putExtra("debugDate", debugDate);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        BuildConfig.init(this);
    }
}
