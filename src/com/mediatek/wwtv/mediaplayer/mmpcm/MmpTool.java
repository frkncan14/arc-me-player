package com.mediatek.wwtv.mediaplayer.mmpcm;

import android.util.Log;

/*
 * mmp log
 */
public class MmpTool {
    private static final String TAG = "MMP";

    public static void logError(String s) {
        if(Log.isLoggable(TAG, Log.ERROR)){
            StackTraceElement[] elog = new Exception().getStackTrace();
            Log.e(TAG,
                    "Class:" + elog[1].getClassName() +
                    "." + elog[1].getMethodName() +
                    " (Line:" + new Integer(elog[1].getLineNumber()) + ") :" + s);

        }
    }

    public static void logInfo(String s) {
        //if(Log.isLoggable(TAG, Log.INFO)){
            StackTraceElement[] elog = new Exception().getStackTrace();
            Log.i(TAG,
                    "Class:" + elog[1].getClassName() +
                    "." + elog[1].getMethodName() +
                    " (Line:" + new Integer(elog[1].getLineNumber()) + ") :" + s);

       // }
    }

    public static void logDbg(String s) {
        if(Log.isLoggable(TAG, Log.DEBUG)){
            StackTraceElement[] elog = new Exception().getStackTrace();
            Log.d(TAG,
                    "Class:" + elog[1].getClassName() +
                    "." + elog[1].getMethodName() +
                    " (Line:" + new Integer(elog[1].getLineNumber()) + ") :" + s);

        }
    }

    public static void logWarn(String s) {
        if(Log.isLoggable(TAG, Log.WARN)){
            StackTraceElement[] elog = new Exception().getStackTrace();
            Log.w(TAG,
                    "Class:" + elog[1].getClassName() +
                    "." + elog[1].getMethodName() +
                    " (Line:" + new Integer(elog[1].getLineNumber()) + ") :" + s);

        }
    }

    public String toString() {
       return "MmpTool";
    }
}
