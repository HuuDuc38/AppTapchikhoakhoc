# AGENTS.md

## Purpose

This repository contains `AppTapchikhoakhoc`, a single-module Android Java app for publishing and reading science-journal style content.

The app currently has two major surfaces:

- User-facing app:
  browse approved articles, featured content, article detail pages, inline video, reactions, comments, search, notifications, settings, and article submission.
- Admin-facing app:
  admin login, article management, article approval, statistics dashboards, comment moderation views, user/view/like/comment drill-down screens, and admin-specific settings.

This codebase is not using MVVM or a repository pattern today. It is an activity-driven Android app with a custom SQLite data layer, several RecyclerView adapters, and a set of SharedPreferences-backed manager utilities for theme, locale, and text size.

When working in this repo, optimize for consistency with the current architecture unless the task explicitly asks for a deeper refactor.

## Current Tech Stack

- Platform: Android
- Language: Java
- Build system: Gradle Kotlin DSL
- Root module: `:app`
- Android Gradle Plugin: `8.8.0`
- Gradle wrapper: `8.13`
- Java source compatibility: `11`
- Java target compatibility: `11`
- Minimum SDK: `24`
- Compile SDK: `35`
- Target SDK: `35`
- UI libraries:
  `androidx.appcompat`, Material Components, ConstraintLayout, RecyclerView, DrawerLayout, NavigationView, BottomNavigationView, ViewPager2, BottomSheetDialog
- Media/image libraries:
  Glide, VideoView, MediaMetadataRetriever, WebView
- Rich text editing:
  `jp.wasabeef:richeditor-android`
- Background work:
  WorkManager
- Persistence:
  SQLite via `SQLiteOpenHelper`
- Localization and theming:
  custom managers built on SharedPreferences

Additional observations:

- `app/google-services.json` exists, but no active Firebase usage was observed in the current Java sources or Gradle dependency list.
- `app/build.gradle.kts` currently contains duplicate/overlapping dependency declarations:
  Material is declared both through version catalog and explicit coordinate, and RichEditor is declared twice with different versions.
- Release minification is currently disabled, and `proguard-rules.pro` is still close to the default template.

## Important Build Files

- `build.gradle.kts`
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `gradle/wrapper/gradle-wrapper.properties`
- `app/src/main/AndroidManifest.xml`

## Repo Layout

Top-level:

- `app/`
  Main Android application module.
- `gradle/`
  Wrapper and version catalog files.
- `build/`
  Root build output.
- `uml_diagrams.md`
  Architecture and UML reference doc. Helpful, but verify against code before trusting it.
- `usecase_diagram.html`
  Use-case documentation artifact.
- `bieudo_phanra.html`
  Additional diagram/documentation artifact.

Key app source layout:

