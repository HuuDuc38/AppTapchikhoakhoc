package com.example.apptapchikhoakhoc.admin;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.model.UserItem;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.util.List;

public class UserStatsAdapter extends RecyclerView.Adapter<UserStatsAdapter.ViewHolder> {

    private final Context        context;
    private final List<UserItem> items;
    private final boolean        isDarkMode;

    public UserStatsAdapter(Context context, List<UserItem> items, boolean isDarkMode) {
        this.context    = context;
        this.items      = items;
        this.isDarkMode = isDarkMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_user_stat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        UserItem item = items.get(position);

        // Avatar chữ cái đầu
        String name   = item.getName();
        String avatar = (!name.isEmpty())
                ? String.valueOf(name.charAt(0)).toUpperCase()
                : "?";
        h.tvAvatar.setText(avatar);

        // Số thứ tự
        h.tvIndex.setText(String.valueOf(position + 1));

        // Nội dung
        h.tvName.setText(name);
        h.tvEmail.setText(item.getEmail());
        h.tvComments.setText(String.valueOf(item.getTotalComments()));
        h.tvLikes.setText(String.valueOf(item.getTotalLikes()));

        // Theme
        if (isDarkMode) {
            h.card.setCardBackgroundColor(AdminThemeManager.DarkColors.CARD_BACKGROUND);
            h.tvIndex.setTextColor(Color.parseColor("#666666"));
            h.tvName.setTextColor(Color.WHITE);
            h.tvEmail.setTextColor(Color.parseColor("#AAAAAA"));
            h.tvComments.setTextColor(Color.parseColor("#64B5F6"));
            h.tvLikes.setTextColor(Color.parseColor("#FF6B8A"));
        } else {
            h.card.setCardBackgroundColor(Color.WHITE);
            h.tvIndex.setTextColor(Color.parseColor("#999999"));
            h.tvName.setTextColor(Color.parseColor("#111111"));
            h.tvEmail.setTextColor(Color.parseColor("#777777"));
            h.tvComments.setTextColor(Color.parseColor("#1565C0"));
            h.tvLikes.setTextColor(Color.parseColor("#C8463D"));
        }

        // Fade-in animation
        h.itemView.setAlpha(0f);
        h.itemView.animate()
                .alpha(1f)
                .setDuration(250)
                .setStartDelay(position * 40L)
                .start();
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvIndex, tvAvatar, tvName, tvEmail, tvComments, tvLikes;

        ViewHolder(@NonNull View v) {
            super(v);
            card       = v.findViewById(R.id.card);
            tvIndex    = v.findViewById(R.id.tvIndex);
            tvAvatar   = v.findViewById(R.id.tvAvatar);
            tvName     = v.findViewById(R.id.tvName);
            tvEmail    = v.findViewById(R.id.tvEmail);
            tvComments = v.findViewById(R.id.tvComments);
            tvLikes    = v.findViewById(R.id.tvLikes);
        }
    }
}