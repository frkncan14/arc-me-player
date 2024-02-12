package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;

import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.MetaDataInfo;

public final class VideoInfo extends Info {
    private static final String TAG = "VideoInfo";

    private UIMediaPlayer mtkPlayer;
    private static VideoInfo vInfo = null;
    private boolean metaLoadStart = false;

    private VideoInfo(int srcType) {
        mSrcType = srcType;
        mtkPlayer = new UIMediaPlayer(srcType);
    }

    private VideoInfo() {
        mSrcType = FileConst.SRC_USB;
        mtkPlayer = new UIMediaPlayer(mSrcType);
    }

    public static synchronized VideoInfo getInstance() {

        if (vInfo == null) {
            vInfo = new VideoInfo();
        }
        return vInfo;
    }

    /**
     * Get MetaDate object assign
     *
     * @param path
     *            ,srcType
     */
    public MetaData getMetaDataInfo(String path, int srcType)
            throws IllegalArgumentException {
        MmpTool.logDbg("path = " + path);

        if (path == null) {
            throw new IllegalArgumentException("empty path!");
        }

        if (srcType != mSrcType) {
            mSrcType = srcType;
            mtkPlayer.release();
            mtkPlayer = new UIMediaPlayer(srcType);
        }

        if (srcType == FileConst.SRC_USB) {
            return getMediaInfo(path);
        }
        //DLNA
        //Disable for debug
        if(FileConst.SRC_DLNA  == srcType ||
           FileConst.SRC_SMB == srcType) {
           return null;
        }

        MetaData mMetaInfo = new MetaData();

        if (mtkPlayer != null && !Util.isUseExoPlayer()) {
            mFilePath = path;
            try {
                mtkPlayer.reset();
                mtkPlayer.setDataSource(mFilePath);
//                mtkPlayer.setPlayerRole(.PlayerRole.ROLE_GET_METADATA);
                mtkPlayer.prepare();
            } catch (Exception e) {
                MmpTool.logInfo(" getMetaDataInfo()  :" + e.toString());
                mMetaInfo.setMetaData(null, null, null, null, null, null, null,
                        0, 0);
                metaLoadStart = false;
                mtkPlayer.closeStream();
                return mMetaInfo;
            }
            metaLoadStart = true;
            MetaDataInfo metaDataInfo = mtkPlayer.getMetaDataInfo();
            if (metaDataInfo != null) {
                String mtitle = metaDataInfo.getTitle();
                String mdirector = metaDataInfo.getDirector();
                String mcopyright = metaDataInfo.getCopyright();
                String myear = metaDataInfo.getYear();
                String mgenre = metaDataInfo.getGenre();
                String martist = metaDataInfo.getArtist();
                String malbum = metaDataInfo.getAlbum();
                int mbitrate = metaDataInfo.getBiteRate();
                mMetaInfo.setMetaData(mtitle, mdirector, mcopyright, myear, mgenre, martist, malbum, mtkPlayer.getDuration(), mbitrate);
            } else {
                mMetaInfo.setMetaData(null, null, null, null, null, null, null, mtkPlayer.getDuration(), 0);
            }
            metaLoadStart = false;
        }

        return mMetaInfo;
    }

    /**
     * stop meta data
     */
    public void stopMetaData() {
        Log.d(TAG, "stopMetaData() ");
        if (mtkPlayer != null && metaLoadStart == true) {
            try {
                mtkPlayer.stop();
                metaLoadStart = false;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                metaLoadStart = false;
                return;
            }
        }
    }

    public void destroyInfo() {
      mtkPlayer = null;
      synchronized(VideoInfo.class) {
          vInfo = null;
      }
    }

}
