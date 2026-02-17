package com.xiehe.spine

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun currentHour24(): Int = js("new Date().getHours()") as Int
