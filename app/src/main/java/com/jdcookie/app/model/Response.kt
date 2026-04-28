package com.jdcookie.app.model

data class Response<T>(val code: Int, val message: String?, val data: T?)