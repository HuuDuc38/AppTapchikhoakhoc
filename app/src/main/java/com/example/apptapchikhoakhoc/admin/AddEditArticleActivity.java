package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;
import com.example.apptapchikhoakhoc.utils.AutoApproveWorker;
import com.example.apptapchikhoakhoc.utils.ContentModerationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class AddEditArticleActivity extends BaseActivity {

    // ── UI ──────────────────────────────────────────────────────────
    private MaterialToolbar    toolbar;
    private TextInputEditText  etTitle, etAuthor;
    private AutoCompleteTextView spinnerCategory;
    private RichEditor         reEditor;
    private TextView           tvPasteImageHint, tvPendingBanner;
    private TextView           tvSectionImage, tvSectionVideo, tvSectionContent;
    private TextView           tvPlaceholderImageText, tvPlaceholderVideoText;

    private ImageView        imgPreview;
    private LinearLayout     placeholderImage;
    private MaterialButton   btnRemoveImage, btnPickImage;

    private FrameLayout      videoPreviewContainer;
    private ImageView        imgVideoThumbnail, imgPlayIcon;
    private LinearLayout     placeholderVideo;
    private MaterialButton   btnRemoveVideo, btnPickVideo;

    private HorizontalScrollView editorToolbar;
    private View                 editorTopSpacer;

    private CardView cardImage, cardVideo, cardEditor;

    private TextInputLayout layoutTitle, layoutAuthor, layoutCategory;

    private LinearLayout rootContent;
    private ScrollView   rootScrollView;

    private MaterialButton            btnSave;
    private CircularProgressIndicator progressLoading;

    // ── DATA ─────────────────────────────────────────────────────────
    private DatabaseHelper db;
    private Article        currentArticle;
    private Uri            imageUri = null;
    private Uri            videoUri = null;
    private final List<String> embeddedImagePaths = new ArrayList<>();
    private boolean isPendingMode = false;
    private boolean isAdmin       = false;

    // ★ Theo dõi trạng thái dark mode để detect thay đổi khi resume
    private boolean isDark;

    private static final String[] CATEGORIES = {
            "Tin tức sự kiện", "Đào tạo", "Thông tin việc làm",
            "Khoa học công nghệ", "Hợp tác quốc tế"
    };

    // ── LAUNCHERS ────────────────────────────────────────────────────
    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    updateImagePreview();
                }
            });

    private final ActivityResultLauncher<Intent> pickVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    videoUri = result.getData().getData();
                    updateVideoPreview();
                }
            });

    private final ActivityResultLauncher<Intent> insertImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null
                        && result.getData().getData() != null) {
                    insertImageIntoRichEditor(result.getData().getData());
                }
            });

    // ── LIFECYCLE ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_article);

        // ★ Đọc trạng thái dark mode từ AdminThemeManager
        isDark = AdminThemeManager.isDarkMode(this);

        db = new DatabaseHelper(this);
        checkAdminStatus();
        readIntentExtras();
        initViews();
        applySystemBarsTheme();   // ★ Đổi màu status bar + nav bar
        applyActivityTheme();     // ★ Áp dụng theme toàn màn hình
        setupToolbar();
        setupCategory();
        setupRichEditor();
        setupClickListeners();
        updatePendingBanner();

        if (currentArticle != null) {
            fillForm();
            toolbar.setTitle("Sửa bài viết");
            btnSave.setText("CẬP NHẬT");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ★ Nếu dark mode thay đổi từ SettingsActivity → recreate lại màn hình
        boolean newDark = AdminThemeManager.isDarkMode(this);
        if (newDark != isDark) {
            isDark = newDark;
            recreate();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SYSTEM BARS (Status bar + Navigation bar)
    // ══════════════════════════════════════════════════════════════

    /**
     * Đổi màu Status Bar và Navigation Bar theo dark/light mode
     * dựa trên AdminThemeManager — nhất quán với SettingsActivity.
     */
    private void applySystemBarsTheme() {
        Window window = getWindow();

        if (isDark) {
            // ── DARK: status bar + nav bar đều tối ─────────────────
            int darkBg = AdminThemeManager.DarkColors.BACKGROUND; // #1A2332

            window.setStatusBarColor(darkBg);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(darkBg);
            }

            // Icon status bar: màu sáng (icon trắng trên nền tối)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0);
            }

            // Icon nav bar: màu sáng trên nền tối
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = window.getDecorView().getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }

        } else {
            // ── LIGHT: status bar đỏ gradient, nav bar trắng ───────
            window.setStatusBarColor(Color.parseColor("#C8463D"));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.setNavigationBarColor(Color.WHITE);
            }

            // Icon status bar: icon trắng trên nền đỏ
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0);
            }

            // Icon nav bar: icon đen trên nền trắng
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int flags = window.getDecorView().getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                window.getDecorView().setSystemUiVisibility(flags);
            }
        }
    }

    // ── INIT ─────────────────────────────────────────────────────────

    private void checkAdminStatus() {
        isAdmin = getSharedPreferences("AdminSession", MODE_PRIVATE)
                .getBoolean("isAdminLoggedIn", false);
    }

    private void readIntentExtras() {
        isPendingMode  = getIntent().getBooleanExtra("pending_mode", false);
        currentArticle = (Article) getIntent().getSerializableExtra("article");
    }

    private void initViews() {
        rootContent    = findViewById(R.id.rootContent);
        rootScrollView = findViewById(R.id.rootScrollView);
        toolbar        = findViewById(R.id.toolbar);
        tvPendingBanner  = findViewById(R.id.tvPendingBanner);
        tvPasteImageHint = findViewById(R.id.tvPasteImageHint);
        etTitle         = findViewById(R.id.etTitle);
        etAuthor        = findViewById(R.id.etAuthor);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        layoutTitle     = findViewById(R.id.layoutTitle);
        layoutAuthor    = findViewById(R.id.layoutAuthor);
        layoutCategory  = findViewById(R.id.layoutCategory);
        tvSectionImage   = findViewById(R.id.tvSectionImage);
        tvSectionVideo   = findViewById(R.id.tvSectionVideo);
        tvSectionContent = findViewById(R.id.tvSectionContent);
        imgPreview             = findViewById(R.id.imgPreview);
        placeholderImage       = findViewById(R.id.placeholderImage);
        tvPlaceholderImageText = findViewById(R.id.tvPlaceholderImageText);
        btnPickImage           = findViewById(R.id.btnPickImage);
        btnRemoveImage         = findViewById(R.id.btnRemoveImage);
        cardImage              = findViewById(R.id.cardImage);
        videoPreviewContainer  = findViewById(R.id.videoPreviewContainer);
        imgVideoThumbnail      = findViewById(R.id.imgVideoThumbnail);
        imgPlayIcon            = findViewById(R.id.imgPlayIcon);
        placeholderVideo       = findViewById(R.id.placeholderVideo);
        tvPlaceholderVideoText = findViewById(R.id.tvPlaceholderVideoText);
        btnPickVideo           = findViewById(R.id.btnPickVideo);
        btnRemoveVideo         = findViewById(R.id.btnRemoveVideo);
        cardVideo              = findViewById(R.id.cardVideo);
        reEditor        = findViewById(R.id.reEditor);
        editorToolbar   = findViewById(R.id.editorToolbar);
        editorTopSpacer = findViewById(R.id.editorTopSpacer);
        cardEditor      = findViewById(R.id.cardEditor);
        btnSave         = findViewById(R.id.btnSave);
        progressLoading = findViewById(R.id.progressLoading);
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME — Dùng AdminThemeManager thay vì ThemeManager
    // ══════════════════════════════════════════════════════════════

    private void applyActivityTheme() {
        // ── Lấy màu từ AdminThemeManager ──────────────────────────
        int bgColor       = AdminThemeManager.getBackgroundColor(this);
        int cardColor     = AdminThemeManager.getCardBackgroundColor(this);
        int textPrimary   = AdminThemeManager.getTextPrimaryColor(this);
        int textSecondary = AdminThemeManager.getTextSecondaryColor(this);
        int dividerColor  = AdminThemeManager.getDividerColor(this);
        int accentColor   = AdminThemeManager.getAccentColor(this);

        // ── Page background ────────────────────────────────────────
        if (rootContent    != null) rootContent.setBackgroundColor(bgColor);
        if (rootScrollView != null) rootScrollView.setBackgroundColor(bgColor);

        // ── Toolbar ────────────────────────────────────────────────
        if (toolbar != null) {
            if (isDark) {
                toolbar.setBackgroundColor(AdminThemeManager.DarkColors.STATUS_BAR); // #141D29
            } else {
                toolbar.setBackground(androidx.core.content.ContextCompat.getDrawable(
                        this, R.drawable.toolbar_gradient_red));
            }
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setNavigationIconTint(Color.WHITE);
        }

        // ── Section labels ─────────────────────────────────────────
        setTextViewColor(tvSectionImage,   textPrimary);
        setTextViewColor(tvSectionVideo,   textPrimary);
        setTextViewColor(tvSectionContent, textPrimary);

        // ── Paste hint ─────────────────────────────────────────────
        if (tvPasteImageHint != null) {
            tvPasteImageHint.setTextColor(isDark
                    ? Color.parseColor("#BB86FC")  // tím sáng trên nền tối
                    : Color.parseColor("#7B2CBF")); // tím đậm trên nền sáng
        }

        // ── Input layouts ──────────────────────────────────────────
        applyInputLayoutTheme(layoutTitle,    cardColor, textPrimary, textSecondary);
        applyInputLayoutTheme(layoutAuthor,   cardColor, textPrimary, textSecondary);
        applyInputLayoutTheme(layoutCategory, cardColor, textPrimary, textSecondary);
        applyEditTextTheme(etTitle,         textPrimary, textSecondary);
        applyEditTextTheme(etAuthor,        textPrimary, textSecondary);
        applyEditTextTheme(spinnerCategory, textPrimary, textSecondary);

        // ── Cards ──────────────────────────────────────────────────
        applyCardTheme(cardImage,  cardColor);
        applyCardTheme(cardVideo,  cardColor);
        applyCardTheme(cardEditor, cardColor);

        // ── Placeholders ───────────────────────────────────────────
        applyPlaceholderTheme(placeholderImage, tvPlaceholderImageText, textSecondary);
        applyPlaceholderTheme(placeholderVideo, tvPlaceholderVideoText, textSecondary);

        // ── Editor toolbar ─────────────────────────────────────────
        if (editorToolbar != null) {
            editorToolbar.setBackgroundColor(cardColor);
            tintAllImageButtons(editorToolbar, textPrimary);
        }
        tintToolbarDividers(editorToolbar, dividerColor);
        if (editorTopSpacer != null) editorTopSpacer.setBackgroundColor(cardColor);

        // ── Rich editor ────────────────────────────────────────────
        if (reEditor != null) {
            reEditor.setEditorBackgroundColor(cardColor);
            reEditor.setEditorFontColor(textPrimary);
        }

        // ── Buttons ────────────────────────────────────────────────
        if (btnSave != null) {
            // Nút Save: màu accent của AdminThemeManager
            btnSave.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(accentColor));
            btnSave.setTextColor(Color.WHITE);
        }

        if (btnPickImage != null) {
            btnPickImage.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(accentColor));
            btnPickImage.setTextColor(Color.WHITE);
        }

        if (btnPickVideo != null) {
            // Nút outlined: tint stroke + text theo accent
            btnPickVideo.setStrokeColor(
                    android.content.res.ColorStateList.valueOf(accentColor));
            btnPickVideo.setTextColor(accentColor);
            // icon tint
            if (btnPickVideo.getIcon() != null)
                btnPickVideo.getIcon().setTint(accentColor);
        }

        // ── Pending banner ─────────────────────────────────────────
        applyPendingBannerTheme();
    }

    // ── HELPERS theme ──────────────────────────────────────────────

    private void applyInputLayoutTheme(TextInputLayout layout, int cardColor,
                                       int textPrimary, int textSecondary) {
        if (layout == null) return;
        layout.setBoxBackgroundColor(cardColor);
        layout.setDefaultHintTextColor(
                android.content.res.ColorStateList.valueOf(textSecondary));
        if (isDark) {
            layout.setBoxStrokeColor(AdminThemeManager.DarkColors.DIVIDER);
            layout.setHintTextColor(
                    android.content.res.ColorStateList.valueOf(
                            AdminThemeManager.DarkColors.TEXT_SECONDARY));
        } else {
            // Giữ màu tím accent cho light mode
            layout.setBoxStrokeColor(Color.parseColor("#9D4EDD"));
            layout.setHintTextColor(
                    android.content.res.ColorStateList.valueOf(
                            Color.parseColor("#9D4EDD")));
        }
    }

    private void applyEditTextTheme(TextView et, int textPrimary, int textSecondary) {
        if (et == null) return;
        et.setTextColor(textPrimary);
        et.setHintTextColor(textSecondary);
    }

    private void applyCardTheme(CardView card, int cardColor) {
        if (card == null) return;
        card.setCardBackgroundColor(cardColor);
    }

    private void applyPlaceholderTheme(LinearLayout placeholder, TextView label,
                                       int textSecondary) {
        if (placeholder == null) return;
        int placeholderBg = isDark
                ? AdminThemeManager.DarkColors.BACKGROUND
                : Color.parseColor("#F5F5F5");
        placeholder.setBackgroundColor(placeholderBg);
        if (label != null) label.setTextColor(textSecondary);

        int iconColor = isDark
                ? Color.parseColor("#4A5568")
                : Color.parseColor("#BBBBBB");
        for (int i = 0; i < placeholder.getChildCount(); i++) {
            View child = placeholder.getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void applyPendingBannerTheme() {
        if (tvPendingBanner == null || tvPendingBanner.getVisibility() != View.VISIBLE) return;
        tvPendingBanner.setBackgroundColor(isDark
                ? Color.parseColor("#332600")
                : Color.parseColor("#FFF3E0"));
        tvPendingBanner.setTextColor(isDark
                ? Color.parseColor("#FFB74D")
                : Color.parseColor("#E65100"));
    }

    private void tintAllImageButtons(ViewGroup parent, int color) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ImageButton)
                ((ImageButton) child).setColorFilter(color, PorterDuff.Mode.SRC_IN);
            else if (child instanceof ViewGroup)
                tintAllImageButtons((ViewGroup) child, color);
        }
    }

    private void tintToolbarDividers(ViewGroup parent, int color) {
        if (parent == null) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getClass().equals(View.class)) child.setBackgroundColor(color);
            else if (child instanceof ViewGroup) tintToolbarDividers((ViewGroup) child, color);
        }
    }

    private void setTextViewColor(TextView tv, int color) {
        if (tv != null) tv.setTextColor(color);
    }

    // ── PENDING BANNER ─────────────────────────────────────────────

    private void updatePendingBanner() {
        if (tvPendingBanner == null) return;
        if (isPendingMode && currentArticle == null) {
            tvPendingBanner.setVisibility(View.VISIBLE);
            tvPendingBanner.setText("⏳ Bài viết sẽ được gửi chờ duyệt trước khi đăng");
            btnSave.setText("GỬI CHỜ DUYỆT");
            applyPendingBannerTheme();
        } else {
            tvPendingBanner.setVisibility(View.GONE);
        }
        if (tvPasteImageHint != null) {
            tvPasteImageHint.setVisibility(View.VISIBLE);
            tvPasteImageHint.setText(isAdmin
                    ? "💡 Admin: Dùng nút 📷 để chèn ảnh vào nội dung"
                    : "💡 Tip: Dùng nút 📷 trên thanh công cụ để chèn ảnh");
        }
    }

    private void resetBanner() {
        if (tvPendingBanner == null) return;
        if (isPendingMode && currentArticle == null) {
            tvPendingBanner.setVisibility(View.VISIBLE);
            tvPendingBanner.setText("⏳ Bài viết sẽ được gửi chờ duyệt trước khi đăng");
            applyPendingBannerTheme();
        } else {
            tvPendingBanner.setVisibility(View.GONE);
        }
    }

    // ── TOOLBAR / CATEGORY ─────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(
                    currentArticle == null ? "Thêm bài viết" : "Sửa bài viết");
        }
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }

    private void setupCategory() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        spinnerCategory.setAdapter(adapter);
        if (currentArticle == null) spinnerCategory.setText(CATEGORIES[0], false);
    }

    // ── RICH EDITOR ────────────────────────────────────────────────

    private void setupRichEditor() {
        int cardColor   = AdminThemeManager.getCardBackgroundColor(this);
        int textPrimary = AdminThemeManager.getTextPrimaryColor(this);

        reEditor.setEditorFontSize(16);
        reEditor.setEditorFontColor(textPrimary);
        reEditor.setPlaceholder("Bắt đầu viết nội dung bài viết tại đây...");
        reEditor.setVerticalScrollBarEnabled(true);
        reEditor.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        reEditor.getSettings().setJavaScriptEnabled(true);
        reEditor.getSettings().setAllowFileAccess(true);
        reEditor.getSettings().setAllowContentAccess(true);
        reEditor.getSettings().setAllowFileAccessFromFileURLs(true);
        reEditor.getSettings().setAllowUniversalAccessFromFileURLs(true);
        reEditor.getSettings().setDomStorageEnabled(true);
        reEditor.setEditorHeight(0);
        reEditor.setEditorBackgroundColor(cardColor);

        // ★ CSS dùng màu từ AdminThemeManager
        String textHex = isDark ? "#FFFFFF" : "#212121";
        String bgHex   = isDark
                ? String.format("#%06X", 0xFFFFFF & AdminThemeManager.DarkColors.CARD_BACKGROUND)
                : "#FFFFFF";
        reEditor.loadCSS("body { padding: 12px 16px; color:" + textHex +
                "; background:" + bgHex + "; font-size:16px; line-height:1.6; } " +
                "img { max-width:100%; height:auto; display:block; margin:10px 0;" +
                " border-radius:8px; box-shadow:0 2px 4px rgba(0,0,0,0.1); }");

        reEditor.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        reEditor.setOnInitialLoadListener(isReady -> {
            if (isReady) {
                reEditor.loadUrl("javascript:" +
                        "RE.setJustify=function(){" +
                        "RE.editor.focus();" +
                        "document.execCommand('justifyFull',false,null);};");
            }
        });

        reEditor.focusEditor();

        bindClick(R.id.action_bold,          v -> reEditor.setBold());
        bindClick(R.id.action_italic,        v -> reEditor.setItalic());
        bindClick(R.id.action_underline,     v -> reEditor.setUnderline());
        bindClick(R.id.action_strikethrough, v -> reEditor.setStrikeThrough());
        bindClick(R.id.action_align_left,    v -> reEditor.setAlignLeft());
        bindClick(R.id.action_align_center,  v -> reEditor.setAlignCenter());
        bindClick(R.id.action_align_right,   v -> reEditor.setAlignRight());
        bindClick(R.id.action_align_justify, v -> {
            reEditor.loadUrl("javascript:RE.setJustify();");
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    reEditor.loadUrl("javascript:void(function(){" +
                            "RE.editor.focus();" +
                            "document.execCommand('justifyFull',false,null);" +
                            "})();"), 100);
        });
        bindClick(R.id.action_bullet_list,   v -> reEditor.setBullets());
        bindClick(R.id.action_number_list,   v -> reEditor.setNumbers());
        bindClick(R.id.action_link,          v -> showInsertLinkDialog());
        bindClick(R.id.action_insert_image1, v -> pickImageForEditor());
    }

    private void bindClick(int id, View.OnClickListener l) {
        View v = findViewById(id); if (v != null) v.setOnClickListener(l);
    }

    // ── LISTENERS ──────────────────────────────────────────────────

    private void setupClickListeners() {
        btnPickImage.setOnClickListener(v -> pickCoverImage());
        btnPickVideo.setOnClickListener(v -> pickVideo());
        btnRemoveImage.setOnClickListener(v -> clearImage());
        btnRemoveVideo.setOnClickListener(v -> clearVideo());
        btnSave.setOnClickListener(v -> validateAndSave());
        videoPreviewContainer.setOnClickListener(v -> {
            if (videoUri != null) playVideoInDialog();
        });
    }

    // ══════════════════════════════════════════════════════════════
    //  SAVE — Luồng: validate → kiểm duyệt AI → lưu → schedule
    // ══════════════════════════════════════════════════════════════

    private void validateAndSave() {
        String title       = etTitle.getText().toString().trim();
        String author      = etAuthor.getText().toString().trim();
        String category    = spinnerCategory.getText().toString().trim();
        String contentHtml = reEditor.getHtml();

        if (TextUtils.isEmpty(title))                                            { showError("Vui lòng nhập tiêu đề"); return; }
        if (title.length() < 10)                                                 { showError("Tiêu đề phải ít nhất 10 ký tự"); return; }
        if (TextUtils.isEmpty(category))                                         { showError("Vui lòng chọn danh mục"); return; }
        if (TextUtils.isEmpty(contentHtml) || contentHtml.equals("<p><br></p>")) { showError("Vui lòng nhập nội dung"); reEditor.focusEditor(); return; }
        if (imageUri == null && currentArticle == null)                          { showError("Vui lòng chọn ảnh bìa"); return; }

        runContentModeration(title, author.isEmpty() ? "Admin" : author,
                category, contentHtml);
    }

    private void runContentModeration(String title, String author,
                                      String category, String content) {
        progressLoading.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (tvPendingBanner != null) {
            tvPendingBanner.setVisibility(View.VISIBLE);
            tvPendingBanner.setText("🔍 Đang kiểm duyệt nội dung...");
            // ★ Màu banner kiểm duyệt theo theme
            tvPendingBanner.setBackgroundColor(isDark
                    ? Color.parseColor("#1A237E") : Color.parseColor("#E3F2FD"));
            tvPendingBanner.setTextColor(isDark
                    ? Color.parseColor("#90CAF9") : Color.parseColor("#1565C0"));
        }

        ContentModerationHelper.moderate(title, content,
                new ContentModerationHelper.ModerationCallback() {

                    @Override
                    public void onApproved() {
                        resetBanner();
                        performSave(title, author, category, content);
                    }

                    @Override
                    public void onRejected(String reason) {
                        progressLoading.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        resetBanner();
                        showRejectionDialog(reason);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        android.util.Log.w("Moderation",
                                "API error: " + errorMessage + " → fallback approve");
                        resetBanner();
                        performSave(title, author, category, content);
                    }
                });
    }

    private void showRejectionDialog(String reason) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Nội dung không được phép đăng")
                .setMessage(reason)
                .setPositiveButton("Chỉnh sửa lại", (d, w) -> {
                    reEditor.focusEditor(); d.dismiss();
                })
                .setNegativeButton("Hủy bỏ", (d, w) -> d.dismiss())
                .setCancelable(false)
                .show();
    }

    private void performSave(String title, String author, String category, String content) {
        progressLoading.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        new Thread(() -> {
            String  imagePath  = resolveImagePath();
            String  videoPath  = resolveVideoPath();
            boolean success;
            long    insertedId = -1;

            if (currentArticle != null) {
                success = db.updateArticle(currentArticle.getId(),
                        title, author, category, content, imagePath, videoPath);
            } else if (isPendingMode) {
                insertedId = db.insertPendingArticle(
                        title, author, category, content, imagePath, videoPath);
                success = insertedId != -1;
            } else {
                success = db.insertArticle(
                        title, author, category, content, imagePath, videoPath) != -1;
            }

            final boolean ok         = success;
            final boolean wasPending = isPendingMode && currentArticle == null;
            final long    pendingId  = insertedId;

            runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
                progressLoading.setVisibility(View.GONE);
                btnSave.setEnabled(true);

                if (ok) {
                    if (wasPending && pendingId > 0) {
                        AutoApproveWorker.schedule(getApplicationContext(), pendingId);
                    }

                    String msg = currentArticle != null
                            ? "✅ Cập nhật bài viết thành công!"
                            : wasPending
                            ? "⏳ Bài đã gửi! Tự động đăng sau 5 phút nếu admin chưa duyệt."
                            : "✅ Đăng bài thành công!";
                    Toast.makeText(AddEditArticleActivity.this, msg, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddEditArticleActivity.this,
                            "❌ Lỗi lưu bài viết!", Toast.LENGTH_LONG).show();
                }
            }, 600));
        }).start();
    }

    private String resolveImagePath() {
        if (imageUri != null) return copyToInternal(imageUri, "cover_images");
        if (currentArticle != null) return currentArticle.getImagePath();
        return null;
    }

    private String resolveVideoPath() {
        if (videoUri != null) return copyToInternal(videoUri, "videos");
        if (currentArticle != null) return currentArticle.getVideoPath();
        return null;
    }

    // ── IMAGE / VIDEO ──────────────────────────────────────────────

    private void pickCoverImage() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(i, "Chọn ảnh bìa"));
    }

    private void pickVideo() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("video/*");
        pickVideoLauncher.launch(Intent.createChooser(i, "Chọn video"));
    }

    private void pickImageForEditor() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*");
        insertImageLauncher.launch(Intent.createChooser(i, "Chọn ảnh để chèn"));
    }

    private void updateImagePreview() {
        if (imageUri == null) return;
        Glide.with(this).load(imageUri).centerCrop().into(imgPreview);
        imgPreview.setVisibility(View.VISIBLE);
        placeholderImage.setVisibility(View.GONE);
        btnRemoveImage.setVisibility(View.VISIBLE);
    }

    private void updateVideoPreview() {
        if (videoUri == null) return;
        Bitmap thumb = getVideoThumbnail(videoUri);
        if (thumb != null) imgVideoThumbnail.setImageBitmap(thumb);
        imgVideoThumbnail.setVisibility(View.VISIBLE);
        imgPlayIcon.setVisibility(View.VISIBLE);
        placeholderVideo.setVisibility(View.GONE);
        btnRemoveVideo.setVisibility(View.VISIBLE);
    }

    private void clearImage() {
        imageUri = null;
        imgPreview.setVisibility(View.GONE);
        placeholderImage.setVisibility(View.VISIBLE);
        btnRemoveImage.setVisibility(View.GONE);
        applyPlaceholderTheme(placeholderImage, tvPlaceholderImageText,
                AdminThemeManager.getTextSecondaryColor(this));
    }

    private void clearVideo() {
        videoUri = null;
        imgVideoThumbnail.setVisibility(View.GONE);
        imgPlayIcon.setVisibility(View.GONE);
        placeholderVideo.setVisibility(View.VISIBLE);
        btnRemoveVideo.setVisibility(View.GONE);
        applyPlaceholderTheme(placeholderVideo, tvPlaceholderVideoText,
                AdminThemeManager.getTextSecondaryColor(this));
    }

    private void insertImageIntoRichEditor(Uri uri) {
        Toast.makeText(this, "Đang xử lý ảnh...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                String savedPath = copyToInternal(uri, "embedded_images");
                if (savedPath == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi lưu ảnh", Toast.LENGTH_SHORT).show());
                    return;
                }
                embeddedImagePaths.add(savedPath);
                Bitmap bm = android.graphics.BitmapFactory.decodeFile(savedPath);
                if (bm == null) return;
                if (bm.getWidth() > 800) {
                    float r = 800f / bm.getWidth();
                    bm = Bitmap.createScaledBitmap(bm, 800, (int)(bm.getHeight() * r), true);
                }
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                String b64 = android.util.Base64.encodeToString(
                        baos.toByteArray(), android.util.Base64.NO_WRAP);
                bm.recycle();
                runOnUiThread(() -> {
                    reEditor.insertImage("data:image/jpeg;base64," + b64, "Ảnh minh họa", 320);
                    Toast.makeText(this, "✓ Đã chèn ảnh!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this,
                        "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private Bitmap getVideoThumbnail(Uri uri) {
        MediaMetadataRetriever r = null;
        try {
            r = new MediaMetadataRetriever();
            r.setDataSource(this, uri);
            return r.getFrameAtTime(1_000_000);
        } catch (Exception e) { return null; }
        finally { if (r != null) try { r.release(); } catch (Exception ignored) {} }
    }

    private void playVideoInDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_video_player, null);
        VideoView vv = view.findViewById(R.id.videoViewDialog);
        vv.setVideoURI(videoUri);
        vv.setMediaController(new android.widget.MediaController(this));
        new AlertDialog.Builder(this).setView(view).create().show();
        vv.start();
    }

    private void showInsertLinkDialog() {
        int cardColor     = AdminThemeManager.getCardBackgroundColor(this);
        int textPrimary   = AdminThemeManager.getTextPrimaryColor(this);
        int textSecondary = AdminThemeManager.getTextSecondaryColor(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);
        layout.setBackgroundColor(cardColor);

        EditText etUrl = new EditText(this);
        etUrl.setHint("URL (https://...)");
        etUrl.setTextColor(textPrimary);
        etUrl.setHintTextColor(textSecondary);

        EditText etText = new EditText(this);
        etText.setHint("Văn bản hiển thị (tùy chọn)");
        etText.setTextColor(textPrimary);
        etText.setHintTextColor(textSecondary);

        layout.addView(etUrl);
        layout.addView(etText);

        new AlertDialog.Builder(this)
                .setTitle("Chèn liên kết")
                .setView(layout)
                .setPositiveButton("Chèn", (d, w) -> {
                    String url  = etUrl.getText().toString().trim();
                    String text = etText.getText().toString().trim();
                    if (!TextUtils.isEmpty(url))
                        reEditor.insertLink(url, TextUtils.isEmpty(text) ? url : text);
                })
                .setNegativeButton("Hủy", null).show();
    }

    // ── FILL FORM ──────────────────────────────────────────────────

    private void fillForm() {
        if (currentArticle == null) return;
        etTitle.setText(currentArticle.getTitle());
        etAuthor.setText(currentArticle.getAuthor());
        spinnerCategory.setText(currentArticle.getCategory(), false);
        reEditor.setHtml(currentArticle.getContent());
        if (currentArticle.getImagePath() != null
                && new File(currentArticle.getImagePath()).exists()) {
            imageUri = Uri.fromFile(new File(currentArticle.getImagePath()));
            updateImagePreview();
        }
        if (currentArticle.getVideoPath() != null
                && new File(currentArticle.getVideoPath()).exists()) {
            videoUri = Uri.fromFile(new File(currentArticle.getVideoPath()));
            updateVideoPreview();
        }
    }

    // ── HELPERS ────────────────────────────────────────────────────

    private String copyToInternal(Uri uri, String folder) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            if (is == null) return null;
            File dir = new File(getFilesDir(), folder);
            if (!dir.exists()) dir.mkdirs();
            String ext  = folder.contains("video") ? ".mp4" : ".jpg";
            File   file = new File(dir, System.currentTimeMillis() + ext);
            try (FileOutputStream os = new FileOutputStream(file)) {
                byte[] buf = new byte[8192]; int len;
                while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            }
            return file.getAbsolutePath();
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}