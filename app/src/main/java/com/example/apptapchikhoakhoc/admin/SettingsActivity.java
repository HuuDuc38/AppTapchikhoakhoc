package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.main.MainActivity;
import com.example.apptapchikhoakhoc.utils.AdminLocaleManager;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

public class SettingsActivity extends BaseAdminActivity {

    // ── Prefs keys ─────────────────────────────────────────────────
    private static final String PREFS_SETTINGS        = "AdminSettings";
    private static final String KEY_NOTIF_NEW_ARTICLE = "notif_new_article";
    private static final String KEY_NOTIF_PENDING     = "notif_pending";
    private static final String KEY_NOTIF_SYSTEM      = "notif_system";

    // ── Views ───────────────────────────────────────────────────────
    private ImageView    btnBack;
    private androidx.appcompat.widget.SwitchCompat switchDarkMode;
    private LinearLayout layoutLanguage, layoutNotifications, layoutHelp, layoutAbout;
    private CardView     btnLogout;
    private TextView     tvCurrentLanguage;
    private LinearLayout rootSettingsLayout, toolbarLayout;
    private CardView     cardProfile, cardUI, cardCustom, cardSupport;
    private TextView     tvAdminName, tvAdminEmail;
    private TextView     tvSectionUI, tvSectionCustom, tvSectionSupport;
    private TextView     tvDarkModeLabel, tvDarkModeDesc;
    private TextView     tvLanguageLabel, tvNotifLabel, tvNotifDesc;
    private TextView     tvHelpLabel, tvHelpDesc;
    private TextView     tvAboutLabel, tvAboutDesc;
    private TextView     tvLogoutLabel;
    private View         dividerCustom, dividerSupport;
    private ImageView    icArrowLanguage, icArrowNotif, icArrowHelp, icArrowAbout, icLogoutArrow;

