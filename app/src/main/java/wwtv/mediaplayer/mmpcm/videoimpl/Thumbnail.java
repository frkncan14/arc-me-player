
package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore.Video.Thumbnails;
import android.media.MediaDataSource;
import android.util.Log;
import android.media.ThumbnailUtils;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;

import com.mediatek.ExoMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAEXODataSource;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import android.media.MediaMetadataRetriever;
import com.mediatek.DLNAFrameworkDataSource;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;

public class Thumbnail extends Info {
  private static final String TAG = "Thumbnail";
  private UIMediaPlayer mtkPlayer;
  private Context mContext;
  private boolean thumbLoadStart = false;
  private static Thumbnail vThumb = null;
  private boolean cancelLoad = false;

  BlockingQueue<Integer> mQueue = new ArrayBlockingQueue(2);

  public static final int INIT = 0;
  public static final int RELEASE = 1;
  public static final int DLNASOURCE = 2;
  public static final int SAMBASOURCE = 3;
  public static final int SETQUEUE = 4;
  public static final int MetaData = 5;

  public static final int PREPARED = 1;
  public static final int ERROR = 2;
  public static final int EXCETPION = 3;
  public static final int MEDIAPLAYER = 4;
  public static final int ABORT = 5;
  private final MyHandler mHandler;
  private boolean mHasResetRigion;
  private MediaMetadataRetriever retriever = null;
  public void setRestRigionFlag(boolean hasResetRigion) {
    mHasResetRigion = hasResetRigion;
  }

  public boolean hasResetRigion() {
    return mHasResetRigion;
  }

  public void setContext(Context context) {
    mContext = context;
  }

  public Context getContext() {
    return mContext;
  }

  public void resetHandlerSource() {
      mHandler.mSource = 0;
  }

  private class MyHandler extends Handler {

