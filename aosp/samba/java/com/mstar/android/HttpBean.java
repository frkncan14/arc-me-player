package com.mstar.android.samba;

import android.util.Log;

public class HttpBean {

    private static final String TAG = "HttpBean";

    private  static String mPassword;
    private  static String mName;
    private  static String mSmbUrl;
    private  static String mIpaddress;


    public static String getmSmbUrl() {
        return mSmbUrl;
    }

    public static void setmSmbUrl(String SmbUrl) {
        HttpBean.mSmbUrl = SmbUrl;
    }

    public static String getmPassword() {
        return mPassword;
    }

    public static void setmPassword(String Password) {
        mPassword = Password;
    }

    public static String getmName() {
        return mName;
    }

    public static void setmName(String Name) {
        mName = Name;
    }

    public static void setmIpAddress(String ipaddress) {
        mIpaddress = ipaddress;
    }

    public static String getmIpAddress() {
        return mIpaddress;
    }

    public static String convertSambaToHttpUrl(String smbPath) {
        if (smbPath == null) {
            return null;
        }
        String t = "smb://";
        smbPath = smbPath.substring(t.length(),smbPath.length());
        String fullString = smbPath;
        int dot = fullString.indexOf( '/' );
        String tmp;
        if (dot > 0) {
            String oriIp = smbPath.substring(0,dot);
            Log.d(TAG,"oriIp = " + oriIp);
            String localIp = "127.0.0.1";
            Log.d(TAG,"smbPath = " + smbPath);
            tmp = fullString.substring(dot,fullString.length());
            Log.d(TAG,"tmp = " + tmp);
            smbPath = localIp + ":8088" + tmp;
        }
        String httpPath = "http://" + smbPath;
        return httpPath;
    }
}
