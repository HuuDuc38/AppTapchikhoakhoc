package com.example.apptapchikhoakhoc.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class PrivacyActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────────
    private ImageView    btnBack;
    private LinearLayout headerPrivacy;
    private LinearLayout btnPersonalInfo;
    private LinearLayout btnChangePassword;
    private LinearLayout btnTwoFactor;
    private LinearLayout btnActiveSessions;
    private TextView     tvPersonalInfoSubtitle;
    private TextView     tvSessionCount;

    // ── Prefs ──────────────────────────────────────────────────────
    private SharedPreferences userPrefs;

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        userPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);

        initViews();
        setupListeners();
        applyCurrentTheme();
        loadUserData();
        animateItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyCurrentTheme();
        loadUserData();
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        btnBack                = findViewById(R.id.btn_back);
        headerPrivacy          = findViewById(R.id.header_privacy);
        btnPersonalInfo        = findViewById(R.id.btn_personal_info);
        btnChangePassword      = findViewById(R.id.btn_change_password);
        btnTwoFactor           = findViewById(R.id.btn_two_factor);
        btnActiveSessions      = findViewById(R.id.btn_active_sessions);
        tvPersonalInfoSubtitle = findViewById(R.id.tv_personal_info_subtitle);
        tvSessionCount         = findViewById(R.id.tv_session_count);
    }

    private void animateItems() {
        int delay = 0;
        int[] ids = {
                R.id.btn_personal_info,
                R.id.btn_change_password,
                R.id.btn_two_factor,
                R.id.btn_active_sessions
        };
        for (int id : ids) {
            View v = findViewById(id);
            if (v == null) continue;
            v.setAlpha(0f);
            v.setTranslationY(20f);
            v.animate().alpha(1f).translationY(0f)
                    .setStartDelay(delay).setDuration(280).start();
            delay += 60;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LISTENERS
    // ══════════════════════════════════════════════════════════════

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        btnPersonalInfo.setOnClickListener(v -> {
            if (!checkLogin()) return;
            showPersonalInfoSheet();
        });

        btnChangePassword.setOnClickListener(v -> {
            if (!checkLogin()) return;
            showChangePasswordSheet();
        });

        btnTwoFactor.setOnClickListener(v ->
                Toast.makeText(this, "Xác thực 2 bước đang phát triển", Toast.LENGTH_SHORT).show());

        btnActiveSessions.setOnClickListener(v ->
                Toast.makeText(this, "Quản lý phiên đăng nhập đang phát triển", Toast.LENGTH_SHORT).show());
    }

    // ══════════════════════════════════════════════════════════════
    //  DATA
    // ══════════════════════════════════════════════════════════════

    private void loadUserData() {
        String email = userPrefs.getString("userEmail", "");
        if (tvPersonalInfoSubtitle != null) {
            tvPersonalInfoSubtitle.setText(
                    email.isEmpty() ? "Tên, email, số điện thoại" : email);
        }
    }

    private boolean checkLogin() {
        if (!userPrefs.getBoolean("isLoggedIn", false)) {
            Toast.makeText(this, "Vui lòng đăng nhập trước", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            return false;
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM SHEET — THÔNG TIN CÁ NHÂN
    // ══════════════════════════════════════════════════════════════

    private void showPersonalInfoSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.BottomSheetStyle);
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        sheet.setContentView(view);

        EditText etName    = view.findViewById(R.id.et_full_name);
        EditText etEmail   = view.findViewById(R.id.et_email);
        EditText etPhone   = view.findViewById(R.id.et_phone);
        TextView btnSave   = view.findViewById(R.id.btn_dialog_save);
        TextView btnCancel = view.findViewById(R.id.btn_dialog_cancel);

        etName.setText(userPrefs.getString("userName",  ""));
        etEmail.setText(userPrefs.getString("userEmail", ""));
        etPhone.setText(userPrefs.getString("userPhone", ""));

        setupFocusBorder(etName,  view.findViewById(R.id.row_name));
        setupFocusBorder(etEmail, view.findViewById(R.id.row_email));
        setupFocusBorder(etPhone, view.findViewById(R.id.row_phone));

        // ── Dark mode cho sheet ──────────────────────────────────
        applySheetDarkMode(view);

        btnSave.setOnClickListener(v -> {
            String name  = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (name.isEmpty()) {
                shakeView(etName);
                etName.setError("Họ tên không được để trống");
                return;
            }
            if (!email.isEmpty() && !isValidEmail(email)) {
                shakeView(etEmail);
                etEmail.setError("Email không đúng định dạng");
                return;
            }
            if (!phone.isEmpty() && !isValidPhone(phone)) {
                shakeView(etPhone);
                etPhone.setError("Số điện thoại không hợp lệ (10 số, bắt đầu 0)");
                return;
            }

            userPrefs.edit()
                    .putString("userName",  name)
                    .putString("userEmail", email)
                    .putString("userPhone", phone)
                    .apply();

            loadUserData();
            sheet.dismiss();
            Toast.makeText(this, "✓ Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> sheet.dismiss());
        sheet.show();
    }

    // ══════════════════════════════════════════════════════════════
    //  BOTTOM SHEET — ĐỔI MẬT KHẨU
    // ══════════════════════════════════════════════════════════════

    private void showChangePasswordSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.BottomSheetStyle);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        sheet.setContentView(view);

        EditText etOld      = view.findViewById(R.id.et_old_password);
        EditText etNew      = view.findViewById(R.id.et_new_password);
        EditText etConfirm  = view.findViewById(R.id.et_confirm_password);
        TextView btnSave    = view.findViewById(R.id.btn_dialog_save);
        TextView btnCancel  = view.findViewById(R.id.btn_dialog_cancel);
        ImageView toggleOld     = view.findViewById(R.id.toggle_old_password);
        ImageView toggleNew     = view.findViewById(R.id.toggle_new_password);
        ImageView toggleConfirm = view.findViewById(R.id.toggle_confirm_password);
        TextView  tvStrength    = view.findViewById(R.id.tv_password_strength);

        setupFocusBorder(etOld,     view.findViewById(R.id.row_old_password));
        setupFocusBorder(etNew,     view.findViewById(R.id.row_new_password));
        setupFocusBorder(etConfirm, view.findViewById(R.id.row_confirm_password));

        setupPasswordToggle(etOld,     toggleOld);
        setupPasswordToggle(etNew,     toggleNew);
        setupPasswordToggle(etConfirm, toggleConfirm);

        if (tvStrength != null) {
            etNew.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                @Override public void afterTextChanged(Editable s) {
                    updatePasswordStrength(s.toString(), tvStrength);
                }
            });
        }

        // ── Dark mode cho sheet ──────────────────────────────────
        applySheetDarkMode(view);

        btnSave.setOnClickListener(v -> {
            String oldPass = etOld.getText().toString().trim();
            String newPass = etNew.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();

            if (oldPass.isEmpty()) {
                shakeView(etOld); etOld.setError("Nhập mật khẩu hiện tại"); return;
            }
            String saved = userPrefs.getString("userPassword", "");
            if (!saved.isEmpty() && !oldPass.equals(saved)) {
                shakeView(etOld); etOld.setError("Mật khẩu hiện tại không đúng"); return;
            }
            if (newPass.length() < 6) {
                shakeView(etNew); etNew.setError("Tối thiểu 6 ký tự"); return;
            }
            if (newPass.equals(oldPass)) {
                shakeView(etNew); etNew.setError("Mật khẩu mới phải khác mật khẩu cũ"); return;
            }
            if (!newPass.equals(confirm)) {
                shakeView(etConfirm); etConfirm.setError("Mật khẩu xác nhận không khớp"); return;
            }

            userPrefs.edit().putString("userPassword", newPass).apply();
            sheet.dismiss();
            Toast.makeText(this, "✓ Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        });

        btnCancel.setOnClickListener(v -> sheet.dismiss());
        sheet.show();
    }

    // ══════════════════════════════════════════════════════════════
    //  DARK MODE — SHEET
    // ══════════════════════════════════════════════════════════════

    /**
     * Áp dụng dark mode lên toàn bộ view của BottomSheet.
     * Gọi sau inflate(), trước sheet.show().
     */
    private void applySheetDarkMode(View root) {
        if (!isDarkMode) return;

        int bgSheet    = ThemeManager.DarkColors.CARD_BACKGROUND;   // nền sheet
        int bgInput    = Color.parseColor("#2A2D35");                 // nền input row
        int txtPri     = ThemeManager.DarkColors.TEXT_PRIMARY;        // text chính
        int txtSec     = ThemeManager.DarkColors.TEXT_SECONDARY;      // text phụ / hint
        int divider    = ThemeManager.DarkColors.DIVIDER;

        // Nền toàn sheet
        if (root instanceof ViewGroup) {
            GradientDrawable sheetBg = new GradientDrawable();
            sheetBg.setColor(bgSheet);
            sheetBg.setCornerRadii(new float[]{dpToPx(20), dpToPx(20), dpToPx(20), dpToPx(20), 0, 0, 0, 0});
            root.setBackground(sheetBg);
        }

        // Duyệt đệ quy tất cả view con
        applyDarkToViewGroup((ViewGroup) root, bgSheet, bgInput, txtPri, txtSec, divider);
    }

    private void applyDarkToViewGroup(ViewGroup parent,
                                      int bgSheet, int bgInput,
                                      int txtPri, int txtSec, int divider) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                int id = tv.getId();
                // Nút Lưu / Xác nhận giữ màu trắng
                if (id == R.id.btn_dialog_save) continue;
                // Nút Hủy
                if (id == R.id.btn_dialog_cancel) {
                    tv.setTextColor(txtSec);
                    GradientDrawable cancelBg = new GradientDrawable();
                    cancelBg.setColor(Color.parseColor("#2A2D35"));
                    cancelBg.setCornerRadius(dpToPx(12));
                    tv.setBackground(cancelBg);
                    continue;
                }
                // Label nhỏ (13sp) → màu phụ
                float sp = tv.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
                if (sp <= 13f) tv.setTextColor(txtSec);
                else           tv.setTextColor(txtPri);

            } else if (child instanceof EditText) {
                EditText et = (EditText) child;
                et.setTextColor(txtPri);
                et.setHintTextColor(Color.parseColor("#555C6A"));

            } else if (child instanceof ImageView) {
                // Giữ nguyên màu icon accent (đỏ/vàng/xanh), chỉ đổi icon toggle
                // Không làm gì ở đây

            } else if (child instanceof LinearLayout) {
                LinearLayout ll = (LinearLayout) child;
                int id = ll.getId();
                // Input row → nền tối
                if (id == R.id.row_name     || id == R.id.row_email
                        || id == R.id.row_phone
                        || id == R.id.row_old_password
                        || id == R.id.row_new_password
                        || id == R.id.row_confirm_password) {
                    GradientDrawable inputBg = new GradientDrawable();
                    inputBg.setColor(bgInput);
                    inputBg.setCornerRadius(dpToPx(14));
                    inputBg.setStroke(dpToPx(1), Color.parseColor("#3A3D45"));
                    ll.setBackground(inputBg);
                }
                applyDarkToViewGroup(ll, bgSheet, bgInput, txtPri, txtSec, divider);

            } else if (child instanceof ViewGroup) {
                applyDarkToViewGroup((ViewGroup) child, bgSheet, bgInput, txtPri, txtSec, divider);

            } else {
                // Divider (View 1dp height)
                if (child.getLayoutParams() != null
                        && child.getLayoutParams().height == dpToPx(1)) {
                    child.setBackgroundColor(divider);
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME — activity_privacy
    // ══════════════════════════════════════════════════════════════

    private void applyCurrentTheme() {
        applySystemBars();

        LinearLayout root = findViewById(R.id.privacy_root);

        if (isDarkMode) {
            int bgPage   = ThemeManager.DarkColors.BACKGROUND;
            int bgCard   = ThemeManager.DarkColors.CARD_BACKGROUND;
            int txtPri   = ThemeManager.DarkColors.TEXT_PRIMARY;
            int txtSec   = ThemeManager.DarkColors.TEXT_SECONDARY;
            int txtSect  = ThemeManager.DarkColors.TEXT_TERTIARY;
            int divColor = ThemeManager.DarkColors.DIVIDER;

            if (root          != null) root.setBackgroundColor(bgPage);
            if (headerPrivacy != null) headerPrivacy.setBackgroundColor(ThemeManager.DarkColors.STATUS_BAR);

            // Tô nền ScrollView
            View scrollView = findViewById(R.id.privacy_root);
            if (root != null) {
                for (int i = 0; i < root.getChildCount(); i++) {
                    View child = root.getChildAt(i);
                    if (child.getClass().getSimpleName().equals("ScrollView")) {
                        child.setBackgroundColor(bgPage);
                    }
                }
            }

            if (root != null) {
                applyPrivacyCardsDark(root, bgCard);
                applyPrivacyTextColors(root, txtPri, txtSec, txtSect, divColor);
            }

        } else {
            if (root          != null) root.setBackgroundColor(Color.parseColor("#F4F6FA"));
            if (headerPrivacy != null) headerPrivacy.setBackgroundResource(R.drawable.toolbar_gradient_red);
        }
    }

    /** Tô nền card (elevation > 0) sang màu tối */
    private void applyPrivacyCardsDark(ViewGroup parent, int cardColor) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof LinearLayout && child.getElevation() > 0) {
                GradientDrawable d = new GradientDrawable();
                d.setColor(cardColor);
                d.setCornerRadius(dpToPx(16));
                child.setBackground(d);
            }
            if (child instanceof ViewGroup)
                applyPrivacyCardsDark((ViewGroup) child, cardColor);
        }
    }

    /** Đổi màu chữ toàn bộ màn hình privacy */
    private void applyPrivacyTextColors(ViewGroup parent,
                                        int txtPri, int txtSec,
                                        int txtSect, int divColor) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                String text = tv.getText().toString();
                // Section header (ALL CAPS nhỏ)
                if (text.equals("TÀI KHOẢN") || text.equals("BẢO MẬT")
                        || text.equals("ACCOUNT") || text.equals("SECURITY")) {
                    tv.setTextColor(txtSect);
                } else {
                    float sp = tv.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
                    if (sp >= 13f) tv.setTextColor(txtPri);
                    else           tv.setTextColor(txtSec);
                }
                // subtitle có id riêng → luôn màu phụ
                if (tv.getId() == R.id.tv_personal_info_subtitle
                        || tv.getId() == R.id.tv_session_count) {
                    tv.setTextColor(txtSec);
                }

            } else if (child instanceof View
                    && child.getLayoutParams() != null
                    && child.getLayoutParams().height == dpToPx(1)) {
                child.setBackgroundColor(divColor);

            } else if (child instanceof ViewGroup) {
                applyPrivacyTextColors((ViewGroup) child, txtPri, txtSec, txtSect, divColor);
            }
        }
    }

    private void applySystemBars() {
        Window window = getWindow();
        if (isDarkMode) {
            int dark = ThemeManager.DarkColors.BACKGROUND;
            window.setStatusBarColor(dark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(dark);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                window.getDecorView().setSystemUiVisibility(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int f = window.getDecorView().getSystemUiVisibility();
                f &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(f);
            }
        } else {
            window.setStatusBarColor(Color.parseColor("#C8463D"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(Color.WHITE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                window.getDecorView().setSystemUiVisibility(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int f = window.getDecorView().getSystemUiVisibility();
                f |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(f);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════════

    private void setupFocusBorder(EditText et, View row) {
        if (et == null || row == null) return;
        et.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dpToPx(14));
            if (isDarkMode) {
                if (hasFocus) {
                    bg.setColor(Color.parseColor("#2A2D35"));
                    bg.setStroke(dpToPx(2), Color.parseColor("#C8463D"));
                } else {
                    bg.setColor(Color.parseColor("#2A2D35"));
                    bg.setStroke(dpToPx(1), Color.parseColor("#3A3D45"));
                }
            } else {
                if (hasFocus) {
                    bg.setColor(Color.WHITE);
                    bg.setStroke(dpToPx(2), Color.parseColor("#C8463D"));
                } else {
                    bg.setColor(Color.parseColor("#F7F8FA"));
                    bg.setStroke(dpToPx(2), Color.parseColor("#EAECF0"));
                }
            }
            row.setBackground(bg);
        });
    }

    private void shakeView(View v) {
        if (v == null) return;
        v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    private void setupPasswordToggle(EditText et, ImageView toggle) {
        if (et == null || toggle == null) return;
        toggle.setOnClickListener(v -> {
            boolean hidden = (et.getInputType()
                    == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));
            et.setInputType(hidden
                    ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                    : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggle.setImageResource(hidden ? R.drawable.ic_eye_off : R.drawable.ic_eye);
            et.setSelection(et.getText().length());
        });
    }

    private void updatePasswordStrength(String pass, TextView tv) {
        if (pass.isEmpty()) { tv.setVisibility(View.GONE); return; }
        tv.setVisibility(View.VISIBLE);
        int score = 0;
        if (pass.length() >= 8)                             score++;
        if (pass.matches(".*[A-Z].*"))                      score++;
        if (pass.matches(".*[0-9].*"))                      score++;
        if (pass.matches(".*[!@#$%^&*()_+\\-=\\[\\]].*"))  score++;
        switch (score) {
            case 0: case 1:
                tv.setText("Yếu");
                tv.setTextColor(Color.parseColor("#EF4444")); break;
            case 2: case 3:
                tv.setText("Trung bình");
                tv.setTextColor(Color.parseColor("#F5A623")); break;
            default:
                tv.setText("Mạnh ✓");
                tv.setTextColor(Color.parseColor("#10B981"));
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  VALIDATION
    // ══════════════════════════════════════════════════════════════

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone.replaceAll("[\\s\\-]", "").matches("^0[0-9]{9}$");
    }

    // ── Helper ────────────────────────────────────────────────────

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}