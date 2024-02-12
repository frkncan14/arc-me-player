
package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import android.util.Log;
import android.view.SurfaceView;
import android.content.Context;
import android.media.PlaybackParams;

import com.google.android.exoplayer.ExoPlayer;

import com.mediatek.ExoMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmp.util.MetaDataInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.VideoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.ExoMediaPlayer.LastMemoryFilePosition;



public class VideoManager {
  private static final String TAG = "VideoManager";
  /**
   * Unspecified media player info.
   */
  public static final int MEDIA_INFO_UNKNOWN = ExoMediaPlayer.MEDIA_INFO_UNKNOWN;

  /**
   * The video is too complex for the decoder: it can't decode frames fast
   * enough. Possibly only the audio plays fine at this stage.
   */
  public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = ExoMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING;

  /**
   *  is temporarily pausing playback internally in order to
   * buffer more data.
   */
  public static final int MEDIA_INFO_BUFFERING_START = ExoMediaPlayer.MEDIA_INFO_BUFFERING_START;

  /**
   *  is resuming playback after filling buffers.
   */
  public static final int MEDIA_INFO_BUFFERING_END = ExoMediaPlayer.MEDIA_INFO_BUFFERING_END;

  /**
   * Bad interleaving means that a media has been improperly interleaved or
   * not interleaved at all, e.g has all the video samples first then all the
   * audio ones. Video is playing but a lot of disk seeks may be happening.
   */
  public static final int MEDIA_INFO_BAD_INTERLEAVING = ExoMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING;

  /** The media cannot be seeked (e.g live stream) */
  public static final int MEDIA_INFO_NOT_SEEKABLE = ExoMediaPlayer.MEDIA_INFO_NOT_SEEKABLE;

  /** A new set of metadata is available. */
  public static final int MEDIA_INFO_METADATA_UPDATE = ExoMediaPlayer.MEDIA_INFO_METADATA_UPDATE;

  public static final int MEDIA_INFO_METADATA_COMPLETE = VideoPlayer.MEDIA_INFO_METADATA_COMPLETE;

  public static final int MTK_MEDIA_INFO_METADATA_UPDATE = ExoMediaPlayer.MEDIA_INFO_METADATA_UPDATE;

  public static final int MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT = ExoMediaPlayer.MEDIA_INFO_VIDEO_ENCODE_FORMAT_UNSUPPORT;
  public static final int MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT = ExoMediaPlayer.MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT;
  public static final int MEDIA_ERROR_FILE_NOT_SUPPORT = ExoMediaPlayer.MEDIA_ERROR_FILE_NOT_SUPPORT;
  public static final int MEDIA_ERROR_FILE_CORRUPT = ExoMediaPlayer.MEDIA_ERROR_FILE_CORRUPT;
  public static final int MEDIA_ERROR_RESOURCE_INTERRUPT = 4000; //Align with cmpb/customer_def.h
  public static final int MEDIA_ERROR_BT_MODE_NOT_SUPPORT = ExoMediaPlayer.MEDIA_ERROR_BT_MODE_NOT_SUPPORT;

  public static final int MEDIA_INFO_POSITION_UPDATE = ExoMediaPlayer.MEDIA_INFO_POSITION_UPDATE;

  public static final int MEDIA_INFO_START_INVALID_STATE
   = VideoPlayer.MEDIA_INFO_START_INVALID_STATE;
  public static final int MEDIA_INFO_PAUSE_INVALID_STATE
   = VideoPlayer.MEDIA_INFO_PAUSE_INVALID_STATE;
  public static final int MEDIA_INFO_STOP_INVALID_STATE = VideoPlayer.MEDIA_INFO_STOP_INVALID_STATE;
  public static final int MEDIA_INFO_SEEK_INVALID_STATE = VideoPlayer.MEDIA_INFO_SEEK_INVALID_STATE;
  public static final int MEDIA_INFO_NOT_SUPPORT = VideoPlayer.MEDIA_INFO_NOT_SUPPORT;
  public static final int MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE = VideoPlayer.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE;
  public static final int MEDIA_INFO_AUDIO_ONLY_SERVICE = ExoMediaPlayer.MEDIA_INFO_AUDIO_ONLY_SERVICE;

