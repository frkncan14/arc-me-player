package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import java.io.File;
import java.math.BigInteger;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.util.Constants;

public class BaseDataManager {
    private String TAG = "BaseDataManager";

    // data container
    public MediaContainerApplication mediaContainer = null;

    private Comparator<BaseData> comparator = new Comparator<BaseData>() {

        @Override
        public int compare(BaseData lData, BaseData rData) {

            String lName = lData.getName();
            String rName = rData.getName();
            if (lName != null && rName != null) {
                Collator collator = Collator.getInstance(Locale.CHINA);
                return collator.compare(lName.toLowerCase(),
                        rName.toLowerCase());

            } else {
                Log.e(TAG, "comparator lName != null && rName != null is false");
                return 0;
            }

        }
    };

    /**
     * The base class constructor.
     *
     * @param resource
     *            {@link Resources}.
     */
    public BaseDataManager() {
        this.mediaContainer = MediaContainerApplication.getInstance();

       Log.e(TAG, "comparator lName != null && rName != null is false");
    }

    /**
     * For UI display need folder data and media file data.
     *
     * @param type
     *            medium type.
     * @return Returns the current directory all folders and designated type of
     *         media files.
     */
    public final synchronized List<BaseData> getUIDataList(final int type) {
        List<BaseData> local = new ArrayList<BaseData>();
        // Add all folders data
        local.addAll(mediaContainer.getMediaData(Constants.FILE_TYPE_DIR));

        if (type == Constants.OPTION_STATE_ALL) {
            // Add all file data
            local.addAll(mediaContainer.getMediaData(Constants.FILE_TYPE_FILE));

            // Add all designated type of media files
        } else {
            local.addAll(getMediaFileList(type));
        }

        return local;
    }

    /**
     * For specified types of media file data.
     *
     * @param type
     *            medium type.
     * @return Returns the current directory all designated type of media files.
     */
    public final synchronized ArrayList<BaseData> getMediaFileList(
            final int type) {
        ArrayList<BaseData> local = new ArrayList<BaseData>();

        switch (type) {
        case Constants.OPTION_STATE_PICTURE:
            // Add all pictures data
            local.addAll(mediaContainer.getMediaData(Constants.FILE_TYPE_PICTURE));
            break;

        case Constants.OPTION_STATE_SONG:
            // Add all the music data
            local.addAll(mediaContainer.getMediaData(Constants.FILE_TYPE_SONG));
            break;

        case Constants.OPTION_STATE_VIDEO:
            // Add all the video data
            local.addAll(mediaContainer.getMediaData(Constants.FILE_TYPE_VIDEO));
            break;

        default:
            break;
        }

        return local;
    }

    /**
     * Open a new thread complete file scanning and classification.
     *
     * @param path
     *            Scanning using absolute path.
     */
    public final synchronized void startScan(final String path) {
        // If the path is empty is directly to return
        if (path == null || path.length() == 0) {
            return;
        }
        File file = new File(path);
        // Only directory to scan
        if (file.isDirectory()) {
            File[] ff = file.listFiles();
            if (file.list() != null && file.list().length > 0) {
                // Under the current directory lists all documents
                // (folder)
                scan(file.listFiles());
            } else {
                // Clear all cache data
                mediaContainer.clearAll();
            }

        }

        onFinish();
    }

    /**
     * Data scan complete.
     */
    public void onFinish() {
    }

    /*
     * Scanning the specified directory of all files or folders.
     */
    private void scan(final File[] files) {
        // all files
        List<BaseData> localFile = new ArrayList<BaseData>();
        // all folders
        List<BaseData> localFolder = new ArrayList<BaseData>();
        // all pictures
        List<BaseData> localPicture = new ArrayList<BaseData>();
        // all musics
        List<BaseData> localSong = new ArrayList<BaseData>();
        // all videos
        List<BaseData> localVideo = new ArrayList<BaseData>();

        try {
            // Under the current directory lists all documents (folder) list
            for (File f : files) {
                // Obtain filename
                String name = f.getName();
                BaseData file = new BaseData();
                // Setting absolute path
                file.setPath(f.getAbsolutePath());
                // Set parent directory path
                file.setParentPath(f.getParent());
                // Set up files (folders) name
                file.setName(name);

                // Scanning to folder
                if (f.isDirectory()) {
                    file.setType(Constants.FILE_TYPE_DIR);
                    localFolder.add(file);

                    // Scanning to file
                } else {
                    // Obtain and set the file extension
                    int pos = name.lastIndexOf(".");
                    String extension = "";
                    if (pos > 0) {
                        extension = name.toLowerCase().substring(pos + 1);
                        file.setFormat(extension);
                    }
                    // setting file size
                    file.setSizeEx((long)(f.length()));
                    //file.setDescription(formatSize);
                    // Set the file modification time
                    file.setModifyTime(f.lastModified());

                    if (check(name, FileConst.photoSuffix)) {
                        file.setType(Constants.FILE_TYPE_PICTURE);
                        localPicture.add(file);
                    } else if (check(name, FileConst.audioSuffix)) {
                        file.setType(Constants.FILE_TYPE_SONG);
                        localSong.add(file);
                    } else if (check(name, FileConst.videoSuffix)) {
                        file.setType(Constants.FILE_TYPE_VIDEO);
                        localVideo.add(file);
//                    } else if (check(name, resource.getStringArray(R.array.playlist_filter))) {
//                        file.setType(Constants.FILE_TYPE_MPLAYLIST);
                    } else {
                        file.setType(Constants.FILE_TYPE_FILE);
                    }

                    // Save all data
                    localFile.add(file);
                }
            }
        } catch (Exception e) {

        }
        // Clear all cache data
        mediaContainer.clearAll();
        // All documents in order to list
        if (localFile.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_FILE, localFile);
        }
        // For all folders sorting
        if (localFolder.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_DIR, localFolder);
        }
        // For all image list sorting
        if (localPicture.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_PICTURE, localPicture);
        }
        // For all music list sorting
        if (localSong.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_SONG, localSong);
        }
        // For all video list sorting
        if (localVideo.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_VIDEO, localVideo);
        }

    }

    /*
     * Through the filename judgment is what types of documents..
     */
    public boolean check(final String name, final String[] extensions) {
        for (String end : extensions) {
            // Name never to null, without exception handling
            if (name.toLowerCase().endsWith(end)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Cache Data.
     */
    public void putAllToCache(final int type, final List<BaseData> src) {
        // sort data
        Collections.sort(src, comparator);
        // cache to memory
        mediaContainer.putMediaData(type, src);
    }

}
