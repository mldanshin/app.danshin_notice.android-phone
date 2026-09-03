package net.danshin.danshinnoticeandroidphone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.danshin.danshinnoticeandroidphone.App;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.SettingsResponse;
import net.danshin.danshinnoticeandroidphone.services.AuthService;
import net.danshin.danshinnoticeandroidphone.services.GenealogyDatesSettingService;

import java.time.ZoneId;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        final PendingResult pendingResult = goAsync();

        new Thread(() -> {
            try {
                doWork(context);
            } catch (Exception e) {
                Log.e("BootReceiver", "Error in BootReceiver", e);
            } finally {
                pendingResult.finish();
            }
        }).start();
    }

    private void doWork(Context context) {
        AuthService auth = new AuthService(context);
        String token = auth.getToken();
        if (token == null) return;

        SettingsResponse settings = new GenealogyDatesSettingService(context).getSetting(token);
        if (settings == null) return;

        App.scheduleNext(context, settings.sendTime(), ZoneId.of(settings.timezone()));
    }
}