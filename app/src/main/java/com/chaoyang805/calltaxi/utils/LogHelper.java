package com.chaoyang805.calltaxi.utils;

import android.util.Log;

/**
 * Created by chaoyang805 on 2015/3/19.
 * 打印日志的帮助类
 */
public class LogHelper {

    public static boolean isShouldUseLog = true;
    private static final String LOG_PREFIX = "blocksms_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGHT = 23;


    public static String makeLogTag(String str){
        if (str.length() > MAX_LOG_TAG_LENGHT - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGHT - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    public static void v(String tag, Object... messages) {
        if (isShouldUseLog) {
            log(tag, Log.VERBOSE, null, messages);
        }
    }

    public static void d(String tag, Object... messages) {
        if (isShouldUseLog) {
            log(tag, Log.DEBUG, null, messages);
        }
    }

    public static void i(String tag, Object... messages) {
        log(tag,Log.INFO,null,messages);
    }

    public static void w(String tag, Object... messages) {
        log(tag,Log.WARN,null,messages);
    }

    public static void e(String tag, Object... messages) {
        log(tag, Log.ERROR, null, messages);
    }


    public static void log(String tag, int level, Throwable t, Object... messages) {
            String message;
            if (t == null && messages != null && messages.length == 1) {
                message = messages[0].toString();
            } else {
                StringBuilder sb = new StringBuilder();
                if (messages != null) for (Object m : messages) {
                    sb.append(m);
                }
                if (t != null) sb.append("\n").append(Log.getStackTraceString(t));
                message = sb.toString();
            }
            Log.println(level, tag, message);
    }

}
