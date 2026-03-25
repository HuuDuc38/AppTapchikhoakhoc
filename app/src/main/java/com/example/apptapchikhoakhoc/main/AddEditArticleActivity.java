package com.example.apptapchikhoakhoc.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.apptapchikhoakhoc.utils.AutoApproveHelper;
import com.example.apptapchikhoakhoc.utils.ContentModerationHelper;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
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
    private MaterialToolbar      toolbar;
    private TextInputEditText    etTitle, etAuthor;
    private AutoCompleteTextView spinnerCategory;
    private RichEditor           reEditor;
    private TextView             tvPasteImageHint, tvPendingBanner;
    private TextView             tvSectionImage, tvSectionVideo, tvSectionContent;
    private TextView             tvPlaceholderImageText, tvPlaceholderVideoText;

    private ImageView      imgPreview;
    private LinearLayout   placeholderImage;
    private MaterialButton btnRemoveImage, btnPickImage;

    private FrameLayout    videoPreviewContainer;
    private ImageView      imgVideoThumbnail, imgPlayIcon;
    private LinearLayout   placeholderVideo;
    private MaterialButton btnRemoveVideo, btnPickVideo;

    private HorizontalScrollView editorToolbar;
    private View                 editorTopSpacer;

    private CardView cardImage, cardVideo, cardEditor;

    private TextInputLayout layoutTitle, layoutAuthor, layoutCategory;

    private LinearLayout rootContent;
    private ScrollView   rootScrollView;

    private MaterialButton            btnSave;
    private CircularProgressIndicator progressLoading;

    // ── DATA ─────────────────────────────────────────────────────────
    private DatabaseHelper     db;
    private Article            currentArticle;
    private Uri                imageUri = null;
    private Uri                videoUri = null;
    private final List<String> embeddedImagePaths = new ArrayList<>();
    private boolean            isPendingMode = false;
    private boolean            isAdmin       = false;

    // ── FIX: lưu dark mode state để tránh vòng lặp recreate vô hạn ──
    private boolean isDarkMode;
    private boolean isRecreating = false;

    private static final String[] CATEGORIES = {
            "Tin tức sự kiện", "Đào tạo", "Thông tin việc làm",
            "Khoa học công nghệ", "Hợp tác quốc tế"
    };

    // ── LAUNCHERS ────────────────────────────────────────────────────

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {
                    imageUri = result.getData().getData();
                    updateImagePreview();
                }
            });

    private final ActivityResultLauncher<Intent> pickVideoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {
                    videoUri = result.getData().getData();
                    updateVideoPreview();
                }
            });

    private final ActivityResultLauncher<Intent> insertImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {
                    insertImageIntoRichEditor(result.getData().getData());
                }
            });

    // ── LIFECYCLE ────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── FIX: lưu trạng thái dark mode ngay khi onCreate ──
        isDarkMode = ThemeManager.isDarkMode(this);
        isRecreating = false;

        setContentView(R.layout.activity_add_edit_article);

        db = new DatabaseHelper(this);
        checkAdminStatus();
        readIntentExtras();
        initViews();

        // ── FIX: áp dụng theme sau khi initViews đã hoàn tất ──
        applyActivityTheme();

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
        // ── FIX: cập nhật isDarkMode TRƯỚC khi gọi recreate để tránh vòng lặp vô hạn ──
        if (!isRecreating) {
            boolean currentDark = ThemeManager.isDarkMode(this);
            if (currentDark != isDarkMode) {
                isDarkMode = currentDark;    // cập nhật trước
                isRecreating = true;         // đánh dấu đang recreate
                recreate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRecreating = false;
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

    // ── THEME HELPERS ─────────────────────────────────────────────

    private boolean dark()     { return ThemeManager.isDarkMode(this); }
    private int bg()           { return ThemeManager.getBackgroundColor(this); }
    private int card()         { return ThemeManager.getCardBackgroundColor(this); }
    private int textMain()     { return ThemeManager.getTextPrimaryColor(this); }
    private int textHint()     { return ThemeManager.getTextSecondaryColor(this); }
    private int textTert()     { return ThemeManager.getTextTertiaryColor(this); }
    private int divider()      { return ThemeManager.getDividerColor(this); }
    private int accent()       { return ThemeManager.getAccentColor(this); }
    private int danger()       { return ThemeManager.getAccentDangerColor(this); }

    // ── APPLY THEME ───────────────────────────────────────────────

    private void applyActivityTheme() {
        // ── FIX: bọc toàn bộ trong try-catch để tránh crash khi theme chưa sẵn sàng ──
        try {
            if (rootContent    != null) rootContent.setBackgroundColor(bg());
            if (rootScrollView != null) rootScrollView.setBackgroundColor(bg());
            applyToolbarTheme();
            tintTextView(tvSectionImage,   textMain());
            tintTextView(tvSectionVideo,   textMain());
            tintTextView(tvSectionContent, textMain());
            if (tvPasteImageHint != null) {
                tvPasteImageHint.setTextColor(dark()
                        ? Color.parseColor("#BB86FC")
                        : Color.parseColor("#7B2CBF"));
            }
            applyInputLayout(layoutTitle);
            applyInputLayout(layoutAuthor);
            applyInputLayout(layoutCategory);
            applyEditText(etTitle);
            applyEditText(etAuthor);
            applyEditText(spinnerCategory);
            applyCard(cardImage);
            applyCard(cardVideo);
            applyCard(cardEditor);
            applyPlaceholder(placeholderImage, tvPlaceholderImageText);
            applyPlaceholder(placeholderVideo, tvPlaceholderVideoText);
            if (editorToolbar != null) {
                editorToolbar.setBackgroundColor(card());
                // ── FIX: dùng try-catch riêng cho tintImageButtons vì hay crash ──
                try {
                    tintImageButtons(editorToolbar, textMain());
                } catch (Exception e) {
                    android.util.Log.w("Theme", "tintImageButtons failed: " + e.getMessage());
                }
            }
            tintDividers(editorToolbar, divider());
            if (editorTopSpacer != null) editorTopSpacer.setBackgroundColor(card());
            applyRichEditorTheme();
            applyFilledButton(btnPickImage, accent());
            applyOutlinedButton(btnPickVideo, accent());
            applyFilledButton(btnSave, accent());
            if (progressLoading != null) progressLoading.setIndicatorColor(accent());
            applyPendingBannerTheme();
        } catch (Exception e) {
            android.util.Log.e("Theme", "applyActivityTheme error: " + e.getMessage());
        }
    }

    private void applyToolbarTheme() {
        if (toolbar == null) return;
        try {
            if (dark()) {
                toolbar.setBackgroundColor(ThemeManager.DarkColors.STATUS_BAR);
            } else {
                toolbar.setBackground(
                        androidx.core.content.ContextCompat.getDrawable(
                                this, R.drawable.toolbar_gradient_red));
            }
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setNavigationIconTint(Color.WHITE);
        } catch (Exception e) {
            toolbar.setBackgroundColor(Color.parseColor("#C8463D"));
            toolbar.setTitleTextColor(Color.WHITE);
        }
    }

    private void applyInputLayout(TextInputLayout layout) {
        if (layout == null) return;
        try {
            layout.setBoxBackgroundColor(card());
            layout.setDefaultHintTextColor(
                    android.content.res.ColorStateList.valueOf(textHint()));
            layout.setHintTextColor(
                    android.content.res.ColorStateList.valueOf(textHint()));
            int strokeColor = dark()
                    ? ThemeManager.DarkColors.DIVIDER
                    : ThemeManager.LightColors.ACCENT;
            layout.setBoxStrokeColor(strokeColor);
            layout.setBoxStrokeColorStateList(
                    android.content.res.ColorStateList.valueOf(strokeColor));
        } catch (Exception e) {
            android.util.Log.w("Theme", "applyInputLayout failed: " + e.getMessage());
        }
    }

    private void applyEditText(TextView et) {
        if (et == null) return;
        et.setTextColor(textMain());
        et.setHintTextColor(textHint());
    }

    private void applyCard(CardView cv) {
        if (cv == null) return;
        cv.setCardBackgroundColor(card());
        cv.setCardElevation(dark() ? 0f : 6f);
    }

    private void applyPlaceholder(LinearLayout placeholder, TextView label) {
        if (placeholder == null) return;
        placeholder.setBackgroundColor(dark()
                ? ThemeManager.DarkColors.BACKGROUND
                : Color.parseColor("#F5F5F5"));
        if (label != null) label.setTextColor(textHint());
        int iconColor = dark()
                ? ThemeManager.DarkColors.TEXT_TERTIARY
                : Color.parseColor("#BBBBBB");
        for (int i = 0; i < placeholder.getChildCount(); i++) {
            View child = placeholder.getChildAt(i);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private void applyRichEditorTheme() {
        if (reEditor == null) return;
        try {
            reEditor.setEditorBackgroundColor(card());
            reEditor.setEditorFontColor(textMain());
            String textHex = dark() ? "#FFFFFF" : "#1A1A1A";
            String bgHex   = dark() ? "#243447"  : "#FFFFFF";
            reEditor.loadCSS(
                    "body { padding:12px 16px; color:" + textHex +
                            "; background:" + bgHex +
                            "; font-size:16px; line-height:1.6; } " +
                            "img { max-width:100%; height:auto; display:block;" +
                            " margin:10px 0; border-radius:8px; } " +
                            "a { color:" + (dark() ? "#4FC3F7" : "#C8463D") + "; } " +
                            "::selection { background:rgba(200,70,61,0.25); }"
            );
        } catch (Exception e) {
            android.util.Log.w("Theme", "applyRichEditorTheme failed: " + e.getMessage());
        }
    }

    private void applyFilledButton(MaterialButton btn, int bgColor) {
        if (btn == null) return;
        btn.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(bgColor));
        btn.setTextColor(Color.WHITE);
        btn.setIconTint(android.content.res.ColorStateList.valueOf(Color.WHITE));
    }

    private void applyOutlinedButton(MaterialButton btn, int accentColor) {
        if (btn == null) return;
        btn.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(Color.TRANSPARENT));
        btn.setStrokeColor(
                android.content.res.ColorStateList.valueOf(accentColor));
        btn.setTextColor(accentColor);
        btn.setIconTint(android.content.res.ColorStateList.valueOf(accentColor));
    }

    private void applyPendingBannerTheme() {
        if (tvPendingBanner == null
                || tvPendingBanner.getVisibility() != View.VISIBLE) return;
        tvPendingBanner.setBackgroundColor(dark()
                ? Color.parseColor("#332600")
                : Color.parseColor("#FFF3E0"));
        tvPendingBanner.setTextColor(dark()
                ? Color.parseColor("#FFB74D")
                : Color.parseColor("#E65100"));
    }

    private void tintTextView(TextView tv, int color) {
        if (tv != null) tv.setTextColor(color);
    }

    private void tintImageButtons(ViewGroup parent, int color) {
        if (parent == null) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ImageButton) {
                // ── FIX: dùng setColorFilter thay vì setImageTintList để tránh crash ──
                ((ImageButton) child).setColorFilter(color, PorterDuff.Mode.SRC_IN);
                // ── FIX: set background transparent để tránh lỗi ?attr/ không resolve ──
                child.setBackgroundColor(Color.TRANSPARENT);
            } else if (child instanceof ViewGroup) {
                tintImageButtons((ViewGroup) child, color);
            }
        }
    }

    private void tintDividers(ViewGroup parent, int color) {
        if (parent == null) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child.getClass().equals(View.class)) {
                child.setBackgroundColor(color);
            } else if (child instanceof ViewGroup) {
                tintDividers((ViewGroup) child, color);
            }
        }
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setupCategory() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES) {
            @Override
            public View getView(int pos, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(pos, convertView, parent);
                tv.setTextColor(textMain());
                tv.setBackgroundColor(card());
                return tv;
            }
            @Override
            public View getDropDownView(int pos, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(pos, convertView, parent);
                tv.setTextColor(textMain());
                tv.setBackgroundColor(card());
                tv.setPadding(32, 24, 32, 24);
                return tv;
            }
        };
        spinnerCategory.setAdapter(adapter);
        spinnerCategory.setDropDownBackgroundDrawable(new ColorDrawable(card()));
        if (currentArticle == null) spinnerCategory.setText(CATEGORIES[0], false);
    }

    // ── RICH EDITOR ────────────────────────────────────────────────

    private void setupRichEditor() {
        if (reEditor == null) return;
        try {
            reEditor.setEditorFontSize(16);
            reEditor.setEditorFontColor(textMain());
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
            applyRichEditorTheme();

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
                    reEditor.loadUrl(
                            "javascript:RE.setJustify=function(){" +
                                    "RE.editor.focus();" +
                                    "document.execCommand('justifyFull',false,null);" +
                                    "};"
                    );
                }
            });

            reEditor.focusEditor();

        } catch (Exception e) {
            android.util.Log.e("RichEditor", "setupRichEditor error: " + e.getMessage());
        }

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
                    reEditor.loadUrl(
                            "javascript:void(function(){" +
                                    "RE.editor.focus();" +
                                    "document.execCommand('justifyFull',false,null);" +
                                    "})();"
                    ), 100);
        });
        bindClick(R.id.action_bullet_list,   v -> reEditor.setBullets());
        bindClick(R.id.action_number_list,   v -> reEditor.setNumbers());
        bindClick(R.id.action_link,          v -> showInsertLinkDialog());
        bindClick(R.id.action_insert_image1, v -> pickImageForEditor());
    }

    private void bindClick(int id, View.OnClickListener l) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(l);
    }

    // ── LISTENERS ──────────────────────────────────────────────────

    private void setupClickListeners() {
        if (btnPickImage  != null) btnPickImage.setOnClickListener(v -> pickCoverImage());
        if (btnPickVideo  != null) btnPickVideo.setOnClickListener(v -> pickVideo());
        if (btnRemoveImage != null) btnRemoveImage.setOnClickListener(v -> clearImage());
        if (btnRemoveVideo != null) btnRemoveVideo.setOnClickListener(v -> clearVideo());
        if (btnSave       != null) btnSave.setOnClickListener(v -> validateAndSave());
        if (videoPreviewContainer != null) {
            videoPreviewContainer.setOnClickListener(v -> {
                if (videoUri != null) playVideoInDialog();
            });
        }
    }

    // ── SAVE + KIỂM DUYỆT + AUTO-APPROVE ─────────────────────────

    private void validateAndSave() {
        String title       = etTitle != null ? etTitle.getText().toString().trim() : "";
        String author      = etAuthor != null ? etAuthor.getText().toString().trim() : "";
        String category    = spinnerCategory != null
                ? spinnerCategory.getText().toString().trim() : "";
        String contentHtml = reEditor != null ? reEditor.getHtml() : "";

        if (TextUtils.isEmpty(title)) {
            showError("Vui lòng nhập tiêu đề"); return;
        }
        if (title.length() < 10) {
            showError("Tiêu đề phải ít nhất 10 ký tự"); return;
        }
        if (TextUtils.isEmpty(category)) {
            showError("Vui lòng chọn danh mục"); return;
        }
        if (TextUtils.isEmpty(contentHtml) || contentHtml.equals("<p><br></p>")) {
            showError("Vui lòng nhập nội dung");
            if (reEditor != null) reEditor.focusEditor();
            return;
        }
        if (imageUri == null && currentArticle == null) {
            showError("Vui lòng chọn ảnh bìa"); return;
        }

        runContentModeration(title,
                author.isEmpty() ? "Admin" : author,
                category, contentHtml);
    }

    private void runContentModeration(String title, String author,
                                      String category, String content) {
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);
        if (btnSave         != null) btnSave.setEnabled(false);

        if (tvPendingBanner != null) {
            tvPendingBanner.setVisibility(View.VISIBLE);
            tvPendingBanner.setText("🔍 Đang kiểm duyệt nội dung...");
            tvPendingBanner.setBackgroundColor(dark()
                    ? Color.parseColor("#1A237E")
                    : Color.parseColor("#E3F2FD"));
            tvPendingBanner.setTextColor(dark()
                    ? Color.parseColor("#90CAF9")
                    : Color.parseColor("#1565C0"));
        }

        ContentModerationHelper.moderate(title, content,
                new ContentModerationHelper.ModerationCallback() {
                    @Override public void onApproved() {
                        resetBanner();
                        performSave(title, author, category, content);
                    }
                    @Override public void onRejected(String reason) {
                        if (progressLoading != null)
                            progressLoading.setVisibility(View.GONE);
                        if (btnSave != null) btnSave.setEnabled(true);
                        resetBanner();
                        showRejectionDialog(reason);
                    }
                    @Override public void onError(String errorMessage) {
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
                    if (reEditor != null) reEditor.focusEditor();
                    d.dismiss();
                })
                .setNegativeButton("Hủy bỏ", (d, w) -> d.dismiss())
                .setCancelable(false)
                .show();
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

    private void performSave(String title, String author,
                             String category, String content) {
        if (progressLoading != null) progressLoading.setVisibility(View.VISIBLE);
        if (btnSave         != null) btnSave.setEnabled(false);

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
                success    = insertedId != -1;
            } else {
                success = db.insertArticle(
                        title, author, category, content, imagePath, videoPath) != -1;
            }

            final boolean ok         = success;
            final boolean wasPending = isPendingMode && currentArticle == null;
            final long    pendingId  = insertedId;

            runOnUiThread(() ->
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (progressLoading != null)
                            progressLoading.setVisibility(View.GONE);
                        if (btnSave != null) btnSave.setEnabled(true);
                        if (ok) {
                            if (wasPending && pendingId > 0) {
                                AutoApproveHelper.scheduleAutoApprove(
                                        getApplicationContext(), pendingId);
                            }
                            String msg = currentArticle != null
                                    ? "✅ Cập nhật bài viết thành công!"
                                    : wasPending
                                    ? "⏳ Bài đã gửi! Tự động duyệt sau 5 phút nếu admin chưa xử lý."
                                    : "✅ Đăng bài thành công!";
                            Toast.makeText(AddEditArticleActivity.this,
                                    msg, Toast.LENGTH_LONG).show();
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
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(i, "Chọn ảnh bìa"));
    }

    private void pickVideo() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        pickVideoLauncher.launch(Intent.createChooser(i, "Chọn video"));
    }

    private void pickImageForEditor() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        insertImageLauncher.launch(Intent.createChooser(i, "Chọn ảnh để chèn"));
    }

    private void updateImagePreview() {
        if (imageUri == null) return;
        Glide.with(this).load(imageUri).centerCrop().into(imgPreview);
        if (imgPreview       != null) imgPreview.setVisibility(View.VISIBLE);
        if (placeholderImage != null) placeholderImage.setVisibility(View.GONE);
        if (btnRemoveImage   != null) btnRemoveImage.setVisibility(View.VISIBLE);
    }

    private void updateVideoPreview() {
        if (videoUri == null) return;
        Bitmap thumb = getVideoThumbnail(videoUri);
        if (thumb != null && imgVideoThumbnail != null) {
            imgVideoThumbnail.setImageBitmap(thumb);
        }
        if (imgVideoThumbnail != null) imgVideoThumbnail.setVisibility(View.VISIBLE);
        if (imgPlayIcon       != null) imgPlayIcon.setVisibility(View.VISIBLE);
        if (placeholderVideo  != null) placeholderVideo.setVisibility(View.GONE);
        if (btnRemoveVideo    != null) btnRemoveVideo.setVisibility(View.VISIBLE);
    }

    private void clearImage() {
        imageUri = null;
        if (imgPreview       != null) imgPreview.setVisibility(View.GONE);
        if (placeholderImage != null) placeholderImage.setVisibility(View.VISIBLE);
        if (btnRemoveImage   != null) btnRemoveImage.setVisibility(View.GONE);
        applyPlaceholder(placeholderImage, tvPlaceholderImageText);
    }

    private void clearVideo() {
        videoUri = null;
        if (imgVideoThumbnail != null) imgVideoThumbnail.setVisibility(View.GONE);
        if (imgPlayIcon       != null) imgPlayIcon.setVisibility(View.GONE);
        if (placeholderVideo  != null) placeholderVideo.setVisibility(View.VISIBLE);
        if (btnRemoveVideo    != null) btnRemoveVideo.setVisibility(View.GONE);
        applyPlaceholder(placeholderVideo, tvPlaceholderVideoText);
    }

    private void insertImageIntoRichEditor(Uri uri) {
        Toast.makeText(this, "Đang xử lý ảnh...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                String savedPath = copyToInternal(uri, "embedded_images");
                if (savedPath == null) {
                    runOnUiThread(() -> Toast.makeText(this,
                            "Lỗi lưu ảnh", Toast.LENGTH_SHORT).show());
                    return;
                }
                embeddedImagePaths.add(savedPath);
                Bitmap bm = android.graphics.BitmapFactory.decodeFile(savedPath);
                if (bm == null) return;
                if (bm.getWidth() > 800) {
                    float r = 800f / bm.getWidth();
                    bm = Bitmap.createScaledBitmap(
                            bm, 800, (int)(bm.getHeight() * r), true);
                }
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                String b64 = android.util.Base64.encodeToString(
                        baos.toByteArray(), android.util.Base64.NO_WRAP);
                bm.recycle();
                runOnUiThread(() -> {
                    reEditor.insertImage(
                            "data:image/jpeg;base64," + b64, "Ảnh minh họa", 320);
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
        } catch (Exception e) {
            return null;
        } finally {
            if (r != null) {
                try { r.release(); } catch (Exception ignored) {}
            }
        }
    }

    private void playVideoInDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_video_player, null);
        VideoView vv = view.findViewById(R.id.videoViewDialog);
        vv.setVideoURI(videoUri);
        vv.setMediaController(new android.widget.MediaController(this));
        AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(card()));
        }
        dialog.show();
        vv.start();
    }

    private void showInsertLinkDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 28, 40, 16);
        layout.setBackgroundColor(card());
        EditText etUrl  = buildDialogEditText("URL (https://...)");
        EditText etText = buildDialogEditText("Văn bản hiển thị (tùy chọn)");
        layout.addView(etUrl);
        layout.addView(etText);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chèn liên kết")
                .setView(layout)
                .setPositiveButton("Chèn", (d, w) -> {
                    String url  = etUrl.getText().toString().trim();
                    String text = etText.getText().toString().trim();
                    if (!TextUtils.isEmpty(url)) {
                        reEditor.insertLink(url, TextUtils.isEmpty(text) ? url : text);
                    }
                })
                .setNegativeButton("Hủy", null)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(card()));
        }
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(accent());
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(textHint());
    }

    private EditText buildDialogEditText(String hint) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setTextColor(textMain());
        et.setHintTextColor(textHint());
        et.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(divider()));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 20);
        et.setLayoutParams(lp);
        return et;
    }

    // ── FILL FORM ──────────────────────────────────────────────────

    private void fillForm() {
        if (currentArticle == null) return;
        if (etTitle         != null) etTitle.setText(currentArticle.getTitle());
        if (etAuthor        != null) etAuthor.setText(currentArticle.getAuthor());
        if (spinnerCategory != null)
            spinnerCategory.setText(currentArticle.getCategory(), false);
        if (reEditor        != null) reEditor.setHtml(currentArticle.getContent());

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
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}