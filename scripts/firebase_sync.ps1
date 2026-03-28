param(
    [string]$ProjectId = "tapchikhoahoc-d465f",
    [string]$PackageName = "com.example.apptapchikhoakhoc",
    [string]$DisplayName = "Tapchikhoahoc",
    [string]$FirestoreLocation = "asia-southeast1",
    [switch]$SkipDeployFirestore
)

$ErrorActionPreference = "Continue"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$firebaseCmd = Join-Path $env:APPDATA "npm\firebase.cmd"
$googleServicesPath = Join-Path $repoRoot "app\google-services.json"

if (-not (Test-Path $firebaseCmd)) {
    throw "Khong tim thay firebase.cmd trong $firebaseCmd"
}

Push-Location $repoRoot
try {
    if ($env:FIREBASE_REPO_CONFIG -eq "1") {
        $env:XDG_CONFIG_HOME = Join-Path $repoRoot ".firebase-user"
        New-Item -ItemType Directory -Force -Path $env:XDG_CONFIG_HOME | Out-Null
    }

    $appsJson = & $firebaseCmd apps:list ANDROID --project $ProjectId --json 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Khong the lay danh sach Android app tu Firebase project $ProjectId"
    }

    $appsResult = ($appsJson | ConvertFrom-Json).result
    $androidApp = $appsResult | Where-Object { $_.packageName -eq $PackageName } | Select-Object -First 1

    if (-not $androidApp) {
        Write-Host "Chua co Android app cho package $PackageName. Dang tao moi..."
        $createJson = & $firebaseCmd apps:create ANDROID $DisplayName --package-name $PackageName --project $ProjectId --json 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Khong tao duoc Android app tren project $ProjectId"
        }
        $androidApp = ($createJson | ConvertFrom-Json).result
    }

    if (Test-Path $googleServicesPath) {
        Remove-Item -LiteralPath $googleServicesPath -Force
    }

    & $firebaseCmd apps:sdkconfig ANDROID $androidApp.appId --project $ProjectId --out $googleServicesPath 2>$null
    if ($LASTEXITCODE -ne 0) {
        throw "Khong tai duoc google-services.json"
    }

    $dbLookup = & $firebaseCmd firestore:databases:get "(default)" --project $ProjectId --json 2>&1
    if ($LASTEXITCODE -ne 0) {
        $dbLookupText = $dbLookup | Out-String
        if ($dbLookupText -match "does not exist" -or $dbLookupText -match "HTTP Error: 404") {
            Write-Host "Chua co Firestore default database. Dang tao moi tai $FirestoreLocation..."
            & $firebaseCmd firestore:databases:create "(default)" --project $ProjectId --location $FirestoreLocation --delete-protection DISABLED 2>$null
            if ($LASTEXITCODE -ne 0) {
                throw "Khong tao duoc Firestore database (default)"
            }
        } else {
            throw "Khong kiem tra duoc Firestore database: $dbLookupText"
        }
    }

    if (-not $SkipDeployFirestore) {
        Write-Host "Dang deploy Firestore rules va indexes..."
        & $firebaseCmd deploy --project $ProjectId --only firestore 2>$null
        if ($LASTEXITCODE -ne 0) {
            throw "Khong deploy duoc Firestore rules/indexes"
        }
    }

    Write-Host "Da dong bo Firebase project $ProjectId thanh cong."
    Write-Host "Neu dang dung Email/Password auth, hay bat provider nay trong Firebase Console neu chua bat."
}
finally {
    Pop-Location
}
