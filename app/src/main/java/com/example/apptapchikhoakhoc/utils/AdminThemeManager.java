package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.graphics.Color;

public class AdminThemeManager {

    private static final String PREFS_NAME    = "AdminThemePrefs";
    private static final String KEY_DARK_MODE = "admin_dark_mode";

    // ── Trạng thái ────────────────────────────────────────────────

    public static boolean isDarkMode(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DARK_MODE, false);
    }

    public static void setDarkMode(Context context, boolean isDark) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DARK_MODE, isDark)
                .apply();
    }

    public static void toggleTheme(Context context) {
        setDarkMode(context, !isDarkMode(context));
    }

    // ══════════════════════════════════════════════════════════════
    //  BẢNG MÀU – DARK MODE
    // ══════════════════════════════════════════════════════════════

    public static final class DarkColors {
        private DarkColors() {}

        public static final int BACKGROUND      = Color.parseColor("#1A2332");
        public static final int CARD_BACKGROUND = Color.parseColor("#243447");
        public static final int STATUS_BAR      = Color.parseColor("#141D29");
        public static final int TEXT_PRIMARY    = Color.parseColor("#FFFFFF");
        public static final int TEXT_SECONDARY  = Color.parseColor("#8A9BAE");
        public static final int TEXT_TERTIARY   = Color.parseColor("#6B7C91");
        public static final int DIVIDER         = Color.parseColor("#2E3D4F");
        public static final int ACCENT          = Color.parseColor("#00D9A5");
        public static final int ACCENT_DANGER   = Color.parseColor("#FF5252");
    }

    // ══════════════════════════════════════════════════════════════
    //  BẢNG MÀU – LIGHT MODE
    // ══════════════════════════════════════════════════════════════

    public static final class LightColors {
        private LightColors() {}

        public static final int BACKGROUND      = Color.parseColor("#F5F5F5");
        public static final int CARD_BACKGROUND = Color.WHITE;
        public static final int STATUS_BAR      = Color.parseColor("#C8463D");
        public static final int TEXT_PRIMARY    = Color.parseColor("#1A1A1A");
        public static final int TEXT_SECONDARY  = Color.parseColor("#757575");
        public static final int TEXT_TERTIARY   = Color.parseColor("#9E9E9E");
        public static final int DIVIDER         = Color.parseColor("#F0F0F0");
        public static final int ACCENT          = Color.parseColor("#C8463D");
        public static final int ACCENT_DANGER   = Color.parseColor("#E53935");
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPER — lấy màu theo theme hiện tại
    // ══════════════════════════════════════════════════════════════

    public static int getBackgroundColor(Context context) {
        return isDarkMode(context) ? DarkColors.BACKGROUND : LightColors.BACKGROUND;
    }

    public static int getCardBackgroundColor(Context context) {
        return isDarkMode(context) ? DarkColors.CARD_BACKGROUND : LightColors.CARD_BACKGROUND;
    }

    public static int getStatusBarColor(Context context) {
        return isDarkMode(context) ? DarkColors.STATUS_BAR : LightColors.STATUS_BAR;
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
}