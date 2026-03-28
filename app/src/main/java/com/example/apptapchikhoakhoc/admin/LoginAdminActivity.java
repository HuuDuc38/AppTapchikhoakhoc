package com.example.apptapchikhoakhoc.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.utils.ThemeManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginAdminActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin;
    private CircularProgressIndicator progressLoading;
    private ImageButton btnBack;
    private TextView tvTitle, tvSubtitle, tvForgotPassword, tvContactAdmin;
    private RelativeLayout rootLayout;

    private static final String PREFS_NAME = "AdminLoginPrefs";
    private static final String KEY_EMAIL = "email";

    private SharedPreferences prefs;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private boolean isDark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyThemeOnStartup(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_admin_login);

        isDark = ThemeManager.isDarkMode(this);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        applyDarkModeTheme();
        loadSavedLogin();
        setupClickListeners();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tilEmail = findViewById(R.id.tilEmail);
        edtEmail = findViewById(R.id.edtEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressLoading = findViewById(R.id.progressLoading);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvContactAdmin = findViewById(R.id.tvContactAdmin);
    }

    private void applyDarkModeTheme() {
        int bgColor = isDark ? ThemeManager.DarkColors.BACKGROUND : Color.parseColor("#F5F7FA");
        int txtPrimary = isDark ? ThemeManager.DarkColors.TEXT_PRIMARY : Color.parseColor("#1A1A1A");
        int txtSecond = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#666666");
        int accentColor = isDark ? ThemeManager.DarkColors.ACCENT : Color.parseColor("#2D68C4");
        int backTint = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#666666");

        if (rootLayout != null) rootLayout.setBackgroundColor(bgColor);
        if (tvTitle != null) tvTitle.setTextColor(txtPrimary);
        if (tvSubtitle != null) tvSubtitle.setTextColor(txtSecond);
        if (btnBack != null) btnBack.setColorFilter(backTint);

        applyInputLayoutTheme(tilEmail);
        applyInputLayoutTheme(tilPassword);

        if (edtEmail != null) edtEmail.setTextColor(txtPrimary);
        if (edtPassword != null) edtPassword.setTextColor(txtPrimary);

        if (tvForgotPassword != null) tvForgotPassword.setTextColor(accentColor);
        if (tvContactAdmin != null) tvContactAdmin.setTextColor(accentColor);
    }

    private void applyInputLayoutTheme(TextInputLayout til) {
        if (til == null) return;

        int hintColor = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY : Color.parseColor("#666666");
        int strokeColor = isDark ? ThemeManager.DarkColors.DIVIDER : Color.parseColor("#CCCCCC");
        int focusColor = isDark ? ThemeManager.DarkColors.ACCENT : Color.parseColor("#2D68C4");
        int boxBg = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;

        til.setHintTextColor(ColorStateList.valueOf(hintColor));
        til.setBoxStrokeColorStateList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                new int[]{focusColor, strokeColor}
        ));
        til.setBoxBackgroundColor(boxBg);
        til.setEndIconTintList(ColorStateList.valueOf(hintColor));
    }

    private void loadSavedLogin() {
        String savedEmail = prefs.getString(KEY_EMAIL, "");
        if (!savedEmail.isEmpty()) {
            edtEmail.setText(savedEmail);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnLogin.setOnClickListener(v -> performLogin());

        tvForgotPassword.setOnClickListener(v ->
                Toast.makeText(this, "Contact IT: it@vinhuni.edu.vn", Toast.LENGTH_LONG).show());

        tvContactAdmin.setOnClickListener(v ->
                startActivity(new Intent(this, ContactAdminActivity.class)));
    }

    private void performLogin() {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        String pass = edtPassword.getText() == null ? "" : edtPassword.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("");
        progressLoading.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                firebaseAuth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener(authResult -> {
                            if (authResult.getUser() == null) {
                                onLoginFailed();
                                return;
                            }
                            firestore.collection("users").document(authResult.getUser().getUid()).get()
                                    .addOnSuccessListener(this::handleRoleCheck)
                                    .addOnFailureListener(e -> onLoginFailed());
                        })
                        .addOnFailureListener(e -> onLoginFailed())
                , 800);
    }

    private void handleRoleCheck(DocumentSnapshot userDoc) {
        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
        String role = userDoc != null && userDoc.contains("role")
                ? String.valueOf(userDoc.get("role"))
                : "";

        if ("admin".equalsIgnoreCase(role)) {
            completeAdminLogin(email);
            return;
        }

        if (isBootstrapAdminEmail(email)) {
            bootstrapAdminProfile(email, userDoc);
            return;
        }

        firebaseAuth.signOut();
        Toast.makeText(this, "This account does not have admin role", Toast.LENGTH_SHORT).show();
        onLoginFailed();
    }

    private boolean isBootstrapAdminEmail(String email) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isEmpty()) return false;

        String[] bootstrapEmails = getResources().getStringArray(R.array.bootstrap_admin_emails);
        for (String candidate : bootstrapEmails) {
            if (normalizedEmail.equals(candidate.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private void bootstrapAdminProfile(String email, DocumentSnapshot userDoc) {
        if (firebaseAuth.getCurrentUser() == null) {
            onLoginFailed();
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();
        String existingName = userDoc != null && userDoc.contains("name")
                ? String.valueOf(userDoc.get("name"))
                : "";
        if (existingName == null || existingName.trim().isEmpty() || "null".equalsIgnoreCase(existingName)) {
            int atIndex = email.indexOf('@');
            existingName = atIndex > 0 ? email.substring(0, atIndex) : "Admin";
        }

        long now = System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", existingName.trim());
        payload.put("email", email);
        payload.put("role", "admin");
        payload.put("updatedAt", now);
        if (userDoc == null || !userDoc.exists()) {
            payload.put("createdAt", now);
        }

        firestore.collection("users").document(uid)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener(unused -> completeAdminLogin(email))
                .addOnFailureListener(e -> onLoginFailed());
    }

    private void completeAdminLogin(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
        getSharedPreferences("AdminSession", MODE_PRIVATE).edit()
                .putBoolean("isAdminLoggedIn", true)
                .putString("adminEmail", email)
                .apply();

        Toast.makeText(this, "Login successful!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(LoginAdminActivity.this, AdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    private void onLoginFailed() {
        Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        edtPassword.setText("");
        resetLoginButton();
        prefs.edit().clear().apply();
    }

    private void resetLoginButton() {
        btnLogin.setEnabled(true);
        btnLogin.setText("Login");
        progressLoading.setVisibility(View.GONE);
    }
}