- `app/src/main/java/com/example/apptapchikhoakhoc/adapter`
  Shared user-facing RecyclerView adapters.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin`
  Admin activities, adapters, and stats/detail screens.
- `app/src/main/java/com/example/apptapchikhoakhoc/data`
  Base activity and SQLite data access layer.
- `app/src/main/java/com/example/apptapchikhoakhoc/main`
  Main user-facing activities and feature flows.
- `app/src/main/java/com/example/apptapchikhoakhoc/model`
  Plain data models such as `Article`, `Comment`, and `UserItem`.
- `app/src/main/java/com/example/apptapchikhoakhoc/register`
  User registration screen.
- `app/src/main/java/com/example/apptapchikhoakhoc/search`
  User search screen.
- `app/src/main/java/com/example/apptapchikhoakhoc/utils`
  Theme, locale, text-size, moderation, and auto-approval helpers.
- `app/src/main/res/layout`
  XML screens, dialogs, bottom sheets, and item layouts.
- `app/src/main/res/values` and `app/src/main/res/values-en`
  String, theme, style, and color resources.
- `app/src/test`
  Local JVM tests. Currently only template coverage.
- `app/src/androidTest`
  Instrumentation tests. Currently only template coverage.

Observed scale at analysis time:

- Java source files: `49`
- Layout XML files: `43`
- Drawable files: `185`
- Menu XML files: `5`
- Animation XML files: `5`

Package counts:

- `adapter`: `4`
- `admin`: `19`
- `data`: `2`
- `main`: `10`
- `model`: `3`
- `register`: `1`
- `search`: `1`
- `utils`: `9`

## Build, Run, and Test Commands

Use these from the repository root on Windows PowerShell:

- Debug APK:
  `.\gradlew.bat assembleDebug`
- Install debug APK on a connected device/emulator:
  `.\gradlew.bat installDebug`
- Clean build outputs:
  `.\gradlew.bat clean`
- Unit tests:
  `.\gradlew.bat test`
- Instrumentation tests:
  `.\gradlew.bat connectedAndroidTest`
- Lint:
  `.\gradlew.bat lint`
- Release bundle:
  `.\gradlew.bat bundleRelease`
- Show available Gradle tasks:
  `.\gradlew.bat tasks --all`

If running inside a sandboxed or agent environment where the default Gradle cache location is not writable, set a repo-local Gradle home first:

```powershell
$env:GRADLE_USER_HOME = (Join-Path (Get-Location) ".gradle-user")
.\gradlew.bat assembleDebug
```

Notes:

- First Gradle invocation may need internet access to download Gradle `8.13`.
- `local.properties` is local-only and ignored by git.
- `connectedAndroidTest` requires an emulator or physical device.

## App Architecture

### High-level structure

The current architecture is best described as:

- activity-driven UI
- SQLite-backed persistence
- utility-manager based cross-cutting concerns
- adapter-heavy list rendering
- feature/layer hybrid package organization

This is not a strict clean architecture setup. In practice:

- Activities own a large amount of UI logic, state management, event handling, and orchestration.
- `DatabaseHelper` owns nearly all persistence behavior, including schema creation, migrations, CRUD, auth checks, reactions, comments, analytics queries, and admin data queries.
- Adapters often contain meaningful interaction logic, not just view binding.
- SharedPreferences managers handle user/admin theme, locale, and text size.

### User/admin split

There is an intentional split between user and admin preferences:

- User theme:
  `ThemeManager`
- User locale:
  `LocaleManager`
- User text size:
  `TextSizeManager`
- Admin theme:
  `AdminThemeManager`
- Admin locale:
  `AdminLocaleManager`

`BaseActivity` is the base for most user-facing screens and applies:

- locale in `attachBaseContext`
- font scale in `attachBaseContext`
- theme/status bar refresh in lifecycle callbacks

`BaseAdminActivity` is the intended base for admin screens and applies:

- admin-localized context
- admin dark mode state
- recreate-on-admin-locale-change behavior

Important caveat:

- not every admin screen actually extends `BaseAdminActivity`
- some admin screens still use `AppCompatActivity`
- one admin add/edit screen extends `BaseActivity`
- this causes inconsistency in admin theme/locale handling

### Content flow

Observed article lifecycle:

- user or admin creates article
- article may be stored as `pending`
- admin can approve or reject
- approved articles appear in main user flows
- user interactions update counts for views, likes, comments, and shares

Status values are stored as raw strings:

- `pending`
- `approved`
- `rejected`

### Rich content flow

Article content is authored as HTML via `RichEditor` and rendered in a `WebView`.

Media handling:

- cover images and videos are selected from storage
- files are copied into app-internal storage by add/edit activities
- article detail screens render image/video from stored file paths
- embedded content images can be inserted into the editor as base64 HTML images

### Auto-approval flow

There are two parallel auto-approval implementations:

- `AutoApproveHelper`
  handler/thread based delayed approval
- `AutoApproveWorker`
  WorkManager-based delayed approval

Current usage is split:

- `main/AddEditArticleActivity` uses `AutoApproveHelper`
- `admin/AddEditArticleActivity` uses `AutoApproveWorker`

Keep that inconsistency in mind before changing approval behavior.

## Important Classes and Interfaces

### Core data layer

- `app/src/main/java/com/example/apptapchikhoakhoc/data/DatabaseHelper.java`
  The central SQLite helper. This is the most important persistence file in the repo.
- `app/src/main/java/com/example/apptapchikhoakhoc/data/BaseActivity.java`
  Base class for user-facing screens. Applies locale, text size, and theme behavior.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/BaseAdminActivity.java`
  Intended base class for admin screens. Keeps admin locale/theme isolated from user settings.

