package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class PaymentActivity : ComponentActivity() {
    
    private var orderId: String = ""
    private var paypalUrl: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        paypalUrl = intent.getStringExtra("paypal_url") ?: ""
        orderId = intent.getStringExtra("order_id") ?: ""
        
        if (paypalUrl.isEmpty()) {
            Toast.makeText(this, "Error: No se pudo iniciar PayPal", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PayPalWebView(
                        url = paypalUrl,
                        onDeepLink = { uri ->
                            handleDeepLink(uri)
                        }
                    )
                }
            }
        }
    }
    
    private fun handleDeepLink(uri: Uri) {
        when {
            uri.path?.contains("/success") == true -> {
                val token = uri.getQueryParameter("token") ?: orderId
                Toast.makeText(this, "¡Pago completado!", Toast.LENGTH_SHORT).show()
                
                val resultIntent = Intent().apply {
                    putExtra("order_id", token)
                    putExtra("success", true)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
            uri.path?.contains("/cancel") == true -> {
                Toast.makeText(this, "Pago cancelado", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }
}

@Composable
fun PayPalWebView(
    url: String,
    onDeepLink: (Uri) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            url: String?
                        ): Boolean {
                            url?.let {
                                if (it.startsWith("lepoutinehouse://")) {
                                    onDeepLink(Uri.parse(it))
                                    return true
                                }
                            }
                            return false
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                        }
                    }
                    
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}