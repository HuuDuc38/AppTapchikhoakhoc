package com.example.apptapchikhoakhoc.admin;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.ArrayList;
import java.util.List;

// ✅ Extend BaseAdminActivity → locale admin tự động được apply
public class ApproveArticleActivity extends BaseAdminActivity
        implements ApproveArticleAdapter.OnActionListener {

    private RecyclerView          recyclerView;
    private LinearLayout          layoutEmpty;
    private TextView              tvPendingCount, tvBadge;
    private TextView              tabAll, tabNew, tabOld;
    private ImageView             btnBack;
    private DatabaseHelper        db;
    private ApproveArticleAdapter adapter;

    private CoordinatorLayout rootApproveLayout;
    private LinearLayout      toolbarLayout;
    private TextView          tvEmptyTitle, tvEmptyDesc;

    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_article);

        db = new DatabaseHelper(this);

        rootApproveLayout = findViewById(R.id.rootApproveLayout);
        toolbarLayout     = findViewById(R.id.toolbarLayout);
        tvEmptyTitle      = findViewById(R.id.tvEmptyTitle);
        tvEmptyDesc       = findViewById(R.id.tvEmptyDesc);
        btnBack           = findViewById(R.id.btn_back);
        recyclerView      = findViewById(R.id.recyclerApprove);
        layoutEmpty       = findViewById(R.id.layoutEmpty);
        tvPendingCount    = findViewById(R.id.tvPendingCount);
        tvBadge           = findViewById(R.id.tvBadge);
        tabAll            = findViewById(R.id.tabAll);
        tabNew            = findViewById(R.id.tabNew);
        tabOld            = findViewById(R.id.tabOld);

        applySystemBarsTheme();
        applyDarkModeTheme();

        btnBack.setOnClickListener(v -> finish());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setupFilterTabs();
        loadByTab(0);
    }

    @Override
    protected void onResume() {
        super.onResume(); // BaseAdminActivity detect locale/theme change → recreate()
        applySystemBarsTheme();
        applyDarkModeTheme();
        loadByTab(currentTab);
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME
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

    private void applyDarkModeTheme() {
        if (rootApproveLayout != null)
            rootApproveLayout.setBackgroundColor(AdminThemeManager.getBackgroundColor(this));

        if (toolbarLayout != null) {
            if (isDarkMode) toolbarLayout.setBackgroundColor(AdminThemeManager.DarkColors.STATUS_BAR);
            else            toolbarLayout.setBackgroundResource(R.drawable.toolbar_gradient_red);
        }

        if (tvEmptyTitle != null) tvEmptyTitle.setTextColor(AdminThemeManager.getTextPrimaryColor(this));
        if (tvEmptyDesc  != null) tvEmptyDesc.setTextColor(AdminThemeManager.getTextSecondaryColor(this));
    }

    // ══════════════════════════════════════════════════════════════
    //  FILTER TABS
    // ══════════════════════════════════════════════════════════════
    private void setupFilterTabs() {
        tabAll.setOnClickListener(v -> { setTabSelected(tabAll); currentTab = 0; loadByTab(0); });
        tabNew.setOnClickListener(v -> { setTabSelected(tabNew); currentTab = 1; loadByTab(1); });
        tabOld.setOnClickListener(v -> { setTabSelected(tabOld); currentTab = 2; loadByTab(2); });
        setTabSelected(tabAll);
    }

    private void setTabSelected(TextView selected) {
        for (TextView tab : new TextView[]{tabAll, tabNew, tabOld}) {
            tab.setBackgroundResource(R.drawable.tab_unselected_bg);
            tab.setTextColor(Color.parseColor("#AAFFFFFF"));
            tab.setTypeface(null, Typeface.NORMAL);
        }
        selected.setBackgroundResource(R.drawable.tab_selected_bg);
        selected.setTextColor(Color.WHITE);
        selected.setTypeface(null, Typeface.BOLD);
    }

    // ══════════════════════════════════════════════════════════════
    //  LOAD DATA
    // ══════════════════════════════════════════════════════════════
    private void loadByTab(int tab) {
        List<Article> list;
        switch (tab) {
            case 1:  list = db.getApprovedArticles(); break;
            case 2:  list = db.getRejectedArticles(); break;
            default: list = db.getPendingArticles();  break;
        }
        if (list == null) list = new ArrayList<>();

        int pendingCount = db.getPendingCount();
        // ✅ getString() cho pending count
        tvPendingCount.setText(getString(R.string.approve_pending_count, pendingCount));
        tvBadge.setVisibility(pendingCount == 0 ? View.GONE : View.VISIBLE);
        if (pendingCount > 0)
            tvBadge.setText(pendingCount >= 100 ? "99+" : String.valueOf(pendingCount));

        if (list.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (adapter != null) adapter.updateList(new ArrayList<>(), tab);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        if (adapter == null) {
            adapter = new ApproveArticleAdapter(list, tab, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(list, tab);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  OnActionListener — ✅ dùng getString() thay vì hardcode
    // ══════════════════════════════════════════════════════════════
    @Override
    public void onApprove(Article article, int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.approve_dialog_approve_title))
                .setMessage(getString(R.string.approve_dialog_approve_message, article.getTitle()))
                .setPositiveButton(getString(R.string.approve_dialog_approve_btn), (dialog, which) -> {
                    article.setApprovedAt(System.currentTimeMillis());
                    db.approveArticle(article.getId());
                    loadByTab(currentTab);
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (d, w) -> {
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onReject(Article article, int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.approve_dialog_reject_title))
                .setMessage(getString(R.string.approve_dialog_reject_message, article.getTitle()))
                .setPositiveButton(getString(R.string.approve_dialog_reject_btn), (dialog, which) -> {
                    article.setApprovedAt(System.currentTimeMillis());
                    db.rejectArticle(article.getId());
                    loadByTab(currentTab);
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (d, w) -> {
                    if (adapter != null) adapter.notifyDataSetChanged();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDelete(Article article, int position) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.approve_dialog_delete_title))
                .setMessage(getString(R.string.approve_dialog_delete_message, article.getTitle()))
                .setPositiveButton(getString(R.string.dialog_delete), (dialog, which) -> {
                    db.deleteArticle(article.getId());
                    if (adapter != null) adapter.removeItem(position);
                    int pendingCount = db.getPendingCount();
                    tvPendingCount.setText(getString(R.string.approve_pending_count, pendingCount));
                    tvBadge.setVisibility(pendingCount == 0 ? View.GONE : View.VISIBLE);
                    if (pendingCount > 0)
                        tvBadge.setText(pendingCount >= 100 ? "99+" : String.valueOf(pendingCount));
                })
                .setNegativeButton(getString(R.string.dialog_cancel), (d, w) -> {
                    if (adapter != null) adapter.notifyItemChanged(position);
                })
                .setCancelable(false)
                .show();
    }
}