### Models

- `app/src/main/java/com/example/apptapchikhoakhoc/model/Article.java`
  Serializable article model passed between activities.
- `app/src/main/java/com/example/apptapchikhoakhoc/model/Comment.java`
  Comment model, also used in admin comment-stat flows.
- `app/src/main/java/com/example/apptapchikhoakhoc/model/UserItem.java`
  Admin stats model for user summaries.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/ArticleStatItem.java`
  Admin stats model for article-level like stats.

### User-facing screens

- `app/src/main/java/com/example/apptapchikhoakhoc/main/MainActivity.java`
  Main entry screen. Drawer, bottom navigation, featured banner, category filtering, and launch points to other flows.
- `app/src/main/java/com/example/apptapchikhoakhoc/main/ArticleDetailActivity.java`
  Large detail screen with WebView, video playback, reactions, comments, reply flow, and related news.
- `app/src/main/java/com/example/apptapchikhoakhoc/main/AddEditArticleActivity.java`
  User submission/editor flow with moderation and auto-approval.
- `app/src/main/java/com/example/apptapchikhoakhoc/main/FeaturedActivity.java`
  Featured/latest approved content.
- `app/src/main/java/com/example/apptapchikhoakhoc/main/NotificationsActivity.java`
  User article and latest-content notification-style screen.
- `app/src/main/java/com/example/apptapchikhoakhoc/main/SettingsActivity.java`
  User settings for theme, locale, text size, privacy, admin login entry point, and profile/logout behavior.
- `app/src/main/java/com/example/apptapchikhoakhoc/main/LoginActivity.java`
  User login against SQLite users table.
- `app/src/main/java/com/example/apptapchikhoakhoc/register/RegisterActivity.java`
  User registration against SQLite users table.
- `app/src/main/java/com/example/apptapchikhoakhoc/search/SearchActivity.java`
  User search over approved articles only.

### Admin screens

- `app/src/main/java/com/example/apptapchikhoakhoc/admin/LoginAdminActivity.java`
  Admin login screen with hardcoded credentials.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/AdminActivity.java`
  Admin home/article management list.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/AddEditArticleActivity.java`
  Admin add/edit flow with admin-specific theme handling and WorkManager auto-approval.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/ApproveArticleActivity.java`
  Pending/approved/rejected article approval queue.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/StatsActivity.java`
  Overview stats dashboard.
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/UserStatsDetailActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/ViewStatsDetailActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/LikeStatsDetailActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/CommentStatsDetailActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/SettingsActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/SearchActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/ContactAdminActivity.java`

### Adapters

- `adapter/NewsAdapter`
  Main article list card adapter.
- `adapter/FeaturedBannerAdapter`
  Main screen featured carousel adapter.
- `adapter/CommentAdapter`
  Comment list, reply input, and comment reaction state.
- `adapter/RelatedNewsAdapter`
  Related content list.
- `main/NotificationItemAdapter`
  Notification-style cards for user article and new content sections.
- `main/FeaturedAdapter`
  Featured screen list.
- `admin/AdminArticleAdapter`
  Admin article list.
- `admin/ApproveArticleAdapter`
  Approval queue cards.
- `admin/CommentAdminAdapter`
  Admin comment list.
- `admin/UserStatsAdapter`
- `admin/LikeStatsAdapter`

### Cross-cutting utilities

- `utils/ThemeManager`
- `utils/AdminThemeManager`
- `utils/LocaleManager`
- `utils/AdminLocaleManager`
- `utils/TextSizeManager`
- `utils/ContentModerationHelper`
- `utils/AutoApproveHelper`
- `utils/AutoApproveWorker`
- `utils/CategoryBadgeHelper`

