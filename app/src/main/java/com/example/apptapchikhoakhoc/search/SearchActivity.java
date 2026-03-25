package com.example.apptapchikhoakhoc.search;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.adapter.NewsAdapter;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity {

    // ── UI ───────────────────────────────────────────────────────
    private EditText         etSearch;
    private RecyclerView     recyclerView;
    private TextView         tvEmpty;
    private TextView         tvResultCount;
    private ImageButton      btnBack;
    private ImageView        searchIcon;
    private LinearLayout     headerSearch;
    private LinearLayout     searchContainer;
    private View             rootContainer;
    private NestedScrollView nestedScroll;

    // ── DATA ─────────────────────────────────────────────────────
    private DatabaseHelper      dbHelper;
    private NewsAdapter         adapter;
    private final List<Article> articleList = new ArrayList<>();

    // ── LIFECYCLE ─────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dbHelper = new DatabaseHelper(this);
        initViews();
        applyActivityTheme();
        setupRecycler();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean newDark = ThemeManager.isDarkMode(this);
        if (newDark != isDarkMode) {
            isDarkMode = newDark;
            recreate();
        }
    }

    // ── INIT ──────────────────────────────────────────────────────

    private void initViews() {
        rootContainer   = findViewById(R.id.root_container);
        headerSearch    = findViewById(R.id.header_search);
        searchContainer = findViewById(R.id.search_container);
        etSearch        = findViewById(R.id.et_search);
        searchIcon      = findViewById(R.id.search_icon);
        btnBack         = findViewById(R.id.btn_back);
        recyclerView    = findViewById(R.id.recycler_view);
        tvEmpty         = findViewById(R.id.tv_empty);
        tvResultCount   = findViewById(R.id.tv_result_count);
        nestedScroll    = findViewById(R.id.nested_scroll);
    }

    private void setupRecycler() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewsAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    // ── THEME ─────────────────────────────────────────────────────

    private void applyActivityTheme() {
        int bgColor       = getBackgroundColor();
        int textPrimary   = getTextColor();
        int textSecondary = getSecondaryTextColor();
        int dividerColor  = getDividerColor();

        if (rootContainer != null) rootContainer.setBackgroundColor(bgColor);
        if (nestedScroll  != null) nestedScroll.setBackgroundColor(bgColor);

        if (headerSearch != null) {
            headerSearch.setBackgroundColor(isDarkMode
                    ? ThemeManager.DarkColors.STATUS_BAR
                    : ThemeManager.LightColors.STATUS_BAR);
        }
        View appBar = findViewById(R.id.appBarLayout);
        if (appBar != null) {
            appBar.setBackgroundColor(isDarkMode
                    ? ThemeManager.DarkColors.STATUS_BAR
                    : ThemeManager.LightColors.STATUS_BAR);
        }

        if (searchContainer != null) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setCornerRadius(dpToPx(24));
            bg.setColor(isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE);
            bg.setStroke(dpToPx(1), dividerColor);
            searchContainer.setBackground(bg);
        }

        if (etSearch  != null) {
            etSearch.setTextColor(textPrimary);
            etSearch.setHintTextColor(textSecondary);
        }
        if (searchIcon    != null) searchIcon.setColorFilter(textSecondary, PorterDuff.Mode.SRC_IN);
        if (btnBack       != null) btnBack.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        if (recyclerView  != null) recyclerView.setBackgroundColor(bgColor);
        if (tvEmpty       != null) tvEmpty.setTextColor(textSecondary);
        if (tvResultCount != null) tvResultCount.setTextColor(textSecondary);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    // ── LISTENERS ─────────────────────────────────────────────────

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

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
                    // Xóa kết quả khi ô trống
                    adapter.setList(null);
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Vui lòng nhập nội dung để tìm kiếm");
                    if (tvResultCount != null) tvResultCount.setVisibility(View.GONE);
                }
            }
        });
    }

    // ── SEARCH ────────────────────────────────────────────────────

    private void searchArticles(String keyword) {
        // DB tìm theo: title, author, category, content (không phân biệt hoa/thường)
        List<Article> results = dbHelper.searchArticles(keyword);

        articleList.clear();
        articleList.addAll(results);
        adapter.setList(new ArrayList<>(results));

        if (results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không tìm thấy kết quả cho \"" + keyword + "\"");
            if (tvResultCount != null) tvResultCount.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            if (tvResultCount != null) {
                tvResultCount.setVisibility(View.VISIBLE);
                tvResultCount.setText(results.size() + " kết quả cho \"" + keyword + "\"");
            }
        }
    }
}