package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import java.util.Locale;

public class AdminLocaleManager {

    private static final String PREFS_NAME   = "AdminSettings";   // ← riêng cho admin
    private static final String KEY_LANGUAGE = "admin_language";  // ← key khác hoàn toàn
    public  static final String LANG_VI      = "vi";
    public  static final String LANG_EN      = "en";

    public static void saveLanguage(Context context, String lang) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, lang)
                .apply();
    }

    public static String getSavedLanguage(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANGUAGE, LANG_VI);
    }

    public static Context applyLocale(Context base, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
            return base.createConfigurationContext(config);
        } else {
            config.locale = locale;
            base.getResources().updateConfiguration(config, base.getResources().getDisplayMetrics());
            return base;
        }
    }
}