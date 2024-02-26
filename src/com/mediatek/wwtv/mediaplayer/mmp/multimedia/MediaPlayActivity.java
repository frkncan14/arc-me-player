
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.File;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.mediatek.twoworlds.tv.MtkTvTVCallbackHandler;
import com.mediatek.twoworlds.tv.model.MtkTvVideoInfoBase;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvAVModeBase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ScoreView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ShowInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.ModelConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.IRootMenuListener;
import com.mediatek.wwtv.mediaplayer.setting.SettingActivity;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.IDmrListener;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.TextToSpeechUtil;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.util.TVContent;
import com.mediatek.wwtv.mediaplayer.mmp.util.TextUtils;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;

import android.graphics.Color;
import android.graphics.Typeface;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.media.MediaMetadata;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import java.util.Arrays;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;

import static android.media.AudioManager.ACTION_HDMI_AUDIO_PLUG;
import static android.media.AudioManager.EXTRA_AUDIO_PLUG_STATE;

/**
 * Multi-media play activty
 *
 * @author hs_weihuiluo
 */
public class MediaPlayActivity extends Activity {

  /**
   * Log tag
   */
  protected static final String TAG = "MediaPlayActivity";

  /**
   * Media content type :photo audio video text
   */
  protected static int sMediaType = 0;

  /**
   * Repeat key duration
   */
  public static final int KEY_DURATION = 400;

  /**
   * Message to dismiss feature not support dialog
   */
  private static final int MSG_DISMISS_FEARTURE_NOT_SUPPORT = 1;

  /**
   * Message to show feature not support dialog
   */
  private static final int MSG_SHOW_FEATURE_NOT_SUPPORT = 2;

  private static final int MSG_HIDE_INFORBAR_POINT = 4;

  private static final int MSG_HIDE_FEATURE_NOT_SUPPORT = 8; // arcelik customized

  /**
   * Update not support tips dialog delay milliseconds
   */
  private static final int MSG_DMR_SET_MUTE = 1001;
  private static final int MSG_SET_MUTE = 1003;
  private static final int MSG_DMR = 1002;

  /**
   * Dismiss feature not support tips dialog delay milliseconds
   */
  protected static final int MSG_DISMISS_DELAY = 2000;

  public static final int DELAYED_LONG = 8000;

  public static final int DELAYED_MIDDLE = 5000;

  public static final int DELAYED_SHORT = 3000;

  protected TVContent mTvContent;

  /**
   * Last key down milliseconds
   */
  protected long mLastKeyDownTime;

  /**
   * for user press epg key , avoid auto next play.
   */
  protected boolean EPG_KEY_PRESS = false;

  /**
   * {@link Resources}
   */
  private Resources mResources;

  /**
   * The screen width
   */
  protected int mDisPlayWidth;

  protected int mRotateDegree = Const.ROTATE_DEGREE_0;

  /**
   * The screen height
   */
  protected int mDisPlayHeight;

  protected boolean mIsAutoPause;

  /**
   * {@link ControlView}
   */
  protected ControlView mControlView;
  /**
   * {@link ControlView}
   */

  /**
   * Show menu dialog
   */
  protected MenuListView menuDialog;

  protected MenuListView menuDialogFontList;

  /**
   * Show Lyric view
   */
  protected ScoreView mScoreView;

  /**
   * Show info view
   */
  protected ShowInfoView mInfo;

  /**
   * {@link LogicManager}
   */
  protected LogicManager mLogicManager;

  protected AudioManager mAudioManager;

  TextToSpeechUtil myTtsUtil = null;

  protected boolean isRenderingStarted = false;

  private String titleValue = "";

  private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

