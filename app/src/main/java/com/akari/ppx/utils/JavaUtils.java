package com.akari.ppx.utils;

import android.content.Context;
import android.os.Build;
import android.os.Process;

import java.util.Arrays;
import java.util.List;

public class JavaUtils {

    public static String getHostPath(Context ctx) {
        return ctx.getClassLoader().getResource("AndroidManifest.xml").getPath().replace("!/AndroidManifest.xml", "").replaceFirst("file:", "");
    }

    public static String getAbiForLibrary() {
        String[] supported = Process.is64Bit() ? Build.SUPPORTED_64_BIT_ABIS : Build.SUPPORTED_32_BIT_ABIS;
        if (supported == null || supported.length == 0) {
            throw new IllegalStateException("No supported ABI in this device");
        }
        List<String> abis = Arrays.asList("armeabi-v7a", "arm64-v8a");
        for (String abi : supported) {
            if (abis.contains(abi)) {
                return abi;
            }
        }
        throw new IllegalStateException("No supported ABI in " + Arrays.toString(supported));
    }
}
