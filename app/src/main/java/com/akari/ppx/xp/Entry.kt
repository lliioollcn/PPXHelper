@file:Suppress("DEPRECATION")

package com.akari.ppx.xp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import cn.lliiooll.ppbuff.utils.PNative
import com.akari.ppx.BuildConfig.APPLICATION_ID
import com.akari.ppx.data.Const.TARGET_APP_ID
import com.akari.ppx.data.XPrefs
import com.akari.ppx.data.alive.AliveActivity
import com.akari.ppx.ui.MainActivity
import com.akari.ppx.utils.JavaUtils
import com.akari.ppx.utils.check
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.utils.new
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.mainActivityClass
import com.akari.ppx.xp.Init.safeModeApplicationClass
import com.akari.ppx.xp.hook.BaseHook
import com.akari.ppx.xp.hook.SwitchHook
import dalvik.system.DexFile
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage
import io.luckypray.dexkit.DexKitBridge

class Entry : IXposedHookLoadPackage, IXposedHookZygoteInit {
    private var modulePath: String? = null

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            APPLICATION_ID -> MainActivity.Companion::class.java.name.replaceMethod(
                lpparam.classLoader,
                "isModuleActive",
                Context::class.java
            ) { true }

            TARGET_APP_ID -> {
                cl = lpparam.classLoader

                arrayListOf<BaseHook>().let { hooks ->
                    safeModeApplicationClass?.hookBeforeMethod(
                        "attachBaseContext",
                        Context::class.java
                    ) { param ->
                        with(param.args[0] as Context) {
                            AliveActivity(this)
                            Init(this)
                            packageManager.getApplicationInfo(APPLICATION_ID, 0).run {
                                DexFile(sourceDir).entries()
                            }.asSequence().filter {
                                it.startsWith(BaseHook::class.java.`package`!!.name)
                            }.map {
                                Class.forName(it)
                            }.filter {
                                !it.isInterface && BaseHook::class.java.isAssignableFrom(it) && it != SwitchHook::class.java
                            }.forEach {
                                hooks += it.new() as BaseHook
                            }
                        }
                    }
                    mainActivityClass?.hookBeforeMethod("onCreate", Bundle::class.java) {
                        val ctx = it.thisObject as Activity
                        if (modulePath != null) {
                            PNative.init(ctx, modulePath!!, JavaUtils.getAbiForLibrary())
                            val dexkit =
                                DexKitBridge.create(JavaUtils.getHostPath(ctx))
                            hooks.forEach {
                                it.doFindDex(dexkit)
                                when (it) {
                                    is SwitchHook -> {
                                        XPrefs<Boolean>(it.key).check(true) {
                                            it.onHook()
                                        }
                                    }

                                    else -> it.onHook()
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
        modulePath = startupParam?.modulePath
    }
}