package com.zelretch.oreoregeo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.zelretch.oreoregeo.data.remote.OsmOAuthManager
import kotlinx.coroutines.launch

class OAuthCallbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val code = intent?.data?.getQueryParameter("code")
        if (code != null) {
            lifecycleScope.launch {
                val manager = OsmOAuthManager(this@OAuthCallbackActivity)
                val result = manager.exchangeCode(code)
                result.onSuccess { token ->
                    (application as OreoregeoApplication).repository.setOsmAccessToken(token)
                }
            }
        }
        finish()
    }
}
