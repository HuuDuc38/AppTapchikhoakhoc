package com.example.apptapchikhoakhoc.utils;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

/**
 * Helper đặt màu badge danh mục tự động theo tên category.
 * Dùng trong NewsAdapter, NotificationItemAdapter, v.v.
 */
public class CategoryBadgeHelper {

    public static void apply(Context context, TextView tvCategory, String category) {
        if (tvCategory == null || category == null) return;

        tvCategory.setText(category);

        int color = getColorForCategory(category);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(context, 6));
        bg.setColor(color);

        tvCategory.setBackground(bg);
    }

    private static int getColorForCategory(String category) {
        if (category == null) return 0xFF9E9E9E; // xám mặc định

        switch (category.trim()) {
            case "Tin tức sự kiện":
                return 0xFF1565C0; // xanh dương đậm
            case "Đào tạo":
                return 0xFF2E7D32; // xanh lá đậm
            case "Thông tin việc làm":
                return 0xFFE65100; // cam đậm
            case "Khoa học công nghệ":
                return 0xFF6A1B9A; // tím
            case "Hợp tác quốc tế":
                return 0xFF00838F; // xanh ngọc
            // Các nhãn ngắn (hiển thị trong badge)
            case "Kỹ thuật":
                return 0xFF1565C0; // xanh dương
            case "Sư phạm":
                return 0xFF2E7D32; // xanh lá
            case "Công nghệ":
                return 0xFFE65100; // cam
            case "Khoa học":
                return 0xFF6A1B9A; // tím
            case "Hợp tác":
                return 0xFF00838F; // ngọc
            default:
                return 0xFF757575; // xám trung tính
        }
    }

    private static int dpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }
}