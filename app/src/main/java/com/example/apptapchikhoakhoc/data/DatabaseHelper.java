package com.example.apptapchikhoakhoc.data;

import android.content.Context;
import android.util.Log;

import com.example.apptapchikhoakhoc.admin.ArticleStatItem;
import com.example.apptapchikhoakhoc.model.Article;
import com.example.apptapchikhoakhoc.model.Comment;
import com.example.apptapchikhoakhoc.model.UserItem;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseHelper {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_REJECTED = "rejected";

    private static final String TAG = "FirebaseDataSource";
    private static final String COL_USERS = "users";
    private static final String COL_ARTICLES = "articles";
    private static final String COL_COMMENTS = "comments";
    private static final String COL_ARTICLE_REACTIONS = "article_reactions";
    private static final String COL_COMMENT_REACTIONS = "comment_reactions";
    private static final String COL_COUNTERS = "counters";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public DatabaseHelper(Context context) {
        this.firestore = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    private static String lower(String s) {
        return normalize(s).toLowerCase(Locale.ROOT);
    }

    private static String reactionDocId(int targetId, String userEmail) {
        return targetId + "__" + lower(userEmail).replace("@", "_at_").replace(".", "_dot_");
    }

    private <T> T awaitTask(Task<T> task, T fallback) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<T> result = new AtomicReference<>(fallback);
            AtomicReference<Exception> error = new AtomicReference<>(null);

            task.addOnSuccessListener(value -> {
                result.set(value);
                latch.countDown();
            }).addOnFailureListener(e -> {
                error.set(e);
                latch.countDown();
            });

            boolean ok = latch.await(20, TimeUnit.SECONDS);
            if (!ok || error.get() != null) {
                if (error.get() != null) {
                    Log.e(TAG, "awaitTask error", error.get());
                }
                return fallback;
            }
            return result.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "awaitTask interrupted", e);
            return fallback;
        }
    }

    private boolean awaitSuccess(Task<?> task) {
        Object marker = new Object();
        Object out = awaitTask((Task<Object>) task, marker);
        return out != marker;
    }

    private int nextCounter(String name) {
        Task<Long> task = firestore.runTransaction(tr -> {
            DocumentReference ref = firestore.collection(COL_COUNTERS).document(name);
            DocumentSnapshot snap = tr.get(ref);
            long current = 0L;
            if (snap.exists()) {
                Long v = snap.getLong("value");
                if (v != null) current = v;
            }
            long next = current + 1L;
            Map<String, Object> payload = new HashMap<>();
            payload.put("value", next);
            payload.put("updatedAt", System.currentTimeMillis());
            tr.set(ref, payload, SetOptions.merge());
            return next;
        });
        Long value = awaitTask(task, 0L);
        return value == null ? 0 : value.intValue();
    }

    private Article docToArticle(DocumentSnapshot doc) {
        int id = 0;
        Long idVal = doc.getLong("id");
        if (idVal != null) id = idVal.intValue();
        if (id == 0) {
            try {
                id = Integer.parseInt(doc.getId());
            } catch (Exception ignored) {
            }
        }

        Article a = new Article(
                id,
                normalize(doc.getString("title")),
                normalize(doc.getString("author")),
                normalize(doc.getString("category")),
                normalize(doc.getString("content")),
                normalize(doc.getString("imagePath")),
                normalize(doc.getString("videoPath"))
        );
        Long likes = doc.getLong("likes");
        Long comments = doc.getLong("comments");
        Long shares = doc.getLong("shares");
        Long views = doc.getLong("viewCount");
        Long approvedAt = doc.getLong("approvedAt");

        a.setLikes(likes == null ? 0 : likes.intValue());
        a.setComments(comments == null ? 0 : comments.intValue());
        a.setShares(shares == null ? 0 : shares.intValue());
        a.setViewCount(views == null ? 0 : views.intValue());
        a.setStatus(normalize(doc.getString("status")));
        a.setUserEmail(normalize(doc.getString("userEmail")));
        a.setApprovedAt(approvedAt == null ? 0L : approvedAt);
        return a;
    }

    private Comment docToComment(DocumentSnapshot doc) {
        int id = 0;
        Long idVal = doc.getLong("id");
        if (idVal != null) id = idVal.intValue();
        if (id == 0) {
            try {
                id = Integer.parseInt(doc.getId());
            } catch (Exception ignored) {
            }
        }

        Long articleIdVal = doc.getLong("articleId");
        Long tsVal = doc.getLong("timestamp");

        return new Comment(
                id,
                articleIdVal == null ? 0 : articleIdVal.intValue(),
                normalize(doc.getString("userName")),
                normalize(doc.getString("userEmail")),
                normalize(doc.getString("content")),
                tsVal == null ? 0L : tsVal
        );
    }

    private Map<String, Object> baseArticlePayload(String title, String author, String category,
                                                   String content, String imagePath, String videoPath,
                                                   String status, String userEmail) {
        Map<String, Object> values = new HashMap<>();
        values.put("title", normalize(title));
        values.put("author", normalize(author));
        values.put("category", normalize(category));
        values.put("content", normalize(content));
        values.put("imagePath", normalize(imagePath));
        values.put("videoPath", normalize(videoPath));
        values.put("status", normalize(status));
        values.put("userEmail", normalize(userEmail));
        values.put("updatedAt", System.currentTimeMillis());
        return values;
    }

    // Project Firebase hien tai chua dung Storage, nen giu nguyen local/remote path.
    private String resolveMediaPath(String rawPath) {
        String path = normalize(rawPath);
        if (path.isEmpty()) return "";
        return path;
    }

    public ArrayList<Article> getPendingArticles() {
        return getArticlesByStatus(STATUS_PENDING);
    }

    public ArrayList<Article> getApprovedArticles() {
        return getArticlesByStatus(STATUS_APPROVED);
    }

    public ArrayList<Article> getRejectedArticles() {
        return getArticlesByStatus(STATUS_REJECTED);
    }

    private ArrayList<Article> getArticlesByStatus(String status) {
        QuerySnapshot snap = awaitTask(
                firestore.collection(COL_ARTICLES).whereEqualTo("status", status).get(),
                null
        );
        ArrayList<Article> out = new ArrayList<>();
        if (snap == null) return out;
        for (QueryDocumentSnapshot doc : snap) out.add(docToArticle(doc));
        out.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return out;
    }

    public ArrayList<Article> getArticlesByUserEmail(String userEmail) {
        QuerySnapshot snap = awaitTask(
                firestore.collection(COL_ARTICLES).whereEqualTo("userEmail", normalize(userEmail)).get(),
                null
        );
        ArrayList<Article> out = new ArrayList<>();
        if (snap == null) return out;
        for (QueryDocumentSnapshot doc : snap) out.add(docToArticle(doc));
        out.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return out;
    }

    public ArrayList<Article> getApprovedArticlesByUserEmail(String userEmail) {
        ArrayList<Article> all = getApprovedArticles();
        ArrayList<Article> out = new ArrayList<>();
        String target = lower(userEmail);
        for (Article a : all) if (target.equals(lower(a.getUserEmail()))) out.add(a);
        return out;
    }

    public ArrayList<Article> getAllArticles() {
        ArrayList<Article> out = getApprovedArticles();
        out.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return out;
    }

    public ArrayList<Article> getArticlesByCategory(String category) {
        ArrayList<Article> source = getApprovedArticles();
        String normalized = normalize(category);
        if ("ALL".equalsIgnoreCase(normalized)) return source;

        ArrayList<Article> out = new ArrayList<>();
        for (Article a : source) {
            String cat = normalize(a.getCategory());
            if (normalized.isEmpty()) {
                if (cat.isEmpty()) out.add(a);
            } else if (normalized.equals(cat)) {
                out.add(a);
            }
        }
        return out;
    }

    public ArrayList<Article> getFeaturedArticles() {
        ArrayList<Article> source = getApprovedArticles();
        long threeDaysAgo = System.currentTimeMillis() - 3L * 24L * 60L * 60L * 1000L;

        ArrayList<Article> recent = new ArrayList<>();
        for (Article a : source) if (a.getApprovedAt() >= threeDaysAgo) recent.add(a);

        if (recent.isEmpty()) {
            source.sort((a, b) -> Long.compare(b.getApprovedAt(), a.getApprovedAt()));
            ArrayList<Article> fallback = new ArrayList<>();
            int limit = Math.min(20, source.size());
            for (int i = 0; i < limit; i++) fallback.add(source.get(i));
            return fallback;
        }

        recent.sort((a, b) -> {
            int scoreA = a.getLikes() * 3 + a.getComments() * 2 + a.getShares();
            int scoreB = b.getLikes() * 3 + b.getComments() * 2 + b.getShares();
            return Integer.compare(scoreB, scoreA);
        });
        return recent;
    }

    public ArrayList<Article> getLatestApprovedArticles(int limit) {
        ArrayList<Article> source = getApprovedArticles();
        source.sort((a, b) -> Long.compare(b.getApprovedAt(), a.getApprovedAt()));
        ArrayList<Article> out = new ArrayList<>();
        int size = Math.min(Math.max(limit, 0), source.size());
        for (int i = 0; i < size; i++) out.add(source.get(i));
        return out;
    }

    public ArrayList<Article> getArticlesWithImage() {
        ArrayList<Article> source = getApprovedArticles();
        ArrayList<Article> out = new ArrayList<>();
        for (Article a : source) if (!normalize(a.getImagePath()).isEmpty()) out.add(a);
        return out;
    }

    public ArrayList<Article> getVideoArticles() {
        ArrayList<Article> source = getApprovedArticles();
        ArrayList<Article> out = new ArrayList<>();
        for (Article a : source) if (!normalize(a.getVideoPath()).isEmpty()) out.add(a);
        return out;
    }

    public ArrayList<Article> searchArticles(String keyword) {
        ArrayList<Article> source = getApprovedArticles();
        String q = lower(keyword);
        if (q.isEmpty()) return source;

        ArrayList<Article> out = new ArrayList<>();
        for (Article a : source) {
            String title = lower(a.getTitle());
            String author = lower(a.getAuthor());
            String category = lower(a.getCategory());
            String content = lower(a.getContent());
            if (title.contains(q) || author.contains(q) || category.contains(q) || content.contains(q)) out.add(a);
        }
        out.sort((a, b) -> Integer.compare(b.getId(), a.getId()));
        return out;
    }

    public ArrayList<Article> getArticlesWithViews() {
        ArrayList<Article> source = getApprovedArticles();
        source.sort((a, b) -> Integer.compare(b.getViewCount(), a.getViewCount()));
        return source;
    }

    public int getTotalApprovedArticles() {
        return getApprovedArticles().size();
    }

    public int getTotalPendingArticles() {
        return getPendingArticles().size();
    }

    public int getTotalUsers() {
        QuerySnapshot snap = awaitTask(firestore.collection(COL_USERS).get(), null);
        return snap == null ? 0 : snap.size();
    }

    public int getTotalViews() {
        int total = 0;
        for (Article a : getApprovedArticles()) total += a.getViewCount();
        return total;
    }

    public int getTotalLikes() {
        QuerySnapshot snap = awaitTask(firestore.collection(COL_ARTICLES).get(), null);
        if (snap == null) return 0;
        int total = 0;
        for (QueryDocumentSnapshot d : snap) {
            Long likes = d.getLong("likes");
            total += likes == null ? 0 : likes.intValue();
        }
        return total;
    }

    public int getTotalComments() {
        QuerySnapshot snap = awaitTask(firestore.collection(COL_COMMENTS).get(), null);
        return snap == null ? 0 : snap.size();
    }

    public int getPendingCount() {
        return getTotalPendingArticles();
    }

    public List<ArticleStatItem> getArticlesOrderedByLikes() {
        ArrayList<Article> approved = getApprovedArticles();
        List<ArticleStatItem> out = new ArrayList<>();
        for (Article a : approved) {
            if (a.getLikes() > 0) out.add(new ArticleStatItem(a.getTitle(), a.getLikes(), a.getCategory()));
        }
        out.sort((x, y) -> Integer.compare(y.getCount(), x.getCount()));
        return out;
    }

    public void incrementViewCount(int articleId) {
        firestore.collection(COL_ARTICLES).document(String.valueOf(articleId))
                .update("viewCount", FieldValue.increment(1L), "updatedAt", System.currentTimeMillis());
    }

    public int getViewCount(int articleId) {
        DocumentSnapshot doc = awaitTask(
                firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).get(),
                null
        );
        if (doc == null || !doc.exists()) return 0;
        Long views = doc.getLong("viewCount");
        return views == null ? 0 : views.intValue();
    }

    private boolean containsViolation(String content) {
        String text = lower(content);
        if (text.isEmpty()) return false;
        String[] badWords = {"ch?i", "d?t", "dm", "dm", "vang t?c", "l?a d?o", "sex", "xxx", "b?o l?c"};
        for (String w : badWords) if (text.contains(w)) return true;
        return false;
    }

    public List<Comment> getAllCommentsForAdmin() {
        QuerySnapshot commentSnap = awaitTask(
                firestore.collection(COL_COMMENTS).orderBy("timestamp", Query.Direction.DESCENDING).get(),
                null
        );
        if (commentSnap == null) return new ArrayList<>();

        QuerySnapshot articleSnap = awaitTask(firestore.collection(COL_ARTICLES).get(), null);
        Map<Integer, String> articleTitleById = new HashMap<>();
        if (articleSnap != null) {
            for (QueryDocumentSnapshot a : articleSnap) {
                Long idVal = a.getLong("id");
                int id = idVal == null ? 0 : idVal.intValue();
                if (id == 0) {
                    try {
                        id = Integer.parseInt(a.getId());
                    } catch (Exception ignored) {
                    }
                }
                articleTitleById.put(id, normalize(a.getString("title")));
            }
        }

        List<Comment> out = new ArrayList<>();
        for (QueryDocumentSnapshot d : commentSnap) {
            Comment c = docToComment(d);
            c.setArticleTitle(articleTitleById.getOrDefault(c.getArticleId(), ""));
            c.setViolated(containsViolation(c.getContent()));
            out.add(c);
        }
        return out;
    }

    public boolean deleteComment(int commentId) {
        DocumentReference commentRef = firestore.collection(COL_COMMENTS).document(String.valueOf(commentId));
        DocumentSnapshot commentDoc = awaitTask(commentRef.get(), null);
        if (commentDoc == null || !commentDoc.exists()) return false;

        Long articleIdVal = commentDoc.getLong("articleId");
        int articleId = articleIdVal == null ? 0 : articleIdVal.intValue();

        QuerySnapshot rs = awaitTask(
                firestore.collection(COL_COMMENT_REACTIONS).whereEqualTo("commentId", commentId).get(),
                null
        );
        if (rs != null) for (QueryDocumentSnapshot r : rs) awaitSuccess(r.getReference().delete());

        boolean deleted = awaitSuccess(commentRef.delete());
        if (!deleted) return false;

        if (articleId > 0) {
            firestore.runTransaction(tr -> {
                DocumentReference aRef = firestore.collection(COL_ARTICLES).document(String.valueOf(articleId));
                DocumentSnapshot aDoc = tr.get(aRef);
                long current = 0L;
                if (aDoc.exists()) {
                    Long c = aDoc.getLong("comments");
                    if (c != null) current = c;
                }
                long next = Math.max(0L, current - 1L);
                tr.update(aRef, "comments", next, "updatedAt", System.currentTimeMillis());
                return null;
            });
        }
        return true;
    }

    public List<UserItem> getAllUsersForAdmin() {
        QuerySnapshot usersSnap = awaitTask(firestore.collection(COL_USERS).get(), null);
        if (usersSnap == null) return new ArrayList<>();

        ArrayList<UserItem> out = new ArrayList<>();
        for (QueryDocumentSnapshot userDoc : usersSnap) {
            int userId = 0;
            Long idVal = userDoc.getLong("id");
            if (idVal != null) userId = idVal.intValue();

            String name = normalize(userDoc.getString("name"));
            String email = normalize(userDoc.getString("email"));

            QuerySnapshot comments = awaitTask(
                    firestore.collection(COL_COMMENTS).whereEqualTo("userEmail", email).get(),
                    null
            );
            int totalComments = comments == null ? 0 : comments.size();

            QuerySnapshot articles = awaitTask(
                    firestore.collection(COL_ARTICLES)
                            .whereEqualTo("userEmail", email)
                            .whereEqualTo("status", STATUS_APPROVED)
                            .get(),
                    null
            );
            int totalLikes = 0;
            if (articles != null) {
                for (QueryDocumentSnapshot article : articles) {
                    Long likes = article.getLong("likes");
                    totalLikes += likes == null ? 0 : likes.intValue();
                }
            }

            out.add(new UserItem(userId, name, email, totalComments, totalLikes));
        }
        out.sort(Comparator.comparing(UserItem::getName));
        return out;
    }

    public boolean registerUser(String name, String email, String password) {
        if (isEmailExists(email)) return false;

        AuthResult authResult = awaitTask(auth.createUserWithEmailAndPassword(email, password), null);
        if (authResult == null || authResult.getUser() == null) return false;

        FirebaseUser user = authResult.getUser();
        int id = nextCounter("users");
        if (id == 0) return false;

        Map<String, Object> profile = new HashMap<>();
        profile.put("id", id);
        profile.put("name", normalize(name));
        profile.put("email", normalize(email));
        profile.put("role", "user");
        profile.put("createdAt", System.currentTimeMillis());
        profile.put("updatedAt", System.currentTimeMillis());

        return awaitSuccess(firestore.collection(COL_USERS).document(user.getUid()).set(profile));
    }

    public boolean isEmailExists(String email) {
        QuerySnapshot users = awaitTask(
                firestore.collection(COL_USERS).whereEqualTo("email", normalize(email)).limit(1).get(),
                null
        );
        return users != null && !users.isEmpty();
    }

    public boolean checkLogin(String email, String password) {
        AuthResult result = awaitTask(auth.signInWithEmailAndPassword(email, password), null);
        return result != null && result.getUser() != null;
    }

    public String getUserName(String email) {
        QuerySnapshot users = awaitTask(
                firestore.collection(COL_USERS).whereEqualTo("email", normalize(email)).limit(1).get(),
                null
        );
        if (users == null || users.isEmpty()) return "";
        return normalize(users.getDocuments().get(0).getString("name"));
    }

    public long insertArticle(String title, String author, String category,
                              String content, String imagePath, String videoPath) {
        return insertInternal(title, author, category, content, imagePath, videoPath, STATUS_APPROVED, "");
    }

    public long insertPendingArticle(String title, String author, String category,
                                     String content, String imagePath, String videoPath,
                                     String userEmail) {
        return insertInternal(title, author, category, content, imagePath, videoPath, STATUS_PENDING, userEmail);
    }

    public long insertPendingArticle(String title, String author, String category,
                                     String content, String imagePath, String videoPath) {
        return insertPendingArticle(title, author, category, content, imagePath, videoPath, "");
    }

    private long insertInternal(String title, String author, String category,
                                String content, String imagePath, String videoPath,
                                String status, String userEmail) {
        int articleId = nextCounter("articles");
        if (articleId == 0) return -1;

        String remoteImagePath = resolveMediaPath(imagePath);
        String remoteVideoPath = resolveMediaPath(videoPath);

        Map<String, Object> values = baseArticlePayload(
                title, author, category, content, remoteImagePath, remoteVideoPath, status, userEmail
        );
        values.put("id", articleId);
        values.put("likes", 0);
        values.put("comments", 0);
        values.put("shares", 0);
        values.put("viewCount", 0);
        values.put("createdAt", System.currentTimeMillis());
        values.put("approvedAt", STATUS_APPROVED.equals(status) ? System.currentTimeMillis() : 0L);

        boolean ok = awaitSuccess(
                firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).set(values)
        );
        return ok ? articleId : -1;
    }

    public boolean updateArticle(int articleId, String title, String author, String category,
                                 String content, String imagePath, String videoPath) {
        DocumentReference ref = firestore.collection(COL_ARTICLES).document(String.valueOf(articleId));
        DocumentSnapshot current = awaitTask(ref.get(), null);
        if (current == null || !current.exists()) return false;

        String remoteImagePath = resolveMediaPath(imagePath);
        String remoteVideoPath = resolveMediaPath(videoPath);

        Map<String, Object> values = baseArticlePayload(
                title,
                author,
                category,
                content,
                remoteImagePath,
                remoteVideoPath,
                normalize(current.getString("status")),
                normalize(current.getString("userEmail"))
        );
        return awaitSuccess(ref.set(values, SetOptions.merge()));
    }

    public boolean deleteArticle(int articleId) {
        QuerySnapshot commentSnap = awaitTask(
                firestore.collection(COL_COMMENTS).whereEqualTo("articleId", articleId).get(),
                null
        );
        if (commentSnap != null) {
            for (QueryDocumentSnapshot c : commentSnap) {
                int commentId = 0;
                Long idVal = c.getLong("id");
                if (idVal != null) commentId = idVal.intValue();
                if (commentId != 0) {
                    QuerySnapshot cr = awaitTask(
                            firestore.collection(COL_COMMENT_REACTIONS).whereEqualTo("commentId", commentId).get(),
                            null
                    );
                    if (cr != null) for (QueryDocumentSnapshot r : cr) awaitSuccess(r.getReference().delete());
                }
                awaitSuccess(c.getReference().delete());
            }
        }

        QuerySnapshot articleReactions = awaitTask(
                firestore.collection(COL_ARTICLE_REACTIONS).whereEqualTo("articleId", articleId).get(),
                null
        );
        if (articleReactions != null) {
            for (QueryDocumentSnapshot r : articleReactions) awaitSuccess(r.getReference().delete());
        }

        return awaitSuccess(firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).delete());
    }

    public boolean approveArticle(int articleId) {
        Map<String, Object> values = new HashMap<>();
        values.put("status", STATUS_APPROVED);
        values.put("approvedAt", System.currentTimeMillis());
        values.put("updatedAt", System.currentTimeMillis());
        return awaitSuccess(
                firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).set(values, SetOptions.merge())
        );
    }

    public boolean rejectArticle(int articleId) {
        Map<String, Object> values = new HashMap<>();
        values.put("status", STATUS_REJECTED);
        values.put("approvedAt", 0L);
        values.put("updatedAt", System.currentTimeMillis());
        return awaitSuccess(
                firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).set(values, SetOptions.merge())
        );
    }

    public boolean isArticlePending(int articleId) {
        DocumentSnapshot doc = awaitTask(
                firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).get(),
                null
        );
        if (doc == null || !doc.exists()) return false;
        return STATUS_PENDING.equals(normalize(doc.getString("status")));
    }

    public String getUserReaction(int articleId, String userEmail) {
        DocumentSnapshot doc = awaitTask(
                firestore.collection(COL_ARTICLE_REACTIONS).document(reactionDocId(articleId, userEmail)).get(),
                null
        );
        if (doc == null || !doc.exists()) return "";
        return normalize(doc.getString("emoji"));
    }

    public boolean addOrUpdateReaction(int articleId, String userEmail, String emoji) {
        DocumentReference reactionRef = firestore.collection(COL_ARTICLE_REACTIONS)
                .document(reactionDocId(articleId, userEmail));
        DocumentReference articleRef = firestore.collection(COL_ARTICLES).document(String.valueOf(articleId));

        Task<Boolean> tx = firestore.runTransaction(tr -> {
            DocumentSnapshot reactionDoc = tr.get(reactionRef);
            DocumentSnapshot articleDoc = tr.get(articleRef);

            Map<String, Object> reactionPayload = new HashMap<>();
            reactionPayload.put("articleId", articleId);
            reactionPayload.put("userEmail", normalize(userEmail));
            reactionPayload.put("emoji", normalize(emoji));
            reactionPayload.put("timestamp", System.currentTimeMillis());
            tr.set(reactionRef, reactionPayload, SetOptions.merge());

            if (!reactionDoc.exists()) {
                long currentLikes = 0L;
                if (articleDoc.exists()) {
                    Long v = articleDoc.getLong("likes");
                    if (v != null) currentLikes = v;
                }
                tr.update(articleRef, "likes", currentLikes + 1L, "updatedAt", System.currentTimeMillis());
            }
            return true;
        });
        Boolean ok = awaitTask(tx, false);
        return ok != null && ok;
    }

    public boolean removeReactionFromDb(int articleId, String userEmail) {
        DocumentReference reactionRef = firestore.collection(COL_ARTICLE_REACTIONS)
                .document(reactionDocId(articleId, userEmail));
        DocumentReference articleRef = firestore.collection(COL_ARTICLES).document(String.valueOf(articleId));

        Task<Boolean> tx = firestore.runTransaction(tr -> {
            DocumentSnapshot reactionDoc = tr.get(reactionRef);
            if (!reactionDoc.exists()) return false;

            DocumentSnapshot articleDoc = tr.get(articleRef);
            long currentLikes = 0L;
            if (articleDoc.exists()) {
                Long likes = articleDoc.getLong("likes");
                if (likes != null) currentLikes = likes;
            }
            tr.delete(reactionRef);
            tr.update(articleRef, "likes", Math.max(0L, currentLikes - 1L), "updatedAt", System.currentTimeMillis());
            return true;
        });
        Boolean ok = awaitTask(tx, false);
        return ok != null && ok;
    }

    public int getReactionCount(int articleId) {
        QuerySnapshot rs = awaitTask(
                firestore.collection(COL_ARTICLE_REACTIONS).whereEqualTo("articleId", articleId).get(),
                null
        );
        return rs == null ? 0 : rs.size();
    }

    public void likeArticle(int articleId) {
        firestore.collection(COL_ARTICLES).document(String.valueOf(articleId))
                .update("likes", FieldValue.increment(1L), "updatedAt", System.currentTimeMillis());
    }

    public boolean addComment(int articleId, String userName, String userEmail, String content, long timestamp) {
        int commentId = nextCounter("comments");
        if (commentId == 0) return false;

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", commentId);
        payload.put("articleId", articleId);
        payload.put("userName", normalize(userName));
        payload.put("userEmail", normalize(userEmail));
        payload.put("content", normalize(content));
        payload.put("timestamp", timestamp);
        payload.put("createdAt", System.currentTimeMillis());

        boolean saved = awaitSuccess(firestore.collection(COL_COMMENTS).document(String.valueOf(commentId)).set(payload));
        if (!saved) return false;

        firestore.collection(COL_ARTICLES).document(String.valueOf(articleId))
                .update("comments", FieldValue.increment(1L), "updatedAt", System.currentTimeMillis());
        return true;
    }

    public boolean addComment(int articleId, String content, long timestamp) {
        return addComment(articleId, "", "", content, timestamp);
    }

    public List<Comment> getCommentsByArticle(int articleId) {
        QuerySnapshot snap = awaitTask(
                firestore.collection(COL_COMMENTS)
                        .whereEqualTo("articleId", articleId)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get(),
                null
        );
        List<Comment> out = new ArrayList<>();
        if (snap == null) return out;
        for (QueryDocumentSnapshot d : snap) out.add(docToComment(d));
        return out;
    }

    public String getCommentReaction(int commentId, String userEmail) {
        DocumentSnapshot doc = awaitTask(
                firestore.collection(COL_COMMENT_REACTIONS).document(reactionDocId(commentId, userEmail)).get(),
                null
        );
        if (doc == null || !doc.exists()) return "";
        return normalize(doc.getString("emoji"));
    }

    public Map<Integer, String> getCommentReactionsForUser(List<Integer> commentIds, String userEmail) {
        Map<Integer, String> out = new HashMap<>();
        if (commentIds == null || commentIds.isEmpty()) return out;

        Set<String> allowedDocIds = new HashSet<>();
        for (Integer id : commentIds) if (id != null) allowedDocIds.add(reactionDocId(id, userEmail));

        QuerySnapshot snap = awaitTask(
                firestore.collection(COL_COMMENT_REACTIONS).whereEqualTo("userEmail", normalize(userEmail)).get(),
                null
        );
        if (snap == null) return out;

        for (QueryDocumentSnapshot doc : snap) {
            if (!allowedDocIds.contains(doc.getId())) continue;
            Long idVal = doc.getLong("commentId");
            if (idVal == null) continue;
            out.put(idVal.intValue(), normalize(doc.getString("emoji")));
        }
        return out;
    }

    public Map<Integer, Integer> getCommentReactionCounts(List<Integer> commentIds) {
        Map<Integer, Integer> out = new HashMap<>();
        if (commentIds == null || commentIds.isEmpty()) return out;

        Set<Integer> allow = new HashSet<>(commentIds);
        QuerySnapshot snap = awaitTask(firestore.collection(COL_COMMENT_REACTIONS).get(), null);
        if (snap == null) return out;

        for (QueryDocumentSnapshot doc : snap) {
            Long idVal = doc.getLong("commentId");
            if (idVal == null) continue;
            int id = idVal.intValue();
            if (!allow.contains(id)) continue;
            out.put(id, out.getOrDefault(id, 0) + 1);
        }
        return out;
    }

    public boolean addOrUpdateCommentReaction(int commentId, String userEmail, String emoji) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("commentId", commentId);
        payload.put("userEmail", normalize(userEmail));
        payload.put("emoji", normalize(emoji));
        payload.put("timestamp", System.currentTimeMillis());
        return awaitSuccess(
                firestore.collection(COL_COMMENT_REACTIONS).document(reactionDocId(commentId, userEmail))
                        .set(payload, SetOptions.merge())
        );
    }

    public boolean removeCommentReaction(int commentId, String userEmail) {
        return awaitSuccess(
                firestore.collection(COL_COMMENT_REACTIONS).document(reactionDocId(commentId, userEmail)).delete()
        );
    }

    public int getCommentsCount(int articleId) {
        QuerySnapshot snap = awaitTask(
                firestore.collection(COL_COMMENTS).whereEqualTo("articleId", articleId).get(),
                null
        );
        return snap == null ? 0 : snap.size();
    }

    public void shareArticle(int articleId) {
        firestore.collection(COL_ARTICLES).document(String.valueOf(articleId))
                .update("shares", FieldValue.increment(1L), "updatedAt", System.currentTimeMillis());
    }

    public int getLikes(int articleId) {
        return getCount(articleId, "likes");
    }

    public int getComments(int articleId) {
        return getCount(articleId, "comments");
    }

    public int getShares(int articleId) {
        return getCount(articleId, "shares");
    }

    private int getCount(int articleId, String field) {
        DocumentSnapshot doc = awaitTask(
                firestore.collection(COL_ARTICLES).document(String.valueOf(articleId)).get(),
                null
        );
        if (doc == null || !doc.exists()) return 0;
        Long v = doc.getLong(field);
        return v == null ? 0 : v.intValue();
    }
}
