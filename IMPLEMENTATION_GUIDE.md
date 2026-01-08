# 未実装機能の実装ガイド

このドキュメントでは、外部サービスの構成が必要な未実装機能の実装方法について説明します。

## OSM OAuth 2.0 の実装

### 1. OAuth アプリケーションの登録

1. https://www.openstreetmap.org/oauth2/applications/new にアクセスします。
2. フォームに入力します：
   - Name: Oreoregeo
   - Redirect URIs: `oreoregeo://oauth/callback`
   - Scopes: `write_api` のみを選択
3. Client ID と Client Secret を保存します。

### 2. OAuth 依存関係の追加

`build.gradle.kts` には既に含まれています：
- Android の AccountManager またはカスタムの OAuth 実装を使用します。
- EncryptedSharedPreferences を使用してトークンを安全に保存します。

### 3. OAuth フローの実装

`OsmOAuthManager.kt` を作成します：

```kotlin
class OsmOAuthManager(private val context: Context) {
    private val clientId = "YOUR_CLIENT_ID"
    private val clientSecret = "YOUR_CLIENT_SECRET"
    private val redirectUri = "oreoregeo://oauth/callback"
    
    fun getAuthorizationUrl(): String {
        return "https://www.openstreetmap.org/oauth2/authorize?" +
               "client_id=$clientId&" +
               "redirect_uri=$redirectUri&" +
               "response_type=code&" +
               "scope=write_api"
    }
    
    suspend fun exchangeCodeForToken(code: String): String {
        // トークン交換の実装
        // https://www.openstreetmap.org/oauth2/token に POST
        // code, client_id, client_secret, grant_type=authorization_code を送信
    }
    
    fun saveToken(token: String) {
        // EncryptedSharedPreferences に保存
    }
    
    fun getToken(): String? {
        // EncryptedSharedPreferences から取得
    }
}
```

### 4. SettingsScreen の更新

```kotlin
val osmOAuthManager = remember { OsmOAuthManager(context) }
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    // OAuth コールバックの処理
}

Button(onClick = {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(osmOAuthManager.getAuthorizationUrl()))
    launcher.launch(intent)
}) {
    Text("OSM アカウントを連携")
}
```

### 5. OAuth コールバックの処理

`AndroidManifest.xml` に追加：
```xml
<activity android:name=".OAuthCallbackActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="oreoregeo" android:host="oauth" android:path="/callback" />
    </intent-filter>
</activity>
```

`OAuthCallbackActivity.kt` を作成：
```kotlin
class OAuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent.data?.getQueryParameter("code")
        if (code != null) {
            lifecycleScope.launch {
                val token = OsmOAuthManager(this@OAuthCallbackActivity)
                    .exchangeCodeForToken(code)
                // リポジトリにトークンを設定
                val app = application as OreoregeoApplication
                app.repository.setOsmAccessToken(token)
                finish()
            }
        }
    }
}
```

## Google Drive API の実装

### 1. Google Cloud プロジェクトの設定

1. https://console.cloud.google.com にアクセスします。
2. 新しいプロジェクトを作成するか、既存のプロジェクトを選択します。
3. Google Drive API を有効にします。
4. OAuth 2.0 クレデンシャルを作成します：
   - アプリケーションの種類: Android
   - パッケージ名: com.example.oreoregeo
   - SHA-1 証明書のフィンガープリント (デバッグ用キーストアから取得)

### 2. google-services.json の追加

Google Cloud Console からダウンロードし、`app/` ディレクトリに配置します。

### 3. バックアップフローの実装

`BackupViewModel.kt` を作成：

```kotlin
class BackupViewModel(
    private val driveBackupManager: DriveBackupManager
) : ViewModel() {
    
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()
    
    fun performBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            // サインイン済みのアカウントを取得
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account == null) {
                _backupState.value = BackupState.NeedSignIn
                return@launch
            }
            
            val result = driveBackupManager.backupDatabase(account)
            _backupState.value = result.fold(
                onSuccess = { BackupState.Success },
                onFailure = { BackupState.Error(it.message ?: "Unknown error") }
            )
        }
    }
}
```

### 4. SettingsScreen の更新（Drive 連携）

```kotlin
val signInLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        // サインイン後にバックアップを実行
        backupViewModel.performBackup()
    }
}

Button(onClick = {
    val account = GoogleSignIn.getLastSignedInAccount(context)
    if (account == null) {
        val driveBackupManager = DriveBackupManager(context)
        signInLauncher.launch(driveBackupManager.getSignInIntent())
    } else {
        backupViewModel.performBackup()
    }
}) {
    Icon(Icons.Default.Backup, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text("Google ドライブにバックアップ")
}
```

## エラーハンドリングの実装

### 1. ネットワーク接続チェック

`NetworkUtil.kt` を作成：

```kotlin
object NetworkUtil {
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
```

### 2. ViewModel の更新

API コールの前にネットワークチェックを追加：

```kotlin
fun searchNearby(lat: Double, lon: Double) {
    viewModelScope.launch {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            _searchState.value = SearchState.Error("インターネット接続がありません")
            return@launch
        }
        _searchState.value = SearchState.Loading
        // ... 残りの実装
    }
}
```

### 3. 再試行ロジックの追加

失敗したリクエストに対して指数バックオフを実装：

```kotlin
suspend fun <T> retryWithBackoff(
    times: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
        }
    }
    return block() // 最後の試行
}
```

## テストチェックリスト

OAuth と Drive の設定が完了したら：

- [ ] 実位置での周辺検索テスト
- [ ] チェックイン機能のテスト
- [ ] 30 分以内の重複防止機能の確認
- [ ] OSM ノード作成のテスト
- [ ] OSM ノードのタグ編集テスト
- [ ] バージョン競合ハンドリングのテスト
- [ ] Drive バックアップと復元のテスト
- [ ] オフラインモードのテスト（履歴閲覧のみ）
- [ ] エラーメッセージと再試行のテスト
- [ ] データベースでの WAL モード有効化の確認
- [ ] 低速ネットワーク環境でのテスト
- [ ] Overpass タイムアウト処理のテスト
- [ ] OAuth トークンのリフレッシュ（必要な場合）のテスト

## セキュリティに関する考慮事項

1. **OAuth クレデンシャルを Git にコミットしない**
2. 機密データには `BuildConfig` フィールドを使用する
3. トークンは EncryptedSharedPreferences に保存する
4. OSM API コールの前にすべてのユーザー入力を検証する
5. API コールのレート制限を実装する
6. OSM API の証明書ピンニング（任意）を追加する
7. ログアウト時にトークンを消去する
8. トークンの期限切れを適切に処理する

## パフォーマンスの最適化

1. Overpass の結果を短期間キャッシュする
2. チェックイン履歴のページネーション（順次読み込み）を実装する
3. リアクティブな更新のために Room の Flow を使用する
4. （将来的に写真を追加する場合）画像読み込みを最適化する
5. バックグラウンドバックアップに WorkManager を使用する
6. 適切なローディング状態を実装する
7. 検索画面にプルダウン更新を追加する
8. API コールを減らすためにスポット詳細をキャッシュする
