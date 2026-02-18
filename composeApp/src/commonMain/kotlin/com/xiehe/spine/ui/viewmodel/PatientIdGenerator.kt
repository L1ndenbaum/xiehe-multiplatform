package com.xiehe.spine.ui.viewmodel

internal fun generatedPatientId(): String {
    return "P${kotlin.random.Random.nextInt(10000000, 99999999)}"
}
