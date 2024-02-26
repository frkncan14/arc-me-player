package com.mediatek.wwtv.mediaplayer.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.*;

import android.app.StatusBarManager;

import com.mediatek.wwtv.tvcenter.util.MtkLog;

public class ScreenConstant {
    public static final String TAG = "ScreenConstant";
    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    public static final String DLNA_PROP = "mtk.force_dlna_enable";
    public static final String SMB_PROP = "mtk.force_samba_enable";
	public static final String TK_MMP_CMPB_FLAG = "use.cmpb.in.videoview";

    public static final String ACTION_PREPARE_SHUTDOWN = "android.mtk.intent.action.ACTION_PREPARE_SHUTDOWN";

    public static int getProperty(String proName){
        String line =null;
        Process ifc = null;
        BufferedReader bis = null;
        int result = -1;
        try {
            ifc = Runtime.getRuntime().exec("getprop "+proName);
            bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
            line = bis.readLine();
            MtkLog.w(TAG,"line ="+line);
            bis.close();
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            ifc.destroy();
            if (null != bis) {
              try {
                bis.close();
              } catch (Exception e) {
                e.printStackTrace();
              }

            }
        }

        MtkLog.w(TAG, proName+"line ="+line);
        if(line != null && line.length() > 0){
            try{
                result = Integer.valueOf(line);
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

        return result;
    }

    /**
     * this method is used to set the height of navigaor bar
     *
     * @return
     */
    public static void setSystemUiBarHight(StatusBarManager statusBarManager, int height){
        try{
            Method method = Class.forName("android.app.StatusBarManager").getMethod("setSystemUiBarHight", int.class);
            method.invoke(statusBarManager, height);
        }catch(Exception ex){
            MtkLog.e(TAG, "setSystemUiBarHight:" + ex);
        }
    }

    public String toString() {
        return "ScreenConstant";
    }
}
