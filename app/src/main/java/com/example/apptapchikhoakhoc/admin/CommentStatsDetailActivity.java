package com.example.apptapchikhoakhoc.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Comment;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.ArrayList;
import java.util.List;

public class CommentStatsDetailActivity extends BaseAdminActivity {

    // ── Views ──────────────────────────────────────────────────────
    private ImageView    btnBack;
    private RecyclerView recyclerView;
    private TextView     tvEmpty;
    private TextView     tvSummaryTotal;
    private TextView     tvSummaryViolated;
    private TextView     tvFilterAll;
    private TextView     tvFilterViolated;
    private TextView     tvLabelTotal;
    private TextView     tvLabelBinhLuan;
    private TextView     tvLabelViolated;
    private TextView     tvLabelCanXuLy;
    private LinearLayout headerLayout;
    private LinearLayout rootLayout;
    private LinearLayout tabLayout;
    private CardView     cardTotalComments;
    private CardView     cardViolated;
    private View         tabIndicatorAll;
    private View         tabIndicatorViolated;

    // ── Data ───────────────────────────────────────────────────────
    private DatabaseHelper      db;
    private List<Comment>       allItems      = new ArrayList<>();
    private List<Comment>       filteredItems = new ArrayList<>();
    private CommentAdminAdapter adapter;
    private boolean             showViolatedOnly = false;

