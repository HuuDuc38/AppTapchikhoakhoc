# Common Workflows

## Build and run

Use these from the repository root on Windows PowerShell:

- `.\gradlew.bat assembleDebug`
- `.\gradlew.bat installDebug`
- `.\gradlew.bat clean`
- `.\gradlew.bat test`
- `.\gradlew.bat connectedAndroidTest`
- `.\gradlew.bat lint`
- `.\gradlew.bat bundleRelease`
- `.\gradlew.bat tasks --all`

If the environment cannot write to the default Gradle cache, use:

```powershell
$env:GRADLE_USER_HOME = (Join-Path (Get-Location) ".gradle-user")
.\gradlew.bat assembleDebug
```

## Safe feature workflow

1. Identify the exact screen and entry point that owns the behavior.
2. Check whether the same feature also exists in `main/`, `admin/`, or an adapter.
3. Inspect `DatabaseHelper` if the feature reads or writes SQLite data.
4. Make the smallest compatible change that fits the existing activity-and-adapter architecture.
5. Run the narrowest Gradle command that exercises the change.
6. Note manual smoke tests still needed.

## Safe refactor checklist

- Check both article editor flows before changing article creation, approval, or media handling.
- Check both user and admin settings flows before changing theme, locale, or text size behavior.
- Check both user and admin search screens before changing article filtering or search queries.
- Audit raw string comparisons before renaming statuses or categories.
- If the change touches `Article`, verify every `Intent` extra sender and receiver.

## Release and deploy notes

- `assembleDebug` is the fastest verification step for code and resource compilation.
- `installDebug` requires a connected device or emulator.
- `bundleRelease` builds a release artifact but does not replace signing or store-release validation.
- This repo currently has minimal automated tests, so release confidence still depends heavily on manual smoke testing.

## Manual smoke tests worth calling out

- Launch the app into `MainActivity`.
- Open an article from the main list, featured list, and related news if touched.
- Log in or register if auth or comment changes were involved.
- Open the comment bottom sheet, add a comment, and verify count updates.
- Verify article reactions and comment reactions if those flows changed.
- Log in as admin and confirm approval, comment moderation, or settings behavior if any admin file changed.
