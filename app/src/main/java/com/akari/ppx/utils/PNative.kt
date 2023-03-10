package cn.lliiooll.ppbuff.utils

import android.app.Application
import android.content.Context
import android.os.Build
import com.tencent.mmkv.MMKV

object PNative {
    val libList = arrayListOf<String>().apply {
        add("dexkit")
        add("mmkv")
    }

    fun init(app: Context, modulePath: String, abi: String) {
        try {
            val mmkvDir = app.getExternalFilesDir("buffMMKV")
            if (mmkvDir?.isFile!!) mmkvDir?.delete()
            if (mmkvDir?.exists()!!) mmkvDir?.mkdirs()
            MMKV.initialize(app, mmkvDir?.absolutePath) {
                libList.forEach {
                    System.load("${modulePath}!/lib/${abi}/lib${it}.so")
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }
}