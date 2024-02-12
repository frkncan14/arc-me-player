package com.mediatek.wwtv.mediaplayer.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public final class TVStorage {
    private static final String TVAP_TABLE = "tvap_storage_table";
    final private SharedPreferences pref;
    final private Editor editor;
    private static TVStorage storage;

    private TVStorage(Context context) {
        pref = context.getSharedPreferences(TVAP_TABLE, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void handleMessage(int msgCode, Object obj) {
        Log.d(TVAP_TABLE, "handleMessage");
    }

    public static synchronized TVStorage getInstance(Context context) {
        if (storage == null) {
            storage = new TVStorage(context);
        }
        return storage;
    }

    public SharedPreferences getShareFerences() {
        return pref;
    }

    public Editor getEditor() {
        return editor;
    }

    public void set(String k, String v) {
        getEditor().putString(k, v);
        getEditor().commit();
        flushMedia();
    }

    /**
     * @return the value of Key
     * @param key
     *            the key to identify value
     */
    public String get(String key) {
        return pref.getString(key, null);
    }

    /**
     * @return the value of key
     * @param key
     *            the key to identify value
     * @param defVal
     *            if the Key key do not exit in share preference, return the
     *            default value.
     */
    public String get(String key, String defValue) {
        return pref.getString(key, defValue);
    }

    public void clean() {
        getEditor().clear();
        getEditor().commit();
        flushMedia();
    }
    public void flushMedia() {
        try {
            Runtime.getRuntime().exec("sync");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
