package com.zelretch.oreoregeo

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class OAuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent?.data?.getQueryParameter("code")
        if (code != null) {
            lifecycleScope.launch {
                // TODO: Exchange code for token when OAuth client is wired.
            }
        }
        finish()
    }
}
