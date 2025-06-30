package com.example.jdcookie

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.jdcookie.databinding.ActivityBackendBinding

class BackendActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityBackendBinding
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityBackendBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // 获取baseUrl
        val baseUrl = intent.getStringExtra("baseUrl")
        if (baseUrl == null) {
            finish()
            return
        }

        webView = viewBinding.webView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = object: WebViewClient(){
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                if (view != null) {
                    view.loadUrl(url)
                    return true // 表示我们处理了
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.loadUrl(baseUrl)
    }
}