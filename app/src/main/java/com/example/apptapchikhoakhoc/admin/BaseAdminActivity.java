package com.example.apptapchikhoakhoc.admin;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apptapchikhoakhoc.utils.AdminLocaleManager;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

/**
 * BaseAdminActivity — base riêng cho toàn bộ màn hình Admin.
 *
 * ✅ Locale admin hoàn toàn độc lập với User
 * ✅ KHÔNG đồng bộ sang AppSettings → MainActivity KHÔNG bị ảnh hưởng
 * ✅ Tự động recreate() khi ngôn ngữ admin thay đổi
 */
public abstract class BaseAdminActivity extends AppCompatActivity {

    protected boolean isDarkMode;

    // ── Instance field, KHÔNG phải static ────────────────────────
    // Bug cũ: static field bị attachBaseContext() overwrite ngay lập tức
    // → onResume() luôn thấy appliedLocale == savedLocale → không recreate()
    private String appliedAdminLocale = null;

    // ════════════════════════════════════════════════════════════
    //  attachBaseContext — áp locale TRƯỚC khi inflate layout
    // ════════════════════════════════════════════════════════════

    @Override
    protected void attachBaseContext(Context newBase) {
        String adminLang = AdminLocaleManager.getSavedLanguage(newBase);
        // KHÔNG set appliedAdminLocale ở đây
        // appliedAdminLocale sẽ được set trong onCreate()
        Context localizedCtx = AdminLocaleManager.applyLocale(newBase, adminLang);
        super.attachBaseContext(localizedCtx);
    }

    // ════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDarkMode = AdminThemeManager.isDarkMode(this);
        // Ghi nhớ locale lúc Activity được tạo (sau khi attachBaseContext đã chạy)
        appliedAdminLocale = AdminLocaleManager.getSavedLanguage(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Đọc locale hiện tại từ SharedPrefs
        String currentLang = AdminLocaleManager.getSavedLanguage(this);

        // Nếu khác với locale lúc create → user vừa đổi ngôn ngữ → recreate
        if (appliedAdminLocale != null && !currentLang.equals(appliedAdminLocale)) {
            // KHÔNG update appliedAdminLocale ở đây
            // onCreate() sẽ tự set lại đúng sau recreate()
            recreate();
            return;
        }

        isDarkMode = AdminThemeManager.isDarkMode(this);
    }

    // ════════════════════════════════════════════════════════════
    //  HELPER — gọi từ SettingsActivity khi user chọn ngôn ngữ
    // ════════════════════════════════════════════════════════════

    /**
     * Lưu ngôn ngữ CHỈ vào AdminSettings.
     * KHÔNG đụng AppSettings → MainActivity KHÔNG bị ảnh hưởng.
     */
    protected void applyAndSaveAdminLanguage(String lang) {
        // Chỉ lưu vào AdminSettings — KHÔNG gọi LocaleManager.saveLanguage()
        AdminLocaleManager.saveLanguage(this, lang);
        // KHÔNG update appliedAdminLocale ở đây
        // Khi các admin activity khác resume, onResume() sẽ detect và recreate()
        recreate();
    }
}