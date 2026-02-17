package com.xiehe.spine

import android.os.Build
import java.util.Calendar

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun currentHour24(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
