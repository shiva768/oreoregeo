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

### 2. Update the OAuth Manager with Your Credentials

Open the file:
```
app/src/main/java/com/zelretch/oreoregeo/auth/OsmOAuthManager.kt
```

Replace the placeholder values:
```kotlin
private val clientId = "YOUR_CLIENT_ID" // Replace with your actual Client ID
private val clientSecret = "YOUR_CLIENT_SECRET" // Replace with your actual Client Secret
```

### 3. Security Best Practices

**For Production:**

Instead of hardcoding credentials in the source code, you should:

1. Use BuildConfig fields (recommended for production):

   In `app/build.gradle.kts`, add:
   ```kotlin
   android {
       defaultConfig {
           buildConfigField("String", "OSM_CLIENT_ID", "\"${project.findProperty("OSM_CLIENT_ID")}\"")
           buildConfigField("String", "OSM_CLIENT_SECRET", "\"${project.findProperty("OSM_CLIENT_SECRET")}\"")
       }
   }
   ```

   In `gradle.properties` (add to `.gitignore`):
   ```properties
   OSM_CLIENT_ID=your_client_id_here
   OSM_CLIENT_SECRET=your_client_secret_here
   ```

   Then update `OsmOAuthManager.kt`:
   ```kotlin
   private val clientId = BuildConfig.OSM_CLIENT_ID
   private val clientSecret = BuildConfig.OSM_CLIENT_SECRET
   ```

2. Or use environment variables in CI/CD:
   ```kotlin
   private val clientId = System.getenv("OSM_CLIENT_ID") ?: "YOUR_CLIENT_ID"
   private val clientSecret = System.getenv("OSM_CLIENT_SECRET") ?: "YOUR_CLIENT_SECRET"
   ```

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
