package com.example.apptapchikhoakhoc.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.VH> {

    private final Context       context;
    private       List<Article> list;
    private final boolean       isDark;

    public FeaturedAdapter(Context context, List<Article> list) {
        this.context = context;
        this.list    = list;
        this.isDark  = ThemeManager.isDarkMode(context);
    }

    public void updateList(List<Article> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_featured_small, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Article a = list.get(position);

        int cardColor   = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.WHITE;
        int textMain    = isDark ? ThemeManager.DarkColors.TEXT_PRIMARY     : Color.parseColor("#1A1A1A");
        int textSub     = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY   : Color.parseColor("#999999");
        int textTime    = isDark ? ThemeManager.DarkColors.TEXT_TERTIARY    : Color.parseColor("#BBBBBB");
        int accentColor = isDark ? ThemeManager.DarkColors.ACCENT           : Color.parseColor("#C0392B");

        holder.itemView.setBackgroundColor(cardColor);

        holder.tvTitle.setText(a.getTitle() != null ? a.getTitle() : "");
        holder.tvTitle.setTextColor(textMain);

        String author = a.getAuthor() != null ? a.getAuthor() : "Ẩn danh";
        holder.tvAuthor.setText(author);
        holder.tvAuthor.setTextColor(textSub);
        holder.tvAvatar.setText(getInitials(author));
        holder.tvTime.setText(getRelativeTime(a.getApprovedAt()));
        holder.tvTime.setTextColor(textTime);

        String cat = a.getCategory();
        if (cat != null && !cat.isEmpty()) {
            holder.tvCategory.setText(cat.toUpperCase());
            holder.tvCategory.setTextColor(accentColor);
            holder.tvCategory.setVisibility(View.VISIBLE);
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        if (a.getImagePath() != null) {
            File f = new File(a.getImagePath());
            if (f.exists()) {
                Glide.with(context)
                        .load(f)
                        .apply(new RequestOptions()
                                .transforms(new CenterCrop(), new RoundedCorners(12))
                                .placeholder(R.drawable.ic_article_placeholder))
                        .into(holder.imgThumb);
            } else {
                holder.imgThumb.setImageResource(R.drawable.ic_article_placeholder);
            }
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_article_placeholder);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ArticleDetailActivity.class);
            intent.putExtra("article", a);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    private String getRelativeTime(long ts) {
        if (ts <= 0) return "";
        long diff = System.currentTimeMillis() - ts;
        long m = TimeUnit.MILLISECONDS.toMinutes(diff);
        long h = TimeUnit.MILLISECONDS.toHours(diff);
        long d = TimeUnit.MILLISECONDS.toDays(diff);
        if (diff < TimeUnit.MINUTES.toMillis(1)) return "Vừa xong";
        if (m < 60) return m + " phút trước";
        if (h < 24) return h + " giờ trước";
        if (d < 7)  return d + " ngày trước";
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(ts));
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] p = name.trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, Math.min(2, p[0].length())).toUpperCase();
        return (p[0].substring(0, 1) + p[p.length - 1].substring(0, 1)).toUpperCase();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView  tvTitle, tvCategory, tvAuthor, tvAvatar, tvTime;
        VH(@NonNull View v) {
            super(v);
            imgThumb   = v.findViewById(R.id.img_featured_thumb);
            tvTitle    = v.findViewById(R.id.tv_featured_title);
            tvCategory = v.findViewById(R.id.tv_featured_category);
            tvAuthor   = v.findViewById(R.id.tv_featured_author);
            tvAvatar   = v.findViewById(R.id.tv_featured_avatar);
            tvTime     = v.findViewById(R.id.tv_featured_time);
        }
    }
}