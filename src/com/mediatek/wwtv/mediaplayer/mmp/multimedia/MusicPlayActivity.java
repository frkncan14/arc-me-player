
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.List;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;

import com.mediatek.ExoMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.LyricTimeContentInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.LrcView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ScoreView;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity.PlayException;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache.DecodeInfo;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;

public class MusicPlayActivity extends MediaPlayActivity {

  private static final String TAG = "MusicPlayActivity";

  private static final int PROGRESS_CHANGED = 0;

  private static final int PROGRESS_START = 1;
  // Spectrum
  private static final int PROGRESS_SCOREVIEW = 2;

  private static final int AUDIO_CHANGED = 3;

  private static final int AUDIO_RESET = 4;

  private static final int NOSUPPORT_PLAYNEXT = 5;
  private static final int SPEED_UPDATE = 6;
  private static final int FINISH_AUDIO = 7;
  private static final int CLEAR_LRC = 8;
  private static final int DISMISS_NOT_SUPPORT = 10;
  // for Gamekit start
  private static final int LOAD_GAMEKIT_VIEW = 13;
  private static final int HANDLE_ERROR_MSG = 14;
  // private static final int LOAD_GAMEKIT_RESUME = 14;
  private static final int DELAY_LOADGAMEKIT_MILLS = 1;
  //private boolean mIsClose3D = false;
  // for Gamekit end
  private static final int DELAYMILLIS = 400;
  private static final int DELAYMILLIS_FOR_PROGRESS = 500;

  private static final int SEEK_DURATION = 3000;

  // //add by xudong chen 20111204 fix DTV00379662
  public static final long SINGLINE = 1;
  public static final long MULTILINE = 8;
  public static final long OFFLINE = 0;
  // end
  private LinearLayout vLayout;

  private ImageView vThumbnail;

  private ScoreView mScoreView;

  private LrcView mLrcView;
  private boolean isDuration = false;
  private boolean mIsSeeking;
  private int mSeekingProgress;
  private List<LyricTimeContentInfo> lrcmap;

  //private final boolean playFlag = true;
  private boolean isActivityLiving = true;
//  private boolean retrunFromTipDismis = false;
  private boolean isCenterKey2Pause = false;
  private int mAudioSource = 0;
  private int mAudioFileType = 0;

  private final Handler myHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      MtkLog.d(TAG, " music msg.what:" + msg.what);
      switch (msg.what) {

      // add for play 3D animal start
        case LOAD_GAMEKIT_VIEW: {
          break;
        }
        case HANDLE_ERROR_MSG:
          MtkLog.d(TAG, "HANDLE_ERROR_MSG ~~");

          mLogicManager.stopAudio();

          sendEmptyMessageDelayed(NOSUPPORT_PLAYNEXT, 3000);

          if (isNotSupport) {
            mScoreView.setVisibility(View.INVISIBLE);
            removeMessages(PROGRESS_SCOREVIEW);

          }

          break;

        // by lei add for play 3D animal end
        case PROGRESS_CHANGED: {

          if (hasMessages(PROGRESS_CHANGED)) {
            removeMessages(PROGRESS_CHANGED);
          }
          MtkLog.d(TAG,
              "PROGRESS_CHANGED mLogicManager.getAudioStatus() = " + mLogicManager.getAudioStatus()
                  + "isDuration =" + isDuration);
          if (mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_PREPARED) {
            break;
          }

          if (mControlView != null) {

            int progress = 0;
            if (isDuration) {
              progress = mLogicManager.getPlaybackProgress();
            } else {
              progress = mLogicManager.getAudioBytePosition();

            }

            if (progress >= 0) {
              MtkLog.i(TAG, "PROGRESS_CHANGED progress:" + progress);
              if (mLogicManager.getAudioStatus() != AudioConst.PLAB_STATUS_SEEKING) {
                mControlView.setCurrentTime(progress);
                mControlView.setProgress(progress);
              }
            }
          }
          if (!mIsSeeking) {
            sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYMILLIS_FOR_PROGRESS);
          }
          break;
        }
        case PROGRESS_START: {
          if (null == lrcmap || (lrcmap.isEmpty())
              || null == mLrcView) {
            return;
          }
          int line = mLogicManager.getLrcLine(mLogicManager
              .getPlaybackProgress());

          if (line != -1) {
            mLrcView.setlrc(line, false);
          }

          if (line == lrcmap.size() - 1) {
            return;
          }

          sendEmptyMessageDelayed(PROGRESS_START, DELAYMILLIS);
          break;
        }

