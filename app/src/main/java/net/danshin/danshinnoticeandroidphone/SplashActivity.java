package net.danshin.danshinnoticeandroidphone;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenRequest;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenResponse;
import net.danshin.danshinnoticeandroidphone.services.AuthService;

public class SplashActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final String PERMISSION_ACCESS_LOCAL_NETWORK = "android.permission.ACCESS_LOCAL_NETWORK";
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 37 && BuildConfig.isDebug()) {
            if (checkSelfPermission(PERMISSION_ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{PERMISSION_ACCESS_LOCAL_NETWORK}, PERMISSION_REQUEST_CODE);
                return;
            }
        }

        if (!PermissionHelper.hasNotificationPermission(this)) {
            PermissionHelper.requestNotificationPermission(this);
        } else {
            authService = new AuthService(this);
            checkAuth();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!am.canScheduleExactAlarms()) {
                showExactAlarmDeniedDialog();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHelper.REQUEST_CODE_NOTIFICATIONS) {
            if (PermissionHelper.hasNotificationPermission(this)) {
                authService = new AuthService(this);
                checkAuth();
            } else {
                showNotificationPermissionDeniedDialog();
            }
        }
    }

    private void checkAuth() {
        String token = authService.getToken();
        if (token == null) {
            goToLogin();
            return;
        }

        if (!authService.hasAllowedRole()) {
            authService.clearAuth();
            goToLogin();
            return;
        }

        if (authService.needsRefresh()) {
            refreshToken();
        } else {
            goToMain();
        }
    }

    private void refreshToken() {
        RefreshTokenRequest request = authService.createRefreshRequest();
        if (request == null) {
            authService.clearAuth();
            goToLogin();
            return;
        }

        try {
            NetworkUtils.postJson(BuildConfig.getRefreshTokenUrl(), request.toJson(), new NetworkUtils.Callback() {
                @Override
                public void onSuccess(int code, String responseBody) {
                    runOnUiThread(() -> {
                        if (code == 200) {
                            try {
                                RefreshTokenResponse response = RefreshTokenResponse.fromJson(responseBody);

                                if (authService.hasAllowedRole(response.roles())) {
                                    authService.updateToken(response);
                                    goToMain();
                                } else {
                                    authService.clearAuth();
                                    goToLogin();
                                }
                            } catch (Exception e) {
                                authService.clearAuth();
                                goToLogin();
                            }
                        } else {
                            authService.clearAuth();
                            goToLogin();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        authService.clearAuth();
                        goToLogin();
                    });
                }
            });
        } catch (Exception e) {
            authService.clearAuth();
            goToLogin();
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void showNotificationPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_permission_required_title)
                .setMessage(R.string.notification_permission_required_message)
                .setCancelable(false)
                .setPositiveButton(R.string.go_to_settings, (_, _) -> openAppSettings())
                .setNegativeButton(R.string.exit_app, (_, _) -> finishAffinity())
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private void showExactAlarmDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exact_alarm_required_title)
                .setMessage(R.string.exact_alarm_required_message)
                .setCancelable(false)
                .setPositiveButton(R.string.go_to_settings, (_, _) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton(R.string.exit_app, (_, _) -> finishAffinity())
                .show();
    }
}