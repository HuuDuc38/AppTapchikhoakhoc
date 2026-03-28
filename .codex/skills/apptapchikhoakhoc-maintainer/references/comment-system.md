# Comment System

## Files to inspect first

- `app/src/main/java/com/example/apptapchikhoakhoc/main/ArticleDetailActivity.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/adapter/CommentAdapter.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/data/DatabaseHelper.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/model/Comment.java`
- `app/src/main/java/com/example/apptapchikhoakhoc/admin/CommentStatsDetailActivity.java`
- `app/src/main/res/layout/bottom_sheet_comment.xml`
- `app/src/main/res/layout/item_comment.xml`

## Current data model

- Comments are stored in the `comments` table.
- Comment reactions are stored in the `comment_reactions` table.
- `DatabaseHelper.addComment(...)` inserts the comment row and increments the article's stored comment counter.
- `DatabaseHelper.getCommentsByArticle(...)` returns a flat list ordered by timestamp descending.
- `DatabaseHelper.getCommentsCount(...)` uses a live `COUNT(*)` query over the comments table.

## User flow in `ArticleDetailActivity`

- Tapping the article comment action requires a logged-in user.
- `showCommentBottomSheet(...)` inflates `bottom_sheet_comment.xml`, loads comments from SQLite, and binds a `CommentAdapter`.
- Sending a new comment writes through `db.addComment(...)`, prepends a new `Comment` to the adapter, then refreshes both:
  - the bottom-sheet header count
  - the external article-detail comment bar

Count changes are updated in more than one place, so comment-related UI work should verify both surfaces stay in sync.

## Reply behavior

- The current system is pseudo-threaded, not a true threaded comment model.
- `CommentAdapter.OnReplyListener` sends reply text back to `ArticleDetailActivity`.
- The activity persists replies as normal comments whose content starts with `@parentUserName`.
- `CommentAdapter.addReplyView(...)` renders nested reply bubbles only in the current view hierarchy.
- When the sheet is reopened, replies are rebuilt as normal flat comments because there is no `parent_comment_id` or tree reconstruction logic.

If a task asks for real threaded replies, expect a schema change, DB migration, model changes, adapter tree rendering, admin query updates, and new count semantics.

## Reaction behavior

- `CommentAdapter` caches the current user's reaction per comment and the total reaction count per comment.
- `setList(...)` clears caches and calls `loadReactionsFromDb()`.
- `loadReactionsFromDb()` returns early if `db == null`.
- The adapter only knows how to reload persisted comment reactions when `setDatabase(db, userEmail, context)` is wired.

Important footgun:

- `ArticleDetailActivity.showCommentBottomSheet(...)` currently calls `adapter.setList(comments)` but does not call `adapter.setDatabase(db, userEmail, context)`.
- Result: comment rendering works, but persisted comment reactions are not reliably reloaded when reopening the bottom sheet.

If a request touches comment reactions, make sure the adapter's DB wiring and user-email source are still correct.

## Session source

- `CommentAdapter` reads the acting user from `UserPrefs`.
- Other parts of the app also use `UserSession`.
- If login, logout, or account-switching behavior changes, verify comment reactions still resolve the correct email.

## Admin comment flow

- `CommentStatsDetailActivity` loads a flat admin list via `DatabaseHelper.getAllCommentsForAdmin()`.
- The admin query joins article title and computes a simple `violated` flag using keyword matching.
- Admin deletion uses `DatabaseHelper.deleteComment(...)`, which deletes `comment_reactions`, deletes the comment, and decrements the article's stored comment counter.
- There is no separate moderation workflow for comments beyond flagging by keyword and deleting rows.

## Change checklist

- If changing comment content, count, or sort order, verify both the live count query and the stored article comment counter stay consistent.
- If changing reply behavior, decide explicitly whether the task should preserve the current flat-storage model or move to a real parent-child schema.
- If changing admin moderation, update both the admin list query and any delete-side effects.
- If changing adapter behavior, test reopening the bottom sheet to catch state that only exists in memory.
