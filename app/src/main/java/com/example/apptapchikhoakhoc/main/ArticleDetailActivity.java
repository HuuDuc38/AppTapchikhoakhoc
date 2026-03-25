package com.example.apptapchikhoakhoc.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.adapter.CommentAdapter;
import com.example.apptapchikhoakhoc.adapter.NewsAdapter;
import com.example.apptapchikhoakhoc.data.BaseActivity;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.model.Comment;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleDetailActivity extends BaseActivity {

    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME  = "name";
    private static final int    RC_LOGIN  = 1001;

    // ── Views ───────────────────────────────────────────────────
    private View         rootLayout;
    private LinearLayout contentLayout, articleInfoLayout, actionBarLayout, relatedNewsLayout;
    private View         separatorView, dividerContent, dividerAboveReaction, dividerReaction;

    private ImageView imgDetail;
    private TextView  tvTitle, tvAuthor, tvCategory, tvDate;
    private ImageView icDate, icAuthor;
    private WebView   webViewContent;

    private CardView    cardVideo;
    private VideoView   videoView;
    private FrameLayout videoContainer, thumbnailContainer;
    private ImageView   imgVideoThumbnail, imgPlayIcon;
    private View        videoControlsLayout;
    private ImageView   btnPlayPause, btnFullscreen;
    private SeekBar     seekBar;
    private TextView    tvCurrentTime, tvTotalTime;
    private ProgressBar progressBar;

    private LinearLayout layoutLike, layoutComment, layoutShare, layoutReactionCount;
    private ImageView    imgLike, imgComment, imgShare;
    private TextView     tvLike, tvComment, tvShare;
    private TextView     tvReactionEmojis, tvReactionTotal, tvCommentCountBar;

    private RecyclerView recyclerRelatedNews;
    private TextView     tvRelatedHeader;

    // ── State ───────────────────────────────────────────────────
    private Article        article;
    private DatabaseHelper db;

    private boolean isPlaying                 = false;
    private boolean isFullscreen              = false;
    private boolean isUserTriggeredFullscreen = false;
    private int     currentPosition           = 0;
    private Uri     currentVideoUri           = null;

    private android.os.Handler handler = new android.os.Handler();

    private ViewGroup              originalParent;
    private int                    originalIndex;
    private ViewGroup.LayoutParams originalLayoutParams;
    private ViewGroup.LayoutParams originalVideoLayoutParams;
    private ViewGroup.LayoutParams originalContainerLayoutParams;

    private String articleReaction = null;
    private final java.util.LinkedHashMap<String, Integer> reactionCountMap = new java.util.LinkedHashMap<>();
    private String currentUserEmail = "";

    // ══════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        db = new DatabaseHelper(this);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        currentUserEmail = prefs.getString(KEY_EMAIL, "");
        if (currentUserEmail == null) currentUserEmail = "";

        initViews();
        setupToolbar();
        applyDarkModeTheme();
        setupCustomVideoControls();
        setupWebView();
        loadArticle(); // ✅ tăng lượt xem bên trong này
        setupActionButtons();
        loadRelatedNews();

        if (savedInstanceState != null) {
            currentPosition           = savedInstanceState.getInt("video_position", 0);
            isPlaying                 = savedInstanceState.getBoolean("is_playing", false);
            isFullscreen              = savedInstanceState.getBoolean("is_fullscreen", false);
            isUserTriggeredFullscreen = savedInstanceState.getBoolean("user_triggered_fullscreen", false);
            if (currentVideoUri != null && currentPosition > 0) {
                videoView.seekTo(currentPosition);
                if (isPlaying) playVideo();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (videoView != null) {
            outState.putInt("video_position", videoView.getCurrentPosition());
            outState.putBoolean("is_playing", isPlaying);
            outState.putBoolean("is_fullscreen", isFullscreen);
            outState.putBoolean("user_triggered_fullscreen", isUserTriggeredFullscreen);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoView != null && isPlaying) currentPosition = videoView.getCurrentPosition();
        if (!isUserTriggeredFullscreen) {
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!isFullscreen && cardVideo.getVisibility() == View.VISIBLE
                        && videoView.getVisibility() == View.VISIBLE) enterFullscreen();
            } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (isFullscreen) exitFullscreen();
            }
        }
        isUserTriggeredFullscreen = false;
        if (currentPosition > 0 && videoView != null) {
            handler.postDelayed(() -> {
                videoView.seekTo(currentPosition);
                if (isPlaying) videoView.start();
            }, 150);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (isFullscreen) { exitFullscreen(); return true; }
            if (videoView != null && videoView.isPlaying()) videoView.stopPlayback();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) { exitFullscreen(); return; }
        if (videoView != null && videoView.isPlaying()) videoView.stopPlayback();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            currentPosition = videoView.getCurrentPosition();
            videoView.pause();
            updatePlayPauseButton();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentPosition > 0 && videoView != null && currentVideoUri != null)
            videoView.seekTo(currentPosition);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (videoView != null) videoView.stopPlayback();
    }

    // ══════════════════════════════════════════════════════════
    //  INIT VIEWS
    // ══════════════════════════════════════════════════════════

    private void initViews() {
        rootLayout           = findViewById(R.id.rootLayout);
        contentLayout        = findViewById(R.id.contentLayout);
        articleInfoLayout    = findViewById(R.id.articleInfoLayout);
        actionBarLayout      = findViewById(R.id.actionBarLayout);
        relatedNewsLayout    = findViewById(R.id.relatedNewsLayout);
        separatorView        = findViewById(R.id.separatorView);
        dividerContent       = findViewById(R.id.dividerContent);
        dividerAboveReaction = findViewById(R.id.dividerAboveReaction);
        dividerReaction      = findViewById(R.id.dividerReaction);

        imgDetail      = findViewById(R.id.imgDetail);
        tvTitle        = findViewById(R.id.tvTitle);
        tvAuthor       = findViewById(R.id.tvAuthor);
        tvCategory     = findViewById(R.id.tvCategory);
        tvDate         = findViewById(R.id.tvDate);
        icDate         = findViewById(R.id.icDate);
        icAuthor       = findViewById(R.id.icAuthor);
        webViewContent = findViewById(R.id.webViewContent);

        cardVideo           = findViewById(R.id.cardVideo);
        videoContainer      = findViewById(R.id.videoContainer);
        videoView           = findViewById(R.id.videoView);
        thumbnailContainer  = findViewById(R.id.thumbnailContainer);
        imgVideoThumbnail   = findViewById(R.id.imgVideoThumbnail);
        imgPlayIcon         = findViewById(R.id.imgPlayIcon);
        videoControlsLayout = findViewById(R.id.videoControlsLayout);
        btnPlayPause        = findViewById(R.id.btnPlayPause);
        btnFullscreen       = findViewById(R.id.btnFullscreen);
        seekBar             = findViewById(R.id.seekBar);
        tvCurrentTime       = findViewById(R.id.tvCurrentTime);
        tvTotalTime         = findViewById(R.id.tvTotalTime);
        progressBar         = findViewById(R.id.progressBar);

        layoutLike          = findViewById(R.id.layoutLike);
        layoutComment       = findViewById(R.id.layoutComment);
        layoutShare         = findViewById(R.id.layoutShare);
        layoutReactionCount = findViewById(R.id.layoutReactionCount);
        imgLike             = findViewById(R.id.imgLike);
        imgComment          = findViewById(R.id.imgComment);
        imgShare            = findViewById(R.id.imgShare);
        tvLike              = findViewById(R.id.tvLike);
        tvComment           = findViewById(R.id.tvComment);
        tvShare             = findViewById(R.id.tvShare);
        tvReactionEmojis    = findViewById(R.id.tvReactionEmojis);
        tvReactionTotal     = findViewById(R.id.tvReactionTotal);
        tvCommentCountBar   = findViewById(R.id.tvCommentCountBar);

        recyclerRelatedNews = findViewById(R.id.recyclerRelatedNews);
        tvRelatedHeader     = findViewById(R.id.tvRelatedHeader);
    }

    // ══════════════════════════════════════════════════════════
    //  TOOLBAR
    // ══════════════════════════════════════════════════════════

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (isDarkMode) {
            int darkColor = ThemeManager.DarkColors.STATUS_BAR;
            toolbar.setBackgroundColor(darkColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().setStatusBarColor(darkColor);
            }
        } else {
            toolbar.setBackgroundResource(R.drawable.toolbar_gradient_red);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                getWindow().setStatusBarColor(Color.parseColor("#8E24AA"));
            }
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết bài viết");
        }
    }

    // ══════════════════════════════════════════════════════════
    //  DARK MODE
    // ══════════════════════════════════════════════════════════

    private void applyDarkModeTheme() {
        int bgMain    = isDarkMode ? ThemeManager.DarkColors.BACKGROUND      : Color.WHITE;
        int bgCard    = isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int bgSep     = isDarkMode ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.parseColor("#F5F5F5");
        int divMain   = isDarkMode ? ThemeManager.DarkColors.DIVIDER         : Color.parseColor("#E4E6EB");
        int divSub    = isDarkMode ? ThemeManager.DarkColors.DIVIDER         : Color.parseColor("#E5E5E5");
        int txtPri    = isDarkMode ? ThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSec    = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#888888");
        int txtTer    = isDarkMode ? ThemeManager.DarkColors.TEXT_TERTIARY   : Color.parseColor("#555555");
        int txtAction = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#606770");
        int txtReact  = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#65676B");
        int accentRed = isDarkMode ? ThemeManager.DarkColors.ACCENT          : Color.parseColor("#C8463D");
        int accentSec = isDarkMode ? ThemeManager.DarkColors.ACCENT          : Color.parseColor("#D84315");

        if (rootLayout          != null) rootLayout.setBackgroundColor(bgMain);
        if (articleInfoLayout   != null) articleInfoLayout.setBackgroundColor(bgMain);
        if (actionBarLayout     != null) actionBarLayout.setBackgroundColor(bgCard);
        if (relatedNewsLayout   != null) relatedNewsLayout.setBackgroundColor(bgCard);
        if (separatorView       != null) separatorView.setBackgroundColor(bgSep);
        if (layoutReactionCount != null) layoutReactionCount.setBackgroundColor(bgCard);

        if (dividerAboveReaction != null) dividerAboveReaction.setBackgroundColor(divMain);
        if (dividerReaction      != null) dividerReaction.setBackgroundColor(divMain);
        if (dividerContent       != null) dividerContent.setBackgroundColor(divSub);

        if (tvTitle    != null) tvTitle.setTextColor(txtPri);
        if (tvDate     != null) tvDate.setTextColor(txtSec);
        if (tvAuthor   != null) tvAuthor.setTextColor(accentRed);
        if (tvCategory != null) tvCategory.setTextColor(txtTer);

        if (icDate   != null) icDate.setColorFilter(txtSec);
        if (icAuthor != null) icAuthor.setColorFilter(accentRed);

        if (imgLike    != null) imgLike.setColorFilter(txtAction);
        if (imgComment != null) imgComment.setColorFilter(txtAction);
        if (imgShare   != null) imgShare.setColorFilter(txtAction);
        if (tvLike     != null) tvLike.setTextColor(txtAction);
        if (tvComment  != null) tvComment.setTextColor(txtAction);
        if (tvShare    != null) tvShare.setTextColor(txtAction);

        if (tvReactionTotal   != null) tvReactionTotal.setTextColor(txtReact);
        if (tvCommentCountBar != null) tvCommentCountBar.setTextColor(txtReact);
        if (tvRelatedHeader   != null) tvRelatedHeader.setTextColor(accentSec);
    }

    private static String hex(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    // ══════════════════════════════════════════════════════════
    //  WEBVIEW
    // ══════════════════════════════════════════════════════════

    private void setupWebView() {
        WebSettings s = webViewContent.getSettings();
        s.setJavaScriptEnabled(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        webViewContent.setInitialScale(1);
        webViewContent.setBackgroundColor(Color.TRANSPARENT);
    }

    private String buildHtmlPage(String body) {
        String textColor   = isDarkMode ? hex(ThemeManager.DarkColors.TEXT_PRIMARY)    : "#333333";
        String bgColor     = isDarkMode ? hex(ThemeManager.DarkColors.BACKGROUND)      : "#FFFFFF";
        String boldColor   = isDarkMode ? "#FFFFFF"                                    : "#000000";
        String linkColor   = isDarkMode ? hex(ThemeManager.DarkColors.ACCENT)          : "#C8463D";
        String quoteBorder = isDarkMode ? hex(ThemeManager.DarkColors.ACCENT)          : "#C8463D";
        String quoteBg     = isDarkMode ? hex(ThemeManager.DarkColors.CARD_BACKGROUND) : "#F5F5F5";
        String quoteTxt    = isDarkMode ? hex(ThemeManager.DarkColors.TEXT_SECONDARY)  : "#666666";
        String borderColor = isDarkMode ? "#2E3D4F"                                    : "#DDDDDD";
        String thBg        = isDarkMode ? hex(ThemeManager.DarkColors.CARD_BACKGROUND) : "#F5F5F5";

        return "<!DOCTYPE html><html><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no'>"
                + "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "html,body { background:" + bgColor + "; }"
                + "body { font-family:'Roboto',Arial,sans-serif; font-size:16px;"
                + "       line-height:1.7; color:" + textColor + "; word-wrap:break-word; }"
                + "p { margin:0 0 12px 0; text-align:justify; }"
                + "img { max-width:100%!important; height:auto!important; display:block;"
                + "      margin:12px 0; border-radius:8px; }"
                + "strong,b { font-weight:bold; color:" + boldColor + "; }"
                + "em,i { font-style:italic; }"
                + "u { text-decoration:underline; }"
                + "ul,ol { margin:12px 0; padding-left:20px; }"
                + "li { margin:6px 0; }"
                + "a { color:" + linkColor + "; text-decoration:none; }"
                + "blockquote { border-left:4px solid " + quoteBorder + ";"
                + "  background:" + quoteBg + "; color:" + quoteTxt + ";"
                + "  font-style:italic; padding:10px 16px; margin:12px 0; border-radius:4px; }"
                + "h1,h2,h3,h4 { color:" + boldColor + "; margin:16px 0 8px; }"
                + "table { width:100%; border-collapse:collapse; margin:12px 0; }"
                + "td,th { border:1px solid " + borderColor + "; padding:8px; color:" + textColor + "; }"
                + "th { background:" + thBg + "; }"
                + "</style></head><body>"
                + body
                + "</body></html>";
    }

    // ══════════════════════════════════════════════════════════
    //  LOAD ARTICLE — ✅ Tăng lượt xem tại đây
    // ══════════════════════════════════════════════════════════

    private void loadArticle() {
        article = (Article) getIntent().getSerializableExtra("article");
        if (article == null) {
            Toast.makeText(this, "Không tìm thấy bài viết", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Tăng lượt xem ngay khi mở bài
        db.incrementViewCount(article.getId());

        tvTitle.setText(article.getTitle());
        tvAuthor.setText("Tác giả: " + (article.getAuthor() != null ? article.getAuthor() : "Admin"));
        tvCategory.setText("Chuyên mục: " + (article.getCategory() != null ? article.getCategory() : "Chưa phân loại"));
        tvDate.setText(getCurrentDate());

        if (article.getImagePath() != null && !article.getImagePath().isEmpty()) {
            File f = new File(article.getImagePath());
            if (f.exists()) Glide.with(this).load(f).into(imgDetail);
        }
        if (article.getVideoPath() != null && !article.getVideoPath().isEmpty()) {
            File f = new File(article.getVideoPath());
            if (f.exists()) setupVideoPlayer(Uri.fromFile(f));
        }
        displayContentInWebView(article.getContent());
    }

    private void setupVideoPlayer(Uri videoUri) {
        try {
            currentVideoUri = videoUri;
            videoView.setVideoURI(videoUri);
            Bitmap thumb = getVideoThumbnail(videoUri);
            if (thumb != null) imgVideoThumbnail.setImageBitmap(thumb);
            else Glide.with(this).asBitmap().load(videoUri).centerCrop()
                    .placeholder(R.drawable.video_placeholder)
                    .error(R.drawable.video_placeholder)
                    .into(imgVideoThumbnail);
            cardVideo.setVisibility(View.VISIBLE);
            thumbnailContainer.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            videoControlsLayout.setVisibility(View.GONE);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
        MediaMetadataRetriever r = null;
        try {
            r = new MediaMetadataRetriever();
            r.setDataSource(this, videoUri);
            Bitmap t = r.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            if (t == null) t = r.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST);
            if (t == null) t = r.getFrameAtTime();
            return t;
        } catch (Exception e) { e.printStackTrace(); return null; }
        finally { if (r != null) { try { r.release(); } catch (Exception ignored) {} } }
    }

    private void displayContentInWebView(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            webViewContent.loadData("<p>Không có nội dung</p>", "text/html; charset=UTF-8", null);
            return;
        }
        webViewContent.loadDataWithBaseURL(null,
                buildHtmlPage(processImagePaths(htmlContent)), "text/html", "UTF-8", null);
    }

    private String processImagePaths(String htmlContent) {
        Pattern p = Pattern.compile("<img[^>]*src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(htmlContent);
        StringBuffer result = new StringBuffer();
        while (m.find()) {
            String tag = m.group(0), path = m.group(1);
            if (path.startsWith("data:image")) { m.appendReplacement(result, Matcher.quoteReplacement(tag)); continue; }
            if (path.startsWith("file://")) path = path.substring(7);
            File f = new File(path);
            if (f.exists()) {
                try {
                    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                    if (bmp != null) {
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos);
                        String b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
                        m.appendReplacement(result, Matcher.quoteReplacement(
                                tag.replaceFirst("src\\s*=\\s*['\"][^'\"]+['\"]",
                                        "src=\"data:image/jpeg;base64," + b64 + "\"")));
                        bmp.recycle(); baos.close(); continue;
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            m.appendReplacement(result, Matcher.quoteReplacement(tag));
        }
        m.appendTail(result);
        return result.toString();
    }

    // ══════════════════════════════════════════════════════════
    //  ACTION BUTTONS — ✅ Bắt buộc đăng nhập để like/comment
    // ══════════════════════════════════════════════════════════

    private void setupActionButtons() {
        int commentCount = db.getCommentsCount(article.getId());
        if (commentCount > 0) updateCommentBarExternal(commentCount);

        loadSavedReaction();

        // ── Like ─────────────────────────────────────────────────
        layoutLike.setOnClickListener(v -> {
            // ✅ Bắt buộc đăng nhập
            if (currentUserEmail.isEmpty()) {
                showLoginRequiredDialog("like");
                return;
            }
            if (articleReaction != null) {
                db.removeReactionFromDb(article.getId(), currentUserEmail);
                removeReactionLocal(articleReaction);
                articleReaction = null;
                applyLikeButton(null);
            } else {
                articleReaction = "👍";
                db.addOrUpdateReaction(article.getId(), currentUserEmail, "👍");
                addReactionLocal("👍");
                applyLikeButton("👍");
            }
            refreshReactionBar(commentCount);
        });

        layoutLike.setOnLongClickListener(v -> {
            // ✅ Bắt buộc đăng nhập
            if (currentUserEmail.isEmpty()) {
                showLoginRequiredDialog("like");
                return true;
            }
            showReactionPopup(commentCount);
            return true;
        });

        // ── Comment ───────────────────────────────────────────────
        layoutComment.setOnClickListener(v -> {
            // ✅ Bắt buộc đăng nhập
            if (currentUserEmail.isEmpty()) {
                showLoginRequiredDialog("comment");
                return;
            }
            showCommentBottomSheet(currentUserEmail);
        });

        // ── Share ─────────────────────────────────────────────────
        layoutShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getTitle());
            String plain = article.getContent()
                    .replaceAll("<img[^>]*>", "")
                    .replaceAll("<br\\s*/?>", "\n")
                    .replaceAll("<[^>]+>", "").trim();
            shareIntent.putExtra(Intent.EXTRA_TEXT, article.getTitle() + "\n\n" + plain);
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài viết"));
            db.shareArticle(article.getId());
        });
    }

    // ══════════════════════════════════════════════════════════
    //  LOGIN REQUIRED DIALOG — dùng chung cho like & comment
    // ══════════════════════════════════════════════════════════

    private void showLoginRequiredDialog(String action) {
        String msg = "like".equals(action)
                ? "Bạn cần đăng nhập để thích bài viết."
                : "Bạn cần đăng nhập để bình luận.";
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔒 Cần đăng nhập")
                .setMessage(msg + "\nBạn muốn đăng nhập hay đăng ký tài khoản mới?")
                .setPositiveButton("Đăng nhập", (d, w) -> {
                    Intent i = new Intent(this, LoginActivity.class);
                    i.putExtra("redirect_comment", true);
                    startActivityForResult(i, RC_LOGIN);
                })
                .setNegativeButton("Đăng ký", (d, w) -> {
                    Intent i = new Intent(this,
                            com.example.apptapchikhoakhoc.register.RegisterActivity.class);
                    i.putExtra("redirect_comment", true);
                    startActivityForResult(i, RC_LOGIN);
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_LOGIN && resultCode == RESULT_OK) {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String email = prefs.getString(KEY_EMAIL, "");
            currentUserEmail = email != null ? email : "";
            // Sau khi đăng nhập → load lại reaction
            if (!currentUserEmail.isEmpty()) loadSavedReaction();
        }
    }

    // ══════════════════════════════════════════════════════════
    //  COMMENT BOTTOM SHEET
    // ══════════════════════════════════════════════════════════

    private void showCommentBottomSheet(String userEmail) {
        BottomSheetDialog sheet  = new BottomSheetDialog(this);
        View              sheetV = getLayoutInflater().inflate(R.layout.bottom_sheet_comment, null);
        sheet.setContentView(sheetV);
        if (sheet.getWindow() != null) sheet.getWindow().setDimAmount(0.4f);

        SharedPreferences prefs    = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String            userName = prefs.getString(KEY_NAME, "");
        if (userName == null || userName.isEmpty()) userName = db.getUserName(userEmail);
        final String finalName  = (userName == null || userName.isEmpty()) ? "Người dùng" : userName;
        final String finalEmail = userEmail;

        TextView     tvCount               = sheetV.findViewById(R.id.tvCommentCount);
        ImageView    btnClose              = sheetV.findViewById(R.id.btnCloseComment);
        RecyclerView recycler              = sheetV.findViewById(R.id.recyclerComments);
        TextView     tvEmpty               = sheetV.findViewById(R.id.tvNoComments);
        TextView     tvAvatar              = sheetV.findViewById(R.id.tvMyAvatar);
        EditText     etComment             = sheetV.findViewById(R.id.etComment);
        ImageView    btnSend               = sheetV.findViewById(R.id.btnSendComment);
        View         bottomSheetRoot       = sheetV.findViewById(R.id.bottomSheetRoot);
        View         sheetHeader           = sheetV.findViewById(R.id.sheetHeader);
        View         inputLayout           = sheetV.findViewById(R.id.inputLayout);
        View         commentInputContainer = sheetV.findViewById(R.id.commentInputContainer);
        View         dividerHeader         = sheetV.findViewById(R.id.dividerHeader);
        View         dividerInput          = sheetV.findViewById(R.id.dividerInput);
        View         handleBar             = sheetV.findViewById(R.id.handleBar);

        int bgSheet   = isDarkMode ? ThemeManager.DarkColors.BACKGROUND     : Color.WHITE;
        int bgInput   = isDarkMode ? ThemeManager.DarkColors.BACKGROUND     : Color.parseColor("#F0F2F5");
        int divColor  = isDarkMode ? ThemeManager.DarkColors.DIVIDER        : Color.parseColor("#E4E6EB");
        int handleClr = isDarkMode ? ThemeManager.DarkColors.DIVIDER        : Color.parseColor("#CCCCCC");
        int txtSheet  = isDarkMode ? Color.WHITE                            : Color.parseColor("#1C1E21");
        int hintColor = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#8A8D91");
        int accent    = isDarkMode ? ThemeManager.DarkColors.ACCENT         : Color.parseColor("#FF2E7D");
        int iconColor = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#606770");

        if (bottomSheetRoot       != null) bottomSheetRoot.setBackgroundColor(bgSheet);
        if (sheetHeader           != null) sheetHeader.setBackgroundColor(bgSheet);
        if (inputLayout           != null) inputLayout.setBackgroundColor(bgSheet);
        if (recycler              != null) recycler.setBackgroundColor(bgSheet);
        if (commentInputContainer != null) commentInputContainer.setBackgroundColor(bgInput);
        if (dividerHeader         != null) dividerHeader.setBackgroundColor(divColor);
        if (dividerInput          != null) dividerInput.setBackgroundColor(divColor);
        if (handleBar             != null) handleBar.setBackgroundColor(handleClr);
        if (tvCount               != null) tvCount.setTextColor(txtSheet);
        if (tvEmpty               != null) tvEmpty.setTextColor(hintColor);
        if (btnClose              != null) btnClose.setColorFilter(iconColor);
        if (btnSend               != null) btnSend.setColorFilter(accent);
        if (etComment             != null) {
            etComment.setHintTextColor(hintColor);
            etComment.setTextColor(txtSheet);
            etComment.setBackgroundColor(Color.TRANSPARENT);
        }
        if (tvAvatar != null) tvAvatar.setText(String.valueOf(finalName.charAt(0)).toUpperCase());

        List<Comment>  comments = db.getCommentsByArticle(article.getId());
        CommentAdapter adapter  = new CommentAdapter();
        adapter.setList(comments);
        adapter.setReplyListener(new CommentAdapter.OnReplyListener() {
            @Override public void onSendReply(Comment parent, String replyText, int position) {
                db.addComment(article.getId(), finalName, finalEmail,
                        "@" + parent.getUserName() + " " + replyText, System.currentTimeMillis());
            }
            @Override public String getMyAvatarLetter() {
                return String.valueOf(finalName.charAt(0)).toUpperCase();
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        updateCommentEmptyState(recycler, tvEmpty, tvCount, comments.size());

        if (etComment != null) {
            etComment.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                    if (btnSend != null)
                        btnSend.setVisibility(s.toString().trim().isEmpty() ? View.GONE : View.VISIBLE);
                }
            });
        }

        if (btnClose != null) btnClose.setOnClickListener(v -> sheet.dismiss());

        if (btnSend != null) btnSend.setOnClickListener(v -> {
            if (etComment == null) return;
            String text = etComment.getText().toString().trim();
            if (TextUtils.isEmpty(text)) return;
            long    ts = System.currentTimeMillis();
            boolean ok = db.addComment(article.getId(), finalName, finalEmail, text, ts);
            if (ok) {
                adapter.addComment(new Comment(0, article.getId(), finalName, finalEmail, text, ts));
                recycler.scrollToPosition(0);
                etComment.setText("");
                hideKeyboard(etComment);
                int count = db.getCommentsCount(article.getId());
                updateCommentEmptyState(recycler, tvEmpty, tvCount, count);
                updateCommentBarExternal(count);
                Toast.makeText(this, "Đã gửi bình luận", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gửi thất bại, thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        sheet.show();
        if (etComment != null) {
            etComment.requestFocus();
            handler.postDelayed(() -> showKeyboard(etComment), 300);
        }
    }

    private void updateCommentEmptyState(RecyclerView rv, TextView tvEmpty,
                                         TextView tvCount, int count) {
        if (tvCount != null) tvCount.setText("Bình luận (" + count + ")");
        if (rv      != null) rv.setVisibility(count == 0 ? View.GONE    : View.VISIBLE);
        if (tvEmpty != null) tvEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
    }

    private void updateCommentBarExternal(int count) {
        if (tvCommentCountBar != null) tvCommentCountBar.setText(count + " bình luận");
        if (count > 0) {
            if (layoutReactionCount != null) layoutReactionCount.setVisibility(View.VISIBLE);
            if (dividerReaction     != null) dividerReaction.setVisibility(View.VISIBLE);
        }
    }

    private void showKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // ══════════════════════════════════════════════════════════
    //  REACTION
    // ══════════════════════════════════════════════════════════

    private void loadSavedReaction() {
        if (article == null) return;
        reactionCountMap.clear();
        String savedEmoji  = null;
        if (!currentUserEmail.isEmpty()) savedEmoji = db.getUserReaction(article.getId(), currentUserEmail);
        int totalReactions = db.getReactionCount(article.getId());
        if (totalReactions > 0) {
            if (savedEmoji != null) {
                int others = totalReactions - 1;
                reactionCountMap.put(savedEmoji, 1);
                if (others > 0) {
                    if (!"👍".equals(savedEmoji)) reactionCountMap.put("👍", others);
                    else reactionCountMap.put("👍", totalReactions);
                }
            } else {
                reactionCountMap.put("👍", totalReactions);
            }
        }
        if (savedEmoji != null) { articleReaction = savedEmoji; applyLikeButton(savedEmoji); }
        else { articleReaction = null; applyLikeButton(null); }
        int commentCount = db.getCommentsCount(article.getId());
        refreshReactionBar(commentCount);
    }

    private void showReactionPopup(int commentCount) {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        View popup = inflater.inflate(R.layout.reaction_popup, null);
        if (isDarkMode) popup.setBackgroundColor(ThemeManager.DarkColors.CARD_BACKGROUND);
        android.widget.PopupWindow pw = new android.widget.PopupWindow(
                popup, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pw.setElevation(12f);
        pw.setOutsideTouchable(true);
        pw.showAsDropDown(layoutLike, 0, -200, android.view.Gravity.START);
        int[]    ids    = { R.id.btnReactLike, R.id.btnReactLove, R.id.btnReactHaha, R.id.btnReactWow, R.id.btnReactSad, R.id.btnReactAngry };
        String[] emojis = { "👍", "❤️", "😆", "😮", "😢", "😡" };
        for (int i = 0; i < ids.length; i++) {
            final int idx = i;
            TextView btn = popup.findViewById(ids[i]);
            if (btn == null) continue;
            btn.setScaleX(0.3f); btn.setScaleY(0.3f); btn.setAlpha(0f);
            handler.postDelayed(() -> btn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(150).start(), idx * 50L);
            if (emojis[i].equals(articleReaction)) { btn.setScaleX(1.3f); btn.setScaleY(1.3f); }
            btn.setOnClickListener(v -> {
                if (emojis[idx].equals(articleReaction)) {
                    db.removeReactionFromDb(article.getId(), currentUserEmail);
                    removeReactionLocal(articleReaction);
                    articleReaction = null; applyLikeButton(null);
                } else {
                    if (articleReaction != null) removeReactionLocal(articleReaction);
                    articleReaction = emojis[idx];
                    db.addOrUpdateReaction(article.getId(), currentUserEmail, emojis[idx]);
                    addReactionLocal(emojis[idx]); applyLikeButton(emojis[idx]);
                }
                refreshReactionBar(commentCount); pw.dismiss();
            });
            btn.setOnTouchListener((v, event) -> {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN)
                    v.animate().scaleX(1.4f).scaleY(1.4f).setDuration(100).start();
                else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                        || event.getAction() == android.view.MotionEvent.ACTION_CANCEL)
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                return false;
            });
        }
    }

    private void applyLikeButton(String emoji) {
        int defaultColor = isDarkMode ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#606770");
        if (emoji == null) {
            imgLike.setColorFilter(defaultColor);
            tvLike.setText("Thích");
            tvLike.setTextColor(defaultColor);
        } else {
            int c = getLikeColor(emoji);
            tvLike.setText(emoji + " " + getLikeLabel(emoji));
            tvLike.setTextColor(c);
            imgLike.setColorFilter(c);
        }
    }

    private String getLikeLabel(String e) {
        switch (e) {
            case "👍": return "Thích";    case "❤️": return "Yêu thích";
            case "😆": return "Haha";     case "😮": return "Wow";
            case "😢": return "Buồn";     case "😡": return "Phẫn nộ";
            default:   return "Thích";
        }
    }

    private int getLikeColor(String e) {
        switch (e) {
            case "👍": return Color.parseColor("#1877F2");
            case "❤️": return Color.parseColor("#F33E58");
            case "😆": case "😮": case "😢": return Color.parseColor("#F7B125");
            case "😡": return Color.parseColor("#E9710F");
            default:   return Color.parseColor("#1877F2");
        }
    }

    private void addReactionLocal(String emoji) {
        reactionCountMap.put(emoji, reactionCountMap.getOrDefault(emoji, 0) + 1);
    }

    private void removeReactionLocal(String emoji) {
        int c = reactionCountMap.getOrDefault(emoji, 0);
        if (c <= 1) reactionCountMap.remove(emoji); else reactionCountMap.put(emoji, c - 1);
    }

    private void refreshReactionBar(int commentCount) {
        int total = 0;
        for (int v : reactionCountMap.values()) total += v;
        if (total == 0 && commentCount == 0) {
            layoutReactionCount.setVisibility(View.GONE);
            dividerReaction.setVisibility(View.GONE);
            return;
        }
        layoutReactionCount.setVisibility(View.VISIBLE);
        dividerReaction.setVisibility(View.VISIBLE);
        layoutReactionCount.setBackgroundColor(isDarkMode
                ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE);
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        if (articleReaction != null && reactionCountMap.containsKey(articleReaction)) {
            sb.append(articleReaction); shown++;
        }
        for (String e : reactionCountMap.keySet()) {
            if (shown >= 3) break;
            if (!e.equals(articleReaction)) { sb.append(e); shown++; }
        }
        tvReactionEmojis.setText(sb.toString());
        tvReactionTotal.setText(total > 0 ? String.valueOf(total) : "");
        tvCommentCountBar.setText(commentCount > 0 ? commentCount + " bình luận" : "");
    }

    // ══════════════════════════════════════════════════════════
    //  VIDEO PLAYER
    // ══════════════════════════════════════════════════════════

    private void setupCustomVideoControls() {
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            mp.setVideoScalingMode(isFullscreen
                    ? MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                    : MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            progressBar.setVisibility(View.GONE);
            int dur = mp.getDuration();
            seekBar.setMax(dur); tvTotalTime.setText(formatTime(dur));
            videoControlsLayout.setVisibility(View.VISIBLE);
            if (currentPosition > 0) videoView.seekTo(currentPosition);
            handler.post(updateSeekBarRunnable);
        });
        videoView.setOnCompletionListener(mp -> {
            isPlaying = false; currentPosition = 0;
            updatePlayPauseButton(); seekBar.setProgress(0);
            tvCurrentTime.setText("00:00"); showThumbnail();
            handler.removeCallbacks(updateSeekBarRunnable);
            Toast.makeText(this, "Video đã phát xong", Toast.LENGTH_SHORT).show();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "Lỗi khi phát video", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); showThumbnail(); return true;
        });
        btnPlayPause.setOnClickListener(v -> { if (isPlaying) pauseVideo(); else playVideo(); });
        btnFullscreen.setOnClickListener(v -> {
            isUserTriggeredFullscreen = true;
            if (isFullscreen) exitFullscreen(); else enterFullscreen();
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser) {
                if (fromUser) { videoView.seekTo(p); currentPosition = p; tvCurrentTime.setText(formatTime(p)); }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { handler.removeCallbacks(updateSeekBarRunnable); }
            @Override public void onStopTrackingTouch(SeekBar sb)  { handler.post(updateSeekBarRunnable); }
        });
        thumbnailContainer.setOnClickListener(v -> playVideoInline());
        videoView.setOnClickListener(v -> {
            if (videoControlsLayout.getVisibility() == View.VISIBLE)
                videoControlsLayout.setVisibility(View.GONE);
            else { videoControlsLayout.setVisibility(View.VISIBLE); autoHideControls(); }
        });
    }

    private final Runnable updateSeekBarRunnable = new Runnable() {
        @Override public void run() {
            if (videoView != null && isPlaying) {
                currentPosition = videoView.getCurrentPosition();
                seekBar.setProgress(currentPosition);
                tvCurrentTime.setText(formatTime(currentPosition));
                handler.postDelayed(this, 100);
            }
        }
    };

    private final Runnable hideControlsRunnable = new Runnable() {
        @Override public void run() {
            if (isPlaying && videoControlsLayout.getVisibility() == View.VISIBLE)
                videoControlsLayout.setVisibility(View.GONE);
        }
    };

    private void playVideoInline() {
        thumbnailContainer.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoControlsLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        playVideo();
    }

    private void playVideo()  { videoView.start(); isPlaying = true; updatePlayPauseButton(); handler.post(updateSeekBarRunnable); autoHideControls(); }
    private void pauseVideo() { videoView.pause(); isPlaying = false; currentPosition = videoView.getCurrentPosition(); updatePlayPauseButton(); handler.removeCallbacks(updateSeekBarRunnable); handler.removeCallbacks(hideControlsRunnable); }
    private void updatePlayPauseButton() { btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play); }
    private void showThumbnail() { videoView.setVisibility(View.GONE); thumbnailContainer.setVisibility(View.VISIBLE); videoControlsLayout.setVisibility(View.GONE); isPlaying = false; }
    private void autoHideControls() { handler.removeCallbacks(hideControlsRunnable); handler.postDelayed(hideControlsRunnable, 3000); }

    private void enterFullscreen() {
        if (isFullscreen) return;
        isFullscreen = true;
        if (videoView.isPlaying()) currentPosition = videoView.getCurrentPosition();
        try {
            originalParent = (ViewGroup) cardVideo.getParent();
            if (originalParent != null) {
                originalIndex        = originalParent.indexOfChild(cardVideo);
                originalLayoutParams = cardVideo.getLayoutParams();
                originalParent.removeView(cardVideo);
            }
            originalContainerLayoutParams = videoContainer.getLayoutParams();
            originalVideoLayoutParams     = videoView.getLayoutParams();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (getSupportActionBar() != null) getSupportActionBar().hide();
            cardVideo.setRadius(0f); cardVideo.setCardElevation(0f);
            cardVideo.setPreventCornerOverlap(false); cardVideo.setUseCompatPadding(false);
            cardVideo.setContentPadding(0,0,0,0); cardVideo.setPadding(0,0,0,0);
            cardVideo.setCardBackgroundColor(Color.BLACK);
            ViewGroup root = (ViewGroup) getWindow().getDecorView();
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            cardVideo.setLayoutParams(p); root.addView(cardVideo);
            videoContainer.setLayoutParams(new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            videoContainer.setPadding(0,0,0,0); videoContainer.setBackgroundColor(Color.BLACK);
            FrameLayout.LayoutParams vp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            vp.gravity = android.view.Gravity.CENTER; videoView.setLayoutParams(vp);
            if (currentVideoUri != null) {
                int sp = currentPosition; boolean wp = isPlaying;
                videoView.stopPlayback(); videoView.setVideoURI(null);
                handler.postDelayed(() -> {
                    videoView.setVideoURI(currentVideoUri);
                    videoView.setOnPreparedListener(mp -> {
                        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                        mp.setLooping(false); progressBar.setVisibility(View.GONE);
                        seekBar.setMax(mp.getDuration()); tvTotalTime.setText(formatTime(mp.getDuration()));
                        videoControlsLayout.setVisibility(View.VISIBLE); videoView.seekTo(sp);
                        if (wp) { videoView.start(); isPlaying = true; updatePlayPauseButton(); handler.post(updateSeekBarRunnable); }
                    });
                }, 100);
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
        } catch (Exception e) { e.printStackTrace(); isFullscreen = false; }
    }

    private void exitFullscreen() {
        if (!isFullscreen) return;
        isFullscreen = false;
        if (videoView.isPlaying()) currentPosition = videoView.getCurrentPosition();
        try {
            ((ViewGroup) getWindow().getDecorView()).removeView(cardVideo);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (getSupportActionBar() != null) getSupportActionBar().show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(isDarkMode ? ThemeManager.DarkColors.STATUS_BAR : Color.parseColor("#8E24AA"));
            float d = getResources().getDisplayMetrics().density;
            cardVideo.setRadius(12 * d); cardVideo.setCardElevation(4 * d);
            cardVideo.setUseCompatPadding(true); cardVideo.setPreventCornerOverlap(true);
            if (originalContainerLayoutParams != null) videoContainer.setLayoutParams(originalContainerLayoutParams);
            if (originalVideoLayoutParams     != null) videoView.setLayoutParams(originalVideoLayoutParams);
            if (originalLayoutParams          != null) cardVideo.setLayoutParams(originalLayoutParams);
            if (originalParent                != null) originalParent.addView(cardVideo, originalIndex);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            btnFullscreen.setImageResource(R.drawable.ic_fullscreen);
            if (currentVideoUri != null) {
                int sp = currentPosition; boolean wp = isPlaying;
                videoView.stopPlayback(); videoView.setVideoURI(null);
                handler.postDelayed(() -> {
                    videoView.setVideoURI(currentVideoUri);
                    videoView.setOnPreparedListener(mp -> {
                        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                        mp.setLooping(false); progressBar.setVisibility(View.GONE);
                        seekBar.setMax(mp.getDuration()); tvTotalTime.setText(formatTime(mp.getDuration()));
                        videoControlsLayout.setVisibility(View.VISIBLE); videoView.seekTo(sp);
                        if (wp) { videoView.start(); isPlaying = true; updatePlayPauseButton(); handler.post(updateSeekBarRunnable); }
                    });
                }, 100);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String formatTime(int ms) {
        return String.format("%02d:%02d", (ms / (1000 * 60)) % 60, (ms / 1000) % 60);
    }

    // ══════════════════════════════════════════════════════════
    //  RELATED NEWS
    // ══════════════════════════════════════════════════════════

    private void loadRelatedNews() {
        if (article == null || article.getCategory() == null) return;
        List<Article> related  = db.getArticlesByCategory(article.getCategory());
        List<Article> filtered = new ArrayList<>();
        for (Article a : related) if (a.getId() != article.getId()) filtered.add(a);
        if (!filtered.isEmpty()) {
            NewsAdapter adapter = new NewsAdapter(this);
            adapter.setList(filtered);
            recyclerRelatedNews.setLayoutManager(new LinearLayoutManager(this));
            recyclerRelatedNews.setAdapter(adapter);
        }
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm",
                java.util.Locale.getDefault()).format(new java.util.Date());
    }
}