### Interfaces worth knowing

- `CommentAdapter.OnReplyListener`
- `ContentModerationHelper.ModerationCallback`
- `AdminArticleAdapter.OnItemClickListener`
- `ApproveArticleAdapter.OnActionListener`
- `CommentAdminAdapter.OnDeleteListener`

## Database and State Notes

`DatabaseHelper` currently handles all of the following:

- schema creation and migration
- user registration/login lookup
- article CRUD
- article status transitions
- article filtering and search
- featured and latest article queries
- counts and stats queries
- comment CRUD helpers
- reaction CRUD helpers
- admin comment/user analytics queries

Schema highlights:

- DB name:
  `AppDatabase.db`
- DB version:
  `10`
- Main tables:
  `users`, `articles`, `comments`, `article_reactions`, `comment_reactions`

Rules when changing schema or persistence logic:

- If you add/remove columns or change table structure, update `DB_VERSION`.
- Add the migration path in `onUpgrade`.
- Search all query helpers for the changed field before finishing.
- Check both user and admin screens for any assumptions about the field.
- Be careful with intent serialization if the field belongs to `Article`.

SharedPreferences names currently used across the codebase:

- `UserPrefs`
- `UserSession`
- `AppSettings`
- `AppTheme`
- `AdminSettings`
- `AdminThemePrefs`
- `AdminLoginPrefs`
- `AdminSession`

Be aware that session/auth state is fragmented across multiple pref files.

## Comment System

### Current shape

The current comment implementation is spread across:

- `main/ArticleDetailActivity`
  owns comment entry, bottom-sheet presentation, live count updates, and reply persistence callbacks.
- `adapter/CommentAdapter`
  owns comment rendering, inline reply UI, local reply bubble rendering, and comment reaction interaction state.
- `data/DatabaseHelper`
  owns comment CRUD and comment-reaction CRUD.
- `admin/CommentStatsDetailActivity`
  provides the admin-side flat list, summary counts, violated-only filter, and deletion flow.

Backing tables:

- `comments`
- `comment_reactions`

### User comment flow

Observed behavior today:

- Tapping the article comment action in `ArticleDetailActivity` requires login.
- `showCommentBottomSheet(...)` inflates `bottom_sheet_comment.xml` and loads comments with `db.getCommentsByArticle(article.getId())`.
- Comments are shown in descending timestamp order.
- New comments are persisted with `db.addComment(articleId, userName, userEmail, content, timestamp)`.
- On successful send, the activity manually prepends a new `Comment` object into the adapter, scrolls to the top, recalculates count with `db.getCommentsCount(article.getId())`, updates the sheet header, and updates the external article-detail comment bar.

Important consequence:

- comment count is maintained in more than one place:
  the article table's stored `comments` value is incremented or decremented during writes, while UI code also reads a live `COUNT(*)` from the comments table.
- if you touch comment creation, deletion, or migration logic, verify both count surfaces stay consistent.

### Bottom sheet and layout notes

`bottom_sheet_comment.xml` is the main user comment entry surface and includes:

- `tvCommentCount`
  bottom-sheet title/count label
- `recyclerComments`
  flat comment list
- `tvNoComments`
  empty state
- `tvMyAvatar`
  current user avatar initial
- `etComment`
  text input
- `btnSendComment`
  send action

The bottom sheet is styled dynamically in Java based on dark-mode state rather than relying only on XML themes.

### Reply behavior

Replies are not implemented as a true threaded comment model.

Current behavior:

- `CommentAdapter` exposes `OnReplyListener`.
- When the user sends a reply, `ArticleDetailActivity` persists it by calling `db.addComment(...)` with content prefixed as `@parentUserName replyText`.
- `CommentAdapter.addReplyView(...)` also renders a nested reply bubble directly into the current item's view hierarchy.

This means:

- replies are stored as normal top-level comments in SQLite
- there is no `parentCommentId` or equivalent relation in the current schema
- reopening the sheet reconstructs a flat list sorted by timestamp, not a nested thread tree
- inline reply bubbles shown by the adapter are session-local UI state, not a persisted threaded view