  public static final int MEDIA_INFO_VIDEO_ONLY_SERVICE = -5009;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_AUDIO_VIDEO_SERVICE = -5010;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_AUDIO_CLEAR_VIDEO_SERVICE = -5011;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_AUDIO_NO_VIDEO_SERVICE = -5012;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_CLEAR_AUDIO_SERVICE = -5013;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_NO_AUDIO_SERVICE = -5014;
  public static final int MTK_MEDIA_INFO_MEDIA_LOST = -5015;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_CHAPTER_CHANGE = 1006;
  public static final int MTK_MEDIA_INFO_SCRAMBLED_VIDEO_DRM_TITLE_CHANGE = 1007;
  public static final int MTK_MEDIA_INFO_VID_INFO_UPDATE = 1004;
  // public static final int MTK_MEDIA_INFO_PLAY_DONE = 1006;
  public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
  public static final int MEDIA_INFO_VIDEO_LOCKED = 2001;
  public static final int MEDIA_INFO_VIDEO_RATING_LOCKED = 1008;
  public static final int MTK_MEDIA_INFO_AB_REPEAT_BEGIN = 1009;
  public static final int MTK_MEDIA_INFO_AB_REPEAT_END = 1010;
  public static final int MTK_MEDIA_INFO_MEDIA_INFO_CHG = 1011;
  public static final int MTK_MEDIA_INFO_MEDIA_TYPE_FORMATE = 1012;
  public static final int MTK_MEDIA_INFO_TS_VIDEO_NUM_RDY = 1013;
  public static final int MTK_MEDIA_INFO_DUR_UPDATE = 1014;
  public static final int MTK_MEDIA_INFO_ON_CUES = ExoMediaPlayer.MTK_MEDIA_INFO_ON_CUES;
  public static final int MEDIA_INFO_ON_REPLAY = ExoMediaPlayer.MEDIA_INFO_ON_REPLAY;
  public static final int MEDIA_INFO_VIDEO_REPLAY_DONE =
      VideoPlayer.MEDIA_INFO_VIDEO_REPLAY_DONE;

  private static VideoManager mVideoManager = null;
  private VideoPlayer mPlayer = null;
  private PlayList mPlayList = null;

  private VideoFile mVideoFile;
  private boolean mPreviewMode = false;
  private int mSpeedStep;
  private int mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_1X;
//  private int subTitleNum = -1;
  public int mFormateType;
  public int mTSVideoNum;
  public int mCurrentTSVideoIndex;

  private VideoManager() {

  }

  /**
   *
   * @param context
   * @param surfaceview
   * @param playerMode
   *            VideoConst.PLAYER_MODE_MMP or VideoConst.PLAYER_MODE_NET
   * @return
   */
  public static synchronized VideoManager getInstance(
      SurfaceView surfaceview, int playerMode) {

    if (mVideoManager == null) {
      MtkLog.i(TAG, "mVideoManager==NULL");
      if (surfaceview != null){
          mVideoManager = new VideoManager(surfaceview, playerMode);
      } else {
          mVideoManager = new VideoManager(playerMode);
      }
    } else {
      MtkLog.i(TAG, "mVideoManager!=NULL");
    }
    return mVideoManager;
  }

  private VideoManager(SurfaceView surfaceview, int playerMode) {
    Log.d(TAG, "VideoManager:");
    if (surfaceview != null) {
      mPlayer = new VideoPlayer(surfaceview, playerMode);
      Log.d(TAG, "new mPlayer: mPlayer:" + mPlayer);
    }
    if (playerMode != VideoConst.PLAYER_MODE_HTTP) {
      mPlayList = PlayList.getPlayList();
    }
  }

  public VideoManager(int playerMode) {
    Log.d(TAG, "VideoManager:");
    mPlayer = new VideoPlayer(playerMode);
    // mPlayer.setPlayMode(playerMode);
    Log.d(TAG, "new mPlayer:");
    if (playerMode != VideoConst.PLAYER_MODE_HTTP) {
      mPlayList = PlayList.getPlayList();
    }
  }

  public void setMediaPlayerListener() {
    if (null != mPlayer) {
      mPlayer.setMediaPlayerListener();
    }
  }

  public void setSurfaceView(SurfaceView surface){
    if (null != mPlayer) {
      mPlayer.setSurfaceView(surface);
    }
  }

