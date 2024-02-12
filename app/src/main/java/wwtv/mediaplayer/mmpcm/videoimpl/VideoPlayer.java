
package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

import com.mediatek.ExoMediaPlayer;
import com.mediatek.ExoMediaPlayer.LastMemoryFilePosition;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
//import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.twoworlds.tv.MtkTvVolCtrl;

public class VideoPlayer extends UIMediaPlayer {
  public static final int MEDIA_INFO_METADATA_COMPLETE = 0;
  public static final int MEDIA_INFO_START_INVALID_STATE = -1;
  public static final int MEDIA_INFO_PAUSE_INVALID_STATE = -2;
  public static final int MEDIA_INFO_STOP_INVALID_STATE = -3;
  public static final int MEDIA_INFO_SEEK_INVALID_STATE = -4;
  public static final int MEDIA_INFO_NOT_SUPPORT = -5;
  public static final int MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE = -100;
  public static final int MEDIA_INFO_VIDEO_SEEK_COMPLETEED = 1003;
  public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
  public static final int MEDIA_INFO_VIDEO_REPLAY_DONE = -5100;
  private static final String TAG = "VideoPlayer";
  private SurfaceHolder mSurfaceHolder = null;
  private String mCurrentPath = null;
  private int mPlayStatus = VideoConst.PLAY_STATUS_INITED;
  private int mPlayerMode = VideoConst.PLAYER_MODE_MMP;
  // private InputStream mInputStream;
  private Object mPreparedListener;
  private Object mErrorListener;
  private Object mBufferingUpdateListener;
  private Object mCompletionListener;
  private Object mInfoListener;
  private Object mSeekCompleteListener;
  private Object mTimedTextListener;
  private Object mVideoSizeChangedListener;
//  private Rect videoRect;
  private int mTmpPlayStatus = VideoConst.PLAY_STATUS_INITED;

  @SuppressWarnings("deprecation")
  public VideoPlayer(SurfaceView surfaceview, int playerMode) {
    super(playerMode);
    mPlayerMode = playerMode;
    Log.d(TAG, "enter VideoPlayer init, playermode:" + playerMode);
    if (mSurfaceHolder == null) {
      Log.d(TAG, "enter VideoPlayer init,mSurfaceHolder ==null");
    }
    mSurfaceHolder = surfaceview.getHolder();
    mSurfaceHolder.addCallback(mSHCallback);
    mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//    videoRect = null;
  }

  public VideoPlayer(int playerMode) {
    super(playerMode);
    Log.d(TAG, "enter VideoPlayer(int playerMode) , playermode:" + playerMode);
    mPlayerMode = playerMode;
    mTmpPlayStatus = VideoConst.PLAY_STATUS_INITED;
  }


