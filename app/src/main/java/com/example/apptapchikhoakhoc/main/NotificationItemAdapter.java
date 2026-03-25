package com.example.apptapchikhoakhoc.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_MY       = 0;
    private static final int VIEW_TYPE_NEW_HERO = 1;
    private static final int VIEW_TYPE_NEW      = 2;

    private final Context       context;
    private       List<Article> list;
    private final boolean       isMyArticles;
    private final boolean       isDark;

    public NotificationItemAdapter(Context context, List<Article> list, boolean isMyArticles) {
        this.context      = context;
        this.list         = list;
        this.isMyArticles = isMyArticles;
        this.isDark       = ThemeManager.isDarkMode(context);
    }

    public void updateList(List<Article> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (isMyArticles) return VIEW_TYPE_MY;
        return (position == 0) ? VIEW_TYPE_NEW_HERO : VIEW_TYPE_NEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_MY) {
            View view = inflater.inflate(R.layout.item_notifications_my, parent, false);
            return new MyArticleViewHolder(view);
        } else if (viewType == VIEW_TYPE_NEW_HERO) {
            View view = inflater.inflate(R.layout.item_notifications_new_hero, parent, false);
            return new NewArticleViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_notifications_new, parent, false);
            return new NewArticleViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Article article = list.get(position);

        // ── Áp dụng dark mode cho item background ─────────────────
        int itemBg   = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int textMain = isDark ? ThemeManager.DarkColors.TEXT_PRIMARY     : Color.parseColor("#2C2C2A");
        int textSub  = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY   : Color.parseColor("#888888");
        int textTime = isDark ? ThemeManager.DarkColors.TEXT_TERTIARY    : Color.parseColor("#AAAAAA");
        int accent   = isDark ? ThemeManager.DarkColors.ACCENT           : Color.parseColor("#C0392B");

        holder.itemView.setBackgroundColor(itemBg);

        if (holder instanceof MyArticleViewHolder) {
            bindMyArticle((MyArticleViewHolder) holder, article, textMain, textTime, accent);
        } else if (holder instanceof NewArticleViewHolder) {
            bindNewArticle((NewArticleViewHolder) holder, article, textMain, textSub, textTime, accent);
        }

        // ── Click ─────────────────────────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            String status = article.getStatus();
            if (DatabaseHelper.STATUS_PENDING.equals(status)) {
                Toast.makeText(context, "⏳ Bài viết đang chờ admin duyệt", Toast.LENGTH_SHORT).show();
            } else if (DatabaseHelper.STATUS_REJECTED.equals(status)) {
                Toast.makeText(context, "❌ Bài viết đã bị từ chối", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("article", article);
                context.startActivity(intent);
            }
        });
    }

    // ──────────────────────────────────────────────────────────────
    //  Bind: Bài của tôi
    // ──────────────────────────────────────────────────────────────

    private void bindMyArticle(MyArticleViewHolder holder, Article article,
                               int textMain, int textTime, int accent) {
        holder.tvTitle.setText(article.getTitle() != null ? article.getTitle() : "");
        holder.tvTitle.setTextColor(textMain);

        String status = article.getStatus();
        String timeText;
        if (DatabaseHelper.STATUS_PENDING.equals(status))       timeText = "Đang chờ duyệt";
        else if (DatabaseHelper.STATUS_REJECTED.equals(status)) timeText = "Đã bị từ chối";
        else                                                     timeText = getRelativeTime(article.getApprovedAt());
        holder.tvTime.setText(timeText);
        holder.tvTime.setTextColor(textTime);

        loadThumbnail(holder.imgThumb, article.getImagePath());

        if (DatabaseHelper.STATUS_PENDING.equals(status)) {
            holder.tvBadge.setText("⏳ Chờ duyệt");
            holder.tvBadge.setTextColor(isDark ? Color.parseColor("#FFB74D") : 0xFF854F0B);
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_amber);
            holder.tvBadge.setVisibility(View.VISIBLE);
        } else if (DatabaseHelper.STATUS_APPROVED.equals(status)) {
            holder.tvBadge.setText("✅ Đã duyệt");
            holder.tvBadge.setTextColor(isDark ? Color.parseColor("#81C784") : 0xFF3B6D11);
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_green);
            holder.tvBadge.setVisibility(View.VISIBLE);
        } else if (DatabaseHelper.STATUS_REJECTED.equals(status)) {
            holder.tvBadge.setText("❌ Bị từ chối");
            holder.tvBadge.setTextColor(isDark ? Color.parseColor("#EF9A9A") : 0xFF922B21);
            holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_red);
            holder.tvBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvBadge.setVisibility(View.GONE);
        }
    }

    // ──────────────────────────────────────────────────────────────
    //  Bind: Bài mới nhất
    // ──────────────────────────────────────────────────────────────

    private void bindNewArticle(NewArticleViewHolder holder, Article article,
                                int textMain, int textSub, int textTime, int accent) {
        holder.tvTitle.setText(article.getTitle() != null ? article.getTitle() : "");
        holder.tvTitle.setTextColor(textMain);

        String author = article.getAuthor() != null ? article.getAuthor() : "Ẩn danh";
        holder.tvAuthor.setText(author);
        holder.tvAuthor.setTextColor(textSub);

        holder.tvAvatar.setText(getInitials(author));
        holder.tvTime.setText(getRelativeTime(article.getApprovedAt()));
        holder.tvTime.setTextColor(textTime);

        String category = article.getCategory();
        if (category != null && !category.isEmpty()) {
            holder.tvCategory.setText(category.toUpperCase());
            holder.tvCategory.setTextColor(accent);
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        loadThumbnail(holder.imgThumb, article.getImagePath());
    }

    // ──────────────────────────────────────────────────────────────
    //  Load thumbnail
    // ──────────────────────────────────────────────────────────────

    private void loadThumbnail(ImageView imageView, String imagePath) {
        if (imageView == null) return;
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Glide.with(context)
                        .load(imgFile)
                        .apply(new RequestOptions()
                                .transforms(new CenterCrop(), new RoundedCorners(16))
                                .placeholder(R.drawable.ic_article_placeholder)
                                .error(R.drawable.ic_article_placeholder))
                        .into(imageView);
                return;
            }
        }
        Glide.with(context)
                .load(R.drawable.ic_article_placeholder)
                .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(16)))
                .into(imageView);
    }

    // ──────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────

    private String getRelativeTime(long timestampMs) {
        if (timestampMs <= 0) return "";
        long diff    = System.currentTimeMillis() - timestampMs;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours   = TimeUnit.MILLISECONDS.toHours(diff);
        long days    = TimeUnit.MILLISECONDS.toDays(diff);
        if (diff    < TimeUnit.MINUTES.toMillis(1)) return "Vừa xong";
        if (minutes < 60) return minutes + " phút trước";
        if (hours   < 24) return hours   + " giờ trước";
        if (days    < 7)  return days    + " ngày trước";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(timestampMs));
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    // ══════════════════════════════════════════════════════════════
    //  VIEW HOLDERS
    // ══════════════════════════════════════════════════════════════

    static class MyArticleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView  tvTitle, tvBadge, tvTime;
        MyArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_notif_thumb);
            tvTitle  = itemView.findViewById(R.id.tv_notif_title);
            tvBadge  = itemView.findViewById(R.id.tv_notif_badge);
            tvTime   = itemView.findViewById(R.id.tv_notif_time);
        }
    }

    static class NewArticleViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView  tvTitle, tvCategory, tvAuthor, tvAvatar, tvTime;
        NewArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb   = itemView.findViewById(R.id.img_notif_thumb);
            tvTitle    = itemView.findViewById(R.id.tv_notif_title);
            tvCategory = itemView.findViewById(R.id.tv_notif_category);
            tvAuthor   = itemView.findViewById(R.id.tv_notif_author);
            tvAvatar   = itemView.findViewById(R.id.tv_notif_avatar);
            tvTime     = itemView.findViewById(R.id.tv_notif_time);
        }
    }
}