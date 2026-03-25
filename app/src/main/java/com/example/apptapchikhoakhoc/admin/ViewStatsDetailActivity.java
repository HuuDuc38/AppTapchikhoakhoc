package com.example.apptapchikhoakhoc.admin;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.ArrayList;

public class ViewStatsDetailActivity extends BaseAdminActivity {

    private ImageView    btnBack;
    private RecyclerView recyclerView;
    private TextView     tvEmpty;
    private TextView     tvSummaryTotal;
    private TextView     tvSummaryArticles;
    private TextView     tvAvgViews;
    private LinearLayout headerLayout;
    private LinearLayout rootLayout;
    private CardView     cardTotalViews;
    private CardView     cardAvgViews;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stats_detail);

        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = new DatabaseHelper(this);

        initViews();
        applySystemBarsTheme();
        applyTheme();
        loadViewStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarsTheme();
        applyTheme();
        loadViewStats();
    }

    private void initViews() {
        rootLayout        = findViewById(R.id.rootLayout);
        headerLayout      = findViewById(R.id.headerLayout);
        btnBack           = findViewById(R.id.btnBack);
        recyclerView      = findViewById(R.id.recyclerView);
        tvEmpty           = findViewById(R.id.tvEmpty);
        tvSummaryTotal    = findViewById(R.id.tvSummaryTotal);
        tvSummaryArticles = findViewById(R.id.tvSummaryArticles);
        tvAvgViews        = findViewById(R.id.tvAvgViews);
        cardTotalViews    = findViewById(R.id.cardTotalViews);
        cardAvgViews      = findViewById(R.id.cardAvgViews);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

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

    private void applyTheme() {
        if (isDarkMode) {
            int bgPage = AdminThemeManager.DarkColors.BACKGROUND;
            int bgCard = AdminThemeManager.DarkColors.CARD_BACKGROUND;
            int bgHead = AdminThemeManager.DarkColors.STATUS_BAR;
            if (rootLayout     != null) rootLayout.setBackgroundColor(bgPage);
            if (headerLayout   != null) headerLayout.setBackgroundColor(bgHead);
            if (cardTotalViews != null) cardTotalViews.setCardBackgroundColor(bgCard);
            if (cardAvgViews   != null) cardAvgViews.setCardBackgroundColor(bgCard);
            setTv(tvSummaryTotal,    Color.WHITE);
            setTv(tvSummaryArticles, Color.parseColor("#64B5F6"));
            setTv(tvAvgViews,        Color.WHITE);
            setTv(tvEmpty,           Color.parseColor("#888888"));
        } else {
            if (rootLayout     != null) rootLayout.setBackgroundColor(Color.parseColor("#F4F6F8"));
            if (headerLayout   != null) headerLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
            if (cardTotalViews != null) cardTotalViews.setCardBackgroundColor(Color.WHITE);
            if (cardAvgViews   != null) cardAvgViews.setCardBackgroundColor(Color.WHITE);
            setTv(tvSummaryTotal,    Color.parseColor("#1A1A1A"));
            setTv(tvSummaryArticles, Color.parseColor("#1565C0"));
            setTv(tvAvgViews,        Color.parseColor("#1A1A1A"));
            setTv(tvEmpty,           Color.parseColor("#AAAAAA"));
        }
    }

    private void setTv(TextView tv, int color) {
        if (tv != null) tv.setTextColor(color);
    }

    private void loadViewStats() {
        ArrayList<Article> list = db.getArticlesWithViews();

        // Tổng & trung bình
        int total = db.getTotalViews();
        int avg   = (list != null && !list.isEmpty()) ? total / list.size() : 0;

        if (tvSummaryTotal    != null) tvSummaryTotal.setText(String.format("%,d", total));
        if (tvSummaryArticles != null) tvSummaryArticles.setText((list != null ? list.size() : 0) + " bài viết");
        if (tvAvgViews        != null) tvAvgViews.setText(String.format("%,d", avg));

        animateCard(cardTotalViews, 0);
        animateCard(cardAvgViews,  100);

        if (list == null || list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ViewStatsAdapter(list, isDarkMode));
    }

    private void animateCard(View card, long delay) {
        if (card == null) return;
        card.setAlpha(0f);
        card.setTranslationY(-24f);
        card.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator()).start();
    }

    // ── Adapter nội bộ ────────────────────────────────────────────

    private class ViewStatsAdapter extends RecyclerView.Adapter<ViewStatsAdapter.VH> {

        private final ArrayList<Article> items;
        private final boolean            dark;
        private final int                maxViews;

        ViewStatsAdapter(ArrayList<Article> items, boolean dark) {
            this.items    = items;
            this.dark     = dark;
            this.maxViews = !items.isEmpty() ? items.get(0).getViewCount() : 1;
        }

        @Override
        public VH onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.item_view_stat, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Article item    = items.get(pos);
            int     views   = item.getViewCount();
            int     progress = maxViews > 0 ? (views * 100 / maxViews) : 0;

            h.tvRank.setText(String.valueOf(pos + 1));
            h.tvTitle.setText(item.getTitle());
            h.tvViewCount.setText(String.format("%,d", views));

            String cat = item.getCategory();
            if (cat != null && !cat.isEmpty()) {
                h.tvCategory.setVisibility(View.VISIBLE);
                h.tvCategory.setText(cat);
            } else {
                h.tvCategory.setVisibility(View.GONE);
            }

            // Progress animation
            h.progressBar.setProgress(0);
            ObjectAnimator anim = ObjectAnimator.ofInt(h.progressBar, "progress", 0, progress);
            anim.setDuration(700);
            anim.setStartDelay(pos * 80L);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.start();

            // Rank badge
            switch (pos) {
                case 0: h.tvRank.setBackgroundResource(R.drawable.rank_gold);   h.tvRank.setTextColor(Color.parseColor("#B8860B")); break;
                case 1: h.tvRank.setBackgroundResource(R.drawable.rank_silver); h.tvRank.setTextColor(Color.parseColor("#707070")); break;
                case 2: h.tvRank.setBackgroundResource(R.drawable.rank_bronze); h.tvRank.setTextColor(Color.parseColor("#A0522D")); break;
                default:h.tvRank.setBackgroundResource(R.drawable.rank_normal); h.tvRank.setTextColor(Color.parseColor("#999999")); break;
            }

            // Theme
            if (dark) {
                h.card.setCardBackgroundColor(AdminThemeManager.DarkColors.CARD_BACKGROUND);
                h.tvTitle.setTextColor(Color.WHITE);
                h.tvCategory.setTextColor(Color.parseColor("#AAAAAA"));
                h.tvViewCount.setTextColor(Color.parseColor("#64B5F6"));
            } else {
                h.card.setCardBackgroundColor(Color.WHITE);
                h.tvTitle.setTextColor(Color.parseColor("#1A1A1A"));
                h.tvCategory.setTextColor(Color.parseColor("#999999"));
                h.tvViewCount.setTextColor(Color.parseColor("#1565C0"));
            }

            // Fade-in
            h.itemView.setAlpha(0f);
            h.itemView.animate().alpha(1f).setDuration(300).setStartDelay(pos * 60L).start();
        }

        @Override public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            CardView    card;
            TextView    tvRank, tvTitle, tvCategory, tvViewCount;
            ProgressBar progressBar;
            VH(View v) {
                super(v);
                card        = v.findViewById(R.id.card);
                tvRank      = v.findViewById(R.id.tvRank);
                tvTitle     = v.findViewById(R.id.tvTitle);
                tvCategory  = v.findViewById(R.id.tvCategory);
                tvViewCount = v.findViewById(R.id.tvViewCount);
                progressBar = v.findViewById(R.id.progressBar);
            }
        }
    }
}