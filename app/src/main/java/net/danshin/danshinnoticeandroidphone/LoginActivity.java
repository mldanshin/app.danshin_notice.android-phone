package net.danshin.danshinnoticeandroidphone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.danshin.danshinnoticeandroidphone.config.BuildConfig;
import net.danshin.danshinnoticeandroidphone.models.auth.AuthResponse;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenRequest;
import net.danshin.danshinnoticeandroidphone.models.auth.RefreshTokenResponse;
import net.danshin.danshinnoticeandroidphone.services.AuthService;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = new AuthService(this);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(_ -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_message_fields_empty), Toast.LENGTH_LONG).show();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            String apiUrl = BuildConfig.getLoginUrl();

            NetworkUtils.postJson(apiUrl, json.toString(), new NetworkUtils.Callback() {
                @Override
                public void onSuccess(int code, String responseBody) {
                    runOnUiThread(() -> {
                        if (code == 200) {
                            try {
                                AuthResponse authResponse = AuthResponse.fromJson(responseBody);
                                if (authService.hasAllowedRole(authResponse.roles())) {
                                    refreshTokenAfterLogin(authResponse);
                                } else {
                                    Toast.makeText(LoginActivity.this, getString(R.string.message_403), Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, getString(R.string.message_error_handle) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else if (code == 400) {
                            try {
                                JSONObject errorJson = new JSONObject(responseBody);
                                String message = errorJson.optString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, getString(R.string.login_message_fields_invalid), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.message_error_server) + ": " + code, Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(LoginActivity.this, getString(R.string.message_error_network) + ": " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
                }
            });
        } catch (Exception _) {
        }
    }

    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void refreshTokenAfterLogin(AuthResponse authResponse) throws Exception {
        RefreshTokenRequest request = authService.createRefreshRequest(authResponse.token());

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
                                Toast.makeText(LoginActivity.this, getString(R.string.message_403), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, getString(R.string.message_error_handle), Toast.LENGTH_LONG).show();
                        }
                    } else if (code == 401) {
                        Toast.makeText(LoginActivity.this, getString(R.string.message_401), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.message_error_server) + ": " + code, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this, getString(R.string.message_error_network) + ": " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        });
    }
}