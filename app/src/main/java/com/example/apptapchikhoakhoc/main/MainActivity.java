package com.example.apptapchikhoakhoc.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.adapter.FeaturedBannerAdapter;
import com.example.apptapchikhoakhoc.adapter.NewsAdapter;
import com.example.apptapchikhoakhoc.admin.AddEditArticleActivity;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.search.SearchActivity;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity — kế thừa BaseActivity.
 *
 * Font scale & dark mode được BaseActivity xử lý tự động:
 *  • attachBaseContext() → áp font scale trước inflate
 *  • onResume()         → detect font scale thay đổi → recreate()
 *                       → detect theme thay đổi     → refreshThemeState()
 *
 * Bottom Navigation:
 *  • nav_home     → Trang chủ  : tất cả bài đã duyệt
 *  • nav_featured → Nổi bật    : top bài theo likes/comments/shares
 *  • nav_add      → Đăng bài   : nộp bài mới (yêu cầu đăng nhập)
 *  • nav_follow   → Theo dõi   : thông báo cá nhân
 *  • nav_profile  → Cá nhân    : cài đặt / hồ sơ
 */
public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout         drawerLayout;
    private NavigationView       navigationView;
    private ImageView            btnMenu, btnSearch, btnHomeIcon;
    private RecyclerView         recyclerNews;
    private NewsAdapter          newsAdapter;
    private LinearLayout         header, tabBar;
    private LinearLayout         fabDangBaiWrapper;
    private FloatingActionButton fabDangBai;
    private View                 scrollContent;

    private ViewPager2            viewPagerFeatured;
    private DotsIndicator         dotsIndicator;
    private FeaturedBannerAdapter bannerAdapter;
    private final Handler         autoSlideHandler = new Handler(Looper.getMainLooper());
    private Runnable              autoSlideRunnable;

    private SharedPreferences userPrefs;

    private boolean isFirstLoad     = true;
    private String  currentCategory = "ALL";

    // isDark snapshot — dùng để detect thay đổi theme trong onResume
    private boolean lastKnownDarkMode = false;

    private TextView             tabTinTuc, tabDaoTao, tabViecLam, tabKhoaHoc, tabHopTac;
    private BottomNavigationView bottomNavigationView;

    private final ActivityResultLauncher<Intent> loginForAddLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    openAddArticle();
                } else {
                    bottomNavigationView.post(() ->
                            bottomNavigationView.setSelectedItemId(R.id.nav_home));
                }
            });

    // ══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lastKnownDarkMode = isDarkMode;
        userPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);

        initializeViews();
        applyActivityTheme();
        initViews();
        setupDrawer();
        setupListeners();
        loadAllData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Detect dark mode thay đổi
        if (isDarkMode != lastKnownDarkMode) {
            lastKnownDarkMode = isDarkMode;
            recreate();
            return;
        }

        // Reload data bình thường
        if (!isFirstLoad) {
            if ("ALL".equals(currentCategory)) loadAllData();
            else filterArticlesByCategory();
        }
        isFirstLoad = false;

        startAutoSlide();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoSlide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoSlide();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  INIT
    // ══════════════════════════════════════════════════════════════

    private void initializeViews() {
        drawerLayout         = findViewById(R.id.drawer_layout);
        navigationView       = findViewById(R.id.nav_view);
        btnMenu              = findViewById(R.id.btn_menu);
        btnSearch            = findViewById(R.id.btn_search);
        btnHomeIcon          = findViewById(R.id.btn_home_icon);
        recyclerNews         = findViewById(R.id.recyclerNews);
        viewPagerFeatured    = findViewById(R.id.viewPagerFeatured);
        dotsIndicator        = findViewById(R.id.dotsIndicator);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        header               = findViewById(R.id.header);
        tabBar               = findViewById(R.id.tab_bar);
        scrollContent        = findViewById(R.id.scroll_content);
        tabTinTuc            = findViewById(R.id.tab_tin_tuc);
        tabDaoTao            = findViewById(R.id.tab_dao_tao);
        tabViecLam           = findViewById(R.id.tab_viec_lam);
        tabKhoaHoc           = findViewById(R.id.tab_khoa_hoc);
        tabHopTac            = findViewById(R.id.tab_hop_tac);
        fabDangBaiWrapper    = findViewById(R.id.fab_dang_bai_wrapper);
        fabDangBai           = findViewById(R.id.fab_dang_bai);
    }

    private void initViews() {
        recyclerNews.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter(this);
        recyclerNews.setAdapter(newsAdapter);
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        updateDrawerHeader();
        applyNavigationViewTheme();
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME
    // ══════════════════════════════════════════════════════════════

    private void applyActivityTheme() {
        int bgColor = getBackgroundColor();

        if (scrollContent != null) scrollContent.setBackgroundColor(bgColor);

        if (isDarkMode) {
            if (header               != null) header.setBackgroundColor(ThemeManager.DarkColors.BACKGROUND);
            if (tabBar               != null) tabBar.setBackgroundColor(ThemeManager.DarkColors.CARD_BACKGROUND);
            if (bottomNavigationView != null) bottomNavigationView.setBackgroundColor(ThemeManager.DarkColors.BACKGROUND);
        }

        int tabTextColor = Color.WHITE;
        if (tabTinTuc != null) tabTinTuc.setTextColor(tabTextColor);
        if (tabDaoTao  != null) tabDaoTao.setTextColor(tabTextColor);
        if (tabViecLam != null) tabViecLam.setTextColor(tabTextColor);
        if (tabKhoaHoc != null) tabKhoaHoc.setTextColor(tabTextColor);
        if (tabHopTac  != null) tabHopTac.setTextColor(tabTextColor);

        updateDrawerHeader();
        applyNavigationViewTheme();

        if (newsAdapter   != null) newsAdapter.notifyDataSetChanged();
        if (bannerAdapter != null) bannerAdapter.refreshTheme();
    }

    private void applyNavigationViewTheme() {
        if (navigationView == null) return;

        if (isDarkMode) {
            navigationView.setBackgroundColor(ThemeManager.DarkColors.BACKGROUND);
            navigationView.setItemTextColor(new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ ThemeManager.DarkColors.ACCENT, ThemeManager.DarkColors.TEXT_PRIMARY }
            ));
            navigationView.setItemIconTintList(new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ ThemeManager.DarkColors.ACCENT, ThemeManager.DarkColors.TEXT_SECONDARY }
            ));
            navigationView.setItemBackground(new ColorDrawable(ThemeManager.DarkColors.CARD_BACKGROUND));
        } else {
            navigationView.setBackgroundColor(Color.WHITE);
            navigationView.setItemTextColor(new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ Color.parseColor("#FF5722"), Color.parseColor("#333333") }
            ));
            navigationView.setItemIconTintList(new ColorStateList(
                    new int[][]{ new int[]{android.R.attr.state_checked}, new int[]{} },
                    new int[]{ Color.parseColor("#FF5722"), Color.parseColor("#666666") }
            ));
            navigationView.setItemBackground(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void updateDrawerHeader() {
        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        LinearLayout navHeaderContainer = headerView.findViewById(R.id.nav_header_container);
        if (navHeaderContainer != null) {
            if (isDarkMode) navHeaderContainer.setBackgroundColor(ThemeManager.DarkColors.STATUS_BAR);
            else            navHeaderContainer.setBackgroundResource(R.drawable.toolbar_gradient_red);
        }

        androidx.cardview.widget.CardView logoCard  = headerView.findViewById(R.id.navHeaderLogoCard);
        LinearLayout                       logoInner = headerView.findViewById(R.id.navHeaderLogoInner);
        TextView                           logoText  = headerView.findViewById(R.id.navHeaderLogoText);

        if (logoCard  != null) logoCard.setCardBackgroundColor(isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE);
        if (logoInner != null) logoInner.setBackgroundColor(isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE);
        if (logoText  != null) logoText.setTextColor(isDarkMode ? ThemeManager.DarkColors.ACCENT : Color.parseColor("#FF5722"));

        TextView tvTitle    = headerView.findViewById(R.id.navHeaderTitle);
        TextView tvSubtitle = headerView.findViewById(R.id.navHeaderSubtitle);

        if (tvTitle    != null) tvTitle.setTextColor(Color.WHITE);
        if (tvSubtitle != null) {
            tvSubtitle.setTextColor(isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.WHITE);
            tvSubtitle.setAlpha(isDarkMode ? 0.75f : 0.9f);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LISTENERS
    // ══════════════════════════════════════════════════════════════

    private void setupListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));

        btnHomeIcon.setOnClickListener(v -> {
            currentCategory = "ALL";
            resetAllTabs();
            loadAllData();
            navigationView.setCheckedItem(R.id.nav_home);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        });

        tabTinTuc.setOnClickListener(v -> selectTab(tabTinTuc, "Tin tức sự kiện"));
        tabDaoTao.setOnClickListener(v -> selectTab(tabDaoTao, "Đào tạo"));
        tabViecLam.setOnClickListener(v -> selectTab(tabViecLam, "Thông tin việc làm"));
        tabKhoaHoc.setOnClickListener(v -> selectTab(tabKhoaHoc, "Khoa học công nghệ"));
        tabHopTac.setOnClickListener(v -> selectTab(tabHopTac, "Hợp tác quốc tế"));

        // FAB Đăng bài
        if (fabDangBaiWrapper != null) {
            fabDangBaiWrapper.setOnClickListener(v -> handleAddArticle());
        }
        if (fabDangBai != null) {
            fabDangBai.setOnClickListener(v -> handleAddArticle());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                currentCategory = "ALL";
                resetAllTabs();
                loadAllData();
                navigationView.setCheckedItem(R.id.nav_home);
                return true;

            } else if (itemId == R.id.nav_featured) {
                startActivity(new Intent(this, FeaturedActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                // Reset lại về Home sau khi mở
                bottomNavigationView.post(() ->
                        bottomNavigationView.setSelectedItemId(R.id.nav_home));
                return true;

            } else if (itemId == R.id.nav_add) {
                handleAddArticle();
                return true;

            } else if (itemId == R.id.nav_follow) {
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;

            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }

            return false;
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  THÊM BÀI
    // ══════════════════════════════════════════════════════════════

    private void handleAddArticle() {
        if (isUserLoggedIn()) {
            openAddArticle();
        } else {
            Toast.makeText(this, "Bạn cần đăng nhập để đăng bài viết", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra("from_add_article", true);
            loginForAddLauncher.launch(loginIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    private boolean isUserLoggedIn() {
        return userPrefs.getBoolean("isLoggedIn", false);
    }

    private void openAddArticle() {
        Intent intent = new Intent(this, AddEditArticleActivity.class);
        intent.putExtra("pending_mode", true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        // Reset về Home sau khi mở màn đăng bài
        bottomNavigationView.post(() -> bottomNavigationView.setSelectedItemId(R.id.nav_home));
    }

    // ══════════════════════════════════════════════════════════════
    //  TABS
    // ══════════════════════════════════════════════════════════════

    private void selectTab(TextView selectedTab, String category) {
        currentCategory = category;
        updateTabSelection(selectedTab);
        filterArticlesByCategory();
    }

    private void resetAllTabs() {
        if (tabTinTuc != null) tabTinTuc.setAlpha(0.7f);
        if (tabDaoTao  != null) tabDaoTao.setAlpha(0.7f);
        if (tabViecLam != null) tabViecLam.setAlpha(0.7f);
        if (tabKhoaHoc != null) tabKhoaHoc.setAlpha(0.7f);
        if (tabHopTac  != null) tabHopTac.setAlpha(0.7f);
    }

    private void updateTabSelection(TextView selectedTab) {
        resetAllTabs();
        if (selectedTab != null) selectedTab.setAlpha(1.0f);
    }

    // ══════════════════════════════════════════════════════════════
    //  LOAD DATA
    // ══════════════════════════════════════════════════════════════

    public void loadAllData() {
        currentCategory = "ALL";
        updateUI(new DatabaseHelper(this).getAllArticles());
    }

    private void loadFeaturedArticles() {
        updateUI(new DatabaseHelper(this).getFeaturedArticles());
    }

    private void filterArticlesByCategory() {
        updateUI(new DatabaseHelper(this).getArticlesByCategory(currentCategory));
    }

    // ══════════════════════════════════════════════════════════════
    //  UI
    // ══════════════════════════════════════════════════════════════

    public void updateUI(List<Article> articles) {
        if (articles == null || articles.isEmpty()) {
            hideBannerAndShowEmpty();
            return;
        }
        List<Article> bannerList = articles.size() > 4 ? articles.subList(0, 4) : new ArrayList<>(articles);
        List<Article> newsList   = articles.size() > 4 ? articles.subList(4, articles.size()) : new ArrayList<>();
        setupBanner(bannerList);
        newsAdapter.setList(newsList);
        showBanner();
    }

    private void hideBannerAndShowEmpty() {
        viewPagerFeatured.setVisibility(View.GONE);
        dotsIndicator.setVisibility(View.GONE);
        newsAdapter.setList(new ArrayList<>());
    }

    private void showBanner() {
        viewPagerFeatured.setVisibility(View.VISIBLE);
        dotsIndicator.setVisibility(View.VISIBLE);
    }

    private void setupBanner(List<Article> bannerList) {
        if (bannerList == null || bannerList.isEmpty()) { hideBannerAndShowEmpty(); return; }
        stopAutoSlide();
        try {
            if (bannerAdapter == null) {
                bannerAdapter = new FeaturedBannerAdapter(this, bannerList);
                viewPagerFeatured.setAdapter(bannerAdapter);
            } else {
                bannerAdapter.updateArticles(bannerList);
            }
            dotsIndicator.setViewPager2(viewPagerFeatured);
            bannerAdapter.setOnItemClickListener(article -> {
                Intent intent = new Intent(this, ArticleDetailActivity.class);
                intent.putExtra("article", article);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
            autoSlideRunnable = () -> {
                if (bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
                    int next = (viewPagerFeatured.getCurrentItem() + 1) % bannerAdapter.getItemCount();
                    viewPagerFeatured.setCurrentItem(next, true);
                    autoSlideHandler.postDelayed(autoSlideRunnable, 15000);
                }
            };
            viewPagerFeatured.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override public void onPageSelected(int position) { stopAutoSlide(); startAutoSlide(); }
            });
            if (bannerList.size() > 1) startAutoSlide();
        } catch (Exception e) {
            Log.e(TAG, "CRASH in setupBanner!", e);
        }
    }

    private void startAutoSlide() {
        if (autoSlideRunnable != null && bannerAdapter != null && bannerAdapter.getItemCount() > 1) {
            autoSlideHandler.removeCallbacks(autoSlideRunnable);
            autoSlideHandler.postDelayed(autoSlideRunnable, 15000);
        }
    }

    private void stopAutoSlide() {
        if (autoSlideRunnable != null) autoSlideHandler.removeCallbacks(autoSlideRunnable);
    }

    // ══════════════════════════════════════════════════════════════
    //  DRAWER NAVIGATION
    // ══════════════════════════════════════════════════════════════

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        drawerLayout.closeDrawer(GravityCompat.START);
        if      (id == R.id.nav_home)        { currentCategory = "ALL"; resetAllTabs(); loadAllData(); }
        else if (id == R.id.nav_news)        loadCategoryAndHighlight("Tin tức sự kiện");
        else if (id == R.id.nav_policy)      loadCategoryAndHighlight("Đào tạo");
        else if (id == R.id.nav_attack)      loadCategoryAndHighlight("Thông tin việc làm");
        else if (id == R.id.nav_certificate) loadCategoryAndHighlight("Khoa học công nghệ");
        else if (id == R.id.nav_crypto)      loadCategoryAndHighlight("Hợp tác quốc tế");
        return true;
    }

    private void loadCategoryAndHighlight(String category) {
        currentCategory = category;
        resetAllTabs();
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        if      (category.equals("Tin tức sự kiện"))    updateTabSelection(tabTinTuc);
        else if (category.equals("Đào tạo"))            updateTabSelection(tabDaoTao);
        else if (category.equals("Thông tin việc làm")) updateTabSelection(tabViecLam);
        else if (category.equals("Khoa học công nghệ")) updateTabSelection(tabKhoaHoc);
        else if (category.equals("Hợp tác quốc tế"))   updateTabSelection(tabHopTac);
        updateUI(new DatabaseHelper(this).getArticlesByCategory(category));
        Toast.makeText(this, category, Toast.LENGTH_SHORT).show();
    }
}