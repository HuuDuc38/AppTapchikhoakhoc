package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class LoginAdminActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private TextInputLayout   tilEmail, tilPassword;
    private MaterialButton    btnLogin;
    private CircularProgressIndicator progressLoading;
    private ImageButton       btnBack;
    private TextView          tvTitle, tvSubtitle, tvForgotPassword, tvContactAdmin;
    private RelativeLayout    rootLayout;

    private SharedPreferences prefs;
    private static final String PREFS_NAME  = "AdminLoginPrefs";
    private static final String KEY_EMAIL    = "email";
    private static final String KEY_PASSWORD = "password";

    private final Map<String, String> adminAccounts = new HashMap<>();
    private boolean isDark;

    // ══════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this); // ★ TRƯỚC setContentView
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_admin_login);

        isDark = ThemeManager.isDarkMode(this);

        initViews();
        applyDarkModeTheme();
        initTestAccounts();

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSavedLogin();
        setupClickListeners();
    }

    // ══════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════

    private void initViews() {
        rootLayout       = findViewById(R.id.rootLayout);
        btnBack          = findViewById(R.id.btnBack);
        tvTitle          = findViewById(R.id.tvTitle);
        tvSubtitle       = findViewById(R.id.tvSubtitle);
        tilEmail         = findViewById(R.id.tilEmail);
        edtEmail         = findViewById(R.id.edtEmail);
        tilPassword      = findViewById(R.id.tilPassword);
        edtPassword      = findViewById(R.id.edtPassword);
        btnLogin         = findViewById(R.id.btnLogin);
        progressLoading  = findViewById(R.id.progressLoading);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvContactAdmin   = findViewById(R.id.tvContactAdmin);
    }

    // ══════════════════════════════════════════════════════════
    //  DARK MODE
    // ══════════════════════════════════════════════════════════

    private void applyDarkModeTheme() {
        // ── Màu nền ────────────────────────────────────────────
        int bgColor     = isDark ? ThemeManager.DarkColors.BACKGROUND      : Color.parseColor("#F5F7FA");
        int txtPrimary  = isDark ? ThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSecond   = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#666666");
        int accentColor = isDark ? ThemeManager.DarkColors.ACCENT          : Color.parseColor("#2D68C4");
        int backTint    = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#666666");

        // ── Nền root ───────────────────────────────────────────
        if (rootLayout != null) rootLayout.setBackgroundColor(bgColor);

        // ── Tiêu đề ────────────────────────────────────────────
        if (tvTitle    != null) tvTitle.setTextColor(txtPrimary);
        if (tvSubtitle != null) tvSubtitle.setTextColor(txtSecond);

        // ── Nút back ───────────────────────────────────────────
        if (btnBack != null) btnBack.setColorFilter(backTint);

        // ── TextInputLayout: hint + stroke + text ──────────────
        applyInputLayoutTheme(tilEmail);
        applyInputLayoutTheme(tilPassword);

        if (edtEmail    != null) edtEmail.setTextColor(txtPrimary);
        if (edtPassword != null) edtPassword.setTextColor(txtPrimary);

        // ── Links ──────────────────────────────────────────────
        if (tvForgotPassword != null) tvForgotPassword.setTextColor(accentColor);
        if (tvContactAdmin   != null) tvContactAdmin.setTextColor(accentColor);

        // ── Nút đăng nhập: giữ màu xanh #2D68C4 ──────────────
        // (không thay đổi — vẫn nổi bật trên cả 2 mode)
    }

    /** Đổi màu hint, stroke, icon của TextInputLayout theo dark mode */
    private void applyInputLayoutTheme(TextInputLayout til) {
        if (til == null) return;

        int hintColor   = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#666666");
        int strokeColor = isDark ? ThemeManager.DarkColors.DIVIDER         : Color.parseColor("#CCCCCC");
        int focusColor  = isDark ? ThemeManager.DarkColors.ACCENT          : Color.parseColor("#2D68C4");
        int boxBg       = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;

        // Hint text color
        til.setHintTextColor(ColorStateList.valueOf(hintColor));

        // Box stroke color (normal + focused)
        til.setBoxStrokeColorStateList(new ColorStateList(
                new int[][]{
                        new int[]{ android.R.attr.state_focused },
                        new int[]{}
                },
                new int[]{ focusColor, strokeColor }
        ));

        // Box background color
        til.setBoxBackgroundColor(boxBg);

        // Password toggle icon tint
        til.setEndIconTintList(ColorStateList.valueOf(hintColor));
    }

    // ══════════════════════════════════════════════════════════
    //  ACCOUNTS & LOGIN
    // ══════════════════════════════════════════════════════════

    private void initTestAccounts() {
        adminAccounts.put("admin@vinhuni.edu.vn",      "admin123");
        adminAccounts.put("admin",                      "admin");
        adminAccounts.put("admin@tapchikhoahoc.vn",    "123456");
    }

    private void loadSavedLogin() {
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        String savedPass  = prefs.getString(KEY_PASSWORD, "");
        if (!savedEmail.isEmpty()) {
            edtEmail.setText(savedEmail);
            edtPassword.setText(savedPass);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> performLogin());

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Liên hệ IT: it@vinhuni.edu.vn", Toast.LENGTH_LONG).show());

        tvContactAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, ContactAdminActivity.class)));
    }

    private void performLogin() {
        String email = edtEmail.getText().toString().trim();
        String pass  = edtPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("");
        progressLoading.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (adminAccounts.containsKey(email) && adminAccounts.get(email).equals(pass)) {

                prefs.edit()
                        .putString(KEY_EMAIL, email)
                        .putString(KEY_PASSWORD, pass)
                        .apply();

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginAdminActivity.this, AdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();

            } else {
                Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                edtPassword.setText("");
                resetLoginButton();
                prefs.edit().clear().apply();
            }
        }, 1400);
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Đăng nhập");
        progressLoading.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}