package com.example.apptapchikhoakhoc.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeaturedBannerAdapter extends RecyclerView.Adapter<FeaturedBannerAdapter.ViewHolder> {

    private static final String TAG = "FeaturedBannerAdapter";
    private Context context;
    private List<Article> articles;
    private OnItemClickListener listener;
    private boolean isDarkMode;

    public interface OnItemClickListener {
        void onItemClick(Article article);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public FeaturedBannerAdapter(Context context, List<Article> articles) {
        this.context    = context;
        this.articles   = articles;
        this.isDarkMode = ThemeManager.isDarkMode(context);
        Log.d(TAG, "Adapter created with " + (articles != null ? articles.size() : 0) + " articles");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_featured_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            Article article = articles.get(position);

            // ① Áp dụng theme TRƯỚC khi set data
            applyThemeToBanner(holder);

            // ② Tiêu đề
            if (holder.tvTitle != null && article.getTitle() != null) {
                holder.tvTitle.setText(article.getTitle());
            }

            // ③ Ngày tháng — dùng approvedAt (timestamp ms)
            if (holder.tvDate != null) {
                long ts = article.getApprovedAt();
                Log.d(TAG, "approvedAt = " + ts);
                if (ts > 0) {
                    String dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(new Date(ts));
                    holder.tvDate.setText(dateStr);
                    holder.tvDate.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDate.setText("");
                    holder.tvDate.setVisibility(View.GONE);
                }
            }

            // ④ Snippet — strip HTML + entities
            String snippet = buildSnippet(article);
            if (holder.tvSnippet != null) {
                if (snippet != null && !snippet.isEmpty()) {
                    holder.tvSnippet.setVisibility(View.VISIBLE);
                    holder.tvSnippet.setText(snippet);
                } else {
                    holder.tvSnippet.setVisibility(View.GONE);
                }
            }

            // ⑤ Ảnh banner
            if (holder.imgBanner != null) {
                if (article.getImagePath() != null && !article.getImagePath().isEmpty()) {
                    Glide.with(context)
                            .load(article.getImagePath())
                            .placeholder(R.drawable.placeholder_news)
                            .error(R.drawable.ic_broken_image)
                            .centerCrop()
                            .into(holder.imgBanner);
                } else {
                    holder.imgBanner.setImageResource(R.drawable.placeholder_news);
                }
            }

            // ⑥ Click
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(article);
            });

        } catch (Exception e) {
            Log.e(TAG, "Error binding view at position " + position, e);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  SNIPPET — strip HTML tags + decode HTML entities
    // ══════════════════════════════════════════════════════════

    private String buildSnippet(Article article) {
        if (article == null) return "";
        try {
            String raw = article.getContent();

            if (raw == null || raw.isEmpty()) return "";

            // Strip HTML: dùng Html.fromHtml để decode entities + tags đúng nhất
            String plain;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                plain = Html.fromHtml(raw, Html.FROM_HTML_MODE_COMPACT).toString();
            } else {
                //noinspection deprecation
                plain = Html.fromHtml(raw).toString();
            }

            // Dọn khoảng trắng thừa
            plain = plain.replaceAll("\\s+", " ").trim();

            if (plain.isEmpty()) return "";
            return plain.length() > 100 ? plain.substring(0, 100) + "…" : plain;

        } catch (Exception e) {
            Log.e(TAG, "Error building snippet", e);
            return "";
        }
    }

    // ══════════════════════════════════════════════════════════
    //  THEME — đồng bộ hoàn toàn với ThemeManager
    // ══════════════════════════════════════════════════════════

    private void applyThemeToBanner(ViewHolder holder) {
        try {
            int cardBg     = isDarkMode
                    ? ThemeManager.DarkColors.CARD_BACKGROUND
                    : Color.WHITE;
            int titleClr   = isDarkMode
                    ? ThemeManager.DarkColors.TEXT_PRIMARY
                    : Color.parseColor("#333333");
            int subClr     = isDarkMode
                    ? ThemeManager.DarkColors.TEXT_SECONDARY
                    : Color.parseColor("#999999");
            int snippetClr = isDarkMode
                    ? ThemeManager.DarkColors.TEXT_TERTIARY
                    : Color.parseColor("#666666");

            if (holder.cardView   != null) holder.cardView.setCardBackgroundColor(cardBg);
            if (holder.layoutText != null) holder.layoutText.setBackgroundColor(cardBg);
            if (holder.tvTitle    != null) holder.tvTitle.setTextColor(titleClr);
            if (holder.tvDate     != null) holder.tvDate.setTextColor(subClr);
            if (holder.tvSnippet  != null) holder.tvSnippet.setTextColor(snippetClr);

        } catch (Exception e) {
            Log.e(TAG, "Error applying theme", e);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  PUBLIC HELPERS
    // ══════════════════════════════════════════════════════════

    @Override
    public int getItemCount() {
        return articles != null ? articles.size() : 0;
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles   = newArticles;
        this.isDarkMode = ThemeManager.isDarkMode(context);
        notifyDataSetChanged();
    }

    public void refreshTheme() {
        this.isDarkMode = ThemeManager.isDarkMode(context);
        notifyDataSetChanged();
    }

    // ══════════════════════════════════════════════════════════
    //  VIEW HOLDER
    // ══════════════════════════════════════════════════════════

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView     cardView;
        ImageView    imgBanner;
        TextView     tvTitle, tvDate, tvSnippet;
        LinearLayout layoutText;

        ViewHolder(View itemView) {
            super(itemView);
            try {
                cardView   = itemView.findViewById(R.id.cardViewBanner);
                imgBanner  = itemView.findViewById(R.id.imgBanner);
                tvTitle    = itemView.findViewById(R.id.tvBannerTitle);
                tvDate     = itemView.findViewById(R.id.tvBannerDate);
                tvSnippet  = itemView.findViewById(R.id.tvBannerSnippet);
                layoutText = itemView.findViewById(R.id.layoutBannerText);
            } catch (Exception e) {
                Log.e(TAG, "Error creating ViewHolder", e);
            }
        }
    }
}