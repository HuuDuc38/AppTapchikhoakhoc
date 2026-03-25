package com.example.apptapchikhoakhoc.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.main.ArticleDetailActivity;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private Context context;
    private List<Article> articles;
    private boolean isDarkMode;

    public NewsAdapter(Context context) {
        this.context = context;
        this.articles = new ArrayList<>();
        this.isDarkMode = ThemeManager.isDarkMode(context);
    }

    public void setList(List<Article> newArticles) {
        final List<Article> newList = newArticles != null ? newArticles : new ArrayList<>();
        this.isDarkMode = ThemeManager.isDarkMode(context);

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return articles.size(); }
            @Override public int getNewListSize() { return newList.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return articles.get(oldPos).getId() == newList.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                String oTitle = articles.get(oldPos).getTitle() != null ? articles.get(oldPos).getTitle() : "";
                String nTitle = newList.get(newPos).getTitle() != null ? newList.get(newPos).getTitle() : "";
                return oTitle.equals(nTitle);
            }
        });

        articles = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);

        applyTheme(holder);

        holder.tvTitle.setText(article.getTitle());

        String author = article.getAuthor();
        holder.tvAuthor.setText((author != null && !author.isEmpty()) ? author : "Không rõ");

        String category = article.getCategory();
        if (category != null && !category.isEmpty()) {
            holder.tvCategory.setText(category);
            setCategoryBadge(holder.tvCategory, category);
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        holder.tvDate.setText(formatDate(article.getApprovedAt()));

        if (article.getImagePath() != null && !article.getImagePath().isEmpty()) {
            Glide.with(context)
                    .load(article.getImagePath())
                    .placeholder(R.drawable.placeholder_news)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(holder.imgThumb);
        } else {
            holder.imgThumb.setImageResource(R.drawable.placeholder_news);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            intent.putExtra("article", article);
            context.startActivity(intent);
        });
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) return "Chưa có ngày";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    /**
     * Badge màu nền đặc — chữ trắng, khớp đúng tên danh mục trong app.
     */
    private void setCategoryBadge(TextView tv, String category) {
        int bgColor = getBadgeColor(category);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(bgColor);
        drawable.setCornerRadius(dpToPx(6));

        tv.setBackground(drawable);
        tv.setTextColor(Color.WHITE);
    }

    private int getBadgeColor(String category) {
        if (category == null) return Color.parseColor("#9E9E9E");

        switch (category.trim()) {
            case "Tin tức sự kiện":
                return Color.parseColor("#1565C0"); // xanh dương đậm
            case "Đào tạo":
                return Color.parseColor("#2E7D32"); // xanh lá đậm
            case "Thông tin việc làm":
                return Color.parseColor("#E65100"); // cam đậm
            case "Khoa học công nghệ":
                return Color.parseColor("#6A1B9A"); // tím
            case "Hợp tác quốc tế":
                return Color.parseColor("#00838F"); // xanh ngọc
            default:
                // Màu fallback dựa theo ký tự đầu — đảm bảo luôn có màu
                int hash = category.hashCode();
                int[] fallbacks = {
                        0xFF1565C0, 0xFF2E7D32, 0xFFE65100,
                        0xFF6A1B9A, 0xFF00838F, 0xFFC62828
                };
                return fallbacks[Math.abs(hash) % fallbacks.length];
        }
    }

    private float dpToPx(int dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    private void applyTheme(ViewHolder holder) {
        if (isDarkMode) {
            holder.itemView.setBackgroundColor(Color.parseColor("#243447"));
            holder.tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
            holder.tvAuthor.setTextColor(Color.parseColor("#B0BEC5"));
            holder.tvDate.setTextColor(Color.parseColor("#B0BEC5"));
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
            holder.tvTitle.setTextColor(Color.parseColor("#1A1A2E"));
            holder.tvAuthor.setTextColor(Color.parseColor("#888888"));
            holder.tvDate.setTextColor(Color.parseColor("#AAAAAA"));
        }
    }

    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvTitle, tvAuthor, tvCategory, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            imgThumb   = itemView.findViewById(R.id.imgNewsThumb);
            tvTitle    = itemView.findViewById(R.id.tvNewsTitle);
            tvAuthor   = itemView.findViewById(R.id.tvNewsAuthor);
            tvCategory = itemView.findViewById(R.id.tvNewsCategory);
            tvDate     = itemView.findViewById(R.id.tvNewsDate);

            View btnEdit   = itemView.findViewById(R.id.btnSmallEdit);
            View btnDelete = itemView.findViewById(R.id.btnSmallDelete);
            if (btnEdit   != null) btnEdit.setVisibility(View.GONE);
            if (btnDelete != null) btnDelete.setVisibility(View.GONE);
        }
    }
}