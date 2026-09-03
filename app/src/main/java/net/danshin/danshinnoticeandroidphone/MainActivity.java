package net.danshin.danshinnoticeandroidphone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.Message;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.SettingsResponse;
import net.danshin.danshinnoticeandroidphone.models.genealogydate.upcoming.UpcomingResponse;
import net.danshin.danshinnoticeandroidphone.services.AuthService;
import net.danshin.danshinnoticeandroidphone.services.GenealogyDatesSettingService;
import net.danshin.danshinnoticeandroidphone.services.GenealogyDatesUpcomingService;
import net.danshin.danshinnoticeandroidphone.services.MessageFormatterService;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private final ExecutorService eventsExecutor = Executors.newSingleThreadExecutor();
    private AuthService authService;
    private TextView eventsPreview;
    private LinearLayout eventsBlock;
    private TextView settingsTitle;
    private TextView sendTimeView, timezoneView, dayBeforeView, dayAfterView, siteLink, settingInfo;
    private ProgressBar progressBar;
    private Button sendNoticeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authService = new AuthService(this);
        bindViews();
        loadSettings();

        showAutostartDialog();
    }

    @Override
    protected void onDestroy() {
        eventsExecutor.shutdownNow();
        super.onDestroy();
    }

    private void bindViews() {
        eventsPreview = findViewById(R.id.eventsPreview);
        eventsBlock = findViewById(R.id.eventsBlock);
        settingsTitle = findViewById(R.id.settingsTitle);
        sendTimeView = findViewById(R.id.sendTime);
        timezoneView = findViewById(R.id.timezone);
        dayBeforeView = findViewById(R.id.dayBefore);
        dayAfterView = findViewById(R.id.dayAfter);
        siteLink = findViewById(R.id.siteLink);
        settingInfo = findViewById(R.id.settingInfo);
        progressBar = findViewById(R.id.progressBar);
        sendNoticeButton = findViewById(R.id.sendNoticeButton);
    }

    private void loadSettings() {
        String token = authService.getToken();
        if (token == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        hideDataViews();

        GenealogyDatesSettingService service = new GenealogyDatesSettingService(this);
        service.fetchSetting(token,
                settings -> runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    loadEventsPreview(token, settings);
                    displaySettings(settings);
                    showDataViews();

                    App.scheduleNext(
                            this,
                            settings.sendTime(),
                            ZoneId.of(settings.timezone())
                    );
                }),
                error -> runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    if ("401".equals(error)) {
                        showErrorAndLogout(getString(R.string.message_401));
                    } else {
                        showError(error);
                    }
                })
        );
    }

    private void loadEventsPreview(String token, SettingsResponse settings) {
        eventsExecutor.execute(() -> {
            try {
                ZoneId zone = ZoneId.of(settings.timezone());
                LocalDate date;
                if (BuildConfig.isDebug()) {
                    date = LocalDate.parse("2026-08-22");
                } else {
                    date = LocalDate.now(zone);
                }

                GenealogyDatesUpcomingService upcomingService = new GenealogyDatesUpcomingService();
                UpcomingResponse upcoming = upcomingService.getGenealogyData(token, date, settings);

                MessageFormatterService formatter = new MessageFormatterService(this);
                Message message = formatter.getMessage(upcoming);

                String text = message.text();

                runOnUiThread(() -> {
                    if (text == null || text.isEmpty()) {
                        eventsBlock.setVisibility(View.GONE);
                    } else {
                        eventsPreview.setText(text);
                        eventsBlock.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> eventsBlock.setVisibility(View.GONE));
            }
        });
    }

    private void displaySettings(SettingsResponse settings) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        sendTimeView.setText(getString(R.string.gen_dates_send_time, settings.sendTime().format(formatter)));
        timezoneView.setText(getString(R.string.gen_dates_timezone, settings.timezone()));
        dayBeforeView.setText(getString(R.string.gen_dates_day_before, settings.dayBefore()));
        dayAfterView.setText(getString(R.string.gen_dates_day_after, settings.dayAfter()));

        String text = getString(R.string.gen_dates_sites_setting);
        SpannableString spannableString = getSpannableString(text);

        siteLink.setText(spannableString);
        siteLink.setMovementMethod(LinkMovementMethod.getInstance());

        sendNoticeButton.setOnClickListener(_ -> sendNotice());
    }

    @androidx.annotation.NonNull
    private SpannableString getSpannableString(String text) {
        String url = BuildConfig.getGenealogySiteUrl();
        String fullText = text + " " + url;

        SpannableString spannableString = new SpannableString(fullText);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        };

        spannableString.setSpan(
                clickableSpan,
                text.length() + 1,
                fullText.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );
        return spannableString;
    }

    private void showErrorAndLogout(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        authService.clearAuth();

        android.os.Handler mainHandler = new android.os.Handler(Looper.getMainLooper());

        Runnable navigationTask = () -> {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        };

        mainHandler.postDelayed(navigationTask, 1500);
    }

    private void showError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    private void hideDataViews() {
        settingsTitle.setVisibility(View.GONE);
        sendTimeView.setVisibility(View.GONE);
        timezoneView.setVisibility(View.GONE);
        dayBeforeView.setVisibility(View.GONE);
        dayAfterView.setVisibility(View.GONE);
        siteLink.setVisibility(View.GONE);
        settingInfo.setVisibility(View.GONE);
        sendNoticeButton.setVisibility(View.GONE);
    }

    private void showDataViews() {
        settingsTitle.setVisibility(View.VISIBLE);
        sendTimeView.setVisibility(View.VISIBLE);
        timezoneView.setVisibility(View.VISIBLE);
        dayBeforeView.setVisibility(View.VISIBLE);
        dayAfterView.setVisibility(View.VISIBLE);
        siteLink.setVisibility(View.VISIBLE);
        settingInfo.setVisibility(View.VISIBLE);
        sendNoticeButton.setVisibility(View.VISIBLE);
    }

    private void sendNotice() {
        if (BuildConfig.isDebug()) {
            App.triggerNow(this, "2026-08-22");
        } else {
            App.triggerNow(this, null);
        }
    }

    private void showAutostartDialog() {
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("autostart_dialog_shown", false)) return;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.autostart_dialog_title))
                .setMessage(getString(R.string.autostart_dialog_message))
                .setPositiveButton(getString(R.string.autostart_dialog_button), (dialog, _) -> {
                    prefs.edit().putBoolean("autostart_dialog_shown", true).apply();
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }
}