  public void onRelease() {
//    Log.d(TAG, "onRelease:" + Log.getStackTraceString(new Throwable()));
    if (null != mPlayer) {
      mPlayer.releaseVideo();
    }
    synchronized(VideoPlayer.class) {
      mPlayer = null;
    }
    synchronized(VideoManager.class) {
      mVideoManager = null;
    }
  }

  // //add by hs_binyan
  /**
   * Register a callback to be invoked when the media source is ready for
   * playback.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnPreparedListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setPreparedListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when the end of a media source has been
   * reached during playback.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnCompletionListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setCompletionListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when the status of a network stream's
   * buffer has changed.
   *
   * @param listener
   *            the callback that will be run.
   */
  public void setOnBufferingUpdateListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setBufferingUpdateListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when a seek operation has been
   * completed.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnSeekCompleteListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setSeekCompleteListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when the video size is known or
   * updated.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnVideoSizeChangedListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setVideoSizeChangedListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when a timed text is available for
   * display.
   *
   * @param listener
   *            the callback that will be run {@hide}
   */
  public void setOnTimedTextListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setTimedTextListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when an info/warning is available.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnInfoListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setInfoListener(listener);
    }
  }

  /**
   * Register a callback to be invoked when an error has happened during an
   * asynchronous operation.
   *
   * @param listener
   *            the callback that will be run
   */
  public void setOnErrorListener(Object listener) {
    if (null != mPlayer) {
      mPlayer.setErrorListener(listener);
    }
  }

  public void setDataSource(final String path) {
    if (null != mPlayer) {
      Log.d(TAG, "setDataSource: null != mPlayer path:" + path);
      mPlayer.notifyInfo(MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE);
      //exo, NOT SAMBA and HHTP , not use sub thread
      if (Util.mIsUseEXOPlayer
          || (mPlayer.getPlaySourceMode() != VideoConst.PLAYER_MODE_HTTP
                && mPlayer.getPlaySourceMode() != VideoConst.PLAYER_MODE_SAMBA)) {
        mTSVideoNum = 0;
        mCurrentTSVideoIndex = 0;
        mPlayer.setVideoDataSource(path);
//      subTitleNum = -1;
        mSpeedStep = 1;
        mMetaData = null;
      } else {
        new Thread(new Runnable() {

          @Override
          public void run() {
            mTSVideoNum = 0;
            mCurrentTSVideoIndex = 0;
            mPlayer.setVideoDataSource(path);
//        subTitleNum = -1;
            mSpeedStep = 1;
            mMetaData = null;
          }
        }).start();
      }
    }
  }

  /**
   * play or pause video
   */
  public void startVideo() {
    Log.d(TAG, "startVideo startxx:");
    //Log.d(TAG, "startVideo startxx:" + Log.getStackTraceString(new Throwable()));
    if (null != mPlayer) {
      try {
        int platstatus = mPlayer.getPlayStatus();
        Log.d(TAG, "startVideo start:" + platstatus);
        if (platstatus == VideoConst.PLAY_STATUS_PAUSED) {
          mPlayer.startVideo();
        } else if (platstatus == VideoConst.PLAY_STATUS_FF
            || platstatus == VideoConst.PLAY_STATUS_FR
            || platstatus == VideoConst.PLAY_STATUS_SF
            || platstatus == VideoConst.PLAY_STATUS_SR) {
          setNormalSpeed();
//          mPlayer.startVideo();
        } else if (platstatus == VideoConst.PLAY_STATUS_STOPPED) {
          setDataSource(mPlayer.getCurrentPath());
        } else if (platstatus == VideoConst.PLAY_STATUS_STARTED
            || platstatus == VideoConst.PLAY_STATUS_PREPARED) {
          MmpTool.logDbg("Has played or prepared!");

        }
        mSpeedStep = 1;
        mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_1X;

      } catch (Exception ex) {
        Log.d(TAG, "startVideo exce Exception ex = " + ex);
        throw new IllegalStateException(ex);

      }
    }
  }

  public void pauseVideoWhenPressStop() {
    if (null != mPlayer) {
      try {
        int platstatus = mPlayer.getPlayStatus();
        Log.d(TAG, "pauseVideoWhenPressStop start:" + platstatus);
        if (platstatus == VideoConst.PLAY_STATUS_FF
            || platstatus == VideoConst.PLAY_STATUS_FR
            || platstatus == VideoConst.PLAY_STATUS_SF
            || platstatus == VideoConst.PLAY_STATUS_SR) {
            setNormalSpeed();
        }
        mSpeedStep = 1;
        mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_1X;

      } catch (Exception ex) {
        Log.d(TAG, "pauseVideoWhenPressStop exce Exception ex = " + ex);
        throw new IllegalStateException(ex);

      }
    }
  }

  public void setNormalSpeed() {
    Log.d(TAG, "setNormalSpeed");
    try {
      mPlayer.setPlayModeEx(ExoPlayer.RENDERER_PLAYERSPEED_1X, VideoConst.PLAY_STATUS_STARTED);
      mSpeedStep = 1;
      mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_1X;
    } catch (RuntimeException ex) {
      Log.d(TAG, "setNormalSpeed exce setPlayModeEx ex " + ex);
      throw ex;
    }
  }

  public void pauseVideo() {
    if (null != mPlayer) {

      if (mPlayer.getPlaySourceMode() == VideoConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(mPlayer.getCurrentPath());
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canPause()) {
            throw new IllegalStateException();
          }
        }
      }

      try {
        //new Exception("pauseVideo").printStackTrace();
        mPlayer.pauseVideo();
        mSpeedStep = 1;
        mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_1X;

        MmpTool.logInfo("VideoManager.java pauseVideo");
      } catch (Exception ex) {
        Log.d(TAG, "VideoManager.java pauseVideo exce Exception ex =" + ex);
        throw new IllegalStateException(ex);
      }

    }
  }

  /**
   * Set Seamless Mode
   */
  public void setSeamlessMode(boolean seamlessMode)
  {
    try {
      mPlayer.setSeamlessMode(seamlessMode);
    } catch (RuntimeException ex) {
      Log.d(TAG, "setSeamlessMode ex " + ex);
      throw ex;
    }
  }

  /**
   * Get Seamless Mode Setting
   */
  public boolean getSeamlessMode()
  {
    return mPlayer.getSeamlessMode();
  }

  public void reset() {
    if (null != mPlayer) {

      mPlayer.resetVideo();

    }

  }

  public boolean isInPlaybackState() {

    return mPlayer.isInPlaybackState();
  }

  public void stopVideo() {
    if (null != mPlayer) {

      mPlayer.stopVideo();

    }
  }

  public void stopDrmVideo() {
    if (null != mPlayer) {
      mPlayer.stopDrmVideo();
    }
  }

  /**
   * @return true if currently playing, false otherwise
   */
  public boolean isPlaying() {
    if (null != mPlayer) {
      Log.d(TAG, "isPlaying");
      return mPlayer.isPlaying();
    } else {
      Log.d(TAG, "mPlayer == null");
    }
    return false;
  }

  public void seek(int msec) {
    if (null != mPlayer) {

      if (msec < getDuration()) {
        mPlayer.seekTo(msec);
      } else {
        Log.d(TAG, "msec > getDuration() msec:" + msec + "--getDuration:" + getDuration());
      }

    } else {
      Log.d(TAG, "mPlayer == null");
    }
  }

  public void seekLastMemory(int msec) {
    if (null != mPlayer) {
      mPlayer.seekToLastMemory(msec);
    } else {
      Log.d(TAG, "mPlayer == null");
    }
  }

  public boolean canDoSeek() {
    if (null != mPlayer) {
      return mPlayer.canDoSeek();

    }
    return false;
  }

  public boolean canDoTrick() {
    if (null != mPlayer) {

      return mPlayer.canDoTrick(mPlayerSpeed);

    }
    return false;
  }

  // manualNext
  public void playNext() {
    play(true);
  }

  // manualPrev
  public void playPrev() {
    play(false);
  }

  public void autoNext() {
    String path = null;
    if (mPreviewMode) {
      path = mPlayer.getCurrentPath();
    } else {

      path = mPlayList.getNext(Const.FILTER_VIDEO, Const.AUTOPLAY);
    }
    Log.d(TAG, "autoNext  path = " + path);

    if (path == null) {
      mPlayer.completVideo();
      return;
    }
    reset();
    setDataSource(path);
  }

  public void replay() {
    if (null != mPlayer) {
      int platstatus = mPlayer.getPlayStatus();
      if (platstatus >= VideoConst.PLAY_STATUS_STARTED
          && platstatus < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.stopVideo();
      }
      String path = mPlayer.getCurrentPath();
      setDataSource(path);
    }
  }

  public long getFileSize() {
    if (mPlayer != null) {
      return mPlayer.getSourceSize();
    }
    return 0;
  }

  public String getFileDate() {
    if (mPlayer != null) {
      return mPlayer.getFileDate();
    }
    return null;
  }

  public String getFileBitRate() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getBitrate() / 1000 + " kb/s";
    }
    return "";
  }

  public int getBytePosition() {

    if (mPlayer != null) {

      return mPlayer.getCurrentBytePosition();

    }
    return 0;

  }

  /**
   * get progress
   */
  public int getProgress() {

    if (mPlayer != null) {

      return mPlayer.getCurrentPosition();

    }
    return 0;
  }

  public void fastForward() {
    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        MtkLog.i(TAG, "!canFastOrSlow()");
        throw new IllegalStateException();
      }
      if (mPlayer.getPlaySourceMode() == VideoConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(mPlayer.getCurrentPath());
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canSeek()) {
            MtkLog.i(TAG, "!dlnaDataSource.getContent().canSeek()");
            throw new IllegalStateException();
          }
        }
      }

      int tmpSpeedStep = mSpeedStep;
      int tmpPlayerSpeed = mPlayerSpeed;

      Log.d(TAG, "fastForward mPlayer.getPlayStatus():" + mPlayer.getPlayStatus());
      Log.d(TAG, "fastForward mSpeedStep:" + mSpeedStep);
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_FF:
            mSpeedStep <<= 1;
            if (mSpeedStep > 32) {
              startVideo();
            } else {
              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_2X;
                  break;
                case 4:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_4X;
                  break;
                case 8:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_8X;
                  break;
                case 16:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_16X;
                  break;
                case 32:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_32X;
                  break;
                default:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_2X;
                  break;
              }
              int result = mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_FF);

              if (result == UIMediaPlayer.IMTK_PB_ERROR_CODE_NEW_TRICK) {
                startVideo();
              }

              Log.d(TAG, "fastForward result = " + result);
            }
            break;

          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FR:
          case VideoConst.PLAY_STATUS_SF:
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep = 2;
            mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FF_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_FF);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {

        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "fastForward exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }
  }

  public void fastRewind() {

    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        throw new IllegalStateException();
      }
      if (mPlayer.getPlaySourceMode() == VideoConst.PLAYER_MODE_DLNA) {
        DLNADataSource dlnaDataSource = DLNAManager.getInstance()
            .getDLNADataSource(mPlayer.getCurrentPath());
        if (dlnaDataSource != null) {
          if (!dlnaDataSource.getContent().canSeek()) {
            throw new IllegalStateException();
          }
        }
      }
      int tmpSpeedStep = mSpeedStep;
      int tmpPlayerSpeed = mPlayerSpeed;
      Log.d(TAG, "fastRewind mPlayer.getPlayStatus():" + mPlayer.getPlayStatus());
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_FR:
            mSpeedStep <<= 1;
            Log.d(TAG, "mSpeedStep:" + mSpeedStep);
            if (mSpeedStep > 32) {
              startVideo();
            } else {

              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_2X;
                  break;
                case 4:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_4X;
                  break;
                case 8:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_8X;
                  break;
                case 16:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_16X;
                  break;
                case 32:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_32X;
                  break;
                /*
                 * case 64: mPlayerSpeed = PlayerSpeed.SPEED_FR_64X; break;
                 */
                default:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_2X;
                  break;
              }
              mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_FR);

            }
            break;
          /*
           * case VideoConst.PLAY_STATUS_STEP: mSpeedStep = 1; mPlayerSpeed = PlayerSpeed.SPEED_1X;
           * mPlayer.startVideo(); break;
           */
          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FF:
          case VideoConst.PLAY_STATUS_SF:
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep = 2;
            mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_FR_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_FR);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {
        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "fastRewind exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }
  }

  public void setPlaybackParams(PlaybackParams para) {
    mPlayer.setPlaybackParams(para);
  }

  public PlaybackParams getPlaybackParams() {
    return mPlayer.getPlaybackParams();
  }

  public boolean isNormalSpeed() {
    boolean isNormal = false;
    if (mPlayerSpeed == ExoPlayer.RENDERER_PLAYERSPEED_1X) {
      isNormal = true;
    }
    Log.i(TAG, "isNormal:" + isNormal);
    return isNormal;
  }

  public void resetReplay() {
    mSpeedStep = 1;
    mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_1X;
  }

  public void slowForward() {

    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        throw new IllegalStateException();
      }

      int tmpSpeedStep = mSpeedStep;
      int tmpPlayerSpeed = mPlayerSpeed;
      Log.d(TAG, "slowForward mPlayer.getPlayStatus() " + mPlayer.getPlayStatus());
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_SF:
            mSpeedStep <<= 1;
            if (mSpeedStep > 32) {
              startVideo();
            } else {
              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_2X;
                  break;
                case 4:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_4X;
                  break;
                case 8:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_8X;
                  break;
                case 16:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_16X;
                  break;
                case 32:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_32X;
                  break;
                default:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_2X;
                  break;
              }
              mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_SF);
            }
            break;

          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FF:
          case VideoConst.PLAY_STATUS_FR:
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep = 2;
            mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SF_1_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_SF);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {
        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "slowForward exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }

  }

  public void slowRewind() {

    if (null != mPlayer) {
      if (!canFastOrSlow()) {
        throw new IllegalStateException();
      }

      int tmpSpeedStep = mSpeedStep;
      int tmpPlayerSpeed = mPlayerSpeed;
      Log.d(TAG, "slowRewind mPlayer.getPlayStatus() " + mPlayer.getPlayStatus());
      try {
        switch (mPlayer.getPlayStatus()) {
          case VideoConst.PLAY_STATUS_SR:
            mSpeedStep <<= 1;
            if (mSpeedStep > 32) {
              startVideo();
            } else {
              switch (mSpeedStep) {
                case 2:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SR_1_2X;
                  break;
                case 4:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SR_1_4X;
                  break;
                case 8:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SR_1_8X;
                  break;
                case 16:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SR_1_16X;
                  break;
                case 32:
                  mPlayerSpeed = ExoPlayer.RENDERER_PLAYERSPEED_SR_1_32X;
                  break;
                default:
                  mPlayerSpeed =ExoPlayer.RENDERER_PLAYERSPEED_SR_1_2X;
                  break;
              }
              mPlayer.setPlayModeEx(mPlayerSpeed,
                  VideoConst.PLAY_STATUS_SR);
            }
            break;

          case VideoConst.PLAY_STATUS_PAUSED:
          case VideoConst.PLAY_STATUS_STARTED:
          case VideoConst.PLAY_STATUS_FF:
          case VideoConst.PLAY_STATUS_FR:
          case VideoConst.PLAY_STATUS_SF:
            mSpeedStep = 2;
            mPlayerSpeed =ExoPlayer.RENDERER_PLAYERSPEED_SR_1_2X;
            mPlayer.setPlayModeEx(mPlayerSpeed, VideoConst.PLAY_STATUS_SR);
            break;
          default:
            break;
        }
      } catch (RuntimeException ex) {
        mSpeedStep = tmpSpeedStep;
        mPlayerSpeed = tmpPlayerSpeed;
        Log.d(TAG, "slowRewind exce setPlayModeEx ex " + ex);

        throw ex;

      }
    }

  }

  public boolean step() {
    boolean stepSuccess = false;
    if (null != mPlayer) {
      stepSuccess = mPlayer.step(1);
    }

    return stepSuccess;
  }

  public boolean selectMts(short mtsIdx) {
    if (null != mPlayer) {
      return mPlayer.setAudioTrack(mtsIdx);
    }
    return false;
  }

  public int getDuration() {
    if (null != mPlayer) {
      return mPlayer.getDuration();
    }
    return 0;
  }

  public int getCurrentPosition() {
    return getProgress();
  }

  public int getPlaySourceMode() {
    if (null != mPlayer) {
      return mPlayer.getPlaySourceMode();
    }
    return VideoConst.PLAYER_MODE_MMP;
  }

  public int getPlayStatus() {
    if (null != mPlayer) {
      return mPlayer.getPlayStatus();
    }
    return VideoConst.PLAY_STATUS_INITED;
  }

  public int getVideoWidth() {
    if (null != mPlayer) {
      return mPlayer.getVideoWidth();
    }
    return 0;
  }

  public int getVideoHeight() {
    if (null != mPlayer) {
      return mPlayer.getVideoHeight();
    }
    return 0;
  }

  public String getVideoTitle() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getTitle();
    }
    return "";
  }

  public String getVideoDirector() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getDirector();
    }
    return "";
  }

  public String getVideoCopyright() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getCopyright();
    }
    return "";
  }

  public String getVideoGenre() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getGenre();
    }
    return "";
  }

  public String getVideoYear() {
    MetaData md = getMetaDataInfo(Const.FILTER_VIDEO);
    if (md != null) {
      return md.getYear();
    }
    return "";
  }

  public String getCurFileName() {
    if (null != mPlayer) {
      return mPlayer.getCurrentPath();
    }
    return "";
  }

  public void setPlayerMode(int mode) {
    if (null != mPlayer) {
      mPlayer.setPlaySourceMode(mode);
    }
  }

  public int getPlaySpeed() {
    return mSpeedStep;
  }

  public void setPreviewMode(boolean preview) {
    mPreviewMode = preview;
    if (mPlayer != null) {
      mPlayer.setPreviewMode(mPreviewMode);
    }
  }

  public void onSubtitleTrack() {
    if (null != mPlayer) {
      mPlayer.onSubtitleTrack();
    }
  }

  public void offSubtitleTrack() {
    if (null != mPlayer) {
      mPlayer.offSubtitleTrack();
    }
  }

  public int updateRotation(final int degree) {
    return mPlayer.updateRotation(degree);
  }

  public boolean setVendCmd(final int cmd, final int arg1, final int arg2, final Object    obj) {
    return mPlayer.setVendCmd(cmd, arg1, arg2, obj);
  }

  public void setSubtitleTrack(int index) {
    if (null != mPlayer) {
      if (mPlayer.getPlayStatus() >= VideoConst.PLAY_STATUS_STARTED
          && mPlayer.getPlayStatus() < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.setSubtitleTrack(index);
      }
    }
  }

  public void setSubOnOff(boolean on) {
    if (on) {
      onSubtitleTrack();
    } else {
      offSubtitleTrack();
    }
  }

  private MetaData mMetaData = null;

  public MetaData getMetaDataInfo(int type) {
    if (type == Const.FILTER_VIDEO) {
      if (mMetaData == null) {
        //mMetaData = new MetaData();
        //mMetaData.setMetaData(null, null, null, null, null, null, null,
         //   mPlayer.getDuration(), 0);
        getMetaDataInfo();
      }
    }
    return mMetaData;
  }

  public MetaData getMetaDataInfo() {

    if (mMetaData == null) {
      mMetaData = new MetaData();
      if (mPlayer != null) {
//        mMetaData.setMetaData(null, null, null, null, null, null, null, mPlayer.getDuration(), 0);
        MetaDataInfo dataInfo = mPlayer.getMetaDataInfo();
        if (dataInfo != null) {
          mMetaData.setMetaData(dataInfo.getTitle(),
                  dataInfo.getDirector(),
                  dataInfo.getCopyright(),
                  dataInfo.getYear(),
                  dataInfo.getGenre(),
                  dataInfo.getArtist(),
                  dataInfo.getAlbum(),
                  mPlayer.getDuration(),
                  dataInfo.getBiteRate());
        } else {
          mMetaData.setMetaData(null, null,
                  null, null, null, null, null,
                  mPlayer.getDuration(), 0);
        }
      }
    }

    return mMetaData;

  }

  public boolean setAudioTrack(int track) {
    if (null != mPlayer) {
      return mPlayer.setAudioTrack(track);
    }
    return false;
  }

  public short getSubtitleTrackNumber() {
    short subTitleNum = 0;
    if (null != mPlayer) {
        try {
          subTitleNum = (short) mPlayer.getAllSubtitleTrackInfo();
        } catch (Exception e) {
          subTitleNum = 0;
        }
    }
    return subTitleNum;
  }

  public int getAudioTranckNumber() {
    if (null != mPlayer) {
      int num = mPlayer.getAudioTrackInfoNum();
      Log.i(TAG, "getAudioTranckNumber Num:" + num);
      return num;
    } else {
      Log.i(TAG, "getAudioTranckNumber mPlayer==null ");
    }
    return 0;
  }

  public String getTrackType(int index) {
    if (null != mPlayer) {
      return mPlayer.getAudioTrackInfoTypeByIndex(index);
    } else {
      Log.i(TAG, "getTrackType mPlayer==null ");
    }
    return "und";
  }

    public String getTrackMimeType(int index) {
        if (null != mPlayer) {
            return mPlayer.getAudioTrackInfoMimeTypeByIndex(index);
        } else {
            Log.i(TAG, "getTrackMimeType mPlayer==null ");
        }
        return "und";
    }

    public String getVideoMimeType() {
        if (null != mPlayer) {
            return mPlayer.getVideoMimeType();
        } else {
            Log.i(TAG, "getVideoMimeType mPlayer==null ");
        }
        return "und";
    }

  /**
   * @param isNext
   *            true is next,false is prev
   */
  private void play(boolean isNext) {
    if (null != mPlayer) {
      int playStatyus = mPlayer.getPlayStatus();
      if (playStatyus >= VideoConst.PLAY_STATUS_STARTED
          && playStatyus < VideoConst.PLAY_STATUS_STOPPED) {
        mPlayer.stopVideo();
      } else {
        mPlayer.resetVideo();
      }

      /*
       * if (isNext) { if(mPlayList.isEnd(Const.FILTER_VIDEO)){
       * Log.d(TAG,"play next  playlist end to completeVideo."); mPlayer.completVideo(); return ; }
       * } else { if(mPlayList.isBegin(Const.FILTER_VIDEO)){
       * Log.d(TAG,"play pre playlist begin to completeVideo."); mPlayer.completVideo(); return ; }
       * }
       */

      String path = isNext ? mPlayList.getNext(Const.FILTER_VIDEO,
          Const.MANUALNEXT) : mPlayList.getNext(Const.FILTER_VIDEO,
          Const.MANUALPRE);
      Log.d(TAG, "play isNext" + isNext + " path = " + path);

      if (path == null) {
        mPlayer.completVideo();
        return;

      }

      setDataSource(path);
    }
  }

  private boolean canFastOrSlow() {
    boolean bFast = true;
    switch (mPlayer.getPlaySourceMode()) {
      case VideoConst.PLAYER_MODE_DLNA:
      case VideoConst.PLAYER_MODE_SAMBA:
      case VideoConst.PLAYER_MODE_MMP: {
        if (mPlayer.getCurrentPath() != null) {
          mVideoFile = new VideoFile(mPlayer.getCurrentPath());
          bFast = !(mVideoFile.isIsoVideoFile());
        }
      }
        break;
      default:
        break;
    }
    return bFast;
  }

  public void setUnLockPin(int pin) {
    if (null != mPlayer) {
      mPlayer.setUnLockPin(pin);
    }
  }

  public void startVideoFromDrm() {
    if (mPlayer != null) {
      mPlayer.startVideoFromDrm();
    }
  }

  public long getDivxLastMemoryFileID() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mPlayer.getDivxLastMemoryFileID();
    } else {
      return -1;
    }
  }

  public LastMemoryFilePosition getLastMemoryFilePositionExo() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mPlayer");
      return mPlayer.getLastMemoryFilePositionExo();
    } else {
      return null;
    }
  }

  public int setLastMemoryFilePositionExo(LastMemoryFilePosition info) {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mPlayer");
      return mPlayer.setLastMemoryFilePositionExo(info);
    } else {
      return -1;
    }
  }

  public void setExternalSubtitleEncodingType(String encoding) {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mPlayer");
      mPlayer.setExternalSubtitleEncodingType(encoding);
    }
  }

  public int getAudioTrackIndex() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mPlayer.getAudioTrackIndex();
    } else {
      return -1;
    }

  }

  public int getSubtitleIndex() {
    if (null != mPlayer) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mPlayer.getSubtitleIndex();
    } else {
      return -1;
    }
  }

  public void setContext(Context context) {
    // TODO Auto-generated method stub
    if (null != mPlayer) {
      mPlayer.setContext(context);
    }
  }


  public void setPlayStatus(int status) {
    if (null != mPlayer) {
      mPlayer.setPlayStatus(status);
    }
  }

  public void setTunnelingMode(int tunnelingMode) {
    if (null != mPlayer) {
      mPlayer.setTunnelingMode(tunnelingMode);
    }
  }

}
