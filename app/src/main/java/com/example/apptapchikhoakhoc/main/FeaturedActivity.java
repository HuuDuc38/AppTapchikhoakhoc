package com.example.apptapchikhoakhoc.main;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FeaturedActivity extends BaseActivity {

    // ── Views ──────────────────────────────────────────────────────
    private View              featuredRoot;
    private LinearLayout      toolbar;
    private ImageButton       btnBack;
    private TextView          tvToolbarTitle, tvArticleCount;
    private androidx.core.widget.NestedScrollView scrollView;
    private LinearLayout      scrollInner;
    private View              cardHero;
    private ImageView         imgHero;
    private TextView          tvHeroTitle, tvHeroCategory, tvHeroAuthor, tvHeroAvatar, tvHeroTime;
    private LinearLayout      layoutSectionList;
    private TextView          tvSectionList;
    private RecyclerView      recyclerFeatured;
    private LinearLayout      layoutEmpty;
    private TextView          tvEmpty;
    private FeaturedAdapter   adapter;

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured);

        initViews();
        applyTheme();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    // ── Init ──────────────────────────────────────────────────────

    private void initViews() {
        featuredRoot     = findViewById(R.id.featured_root);
        toolbar          = findViewById(R.id.featured_toolbar);
        btnBack          = findViewById(R.id.btn_back);
        tvToolbarTitle   = findViewById(R.id.tv_toolbar_title);
        tvArticleCount   = findViewById(R.id.tv_article_count);
        scrollView       = findViewById(R.id.featured_scroll);
        scrollInner      = findViewById(R.id.featured_scroll_inner);
        cardHero         = findViewById(R.id.card_hero);
        imgHero          = findViewById(R.id.img_hero);
        tvHeroTitle      = findViewById(R.id.tv_hero_title);
        tvHeroCategory   = findViewById(R.id.tv_hero_category);
        tvHeroAuthor     = findViewById(R.id.tv_hero_author);
        tvHeroAvatar     = findViewById(R.id.tv_hero_avatar);
        tvHeroTime       = findViewById(R.id.tv_hero_time);
        layoutSectionList= findViewById(R.id.layout_section_list);
        tvSectionList    = findViewById(R.id.tv_section_list);
        recyclerFeatured = findViewById(R.id.recycler_featured);
        layoutEmpty      = findViewById(R.id.layout_empty);
        tvEmpty          = findViewById(R.id.tv_empty);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        recyclerFeatured.setLayoutManager(new LinearLayoutManager(this));
        recyclerFeatured.setNestedScrollingEnabled(false);
        adapter = new FeaturedAdapter(this, new ArrayList<>());
        recyclerFeatured.setAdapter(adapter);
    }

    // ── Theme ─────────────────────────────────────────────────────

    private void applyTheme() {
        int bgColor       = isDarkMode ? ThemeManager.DarkColors.BACKGROUND     : Color.parseColor("#F5F3EF");
        int cardColor     = isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int textPrimary   = isDarkMode ? ThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#2C2C2A");
        int textSecondary = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#999999");

        if (featuredRoot != null) featuredRoot.setBackgroundColor(bgColor);
        if (scrollView   != null) scrollView.setBackgroundColor(bgColor);
        if (scrollInner  != null) scrollInner.setBackgroundColor(bgColor);

        // Toolbar
        if (toolbar != null) {
            if (isDarkMode) toolbar.setBackgroundColor(ThemeManager.DarkColors.STATUS_BAR);
            else            toolbar.setBackgroundResource(R.drawable.toolbar_gradient_red);
        }
        if (btnBack != null)
            btnBack.setImageTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
        if (tvToolbarTitle != null) tvToolbarTitle.setTextColor(Color.WHITE);
        if (tvArticleCount != null) tvArticleCount.setTextColor(isDarkMode ? Color.parseColor("#AAAAAA") : Color.parseColor("#DDDDDD"));

        // Section header
        if (tvSectionList != null) tvSectionList.setTextColor(textPrimary);
        if (tvEmpty       != null) tvEmpty.setTextColor(textSecondary);

        // Status bar
        getWindow().setStatusBarColor(isDarkMode
                ? ThemeManager.DarkColors.STATUS_BAR
                : ThemeManager.LightColors.STATUS_BAR);
    }

    // ── Load data ─────────────────────────────────────────────────

    private void loadData() {
        DatabaseHelper db       = new DatabaseHelper(this);
        List<Article>  articles = db.getLatestApprovedArticles(20);

        if (articles == null || articles.isEmpty()) {
            cardHero.setVisibility(View.GONE);
            recyclerFeatured.setVisibility(View.GONE);
            layoutSectionList.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
            if (tvArticleCount != null) tvArticleCount.setText("");
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        if (tvArticleCount != null)
            tvArticleCount.setText(articles.size() + " bài viết");

        // Hero = bài đầu tiên
        Article hero = articles.get(0);
        bindHero(hero);
        cardHero.setVisibility(View.VISIBLE);
        cardHero.setOnClickListener(v -> openArticle(hero));

        // Danh sách còn lại
        List<Article> rest = articles.size() > 1
                ? new ArrayList<>(articles.subList(1, articles.size()))
                : new ArrayList<>();

        if (rest.isEmpty()) {
            recyclerFeatured.setVisibility(View.GONE);
            layoutSectionList.setVisibility(View.GONE);
        } else {
            recyclerFeatured.setVisibility(View.VISIBLE);
            layoutSectionList.setVisibility(View.VISIBLE);
            adapter.updateList(rest);
        }
    }

    private void bindHero(Article article) {
        if (tvHeroTitle  != null)
            tvHeroTitle.setText(article.getTitle() != null ? article.getTitle() : "");

        String author = article.getAuthor() != null ? article.getAuthor() : "Ẩn danh";
        if (tvHeroAuthor != null) tvHeroAuthor.setText(author);
        if (tvHeroAvatar != null) tvHeroAvatar.setText(getInitials(author));
        if (tvHeroTime   != null) tvHeroTime.setText(getRelativeTime(article.getApprovedAt()));

        String category = article.getCategory();
        if (tvHeroCategory != null) {
            if (category != null && !category.isEmpty()) {
                tvHeroCategory.setText(category.toUpperCase());
                tvHeroCategory.setVisibility(View.VISIBLE);
            } else {
                tvHeroCategory.setVisibility(View.GONE);
            }
        }

        if (imgHero != null && article.getImagePath() != null) {
            File f = new File(article.getImagePath());
            if (f.exists()) {
                Glide.with(this)
                        .load(f)
                        .apply(new RequestOptions()
                                .transforms(new CenterCrop())
                                .placeholder(R.drawable.ic_article_placeholder))
                        .into(imgHero);
            }
        }
    }

    private void openArticle(Article article) {
        Intent intent = new Intent(this, ArticleDetailActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String getRelativeTime(long ts) {
        if (ts <= 0) return "";
        long diff = System.currentTimeMillis() - ts;
        long m = TimeUnit.MILLISECONDS.toMinutes(diff);
        long h = TimeUnit.MILLISECONDS.toHours(diff);
        long d = TimeUnit.MILLISECONDS.toDays(diff);
        if (diff < TimeUnit.MINUTES.toMillis(1)) return "Vừa xong";
        if (m < 60) return m + " phút trước";
        if (h < 24) return h + " giờ trước";
        if (d < 7)  return d + " ngày trước";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(ts));
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] p = name.trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return (p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }
}