    public void setSurfaceView(SurfaceView surfaceview){
        if (mSurfaceHolder == null) {
            Log.d(TAG, "enter VideoPlayer init,mSurfaceHolder ==null");
        }
        mSurfaceHolder = surfaceview.getHolder();
        mSurfaceHolder.addCallback(mSHCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

  /*
   * public void setRect(Rect rect){ videoRect = rect; }
   */

  public void setVideoDataSource(String path) {
    Log.d(TAG, "enter setDataSource, path:" + path);
    mCurrentPath = path;
//    notifyInfo(MEDIA_INFO_DATA_PREPAREED_STATE);
    openVideo();
    Log.d(TAG, "leave setDataSource, " + "mPlayStatus:" + mPlayStatus);
  }

  public void startVideo() {
    Log.d(TAG, "enter startVideo, mCurrentPath:" + mCurrentPath
        + "mPlayStatus:" + mPlayStatus);
    if (mPlayStatus >= VideoConst.PLAY_STATUS_PREPARED
        && mPlayStatus < VideoConst.PLAY_STATUS_STOPPED) {
      try {
        start();
        if (mPlayStatus == VideoConst.PLAY_STATUS_PREPARED) {
          mPlayStatus = VideoConst.PLAY_STATUS_STARTING;
        } else {
          mPlayStatus = VideoConst.PLAY_STATUS_STARTED;
        }

      } catch (IllegalStateException e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    }
    Log.d(TAG, "leave startVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  public void pauseVideo() {
    Log.d(TAG, "enter pauseVideo, mCurrentPath:" + mCurrentPath
        + "mPlayStatus:" + mPlayStatus);
    if (isInPlaybackState()) {
      if (isPlaying()) {
        try {
          pause();
//          if (mPlayStatus == VideoConst.PLAY_STATUS_SEEKING) {
//            mTmpPlayStatus = VideoConst.PLAY_STATUS_PAUSED;
//          }
          mPlayStatus = VideoConst.PLAY_STATUS_PAUSED;
        } catch (IllegalStateException e) {
          e.printStackTrace();
          throw new IllegalStateException(e);
        }
      }
    }
    Log.d(TAG, "leave pauseVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  public void stopVideo() {
    Log.d(TAG, "enter stopVideo, mCurrentPath:" + mCurrentPath
        + "mPlayStatus:" + mPlayStatus);
    CommonSet.getInstance(mContext)
    .setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LR);
    if (isInPlaybackState()) {
      try {
        stop();
        mPlayStatus = VideoConst.PLAY_STATUS_STOPPED;
      } catch (IllegalStateException e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    }
    Log.d(TAG, "leave stopVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  public void stopDrmVideo() {
    Log.d(TAG, "enter stopVideo, mCurrentPath:" + mCurrentPath
        + "mPlayStatus:" + mPlayStatus);
    CommonSet.getInstance(mContext)
    .setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LR);
    if (isInPlaybackState()) {
      try {
        stop();
      } catch (IllegalStateException e) {
        e.printStackTrace();
        throw new IllegalStateException(e);
      }
    }
    Log.d(TAG, "leave stopVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  /*
   * @Override public void step(int amount) { super.step(amount); //mPlayStatus =
   * VideoConst.PLAY_STATUS_STEP; }
   */

  public int setPlayModeEx(int speed, int playStatus) {
    int result = setPlayModeEx(speed);
    Log.i(TAG, "setPlayModeEx: result:" + result);
    if (result < 0 && result != UIMediaPlayer.IMTK_PB_ERROR_CODE_NEW_TRICK) {
      if (result == UIMediaPlayer.IMTK_PB_BUFFER_NOT_ENOUGH) {
        throw new IllegalStateException("BUFFER NOT ENOUGH");
      } else {
        throw new RuntimeException("MEDIA_INFO_NOT_SUPPORT");
      }

    } else {
      mPlayStatus = playStatus;
    }

    return result;
  }

  public void completVideo() {
    Log.d(TAG, "enter completVideo, " + "mPlayStatus:" + mPlayStatus);
    notifyInfo(MEDIA_INFO_METADATA_COMPLETE);
    // releaseVideo();
    Log.d(TAG, "leave completVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  public void notifyInfo(int what){
      if (null != mInfoListener){
        ((OnInfoListener) mInfoListener).onInfo(this.getPlayer(), what, what);

      }
  }

  public void releaseVideo() {
    Log.d(TAG, "enter releaseVideo, " + "mPlayStatus:" + mPlayStatus);
  // NOT SAMBA and HHTP , not use sub thread
    if ((getPlaySourceMode() != VideoConst.PLAYER_MODE_HTTP && getPlaySourceMode() != VideoConst.PLAYER_MODE_SAMBA)) {
      release();
      closeStream();
    } else {
      new Thread(new Runnable() {

        @Override
        public void run() {
          release();
          closeStream();
        }
      }).start();
    }
    mPreparedListener = null;
    mErrorListener = null;
    mBufferingUpdateListener = null;
    mCompletionListener = null;
    mInfoListener = null;
    mSeekCompleteListener = null;
    mTimedTextListener = null;
    mVideoSizeChangedListener = null;
    mCurrentPath = null;
    // setDisplay = false;
    mPlayStatus = VideoConst.PLAY_STATUS_INITED;
    Log.d(TAG, "leave releaseVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  public void resetVideo() {
    reset();
    mPlayStatus = VideoConst.PLAY_STATUS_INITED;
    CommonSet.getInstance(mContext)
    .setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LR);
    Log.d(TAG, "leave resetVideo, " + "mPlayStatus:" + mPlayStatus);
  }

  public String getCurrentPath() {
    return mCurrentPath;
  }

  public int getPlayStatus() {
    Log.d(TAG, "getPlayStatus,  mPlayStatus:" + mPlayStatus);
    return mPlayStatus;
  }

  public void setPlayStatus(int status) {
    mPlayStatus = status;
  }

  public void setPlaySourceMode(int mode) {
    Log.d(TAG, "setPlaySourceMode mode = " + mode);
    mPlayerMode = mode;
  }

  public int getPlaySourceMode() {
    return mPlayerMode;
  }

  public boolean isInPlaybackState() {
    Log.i(TAG, "mPlayStatus:" + mPlayStatus);
    return (mPlayStatus >= VideoConst.PLAY_STATUS_STARTED && mPlayStatus < VideoConst.PLAY_STATUS_STOPPED);
  }

  @Override
  public void seekTo(int arg0) throws IllegalStateException {

    Log.d(
        TAG,
        "seekTo arg0 = " + arg0 + "mPlayStatus = " + mPlayStatus + "curtime ="
            + System.currentTimeMillis());
    if (mPlayStatus >= VideoConst.PLAY_STATUS_STARTED &&
        mPlayStatus < VideoConst.PLAY_STATUS_SEEKING) {

      try {
        super.seekTo(arg0);
        mTmpPlayStatus = mPlayStatus;
        mPlayStatus = VideoConst.PLAY_STATUS_SEEKING;

      } catch (IllegalStateException ex) {
        Log.d(TAG, "seekTo ex = " + ex);
        throw ex;

      }

    }

  }

  public void seekToLastMemory(int arg0) throws IllegalStateException {

    Log.d(
        TAG,
        "seekTo arg0 = " + arg0 + "mPlayStatus = " + mPlayStatus + "curtime ="
            + System.currentTimeMillis());

    try {
      super.seekTo(arg0);
    } catch (IllegalStateException ex) {
      Log.d(TAG, "seekTo ex = " + ex);
      throw ex;

    }

  }

  public int getProgress() {

    int position = 0;
    if (isInPlaybackState()) {
      position = super.getCurrentPosition();
    }
    Log.d(TAG, "getProgress ~position = " + position);
    return position;
  }

  public int getBytePosition() {

    int position = 0;
    if (isInPlaybackState()) {
      position = super.getCurrentBytePosition();

    }
    Log.e(TAG, "getBytePosition pos = " + position);
    return position;

  }

  @Override
  public boolean step(int amount) throws IllegalStateException {

    Log.d(
        TAG,
        "step amount = " + amount + "mPlayStatus = " + mPlayStatus + "curtime = "
            + System.currentTimeMillis());
    boolean stepSuccess = false;
    if (mPlayStatus == VideoConst.PLAY_STATUS_PAUSED) {

      try {
        stepSuccess = super.step(amount);
        if (stepSuccess){
            mTmpPlayStatus = mPlayStatus;
            mPlayStatus = VideoConst.PLAY_STATUS_STEP;
        }
      } catch (IllegalStateException ex) {

        Log.d(TAG, "step ex = " + ex);

        throw ex;
      }

    }

    return stepSuccess;

  }

  @Override
  public boolean canDoSeek() {
    boolean res = false;
    if (isInPlaybackState()) {

      res = super.canDoSeek();

    }
    return res;
  }

  @Override
  public boolean canDoTrick(int speed) {
    boolean res = false;
    if (isInPlaybackState()) {

      res = super.canDoTrick(speed);

    }
    return res;
  }

  @Override
  public int getDuration() {
    Log.d(TAG, "getDuration ~mPlayStatus:" + mPlayStatus);
    int dur = 0;
    if (mPlayStatus >= VideoConst.PLAY_STATUS_PREPARED) {
      dur = super.getDuration();
    }
    Log.d(TAG, "getDuration ~dur = " + dur);
    return dur;
  }

  public void setMediaPlayerListener() {
    if (mPlayerMode == VideoConst.PLAYER_MODE_MMP ||
        Util.isUseExoPlayer()){
      setOnPreparedListener(mOnPreparedListener);
      setOnCompletionListener(mCompletionListener);
      setOnBufferingUpdateListener(mBufferingUpdateListener);
      setOnErrorListener(mOnErrorListener);
      setOnInfoListener(mOnInfoListener);
      setOnVideoSizeChangedListener(mVideoSizeChangedListener);
      setOnSeekCompleteListener(mOnSeekCompletionListener);
      setOnTimedTextListener(mTimedTextListener);
    } else {
      setOnPreparedListener(mtkOnPreparedListener);
      setOnCompletionListener(mCompletionListener);
      setOnBufferingUpdateListener(mBufferingUpdateListener);
      setOnErrorListener(mtkOnErrorListener);
      setOnInfoListener(mtkOnInfoListener);
      setOnVideoSizeChangedListener(mVideoSizeChangedListener);
      setOnSeekCompleteListener(mtkSeekCompletionListener);
      setOnTimedTextListener(mTimedTextListener);
    }
  }

  private void openVideo() {
    Log.d(TAG, "enter openVideo, " + "mPlayStatus:" + mPlayStatus + "  mPlayerMode:" + mPlayerMode
        + "  mCompletionListener:" + mCompletionListener);
    if (mCurrentPath == null) {
      Log.e("TAG", "openVideo mCurrentPath:" + mCurrentPath + "mSurfaceHolder:  "
          + (mSurfaceHolder == null));
      return;
    }

    if (mCompletionListener != null){
        setMediaPlayerListener();
    }

    try {
      setDataSource(mCurrentPath, mContext);

       if (mPlayerMode == VideoConst.PLAYER_MODE_MMP || mPlayerMode == VideoConst.PLAYER_MODE_DLNA) {
          setSubtitleDataSource(mCurrentPath);
       }

//       setPlayerRole(PlayerRole.ROLE_VIDEO_PLAYBACK);

      setScreenOnWhilePlaying(true);

      Log.i(TAG, "divx support test");

      if (mPreviewMode) {
        Log.i(TAG, "mPreviewMode true:" + mPreviewMode + "  mSurfaceHolder:"
            + mSurfaceHolder.getSurfaceFrame());

      } else {
        Log.i(TAG, "mPreviewMode false:" + mPreviewMode);
      }
      LastMemory.recoveryLastMemoryInfo(mContext, LastMemory.LASTMEMORY_TIME);
      LastMemory.recoveryLastMemoryInfo(mContext, LastMemory.LASTMEMORY_POSITION);
      prepareAsync();
      mPlayStatus = VideoConst.PLAY_STATUS_PREPAREING;
    }
    catch (Exception e) {
        mPlayStatus = VideoConst.PLAY_STATUS_ERROR;
        e.printStackTrace();
        Log.d(TAG, "enter openVideo, " + "file not support error to ap " + "mErrorListener " + mErrorListener);
    }
    Log.d(TAG, "leave openVideo, " + "mPlayStatus:" + mPlayStatus + "getCurrentPosition:" + getCurrentPosition());
  }

  public void setPreparedListener(Object l) {
    mPreparedListener = l;
  }

  public void setErrorListener(Object l) {
    mErrorListener = l;
  }

  public void setBufferingUpdateListener(Object l) {
    mBufferingUpdateListener = l;
  }

  public void setCompletionListener(Object l) {
    mCompletionListener = l;
  }

  public void setVideoSizeChangedListener(Object l) {
    mVideoSizeChangedListener = l;
  }

  public void setInfoListener(Object l) {
    mInfoListener = l;
  }

  public void setSeekCompleteListener(Object l) {
    mSeekCompleteListener = l;
  }

  public void setTimedTextListener(Object l) {
    mTimedTextListener = l;
  }

  // private boolean setDisplay = false;
  private final OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

    @Override
    public void onPrepared(MediaPlayer mp) {
      handlePrepare();
    }
  };

  private final OnErrorListener mOnErrorListener = new OnErrorListener() {

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      return handleErr(what, extra);
    }
  };
  private final OnInfoListener mOnInfoListener = new OnInfoListener() {

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
      Log.d(TAG, "enter onInfo, " + "mPlayStatus:" + mPlayStatus
          + "what:" + what);
      return handleInfo(what, extra);
    }
  };

  private final OnSeekCompleteListener mOnSeekCompletionListener = new OnSeekCompleteListener() {
    @Override
    public void onSeekComplete(MediaPlayer arg0) {
      handleSeekComplete();
    }
  };

  private final ExoMediaPlayer.OnPreparedListener mtkOnPreparedListener = new ExoMediaPlayer.OnPreparedListener() {
    @Override
    public void onPrepared(MediaPlayer mp) {
      handlePrepare();
    }
  };

  private final ExoMediaPlayer.OnErrorListener mtkOnErrorListener = new ExoMediaPlayer.OnErrorListener() {

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      return false;
    }
  };
  private final ExoMediaPlayer.OnInfoListener mtkOnInfoListener = new ExoMediaPlayer.OnInfoListener() {

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
      Log.d(TAG, "enter onInfo, " + "mPlayStatus:" + mPlayStatus + "what:" + what);
      return handleInfo(what, extra);
    }
  };

  private final ExoMediaPlayer.OnSeekCompleteListener mtkSeekCompletionListener = new ExoMediaPlayer.OnSeekCompleteListener() {

    @Override
    public void onSeekComplete(MediaPlayer mp) {
      handleSeekComplete();
    }
  };

  private void handlePrepare() {
    mPlayStatus = VideoConst.PLAY_STATUS_PREPARED;
    Log.d(TAG, "enter onPrepared~~~ mSurfaceHolder =" + mSurfaceHolder);
    if (mSurfaceHolder != null) {
      Log.d(TAG, "enter onPrepared~~~ mSurfaceHolder!=null mSurfaceHolder.getSurfaceFrame()= "
          + mSurfaceHolder.getSurfaceFrame());
//      if (!mPreviewMode) {
        try {
          setDisplay(mSurfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }
//      }
    }
    Log.d(TAG, "enter onPrepared~~~ ");
    if (null != mPreparedListener) {
      ((OnPreparedListener) mPreparedListener).onPrepared(getPlayer());
    }
  }

  public void startVideoFromDrm() {
    try {
      Log.d(TAG, "enter startVideoFromDrm, " + "mPlayStatus:" + mPlayStatus);
      mPlayStatus = VideoConst.PLAY_STATUS_PREPARED;
      startVideo();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void handleSeekComplete() {
    Log.d(TAG, "handleSeekComplete mTmpPlayStatus = " + mTmpPlayStatus + "mPlayStatusb ="
        + mPlayStatus + "curtime = " + System.currentTimeMillis());

    if (mTmpPlayStatus != VideoConst.PLAY_STATUS_INITED) {
      mPlayStatus = mTmpPlayStatus;
      if (mPlayStatus == VideoConst.PLAY_STATUS_STARTED && !isPlaying()) {
        mPlayStatus = VideoConst.PLAY_STATUS_PAUSED;
      }
      mTmpPlayStatus = VideoConst.PLAY_STATUS_INITED;
    } else {
      if (mPlayStatus != VideoConst.PLAY_STATUS_PAUSED) {
        mPlayStatus = VideoConst.PLAY_STATUS_STARTED;
        mTmpPlayStatus = VideoConst.PLAY_STATUS_INITED;
      }
    }

    if (null != mSeekCompleteListener) {
      ((OnSeekCompleteListener) mSeekCompleteListener).onSeekComplete(getPlayer());
    }
  }

  private boolean handleErr(int what, int extra) {
    mPlayStatus = VideoConst.PLAY_STATUS_ERROR;
    Log.d(TAG, "enter onError, " + "mPlayStatus:" + mPlayStatus);
    if (null != mErrorListener) {
      return ((OnErrorListener) mErrorListener).onError(getPlayer(), what, extra);
    }
    return false;
  }

  private boolean handleInfo(int what, int extra) {
    Log.d(TAG, "handleInfo enter onInfo what:" + what);
    switch (what) {
      case MEDIA_INFO_VIDEO_SEEK_COMPLETEED:
        Log.d(TAG,
            "enter onInfo:MEDIA_INFO_VIDEO_SEEK_COMPLETEED mTmpPlayStatus = " + mTmpPlayStatus
                + "mPlayStatus = " + mPlayStatus + "curtime = " + System.currentTimeMillis());
        if (mTmpPlayStatus != VideoConst.PLAY_STATUS_INITED) {
          mPlayStatus = mTmpPlayStatus;
          mTmpPlayStatus = VideoConst.PLAY_STATUS_INITED;
        } else {
          mPlayStatus = VideoConst.PLAY_STATUS_STARTED;
          mTmpPlayStatus = VideoConst.PLAY_STATUS_INITED;
        }
        break;
      case MEDIA_INFO_VIDEO_RENDERING_START:
      case MEDIA_INFO_VIDEO_REPLAY_DONE:
        Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
        Log.d(TAG, "MEDIA_INFO_VIDEO_REPLAY_DONE");
        mPlayStatus = VideoConst.PLAY_STATUS_STARTED;
        break;
      default:
        break;
    }
    if (null != mInfoListener) {
      if((Util.isUseExoPlayer())) {
//        if (VideoManager.MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_TITLE_CHANGE == what && DivxUtil.getChapterChanged()) {
////          DivxUtil.setChapterChanged(false);
//          mPlayStatus = VideoConst.PLAY_STATUS_STARTED;
//        }
        return ((OnInfoListener) mInfoListener).onInfo(getPlayer(), what, extra);
      }
    }
    return false;

  }

  SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      Log.d(TAG, "enter surfaceCreated, " + "mPlayStatus:" + mPlayStatus);
      try {
        setDisplay(mSurfaceHolder);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      Log.d(TAG, "SurfaceView ~~:" + mSurfaceHolder.getSurfaceFrame());
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      Log.d(TAG, "enter surfaceChanged, " + "width:" + width + "height:" + height + "  holder:" + holder.getSurfaceFrame());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      Log.d(TAG, "enter surfaceDestroyed, ");
      mSurfaceHolder.removeCallback(mSHCallback);
      mSurfaceHolder = null;
    }
  };

  public long getDivxLastMemoryFileID() {
    if (null != getPlayer()) {
      MtkLog.i(TAG, "null != getPlayer(),getDivxLastMemoryFileID");
      return getPlayer().getLastMemoryFileID();
    } else {
      return -1;
    }
  }

  public LastMemoryFilePosition getLastMemoryFilePositionExo() {
      if (null != getPlayer()) {
        MtkLog.i(TAG, "null != getExoPlayer, getLastMemoryFilePositionExo");
        return getPlayer().getLastMemoryFilePosition();
      } else {
        return null;
      }
  }

  public int setLastMemoryFilePositionExo(LastMemoryFilePosition info) {
      if (null != getPlayer()) {
        MtkLog.i(TAG, "null != getExoPlayer, setLastMemoryFilePositionExo:");
        return ((com.mediatek.ExoMediaPlayer) getPlayer()).setLastMemoryFilePosition(info);
      } else {
        return -1;
      }
  }

  public void setExternalSubtitleEncodingType(String encoding) {
      if (null != getPlayer()) {
        MtkLog.i(TAG, "null != getExoPlayer, setExternalSubtitleEncodingType:");
        ((com.mediatek.ExoMediaPlayer) getPlayer()).setExternalSubtitleCharsetEncoding(encoding);
      }
  }

  private Context mContext;

  public void setContext(Context context) {
    // TODO Auto-generated method stub
    mContext = context;
  }

  boolean mPreviewMode = false;

  public void setPreviewMode(boolean previewMode) {
    // TODO Auto-generated method stub
    mPreviewMode = previewMode;
  }

}