    @Override
    public void onAudioFocusChange(int focusChange) {
      if (!MediaPlayActivity.this.isInPictureInPictureMode()) {
        MtkLog.d(TAG, "onAudioFocusChange, this is not in pip, return");
        return;
      }
      MtkLog.d(TAG, "onAudioFocusChange focusChange:" + focusChange);
      if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
          focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
        if (mControlView != null) {
          if (mControlView.isPlaying()) {
            mIsAutoPause = true;
            mControlView.pause();
          }
        }
      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        if (isGooglePause) {
          isGooglePause = false;
        } else {
          if (mControlView != null && mControlView.isPause() && mIsAutoPause) {
            mIsAutoPause = false;
            mControlView.play();
          }
        }
      }
    }
  };

  /**
   * Max volume value
   */
  protected int maxVolume = 0;

  /**
   * The current volume value
   */
  protected int currentVolume = 0;

  /**
   * Lyric lines per screen
   */
  protected static int mPerLine = 8;

  /**
   * Tips dialog
   */
  protected TipsDialog mTipsDialog;

  /**
   * Control bar contentView
   */
  protected View contentView;
  /**
   * metedata bar contentView
   */

  /**
   * Resume from capureLog flag
   */
  protected boolean isBackFromCapture = false;

  public static final String AUTO_TEST_PROPERTY = "vendor.mtk.auto_test";
  public static final String AUTO_TEST_DLNA_PROPERTY = "vendor.mtk.dlna.auto_test";
  public static final String AUTO_TEST_SAMBA_PROPERTY = "vendor.mtk.samba.auto_test";
  public static final String CMPB_PROPERTY = "vendor.mtkmmp.use.cmpb";

  /**
   * Not support flag
   */

  protected boolean SCREENMODE_NOT_SUPPORT = false;
  protected boolean isSetPicture = false;

  // add by keke for DTV00383992
  protected boolean isNotSupport = false;

  /**
   * Last not support content(used to switch from feature not support)
   */
  public enum PlayException {
    DEFAULT_STATUS, VIDEO_NOT_SUPPORT, VIDEO_ONLY, AUDIO_NOT_SUPPORT,
    FILE_NOT_SUPPORT, AV_NOT_SUPPORT
  }

  protected PlayException playExce = PlayException.DEFAULT_STATUS;

  /**
   * The current not support content
   */
  protected String mTitle;
  public static String mPhotoFramePath;
  public static int mPhotoFrameSource;

  // Added by Dan for fix bug DTV00373545
  private boolean mIsMute;

  // add for fix bug DTVDTV00392376
  private boolean mIsActiveLiving = true;

  protected ScheduledThreadPoolExecutor stpe = null;

  /**
   * {@link ListView.OnItemClickListener}
   */
  private final ListView.OnItemClickListener mListener = new ListView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
        long arg3) {
      TextView tvTextView = (TextView) arg1
          .findViewById(R.id.mmp_menulist_tv);
      String content = tvTextView.getText().toString();

      final MenuFatherObject mnuItem = (MenuFatherObject) arg0.getAdapter().getItem(pos);
      if (null != mnuItem) {
        final int id = mnuItem.getId();
        if (MenuFatherObject.MENU_INVALID_ID != id) {
          if (true == controlStateById(id)) {
            return;
          }
        }
      }
      controlState(content);
    }
  };

  private HandlerThread mHandlerThead;
  protected Handler mThreadHandler;

  /**
   * An handler used to send message
   */
  protected Handler mHandler = new Handler() {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      // add for fix bug DTVDTV00392376
      if (!mIsActiveLiving) {
        return;
      }
      MtkLog.i(TAG, " handleMessage msg.what:" + msg.what);
      switch (msg.what) {
        case MSG_DISMISS_FEARTURE_NOT_SUPPORT: {
          MtkLog.i(TAG, " handleMessage mIsEnterPip:" + Util.mIsEnterPip);
          if (Util.mIsEnterPip) {
            return;
          }

          if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
            if (mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED
                || mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_ERROR) {
              dismissNotSupprot();
            } else {
              onNotSuppsort(mResources.getString(R.string.mmp_audio_notsupport));
            }
          } else if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
            if (mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED
                || mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_ERROR) {
              dismissNotSupprot();
            } else {
              onNotSuppsort(mResources.getString(R.string.mmp_video_notsupport));
            }
          } else if (playExce == PlayException.VIDEO_ONLY) {
            if (mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED
                || mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_ERROR) {
              dismissNotSupprot();
            } else {
              onNotSuppsort(mResources.getString(R.string.mmp_video_only));
            }
          } else if (playExce == PlayException.FILE_NOT_SUPPORT) {
            break;
          } else {
            MtkLog.i(TAG, "MSG.WHAT==1 else dismissNotSupprot");
            dismissNotSupprot();
          }

          break;
        }
        case MSG_SHOW_FEATURE_NOT_SUPPORT: {
          onNotSuppsort(mTitle);
          break;
        }

        case MSG_HIDE_INFORBAR_POINT:
          if (mControlView != null) {
            if (mControlView.getWidth() == 1 && mControlView.getHeight() == 1) {
              mControlView.update(-1, -1, -1, -1);
              // mControlView.hiddlen(View.INVISIBLE);
            }
          }
          break;
        case MSG_SET_MUTE:
          onMute();
          break;
        case MSG_DMR_SET_MUTE:
          // reSetController();
          // onMute();
          break;
        case MSG_DMR:
          // reSetController();
          // onMute();
          Log.i(TAG, " handdmrEvent:" + MSG_DMR);
          handleDmrEvent(msg.arg1, msg.arg2);
          Log.i(TAG, " handdmrEvent: end:" + MSG_DMR);
          break;

        case MSG_HIDE_FEATURE_NOT_SUPPORT: { // arcelik customized
          dismissNotSupprot();
          break;
        }
        default:
          break;
      }
    }

  };
  public boolean isDmrSource = false;
  public IDmrListener mDmrListener = null;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int isSupportOLED = MtkTvAVMode.getInstance().getVideoInfoValue(MtkTvAVModeBase.VIDEOINFO_TYPE_OLED_SUPPORT_COMPENSATION);
    if(isSupportOLED != 1)
    {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    MmpApp des = (MmpApp) this.getApplication();
    des.add(this);
    des.registerRootMenu(mRootMenuListener);
    Intent it = getIntent();
    if (it != null) {
      isDmrSource = it.getBooleanExtra(DmrHelper.DMRSOURCE, false);
      if (isDmrSource == false) {
        DmrHelper.setHandler(mHandler);
      } else {
        MtkFilesBaseListActivity.setFromDmr();
      }
    }

    mRotateDegree = Const.ROTATE_DEGREE_0;
    mTvContent = TVContent.getInstance(this);
    mResources = this.getResources();
    mLogicManager = LogicManager.getInstance(this.getApplicationContext());
    // mAudioManager.requestAudioFocus(mAudioFocusListener,
    // AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    // if (MultiFilesManager.getInstance(getApplicationContext()).getContentType()
    // == MultiFilesManager.CONTENT_VIDEO
    // && CommonSet.VID_SCREEN_MODE_NORMAL == mLogicManager.getCurScreenMode()) {
    // setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
    // }
    // Added by Dan for fix bug DTV00373545
    mIsMute = mLogicManager.isMute();

    // add for fix bug DTVDTV00392376
    mIsActiveLiving = true;

    Util.logLife(TAG, "onCreate pip:" + "   " + Util.mIsEnterPip);
    IntentFilter filter = new IntentFilter();
    // filter.addAction(AudioManager.STREAM_MUTE_CHANGED_ACTION);
    // filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
    filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
    registerReceiver(mReceiver, filter);
    registerReceivers();
    mHandlerThead = new HandlerThread(TAG);
    mHandlerThead.start();
    mThreadHandler = new Handler(mHandlerThead.getLooper());
    if (null != MediaMainActivity.getInstance()) {
      myTtsUtil = MediaMainActivity.getInstance().getTTSUtil();
    }
    initAudioFocus();
    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
    registBroadcast();
    VideoInfoCallbackHandler videoInfoCallbackHandler = new VideoInfoCallbackHandler();
  }

  private AudioFocusRequest mRequest = null;

  private void initAudioFocus() {
    AudioAttributes mAttributeMusic = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build();
    mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    mRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(mAttributeMusic)
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(mAudioFocusListener)
        .setWillPauseWhenDucked(true)
        .build();
  }

  public static MediaSession mMediaSession;
  public MediaMetadata.Builder mMetadataBuilder;
  public PlaybackState.Builder mPb;
  public boolean isMusicPlay = false;

  private void createMediaSession() {
    // Create the MediaSession
    Log.d(TAG, "createMediaSession");

    if (null != mMediaSession) {
      if (MediaPlayActivity.this instanceof MusicPlayActivity && isMusicPlay) {
        MtkLog.d(TAG, "music mMediaSession return");
        return;
      }

      MtkLog.d(TAG, "mMediaSession release");
      if (mMediaSession.isActive()) {
        setMediaPlaybackState(PlaybackState.STATE_NONE, 0);
      }
      mMediaSession.release();
    }

    if (MediaPlayActivity.this instanceof MusicPlayActivity) {
      isMusicPlay = true;
    } else {
      isMusicPlay = false;
    }
    mMediaSession = new MediaSession(this, "MultiMediaPlayer");
    mMetadataBuilder = new MediaMetadata.Builder();
    mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
        | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
    mMediaSession.setCallback(new MediaSession.Callback() {
      @Override
      public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
        Log.d(TAG, "onMediaButtonEvent,mediaButtonEvent==" + mediaButtonEvent);
        if (mediaButtonEvent != null) {
          return handleMediaKey(
              (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT));
        }
        return false;
      }
    });

    MtkLog.d(TAG, "setPlaybackState STATE_PLAYING,value is 3");
    setMediaPlaybackState(PlaybackState.STATE_PLAYING, 1);

    MtkLog.d(TAG, "setActive true");
    mMediaSession.setActive(true);

    MtkLog.d(TAG, "setMetadata title");
    mMetadataBuilder = new MediaMetadata.Builder();
    mMetadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, "MMP");
    mMediaSession.setMetadata(mMetadataBuilder.build());

    mControlView.setMediaSession(mMediaSession, mPb);

  }

  public void setMediaPlaybackState(int playbackState, int playbackSpeed) {
    Log.d(TAG, "setMediaPlaybackState, playbackState: " + playbackState);
    if (mMediaSession != null) {
      mPb = new PlaybackState.Builder();
      mPb.setState(playbackState, 0, playbackSpeed);
      mMediaSession.setPlaybackState(mPb.build());
    }
  }

  private boolean isGooglePause = false;

  public boolean handleMediaKey(KeyEvent event) {
    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN
        && event.getRepeatCount() == 0) {
      Log.d(TAG, "handleMediaKey,event.getKeyCode()==" + event.getKeyCode());

      // Comment this part, make the icons can be updated even when mmp is not on top
      // For example, voice asssitant cases
      // if (!Util.isMMpActivity(getApplicationContext())) {
      // Log.d(TAG, "handleMediaKey,not in MMP");
      // return false;
      // }

      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        case KeyEvent.KEYCODE_HEADSETHOOK: {
          Log.d(TAG, "Received Play/Pause event from RemoteControlClient");
          return true;
        }
        case KeyEvent.KEYCODE_MEDIA_PLAY:
        case KeyEvent.KEYCODE_MEDIA_PAUSE: {
          Log.d(TAG, "Received Play or Pause event from RemoteControlClient");
          if (mControlView != null) {
            mControlView.setMediaPlayState();
            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE) {
              isGooglePause = true;
            }
          }
          reSetController();
          if (null != mControlView && menuDialog != null) {
            if (mControlView.isPlaying()) {
              menuDialog.updateItem(0,
                  getResources().getString(R.string.mmp_menu_pause));
            } else {
              menuDialog.updateItem(0,
                  getResources().getString(R.string.mmp_menu_play));
            }
          }
          return true;
        }
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS: {
          Log.d(TAG, "Received previous event from RemoteControlClient");
          playPre();
          return true;
        }
        case KeyEvent.KEYCODE_MEDIA_NEXT: {
          Log.d(TAG, "Received next event from RemoteControlClient");
          playNext();
          return true;
        }
        case KeyEvent.KEYCODE_MEDIA_STOP: {
          Log.d(TAG, "Received Stop event from RemoteControlClient");
          return true;
        }
        default:
          break;
      }
    }
    return false;
  }

  protected void getScreenWH() {
    if (SettingsUtil.SCREEN_WIDTH == 0 || SettingsUtil.SCREEN_HEIGHT == 0) {
      DisplayMetrics dm = new DisplayMetrics();
      this.getWindowManager().getDefaultDisplay().getMetrics(dm);
      SettingsUtil.SCREEN_WIDTH = dm.widthPixels;
      SettingsUtil.SCREEN_HEIGHT = dm.heightPixels;

      ScreenConstant.SCREEN_WIDTH = dm.widthPixels;
      ScreenConstant.SCREEN_HEIGHT = dm.heightPixels;
    }
  }

  protected boolean autoTest(int constFilter, int mulFilter) {

    if (!MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {

      String dataStr = getIntent().getDataString();
      Log.d(TAG, "autoTest dataStr:" + dataStr);
      if (dataStr != null && dataStr.length() > 0) {

        if (dataStr.contains("../")) {
          Log.d(TAG, "autoTest command contains ../ ,not safe");
          return false;
        }
        if (!(dataStr.startsWith("/mnt/media_rw/") || dataStr.startsWith("/storage/"))) {
          Log.d(TAG, "dataStr not startsWith /mnt/media_rw/ or /storage/");
          return false;
        }

        File f = new File(dataStr);
        if (!f.exists()) {
          Log.d(TAG, "autoTest !f.exists()");
          return false;
        }

        PlayList.getPlayList().cleanList(constFilter);
        LocalFileAdapter file = new LocalFileAdapter(new MtkFile(f));
        List<FileAdapter> files = new ArrayList<FileAdapter>();
        files.add(file);
        PlayList playlist = MultiFilesManager.getInstance(this).getPlayList(files, 0, mulFilter,
            MultiFilesManager.SOURCE_LOCAL);
        Log.d(TAG, "autoTest playlist path:" + playlist.getCurrentPath(constFilter));
        return true;
      }

    }

    return false;
  }

  protected boolean playLocalPvr(int constFilter, int mulFilter) {

    if (getIntent().getData() != null) {

      Bundle bundle = getIntent().getExtras();
      String dataStr = bundle.getString("PATH");
      String[] paths = bundle.getStringArray("PATHS");
      int position = bundle.getInt("POSITION");
      Log.d(TAG, "playLocalPvr dataStr:" + dataStr + " position:" + position + " paths.length:"
          + paths.length);
      String file = "";
      List<FileAdapter> files = new ArrayList<FileAdapter>();
      PlayList.getPlayList().cleanList(constFilter);
      for (String path : paths) {
        file = path;
        if (file != null && file.length() > 0) {
          if (file.contains("../")) {
            Log.d(TAG, "autoTest command contains ../ ,not safe");
            return false;
          }
          if (!(file.startsWith("/mnt/media_rw/") || file.startsWith("/storage/"))) {
            Log.d(TAG, "dataStr not startsWith /mnt/media_rw/ or /storage/");
            return false;
          }

          File f = new File(Paths.get(file).toUri());
          if (!f.exists()) {

            if (file.equals(dataStr)) {
              Log.d(TAG, "!f.exists()");
              return false;
            }
          } else {
            Log.d(TAG, "playLocalPvr f.exists() " + " --file:" + file);
            LocalFileAdapter fl = new LocalFileAdapter(new MtkFile(f));
            files.add(fl);
          }
        }

      }
      MultiFilesManager.getInstance(this).setCurrentSourceType(MultiFilesManager.SOURCE_LOCAL);
      Log.d(TAG, "playLocalPvr playlist  files.size():" + files.size());
      PlayList playlist = MultiFilesManager.getInstance(this).getPlayList(files, position,
          mulFilter, MultiFilesManager.SOURCE_LOCAL);
      if (playlist != null) {
        Log.d(TAG, "playLocalPvr playlist path:" + playlist.getCurrentPath(constFilter));
      } else {
        Log.i(TAG, "playLocalPvr Error playlist == null");
        finish();
      }
      return true;
    }
    return false;
  }

  /**
   * show not support tips dialog
   *
   * @param title
   *              the tips dialog content
   */
  private void onNotSuppsort(String title) {
    // new Exception().printStackTrace();
    if (titleValue.equals(title) && mTipsDialog != null && mTipsDialog.isShowing()) {
      return;
    } else {
      titleValue = title;
    }
    MtkLog.i(TAG, "onNotSuppsort  :" + title);
    if (null == mTipsDialog) {
      MtkLog.i(TAG, "null == mTipsDialog");
      mTipsDialog = new TipsDialog(this);
      mTipsDialog.setText(title);
      mTipsDialog.show();
      MtkLog.i(TAG, "null == mTipsDialog2");
      mTipsDialog.setBackground(R.drawable.toolbar_playerbar_test_bg);
      Drawable drawable = this.getResources().getDrawable(
          R.drawable.toolbar_playerbar_test_bg);

      int weight = (int) (drawable.getIntrinsicWidth() * 0.6);
      int height = drawable.getIntrinsicHeight();
      // mTipsDialog.setDialogParams(weight, height);

      int x = -((ScreenConstant.SCREEN_WIDTH) - weight / 2)
          + (ScreenConstant.SCREEN_WIDTH / 10);
      int y = (int) (ScreenConstant.SCREEN_HEIGHT * 3 / 8
          - ScreenConstant.SCREEN_HEIGHT * 0.16 - height / 2);
      mTipsDialog.setWindowPosition(x, y);

    } else {
      MtkLog.i(TAG, "null != mTipsDialog");
      try {

        // need updata tip text , eg:video codec not support show,user do ff/fr need tmp
        // show
        // feature not support
        /*
         * if(mTipsDialog!=null&&mTipsDialog.isShowing()){ return; }
         */
        if (mTipsDialog.isShowing()) {
          mTipsDialog.dismiss();
        }
        mTipsDialog.setText(title);
        mTipsDialog.show();
        MtkLog.i(TAG, "mTipsDialog.showing()");
      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }

    }
    // mLastText = title;
  }

  @Override
  public void finish() {
    dismissNotSupprot();

    super.finish();
  }

  /**
   * Show feature not support dialog
   *
   * @param title
   *              the dialog content
   */
  protected void featureNotWork(String title) {
    Log.d(TAG, "featureNotWork title:" + title + " xx " + Log.getStackTraceString(new Throwable()));
    mTitle = title;
    if (mHandler != null) {
      mHandler.sendEmptyMessage(MSG_SHOW_FEATURE_NOT_SUPPORT);
      if (mHandler.hasMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT)) {
        mHandler.removeMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT);
      }
      mHandler.sendEmptyMessageDelayed(MSG_DISMISS_FEARTURE_NOT_SUPPORT, MSG_DISMISS_DELAY);
      mHandler.sendEmptyMessageDelayed(MSG_HIDE_FEATURE_NOT_SUPPORT, DELAYED_MIDDLE); // arcelik customized

    }
  }

  /**
   * Remove feature not support messages
   */
  protected void removeFeatureMessage() {
    if (mHandler != null) {
      mHandler.removeMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT);
      mHandler.removeMessages(MSG_SHOW_FEATURE_NOT_SUPPORT);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStart() {
    super.onStart();
    Util.logLife(TAG, "onStart pip:" + Util.mIsEnterPip);
    if (mThreadHandler != null) {
      mThreadHandler.post(new Runnable() {

        @Override
        public void run() {
          if (sMediaType == MultiMediaConstant.VIDEO
              || sMediaType == MultiMediaConstant.AUDIO) {
            // mLogicManager.freeVideoResource();
            if (!Util.mIsUseEXOPlayer) {
              AudioBTManager.getInstance(getApplicationContext()).creatAudioPatch();
            }
          }
        }
      });
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (this instanceof VideoPlayActivity && mRequest != null) {
      mAudioManager.requestAudioFocus(mRequest);
    }
    if (mIsAutoPause) {
      if (mControlView != null && mControlView.isPause()) {
        mIsAutoPause = false;
        mControlView.play();
      }
    }
    Util.logLife(TAG, "onResume pip: " + Util.mIsEnterPip);
  }

  /**
   * Dismiss not support tips dialog
   */
  protected void dismissNotSupprot() {
    try {
      if (null != mTipsDialog) {
        // mTipsDialog.hide();
        // MtkLog.i(TAG, "dismissNotSupprot null != mTipsDialog && mTipsDialog.hide()"
        // + Log.getStackTraceString(new Throwable()));
        mTipsDialog.dismiss();
        titleValue = "";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void hiddenView() {
    try {
      if (null != mTipsDialog) {
        mTipsDialog.hide();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void dismissMenuDialog() {
    try {
      if (null != menuDialog && menuDialog.isShowing()) {
        menuDialog.dismiss();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Remove lyrics
   *
   * @param ishide
   *               is hidden lyrics view
   */
  public void removeScore(boolean ishide) {
    MtkLog.i(TAG, "removeScore" + ishide);
  }

  /**
   * Setup lyrics lines per screen
   *
   * @param perline
   *                line number
   */
  public void setLrcLine(int perline) {
    MtkLog.i(TAG, "setLrcLine");
  }

  /**
   * Is the current audio has lyrics
   *
   * @return true:has lyrics, false: no lyrics
   */
  public boolean hasLrc() {
    return false;
  }

  /**
   * Hidden lyrics view
   */
  public void hideLrc() {
    MtkLog.i(TAG, "hideLrc");
  }

  /**
   * Initialize volume
   *
   * @param manager
   */
  protected void initVulume(LogicManager manager) {
    mLogicManager = manager;
    maxVolume = mLogicManager.getMaxVolume();
    currentVolume = mLogicManager.getVolume();
    MtkLog.i(TAG, "maxVolume:" + maxVolume + "--currentVolume:=" + currentVolume);
    boolean isMute = mLogicManager.isMute();
    if (mControlView != null) {
      mControlView.setMute(isMute);
    }
    if (DmrHelper.isDmr()) {
      if (!isMute) {
        DmrHelper.notifyVolume(this, currentVolume, 0);
      } else {
        DmrHelper.notifyVolume(this, currentVolume, 1);
      }
    }
  }

  /**
   * Initialize control bar
   *
   * @param resource
   *                   Control bar Layout resource id
   * @param mediatype
   *                   Media type
   * @param controlImp
   *                   ControlPlayState:control play or pause
   */
  protected void getPopView(int resource, int mediatype,
      ControlPlayState controlImp) {
    sMediaType = mediatype;
    contentView = LayoutInflater.from(this).inflate(
        resource, null);
    mDisPlayWidth = ScreenConstant.SCREEN_WIDTH;
    mDisPlayHeight = ScreenConstant.SCREEN_HEIGHT;
    mControlView = new ControlView(this, sMediaType, controlImp,
        contentView, mDisPlayWidth, mDisPlayHeight);

    if ((sMediaType == MultiMediaConstant.VIDEO)
        || (sMediaType == MultiMediaConstant.AUDIO)) {
      createMediaSession();
    }
  }

  /**
   * Show control bar
   *
   * @param topview
   *                Control bar parent view
   */
  protected void showPopUpWindow(final View topview) {
    Looper.myQueue().addIdleHandler(new IdleHandler() {

      @Override
      public boolean queueIdle() {
        MtkLog
            .i(TAG,
                "---------- showPopUpWindow   IdleHandler mControlView:" + mControlView);
        mControlView.showAtLocation(topview,
            Gravity.START | Gravity.TOP, mDisPlayWidth / 10,
            mDisPlayHeight / 20);
        // isControlBarShow = true;
        return false;
      }
    });
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    // if (keyCode == KeyMap.KEYCODE_VOLUME_UP || keyCode ==
    // KeyMap.KEYCODE_VOLUME_DOWN) {
    // return true;
    // }
    return super.onKeyUp(keyCode, event);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "keyCode:" + keyCode);
    keyCode = KeyMap.getKeyCode(keyCode, event);
    MtkLog.i(TAG, "onKeyDown later keycode:" + keyCode);

    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        if ((sMediaType == MultiMediaConstant.AUDIO) && null != menuDialog && menuDialog.isShowing()) {
          String playOrPauseString = null;
          if (mControlView.isPlaying()) {
            playOrPauseString = mResources.getString(R.string.mmp_menu_pause);
          } else {
            playOrPauseString = mResources.getString(R.string.mmp_menu_play);
          }
          controlState(playOrPauseString);
          break;
        }

      case KeyMap.KEYCODE_DPAD_CENTER: {
        if (mControlView != null) {
          if (!mControlView.isPlaying()
              && Util.mIsDolbyVision) {
            Util.showDoViToast(this);
          }
          mControlView.setMediaPlayState();
        }
        reSetController();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PLAY: {
        if (mControlView != null) {
          if (!mControlView.isPlaying()) {
            mControlView.play();
            if (Util.mIsDolbyVision) {
              Util.showDoViToast(this);
            }
          }
        }
        reSetController();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PAUSE: {
        if (mControlView != null) {
          if (!mControlView.isPause()) {
            mControlView.pause();
          }
        }
        reSetController();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_INFO: {
        onInfoClick();
        return true;
      }
      // case KeyMap.KEYCODE_VOLUME_UP:
      // MtkLog.d(TAG, "KEYCODE_VOLUME_UPs MediaType = " + sMediaType);
      // if (sMediaType == MultiMediaConstant.VIDEO
      // || sMediaType == MultiMediaConstant.AUDIO) {
      // reSetController();
      // }
      // if (mLogicManager.isMute()) {
      // Log.i(TAG, "mute true");
      // onMute();
      // if (currentVolume == 0) {
      // currentVolume = mLogicManager.getVolume();
      // }
      // mLogicManager.setVolume(currentVolume);
      // if (DmrHelper.isDmr()) {
      // DmrHelper.notifyVolume(this, currentVolume, 1);
      // }
      // return true;
      // } else {
      // Log.i(TAG, "unmute true");
      // }
      // if (currentVolume == 0) {
      // currentVolume = mLogicManager.getVolume();
      // }
      // currentVolume = currentVolume + 1;
      // if (currentVolume > maxVolume) {
      // currentVolume = maxVolume;
      // }
      // mLogicManager.setVolume(currentVolume);
      // mControlView.setCurrentVolume(currentVolume);
      // if (DmrHelper.isDmr()) {
      // DmrHelper.notifyVolume(this, currentVolume, 0);
      // }
      //
      // return true;
      // case KeyMap.KEYCODE_VOLUME_DOWN:
      // if (sMediaType == MultiMediaConstant.VIDEO
      // || sMediaType == MultiMediaConstant.AUDIO) {
      // reSetController();
      // }
      // if (mLogicManager.isMute()) {
      // onMute();
      // if (currentVolume == 0) {
      // currentVolume = mLogicManager.getVolume();
      // }
      // mLogicManager.setVolume(currentVolume);
      // if (DmrHelper.isDmr()) {
      // DmrHelper.notifyVolume(this, currentVolume, 1);
      // }
      // return true;
      // }
      // if (currentVolume == 0) {
      // currentVolume = mLogicManager.getVolume();
      // }
      // currentVolume = currentVolume - 1;
      // if (currentVolume < 0) {
      // currentVolume = 0;
      // }
      // mLogicManager.setVolume(currentVolume);
      // mControlView.setCurrentVolume(currentVolume);
      // if (DmrHelper.isDmr()) {
      // DmrHelper.notifyVolume(this, currentVolume, 0);
      // }
      // return true;
      case KeyMap.KEYCODE_MENU:
        if (!isValid()) {
          return true;
        }
        MtkLog.d(TAG, "KeyMap.KEYCODE_MENU");
        reSetController();
        if (null != menuDialog && menuDialog.isShowing()) {
          menuDialog.dismiss();

        } else {
          showDialog();
        }
        break;
      case KeyMap.KEYCODE_MTKIR_ANGLE: {
        // Util.exitMmpActivity(this);
        break;
      }
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        reSetController();
        onRepeat();
        updateInfoView();
        break;
      }
      // case KeyMap.KEYCODE_MTKIR_MUTE: {
      // if (isValid()) {
      // if (sMediaType == MultiMediaConstant.VIDEO
      // || sMediaType == MultiMediaConstant.AUDIO) {
      // reSetController();
      // }
      // onMute();
      // }
      // return true;
      // }
      case KeyMap.KEYCODE_BACK: {
        handBack();
        break;
      }
      case KeyMap.KEYCODE_MTKIR_GUIDE: {
        // if (isValid()) {
        // EPG_KEY_PRESS = true;
        // EPG_KEY_PRESS = Util.startEPGActivity(this);
        // }
        break;
      }
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  protected void handleDmrPlayPause(int state) {
    Log.i(TAG, "state:" + state);
    reSetController();
    if (mControlView != null) {
      mControlView.setMediaPlayState();
    }
    if (state == DmrHelper.DLNA_DMR_PAUSE) {
      Log.d(TAG, "handleDmrPlayPause");
      DmrHelper.tellDmcState(this, 1);
    } else {
      Log.d(TAG, "handleDmrPlayPause");
      DmrHelper.tellDmcState(this, 0);
    }
  }

  protected void handleDmrStop() {
    DmrHelper.tellDmcState(this, 2);
  }

  protected void handBack() {
    dismissMenuDialog();
    dismissNotSupprot();
    removeControlView();
  }

  /**
   * Is the current key down valid
   *
   * @return true:valid,false:invalid
   */
  protected boolean isValid() {
    long currentTime = System.currentTimeMillis();
    if ((currentTime - mLastKeyDownTime) >= KEY_DURATION) {
      mLastKeyDownTime = currentTime;
      return true;
    } else {
      MtkLog.i(TAG, " key down duration :"
          + (currentTime - mLastKeyDownTime) + "< 400 mmm");
      mLastKeyDownTime = currentTime;
      return false;
    }
  }

  /**
   * Show or hidden info menu
   */
  private void onInfoClick() {
    MtkLog.d(TAG, "onInfoClick playExce:" + playExce);
    // if (playExce == PlayException.FILE_NOT_SUPPORT
    // || mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STARTED
    // || mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_STOPPED) {
    // return;
    // }
    if (mControlView != null && !mControlView.isShowed()) {
      reSetController();
      return;
    }
    if (null != mInfo && mInfo.isShowing()) {
      if (sMediaType != MultiMediaConstant.AUDIO) {
        hideController();
      }
      mInfo.dismiss();
      return;
    }
    hideControllerDelay();
    showinfoview(sMediaType);

  }

  protected void updateInfoView() {
    MtkLog.i(TAG, "updateInfoView");
    if (null != mInfo) {
      mInfo.updateView();
    }
  }

  /**
   * Set mute or resume from mute
   */
  public void onMute() {
    mIsMute = mLogicManager.isMute();
    Log.i(TAG, "mIsMute before:" + mIsMute);
    mLogicManager.setMute();
    // Added by Dan for fix bug DTV00373545
    mIsMute = !mIsMute;
    // mIsMute =mLogicManager.isMute();
    Log.i(TAG, "mIsMute later:" + mIsMute);
    // Modified by Dan for fix bug DTV00373545
    mControlView.setMute(mIsMute);
    removeScore(mIsMute);
    if (sMediaType == MultiMediaConstant.AUDIO) {
      if (menuDialog != null && menuDialog.isShowing()) {
        if (mIsMute) {
          menuDialog.setItemEnabled(4, false);

        } else {
          menuDialog.setItemEnabled(4, true);
        }
        String content = menuDialog.getItem(4).content;
        menuDialog.setItem(4, content);
      }
    }

  }
  /*
   * private void onMute(boolean isMute) {
   * if (isMute) {
   * mControlView.setMute(true);
   * } else {
   * mControlView.setMute(false);
   * int currentVolume = mLogicManager.getVolume();
   * mControlView.setCurrentVolume(currentVolume);
   * }
   * removeScore(isMute);
   * if (sMediaType == MultiMediaConstant.AUDIO) {
   * if (menuDialog != null && menuDialog.isShowing()) {
   * if (isMute) {
   * menuDialog.setItemEnabled(4, false);
   * } else {
   * menuDialog.setItemEnabled(4, true);
   * }
   * String content = menuDialog.getItem(4).content;
   * menuDialog.setItem(4, content);
   * }
   * }
   * }
   */

  /**
   * Switch repeat mode
   */
  protected void onRepeat() {
    MtkLog.i(TAG, "onRepeat~~");

    if (null == mControlView) {
      MtkLog.i(TAG, "onRepeat mControlView = null");
      return;
    }
    int type;
    switch (sMediaType) {
      case MultiMediaConstant.AUDIO: {
        type = Const.FILTER_AUDIO;
        break;
      }
      case MultiMediaConstant.VIDEO: {
        type = Const.FILTER_VIDEO;
        break;
      }
      case MultiMediaConstant.PHOTO: {
        type = Const.FILTER_IMAGE;
        break;
      }
      case MultiMediaConstant.TEXT: {
        type = Const.FILTER_TEXT;
        break;
      }
      case MultiMediaConstant.THRD_PHOTO: {
        type = Const.FILTER_IMAGE;
        break;
      }

      default:
        type = 0;
        break;
    }
    int model = mLogicManager.getRepeatModel(type);

    MtkLog.i(TAG, "onRepeat mediatype = " + type + "repeatmode = " + model);

    switch (model) {
      case Const.REPEAT_NONE: {
        mControlView.setRepeatSingle();
        mLogicManager.setRepeatMode(type, Const.REPEAT_ONE);
        break;
      }
      case Const.REPEAT_ONE: {
        mControlView.setRepeatAll();
        mLogicManager.setRepeatMode(type, Const.REPEAT_ALL);
        break;
      }
      case Const.REPEAT_ALL: {
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(type, Const.REPEAT_NONE);
        break;

      }
      default:
        break;
    }
    // writeRepeatMode(type);
  }

  /**
   * Show menu dialog
   */
  private void showDialog() {

    if (sMediaType == MultiMediaConstant.AUDIO) {
      MtkLog.d(TAG, "showDialog isNotSupport==" + isNotSupport);
      if (isNotSupport || getPlayerStop()) {
        List<MenuFatherObject> menunotList = GetDataImp.getInstance()
            .getComMenu(this,
                R.array.mmp_menu_musicplaynotsupportlist,
                R.array.mmp_menu_musicplaynotsupportlist_enable,
                R.array.mmp_menu_musicplaynotsupportlist_hasnext);
        menuDialog = new MenuListView(this, menunotList,
            mListener, null);
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_picture_off));
        MtkLog.i(TAG, "sTTSEnabled() 1=" + myTtsUtil.isTTSEnabled() + "; index =" + index);
        if (myTtsUtil.isTTSEnabled() && menunotList != null && index != -1) {
          menunotList.remove(index);
        }

        if (null != mLogicManager) {
          boolean isShuffle = mLogicManager
              .getShuffleMode(Const.FILTER_AUDIO);
          if (isShuffle) {
            menuDialog.setItem(1, mResources
                .getString(R.string.mmp_menu_shuffleoff));
          }
        }
        menuDialog.mControlView(this);
      } else if (!mLogicManager.isAudioStarted()) {
        MtkLog.d(TAG, "audio is not started");
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        return;
      } else {
        List<MenuFatherObject> menuList = GetDataImp.getInstance()
            .getComMenu(this,
                R.array.mmp_menu_musicplaylist,
                R.array.mmp_menu_musicplaylist_enable,
                R.array.mmp_menu_musicplaylist_hasnext);
        if (!hasLrc()) {
          if (menuList.size() > 5) {
            menuList.get(5).enable = false;
          }
        }

        if (!mLogicManager.isHideSpectrum()) {
          menuList.get(4).content = mResources
              .getString(R.string.mmp_menu_hidescore);
        } else {
          menuList.get(4).content = mResources
              .getString(R.string.mmp_menu_showscore);
        }
        menuDialog = new MenuListView(this, menuList,
            mListener, null);

        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_picture_off));
        MtkLog.i(TAG, "sTTSEnabled() 2=" + myTtsUtil.isTTSEnabled() + "; index =" + index);
        if (myTtsUtil.isTTSEnabled() && menuList != null && index != -1) {
          menuList.remove(index);
        }

        if (null != mLogicManager) {
          boolean isShuffle = mLogicManager
              .getShuffleMode(Const.FILTER_AUDIO);
          if (isShuffle) {
            menuDialog.setItem(2, mResources
                .getString(R.string.mmp_menu_shuffleoff));

          }

          int isDAPSwitch = MtkTvConfig.getInstance().getConfigValue(
              MtkTvConfigType.CFG_AUD_DOLBY_AUDIO_PROCESSING);
          MtkLog.i(TAG, "isDAPSwitch == " + isDAPSwitch);

          if (mLogicManager.isMute() || (isDAPSwitch != 0)) {
            menuDialog.setItemEnabled(4, false);
          } else {
            menuDialog.setItemEnabled(4, true);
          }
        }
        menuDialog.mControlView(this);
      }
    } else if (sMediaType == MultiMediaConstant.VIDEO) {
      int menuid = R.array.mmp_menu_videoplaylist;
      int menuenableid = R.array.mmp_menu_videoplaylist_enable;
      int menunextid = R.array.mmp_menu_videoplaylist_hasnext;
      if (!MultiFilesManager.isSourceLocal(getApplicationContext())) {
        menuid = R.array.mmp_menu_videoplaylist_net;
        menuenableid = R.array.mmp_menu_videoplaylist_enable_net;
        menunextid = R.array.mmp_menu_videoplaylist_hasnext_net;
      }
      if (true == Feature.isAospCnPlatform()) {
        if (Util.isUseExoPlayer()) {
          menuid = R.array.mmp_menu_videoplaylist_cn;
          menuenableid = R.array.mmp_menu_videoplaylist_enable_cn;
          menunextid = R.array.mmp_menu_videoplaylist_hasnext_cn;
        }
      }

      menuDialog = new MenuListView(this, GetDataImp
          .getInstance().getComMenu(this,
              menuid,
              menuenableid,
              menunextid),
          mListener, mCallBack);

      // Chapter and Edition
      if (mControlView != null) {
        if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_FF
            || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_FR
            || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_SF
            || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_SR
            || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STEP) {
          menuDialog.setItemEnableState(mResources.getString(R.string.mmp_divx_chapter), false);
          menuDialog.setItemEnableState(mResources.getString(R.string.mmp_divx_edition), false);
        }
      }

      // screen mode
      if (SCREENMODE_NOT_SUPPORT
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STOPPED) {
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_screenmode));
        if (index != -1) {
          menuDialog.removeItem(index);
        }
      } else {
        int picture_film_maker_mode = Settings.Global.getInt(getContentResolver(), "picture_film_maker_mode", 0);
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_screenmode), picture_film_maker_mode == 0);
      }
      // last memory
      if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STOPPED) {
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_last_memory));
        if (index != -1) {
          menuDialog.removeItem(index);
        }
      } else {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_last_memory), true);
      }

      // play speed
      if (true != Feature.isAospCnPlatform()) {
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_play_speed));
        if (index != -1) {
          menuDialog.removeItem(index);
        }
      }

      // soundtracks
      int audioTrackNumber = mLogicManager.getAudioTranckNumber();
      MtkLog.d(TAG, "audioTrackNumber==" + audioTrackNumber);
      if (audioTrackNumber < 2) {
        menuDialog.setItemEnableState(mResources.getString(R.string.menu_audio_sound_tracks), false);
      }

      // subtitle encoding
      short subtitleTrackNumber = mLogicManager.getSubtitleTrackNumber();
      short subtitleIndex = (short) (mLogicManager.getSubtitleIndex());
      MtkLog.d(TAG, "subtitleTrackNumber==" + subtitleTrackNumber + ",subtitleIndex==" + subtitleIndex);
      if (subtitleIndex < 0) {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_subtitle_encoding), false);
      }

      MtkLog.d(TAG, "mLogicManager.getMediaType():" + mLogicManager.getMediaType()
          + "  mLogicManager.getTSVideoNum():" + mLogicManager.getTSVideoNum());
      if (mLogicManager.getMediaType() == FileConst.MEDIA_TYPE_MPEG2_TS
          && mLogicManager.getTSVideoNum() > 0 && mLogicManager.isInPlaybackState()) {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_ts_program), true);
      } else {
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_ts_program));
        if (index != -1) {
          menuDialog.removeItem(index);
        }
      }
      if (SCREENMODE_NOT_SUPPORT) {
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_pic_setting));
        if (index != -1) {
          menuDialog.removeItem(index);
        }
      } else {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_pic_setting), true);
      }

    } else if (sMediaType == MultiMediaConstant.TEXT) {
      menuDialog = new MenuListView(this, GetDataImp
          .getInstance().getComMenu(this,
              R.array.mmp_menu_textplaylist,
              R.array.mmp_menu_textplaylist_enable,
              R.array.mmp_menu_textplaylist_hasnext),
          mListener,
          null);
      menuDialog.setItemEnabled(0, !isNotSupport);
      menuDialog.setItemEnabled(3, !isNotSupport);

      if (null != mLogicManager) {
        boolean isShuffle = mLogicManager
            .getShuffleMode(Const.FILTER_TEXT);
        if (isShuffle) {
          menuDialog.setItem(2, mResources
              .getString(R.string.mmp_menu_shuffleoff));

        }
      }
    } else if ((sMediaType == MultiMediaConstant.PHOTO) && (true == Feature.isAospCnPlatform())) {
      menuDialog = new MenuListView(this, GetDataImp
          .getInstance().getComMenuEx(this,
              R.array.mmp_menu_imageplaylist,
              R.array.mmp_menu_imageplaylist_enable,
              R.array.mmp_menu_imageplaylist_hasnext),
          mListener,
          null);
      if (mControlView.isPlaying()) {
        menuDialog.setItem(0, mResources
            .getString(R.string.mmp_menu_pause));
      } else {
        menuDialog.setItem(0, mResources
            .getString(R.string.mmp_menu_play));
      }
    } else {
      menuDialog = new MenuListView(this, GetDataImp
          .getInstance().getComMenu(this,
              R.array.mmp_menu_textplaylist,
              R.array.mmp_menu_textplaylist_enable,
              R.array.mmp_menu_textplaylist_hasnext),
          mListener,
          null);
    }

    if (null != mControlView && (!isNotSupport || sMediaType == MultiMediaConstant.TEXT)
        && !getPlayerStop()) {
      if (mControlView.isPlaying()) {
        menuDialog.setItem(0, mResources
            .getString(R.string.mmp_menu_pause));
      } else {
        menuDialog.setItem(0, mResources
            .getString(R.string.mmp_menu_play));
      }
    }
    menuDialog.setMediaType(sMediaType);
    menuDialog.show();
  }

  boolean isFromStop = false;

  @Override
  protected void onPause() {
    if (this instanceof VideoPlayActivity) {
      boolean isPIP = isInPictureInPictureMode();
      Util.mIsEnterPip = isPIP;
    }
    Util.logLife(TAG, "onPause isPIP:" + Util.mIsEnterPip);

    super.onPause();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStop() {
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.onDismiss(mInfo);
      mInfo.dismiss();
      mInfo = null;
    }
    super.onStop();
    Util.logLife(TAG, "onStop Util.mIsEnterPip:" + Util.mIsEnterPip);
    // boolean isCurGoogleSettingActivity =
    // GetCurrentTask.getInstance(this).isCurGoogleSettingActivity();
    // if (!Util.isMMpActivity(getApplicationContext()) &&
    // !isCurGoogleSettingActivity) {
    // Util.logLife(TAG, "top is not mmp, go to finish.");
    // isFromStop = true;
    // handleRootMenuEvent();
    // ((MmpApp) getApplication()).unregister();
    // this.finish();
    // }

    int isMMPMode = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE);
    MtkLog.d(TAG, "onStop,isMMPMode==" + isMMPMode);

    Util.logLife(TAG, "onStop");
  }

  public void finishSetting() {
    if (!Util.isMMpActivity(getApplicationContext())) {
      // exit google setting activity
      Util.exitAndroidSetting(this);
    }
    MtkLog.d(TAG, "finishSetting,isSetPicture==" + isSetPicture);
    if (isSetPicture) {
      SettingActivity.getInstance().finish();
      isSetPicture = false;
    }
  }

  public void resumeMediaPlayActivity(Context context) {
    MtkLog.d(TAG, "resumeMediaPlayActivity");
    Intent intent = null;
    if (MultiMediaConstant.VIDEO == sMediaType) {
      intent = new Intent(MmpConst.INTENT_VIDEO);
    } else if (MultiMediaConstant.AUDIO == sMediaType) {
      intent = new Intent(MmpConst.INTENT_MUSIC);
    } else {
      return;
    }
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  public void resumeFilesListActivity(int type) {
    MtkLog.d(TAG, "resumeFilesListActivity,type==" + type);
    Intent intent = null;
    intent = new Intent(MmpConst.INTENT_FILEGRID);

    intent.putExtra("Path", "/");
    intent.putExtra("Position", 0);
    intent.putExtra(MultiMediaConstant.MEDIAKEY, type);
    this.startActivity(intent);
  }

  public void resetResource() {
    MtkLog.i(TAG, "resetResource");
  }

  public void resetListener() {
    MtkLog.i(TAG, "resetListener");

  }

  /**
   * Dismiss control bar
   */
  protected void removeControlView() {
    MtkLog.d(TAG, "removeControlView");
    if (mControlView != null && mControlView.isShowing()) {
      try {
        mControlView.dismiss();
      } catch (Exception e) {
        e.printStackTrace();
      }
      mControlView = null;
      contentView = null;
    }
  }

  /**
   * Dismiss control bar
   */
  protected void removeMenuDialog() {
    if (menuDialog != null && menuDialog.isShowing()) {
      try {
        menuDialog.dismiss();
      } catch (Exception e) {
        e.printStackTrace();
      }
      menuDialog = null;
    }
    if (menuDialogFontList != null && menuDialogFontList.isShowing()) {
      try {
        menuDialogFontList.dismiss();
      } catch (Exception e) {
        e.printStackTrace();
      }
      menuDialogFontList = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    if (mAudioManager != null) {
      if (this instanceof VideoPlayActivity && mRequest != null) {
        mAudioManager.abandonAudioFocusRequest(mRequest);
      }
      mAudioManager = null;
    }
    removeControlView();
    removeMenuDialog();

    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
      mHandler = null;
    }
    DmrHelper.setHandler(null);
    ((MmpApp) getApplication()).removeRootMenuListener(mRootMenuListener);
    ((MmpApp) getApplication()).remove(this);
    super.onDestroy();
    // add for fix bug DTVDTV00392376
    mIsActiveLiving = false;
    if (isNeedStartNewActivity) {
      isNeedStartNewActivity = false;
      DmrHelper.handleStart();
    }
    unregisterReceiver(mReceiver);
    unregisterReceiver(mLogoReceiver);
    unregisterReceiver(eventReceiver);
    Util.logLife(TAG, "onDestroy pip:" + Util.mIsEnterPip);
    if (mThreadHandler != null) {
      mThreadHandler.removeCallbacksAndMessages(null);
      mThreadHandler.getLooper().quit();
      mThreadHandler = null;
      mHandlerThead = null;
    }
    if (!((MmpApp) getApplication()).isEnterMMP() && !Util.mIsUseEXOPlayer) {
      // mLogicManager.restoreVideoResource();
      AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
    }

    // if (null != mMediaSession && mMediaSession.isActive()){
    // MtkLog.d(TAG, "mMediaSession release");
    // PlaybackState.Builder mPb=new PlaybackState.Builder();
    // mPb.setState(PlaybackState.STATE_NONE, 0 , 0);
    // mMediaSession.setPlaybackState(mPb.build());
    // mMediaSession.release();
    // }

    // exit mmp when enter other app
    // boolean isCurGoogleSettingActivity =
    // GetCurrentTask.getInstance(this).isCurGoogleSettingActivity();
    // if (!Util.isMMpActivity(this) && !isCurGoogleSettingActivity){
    // Util.exitMmpActivity(getApplicationContext());
    // }

    finishSetting();
  }

  public static final int SCREEN_MODE_FULL_SCREEN = 0;
  public static final int SCREEN_MODE_4_3 = 1;
  public static final int SCREEN_MODE_16_9 = 2;
  public static final int SCREEN_MODE_AUTO = 3;
  public static final int SCREEN_MODE_OVER_SCAN = 4;

  protected void changeScreenModeBySurface(int screenMode, SurfaceView surfaceView) {
    int screenWidth = SettingsUtil.SCREEN_WIDTH;
    int screenHeight = SettingsUtil.SCREEN_HEIGHT;
    int videoWidth = mLogicManager.getVideoWidth();
    int videoHeight = mLogicManager.getVideoHeight();
    MtkLog.d(TAG, "screenWidth==" + screenWidth + ", screenHeight==" + screenHeight);
    MtkLog.d(TAG, "videoWidth==" + videoWidth + ", videoHeight==" + videoHeight);
    if (videoWidth == 0 || videoHeight == 0) {
      return;
    }
    boolean isScreenLandscape = true;
    if (mRotateDegree == Const.ROTATE_DEGREE_90 || mRotateDegree == Const.ROTATE_DEGREE_270) {
      isScreenLandscape = false;
    }
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) surfaceView.getLayoutParams();
    switch (screenMode) {
      case SCREEN_MODE_4_3:
        // 4:3, If aspect ratio of screen is greater than 4:3, height is unchanged, and
        // width is calculated according to the proportion.
        // else width is unchanged, and height is calculated according to the
        // proportion.
        if (true == isScreenLandscape) {
          if ((double) screenWidth / screenHeight > (double) 4 / 3) {
            lp.height = screenHeight;
            lp.width = screenHeight * 4 / 3;
          } else {
            lp.width = screenWidth;
            lp.height = screenWidth * 3 / 4;
          }
        } else {
          lp.height = screenHeight;
          lp.width = screenHeight * 3 / 4;
        }
        break;
      case SCREEN_MODE_16_9:
        // 16:9, If aspect ratio of screen is greater than 16:9, height is unchanged,
        // and width is calculated according to the proportion.
        // else width is unchanged, and height is calculated according to the
        // proportion.
        if (true == isScreenLandscape) {
          if ((double) screenWidth / screenHeight > (double) 16 / 9) {
            lp.height = screenHeight;
            lp.width = screenHeight * 16 / 9;
          } else {
            lp.width = screenWidth;
            lp.height = screenWidth * 9 / 16;
          }
        } else {
          lp.height = screenHeight;
          lp.width = screenHeight * 9 / 16;
        }
        break;
      case SCREEN_MODE_AUTO:
        // auto, if the aspect ratio of screen is greater than the aspect ratio of
        // video, height is unchanged, and width is calculated according to the
        // proportion.
        // else width is unchanged, and height is calculated according to the
        // proportion.
        if (isScreenLandscape) {
          if ((double) screenWidth / screenHeight > (double) videoWidth / videoHeight) {
            lp.height = screenHeight;
            lp.width = screenHeight * videoWidth / videoHeight;
          } else {
            lp.width = screenWidth;
            lp.height = screenWidth * videoHeight / videoWidth;
          }
        } else {
          lp.height = screenHeight;
          lp.width = screenHeight * videoHeight / videoWidth;
        }
        break;
      case SCREEN_MODE_FULL_SCREEN:
        // full screen, surface's size equals to screen size
        lp.height = screenHeight;
        lp.width = screenWidth;
        break;

      case SCREEN_MODE_OVER_SCAN:
        // over scan, surface's size equals to (screen size * 1.1)
        lp.height = (int) (screenHeight * 1.1);
        lp.width = (int) (screenWidth * 1.1);
        break;
      default:
        break;
    }
    // make the change
    MtkLog.d(TAG, "screenMode==" + screenMode);
    mLogicManager.setVendCmd(Const.VND_CMD_SCREEN_MODE, screenMode, 0, null);
    mLogicManager.setScreenMode(screenMode);
    surfaceView.setLayoutParams(lp);
  }

  /**
   * Click menu item callback
   *
   * @param content
   *                menu item value
   */
  private void controlState(String content) {
    MtkLog.d(TAG, "controlState content:" + content);
    menuDialog.hideMenuDelay();
    if (sMediaType == MultiMediaConstant.AUDIO) {
      controlStateAudio(content);
    }
    if (sMediaType == MultiMediaConstant.TEXT) {
      controlStateText(content);
    }
    if (sMediaType == MultiMediaConstant.VIDEO) {
      controlStateVideo(content);
    }
    if ((sMediaType == MultiMediaConstant.PHOTO) && (true == Feature.isAospCnPlatform())) {
      controlStateImage(content);
    }
  }

  private boolean controlStateById(final int mnuId) {
    menuDialog.hideMenuDelay();
    switch (sMediaType) {
      case MultiMediaConstant.AUDIO:
        return controlStateAudioById(mnuId);

      case MultiMediaConstant.TEXT:
        return controlStateTextById(mnuId);

      case MultiMediaConstant.VIDEO:
        return controlStateVideoById(mnuId);

      case MultiMediaConstant.PHOTO:
        if (true == Feature.isAospCnPlatform()) {
          return controlStateImageById(mnuId);
        }
        break;

      default:
        break;
    }
    return false;
  }

  private void controlStateSeamless(String content) {
    if (content.equals(mResources.getString(R.string.mmp_seamless_mode_on))) {
      mLogicManager.setSeamlessMode(true);
    } else if (content.equals(mResources.getString(R.string.mmp_seamless_mode_off))) {
      mLogicManager.setSeamlessMode(false);
    }
  }

  private void controlStateVideo(String content) {
    int idx = menuDialog.getItemIndex(mResources.getString(R.string.mmp_seamless_mode));
    if (idx >= 0 && menuDialog.selectPosition == idx) {
      controlStateSeamless(content);
      return;
    }
    if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_VIDEO,
          Const.REPEAT_NONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatone))) {
      mControlView.setRepeatSingle();
      mLogicManager.setRepeatMode(Const.FILTER_VIDEO,
          Const.REPEAT_ONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatall))) {
      mControlView.setRepeatAll();
      mLogicManager.setRepeatMode(Const.FILTER_VIDEO,
          Const.REPEAT_ALL);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_pause))) {
      mControlView.setMediaPlayState();
      if (!mLogicManager.isPlaying()) {
        menuDialog.initItem(0,
            mResources.getString(R.string.mmp_menu_play));
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_play))) {
      mControlView.setMediaPlayState();
      if (mLogicManager.isPlaying()) {
        if (Util.mIsDolbyVision) {
          Util.showDoViToast(this);
        }
        menuDialog.initItem(0,
            mResources.getString(R.string.mmp_menu_pause));
      }
    } else if (content.startsWith(mResources
        .getString(R.string.mmp_menu_ts_program) + " ")
        && content.length() > mResources.getString(R.string.mmp_menu_ts_program).length() + 1) {
      playExce = PlayException.DEFAULT_STATUS;
      dismissNotSupprot();
      String index = content.substring(content.indexOf(" ") + 1);
      MtkLog.d(TAG, "controlState index :" + index);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showinfo))) {
      showinfoview(sMediaType);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_user))) {
      mLogicManager.setPictureMode(ModelConstant.PICTURE_MODEL_USER);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_cinema))) {
      mLogicManager
          .setPictureMode(ModelConstant.PICTURE_MODEL_CINEMA);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_sport))) {
      mLogicManager.setPictureMode(ModelConstant.PICTURE_MODEL_SPORT);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_vivid))) {
      mLogicManager.setPictureMode(ModelConstant.PICTURE_MODEL_VIVID);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_hibright))) {
      mLogicManager
          .setPictureMode(ModelConstant.PICTURE_MODEL_HIBRIGHT);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_4_3))) {
      changeScreenModeBySurface(SCREEN_MODE_4_3, null);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_16_9))) {
      changeScreenModeBySurface(SCREEN_MODE_16_9, null);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_auto))) {
      changeScreenModeBySurface(SCREEN_MODE_AUTO, null);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_full_screen))) {
      changeScreenModeBySurface(SCREEN_MODE_FULL_SCREEN, null);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_over_scan))) {
      changeScreenModeBySurface(SCREEN_MODE_OVER_SCAN, null);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_pic_setting))) {
      dismissMenuDialog();
      if (mControlView != null && mControlView.isShowed()) {
        // hideController();
        removeControlView();
      }
      showPictureSetting();
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_sound_setting))) {
      dismissMenuDialog();
      if (mControlView != null && mControlView.isShowed()) {
        // hideController();
        removeControlView();
      }
      showSoundSetting();
    } else if (content.equals(mResources.getString(R.string.mmp_menu_audio_output_setting))) {
      dismissMenuDialog();
      if (mControlView != null && mControlView.isShowed()) {
        removeControlView();
      }
      showAudioOutputSetting();
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_off))) {
      MtkLog.i(TAG, "MEMEORY OFF");
      LastMemory.clearLastMemory(getApplicationContext());
      SaveValue.getInstance(getApplicationContext()).saveValue(LastMemory.LASTMEMORY_ID, LastMemory.LASTMEMORY_OFF);
    } else if (content.equals(mResources
        .getString(R.string.mmp_last_memory_time))) {
      MtkLog.i(TAG, "MEMEORY TIME");
      LastMemory.clearLastMemory(getApplicationContext());
      SaveValue.getInstance(getApplicationContext()).saveValue(LastMemory.LASTMEMORY_ID,
          LastMemory.LASTMEMORY_TIME);
    } else if (content.equals(mResources
        .getString(R.string.mmp_last_memory_position))) {
      LastMemory.clearLastMemory(getApplicationContext());
      MtkLog.i(TAG, "MEMEORY POSITION");
      SaveValue.getInstance(getApplicationContext()).saveValue(LastMemory.LASTMEMORY_ID,
          LastMemory.LASTMEMORY_POSITION);
    } else if (content.equals(mResources.getString(R.string.mmp_menu_jump_time))) {
      MtkLog.i(TAG, "JUMP TIME");
      dismissMenuDialog();
      jumpTime();
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play_speed_0_5))
        && (true == Feature.isAospCnPlatform())) {
      if (false == mLogicManager.isNormalSpeed()) {
        mLogicManager.fastForwardVideoNormal();
        mControlView.onFast(mLogicManager.getVideoSpeed(), 0, Const.FILTER_VIDEO);
      }
      mLogicManager.setPlaybackParams(new PlaybackParams().setSpeed(0.5f));
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play_speed_0_75))
        && (true == Feature.isAospCnPlatform())) {
      if (false == mLogicManager.isNormalSpeed()) {
        mLogicManager.fastForwardVideoNormal();
        mControlView.onFast(mLogicManager.getVideoSpeed(), 0, Const.FILTER_VIDEO);
      }
      mLogicManager.setPlaybackParams(new PlaybackParams().setSpeed(0.75f));
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play_speed_1))
        && (true == Feature.isAospCnPlatform())) {
      if (false == mLogicManager.isNormalSpeed()) {
        mLogicManager.fastForwardVideoNormal();
        mControlView.onFast(mLogicManager.getVideoSpeed(), 0, Const.FILTER_VIDEO);
      }
      mLogicManager.setPlaybackParams(new PlaybackParams().setSpeed(1.0f));
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play_speed_1_25))
        && (true == Feature.isAospCnPlatform())) {
      if (false == mLogicManager.isNormalSpeed()) {
        mLogicManager.fastForwardVideoNormal();
        mControlView.onFast(mLogicManager.getVideoSpeed(), 0, Const.FILTER_VIDEO);
      }
      mLogicManager.setPlaybackParams(new PlaybackParams().setSpeed(1.25f));
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play_speed_1_5))
        && (true == Feature.isAospCnPlatform())) {
      if (false == mLogicManager.isNormalSpeed()) {
        mLogicManager.fastForwardVideoNormal();
        mControlView.onFast(mLogicManager.getVideoSpeed(), 0, Const.FILTER_VIDEO);
      }
      mLogicManager.setPlaybackParams(new PlaybackParams().setSpeed(1.5f));
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play_speed_2))
        && (true == Feature.isAospCnPlatform())) {
      if (false == mLogicManager.isNormalSpeed()) {
        mLogicManager.fastForwardVideoNormal();
        mControlView.onFast(mLogicManager.getVideoSpeed(), 0, Const.FILTER_VIDEO);
      }
      mLogicManager.setPlaybackParams(new PlaybackParams().setSpeed(2.0f));
    } else if (content.startsWith(mResources.getString(R.string.mmp_menu_rotate_0))) {
      mRotateDegree = Const.ROTATE_DEGREE_0;
      mLogicManager.updateRotation(mRotateDegree);
      changeScreenModeBySurface(mLogicManager.getCurScreenMode(), null);
    } else if (content.startsWith(mResources.getString(R.string.mmp_menu_rotate_90))) {
      mRotateDegree = Const.ROTATE_DEGREE_90;
      mLogicManager.updateRotation(mRotateDegree);
      changeScreenModeBySurface(mLogicManager.getCurScreenMode(), null);
    } else if (content.startsWith(mResources.getString(R.string.mmp_menu_rotate_180))) {
      mRotateDegree = Const.ROTATE_DEGREE_180;
      mLogicManager.updateRotation(mRotateDegree);
      changeScreenModeBySurface(mLogicManager.getCurScreenMode(), null);
    } else if (content.startsWith(mResources.getString(R.string.mmp_menu_rotate_270))) {
      mRotateDegree = Const.ROTATE_DEGREE_270;
      mLogicManager.updateRotation(mRotateDegree);
      changeScreenModeBySurface(mLogicManager.getCurScreenMode(), null);
    } else {
      // Subtitle Encoding
      String[] encodingArray = this.getResources().getStringArray(R.array.mmp_subtitle_encoding_array);
      boolean isSetEncoding = Arrays.asList(encodingArray).contains(content);
      MtkLog.d(TAG, "isSetEncoding==" + isSetEncoding + ",content==" + content);
      if (isSetEncoding) {
        mLogicManager.setExternalSubtitleEncodingType(content);
        mLogicManager.setSubtitleEncodingType(content);
        return;
      }

      // Soundtracks
      int soundListsize = mLogicManager.getAudioTranckNumber();
      String[] soundTracksList = new String[soundListsize];
      for (int i = 0; i < soundListsize; i++) {
        String soundString = mLogicManager.getCurrentAudioTranckMimeType(i);
        soundString = (i + 1) + ": " + soundString;
        MtkLog.d(TAG, "soundString==" + soundString + ",content==" + content + ",i==" + i);
        if (content.equals(soundString)) {
          mLogicManager.setAudioTranckNumber((short) (i));
          mControlView.initVideoTrackNumber();
          return;
        }
      }
    }
  }

  protected void setAutoNextWithAutoSceenMode() {
    MtkLog.i(TAG, "setAutoNextWithAutoSceenMode.  getCurScreenMode() = " + mLogicManager.getCurScreenMode());
    if (mLogicManager.getCurScreenMode() == SCREEN_MODE_AUTO) { // SCREEN_MODE_AUTO = 3, auto is need adjust screen mode
                                                                // after play next video
      changeScreenModeBySurface(SCREEN_MODE_AUTO, null);
    }
  }

  private boolean controlStateVideoById(final int mnuId) {
    return false;
  }

  private void controlStateAudio(String content) {
    if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
          Const.REPEAT_NONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatone))) {
      mControlView.setRepeatSingle();
      mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
          Const.REPEAT_ONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatall))) {
      mControlView.setRepeatAll();
      mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
          Const.REPEAT_ALL);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_pause))) {
      mControlView.setMediaPlayState();
      menuDialog.initItem(0, mResources
          .getString(R.string.mmp_menu_play));
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_play))) {
      mControlView.setMediaPlayState();
      menuDialog.initItem(0, mResources
          .getString(R.string.mmp_menu_pause));
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleon))) {
      if (isNotSupport || getPlayerStop()) {
        mControlView.setShuffleVisble(View.VISIBLE);
        menuDialog.initItem(1, mResources
            .getString(R.string.mmp_menu_shuffleoff));
        mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_ON);
      } else {
        mControlView.setShuffleVisble(View.VISIBLE);
        menuDialog.initItem(2, mResources
            .getString(R.string.mmp_menu_shuffleoff));
        mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_ON);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleoff))) {
      if (isNotSupport || getPlayerStop()) {
        mControlView.setShuffleVisble(View.INVISIBLE);
        menuDialog.initItem(1, mResources
            .getString(R.string.mmp_menu_shuffleon));
        mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_OFF);
      } else {
        mControlView.setShuffleVisble(View.INVISIBLE);
        menuDialog.initItem(2, mResources
            .getString(R.string.mmp_menu_shuffleon));
        mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_OFF);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showinfo))) {
      showinfoview(sMediaType);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_lyricoptions))) {
      menuDialog.dismiss();
      MenuListView menuDialog = new MenuListView(
          this,
          GetDataImp.getInstance().getComMenu(
              this,
              R.array.mmp_menu_lyricplaylist,
              R.array.mmp_menu_lyricplaylist_enable,
              R.array.mmp_menu_lyricplaylist_hasnext),
          mListener, null);
      menuDialog.show();
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showscore))) {
      mLogicManager.setHideSpectrum(false);
      menuDialog.initItem(4, mResources
          .getString(R.string.mmp_menu_hidescore));
      menuDialog.setSelectShowText(mResources
          .getString(R.string.mmp_menu_hidescore));
      removeScore(false);

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_hidescore))) {
      menuDialog.initItem(4, mResources
          .getString(R.string.mmp_menu_showscore));
      mLogicManager.setHideSpectrum(true);
      menuDialog.setSelectShowText(mResources
          .getString(R.string.mmp_menu_showscore));
      removeScore(true);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_singleline))) {
      mPerLine = 1;
      setLrcLine(mPerLine);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_multiline))) {
      mPerLine = 8;
      setLrcLine(mPerLine);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_off))) {
      // Modified by Dan for fix bug DTV00389330&DTV00389362
      if (menuDialog.isInLrcOffsetMenu()) {
        mLogicManager.setLrcOffsetMode(0);
      } else {
        mPerLine = 0;
        hideLrc();
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_picture_off))) {
      dismissMenuDialog();
      if (mControlView != null && mControlView.isShowed()) {
        hideController();
      }
      showPowerSetting();

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_auto))) {
      // Modified by Dan for fix bug DTV00389362
      if (menuDialog.isInLrcOffsetMenu()) {
        mLogicManager.setLrcOffsetMode(1);
      } else if (menuDialog.isInEncodingMenu()) {
        mLogicManager.setLrcEncodingMode(0);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_userdefine))) {
      // Added by Dan for fix bug DTV00389362
      if (menuDialog.isInLrcOffsetMenu()) {
        mLogicManager.setLrcOffsetMode(2);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_gb))) {
      if (menuDialog.isInEncodingMenu()) {
        mLogicManager.setLrcEncodingMode(1);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_big5))) {
      if (menuDialog.isInEncodingMenu()) {
        mLogicManager.setLrcEncodingMode(2);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_utf8))) {
      if (menuDialog.isInEncodingMenu()) {
        mLogicManager.setLrcEncodingMode(3);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_utf16))) {
      if (menuDialog.isInEncodingMenu()) {
        mLogicManager.setLrcEncodingMode(4);
      }
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_sound_setting))) {
      dismissMenuDialog();
      if (mControlView != null && mControlView.isShowed()) {
        hideController();
      }
      showSoundSetting();
    } else if (content.equals(mResources.getString(R.string.mmp_menu_audio_output_setting))) {
      dismissMenuDialog();
      if (mControlView != null && mControlView.isShowed()) {
        removeControlView();
      }
      showAudioOutputSetting();
    }
  }

  private boolean controlStateAudioById(final int mnuId) {
    return false;
  }

  private void controlStateText(String content) {
    if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_TEXT,
          Const.REPEAT_NONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatone))) {
      mControlView.setRepeatSingle();
      mLogicManager
          .setRepeatMode(Const.FILTER_TEXT, Const.REPEAT_ONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatall))) {
      mControlView.setRepeatAll();
      mLogicManager
          .setRepeatMode(Const.FILTER_TEXT, Const.REPEAT_ALL);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_pause))) {
      mControlView.setMediaPlayState();
      menuDialog.initItem(0, mResources
          .getString(R.string.mmp_menu_play));
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_play))) {
      mControlView.setMediaPlayState();
      menuDialog.initItem(0, mResources
          .getString(R.string.mmp_menu_pause));
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleon))) {
      menuDialog.initItem(2, mResources
          .getString(R.string.mmp_menu_shuffleoff));
      mControlView.setShuffleVisble(View.VISIBLE);
      // Modified by Dan for fix bug DTV00375629
      mLogicManager.setShuffle(Const.FILTER_TEXT, Const.SHUFFLE_ON);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleoff))) {
      menuDialog.initItem(2, mResources
          .getString(R.string.mmp_menu_shuffleon));
      mControlView.setShuffleVisble(View.INVISIBLE);
      // Modified by Dan for fix bug DTV00375629
      mLogicManager.setShuffle(Const.FILTER_TEXT, Const.SHUFFLE_OFF);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showinfo))) {
      showinfoview(sMediaType);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_font))) {
      menuDialog.dismiss();
      menuDialogFontList = new MenuListView(
          this, GetDataImp.getInstance()
              .getComMenu(this,
                  R.array.mmp_menu_fontlist,
                  R.array.mmp_menu_fontlist_enable,
                  R.array.mmp_menu_fontlist_hasnext),
          mListener, null);
      menuDialogFontList.show();
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_small))) {
      setFontSize(TextUtils.SMALLSIZE);
      // reflashPageNumber();

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_medium))) {
      setFontSize(TextUtils.MEDSIZE);
      // reflashPageNumber();

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_large))) {
      setFontSize(TextUtils.LARSIZE);
      // reflashPageNumber();

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_regular))) {
      setFontStyle(Typeface.NORMAL);

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_italic))) {
      setFontStyle(Typeface.ITALIC);

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_bold))) {
      setFontStyle(Typeface.BOLD);

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_outline))) {
      setFontStyle(Typeface.BOLD_ITALIC);

    } else {
      setFontColor(changeContent(content));
    }
  }

  private boolean controlStateTextById(final int mnuId) {
    return false;
  }

  protected void controlStateImage(String content) {
  }

  protected boolean controlStateImageById(final int mnuId) {
    return false;
  }

  public void showPictureSetting() {
    MtkLog.i(TAG, "showPictureSetting");
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName("com.android.tv.settings", "com.android.tv.settings.partnercustomizer.picture.PictureActivity");
    startActivity(intent);
  }

  public void showSoundSetting() {
    MtkLog.i(TAG, "showSoundSetting");
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName("com.android.tv.settings", "com.android.tv.settings.device.sound.SoundActivity");
    startActivity(intent);
  }

  public void showAudioOutputSetting() {
    MtkLog.i(TAG, "showSoundSetting");
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName("com.android.tv.settings", "com.android.tv.settings.device.sound.AudioOutputActivity");
    startActivity(intent);
  }

  public void showPowerSetting() {
    MtkLog.i(TAG, "showPowerSetting");
    Intent intent = new Intent();
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName("com.android.tv.settings", "com.android.tv.settings.partnercustomizer.power.PowerActivity");
    startActivity(intent);
  }

  // add by keke for fix DTV00380564
  protected void setScreenMode(int screenmode) {
    mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    if (null != mControlView) {
      mControlView.setZoomSize();
    }
    mLogicManager.setScreenMode(screenmode);
    TVStorage.getInstance(this).set("SCREENMODE_FILELIST",
        screenmode + "");

  }

  /**
   * Refresh control bar page number
   */
  protected void reflashPageNumber() {
    MtkLog.i(TAG, "reflashPageNumber");
  }

  /**
   * Change the content to match common logic
   *
   * @param content
   * @return
   */
  private int changeContent(String content) {
    int color = Color.WHITE;
    if (mResources.getString(R.string.mmp_menu_red).equals(content)) {
      color = Color.RED;

    } else if (mResources.getString(R.string.mmp_menu_green)
        .equals(content)) {
      color = Color.GREEN;

    } else if (mResources.getString(R.string.mmp_menu_black)
        .equals(content)) {
      color = Color.BLACK;

    } else if (mResources.getString(R.string.mmp_menu_white)
        .equals(content)) {
      color = Color.WHITE;

    } else if (mResources.getString(R.string.mmp_menu_blue).equals(content)) {
      color = Color.BLUE;

    }

    return color;

  }

  /**
   * Show info view
   *
   * @param type
   */
  protected void showinfoview(int type) {
    int resid;
    switch (type) {
      case MultiMediaConstant.AUDIO: {
        resid = R.layout.mmp_musicinfo;
        break;
      }
      case MultiMediaConstant.PHOTO:
      case MultiMediaConstant.THRD_PHOTO: {
        resid = R.layout.mmp_photoinfo;
        break;
      }
      case MultiMediaConstant.VIDEO: {
        resid = R.layout.mmp_videoinfo;
        break;
      }
      case MultiMediaConstant.TEXT: {
        resid = R.layout.mmp_textinfo;
        break;
      }
      default:
        return;
    }
    View contentView = LayoutInflater.from(this).inflate(
        resid, null);
    mInfo = new ShowInfoView(this, contentView, type, mLogicManager);
    dismissMenuDialog();
    mInfo.show();
  }

  private final MenuListView.MenuDismissCallBack mCallBack = new MenuListView.MenuDismissCallBack() {

    @Override
    public void onDismiss() {
      hideController();
    }

    @Override
    public void sendMessage() {
      MtkLog.i(TAG, "sendMessage");
    }

    @Override
    public void noDismissPannel() {
      MtkLog.i(TAG, "noDismissPannel");

    };
  };

  /**
   * Send a delay message to hidden control bar
   */
  protected void hideControllerDelay() {
    MtkLog.i(TAG, "hideControllerDelay");
  }

  // protected void hideMeteDataDelay() {
  //
  // }

  /**
   * Recount hidden control bar delay time
   */
  protected void reSetController() {
    showController();
    hideControllerDelay();
  }

  /**
   * Hidden Control bar
   */
  protected void hideController() {
    if (null != menuDialog && menuDialog.isShowing()) {
      return;
    }
    if (mControlView != null && mControlView.isShowed()) {
      // add by shuming for fix CR: DTV00407914
      mControlView.hiddlen(View.INVISIBLE);
      // mControlView.update(mDisPlayWidth / 10, mDisPlayHeight*20, -1, -1);
      if (mHandler != null) {
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_INFORBAR_POINT, 70);
      }
    }
    // isControlBarShow = false;
    // removeProgressMessage();
  }

  /**
   * Remove to get progress inforamtion and time information message.
   */
  protected void removeProgressMessage() {
    MtkLog.i(TAG, "removeProgressMessage");
  }

  /**
   * Add to get progress inforamtion and time information message
   */
  protected void addProgressMessage() {
    MtkLog.i(TAG, "addProgressMessage");

  }

  /**
   * Show control bar
   */
  protected void showController() {
    if (mControlView != null && !mControlView.isShowed()) {
      mControlView.hiddlen(View.VISIBLE);
      // isControlBarShow = true;
    }
    addProgressMessage();

  }

  /**
   * Get lines number per screen
   *
   * @return int lines number
   */

  // change by xudong.chen 20111204 fix DTV00379662
  public static int getPerLine() {
    return mPerLine;
  }

  // end

  /**
   * Get media type
   *
   * @return int type:photo audio video text
   */
  public static int getMediaType() {
    return sMediaType;
  }

  /**
   * blue screen status. true to blue screen.
   */
  /**
   * Set bule dialog status.
   * 
   * @param status
   */
  /**
   * Get current blue dialog status.
   * 
   * @return
   */
  // add by keke for fix DTV00383992
  protected void hideFeatureNotWork() {
    if (mHandler != null) {
      mHandler.sendEmptyMessage(MSG_DISMISS_FEARTURE_NOT_SUPPORT);
      mHandler.sendEmptyMessageDelayed(MSG_HIDE_FEATURE_NOT_SUPPORT, DELAYED_MIDDLE); // arcelik customized
    }
  }

  protected boolean getPlayerStop() {
    return false;
  }

  protected void setFontSize(float size) {
    MtkLog.i(TAG, "setFontSize " + size);
  }

  protected void setFontStyle(int style) {
    MtkLog.i(TAG, "setFontStyle " + style);
  }

  protected void setFontColor(int color) {
    MtkLog.i(TAG, "setFontColor " + color);
  }

  public void updateIndex() {
    if (null != mControlView && !isDmrSource) {
      mControlView.setFilePosition(mLogicManager.getImagePageSize());
      mControlView.setFileName(mLogicManager.getPhotoName());
    }
  }

  public void setRepeatMode() {
    MtkLog.i(TAG, "setRepeatMode");

    if (mControlView == null || mLogicManager == null) {
      MtkLog.i(TAG, "mControlView==null||mLogicManager==null");
      return;
    }
    int typeutil = MmpConst.AUDIO;
    int typeconst = Const.FILTER_AUDIO;
    switch (sMediaType) {
      case MultiMediaConstant.AUDIO:
        typeutil = MmpConst.AUDIO;
        typeconst = Const.FILTER_AUDIO;
        break;
      case MultiMediaConstant.VIDEO:
        typeutil = MmpConst.VIDEO;
        typeconst = Const.FILTER_VIDEO;
        break;
      case MultiMediaConstant.PHOTO:
        typeutil = MmpConst.PHOTO;
        typeconst = Const.FILTER_IMAGE;
        break;
      case MultiMediaConstant.TEXT:
        typeutil = MmpConst.TXT;
        typeconst = Const.FILTER_TEXT;
        break;
      default:
        break;
    }

    // if (sMediaType == MultiMediaConstant.AUDIO) {
    // int value = Util.getMediaRepeatMode(getApplicationContext(), typeutil);
    MtkLog.i(TAG, "setRepeatMode type_util:" + typeutil + "--typeconst:" + typeconst);
    int value = mLogicManager.getRepeatModel(typeconst);
    switch (value) {
      case MmpConst.NONE:
        MtkLog.i(TAG, "setRepeatMode NONE");
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(typeconst,
            Const.REPEAT_NONE);
        break;
      case MmpConst.REPEATE_ONE:
        MtkLog.i(TAG, "setRepeatMode REPEATE_ONE");
        mControlView.setRepeatSingle();
        mLogicManager.setRepeatMode(typeconst,
            Const.REPEAT_ONE);
        break;
      case MmpConst.REPEATE_ALL:
        MtkLog.i(TAG, "setRepeatMode REPEATE_ALL");
        mControlView.setRepeatAll();
        mLogicManager.setRepeatMode(typeconst,
            Const.REPEAT_ALL);
        break;
      default:
        break;
    }
    // }
  }

  private boolean isNeedStartNewActivity = false;

  private void handleDmrEvent(int state, int param) {
    Log.i(TAG, "state:" + state);
    switch (state) {
      case DmrHelper.DLNA_DMR_STOP:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_STOP");
        handleDmrStop();
        break;
      case DmrHelper.DLNA_DMR_PAUSE:
        // if(mControlView.isPlaying()){
        Log.d(TAG, "handleDmrEvent DLNA_DMR_PAUSE");
        handleDmrPlayPause(state);
        // }
        break;
      case DmrHelper.DLNA_DMR_PLAY:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_PLAY");
        // if(!mControlView.isPlaying()){
        handleDmrPlayPause(state);
        // }
        break;
      case DmrHelper.DLNA_DMR_SEEKTIME:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_SEEKTIME");
        handleDmrSeek(param);
        break;
      case DmrHelper.DLNA_DMR_SET_VOLUME:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_SET_VOLUME");
        handleDmrSetVolume(param);
        break;
      case DmrHelper.DLNA_DMR_SET_MUTE:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_SET_MUTE");
        handleDmrSetMute(param);
        break;
      case DmrHelper.DLNA_DMR_STOPOLDE_STARTNEW:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_STOPOLDE_STARTNEW");
        DmrHelper.handleStart();
        isNeedStartNewActivity = true;
        this.finish();
        break;
      default:
        break;
    }
    // if(state == DmrHelper.DLNA_DMR_STOP){
    // handleDmrStop();
    // }
    // if(state == DmrHelper.DLNA_DMR_PAUSE){
    // if(mControlView.isPlaying()){
    // handleDmrPlayPause(state);
    // }
    // }
    // if(state == DmrHelper.DLNA_DMR_PLAY){
    // if(!mControlView.isPlaying()){
    // handleDmrPlayPause(state);
    // }
    // }
    //
    // if(DmrHelper.DLNA_DMR_SEEKTIME== state){
    // Log.i(TAG,"DLNA_DMR_SEEKTIME");
    // handleDmrSeek(param);
    // }else if(DmrHelper.DLNA_DMR_SET_VOLUME== state){
    // Log.i(TAG,"handleDmrSetVolume");
    // handleDmrSetVolume(param);
    // }else if(DmrHelper.DLNA_DMR_SET_MUTE== state){
    // Log.i(TAG,"handleDmrSetMute");
    // handleDmrSetMute(param);
    // }
  }

  // private void handleDmrEventWithParam(int state,int param){
  // if(DmrHelper.DLNA_DMR_SEEKTIME== state){
  // Log.i(TAG,"DLNA_DMR_SEEKTIME");
  // handleDmrSeek(param);
  // }else if(DmrHelper.DLNA_DMR_SET_VOLUME== state){
  // Log.i(TAG,"handleDmrSetVolume");
  // handleDmrSetVolume(param);
  // }else if(DmrHelper.DLNA_DMR_SET_MUTE== state){
  // Log.i(TAG,"handleDmrSetMute");
  // handleDmrSetMute(param);
  // }
  // }
  public class DmrListener implements IDmrListener {

    @Override
    public void notifyNewEvent(int state) {
      // TODO Auto-generated method stub
      // if(state == DmrHelper.DLNA_DMR_STOP){
      // handleDmrStop();
      // }
      // if(state == DmrHelper.DLNA_DMR_PAUSE){
      // if(mControlView.isPlaying()){
      // handleDmrPlayPause(state);
      // }
      // }
      // if(state == DmrHelper.DLNA_DMR_PLAY){
      // if(!mControlView.isPlaying()){
      // handleDmrPlayPause(state);
      // }
      // }
      Log.i(TAG, "notifyNewEvent:" + state);
      if (mHandler != null) {
        Message msg = new Message();
        msg.what = MSG_DMR;
        msg.arg1 = state;
        mHandler.sendMessage(msg);
      }
    }

    @Override
    public void notifyNewEventWithParam(int state, int param) {
      // if(DmrHelper.DLNA_DMR_SEEKTIME== state){
      // Log.i(TAG,"DLNA_DMR_SEEKTIME");
      // handleDmrSeek(param);
      // }else if(DmrHelper.DLNA_DMR_SET_VOLUME== state){
      // Log.i(TAG,"handleDmrSetVolume");
      // handleDmrSetVolume(param);
      // }else if(DmrHelper.DLNA_DMR_SET_MUTE== state){
      // Log.i(TAG,"handleDmrSetMute");
      // handleDmrSetMute(param);
      // }
      Log.i(TAG, "notifyNewEventWithParam:" + state + " param:" + param);
      if (mHandler != null) {
        Message msg = new Message();
        msg.what = MSG_DMR;
        msg.arg1 = state;
        msg.arg2 = param;
        mHandler.sendMessage(msg);
      }
    }

    @Override
    public long getProgress() {
      // TODO Auto-generated method stub
      return getCurrentTime();
    }

    @Override
    public long getDuration() {
      // TODO Auto-generated method stub
      return getDurationTime();
    }
  }

  protected long getCurrentTime() {
    return 0;
  }

  protected long getDurationTime() {
    return 0;
  }

  protected void handleDmrSeek(int param) {
    Log.i(TAG, "handleDmrSeek = " + param);
  }

  protected void handleDmrSetVolume(int currentVolume) {
    if (mLogicManager.isMute()) {
      onMute();
      // return;
    }
    Log.i(TAG, "c:" + mLogicManager.getVolume() + " max:" + mLogicManager.getMaxVolume()
        + "--setVolume:" + currentVolume);
    currentVolume = (currentVolume * mLogicManager.getMaxVolume()) / 100;
    mLogicManager.setVolume(currentVolume);
    mControlView.setCurrentVolume(currentVolume);

  }

  public void handleDmrSetMute(int param) {
    reSetController();
    onMute();
  }

  protected void showDrmDialog(int index) {
    Log.i(TAG, "showDrmDialog = " + index);
  }

  protected void setDivxTitleVideo(int index) {
    Log.i(TAG, "setDivxTitleVideo " + index);
  }

  protected void switchTitle() {
    Log.i(TAG, "switchTitle");
  }

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context content, Intent intent) {
      // TODO Auto-generated method stub
      /*
       * if (intent.getAction().equals(
       * AudioManager.VOLUME_CHANGED_ACTION)) {
       * Log.i(TAG, "register receiver VOLUME_CHANGED_ACTION");
       * currentVolume = mLogicManager.getVolume();
       * if (mControlView != null){
       * mControlView.setCurrentVolume(currentVolume);
       * }
       * } else if
       * (intent.getAction().equals(AudioManager.STREAM_MUTE_CHANGED_ACTION)) {
       * if (mAudioManager == null) {
       * mAudioManager =
       * (AudioManager)content.getSystemService(Context.AUDIO_SERVICE);
       * }
       * if (mControlView != null) {
       * if (mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
       * onMute(true);
       * } else {
       * onMute(false);
       * }
       * }
       * } else
       */
      if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
          int majorDeviceClass = device.getBluetoothClass().getMajorDeviceClass();
          Log.d(TAG, "ACTION_CONNECTION_STATE_CHANGED,majorDeviceClass==" + majorDeviceClass);
          // AUDIO_VIDEO is a2dp device
          if (majorDeviceClass != BluetoothClass.Device.Major.AUDIO_VIDEO) {
            return;
          }
        }
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
        Log.d(TAG, "ACTION_CONNECTION_STATE_CHANGED,state==" + state);
        switch (state) {
          case BluetoothAdapter.STATE_DISCONNECTED:
            Log.d(TAG, "BLUETOOTH_STATE_DISCONNECTED");
            if (mControlView != null && mControlView.isPlaying()) {
              mControlView.setMediaPlayState();
            }
            break;
          case BluetoothAdapter.STATE_CONNECTED:
            Log.d(TAG, "BLUETOOTH_STATE_CONNECTED");
            if (mControlView != null && !mControlView.isPlaying()) {
              mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  mControlView.setMediaPlayState();
                }
              }, 2000);
            }
            break;
          default:
            Log.d(TAG, "unhandled bluetooth state");
            break;
        }
      }
    }
  };

  private void switchPlay() {
    // if (mControlView != null) {
    // mControlView.initRepeatAB();
    // }
  }

  public void handleCIIssue(boolean isTrue) {
    MtkLog.i(TAG, "handleCIIssue:" + isTrue);
  }

  public void reInitWhenReplay() {
    // if (mControlView != null) {
    // mControlView.initVideoTrackNumber();
    // }
    // if (mControlView != null) {
    // mControlView.reinitSubtitle(mLogicManager.getSubtitleTrackNumber());
    // }
    if (mLogicManager != null) {
      mLogicManager.setPlayStatus(VideoConst.PLAY_STATUS_STARTED);
    }
  }

  public void handleRootMenuEvent() {
    if (!isFromStop) {
      Util.enterMmp(0, getApplicationContext());
    }
  }

  IRootMenuListener mRootMenuListener = new IRootMenuListener() {

    @Override
    public void handleRootMenu() {
      // TODO Auto-generated method stub
      MtkLog.i(TAG, "handleRootMenu Received!");
      finishSetting();
      handleRootMenuEvent();
    }
  };

  public void reSetUIWhenAvDbChanged() {
    if (mControlView != null) {
      mControlView.initVideoTrackNumber();
    }
    if (mControlView != null) {
      mControlView.reinitSubtitle(mLogicManager.getSubtitleTrackNumber());
    }
  }

  public void pictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    if (isInPictureInPictureMode) {
      setMediaSessionMetadata();
      dismissNotSupprot();
    }
  }

  public void setMediaSessionMetadata() {
    MtkLog.d(TAG, "SetMediaSessionMetadata,CurrentFileName=="
        + mLogicManager.getCurrentFileName(Const.FILTER_VIDEO));
    mMediaSession.setActive(false);
    mMetadataBuilder.putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE,
        mLogicManager.getCurrentFileName(Const.FILTER_VIDEO));
    mMediaSession.setMetadata(mMetadataBuilder.build());
    mMediaSession.setActive(true);
  }

  public void playNext() {
    MtkLog.d(TAG, "playNext");
  }

  public void playPre() {
    MtkLog.d(TAG, "playPre");
  }

  protected void jumpTime() {
    MtkLog.d(TAG, "jumpTime");
  }

  private void registerReceivers() {
    IntentFilter intentFilter1 = new IntentFilter();
    intentFilter1.addAction("mtk.intent.action.dolby.version");
    intentFilter1.addAction("mtk.intent.action.dolby.audio");
    intentFilter1.addAction("mtk.intent.action.dolby.atmos");
    intentFilter1.addAction("mtk.intent.action.dolby.vision_atmos");
    intentFilter1.addAction("mtk.intent.action.dolby.vision_audio");

    intentFilter1.addAction("mtk.intent.action.mm.logo.dts");
    intentFilter1.addAction("mtk.intent.action.mm.logo.dts_hd");
    intentFilter1.addAction("mtk.intent.action.mm.logo.dts_express");
    intentFilter1.addAction("mtk.intent.action.mm.logo.dts_hd_master");
    intentFilter1.addAction("mtk.intent.action.mm.logo.dts_x");
    this.registerReceiver(mLogoReceiver, intentFilter1);
  }

  private BroadcastReceiver mLogoReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "mReceiver,the intent action = " + intent.getAction());
      if (intent.getAction().equals("mtk.intent.action.dolby.version")) {
        Util.setDolbyType(1);
      } else if (intent.getAction().equals("mtk.intent.action.dolby.audio")) {
        Util.setDolbyType(2);
      } else if (intent.getAction().equals("mtk.intent.action.dolby.atmos")) {
        Util.setDolbyType(3);
      } else if (intent.getAction().equals("mtk.intent.action.dolby.vision_audio")) {
        Util.setDolbyType(4);
      } else if (intent.getAction().equals("mtk.intent.action.dolby.vision_atmos")) {
        Util.setDolbyType(5);
      }

      if (intent.getAction().equals("mtk.intent.action.mm.logo.dts")) {
        Util.setDolbyType(11);
      } else if (intent.getAction().equals("mtk.intent.action.mm.logo.dts_hd")) {
        Util.setDolbyType(12);
      } else if (intent.getAction().equals("mtk.intent.action.mm.logo.dts_express")) {
        Util.setDolbyType(13);
      } else if (intent.getAction().equals("mtk.intent.action.mm.logo.dts_hd_master")) {
        Util.setDolbyType(14);
      } else if (intent.getAction().equals("mtk.intent.action.mm.logo.dts_x")) {
        Util.setDolbyType(15);
      }

      if (mControlView != null) {
        mControlView.initDolbyAndDtsIcon();
      }
    }
  };

  private void registBroadcast() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_HDMI_AUDIO_PLUG);
    filter.addAction(EXTRA_AUDIO_PLUG_STATE);
    registerReceiver(eventReceiver, filter);
  }

  class VideoInfoCallbackHandler extends MtkTvTVCallbackHandler {

    @Override
    public int notifyVideoInfoMessage(int updateType, int argv1, int argv2, int argv3, int argv4, int argv5, int argv6)
        throws RemoteException {
      Log.d(TAG, "updateType: " + updateType + ", argv1:" + argv1 + ",argv2:" + argv2);
      if (updateType == MtkTvVideoInfoBase.VIDEOINFO_NFY_TYPE_HDR && mControlView != null) {
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            if (argv1 == MtkTvVideoInfoBase.VIDEOINFO_HDR_COND_CHG && mControlView != null) {
              switch (argv2) {
                case MtkTvVideoInfoBase.VIDEOINFO_HDR_TYPE_SDR:
                  mControlView.updateHdrShow(16);
                  break;
                case MtkTvVideoInfoBase.VIDEOINFO_HDR_TYPE_HDR10:
                  mControlView.updateHdrShow(17);
                  break;
                case MtkTvVideoInfoBase.VIDEOINFO_HDR_TYPE_HLG:
                  mControlView.updateHdrShow(18);
                  break;
                case MtkTvVideoInfoBase.VIDEOINFO_HDR_TYPE_DOVI:
                  mControlView.updateHdrShow(19);
                  break;
                case MtkTvVideoInfoBase.VIDEOINFO_HDR_TYPE_TECHNI:
                  mControlView.updateHdrShow(20);
                  break;
                case MtkTvVideoInfoBase.VIDEOINFO_HDR_TYPE_HDR10PLUS:
                  mControlView.updateHdrShow(21);
                  break;
                default:
                  ControlView.sHdrType = 0;
                  break;
              }
            }
          }
        }, 1000);
      }
      return super.notifyVideoInfoMessage(updateType, argv1, argv2, argv3, argv4, argv5, argv6);
    }
  }

  private BroadcastReceiver eventReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      // pause video
      String action = intent.getAction();
      Log.d(TAG, "action1:" + action);
      switch (action) {
        case ACTION_HDMI_AUDIO_PLUG:
          // EXTRA_AUDIO_PLUG_STATE: 0 - UNPLUG, 1 - PLUG
          int hdmiDetectFlag = intent.getIntExtra(EXTRA_AUDIO_PLUG_STATE, -1);
          Log.d(TAG, "action2:" + hdmiDetectFlag);
          if (hdmiDetectFlag == 1) {
            Util.setDolbyType(2);
          } else {
            Util.setDolbyType(3);
          }
          // String parameters = ((AudioManager)
          // context.getSystemService(Context.AUDIO_SERVICE)).getParameters("audioindicator=");
          // Log.d(TAG, "jinxin2:" + parameters);
          // if(!android.text.TextUtils.isEmpty(parameters)&&parameters.toLowerCase().contains("dolby"))
          // {
          // if(parameters.toLowerCase().contains("atmos")) {
          // //atmos
          // Util.setDolbyType(3);
          // }else {
          // //audio
          // Util.setDolbyType(2);
          // }
          if (mControlView != null) {
            mControlView.initDolbyAndDtsIcon();
          }
          // }
          break;
      }
    }
  };

}
