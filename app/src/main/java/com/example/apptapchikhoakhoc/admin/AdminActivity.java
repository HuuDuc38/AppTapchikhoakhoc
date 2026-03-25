package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.main.MainActivity;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class AdminActivity extends BaseAdminActivity {

    private RecyclerView         recyclerView;
    private AdminArticleAdapter  adapter;
    private DatabaseHelper       db;
    private ImageView            btnBack, btnSearch;
    private BottomNavigationView bottomNav;
    private CoordinatorLayout    rootAdminLayout;
    private AppBarLayout         appBarAdmin;
    private LinearLayout         toolbarAdmin;
    private FloatingActionButton fab;
    private LinearLayout         fabContainer;
    private TextView             fabLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        initViews();
        applyTheme();
        setupClickListeners();
        setupRecyclerView();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        loadArticles();
        updatePendingBadge();
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void initViews() {
        rootAdminLayout = findViewById(R.id.rootAdminLayout);
        appBarAdmin     = findViewById(R.id.appBarAdmin);
        toolbarAdmin    = findViewById(R.id.toolbarAdmin);
        btnBack         = findViewById(R.id.btn_back);
        btnSearch       = findViewById(R.id.btn_search);
        recyclerView    = findViewById(R.id.recyclerAdminArticles);
        bottomNav       = findViewById(R.id.bottomNavigationAdmin);
        fab             = findViewById(R.id.fab_dang_bai);
        fabContainer    = findViewById(R.id.fab_container);
        fabLabel        = findViewById(R.id.fab_label);
        db              = new DatabaseHelper(this);

        // Chỉ bringToFront, không dùng translationY
        bottomNav.post(() -> fabContainer.bringToFront());

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditArticleActivity.class);
            intent.putExtra("pending_mode", true);
            startActivity(intent);
        });
    }

    private void applyTheme() {
        boolean dark = AdminThemeManager.isDarkMode(this);
        applySystemBars(dark);

        int bgPage = dark ? AdminThemeManager.DarkColors.BACKGROUND : Color.parseColor("#F8F5FF");
        if (rootAdminLayout != null) rootAdminLayout.setBackgroundColor(bgPage);
        if (recyclerView    != null) recyclerView.setBackgroundColor(bgPage);
        if (appBarAdmin     != null) appBarAdmin.setBackgroundColor(Color.TRANSPARENT);

        if (toolbarAdmin != null) {
            if (dark) toolbarAdmin.setBackgroundColor(AdminThemeManager.DarkColors.STATUS_BAR);
            else      toolbarAdmin.setBackground(ContextCompat.getDrawable(this, R.drawable.toolbar_gradient_red));
        }

        if (bottomNav != null) {
            if (dark) {
                bottomNav.setBackgroundColor(AdminThemeManager.DarkColors.STATUS_BAR);
                ColorStateList navColors = new ColorStateList(
                        new int[][]{ new int[]{ android.R.attr.state_checked }, new int[]{} },
                        new int[]{ AdminThemeManager.DarkColors.ACCENT, AdminThemeManager.DarkColors.TEXT_SECONDARY });
                bottomNav.setItemIconTintList(navColors);
                bottomNav.setItemTextColor(navColors);
                if (fabLabel != null) fabLabel.setTextColor(AdminThemeManager.DarkColors.TEXT_SECONDARY);
            } else {
                bottomNav.setBackground(ContextCompat.getDrawable(this, R.drawable.toolbar_gradient_red));
                ColorStateList navColors = new ColorStateList(
                        new int[][]{ new int[]{ android.R.attr.state_checked }, new int[]{} },
                        new int[]{ Color.WHITE, Color.parseColor("#CCFFFFFF") });
                bottomNav.setItemIconTintList(navColors);
                bottomNav.setItemTextColor(navColors);
                if (fabLabel != null) fabLabel.setTextColor(Color.WHITE);
            }
        }

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private void applySystemBars(boolean dark) {
        Window window = getWindow();
        if (dark) {
            window.setStatusBarColor(AdminThemeManager.DarkColors.STATUS_BAR);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(AdminThemeManager.DarkColors.STATUS_BAR);
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

    private void setupClickListeners() {
        btnBack.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.admin_exit_title))
                        .setMessage(getString(R.string.admin_exit_message))
                        .setPositiveButton(getString(R.string.dialog_yes), (d, w) -> {
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton(getString(R.string.dialog_no), null)
                        .show());

        btnSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadArticles();
    }

    private void loadArticles() {
        List<Article> list = db.getAllArticles();
        if (adapter == null) {
            adapter = new AdminArticleAdapter(list,
                    new AdminArticleAdapter.OnItemClickListener() {
                        @Override public void onEdit(Article a)   { editArticle(a); }
                        @Override public void onDelete(Article a) { deleteArticle(a); }
                    });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(list);
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        updatePendingBadge();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_home)     { loadArticles(); return true; }
            else if (id == R.id.nav_approve)  { startActivity(new Intent(this, ApproveArticleActivity.class)); return true; }
            else if (id == R.id.nav_stats)    { startActivity(new Intent(this, StatsActivity.class)); return true; }
            else if (id == R.id.nav_settings) { startActivity(new Intent(this, SettingsActivity.class)); return true; }
            return false;
        });
    }

    private void updatePendingBadge() {
        int count = db.getTotalPendingArticles();
        BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_approve);
        if (count > 0) {
            badge.setVisible(true);
            badge.setMaxCharacterCount(3);
            badge.setNumber(count);
        } else {
            badge.setVisible(false);
            badge.clearNumber();
        }
    }

    private void editArticle(Article article) {
        Intent intent = new Intent(this, AddEditArticleActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }

    private void deleteArticle(Article article) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.admin_delete_title))
                .setMessage(getString(R.string.admin_delete_message, article.getTitle()))
                .setPositiveButton(getString(R.string.dialog_delete), (d, w) -> {
                    db.deleteArticle(article.getId());
                    loadArticles();
                    updatePendingBadge();
                })
                .setNegativeButton(getString(R.string.dialog_no), null)
                .show();
    }
}