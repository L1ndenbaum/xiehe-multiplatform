package com.xiehe.spine

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun currentHour24(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
