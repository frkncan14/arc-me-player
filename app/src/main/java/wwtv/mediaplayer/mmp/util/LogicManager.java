
package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.PlaybackParams;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.Tools;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.CorverPic;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.Lyric;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.LyricTimeContentInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.PlaybackService;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.PlaybackService.LocalBinder;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Capture;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.EffectView;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoCompletedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoDecodeListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photo.IThrdListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoComset;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.ExoMediaPlayer.LastMemoryFilePosition;

public class LogicManager {

  private static final String TAG = "LogicManager";

  private static LogicManager mLogicManager = null;

  private VideoManager mVideoManager;

  private PlayList mPlayList;

  private PlaybackService mAudioPlayback = null;

  private Intent serviceIntent;

  private ServiceConnection serviceConnection;

  private Lyric mLyric;

  private Object mPreparedListener;

  private Object mCompletionListener;

  private Object mSeekCompletionListener;

  private Object mInfoListener;

  private Object mErrorListener; // fix bug by hs_haizhudeng

  private Imageshowimpl mImageManager;

  //private MPlayback mPhotoPlayback; // add by lei

  private CommonSet mmpset;

  private final Context mContext;

  private VideoComset mVideoComSet;

  private EffectView mImageEffectView;

  static public final int MMP_EQ_ELEM_NUM = 10;

  static public final int MMP_EQ_MAX = 0x3FFFFF;

  static public final int MMP_EQ_MIN = 0x000FFF;

  private UnmountLisenter unmountLisenter;

  private Capture mCapturer;

  private AsyncLoader<Integer> mPlayLoader;

  //private int mPlayFlag = 0;

  private boolean is3DPhotoMpo = false;

  private final MtkTvConfig mConfig;
  private Handler mThreadHandler;
  /*

  private class PlayWork implements LoadWork<Integer> {

    private final MPlayback mPlayBack;
    private final String mPath;
    private final int mSource;

    public PlayWork(MPlayback playBack, String path, int source) {
      mPlayBack = playBack;
      mPath = path;
      mSource = source;

    }

    @Override
    public Integer load() {
      MtkLog.d(TAG, "playwork load mPlayBack = " + mPlayBack);
      if (mPlayBack == null) {
        mPlayFlag = -1;
      } else {
        // setPicSetting();
        mPlayBack.decode3DPhoto(mPath, mSource);
        mPlayFlag = 1;
      }
      return mPlayFlag;
    }

    @Override
    public void loaded(Integer result) {
        MtkLog.d(TAG, "loaded");
    }

  }
  */

  private LogicManager(Context context) {
    mPlayLoader = AsyncLoader.getInstance(1);
    mPlayList = PlayList.getPlayList();
    mContext = context;
    mConfig = MtkTvConfig.getInstance();
    mmpset = CommonSet.getInstance(mContext);
    mCapturer = new Capture();
  }

  public static synchronized LogicManager getInstance(Context context) {
    if (context == null && mLogicManager == null){
        return null;
    }

    if (null == mLogicManager) {
      mLogicManager = new LogicManager(context.getApplicationContext());
    }
    return mLogicManager;
  }

  public void setThreadHandler(Handler threadHandler) {
    mThreadHandler = threadHandler;
  }

  public Handler getThreadHandler() {
    return mThreadHandler;
  }

  public void setSurfaceView(SurfaceView surface){
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setSurfaceView(surface);
  }

