package com.example.apptapchikhoakhoc.admin;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.AdminThemeManager;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ApproveArticleAdapter extends RecyclerView.Adapter<ApproveArticleAdapter.ViewHolder> {

    public interface OnActionListener {
        void onApprove(Article article, int position);
        void onReject(Article article, int position);
        void onDelete(Article article, int position);
    }

    public static final int TAB_PENDING  = 0;
    public static final int TAB_APPROVED = 1;
    public static final int TAB_REJECTED = 2;

    private List<Article>          displayList;
    private final OnActionListener listener;
    private int                    currentTab;

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public ApproveArticleAdapter(List<Article> list, int tab, OnActionListener listener) {
        this.displayList = new ArrayList<>(list);
        this.currentTab  = tab;
        this.listener    = listener;
    }

    public void updateList(List<Article> newList, int tab) {
        this.currentTab  = tab;
        this.displayList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position < 0 || position >= displayList.size()) return;
        displayList.remove(position);
        notifyItemRemoved(position);
    }

    private String stripHtml(String html) {
        if (html == null || html.isEmpty()) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim();
        else
            return Html.fromHtml(html).toString().trim();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_approve_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context ctx     = holder.itemView.getContext();
        Article article = displayList.get(position);

        holder.tvTitle.setText(article.getTitle());

        String content = stripHtml(article.getContent());
        if (content.length() > 120) content = content.substring(0, 120) + "...";
        // ✅ getString() thay vì hardcode
        holder.tvContent.setText(content.isEmpty()
                ? ctx.getString(R.string.approve_no_content) : content);

        String author = article.getAuthor();
        if (author == null || author.isEmpty()) author = ctx.getString(R.string.approve_anonymous);
        holder.tvAuthorName.setText(author);
        holder.tvAuthorAvatar.setText(String.valueOf(author.charAt(0)).toUpperCase());

        String email = article.getUserEmail();
        if (email == null || email.isEmpty()) email = ctx.getString(R.string.approve_unknown_account);
        holder.tvAuthorUsername.setText(email);

        String category = article.getCategory();
        if (category == null || category.isEmpty()) category = ctx.getString(R.string.approve_uncategorized);
        holder.tvCategory.setText(category);
        holder.tvCategoryTag.setText(category);

        // ✅ Thời gian duyệt/từ chối
        long approvedAt = article.getApprovedAt();
        if (approvedAt > 0) {
            String label = (currentTab == TAB_APPROVED)
                    ? ctx.getString(R.string.approve_time_approved)
                    : ctx.getString(R.string.approve_time_rejected);
            holder.tvApprovedTime.setText(label + DATE_FORMAT.format(new Date(approvedAt)));
            holder.layoutApprovedTime.setVisibility(View.VISIBLE);
        } else {
            holder.layoutApprovedTime.setVisibility(View.GONE);
        }

        // ✅ Trạng thái và nút — tất cả dùng getString()
        switch (currentTab) {
            case TAB_PENDING:
                holder.tvTime.setText(ctx.getString(R.string.approve_status_pending));
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnApprove.setText(ctx.getString(R.string.approve_btn_approve));
                holder.btnReject.setText(ctx.getString(R.string.approve_btn_reject));
                break;
            case TAB_APPROVED:
                holder.tvTime.setText(ctx.getString(R.string.approve_status_approved));
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReject.setText(ctx.getString(R.string.approve_btn_revoke));
                break;
            case TAB_REJECTED:
                holder.tvTime.setText(ctx.getString(R.string.approve_status_rejected));
                holder.btnApprove.setVisibility(View.VISIBLE);
                holder.btnApprove.setText(ctx.getString(R.string.approve_btn_reapprove));
                holder.btnReject.setVisibility(View.GONE);
                break;
        }

        holder.btnApprove.setEnabled(true);
        holder.btnReject.setEnabled(true);
        holder.btnDelete.setEnabled(true);
        holder.btnApprove.setAlpha(1f);
        holder.btnReject.setAlpha(1f);
        holder.btnDelete.setAlpha(1f);

        applyItemTheme(holder);

        holder.btnApprove.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.btnApprove.setAlpha(0.6f);
            holder.btnReject.setAlpha(0.6f);
            if (listener != null) listener.onApprove(article, pos);
        });

        holder.btnReject.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            holder.btnApprove.setEnabled(false);
            holder.btnReject.setEnabled(false);
            holder.btnApprove.setAlpha(0.6f);
            holder.btnReject.setAlpha(0.6f);
            if (listener != null) listener.onReject(article, pos);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            holder.btnDelete.setEnabled(false);
            holder.btnDelete.setAlpha(0.6f);
            if (listener != null) listener.onDelete(article, pos);
        });
    }

    private void applyItemTheme(ViewHolder holder) {
        Context ctx  = holder.itemView.getContext();
        boolean dark = AdminThemeManager.isDarkMode(ctx);

        if (dark) {
            holder.cardRoot.setCardBackgroundColor(AdminThemeManager.DarkColors.CARD_BACKGROUND);
            holder.tvTitle.setTextColor(AdminThemeManager.DarkColors.TEXT_PRIMARY);
            holder.tvContent.setTextColor(AdminThemeManager.DarkColors.TEXT_SECONDARY);
            holder.tvTime.setTextColor(AdminThemeManager.DarkColors.TEXT_TERTIARY);
            holder.tvApprovedTime.setTextColor(AdminThemeManager.DarkColors.ACCENT);
            holder.tvAuthorName.setTextColor(AdminThemeManager.DarkColors.TEXT_PRIMARY);
            holder.tvAuthorUsername.setTextColor(AdminThemeManager.DarkColors.ACCENT);
            holder.tvCategory.setTextColor(AdminThemeManager.DarkColors.TEXT_SECONDARY);
            holder.dividerItem.setBackgroundColor(AdminThemeManager.DarkColors.DIVIDER);

            holder.btnApprove.setTextColor(Color.WHITE);
            holder.btnApprove.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF2E7D")));
            holder.btnApprove.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#FF2E7D")));

            holder.btnReject.setTextColor(AdminThemeManager.DarkColors.ACCENT_DANGER);
            holder.btnReject.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E2D3F")));
            holder.btnReject.setStrokeColor(ColorStateList.valueOf(AdminThemeManager.DarkColors.ACCENT_DANGER));

            holder.btnDelete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1E2D3F")));
            holder.btnDelete.setStrokeColor(ColorStateList.valueOf(AdminThemeManager.DarkColors.DIVIDER));
        } else {
            holder.cardRoot.setCardBackgroundColor(AdminThemeManager.LightColors.CARD_BACKGROUND);
            holder.tvTitle.setTextColor(AdminThemeManager.LightColors.TEXT_PRIMARY);
            holder.tvContent.setTextColor(AdminThemeManager.LightColors.TEXT_SECONDARY);
            holder.tvTime.setTextColor(AdminThemeManager.LightColors.TEXT_TERTIARY);
            holder.tvApprovedTime.setTextColor(AdminThemeManager.LightColors.ACCENT);
            holder.tvAuthorName.setTextColor(AdminThemeManager.LightColors.TEXT_PRIMARY);
            holder.tvAuthorUsername.setTextColor(AdminThemeManager.LightColors.ACCENT);
            holder.tvCategory.setTextColor(AdminThemeManager.LightColors.TEXT_SECONDARY);
            holder.dividerItem.setBackgroundColor(AdminThemeManager.LightColors.DIVIDER);

            holder.btnApprove.setTextColor(Color.WHITE);
            holder.btnApprove.setBackgroundTintList(ColorStateList.valueOf(AdminThemeManager.LightColors.ACCENT));
            holder.btnApprove.setStrokeColor(ColorStateList.valueOf(AdminThemeManager.LightColors.ACCENT));

            holder.btnReject.setTextColor(AdminThemeManager.LightColors.ACCENT_DANGER);
            holder.btnReject.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFEBEE")));
            holder.btnReject.setStrokeColor(ColorStateList.valueOf(AdminThemeManager.LightColors.ACCENT_DANGER));

            holder.btnDelete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            holder.btnDelete.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
        }
    }

    @Override
    public int getItemCount() {
        return displayList == null ? 0 : displayList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView       cardRoot;
        TextView       tvTitle, tvContent, tvAuthorAvatar, tvAuthorName,
                tvAuthorUsername, tvCategory, tvCategoryTag,
                tvTime, tvApprovedTime;
        View           dividerItem;
        LinearLayout   layoutApprovedTime;
        MaterialButton btnApprove, btnReject, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRoot           = (CardView) itemView;
            tvTitle            = itemView.findViewById(R.id.tvArticleTitle);
            tvContent          = itemView.findViewById(R.id.tvArticleContent);
            tvAuthorAvatar     = itemView.findViewById(R.id.tvAuthorAvatar);
            tvAuthorName       = itemView.findViewById(R.id.tvAuthorName);
            tvAuthorUsername   = itemView.findViewById(R.id.tvAuthorUsername);
            tvCategory         = itemView.findViewById(R.id.tvArticleCategory);
            tvCategoryTag      = itemView.findViewById(R.id.tvCategoryTag);
            tvTime             = itemView.findViewById(R.id.tvArticleTime);
            tvApprovedTime     = itemView.findViewById(R.id.tvApprovedTime);
            dividerItem        = itemView.findViewById(R.id.dividerItem);
            layoutApprovedTime = itemView.findViewById(R.id.layoutApprovedTime);
            btnApprove         = itemView.findViewById(R.id.btnApprove);
            btnReject          = itemView.findViewById(R.id.btnReject);
            btnDelete          = itemView.findViewById(R.id.btnDelete);
        }
    }
}