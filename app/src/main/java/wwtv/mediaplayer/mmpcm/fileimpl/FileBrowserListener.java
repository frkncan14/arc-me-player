package com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl;

public interface FileBrowserListener {
    void onFileParseing(FileList fl);

    void onFileFound(FileList fl);

    void onFileRemove(FileList fl);

    void onFileFailed(int errMsg);

    /**
     * for get mount list
     * @param dl
     */
    void onDeviceFound(DeviceList dl);

    /**
     * for device mounted when has in MMP
     */
    void onDeviceMount();

    /**
     * for device unmounted when has in MMP
     */
    void onDeviceUnmount();

    void onDeviceUnsupport();
}