  public void setMediaPlayerListener() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setMediaPlayerListener();
  }
  public void initVideo(int videoSource, Context context){
    setReplay(false);
    mPlayList = PlayList.getPlayList();
    mVideoManager = VideoManager.getInstance(null, videoSource);
    mVideoManager.setPlayerMode(videoSource);
    mVideoManager.setPreviewMode(false);
    mVideoManager.setContext(context);
  }
  public void initVideo(SurfaceView surface, int videoSource, Context context) {
    setReplay(false);
    mPlayList = PlayList.getPlayList();
    mVideoManager = VideoManager.getInstance(surface, videoSource);
    // mVideoManager.setVideoRect(new
    // Rect(0,0,ScreenConstant.SCREEN_WIDTH,ScreenConstant.SCREEN_HEIGHT));
    mVideoManager.setPlayerMode(videoSource);
    mVideoManager.setPreviewMode(false);
    mVideoManager.setContext(context);
  }

  public void initDataSource() {
    if (mVideoManager != null) {
      try {
        if (DmrHelper.isDmr()) {
          mVideoManager.setDataSource(DmrHelper.getUrl());
        } else {
          mVideoManager.setDataSource(mPlayList
              .getCurrentPath(Const.FILTER_VIDEO));
        }
      } catch (IllegalArgumentException ie) {
        MtkLog.e(TAG, ie.getMessage());
      } catch (Exception e) {
        MtkLog.e(TAG, e.getMessage());
      }
    }
  }

  public void setVideoContext(Context context) {
    if (mVideoManager != null) {
      mVideoManager.setContext(context);

    }
  }

  public void setMediaType(int type) {
    if (mVideoManager != null) {
      mVideoManager.mFormateType = type;
    }
  }

  public int getMediaType() {
    if (mVideoManager != null) {
      return mVideoManager.mFormateType;
    }
    return -1;
  }

  public void setTSVideoNum(int num) {
    if (mVideoManager != null) {
      mVideoManager.mTSVideoNum = num;
    }
  }

  public int getTSVideoNum() {
    if (mVideoManager != null) {
      return mVideoManager.mTSVideoNum;
    }
    return 0;
  }

  public boolean isMMPLocalSource() {
    if (mVideoManager != null) {
      return mVideoManager.getPlaySourceMode() == VideoConst.PLAYER_MODE_MMP;
    }
    return false;
  }

  /*--------------------------------------- Video --------------------------------*/
  public void freeVideoResource() {
    mmpset.mmpFreeVideoResource();
  }

  public void restoreVideoResource() {
    mmpset.mmpRestoreVideoResource();
  }

  public void setDisplayRegionToFullScreen() {
    mmpset.setDisplayRegionToFullScreen();
  }

  public void setCapturer(View view) {
    if (null == mCapturer) {
      mCapturer = new Capture();
    }
    mCapturer.captureImage(view);
  }

  public int getNativeBitmap() {
    return mCapturer.getNativeBitmap();
  }

  public int getWidth() {
    return mCapturer.getWidth();
  }

  public int getHeight() {
    return mCapturer.getHeight();
  }

  public int getPitch() {
    return mCapturer.getPitch();
  }

  public int getMode() {
    return mCapturer.getColorMode();
  }

  public int updateRotation(final int degree) {
    return mVideoManager.updateRotation(degree);
  }

  public boolean setVendCmd(final int cmd, final int arg1, final int arg2, final Object    obj) {
    return mVideoManager.setVendCmd(cmd, arg1, arg2, obj);
  }

  public void setSubtitleTrack(short index) {
    if (null == mVideoManager) {
      return;
    }
    MtkLog.i(TAG, "------setSubtitleTrack  index:" + index);
    mVideoManager.setSubtitleTrack(index);
  }

  // fix:cr:DTV00699132
  private boolean isReplay = false;

  public void setReplay(boolean replay) {
    MtkLog.i(TAG, "setReplay :" + replay);
    isReplay = replay;
  }

  public boolean isReplay() {
    MtkLog.i(TAG, "isReplay :" + isReplay);
    return isReplay;
  }

  public void setSubOnOff(boolean flag) {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setSubOnOff(flag);
  }

  public short getSubtitleTrackNumber() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getSubtitleTrackNumber();
  }

  public int getAudioTranckNumber() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getAudioTranckNumber();

  }

  public String getCurrentAudioTranckType(int index) {
    if (null == mVideoManager) {
      return null;
    }
    return mVideoManager.getTrackType(index);

  }
    public String getCurrentAudioTranckMimeType(int index) {
        if (null == mVideoManager) {
            return null;
        }
        return mVideoManager.getTrackMimeType(index);
    }

    public String getCurrentVideoMimeType() {
        if (null == mVideoManager) {
            return null;
        }
        return mVideoManager.getVideoMimeType();
    }

  public boolean setAudioTranckNumber(short mtsIdx) {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.selectMts(mtsIdx);
  }

  public boolean isPlaying() {
    if (null == mVideoManager) {
      MtkLog.d(TAG,"mVideoManager == null");
      return false;
    }
    return mVideoManager.isPlaying();

  }

  public int[] getAvailableScreenMode() {
    return mmpset.getAvailableScreenMode();
  }

  public String[] getTSVideoProgramList() {
    if (mVideoManager != null) {
      String programs[] = new String[mVideoManager.mTSVideoNum];
      for (int i = 0; i < programs.length; i++) {
        programs[i] = mContext.getString(R.string.mmp_menu_ts_program) + " " + i;
      }
      return programs;
    }
    return new String[0];
  }

  public void videoZoom(int zoomType) {
    if (mVideoComSet == null) {
        mVideoComSet = new VideoComset();
    }
    mVideoComSet.videoZoom(zoomType);
  }

  /**
   * set picture zoom type 1X, 2X 4X.
   *
   * @param zoomType
   */
  public void setPicZoom(int zoomType) {
    if (Util.isSupport4K8K()) {
      if (mEffectView != null) {
        mEffectView.setMultiple(zoomType);
      }
    } else {
      if (mImageEffectView != null) {
        mImageEffectView.setMultiple(zoomType);
      }
    }
  }

  /**
   * get current setting zoom value
   *
   * @return
   */
  public int getPicCurZoom() {
    int zoom = 0;
    if (Util.isSupport4K8K()) {
      if (mEffectView != null) {
        zoom = mEffectView.getMultiple();
      }
    } else {
      if (mImageEffectView != null) {
        zoom = mImageEffectView.getMultiple();
      }
    }
    return zoom;
  }

  public int getCurZomm() {
    if (null == mVideoComSet) {
      return 1;
    }
    return mVideoComSet.getCurZoomType();
  }

  public int getMaxZoom() {
    if (mVideoComSet == null) {
        mVideoComSet = new VideoComset();
    }
    return mVideoComSet.getMaxZoom();
  }

  public void videoZoomReset() {
    if (mVideoComSet == null) {
      mVideoComSet = new VideoComset();
    }
    mVideoComSet.videoZoomReset();
  }

  public String getVideoPageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_VIDEO) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_VIDEO);
  }

  public void setOnInfoListener(Object infoListener) {
    if (null != mVideoManager) {
      mVideoManager.setOnInfoListener(infoListener);
    }
  }

  public void playVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.startVideo();
  }

  public void pauseVideoWhenStopKey() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.pauseVideoWhenPressStop();
  }

  public void autoNext() {
    if (null == mVideoManager) {
      return;
    }
    try {
      mVideoManager.autoNext();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, "IllegalStateException:" + e.getMessage());
      throw new IllegalStateException(e);
    }

  }

  public void pauseVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.pauseVideo();
  }

  public boolean stepVideo() {
    boolean stepSuccess = false;
    if (null == mVideoManager) {
      return false;
    }
    stepSuccess = mVideoManager.step();

    return stepSuccess;
  }

  public void stopVideo() {
    if (null == mVideoManager) {
      return;
    }
    try {
      mVideoManager.stopVideo();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
  }

  public void stopDrmVideo() {
    if (null == mVideoManager) {
      return;
    }
    try {
      mVideoManager.stopDrmVideo();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
  }

  public void finishVideo() {
    Log.d(TAG, "finishVideo enter");
    if (null == mVideoManager) {
      MtkLog.e(TAG, "finishVideo mVideoManager is null");
      return;
    }
    try {
      mVideoManager.stopVideo();

    } catch (IllegalStateException e) {
      Log.d(TAG, "stopVideo finishVideo exception");
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
    try {
      mVideoManager.reset();

    } catch (IllegalStateException e) {
      Log.d(TAG, "finishVideo reset exception");
      MtkLog.e(TAG, "stop  " + e.getMessage());
    }
    try {
      mVideoManager.onRelease();
      mVideoManager = null;
      /* Had closed video play and send broadcast tell it */
    } catch (Exception e) {
      mVideoManager = null;
      MtkLog.e(TAG, "finishVideo onRelease  " + e.toString());
    }
    Util.logResRelease("Video finished");
  }

  public void sendCloseBroadCast() {

    Intent intent = new Intent(MultiMediaConstant.STOPMUSIC);
    mContext.sendBroadcast(intent);
    MtkLog.e(TAG, "Video Play Activity sendCloseVideoBroadCast ! ");
    clearAudio();
  }

  /**
   * Play prev video.
   *
   * @return -1, play failed, 0, successful.
   */
  public int playPrevVideo() {
    if (null == mVideoManager) {
      return -1;
    }
    try {
      mVideoManager.playPrev();
      if (!Util.mIsEnterPip) {
        videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
    return 0;
  }

  /**
   * Replay video.
   *
   * @return -1, replay failed, 0, successful.
   */
//  public int replayVideo() {
//    if (null == mVideoManager) {
//      return -1;
//    }
//    try {
//      mVideoManager.replay();
//    } catch (Exception e) {
//      e.printStackTrace();
//      return -1;
//    }
//    return 0;
//  }

  public void resetReplay() {
    if (null != mVideoManager) {
      mVideoManager.resetReplay();
    }
  }

  /**
   * Play next video.
   *
   * @return -1, play failed, 0, successful.
   */
  public int playNextVideo() {
    if (null == mVideoManager) {
      return -1;
    }
    try {
      mVideoManager.playNext();
      if (!Util.mIsEnterPip) {
        videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }

    return 0;
  }

  // add by shuming fix CR00385698
  /**
   *
   * @param featurenotsurport
   */

  /**
   *
   * @return isVideoFeaturenotsurport
   */

  // end
  public void onDevUnMount(String devicePath) {
    if (unmountLisenter != null) {
      MtkLog.e(TAG, "unmount dismiss music view~~~");
      unmountLisenter.onUnmount(devicePath);
    }
//    stopAudio();
//    finishVideo();
  }

  public void slowForwardVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.slowForward();

  }

  public void slowRewindVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.slowRewind();

  }

  public void fastForwardVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.fastForward();

  }

  public void fastForwardVideoNormal() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setNormalSpeed();

  }

  public void fastRewindVideo() {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.fastRewind();

  }

  public boolean canDoSeek() {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.canDoSeek();
  }

  public boolean isNormalSpeed() {
    if (null == mVideoManager) {
      Log.i(TAG, "isNormalSpeed null == mVideoManager");
      return true;
    }

    return mVideoManager.isNormalSpeed();
  }

  public boolean isNormalAudioTrackSpeed() {
    if (null == mVideoManager) {
      Log.i(TAG, "isNormalSpeed null == mVideoManager");
      return true;
    }

    PlaybackParams para = mVideoManager.getPlaybackParams();
    if (null != para) {
      if (1.0f != para.getSpeed()) {
        return false;
      }
    }

    return true;
  }
  public int getVideoSpeed() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getPlaySpeed();
  }

  public void seek(int positon) {
    if (null == mVideoManager) {
      MtkLog.i(TAG, "null == mVideoManager");
      return;
    }
    mVideoManager.seek(positon);
  }

  public void seekLastMemory(int positon) {
    if (null == mVideoManager) {
      MtkLog.i(TAG, "null == mVideoManager");
      return;
    }
    mVideoManager.seekLastMemory(positon);
  }

  public boolean isVideoFast() {
    if (null == mVideoManager) {
      return false;
    }
    return (mVideoManager.getPlayStatus() == VideoConst.PLAY_STATUS_FR)
        || (mVideoManager.getPlayStatus() == VideoConst.PLAY_STATUS_FF);
  }

  public int getVideoDuration() {
    if (mVideoManager != null) {
      return mVideoManager.getDuration();
    }
    return 0;
  }

  /**
   * Set Seamless Mode
   */
  public void setSeamlessMode(boolean seamlessMode){
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setSeamlessMode(seamlessMode);
  }

  /**
   * Get Seamless Mode Setting
   */
  public boolean getSeamlessMode(){
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.getSeamlessMode();
  }

  /**
   * Get video width;
   *
   * @return
   */
  public int getVideoWidth() {
    int width = 0;
    if (mVideoManager != null) {
      width = mVideoManager.getVideoWidth();
    }
    return width;
  }

  /**
   * Get video height.
   *
   * @return
   */
  public int getVideoHeight() {
    int heghit = 0;
    if (mVideoManager != null) {
      heghit = mVideoManager.getVideoHeight();
      // end
    }

    return heghit;
  }

  public String getVideoBitRate() {
    if (null != mVideoManager) {
      return mVideoManager.getFileBitRate();
    }
    return "";
  }

  public String getVideoTitle() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoTitle();
    }
    return "";
  }

  public String getVideoCopyright() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoCopyright();
    }
    return "";
  }

  public String getVideoYear() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoYear();
    }
    return "";
  }

  public String getVideoGenre() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoGenre();
    }
    return "";
  }

  public String getVideoDirector() {
    if (null != mVideoManager) {
      return mVideoManager.getVideoDirector();
    }
    return "";
  }

  public long getVideoFileSize() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getFileSize();
  }

  public String getVideoFileDate() {
    if (null == mVideoManager) {
      return null;
    }
    return mVideoManager.getFileDate();
  }

  public int getVideoBytePosition() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getBytePosition();
  }

  public int getVideoProgress() {
    if (null == mVideoManager) {
      return 0;
    }
    return mVideoManager.getProgress();
  }

  public boolean isInPlaybackState() {
    if (null == mVideoManager) {
      return false;
    }
    return mVideoManager.isInPlaybackState();
  }

  /*------------------- mmpset -----------------------*/
  public int getVolume() {
    if (null == mmpset) {
      return 0;
    }
    return mmpset.getVolume();
  }

  public void setVolume(int volume) {
    if (null == mmpset) {
      return;
    }
    mmpset.setVolume(volume);
  }

  public void setVolumeUp() {
    if (isMute()) {
      setMute();
      return;
    }
    int maxVolume = getMaxVolume();
    int currentVolume = getVolume();
    currentVolume = currentVolume + 1;
    if (currentVolume > maxVolume) {
      currentVolume = maxVolume;
    }
    setVolume(currentVolume);
  }

  public void setVolumeDown() {
    if (isMute()) {
      setMute();
      return;
    }
    int currentVolume = getVolume();
    currentVolume = currentVolume - 1;
    if (currentVolume < 0) {
      currentVolume = 0;
    }
    setVolume(currentVolume);
  }

  public void setAudioOnly(boolean switchFlag) {
    if (null == mmpset) {
      return;
    }

    mmpset.setAudOnly(switchFlag);
  }

  public boolean isAudioOnly() {
    if (null == mmpset) {
      return false;
    }
    return mmpset.getAudOnly();
    // return false;
  }

  private static final String COMMA = ",";

  private List<Integer> getSpectrum() {

    String sp = mConfig.getConfigString(MtkTvConfigType.CFG_MISC_EX_AUD_TYPE_SPECTRUM_INFO);

    String[] spList = sp.split(COMMA);
    MtkLog.d(TAG, "getSpectrum sp = " + sp + "spList =" + spList + "spList.length = "
        + spList.length);
    List<Integer> list = new ArrayList<Integer>();
    if (spList != null && spList.length > 0) {
      for (String item : spList) {
        MtkLog.d(TAG, "getSpectrum valueOf item = " + item);

        try {
          int val = Integer.valueOf(item.trim());
          list.add(val);

        } catch (Exception ex) {

          MtkLog.d(TAG, "Exception item = " + item);
        }

      }

    }

    return list;

  }

  public int[] getAudSpectrum() {
    int[] valueArray = new int[15];
    List<Integer> array = getSpectrum();
    if (array != null) {
      for (int i = 0; i < array.size(); i++) {
        valueArray[i] = (MMP_EQ_ELEM_NUM * (array.get(i) - MMP_EQ_MIN) / (MMP_EQ_MAX - MMP_EQ_MIN));
//        MtkLog.d(TAG,
//            "getAudSpectrum valueArray[i] =" + valueArray[i] + " array.get(i) =" + array.get(i));

      }
    }
    return valueArray;
  }

  public int getMaxVolume() {
    return mmpset.getMaxVolume();
  }


  public int getTSProgramIndex() {
    if (mVideoManager != null) {
      MtkLog.d(TAG, "getTSProgramIndex mVideoManager.mCurrentTSVideoIndex:"
          + mVideoManager.mCurrentTSVideoIndex);
     return mVideoManager.mCurrentTSVideoIndex;
    }
    return 0;
  }

  /**
   * set picture mode
   *
   * @param type
   */
  public void setPictureMode(int type) {
    mmpset.setPictureMode(type);
  }

  /**
   * set screen mode
   *
   * @param type
   */
  public void setScreenMode(int type) {
    mmpset.setScreenMode(type);
  }

  public int getCurPictureMode() {
    return mmpset.getCurPictureMode();
  }

  public int getCurScreenMode() {
    if (null == mVideoManager) {
      return 0;
    }
    return mmpset.getCurScreenMode();
  }

  public void setMute() {
    if (null == mmpset) {
      return;
    }
    mmpset.setMute();
  }

  public boolean isMute() {
    if (null == mmpset) {
      return false;
    }
    return mmpset.isMute();
  }

  public int getVideoPlayStatus() {
    if (null == mVideoManager) {
      return VideoConst.PLAY_STATUS_INITED;
    }
    return mVideoManager.getPlayStatus();
  }

  public String getFileDuration() {
    if (null == mVideoManager) {
      return "";
    }
    return mVideoManager.getVideoYear();
  }

  public String getFileName() {
    if (null == mVideoManager) {
      return "";
    }
    String filename = mVideoManager.getCurFileName();
    MtkLog.e(TAG, "getFilename:" + filename);
    try {
      return filename.substring(filename.lastIndexOf("/") + 1);

    } catch (Exception e) {
      MtkLog.d(TAG, "IllegalStateException:" + e.toString());
      return null;
    }
  }

  public void setVideoPreparedListener(Object listener) {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setOnPreparedListener(listener);
  }

  public void setCompleteListener(Object listener) {
    if (null == mVideoManager) {
      return;
    }
    mVideoManager.setOnCompletionListener(listener);// setOnPBCompleteListener(listener);
  }

  public void setPlaybackParams(PlaybackParams para) {
    mVideoManager.setPlaybackParams(para);
  }

  public PlaybackParams getPlaybackParams() {
    return mVideoManager.getPlaybackParams();
  }

  /*-------------------aduido ------------------*/
  private int mAudioSource;

  public int getAudioSourceType() {
    return mAudioSource;

  }

  private Context mMusicContext;

  // change by browse fix CR DTV00384318
  /**
   * New Service when service not exist.
   * */
  private void initService(Context context) {
    mMusicContext = context;
    serviceIntent = new Intent(context, PlaybackService.class);
    serviceIntent.putExtra(PlaybackService.PLAY_TYPE, mAudioSource);
    serviceConnection = new ServiceConnection() {
      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e(TAG,"onServiceConnected jinxin");
        LocalBinder binder = (LocalBinder) service;
        mAudioPlayback = binder.getService();
        mAudioPlayback.setContext(mMusicContext);
        startPlayAudio(mAudioSource);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {
        Log.e(TAG,"onServiceDisconnected jinxin");
        MtkLog.d(TAG, "onServiceDisconnected");
      }

    };
    startService(context);
    bindService(context);
  }

  public void initAudio(Context context, final int audioSource) {
    if (false == isAudioFocused) {
      Log.v(TAG, "AudioFocusListener initAudio");
      isAudioFocused = true;
      registerAudioFocusListener();
    }
    mPlayList = PlayList.getPlayList();
    mmpset = CommonSet.getInstance(context);
    mAudioSource = audioSource;
    if (mAudioPlayback == null) {
      initService(context);
    } else {
      if (mAudioPlayback.getPlayStatus() < AudioConst.PLAY_STATUS_STOPPED) {
        stopAudio();
      }
      bindService(context);
    }
  }

  public void clearAudio() {
    mAudioPlayback = null;
    unmountLisenter = null;
  }

  // end
  private void startPlayAudio(int audioSource) {
    mAudioPlayback.registerAudioPreparedListener(mPreparedListener);
    mAudioPlayback.registerAudioCompletionListener(mCompletionListener);
    mAudioPlayback.registerAudioSeekCompletionListener(mSeekCompletionListener);
    mAudioPlayback.registerAudioErrorListener(mErrorListener);
    mAudioPlayback.registerInfoListener(mInfoListener);
    mAudioPlayback.setPlayMode(audioSource);
    String url = mPlayList.getCurrentPath(Const.FILTER_AUDIO);
    if (DmrHelper.isDmr()) {
      url = DmrHelper.getUrl();
    }
    Log.i(TAG, "url:" + url);
    mAudioPlayback.setDataSource(url);
  }

  public void startService(Context context) {
    context.startService(serviceIntent);
  }

  public void stopService(Context context) {
    context.stopService(serviceIntent);
  }

  public void bindService(Context context) {
    Log.e(TAG,"bindService jinxin");
    mMusicContext = context;
    try {
      context.bindService(serviceIntent, serviceConnection,
          Context.BIND_AUTO_CREATE);
    } catch (Exception e) {
      Log.e(TAG, "Exception:" + e.toString());
    }

  }

  public void unbindService(Context context) {
    Log.e(TAG,"unbindService jinxin");
    try {
      context.unbindService(serviceConnection);
    } catch (Exception e) {
      Log.e(TAG, "Exception:" + e.toString());
    }

  }

  public PlaybackService getAudioPlaybackService() {
    return mAudioPlayback;
  }

    public int getMusicAudioTrackNumber() {
        MtkLog.i(TAG, "getMusicAudioTrackNumber");
        if (null == mAudioPlayback) {
          return 0;
        }
        return mAudioPlayback.getMusicAudioTrackNumber();
    }

    public int getMusicAudioTrackIndex() {
        MtkLog.i(TAG, "getMusicAudioTrackIndex");
        int index = -1;
        if (null != mAudioPlayback) {
          index = mAudioPlayback.getMusicAudioTrackIndex();
        } else {
          MtkLog.i(TAG, "LogicManager getMusicAudioTrackIndex null == mAudioPlayback");
        }
        return index;
    }

  public String getCurrentMusicAudioTrackType(int index) {
    MtkLog.i(TAG, "getCurrentMusicAudioTrackType");
    if (null == mAudioPlayback) {
      return null;
    }
    return mAudioPlayback.getCurrentMusicAudioTrackType(index);

  }

  public void playAudio() {
    if (null == mAudioPlayback)
    {
      return;
    }
    try {
      mAudioPlayback.play();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, e.getMessage());
    }

  }

  public void pauseAudio() {
    if (null == mAudioPlayback) {
      return;
    }
    try {
      mAudioPlayback.pause();
    } catch (IllegalStateException e) {
      MtkLog.e(TAG, e.getMessage());
    }

  }

  public void stopAudio() {
    MtkLog.d(TAG, "stopAudio");
    if (mAudioPlayback != null) {
      try {
        mAudioPlayback.stop();
      } catch (IllegalStateException e) {
        MtkLog.e(TAG, e.getMessage());
      }

    }

  }

  public interface UnmountLisenter {

    void onUnmount(String devicePath);
  }

  public void registerUnMountLisenter(UnmountLisenter lisenter) {
    MtkLog.e(TAG, "register unmount listener");
    unmountLisenter = lisenter;

  }

  public void playNextAudio() {
    if (null != mAudioPlayback) {
      try {
        mAudioPlayback.playNext();
      } catch (IllegalStateException e) {
        MtkLog.i(TAG, e.getMessage());
      }

    }
  }

  public void playPrevAudio() {
    if (null != mAudioPlayback) {
      try {
        mAudioPlayback.playPrevious();
      } catch (IllegalStateException e) {
        MtkLog.i(TAG, e.getMessage());
      }

    }
  }

  public void replayAudio() {
    if (null != mAudioPlayback) {
      try {
        mAudioPlayback.setDataSource(mAudioPlayback.getFilePath());
      } catch (IllegalStateException e) {
        MtkLog.i(TAG, e.getMessage());
      }

    }
  }

  public boolean isAudioFast() {
    if (null == mAudioPlayback)
    {
      return false;
    }
    return (mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_FR)
        || (mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_FF)
        || (mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_SF);
  }

  public boolean isAudioPlaying() {

    if (null != mAudioPlayback) {
      return mAudioPlayback.isPlaying();
    }
    return false;
  }

  public boolean isAudioPause() {

    if (null == mAudioPlayback) {
      return false;
    }

    return mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_PAUSED;
  }

  public boolean isAudioStarted() {
    if (null == mAudioPlayback) {
      return false;
    }
    int status = mAudioPlayback.getPlayStatus();
    return ((status >= AudioConst.PLAY_STATUS_STARTED)
        && (status < AudioConst.PLAY_STATUS_STOPPED));
  }

  public int getPlayMode() {
    if (null == mAudioPlayback) {
      return AudioConst.PLAYER_MODE_LOCAL;
    }
    return mAudioPlayback.getPlayMode();
  }

  public void registerAudioPlayListener(IAudioPlayListener mListener) {
    if (null != mAudioPlayback) {
      mAudioPlayback.registerPlayListener(mListener);
    }
  }

  public String getAudioFilePath() {
    if (null == mAudioPlayback) {
      return null;
    }

    return mAudioPlayback.getFilePath();

  }

  public boolean isAudioStoped() {
    if (null == mAudioPlayback) {
      return true;
    }
    return mAudioPlayback.getPlayStatus() == AudioConst.PLAY_STATUS_STOPPED;
  }

  public void seekToCertainTime(long time) throws IllegalStateException {
    if (null != mAudioPlayback) {
      mAudioPlayback.seekToCertainTime(time);
    }
  }

  public boolean canSeek() {
    if (null == mAudioPlayback) {
      return false;
    }
    return mAudioPlayback.canSeek();
  }

