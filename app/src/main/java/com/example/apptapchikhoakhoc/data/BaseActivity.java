package com.example.apptapchikhoakhoc.data;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apptapchikhoakhoc.utils.LocaleManager;
import com.example.apptapchikhoakhoc.utils.TextSizeManager;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

/**
 * Activity cơ sở – mọi Activity trong app nên kế thừa class này.
 *
 * Chức năng:
 *  ① Font scale (TextSizeManager) — áp qua attachBaseContext() TRƯỚC inflate layout.
 *     Mỗi lần onResume(), tự so sánh fontScale hiện tại với setting đã lưu;
 *     nếu khác → gọi recreate() để áp scale mới ngay lập tức.
 *  ② Locale (LocaleManager) — áp cùng lúc với font scale trong attachBaseContext().
 *     Mọi Activity tự động dùng đúng ngôn ngữ mà không cần gọi thêm ở subclass.
 *     onResume() detect ngôn ngữ thay đổi → recreate() tự động.
 *  ③ Status bar color đúng theme (ThemeManager).
 *  ④ Helper màu sắc và utility cho subclass.
 */
public class BaseActivity extends AppCompatActivity {

    /** True nếu đang ở Dark mode; cập nhật trong refreshThemeState() */
    protected boolean isDarkMode;

    /**
     * Font scale đang được áp dụng cho Activity này.
     * Ghi lại sau mỗi lần attachBaseContext() để so sánh khi onResume().
     */
    private float  appliedFontScale = -1f;

    /**
     * Locale code ("vi" / "en") đang được áp dụng.
     * Ghi lại trong attachBaseContext() để so sánh khi onResume().
     */
    private String appliedLocale = null;

    // ════════════════════════════════════════════════════════════
    //  ① FONT SCALE + ② LOCALE — áp cùng nhau trong attachBaseContext
    //     để TRƯỚC khi layout inflate, mọi sp và string đều đúng
    // ════════════════════════════════════════════════════════════

    @Override
    protected void attachBaseContext(Context newBase) {
        // ── Bước 1: áp Locale ─────────────────────────────────────
        String lang = LocaleManager.getSavedLanguage(newBase);
        appliedLocale = lang;
        Context localeContext = LocaleManager.applyLocale(newBase, lang);

        // ── Bước 2: áp Font scale lên context đã có locale ────────
        float targetScale = TextSizeManager.getFontScale(localeContext);
        appliedFontScale  = targetScale;
        Context finalContext = TextSizeManager.applyTextSize(localeContext);

        super.attachBaseContext(finalContext);
    }

    // ════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshThemeState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ── ① Kiểm tra font scale có thay đổi không ──────────────
        float savedScale = TextSizeManager.getFontScale(this);
        if (Math.abs(savedScale - appliedFontScale) > 0.001f) {
            recreate();
            return;
        }

        // ── ② Kiểm tra locale có thay đổi không ──────────────────
        String savedLocale = LocaleManager.getSavedLanguage(this);
        if (!savedLocale.equals(appliedLocale)) {
            recreate();
            return;
        }

        // ── ③ Cập nhật theme ──────────────────────────────────────
        refreshThemeState();
    }

    // ════════════════════════════════════════════════════════════
    //  THEME HELPERS
    // ════════════════════════════════════════════════════════════

    /**
     * Đọc trạng thái theme mới nhất và áp dụng status bar.
     * Subclass có thể gọi lại sau khi toggle theme.
     */
    protected void refreshThemeState() {
        isDarkMode = ThemeManager.isDarkMode(this);
        applyStatusBarColor();
    }

    private void applyStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ThemeManager.getStatusBarColor(this));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                if (!isDarkMode) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else {
                    flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(flags);
            }
        }
    }

    // ════════════════════════════════════════════════════════════
    //  MÀU SẮC — delegate sang ThemeManager
    // ════════════════════════════════════════════════════════════

    protected int getBackgroundColor()      { return ThemeManager.getBackgroundColor(this); }
    protected int getCardColor()            { return ThemeManager.getCardBackgroundColor(this); }
    protected int getTextColor()            { return ThemeManager.getTextPrimaryColor(this); }
    protected int getSecondaryTextColor()   { return ThemeManager.getTextSecondaryColor(this); }
    protected int getSectionHeaderColor()   { return ThemeManager.getTextTertiaryColor(this); }
    protected int getDividerColor()         { return ThemeManager.getDividerColor(this); }
    protected int getAccentColor()          { return ThemeManager.getAccentColor(this); }
    protected int getAccentDangerColor()    { return ThemeManager.getAccentDangerColor(this); }
    protected int getButtonPrimaryColor()   { return ThemeManager.getButtonPrimaryColor(this); }

    // ════════════════════════════════════════════════════════════
    //  TEXT SIZE HELPERS
    // ════════════════════════════════════════════════════════════

    /** Lấy setting cỡ chữ hiện tại: "small" | "medium" | "large" */
    protected String getCurrentTextSize() {
        return TextSizeManager.getTextSize(this);
    }

    /**
     * Lưu cỡ chữ mới và recreate Activity để áp dụng ngay.
     * Gọi từ subclass khi user chọn size mới trong dialog.
     */
    protected void applyAndSaveTextSize(String size) {
        TextSizeManager.setTextSize(this, size);
        appliedFontScale = TextSizeManager.getFontScale(this);
        appliedLocale    = LocaleManager.getSavedLanguage(this); // giữ locale không đổi
        recreate();
    }

    /** Lưu ngôn ngữ mới và recreate để áp dụng toàn app */
    protected void applyAndSaveLanguage(String lang) {
        LocaleManager.saveLanguage(this, lang);
        appliedLocale    = lang;
        appliedFontScale = TextSizeManager.getFontScale(this); // giữ font scale không đổi
        recreate();
    }

    // ════════════════════════════════════════════════════════════
    //  UTILITY
    // ════════════════════════════════════════════════════════════

    /** Làm sáng màu theo factor 0.0–1.0 (0=không đổi, 1=trắng) */
    protected int lightenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.min(Math.round(Color.red(color)   + (255 - Color.red(color))   * factor), 255);
        int g = Math.min(Math.round(Color.green(color) + (255 - Color.green(color)) * factor), 255);
        int b = Math.min(Math.round(Color.blue(color)  + (255 - Color.blue(color))  * factor), 255);
        return Color.argb(a, r, g, b);
    }

    /** Làm tối màu theo factor 0.0–1.0 (0=không đổi, 1=đen) */
    protected int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.max(Math.round(Color.red(color)   * (1 - factor)), 0);
        int g = Math.max(Math.round(Color.green(color) * (1 - factor)), 0);
        int b = Math.max(Math.round(Color.blue(color)  * (1 - factor)), 0);
        return Color.argb(a, r, g, b);
    }
}