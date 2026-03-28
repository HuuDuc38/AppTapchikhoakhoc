# Firebase setup

Project Firebase hien tai cua app:

- Project ID: `tapchikhoahoc-d465f`
- Android package: `com.example.apptapchikhoakhoc`
- Android app ID: `1:1080672591756:android:009a95c25b6b8c836400a9`

Nhung gi repo da chuan hoa:

- `app/google-services.json` da doi sang project moi.
- `.firebaserc` mac dinh tro den `tapchikhoahoc-d465f`.
- `firestore.indexes.json` da co index cho query comment va admin article.
- `DatabaseHelper` khong con phu thuoc vao Firebase Storage.
- `LoginAdminActivity` co bootstrap admin role cho email nam trong `bootstrap_admin_emails`.

Cach dong bo lai bang Firebase CLI:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\firebase_sync.ps1
```

Neu chay trong moi truong sandbox bi chan thu muc home cua Firebase CLI, co the them:

```powershell
$env:FIREBASE_REPO_CONFIG = "1"
powershell -ExecutionPolicy Bypass -File .\scripts\firebase_sync.ps1
```

Script tren se:

1. Tim Android app theo package `com.example.apptapchikhoakhoc`.
2. Tao app neu project chua co app Android.
3. Tai lai `app/google-services.json`.
4. Tao Firestore database `(default)` tai `asia-southeast1` neu chua co.
5. Deploy `firestore.rules` va `firestore.indexes.json`.

Phan van can bat tay trong Firebase Console:

- Authentication > Sign-in method > bat `Email/Password`.

Bootstrap admin dau tien:

- Sua danh sach email trong `app/src/main/res/values/firebase_bootstrap.xml` neu can.
- Dang ky tai khoan bang mot email nam trong danh sach do.
- Dang nhap o man admin. App se tu merge `role = admin` vao document user neu tai khoan hop le.

Luu y:

- Vi hien tai khong dung Firebase Storage, anh/video bai viet van luu theo local path cua thiet bi.
- Neu sau nay can dong bo media giua nhieu thiet bi, can bat lai Storage va doi `DatabaseHelper` sang upload file len bucket.