//  public void fastForwardAudio() throws IllegalStateException, RuntimeException {
//    if (mAudioPlayback == null) {
//      return;
//    }
//
//    mAudioPlayback.fastForward();
//
//  }

  public void fastForwardAudioNormal() {
    if (null == mAudioPlayback) {
      return;
    }
    mAudioPlayback.setNormalSpeed();

  }

  public int getAudioSpeed() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getSpeed();
  }

  public long getAudioFileSize() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getFileSize();
  }

  public void setAuidoSpeed(int speed) {
    if (null != mAudioPlayback) {
      mAudioPlayback.setSpeed(speed);
    }
  }

  public int getAudioStatus() {
    if (null == mAudioPlayback) {
      return -1;
    }
    return mAudioPlayback.getPlayStatus();
  }

//  public void fastRewindAudio() throws IllegalStateException, RuntimeException {
//    if (mAudioPlayback == null) {
//      return;
//    }
//
//    mAudioPlayback.fastRewind();
//
//  }

  public int getPlaybackProgress() {
    if (mAudioPlayback != null) {
      return mAudioPlayback.getPlaybackProgress();
    } else {
      return 0;
    }

  }

  public int getAudioBytePosition() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getCurrentBytePosition();
  }

  public int getTotalPlaybackTime() {
    if (null == mAudioPlayback) {
      return 0;
    }
    return mAudioPlayback.getTotalPlaybackTime();
  }

  public Bitmap getAlbumArtwork(int srcType, String path, int width,
      int height) {
    return CorverPic.getInstance().getAudioCorverPic(srcType, path, width,
        height);
  }

  public String getMusicAlbum() {
    if (null == mAudioPlayback)
    {
      return "";
    }
    return mAudioPlayback.getAlbum();
  }

  public String getMusicArtist() {
    if (null == mAudioPlayback)
    {
      return "";
    }
    return mAudioPlayback.getArtist();
  }

  public String getMusicGenre() {
    return mAudioPlayback.getGenre();
  }

  public String getMusicTitle() {
    if (null == mAudioPlayback) {
      return "";
    }
    return mAudioPlayback.getTitle();
  }

  public String getMusicYear() {
    return mAudioPlayback.getYear();
  }

  public int getPlayStatus() {
    if (mAudioPlayback != null) {
      return mAudioPlayback.getPlayStatus();
    }
    return 0;
  }

  public int getPlayVideoStatusOfUI() {
    if (mVideoManager != null) {
      return mVideoManager.getPlayStatus();
    }
    return VideoConst.PLAY_STATUS_ERROR;
  }

  public List<LyricTimeContentInfo> getLrcInfo() {

    // TODO change
    List<LyricTimeContentInfo> lrcInfo = new ArrayList<LyricTimeContentInfo>();
    String mp3Path = mPlayList.getCurrentPath(Const.FILTER_AUDIO);
    try {
      if (null != mp3Path) {
        int index = mp3Path.lastIndexOf(".");
        if (index == -1) {
          return lrcInfo;
        }
        String lrcPath = mp3Path.substring(0, index) + ".lrc";
        MtkLog.i(TAG, "  lrcPath =" + lrcPath + "  mp3Path=" + mp3Path);
        File lrcFile = new File(lrcPath);
        if (lrcFile.exists() || Tools.isSambaPlaybackUrl(lrcPath)) {
          String convertedPath = null;
          if (Tools.isSambaPlaybackUrl(lrcPath)) {
              convertedPath = Tools.convertToHttpUrl(lrcPath);
          } else {
              convertedPath = lrcPath;
          }
          mLyric = new Lyric(convertedPath);
          lrcInfo = mLyric.getLyricTimeContentInfo();
        } else {
          Log.d (TAG, "lrcFile not exists");
        }
      }
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
      return null;
    }
    return lrcInfo;
  }

  public int getLrcLine(long time) {
    if (mLyric != null) {
      return mLyric.getLine(time);
    } else {
      return 0;
    }
  }

  public String getCurrentPath(int type) {
    return mPlayList.getCurrentPath(type);
  }

  // public String getAudioFilenmae() {
  //
  // return mPlayList.getCurrentFileName(Const.FILTER_AUDIO);
  // }

  public String getAudioPageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_AUDIO) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_AUDIO);
  }

  public void setAudioPreparedListener(Object listener) {

    mPreparedListener = listener;

  }

  // fix bug by hs_hzd
  public void setAudioErrorListener(Object listener) {

    mErrorListener = listener;

  }

  public void setVideoErrorListener(Object listener) {
    if (mVideoManager != null) {
      mVideoManager.setOnErrorListener(listener);
    }
  }

  public void setSeekCompleteListener(Object listener) {
    if (null != mVideoManager) {
      mVideoManager.setOnSeekCompleteListener(listener);
    }
  }

  public void removeErrorListener() {
    if (mAudioPlayback != null) {
      mAudioPlayback.unregisterAudioErrorListener();
      mErrorListener = null;
    }
  }

  public void unRegisterAllListener() {
    if (mAudioPlayback != null) {
      mAudioPlayback.unregisterAudioPreparedListener();
      mAudioPlayback.unregisterAudioSeekCompletionListener();
      mAudioPlayback.unregisterAudioCompletionListener();
      mAudioPlayback.unregisterAudioErrorListener();
      mPreparedListener = null;
      mSeekCompletionListener = null;
      mCompletionListener = null;
      mErrorListener = null;
    }
  }

  public void setAudioSeekCompletionListener(Object listener) {

    mSeekCompletionListener = listener;

  }

  public void setAudioCompletionListener(Object listener) {

    mCompletionListener = listener;

  }

  public void setAudioInfoListener(Object listener) {
    mInfoListener = listener;
  }

  public int getRepeatModel(int type) {
    if (null == mPlayList) {
      return 0;
    }
    return mPlayList.getRepeatMode(type);
  }

  public boolean getShuffleMode(int fileType) {
    return mPlayList.getShuffleMode(fileType);
  }

  public void setShuffle(int type, boolean model) {
    mPlayList.setShuffleMode(type, model);
  }

  public void initPhoto(Display display, EffectView view) {
    mPlayList = PlayList.getPlayList();
    mImageEffectView = view;
    if (null == mImageManager) {
      mImageManager = new Imageshowimpl(display, mContext.getApplicationContext());
    }
  }

  EffectViewNative mEffectView;
  public void initPhotoFor4K2K(Display display, EffectViewNative effectview) {
    mEffectView = effectview;
    mPlayList = PlayList.getPlayList();
    if (null == mImageManager) {
      mImageManager = new Imageshowimpl(display, mContext.getApplicationContext());
    }
  }

