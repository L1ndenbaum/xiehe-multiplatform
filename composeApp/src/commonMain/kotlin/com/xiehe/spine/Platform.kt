package com.xiehe.spine

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform