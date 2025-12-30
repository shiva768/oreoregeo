# Implementation Guide for Remaining Features

This document provides guidance for implementing the remaining features that require external service configuration.

## OSM OAuth 2.0 Implementation

### 1. Register OAuth Application

1. Go to https://www.openstreetmap.org/oauth2/applications/new
2. Fill in the form:
   - Name: Oreoregeo
   - Redirect URIs: `oreoregeo://oauth/callback`
   - Scopes: Select only `write_api`
3. Save the Client ID and Client Secret

### 2. Add OAuth Dependencies

Already included in build.gradle.kts:
- Use Android AccountManager or custom OAuth implementation
- Store tokens securely using EncryptedSharedPreferences

### 3. Implement OAuth Flow

Create `OsmOAuthManager.kt`:

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
        // Implement token exchange
        // POST to https://www.openstreetmap.org/oauth2/token
        // with code, client_id, client_secret, grant_type=authorization_code
    }
    
    fun saveToken(token: String) {
        // Save to EncryptedSharedPreferences
    }
    
    fun getToken(): String? {
        // Retrieve from EncryptedSharedPreferences
    }
}
```

### 4. Update SettingsScreen

```kotlin
val osmOAuthManager = remember { OsmOAuthManager(context) }
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    // Handle OAuth callback
}

Button(onClick = {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(osmOAuthManager.getAuthorizationUrl()))
    launcher.launch(intent)
}) {
    Text("Connect OSM Account")
}
```

### 5. Handle OAuth Callback

Add to AndroidManifest.xml:
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

Create `OAuthCallbackActivity.kt`:
```kotlin
class OAuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent.data?.getQueryParameter("code")
        if (code != null) {
            lifecycleScope.launch {
                val token = OsmOAuthManager(this@OAuthCallbackActivity)
                    .exchangeCodeForToken(code)
                // Update repository with token
                val app = application as OreoregeoApplication
                app.repository.setOsmAccessToken(token)
                finish()
            }
        }
    }
}
```

## Google Drive API Implementation

### 1. Setup Google Cloud Project

1. Go to https://console.cloud.google.com
2. Create a new project or select existing
3. Enable Google Drive API
4. Create OAuth 2.0 credentials:
   - Application type: Android
   - Package name: com.example.oreoregeo
   - SHA-1 certificate fingerprint (get from debug keystore)

### 2. Add google-services.json

Download from Google Cloud Console and place in `app/` directory.

### 3. Implement Backup Flow

Create `BackupViewModel.kt`:

```kotlin
class BackupViewModel(
    private val driveBackupManager: DriveBackupManager
) : ViewModel() {
    
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState = _backupState.asStateFlow()
    
    fun performBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            // Get signed in account
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

### 4. Update SettingsScreen with Drive Integration

```kotlin
val signInLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        // Perform backup after sign in
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
    Text("Backup to Google Drive")
}
```

## Error Handling Implementation

### 1. Network Connectivity Check

Create `NetworkUtil.kt`:

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

### 2. Update ViewModels

Add network checks before API calls:

```kotlin
fun searchNearby(lat: Double, lon: Double) {
    viewModelScope.launch {
        if (!NetworkUtil.isNetworkAvailable(context)) {
            _searchState.value = SearchState.Error("No internet connection")
            return@launch
        }
        _searchState.value = SearchState.Loading
        // ... rest of implementation
    }
}
```

### 3. Add Retry Logic

Implement exponential backoff for failed requests:

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
    return block() // last attempt
}
```

## Testing Checklist

Once OAuth and Drive are configured:

- [ ] Test nearby search with real location
- [ ] Test check-in functionality
- [ ] Verify 30-minute duplicate prevention works
- [ ] Test OSM node creation
- [ ] Test OSM node tag editing
- [ ] Test version conflict handling
- [ ] Test Drive backup and restore
- [ ] Test offline mode (history view only)
- [ ] Test error messages and retry
- [ ] Verify WAL mode is active in database
- [ ] Test with poor network conditions
- [ ] Verify Overpass timeout handling
- [ ] Test OAuth token refresh if needed

## Security Considerations

1. **Never commit OAuth credentials** to git
2. Use `BuildConfig` fields for sensitive data
3. Store tokens in EncryptedSharedPreferences
4. Validate all user input before OSM API calls
5. Implement rate limiting for API calls
6. Add certificate pinning for OSM API (optional)
7. Clear tokens on logout
8. Handle token expiration gracefully

## Performance Optimizations

1. Cache Overpass results for short duration
2. Implement pagination for check-in history
3. Use Room's Flow for reactive updates
4. Optimize image loading if adding photos later
5. Use WorkManager for background backups
6. Implement proper loading states
7. Add pull-to-refresh on search screen
8. Cache place details to reduce API calls
