package com.example.jdcookie.model

data class EnvInfo(
    val id: Int,
    val value: String,
    val timestamp: String,
    val status: Int,
    val position: Long,
    val name: String,
    val remark: String,
    val createdAt: String,
    val updatedAt: String
)