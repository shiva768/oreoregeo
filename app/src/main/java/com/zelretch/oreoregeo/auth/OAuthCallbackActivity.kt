package com.zelretch.oreoregeo.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.zelretch.oreoregeo.OreoregeoApplication
import com.zelretch.oreoregeo.R
import kotlinx.coroutines.launch
import timber.log.Timber

class OAuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("OAuthCallbackActivity started")

        val code = intent.data?.getQueryParameter("code")
        val error = intent.data?.getQueryParameter("error")

        if (error != null) {
            Timber.e("OAuth error: $error")
            android.widget.Toast.makeText(
                this,
                getString(R.string.osm_auth_error, error),
                android.widget.Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        if (code != null) {
            Timber.d("Received authorization code, exchanging for token")
            val osmOAuthManager = OsmOAuthManager(this)

            lifecycleScope.launch {
                val result = osmOAuthManager.exchangeCodeForToken(code)

                result.fold(
                    onSuccess = { token ->
                        Timber.i("Successfully obtained token, saving to storage")
                        osmOAuthManager.saveToken(token)

                        // Update repository with the new token
                        val app = application as OreoregeoApplication
                        app.repository.setOsmAccessToken(token)

                        android.widget.Toast.makeText(
                            this@OAuthCallbackActivity,
                            getString(R.string.osm_auth_success),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = { exception ->
                        Timber.e(exception, "Failed to exchange code for token")
                        android.widget.Toast.makeText(
                            this@OAuthCallbackActivity,
                            getString(R.string.osm_token_exchange_failed),
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                )

                finish()
            }
        } else {
            Timber.w("No authorization code received")
            android.widget.Toast.makeText(
                this,
                getString(R.string.osm_no_auth_code),
                android.widget.Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }
}
