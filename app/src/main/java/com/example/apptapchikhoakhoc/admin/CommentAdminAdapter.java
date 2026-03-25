package com.example.apptapchikhoakhoc.admin;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
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
import com.example.apptapchikhoakhoc.model.Comment;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdminAdapter extends RecyclerView.Adapter<CommentAdminAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(Comment item, int position);
    }

    private final Context          context;
    private       List<Comment>    items;
    private       boolean          isDarkMode;
    private final OnDeleteListener onDelete;

    public CommentAdminAdapter(Context context, List<Comment> items,
                               boolean isDarkMode, OnDeleteListener onDelete) {
        this.context    = context;
        this.items      = items;
        this.isDarkMode = isDarkMode;
        this.onDelete   = onDelete;
    }

    // Cập nhật data + theme cùng lúc
    public void updateData(List<Comment> newItems, boolean isDarkMode) {
        this.items      = newItems;
        this.isDarkMode = isDarkMode;
        notifyDataSetChanged();
    }

    // Giữ lại cho tương thích
    public void updateData(List<Comment> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_comment_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Comment item = items.get(position);

        // ── Avatar ──
        String name   = item.getUserName();
        String avatar = (!name.isEmpty()) ? String.valueOf(name.charAt(0)).toUpperCase() : "?";
        h.tvAvatar.setText(avatar);

        // ── Nội dung ──
        h.tvUserName.setText(name);
        h.tvContent.setText(item.getContent());
        h.tvArticleTitle.setText("Bài: " + item.getArticleTitle());
        h.tvTime.setText(formatTime(item.getTimestamp()));

        // ── Vi phạm ──
        if (item.isViolated()) {
            h.badgeViolated.setVisibility(View.VISIBLE);
            h.dividerViolated.setVisibility(View.VISIBLE);
            h.layoutViolatedWarn.setVisibility(View.VISIBLE);
            h.card.setCardBackgroundColor(
                    isDarkMode
                            ? Color.parseColor("#4D1A1A")
                            : Color.parseColor("#FFF5F5")
            );
        } else {
            h.badgeViolated.setVisibility(View.GONE);
            h.dividerViolated.setVisibility(View.GONE);
            h.layoutViolatedWarn.setVisibility(View.GONE);
            h.card.setCardBackgroundColor(
                    isDarkMode
                            ? AdminThemeManager.DarkColors.CARD_BACKGROUND
                            : Color.WHITE
            );
        }

        // ── Theme text ──
        if (isDarkMode) {
            h.tvUserName.setTextColor(Color.WHITE);
            h.tvUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);

            h.tvContent.setTextColor(Color.parseColor("#E0E0E0"));

            h.tvArticleTitle.setTextColor(Color.parseColor("#AAAAAA"));

            h.tvTime.setTextColor(Color.parseColor("#999999"));

        } else {
            h.tvUserName.setTextColor(Color.parseColor("#111111"));
            h.tvUserName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);

            h.tvContent.setTextColor(Color.parseColor("#222222"));

            h.tvArticleTitle.setTextColor(Color.parseColor("#666666"));

            h.tvTime.setTextColor(Color.parseColor("#666666"));
        }

        // ── Nút xóa ──
        h.btnDelete.setOnClickListener(v -> {
            if (onDelete != null) onDelete.onDelete(item, h.getAdapterPosition());
        });

        // ── Fade-in animation ──
        h.itemView.setAlpha(0f);
        h.itemView.animate()
                .alpha(1f)
                .setDuration(250)
                .setStartDelay(position * 40L)
                .start();
    }

    private String formatTime(long timestamp) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView     card;
        TextView     tvAvatar, tvUserName, tvContent;
        TextView     tvArticleTitle, tvTime, badgeViolated;
        ImageView    btnDelete;
        View         dividerViolated;
        LinearLayout layoutViolatedWarn;

        ViewHolder(@NonNull View v) {
            super(v);
            card               = v.findViewById(R.id.card);
            tvAvatar           = v.findViewById(R.id.tvAvatar);
            tvUserName         = v.findViewById(R.id.tvUserName);
            tvContent          = v.findViewById(R.id.tvContent);
            tvArticleTitle     = v.findViewById(R.id.tvArticleTitle);
            tvTime             = v.findViewById(R.id.tvTime);
            badgeViolated      = v.findViewById(R.id.badgeViolated);
            btnDelete          = v.findViewById(R.id.btnDelete);
            dividerViolated    = v.findViewById(R.id.dividerViolated);
            layoutViolatedWarn = v.findViewById(R.id.layoutViolatedWarn);
        }
    }
}