If a future task asks for real threaded replies, that is not a small UI-only change. It requires:

- schema changes and `DB_VERSION` bump
- migration logic in `DatabaseHelper.onUpgrade`
- `Comment` model changes
- adapter/UI tree reconstruction
- admin query and moderation updates
- clear decisions about how comment counts should behave for parent and child replies

### Comment reactions

Comment reactions are partially persisted and partially adapter-managed.

Current design:

- `CommentAdapter` keeps:
  `reactionMap` for the current user's selected emoji per comment
- `CommentAdapter` also keeps:
  `reactionCountMap` for total reactions per comment
- `DatabaseHelper` provides:
  `getCommentReactionsForUser(...)`,
  `getCommentReactionCounts(...)`,
  `addOrUpdateCommentReaction(...)`,
  and `removeCommentReaction(...)`

Important implementation detail:

- `CommentAdapter.setList(...)` clears caches and calls `loadReactionsFromDb()`
- `loadReactionsFromDb()` returns early when `db == null`
- `ArticleDetailActivity.showCommentBottomSheet(...)` currently calls `adapter.setList(comments)` but does not call `adapter.setDatabase(db, userEmail, context)`

Consequence:

- comment reactions can be toggled in the adapter
- but persisted comment-reaction state is not reliably reloaded when reopening the comment sheet unless the adapter is wired with `setDatabase(...)`

This is one of the most important comment-system footguns in the current repo. If you touch comment reactions, verify reload behavior by closing and reopening the bottom sheet, not just by testing the first live interaction.

### Session source for comment actions

Another subtle point:

- `CommentAdapter` reads user identity from `UserPrefs`
- other parts of the app also use `UserSession`

If login, logout, account switching, or session persistence changes, test comment reactions and replies carefully because the acting email source is not centralized.

### Admin comment flow

Admin comment review is implemented as a flat moderation/analytics view rather than a true moderation pipeline.

Observed behavior:

- `CommentStatsDetailActivity` loads all comments from `db.getAllCommentsForAdmin()`
- `DatabaseHelper.getAllCommentsForAdmin()` joins comments to article title and marks items as `violated` by running `containsViolation(...)` against a local keyword blacklist
- admin UI supports:
  all comments
  violated-only comments
  delete actions
- `db.deleteComment(commentId)` deletes related `comment_reactions`, deletes the comment row, and decrements the article's stored comment counter

Limitations:

- comment moderation is keyword-based only
- there is no review state, approval queue, or audit trail for comments
- replies are not represented as parent/child structures in admin views either

### Files to inspect before changing comments

At minimum, inspect all of these before shipping a comment-related change:

- `app/src/main/java/com/example/apptapchikhoakhoc/main/ArticleDetailActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/adapter/CommentAdapter.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/data/DatabaseHelper.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/model/Comment.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/CommentStatsDetailActivity.java`
- `app/src/main/res/layout/bottom_sheet_comment.xml`
- `app/src/main/res/layout/item_comment.xml`

### Recommended comment-system guardrails

- For small UI tweaks, preserve the existing flat comment storage model unless the task explicitly asks for threaded replies.
- For reaction fixes, confirm adapter DB wiring before changing SQL logic.
- For schema changes, update `DB_VERSION`, `onUpgrade`, model constructors, admin screens, and any code that depends on comment count.
- For moderation work, remember that admin views are driven by a flat joined query and a keyword blacklist, not a separate moderation domain model.

## Coding Conventions Already In Use

These are the strongest conventions observed in the current code:

### General style

- Java classes and method names are mostly English.
- Many comments and user-visible strings are Vietnamese.
- New work should preserve the current language style in touched areas unless the task explicitly asks to translate or normalize copy.
- Models are plain mutable POJOs.
- `Article` is `Serializable` and is passed through `Intent` extras between activities.

### UI conventions

