package com.example.apptapchikhoakhoc.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.admin.LoginAdminActivity;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.utils.LocaleManager;
import com.example.apptapchikhoakhoc.utils.TextSizeManager;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

public class SettingsActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────────
    private Switch       switchDarkMode;
    private LinearLayout profileCard;
    private LinearLayout btnLanguage, btnTextSize;
    private LinearLayout btnPrivacy;
    private LinearLayout btnHelp, btnAbout, btnAdminLogin;
    private LinearLayout btnLogout;
    private TextView     tvUserName, tvUserEmail;
    private TextView     tvCurrentLanguage, tvCurrentTextSize;
    private ImageView    imgUserAvatarBg;
    private TextView     tvDarkModeTitle, tvDarkModeSubtitle;
    private TextView     tvLogoutLabel;

    // ── Prefs ──────────────────────────────────────────────────────
    private SharedPreferences userPrefs;

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
        updateUserProfile();
        applyCurrentTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        updateUserProfile();
        applyCurrentTheme();
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT VIEWS
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        switchDarkMode     = findViewById(R.id.switch_dark_mode);
        profileCard        = findViewById(R.id.profile_card);
        tvUserName         = findViewById(R.id.tv_user_name);
        tvUserEmail        = findViewById(R.id.tv_user_email);
        imgUserAvatarBg    = findViewById(R.id.img_user_avatar_bg);
        btnLanguage        = findViewById(R.id.btn_language);
        btnTextSize        = findViewById(R.id.btn_text_size);
        btnPrivacy         = findViewById(R.id.btn_privacy);
        btnHelp            = findViewById(R.id.btn_help);
        btnAbout           = findViewById(R.id.btn_about);
        btnAdminLogin      = findViewById(R.id.btn_admin_login);
        btnLogout          = findViewById(R.id.btn_logout);
        tvLogoutLabel      = findViewById(R.id.tv_logout_label);
        tvDarkModeTitle    = findViewById(R.id.tv_dark_mode_title);
        tvDarkModeSubtitle = findViewById(R.id.tv_dark_mode_subtitle);
        tvCurrentLanguage  = findViewById(R.id.tv_current_language);
        tvCurrentTextSize  = findViewById(R.id.tv_current_text_size);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    // ══════════════════════════════════════════════════════════════
    //  LOAD SETTINGS
    // ══════════════════════════════════════════════════════════════

    private void loadSettings() {
        isDarkMode = ThemeManager.isDarkMode(this);

        switchDarkMode.setOnCheckedChangeListener(null);
        switchDarkMode.setChecked(isDarkMode);
        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            ThemeManager.setDarkMode(this, isChecked);
            recreate();
        });

        String lang = LocaleManager.getSavedLanguage(this);
        if (tvCurrentLanguage != null)
            tvCurrentLanguage.setText(LocaleManager.LANG_VI.equals(lang)
                    ? getString(R.string.language_vietnamese)
                    : getString(R.string.language_english));

        if (tvCurrentTextSize != null)
            tvCurrentTextSize.setText(getTextSizeLabel(getCurrentTextSize()));
    }

    // ══════════════════════════════════════════════════════════════
    //  LISTENERS
    // ══════════════════════════════════════════════════════════════

    private void setupListeners() {

        btnLanguage.setOnClickListener(v -> showLanguageDialog());
        btnTextSize.setOnClickListener(v -> showTextSizeDialog());

        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(v -> {
                startActivity(new Intent(this, PrivacyActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        btnHelp.setOnClickListener(v -> showHelpDialog());
        btnAbout.setOnClickListener(v -> showAboutDialog());

        btnAdminLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginAdminActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        profileCard.setOnClickListener(v -> {
            if (!userPrefs.getBoolean("isLoggedIn", false)) {
                startActivity(new Intent(this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else {
                showUserInfoDialog();
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  PROFILE
    // ══════════════════════════════════════════════════════════════

    private void updateUserProfile() {
        boolean isLoggedIn = userPrefs.getBoolean("isLoggedIn", false);
        String  userName   = userPrefs.getString("userName", "");
        String  userEmail  = userPrefs.getString("userEmail", "");

        if (isLoggedIn && !userName.isEmpty()) {
            tvUserName.setText(userName);
            tvUserEmail.setText(userEmail.isEmpty()
                    ? getString(R.string.settings_guest) : userEmail);
            if (btnLogout != null) btnLogout.setVisibility(View.VISIBLE);
        } else {
            tvUserName.setText(getString(R.string.settings_guest));
            tvUserEmail.setText(getString(R.string.settings_login_prompt));
            if (btnLogout != null) btnLogout.setVisibility(View.GONE);
        }

        tvUserName.setTextColor(Color.WHITE);
        tvUserEmail.setTextColor(Color.parseColor("#E0E0E0"));
    }

    // ══════════════════════════════════════════════════════════════
    //  DIALOGS
    // ══════════════════════════════════════════════════════════════

    private void showLanguageDialog() {
        String currentLang = LocaleManager.getSavedLanguage(this);
        int checkedItem = LocaleManager.LANG_VI.equals(currentLang) ? 0 : 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_language_title)
                .setSingleChoiceItems(
                        new String[]{
                                getString(R.string.language_vietnamese),
                                getString(R.string.language_english)
                        },
                        checkedItem,
                        (dialog, which) -> {
                            String newLang = which == 0
                                    ? LocaleManager.LANG_VI : LocaleManager.LANG_EN;
                            dialog.dismiss();
                            if (!newLang.equals(currentLang))
                                applyAndSaveLanguage(newLang);
                        })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showTextSizeDialog() {
        String currentSize = getCurrentTextSize();
        int checkedItem;
        switch (currentSize) {
            case TextSizeManager.SIZE_SMALL: checkedItem = 0; break;
            case TextSizeManager.SIZE_LARGE: checkedItem = 2; break;
            default:                          checkedItem = 1; break;
        }
        String[] options = {
                getString(R.string.text_size_small)  + "  (Aa)",
                getString(R.string.text_size_medium) + "  (Aa)",
                getString(R.string.text_size_large)  + "  (Aa)"
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_text_size_title)
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    String newSize;
                    switch (which) {
                        case 0:  newSize = TextSizeManager.SIZE_SMALL;  break;
                        case 2:  newSize = TextSizeManager.SIZE_LARGE;  break;
                        default: newSize = TextSizeManager.SIZE_MEDIUM; break;
                    }
                    dialog.dismiss();
                    if (!newSize.equals(currentSize)) applyAndSaveTextSize(newSize);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_help_title)
                .setMessage(R.string.dialog_help_message)
                .setPositiveButton(R.string.dialog_close, null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_about_title)
                .setMessage(R.string.dialog_about_message)
                .setPositiveButton(R.string.dialog_close, null)
                .show();
    }

    private void showUserInfoDialog() {
        String userName  = userPrefs.getString("userName", "");
        String userEmail = userPrefs.getString("userEmail", "");
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_user_info_title)
                .setMessage(getString(R.string.dialog_user_info_message, userName,
                        userEmail.isEmpty()
                                ? getString(R.string.email_not_updated) : userEmail))
                .setPositiveButton(R.string.dialog_close, null)
                .setNegativeButton(R.string.logout, (d, w) -> showLogoutDialog())
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    userPrefs.edit().clear().apply();
                    Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                    updateUserProfile();
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    // ══════════════════════════════════════════════════════════════
    //  APPLY THEME
    // ══════════════════════════════════════════════════════════════

    private void applyCurrentTheme() {
        applySystemBars();

        LinearLayout settingsContent = findViewById(R.id.settings_content);
        LinearLayout headerSettings  = findViewById(R.id.header_settings);
        ImageView    btnBack         = findViewById(R.id.btn_back);

        if (isDarkMode) {
            int bgPage   = ThemeManager.DarkColors.BACKGROUND;
            int bgCard   = ThemeManager.DarkColors.CARD_BACKGROUND;
            int txtPri   = ThemeManager.DarkColors.TEXT_PRIMARY;
            int txtSec   = ThemeManager.DarkColors.TEXT_SECONDARY;
            int txtSect  = ThemeManager.DarkColors.TEXT_TERTIARY;
            int divColor = ThemeManager.DarkColors.DIVIDER;

            if (headerSettings  != null) headerSettings.setBackgroundColor(ThemeManager.DarkColors.STATUS_BAR);
            if (settingsContent != null) settingsContent.setBackgroundColor(bgPage);
            if (btnBack != null) btnBack.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

            if (profileCard != null) {
                GradientDrawable d = new GradientDrawable();
                d.setColor(bgCard);
                d.setCornerRadius(dpToPx(16));
                profileCard.setBackground(d);
            }

            if (tvDarkModeTitle    != null) tvDarkModeTitle.setTextColor(txtPri);
            if (tvDarkModeSubtitle != null) tvDarkModeSubtitle.setTextColor(txtSec);

            if (settingsContent != null) {
                applyCardColors(settingsContent, bgCard, true);
                applyColorToSectionHeaders(settingsContent, txtSect);
                applyColorToItemTitles(settingsContent, txtPri);
                applyColorToDividers(settingsContent, divColor);
                applyColorToSubtitles(settingsContent, txtSec);
            }

            if (btnLogout != null) {
                GradientDrawable logoutBg = new GradientDrawable();
                logoutBg.setColor(bgCard);
                logoutBg.setCornerRadius(dpToPx(12));
                btnLogout.setBackground(logoutBg);
            }

        } else {
            if (headerSettings  != null) headerSettings.setBackgroundResource(R.drawable.toolbar_gradient_red);
            if (settingsContent != null) settingsContent.setBackgroundColor(ThemeManager.LightColors.BACKGROUND);
            if (profileCard != null)     profileCard.setBackgroundResource(R.drawable.profile_card_bg);
            if (btnBack != null) btnBack.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

            if (tvDarkModeTitle    != null) tvDarkModeTitle.setTextColor(ThemeManager.LightColors.TEXT_PRIMARY);
            if (tvDarkModeSubtitle != null) tvDarkModeSubtitle.setTextColor(ThemeManager.LightColors.TEXT_SECONDARY);

            if (settingsContent != null) {
                applyCardColors(settingsContent, ThemeManager.LightColors.CARD_BACKGROUND, false);
                applyColorToSectionHeaders(settingsContent, ThemeManager.LightColors.TEXT_TERTIARY);
                applyColorToItemTitles(settingsContent, ThemeManager.LightColors.TEXT_PRIMARY);
                applyColorToDividers(settingsContent, ThemeManager.LightColors.DIVIDER);
                applyColorToSubtitles(settingsContent, ThemeManager.LightColors.TEXT_SECONDARY);
            }

            if (btnLogout != null) btnLogout.setBackgroundResource(R.drawable.card_background);
        }

        updateUserProfile();
    }

    private void applySystemBars() {
        Window window = getWindow();
        if (isDarkMode) {
            int darkBg = ThemeManager.DarkColors.BACKGROUND;
            window.setStatusBarColor(darkBg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(darkBg);
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
    //  VIEW HELPERS — theme traversal
    // ══════════════════════════════════════════════════════════════

    private void applyCardColors(ViewGroup parent, int cardColor, boolean isDark) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof LinearLayout
                    && child.getElevation() > 0
                    && child.getId() != R.id.btn_logout) {
                if (isDark) {
                    GradientDrawable d = new GradientDrawable();
                    d.setColor(cardColor);
                    d.setCornerRadius(dpToPx(12));
                    child.setBackground(d);
                } else {
                    child.setBackgroundResource(R.drawable.card_background);
                }
            }
            if (child instanceof ViewGroup) applyCardColors((ViewGroup) child, cardColor, isDark);
        }
    }

    private static final String[] SECTION_HEADERS = {
            "GIAO DIỆN", "TÙY CHỈNH", "HỖ TRỢ", "THÔNG TIN",
            "INTERFACE", "CUSTOMIZATION", "SUPPORT", "INFORMATION"
    };

    private void applyColorToSectionHeaders(ViewGroup parent, int color) {
        for (String h : SECTION_HEADERS) applyTextColorByContent(parent, h, color);
    }

    private void applyTextColorByContent(ViewGroup parent, String target, int color) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView
                    && ((TextView) child).getText().toString().equals(target)) {
                ((TextView) child).setTextColor(color);
            } else if (child instanceof ViewGroup) {
                applyTextColorByContent((ViewGroup) child, target, color);
            }
        }
    }

    private void applyColorToItemTitles(ViewGroup parent, int color) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                TextView tv = (TextView) child;
                int id = tv.getId();
                if (id == R.id.tv_logout_label
                        || id == R.id.tv_current_language
                        || id == R.id.tv_current_text_size
                        || id == R.id.tv_user_name
                        || id == R.id.tv_user_email
                        || id == R.id.tv_dark_mode_subtitle) continue;
                if (isSectionHeader(tv.getText().toString())) continue;
                if (tv.getTextSize() >= pxToSp(14)) tv.setTextColor(color);
            } else if (child instanceof ViewGroup) {
                applyColorToItemTitles((ViewGroup) child, color);
            }
        }
    }

    private void applyColorToSubtitles(ViewGroup parent, int color) {
        int[] ids = {
                R.id.tv_current_language,
                R.id.tv_current_text_size,
                R.id.tv_dark_mode_subtitle
        };
        for (int id : ids) {
            View v = parent.getRootView().findViewById(id);
            if (v instanceof TextView) ((TextView) v).setTextColor(color);
        }
    }

    private void applyColorToDividers(ViewGroup parent, int color) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getClass().equals(View.class)
                    && child.getLayoutParams().height == dpToPx(1)) {
                child.setBackgroundColor(color);
            } else if (child instanceof ViewGroup) {
                applyColorToDividers((ViewGroup) child, color);
            }
        }
    }

    private boolean isSectionHeader(String text) {
        for (String h : SECTION_HEADERS) if (h.equals(text)) return true;
        return false;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private float pxToSp(float px) {
        return px / getResources().getDisplayMetrics().scaledDensity;
    }

    private String getTextSizeLabel(String size) {
        if (TextSizeManager.SIZE_SMALL.equals(size)) return getString(R.string.text_size_small);
        if (TextSizeManager.SIZE_LARGE.equals(size)) return getString(R.string.text_size_large);
        return getString(R.string.text_size_medium);
    }
}