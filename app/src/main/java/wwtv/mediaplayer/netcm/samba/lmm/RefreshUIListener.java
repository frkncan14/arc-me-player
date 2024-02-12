package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import java.util.List;

/**
 * Refresh UI data encapsulation completed or failed callback interface.
 */
public interface RefreshUIListener {

    public void onFinish(List<BaseData> data, int currentPage, int totalPage, int position);
    public void onOneItemAdd(List<BaseData> data, int currentPage, int totalPage, int position);
    public void onScanDeviceCompleted();
    public void onFailed(int code);
    public void onShowLoginDialog();
    public void onSambaEquipment();
}
