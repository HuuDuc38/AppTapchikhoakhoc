package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.apptapchikhoakhoc.data.DatabaseHelper;

import java.util.concurrent.TimeUnit;

/**
 * AutoApproveWorker — WorkManager Worker tự động duyệt bài sau 5 phút.
 * Hoạt động ngay cả khi app bị kill / màn hình tắt.
 */
public class AutoApproveWorker extends Worker {

    private static final String TAG        = "AutoApproveWorker";
    public  static final String KEY_ID     = "article_id";
    public  static final long   DELAY_MIN  = 5L; // phút

    public AutoApproveWorker(@NonNull Context context,
                             @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        int articleId = getInputData().getInt(KEY_ID, -1);
        if (articleId == -1) {
            Log.w(TAG, "Invalid articleId, skip");
            return Result.failure();
        }

        try {
            DatabaseHelper db = new DatabaseHelper(getApplicationContext());

            // Chỉ approve nếu bài vẫn còn "pending"
            // (tránh ghi đè nếu admin đã xử lý trước đó)
            if (db.isArticlePending(articleId)) {
                boolean ok = db.approveArticle(articleId);
                if (ok) {
                    Log.i(TAG, "✅ Auto-approved article #" + articleId);
                } else {
                    Log.w(TAG, "❌ approveArticle failed for #" + articleId);
                }
            } else {
                Log.d(TAG, "ℹ️ Article #" + articleId
                        + " đã được admin xử lý rồi, bỏ qua auto-approve");
            }
            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return Result.retry();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  SCHEDULE — gọi sau khi insertPendingArticle thành công
    // ══════════════════════════════════════════════════════════════

    /**
     * Lên lịch tự duyệt bài sau DELAY_MIN phút.
     *
     * @param context   ApplicationContext
     * @param articleId ID bài vừa insert dạng pending
     */
    public static void schedule(Context context, long articleId) {
        if (articleId <= 0) return;

        Data inputData = new Data.Builder()
                .putInt(KEY_ID, (int) articleId)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AutoApproveWorker.class)
                .setInitialDelay(DELAY_MIN, TimeUnit.MINUTES)
                .setInputData(inputData)
                .addTag("auto_approve_" + articleId)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);

        Log.d(TAG, "⏰ Đã lên lịch auto-approve bài #"
                + articleId + " sau " + DELAY_MIN + " phút");
    }
}