- Activities usually call `setContentView(...)`, then manually `findViewById(...)`, then a chain of setup/apply methods.
- Theme is frequently applied programmatically after view initialization.
- A lot of color and UI state is set directly in Java rather than fully delegated to XML theme resources.
- RecyclerView adapters commonly expose `setList(...)` or `updateList(...)`.
- Some adapters also own interaction state and DB calls, especially `CommentAdapter`.

### Base-class expectations

- Most user-facing screens should extend `BaseActivity`.
- Admin screens ideally should extend `BaseAdminActivity`.
- If you add a new user-facing activity and it needs theme/locale/text-size behavior, use `BaseActivity`.
- If you add a new admin activity and it should respect admin-local settings, prefer `BaseAdminActivity`.

### Persistence conventions

- Activities often instantiate `new DatabaseHelper(this)` directly.
- There is no repository abstraction today.
- Status/category filtering is often done by direct string matching in SQL or Java.
- If you change raw string values used in DB state, audit every comparison.

### Resource conventions

- Prefer string resources for new user-facing text.
- Existing code still contains hardcoded strings in some activities and adapters.
- Layout naming is mostly feature-oriented:
  `activity_*`, `dialog_*`, `item_*`, `bottom_sheet_*`

### Categories and statuses

- Article status strings are raw constants from `DatabaseHelper`.
- Category values are also stored as plain text and compared literally across screens, editors, tabs, and adapters.
- Do not casually rename categories without auditing all filtering, badge-color, spinner, and UI display logic.

## How To Work Safely In This Repo

### Before editing

- Search for both user and admin versions of a feature before changing anything.
- Check whether there is already a similar class in another package.
- If changing article editor behavior, inspect both:
  `main/AddEditArticleActivity` and `admin/AddEditArticleActivity`
- If changing theme/locale/text size, inspect:
  `BaseActivity`, `BaseAdminActivity`, settings screens, and the relevant manager classes.
- If changing comments or reactions, inspect:
  `ArticleDetailActivity`, `CommentAdapter`, and related `DatabaseHelper` methods.
- If changing article lifecycle or approval, inspect:
  user add/edit flow, admin add/edit flow, approval screen, notification screen, and both auto-approve helpers.

### After editing

- Run the smallest relevant Gradle task first.
- For UI changes, prefer a targeted manual smoke test on device/emulator because automated coverage is currently minimal.
- If you changed schema logic, clear/install or test upgrade paths deliberately.

## Testing Strategy

### Current status

Current automated coverage is minimal:

- `app/src/test/java/com/example/apptapchikhoakhoc/ExampleUnitTest.java`
- `app/src/androidTest/java/com/example/apptapchikhoakhoc/ExampleInstrumentedTest.java`

These are template tests and do not meaningfully cover application behavior.

### Practical testing approach for this repo

For now, testing should be a mix of targeted automated checks and manual regression testing.

Recommended manual smoke tests for most feature changes:

- app launch to `MainActivity`
- open article detail from list/banner/featured
- login and register flows
- submit a new article
- admin login
- approve or reject a pending article
- comment and react on an article
- switch user theme, locale, and text size
- switch admin theme and locale

Recommended areas for future automated coverage:

- `DatabaseHelper` query and migration logic
- `ThemeManager`, `AdminThemeManager`, `LocaleManager`, `TextSizeManager`
- `ContentModerationHelper` response parsing
- article approval and auto-approval behavior
- search/filter logic
- session preference handling

## Known Pain Points and Improvement Areas

These are the most important issues currently visible in the codebase.

### 1. Monolithic data layer

`DatabaseHelper` is very large and mixes many unrelated responsibilities:

- auth
- content CRUD
- moderation helpers
- reactions
- comments
- analytics
- admin reporting

Any change here has broad blast radius. Refactor carefully.

### 2. Very large activities

Several activities are large enough to be high-risk maintenance hotspots:

- `DatabaseHelper.java` is over 1000 lines
- `ArticleDetailActivity.java` is near 1000 lines
- `main/AddEditArticleActivity.java` is very large
- `admin/AddEditArticleActivity.java` is also very large
- settings/privacy screens contain a lot of direct UI logic

