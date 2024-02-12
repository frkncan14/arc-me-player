package com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.util.MetaDataInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.util.Util;

public final class AudioInfo extends Info {
    private static final Uri mArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    private static final String TAG = "AudioInfo";

    private ContentResolver mResolver;
    private Bitmap mBitmap;
    private UIMediaPlayer mtkPlayer;

    private static AudioInfo aInfo = null;

    private boolean metaLoadStart = false;

    private AudioInfo(Context context,int srcType) {
        mSrcType = srcType;
        mResolver = context.getContentResolver();
        mtkPlayer = new UIMediaPlayer(srcType);
        mBitmap = null;
    }

    private AudioInfo(int srcType) {
        mSrcType = srcType;
        mtkPlayer = new UIMediaPlayer(srcType);
        mBitmap = null;
    }

    public UIMediaPlayer getPlayer() {
        return mtkPlayer;
    }

    private AudioInfo(Context context) {
            mSrcType = FileConst.SRC_USB;
        mResolver = context.getContentResolver();
            mtkPlayer = new UIMediaPlayer(mSrcType);
        mBitmap = null;
    }

    private AudioInfo() {
        mSrcType = FileConst.SRC_USB;
        mtkPlayer = new UIMediaPlayer(mSrcType);
        mBitmap = null;
    }

    public static synchronized AudioInfo getInstance() {
        if (aInfo == null) {
            aInfo = new AudioInfo();
        }
        return aInfo;
    }

    /**
     * According to the parameters for the meta data info
     *
     * @param path
     * @param srcType
     *            , specified source typle
     * @return return meta data info
     */
    public MetaData getMetaDataInfo(String path, int srcType)
            throws IllegalArgumentException {
        MmpTool.logDbg("path = " + path);
        Log.d(TAG,"getMetaDataInfo()...srcType=="+srcType);

        if (srcType == FileConst.SRC_DLNA) {
            return null;
        }

        if(srcType != mSrcType){
            mSrcType = srcType;
        }
        if (mtkPlayer != null) {
            mtkPlayer.stop();
            mtkPlayer.release();
            mtkPlayer = null;
        }

        mtkPlayer = new UIMediaPlayer(srcType);

        if (srcType == FileConst.SRC_USB) {

            return  getMediaInfo(path);
        }

        //Network begin

        MetaData mMetaInfo = new MetaData();

        if (path == null) {
            throw new IllegalArgumentException("empty path!");
        }

       //Keep for debug
       // if(false)
       // {
       //    mMetaInfo.setMetaData(null, null, null, null, null, null, null,
       //                0, 0);
       //      return mMetaInfo;
       // }

        if (mtkPlayer != null && !Util.isUseExoPlayer())  {
            mFilePath = path;

            try {
                //mtkPlayer.reset();

                mtkPlayer.setDataSource(mFilePath);
                // mtkPlayer.setPlayerRole(.PlayerRole.ROLE_GET_METADATA);
                mtkPlayer.prepare();
            } catch (Exception e) {
                MmpTool.logInfo(" getMetaDataInfo()  :" + e.toString());
                mMetaInfo.setMetaData(null, null, null, null, null, null, null,
                        0, 0);

                //Needn't stop for reuse next file
                //Next File will reset
                return mMetaInfo;
            }

            metaLoadStart = true;

            MetaDataInfo info = mtkPlayer.getMetaDataInfo();
            if (null == info) {
                Log.e(TAG, "info is null");
                mMetaInfo.setMetaData(null, null, null, null, null, null, null,
                        0, 0);
            } else {
                mMetaInfo.setMetaData(info.getTitle(), info.getDirector(),
                        info.getCopyright(), info.getYear(), info.getGenre(),
                        info.getArtist(), info.getAlbum(),
                        info.getTotalPlayTime(), info.getBiteRate());
            }
        }

        return mMetaInfo;
    }

    /**
     * Stop get meta data
     */
    public void stopMetaData() {
        Log.d(TAG, "stopMetaData()");
        if (mtkPlayer != null && metaLoadStart == true) {
            try {
                mtkPlayer.stop();
                mtkPlayer.release();
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
      mBitmap = null;
      mResolver = null;
      synchronized(AudioInfo.class) {
          aInfo = null;
      }
    }

    @Deprecated
    private Cursor getCursor(String path, String[] projection) {
        if (path == null) {
            return null;
        }

        Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(path);
        String selection = MediaStore.Audio.Media.DATA + "=?";
        String[] selectionArgs = new String[] { path };

        return mResolver.query(contentUri, projection, selection,
                selectionArgs, null);
    }

    /**
     * @deprecated
     */
//    @Deprecated
//    private Bitmap getArtwork(long albumId) {
//        Uri uri = ContentUris.withAppendedId(mArtworkUri, albumId);
//        if (uri != null) {
//            InputStream in = null;
//            try {
//                in = mResolver.openInputStream(uri);
//                mBitmap = BitmapFactory.decodeStream(in, null, null);
//            } catch (FileNotFoundException e) {
//                // TODO The album artwork file doesn't exist
//                // should be replaced by default bitmap
//            } finally {
//                if (null != in) {
//                  try {
//                    in.close();
//                  } catch (IOException e) {
//                    e.printStackTrace();
//                  }
//                }
//
//            }
//        }
//
//        return mBitmap;
//    }

}
