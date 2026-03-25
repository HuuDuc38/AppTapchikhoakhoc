# 📊 UML Diagrams – AppTapchikhoakhoc

> **Ứng dụng Tạp Chí Khoa Học** – Android (Java + SQLite)  
> Ngày tạo: 2026-03-13

---

## Mục lục

1. [Biểu đồ Lớp (Class Diagram)](#1-biểu-đồ-lớp-class-diagram)
2. [Biểu đồ Use Case](#2-biểu-đồ-use-case)
3. [Biểu đồ Tuần Tự – Đăng nhập](#3-biểu-đồ-tuần-tự--đăng-nhập)
4. [Biểu đồ Tuần Tự – Đăng bài viết (User)](#4-biểu-đồ-tuần-tự--đăng-bài-viết-user)
5. [Biểu đồ Tuần Tự – Duyệt bài (Admin)](#5-biểu-đồ-tuần-tự--duyệt-bài-admin)
6. [Biểu đồ Tuần Tự – Bình luận & Reaction](#6-biểu-đồ-tuần-tự--bình-luận--reaction)
7. [Biểu đồ Hoạt Động – Luồng chính User](#7-biểu-đồ-hoạt-động--luồng-chính-user)
8. [Biểu đồ Hoạt Động – Luồng Admin xử lý bài](#8-biểu-đồ-hoạt-động--luồng-admin-xử-lý-bài)
9. [Biểu đồ Trạng Thái – Article](#9-biểu-đồ-trạng-thái--article)
10. [Biểu đồ Thành Phần (Component Diagram)](#10-biểu-đồ-thành-phần-component-diagram)
11. [Biểu đồ Gói (Package Diagram)](#11-biểu-đồ-gói-package-diagram)
12. [Biểu đồ Cơ Sở Dữ Liệu (ERD)](#12-biểu-đồ-cơ-sở-dữ-liệu-erd)

---

## 1. Biểu đồ Lớp (Class Diagram)

```mermaid
classDiagram
    direction TB

    %% ═══════════════ MODEL ═══════════════
    class Article {
        -int id
        -String title
        -String author
        -String category
        -String content
        -String imagePath
        -String videoPath
        -int likes
        -int comments
        -int shares
        -String status
        -String userEmail
        -long approvedAt
        +getId() int
        +getTitle() String
        +getAuthor() String
        +getCategory() String
        +getContent() String
        +getImagePath() String
        +getVideoPath() String
        +getLikes() int
        +getComments() int
        +getShares() int
        +getStatus() String
        +getUserEmail() String
        +getApprovedAt() long
        +setStatus(String)
        +setLikes(int)
        +setComments(int)
        +setShares(int)
    }

    class Comment {
        -int id
        -int articleId
        -String userName
        -String userEmail
        -String content
        -long timestamp
        +getId() int
        +getArticleId() int
        +getUserName() String
        +getUserEmail() String
        +getContent() String
        +getTimestamp() long
    }

    %% ═══════════════ DATA ═══════════════
    class DatabaseHelper {
        -String DB_NAME
        -int DB_VERSION
        +STATUS_PENDING$ String
        +STATUS_APPROVED$ String
        +STATUS_REJECTED$ String
        +getPendingArticles() ArrayList~Article~
        +getApprovedArticles() ArrayList~Article~
        +getRejectedArticles() ArrayList~Article~
        +getAllArticles() ArrayList~Article~
        +getArticlesByCategory(String) ArrayList~Article~
        +getFeaturedArticles() ArrayList~Article~
        +getLatestApprovedArticles(int) ArrayList~Article~
        +searchArticles(String) ArrayList~Article~
        +insertArticle(String,...) long
        +insertPendingArticle(String,...) long
        +updateArticle(int,...) boolean
        +deleteArticle(int) boolean
        +approveArticle(int) boolean
        +rejectArticle(int) boolean
        +registerUser(String,String,String) boolean
        +checkLogin(String,String) boolean
        +isEmailExists(String) boolean
        +getUserName(String) String
        +addComment(int,String,String,String,long) boolean
        +getCommentsByArticle(int) List~Comment~
        +addOrUpdateReaction(int,String,String) boolean
        +removeReactionFromDb(int,String) boolean
        +getUserReaction(int,String) String
        +getTotalApprovedArticles() int
        +getTotalPendingArticles() int
        +getTotalUsers() int
        +getTotalLikes() int
        +getTotalComments() int
    }

    class BaseActivity {
        #boolean isDarkMode
        -float appliedFontScale
        -String appliedLocale
        #refreshThemeState()
        #getBackgroundColor() int
        #getCardColor() int
        #getTextColor() int
        #getAccentColor() int
        #applyAndSaveTextSize(String)
        #applyAndSaveLanguage(String)
        #lightenColor(int,float) int
        #darkenColor(int,float) int
    }

    class BaseAdminActivity {
        #boolean isDarkMode
        -String appliedAdminLocale
        #applyAndSaveAdminLanguage(String)
    }

    %% ═══════════════ MAIN ═══════════════
    class MainActivity {
        -DrawerLayout drawerLayout
        -NewsAdapter newsAdapter
        -FeaturedBannerAdapter bannerAdapter
        -String currentCategory
        +loadAllData()
        +updateUI(List~Article~)
        -loadFeaturedArticles()
        -filterArticlesByCategory()
        -handleAddArticle()
        -setupBanner(List~Article~)
        -startAutoSlide()
        -stopAutoSlide()
        +onNavigationItemSelected(MenuItem) boolean
    }

    class ArticleDetailActivity {
        -Article article
        -CommentAdapter commentAdapter
        -DatabaseHelper db
        +loadArticleDetail()
        -setupReactions()
        -setupComments()
        -submitComment()
        -shareArticle()
    }

    class LoginActivity {
        -DatabaseHelper db
        -loginUser()
        -validateInput() boolean
    }

    class RegisterActivity {
        -DatabaseHelper db
        -registerUser()
        -validateInput() boolean
    }

    class NotificationsActivity {
        -DatabaseHelper db
        -NotificationItemAdapter adapter
        +loadNotifications()
    }

    class SettingsActivity {
        -ThemeManager themeManager
        -LocaleManager localeManager
        -TextSizeManager textSizeManager
        +applyTheme()
        +changeLanguage(String)
        +changeTextSize(String)
    }

    class SearchActivity {
        -DatabaseHelper db
        -NewsAdapter newsAdapter
        +search(String)
    }

    class PrivacyActivity {
        +loadPrivacyContent()
    }

    %% ═══════════════ ADMIN ═══════════════
    class AdminActivity {
        -DatabaseHelper db
        -AdminArticleAdapter adapter
        +loadArticles()
        -openAddEdit()
        -deleteArticle(int)
    }

    class AddEditArticleActivity["AddEditArticleActivity (admin)"] {
        -DatabaseHelper db
        -ContentModerationHelper moderator
        +saveArticle()
        -pickImage()
        -pickVideo()
        -moderateContent()
    }

    class AddEditArticleActivity_main["AddEditArticleActivity (main)"] {
        -DatabaseHelper db
        -ContentModerationHelper moderator
        +submitPendingArticle()
        -pickImage()
        -pickVideo()
    }

    class ApproveArticleActivity {
        -DatabaseHelper db
        -ApproveArticleAdapter adapter
        +loadPendingArticles()
        -approveArticle(int)
        -rejectArticle(int)
    }

    class LoginAdminActivity {
        -checkAdminCredentials() boolean
    }

    class StatsActivity {
        -DatabaseHelper db
        +loadStats()
    }

    class ContactAdminActivity {
        +loadContactInfo()
    }

    class AdminSearchActivity["SearchActivity (admin)"] {
        -DatabaseHelper db
        +search(String)
    }

    class AdminSettingsActivity["SettingsActivity (admin)"] {
        -AdminThemeManager themeManager
        -AdminLocaleManager localeManager
        +applyAdminTheme()
        +changeAdminLanguage(String)
    }

    %% ═══════════════ ADAPTER ═══════════════
    class NewsAdapter {
        -List~Article~ articleList
        -Context context
        +setList(List~Article~)
        +onCreateViewHolder(...) ViewHolder
        +onBindViewHolder(...) void
        +getItemCount() int
    }

    class FeaturedBannerAdapter {
        -List~Article~ articles
        -Context context
        +updateArticles(List~Article~)
        +setOnItemClickListener(OnItemClickListener)
        +refreshTheme()
    }

    class CommentAdapter {
        -List~Comment~ commentList
        -DatabaseHelper db
        -String currentUserEmail
        +setList(List~Comment~)
        +deleteComment(int)
        +showReactionPicker(int)
    }

    class RelatedNewsAdapter {
        -List~Article~ articles
        +setList(List~Article~)
    }

    class AdminArticleAdapter {
        -List~Article~ articles
        +setOnEditListener(OnEditListener)
        +setOnDeleteListener(OnDeleteListener)
    }

    class ApproveArticleAdapter {
        -List~Article~ articles
        +setOnApproveListener(OnApproveListener)
        +setOnRejectListener(OnRejectListener)
    }

    class NotificationItemAdapter {
        -List~Article~ articles
    }

    %% ═══════════════ UTILS ═══════════════
    class ThemeManager {
        -PREFS_NAME$ String
        +isDarkMode(Context) bool$
        +setDarkMode(Context,bool)$
        +toggleTheme(Context)$
        +getBackgroundColor(Context) int$
        +getStatusBarColor(Context) int$
        +getTextPrimaryColor(Context) int$
        +getAccentColor(Context) int$
    }

    class AdminThemeManager {
        +isDarkMode(Context) bool$
        +setDarkMode(Context,bool)$
        +getBackgroundColor(Context) int$
    }

    class LocaleManager {
        +getSavedLanguage(Context) String$
        +saveLanguage(Context,String)$
        +applyLocale(Context,String) Context$
    }

    class AdminLocaleManager {
        +getSavedLanguage(Context) String$
        +saveLanguage(Context,String)$
        +applyLocale(Context,String) Context$
    }

    class TextSizeManager {
        +getFontScale(Context) float$
        +setTextSize(Context,String)$
        +applyTextSize(Context) Context$
    }

    class ContentModerationHelper {
        -API_KEY$ String
        -API_URL$ String
        -MODEL$ String
        +moderate(String,String,ModerationCallback)$
        -callApi(String,String) String$
        -buildPrompt(String,String) String$
        -stripHtml(String) String$
    }

    class ModerationCallback {
        <<interface>>
        +onApproved()
        +onRejected(String)
        +onError(String)
    }

    class AutoApproveHelper {
        +scheduleAutoApprove(Context,int,long)$
        +cancelAutoApprove(Context,int)$
    }

    class AutoApproveWorker {
        -DatabaseHelper db
        +doWork() Result
    }

    class CategoryBadgeHelper {
        +getCategoryColor(String) int$
        +getCategoryIcon(String) int$
    }

    %% ═══════════════ RELATIONSHIPS ═══════════════
    Article --> Comment : "1 → *"
    DatabaseHelper --> Article : creates/manages
    DatabaseHelper --> Comment : creates/manages
    DatabaseHelper --|> SQLiteOpenHelper

    BaseActivity --|> AppCompatActivity
    BaseAdminActivity --|> AppCompatActivity

    MainActivity --|> BaseActivity
    ArticleDetailActivity --|> BaseActivity
    LoginActivity --|> BaseActivity
    RegisterActivity --|> BaseActivity
    NotificationsActivity --|> BaseActivity
    SettingsActivity --|> BaseActivity
    SearchActivity --|> BaseActivity
    PrivacyActivity --|> BaseActivity
    AddEditArticleActivity_main --|> BaseActivity

    AdminActivity --|> BaseAdminActivity
    AddEditArticleActivity --|> BaseAdminActivity
    ApproveArticleActivity --|> BaseAdminActivity
    LoginAdminActivity --|> AppCompatActivity
    StatsActivity --|> BaseAdminActivity
    ContactAdminActivity --|> BaseAdminActivity
    AdminSearchActivity --|> BaseAdminActivity
    AdminSettingsActivity --|> BaseAdminActivity

    MainActivity --> DatabaseHelper : uses
    MainActivity --> NewsAdapter : uses
    MainActivity --> FeaturedBannerAdapter : uses
    ArticleDetailActivity --> DatabaseHelper : uses
    ArticleDetailActivity --> CommentAdapter : uses
    ApproveArticleActivity --> DatabaseHelper : uses
    AdminActivity --> DatabaseHelper : uses
    AddEditArticleActivity --> ContentModerationHelper : uses
    AddEditArticleActivity_main --> ContentModerationHelper : uses

    BaseActivity --> ThemeManager : delegates
    BaseActivity --> LocaleManager : delegates
    BaseActivity --> TextSizeManager : delegates
    BaseAdminActivity --> AdminThemeManager : delegates
    BaseAdminActivity --> AdminLocaleManager : delegates

    ContentModerationHelper --> ModerationCallback : callback
    AutoApproveWorker --> DatabaseHelper : uses
    AutoApproveHelper --> AutoApproveWorker : schedules
```

---

## 2. Biểu đồ Use Case

```mermaid
graph LR
    subgraph Actors
        User(["👤 Người dùng"])
        Admin(["🔑 Quản trị viên"])
        System(["⚙️ Hệ thống"])
    end

    subgraph UC_User["Use Cases – Người Dùng"]
        UC1["Đăng ký tài khoản"]
        UC2["Đăng nhập"]
        UC3["Xem danh sách bài viết"]
        UC4["Lọc theo danh mục"]
        UC5["Xem bài nổi bật"]
        UC6["Tìm kiếm bài viết"]
        UC7["Xem chi tiết bài viết"]
        UC8["Thả reaction bài viết"]
        UC9["Bình luận bài viết"]
        UC10["Thả reaction bình luận"]
        UC11["Chia sẻ bài viết"]
        UC12["Đăng bài viết mới"]
        UC13["Xem thông báo"]
        UC14["Chỉnh sửa cài đặt"]
        UC14a["Đổi giao diện Dark/Light"]
        UC14b["Đổi ngôn ngữ"]
        UC14c["Đổi cỡ chữ"]
    end

    subgraph UC_Admin["Use Cases – Quản Trị"]
        UA1["Đăng nhập Admin"]
        UA2["Quản lý bài viết"]
        UA2a["Thêm bài viết"]
        UA2b["Sửa bài viết"]
        UA2c["Xóa bài viết"]
        UA3["Duyệt bài chờ"]
        UA3a["Phê duyệt bài"]
        UA3b["Từ chối bài"]
        UA4["Tìm kiếm bài"]
        UA5["Xem thống kê"]
        UA6["Cài đặt Admin"]
    end

    subgraph UC_System["Use Cases – Hệ Thống"]
        US1["Kiểm duyệt AI (Claude)"]
        US2["Tự động duyệt bài (24h)"]
        US3["Lưu trữ SQLite"]
    end

    User --> UC1
    User --> UC2
    User --> UC3
    User --> UC4
    User --> UC5
    User --> UC6
    User --> UC7
    User --> UC8
    User --> UC9
    User --> UC10
    User --> UC11
    User --> UC12
    User --> UC13
    User --> UC14
    UC14 --> UC14a
    UC14 --> UC14b
    UC14 --> UC14c
    UC12 --> US1
    UC12 --> US3

    Admin --> UA1
    Admin --> UA2
    UA2 --> UA2a
    UA2 --> UA2b
    UA2 --> UA2c
    Admin --> UA3
    UA3 --> UA3a
    UA3 --> UA3b
    Admin --> UA4
    Admin --> UA5
    Admin --> UA6
    UA2a --> US1
    UA3 --> US2
    US2 --> US3
```

---

## 3. Biểu đồ Tuần Tự – Đăng nhập

```mermaid
sequenceDiagram
    actor User as 👤 Người dùng
    participant LA as LoginActivity
    participant DB as DatabaseHelper
    participant SP as SharedPreferences
    participant Main as MainActivity

    User->>LA: Nhập email + mật khẩu
    User->>LA: Nhấn "Đăng nhập"
    LA->>LA: validateInput()
    alt Input hợp lệ
        LA->>DB: checkLogin(email, password)
        DB->>DB: Query bảng users
        DB-->>LA: true / false
        alt Đăng nhập thành công
            LA->>SP: Lưu isLoggedIn=true, userEmail
            LA-->>User: Toast "Đăng nhập thành công"
            LA->>Main: startActivity(Intent)
            Main->>DB: getAllArticles()
            DB-->>Main: List<Article>
            Main-->>User: Hiển thị danh sách bài
        else Sai thông tin
            LA-->>User: Toast "Email hoặc mật khẩu không đúng"
        end
    else Input rỗng
        LA-->>User: Hiện thông báo lỗi trường nhập
    end
```

---

## 4. Biểu đồ Tuần Tự – Đăng bài viết (User)

```mermaid
sequenceDiagram
    actor User as 👤 Người dùng
    participant Main as MainActivity
    participant SP as SharedPreferences
    participant LA as LoginActivity
    participant AE as AddEditArticleActivity
    participant CM as ContentModerationHelper
    participant API as Claude AI API
    participant DB as DatabaseHelper

    User->>Main: Nhấn FAB "Đăng bài"
    Main->>SP: isUserLoggedIn()?
    alt Chưa đăng nhập
        Main->>LA: startActivityForResult()
        LA-->>User: Màn hình đăng nhập
        User->>LA: Đăng nhập thành công
        LA-->>Main: RESULT_OK
    end
    Main->>AE: startActivity(pending_mode=true)
    User->>AE: Nhập tiêu đề, nội dung, chọn ảnh/video
    User->>AE: Nhấn "Đăng bài"
    AE->>CM: moderate(title, content, callback)
    CM->>API: POST /v1/messages (Claude Haiku)
    API-->>CM: JSON {approved, reason, ...}
    alt Nội dung vi phạm
        CM-->>AE: onRejected(reason)
        AE-->>User: Dialog "Nội dung vi phạm: [lý do]"
    else Nội dung sạch
        CM-->>AE: onApproved()
        AE->>DB: insertPendingArticle(..., STATUS_PENDING)
        DB-->>AE: articleId
        AE-->>User: Toast "Bài đã gửi chờ duyệt"
        note over AE: AutoApproveWorker lên lịch duyệt tự động sau 24h
    else Lỗi API
        CM-->>AE: onError(msg)
        AE->>DB: insertPendingArticle(..., STATUS_PENDING)
        AE-->>User: Toast "Bài đã gửi (bỏ qua kiểm duyệt)"
    end
```

---

## 5. Biểu đồ Tuần Tự – Duyệt bài (Admin)

```mermaid
sequenceDiagram
    actor Admin as 🔑 Quản trị
    participant AA as ApproveArticleActivity
    participant Adapter as ApproveArticleAdapter
    participant DB as DatabaseHelper
    participant Worker as AutoApproveWorker

    Admin->>AA: Mở màn hình duyệt bài
    AA->>DB: getPendingArticles()
    DB-->>AA: List<Article> (status=pending)
    AA->>Adapter: setList(pendingArticles)
    Adapter-->>Admin: Hiển thị danh sách chờ duyệt

    alt Admin nhấn "Duyệt"
        Admin->>Adapter: onApproveClick(articleId)
        Adapter->>AA: callback onApprove(articleId)
        AA->>DB: approveArticle(articleId)
        DB->>DB: UPDATE status=approved, approved_at=now()
        DB-->>AA: true
        AA->>AA: Xóa khỏi danh sách UI
        AA-->>Admin: Thông báo "Đã duyệt"
    else Admin nhấn "Từ chối"
        Admin->>Adapter: onRejectClick(articleId)
        Adapter->>AA: callback onReject(articleId)
        AA->>DB: rejectArticle(articleId)
        DB->>DB: UPDATE status=rejected
        DB-->>AA: true
        AA->>AA: Xóa khỏi danh sách UI
        AA-->>Admin: Thông báo "Đã từ chối"
    end

    note over Worker: Tự động duyệt sau 24h nếu admin không xử lý
    Worker->>DB: isArticlePending(articleId)
    DB-->>Worker: true
    Worker->>DB: approveArticle(articleId)
```

---

## 6. Biểu đồ Tuần Tự – Bình luận & Reaction

```mermaid
sequenceDiagram
    actor User as 👤 Người dùng
    participant AD as ArticleDetailActivity
    participant CA as CommentAdapter
    participant DB as DatabaseHelper

    %% ─── Xem bài & bình luận ───
    User->>AD: Mở chi tiết bài viết
    AD->>DB: getCommentsByArticle(articleId)
    DB-->>AD: List<Comment>
    AD->>CA: setList(comments)
    CA-->>User: Hiển thị danh sách bình luận

    %% ─── Thêm bình luận ───
    User->>AD: Nhập bình luận → Gửi
    AD->>DB: addComment(articleId, userName, userEmail, content, timestamp)
    DB->>DB: INSERT comments + UPDATE articles.comments += 1
    DB-->>AD: true
    AD->>DB: getCommentsByArticle(articleId)
    DB-->>AD: List<Comment> (mới)
    AD->>CA: setList(newComments)
    CA-->>User: Hiển thị bình luận mới

    %% ─── Reaction bài viết ───
    User->>AD: Long-press nút reaction
    AD-->>User: Hiện Emoji Picker
    User->>AD: Chọn emoji
    AD->>DB: getUserReaction(articleId, userEmail)
    DB-->>AD: emoji hiện tại / null
    alt Chưa có reaction
        AD->>DB: addOrUpdateReaction(articleId, userEmail, emoji)
        DB->>DB: INSERT reactions + UPDATE likes += 1
    else Đổi reaction
        AD->>DB: addOrUpdateReaction(articleId, userEmail, emoji)
        DB->>DB: UPDATE emoji (không tăng likes)
    else Bỏ reaction
        AD->>DB: removeReactionFromDb(articleId, userEmail)
        DB->>DB: DELETE + UPDATE likes -= 1
    end
    DB-->>AD: success
    AD-->>User: Cập nhật UI reaction

    %% ─── Reaction bình luận ───
    User->>CA: Long-press bình luận
    CA-->>User: Emoji Picker
    User->>CA: Chọn emoji
    CA->>DB: addOrUpdateCommentReaction(commentId, userEmail, emoji)
    DB-->>CA: success
    CA-->>User: Cập nhật reaction bình luận
```

---

## 7. Biểu đồ Hoạt Động – Luồng chính User

```mermaid
flowchart TD
    Start([🚀 Khởi động ứng dụng]) --> CheckTheme
    CheckTheme[Áp dụng Theme + Locale] --> Main
    Main[MainActivity - Hiển thị trang chủ]
    Main --> LoadArticles[Tải bài viết đã duyệt từ DB]
    LoadArticles --> DisplayList[Hiển thị Banner + Danh sách bài]

    DisplayList --> UserAction{Hành động người dùng?}

    UserAction -->|Chọn danh mục| FilterCat[Lọc theo danh mục]
    FilterCat --> DisplayList

    UserAction -->|Tab Nổi bật| LoadFeatured[Tải bài nổi bật theo điểm]
    LoadFeatured --> DisplayList

    UserAction -->|Tìm kiếm| GoSearch[SearchActivity]
    GoSearch --> SearchDB[searchArticles keyword]
    SearchDB --> ShowResults[Hiển thị kết quả]

    UserAction -->|Nhấn bài viết| GoDetail[ArticleDetailActivity]
    GoDetail --> LoadDetail[Tải chi tiết + bình luận + reaction]
    LoadDetail --> DetailActions{Hành động?}
    DetailActions -->|Thả reaction| UpdateReaction[Cập nhật reaction DB]
    DetailActions -->|Bình luận| AddComment[Thêm bình luận DB]
    DetailActions -->|Chia sẻ| ShareArticle[Share Intent]
    DetailActions -->|Quay lại| DisplayList

    UserAction -->|Đăng bài| CheckLogin{Đã đăng nhập?}
    CheckLogin -->|Chưa| GoLogin[LoginActivity / RegisterActivity]
    GoLogin --> CheckLogin
    CheckLogin -->|Rồi| GoAdd[AddEditArticleActivity]
    GoAdd --> FillForm[Nhập tiêu đề, nội dung, ảnh/video]
    FillForm --> AIMod[Kiểm duyệt AI Claude]
    AIMod -->|Vi phạm| ShowViolation[Hiển thị lý do vi phạm]
    ShowViolation --> FillForm
    AIMod -->|Sạch| SavePending[Lưu bài PENDING vào DB]
    SavePending --> ScheduleAutoApprove[Lên lịch tự động duyệt 24h]
    ScheduleAutoApprove --> DisplayList

    UserAction -->|Cài đặt| GoSettings[SettingsActivity]
    GoSettings --> SettingsOpts{Tùy chọn?}
    SettingsOpts -->|Theme| ToggleTheme[Đổi Dark/Light]
    SettingsOpts -->|Ngôn ngữ| ChangeLang[Đổi Vi/En]
    SettingsOpts -->|Cỡ chữ| ChangeFont[Đổi Small/Medium/Large]
    ToggleTheme --> Recreate[Recreate Activity]
    ChangeLang --> Recreate
    ChangeFont --> Recreate
    Recreate --> Main
```

---

## 8. Biểu đồ Hoạt Động – Luồng Admin xử lý bài

```mermaid
flowchart TD
    Start([🔑 Admin khởi động]) --> LoginAdmin[LoginAdminActivity]
    LoginAdmin --> CheckCred{Xác thực admin?}
    CheckCred -->|Thất bại| ShowErr[Hiển thị lỗi]
    ShowErr --> LoginAdmin
    CheckCred -->|Thành công| AdminHome[AdminActivity – Quản lý bài viết]

    AdminHome --> AdminAction{Hành động?}

    AdminAction -->|Xem bài pending| GoApprove[ApproveArticleActivity]
    GoApprove --> LoadPending[Tải danh sách bài PENDING]
    LoadPending --> ArticleAction{Xử lý bài?}
    ArticleAction -->|Duyệt| ApproveBai[approveArticle - DB]
    ApproveBai --> SetApproved[Status = approved, approved_at = now]
    SetApproved --> RemoveFromList[Xóa khỏi danh sách pending]

    ArticleAction -->|Từ chối| RejectBai[rejectArticle - DB]
    RejectBai --> SetRejected[Status = rejected]
    SetRejected --> RemoveFromList

    RemoveFromList --> LoadPending

    AdminAction -->|Thêm bài| GoAddAdmin[AddEditArticleActivity admin]
    GoAddAdmin --> FillAdmin[Nhập tiêu đề, nội dung, ảnh/video]
    FillAdmin --> AIModAdmin[Kiểm duyệt AI Claude]
    AIModAdmin -->|Sạch| SaveApproved[insertArticle - APPROVED ngay]
    SaveApproved --> AdminHome
    AIModAdmin -->|Vi phạm| ShowWarn[Cảnh báo nội dung]
    ShowWarn --> FillAdmin

    AdminAction -->|Sửa bài| GoEdit[AddEditArticleActivity sửa]
    GoEdit --> EditForm[Chỉnh sửa thông tin]
    EditForm --> UpdateDB[updateArticle - DB]
    UpdateDB --> AdminHome

    AdminAction -->|Xóa bài| ConfirmDelete{Xác nhận xóa?}
    ConfirmDelete -->|Có| DeleteBai[deleteArticle - DB]
    DeleteBai --> DeleteCascade[Xóa comments + reactions liên quan]
    DeleteCascade --> AdminHome
    ConfirmDelete -->|Không| AdminHome

    AdminAction -->|Xem thống kê| GoStats[StatsActivity]
    GoStats --> LoadStats[Tải: Articles, Users, Likes, Comments]
    LoadStats --> ShowStats[Hiển thị dashboard thống kê]
    ShowStats --> AdminHome

    AdminAction -->|Tìm kiếm| GoSearchAdmin[SearchActivity admin]
    GoSearchAdmin --> AdminHome

    AdminAction -->|Cài đặt Admin| GoSettingsAdmin[SettingsActivity admin]
    GoSettingsAdmin --> AdminTheme[Đổi theme / ngôn ngữ riêng Admin]
    AdminTheme --> AdminHome
```

---

## 9. Biểu đồ Trạng Thái – Article

```mermaid
stateDiagram-v2
    [*] --> PENDING : User đăng bài\n(insertPendingArticle)

    PENDING --> APPROVED : Admin duyệt\n(approveArticle)\nhoặc AutoApprove sau 24h

    PENDING --> REJECTED : Admin từ chối\n(rejectArticle)

    APPROVED --> [*] : Admin xóa\n(deleteArticle)
    REJECTED --> [*] : Admin xóa\n(deleteArticle)

    APPROVED --> APPROVED : Admin chỉnh sửa\n(updateArticle)\nStatus không đổi

    state APPROVED {
        [*] --> Visible : approved_at được ghi
        Visible --> Featured : Điểm tương tác cao\n(likes×3 + comments×2 + shares)
        Featured --> Visible : Điểm tương tác giảm
    }

    note right of PENDING
        AI Claude kiểm duyệt
        trước khi insert
    end note

    note right of APPROVED
        Hiển thị cho tất cả
        người dùng
    end note

    note right of REJECTED
        Chỉ Admin thấy
        trong danh sách rejected
    end note
```

---

## 10. Biểu đồ Thành Phần (Component Diagram)

```mermaid
graph TB
    subgraph App["📱 AppTapchikhoakhoc (Android App)"]
        subgraph UI["🖥️ UI Layer"]
            UserUI["User Screens\n(Main, Detail, Login,\nRegister, Search,\nNotifications, Settings)"]
            AdminUI["Admin Screens\n(Admin, ApproveArticle,\nAddEdit, Stats,\nSettings, Search)"]
        end

        subgraph Logic["⚙️ Business Logic"]
            Adapters["RecyclerView Adapters\n(News, Banner, Comment,\nAdminArticle, Approve)"]
            Utils["Utils\n(ThemeManager, LocaleManager,\nTextSizeManager,\nAdminThemeManager,\nAdminLocaleManager)"]
            Moderation["ContentModerationHelper\n(Claude AI Integration)"]
            AutoApprove["AutoApproveHelper\n+ AutoApproveWorker\n(WorkManager)"]
        end

        subgraph Data["💾 Data Layer"]
            DBHelper["DatabaseHelper\n(SQLiteOpenHelper)"]
            Models["Models\n(Article, Comment)"]
        end

        subgraph Storage["🗄️ Local Storage"]
            SQLite[("SQLite DB\nAppDatabase.db\nv9")]
            SharedPrefs[("SharedPreferences\n- UserSession\n- AppTheme\n- AppSettings\n- AdminSettings")]
        end
    end

    subgraph External["🌐 External"]
        ClaudeAPI["Anthropic API\nClaude Haiku\n(Content Moderation)"]
    end

    UserUI <--> Logic
    AdminUI <--> Logic
    Logic <--> Data
    Data <--> Storage
    Moderation <-->|HTTPS POST| ClaudeAPI
    AutoApprove -->|WorkManager| DBHelper
    DBHelper --> SQLite
    Utils --> SharedPrefs
```

---

## 11. Biểu đồ Gói (Package Diagram)

```mermaid
graph LR
    subgraph pkg["com.example.apptapchikhoakhoc"]
        model["📦 model\n─────────────\nArticle\nComment"]

        data["📦 data\n─────────────\nDatabaseHelper\nBaseActivity"]

        utils["📦 utils\n─────────────\nThemeManager\nAdminThemeManager\nLocaleManager\nAdminLocaleManager\nTextSizeManager\nContentModerationHelper\nAutoApproveHelper\nAutoApproveWorker\nCategoryBadgeHelper"]

        adapter["📦 adapter\n─────────────\nNewsAdapter\nFeaturedBannerAdapter\nCommentAdapter\nRelatedNewsAdapter"]

        main["📦 main\n─────────────\nMainActivity\nArticleDetailActivity\nLoginActivity\nNotificationsActivity\nNotificationItemAdapter\nSettingsActivity\nPrivacyActivity\nAddEditArticleActivity"]

        register["📦 register\n─────────────\nRegisterActivity"]

        search["📦 search\n─────────────\nSearchActivity"]

        admin["📦 admin\n─────────────\nBaseAdminActivity\nAdminActivity\nAdminArticleAdapter\nApproveArticleActivity\nApproveArticleAdapter\nAddEditArticleActivity\nLoginAdminActivity\nSearchActivity\nSettingsActivity\nStatsActivity\nContactAdminActivity"]
    end

    main --> model
    main --> data
    main --> adapter
    main --> utils
    admin --> model
    admin --> data
    admin --> utils
    adapter --> model
    data --> model
    register --> data
    search --> model
    search --> data
    admin --> adapter
```

---

## 12. Biểu đồ Cơ Sở Dữ Liệu (ERD)

```mermaid
erDiagram
    users {
        INTEGER id PK "AUTOINCREMENT"
        TEXT name "NOT NULL"
        TEXT email "UNIQUE NOT NULL"
        TEXT password "NOT NULL"
    }

    articles {
        INTEGER id PK "AUTOINCREMENT"
        TEXT title "NOT NULL"
        TEXT author
        TEXT category
        TEXT content
        TEXT image_path
        TEXT video_path
        INTEGER likes "DEFAULT 0"
        INTEGER comments "DEFAULT 0"
        INTEGER shares "DEFAULT 0"
        TEXT status "DEFAULT 'approved'"
        TEXT user_email "DEFAULT ''"
        LONG approved_at "DEFAULT 0"
    }

    comments {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER article_id "NOT NULL, FK"
        TEXT user_name "DEFAULT ''"
        TEXT user_email "DEFAULT ''"
        TEXT content "NOT NULL"
        LONG timestamp "NOT NULL"
    }

    article_reactions {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER article_id "NOT NULL, FK"
        TEXT user_email "NOT NULL"
        TEXT emoji "NOT NULL"
        LONG timestamp "NOT NULL"
    }

    comment_reactions {
        INTEGER id PK "AUTOINCREMENT"
        INTEGER comment_id "NOT NULL, FK"
        TEXT user_email "NOT NULL"
        TEXT emoji "NOT NULL"
        LONG timestamp "NOT NULL"
    }

    users ||--o{ articles : "đăng"
    articles ||--o{ comments : "có"
    articles ||--o{ article_reactions : "nhận"
    comments ||--o{ comment_reactions : "nhận"
    users ||--o{ comments : "viết"
    users ||--o{ article_reactions : "thả"
    users ||--o{ comment_reactions : "thả"
```

---

## Tổng kết kiến trúc

| Thành phần | Số lượng | Mô tả |
|---|---|---|
| **Packages** | 8 | model, data, utils, adapter, main, register, search, admin |
| **Activities (User)** | 8 | Main, Detail, Login, Register, Search, Notifications, Settings, Privacy |
| **Activities (Admin)** | 8 | Admin, Approve, AddEdit, Login, Search, Settings, Stats, Contact |
| **Adapters** | 7 | News, Banner, Comment, RelatedNews, AdminArticle, Approve, Notification |
| **Models** | 2 | Article, Comment |
| **Bảng DB** | 5 | users, articles, comments, article_reactions, comment_reactions |
| **Utils** | 9 | Theme×2, Locale×2, TextSize, Moderation, AutoApprove×2, CategoryBadge |
| **External API** | 1 | Anthropic API (Claude Haiku) |
