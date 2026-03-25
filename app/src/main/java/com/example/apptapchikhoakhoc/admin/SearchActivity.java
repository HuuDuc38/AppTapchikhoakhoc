package com.example.apptapchikhoakhoc.admin;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.adapter.NewsAdapter;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    // ── Views ──────────────────────────────────────────────────────
    private EditText        etSearch;
    private RecyclerView    recyclerView;
    private TextView        tvEmpty;
    private ImageButton     btnBack;
    private ImageView       searchIcon;
    private LinearLayout    headerSearch;
    private LinearLayout    searchContainer;
    private AppBarLayout    appBarLayout;
    private CoordinatorLayout rootContainer;
    private NestedScrollView nestedScroll;

    // ── Data ───────────────────────────────────────────────────────
    private DatabaseHelper  dbHelper;
    private NewsAdapter     adapter;
    private List<Article>   articleList = new ArrayList<>();
    private boolean         isDark;

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        isDark   = AdminThemeManager.isDarkMode(this);
        dbHelper = new DatabaseHelper(this);

        initViews();
        applySystemBarsTheme();
        applyTheme();
        setupRecycler();
        setupSearch();
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════

    private void initViews() {
        rootContainer   = findViewById(R.id.root_container);
        appBarLayout    = findViewById(R.id.appBarLayout);
        headerSearch    = findViewById(R.id.header_search);
        searchContainer = findViewById(R.id.search_container);
        nestedScroll    = findViewById(R.id.nested_scroll);
        etSearch        = findViewById(R.id.et_search);
        searchIcon      = findViewById(R.id.search_icon);
        recyclerView    = findViewById(R.id.recycler_view);
        tvEmpty         = findViewById(R.id.tv_empty);
        btnBack         = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    // ══════════════════════════════════════════════════════════════
    //  SYSTEM BARS
    // ══════════════════════════════════════════════════════════════

    private void applySystemBarsTheme() {
        Window window = getWindow();

        if (isDark) {
            window.setStatusBarColor(AdminThemeManager.DarkColors.STATUS_BAR); // #141D29
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(AdminThemeManager.DarkColors.BACKGROUND);
            // Icon trắng
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
    //  THEME
    // ══════════════════════════════════════════════════════════════

    private void applyTheme() {
        if (isDark) {
            applyDarkTheme();
        } else {
            applyLightTheme();
        }
    }

    private void applyDarkTheme() {
        int bgPage    = AdminThemeManager.DarkColors.BACKGROUND;      // #1A2332
        int bgHeader  = AdminThemeManager.DarkColors.STATUS_BAR;      // #141D29
        int bgInput   = Color.parseColor("#1E2D3D");                   // input field dark
        int txtColor  = AdminThemeManager.DarkColors.TEXT_PRIMARY;    // #FFFFFF
        int hintColor = AdminThemeManager.DarkColors.TEXT_TERTIARY;   // #6B7C91
        int iconColor = AdminThemeManager.DarkColors.TEXT_SECONDARY;  // #8A9BAE
        int emptyTxt  = AdminThemeManager.DarkColors.TEXT_TERTIARY;

        // Page & scroll background
        if (rootContainer != null) rootContainer.setBackgroundColor(bgPage);
        if (nestedScroll  != null) nestedScroll.setBackgroundColor(bgPage);

        // AppBar & header
        if (appBarLayout  != null) appBarLayout.setBackgroundColor(bgHeader);
        if (headerSearch  != null) headerSearch.setBackgroundColor(bgHeader);

        // Search input box: nền tối, viền xanh đậm
        if (searchContainer != null) {
            GradientDrawable inputBg = new GradientDrawable();
            inputBg.setColor(bgInput);
            inputBg.setCornerRadius(dpToPx(23)); // bo tròn pill
            inputBg.setStroke(dpToPx(1), Color.parseColor("#2E3D4F"));
            searchContainer.setBackground(inputBg);
        }

        // EditText
        if (etSearch != null) {
            etSearch.setTextColor(txtColor);
            etSearch.setHintTextColor(hintColor);
        }

        // Search icon
        if (searchIcon != null) searchIcon.setColorFilter(iconColor);

        // Empty text
        if (tvEmpty != null) tvEmpty.setTextColor(emptyTxt);
    }

    private void applyLightTheme() {
        // Light: giữ nguyên XML defaults
        if (rootContainer != null) rootContainer.setBackgroundColor(Color.WHITE);
        if (nestedScroll  != null) nestedScroll.setBackgroundColor(Color.WHITE);
        // appBar giữ gradient đỏ từ XML
        // searchContainer giữ @drawable/search_background từ XML

        if (etSearch   != null) {
            etSearch.setTextColor(Color.parseColor("#000000"));
            etSearch.setHintTextColor(Color.parseColor("#999999"));
        }
        if (searchIcon != null) searchIcon.setColorFilter(Color.parseColor("#757575"));
        if (tvEmpty    != null) tvEmpty.setTextColor(Color.parseColor("#BBBBBB"));
    }

    // ══════════════════════════════════════════════════════════════
    //  RECYCLER + SEARCH
    // ══════════════════════════════════════════════════════════════

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (keyword.length() >= 2) {
                    searchArticles(keyword);
                } else {
                    adapter.setList(null);
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Vui lòng nhập nội dung để tìm kiếm");
                }
            }
        });
    }

    private void searchArticles(String keyword) {
        List<Article> results = dbHelper.searchArticles(keyword);
        articleList.clear();
        articleList.addAll(results);
        adapter.notifyDataSetChanged();

        if (results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không tìm thấy bài viết nào");
            Toast.makeText(this, "Không tìm thấy bài viết nào", Toast.LENGTH_SHORT).show();
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPER
    // ══════════════════════════════════════════════════════════════

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}