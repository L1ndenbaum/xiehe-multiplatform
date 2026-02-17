package com.xiehe.spine

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun currentHour24(): Int = js("new Date().getHours()") as Int
