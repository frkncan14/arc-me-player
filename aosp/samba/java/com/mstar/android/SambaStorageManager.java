
package com.mstar.android.samba;

import android.content.Context;

public class SambaStorageManager {

    public static SambaStorageManager getInstance(Context context) {
      return null;
    }

    public boolean mountSamba(String host, String shareDirectory, String mountPoint, String userName,
                              String password) {
      return false;
    }

    public boolean unmountSamba(String mountPoint, boolean force) {
      return false;
    }
}
