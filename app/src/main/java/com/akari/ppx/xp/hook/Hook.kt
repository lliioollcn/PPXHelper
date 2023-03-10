package com.akari.ppx.xp.hook

import io.luckypray.dexkit.DexKitBridge

interface BaseHook {
    fun onHook() = Unit

    fun doFindDex(dex: DexKitBridge?) = Unit
}

open class SwitchHook(val key: String) : BaseHook