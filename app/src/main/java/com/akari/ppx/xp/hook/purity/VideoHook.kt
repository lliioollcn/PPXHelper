@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import io.luckypray.dexkit.DexKitBridge
import java.net.URL

class VideoHook : SwitchHook("save_video") {

    var SUP_VIDEO_DOWNLOADHELPER: String? = null
    override fun onHook() {
        if (SUP_VIDEO_DOWNLOADHELPER != null) {
            //"com.sup.android.video.VideoDownloadHelper".findClass(cl).apply {
            "$SUP_VIDEO_DOWNLOADHELPER".findClass(cl).apply {
                for (m in declaredMethods) {
                    if (
                        m.parameterTypes.size == 7
                        && m.parameterTypes[0] == Context::class.java
                        && m.parameterTypes[5] == Boolean::class.java
                    ) {
                        XposedBridge.hookMethod(m, object : XC_MethodReplacement() {
                            override fun replaceHookedMethod(p: MethodHookParam?): Any {
                                val param = p!!
                                CoroutineScope(Dispatchers.Main).launch {
                                    val videoModel = param.args[1]
                                    var resp: String



                                    /*
                                    String(
                                    "com.bytedance.apm.net.DefaultHttpServiceImpl".findClass(cl)
                                        .new()
                                        .callMethod("doGet", this, null)
                                        ?.callMethodAs<ByteArray>("b")!!
                                        )

                                     */



                                    withContext(Dispatchers.IO) {

                                        resp =
                                            "https://is.snssdk.com/bds/cell/detail/?cell_type=1&aid=1319&app_name=super&cell_id=${
                                                param.args[2].getLongField("a")
                                            }".apply { Log.i("PPXHelper", this) }.get()

                                        videoModel.callMethodAs<ArrayList<*>>("getUrlList")
                                            .forEach {
                                                it.callMethod(
                                                    "setUrl",
                                                    resp.fromJsonElement().asJsonObject
                                                        //["data"]["data"]["item"]["origin_video_download"]["url_list"][0]["url"]
                                                        .getAsJsonObject("data")
                                                        .getAsJsonObject("data")
                                                        .getAsJsonObject("item")
                                                        .getAsJsonObject("origin_video_download")
                                                        .getAsJsonArray("url_list")
                                                        .get(0).asJsonObject
                                                        .get("url").asString
                                                )
                                            }
                                        /*
                                        uri.check("") {
                                            uri =
                                                "https://h5.pipix.com/bds/webapi/item/detail/?item_id=${
                                                    param.args[2].callMethodAs<Long>("getItemId")
                                                }".apply { Log.i("PPXHelper", this) }.get()
                                                    .fromJsonElement()["data"]["item"]["origin_video_id"].asString
                                        }
                                        val ts = System.currentTimeMillis()
                                        resp = "https://i.snssdk.com/video/play/1/bds/$ts/${
                                            "com.bytedance.common.utility.DigestUtils".findClass(cl)
                                                .callStaticMethod(
                                                    "md5Hex",
                                                    "ts${ts}userbdsversion1video${uri}vtypemp4f425df23905d4ee38685e276072faa0c"
                                                )
                                        }/mp4/$uri".apply { Log.i("PPXHelper", this) }.get()

                                         */
                                    }
                                    /*
                                                                        fun JsonElement.getMaxQualityVideo(i: Int = 5): String =
                                                                            runCatching {
                                                                                this["video_$i"]["main_url"].asString
                                                                            }.getOrElse { getMaxQualityVideo(i - 1) }

                                                                        String(
                                                                            Base64.decode(
                                                                                resp.fromJsonElement()["video_info"]["data"]["video_list"].getMaxQualityVideo(),
                                                                                0
                                                                            )
                                                                        ).let { url ->
                                                                            videoModel.callMethodAs<ArrayList<*>>("getUrlList")
                                                                                .forEach {
                                                                                    it.callMethod("setUrl", url)
                                                                                }
                                                                        }

                                     */
                                    param.invokeOriginalMethod()
                                }
                                return Unit
                            }

                        })
                    }
                    if (m.parameterTypes.isEmpty() && m.returnType == Boolean::class.java) {
                        XposedBridge.hookMethod(m, object : XC_MethodReplacement() {
                            override fun replaceHookedMethod(param: MethodHookParam?): Any {
                                return false
                            }

                        })
                    }
                }
                /*
                replaceMethod(
                    "doDownload",
                    Context::class.java,
                    "com.sup.android.base.model.VideoModel",
                    "com.sup.android.video.VideoDownLoadConfig",
                    "com.ss.android.socialbase.downloader.depend.IDownloadListener",
                    Boolean::class.java,
                    "kotlin.jvm.functions.Function1"
                ) { param ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val videoModel = param.args[1]
                        var resp: String

                        fun String.get() = String(
                            "com.bytedance.apm.net.DefaultHttpServiceImpl".findClass(cl).new()
                                .callMethod("doGet", this, null)?.callMethodAs<ByteArray>("b")!!
                        )

                        withContext(Dispatchers.IO) {
                            var uri = videoModel.callMethodAs<String>("getUri")
                            uri.check("") {
                                uri = "https://h5.pipix.com/bds/webapi/item/detail/?item_id=${
                                    param.args[2].callMethodAs<Long>("getItemId")
                                }".get()
                                    .fromJsonElement()["data"]["item"]["origin_video_id"].asString
                            }
                            val ts = System.currentTimeMillis()
                            resp = "https://i.snssdk.com/video/play/1/bds/$ts/${
                                "com.bytedance.common.utility.DigestUtils".findClass(cl)
                                    .callStaticMethod(
                                        "md5Hex",
                                        "ts${ts}userbdsversion1video${uri}vtypemp4f425df23905d4ee38685e276072faa0c"
                                    )
                            }/mp4/$uri".get()
                        }

                        fun JsonElement.getMaxQualityVideo(i: Int = 5): String =
                            runCatching {
                                this["video_$i"]["main_url"].asString
                            }.getOrElse { getMaxQualityVideo(i - 1) }

                        String(
                            Base64.decode(
                                resp.fromJsonElement()["video_info"]["data"]["video_list"].getMaxQualityVideo(),
                                0
                            )
                        ).let { url ->
                            videoModel.callMethodAs<ArrayList<*>>("getUrlList").forEach {
                                it.callMethod("setUrl", url)
                            }
                        }
                        param.invokeOriginalMethod()
                    }
                }


                replaceMethod("isEnableDownloadGodVideo") { false }
                 */

            }


        }
    }

    override fun doFindDex(dex: DexKitBridge?) {
        val result = dex?.batchFindClassesUsingStrings {
            queryMap = hashMapOf<String, Set<String>>().apply {
                put("com.sup.android.video.VideoDownloadHelper", hashSetOf<String>().apply {
                    add("videoUrl[0]")
                    add("videoUrl[1]")
                    add("iterator.next()")
                    add("Uri.parse(url)")
                })
            }
        }
        if (!result?.isEmpty()!!) {
            SUP_VIDEO_DOWNLOADHELPER =
                result.get("com.sup.android.video.VideoDownloadHelper")?.get(0)?.name
        }
    }
}

fun String.get():String{
    val url = URL(this)
    val ins = url.openConnection().getInputStream()
    return String(JavaUtils.readStream(ins))
}