package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/**
 * TextSizeManager — quản lý kích thước chữ toàn app.
 *
 * Dùng fontScale trong Configuration để scale toàn bộ sp:
 *   small  → 0.85f  (~14sp thành ~12sp)
 *   medium → 1.00f  (mặc định)
 *   large  → 1.20f  (~14sp thành ~17sp)
 *
 * Cách dùng:
 *   1. Gọi TextSizeManager.applyTextSize(this) trong BaseActivity.attachBaseContext()
 *   2. Sau khi lưu setting mới → gọi recreate() để áp dụng.
 */
public class TextSizeManager {

    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_TEXT_SIZE = "text_size";

    public static final String SIZE_SMALL  = "small";
    public static final String SIZE_MEDIUM = "medium";
    public static final String SIZE_LARGE  = "large";

    // ── Font scale tương ứng ──────────────────────────────────────
    public static final float SCALE_SMALL  = 0.85f;
    public static final float SCALE_MEDIUM = 1.00f;
    public static final float SCALE_LARGE  = 1.20f;

    // ── Lưu setting ──────────────────────────────────────────────
    public static void setTextSize(Context context, String size) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TEXT_SIZE, size)
                .apply();
    }

    // ── Đọc setting hiện tại ─────────────────────────────────────
    public static String getTextSize(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_TEXT_SIZE, SIZE_MEDIUM);
    }

    // ── Trả về fontScale tương ứng ───────────────────────────────
    public static float getFontScale(Context context) {
        String size = getTextSize(context);
        switch (size) {
            case SIZE_SMALL: return SCALE_SMALL;
            case SIZE_LARGE: return SCALE_LARGE;
            default:         return SCALE_MEDIUM;
        }
    }

    /**
     * Áp dụng font scale vào Context.
     * Gọi trong attachBaseContext() của BaseActivity (hoặc Application).
     *
     * @param base Context gốc truyền vào attachBaseContext
     * @return Context đã được override fontScale
     */
    public static android.content.Context applyTextSize(android.content.Context base) {
        float scale = getFontScale(base);

        Configuration config = new Configuration(base.getResources().getConfiguration());

        // Chỉ thay đổi nếu khác với giá trị hiện tại (tránh loop)
        if (config.fontScale != scale) {
            config.fontScale = scale;
        }

        // API 17+: dùng createConfigurationContext để không làm bẩn context gốc
        return base.createConfigurationContext(config);
    }
}