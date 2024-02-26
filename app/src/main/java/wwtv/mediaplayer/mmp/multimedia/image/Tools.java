package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.mediatek.twoworlds.tv.SystemProperties;
import com.mstar.android.samba.HttpBean;

public class Tools {
    private static final String TAG = "Tools";

    private static long mTotalMem = 0;
    
    private static int[] mConfigPanelSize = {0, 0};

    public static int[] getPanelSize() {
        if (mConfigPanelSize[0] == 0 || mConfigPanelSize[1] == 0) {
            String sizeString = SystemProperties.get("vendor.display-size", null);
            if (sizeString != null) {
                mConfigPanelSize[0] = Integer.parseInt(sizeString.substring(0, sizeString.indexOf("x")));
                mConfigPanelSize[1] = Integer.parseInt(sizeString.substring(sizeString.indexOf("x") + 1));
                Log.d (TAG, "vendor.display-size w:" + mConfigPanelSize[0] + ", h:"+ mConfigPanelSize[1]);
            }
        }
        return mConfigPanelSize;
    }

    public static boolean checkPath(String path) {
        if (isNetPlayback(path)) {
            return true;
        }
        try{
            File destFile = new File(path);
            if (destFile == null) {
                return false;
            }
            if (destFile.exists()) {
                String destDirCanonicalPath = destFile.getCanonicalPath();
                if (!destDirCanonicalPath.startsWith(path)) {
                    return false;
                }
            } else {
                return false;
            }
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isNetPlayback(String path){
        String ret = null;
        if (path != null && path.length()!=0) {
            ret = path.substring(0,4);
        }
        if (ret != null && ret.length()!=0) {
            if (ret.equals("http") || ret.equals("rtsp")) {
                return true;
            }
        }
        return false;
    }

    public static String convertToHttpUrl(String path) {
        Log.i(TAG,"HttpBean.setmSmbUrl path:"+path);

        HttpBean.setmSmbUrl(path);
        Log.i(TAG,"before convertSambaToHttpUrl's path:"+path);
        String sambaPath = HttpBean.convertSambaToHttpUrl(path);
        Log.i(TAG,"convertSambaToHttpUrl's result samba Path:"+sambaPath);
        sambaPath = sambaPath.substring(22);
        Log.i(TAG,"sambaPath.substring(22)'s result:"+sambaPath);
        sambaPath = Uri.encode(sambaPath);
        Log.i(TAG,"Uri.encode(sambaPath)'s result:"+sambaPath);
        sambaPath = "http://127.0.0.1:8088/"+sambaPath;
        Log.i(TAG,"passed to mediaplayer's samba Path:"+sambaPath);
        return sambaPath;
    }
    
    public static boolean isSambaPlaybackUrl(String path){
        String ret = null;
        if (path != null && path.length()!=0) {
            ret = path.substring(0,3);
        }
        if (ret != null && ret.length()!=0) {
            if (ret.equals("smb")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPhotoStreamlessModeOn() {
        return true;
    }

    public static boolean isFileExist(String path) {
        if (!checkPath(path)) {
            return false;
        }
        return isFileExist(new File(path));
    }

    public static boolean isFileExist(File file) {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public static void stopMediascanner(Context context) {
        Intent intent = new Intent();
        intent.setAction("action_media_scanner_stop");
        context.sendBroadcast(intent);
    }

    public static void startMediascanner(Context context) {
        Intent intent = new Intent();
        intent.setAction("action_media_scanner_start");
        context.sendBroadcast(intent);
    }

    private static long parseMem(String index) {
        String str = null;
        long mTotal = 0;
        FileReader reader = null;
        BufferedReader br = null;
        try {
             reader = new FileReader("/proc/meminfo");
             br = new BufferedReader(reader);
             while ((str = br.readLine()) != null) {
                 if (str.indexOf(index) >= 0) {
                     break;
                 }
             }
             if (str != null) {
                 int begin = str.indexOf(':');
                 int end = str.indexOf('k');
                 str = str.substring(begin + 1, end).trim();
                 mTotal = Integer.parseInt(str);
             }
             br.close();
             reader.close();
        } catch (Exception e) {
             e.printStackTrace();
        } finally {
            str = null;
            try {
                if (br != null) {
                    br.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "parse " + index + " : " + mTotal + " kB");
        return mTotal;
    }
    
    public static long getTotalMem() {
        if(mTotalMem == -1) {
            mTotalMem = parseMem("MemTotal");
        }
        return mTotalMem;
    }
    
    public static boolean isTotalMemLowEnd() {
        // return true if current device is 512MB or lower
        if (getTotalMem() < 512 * 1024) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String parseUri(Uri intent) {
        if (intent != null) {
            return intent.getPath();
        }
        return null;
    }
}
