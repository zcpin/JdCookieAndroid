package com.example.jdcookie

import android.content.Context
import android.util.Log
import com.example.jdcookie.model.EnvInfo
import com.example.jdcookie.model.JdCookie
import com.example.jdcookie.model.Response
import com.example.jdcookie.model.TokenInfo


class QingLong(private val context: Context) {
    private val config: Map<String, *> = PrefsHelper.getAll(Constants.PREF_CONFIG_NAME, context)

    private val baseUrl: String = (config["baseUrl"] as String)
    private val secretId: String = config["secretId"] as String
    private val secretKey: String = config["secretKey"] as String

    // 获取token
    suspend fun getToken(): String {
        // 先从缓存中获取token
        val tokenInfo = PrefsHelper.getAll(Constants.PREF_QL_NAME, context)
        if (tokenInfo["token"] != "" && tokenInfo["expiration"] != null && (tokenInfo["expiration"] as String).toLong() > System.currentTimeMillis() / 1000) {
            return tokenInfo["token"] as String
        }
        val url =
            "$baseUrl${Constants.ApiPaths.GET_TOKEN}?client_id=$secretId&client_secret=$secretKey"
        val result: Response<TokenInfo> = HttpHelper.get(url)

        var token = ""
        if (result.code == 200) {
            // 缓存token
            token = result.data?.token ?: ""
            if (token.isEmpty()) {
                return ""
            }
            PrefsHelper.save(Constants.PREF_QL_NAME, context, "token", token)
            PrefsHelper.save(
                Constants.PREF_QL_NAME,
                context,
                "expiration",
                result.data?.expiration.toString()
            )
            return token
        }
        return token
    }

    // 设置token
    suspend fun setToken(): Map<String, String> {
        val token = getToken()
        val headers = mapOf(Constants.Headers.AUTHORIZATION to "Bearer $token")
        return headers
    }

    // 环境变量列表
    suspend fun getEnv(): List<EnvInfo> {
        val url =
            "$baseUrl${Constants.ApiPaths.GET_ENV}?searchValue=${Constants.ENV_NAME}&t=${System.currentTimeMillis()}"
        val result = HttpHelper.get<Response<List<EnvInfo>>>(url, setToken())
        if (result.code == 200) {
            return result.data ?: emptyList()
        }
        return emptyList()

    }

    // 添加环境变量
    suspend fun addEnv(name: String, value: String, remark: String? = ""): Boolean {
        val url = "$baseUrl${Constants.ApiPaths.ADD_ENV}"
        val body = mapOf("name" to name, "value" to value, "remark" to remark)
        // 将body转换为json字符串
        val bodyStr = HttpHelper.gson.toJson(body)
        val result = HttpHelper.post<Response<EnvInfo>>(url, bodyStr, setToken())
        if (result.code != 200) {
            throw Exception("添加环境失败")
        }
        return true

    }

    // 更新环境变量
    suspend fun updateEnv(id: Int, name: String, value: String): Boolean {
        val url = "$baseUrl${Constants.ApiPaths.UPDATE_ENV}"
        Log.d("QingLong", "url: $url")
        val body = mapOf("id" to id, "name" to name, "value" to value)
        // 将body转换为json字符串
        val bodyStr = HttpHelper.gson.toJson(body)
        Log.d("QingLong", "bodyStr: $bodyStr")
        val result = HttpHelper.put<Response<EnvInfo>>(url, bodyStr, setToken())
        if (result.code != 200) {
            throw Exception("更新环境失败")
        }
        return true
    }

    // 获取环境变量的id
    fun getEnvId(pin: String, envs: List<EnvInfo>): Int {
        val env = envs.find { it.name == Constants.ENV_NAME && it.value.contains(pin) }
        return env?.id ?: -1
    }

    // 推送cookie
    suspend fun pushCookie(cookie: JdCookie): Boolean {
        if (baseUrl == "" || secretId == "" || secretKey == "") {
            throw Exception("请先配置")
        }

        val pin = cookie.ptPin
        val envs = getEnv()
        val id = getEnvId(pin, envs)
        // 将cookie转成k1=v1;k2=v2;格式
        val cookieStr = "pt_key=${cookie.ptKey};pt_pin=$pin;"
        return if (id == -1) {
            addEnv(Constants.ENV_NAME, cookieStr, pin)
        } else {
            updateEnv(id, Constants.ENV_NAME, cookieStr)
        }
    }

}