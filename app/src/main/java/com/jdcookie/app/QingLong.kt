package com.jdcookie.app

import android.content.Context
import com.jdcookie.app.model.EnvInfo
import com.jdcookie.app.model.JdCookie
import com.jdcookie.app.model.Response
import com.jdcookie.app.model.TokenInfo


class QingLong(private val context: Context) {
    private val config: Map<String, *> = PrefsHelper.getAll(Constants.PREF_CONFIG_NAME, context)

    private val baseUrl: String = (config["baseUrl"] as? String ?: "").trimEnd('/')
    private val secretId: String = config["secretId"] as? String ?: ""
    private val secretKey: String = config["secretKey"] as? String ?: ""

    // 获取token
    suspend fun getToken(): String {
        val tokenInfo = PrefsHelper.getAll(Constants.PREF_QL_NAME, context)
        val cachedToken = tokenInfo["token"] as? String
        val cachedExpiration = tokenInfo["expiration"] as? String
        if (!cachedToken.isNullOrBlank()
            && !cachedExpiration.isNullOrBlank()
            && cachedExpiration.toLong() > System.currentTimeMillis() / 1000
        ) {
            LogHelper.info(context, "QingLong", "使用缓存Token，过期时间: $cachedExpiration")
            return cachedToken
        }

        if (baseUrl.isBlank()) {
            LogHelper.error(context, "QingLong", "baseUrl为空，请先到配置页面填写")
            throw Exception("baseUrl为空，请先到配置页面填写")
        }

        val url =
            "$baseUrl${Constants.ApiPaths.GET_TOKEN}?client_id=$secretId&client_secret=$secretKey"
        LogHelper.info(context, "QingLong", "请求Token: $url")
        val result: Response<TokenInfo> = HttpHelper.get(url)

        if (result.code == 200) {
            val token = result.data?.token ?: ""
            if (token.isEmpty()) {
                LogHelper.error(context, "QingLong", "获取Token成功但返回为空")
                return ""
            }
            PrefsHelper.save(Constants.PREF_QL_NAME, context, "token", token)
            PrefsHelper.save(
                Constants.PREF_QL_NAME,
                context,
                "expiration",
                result.data?.expiration.toString()
            )
            LogHelper.info(context, "QingLong", "获取Token成功，过期时间: ${result.data?.expiration}")
            return token
        }
        LogHelper.error(context, "QingLong", "获取Token失败，code: ${result.code}, msg: ${result.message}")
        return ""
    }

    // 环境变量列表
    suspend fun getEnv(): List<EnvInfo> {
        val url =
            "$baseUrl${Constants.ApiPaths.GET_ENV}?searchValue=${Constants.ENV_NAME}&t=${System.currentTimeMillis()}"
        LogHelper.info(context, "QingLong", "查询环境变量: $url")
        val token = getToken()
        val headers = mapOf(Constants.Headers.AUTHORIZATION to "Bearer $token")
        val result = HttpHelper.get<Response<List<EnvInfo>>>(url, headers)
        if (result.code == 200) {
            val envs = result.data ?: emptyList()
            LogHelper.info(context, "QingLong", "查询到 ${envs.size} 条环境变量")
            return envs
        }
        LogHelper.error(context, "QingLong", "查询环境变量失败，code: ${result.code}, msg: ${result.message}")
        return emptyList()
    }

    // 添加环境变量
    suspend fun addEnv(name: String, value: String, remark: String? = ""): Boolean {
        val url = "$baseUrl${Constants.ApiPaths.ADD_ENV}"
        val body = mapOf("name" to name, "value" to value, "remark" to remark)
        val bodyStr = HttpHelper.gson.toJson(body)
        LogHelper.info(context, "QingLong", "添加环境变量: $remark")
        val token = getToken()
        val headers = mapOf(Constants.Headers.AUTHORIZATION to "Bearer $token")
        val result = HttpHelper.post<Response<EnvInfo>>(url, bodyStr, headers)
        if (result.code != 200) {
            LogHelper.error(context, "QingLong", "添加环境变量失败，code: ${result.code}, msg: ${result.message}")
            throw Exception("添加环境失败: ${result.message}")
        }
        LogHelper.info(context, "QingLong", "添加环境变量成功")
        return true
    }

    // 更新环境变量
    suspend fun updateEnv(id: Int, name: String, value: String): Boolean {
        val url = "$baseUrl${Constants.ApiPaths.UPDATE_ENV}"
        val body = mapOf("id" to id, "name" to name, "value" to value)
        val bodyStr = HttpHelper.gson.toJson(body)
        LogHelper.info(context, "QingLong", "更新环境变量: id=$id")
        val token = getToken()
        val headers = mapOf(Constants.Headers.AUTHORIZATION to "Bearer $token")
        val result = HttpHelper.put<Response<EnvInfo>>(url, bodyStr, headers)
        if (result.code != 200) {
            LogHelper.error(context, "QingLong", "更新环境变量失败，code: ${result.code}, msg: ${result.message}")
            throw Exception("更新环境失败: ${result.message}")
        }
        LogHelper.info(context, "QingLong", "更新环境变量成功")
        return true
    }

    // 获取环境变量的id
    fun getEnvId(pin: String, envs: List<EnvInfo>): Int {
        val env = envs.find { it.name == Constants.ENV_NAME && it.value.contains(pin) }
        return env?.id ?: -1
    }

    // 推送cookie
    suspend fun pushCookie(cookie: JdCookie): Boolean {
        if (baseUrl.isBlank() || secretId.isBlank() || secretKey.isBlank()) {
            throw Exception("请先配置")
        }

        val pin = cookie.ptPin
        LogHelper.info(context, "QingLong", "开始推送Cookie: pt_pin=$pin")
        val envs = getEnv()
        val id = getEnvId(pin, envs)
        val cookieStr = "pt_key=${cookie.ptKey};pt_pin=$pin;"
        return if (id == -1) {
            LogHelper.info(context, "QingLong", "未找到已有环境变量，新增")
            addEnv(Constants.ENV_NAME, cookieStr, pin)
        } else {
            LogHelper.info(context, "QingLong", "找到已有环境变量 id=$id，更新")
            updateEnv(id, Constants.ENV_NAME, cookieStr)
        }
    }

}
