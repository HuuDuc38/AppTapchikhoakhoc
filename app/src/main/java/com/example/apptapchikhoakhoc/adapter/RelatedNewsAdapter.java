package com.example.apptapchikhoakhoc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.apptapchikhoakhoc.main.ArticleDetailActivity;
import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.model.Article;

import java.util.List;

public class RelatedNewsAdapter extends RecyclerView.Adapter<RelatedNewsAdapter.RelatedViewHolder> {

    private final Context context;
    private final List<Article> relatedArticles;

    public RelatedNewsAdapter(Context context, List<Article> relatedArticles) {
        this.context = context;
        this.relatedArticles = relatedArticles;
    }

    @NonNull
    @Override
    public RelatedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_related_news, parent, false);
        return new RelatedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedViewHolder holder, int position) {
        Article article = relatedArticles.get(position);

        // Bắt buộc phải có tvTitle trong XML rồi mới được set
        holder.tvTitle.setText(article.getTitle());
        holder.tvAuthor.setText(article.getAuthor());
        holder.tvCategory.setText("Chuyên mục: " + article.getCategory());
        holder.tvDate.setText("20/11/2025"); // Sau này thay bằng article.getDate()

        // Load ảnh an toàn
        String imagePath = article.getImagePath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            Glide.with(context)
                    .load(imagePath)
                    .placeholder(R.drawable.ic_launcher_background)  // Tạo ảnh placeholder xám
                    .error(R.drawable.ic_launcher_background)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgThumbnail);
        } else {
            holder.imgThumbnail.setImageResource(R.drawable.ic_launcher_background);
        }

        // Click vào item → mở chi tiết (an toàn, không crash)
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra("article", article);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                // Không bao giờ crash ở đây nữa
            }
        });
    }

    @Override
    public int getItemCount() {
        return relatedArticles == null ? 0 : relatedArticles.size();
    }

    static class RelatedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView tvTitle, tvAuthor, tvCategory, tvDate;

        public RelatedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);          // BẮT BUỘC PHẢI CÓ TRONG XML
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}