Prefer small, targeted edits unless the task is explicitly a larger refactor.

### 3. Security issues

Observed security concerns:

- user passwords are stored in plaintext in SQLite
- admin credentials are hardcoded in `LoginAdminActivity`
- `android:usesCleartextTraffic="true"` is enabled
- `android:requestLegacyExternalStorage="true"` is enabled

Treat auth and transport changes as high-risk and high-value improvements.

### 4. Duplicate and inconsistent implementations

There are multiple duplicated or overlapping implementations:

- two article editor screens with overlapping logic
- two auto-approval mechanisms
- duplicated dependency declarations in Gradle
- multiple pref files for related session/auth state
- multiple admin screens not aligned on the same base class/theme manager

When fixing one flow, check whether the parallel implementation also needs the same change.

### 5. Admin consistency gaps

Admin architecture is inconsistent today:

- `admin/SearchActivity` extends `AppCompatActivity`
- `admin/ContactAdminActivity` extends `AppCompatActivity`
- `admin/LoginAdminActivity` extends `AppCompatActivity`
- `admin/AddEditArticleActivity` extends `BaseActivity`, not `BaseAdminActivity`

This means admin screens do not all share the same locale/theme/session model.

### 6. Admin session flag looks incomplete

`AdminSession.isAdminLoggedIn` is read in both add/edit article flows, but no corresponding setter was observed during code search.

Implication:

- do not assume `AdminSession` is a reliable source of truth
- verify intended admin-session behavior before building on top of it

### 7. Search behavior needs review

Potential issues observed:

- `DatabaseHelper.searchArticles(...)` only searches approved articles
- admin search reuses that same query
- `admin/SearchActivity` updates a local list but does not push results into the adapter with `setList(...)`

This area likely needs cleanup before any admin-search enhancement.

### 8. Moderation integration is not production-ready

`ContentModerationHelper` currently has several limitations:

- placeholder API key string
- raw `HttpURLConnection`
- manual thread management
- manual JSON string parsing
- fallback behavior that approves on API/parser error

This should be treated as a prototype, not a hardened production integration.

### 9. Timestamp semantics are inconsistent

`approvedAt` is being used as an important ordering/display field, but rejection handling currently resets it to `0`.

If you touch approval/rejection UX, verify how timestamps are intended to work for both approved and rejected items.

### 10. Automated coverage is missing

Any non-trivial change should be paired with either:

- at least a targeted test addition, or
- a clear manual regression checklist

## Recommendations For Future Refactors

If the project moves toward a more maintainable architecture, the highest-value refactors are:

- split `DatabaseHelper` into focused data/service classes
- extract reusable article-editor logic shared by user/admin flows
- normalize session handling into one clear user session model and one clear admin session model
- replace plaintext password storage with proper hashing
- replace hardcoded admin credentials with a real auth mechanism
- consolidate auto-approval into one implementation
- migrate admin screens to a consistent base-class/theming model
- move repeated category/status logic into centralized constants/helpers
- expand test coverage around SQL and lifecycle-sensitive flows

## Agent Workflow Guidance

When using Codex or another coding agent on this repo:

- do not assume MVVM, Room, or Hilt exist
- do not introduce large architectural churn unless explicitly asked
- prefer matching the existing activity/adaptor/SQLite style for localized fixes
- search for parallel user/admin implementations before finalizing any change
- treat `DatabaseHelper`, theme managers, and article-editor screens as high-risk areas
- keep changes incremental and verify behavior with targeted builds or manual smoke tests
- if a task touches auth, moderation, schema, or storage, call out risk clearly in the final summary

## Quick Change Checklist

Use this before wrapping up a change:

- Did I check both user and admin variants of the feature?
- Did I update `DatabaseHelper` and `DB_VERSION` if schema changed?
- Did I keep raw status/category string usage consistent?
- Did I preserve `Article` intent serialization expectations?
- Did I verify theme/locale/text-size behavior on touched screens?
- Did I run at least one relevant Gradle task?
- Did I note any manual testing still required?