    // ═══════════════════════════════════════════════════════════════
    //  LIFECYCLE
    // ═══════════════════════════════════════════════════════════════

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settingsadmin);

        initViews();
        applySystemBarsTheme();
        applyDarkModeTheme();
        loadSettings();
        setupClickListeners();
    }

    // ═══════════════════════════════════════════════════════════════
    //  SYSTEM BARS
    // ═══════════════════════════════════════════════════════════════

    private void applySystemBarsTheme() {
        Window window = getWindow();
        if (isDarkMode) {
            int darkBg = AdminThemeManager.DarkColors.BACKGROUND;
            window.setStatusBarColor(darkBg);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                window.setNavigationBarColor(darkBg);
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

    // ═══════════════════════════════════════════════════════════════
    //  INIT VIEWS
    // ═══════════════════════════════════════════════════════════════

    private void initViews() {
        rootSettingsLayout  = findViewById(R.id.rootSettingsLayout);
        toolbarLayout       = findViewById(R.id.toolbarLayout);
        btnBack             = findViewById(R.id.btn_back);
        switchDarkMode      = findViewById(R.id.switch_dark_mode);
        layoutLanguage      = findViewById(R.id.layout_language);
        layoutNotifications = findViewById(R.id.layout_notifications);
        layoutHelp          = findViewById(R.id.layout_help);
        layoutAbout         = findViewById(R.id.layout_about);
        btnLogout           = findViewById(R.id.btn_logout);
        tvCurrentLanguage   = findViewById(R.id.tv_current_language);
        cardProfile         = findViewById(R.id.cardProfile);
        cardUI              = findViewById(R.id.cardUI);
        cardCustom          = findViewById(R.id.cardCustom);
        cardSupport         = findViewById(R.id.cardSupport);
        tvAdminName         = findViewById(R.id.tvAdminName);
        tvAdminEmail        = findViewById(R.id.tvAdminEmail);
        tvSectionUI         = findViewById(R.id.tvSectionUI);
        tvSectionCustom     = findViewById(R.id.tvSectionCustom);
        tvSectionSupport    = findViewById(R.id.tvSectionSupport);
        tvDarkModeLabel     = findViewById(R.id.tvDarkModeLabel);
        tvDarkModeDesc      = findViewById(R.id.tvDarkModeDesc);
        tvLanguageLabel     = findViewById(R.id.tvLanguageLabel);
        tvNotifLabel        = findViewById(R.id.tvNotifLabel);
        tvNotifDesc         = findViewById(R.id.tvNotifDesc);
        tvHelpLabel         = findViewById(R.id.tvHelpLabel);
        tvHelpDesc          = findViewById(R.id.tvHelpDesc);
        tvAboutLabel        = findViewById(R.id.tvAboutLabel);
        tvAboutDesc         = findViewById(R.id.tvAboutDesc);
        tvLogoutLabel       = findViewById(R.id.tvLogoutLabel);
        dividerCustom       = findViewById(R.id.dividerCustom);
        dividerSupport      = findViewById(R.id.dividerSupport);
        icArrowLanguage     = findViewById(R.id.icArrowLanguage);
        icArrowNotif        = findViewById(R.id.icArrowNotif);
        icArrowHelp         = findViewById(R.id.icArrowHelp);
        icArrowAbout        = findViewById(R.id.icArrowAbout);
        icLogoutArrow       = findViewById(R.id.icLogoutArrow);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DARK MODE THEME
    // ═══════════════════════════════════════════════════════════════

    private void applyDarkModeTheme() {
        int bgPage   = isDarkMode ? AdminThemeManager.DarkColors.BACKGROUND      : Color.parseColor("#F5F5F5");
        int bgCard   = isDarkMode ? AdminThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int txtPri   = isDarkMode ? AdminThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSec   = isDarkMode ? AdminThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#757575");
        int txtSect  = isDarkMode ? AdminThemeManager.DarkColors.TEXT_TERTIARY   : Color.parseColor("#9E9E9E");
        int divColor = isDarkMode ? AdminThemeManager.DarkColors.DIVIDER         : Color.parseColor("#F0F0F0");
        int arrowClr = isDarkMode ? AdminThemeManager.DarkColors.TEXT_TERTIARY   : Color.parseColor("#BDBDBD");

        if (rootSettingsLayout != null) rootSettingsLayout.setBackgroundColor(bgPage);
        if (toolbarLayout != null && isDarkMode)
            toolbarLayout.setBackgroundColor(AdminThemeManager.DarkColors.STATUS_BAR);

        setCardBg(cardProfile, bgCard);
        setCardBg(cardUI,      bgCard);
        setCardBg(cardCustom,  bgCard);
        setCardBg(cardSupport, bgCard);
        setCardBg(btnLogout,   bgCard);

        setTv(tvAdminName,     txtPri);  setTv(tvAdminEmail,      txtSec);
        setTv(tvSectionUI,     txtSect); setTv(tvSectionCustom,   txtSect); setTv(tvSectionSupport, txtSect);
        setTv(tvDarkModeLabel, txtPri);  setTv(tvDarkModeDesc,    txtSec);
        setTv(tvLanguageLabel, txtPri);  setTv(tvCurrentLanguage, txtSec);
        setTv(tvNotifLabel,    txtPri);  setTv(tvNotifDesc,       txtSec);
        setTv(tvHelpLabel,     txtPri);  setTv(tvHelpDesc,        txtSec);
        setTv(tvAboutLabel,    txtPri);  setTv(tvAboutDesc,       txtSec);
        // tvLogoutLabel giữ màu đỏ

        if (dividerCustom  != null) dividerCustom.setBackgroundColor(divColor);
        if (dividerSupport != null) dividerSupport.setBackgroundColor(divColor);

        if (icArrowLanguage != null) icArrowLanguage.setColorFilter(arrowClr);
        if (icArrowNotif    != null) icArrowNotif.setColorFilter(arrowClr);
        if (icArrowHelp     != null) icArrowHelp.setColorFilter(arrowClr);
        if (icArrowAbout    != null) icArrowAbout.setColorFilter(arrowClr);
    }

    private void setCardBg(CardView c, int color) { if (c != null) c.setCardBackgroundColor(color); }
    private void setTv(TextView tv, int color)    { if (tv != null) tv.setTextColor(color); }

    // ═══════════════════════════════════════════════════════════════
    //  LOAD SETTINGS
    // ═══════════════════════════════════════════════════════════════

    private void loadSettings() {
        switchDarkMode.setChecked(AdminThemeManager.isDarkMode(this));
        String lang = AdminLocaleManager.getSavedLanguage(this);
        if (tvCurrentLanguage != null)
            tvCurrentLanguage.setText(AdminLocaleManager.LANG_VI.equals(lang)
                    ? getString(R.string.language_vietnamese)
                    : getString(R.string.language_english));
    }

    // ═══════════════════════════════════════════════════════════════
    //  CLICK LISTENERS
    // ═══════════════════════════════════════════════════════════════

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchDarkMode.setOnCheckedChangeListener((btn, isChecked) -> {
            AdminThemeManager.setDarkMode(this, isChecked);
            Toast.makeText(this,
                    isChecked ? getString(R.string.dark_mode_on)
                            : getString(R.string.dark_mode_off),
                    Toast.LENGTH_SHORT).show();
            recreate();
        });

        layoutLanguage.setOnClickListener(v      -> showLanguageDialog());
        layoutNotifications.setOnClickListener(v -> showNotificationsDialog());
        layoutHelp.setOnClickListener(v          -> showHelpDialog());
        layoutAbout.setOnClickListener(v         -> showAboutDialog());
        btnLogout.setOnClickListener(v           -> showLogoutDialog());
    }

    // ═══════════════════════════════════════════════════════════════
    //  1. DIALOG NGÔN NGỮ
    // ═══════════════════════════════════════════════════════════════

    private void showLanguageDialog() {
        String currentLang = AdminLocaleManager.getSavedLanguage(this);
        int checkedItem = AdminLocaleManager.LANG_VI.equals(currentLang) ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_language_title))
                .setSingleChoiceItems(
                        new String[]{
                                getString(R.string.language_vietnamese),
                                getString(R.string.language_english)
                        },
                        checkedItem,
                        (dialog, which) -> {
                            String newLang = which == 0
                                    ? AdminLocaleManager.LANG_VI
                                    : AdminLocaleManager.LANG_EN;
                            dialog.dismiss();
                            if (!newLang.equals(currentLang))
                                applyAndSaveAdminLanguage(newLang); // từ BaseAdminActivity
                        })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .show();
    }

    // ═══════════════════════════════════════════════════════════════
    //  2. DIALOG THÔNG BÁO
    // ═══════════════════════════════════════════════════════════════

    private void showNotificationsDialog() {
        int bgCard = isDarkMode ? AdminThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int txtPri = isDarkMode ? AdminThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSec = isDarkMode ? AdminThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#757575");
        int accent = isDarkMode ? AdminThemeManager.DarkColors.ACCENT          : Color.parseColor("#C8463D");
        int divClr = isDarkMode ? AdminThemeManager.DarkColors.DIVIDER         : Color.parseColor("#F0F0F0");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(8), dp(24), dp(8));
        root.setBackgroundColor(bgCard);

        boolean notifNew     = prefs().getBoolean(KEY_NOTIF_NEW_ARTICLE, true);
        boolean notifPending = prefs().getBoolean(KEY_NOTIF_PENDING,     true);
        boolean notifSystem  = prefs().getBoolean(KEY_NOTIF_SYSTEM,      true);

        androidx.appcompat.widget.SwitchCompat swNew = buildNotifRow(
                root, getString(R.string.notif_new_article), getString(R.string.notif_new_article_desc),
                notifNew, txtPri, txtSec, accent, divClr);
        androidx.appcompat.widget.SwitchCompat swPending = buildNotifRow(
                root, getString(R.string.notif_pending), getString(R.string.notif_pending_desc),
                notifPending, txtPri, txtSec, accent, divClr);
        androidx.appcompat.widget.SwitchCompat swSystem = buildNotifRow(
                root, getString(R.string.notif_system), getString(R.string.notif_system_desc),
                notifSystem, txtPri, txtSec, accent, divClr);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.notification_title))
                .setView(root)
                .setPositiveButton(getString(R.string.btn_save), (d, w) -> {
                    prefs().edit()
                            .putBoolean(KEY_NOTIF_NEW_ARTICLE, swNew.isChecked())
                            .putBoolean(KEY_NOTIF_PENDING,     swPending.isChecked())
                            .putBoolean(KEY_NOTIF_SYSTEM,      swSystem.isChecked())
                            .apply();
                    Toast.makeText(this, getString(R.string.notif_saved), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .create();

        styleDialog(dialog, bgCard, txtPri, accent);
        dialog.show();
    }

    private androidx.appcompat.widget.SwitchCompat buildNotifRow(
            LinearLayout parent, String title, String subtitle,
            boolean checked, int txtPri, int txtSec, int accent, int divClr) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(14), 0, dp(14));
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        LinearLayout textGroup = new LinearLayout(this);
        textGroup.setOrientation(LinearLayout.VERTICAL);
        textGroup.setLayoutParams(
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(txtPri);
        tvTitle.setTextSize(15);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(txtSec);
        tvSub.setTextSize(13);
        tvSub.setPadding(0, dp(2), 0, 0);

        textGroup.addView(tvTitle);
        textGroup.addView(tvSub);

        androidx.appcompat.widget.SwitchCompat sw = new androidx.appcompat.widget.SwitchCompat(this);
        sw.setChecked(checked);
        sw.setThumbTintList(android.content.res.ColorStateList.valueOf(accent));
        sw.setTrackTintList(android.content.res.ColorStateList.valueOf(
                isDarkMode ? Color.parseColor("#2E3D4F") : Color.parseColor("#E0E0E0")));

        row.addView(textGroup);
        row.addView(sw);
        parent.addView(row);

        View div = new View(this);
        div.setBackgroundColor(divClr);
        parent.addView(div, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        return sw;
    }

    // ═══════════════════════════════════════════════════════════════
    //  3. DIALOG TRỢ GIÚP
    // ═══════════════════════════════════════════════════════════════

    private void showHelpDialog() {
        int bgCard = isDarkMode ? AdminThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int txtPri = isDarkMode ? AdminThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSec = isDarkMode ? AdminThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#757575");
        int accent = isDarkMode ? AdminThemeManager.DarkColors.ACCENT          : Color.parseColor("#C8463D");
        int divClr = isDarkMode ? AdminThemeManager.DarkColors.DIVIDER         : Color.parseColor("#F0F0F0");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(8), dp(24), dp(8));
        root.setBackgroundColor(bgCard);

        String[][] faqs = {
                {getString(R.string.faq_q1), getString(R.string.faq_a1)},
                {getString(R.string.faq_q2), getString(R.string.faq_a2)},
                {getString(R.string.faq_q3), getString(R.string.faq_a3)},
                {getString(R.string.faq_q4), getString(R.string.faq_a4)},
                {getString(R.string.faq_q5), getString(R.string.faq_a5)},
        };
        for (String[] faq : faqs)
            addFaqItem(root, faq[0], faq[1], txtPri, txtSec, accent, divClr);

        TextView tvContact = new TextView(this);
        tvContact.setText(getString(R.string.help_contact));
        tvContact.setTextColor(accent);
        tvContact.setTextSize(13);
        tvContact.setPadding(0, dp(12), 0, dp(4));
        tvContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:support@tapchikhoakhoc.edu.vn"));
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.help_email_subject));
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
            else
                Toast.makeText(this, getString(R.string.help_no_email_app), Toast.LENGTH_SHORT).show();
        });
        root.addView(tvContact);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.help_title))
                .setView(root)
                .setPositiveButton(getString(R.string.dialog_close), null)
                .create();

        styleDialog(dialog, bgCard, txtPri, accent);
        dialog.show();
    }

    private void addFaqItem(LinearLayout parent, String question, String answer,
                            int txtPri, int txtSec, int accent, int divClr) {
        TextView tvQ = new TextView(this);
        tvQ.setText(question);
        tvQ.setTextColor(txtPri);
        tvQ.setTextSize(14);
        tvQ.setTypeface(null, android.graphics.Typeface.BOLD);
        tvQ.setPadding(0, dp(10), 0, dp(4));

        TextView tvA = new TextView(this);
        tvA.setText(answer);
        tvA.setTextColor(txtSec);
        tvA.setTextSize(13);
        tvA.setPadding(dp(8), 0, 0, dp(6));
        tvA.setVisibility(View.GONE);

        tvQ.setOnClickListener(v -> {
            if (tvA.getVisibility() == View.VISIBLE) {
                tvA.setVisibility(View.GONE);
                tvQ.setTextColor(txtPri);
            } else {
                tvA.setVisibility(View.VISIBLE);
                tvQ.setTextColor(accent);
            }
        });

        View div = new View(this);
        div.setBackgroundColor(divClr);
        LinearLayout.LayoutParams divLp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divLp.topMargin = dp(6);

        parent.addView(tvQ);
        parent.addView(tvA);
        parent.addView(div, divLp);
    }

    // ═══════════════════════════════════════════════════════════════
    //  4. DIALOG VỀ CHÚNG TÔI
    // ═══════════════════════════════════════════════════════════════

    private void showAboutDialog() {
        int bgCard = isDarkMode ? AdminThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int txtPri = isDarkMode ? AdminThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int txtSec = isDarkMode ? AdminThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#757575");
        int accent = isDarkMode ? AdminThemeManager.DarkColors.ACCENT          : Color.parseColor("#C8463D");
        int divClr = isDarkMode ? AdminThemeManager.DarkColors.DIVIDER         : Color.parseColor("#F0F0F0");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(16), dp(24), dp(8));
        root.setBackgroundColor(bgCard);

        TextView tvLogo = new TextView(this);
        tvLogo.setText("📚");
        tvLogo.setTextSize(48);
        tvLogo.setGravity(android.view.Gravity.CENTER);
        tvLogo.setPadding(0, 0, 0, dp(8));
        root.addView(tvLogo);

        TextView tvAppName = new TextView(this);
        tvAppName.setText(getString(R.string.app_name));
        tvAppName.setTextColor(txtPri);
        tvAppName.setTextSize(20);
        tvAppName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvAppName.setGravity(android.view.Gravity.CENTER);
        root.addView(tvAppName);

        TextView tvSlogan = new TextView(this);
        tvSlogan.setText(getString(R.string.about_slogan));
        tvSlogan.setTextColor(accent);
        tvSlogan.setTextSize(13);
        tvSlogan.setGravity(android.view.Gravity.CENTER);
        tvSlogan.setPadding(0, dp(4), 0, dp(16));
        root.addView(tvSlogan);

        View div1 = new View(this);
        div1.setBackgroundColor(divClr);
        root.addView(div1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        String[][] infos = {
                {getString(R.string.about_version_label), getString(R.string.about_version_value)},
                {getString(R.string.about_org_label),     getString(R.string.about_org_value)},
                {getString(R.string.about_website_label), getString(R.string.about_website_value)},
                {getString(R.string.about_email_label),   getString(R.string.about_email_value)},
                {getString(R.string.about_phone_label),   getString(R.string.about_phone_value)},
                {getString(R.string.about_address_label), getString(R.string.about_address_value)},
        };
        for (String[] info : infos)
            addInfoRow(root, info[0], info[1], txtPri, txtSec, divClr);

        View div2 = new View(this);
        div2.setBackgroundColor(divClr);
        root.addView(div2, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));

        TextView tvCopy = new TextView(this);
        tvCopy.setText(getString(R.string.about_copyright));
        tvCopy.setTextColor(txtSec);
        tvCopy.setTextSize(11);
        tvCopy.setGravity(android.view.Gravity.CENTER);
        tvCopy.setPadding(0, dp(12), 0, dp(4));
        root.addView(tvCopy);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_about_title))
                .setView(root)
                .setPositiveButton(getString(R.string.dialog_close), null)
                .create();

        styleDialog(dialog, bgCard, txtPri, accent);
        dialog.show();
    }

    private void addInfoRow(LinearLayout parent, String label, String value,
                            int txtPri, int txtSec, int divClr) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, dp(10));

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(txtSec);
        tvLabel.setTextSize(13);
        tvLabel.setLayoutParams(
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.45f));

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setTextColor(txtPri);
        tvValue.setTextSize(13);
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        tvValue.setLayoutParams(
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.55f));

        row.addView(tvLabel);
        row.addView(tvValue);
        parent.addView(row);

        View div = new View(this);
        div.setBackgroundColor(divClr);
        parent.addView(div, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
    }

    // ═══════════════════════════════════════════════════════════════
    //  5. DIALOG ĐĂNG XUẤT
    // ═══════════════════════════════════════════════════════════════

    private void showLogoutDialog() {
        int bgCard = isDarkMode ? AdminThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int txtPri = isDarkMode ? AdminThemeManager.DarkColors.TEXT_PRIMARY    : Color.parseColor("#1A1A1A");
        int accent = isDarkMode ? AdminThemeManager.DarkColors.ACCENT_DANGER   : Color.parseColor("#E53935");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_logout_title))
                .setMessage(getString(R.string.dialog_admin_logout_message))
                .setPositiveButton(getString(R.string.logout), (d, w) -> {
                    getSharedPreferences("AdminSession", MODE_PRIVATE)
                            .edit().clear().apply();
                    Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null)
                .create();

        styleDialog(dialog, bgCard, txtPri, accent);
        dialog.show();
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER — Style dialog
    // ═══════════════════════════════════════════════════════════════

    private void styleDialog(AlertDialog dialog, int bgCard, int txtPri, int accent) {
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null)
                dialog.getWindow().getDecorView()
                        .setBackgroundColor(Color.TRANSPARENT);

            android.widget.Button btnPos = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (btnPos != null) {
                btnPos.setTextColor(accent);
                btnPos.setTypeface(null, android.graphics.Typeface.BOLD);
            }
            android.widget.Button btnNeg = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            if (btnNeg != null)
                btnNeg.setTextColor(isDarkMode
                        ? AdminThemeManager.DarkColors.TEXT_SECONDARY
                        : Color.parseColor("#757575"));

            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            View titleView = dialog.findViewById(titleId);
            if (titleView instanceof TextView)
                ((TextView) titleView).setTextColor(txtPri);

            View msgView = dialog.findViewById(android.R.id.message);
            if (msgView instanceof TextView)
                ((TextView) msgView).setTextColor(
                        isDarkMode ? AdminThemeManager.DarkColors.TEXT_SECONDARY
                                : Color.parseColor("#757575"));
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  UTILITY
    // ═══════════════════════════════════════════════════════════════

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
    }

    private int dp(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}