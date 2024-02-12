package com.mediatek.wwtv.mediaplayer.mmpcm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.util.Log;

/*
 *
 * get media Metedata base class
 */
public class Info {
    private static final String TAG ="Info";
    protected String mFilePath;
    protected int mSrcType;
    protected static MetaData mCacheMetaData = null;

    public static MetaData getCacheMetaData(){
        return mCacheMetaData;
    }

    public MetaData getMediaInfoWithRetriever(MediaMetadataRetriever retriever){
        MetaData mMetaInfo = new MetaData();

        String mtitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        //Log.d(TAG, "mtitle:"+mtitle);

        String mdirector = null;//retriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_DIRECTOR);
        String mcopyright = null;//retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COPYRIGHT);

        String myear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
        //Log.d(TAG, "myear:"+myear);

        String mgenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
        //Log.d(TAG, "mgenre:"+mgenre);

        String martist =  retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        //Log.d(TAG, "martist:"+martist);

        String malbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        //Log.d(TAG, "malbum:"+malbum);

        String mbitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        //Log.d(TAG, "mbitrate:"+mbitrate);

        //String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        //Log.d(TAG, "mimeType:"+mimeType);

        String mdur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        //Log.d(TAG, "mdur:"+mdur);


        int dur = 0;
        try{
            dur = Integer.valueOf(mdur);

        }catch(Exception ex){
            Log.d(TAG,"duration to int error~~");
        }

        int mbitratet= 200;
        try{
            mbitratet = Integer.valueOf(mbitrate);

        }catch(Exception ex){
            Log.d(TAG,"mbitrate to int error~~");
        }

        mMetaInfo.setMetaData(mtitle, mdirector, mcopyright, myear,
                mgenre, martist, malbum, dur,
                mbitratet);
        /*
        Log.d(TAG, "video myear:"+myear+"_mtitle:"+mtitle+"_martist:"+martist
                +"_malbum:"+malbum+"_mgenre:"+mgenre);
        */

        return mMetaInfo;
    }

    public MetaData getMediaInfo(String path){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
        } catch(Exception e) {
            retriever.release();
            Log.d(TAG,"setdataSource fail ~");
            return null;
        }

        MetaData mMetaInfo = getMediaInfoWithRetriever(retriever);

        retriever.release();

        return mMetaInfo;
    }

    public MetaData getMediaInfo(String path, MediaMetadataRetriever retriever){
        MetaData mMetaInfo;

        try {
            mMetaInfo = getMediaInfoWithRetriever(retriever);
        }catch (Exception ex) {
            // File is corrupt or retriever already released
            Log.i(TAG, "File is corrupt or retriever already released");
            mMetaInfo = null;
        } finally {
            try {
                retriever.release();
                Log.i(TAG, "release");
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up
                Log.i(TAG, "RuntimeException ex");
            }
        }

        return mMetaInfo;
    }

    public Bitmap getAudioBmp(String path){
         MediaMetadataRetriever retriever = new MediaMetadataRetriever();
         try{
             retriever.setDataSource(path);

         }catch(Exception e){
                 retriever.release();
             return null;
         }
         byte[] bit = retriever.getEmbeddedPicture();
         Bitmap bmp = null;
         if(bit != null){
            int len = bit.length;
            bmp = BitmapFactory.decodeByteArray(bit,0,len);
         }else{
             retriever.release();
            Log.d(TAG,"get bit = null");
            return null;
         }
         retriever.release();
         Log.d(TAG,"getAudiobmp:"+bmp);
         return bmp;

    }
}
