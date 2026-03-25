package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

/**
 * LocaleManager — quản lý ngôn ngữ toàn app.
 *
 * Cách hoạt động:
 *  • Ngôn ngữ được lưu vào SharedPreferences ("AppSettings" → "language")
 *  • BaseActivity.attachBaseContext() gọi applyLocale() TRƯỚC khi inflate layout
 *    → mọi getString() đều trả về đúng ngôn ngữ ngay từ đầu
 *  • BaseActivity.onResume() so sánh appliedLocale với getSavedLanguage()
 *    → nếu khác → recreate() → attachBaseContext() chạy lại với locale mới
 *
 * Subclass KHÔNG cần gọi gì thêm — mọi thứ tự động qua BaseActivity.
 */
public class LocaleManager {

    private static final String PREFS_NAME   = "AppSettings";
    private static final String KEY_LANGUAGE = "language";
    public  static final String LANG_VI      = "vi";
    public  static final String LANG_EN      = "en";

    // ── Lưu ngôn ngữ ─────────────────────────────────────────────

    public static void saveLanguage(Context context, String lang) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, lang)
                .apply();
    }

    // ── Đọc ngôn ngữ đã lưu ──────────────────────────────────────

    public static String getSavedLanguage(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, LANG_VI);
    }

    // ── Áp locale vào Context ─────────────────────────────────────

    /**
     * Tạo Context mới với locale chỉ định.
     * Gọi trong attachBaseContext() — KHÔNG gọi ở nơi khác.
     *
     * @param base context gốc từ attachBaseContext
     * @param lang "vi" hoặc "en"
     * @return Context đã được override locale
     */
    public static Context applyLocale(Context base, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(base.getResources().getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return base.createConfigurationContext(config);
        } else {
            //noinspection deprecation
            config.locale = locale;
            //noinspection deprecation
            base.getResources().updateConfiguration(config, base.getResources().getDisplayMetrics());
            return base;
        }
    }

    /**
     * Áp locale vào Resources của Activity đang chạy (dùng khi cần update UI ngay).
     * Thường KHÔNG cần gọi trực tiếp — để BaseActivity xử lý qua recreate().
     */
    public static void applyToResources(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            //noinspection deprecation
            config.locale = locale;
        }
        //noinspection deprecation
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}