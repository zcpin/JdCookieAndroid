package com.jdcookie.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jdcookie.app.databinding.ActivityMainBinding
import com.jdcookie.app.model.JdCookie
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var webView: WebView
    private val targetUrl = Constants.MY_URL
    private var isExpanded = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        webView = viewBinding.webView

        webView.settings.javaScriptEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        webView.webChromeClient = android.webkit.WebChromeClient()
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                if (url.startsWith(Constants.MAIN_URL)) {
                    view?.loadUrl(targetUrl)
                    return true
                }
                view?.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                val hideCss = """
                    javascript:(function(){
                        document.getElementById('m_common_tip').style.display='none';
                        document.getElementsByClassName('modal')[0].style.display='none';
                    })()
                """.trimIndent()
                webView.evaluateJavascript(hideCss, null)
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                LogHelper.error(this@MainActivity, "WebView", "加载失败: $description (code=$errorCode)")
                Toast.makeText(this@MainActivity, "页面加载失败，请检查网络", Toast.LENGTH_SHORT).show()
            }

            @Deprecated("Deprecated in Java")
            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                val statusCode = errorResponse?.statusCode ?: -1
                LogHelper.error(this@MainActivity, "WebView", "HTTP错误: $statusCode ${errorResponse?.reasonPhrase}")
            }
        }
        webView.loadUrl(targetUrl)

        // 设置配置按钮点击事件
        viewBinding.config.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
            hiddenActionBar()
        }

        // 日志面板
        viewBinding.logs.setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
            hiddenActionBar()
        }

        // 悬浮按钮点击事件
        viewBinding.floatingActionButton.setOnClickListener {
            isExpanded = !isExpanded
            viewBinding.actionButtons.visibility =
                if (isExpanded) View.VISIBLE else View.GONE
        }

        // 设置推送按钮点击事件
        viewBinding.push.setOnClickListener {
            val baseUrl = PrefsHelper.get(Constants.PREF_CONFIG_NAME, this, "baseUrl")
            val secretId = PrefsHelper.get(Constants.PREF_CONFIG_NAME, this, "secretId")
            val secretKey = PrefsHelper.get(Constants.PREF_CONFIG_NAME, this, "secretKey")
            if (baseUrl.isBlank() || secretId.isBlank() || secretKey.isBlank()) {
                Toast.makeText(this, "请先配置baseUrl、secretId、secretKey", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    delay(500)
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                }
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val cookies = CookieManager.getInstance().getCookie(targetUrl)
                val ptKey = getCookieValue(cookies, "pt_key")
                val ptPin = getCookieValue(cookies, "pt_pin")
                if (ptKey == null || ptPin == null) {
                    LogHelper.warn(this@MainActivity, "MainActivity", "未获取到Cookie，请先登录")
                    Toast.makeText(this@MainActivity, "未获取到cookie", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val cookie = JdCookie(ptKey, ptPin)
                LogHelper.info(this@MainActivity, "MainActivity", "开始推送Cookie: pt_pin=$ptPin")
                try {
                    val result = QingLong(this@MainActivity).pushCookie(cookie)
                    if (result) {
                        LogHelper.info(this@MainActivity, "MainActivity", "推送成功")
                        Toast.makeText(this@MainActivity, "推送成功", Toast.LENGTH_SHORT).show()
                    } else {
                        LogHelper.error(this@MainActivity, "MainActivity", "推送返回失败")
                        Toast.makeText(this@MainActivity, "推送失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    LogHelper.error(this@MainActivity, "MainActivity", "推送异常: ${e.message}")
                    Toast.makeText(this@MainActivity, "推送失败: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
                hiddenActionBar()
            }
        }

        // 打开后台页面
        viewBinding.open.setOnClickListener {
            val baseUrl = PrefsHelper.get(Constants.PREF_CONFIG_NAME, this, "baseUrl")
            if (baseUrl.isBlank()) {
                Toast.makeText(this, "请先配置baseUrl", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    delay(500)
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                }
                return@setOnClickListener
            }
            val intent = Intent(this, BackendActivity::class.java)
            intent.putExtra("baseUrl", baseUrl)
            startActivity(intent)
            hiddenActionBar()
        }

        // 设置切换账号按钮点击事件
        viewBinding.handoff.setOnClickListener {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            LogHelper.info(this, "MainActivity", "已清除Cookie，跳转登录页")
            webView.loadUrl(Constants.LOGIN_URL)
            hiddenActionBar()
        }
    }

    private fun getCookieValue(cookie: String, name: String): String? {
        if (cookie.isEmpty()) return null
        return cookie.split(";").map { it.trim() }
            .firstOrNull { it.startsWith("$name=") }
            ?.substringAfter("=")
    }

    private fun hiddenActionBar() {
        viewBinding.actionButtons.visibility = View.GONE
        isExpanded = false
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
