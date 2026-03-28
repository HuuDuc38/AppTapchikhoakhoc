---
name: apptapchikhoakhoc-maintainer
description: Use this skill when working on the AppTapchikhoakhoc Android Java project, especially for Gradle build and deploy tasks, safe refactors, SQLite-backed feature work, and comment or reaction changes that touch both user and admin flows.
---

# AppTapchikhoakhoc Maintainer

## Overview

This skill helps Codex make safe, repo-aware changes in `AppTapchikhoakhoc`. Use it for Android Java feature work, build and release tasks, incremental refactors, SQLite or SharedPreferences changes, and any work on comments, reactions, or moderation.

## Workflow Decision Tree

- If the task is build, install, lint, test, or release oriented, read [references/workflows.md](references/workflows.md) first.
- If the task touches comments, replies, comment reactions, or admin comment moderation, read [references/comment-system.md](references/comment-system.md) before editing.
- If the task changes article creation, approval, search, settings, or theming, inspect both user and admin implementations before coding.
- If the task changes database schema or persisted state, audit `DatabaseHelper`, affected models, and every screen that reads or writes the field before finishing.

## Core Working Rules

- Match the current architecture: activity-driven UI, SQLite via `DatabaseHelper`, adapters for list interactions, and SharedPreferences managers for theme, locale, and session behavior.
- Prefer incremental changes over introducing MVVM, Room, Hilt, or repository abstractions unless the user explicitly asks for architectural work.
- Search for parallel implementations before editing: `main/` vs `admin/`, user settings vs admin settings, `BaseActivity` vs `BaseAdminActivity`, and both add/edit article flows.
- Treat `DatabaseHelper.java`, `ArticleDetailActivity.java`, `CommentAdapter.java`, and both add/edit article activities as high-risk files. Make the smallest change that solves the task.
- Keep raw status strings, category names, intent extras, and serialized `Article` usage backward compatible unless every caller is being updated in the same change.
- Prefer string resources for new user-facing text when practical, but preserve the local style in touched files.
- After changes, run the smallest relevant Gradle command first and call out manual testing that still matters.

## Common Tasks

### Build, install, and release

- Use the repo root and the Windows-friendly Gradle commands listed in [references/workflows.md](references/workflows.md).
- If Gradle cannot write to the default cache, set `GRADLE_USER_HOME` to `.gradle-user`.
- For production or release work, verify article detail, login, comment flow, and admin approval manually even if the build succeeds.

### Safe refactor

1. Locate the exact user-visible behavior first.
2. Check for mirrored code in `main/`, `admin/`, adapters, and `DatabaseHelper`.
3. Refactor in place unless the user explicitly asks for architecture cleanup.
4. Keep DB fields and persisted keys stable unless the migration work is part of the task.
5. Verify the build plus the most affected user and admin flow.

### Add or change comment behavior

- Always inspect:
  `main/ArticleDetailActivity.java`,
  `adapter/CommentAdapter.java`,
  `data/DatabaseHelper.java`,
  `model/Comment.java`,
  `res/layout/bottom_sheet_comment.xml`,
  `res/layout/item_comment.xml`,
  and `admin/CommentStatsDetailActivity.java`.
- The current system is not truly threaded:
  replies are saved as normal comments with an `@username` prefix, while nested reply bubbles are view-only for the current adapter session.
- Comment reactions only reload from SQLite if the adapter gets DB and context wiring through `setDatabase(...)`. If you want persistent comment reactions in a screen, wire that explicitly.
- If you add a real reply or thread model, plan a schema change, a migration, model updates, admin query updates, and adapter reconstruction logic.

### Deploy or handoff

- Prefer verified artifacts over assumptions. State which Gradle command ran, whether an APK or AAB was produced, and what manual testing is still pending.
- If release behavior depends on moderation or API configuration, mention that `ContentModerationHelper` is currently prototype-level and should not be treated as production-hard.

## High-Risk Areas

- `DatabaseHelper` mixes auth, articles, reactions, comments, analytics, and admin reporting.
- `main/AddEditArticleActivity` and `admin/AddEditArticleActivity` duplicate important editor and approval behavior.
- Session state is split across `UserPrefs`, `UserSession`, `AdminLoginPrefs`, and `AdminSession`.
- Admin screens do not all inherit from `BaseAdminActivity`, so theme and locale behavior is inconsistent.
- `LoginAdminActivity` uses hardcoded credentials, and user passwords are stored in plaintext.
- `ContentModerationHelper` can approve on API or parsing failure.

## References

- For command checklists, release notes, and safe refactor routing, read [references/workflows.md](references/workflows.md).
- For the current comment architecture, reaction flow, and reply limitations, read [references/comment-system.md](references/comment-system.md).