        case PROGRESS_SCOREVIEW: {
          if (mLogicManager.isHideSpectrum()) {
            return;
          }

          if (mLogicManager.isMute()){
            mScoreView.setVisibility(View.INVISIBLE);
          } else {
            mScoreView.setVisibility(View.VISIBLE);
          }

          if (hasMessages(PROGRESS_SCOREVIEW)) {
            removeMessages(PROGRESS_SCOREVIEW);
          }
          mScoreView.update(mLogicManager.getAudSpectrum());
          mScoreView.invalidate();
          if (mLogicManager.isAudioPlaying()) {
            sendEmptyMessageDelayed(PROGRESS_SCOREVIEW, DELAYMILLIS);
          }

          break;
        }
        case AUDIO_CHANGED: {
          setMusicInfo();
          break;
        }
        case AUDIO_RESET: {
          resetMusicInfo();
          break;
        }
        case NOSUPPORT_PLAYNEXT:
          if (isActivityLiving) {
            MtkLog.i(TAG, "  NOSUPPORT_PLAYNEXT: dismissNotSupprot");
            dismissNotSupprot();
            dismissMenuDialog();
          }
          mIsSeeking = false;
          mLogicManager.playNextAudio();
          break;
        case SPEED_UPDATE:
          MtkLog.i(TAG, "  SPEED_UPDATE  speed:" + SPEED_UPDATE);
          // set play icon.
          if (mControlView != null) {
            mLogicManager.setAuidoSpeed(1);
            mControlView.onFast(1, 1, Const.FILTER_AUDIO);
            setMusicInfo();
            mControlView.play();
          }
          break;
        // add by keke 1215 fix DTV00380491
        case FINISH_AUDIO: {
          /* fix cr DTV00386326 by lei 1228 */
          mLogicManager.unbindService(MusicPlayActivity.this);
          mLogicManager.stopAudio();
          MusicPlayActivity.this.finish();
        }
          break;
        case CLEAR_LRC:
          clearLrc();
          break;

        case DISMISS_NOT_SUPPORT:
          if (isActivityLiving) {
            MtkLog.i(TAG, "  DISMISS_NOT_SUPPORT: dismissNotSupprot");
            dismissNotSupprot();
          }
          break;

