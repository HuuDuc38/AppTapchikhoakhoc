package com.example.apptapchikhoakhoc.main;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends BaseActivity {

    private static final int TAB_ALL   = 0;
    private static final int TAB_MY    = 1;
    private static final int TAB_SAVED = 2;

    private int currentTab = TAB_ALL;

    // ── Toolbar ────────────────────────────────────────────────────
    private LinearLayout toolbarLayout;
    private ImageButton  btnBack;
    private TextView     tvTitle;
    private View         viewNotifDot;

    // ── Tab bar ────────────────────────────────────────────────────
    private LinearLayout tabAll, tabMy, tabSaved;
    private TextView     tvTabAll, tvTabMy, tvTabSaved;
    private View         indicatorAll, indicatorMy, indicatorSaved;
    private LinearLayout tabBarLayout;
    private View         tabBottomBorder;

    // ── Scroll content ─────────────────────────────────────────────
    private android.widget.ScrollView scrollContent;

    // ── Section: Bài viết của tôi ──────────────────────────────────
    private LinearLayout sectionMyArticles;
    private TextView     tvEmptyMyArticles;
    private RecyclerView recyclerMyArticles;
    private NotificationItemAdapter myArticlesAdapter;

    // ── Section: Mới nhất ─────────────────────────────────────────
    private LinearLayout sectionNewArticles;
    private TextView     tvEmptyNewArticles;
    private RecyclerView recyclerNewArticles;
    private NotificationItemAdapter newArticlesAdapter;

    // ── Section: Đã lưu ───────────────────────────────────────────
    private LinearLayout sectionSaved;
    private TextView     tvEmptySaved;
    private RecyclerView recyclerSaved;

    // ── Divider ────────────────────────────────────────────────────
    private View dividerSections;

    // ── Section headers ────────────────────────────────────────────
    private TextView tvSeeAllMy;
    private TextView tvSeeMoreNew;

    // ── User session ───────────────────────────────────────────────
    private SharedPreferences userPrefs;
    private String            currentUserEmail;
    private boolean           isLoggedIn;

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        loadSession();
        initViews();
        initTabs();
        applyTheme();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSession();
        loadData();
    }

    private void loadSession() {
        userPrefs        = getSharedPreferences("UserSession", MODE_PRIVATE);
        isLoggedIn       = userPrefs.getBoolean("isLoggedIn", false);
        currentUserEmail = userPrefs.getString("userEmail", "");
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT VIEWS
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        toolbarLayout   = findViewById(R.id.toolbar_layout);
        btnBack         = findViewById(R.id.btn_back);
        tvTitle         = findViewById(R.id.tv_title);
        viewNotifDot    = findViewById(R.id.view_notif_dot);
        tabBarLayout    = findViewById(R.id.tab_bar_layout);
        tabBottomBorder = findViewById(R.id.tab_bottom_border);
        scrollContent   = findViewById(R.id.scroll_content);

        tabAll         = findViewById(R.id.tab_all);
        tabMy          = findViewById(R.id.tab_my_articles);
        tabSaved       = findViewById(R.id.tab_saved);
        tvTabAll       = findViewById(R.id.tv_tab_all);
        tvTabMy        = findViewById(R.id.tv_tab_my);
        tvTabSaved     = findViewById(R.id.tv_tab_saved);
        indicatorAll   = findViewById(R.id.indicator_all);
        indicatorMy    = findViewById(R.id.indicator_my);
        indicatorSaved = findViewById(R.id.indicator_saved);

        sectionMyArticles   = findViewById(R.id.section_my_articles);
        tvEmptyMyArticles   = findViewById(R.id.tv_empty_my_articles);
        recyclerMyArticles  = findViewById(R.id.recycler_my_articles);

        sectionNewArticles  = findViewById(R.id.section_new_articles);
        tvEmptyNewArticles  = findViewById(R.id.tv_empty_new_articles);
        recyclerNewArticles = findViewById(R.id.recycler_new_articles);

        sectionSaved    = findViewById(R.id.section_saved);
        tvEmptySaved    = findViewById(R.id.tv_empty_saved);
        recyclerSaved   = findViewById(R.id.recycler_saved);
        dividerSections = findViewById(R.id.divider_sections);
        tvSeeAllMy      = findViewById(R.id.tv_see_all_my);
        tvSeeMoreNew    = findViewById(R.id.tv_see_more_new);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        recyclerMyArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyArticles.setNestedScrollingEnabled(false);
        myArticlesAdapter = new NotificationItemAdapter(this, new ArrayList<>(), true);
        recyclerMyArticles.setAdapter(myArticlesAdapter);

        recyclerNewArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerNewArticles.setNestedScrollingEnabled(false);
        newArticlesAdapter = new NotificationItemAdapter(this, new ArrayList<>(), false);
        recyclerNewArticles.setAdapter(newArticlesAdapter);

        if (tvSeeAllMy   != null) tvSeeAllMy.setOnClickListener(v -> selectTab(TAB_MY));
        if (tvSeeMoreNew != null) tvSeeMoreNew.setOnClickListener(v -> selectTab(TAB_ALL));
    }

    // ══════════════════════════════════════════════════════════════
    //  TAB BAR
    // ══════════════════════════════════════════════════════════════

    private void initTabs() {
        tabAll.setOnClickListener(v   -> selectTab(TAB_ALL));
        tabMy.setOnClickListener(v    -> selectTab(TAB_MY));
        tabSaved.setOnClickListener(v -> selectTab(TAB_SAVED));
        selectTab(TAB_ALL);
    }

    private void selectTab(int tab) {
        currentTab = tab;
        int activeColor   = isDarkMode ? ThemeManager.DarkColors.ACCENT : 0xFFC0392B;
        int inactiveColor = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY : 0xFF999999;

        setTabActive(tvTabAll,   indicatorAll,   tab == TAB_ALL,   activeColor, inactiveColor);
        setTabActive(tvTabMy,    indicatorMy,    tab == TAB_MY,    activeColor, inactiveColor);
        setTabActive(tvTabSaved, indicatorSaved, tab == TAB_SAVED, activeColor, inactiveColor);

        switch (tab) {
            case TAB_ALL:   showSectionAll();   break;
            case TAB_MY:    showSectionMy();    break;
            case TAB_SAVED: showSectionSaved(); break;
        }
    }

    private void setTabActive(TextView tvTab, View indicator, boolean active,
                              int activeColor, int inactiveColor) {
        tvTab.setTextColor(active ? activeColor : inactiveColor);
        tvTab.setTypeface(null, active
                ? android.graphics.Typeface.BOLD
                : android.graphics.Typeface.NORMAL);
        indicator.setBackgroundColor(active ? activeColor : Color.TRANSPARENT);
    }

    private boolean hasValidSession() {
        return isLoggedIn && currentUserEmail != null && !currentUserEmail.isEmpty();
    }

    private void showSectionAll() {
        boolean showMy = hasValidSession();
        sectionMyArticles.setVisibility(showMy ? View.VISIBLE : View.GONE);
        sectionNewArticles.setVisibility(View.VISIBLE);
        if (sectionSaved    != null) sectionSaved.setVisibility(View.GONE);
        if (dividerSections != null)
            dividerSections.setVisibility(showMy ? View.VISIBLE : View.GONE);
    }

    private void showSectionMy() {
        sectionMyArticles.setVisibility(hasValidSession() ? View.VISIBLE : View.GONE);
        sectionNewArticles.setVisibility(View.GONE);
        if (sectionSaved    != null) sectionSaved.setVisibility(View.GONE);
        if (dividerSections != null) dividerSections.setVisibility(View.GONE);
    }

    private void showSectionSaved() {
        sectionMyArticles.setVisibility(View.GONE);
        sectionNewArticles.setVisibility(View.GONE);
        if (sectionSaved    != null) sectionSaved.setVisibility(View.VISIBLE);
        if (dividerSections != null) dividerSections.setVisibility(View.GONE);
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME
    // ══════════════════════════════════════════════════════════════

    private void applyTheme() {
        View root = findViewById(R.id.notifications_root);

        int bgColor       = isDarkMode ? ThemeManager.DarkColors.BACKGROUND     : Color.parseColor("#F5F3EF");
        int cardColor     = isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int textPrimary   = isDarkMode ? ThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#2C2C2A");
        int textSecondary = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#AAAAAA");
        int accentColor   = isDarkMode ? ThemeManager.DarkColors.ACCENT          : Color.parseColor("#C0392B");
        int dividerColor  = isDarkMode ? ThemeManager.DarkColors.DIVIDER         : Color.parseColor("#EBE9E4");

        // ── Root + Scroll background ───────────────────────────────
        if (root          != null) root.setBackgroundColor(bgColor);
        if (scrollContent != null) scrollContent.setBackgroundColor(bgColor);

        // ── Scroll inner LinearLayout ──────────────────────────────
        // Set màu cho tất cả LinearLayout con trong scroll
        View scrollInner = findViewById(R.id.scroll_inner);
        if (scrollInner != null) scrollInner.setBackgroundColor(bgColor);

        // ── Toolbar ────────────────────────────────────────────────
        if (toolbarLayout != null) {
            if (isDarkMode) toolbarLayout.setBackgroundColor(ThemeManager.DarkColors.STATUS_BAR);
            else            toolbarLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
        }
        if (btnBack != null) {
            btnBack.setImageTintList(
                    android.content.res.ColorStateList.valueOf(Color.WHITE));
        }
        if (tvTitle != null) tvTitle.setTextColor(Color.WHITE);

        // ── Status bar ─────────────────────────────────────────────
        getWindow().setStatusBarColor(isDarkMode
                ? ThemeManager.DarkColors.STATUS_BAR
                : ThemeManager.LightColors.STATUS_BAR);

        // ── Tab bar ────────────────────────────────────────────────
        if (tabBarLayout    != null) tabBarLayout.setBackgroundColor(cardColor);
        if (tabBottomBorder != null) tabBottomBorder.setBackgroundColor(dividerColor);

        // ── Section headers text (BÀI VIẾT ĐÃ DUYỆT, MỚI NHẤT...) ──
        // Tìm tất cả TextView trong section header và set màu
        applyColorToSectionHeader(R.id.section_my_articles,  textPrimary, accentColor, textSecondary);
        applyColorToSectionHeader(R.id.section_new_articles, textPrimary, accentColor, textSecondary);
        applyColorToSectionHeader(R.id.section_saved,        textPrimary, accentColor, textSecondary);

        // ── Empty state text ───────────────────────────────────────
        if (tvEmptyMyArticles  != null) tvEmptyMyArticles.setTextColor(textSecondary);
        if (tvEmptyNewArticles != null) tvEmptyNewArticles.setTextColor(textSecondary);
        if (tvEmptySaved       != null) tvEmptySaved.setTextColor(textSecondary);

        // ── "Xem tất cả" / "Xem thêm" ────────────────────────────
        if (tvSeeAllMy   != null) tvSeeAllMy.setTextColor(accentColor);
        if (tvSeeMoreNew != null) tvSeeMoreNew.setTextColor(accentColor);

        // ── Tái apply tab ──────────────────────────────────────────
        selectTab(currentTab);
    }

    /**
     * Set màu text cho các TextView trong section header.
     * TextView đầu tiên (tiêu đề in hoa) → textPrimary
     * TextView "Xem tất cả" / "Xem thêm" → accentColor
     */
    private void applyColorToSectionHeader(int sectionId, int textPrimary,
                                           int accentColor, int textSecondary) {
        ViewGroup section = findViewById(sectionId);
        if (section == null) return;
        section.setBackgroundColor(
                isDarkMode ? ThemeManager.DarkColors.BACKGROUND : Color.parseColor("#F5F3EF"));

        // LinearLayout header (hàng đầu tiên trong section)
        if (section.getChildCount() > 0 && section.getChildAt(0) instanceof ViewGroup) {
            ViewGroup header = (ViewGroup) section.getChildAt(0);
            for (int i = 0; i < header.getChildCount(); i++) {
                View child = header.getChildAt(i);
                if (child instanceof TextView) {
                    TextView tv = (TextView) child;
                    String text = tv.getText() != null ? tv.getText().toString() : "";
                    if (text.contains("→") || text.contains("Xem")) {
                        tv.setTextColor(accentColor);
                    } else {
                        tv.setTextColor(textPrimary);
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LOAD DATA
    // ══════════════════════════════════════════════════════════════

    private void loadData() {
        DatabaseHelper db = new DatabaseHelper(this);

        if (hasValidSession()) {
            List<Article> myArticles = getAllMyArticles(db);
            if (myArticles.isEmpty()) {
                tvEmptyMyArticles.setVisibility(View.VISIBLE);
                recyclerMyArticles.setVisibility(View.GONE);
            } else {
                tvEmptyMyArticles.setVisibility(View.GONE);
                recyclerMyArticles.setVisibility(View.VISIBLE);
                myArticlesAdapter.updateList(myArticles);
            }
            boolean hasPending = myArticles.stream()
                    .anyMatch(a -> DatabaseHelper.STATUS_PENDING.equals(a.getStatus()));
            if (viewNotifDot != null)
                viewNotifDot.setVisibility(hasPending ? View.VISIBLE : View.GONE);
        } else {
            if (tvEmptyMyArticles  != null) tvEmptyMyArticles.setVisibility(View.GONE);
            if (recyclerMyArticles != null) recyclerMyArticles.setVisibility(View.GONE);
            if (viewNotifDot       != null) viewNotifDot.setVisibility(View.GONE);
        }

        List<Article> newArticles = db.getLatestApprovedArticles(10);
        if (newArticles.isEmpty()) {
            tvEmptyNewArticles.setVisibility(View.VISIBLE);
            recyclerNewArticles.setVisibility(View.GONE);
        } else {
            tvEmptyNewArticles.setVisibility(View.GONE);
            recyclerNewArticles.setVisibility(View.VISIBLE);
            newArticlesAdapter.updateList(newArticles);
        }

        selectTab(currentTab);
    }

    private List<Article> getAllMyArticles(DatabaseHelper db) {
        List<Article> result = new ArrayList<>();
        List<Article> allArticles = db.getArticlesByUserEmail(currentUserEmail);
        if (!allArticles.isEmpty()) {
            result.addAll(allArticles);
            result.sort((a, b) -> {
                int pa = getStatusPriority(a.getStatus());
                int pb = getStatusPriority(b.getStatus());
                if (pa != pb) return pa - pb;
                return b.getId() - a.getId();
            });
            return result;
        }
        for (Article a : db.getPendingArticles()) {
            if (a.getUserEmail() != null && a.getUserEmail().equalsIgnoreCase(currentUserEmail))
                result.add(a);
        }
        for (Article a : db.getApprovedArticles()) {
            if (a.getUserEmail() != null && a.getUserEmail().equalsIgnoreCase(currentUserEmail))
                result.add(a);
        }
        for (Article a : db.getRejectedArticles()) {
            if (a.getUserEmail() != null && a.getUserEmail().equalsIgnoreCase(currentUserEmail))
                result.add(a);
        }
        result.sort((a, b) -> {
            int pa = getStatusPriority(a.getStatus());
            int pb = getStatusPriority(b.getStatus());
            if (pa != pb) return pa - pb;
            return b.getId() - a.getId();
        });
        return result;
    }

    private int getStatusPriority(String status) {
        if (DatabaseHelper.STATUS_PENDING.equals(status))  return 0;
        if (DatabaseHelper.STATUS_REJECTED.equals(status)) return 1;
        return 2;
    }
}