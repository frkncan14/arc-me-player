package com.mstar.android.samba;

import android.os.Parcel;
import android.os.Parcelable;

public class SmbShareFolder extends SambaFile implements Parcelable {
    public SmbShareFolder(SmbDevice ssv) {
    }

    public SmbShareFolder(SmbDevice ssv, String name) {
    }

    public SmbDevice getSmbDevice() {
      return null;
    }

    public SmbAuthentication getAuth() {
      return null;
    }

    public String localPath() {
      return "";
    }

    public String remotePath() {
      return "";
    }

    public boolean canRead() {
      return false;
    }

    public boolean canWrite() {
      return false;
    }

    public void mount(SmbAuthentication smbAuthentication, int flags) {
    }

    public void unmount() {
    }

    public boolean isMounted() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public static final Parcelable.Creator<SmbShareFolder> CREATOR =
    new Parcelable.Creator<SmbShareFolder>() {
        public SmbShareFolder createFromParcel(Parcel in) {
            return new SmbShareFolder(in);
        }
        public SmbShareFolder[] newArray(int size) {
            return new SmbShareFolder[size];
        }
    };

    public SmbShareFolder(Parcel in) {
    }
}
