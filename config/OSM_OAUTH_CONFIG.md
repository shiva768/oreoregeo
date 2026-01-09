# OSM OAuth Configuration

This file documents how to configure OpenStreetMap OAuth 2.0 credentials for the Oreoregeo app.

## Steps to Configure

### 1. Register an OAuth Application on OpenStreetMap

1. Go to https://www.openstreetmap.org/oauth2/applications/new
2. Fill in the form:
   - **Name**: Oreoregeo (or your app name)
   - **Redirect URIs**: `oreoregeo://oauth/callback`
   - **Scopes**: Select only `write_api`
3. Submit the form to create the application
4. Copy the **Client ID** and **Client Secret** that are generated

### 2. Configure Credentials via GitHub Secrets

The OAuth credentials are automatically injected during build from environment variables.

**For GitHub Actions (CI/CD):**

1. Go to your GitHub repository → Settings → Secrets and variables → Actions
2. Add two repository secrets:
   - Name: `OSM_CLIENT_ID`, Value: Your OSM Client ID
   - Name: `OSM_CLIENT_SECRET`, Value: Your OSM Client Secret
3. In your workflow file (`.github/workflows/*.yml`), set the environment variables:
   ```yaml
   - name: Build Debug APK
     env:
       OSM_CLIENT_ID: ${{ secrets.OSM_CLIENT_ID }}
       OSM_CLIENT_SECRET: ${{ secrets.OSM_CLIENT_SECRET }}
     run: ./gradlew assembleDebug
   ```

**For Local Development:**

Set environment variables before building:

```bash
# Linux/macOS
export OSM_CLIENT_ID="your_client_id_here"
export OSM_CLIENT_SECRET="your_client_secret_here"
./gradlew assembleDebug

# Windows (PowerShell)
$env:OSM_CLIENT_ID="your_client_id_here"
$env:OSM_CLIENT_SECRET="your_client_secret_here"
.\gradlew.bat assembleDebug
```

Or add them to your shell profile (`~/.bashrc`, `~/.zshrc`, etc.):
```bash
export OSM_CLIENT_ID="your_client_id_here"
export OSM_CLIENT_SECRET="your_client_secret_here"
```

### 3. How It Works

The OAuth credentials are injected at build time via `BuildConfig`:

In `app/build.gradle.kts`:
```kotlin
defaultConfig {
    buildConfigField("String", "OSM_CLIENT_ID", "\"${System.getenv("OSM_CLIENT_ID") ?: ""}\"")
    buildConfigField("String", "OSM_CLIENT_SECRET", "\"${System.getenv("OSM_CLIENT_SECRET") ?: ""}\"")
}
```

In `OsmOAuthManager.kt`:
```kotlin
private val clientId = BuildConfig.OSM_CLIENT_ID
private val clientSecret = BuildConfig.OSM_CLIENT_SECRET
```

This ensures:
- ✅ Credentials are never committed to source control
- ✅ Different credentials can be used for different environments
- ✅ GitHub Secrets integration works seamlessly in CI/CD

### 4. Testing the OAuth Flow

1. Build and install the app on a device or emulator
2. Navigate to Settings
3. Click "Connect OSM Account"
4. You will be redirected to OpenStreetMap in your browser
5. Log in with your OSM account (if not already logged in)
6. Authorize the application
7. You will be redirected back to the app
8. The app will exchange the authorization code for an access token
9. The token will be saved securely using EncryptedSharedPreferences
10. You should see "Connected to OSM" in the Settings screen

### 5. Verifying the Connection

Once connected:
- The "Add Place" button should appear in the top app bar
- You can create new OSM nodes
- You can edit tags on existing OSM nodes

### 6. Disconnecting

To disconnect your OSM account:
1. Navigate to Settings
2. Click "Disconnect"
3. The access token will be removed from secure storage
4. The "Connect OSM Account" button will reappear

## Troubleshooting

### Error: "Failed to get token: 400"
- Verify that your Client ID and Client Secret are correct
- Verify that the redirect URI in the OSM app registration exactly matches `oreoregeo://oauth/callback`

### Error: "認証に失敗しました" (Authentication failed)
- Check the logs (Logcat) for detailed error messages
- Verify that you have `write_api` scope enabled in your OSM app registration

### OAuth callback doesn't open the app
- Verify that the `OAuthCallbackActivity` is properly registered in `AndroidManifest.xml`
- Verify that the intent filter has the correct scheme, host, and path

### Token not persisting after app restart
- Check that EncryptedSharedPreferences is properly initialized
- Verify that the security-crypto library is included in dependencies
