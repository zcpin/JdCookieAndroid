package com.example.jdcookie

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.jdcookie.databinding.ActivityMainBinding
import com.example.jdcookie.model.JdCookie
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
            // 告诉web view自己处理跳转页面
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                // 当url是以MAIN_URL开头，则重定向到MY_URL
                if (url.startsWith(Constants.MAIN_URL)) {
                    view?.loadUrl(targetUrl)
                    return true
                }
                view?.loadUrl(url) // 告诉 WebView 自己加载跳转页面
                return true // 表示“我们已经处理了这个请求”
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 隐藏id=m_common_tip和class=modal的元素
                val hideCss = """
                    javascript:(function(){
                        document.getElementById('m_common_tip').style.display='none';
                        document.getElementsByClassName('modal')[0].style.display='none';
                    })()
                """.trimIndent()
                webView.evaluateJavascript(hideCss, null)
            }
        }
        webView.loadUrl(targetUrl)

        // 设置配置按钮点击事件
        viewBinding.config.setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
            // 隐藏操作按钮
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
                // 延迟1s后跳转到配置页面
                lifecycleScope.launch {
                    delay(500)
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                }
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val cookies = CookieManager.getInstance().getCookie(targetUrl)
                Log.d("MainActivity", "cookie: $cookies")
                val ptKey = getCookieValue(cookies, "pt_key")
                val ptPin = getCookieValue(cookies, "pt_pin")
                if (ptKey == null || ptPin == null) {
                    Toast.makeText(this@MainActivity, "未获取到cookie", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val cookie = JdCookie(ptKey, ptPin)
                try {
                    val result = QingLong(this@MainActivity).pushCookie(cookie)
                    Log.d("MainActivity", "result: $result")
                    if (result) {
                        Toast.makeText(this@MainActivity, "推送成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "推送失败", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this@MainActivity, "推送失败: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
                // 隐藏操作按钮
                hiddenActionBar()

            }
        }

        // 打开后台页面
        viewBinding.open.setOnClickListener {
            // 获取baseUrl
            val baseUrl = PrefsHelper.get(Constants.PREF_CONFIG_NAME, this, "baseUrl")
            if (baseUrl.isBlank()) {
                Toast.makeText(this, "请先配置baseUrl", Toast.LENGTH_SHORT).show()
                // 延迟1s后跳转到配置页面
                lifecycleScope.launch {
                    delay(500)
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                }
                return@setOnClickListener
            }
            // 将baseUrl传递到下个页面
            val intent = Intent(this, BackendActivity::class.java)
            intent.putExtra("baseUrl", baseUrl)
            startActivity(intent)
            // 隐藏操作按钮
            hiddenActionBar()
        }

        // 设置切换账号按钮点击事件
        viewBinding.handoff.setOnClickListener {
            // 删除所有cookie
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
            webView.loadUrl(Constants.LOGIN_URL)
            // 隐藏操作按钮
            hiddenActionBar()
        }
    }

    // 获取cookie值
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
}
