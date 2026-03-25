package com.example.apptapchikhoakhoc.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.apptapchikhoakhoc.admin.ArticleStatItem;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.model.Comment;
import com.example.apptapchikhoakhoc.model.UserItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "AppDatabase.db";
    private static final int    DB_VERSION = 10;

    public static final String STATUS_PENDING  = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    // ── Bảng users ───────────────────────────────────────────────
    private static final String TABLE_USER   = "users";
    private static final String COL_ID       = "id";
    private static final String COL_NAME     = "name";
    private static final String COL_EMAIL    = "email";
    private static final String COL_PASSWORD = "password";

    // ── Bảng articles ────────────────────────────────────────────
    private static final String TABLE_ARTICLE   = "articles";
    private static final String COL_ARTICLE_ID  = "id";
    private static final String COL_TITLE       = "title";
    private static final String COL_AUTHOR      = "author";
    private static final String COL_CATEGORY    = "category";
    private static final String COL_CONTENT     = "content";
    private static final String COL_IMAGE       = "image_path";
    private static final String COL_VIDEO       = "video_path";
    private static final String COL_LIKES       = "likes";
    private static final String COL_COMMENTS    = "comments";
    private static final String COL_SHARES      = "shares";
    private static final String COL_VIEWS       = "view_count";
    private static final String COL_STATUS      = "status";
    private static final String COL_USER_EMAIL  = "user_email";
    private static final String COL_APPROVED_AT = "approved_at";

    // ── Bảng comments ────────────────────────────────────────────
    private static final String TABLE_COMMENTS         = "comments";
    private static final String COL_COMMENT_ARTICLE_ID = "article_id";
    private static final String COL_COMMENT_CONTENT    = "content";
    private static final String COL_COMMENT_TIMESTAMP  = "timestamp";
    private static final String COL_COMMENT_USER_NAME  = "user_name";
    private static final String COL_COMMENT_USER_EMAIL = "user_email";

    // ── Bảng article_reactions ────────────────────────────────────
    private static final String TABLE_REACTIONS         = "article_reactions";
    private static final String COL_REACTION_ID         = "id";
    private static final String COL_REACTION_ARTICLE_ID = "article_id";
    private static final String COL_REACTION_USER_EMAIL = "user_email";
    private static final String COL_REACTION_EMOJI      = "emoji";
    private static final String COL_REACTION_TIMESTAMP  = "timestamp";

    // ── Bảng comment_reactions ────────────────────────────────────
    private static final String TABLE_CMT_REACTIONS         = "comment_reactions";
    private static final String COL_CMT_REACTION_ID         = "id";
    private static final String COL_CMT_REACTION_COMMENT_ID = "comment_id";
    private static final String COL_CMT_REACTION_USER_EMAIL = "user_email";
    private static final String COL_CMT_REACTION_EMOJI      = "emoji";
    private static final String COL_CMT_REACTION_TIMESTAMP  = "timestamp";

    private static final String COL_CREACT_COMMENT_ID = COL_CMT_REACTION_COMMENT_ID;
    private static final String COL_CREACT_USER_EMAIL = COL_CMT_REACTION_USER_EMAIL;
    private static final String COL_CREACT_EMOJI      = COL_CMT_REACTION_EMOJI;
    private static final String COL_CREACT_TIMESTAMP  = COL_CMT_REACTION_TIMESTAMP;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ==================== TẠO BẢNG ====================

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME     + " TEXT NOT NULL, " +
                COL_EMAIL    + " TEXT UNIQUE NOT NULL, " +
                COL_PASSWORD + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_ARTICLE + " (" +
                COL_ARTICLE_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE       + " TEXT NOT NULL, " +
                COL_AUTHOR      + " TEXT, " +
                COL_CATEGORY    + " TEXT, " +
                COL_CONTENT     + " TEXT, " +
                COL_IMAGE       + " TEXT, " +
                COL_VIDEO       + " TEXT, " +
                COL_LIKES       + " INTEGER DEFAULT 0, " +
                COL_COMMENTS    + " INTEGER DEFAULT 0, " +
                COL_SHARES      + " INTEGER DEFAULT 0, " +
                COL_VIEWS       + " INTEGER DEFAULT 0, " +
                COL_STATUS      + " TEXT DEFAULT '" + STATUS_APPROVED + "', " +
                COL_USER_EMAIL  + " TEXT DEFAULT '', " +
                COL_APPROVED_AT + " LONG DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_COMMENTS + " (" +
                COL_ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COMMENT_ARTICLE_ID  + " INTEGER NOT NULL, " +
                COL_COMMENT_USER_NAME   + " TEXT DEFAULT '', " +
                COL_COMMENT_USER_EMAIL  + " TEXT DEFAULT '', " +
                COL_COMMENT_CONTENT     + " TEXT NOT NULL, " +
                COL_COMMENT_TIMESTAMP   + " LONG NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_REACTIONS + " (" +
                COL_REACTION_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_REACTION_ARTICLE_ID + " INTEGER NOT NULL, " +
                COL_REACTION_USER_EMAIL + " TEXT NOT NULL, " +
                COL_REACTION_EMOJI      + " TEXT NOT NULL, " +
                COL_REACTION_TIMESTAMP  + " LONG NOT NULL, " +
                "UNIQUE(" + COL_REACTION_ARTICLE_ID + ", " + COL_REACTION_USER_EMAIL + "))");

        db.execSQL("CREATE TABLE " + TABLE_CMT_REACTIONS + " (" +
                COL_CMT_REACTION_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CMT_REACTION_COMMENT_ID + " INTEGER NOT NULL, " +
                COL_CMT_REACTION_USER_EMAIL + " TEXT NOT NULL, " +
                COL_CMT_REACTION_EMOJI      + " TEXT NOT NULL, " +
                COL_CMT_REACTION_TIMESTAMP  + " LONG NOT NULL, " +
                "UNIQUE(" + COL_CMT_REACTION_COMMENT_ID + ", " + COL_CMT_REACTION_USER_EMAIL + "))");
    }

    // ==================== NÂNG CẤP DB ====================

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ARTICLE + " ADD COLUMN " + COL_LIKES    + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ARTICLE + " ADD COLUMN " + COL_COMMENTS + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ARTICLE + " ADD COLUMN " + COL_SHARES   + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_COMMENTS + " (" +
                    COL_ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_COMMENT_ARTICLE_ID + " INTEGER NOT NULL, " +
                    COL_COMMENT_CONTENT    + " TEXT NOT NULL, " +
                    COL_COMMENT_TIMESTAMP  + " LONG NOT NULL)");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_ARTICLE +
                    " ADD COLUMN " + COL_STATUS + " TEXT DEFAULT '" + STATUS_APPROVED + "'");
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_ARTICLE +
                    " ADD COLUMN " + COL_USER_EMAIL + " TEXT DEFAULT ''");
        }
        if (oldVersion < 6) {
            try { db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COL_COMMENT_USER_NAME  + " TEXT DEFAULT ''"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_COMMENTS + " ADD COLUMN " + COL_COMMENT_USER_EMAIL + " TEXT DEFAULT ''"); } catch (Exception ignored) {}
        }
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ARTICLE + " ADD COLUMN " + COL_APPROVED_AT + " LONG DEFAULT 0");
                db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_APPROVED_AT + " = " + System.currentTimeMillis() +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 8) {
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REACTIONS + " (" +
                        COL_REACTION_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_REACTION_ARTICLE_ID + " INTEGER NOT NULL, " +
                        COL_REACTION_USER_EMAIL + " TEXT NOT NULL, " +
                        COL_REACTION_EMOJI      + " TEXT NOT NULL, " +
                        COL_REACTION_TIMESTAMP  + " LONG NOT NULL, " +
                        "UNIQUE(" + COL_REACTION_ARTICLE_ID + ", " + COL_REACTION_USER_EMAIL + "))");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 9) {
            try {
                db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CMT_REACTIONS + " (" +
                        COL_CMT_REACTION_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_CMT_REACTION_COMMENT_ID + " INTEGER NOT NULL, " +
                        COL_CMT_REACTION_USER_EMAIL + " TEXT NOT NULL, " +
                        COL_CMT_REACTION_EMOJI      + " TEXT NOT NULL, " +
                        COL_CMT_REACTION_TIMESTAMP  + " LONG NOT NULL, " +
                        "UNIQUE(" + COL_CMT_REACTION_COMMENT_ID + ", " + COL_CMT_REACTION_USER_EMAIL + "))");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_ARTICLE +
                        " ADD COLUMN " + COL_VIEWS + " INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }
    }

    // ==================== HELPER ====================

    private Article cursorToArticle(Cursor cursor, SQLiteDatabase db) {
        Article a = new Article(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ARTICLE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_AUTHOR)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENT)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_VIDEO))
        );
        a.setLikes(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LIKES)));
        a.setComments(cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMMENTS)));
        a.setShares(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SHARES)));

        int viewsIdx = cursor.getColumnIndex(COL_VIEWS);
        if (viewsIdx != -1) a.setViewCount(cursor.getInt(viewsIdx));

        int statusIdx = cursor.getColumnIndex(COL_STATUS);
        if (statusIdx != -1) a.setStatus(cursor.getString(statusIdx));

        int approvedAtIdx = cursor.getColumnIndex(COL_APPROVED_AT);
        if (approvedAtIdx != -1) a.setApprovedAt(cursor.getLong(approvedAtIdx));

        // Đọc user_email từ cột — ưu tiên cột DB trước
        String userEmail = "";
        int emailIdx = cursor.getColumnIndex(COL_USER_EMAIL);
        if (emailIdx != -1) {
            String val = cursor.getString(emailIdx);
            if (val != null) userEmail = val.trim();
        }

        // Fallback: nếu cột user_email trống → tìm email theo author name
        if (userEmail.isEmpty()) {
            String authorName = a.getAuthor();
            if (authorName != null && !authorName.isEmpty()) {
                Cursor uc = db.rawQuery(
                        "SELECT " + COL_EMAIL + " FROM " + TABLE_USER +
                                " WHERE LOWER(" + COL_NAME + ") = LOWER(?) LIMIT 1",
                        new String[]{authorName});
                if (uc.moveToFirst()) {
                    String found = uc.getString(0);
                    if (found != null) userEmail = found.trim();
                }
                uc.close();
            }
        }
        a.setUserEmail(userEmail);
        return a;
    }

    // ==================== LẤY BÀI VIẾT ====================

    public ArrayList<Article> getPendingArticles() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_PENDING + "'" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getApprovedArticles() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getRejectedArticles() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_REJECTED + "'" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    /**
     * ★ MỚI: Lấy TẤT CẢ bài viết của 1 user (pending + approved + rejected)
     * Dùng cho tab "Bài viết của tôi" trong NotificationsActivity.
     */
    public ArrayList<Article> getArticlesByUserEmail(String userEmail) {
        ArrayList<Article> list = new ArrayList<>();
        if (userEmail == null || userEmail.isEmpty()) return list;

        SQLiteDatabase db = this.getReadableDatabase();

        // Query chính: lấy tất cả status theo user_email
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE LOWER(" + COL_USER_EMAIL + ") = LOWER(?)" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC",
                new String[]{ userEmail }
        );
        if (cursor.moveToFirst()) {
            do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        }
        cursor.close();

        // Fallback: nếu không tìm được qua email, thử tìm theo author name
        if (list.isEmpty()) {
            String userName = getUserNameInternal(db, userEmail);
            if (userName != null && !userName.isEmpty()) {
                Cursor c2 = db.rawQuery(
                        "SELECT * FROM " + TABLE_ARTICLE +
                                " WHERE LOWER(" + COL_AUTHOR + ") = LOWER(?)" +
                                " ORDER BY " + COL_ARTICLE_ID + " DESC",
                        new String[]{ userName }
                );
                if (c2.moveToFirst()) {
                    do {
                        Article a = cursorToArticle(c2, db);
                        if (a.getUserEmail() == null || a.getUserEmail().isEmpty()) {
                            a.setUserEmail(userEmail);
                        }
                        list.add(a);
                    } while (c2.moveToNext());
                }
                c2.close();
            }
        }

        db.close();
        return list;
    }

    /**
     * Lấy bài đã duyệt của 1 user cụ thể — query trực tiếp theo user_email + status
     */
    public ArrayList<Article> getApprovedArticlesByUserEmail(String userEmail) {
        ArrayList<Article> list = new ArrayList<>();
        if (userEmail == null || userEmail.isEmpty()) return list;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS     + " = '" + STATUS_APPROVED + "'" +
                        " AND LOWER(" + COL_USER_EMAIL + ") = LOWER(?)" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC",
                new String[]{ userEmail }
        );
        if (cursor.moveToFirst()) {
            do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        }
        cursor.close();

        // Fallback: tìm theo author name
        if (list.isEmpty()) {
            String userName = getUserNameInternal(db, userEmail);
            if (userName != null && !userName.isEmpty()) {
                Cursor c2 = db.rawQuery(
                        "SELECT * FROM " + TABLE_ARTICLE +
                                " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                                " AND LOWER(" + COL_AUTHOR + ") = LOWER(?)" +
                                " ORDER BY " + COL_ARTICLE_ID + " DESC",
                        new String[]{ userName }
                );
                if (c2.moveToFirst()) {
                    do {
                        Article a = cursorToArticle(c2, db);
                        if (a.getUserEmail() == null || a.getUserEmail().isEmpty()) {
                            a.setUserEmail(userEmail);
                        }
                        list.add(a);
                    } while (c2.moveToNext());
                }
                c2.close();
            }
        }

        db.close();
        return list;
    }

    /** Lấy tên user từ email — dùng DB đang mở sẵn, không mở lại */
    private String getUserNameInternal(SQLiteDatabase db, String email) {
        Cursor c = db.rawQuery(
                "SELECT " + COL_NAME + " FROM " + TABLE_USER +
                        " WHERE LOWER(" + COL_EMAIL + ") = LOWER(?) LIMIT 1",
                new String[]{ email }
        );
        String name = null;
        if (c.moveToFirst()) name = c.getString(0);
        c.close();
        return name;
    }

    public ArrayList<Article> getAllArticles() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getArticlesByCategory(String category) {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_STATUS + " = '" + STATUS_APPROVED + "' AND (" +
                ((category == null || category.trim().isEmpty())
                        ? COL_CATEGORY + " IS NULL OR " + COL_CATEGORY + " = ''"
                        : COL_CATEGORY + " = ?") + ")";
        String[] args = (category == null || category.trim().isEmpty())
                ? null : new String[]{category};
        Cursor cursor = db.query(TABLE_ARTICLE, null, selection, args,
                null, null, COL_ARTICLE_ID + " DESC");
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getFeaturedArticles() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        long threeDaysAgo = System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000);
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " AND " + COL_APPROVED_AT + " >= " + threeDaysAgo +
                        " ORDER BY (" + COL_LIKES + " * 3 + " + COL_COMMENTS + " * 2 + " + COL_SHARES + ") DESC" +
                        " LIMIT 20", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        if (list.isEmpty()) {
            Cursor fallback = db.rawQuery(
                    "SELECT * FROM " + TABLE_ARTICLE +
                            " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                            " ORDER BY " + COL_APPROVED_AT + " DESC LIMIT 20", null);
            if (fallback.moveToFirst()) do { list.add(cursorToArticle(fallback, db)); } while (fallback.moveToNext());
            fallback.close();
        }
        db.close();
        return list;
    }

    public ArrayList<Article> getLatestApprovedArticles(int limit) {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " ORDER BY " + COL_APPROVED_AT + " DESC LIMIT " + limit, null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getArticlesWithImage() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " AND " + COL_IMAGE + " IS NOT NULL AND " + COL_IMAGE + " != ''" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getVideoArticles() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " AND " + COL_VIDEO + " IS NOT NULL AND " + COL_VIDEO + " != ''" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> searchArticles(String keyword) {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String pattern = "%" + keyword + "%";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " AND (UPPER(" + COL_TITLE    + ") LIKE UPPER(?)" +
                        " OR UPPER("  + COL_AUTHOR   + ") LIKE UPPER(?)" +
                        " OR UPPER("  + COL_CATEGORY + ") LIKE UPPER(?)" +
                        " OR UPPER("  + COL_CONTENT  + ") LIKE UPPER(?))" +
                        " ORDER BY " + COL_ARTICLE_ID + " DESC",
                new String[]{pattern, pattern, pattern, pattern});
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    public ArrayList<Article> getArticlesWithViews() {
        ArrayList<Article> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ARTICLE +
                        " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                        " ORDER BY " + COL_VIEWS + " DESC", null);
        if (cursor.moveToFirst()) do { list.add(cursorToArticle(cursor, db)); } while (cursor.moveToNext());
        cursor.close();
        db.close();
        return list;
    }

    // ==================== THỐNG KÊ ====================

    public int getTotalApprovedArticles() {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_ARTICLE +
                " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'");
    }

    public int getTotalPendingArticles() {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_ARTICLE +
                " WHERE " + COL_STATUS + " = '" + STATUS_PENDING + "'");
    }

    public int getTotalUsers() {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_USER);
    }

    public int getTotalViews() {
        return (int) queryCount("SELECT SUM(" + COL_VIEWS + ") FROM " + TABLE_ARTICLE +
                " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'");
    }

    public int getTotalLikes() {
        return (int) queryCount("SELECT SUM(" + COL_LIKES + ") FROM " + TABLE_ARTICLE);
    }

    public int getTotalComments() {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_COMMENTS);
    }

    public int getPendingCount() {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_ARTICLE +
                " WHERE " + COL_STATUS + " = '" + STATUS_PENDING + "'");
    }

    private long queryCount(String sql) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        long count = 0;
        if (c.moveToFirst()) count = c.getLong(0);
        c.close();
        db.close();
        return count;
    }

    // ==================== THỐNG KÊ LƯỢT THÍCH ====================

    public List<ArticleStatItem> getArticlesOrderedByLikes() {
        List<ArticleStatItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT " + COL_TITLE + ", " + COL_LIKES + ", " + COL_CATEGORY +
                            " FROM "  + TABLE_ARTICLE +
                            " WHERE " + COL_STATUS + " = '" + STATUS_APPROVED + "'" +
                            " AND "   + COL_LIKES  + " > 0" +
                            " ORDER BY " + COL_LIKES + " DESC" +
                            " LIMIT 50",
                    null
            );
            while (cursor.moveToNext()) {
                String title    = cursor.getString(0);
                int    likes    = cursor.getInt(1);
                String category = cursor.getString(2);
                list.add(new ArticleStatItem(
                        title    != null ? title    : "",
                        likes,
                        category != null ? category : ""
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }

    // ==================== LƯỢT XEM ====================

    public void incrementViewCount(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_VIEWS + " = " +
                            COL_VIEWS + " + 1 WHERE " + COL_ARTICLE_ID + " = ?",
                    new Object[]{articleId});
        } finally {
            db.close();
        }
    }

    public int getViewCount(int articleId) {
        return getCount(articleId, COL_VIEWS);
    }

    // ==================== QUẢN LÝ BÌNH LUẬN (ADMIN) ====================

    public List<Comment> getAllCommentsForAdmin() {
        List<Comment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT c." + COL_ID                  + ", " +
                            "c." + COL_COMMENT_ARTICLE_ID  + ", " +
                            "c." + COL_COMMENT_USER_NAME   + ", " +
                            "c." + COL_COMMENT_USER_EMAIL  + ", " +
                            "c." + COL_COMMENT_CONTENT     + ", " +
                            "c." + COL_COMMENT_TIMESTAMP   + ", " +
                            "a." + COL_TITLE + " AS article_title " +
                            "FROM "  + TABLE_COMMENTS + " c " +
                            "LEFT JOIN " + TABLE_ARTICLE + " a " +
                            "ON c." + COL_COMMENT_ARTICLE_ID + " = a." + COL_ARTICLE_ID +
                            " ORDER BY c." + COL_COMMENT_TIMESTAMP + " DESC",
                    null
            );
            while (cursor.moveToNext()) {
                int    id           = cursor.getInt(0);
                int    articleId    = cursor.getInt(1);
                String userName     = cursor.getString(2);
                String userEmail    = cursor.getString(3);
                String content      = cursor.getString(4);
                long   timestamp    = cursor.getLong(5);
                String articleTitle = cursor.getString(6);
                boolean violated    = containsViolation(content);
                list.add(new Comment(
                        id, articleId, userName, userEmail,
                        content, timestamp, articleTitle, violated
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }

    public boolean deleteComment(int commentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            Cursor c = db.rawQuery(
                    "SELECT " + COL_COMMENT_ARTICLE_ID +
                            " FROM " + TABLE_COMMENTS +
                            " WHERE " + COL_ID + " = ?",
                    new String[]{String.valueOf(commentId)}
            );
            int articleId = -1;
            if (c.moveToFirst()) articleId = c.getInt(0);
            c.close();

            db.delete(
                    TABLE_CMT_REACTIONS,
                    COL_CMT_REACTION_COMMENT_ID + " = ?",
                    new String[]{String.valueOf(commentId)}
            );

            int rows = db.delete(
                    TABLE_COMMENTS,
                    COL_ID + " = ?",
                    new String[]{String.valueOf(commentId)}
            );

            if (rows > 0) {
                success = true;
                if (articleId != -1) {
                    db.execSQL(
                            "UPDATE " + TABLE_ARTICLE +
                                    " SET " + COL_COMMENTS + " = MAX(0, " + COL_COMMENTS + " - 1)" +
                                    " WHERE " + COL_ARTICLE_ID + " = ?",
                            new Object[]{articleId}
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return success;
    }

    private boolean containsViolation(String content) {
        if (content == null || content.isEmpty()) return false;
        String lower = content.toLowerCase();
        String[] badWords = {
                "đm", "đụ", "địt", "lồn", "cặc", "đéo", "đít",
                "mẹ mày", "bố mày", "óc chó",
                "thằng ngu", "con ngu", "súc vật", "vô học",
                "fuck", "shit", "bitch", "asshole", "idiot"
        };
        for (String word : badWords) {
            if (lower.contains(word)) return true;
        }
        return false;
    }

    // ==================== DANH SÁCH NGƯỜI DÙNG (ADMIN) ====================

    public List<UserItem> getAllUsersForAdmin() {
        List<UserItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT " +
                            "u." + COL_ID    + ", " +
                            "u." + COL_NAME  + ", " +
                            "u." + COL_EMAIL + ", " +
                            "(SELECT COUNT(*) FROM " + TABLE_COMMENTS +
                            " WHERE " + COL_COMMENT_USER_EMAIL +
                            " = u." + COL_EMAIL + ") AS total_comments, " +
                            "(SELECT COALESCE(SUM(a." + COL_LIKES + "), 0) " +
                            "FROM " + TABLE_ARTICLE + " a " +
                            "WHERE a." + COL_USER_EMAIL + " = u." + COL_EMAIL +
                            " AND a." + COL_STATUS + " = '" + STATUS_APPROVED + "') AS total_likes " +
                            "FROM " + TABLE_USER + " u " +
                            "ORDER BY u." + COL_NAME + " ASC",
                    null
            );
            while (cursor.moveToNext()) {
                int    id            = cursor.getInt(0);
                String name          = cursor.getString(1);
                String email         = cursor.getString(2);
                int    totalComments = cursor.getInt(3);
                int    totalLikes    = cursor.getInt(4);
                list.add(new UserItem(id, name, email, totalComments, totalLikes));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return list;
    }

    // ==================== USER ====================

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result != -1;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_USER +
                " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public boolean checkLogin(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT 1 FROM " + TABLE_USER +
                        " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?",
                new String[]{email, password});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valid;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_USER +
                " WHERE " + COL_EMAIL + " = ?", new String[]{email});
        String name = "";
        if (cursor.moveToFirst()) name = cursor.getString(0);
        cursor.close();
        db.close();
        return name;
    }

    // ==================== ARTICLE MANAGEMENT ====================

    public long insertArticle(String title, String author, String category,
                              String content, String imagePath, String videoPath) {
        return insertArticleWithStatus(title, author, category, content,
                imagePath, videoPath, STATUS_APPROVED, "");
    }

    public long insertPendingArticle(String title, String author, String category,
                                     String content, String imagePath, String videoPath,
                                     String userEmail) {
        return insertArticleWithStatus(title, author, category, content,
                imagePath, videoPath, STATUS_PENDING,
                userEmail != null ? userEmail : "");
    }

    public long insertPendingArticle(String title, String author, String category,
                                     String content, String imagePath, String videoPath) {
        return insertPendingArticle(title, author, category, content, imagePath, videoPath, "");
    }

    private long insertArticleWithStatus(String title, String author, String category,
                                         String content, String imagePath, String videoPath,
                                         String status, String userEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE,      title);
        values.put(COL_AUTHOR,     author   != null ? author   : "");
        values.put(COL_CATEGORY,   category != null ? category : "");
        values.put(COL_CONTENT,    content);
        values.put(COL_IMAGE,      imagePath);
        values.put(COL_VIDEO,      videoPath);
        values.put(COL_STATUS,     status);
        values.put(COL_USER_EMAIL, userEmail != null ? userEmail : "");
        values.put(COL_VIEWS,      0);
        values.put(COL_APPROVED_AT,
                STATUS_APPROVED.equals(status) ? System.currentTimeMillis() : 0L);
        long id = db.insert(TABLE_ARTICLE, null, values);
        db.close();
        return id;
    }

    public boolean updateArticle(int articleId, String title, String author, String category,
                                 String content, String imagePath, String videoPath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE,    title);
        values.put(COL_AUTHOR,   author   != null ? author   : "");
        values.put(COL_CATEGORY, category != null ? category : "");
        values.put(COL_CONTENT,  content);
        values.put(COL_IMAGE,    imagePath);
        values.put(COL_VIDEO,    videoPath);
        int rows = db.update(TABLE_ARTICLE, values,
                COL_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteArticle(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COMMENTS,
                COL_COMMENT_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)});
        db.delete(TABLE_REACTIONS,
                COL_REACTION_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)});
        db.delete(TABLE_CMT_REACTIONS,
                COL_CMT_REACTION_COMMENT_ID + " IN (SELECT " + COL_ID +
                        " FROM " + TABLE_COMMENTS +
                        " WHERE " + COL_COMMENT_ARTICLE_ID + " = ?)",
                new String[]{String.valueOf(articleId)});
        int rows = db.delete(TABLE_ARTICLE,
                COL_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)});
        db.close();
        return rows > 0;
    }

    public boolean approveArticle(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS,      STATUS_APPROVED);
        values.put(COL_APPROVED_AT, System.currentTimeMillis());
        int rows = db.update(TABLE_ARTICLE, values,
                COL_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)});
        db.close();
        return rows > 0;
    }

    public boolean rejectArticle(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS,      STATUS_REJECTED);
        values.put(COL_APPROVED_AT, 0L);
        int rows = db.update(TABLE_ARTICLE, values,
                COL_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)});
        db.close();
        return rows > 0;
    }

    public boolean isArticlePending(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c = null;
        boolean pending = false;
        try {
            c = db.rawQuery(
                    "SELECT " + COL_STATUS + " FROM " + TABLE_ARTICLE +
                            " WHERE " + COL_ARTICLE_ID + " = ?",
                    new String[]{String.valueOf(articleId)});
            if (c.moveToFirst()) pending = STATUS_PENDING.equals(c.getString(0));
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return pending;
    }

    // ==================== REACTION ====================

    public String getUserReaction(int articleId, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_REACTION_EMOJI + " FROM " + TABLE_REACTIONS +
                        " WHERE " + COL_REACTION_ARTICLE_ID + " = ? AND " +
                        COL_REACTION_USER_EMAIL + " = ?",
                new String[]{String.valueOf(articleId), userEmail});
        String emoji = null;
        if (c.moveToFirst()) emoji = c.getString(0);
        c.close();
        db.close();
        return emoji;
    }

    public boolean addOrUpdateReaction(int articleId, String userEmail, String emoji) {
        if (userEmail == null || userEmail.isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            String existing = null;
            Cursor c = db.rawQuery(
                    "SELECT " + COL_REACTION_EMOJI + " FROM " + TABLE_REACTIONS +
                            " WHERE " + COL_REACTION_ARTICLE_ID + " = ? AND " +
                            COL_REACTION_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(articleId), userEmail});
            if (c.moveToFirst()) existing = c.getString(0);
            c.close();

            ContentValues values = new ContentValues();
            values.put(COL_REACTION_ARTICLE_ID, articleId);
            values.put(COL_REACTION_USER_EMAIL, userEmail);
            values.put(COL_REACTION_EMOJI,      emoji);
            values.put(COL_REACTION_TIMESTAMP,  System.currentTimeMillis());

            long result = db.insertWithOnConflict(TABLE_REACTIONS, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            success = result != -1;

            if (success && existing == null) {
                db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_LIKES + " = " +
                                COL_LIKES + " + 1 WHERE " + COL_ARTICLE_ID + " = ?",
                        new String[]{String.valueOf(articleId)});
            }
        } finally {
            db.close();
        }
        return success;
    }

    public boolean removeReactionFromDb(int articleId, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            int rows = db.delete(TABLE_REACTIONS,
                    COL_REACTION_ARTICLE_ID + " = ? AND " + COL_REACTION_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(articleId), userEmail});
            if (rows > 0) {
                db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_LIKES +
                                " = MAX(0, " + COL_LIKES + " - 1) WHERE " + COL_ARTICLE_ID + " = ?",
                        new String[]{String.valueOf(articleId)});
                success = true;
            }
        } finally {
            db.close();
        }
        return success;
    }

    public int getReactionCount(int articleId) {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_REACTIONS +
                " WHERE " + COL_REACTION_ARTICLE_ID + " = " + articleId);
    }

    @Deprecated
    public void likeArticle(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_LIKES + " = " +
                        COL_LIKES + " + 1 WHERE " + COL_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)});
        db.close();
    }

    // ==================== COMMENT ====================

    public boolean addComment(int articleId, String userName, String userEmail,
                              String content, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMMENT_ARTICLE_ID, articleId);
        values.put(COL_COMMENT_USER_NAME,  userName  != null ? userName  : "");
        values.put(COL_COMMENT_USER_EMAIL, userEmail != null ? userEmail : "");
        values.put(COL_COMMENT_CONTENT,    content);
        values.put(COL_COMMENT_TIMESTAMP,  timestamp);
        long result = db.insert(TABLE_COMMENTS, null, values);
        if (result != -1) {
            db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_COMMENTS + " = " +
                            COL_COMMENTS + " + 1 WHERE " + COL_ARTICLE_ID + " = ?",
                    new String[]{String.valueOf(articleId)});
        }
        db.close();
        return result != -1;
    }

    public boolean addComment(int articleId, String content, long timestamp) {
        return addComment(articleId, "Ẩn danh", "", content, timestamp);
    }

    public List<Comment> getCommentsByArticle(int articleId) {
        List<Comment> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_COMMENTS +
                        " WHERE " + COL_COMMENT_ARTICLE_ID + " = ?" +
                        " ORDER BY " + COL_COMMENT_TIMESTAMP + " DESC",
                new String[]{String.valueOf(articleId)});
        if (cursor.moveToFirst()) {
            do {
                int    id       = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String uName    = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMMENT_USER_NAME));
                String uEmail   = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMMENT_USER_EMAIL));
                String cContent = cursor.getString(cursor.getColumnIndexOrThrow(COL_COMMENT_CONTENT));
                long   ts       = cursor.getLong(cursor.getColumnIndexOrThrow(COL_COMMENT_TIMESTAMP));
                list.add(new Comment(id, articleId, uName, uEmail, cContent, ts));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // ==================== COMMENT REACTION ====================

    public String getCommentReaction(int commentId, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT " + COL_CMT_REACTION_EMOJI + " FROM " + TABLE_CMT_REACTIONS +
                        " WHERE " + COL_CMT_REACTION_COMMENT_ID + " = ? AND " +
                        COL_CMT_REACTION_USER_EMAIL + " = ?",
                new String[]{String.valueOf(commentId), userEmail});
        String emoji = null;
        if (c.moveToFirst()) emoji = c.getString(0);
        c.close();
        db.close();
        return emoji;
    }

    public java.util.Map<Integer, String> getCommentReactionsForUser(
            java.util.List<Integer> commentIds, String userEmail) {
        java.util.Map<Integer, String> result = new java.util.HashMap<>();
        if (userEmail == null || userEmail.isEmpty()
                || commentIds == null || commentIds.isEmpty()) return result;
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commentIds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(commentIds.get(i));
        }
        Cursor c = db.rawQuery(
                "SELECT " + COL_CMT_REACTION_COMMENT_ID + ", " + COL_CMT_REACTION_EMOJI +
                        " FROM " + TABLE_CMT_REACTIONS +
                        " WHERE " + COL_CMT_REACTION_COMMENT_ID + " IN (" + sb + ")" +
                        " AND " + COL_CMT_REACTION_USER_EMAIL + " = ?",
                new String[]{userEmail});
        if (c.moveToFirst()) do { result.put(c.getInt(0), c.getString(1)); } while (c.moveToNext());
        c.close();
        db.close();
        return result;
    }

    public java.util.Map<Integer, Integer> getCommentReactionCounts(
            java.util.List<Integer> commentIds) {
        java.util.Map<Integer, Integer> result = new java.util.HashMap<>();
        if (commentIds == null || commentIds.isEmpty()) return result;
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < commentIds.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(commentIds.get(i));
        }
        Cursor c = db.rawQuery(
                "SELECT " + COL_CMT_REACTION_COMMENT_ID + ", COUNT(*) FROM " + TABLE_CMT_REACTIONS +
                        " WHERE " + COL_CMT_REACTION_COMMENT_ID + " IN (" + sb + ")" +
                        " GROUP BY " + COL_CMT_REACTION_COMMENT_ID, null);
        if (c.moveToFirst()) do { result.put(c.getInt(0), c.getInt(1)); } while (c.moveToNext());
        c.close();
        db.close();
        return result;
    }

    public boolean addOrUpdateCommentReaction(int commentId, String userEmail, String emoji) {
        if (userEmail == null || userEmail.isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            ContentValues values = new ContentValues();
            values.put(COL_CMT_REACTION_COMMENT_ID, commentId);
            values.put(COL_CMT_REACTION_USER_EMAIL, userEmail);
            values.put(COL_CMT_REACTION_EMOJI,      emoji);
            values.put(COL_CMT_REACTION_TIMESTAMP,  System.currentTimeMillis());
            long r = db.insertWithOnConflict(TABLE_CMT_REACTIONS, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            success = r != -1;
        } finally {
            db.close();
        }
        return success;
    }

    public boolean removeCommentReaction(int commentId, String userEmail) {
        if (userEmail == null || userEmail.isEmpty()) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;
        try {
            int rows = db.delete(TABLE_CMT_REACTIONS,
                    COL_CMT_REACTION_COMMENT_ID + " = ? AND " +
                            COL_CMT_REACTION_USER_EMAIL + " = ?",
                    new String[]{String.valueOf(commentId), userEmail});
            success = rows > 0;
        } finally {
            db.close();
        }
        return success;
    }

    public int getCommentsCount(int articleId) {
        return (int) queryCount("SELECT COUNT(*) FROM " + TABLE_COMMENTS +
                " WHERE " + COL_COMMENT_ARTICLE_ID + " = " + articleId);
    }

    public void shareArticle(int articleId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_ARTICLE + " SET " + COL_SHARES + " = " +
                        COL_SHARES + " + 1 WHERE " + COL_ARTICLE_ID + " = ?",
                new String[]{String.valueOf(articleId)});
        db.close();
    }

    public int getLikes(int articleId)    { return getCount(articleId, COL_LIKES); }
    public int getComments(int articleId) { return getCount(articleId, COL_COMMENTS); }
    public int getShares(int articleId)   { return getCount(articleId, COL_SHARES); }

    private int getCount(int articleId, String column) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT " + column + " FROM " + TABLE_ARTICLE +
                " WHERE " + COL_ARTICLE_ID + " = ?", new String[]{String.valueOf(articleId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }
}