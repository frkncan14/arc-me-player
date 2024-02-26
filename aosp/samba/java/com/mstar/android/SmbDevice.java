
package com.mstar.android.samba;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class SmbDevice implements Parcelable {

    public SmbDevice() {
    }

    public SmbDevice(String ipaddress) {
    }

    public SmbDevice(Parcel in) {
    }

    public void setStorageManager(SambaStorageManager mstoragemanger) {
    }

    public String getAddress() {
      return "";
    }

    public String getHostName() {
      return "";
    }

    public void setHostName(String name) {
    }

    public ArrayList<SmbShareFolder> getSharefolderList() {
      return null;
    }

    public  ArrayList<SmbShareFolder> enterDirectory(String DirectoryName) {
      return null;
    }


    public  ArrayList<SmbShareFolder>  enterParent() {
      return null;
    }

    public void SetPath(String url) {
    }

    public void SetParent(String url) {
    }

    public String GetCurrentPath() {
      return "";
    }

    public String GetParentPath(){
      return "";
    }

    public void setAuth(SmbAuthentication auth) {
    }

    public SmbAuthentication getAuth() {
      return null;
    }

    public boolean hasPassword() {
        return true;
    }

    public void mount(SmbAuthentication smbAuthentication) {
    }

    public int mount(SmbAuthentication smbAuthentication, int flags) {
      return 0;
    }

    public int unmount() {
      return 0;
    }

    public boolean isMounted() {
      return false;
    }

    private boolean isChinese(String str) {
      return false;
    }

    public String localPath() {
      return "";
    }

    public String remotePath() {
      return "";
    }

    public void setOnRecvMsg(OnRecvMsg onRecvMsg) {
    }

    public boolean isActive() {
        return false;
    }

    public boolean testPassword(String user, String pw) {
        return false;
    }

    public void setAddress(String ip) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<SmbDevice> CREATOR = new Parcelable.Creator<SmbDevice>() {
        public SmbDevice createFromParcel(Parcel in) {
            return new SmbDevice(in);
        }

        public SmbDevice[] newArray(int size) {
            return new SmbDevice[size];
        }
    };
}
