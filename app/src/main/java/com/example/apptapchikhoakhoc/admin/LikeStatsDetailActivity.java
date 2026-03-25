package com.example.apptapchikhoakhoc.admin;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.List;

public class LikeStatsDetailActivity extends BaseAdminActivity {

    private ImageView    btnBack;
    private RecyclerView recyclerView;
    private TextView     tvEmpty;
    private TextView     tvSummaryTotal;
    private TextView     tvSummaryArticles;
    private TextView     tvAvgLikes;
    private LinearLayout headerLayout;
    private LinearLayout rootLayout;
    private CardView     cardTotalLikes;
    private CardView     cardAvgLikes;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_stats_detail);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = new DatabaseHelper(this);

        initViews();
        applySystemBarsTheme();
        applyTheme();
        loadLikeStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarsTheme();
        applyTheme();
        loadLikeStats();
    }

    // ── Init ──────────────────────────────────────────────────────

    private void initViews() {
        rootLayout        = findViewById(R.id.rootLayout);
        headerLayout      = findViewById(R.id.headerLayout);
        btnBack           = findViewById(R.id.btn_back);
        recyclerView      = findViewById(R.id.recyclerView);
        tvEmpty           = findViewById(R.id.tvEmpty);
        tvSummaryTotal    = findViewById(R.id.tvSummaryTotal);
        tvSummaryArticles = findViewById(R.id.tvSummaryArticles);
        tvAvgLikes        = findViewById(R.id.tvAvgLikes);
        cardTotalLikes    = findViewById(R.id.cardTotalLikes);
        cardAvgLikes      = findViewById(R.id.cardAvgLikes);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    // ── System bars ───────────────────────────────────────────────

    private void applySystemBarsTheme() {
        Window window = getWindow();
        if (isDarkMode) {
            window.setStatusBarColor(AdminThemeManager.DarkColors.STATUS_BAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(AdminThemeManager.DarkColors.BACKGROUND);
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

    // ── Theme ─────────────────────────────────────────────────────

    private void applyTheme() {
        if (isDarkMode) {
            int bgPage = AdminThemeManager.DarkColors.BACKGROUND;
            int bgCard = AdminThemeManager.DarkColors.CARD_BACKGROUND;
            int bgHead = AdminThemeManager.DarkColors.STATUS_BAR;

            if (rootLayout   != null) rootLayout.setBackgroundColor(bgPage);
            if (headerLayout != null) headerLayout.setBackgroundColor(bgHead);
            if (cardTotalLikes != null) cardTotalLikes.setCardBackgroundColor(bgCard);
            if (cardAvgLikes   != null) cardAvgLikes.setCardBackgroundColor(bgCard);

            setTextColor(tvSummaryTotal,    Color.WHITE);
            setTextColor(tvSummaryArticles, Color.parseColor("#FF6B8A"));
            setTextColor(tvAvgLikes,        Color.WHITE);
            setTextColor(tvEmpty,           Color.parseColor("#888888"));

        } else {
            if (rootLayout   != null) rootLayout.setBackgroundColor(Color.parseColor("#F4F6F8"));
            if (headerLayout != null) headerLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
            if (cardTotalLikes != null) cardTotalLikes.setCardBackgroundColor(Color.WHITE);
            if (cardAvgLikes   != null) cardAvgLikes.setCardBackgroundColor(Color.WHITE);

            setTextColor(tvSummaryTotal,    Color.parseColor("#1A1A1A"));
            setTextColor(tvSummaryArticles, Color.parseColor("#C8463D"));
            setTextColor(tvAvgLikes,        Color.parseColor("#1A1A1A"));
            setTextColor(tvEmpty,           Color.parseColor("#AAAAAA"));
        }
    }

    private void setTextColor(TextView tv, int color) {
        if (tv != null) tv.setTextColor(color);
    }

    // ── Load data ─────────────────────────────────────────────────

    private void loadLikeStats() {
        // Lấy tổng like từ DB (dùng getTotalLikes() đã có sẵn)
        int totalLikes = db.getTotalLikes();

        // Lấy danh sách bài theo like
        List<ArticleStatItem> list = db.getArticlesOrderedByLikes();

        // Cập nhật summary
        int avg = (list != null && !list.isEmpty()) ? totalLikes / list.size() : 0;

        setTextSafe(tvSummaryTotal,    String.format("%,d", totalLikes));
        setTextSafe(tvSummaryArticles, (list != null ? list.size() : 0) + " bài viết");
        setTextSafe(tvAvgLikes,        String.format("%,d", avg));

        // Animate summary cards
        animateCard(cardTotalLikes, 0);
        animateCard(cardAvgLikes,  100);

        if (list == null || list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new LikeStatsAdapter(this, list, isDarkMode));
    }

    // ── Helpers ───────────────────────────────────────────────────

    private void setTextSafe(TextView tv, String text) {
        if (tv != null) tv.setText(text);
    }

    private void animateCard(View card, long delay) {
        if (card == null) return;
        card.setAlpha(0f);
        card.setTranslationY(-24f);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }
}