    // ══════════════════════════════════════════════════════════════
    //  Lifecycle
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_stats_detail);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        db = new DatabaseHelper(this);

        initViews();
        applySystemBarsTheme();
        applyTheme();
        loadComments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySystemBarsTheme();
        applyTheme();
        loadComments();
    }

    // ══════════════════════════════════════════════════════════════
    //  Init
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        rootLayout           = findViewById(R.id.rootLayout);
        headerLayout         = findViewById(R.id.headerLayout);
        tabLayout            = findViewById(R.id.tabLayout);
        btnBack              = findViewById(R.id.btn_back);
        recyclerView         = findViewById(R.id.recyclerView);
        tvEmpty              = findViewById(R.id.tvEmpty);
        tvSummaryTotal       = findViewById(R.id.tvSummaryTotal);
        tvSummaryViolated    = findViewById(R.id.tvSummaryViolated);
        tvFilterAll          = findViewById(R.id.tvFilterAll);
        tvFilterViolated     = findViewById(R.id.tvFilterViolated);
        tvLabelTotal         = findViewById(R.id.tvLabelTotal);
        tvLabelBinhLuan      = findViewById(R.id.tvLabelBinhLuan);
        tvLabelViolated      = findViewById(R.id.tvLabelViolated);
        tvLabelCanXuLy       = findViewById(R.id.tvLabelCanXuLy);
        cardTotalComments    = findViewById(R.id.cardTotalComments);
        cardViolated         = findViewById(R.id.cardViolated);
        tabIndicatorAll      = findViewById(R.id.tabIndicatorAll);
        tabIndicatorViolated = findViewById(R.id.tabIndicatorViolated);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        tvFilterAll.setOnClickListener(v -> {
            showViolatedOnly = false;
            updateTabUI();
            filterAndShow();
        });

        tvFilterViolated.setOnClickListener(v -> {
            showViolatedOnly = true;
            updateTabUI();
            filterAndShow();
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  Tab UI
    // ══════════════════════════════════════════════════════════════

    private void updateTabUI() {
        if (showViolatedOnly) {
            setTvColor(tvFilterAll, isDarkMode
                    ? Color.parseColor("#888888")
                    : Color.parseColor("#777777"));
            setTvColor(tvFilterViolated, isDarkMode
                    ? Color.parseColor("#FF6B6B")
                    : Color.parseColor("#C8463D"));
            if (tabIndicatorAll      != null) tabIndicatorAll.setVisibility(View.INVISIBLE);
            if (tabIndicatorViolated != null) tabIndicatorViolated.setVisibility(View.VISIBLE);
        } else {
            setTvColor(tvFilterAll, isDarkMode
                    ? Color.WHITE
                    : Color.parseColor("#111111"));
            setTvColor(tvFilterViolated, isDarkMode
                    ? Color.parseColor("#888888")
                    : Color.parseColor("#777777"));
            if (tabIndicatorAll      != null) tabIndicatorAll.setVisibility(View.VISIBLE);
            if (tabIndicatorViolated != null) tabIndicatorViolated.setVisibility(View.INVISIBLE);
        }
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

            if (rootLayout        != null) rootLayout.setBackgroundColor(bgPage);
            if (headerLayout      != null) headerLayout.setBackgroundColor(bgHead);
            if (tabLayout         != null) tabLayout.setBackgroundColor(bgCard);
            if (cardTotalComments != null) cardTotalComments.setCardBackgroundColor(bgCard);
            if (cardViolated      != null) cardViolated.setCardBackgroundColor(bgCard);

            // Card tổng bình luận
            setTvColor(tvLabelTotal,    Color.parseColor("#AAAAAA"));
            setTvColor(tvSummaryTotal,  Color.WHITE);
            setTvColor(tvLabelBinhLuan, Color.parseColor("#AAAAAA"));

            // Card vi phạm
            setTvColor(tvLabelViolated,   Color.parseColor("#AAAAAA"));
            setTvColor(tvSummaryViolated, Color.parseColor("#FF6B6B"));
            setTvColor(tvLabelCanXuLy,    Color.parseColor("#FF6B6B"));

            // Empty
            setTvColor(tvEmpty, Color.parseColor("#888888"));

        } else {
            if (rootLayout        != null) rootLayout.setBackgroundColor(Color.parseColor("#F4F6F8"));
            if (headerLayout      != null) headerLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
            if (tabLayout         != null) tabLayout.setBackgroundColor(Color.WHITE);
            if (cardTotalComments != null) cardTotalComments.setCardBackgroundColor(Color.WHITE);
            if (cardViolated      != null) cardViolated.setCardBackgroundColor(Color.WHITE);

            // Card tổng bình luận
            setTvColor(tvLabelTotal,    Color.parseColor("#555555"));
            setTvColor(tvSummaryTotal,  Color.parseColor("#111111"));
            setTvColor(tvLabelBinhLuan, Color.parseColor("#444444"));

            // Card vi phạm
            setTvColor(tvLabelViolated,   Color.parseColor("#555555"));
            setTvColor(tvSummaryViolated, Color.parseColor("#B71C1C"));
            setTvColor(tvLabelCanXuLy,    Color.parseColor("#B71C1C"));

            // Empty
            setTvColor(tvEmpty, Color.parseColor("#888888"));
        }

        // Cập nhật tab sau khi đổi theme
        updateTabUI();
    }

    private void setTvColor(TextView tv, int color) {
        if (tv != null) tv.setTextColor(color);
    }

    // ══════════════════════════════════════════════════════════════
    //  Load & Filter data
    // ══════════════════════════════════════════════════════════════

    private void loadComments() {
        allItems = db.getAllCommentsForAdmin();

        int violated = 0;
        for (Comment c : allItems) if (c.isViolated()) violated++;

        if (tvSummaryTotal    != null) tvSummaryTotal.setText(String.valueOf(allItems.size()));
        if (tvSummaryViolated != null) tvSummaryViolated.setText(String.valueOf(violated));

        animateCard(cardTotalComments, 0);
        animateCard(cardViolated,    100);

        updateTabUI();
        filterAndShow();
    }

    private void filterAndShow() {
        filteredItems.clear();
        for (Comment c : allItems) {
            if (!showViolatedOnly || c.isViolated()) filteredItems.add(c);
        }

        if (filteredItems.isEmpty()) {
            if (tvEmpty      != null) tvEmpty.setVisibility(View.VISIBLE);
            if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            if (tvEmpty != null) {
                tvEmpty.setText(showViolatedOnly
                        ? "Không có bình luận vi phạm"
                        : "Chưa có bình luận nào");
            }
            return;
        }

        if (tvEmpty      != null) tvEmpty.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);

        if (adapter == null) {
            adapter = new CommentAdminAdapter(
                    this,
                    filteredItems,
                    isDarkMode,
                    this::onDeleteComment
            );
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(filteredItems, isDarkMode);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Xóa bình luận
    // ══════════════════════════════════════════════════════════════

    private void onDeleteComment(Comment item, int position) {
        String preview = item.getContent();
        if (preview.length() > 60) preview = preview.substring(0, 60) + "...";

        new AlertDialog.Builder(this)
                .setTitle("Xóa bình luận")
                .setMessage("Bạn có chắc muốn xóa bình luận này không?\n\n\"" + preview + "\"")
                .setPositiveButton("Xóa", (dialog, which) -> doDeleteComment(item, position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doDeleteComment(Comment item, int position) {
        boolean ok = db.deleteComment(item.getId());
        if (ok) {
            allItems.remove(item);
            int filteredPos = filteredItems.indexOf(item);
            if (filteredPos >= 0) {
                filteredItems.remove(filteredPos);
                adapter.notifyItemRemoved(filteredPos);
                adapter.notifyItemRangeChanged(filteredPos, filteredItems.size());
            }

            // Cập nhật summary
            int violated = 0;
            for (Comment c : allItems) if (c.isViolated()) violated++;
            if (tvSummaryTotal    != null) tvSummaryTotal.setText(String.valueOf(allItems.size()));
            if (tvSummaryViolated != null) tvSummaryViolated.setText(String.valueOf(violated));

            Toast.makeText(this, "Đã xóa bình luận", Toast.LENGTH_SHORT).show();

            if (filteredItems.isEmpty()) {
                if (tvEmpty      != null) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(showViolatedOnly
                            ? "Không có bình luận vi phạm"
                            : "Chưa có bình luận nào");
                }
                if (recyclerView != null) recyclerView.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Xóa thất bại, thử lại", Toast.LENGTH_SHORT).show();
        }
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