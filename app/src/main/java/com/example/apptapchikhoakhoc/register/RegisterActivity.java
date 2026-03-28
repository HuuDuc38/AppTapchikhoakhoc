package com.example.apptapchikhoakhoc.register;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// ✅ SỬA: extends BaseActivity thay vì AppCompatActivity
//    → BaseActivity tự apply locale khi người dùng đổi ngôn ngữ
public class RegisterActivity extends BaseActivity {

    private TextInputEditText edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    private TextInputLayout   tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private MaterialButton    btnRegister;
    private TextView          tvLogin, tvTitle;
    private ImageButton       btnBack;
    private CircularProgressIndicator progressLoading;
    private DatabaseHelper    dbHelper;
    private View              rootScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        edtFullName        = findViewById(R.id.edtFullName);
        edtEmail           = findViewById(R.id.edtEmail);
        edtPassword        = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        tilFullName        = findViewById(R.id.tilFullName);
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnRegister        = findViewById(R.id.btnRegister);
        tvLogin            = findViewById(R.id.tvLogin);
        tvTitle            = findViewById(R.id.tvTitle);
        progressLoading    = findViewById(R.id.progressLoading);
        btnBack            = findViewById(R.id.btnBack);
        rootScrollView     = findViewById(R.id.rootScrollView);

        applyDarkModeTheme();

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME
    // ══════════════════════════════════════════════════════════════

    private void applyDarkModeTheme() {
        boolean isDark = ThemeManager.isDarkMode(this);

        // ── 1. Background ──────────────────────────────────────────
        if (rootScrollView != null) {
            rootScrollView.setBackgroundColor(isDark
                    ? ThemeManager.DarkColors.BACKGROUND
                    : Color.parseColor("#F5F7FA"));
        }

        // ── 2. Tiêu đề ────────────────────────────────────────────
        if (tvTitle != null) {
            tvTitle.setTextColor(isDark ? Color.WHITE : Color.parseColor("#1A1A1A"));
        }

        // ── 3. Màu dùng chung ─────────────────────────────────────
        int accentColor     = isDark ? ThemeManager.DarkColors.ACCENT : Color.parseColor("#2D68C4");
        int hintColor       = isDark ? Color.parseColor("#7A8A9A")     : Color.parseColor("#888888");
        int strokeFocused   = accentColor;
        int strokeUnfocused = isDark ? Color.parseColor("#4A5568")     : Color.parseColor("#CCCCCC");
        int boxBgColor      = isDark ? Color.parseColor("#2A3A4D")     : Color.WHITE;
        int textColor       = isDark ? Color.WHITE                     : Color.parseColor("#1A1A1A");
        int iconTintColor   = isDark ? Color.parseColor("#AAAAAA")     : Color.parseColor("#666666");

        // State list stroke
        int[][] strokeStates = {
                new int[]{ android.R.attr.state_focused },
                new int[]{}
        };
        ColorStateList strokeStateList = new ColorStateList(strokeStates,
                new int[]{ strokeFocused, strokeUnfocused });

        // State list hint label
        int[][] hintStates = {
                new int[]{ android.R.attr.state_focused },
                new int[]{}
        };
        ColorStateList hintStateList = new ColorStateList(hintStates,
                new int[]{ accentColor, hintColor });

        // ── 4. Áp dụng cho từng TextInputLayout ───────────────────
        TextInputLayout[] layouts = {
                tilFullName, tilEmail, tilPassword, tilConfirmPassword
        };
        for (TextInputLayout til : layouts) {
            if (til == null) continue;
            til.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
            til.setBoxBackgroundColor(boxBgColor);
            til.setHintTextColor(hintStateList);
            til.setBoxStrokeColorStateList(strokeStateList);
            til.setEndIconTintList(ColorStateList.valueOf(iconTintColor));
        }

        // ── 5. Áp dụng cho từng TextInputEditText ─────────────────
        TextInputEditText[] edits = {
                edtFullName, edtEmail, edtPassword, edtConfirmPassword
        };
        for (TextInputEditText edt : edits) {
            if (edt == null) continue;
            edt.setTextColor(textColor);
            edt.setHintTextColor(hintColor);
            edt.setHighlightColor(Color.TRANSPARENT);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                edt.setAutofillHints((String[]) null);
            }
        }

        // ── 6. Link + Back ────────────────────────────────────────
        if (tvLogin != null) tvLogin.setTextColor(accentColor);
        if (btnBack != null) {
            btnBack.setColorFilter(isDark
                    ? ThemeManager.DarkColors.TEXT_SECONDARY
                    : Color.parseColor("#666666"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  ĐĂNG KÝ
    // ══════════════════════════════════════════════════════════════

    private void registerUser() {
        String name    = edtFullName.getText().toString().trim();
        String email   = edtEmail.getText().toString().trim();
        String pass    = edtPassword.getText().toString();
        String confirm = edtConfirmPassword.getText().toString();

        // Xóa lỗi cũ
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);

        // Validate
        if (name.isEmpty()) {
            tilFullName.setError("Vui lòng nhập họ tên");
            tilFullName.requestFocus();
            return;
        }
        if (email.isEmpty()) {
            tilEmail.setError("Vui lòng nhập email");
            tilEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            tilEmail.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            tilPassword.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            tilPassword.setError("Mật khẩu phải từ 6 ký tự trở lên");
            tilPassword.requestFocus();
            return;
        }
        if (confirm.isEmpty()) {
            tilConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            tilConfirmPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            tilConfirmPassword.setError("Mật khẩu xác nhận không khớp!");
            tilConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            new Thread(() -> {
                boolean emailExists = dbHelper.isEmailExists(email);
                if (emailExists) {
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        showLoading(false);
                        tilEmail.setError("Email này đã được đăng ký!");
                        tilEmail.requestFocus();
                    });
                    return;
                }

                boolean success = dbHelper.registerUser(name, email, pass);
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    if (success) {
                        Toast.makeText(this,
                                "Đăng ký thành công!\nVui lòng đăng nhập để tiếp tục.",
                                Toast.LENGTH_LONG).show();
                        showLoading(false);
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    } else {
                        showLoading(false);
                        Toast.makeText(this,
                                "Đăng ký thất bại. Vui lòng thử lại!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }, 1500);
    }

    // ══════════════════════════════════════════════════════════════
    //  LOADING
    // ══════════════════════════════════════════════════════════════

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            btnRegister.setText("");
            btnRegister.setEnabled(false);
            progressLoading.setVisibility(View.VISIBLE);
        } else {
            btnRegister.setText(getString(R.string.register_btn));
            btnRegister.setEnabled(true);
            progressLoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}