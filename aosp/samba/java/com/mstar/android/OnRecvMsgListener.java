package com.mstar.android.samba;

public interface OnRecvMsgListener {
    public static final int MSG_UPDATE_DEVLIST_CANCEL = 0x01;
    public static final int MSG_UPDATE_DEVLIST_DONE = 0x02;
    public static final int MSG_UPDATE_DEVLIST_ADD = 0x03;

    public void onRecvMsgListener(int msg);
}
