package com.alzzz.idlefishhook.utils;

import android.util.Log;

/**
 * @Description LOGGER
 * @Date 2019-06-22
 * @Author sz
 */
public class LOGGER {
    private static final String TAG = "xPosedAlzzz";

    /**
     * Log.d
     * @param tag
     * @param content
     */
    public static void d(String tag, String content){
        Log.d(tag, content);
    }

    /**
     * log.d
     *
     * @param content
     */
    public static void d(String content){
        Log.d(TAG, content);
    }


    /**
     * log.d
     *
     * @param content
     */
    public static void e(String content){
        Log.e(TAG, content);
    }

    /**
     * log.d
     *
     * @param content
     */
    public static void e(String tag, String content){
        Log.e(tag, content);
    }
}
