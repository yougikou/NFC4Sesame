package com.dlazaro66.qrcodereaderview;

//import android.util.Log;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SimpleLog {
    // 定义日志标签
    static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "TAG");
    private static boolean loggingEnabled = false;

    public static void setLoggingEnabled(boolean enabled) {
        loggingEnabled = enabled;
    }

    public static void d(String tag, String text) {
        if (loggingEnabled) {
//          Log.d(tag, text);
            HiLog.debug(label,"","");
        }
    }

    public static void w(String tag, String text) {
        if (loggingEnabled) {
//          Log.w(tag, text);
            HiLog.warn(label,"","");
        }
    }

    public static void w(String tag, String text, Throwable e) {
        if (loggingEnabled) {
//          Log.w(tag, text, e);
            HiLog.warn(label,"","");
        }
    }

    public static void e(String tag, String text) {
        if (loggingEnabled) {
//          Log.e(tag, text);
            HiLog.error(label,"","");
        }
    }

    public static void d(String tag, String text, Throwable e) {
        if (loggingEnabled) {
//          Log.d(tag, text, e);
            HiLog.debug(label,"","");
        }
    }

    public static void i(String tag, String text) {
        if (loggingEnabled) {
//          Log.i(tag, text);
            HiLog.info(label,"","");
        }
    }
}
