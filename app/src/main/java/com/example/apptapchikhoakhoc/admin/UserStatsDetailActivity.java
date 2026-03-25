package com.example.apptapchikhoakhoc.admin;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.UserItem;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.List;

public class UserStatsDetailActivity extends BaseAdminActivity {

    // ── Views ──────────────────────────────────────────────────────
    private ImageView    btnBack;
    private RecyclerView recyclerView;
    private TextView     tvEmpty;
    private TextView     tvSummaryTotal;
    private TextView     tvSummaryLabel;
    private LinearLayout headerLayout;
    private LinearLayout rootLayout;
    private CardView     cardTotalUsers;

    // ── Data ───────────────────────────────────────────────────────
    private DatabaseHelper db;

    // ══════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stats_detail);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = new DatabaseHelper(this);

        initViews();
        applySystemBarsTheme();
        applyTheme();
        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarsTheme();
        applyTheme();
        loadUsers();
    }

    // ══════════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        rootLayout     = findViewById(R.id.rootLayout);
        headerLayout   = findViewById(R.id.headerLayout);
        btnBack        = findViewById(R.id.btn_back);
        recyclerView   = findViewById(R.id.recyclerView);
        tvEmpty        = findViewById(R.id.tvEmpty);
        tvSummaryTotal = findViewById(R.id.tvSummaryTotal);
        tvSummaryLabel = findViewById(R.id.tvSummaryLabel);
        cardTotalUsers = findViewById(R.id.cardTotalUsers);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

            if (rootLayout     != null) rootLayout.setBackgroundColor(bgPage);
            if (headerLayout   != null) headerLayout.setBackgroundColor(bgHead);
            if (cardTotalUsers != null) cardTotalUsers.setCardBackgroundColor(bgCard);

            setTv(tvSummaryTotal, Color.WHITE);
            setTv(tvSummaryLabel, Color.parseColor("#CE93D8"));
            setTv(tvEmpty,        Color.parseColor("#888888"));

        } else {
            if (rootLayout     != null) rootLayout.setBackgroundColor(Color.parseColor("#F4F6F8"));
            if (headerLayout   != null) headerLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
            if (cardTotalUsers != null) cardTotalUsers.setCardBackgroundColor(Color.WHITE);

            setTv(tvSummaryTotal, Color.parseColor("#111111"));
            setTv(tvSummaryLabel, Color.parseColor("#7B1FA2"));
            setTv(tvEmpty,        Color.parseColor("#888888"));
        }
    }

    private void setTv(TextView tv, int color) {
        if (tv != null) tv.setTextColor(color);
    }

    // ══════════════════════════════════════════════════════════════
    //  Load data
    // ══════════════════════════════════════════════════════════════

    private void loadUsers() {
        List<UserItem> list = db.getAllUsersForAdmin();

        if (tvSummaryTotal != null) tvSummaryTotal.setText(String.valueOf(list.size()));

        animateCard(cardTotalUsers, 0);

        if (list.isEmpty()) {
            if (tvEmpty      != null) tvEmpty.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            return;
        }

        if (tvEmpty      != null) tvEmpty.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new UserStatsAdapter(this, list, isDarkMode));
    }

    // ══════════════════════════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════════════════════════

    private void animateCard(View card, long delay) {
        if (card == null) return;
        card.setAlpha(0f);
        card.setTranslationY(-24f);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}