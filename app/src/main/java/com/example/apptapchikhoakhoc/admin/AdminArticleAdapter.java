package com.example.apptapchikhoakhoc.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_FEATURED = 0;
    private static final int TYPE_SMALL    = 1;

    public interface OnItemClickListener {
        void onEdit(Article article);
        void onDelete(Article article);
    }

    private List<Article>             list;
    private final OnItemClickListener listener;

    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AdminArticleAdapter(List<Article> list, OnItemClickListener listener) {
        this.list     = list;
        this.listener = listener;
    }

    public void updateList(List<Article> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? TYPE_FEATURED : TYPE_SMALL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_FEATURED) {
            View view = inflater.inflate(R.layout.item_admin_article, parent, false);
            return new FeaturedViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_news_small, parent, false);
            return new SmallViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Article article = list.get(position);
        Context ctx     = holder.itemView.getContext();

        if (holder instanceof FeaturedViewHolder) {
            bindFeatured((FeaturedViewHolder) holder, article, ctx);
        } else {
            bindSmall((SmallViewHolder) holder, article, ctx);
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // ══════════════════════════════════════════════════════════════
    //  BIND — Featured
    // ══════════════════════════════════════════════════════════════
    private void bindFeatured(FeaturedViewHolder holder, Article article, Context ctx) {
        applyFeaturedTheme(holder, ctx);

        loadImage(article.getImagePath(), holder.imgThumbnail);

        String cat = article.getCategory();
        holder.tvAdminCategory.setText(cat != null && !cat.isEmpty() ? cat : "Chưa phân loại");

        applyStatusBadge(holder.tvAdminStatus, article.getStatus());

        String title = article.getTitle();
        holder.tvAdminTitle.setText(title != null && !title.isEmpty() ? title : "Không có tiêu đề");

        holder.tvAdminContent.setText(buildSummary(article.getContent(), 100));

        String author = article.getAuthor();
        if (author == null || author.isEmpty()) author = "Ẩn danh";
        holder.tvAdminAvatar.setText(String.valueOf(author.charAt(0)).toUpperCase());
        holder.tvAdminAuthorName.setText(author);
        long ts = article.getApprovedAt();
        holder.tvAdminTime.setText(ts > 0 ? DATE_FMT.format(new Date(ts)) : "Chưa xử lý");

        holder.btnEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(article); });
        holder.btnDeleteAdmin.setOnClickListener(v -> { if (listener != null) listener.onDelete(article); });
    }

    // ══════════════════════════════════════════════════════════════
    //  BIND — Small  ✅ thêm ctx để apply theme
    // ══════════════════════════════════════════════════════════════
    private void bindSmall(SmallViewHolder holder, Article article, Context ctx) {
        // ✅ Apply theme trước — override màu cứng trong XML
        applySmallTheme(holder, ctx);

        loadImage(article.getImagePath(), holder.imgNewsThumb);

        String cat = article.getCategory();
        holder.tvNewsCategory.setText(cat != null && !cat.isEmpty() ? cat : "Chưa phân loại");

        String title = article.getTitle();
        holder.tvNewsTitle.setText(title != null && !title.isEmpty() ? title : "Không có tiêu đề");

        String author = article.getAuthor();
        holder.tvNewsAuthor.setText(author != null && !author.isEmpty() ? author : "Ẩn danh");

        long ts = article.getApprovedAt();
        holder.tvNewsDate.setText(ts > 0 ? DATE_FMT.format(new Date(ts)) : "Chưa xác định");

        holder.btnSmallEdit.setOnClickListener(v -> { if (listener != null) listener.onEdit(article); });
        holder.btnSmallDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(article); });
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════
    private void loadImage(String imgPath, ImageView target) {
        if (imgPath != null && !imgPath.isEmpty()) {
            Bitmap bmp = BitmapFactory.decodeFile(imgPath);
            if (bmp != null) {
                target.setImageBitmap(bmp);
                target.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return;
            }
        }
        target.setImageResource(R.drawable.placeholder_image);
        target.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    }

    private String buildSummary(String content, int maxLen) {
        if (content == null || content.isEmpty()) return "Không có nội dung";
        String plain = content.replaceAll("<[^>]*>", "").trim();
        return plain.length() > maxLen ? plain.substring(0, maxLen) + "..." : plain;
    }

    private void applyStatusBadge(TextView tv, String status) {
        if (status == null) status = "pending";
        switch (status) {
            case "approved":
                tv.setText("✅ Đã duyệt");
                tv.setTextColor(Color.parseColor("#00D9A5"));
                tv.setBackgroundResource(R.drawable.badge_approved_bg);
                break;
            case "rejected":
                tv.setText("❌ Từ chối");
                tv.setTextColor(Color.parseColor("#FF5252"));
                tv.setBackgroundResource(R.drawable.badge_rejected_bg);
                break;
            default:
                tv.setText("⏳ Chờ duyệt");
                tv.setTextColor(Color.parseColor("#FFD166"));
                tv.setBackgroundResource(R.drawable.badge_pending_bg);
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME — Featured
    // ══════════════════════════════════════════════════════════════
    private void applyFeaturedTheme(FeaturedViewHolder holder, Context ctx) {
        boolean dark = AdminThemeManager.isDarkMode(ctx);

        holder.cardAdminArticle.setBackgroundColor(dark
                ? AdminThemeManager.DarkColors.CARD_BACKGROUND
                : AdminThemeManager.LightColors.CARD_BACKGROUND);

        holder.tvAdminTitle.setTextColor(Color.WHITE);

        holder.tvAdminContent.setTextColor(dark
                ? AdminThemeManager.DarkColors.TEXT_SECONDARY
                : AdminThemeManager.LightColors.TEXT_SECONDARY);

        holder.dividerAdmin.setBackgroundColor(dark
                ? AdminThemeManager.DarkColors.DIVIDER
                : AdminThemeManager.LightColors.DIVIDER);

        holder.layoutAdminCardBottom.setBackgroundColor(dark
                ? AdminThemeManager.DarkColors.CARD_BACKGROUND
                : AdminThemeManager.LightColors.CARD_BACKGROUND);

        holder.tvAdminAvatar.setTextColor(dark
                ? AdminThemeManager.DarkColors.ACCENT
                : AdminThemeManager.LightColors.ACCENT);

        holder.tvAdminAuthorName.setTextColor(dark
                ? AdminThemeManager.DarkColors.TEXT_PRIMARY
                : AdminThemeManager.LightColors.TEXT_PRIMARY);

        holder.tvAdminTime.setTextColor(dark
                ? AdminThemeManager.DarkColors.TEXT_TERTIARY
                : AdminThemeManager.LightColors.TEXT_TERTIARY);

        holder.btnEdit.setImageTintList(ColorStateList.valueOf(dark
                ? AdminThemeManager.DarkColors.TEXT_SECONDARY
                : Color.parseColor("#9E9E9E")));

        holder.btnDeleteAdmin.setImageTintList(ColorStateList.valueOf(dark
                ? AdminThemeManager.DarkColors.ACCENT_DANGER
                : AdminThemeManager.LightColors.ACCENT_DANGER));
    }

    // ══════════════════════════════════════════════════════════════
    //  THEME — Small  ✅ hàm mới — fix lỗi màu cứng #1E2A38 trong XML
    // ══════════════════════════════════════════════════════════════
    private void applySmallTheme(SmallViewHolder holder, Context ctx) {
        boolean dark = AdminThemeManager.isDarkMode(ctx);

        // Nền item
        holder.itemView.setBackgroundColor(dark
                ? AdminThemeManager.DarkColors.CARD_BACKGROUND   // #1E2A38
                : AdminThemeManager.LightColors.CARD_BACKGROUND); // trắng/sáng

        // Tiêu đề
        holder.tvNewsTitle.setTextColor(dark
                ? AdminThemeManager.DarkColors.TEXT_PRIMARY
                : AdminThemeManager.LightColors.TEXT_PRIMARY);

        // Tác giả
        holder.tvNewsAuthor.setTextColor(dark
                ? AdminThemeManager.DarkColors.TEXT_SECONDARY
                : AdminThemeManager.LightColors.TEXT_SECONDARY);

        // Ngày
        holder.tvNewsDate.setTextColor(dark
                ? AdminThemeManager.DarkColors.TEXT_TERTIARY
                : AdminThemeManager.LightColors.TEXT_TERTIARY);

        // Nút Edit
        holder.btnSmallEdit.setImageTintList(ColorStateList.valueOf(dark
                ? AdminThemeManager.DarkColors.TEXT_SECONDARY
                : Color.parseColor("#9E9E9E")));

        // Nút Xóa
        holder.btnSmallDelete.setImageTintList(ColorStateList.valueOf(dark
                ? AdminThemeManager.DarkColors.ACCENT_DANGER
                : AdminThemeManager.LightColors.ACCENT_DANGER));
    }

    // ══════════════════════════════════════════════════════════════
    //  VIEWHOLDER — Featured
    // ══════════════════════════════════════════════════════════════
    static class FeaturedViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardAdminArticle;
        ImageView    imgThumbnail;
        TextView     tvAdminCategory;
        TextView     tvAdminStatus;
        TextView     tvAdminTitle;
        TextView     tvAdminContent;
        View         dividerAdmin;
        View         layoutAdminCardBottom;
        TextView     tvAdminAvatar;
        TextView     tvAdminAuthorName;
        TextView     tvAdminTime;
        ImageView    btnEdit;
        ImageView    btnDeleteAdmin;

        FeaturedViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAdminArticle      = itemView.findViewById(R.id.cardAdminArticle);
            imgThumbnail          = itemView.findViewById(R.id.imgThumbnail);
            tvAdminCategory       = itemView.findViewById(R.id.tvAdminCategory);
            tvAdminStatus         = itemView.findViewById(R.id.tvAdminStatus);
            tvAdminTitle          = itemView.findViewById(R.id.tvAdminTitle);
            tvAdminContent        = itemView.findViewById(R.id.tvAdminContent);
            dividerAdmin          = itemView.findViewById(R.id.dividerAdmin);
            layoutAdminCardBottom = itemView.findViewById(R.id.layoutAdminCardBottom);
            tvAdminAvatar         = itemView.findViewById(R.id.tvAdminAvatar);
            tvAdminAuthorName     = itemView.findViewById(R.id.tvAdminAuthorName);
            tvAdminTime           = itemView.findViewById(R.id.tvAdminTime);
            btnEdit               = itemView.findViewById(R.id.btnEdit);
            btnDeleteAdmin        = itemView.findViewById(R.id.btnDeleteAdmin);
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  VIEWHOLDER — Small
    // ══════════════════════════════════════════════════════════════
    static class SmallViewHolder extends RecyclerView.ViewHolder {
        ImageView imgNewsThumb;
        TextView  tvNewsCategory;
        TextView  tvNewsTitle;
        TextView  tvNewsAuthor;
        TextView  tvNewsDate;
        ImageView btnSmallEdit;
        ImageView btnSmallDelete;

        SmallViewHolder(@NonNull View itemView) {
            super(itemView);
            imgNewsThumb   = itemView.findViewById(R.id.imgNewsThumb);
            tvNewsCategory = itemView.findViewById(R.id.tvNewsCategory);
            tvNewsTitle    = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsAuthor   = itemView.findViewById(R.id.tvNewsAuthor);
            tvNewsDate     = itemView.findViewById(R.id.tvNewsDate);
            btnSmallEdit   = itemView.findViewById(R.id.btnSmallEdit);
            btnSmallDelete = itemView.findViewById(R.id.btnSmallDelete);
        }
    }
}