    public int mSource;
    BlockingQueue<Integer> mBlockQueue = null;
    private final ExoMediaPlayer.OnPreparedListener mtkOnPreparedListener
    = new ExoMediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            Log.i(TAG, "onPrepared Thumbnail");
            try {
                Log.i(TAG, "onPrepared put PREPARED");
                mBlockQueue.put(PREPARED);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "obj notify");
        }
    };

    private final ExoMediaPlayer.OnErrorListener
    mtkOnErrorListener = new ExoMediaPlayer.OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            try {
                Log.i(TAG, "onError put ERROR");
                mBlockQueue.put(ERROR);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    };

    private UIMediaPlayer mtkPlayer;

    public UIMediaPlayer getPlayer() {
      return mtkPlayer;
    }

    public MyHandler(Looper looper) {
      super(looper);
    }


    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case INIT:
          Log.i(TAG, "handleMessage INIT");
          mSource = -1;
          if (mtkPlayer != null) {
            mtkPlayer.setOnPreparedListener(null);
            mtkPlayer.setOnErrorListener(null);
            mtkPlayer.release();
            mtkPlayer = null;
          }
          break;
        case RELEASE:
          Log.i(TAG, "handleMessage RELEASE");
          if (mtkPlayer != null) {
            mtkPlayer.reset();
          }
          try {
            mBlockQueue.put(ABORT);
          } catch (Exception e) {
            Log.i(TAG, "handleMessage RELEASE put Error Exception");
          }
          break;
        case DLNASOURCE:
        case SAMBASOURCE:
          Log.i(TAG, "msg.what:" + msg.what);

          try {
            //Switch
            if (mSource != msg.what)
            {
                mSource = msg.what;
            }

            if (mtkPlayer != null) {
                mtkPlayer.stop();
                mtkPlayer.release();
                mtkPlayer = null;
            }
            mtkPlayer = new UIMediaPlayer(msg.what);

            mBlockQueue.put(MEDIAPLAYER);
            mFilePath = (String) msg.obj;

            mtkPlayer.setOnPreparedListener(mtkOnPreparedListener);
            mtkPlayer.setOnErrorListener(mtkOnErrorListener);
            mtkPlayer.setDataSource(mFilePath);
//            mtkPlayer.setPlayerRole(PlayerRole.ROLE_VIDEO_PLAYBACK);
            Log.i(TAG, "Thread name: " + Thread.currentThread().getName());
            mtkPlayer.prepare();
            Log.d(TAG, "prepare done ");
          } catch (Exception e) {
            e.printStackTrace();
            try {
              mBlockQueue.put(EXCETPION);
            } catch (Exception e2) {
              e2.printStackTrace();
            }
          }
          break;
        case SETQUEUE:
          Log.i(TAG, "handleMessage SETQUEUE ");
          mBlockQueue = (ArrayBlockingQueue) msg.obj;
          break;
        case MetaData:
          Log.i(TAG, "handleMessage MetaData ");
          MediaMetadataRetriever retriever = (MediaMetadataRetriever) msg.obj;
          mCacheMetaData = getMediaInfo(mFilepath, retriever);
          break;
        default:
          break;
      }

    }
  }

  private Thumbnail() {
    mSrcType = FileConst.SRC_USB;

    HandlerThread mHandlerThread = new HandlerThread("Thumbnail", Process.THREAD_PRIORITY_LOWEST);
    mHandlerThread.start();

    mHandler = new MyHandler(mHandlerThread.getLooper());
    Message msg = mHandler.obtainMessage(SETQUEUE, mQueue);
    mHandler.sendMessage(msg);

  }

  public static synchronized Thumbnail getInstance() {
    if (vThumb == null) {
      vThumb = new Thumbnail();
    }
    return vThumb;
  }

  private String mFilepath = null;


  /**
   * get video thumbnail bitmap
   */
  public Bitmap getVideoThumbnail(int srcType, String filepath, int width,
      int height) throws IllegalArgumentException {
    Log.d(TAG, "filepath = " + filepath);
    mFilepath = filepath;

    if (filepath == null) {
      throw new IllegalArgumentException("empty filepath!");
    }

    if (LogicManager.getInstance(null) != null){
        boolean isPlaying = LogicManager.getInstance(null).isPlaying();
        Log.i(TAG, "getVideoThumbnail,isPlaying=="+isPlaying);
        if (isPlaying){
            return null;
        }
    }

    mSrcType = srcType;
    //Local
    if (srcType == FileConst.SRC_USB){
        if (srcType != mSrcType){
          Log.i(TAG, "sendEmptyMessage INIT");
          mHandler.sendEmptyMessage(INIT);
        }
        Bitmap videoThumbnailLocal = getVideoThumbnailLocal(filepath, width, height,
                Thumbnails.MINI_KIND);
        Message msg = mHandler.obtainMessage(MetaData, retriever);
        mHandler.sendMessage(msg);
        return videoThumbnailLocal;
    }

    //ExoPlayer && Network
    if (srcType != FileConst.SRC_USB)
    {
        return getVideoThumbnaiExoPlayerNetwork(filepath, width, height,
          Thumbnails.MINI_KIND);
    }

    //CmpbPlayer && Network
    return getVideoThumbnaiCmpbPlayerNetwork(filepath, width, height,
      Thumbnails.MINI_KIND);

  }

  private void loadDone() {
    Log.d(TAG, "begin Thumbnail loadDone ");
    synchronized(this) {
        try {
          thumbLoadStart = false;
          notifyAll();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
    }
    Log.d(TAG, "end Thumbnail loadDone ");
  }


private Bitmap getVideoThumbnaiExoPlayerNetwork(String videoPath,
                                    int width,
                                    int height,
                                    int kind)
{
    Log.i(TAG, "getVideoThumbnaiExoPlayerNetwork,kind=="+kind);
    Bitmap bitmap = null;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    try
    {
       if (mSrcType != FileConst.SRC_USB)
       {
           if(mtkPlayer == null)
           {
               mtkPlayer = new UIMediaPlayer(mSrcType);
           }

           mtkPlayer.setFilePath(videoPath);
           DLNAEXODataSource tempSource = new DLNAEXODataSource(mtkPlayer);

           DLNAFrameworkDataSource frameworkDataSource = new DLNAFrameworkDataSource(tempSource, null);

           MediaDataSource mediaDataSource = (MediaDataSource)frameworkDataSource.getDataSourceForNativeExtractor();

           if (mediaDataSource != null)
           {
               //Log.i(TAG, "APP setDataSource:+:");
               retriever.setDataSource(mediaDataSource);
               //Log.i(TAG, "APP setDataSource:-");

               //Log.i(TAG, "getFrameAtTime:+:");
               bitmap = retriever.getFrameAtTime(-1);
               //Log.i(TAG, "getFrameAtTime:-");
               if(null == bitmap)
               {
                   //Log.i(TAG, "getFrameAtTime0:+:");
                   bitmap = retriever.getFrameAtTime(0);
                   //Log.i(TAG, "getFrameAtTime0:-");
               }
           }
        }
    }
    catch (IllegalArgumentException ex)
    {
        // Assume file is a corrupt video file
        Log.i(TAG, "IllegalArgumentException");
    }
    catch (RuntimeException ex)
    {
        // Assume file is a corrupt video file.
        Log.i(TAG, "RuntimeException");
    }
    finally
    {
       try {
         //Log.i(TAG, "release:+");
         retriever.release();
         //Log.i(TAG, "release:-");
       }
       catch (RuntimeException ex)
       {
         // Ignore failures while cleaning up.
         Log.i(TAG, "RuntimeException ex");
       }
    }
    //Log.i(TAG, "Thumbnail getThumbnailUtil bitmap:" + bitmap);
    if (bitmap == null)
    {
      return null;
    }

    bitmap = extractThumbnail(bitmap, width, height,
        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

    return bitmap;

  }


private Bitmap getVideoThumbnaiCmpbPlayerNetwork(String videoPath,
                                                int width,
                                                int height,
                                                int kind)
{
    //boolean isMp4 = false;
    //String tmpStr= videoPath.toLowerCase();
    Log.i(TAG, "getVideoThumbnaiCmpbPlayerNetwork,kind=="+kind);

    //For mp4 thumbnail crash issue
    //Mp4 Multi-instance Issue
    /*
    if(tmpStr.endsWith(".mp4"))
    {
       isMp4 = true;

       loadDone();
       return null;
    }
   */
    mQueue.clear();

    synchronized (this) {
      cancelLoad = false;
      thumbLoadStart = true;
    }

    Message msg = mHandler.obtainMessage(mSrcType, 0, 0, videoPath);
    mHandler.sendMessage(msg);

    //Poll out MediaPlayer
    int mediaExsit = -1;
    try {
      Integer state = mQueue.poll(1,TimeUnit.SECONDS);
      if (state == null) {
        Log.i(TAG, "mediaExsit mQueue state timeout");
      } else {
        mediaExsit = state;
      }
      Log.i(TAG, "mediaExsit ==" + mediaExsit);
    } catch (Exception e) {
      Log.i(TAG, "mediaExsit take Exception");
      loadDone();
      return null;
    }

    if (MEDIAPLAYER == mediaExsit) {
      //Log.i(TAG, "get mtkPlayer before ");
      mtkPlayer = mHandler.getPlayer();
      //Log.i(TAG, "get mtkPlayer ==" + mtkPlayer);
    } else {
      Log.i(TAG, "get mediaExsit != MEDIAPLAYER ");
      loadDone();
      return null;
    }

    //Poll out state, if not PREPARED, return
    int situration = -1;
    try {

      Integer state1 = mQueue.poll(5,TimeUnit.SECONDS);
      if (state1 == null) {
        Log.i(TAG, "mediaExsit mQueue state1 timeout");
      } else {
        situration = state1;
      }
      Log.i(TAG, "Situration take after:Situration == " + situration);
    } catch (InterruptedException e) {
      // e.printStackTrace();
      Log.i(TAG, "Situration take Exception");
      loadDone();
      return null;
    }

    if (PREPARED != situration) {
      Log.i(TAG, "Situration !==PREPARED,Issue Handle ");
      //If not PREPARED, just return
      loadDone();
      return null;
    }

    byte[] thBuffer = new byte[0];
    Bitmap bitmap = Bitmap.createBitmap(VideoConst.THUMBNAIL_WIDTH,
        VideoConst.THUMBNAIL_HEIGTH, Bitmap.Config.RGB_565);

    ByteBuffer buffer = ByteBuffer.wrap(thBuffer);
    bitmap.copyPixelsFromBuffer(buffer);

    int bitWidth = bitmap.getWidth();
    int bitHeight = bitmap.getHeight() - 2;
    float scaleWidth = width / (float) bitWidth;
    float scaleHeight = height / (float) bitHeight;
    Matrix matrix = new Matrix();
    matrix.postScale(scaleWidth, scaleHeight);
    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitWidth, bitHeight,
        matrix, true);
    loadDone();
    return bitmap;

}

  private Bitmap getVideoThumbnailLocal(String videoPath, int width, int height,
      int kind) {
    Log.d(TAG, "getVideoThumbnailLocal start,videoPath=="+videoPath+" ,kind=="+kind);
    Bitmap bitmap = null;
    retriever = new MediaMetadataRetriever();
    try {
        Log.i(TAG, "retriever.setDataSource");
        retriever.setDataSource(videoPath);

         boolean isSupportLoadThumbnail = isSupportLoadThumbnail(retriever);
         if (!isSupportLoadThumbnail){
             Log.d(TAG,"not support load thumbnail");
             return null;
         }

        Log.i(TAG, "retriever.getFrameAtTime");
        bitmap = retriever.getFrameAtTime(-1);
        Log.i(TAG, "getVideoThumbnailLocal,bitmap=="+bitmap);
        if(null == bitmap){
            bitmap = retriever.getFrameAtTime(0);
            Log.i(TAG, "getVideoThumbnailLocal,getFrameAtTime(0) end");
        }

    } catch (IllegalArgumentException ex) {
      // Assume file is a corrupt video file
      Log.i(TAG, "IllegalArgumentException");
    } catch (RuntimeException ex){
      // Assume file is a corrupt video file.
      Log.i(TAG, "RuntimeException");
    }
//    finally {
//       try {
//         retriever.release();
//        Log.i(TAG, "release");
//       } catch (RuntimeException ex) {
//         // Ignore failures while cleaning up.
//         Log.i(TAG, "RuntimeException ex");
//       }
//    }
    MtkLog.i(TAG, "Thumbnail bitmap:" + bitmap);
    if (bitmap == null) {
      return null;
    }
    bitmap = extractThumbnail(bitmap, width, height,
        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

    Log.d(TAG, "getVideoThumbnailLocal end");
    return bitmap;

  }

  private Bitmap extractThumbnail(Bitmap source, int width, int height,
      int options) {
    if (source == null) {
      return null;
    }

    float scale;
    if (source.getWidth() < source.getHeight()) {
      scale = width / (float) source.getWidth();
    } else {
      scale = height / (float) source.getHeight();
    }
    Matrix matrix = new Matrix();
    matrix.setScale(scale, scale);
    Bitmap thumbnail = transform(matrix, source, width, height,
        0x1 | options);
    MtkLog.i(TAG, "Thumbnail extractThumbnail thumbnail:"
        + thumbnail);
    return thumbnail;
  }

  private Bitmap transform(Matrix scaler, Bitmap source, int targetWidth,
      int targetHeight, int options) {
    boolean scaleUp = (options & 0x1) != 0;
    boolean recycle = (options & ThumbnailUtils.OPTIONS_RECYCLE_INPUT) != 0;

    int deltaX = source.getWidth() - targetWidth;
    int deltaY = source.getHeight() - targetHeight;
    if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
      /*
       * In such case the bitmap is smaller, at least in one dimension, than the target. Transform
       * it by placing as much of the image as possible into the target and leaving the top/bottom
       * or left/right (or both) black.
       */
      Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
          Bitmap.Config.ARGB_8888);
      Canvas c = new Canvas(b2);

      int deltaXHalf = Math.max(0, deltaX / 2);
      int deltaYHalf = Math.max(0, deltaY / 2);
      Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
          + Math.min(targetWidth, source.getWidth()), deltaYHalf
          + Math.min(targetHeight, source.getHeight()));
      int dstX = (targetWidth - src.width()) / 2;
      int dstY = (targetHeight - src.height()) / 2;
      Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
          - dstY);
      c.drawBitmap(source, src, dst, null);
      if (recycle) {
        source.recycle();
      }
      c.setBitmap(null);
      return b2;
    }
    float bitmapWidthF = source.getWidth();
    float bitmapHeightF = source.getHeight();

    float bitmapAspect = bitmapWidthF / bitmapHeightF;
    float viewAspect = (float) targetWidth / targetHeight;

    if (bitmapAspect > viewAspect) {
      float scale = targetHeight / bitmapHeightF;
      if (scale < .9F || scale > 1F) {
        scaler.setScale(scale, scale);
      } else {
        scaler = null;
      }
    } else {
      float scale = targetWidth / bitmapWidthF;
      if (scale < .9F || scale > 1F) {
        scaler.setScale(scale, scale);
      } else {
        scaler = null;
      }
    }

    Bitmap b1;
    if (scaler != null) {
      // used for minithumb and crop, so we want to filter here.
      b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
          source.getHeight(), scaler, true);
    } else {
      b1 = source;
    }

    int dx1 = Math.max(0, b1.getWidth() - targetWidth);
    int dy1 = Math.max(0, b1.getHeight() - targetHeight);

    Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
        targetHeight);

      if (recycle && !b1.equals(source)) {
          source.recycle();
      }

    if (!b2.equals(b1)) {
      if (recycle || !b1.equals(source)) {
        b1.recycle();
      }
    }

    return b2;
  }

  /**
   * stop thumbnail
   */
  public void stopThumbnail() {
    Log.d(TAG, "stopThumbnail call ");

    synchronized(this) {
        while (thumbLoadStart) {
          try {
            Log.d(TAG, "stopThumbnail  cancelLoad =  " + cancelLoad);
            cancelLoad = true;
            if (mHandler != null) {
              Log.i(TAG, "sendEmptyMessage RELEASE");
              mHandler.sendEmptyMessage(RELEASE);
            } else {
              if (mtkPlayer != null) {
                mtkPlayer.reset();
              }
            }

            if (thumbLoadStart) {
              wait();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        try {
            //Log.i(TAG, "release:+");
            if (null != retriever){
                retriever.release();
            }

            if (null != mtkPlayer){
                mtkPlayer.stop();
                mtkPlayer.release();
            }
            //Log.i(TAG, "release:-");
        } catch (RuntimeException ex) {
            // Ignore failures while cleaning up.
            Log.i(TAG, "RuntimeException ex");
        }
    }

    Log.d(TAG, "stopThumbnail done");
  }

  public boolean isLoadThumanil() {
    return thumbLoadStart;
  }

  public synchronized void reset() {
    if (mtkPlayer != null) {
      mtkPlayer.reset();
    }
  }

 public boolean isSupportLoadThumbnail(MediaMetadataRetriever retriever){
      String videoWidth = retriever.extractMetadata(
              MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
      String videoHeight = retriever.extractMetadata(
              MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
      Log.i(TAG, "isSupportLoadThumbnail,videoWidth=="+videoWidth+",videoHeight=="+videoHeight);

      //support video below 4K
      if(Integer.parseInt(videoHeight) <= ConstPhoto.MAX_4K_HEIGHT &&
              Integer.parseInt(videoWidth) <= ConstPhoto.MAX_4K_WIDTH) {
          return true;
      }

      boolean isVideoCodecSupported = true;
      try {
          isVideoCodecSupported =
              MediaCodecUtil.is8KVideoSupported(Integer.parseInt(videoWidth), Integer.parseInt(videoHeight));
      } catch (DecoderQueryException e) {
        Log.e(TAG, "Codec warning failed", e);
      }
      Log.i(TAG, "isSupportLoadThumbnail,isVideoCodecSupported=="+isVideoCodecSupported);

      return isVideoCodecSupported;
  }

}