        // end
        default:
          break;
      }

    }
  };


  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      /* add by lei for fix cr 386020 */
      if (isNotSupport || null == mLogicManager.getAudioPlaybackService()) {
        return;
      }

      /* add by lei for fix cr DTV00381177&DTV00390959 */
      MtkLog.e(TAG, "***********hide Spectrum****************"
          + mLogicManager.isHideSpectrum());
      if (!mLogicManager.isHideSpectrum()) {
        // add by keke 2.1 for DTV00393701
        mScoreView.clearTiles();
        mScoreView.setVisibility(View.VISIBLE);
        myHandler.sendEmptyMessage(PROGRESS_SCOREVIEW);
      }
      myHandler.sendEmptyMessage(PROGRESS_CHANGED);
      myHandler.sendEmptyMessage(PROGRESS_START);
      mLogicManager.playAudio();

    }

    @Override
    public void pause() {
      /* add by lei for fix cr 386020 */
      if (isNotSupport || null == mLogicManager.getAudioPlaybackService()) {
        return;
      }
      // change by shuming fix CR 00386020
      try {
        mLogicManager.pauseAudio();
      } catch (Exception e) {
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        MtkLog.i(TAG, "e.getMessage()=="+e.getMessage().toString());
        if ((AudioConst.MSG_ERR_CANNOTPAUSE).equals(e.getMessage()
            .toString())
            && isCenterKey2Pause) {
          isCenterKey2Pause = false;
          // displayErrorMessage(AudioConst.MSG_FILE_NOT_SUPPORT, 0);
          //if (isActivityLiving) {
            // myHandler.sendEmptyMessageDelayed(DISMISS_NOT_SUPPORT,3000);
          //}
        }
      }
      // end

      // mScoreView.clearTiles();
      // mScoreView.setVisibility(View.INVISIBLE);

      /* add by lei for fix cr DTV00381177&DTV00390959 */
      myHandler.removeMessages(PROGRESS_SCOREVIEW);
      // change by shuming fix CR DTV00
      myHandler.removeMessages(PROGRESS_START);
      MtkLog.i(TAG, "myHandler.removeMessages(PROGRESS_CHANGED) before");
      myHandler.removeMessages(PROGRESS_CHANGED);
      MtkLog.i(TAG, "myHandler.removeMessages(PROGRESS_CHANGED) after");

    }
  };

  // MEDIAPLAYER

  private final MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
    // @Override if add "override" P4 will build failure
    @Override
    public boolean onError(MediaPlayer arg0, final int what,
        final int extra) {
      MtkLog.i(TAG, " MediaPlayer.OnErrorListener  OnErrorListener  targ1:" + what + "  arg2"
          + extra
          + " " + System.currentTimeMillis());

      return handleError(what, extra);
    }
  };

  private final MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {

    @Override
    public boolean onInfo(MediaPlayer arg0, int arg1, int arg2) {
      // TODO Auto-generated method stub
      return handleInfo(arg1);

    }
  };

  private final MediaPlayer.OnPreparedListener
  mPreparedListener = new MediaPlayer.OnPreparedListener() {

    @Override
    public void onPrepared(MediaPlayer mp) {
      handlePrepare();
    }

  };

  /* true playing(ff, fr,fb), else stop or pause */
  // private boolean isPlay = true;
  private final MediaPlayer.OnCompletionListener
  mCompletionListener = new MediaPlayer.OnCompletionListener() {

    @Override
    public void onCompletion(MediaPlayer mp) {

      handleComplete();
    }

  };

  private final MediaPlayer.OnSeekCompleteListener
  mSeekCompletionListener = new MediaPlayer.OnSeekCompleteListener() {

    @Override
    public void onSeekComplete(MediaPlayer mp) {

      handleSeekComplete();
    }

  };

  private boolean handleError(final int what, final int extra) {
    MtkLog.i(TAG, "handleError isNotSupport = " + isNotSupport + "what = " + what);
    if (isNotSupport || !isActivityLiving) {
      return true;
    }

    switch (what) {
      case ExoMediaPlayer.MEDIA_ERROR_FILE_NOT_SUPPORT:
      // case ExoMediaPlayer.MEDIA_ERROR_OPEN_FILE_FAILED:
        isNotSupport = true;
        featureNotWork(getResources()
            .getString(R.string.mmp_file_notsupport));
        myHandler.sendEmptyMessage(HANDLE_ERROR_MSG);
        break;
      case ExoMediaPlayer.MEDIA_ERROR_FILE_CORRUPT:
        isNotSupport = true;
        featureNotWork(getResources().getString(R.string.mmp_file_corrupt));
        myHandler.sendEmptyMessage(HANDLE_ERROR_MSG);
        break;
      default:

        MtkLog.i(TAG, "displayErrorMessage what = " + what + "extra = " + extra);
        // featureNotWork(getResources().getString(R.string.mmp_file_notsupport));
        break;
    }

    return true;

  }

  private boolean handleInfo(int arg1) {
    MtkLog.d(TAG, "handleInfo arg1 = " + arg1);
    switch (arg1) {
      case AudioConst.MSG_POSITION_UPDATE:
//        MtkLog.i(TAG, "handleInfo MSG_POSITION_UPDATE PROGRESS_CHANGED ");
//        myHandler.sendEmptyMessage(PROGRESS_CHANGED);
        break;

      case AudioConst.MEDIA_INFO_METADATA_COMPLETE:
        myHandler.sendEmptyMessage(FINISH_AUDIO);
        // isNotSupport = true;
        return false;
      case ExoMediaPlayer.MEDIA_INFO_ON_REPLAY:
        // myHandler.sendEmptyMessage(SPEED_UPDATE);
        mLogicManager.replayAudio();
        break;
      case ExoMediaPlayer.MEDIA_INFO_AUDIO_ENCODE_FORMAT_UNSUPPORT:
        if (isNotSupport) {
          return false;
        }
        isNotSupport = true;
        playExce = PlayException.AUDIO_NOT_SUPPORT;
        featureNotWork(getResources().getString(R.string.mmp_audio_notsupport));
        if (null != mControlView) {
          mControlView.setPauseIconGone();
        }
        myHandler.sendEmptyMessage(HANDLE_ERROR_MSG);

        break;
      case ExoMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
        MtkLog.d(TAG, "enter onInfo: MEDIA_INFO_NOT_SEEKABLE");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        break;
      case AudioConst.MEDIA_INFO_FEATURE_NOT_SUPPORT:
        MtkLog.i(TAG, "AudioConst.MEDIA_INFO_FEATURE_NOT_SUPPORT");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        break;
      case AudioConst.MEDIA_INFO_DATA_BEFORE_SET_DATA_SOURCE_STATE:
        isNotSupport = false;
        playExce = PlayException.DEFAULT_STATUS;
        myHandler.sendEmptyMessage(AUDIO_RESET);
        // resetMusicInfo();
        break;
      case AudioConst.MEDIA_INFO_PLAY_RENDERING_START:
        Log.d(TAG, "handleInfo rendering start ");
        setMediaPlaybackState(PlaybackState.STATE_PLAYING, 1);
        isRenderingStarted = true;
        myHandler.sendEmptyMessage(AUDIO_CHANGED);
        if (mControlView != null ) {
            mControlView.initMusicTrackNumber();
        }
        break;
      default:
        MtkLog.d(TAG, "enter onInfo:" + arg1);
        break;
    }
    return false;

  }

  private void handlePrepare() {
    MtkLog.i(TAG, " audio  OnPrepared   -------------- ");
    /* add by lei for fix cr 386020 */

    myHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYMILLIS_FOR_PROGRESS);
    myHandler.sendEmptyMessage(PROGRESS_START);
    removeScore(mLogicManager.isHideSpectrum());
    reSetController();

  }

  private void handleComplete() {
    MtkLog.i(TAG, "-------------- Completion ----------------- flag=");
    if (mScoreView != null) {
      mScoreView.clearTiles();
      mScoreView.invalidate();
    }
    finishSetting();
    myHandler.sendEmptyMessage(CLEAR_LRC);
    removeMessages();
    myHandler.sendEmptyMessage(NOSUPPORT_PLAYNEXT);

  }

  private void handleSeekComplete() {
    MtkLog.d(TAG, "handleSeekComplete!!!!mIsSeeking:" + mIsSeeking);
    mIsSeeking = false;
    if (mControlView != null) {
      mControlView.setMediaPlayState();
    }
  }

  private void setMusicInfo() {
    // Added by Dan for fix bug DTV00384892
    new Thread(new Runnable() {
        public void run() {
            lrcmap = mLogicManager.getLrcInfo();
            initLrc(mPerLine);
        }
    }).start();
    isDuration = true;
    if (mControlView != null) {
      // getTotalPlaybackTime return 0 or minus,progressbar shows size/total-size DTV00595824
      int times = mLogicManager.getTotalPlaybackTime();
      // Modified by yongzheng for fix CR DTV00388558 12/1/12
      if (!isNotSupport) {
        mControlView.showProgress();
        mControlView.setCurrentTime(0);
        if (times <= 0) {
          // progressFlag = true;
          isDuration = false;
          mControlView.setTimeViewVisibility(false);
          // progressbar shows size/total-size,starttime && endtime hide
          times = (int) mLogicManager.getAudioFileSize();
        }
        mControlView.setProgressMax(times);
        mControlView.setEndtime(times);
      }
    }

    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setAudioView();
    }

    final String path = mLogicManager.getCurrentPath(Const.FILTER_AUDIO);
    if (null != path) {
      final BitmapCache cache = BitmapCache.createCache(false);
      BitmapCache.DecodeInfo cover = cache.getDecodeInfo(path);

      if (cover != null) {
        MtkLog.i("xxxxxxxxxxx", "-----------Hit Cache------- path: " + path);
        vThumbnail.setImageBitmap(Util.getScaledBitmap(cover.getBitmap()));
      } else {

        new Thread(new Runnable() {

          @Override
          public void run() {
            final Bitmap bmp = mLogicManager.getAlbumArtwork(mAudioFileType,
                path, vThumbnail.getWidth(), vThumbnail.getHeight());

            MtkLog.i(TAG, "setMusicInfo load bmp= " + bmp);

            final boolean isFailed = (bmp == null ? false : true);
            BitmapCache.DecodeInfo info = cache.new DecodeInfo(bmp, isFailed);
            cache.putDecodeInfo(path, info);
            vThumbnail.post(new Runnable() {

              @Override
              public void run() {
                MtkLog.i(TAG, "setMusicInfo setbmp result = " + bmp);
                vThumbnail.setImageBitmap(Util.getScaledBitmap(bmp));
              }

            });
          }

        }).start();

      }

    }
  }

  public void resetMusicInfo() {
    MtkLog.d(TAG, "resetMusicInfo!~ dismissNotSupprot");
    dismissMenuDialog();
    dismissNotSupprot();
    isNotSupport = false;
    if (mControlView != null) {
      mControlView.reSetAudio();
      mControlView.setProgress(0);
      mControlView.hideProgress();
      mControlView.setRepeat(Const.FILTER_AUDIO);
      mControlView.setVolumeMax(maxVolume);
      mControlView.setCurrentVolume(currentVolume);
      mControlView.setFileName(mLogicManager
          .getCurrentFileName(Const.FILTER_AUDIO));
      mControlView.setFilePosition(mLogicManager.getAudioPageSize());

    }

    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setAudioView();
    }

    removeMessages();

    if (null != vThumbnail) {
      vThumbnail.setImageBitmap(null);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_musicplay);
    getScreenWH();
    turnOfAnimation();
    findView();

    String dataStr = getIntent().getDataString();
    if ((dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      mAudioSource = AudioConst.PLAYER_MODE_LOCAL;
      mAudioFileType = FileConst.SRC_USB;
      autoTest(Const.FILTER_AUDIO, MultiFilesManager.CONTENT_AUDIO);
      MtkLog.d(TAG,"onCreate autoTest");
    } else {
      getIntentData();
    }

    initData();
    // add by keke for fix DTV00380638
    mControlView.setRepeatVisibility(Const.FILTER_AUDIO);
    int repeatMode = getIntent().getIntExtra("repeatMode", 0);
    MtkLog.d(TAG,"onCreate,repeatMode=="+repeatMode);
    if (1 == repeatMode){
        mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
            Const.REPEAT_ONE);
    }
    setRepeatMode();
    showPopUpWindow(vLayout);
    if (mControlView != null ) {
        mControlView.initMusicTrackNumber();
    }
    MtkLog.d(TAG,"onCreate");
  }

  private void turnOfAnimation() {
    //mIsClose3D = true;
  }

  /**//**
       * Set Spreum status, true hide, false display.
       */
  /*
   * private boolean mIsHideSperum = false;
   */

  @Override
  public void removeScore(boolean ishide) {

    if (ishide) {
      MtkLog.i(TAG, "removeScore:ishide " + ishide);
      // vLayout.setVisibility(View.GONE);
      myHandler.removeMessages(PROGRESS_SCOREVIEW);
      mScoreView.setVisibility(View.GONE);
    } else {
      mScoreView.clearTiles();
      mScoreView.setVisibility(View.VISIBLE);
      myHandler.sendEmptyMessageDelayed(PROGRESS_SCOREVIEW, DELAYMILLIS);
    }

  }

  private void removeScore() {

    vLayout.setVisibility(View.GONE);
    myHandler.removeMessages(PROGRESS_SCOREVIEW);
    mScoreView.setVisibility(View.GONE);
  }

  public void removeScorePause() {
    mScoreView.setVisibility(View.INVISIBLE);
  }

  public void initLrc(int perline) {
    myHandler.removeMessages(PROGRESS_START);
    mLrcView.setVisibility(View.VISIBLE);
    if (null != lrcmap && !lrcmap.isEmpty()) {
      MtkLog.d(TAG, "perline:" + perline);
      mLrcView.init(lrcmap, perline);
      myHandler.sendEmptyMessageDelayed(PROGRESS_START, DELAYMILLIS);
    } else {
      mLrcView.noLrc(getString(R.string.mmp_info_nolrc));
    }
  }

  @Override
  public void setLrcLine(int perline) {
    if (null != mLrcView) {
     mLrcView.setVisibility(View.VISIBLE);
      if (null != lrcmap && !lrcmap.isEmpty()) {

        mLrcView.setLines(perline);
        int progress = mLogicManager.getPlaybackProgress();
        if (progress >= 0) {
          int currentline = mLogicManager.getLrcLine(progress);
          if (-1 != currentline){
              mLrcView.setlrc(currentline, true);
          }

          if (currentline == lrcmap.size() - 1) {
            return;
          }
        }
        myHandler.sendEmptyMessageDelayed(PROGRESS_START, DELAYMILLIS);
      }
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void hideLrc() {
    myHandler.removeMessages(PROGRESS_START);
    mLrcView.setVisibility(View.INVISIBLE);
  }

  private void getIntentData() {

    mAudioSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();
    MtkLog.d(TAG,"onCreate mAudioSource=="+mAudioSource);

    switch (mAudioSource) {
      case MultiFilesManager.SOURCE_LOCAL:
        mAudioSource = AudioConst.PLAYER_MODE_LOCAL;
        mAudioFileType = FileConst.SRC_USB;
        break;
      case MultiFilesManager.SOURCE_SMB:
        mAudioSource = AudioConst.PLAYER_MODE_SAMBA;
        mAudioFileType = FileConst.SRC_SMB;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        mAudioSource = AudioConst.PLAYER_MODE_DLNA;
        mAudioFileType = FileConst.SRC_DLNA;
        break;
      default:
        break;
    }
  }

  private void initData() {
    mLogicManager = LogicManager.getInstance(this);
    mLogicManager.setAudioPreparedListener(mPreparedListener);
    mLogicManager.setAudioSeekCompletionListener(mSeekCompletionListener);
    mLogicManager.setAudioCompletionListener(mCompletionListener);
    mLogicManager.setAudioErrorListener(mErrorListener);
    mLogicManager.setAudioInfoListener(mInfoListener);

    mIsSeeking = false;
    mLogicManager.initAudio(this, mAudioSource);

    initVulume(mLogicManager);
    // isNotSupport = true;
    isNotSupport = false;
    isActivityLiving = true;
    // add by xudong fix cr DTV00385993
//    retrunFromTipDismis = false;
    isCenterKey2Pause = false;
    // end

  }

  private void findView() {
    vLayout = (LinearLayout) findViewById(R.id.mmp_music_top);
    vThumbnail = (ImageView) findViewById(R.id.mmp_music_img);
    mScoreView = (ScoreView) findViewById(R.id.mmp_music_tv);
    mLrcView = (LrcView) findViewById(R.id.mmp_music_lrc);

    getPopView(R.layout.mmp_popupmusic, MultiMediaConstant.AUDIO,
        mControlImp);

    mControlView.setFilePosition(mLogicManager.getAudioPageSize());
    // Delay load gamekit view.
    myHandler.sendEmptyMessageDelayed(LOAD_GAMEKIT_VIEW, DELAY_LOADGAMEKIT_MILLS);
  }

  /**
   * {@inheritDoc} fix bug DTV00365251 by lei add.
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (mLogicManager.isAudioOnly()) {
      /* by lei add for fix cr DTV00390970 */
      if (event.getAction() == KeyEvent.ACTION_UP) {
        mLogicManager.setAudioOnly(false);
      }
      return true;
    }

    return super.dispatchKeyEvent(event);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "keyCode:" + keyCode);
    keyCode = KeyMap.getKeyCode(keyCode, event);
    keyCode = Util.changeKeycodeToRtl(keyCode);
    MtkLog.i(TAG, "onKeyDown later keycode:" + keyCode);

    textToSpeech(keyCode);

    switch (keyCode) {

      case KeyMap.KEYCODE_DPAD_CENTER:
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        // add by shuming fix CR 00386020
        // if (mControlView.isPlaying()) {
        if (mLogicManager.isAudioPlaying()) {
          isCenterKey2Pause = true;
        } else {
          isCenterKey2Pause = false;
        }
        // end
      case KeyMap.KEYCODE_MTKIR_PLAY:
          isCenterKey2Pause = false;
      case KeyMap.KEYCODE_MTKIR_PAUSE:
          isCenterKey2Pause = true;
        // setPlayerStop(false);
        if (isNotSupport) {
          return true;
        }
        // Added by yognzheng for fix CR DTV00390968 16/1/12
        if (mTipsDialog != null
            && mTipsDialog.isShowing()
            && mTipsDialog.getTitle().equals(
                getResources().getString(
                    R.string.mmp_file_notsupport))) {
          mTipsDialog.dismiss();
        }
        break;
      // Added by yognzheng for fix CR DTV00390968 16/1/12
      /*
       * case KeyMap.KEYCODE_MENU: { if (mTipsDialog != null && mTipsDialog.isShowing() &&
       * mTipsDialog.getTitle().equals( getResources().getString( R.string.mmp_file_notsupport))) {
       * mTipsDialog.dismiss(); } } break;
       */
      case KeyMap.KEYCODE_MTKIR_CHDN:
      case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
        playPre();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_CHUP:
      case KeyMap.KEYCODE_MTKIR_NEXT: {
        playNext();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
      case KeyMap.KEYCODE_MTKIR_REWIND:
      case KeyMap.KEYCODE_DPAD_LEFT:
//        MtkLog.i(TAG, "KEYCODE_DPAD_LEFT");
      case KeyMap.KEYCODE_DPAD_RIGHT: {
        MtkLog.i(TAG, "KEYCODE_DPAD_RIGHT");
        if (isNotSupport || mLogicManager.isAudioFast()) {//|| mIsSeeking
          return true;
        }
        // add by xiaojie fix cr DTV00381177
        // if (mLogicManager.isAudioPause()) {
        // mScoreView.setVisibility(View.INVISIBLE);
        // }
        // end
        // add by xiaojie fix cr DTV00381234
        String fileNotSupport = this.getResources().getString(
            R.string.mmp_file_notsupport);
        if (mTipsDialog != null && mTipsDialog.isShowing()
            && mTipsDialog.getTitle().equals(fileNotSupport)) {
          mTipsDialog.dismiss();
          // add by xudong fix cr DTV00385993
//          retrunFromTipDismis = true;
          // end
          return true;

        }

        // end

        // add by xudong fix cr DTV00385993
//        retrunFromTipDismis = false;
        // end
        return seek(keyCode);
      }
//      case KeyMap.KEYCODE_MTKIR_REWIND: {
//        MtkLog.i(TAG, "KeyMap.KEYCODE_MTKIR_REWIND :" + keyCode);
//        if (mLogicManager.isAudioStoped()) {
//          MtkLog.i(TAG, "isAudioStoped");
//          return true;
//        }
//        MtkLog.i(TAG, "mLogicManager.getAudioStatus():" + mLogicManager.getAudioStatus());
//        if (isValid() && mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STOPPED) {
//          if (mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STARTED) {
//            featureNotWork(getString(R.string.mmp_featue_notsupport));
//            return true;
//          }
//          MtkLog.i(TAG, "KEYCODE_MTKIR_REWIND");
//          try {
//            mLogicManager.fastRewindAudio();
//            setFast(1);
//          } catch (IllegalStateException e) {
//            MtkLog.d(TAG, "Exception" + e.getMessage());
//            featureNotWork(getString(R.string.mmp_featue_notsupport));
//          } catch (Exception e) {
//            MtkLog.d(TAG, "Exception" + e.getMessage());
//            featureNotWork(getString(R.string.mmp_featue_notsupport));
//          }
//        } else {
//          MtkLog.i(TAG, "KEYCODE_MTKIR_REWIND:novaild");
//        }
//
//        return true;
//      }
//      case KeyMap.KEYCODE_MTKIR_FASTFORWARD: {
//        MtkLog.i(TAG, "KEYCODE_MTKIR_FASTFORWARD");
//        if (mLogicManager.isAudioStoped()) {
//          MtkLog.i(TAG, "KEYCODE_MTKIR_FASTFORWARD isAudioStoped() == TRUE");
//          return true;
//        }
//        if (isValid() && mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STOPPED) {
//          MtkLog
//              .i(TAG,
//                  "KEYCODE_MTKIR_FASTFORWARD isValid()" +
//                  "&& mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STOPPED");
//          if (mLogicManager.getAudioStatus() < AudioConst.PLAY_STATUS_STARTED) {
//            MtkLog
//                .i(TAG,
//                    "KEYCODE_MTKIR_FASTFORWARD mLogicManager.getAudioStatus()" +
//                    "<  AudioConst.PLAY_STATUS_STARTED");
//            featureNotWork(getString(R.string.mmp_featue_notsupport));
//            return true;
//          }
//          try {
//            mLogicManager.fastForwardAudio();
//            MtkLog.i(TAG, "KEYCODE_MTKIR_FASTFORWARD fastForwardAudio");
//            setFast(0);
//          } catch (IllegalStateException e) {
//            MtkLog.d(TAG, "Exception" + e.getMessage());
//            featureNotWork(getString(R.string.mmp_featue_notsupport));
//          } catch (Exception e) {
//            MtkLog.d(TAG, "Exception" + e.getMessage());
//            if (mLogicManager.getPlayStatus() == AudioConst.PLAY_STATUS_FF) {
//              try {
//                mLogicManager.fastForwardAudioNormal();
//                setFast(0);
//              } catch (Exception ex) {
//                featureNotWork(getString(R.string.mmp_featue_notsupport));
//              }
//            } else {
//              featureNotWork(getString(R.string.mmp_featue_notsupport));
//            }
//          }
//        }
//        return true;
//      }
      case KeyMap.KEYCODE_MTKIR_STOP: {
        /* add by lei 1228 */
        if (isNotSupport) {
          return true;
        }
        // Added by yognzheng for fix CR DTV00390968 16/1/12
        if (mTipsDialog != null
            && mTipsDialog.isShowing()
            && mTipsDialog.getTitle().equals(
                getResources().getString(
                    R.string.mmp_file_notsupport))) {
          mTipsDialog.dismiss();
        }
        mLogicManager.stopAudio();
        stop();
        return true;
      }
      case KeyMap.KEYCODE_BACK: {
        removeScore();
        removeControlView();
        finish();
        break;
      }
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

    private void textToSpeech(int keyCode) {
        String textString = null;

        switch (keyCode) {
            case KeyMap.KEYCODE_DPAD_CENTER:
            case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
                if (null != mControlView && mControlView.isPlaying()){
                    textString = " pause";
                } else {
                    textString = " play";
                }
                break;

            case KeyMap.KEYCODE_MTKIR_PLAY:
                textString = " play";
                break;

            case KeyMap.KEYCODE_MTKIR_PAUSE:
                textString = " pause";
                break;

            case KeyMap.KEYCODE_MTKIR_STOP:
                textString = " stop";
                break;

            case KeyMap.KEYCODE_MTKIR_REWIND:
                textString = " rewind";
                break;

            case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
                textString = " fast forward";
                break;
			default:
				break;
        }

        if (null != textString && null != MediaMainActivity.getInstance() && null != myTtsUtil){
            MtkLog.d(TAG,"musicPlayActivity,textToSpeech,textString=="+textString);
            myTtsUtil.speak(textString);
        }
    }

  public void stop() {

    removeMessages();
    // Add by yongzheng for fix CR DTV00379673
    // setPlayerStop(true);
    if (null != mLrcView && null != lrcmap && !lrcmap.isEmpty()) {
      mLrcView.noLrc(null);
    }
    // end
    if (null != mControlView) {
      mControlView.setCurrentTime(0);
      mControlView.setProgress(0);
      mControlView.stop();
    }
    if (null != mScoreView) {
      mScoreView.setVisibility(View.INVISIBLE);
    }

  }

  @Override
  public void finish() {
    MtkLog.d(TAG, "finish()");
    removeScore();
    removeControlView();
    setResult(100, null);
    super.finish();
  }

  private void removeMessages() {
    myHandler.removeMessages(PROGRESS_START);
    myHandler.removeMessages(PROGRESS_CHANGED);
    myHandler.removeMessages(PROGRESS_SCOREVIEW);
    myHandler.removeMessages(AUDIO_CHANGED);
    myHandler.removeMessages(SPEED_UPDATE);
    myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
  }

  private void removeLoadGamekitMessage() {
    MtkLog.e(TAG, "removeLoadGamekitMessage()");
    myHandler.removeMessages(LOAD_GAMEKIT_VIEW);
    // myHandler.removeMessages(LOAD_GAMEKIT_RESUME);
  }
/*  private void setFast(int isForward) {

    int speed = mLogicManager.getAudioSpeed();
    if (speed == 0) {
      return;
    }

    if (null == mControlView) {
      return;
    }
    // hideFeatureNotWork();

    if (!myHandler.hasMessages(PROGRESS_CHANGED)) {
      myHandler.sendEmptyMessage(PROGRESS_CHANGED);
    }
    if (!myHandler.hasMessages(PROGRESS_SCOREVIEW)) {
      myHandler.sendEmptyMessage(PROGRESS_SCOREVIEW);

    }
    if (!myHandler.hasMessages(PROGRESS_CHANGED)) {
      myHandler.sendEmptyMessage(PROGRESS_CHANGED);
    }
    mControlView.onFast(speed, isForward, Const.FILTER_AUDIO);
  }
  */

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "onkeyup keyCode:" + keyCode);
    if (keyCode == KeyMap.KEYCODE_DPAD_LEFT
        || keyCode == KeyMap.KEYCODE_DPAD_RIGHT
        || keyCode == KeyMap.KEYCODE_MTKIR_REWIND
        || keyCode == KeyMap.KEYCODE_MTKIR_FASTFORWARD) {
      if (mLogicManager.isAudioFast()) {
        return true;
      }
//      // add by keke 2.1 for DTV00393701
//      if (mLogicManager.getPlayStatus() == AudioConst.PLAY_STATUS_PAUSED) {
//        removeScorePause();
//      }
      if (mIsSeeking) {
        try {
          MtkLog.i(TAG, "seek progress:" + mSeekingProgress);
          if (! isAudioSeekable()){
            return true;
          }

          mLogicManager.seekToCertainTime(mSeekingProgress);
          // mControlView.setCurrentTime(progress);
          // mControlView.setProgress((int) progress);
        } catch (Exception e) {
          MtkLog.i(TAG, "Seek exception");
          mIsSeeking = false;
          //featureNotWork(getString(R.string.mmp_featue_notsupport));
          return true;
        }
//        // Added by yongzheng for fix CR DTV00379673
//        if (getPlayerStop()) {
//          removeMessages();
//          return true;
//        }
//        // end
//        // modified by keke for fix DTV00381199
//        if (hasLrc()) {
//          myHandler.sendEmptyMessage(PROGRESS_START);
//        }
//        if (!mHandler.hasMessages(PROGRESS_CHANGED)) {
//          mHandler.sendEmptyMessageDelayed(PROGRESS_CHANGED, DELAYMILLIS_FOR_PROGRESS);
//        }
      }
      // DTV00710486
      // add "&& !retrunFromTipDismis" by xudong fix cr DTV00385993
      /*
       * if (playFlag && !retrunFromTipDismis) { if(null != mControlView){
       * mControlView.setMediaPlayState(); } }
       */
    }

    return super.onKeyUp(keyCode, event);
  }


  private boolean isAudioSeekable() {
    if (!mLogicManager.canSeek()) {
      featureNotWork(getString(R.string.mmp_featue_notsupport));
      MtkLog.d(TAG, "isAudioSeekable false: canseek is false");
      return false;
    }

    int totalDuration = mLogicManager.getTotalPlaybackTime();
    MtkLog.d(TAG, "isAudioSeekable totalDuration=="+totalDuration);
    if (totalDuration <= 0){
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        MtkLog.d(TAG, "isAudioSeekable false: audio duration issue");
        return false;
    }

    return true;
  }

  private boolean seek(int keyCode) {
    if (null == mControlView) {
      return true;
    }
    /*
     * //if (mControlView.isPlaying()) { if(mControlView.isPlaying()){ playFlag = true;
     * mControlView.setMediaPlayState(); } else if (event.getRepeatCount() == 0) { playFlag = false;
     * }
     */


    if (!isAudioSeekable()) {
      MtkLog.i(TAG, "!isAudioSeekable()");
      return true;
    }

    if (mLogicManager.isAudioPlaying()) {
      mLogicManager.pauseAudio();
    }
    if (myHandler.hasMessages(PROGRESS_CHANGED)) {
      myHandler.removeMessages(PROGRESS_CHANGED);
    }
//    if (progress < 0) {
//      return true;
//    }
    if (!mIsSeeking) {
      mIsSeeking = true;
      int progressTemp = mLogicManager.getPlaybackProgress();
      mSeekingProgress = progressTemp;//progressTemp & 0xffffffffL;
    }
    if (keyCode == KeyMap.KEYCODE_DPAD_LEFT
        || keyCode == KeyMap.KEYCODE_MTKIR_REWIND) {
      mSeekingProgress = mSeekingProgress - SEEK_DURATION;
      if (mSeekingProgress < 0) {
        mSeekingProgress = 0;
      }
    } else {
      mSeekingProgress = mSeekingProgress + SEEK_DURATION;
      int totalProgressTemp = mLogicManager.getTotalPlaybackTime();
//      long totalProgress = totalProgressTemp & 0xffffffffL;
      if (mSeekingProgress > totalProgressTemp) {
        mSeekingProgress = totalProgressTemp;
      }
    }
    MtkLog.i(TAG, "seek progress calc:" + mSeekingProgress);
    if (mLogicManager.getAudioStatus() != AudioConst.PLAB_STATUS_SEEKING) {
      mControlView.setCurrentTime(mSeekingProgress);
      mControlView.setProgress(mSeekingProgress);
    }
    return true;
  }

  @Override
  public boolean hasLrc() {
      return !(null == lrcmap || (lrcmap.isEmpty()) || null == mLrcView);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Util.logLife(TAG, "onResume");
    if (mControlView != null && !mControlView.isShowed()) {
      reSetController();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPause() {
    super.onPause();
    // if (!mIsClose3D && mSurfaceView != null) mSurfaceView.onPause();
    //removeMessages();
    removeLoadGamekitMessage();
    Util.logLife(TAG, "onPause");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    removeMessages();
    isActivityLiving = false;
    if (mLogicManager.getAudioPlaybackService() != null) {
      mLogicManager.unbindService(this);
    }
    super.onDestroy();
    Util.logLife(TAG, "onDestroy");
  }

  public void clearLrc() {
    if (mLrcView != null && mLrcView.getVisibility() == View.VISIBLE
        && null != lrcmap && !lrcmap.isEmpty()) {
      mLrcView.noLrc("");
      lrcmap = null;
    }
  }

  /**
   * Audio wheather stop
   */
  // Added by yongzheng for fix CR DTV00379673 and DTV00388521
  // private boolean isMusicStop = false;

  @Override
  protected boolean getPlayerStop() {
    // return false;
    return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STOPPED;
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    if (mLogicManager != null) {
      mLogicManager.stopAudio();
    }
  }


  public void playNext() {
      MtkLog.d(TAG,"playNext");
      if (isValid()) {
          dismissNotSupprot();
          myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
          // add by xiaojie fix cr DTV00379650
          myHandler.removeMessages(CLEAR_LRC);
          myHandler.sendEmptyMessage(CLEAR_LRC);
          // end
          mIsSeeking = false;
          mLogicManager.playNextAudio();
          myHandler.removeMessages(PROGRESS_START);
      }
  }

  public void playPre() {
      MtkLog.d(TAG,"playPre");
      if (isValid()) {
          dismissNotSupprot();
          myHandler.removeMessages(NOSUPPORT_PLAYNEXT);
          // add by xiaojie fix cr DTV00379650
          myHandler.removeMessages(CLEAR_LRC);
          myHandler.sendEmptyMessage(CLEAR_LRC);
          // end
          mIsSeeking = false;
          mLogicManager.playPrevAudio();
          myHandler.removeMessages(PROGRESS_START);
      }
  }
}
