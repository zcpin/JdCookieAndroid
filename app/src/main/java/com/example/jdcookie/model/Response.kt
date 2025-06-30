package com.example.jdcookie.model

data class Response<T>(val code: Int, val message: String?, val data: T?)