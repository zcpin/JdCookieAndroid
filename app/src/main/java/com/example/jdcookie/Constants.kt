package com.example.jdcookie

object Constants {
    const val PREF_CONFIG_NAME = "AppConfig"
    const val PREF_QL_NAME = "QlToken"
    const val MAIN_URL = "https://m.jd.com"
    const val MY_URL = "https://my.m.jd.com"
    const val LOGIN_URL = "https://plogin.m.jd.com/login/login"
    const val ENV_NAME = "JD_COOKIE"

    object ApiPaths {
        const val GET_TOKEN = "/open/auth/token"
        const val GET_ENV = "/open/envs"
        const val ADD_ENV = "/open/envs"
        const val UPDATE_ENV = "/open/envs"
    }

    object Headers {
        const val AUTHORIZATION = "Authorization"
    }
}