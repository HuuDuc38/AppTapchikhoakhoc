package com.example.apptapchikhoakhoc.admin;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.List;

public class LikeStatsAdapter extends RecyclerView.Adapter<LikeStatsAdapter.ViewHolder> {

    private final Context               context;
    private final List<ArticleStatItem> items;
    private final boolean               isDarkMode;
    private final int                   maxCount;

    public LikeStatsAdapter(Context context, List<ArticleStatItem> items, boolean isDarkMode) {
        this.context   = context;
        this.items     = items;
        this.isDarkMode = isDarkMode;
        this.maxCount  = (items != null && !items.isEmpty()) ? items.get(0).getCount() : 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_like_stat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArticleStatItem item = items.get(position);

        // Nội dung
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvTitle.setText(item.getTitle());
        holder.tvLikeCount.setText(String.format("%,d", item.getCount()));

        if (!item.getCategory().isEmpty()) {
            holder.tvCategory.setVisibility(View.VISIBLE);
            holder.tvCategory.setText(item.getCategory());
        } else {
            holder.tvCategory.setVisibility(View.GONE);
        }

        // Progress bar animation
        int progress = maxCount > 0 ? (item.getCount() * 100 / maxCount) : 0;
        holder.progressBar.setProgress(0);
        ObjectAnimator anim = ObjectAnimator.ofInt(holder.progressBar, "progress", 0, progress);
        anim.setDuration(700);
        anim.setStartDelay(position * 80L);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

        // Rank badge
        applyRankBadge(holder.tvRank, position);

        // Theme
        applyTheme(holder);

        // Fade-in từng item
        holder.itemView.setAlpha(0f);
        holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(position * 60L)
                .start();
    }

    private void applyRankBadge(TextView tvRank, int position) {
        switch (position) {
            case 0:
                tvRank.setBackgroundResource(R.drawable.rank_gold);
                tvRank.setTextColor(Color.parseColor("#B8860B"));
                break;
            case 1:
                tvRank.setBackgroundResource(R.drawable.rank_silver);
                tvRank.setTextColor(Color.parseColor("#707070"));
                break;
            case 2:
                tvRank.setBackgroundResource(R.drawable.rank_bronze);
                tvRank.setTextColor(Color.parseColor("#A0522D"));
                break;
            default:
                tvRank.setBackgroundResource(R.drawable.rank_normal);
                tvRank.setTextColor(Color.parseColor("#999999"));
                break;
        }
    }

    private void applyTheme(ViewHolder holder) {
        if (isDarkMode) {
            holder.card.setCardBackgroundColor(AdminThemeManager.DarkColors.CARD_BACKGROUND);
            holder.tvTitle.setTextColor(Color.WHITE);
            holder.tvCategory.setTextColor(Color.parseColor("#AAAAAA"));
            holder.tvLikeCount.setTextColor(Color.parseColor("#FF6B8A"));
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE);
            holder.tvTitle.setTextColor(Color.parseColor("#1A1A1A"));
            holder.tvCategory.setTextColor(Color.parseColor("#999999"));
            holder.tvLikeCount.setTextColor(Color.parseColor("#C8463D"));
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView    card;
        TextView    tvRank, tvTitle, tvCategory, tvLikeCount;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card        = itemView.findViewById(R.id.card);
            tvRank      = itemView.findViewById(R.id.tvRank);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvCategory  = itemView.findViewById(R.id.tvCategory);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}