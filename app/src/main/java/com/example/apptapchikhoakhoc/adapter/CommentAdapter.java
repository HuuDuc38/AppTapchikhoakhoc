package com.example.apptapchikhoakhoc.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptapchikhoakhoc.R;
import com.example.apptapchikhoakhoc.data.DatabaseHelper;
import com.example.apptapchikhoakhoc.model.Comment;
import com.example.apptapchikhoakhoc.utils.ThemeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    public interface OnReplyListener {
        void onSendReply(Comment parentComment, String replyText, int position);
        String getMyAvatarLetter();
    }

    private final List<Comment>         list             = new ArrayList<>();
    private final Map<Integer, String>  reactionMap      = new HashMap<>();
    private final Map<Integer, Integer> reactionCountMap = new HashMap<>();
    private       OnReplyListener       replyListener;
    private       DatabaseHelper        db;
    private       String                currentUserEmail = "";
    private       Context               context;

    public void setReplyListener(OnReplyListener listener) { this.replyListener = listener; }

    public void setDatabase(DatabaseHelper db, String userEmail, Context context) {
        this.db      = db;
        this.context = context;

        String newEmail = userEmail != null ? userEmail : "";

        // ✅ Nếu đổi user → xóa toàn bộ reaction cache của user cũ
        if (!newEmail.equals(this.currentUserEmail)) {
            reactionMap.clear();
            reactionCountMap.clear();
        }

        this.currentUserEmail = newEmail;

        // ✅ Reload reactions theo user mới nếu đã có data
        if (!list.isEmpty()) {
            loadReactionsFromDb();
            notifyDataSetChanged();
        }
    }

    /** Giữ tương thích ngược */
    public void setDatabase(DatabaseHelper db, String userEmail) {
        setDatabase(db, userEmail, context);
    }

    private String getEmailFromPrefs(Context ctx) {
        if (ctx == null) ctx = context;
        if (ctx == null) return currentUserEmail;
        SharedPreferences prefs = ctx.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("email", "");
        if (email == null) email = "";
        // ✅ Nếu email từ prefs khác email cache → user vừa đổi, clear cache
        if (!email.isEmpty() && !email.equals(currentUserEmail)) {
            reactionMap.clear();
            reactionCountMap.clear();
            currentUserEmail = email;
        }
        return currentUserEmail;
    }

    public void setList(List<Comment> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        // ✅ Luôn clear reaction cache trước khi load lại cho danh sách mới
        reactionMap.clear();
        reactionCountMap.clear();
        loadReactionsFromDb();
        notifyDataSetChanged();
    }

    private void loadReactionsFromDb() {
        if (db == null || list.isEmpty()) return;

        // ✅ Luôn đọc email mới nhất từ SharedPrefs
        if (context != null) getEmailFromPrefs(context);

        List<Integer> ids = new ArrayList<>();
        for (Comment c : list) ids.add(c.getId());

        reactionMap.clear();
        if (!currentUserEmail.isEmpty()) {
            Map<Integer, String> userReactions =
                    db.getCommentReactionsForUser(ids, currentUserEmail);
            reactionMap.putAll(userReactions);
        }

        reactionCountMap.clear();
        Map<Integer, Integer> counts = db.getCommentReactionCounts(ids);
        reactionCountMap.putAll(counts);
    }

    public void addComment(Comment comment) {
        list.add(0, comment);
        notifyItemInserted(0);
    }

    private String getRelativeTime(long timestamp) {
        long diff    = System.currentTimeMillis() - timestamp;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours / 24;
        long weeks   = days / 7;
        if (seconds < 60) return "Vừa xong";
        if (minutes < 60) return minutes + " phút";
        if (hours   < 24) return hours + " giờ";
        if (days    <  7) return days + " ngày";
        if (weeks   <  4) return weeks + " tuần";
        return (days / 30) + " tháng";
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment c      = list.get(position);
        Context ctx    = holder.itemView.getContext();
        boolean isDark = ThemeManager.isDarkMode(ctx);

        applyItemTheme(holder, isDark);

        String name = c.getUserName();
        holder.tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        holder.tvName.setText(name);
        holder.tvTime.setText(getRelativeTime(c.getTimestamp()));
        holder.tvContent.setText(c.getContent());

        applyReactionWithCount(holder, reactionMap.get(c.getId()), c.getId(), isDark);

        // ── Nút THÍCH ──
        holder.btnLike.setOnClickListener(v -> {
            String email = getEmailFromPrefs(ctx);
            if (email.isEmpty()) {
                Toast.makeText(ctx, "Đăng nhập để thích bình luận", Toast.LENGTH_SHORT).show();
                return;
            }
            String current = reactionMap.get(c.getId());
            if (current != null) {
                reactionMap.remove(c.getId());
                int cnt = reactionCountMap.getOrDefault(c.getId(), 1);
                reactionCountMap.put(c.getId(), Math.max(0, cnt - 1));
                if (db != null) db.removeCommentReaction(c.getId(), email);
                applyReactionWithCount(holder, null, c.getId(), isDark);
            } else {
                reactionMap.put(c.getId(), "👍");
                int cnt = reactionCountMap.getOrDefault(c.getId(), 0);
                reactionCountMap.put(c.getId(), cnt + 1);
                if (db != null) db.addOrUpdateCommentReaction(c.getId(), email, "👍");
                applyReactionWithCount(holder, "👍", c.getId(), isDark);
            }
        });

        holder.btnLike.setOnLongClickListener(v -> {
            String email = getEmailFromPrefs(ctx);
            if (email.isEmpty()) {
                Toast.makeText(ctx, "Đăng nhập để thích bình luận", Toast.LENGTH_SHORT).show();
                return true;
            }
            showReactionPopup(ctx, holder, c, isDark);
            return true;
        });

        // ── Nút TRẢ LỜI ──
        holder.btnReply.setOnClickListener(v -> {
            boolean visible = holder.layoutReply.getVisibility() == View.VISIBLE;
            if (visible) {
                holder.layoutReply.setVisibility(View.GONE);
            } else {
                holder.layoutReply.setVisibility(View.VISIBLE);
                String letter = replyListener != null ? replyListener.getMyAvatarLetter() : "?";
                holder.tvReplyAvatar.setText(letter);
                holder.etReply.requestFocus();
            }
        });

        holder.etReply.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int cnt, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int cnt) {
                holder.btnSendReply.setVisibility(
                        s.toString().trim().isEmpty() ? View.GONE : View.VISIBLE);
                int sendColor = isDark ? ThemeManager.DarkColors.ACCENT : Color.parseColor("#FF2E7D");
                holder.btnSendReply.setColorFilter(sendColor);
            }
        });

        holder.btnSendReply.setOnClickListener(v -> {
            String text = holder.etReply.getText().toString().trim();
            if (text.isEmpty()) return;
            if (replyListener != null)
                replyListener.onSendReply(c, text, holder.getAdapterPosition());
            addReplyView(ctx, holder.layoutReplies, name, text, isDark);
            holder.layoutReplies.setVisibility(View.VISIBLE);
            holder.etReply.setText("");
            holder.layoutReply.setVisibility(View.GONE);
            Toast.makeText(ctx, "Đã gửi trả lời", Toast.LENGTH_SHORT).show();
        });
    }

    private void applyItemTheme(ViewHolder holder, boolean isDark) {
        int textPrimary = isDark ? Color.WHITE                             : Color.parseColor("#1C1E21");
        int textSecond  = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#65676B");
        int hintColor   = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#8A8D91");
        int bubbleBg    = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.parseColor("#F0F2F5");
        int inputBg     = isDark ? ThemeManager.DarkColors.BACKGROUND      : Color.parseColor("#F0F2F5");

        holder.tvName.setTextColor(textPrimary);
        holder.tvContent.setTextColor(textPrimary);
        holder.tvTime.setTextColor(textSecond);
        holder.btnLike.setTextColor(textSecond);
        holder.btnReply.setTextColor(textSecond);
        holder.tvAvatar.setTextColor(Color.WHITE);

        if (holder.etReply != null) {
            holder.etReply.setTextColor(textPrimary);
            holder.etReply.setHintTextColor(hintColor);
            if (holder.etReply.getParent() instanceof View) {
                View parent = (View) holder.etReply.getParent();
                android.graphics.drawable.GradientDrawable d =
                        new android.graphics.drawable.GradientDrawable();
                d.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                d.setCornerRadius(24f);
                d.setColor(inputBg);
                parent.setBackground(d);
            }
        }

        if (holder.bubbleLayout != null) {
            android.graphics.drawable.GradientDrawable bubble =
                    new android.graphics.drawable.GradientDrawable();
            bubble.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            bubble.setCornerRadii(new float[]{ 0f, 0f, 18f, 18f, 18f, 18f, 18f, 18f });
            bubble.setColor(bubbleBg);
            holder.bubbleLayout.setBackground(bubble);
        }
    }

    private void showReactionPopup(Context ctx, ViewHolder holder,
                                   Comment comment, boolean isDark) {
        View popup = LayoutInflater.from(ctx).inflate(R.layout.reaction_popup, null);
        if (isDark) popup.setBackgroundColor(ThemeManager.DarkColors.CARD_BACKGROUND);

        PopupWindow pw = new PopupWindow(popup,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        pw.setElevation(12f);
        pw.setOutsideTouchable(true);
        pw.showAsDropDown(holder.btnLike, 0, -180, Gravity.START);

        int[]    ids    = { R.id.btnReactLike, R.id.btnReactLove, R.id.btnReactHaha,
                R.id.btnReactWow,  R.id.btnReactSad,  R.id.btnReactAngry };
        String[] emojis = { "👍","❤️","😆","😮","😢","😡" };

        Handler handler = new Handler();
        for (int i = 0; i < ids.length; i++) {
            final int idx = i;
            TextView btn = popup.findViewById(ids[i]);
            if (btn == null) continue;
            btn.setScaleX(0.3f); btn.setScaleY(0.3f); btn.setAlpha(0f);
            handler.postDelayed(() ->
                            btn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(150).start(),
                    idx * 50L);

            String saved = reactionMap.get(comment.getId());
            if (emojis[i].equals(saved)) { btn.setScaleX(1.3f); btn.setScaleY(1.3f); }

            btn.setOnClickListener(v -> {
                String email = getEmailFromPrefs(ctx);
                String prev  = reactionMap.get(comment.getId());
                if (emojis[idx].equals(prev)) {
                    reactionMap.remove(comment.getId());
                    int cnt = reactionCountMap.getOrDefault(comment.getId(), 1);
                    reactionCountMap.put(comment.getId(), Math.max(0, cnt - 1));
                    if (db != null) db.removeCommentReaction(comment.getId(), email);
                    applyReactionWithCount(holder, null, comment.getId(), isDark);
                } else {
                    boolean isNew = prev == null;
                    reactionMap.put(comment.getId(), emojis[idx]);
                    if (isNew) {
                        int cnt = reactionCountMap.getOrDefault(comment.getId(), 0);
                        reactionCountMap.put(comment.getId(), cnt + 1);
                    }
                    if (db != null) db.addOrUpdateCommentReaction(comment.getId(), email, emojis[idx]);
                    applyReactionWithCount(holder, emojis[idx], comment.getId(), isDark);
                }
                pw.dismiss();
            });

            btn.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    v.animate().scaleX(1.4f).scaleY(1.4f).setDuration(100).start();
                else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL)
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                return false;
            });
        }
    }

    private void applyReactionWithCount(ViewHolder holder, String emoji,
                                        int commentId, boolean isDark) {
        int defaultColor = isDark
                ? ThemeManager.DarkColors.TEXT_SECONDARY
                : Color.parseColor("#65676B");
        int count = commentId >= 0 ? reactionCountMap.getOrDefault(commentId, 0) : 0;

        if (emoji == null) {
            holder.btnLike.setText(count > 0 ? "Thích · " + count : "Thích");
            holder.btnLike.setTextColor(defaultColor);
            if (holder.tvReactionBadge != null) holder.tvReactionBadge.setVisibility(View.GONE);
        } else {
            holder.btnLike.setText(emoji + " " + getReactionLabel(emoji)
                    + (count > 0 ? " · " + count : ""));
            holder.btnLike.setTextColor(getReactionColor(emoji));
            if (holder.tvReactionBadge != null) {
                holder.tvReactionBadge.setText(emoji);
                holder.tvReactionBadge.setVisibility(View.VISIBLE);
            }
        }
        if (holder.tvReactionCount != null) holder.tvReactionCount.setVisibility(View.GONE);
    }

    private String getReactionLabel(String emoji) {
        switch (emoji) {
            case "👍": return "Thích";
            case "❤️": return "Yêu thích";
            case "😆": return "Haha";
            case "😮": return "Wow";
            case "😢": return "Buồn";
            case "😡": return "Phẫn nộ";
            default:   return "Thích";
        }
    }

    private int getReactionColor(String emoji) {
        switch (emoji) {
            case "👍": return Color.parseColor("#1877F2");
            case "❤️": return Color.parseColor("#F33E58");
            case "😆": case "😮": case "😢": return Color.parseColor("#F7B125");
            case "😡": return Color.parseColor("#E9710F");
            default:   return Color.parseColor("#1877F2");
        }
    }

    private void addReplyView(Context ctx, LinearLayout container,
                              String parentName, String replyText, boolean isDark) {
        View replyView = LayoutInflater.from(ctx)
                .inflate(R.layout.item_reply, container, false);

        TextView     tvName       = replyView.findViewById(R.id.tvReplyName);
        TextView     tvContent    = replyView.findViewById(R.id.tvReplyContent);
        TextView     tvTime       = replyView.findViewById(R.id.tvReplyTime);
        LinearLayout bubbleLayout = replyView.findViewById(R.id.replyBubbleLayout);

        if (tvName    != null) tvName.setText("Bạn");
        if (tvContent != null) tvContent.setText(replyText);
        if (tvTime    != null) tvTime.setText("Vừa xong");

        int textPrimary = isDark ? Color.WHITE                             : Color.parseColor("#1C1E21");
        int timeColor   = isDark ? ThemeManager.DarkColors.TEXT_SECONDARY  : Color.parseColor("#65676B");
        int bubbleBg    = isDark ? ThemeManager.DarkColors.CARD_BACKGROUND : Color.parseColor("#F0F2F5");

        if (tvName    != null) tvName.setTextColor(textPrimary);
        if (tvContent != null) tvContent.setTextColor(textPrimary);
        if (tvTime    != null) tvTime.setTextColor(timeColor);

        if (bubbleLayout != null) {
            android.graphics.drawable.GradientDrawable bubble =
                    new android.graphics.drawable.GradientDrawable();
            bubble.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            bubble.setCornerRadii(new float[]{ 0f, 0f, 18f, 18f, 18f, 18f, 18f, 18f });
            bubble.setColor(bubbleBg);
            bubbleLayout.setBackground(bubble);
        }

        container.addView(replyView);
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView     tvAvatar, tvName, tvTime, tvContent;
        TextView     btnLike, btnReply, tvReactionBadge, tvReactionCount;
        LinearLayout layoutReply, layoutReplies;
        LinearLayout bubbleLayout;
        TextView     tvReplyAvatar;
        EditText     etReply;
        ImageView    btnSendReply;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar        = itemView.findViewById(R.id.tvCommentAvatar);
            tvName          = itemView.findViewById(R.id.tvCommentName);
            tvTime          = itemView.findViewById(R.id.tvCommentTime);
            tvContent       = itemView.findViewById(R.id.tvCommentContent);
            btnLike         = itemView.findViewById(R.id.btnLikeComment);
            btnReply        = itemView.findViewById(R.id.btnReplyComment);
            tvReactionBadge = itemView.findViewById(R.id.tvReactionBadge);
            tvReactionCount = itemView.findViewById(R.id.tvReactionCount);
            layoutReply     = itemView.findViewById(R.id.layoutReply);
            layoutReplies   = itemView.findViewById(R.id.layoutReplies);
            tvReplyAvatar   = itemView.findViewById(R.id.tvReplyAvatar);
            etReply         = itemView.findViewById(R.id.etReply);
            btnSendReply    = itemView.findViewById(R.id.btnSendReply);
            bubbleLayout    = itemView.findViewById(R.id.commentBubbleLayout);
        }
    }
}