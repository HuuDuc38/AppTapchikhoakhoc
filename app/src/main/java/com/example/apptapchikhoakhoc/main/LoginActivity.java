package com.example.apptapchikhoakhoc.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.register.RegisterActivity;
import com.example.apptapchikhoakhoc.utils.TextSizeManager;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends BaseActivity {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME  = "name";

    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton    btnLogin;
    private TextView          tvRegister, tvForgotPassword, tvTitle, tvSubtitle;
    private CircularProgressIndicator progressLoading;
    private ImageView         btnBack;
    private TextInputLayout   tilEmail, tilPassword;
    private DatabaseHelper    dbHelper;
    private View              rootView;

    private boolean redirectComment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        redirectComment = getIntent().getBooleanExtra("redirect_comment", false);

        edtEmail         = findViewById(R.id.edtEmail);
        edtPassword      = findViewById(R.id.edtPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        tvRegister       = findViewById(R.id.tvRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressLoading  = findViewById(R.id.progressLoading);
        btnBack          = findViewById(R.id.btnBack);
        tilEmail         = findViewById(R.id.tilEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        tvTitle          = findViewById(R.id.tvTitle);
        tvSubtitle       = findViewById(R.id.tvSubtitle);
        rootView         = findViewById(R.id.rootScrollView);

        applyDarkModeTheme();

        btnBack.setOnClickListener(v -> {
            if (redirectComment) setResult(RESULT_CANCELED);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Vui lòng nhập email hợp lệ để khôi phục", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Đã gửi link đặt lại mật khẩu đến " + email, Toast.LENGTH_LONG).show();
            }
        });

        btnLogin.setOnClickListener(v -> {
            String email    = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ Email và Mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading(true);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                new Thread(() -> {
                    boolean loginSuccess = dbHelper.checkLogin(email, password);
                    String userName = loginSuccess ? dbHelper.getUserName(email) : "";

                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;

                        if (loginSuccess) {
                            SharedPreferences sp = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                            sp.edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString(KEY_NAME, userName)
                                    .putString(KEY_EMAIL, email)
                                    .apply();

                            getSharedPreferences("UserSession", MODE_PRIVATE).edit()
                                    .putBoolean("isLoggedIn", true)
                                    .putString("userName", userName)
                                    .putString("userEmail", email)
                                    .apply();

                            Toast.makeText(LoginActivity.this,
                                    "Đăng nhập thành công!\nXin chào " + userName,
                                    Toast.LENGTH_SHORT).show();

                            if (redirectComment) {
                                setResult(RESULT_OK);
                                finish();
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            } else {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            }
                            return;
                        }

                        showLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Email hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }, 1500);
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            if (redirectComment) intent.putExtra("redirect_comment", true);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME
    // ══════════════════════════════════════════════════════════════

    private void applyDarkModeTheme() {

        // ── 1. Background ──────────────────────────────────────────
        if (rootView != null) {
            rootView.setBackgroundColor(isDarkMode
                    ? ThemeManager.DarkColors.BACKGROUND
                    : Color.parseColor("#F5F7FA"));
        }

        // ── 2. Tiêu đề ────────────────────────────────────────────
        if (tvTitle != null) {
            tvTitle.setTextColor(isDarkMode
                    ? Color.WHITE
                    : Color.parseColor("#1A1A1A"));
        }
        if (tvSubtitle != null) {
            tvSubtitle.setTextColor(isDarkMode
                    ? ThemeManager.DarkColors.TEXT_SECONDARY
                    : Color.parseColor("#666666"));
        }

        // ── 3. Màu dùng chung ─────────────────────────────────────
        int accentColor     = isDarkMode ? ThemeManager.DarkColors.ACCENT : Color.parseColor("#2D68C4");
        int hintColor       = isDarkMode ? Color.parseColor("#7A8A9A")     : Color.parseColor("#888888");
        int strokeFocused   = accentColor;
        int strokeUnfocused = isDarkMode ? Color.parseColor("#4A5568")     : Color.parseColor("#CCCCCC");
        int boxBgColor      = isDarkMode ? Color.parseColor("#2A3A4D")     : Color.WHITE;
        int editTextColor   = isDarkMode ? Color.WHITE                     : Color.parseColor("#1A1A1A");

        // ── 4. TextInputLayout Email ──────────────────────────────
        if (tilEmail != null) {
            applyInputLayout(tilEmail, hintColor, accentColor, strokeFocused, strokeUnfocused, boxBgColor);
        }

        // ── 5. TextInputLayout Password ───────────────────────────
        if (tilPassword != null) {
            applyInputLayout(tilPassword, hintColor, accentColor, strokeFocused, strokeUnfocused, boxBgColor);
            tilPassword.setEndIconTintList(ColorStateList.valueOf(
                    isDarkMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#888888")));
        }

        // ── 6. Text & hint bên trong EditText ─────────────────────
        // FIX AUTOFILL: set màu nền trong suốt để loại bỏ highlight vàng của autofill
        if (edtEmail != null) {
            edtEmail.setTextColor(editTextColor);
            edtEmail.setHintTextColor(hintColor);
            edtEmail.setHighlightColor(Color.TRANSPARENT);
            // Xoá highlight autofill màu vàng
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                edtEmail.setAutofillHints((String[]) null);
            }
        }
        if (edtPassword != null) {
            edtPassword.setTextColor(editTextColor);
            edtPassword.setHintTextColor(hintColor);
            edtPassword.setHighlightColor(Color.TRANSPARENT);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                edtPassword.setAutofillHints((String[]) null);
            }
        }

        // ── 7. Links ──────────────────────────────────────────────
        if (tvForgotPassword != null) tvForgotPassword.setTextColor(accentColor);
        if (tvRegister       != null) tvRegister.setTextColor(accentColor);

        // ── 8. Nút back ───────────────────────────────────────────
        if (btnBack != null) {
            btnBack.setColorFilter(isDarkMode
                    ? ThemeManager.DarkColors.TEXT_SECONDARY
                    : Color.parseColor("#666666"));
        }
    }

    /**
     * Áp dụng style đồng nhất cho 1 TextInputLayout.
     */
    private void applyInputLayout(TextInputLayout til,
                                  int hintColor,
                                  int accentColor,
                                  int strokeFocused,
                                  int strokeUnfocused,
                                  int boxBgColor) {
        til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        til.setBoxBackgroundColor(boxBgColor);

        // Hint label: focused = accent, unfocused = hint
        int[][] hintStates = {
                new int[]{ android.R.attr.state_focused },
                new int[]{}
        };
        til.setHintTextColor(new ColorStateList(hintStates,
                new int[]{ accentColor, hintColor }));

        // Stroke: focused = accent, unfocused = muted
        int[][] strokeStates = {
                new int[]{ android.R.attr.state_focused },
                new int[]{}
        };
        til.setBoxStrokeColorStateList(new ColorStateList(strokeStates,
                new int[]{ strokeFocused, strokeUnfocused }));
    }

    // ══════════════════════════════════════════════════════════════
    //  LOADING
    // ══════════════════════════════════════════════════════════════

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnLogin.setText("");
            progressLoading.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            btnLogin.setText("Đăng nhập");
            progressLoading.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (redirectComment) setResult(RESULT_CANCELED);
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}