//  public int getPicIndex() {
//    String paths[] = {
//        ".jpg", ".png", ".gif", ".bmp"
//    };
//    String path = mPlayList.getCurrentPath(Const.FILTER_IMAGE);
//    MtkLog.i(TAG, "cur file path:" + path);
//    try {
//      for (int index = 0; index < paths.length; index++) {
//        if (paths[index].equalsIgnoreCase(path.substring(path.lastIndexOf('.')))) {
//          return index;
//        }
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//    return 5;
//
//  }

  public void stopDecode() {
//    if (null != mImageManager) {
//      if (mThreadHandler != null) {
//        mThreadHandler.post(new Runnable() {
//
//          @Override
//          public void run() {
//            mImageManager.stopDecode();
//          }
//        });
//      } else {
//        mImageManager.stopDecode();
//      }
//    }
    ThreadUtil.runOnSubThread(new Runnable() {
      @Override
      public void run() {
        if (null != mImageManager) {
          mImageManager.stopDecode();
        }
      }
    });
  }

  public void initThrdPhoto(Context context) {
    mPlayList = PlayList.getPlayList();
    //mPhotoPlayback = PhotoManager.getInstance(context).getPlayback();

  }

  public void stopPlayWork() {

    if (null != mPlayLoader) {
      mPlayLoader.clearQueue();
      mPlayLoader = null;
    }
  }

  public void playThrdPhoto(int type) {
    /*
        try {
          String path = null;

          if (type == Const.CURRENTPLAY) {
            path = mPlayList.getCurrentPath(Const.FILTER_IMAGE);
          } else {
            path = mPlayList.getNext(Const.FILTER_IMAGE, type);
          }

          MtkLog.d(TAG, "playThrdPhoto path =" + path);

          if (mPlayLoader != null) {
            mPhotoPlayback.cancel();
            mPhotoPlayback.close();
            mPlayLoader.clearQueue();
            mPlayLoader.addWork(new PlayWork(mPhotoPlayback, path, mPlayList.getSource()));
          }

        } catch (NotSupportException e) {
          e.printStackTrace();
        }
        */
  }

  public void closeThrdPhoto() {
    /*
    try {
      if (mPhotoPlayback != null) {
        if (mThreadHandler != null) {
          mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
              mPhotoPlayback.close();
            }
          });
        } else {
          mPhotoPlayback.close();
        }
      }
    } catch (NotSupportException e) {
        e.printStackTrace();
    }
    */
  }

  public void setPhotoCompleteListener(
      OnPhotoCompletedListener completeListener) {
    mImageManager.setCompleteListener(completeListener);
  }

  public void setPhotoDecodeListener(OnPhotoDecodeListener decodeListener) {
    mImageManager.setDecodeListener(decodeListener);
  }

  public PhotoUtil transfBitmap(String path, int source) {

    if (null == path || path.length() <= 0) {
      return null;
    }
    try {
      mImageManager.setLocOrNet(source);
      return mImageManager.getPhoto(path);

    } catch (OutOfMemoryError error) {
      MtkLog.i(TAG, " transfBitmap  " + error.getMessage());
      return null;
    }
  }

  public void setImageSource(int source) {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    mImageManager.setLocOrNet(source);
  }

  public PhotoUtil loadImageBitmap(int type) {

    return mImageManager.loadBitmap(type);
  }

  public int getImageEffect() {
    int effect = ConstPhoto.DEFAULT;
    if (Util.isSupport4K8K()) {
      if (mEffectView != null) {
        effect = mEffectView.getEffectValue();
      }
    } else {
      if (mImageEffectView != null) {
        effect = mImageEffectView.getEffectValue();
      }
    }
    return effect;
  }

  public Bitmap setLeftRotate(Bitmap bitmap) {
    return mImageManager.leftRotate(bitmap);
  }

  public Bitmap setRightRotate(Bitmap bitmap) {
    return mImageManager.rightRotate(bitmap);
  }

  int mRotateCount = 0;

  public void incRotate() {
    MtkLog.d(TAG, "incRotate:" + isFirst + "  mRotateCount:" + mRotateCount);
    if (isFirst == true) {
      isFirst = false;
    }
    mRotateCount++;
    mRotateCount = mRotateCount % 4;
  }

  private int mRotation = 0;
  boolean isFirst = true;
  boolean isRationChanged = false;

  public int getRotate() {
    MtkLog.i(TAG, "getRotate mRotation:" + mRotation + "--mRotateCount:" + mRotateCount);
    return (mRotation + mRotateCount * 90) % 360;
  }

  public boolean isFirstIn() {
    return isFirst;
  }

  public void initRotate() {
    mRotateCount = 0;
    isFirst = true;
    isRationChanged = false;

    ThreadUtil.runOnSubThread(new Runnable() {
      @Override
      public void run() {
        mRotation = mImageManager.getOrientation();
        if(mRotation >0 && mRotation <= 8){
          int temp = mRotation;
          mRotation = Const.ORIENTATION_NEXT_ARRAY[mRotation]-1;
          MtkLog.i(TAG, "initRotate index:"+mRotation+" ---oldmRotation:"+temp);
          mRotation = (mRotation%4) * 90 ;
        }else {
          mRotation = 0;
        }
//    mRotation = (mRotation) % 4 * 90;
      }
    });
  }

  public void setRotationChanged() {
    isRationChanged = true;
  }

  public boolean isOrientantionChanged() {
    return isRationChanged;
  }

  public void zoomImage(ImageView view, int inOrOut, Bitmap bitmap, int size) {
    mImageManager.zoom(view, inOrOut, bitmap, size);
  }

  // add by xiaojie fix cr DTV00389237
  public int getCurrentZoomSize() {
    return mImageManager.getZoomOutSize();
  }

  public int getCurrentIndex() {

    return mPlayList.getCurrentIndex(Const.FILTER_IMAGE);
  }

  public int getImageNumber() {

    return mPlayList.getFileNum(Const.FILTER_IMAGE);
  }

  public String getImagePageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_IMAGE) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_IMAGE);
  }

  public String getThrdPhotoPageSize() {
    return (mPlayList.getCurrentIndex(Const.FILTER_IMAGE) + 1) + "/"
        + mPlayList.getFileNum(Const.FILTER_IMAGE);
  }

  // Public method
  public int getMode(int type) {
    return mPlayList.getRepeatMode(type);
  }

  public void setRepeatMode(int type, int mode) {
    if (null != mPlayList) {
      mPlayList.setRepeatMode(type, mode);
      MtkLog.i(TAG, "setRepeatMode mode:" + mode + " type:" + type);
    }
  }

  public int getPhotoOrientation() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getOrientation();
  }

  public String getPhotoName() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getName();
  }

  public String getWhiteBalance() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getWhiteBalance();
  }

  public String getAlbum() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getAlbum();
  }

  public String getMake() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getMake();
  }

  public String getModifyDate() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getModifyDate();
  }

  public int getPhotoDur() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getDuration();
  }

  public String getPhotoModel() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getModel();
  }

  public String getFlash() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getFlash();
  }

  /*
   * public String getResolution() { if(mImageManager == null){ mImageManager = new Imageshowimpl();
   * } return mImageManager.getPwidth() + " x " + mImageManager.getPheight(); }
   */
  /* add by lei 2011-12-26, fix 3d photo get resolution issue */
  public String getResolution() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getResolution();
  }

  public String getPhotoSize() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getSize();
  }

  public String getFocalLength() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getFocalLength();
  }

  public HashMap<String, String> getAllExifInterfaceInfo() {
    if (mImageManager == null) {
      mImageManager = new Imageshowimpl(mContext);
    }
    return mImageManager.getAllExifInterfaceInfo();
  }

  public boolean setCurrentIndex(int filterType, int index) {
    final boolean bb= mPlayList.setCurrentIndex(filterType, index);
    return bb;
  }

  public int getNextIndex(int filterType, int playMode) {
    final int idx = mPlayList.getNextIndex(filterType, playMode);
    return idx;
  }

  public String getCurrentFileName(int fileType) {
    return mPlayList.getCurrentFileName(fileType);
  }

  public String getCurrentFilePath(int fileType) {
    return mPlayList
        .getCurrentPath(fileType);
  }

  public String getFilePathByIndex(int filterType, int index) {
    return mPlayList.getFilePathByIndex(filterType, index);
  }

  public String getTextPageSize() {
    // Modified by Dan for fix bug DTV00375633
    int currentPos = mPlayList.getCurrentIndex(Const.FILTER_TEXT) + 1;
    int count = mPlayList.getFileNum(Const.FILTER_TEXT);

    String result = "";
    if (currentPos > 0 && count > 0) {
      result = currentPos + "/" + count;
    }

    return result;
  }

  public String getTextAlbum() {
    String album = mPlayList.getCurrentFileName(Const.FILTER_TEXT);
    int start = 0;
    if (album != null) {
      start = album.indexOf(".");
      if (start + 1 < album.length()) {
        album = album.substring(start + 1);
      } else {
        album = "";
      }
    } else {
      album = "";
    }
    return album + " ...";
  }

  public String getTextSize() {
    long length = mPlayList.getCurrentFileSize(Const.FILTER_TEXT);
    return length + " Byte";
  }

  public String getNextName(int type) {
    return mPlayList.getNextFileName(type);
  }

  public String getCurrentPhotoPath() {
    String path = mPlayList.getCurrentFileName(Const.FILTER_IMAGE);
    if (null == path || path.length() <= 0) {
      return "";
    } else {
      return path;
    }
  }

  public void setThrdPhotoCompelet(IThrdListener thrdPhotoListener) {

    //if (mPhotoPlayback == null) {
    //  return;
    //}
    //mPhotoPlayback.setEventListener(thrdPhotoListener);
  }

  // Added by Dan for fix bug DTV00389362
  private int mLrcOffsetMode = 0;
  private int mLrcEncodingMode = 0;

  public void setLrcOffsetMode(int lrcOffsetMode) {
    mLrcOffsetMode = lrcOffsetMode;
  }

  public int getLrcOffsetMode() {
    return mLrcOffsetMode;
  }

  public void setLrcEncodingMode(int lrcEncodingMode) {
    mLrcEncodingMode = lrcEncodingMode;
  }

  public int getLrcEncodingMode() {
    return mLrcEncodingMode;
  }

  /**
   * Delete
   */
  public void setPicSetting() {

    String fileName = getCurrentPhotoPath();
    if (fileName != null &&
        fileName.toLowerCase(Locale.ROOT).endsWith(".mpo")) {
      is3DPhotoMpo = true;
    } else {
      is3DPhotoMpo = false;
    }
    MtkLog.d(TAG, "LogicManager setPicSetting after fileName = " + fileName + "is3DPhotoMpo ="
        + is3DPhotoMpo);
    if (is3DPhotoMpo) {
      mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE, 1);
    } else {
      mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE, 0);
    }
    MtkLog.d(
        TAG,
        "LogicManager afterSet 3d mode:"
            + mConfig.getConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE));
  }

  public void setUnLockPin(int pin) {
    if (null != mVideoManager) {
      mVideoManager.setUnLockPin(pin);
    }
  }

  public void reset3Dsetting() {
    mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_3D_MODE, 0);
  }

  private AudioFocusRequest mRequest = null;

  private void registerAudioFocusListener() {
    AudioAttributes mAttributeMusic =
            new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
    AudioManager mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
    mRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(mAttributeMusic)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(mAudioFocusListener)
            .setWillPauseWhenDucked(true)
            .build();
    mAudioManager.requestAudioFocus(mRequest);
  }

  private boolean isAudioFocused = false;
  private boolean mIsAutoPause = false;

  private final OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
    @Override
    public void onAudioFocusChange(int focusChange) {

      Log.v(TAG, "AudioFocusListener focusChange = " + focusChange);

      switch (focusChange) {
        case android.media.AudioManager.AUDIOFOCUS_LOSS:
        case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        case android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
          isAudioFocused = false;
          if (isAudioPlaying()){
            pauseAudio();
            mIsAutoPause = true;
          }
          break;

        case android.media.AudioManager.AUDIOFOCUS_GAIN:
          isAudioFocused = true;
          if (mIsAutoPause && isAudioPause()){
            playAudio();
            mIsAutoPause = false;
          }
          break;
         default:
             break;

      }

    }
  };

  public void startVideoFromDrm() {
    if (mVideoManager != null) {
      mVideoManager.startVideoFromDrm();
    }
  }

  public long getDivxLastMemoryFileID() {

    if (null != mVideoManager) {
      return mVideoManager.getDivxLastMemoryFileID();
    } else {
      MtkLog.i(TAG, "null != mVideoManager");
      return -1;
    }
  }

  public LastMemoryFilePosition getLastMemoryFilePositionExo() {
    if (null != mVideoManager) {
      MtkLog.i(TAG, "null != mVideoManager");
      return mVideoManager.getLastMemoryFilePositionExo();
    } else {
      return null;
    }
  }

  public int setLastMemoryFilePositionExo(LastMemoryFilePosition info) {
    int index = -1;
    if (null != mVideoManager) {
      index = mVideoManager.setLastMemoryFilePositionExo(info);
    } else {
      MtkLog.i(TAG, "null == mVideoManager");
    }
    return index;
  }

  public void setExternalSubtitleEncodingType(String encoding) {
    MtkLog.i(TAG, "setExternalSubtitleEncodingType");
    if (null != mVideoManager) {
      mVideoManager.setExternalSubtitleEncodingType(encoding);
    }
  }

  public int getAudioTrackIndex() {
    int index = -1;
    if (null != mVideoManager) {
      index = mVideoManager.getAudioTrackIndex();
    } else {
      MtkLog.i(TAG, "LogicManager getAudioTrackIndex null == mVideoManager");
    }
    return index;

  }

  public int getSubtitleIndex() {
    int index = -1;
    if (null != mVideoManager) {
      index = mVideoManager.getSubtitleIndex();
    } else {
      MtkLog.i(TAG, "mVideoManager == NULL");
    }
    MtkLog.i(TAG, "LogicManager getSubtitleIndex index = " + index);
    return index;
  }

  public void setVolume(boolean isUp) {
    if (isMute()) {
      setMute();
    } else {
      int curVol = getVolume();
      if (isUp) {
        int max = getMaxVolume();
        curVol += 1;
        if (curVol < max) {
          curVol = max;
        }
      } else {
        curVol -= 1;
        if (curVol < 0) {
          curVol = 0;
        }
      }
      MtkLog.i(TAG, "curVol:" + curVol + "---- isUp:" + isUp);
      setVolume(curVol);
    }
  }

  public void setPlayStatus(int status) {
    if (null != mVideoManager) {
      mVideoManager.setPlayStatus(status);
    }
  }

  public void setTunnelingMode (int tunnelingMode) {
    if (null != mVideoManager) {
      mVideoManager.setTunnelingMode(tunnelingMode);
    }
  }

  public boolean isHideSpectrum() {
    return mmpset.isHideSpectrum();
  }

  public void setHideSpectrum(boolean hide) {
    mmpset.setHideSpectrum(hide);
  }

  public void setSubtitleEncodingType(String encodingType) {
    mmpset.setSubtitleEncodingType(encodingType);
  }

  public String getSubtitleEncodingType() {
    return mmpset.getSubtitleEncodingType();
  }

}
