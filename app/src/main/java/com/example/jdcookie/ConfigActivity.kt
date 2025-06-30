package com.example.jdcookie

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.jdcookie.databinding.ActivityConfigBinding

class ConfigActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityConfigBinding
    private val prefName = Constants.PREF_CONFIG_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewBinding = ActivityConfigBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // 加载已有参数
        viewBinding.baseUrl.setText(PrefsHelper.get(prefName, this, "baseUrl"))
        viewBinding.secretId.setText(PrefsHelper.get(prefName, this, "secretId"))
        viewBinding.secretKey.setText(PrefsHelper.get(prefName, this, "secretKey"))

        viewBinding.save.setOnClickListener {
            val baseUrl = viewBinding.baseUrl.text.toString().trim()
            val secretId = viewBinding.secretId.text.toString().trim()
            val secretKey = viewBinding.secretKey.text.toString().trim()
            if (!baseUrl.startsWith("http")) {
                Toast.makeText(this, "baseUrl必须以http开头", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (secretId == "") {
                Toast.makeText(this, "secretId不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (secretKey == "") {
                Toast.makeText(this, "secretKey不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            PrefsHelper.apply {
                save(prefName, this@ConfigActivity, "baseUrl", baseUrl)
                save(prefName, this@ConfigActivity, "secretId", secretId)
                save(prefName, this@ConfigActivity, "secretKey", secretKey)
            }
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            finish()
        }

    }
}