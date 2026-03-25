package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

public class ContactAdminActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────
    private Toolbar      toolbar;
    private LinearLayout contentLayout;
    private android.widget.ScrollView rootScrollView; // nếu có wrap ScrollView

    private TextView tvContactTitle;
    private TextView tvHotline,   tvEmail,   tvZalo;
    private TextView tvLabelCall, tvLabelEmail, tvLabelZalo;
    private CardView cardCall,    cardEmail,    cardZalo;

    private static final String ADMIN_HOTLINE = "0969 720 324";
    private static final String ADMIN_EMAIL   = "nguyenhuuduc26072004@gmail.com";
    private static final String ADMIN_ZALO    = "0969 720 324";

    private boolean isDark;

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_admin);

        isDark = ThemeManager.isDarkMode(this);

        initViews();
        applySystemBars();
        applyDarkModeTheme();
        displayInfo();
        setupClickActions();
    }

    // ══════════════════════════════════════════════════════════════
    //  SYSTEM BARS
    // ══════════════════════════════════════════════════════════════

    private void applySystemBars() {
        Window window = getWindow();
        if (isDark) {
            int darkBg = ThemeManager.DarkColors.BACKGROUND; // #1A2332
            window.setStatusBarColor(darkBg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(darkBg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                window.getDecorView().setSystemUiVisibility(0); // icon trắng
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int f = window.getDecorView().getSystemUiVisibility();
                f &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(f);
            }
        } else {
            window.setStatusBarColor(Color.parseColor("#2D68C4")); // xanh toolbar
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
    //  INIT
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        toolbar        = findViewById(R.id.toolbar);
        contentLayout  = findViewById(R.id.contentLayout);

        tvContactTitle = findViewById(R.id.tvContactTitle);

        tvHotline      = findViewById(R.id.tvHotline);
        tvEmail        = findViewById(R.id.tvEmail);
        tvZalo         = findViewById(R.id.tvZalo);

        tvLabelCall    = findViewById(R.id.tvLabelCall);
        tvLabelEmail   = findViewById(R.id.tvLabelEmail);
        tvLabelZalo    = findViewById(R.id.tvLabelZalo);

        cardCall       = findViewById(R.id.cardCall);
        cardEmail      = findViewById(R.id.cardEmail);
        cardZalo       = findViewById(R.id.cardZalo);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  DARK MODE THEME
    // ══════════════════════════════════════════════════════════════

    private void applyDarkModeTheme() {
        int bgPage  = isDark ? ThemeManager.DarkColors.BACKGROUND      : Color.parseColor("#F5F7FA");
        int bgCard  = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int txtPri  = isDark ? ThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSec  = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#666666");

        // Màu accent xanh dương — giữ nguyên cả 2 mode
        int txtAccent = Color.parseColor("#2D68C4");

        // Toolbar
        if (toolbar != null) {
            int toolbarBg = isDark ? ThemeManager.DarkColors.STATUS_BAR : Color.parseColor("#2D68C4");
            toolbar.setBackgroundColor(toolbarBg);
        }

        // Nền trang
        if (contentLayout != null) contentLayout.setBackgroundColor(bgPage);

        // Tiêu đề
        if (tvContactTitle != null) tvContactTitle.setTextColor(txtPri);

        // Cards
        setCardBg(cardCall,  bgCard);
        setCardBg(cardEmail, bgCard);
        setCardBg(cardZalo,  bgCard);

        // Label (Gọi hotline / Gửi email / Nhắn tin Zalo)
        if (tvLabelCall  != null) tvLabelCall.setTextColor(txtSec);
        if (tvLabelEmail != null) tvLabelEmail.setTextColor(txtSec);
        if (tvLabelZalo  != null) tvLabelZalo.setTextColor(txtSec);

        // Giá trị (số điện thoại / email) — giữ màu xanh nổi bật
        if (tvHotline != null) tvHotline.setTextColor(txtAccent);
        if (tvEmail   != null) tvEmail.setTextColor(txtAccent);
        if (tvZalo    != null) tvZalo.setTextColor(txtAccent);
    }

    private void setCardBg(CardView card, int color) {
        if (card != null) card.setCardBackgroundColor(color);
    }

    // ══════════════════════════════════════════════════════════════
    //  DISPLAY INFO
    // ══════════════════════════════════════════════════════════════

    private void displayInfo() {
        tvHotline.setText(ADMIN_HOTLINE);
        tvEmail.setText(ADMIN_EMAIL);
        tvZalo.setText(ADMIN_ZALO.isEmpty() ? "Chưa cung cấp" : ADMIN_ZALO);
    }

    // ══════════════════════════════════════════════════════════════
    //  CLICK ACTIONS
    // ══════════════════════════════════════════════════════════════

    private void setupClickActions() {
        cardCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + ADMIN_HOTLINE));
            startActivity(intent);
        });

        cardEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + ADMIN_EMAIL));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Yêu cầu cấp tài khoản Admin - App Tạp chí KH");
            try {
                startActivity(Intent.createChooser(intent, "Gửi email qua..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Không có ứng dụng email nào!", Toast.LENGTH_SHORT).show();
            }
        });

        cardZalo.setOnClickListener(v -> {
            if (ADMIN_ZALO.isEmpty()) return;
            try {
                String url = "https://zalo.me/" + ADMIN_ZALO.replaceAll("\\s", "");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Không thể mở Zalo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}