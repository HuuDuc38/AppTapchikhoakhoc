package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.graphics.Color;

/**
 * Quản lý theme (Dark/Light) cho giao diện NGƯỜI DÙNG.
 *
 * ★ ĐÃ XÓA AppCompatDelegate.setDefaultNightMode() để tránh ảnh hưởng Admin.
 *   Settings user thay đổi → chỉ giao diện user thay đổi.
 *   Settings admin thay đổi → chỉ giao diện admin thay đổi.
 *
 * Mỗi Activity tự set màu bằng code sau setContentView().
 */
public class ThemeManager {

    private static final String PREFS_NAME   = "AppTheme";
    private static final String KEY_DARK_MODE = "dark_mode";

    // ============================================================
    //  QUẢN LÝ TRẠNG THÁI
    // ============================================================

    public static boolean isDarkMode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean isDark) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_MODE, isDark)
                .apply();
        // ★ ĐÃ XÓA: AppCompatDelegate.setDefaultNightMode(...)
        //   Lý do: setDefaultNightMode() ảnh hưởng TOÀN APP kể cả Admin
    }

    public static void toggleTheme(Context context) {
        setDarkMode(context, !isDarkMode(context));
    }

    /**
     * ★ ĐÃ XÓA AppCompatDelegate khỏi đây.
     *   Giữ lại method này để các Activity cũ gọi không bị lỗi compile.
     */
    public static void applyThemeOnStartup(Context context) {
        // Không làm gì — mỗi Activity tự applyTheme() bằng code
    }

    // ============================================================
    //  BẢNG MÀU – DARK MODE
    // ============================================================

    public static final class DarkColors {
        private DarkColors() {}

        public static final int BACKGROUND      = Color.parseColor("#1A2332");
        public static final int STATUS_BAR      = Color.parseColor("#141D29");
        public static final int CARD_BACKGROUND = Color.parseColor("#243447");
        public static final int TEXT_PRIMARY    = Color.parseColor("#FFFFFF");
        public static final int TEXT_SECONDARY  = Color.parseColor("#8A9BAE");
        public static final int TEXT_TERTIARY   = Color.parseColor("#6B7C91");
        public static final int DIVIDER         = Color.parseColor("#2E3D4F");
        public static final int ACCENT          = Color.parseColor("#00D9A5");
        public static final int ACCENT_DANGER   = Color.parseColor("#FF5252");
        public static final int BUTTON_PRIMARY  = Color.parseColor("#3B82F6");
    }

    // ============================================================
    //  BẢNG MÀU – LIGHT MODE
    // ============================================================

    public static final class LightColors {
        private LightColors() {}

        public static final int BACKGROUND      = Color.parseColor("#F5F5F5");
        public static final int STATUS_BAR      = Color.parseColor("#7B1FA2");
        public static final int CARD_BACKGROUND = Color.WHITE;
        public static final int TEXT_PRIMARY    = Color.parseColor("#212121");
        public static final int TEXT_SECONDARY  = Color.parseColor("#757575");
        public static final int TEXT_TERTIARY   = Color.parseColor("#9E9E9E");
        public static final int DIVIDER         = Color.parseColor("#E0E0E0");
        public static final int ACCENT          = Color.parseColor("#4CAF50");
        public static final int ACCENT_DANGER   = Color.parseColor("#F44336");
        public static final int BUTTON_PRIMARY  = Color.parseColor("#2196F3");
    }

    // ============================================================
    //  HELPER — lấy màu theo theme hiện tại
    // ============================================================

    public static int getBackgroundColor(Context context) {
        return isDarkMode(context) ? DarkColors.BACKGROUND : LightColors.BACKGROUND;
    }

    public static int getStatusBarColor(Context context) {
        return isDarkMode(context) ? DarkColors.STATUS_BAR : LightColors.STATUS_BAR;
    }

    public static int getCardBackgroundColor(Context context) {
        return isDarkMode(context) ? DarkColors.CARD_BACKGROUND : LightColors.CARD_BACKGROUND;
    }

    public static int getTextPrimaryColor(Context context) {
        return isDarkMode(context) ? DarkColors.TEXT_PRIMARY : LightColors.TEXT_PRIMARY;
    }

    public static int getTextSecondaryColor(Context context) {
        return isDarkMode(context) ? DarkColors.TEXT_SECONDARY : LightColors.TEXT_SECONDARY;
    }

    public static int getTextTertiaryColor(Context context) {
        return isDarkMode(context) ? DarkColors.TEXT_TERTIARY : LightColors.TEXT_TERTIARY;
    }

    public static int getDividerColor(Context context) {
        return isDarkMode(context) ? DarkColors.DIVIDER : LightColors.DIVIDER;
    }

    public static int getAccentColor(Context context) {
        return isDarkMode(context) ? DarkColors.ACCENT : LightColors.ACCENT;
    }

    public static int getAccentDangerColor(Context context) {
        return isDarkMode(context) ? DarkColors.ACCENT_DANGER : LightColors.ACCENT_DANGER;
    }

    public static int getButtonPrimaryColor(Context context) {
        return isDarkMode(context) ? DarkColors.BUTTON_PRIMARY : LightColors.BUTTON_PRIMARY;
    }
}