package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

public class StatsActivity extends BaseAdminActivity {

    private DatabaseHelper db;

    private TextView  tvTotalArticles, tvPendingArticles, tvTotalUsers,
            tvTotalViews, tvTotalLikes, tvTotalComments;
    private ImageView btnBack;

    private ScrollView   rootScrollView;
    private LinearLayout headerLayout;
    private LinearLayout innerContentLayout;

    private CardView cardApproved, cardPending, cardUsers,
            cardViews, cardLikes, cardComments;

    private TextView tvSectionContent, tvSectionInteraction;

    // ══════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = new DatabaseHelper(this);

        initViews();
        applySystemBarsTheme();
        applyTheme();
        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarsTheme();
        applyTheme();
        loadStats();
    }

    // ══════════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        rootScrollView     = findViewById(R.id.rootScrollView);
        headerLayout       = findViewById(R.id.headerLayout);
        innerContentLayout = findViewById(R.id.innerContentLayout);
        btnBack            = findViewById(R.id.btn_back);

        tvTotalArticles   = findViewById(R.id.tvTotalArticles);
        tvPendingArticles = findViewById(R.id.tvPendingArticles);
        tvTotalUsers      = findViewById(R.id.tvTotalUsers);
        tvTotalViews      = findViewById(R.id.tvTotalViews);
        tvTotalLikes      = findViewById(R.id.tvTotalLikes);
        tvTotalComments   = findViewById(R.id.tvTotalComments);

        cardApproved = findViewById(R.id.cardApproved);
        cardPending  = findViewById(R.id.cardPending);
        cardUsers    = findViewById(R.id.cardUsers);
        cardViews    = findViewById(R.id.cardViews);
        cardLikes    = findViewById(R.id.cardLikes);
        cardComments = findViewById(R.id.cardComments);

        tvSectionContent     = findViewById(R.id.tvSectionContent);
        tvSectionInteraction = findViewById(R.id.tvSectionInteraction);

        // ── Nút back ──
        btnBack.setOnClickListener(v -> finish());

        // ── Card người dùng ──
        cardUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, UserStatsDetailActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // ── Card lượt xem ──
        cardViews.setOnClickListener(v -> {
            startActivity(new Intent(this, ViewStatsDetailActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // ── Card lượt thích ──
        cardLikes.setOnClickListener(v -> {
            startActivity(new Intent(this, LikeStatsDetailActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // ── Card bình luận ──
        cardComments.setOnClickListener(v -> {
            startActivity(new Intent(this, CommentStatsDetailActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  System bars
    // ══════════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════════
    //  Theme
    // ══════════════════════════════════════════════════════════════

    private void applyTheme() {
        if (isDarkMode) {
            int bgPage = AdminThemeManager.DarkColors.BACKGROUND;
            int bgCard = AdminThemeManager.DarkColors.CARD_BACKGROUND;
            int bgHead = AdminThemeManager.DarkColors.STATUS_BAR;

            if (rootScrollView     != null) rootScrollView.setBackgroundColor(bgPage);
            if (innerContentLayout != null) innerContentLayout.setBackgroundColor(bgPage);
            if (headerLayout       != null) headerLayout.setBackgroundColor(bgHead);

            setCard(cardApproved, bgCard);
            setCard(cardPending,  bgCard);
            setCard(cardUsers,    bgCard);
            setCard(cardViews,    bgCard);
            setCard(cardLikes,    bgCard);
            setCard(cardComments, bgCard);

        } else {
            if (rootScrollView != null) rootScrollView.setBackgroundColor(Color.WHITE);
            if (headerLayout   != null) headerLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
        }
    }

    private void setCard(CardView card, int color) {
        if (card != null) card.setCardBackgroundColor(color);
    }

    // ══════════════════════════════════════════════════════════════
    //  Load stats
    // ══════════════════════════════════════════════════════════════

    private void loadStats() {
        if (tvTotalArticles   != null) tvTotalArticles.setText(String.valueOf(db.getTotalApprovedArticles()));
        if (tvPendingArticles != null) tvPendingArticles.setText(String.valueOf(db.getTotalPendingArticles()));
        if (tvTotalUsers      != null) tvTotalUsers.setText(String.valueOf(db.getTotalUsers()));
        if (tvTotalViews      != null) tvTotalViews.setText(String.valueOf(db.getTotalViews()));
        if (tvTotalLikes      != null) tvTotalLikes.setText(String.valueOf(db.getTotalLikes()));
        if (tvTotalComments   != null) tvTotalComments.setText(String.valueOf(db.getTotalComments()));
    }
}