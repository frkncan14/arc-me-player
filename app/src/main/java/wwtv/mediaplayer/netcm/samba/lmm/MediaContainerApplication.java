package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.netcm.samba.lmm.BaseData;
import com.mediatek.wwtv.mediaplayer.util.Constants;

/**
 * Container of media data.
 *
 * @author felix.hu
 */
public class MediaContainerApplication {
    private final static String TAG ="MediaContainerApplication";
    // singleton
    private static MediaContainerApplication instance;

    // all file in current path
    private List<BaseData> allFileList = null;

    // all folder in current path
    private List<BaseData> allFolderList = null;

    // all picture in current path
    private List<BaseData> allPictureFileList = null;

    // all song in current path
    private List<BaseData> allSongFileList = null;

    // all video in current path
    private List<BaseData> allVideoFileList = null;

    private int[] panelSize = {0,0};

    private long totalMem = -1;

    public MediaContainerApplication() {
        // init all arrayList
        allFileList = new ArrayList<BaseData>();
        allFolderList = new ArrayList<BaseData>();
        allPictureFileList = new ArrayList<BaseData>();
        allSongFileList = new ArrayList<BaseData>();
        allVideoFileList = new ArrayList<BaseData>();
    }

    /**
     * @return singleton instance.
     */
    public static MediaContainerApplication getInstance() {
        if (null == instance) {
          instance = new MediaContainerApplication();
        }
        return instance;
    }

    /**
     * Cache the media data.
     *
     * @param type media type.
     * @param data media data that will be cached in memory.
     */
    public final void putMediaData(final int type, final List<BaseData> data) {
        switch (type) {
            case Constants.FILE_TYPE_FILE:
                // cache all file
                allFileList.addAll(data);

                break;
            case Constants.FILE_TYPE_PICTURE:
                // cache picture file
                allPictureFileList.addAll(data);

                break;
            case Constants.FILE_TYPE_SONG:
                // cache song file
                allSongFileList.addAll(data);

                break;
            case Constants.FILE_TYPE_VIDEO:
                // cache video file
                allVideoFileList.addAll(data);

                break;
            case Constants.FILE_TYPE_DIR:
                // cache folder
                allFolderList.addAll(data);

                break;
            default:
                break;
        }
    }

    /**
     * Get all the media data with specified type.
     *
     * @param type media type.
     * @return media data in current folder or empty List.
     */
    public final List<BaseData> getMediaData(final int type) {
        List<BaseData> local = new ArrayList<BaseData>();
        // switch media type
        switch (type) {
            case Constants.FILE_TYPE_FILE:
                // return all file
                local.addAll(allFileList);

                break;
            case Constants.FILE_TYPE_PICTURE:
                // return all picture file
                local.addAll(allPictureFileList);

                break;
            case Constants.FILE_TYPE_SONG:
                // return all song file
                local.addAll(allSongFileList);

                break;
            case Constants.FILE_TYPE_VIDEO:
                // return all video file
                local.addAll(allVideoFileList);

                break;
            case Constants.FILE_TYPE_DIR:
                local.addAll(allFolderList);

                break;
            default:
                break;
        }

        return local;
    }

    /**
     * Clear all media data in memory.
     */
    public final void clearAll() {
        allFileList.clear();
        allFolderList.clear();
        allPictureFileList.clear();
        allSongFileList.clear();
        allVideoFileList.clear();
    }

    /**
     * Check whether has specified media data.
     *
     * @param type the type of media, such as image.
     * @return true if has specified media data in container.
     */
    public final boolean hasMedia(final int type) {
        boolean flag = false;

        switch (type) {
            case Constants.FILE_TYPE_PICTURE:
                // whether has picture or not
                if (allPictureFileList == null || allPictureFileList.size() == 0) {
                    flag = false;
                } else {
                    flag = true;
                }

                break;
            case Constants.FILE_TYPE_SONG:
                // whether has song or not
                if (allSongFileList == null || allSongFileList.size() == 0) {
                    flag = false;
                } else {
                    flag = true;
                }

                break;
            case Constants.FILE_TYPE_VIDEO:
                // whether has video or not
                if (allVideoFileList == null || allVideoFileList.size() == 0) {
                    flag = false;
                } else {
                    flag = true;
                }

                break;
            default:
                break;
        }

        return flag;
    }

    public final void setPanelSize(final int[] config) {
        panelSize = config;
    }

    public final int[] getPanelSize() {
        return panelSize;
    }

    public final void setTotalMem(final long total) {
        totalMem = total;
    }

    public final long getTotalMem() {
        return totalMem;
    }

    public final void clearPhotoList() {
        allPictureFileList.clear();
    }

    public final void setAllPhotoList(ArrayList<BaseData> photoList) {
        allPictureFileList = photoList;
    }
}
