package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.apptapchikhoakhoc.data.DatabaseHelper;

/**
 * AutoApproveHelper — Tự động duyệt bài viết sau 5 phút nếu admin chưa duyệt.
 *
 * Cách dùng (gọi ngay sau khi insertPendingArticle thành công):
 *
 *   AutoApproveHelper.scheduleAutoApprove(context, articleId);
 *
 * Khi timer kích hoạt:
 *  - Kiểm tra bài có còn ở trạng thái "pending" không
 *  - Nếu có → tự approve → bài xuất hiện trên trang người dùng
 *  - Nếu admin đã xử lý rồi → bỏ qua
 */
public class AutoApproveHelper {

    private static final String TAG          = "AutoApproveHelper";
    public  static final long   DELAY_MS     = 5 * 60 * 1000L; // 5 phút
    // Test nhanh: đổi thành 30 * 1000L (30 giây)

    /**
     * Lên lịch tự động duyệt bài sau DELAY_MS.
     *
     * @param context   Context (dùng ApplicationContext để tránh leak)
     * @param articleId ID bài viết vừa insert dạng pending
     */
    public static void scheduleAutoApprove(Context context, long articleId) {
        if (articleId <= 0) return;

        Context appContext = context.getApplicationContext();
        Handler handler    = new Handler(Looper.getMainLooper());

        handler.postDelayed(() -> {
            new Thread(() -> {
                try {
                    DatabaseHelper db = new DatabaseHelper(appContext);

                    // Chỉ approve nếu bài vẫn đang "pending"
                    boolean stillPending = db.isArticlePending((int) articleId);

                    if (stillPending) {
                        boolean ok = db.approveArticle((int) articleId);
                        if (ok) {
                            Log.i(TAG, "✅ Auto-approved article #" + articleId + " sau 5 phút");
                        } else {
                            Log.w(TAG, "❌ Auto-approve thất bại cho article #" + articleId);
                        }
                    } else {
                        Log.d(TAG, "ℹ️ Article #" + articleId + " đã được admin xử lý, bỏ qua auto-approve");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi auto-approve: " + e.getMessage());
                }
            }).start();
        }, DELAY_MS);

        Log.d(TAG, "⏰ Đã lên lịch auto-approve bài #" + articleId + " sau 5 phút");
    }
}