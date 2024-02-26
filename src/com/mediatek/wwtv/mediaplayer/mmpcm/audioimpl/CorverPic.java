package com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.util.Util;

public final class CorverPic extends Info{
    private static final String TAG = "CorverPic";
    private UIMediaPlayer mtkPlayer;
    private static CorverPic aCorver = null;
    private boolean thumbLoadStart = false;
    private boolean cancelLoad = false;


    private CorverPic(int srcType) {
        mSrcType = srcType;
        mtkPlayer = new UIMediaPlayer(srcType) ;
    }

    private CorverPic() {
        mSrcType = FileConst.SRC_USB;
        mtkPlayer = new UIMediaPlayer(mSrcType) ;
    }

    public static synchronized CorverPic getInstance() {
        if (aCorver == null) {
            aCorver = new CorverPic();
        }
        return aCorver;
    }
    /**
     * Get audio corver picture by specified source type,path,
     * @param srcType
     * @param filepath
     * @param width
     * @param height
     * @return
     * @throws IllegalArgumentException
     */
    public Bitmap getAudioCorverPic(int srcType, String filepath, int width,
            int height) throws IllegalArgumentException {


        MmpTool.logDbg("path = " + filepath);
        Log.e(TAG, "getAudioCorverPic()...");
        if (filepath == null) {
            throw new IllegalArgumentException("empty path!");
        }

        if(srcType != mSrcType){
            mSrcType = srcType;
            mtkPlayer.release();
            mtkPlayer = new UIMediaPlayer(srcType);
        }

        if (srcType == FileConst.SRC_USB) {
            mCacheMetaData = getMediaInfo(filepath);
            return  getAudioBmp(filepath);
        }
        //Network Begin
        //Network Coverpicture not ready in mp3 parse,disable it
//        if(srcType != FileConst.SRC_USB)
//        {
//            loadDone();
//            return null;
//        }
//
//        synchronized(this) {
//            cancelLoad = false;
//            thumbLoadStart = true;
//        }
//
//        if (mtkPlayer != null && !(Util.isUseExoPlayer() && srcType != FileConst.SRC_USB)) {
//
//            mFilePath = filepath;
//
//            try {
//                mtkPlayer.reset();
//                Log.e(TAG, "filepath="+filepath);
//
//                mtkPlayer.setDataSource(mFilePath);
//                mtkPlayer.setPlayerRole(PlayerRole.ROLE_AUDIO_PLAYBACK);
//                mtkPlayer.prepare();
//
//            } catch (Exception e) {
//                Log.e(TAG, "exception");
//                MmpTool.logInfo( " getAudioCorverPic() :" + e.toString());
//                //Needn't stop for reuse next file
//                //Next File will reset
//
//                loadDone();
//                return null;
//            }
//
//            byte[] thBuffer = null;
//            if (!cancelLoad) {
//
//                thBuffer =mtkPlayer.getEmbeddedPicture();
//
//            }
//
//            if (thBuffer == null) {
//                //Needn't stop for reuse next file
//                //Next File will reset
//                loadDone();
//                return null;
//            }else{
//                try{
//                    Bitmap bitMap = BitmapFactory.decodeByteArray(thBuffer, 0,thBuffer.length);
//                    //Needn't stop for reuse next file
//                    //Next File will reset
//                    int bitWidth = bitMap.getWidth();
//                    int bitHeight = bitMap.getHeight();
//                    float scaleWidth = width / (float) bitWidth;
//                    float scaleHeight = height / (float) bitHeight;
//                    Matrix matrix = new Matrix();
//                    matrix.postScale(scaleWidth, scaleHeight);
//                    bitMap = Bitmap.createBitmap(bitMap, 0, 0, bitWidth, bitHeight,matrix, true);
//                    loadDone();
//
//                    return bitMap;
//                }catch(Exception ex){
//
//                    ex.printStackTrace();
//
//                }
//            }
//        }

        loadDone();
        return null;
    }


    private void loadDone(){
        synchronized(this) {
            thumbLoadStart = false;
            try{
                notifyAll();

            }catch(Exception ex){

                ex.printStackTrace();
            }
        }
        Log.d(TAG,"CorverPic loadDone ");

    }


    /**
     * Stop get meta data thumbnail
     */
    public void stopThumbnail() {
        Log.d(TAG,"CorverPic call ");
        synchronized(this) {
             while (thumbLoadStart) {
                try {

                    mtkPlayer.reset();

                    cancelLoad = true;

                    wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        Log.d(TAG,"CorverPic done");
    }

    public void resetSrcType() {
        mSrcType = 0;
    }
}
