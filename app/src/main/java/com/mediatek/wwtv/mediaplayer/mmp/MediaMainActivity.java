
package com.mediatek.wwtv.mediaplayer.mmp;

import static com.mediatek.wwtv.mediaplayer.R.layout.browse_fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.mediaplayer.setting.presenter.SettingItem;
import com.mediatek.wwtv.mediaplayer.setting.presenter.SettingItemPresenter;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.util.TVContent;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.IRootMenuListener;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesBaseListActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import android.util.Log;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.Row;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import mediatek.sysprop.VendorProperties;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.wwtv.mediaplayer.util.TextToSpeechUtil;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.dm.DeviceManagerEvent;

public class MediaMainActivity extends Activity {

  String TAG = "MediaMainActivity";
  public static int mSelection;
  private static final String TAG_BROWSE_FRAGMENT = "browse_fragment";
  private Context mContext;
  private BrowseFragment mBrowseFragment;
  protected final List<HeaderItem> mHeaderItems = new ArrayList<HeaderItem>();
  protected final List<HeaderItem> mSettingItems = new ArrayList<HeaderItem>();
  protected final SparseArray<ArrayObjectAdapter> mRows = new SparseArray<ArrayObjectAdapter>();
  private final ClassPresenterSelector mItemPresenterSelector = new ClassPresenterSelector();
  private final Map<String, Integer> activityMap = new HashMap<String, Integer>();
  private ShutDownBroadcastReceiver mReceiver;
  //private VideoView videoView, MusicVideoView, PhotoVideoView;
  private ImageView ImageVideo, ImagePhoto, ImageMusic;
  private static final int MSG_AUTO_TEST_START = 1106;
  private static final int MSG_START_EPG_DELAY = 1100;
  private static final int MSG_RESET_SEEKIING = 1101;
  private static final int MSG_EJECTING = 1102;
  private boolean mIsSeeking;
  private int mSeekingProgress;
  private LogicManager mLogicManager;
  public static boolean mIsDlnaAutoTest;
  public static boolean mIsSambaAutoTest;
  public static String mAutoTestFilePath;
  public static String mAutoTestFileName;
  public static List<String> mAutoTestFileDirectorys;
  private TextToSpeechUtil mTTSUtil;
  private static MediaMainActivity mMediaMainActivity = null;
  private Toast mToast = null;

  private HandlerThread mHandlerThead;
  private Handler mThreadHandler;

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case MSG_START_EPG_DELAY:
          // EPGManager.getInstance(MediaMainActivity.this).startEpg(MediaMainActivity.this,
          // NavBasic.NAV_REQUEST_CODE);
          break;
        case MSG_AUTO_TEST_START:
          startAutoTest();
          break;
        case MSG_RESET_SEEKIING:
          mIsSeeking = false;
          break;
        case MSG_EJECTING:
          mLogicManager.onDevUnMount(mDevicePath);
          break;
        default:
          break;
      }
    };
  };

  // View mLayout = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(browse_fragment);
    ImageVideo = findViewById(R.id.VideoImageView);
    ImageMusic = findViewById(R.id.MusicImageView);
    ImagePhoto = findViewById(R.id.PhotoImageView);

   // videoView = findViewById(R.id.VideoView);
   // MusicVideoView = findViewById(R.id.MusicVideoView);
   // PhotoVideoView = findViewById(R.id.PhotoVideoView);
    // initMmp();
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    MmpApp mmp = (MmpApp) this.getApplication();
    mmp.setIsFirst(true);
    mmp.add(this);
    mmp.register();
    mmp.registerRootMenu(mRootMenuListener);
    mHandlerThead = new HandlerThread(TAG);
    mHandlerThead.start();
    mThreadHandler = new Handler(mHandlerThead.getLooper());
    // setContentView(R.layout.browse_fragment);
    // mLayout = findViewById(R.id.main_frag_layout);
    // mBrowseFragment = (BrowseFragment) getFragmentManager()
    // .findFragmentById(R.id.browse_fragment);
    mBrowseFragment = (BrowseFragment) getFragmentManager().findFragmentByTag(
        TAG_BROWSE_FRAGMENT);
    if (mBrowseFragment == null) {
      mBrowseFragment = new BrowseFragment();
      getFragmentManager()
          .beginTransaction()
          .replace(R.id.browse_frag, mBrowseFragment, TAG_BROWSE_FRAGMENT)
          .commit();
    }
    mContext = this;
    mMediaMainActivity = this;
    TVContent mTV = TVContent.getInstance(mContext);
    Thumbnail.getInstance().setContext(mContext.getApplicationContext());
    ClassPresenterSelector rowPresenterSelector = new ClassPresenterSelector();
    ListRowPresenter mListRowPresenter = new ListRowPresenter();
    mListRowPresenter.enableChildRoundedCorners(true);
    rowPresenterSelector.addClassPresenter(ListRow.class, mListRowPresenter);
    Uri uri1 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.film);
    Uri uri2 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.music);
    Uri uri3 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pic);
    /*videoView.setVideoURI(uri1);
    videoView.start();
    MusicVideoView.setVideoURI(uri2);
    MusicVideoView.start();
    PhotoVideoView.setVideoURI(uri3);
    PhotoVideoView.start();*/
    int isNet = VendorProperties.mtkmmp_use_net().orElse(0);
    MtkLog.d(TAG, "isNet==" + isNet);
    if (mTV.isCNRegion()) {
      MtkLog.d(TAG, "isCNRegion");
      isNet = 1;
    }
    ArrayObjectAdapter adapter = new ArrayObjectAdapter();
    mHeaderItems.add(new HeaderItem(0x0001, ""));
    mHeaderItems.add(new HeaderItem(0x0002, ""));
    if (1 == isNet) {
      if (false == Feature.isSupportCnSamba()) {
        mSettingItems.add(new HeaderItem(0x0003, " Settings"));
      }
    }
    mItemPresenterSelector.addClassPresenter(SettingItem.class, new SettingItemPresenter());
    initMenuRows();

    for (HeaderItem mHeaderItem : mHeaderItems) {
      ArrayObjectAdapter temp = mRows.get((int) mHeaderItem.getId());
      temp.setPresenterSelector(mItemPresenterSelector);
      adapter.add(new ListRow(mHeaderItem, temp));
    }

   /* for (HeaderItem mSettingItem : mSettingItems) {
      ArrayObjectAdapter temp = mRows.get((int) mSettingItem.getId());
//      temp.setPresenterSelector(mItemPresenterSelector);
      adapter.add(new ListRow(mSettingItem, temp));
    }*/

    adapter.setPresenterSelector(rowPresenterSelector);
    mBrowseFragment.setAdapter(adapter);

    mBrowseFragment.setTitle("");
    mBrowseFragment.setBadgeDrawable(null);
    mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_DISABLED);
    mBrowseFragment.setOnItemViewSelectedListener(new OnItemViewSelectedListener() {

      @Override
      public void onItemSelected(
          ViewHolder arg0,
          Object arg1,
          android.support.v17.leanback.widget.RowPresenter.ViewHolder arg2,
          Row arg3) {
        if (arg1 instanceof SettingItem) {
          SettingItem citem = (SettingItem) arg1;
          Log.i(TAG, "title:" + citem.getTitle());
          mSelection = activityMap.get(citem.getTitle());
          Log.i(TAG, "mSelection111:" + mSelection);
          //arg0.view.setVisibility(citem.isSelected() ? View.VISIBLE : View.GONE);
        }

        if (mSelection == 0) {
          ImageVideo.setVisibility(View.VISIBLE);
          //videoView.start();
        } else {
          //videoView.pause();
          ImageVideo.setVisibility(View.GONE);
        }
        if (mSelection ==1 )
        {
          ImagePhoto.setVisibility(View.VISIBLE);
          //MusicVideoView.start();
        }
        else
        {
          //MusicVideoView.pause();
          ImagePhoto.setVisibility(View.GONE);
        }
        if (mSelection ==2)
        {
          ImageMusic.setVisibility(View.VISIBLE);
          //PhotoVideoView.start();
        }
        else
        {
          ImageMusic.setVisibility(View.GONE);
          //PhotoVideoView.pause();
        }
      }
    });
    mHandler.postDelayed(new Runnable() {

      @Override
      public void run() {
        if (null != mBrowseFragment) {
          mBrowseFragment.setSelectedPosition(1);
        }
      }
    }, 500);

    getScreenWH();

    mLogicManager = LogicManager.getInstance(this.getApplicationContext());
    mLogicManager.setThreadHandler(mThreadHandler);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    }
    if (!Util.mIsEnterPip) {
      mLogicManager.finishVideo();
    }
    // mLogicManager.freeVideoResource();
    Util.isMMpActivity(this);
    registerReceivers();
    mTTSUtil = new TextToSpeechUtil(mContext);
    Util.isInAppPipAction(mContext);
    Log.d(TAG, "MediaMain onCreate");

    //com.mediatek.wwtv.tvcenter.util.SaveValue.getInstance(this).saveWorldValue(this, "isInMMP", 1, true);
  }



  @Override
  protected void onStart() {
    super.onStart();
    findViewById(R.id.main_frag_layout)
        .setBackground(getResources().getDrawable(R.drawable.mmp_main_bg, null));
  }


  private void initMmp() {
    // TODO Auto-generated method stub
    Log.i(TAG, "MtkTvAppTV updatedSysStatus");
    MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
    Log.i(TAG, "MtkTvAppTV updatedSysStatus later");
  }

  private void registerReceivers() {
    // to restore videoResource when powerdown
    mReceiver = new ShutDownBroadcastReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SHUTDOWN);
    registerReceiver(mReceiver, filter);
    Util.logLife(TAG, "onCreate end: " + ((MmpApp) getApplication()).isEnterMMP());
  }

  private class ShutDownBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      // LogicManager.getInstance(MediaMainActivity.this)
      // .restoreVideoResource();
    }
  }

  private void showFilesGrid(int contentType) {
    Log.i(TAG, "showFilesGrid:" + mSelection);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    } else {
      Util.mIsEnterPip = false;
    }
    if (MultiFilesManager.hasInstance()) {
      MultiFilesManager.getInstance(this).destroy();
    } else if (!Util.mIsEnterPip) {
      //int result = DLNAManager.stoDMP();
      //MtkLog.d(TAG, "showFilesGrid is not pip, stop dmp result:" + result);
    }
    if (mSelection == 5) {
      Intent intent = new Intent(MmpConst.INTENT_NETSETTING);
      startActivity(intent);
    } else if (mSelection == 4) {
      Util.exitPIP(mContext);
      mLogicManager.stopAudio();
      mLogicManager.finishVideo();
      if (VideoPlayActivity.getInstance() != null) {
        VideoPlayActivity.getInstance().finish();
      }
      Intent intent = new Intent(MmpConst.INTENT_DMR);
      // Class clazz = Class.forName("com.mediatek.ui.mmp.DmrActivity");
      // intent.setClass(MediaMainActivity.this, DmrActivity.class);
      intent.putExtra("TKUI", true);
      this.startActivity(intent);
    }
    else {
      SaveValue pref = SaveValue.getInstance(this);

      boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false
          : true;
      boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false
          : true;

      if (true == Feature.isSupportCnSamba()) {
        smbAvailable = true;
      }

      MtkLog.d(TAG, "Dlna Available : " + dlnaAvailable
          + "Samba Available : " + smbAvailable);
      if (mIsDlnaAutoTest) {
        Log.d(TAG, "Dlna Available : " + dlnaAvailable
            + "Samba Available : " + smbAvailable);
      }
      if (contentType == FileConst.MMP_TYPE_ALL) {
        dlnaAvailable = false;
        smbAvailable = false;
      }

      MultiFilesManager.setSmbAndDlnaAvailable(smbAvailable, dlnaAvailable);
      MultiFilesManager.getInstance(this).getLocalDevices();
      int deviceNum = MultiFilesManager.getInstance(this)
          .getAllDevicesNum();
      Log.d(TAG, "deviceNum == " + deviceNum);

      if (deviceNum == MultiFilesManager.NO_DEVICES && !dlnaAvailable
          && !smbAvailable) {
        MultiFilesManager.getInstance(this).destroy();
        showNoDevicesTips();
        return;
      }

      if (dlnaAvailable || smbAvailable) {
        MtkFilesBaseListActivity.reSetModel();
      }

      MultiFilesManager.getInstance(this).pushOpenedHistory(contentType);
      Intent intent = new Intent(MmpConst.INTENT_FILEGRID);
      intent.putExtra(MultiMediaConstant.MEDIAKEY, contentType);
      startActivity(intent);
    }
  }

  private void showFilesList(int content) {
    MtkLog.i(TAG, "showFilesList:" + content);
    Util.exitPIP(mContext);
    mLogicManager.finishVideo();
    if (VideoPlayActivity.getInstance() != null) {
      VideoPlayActivity.getInstance().finish();
    }
    if (false == Feature.isSupportCnSamba()) {
      if (MultiFilesManager.hasInstance()) {
        MultiFilesManager.getInstance(this).destroy();
        // TODO: temp solution
        MultiFilesManager.setSmbAndDlnaAvailable(true, false);
      }
    }
    Intent intent = new Intent(MmpConst.INTENT_FILELIST);
    intent.putExtra(MmpConst.INTENT_NAME_SELECTION, content);
    startActivity(intent);
  }

  private void exitMediaMain() {
    Log.d(TAG, "MediaMain exitMediaMain ");
    Util.exitMmpActivity(getApplicationContext());
    resetResouce();
  }

  private void resetResouce() {
    MtkLog.d(TAG, "MediaMain resetResouce ");
    ((MmpApp) getApplication()).remove(this);
    if (mLogicManager != null) {
      mLogicManager.sendCloseBroadCast();
    }
    mSelection = 0;
    AsyncLoader.getInstance(0).clearQueue();
    Thumbnail thumbnail = Thumbnail.getInstance();
    if (thumbnail.isLoadThumanil()) {
      thumbnail.stopThumbnail();
    }
    MtkFilesBaseListActivity.reSetModel();
  }

  private void getScreenWH() {
    DisplayMetrics dm = new DisplayMetrics();
    this.getWindowManager().getDefaultDisplay().getMetrics(dm);
    SettingsUtil.SCREEN_WIDTH = dm.widthPixels;
    SettingsUtil.SCREEN_HEIGHT = dm.heightPixels;

    ScreenConstant.SCREEN_WIDTH = dm.widthPixels;
    ScreenConstant.SCREEN_HEIGHT = dm.heightPixels;
  }


  private void initMenuRows() {
    String videoTitle = getResources().getString(R.string.mmp_menu_video);
    String musicTitle = getResources().getString(R.string.mmp_menu_music);
    String photoTitle = getResources().getString(R.string.mmp_menu_phototext);
    String textTitle = getResources().getString(R.string.mmp_main_text);

    ArrayObjectAdapter emptyRow = new ArrayObjectAdapter();
    mRows.put(0x0001, emptyRow);
    ArrayObjectAdapter menuRow = new ArrayObjectAdapter();
    SettingItem video = new SettingItem(
        videoTitle,
        getResources().getDrawable(R.drawable.mmp_video_icon_selected),
        getResources().getDrawable(R.drawable.mmp_video_selected));

    activityMap.put(videoTitle, MmpConst.VIDEO);

    SettingItem photo = new SettingItem(
        photoTitle,
        getResources().getDrawable(R.drawable.mmp_photo_icon_selected),
        getResources().getDrawable(R.drawable.mmp_photo_selected));
    activityMap.put(photoTitle, MmpConst.PHOTO);
    SettingItem music = new SettingItem(
        musicTitle,
        getResources().getDrawable(R.drawable.mmp_audio_icon_selected),
        getResources().getDrawable(R.drawable.mmp_audio_selected));
    activityMap.put(musicTitle, MmpConst.AUDIO);
    SettingItem text = new SettingItem(
        textTitle,
        getResources().getDrawable(R.drawable.mmp_text_icon_selected),
        getResources().getDrawable(R.drawable.mmp_text_selected));
    activityMap.put(textTitle, MmpConst.TXT);
    //SettingItem dmr = new SettingItem(MmpConst.MMP_DMR, this.getResources().getDrawable(
      //  R.drawable.menu_dmr), null);
    //activityMap.put(MmpConst.MMP_DMR, MmpConst.DMR);

   menuRow.add(video);
   menuRow.add(photo);
   menuRow.add(music);
    /*
     * if (false == Feature.isAospCnPlatform()) {
     * menuRow.add(text);
     * }
     */ // arcelik customized. Remove text mode.
   /* if (TVContent.getInstance(mContext).isCNRegion()) {
      if (false == Feature.isSupportCnSamba()) {
        menuRow.add(dmr);
      }
    }*/


    mRows.put(0x0002, menuRow);
    ArrayObjectAdapter settingRow = new ArrayObjectAdapter();
    //SettingItem mSetting = new SettingItem(MmpConst.MMP_SETTING, this.getResources().getDrawable(
      //  R.drawable.content_setting), null);
    //settingRow.add(mSetting);
   // activityMap.put(MmpConst.MMP_SETTING, MmpConst.MMP_SETTING_INT);
    //mRows.put(0x0003, settingRow);
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    Log.d(TAG, "onPause");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Util.logLife(TAG, "onResume:" + ((MmpApp) getApplication()).isEnterMMP()
        + "  Util.mIsEnterPip:" + Util.mIsEnterPip);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    }
    Util.logLife(TAG, "onResume:" + VideoPlayActivity.getInstance()
        + "  Util.mIsEnterPip:" + Util.mIsEnterPip);
    ((MmpApp) getApplication()).register();

    SaveValue pref = SaveValue.getInstance(this);
    if (0 != VendorProperties.mtk_dlna_auto_test().orElse(0)) {
      mIsDlnaAutoTest = true;
      if (pref.readValue(MmpConst.DLNA) == 0) {
        pref.saveValue(MmpConst.DLNA, 1);
      }
    } else {
      mIsDlnaAutoTest = false;
      if (0 != VendorProperties.mtk_auto_test().orElse(0)) {
        if (pref.readValue(MmpConst.DLNA) == 1) {
          pref.saveValue(MmpConst.DLNA, 0);
        }
      }
    }
    if (0 != VendorProperties.mtk_samba_auto_test().orElse(0)) {
      mIsSambaAutoTest = true;
      if (pref.readValue(MmpConst.MY_NET_PLACE) == 0) {
        pref.saveValue(MmpConst.MY_NET_PLACE, 1);
      }
    } else {
      mIsSambaAutoTest = false;
      if (0 != VendorProperties.mtk_auto_test().orElse(0)) {
        if (pref.readValue(MmpConst.MY_NET_PLACE) == 1) {
          pref.saveValue(MmpConst.MY_NET_PLACE, 0);
        }
      }
    }
    if (!((MmpApp) getApplication()).isEnterMMP()) {
      ((MmpApp) getApplication()).setEnterMMP(true);
      initMmp();
      // mLogicManager.freeVideoResource();
      // if (mLogicManager.isAudioOnly()) {
      // mLogicManager.setAudioOnly(false);
      // }
      if (mIsDlnaAutoTest || mIsSambaAutoTest) {
        mHandler.sendEmptyMessageDelayed(MSG_AUTO_TEST_START, 1500);
      }
    }
    int isCmpb = VendorProperties.mtkmmp_use_cmpb().orElse(0);
    Log.d(TAG, "CMPB_PROPERTY==" + isCmpb);
    if (isCmpb == 0) {
      SaveValue.getInstance(this).saveValue(MenuConfigManager.EXO_PLAYER_SWITCHER, 1);
      MtkLog.d(TAG, "is exo");
      Util.mIsUseEXOPlayer = true;
    } else {
      SaveValue.getInstance(this).saveValue(MenuConfigManager.EXO_PLAYER_SWITCHER, 0);
      MtkLog.d(TAG, "is not exo");
      Util.mIsUseEXOPlayer = false;
    }

    if (MultiFilesManager.hasInstance()) {
      MultiFilesManager.getInstance(this).destroy();
    }
    //onRegisterUsbEvent();
  }

  private void startAutoTest() {
    Intent intent = getIntent();
    mAutoTestFilePath = intent.getStringExtra("urlStr");
    int contentType = intent.getIntExtra("contentType", -1);
    Log.d(TAG, "contentType:" + contentType + "   mAutoTestFilePath:" + mAutoTestFilePath
        + "  " + intent.getDataString());
    String[] temArray = null;
    if (mAutoTestFilePath != null) {
      temArray = mAutoTestFilePath.split("/");
    }
    if (temArray != null && temArray.length > 0 && mSelection > -1 && mSelection < 5) {
      mAutoTestFileName = temArray[temArray.length - 1];
      if (mAutoTestFileDirectorys == null) {
        mAutoTestFileDirectorys = new ArrayList<String>();
      } else {
        mAutoTestFileDirectorys.clear();
      }
      for (int i = 0; i < temArray.length - 1; i++) {
        if (temArray[i] != null && temArray[i].length() > 0) {
          mAutoTestFileDirectorys.add(temArray[i]);
        }
      }
      mSelection = contentType;
      showFilesGrid(mSelection);
    } else {
      if (mSelection < 0 || mSelection > 5) {
        mSelection = 0;
      }
      Toast.makeText(getApplicationContext(), "Params error, please check.",
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onRestart() {
    // TODO Auto-generated method stub
    super.onRestart();
    // mLayout.setVisibility(View.VISIBLE);
    Log.i(TAG, "onRestart --mSelection:" + mSelection);
    Util.logLife(TAG, "onRestart");
  }

  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
    // mLayout.setVisibility(View.INVISIBLE);
    if (!Util.isMMpActivity(this)) {
      mLogicManager.stopAudio();
      ((MmpApp) getApplication()).unregister();
    }
    hideNoDevicesTips();
    if (mDevManager != null) {
      mDevManager.removeDevListener(mDevListener);
    }
    findViewById(R.id.main_frag_layout)
        .setBackground(null);
    Log.d(TAG, "onStop");
  }

  @Override
  protected void onDestroy() {
    //com.mediatek.wwtv.tvcenter.util.SaveValue.getInstance(this).saveWorldValue(this, "isInMMP", 0, true);
    // exitMediaMain();
    Thumbnail.getInstance().setContext(null);
    if (mLogicManager != null) {
      mLogicManager.stopDecode();
      mLogicManager.unRegisterAllListener();
      mLogicManager = null;
    }
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
      mHandler = null;
    }
    if (mThreadHandler != null) {
      mThreadHandler.removeCallbacksAndMessages(null);
      mThreadHandler.getLooper().quit();
      mThreadHandler = null;
      mHandlerThead = null;
    }
    ((MmpApp) getApplication()).removeRootMenuListener(mRootMenuListener);
    ((MmpApp) getApplication()).remove(this);
    unregisterReceiver(mReceiver);
    if (!Util.mIsEnterPip && !Util.mIsUseEXOPlayer) {
      AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
    }

    if (null != mTTSUtil) {
      mTTSUtil.shutdown();
    }

    Util.mIsFirstPlayVideoListMode = true;
    ((MmpApp) getApplication()).unregister();
    mMediaMainActivity = null;

    if (null != MediaPlayActivity.mMediaSession) {
      MtkLog.d(TAG, "mMediaSession release");
      if (MediaPlayActivity.mMediaSession.isActive()) {
        PlaybackState.Builder mPb = new PlaybackState.Builder();
        mPb.setState(PlaybackState.STATE_NONE, 0, 0);
        MediaPlayActivity.mMediaSession.setPlaybackState(mPb.build());
      }
      MediaPlayActivity.mMediaSession.release();
    }

    Log.d(TAG, "onDestroy");
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "onKeyDown keyCode:" + keyCode);
    keyCode = KeyMap.getKeyCode(keyCode, event);
    Log.i(TAG, "onKeyDown later keycode:" + keyCode);

    switch (keyCode) {
      case KeyMap.KEYCODE_DPAD_UP:
        // int selectionRow = mBrowseFragment.getSelectedPosition();
        return (mSelection != 5);
      case KeyMap.KEYCODE_DPAD_CENTER:
        //showFilesGrid(mSelection);

        break;
      case KeyMap.KEYCODE_MTKIR_BLUE:
        if (isValid()) {
          // if(MultiFilesManager.getInstance(mContext).getCurrentSourceType() != 0){
          showFilesList(mSelection);
          // }else{
          // return true;
          // }
        }
        // if (AnimationManager.getInstance().getIsAnimation()) {

        // }
        break;

      case KeyMap.KEYCODE_MTKIR_ANGLE:
        return true;
      case KeyMap.KEYCODE_BACK: {
        exitMediaMain();
        // if (AnimationManager.getInstance().getIsAnimation()) {
        // AnimationManager.getInstance().startActivityEndAnimation(this,
        // findViewById(R.id.mmp_main_layout), null);
        // } else {
        if (isValid()) {
          finish();
        }
        // }
        break;
      }
      case KeyMap.KEYCODE_DPAD_DOWN: {
        return false;
      }
      // // Mute
      // case KeyMap.KEYCODE_MTKIR_MUTE: {
      // mLogicManager.setMute();
      // return true;
      // }
      // Repeat
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        onRepeat();
        return true;
      }
      // Play next audio
      case KeyMap.KEYCODE_MTKIR_NEXT: {
        mLogicManager.playNextAudio();
        return true;
      }
      // Play previous audio
      case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
        mLogicManager.playPrevAudio();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
      case KeyMap.KEYCODE_MTKIR_REWIND: {
        if (!mLogicManager.canSeek()) {
          return true;
        }
        if (mLogicManager.isAudioPlaying()) {
          mLogicManager.pauseAudio();
        }
        if (!mIsSeeking) {
          mIsSeeking = true;
          int progressTemp = mLogicManager.getPlaybackProgress();
          mSeekingProgress = progressTemp;// progressTemp & 0xffffffffL;
        }
        if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND) {
          mSeekingProgress = mSeekingProgress - MusicPlayInfoView.SEEK_DURATION;
          if (mSeekingProgress < 0) {
            mSeekingProgress = 0;
          }
        } else {
          mSeekingProgress = mSeekingProgress + MusicPlayInfoView.SEEK_DURATION;
          int totalProgressTemp = mLogicManager.getTotalPlaybackTime();
          if (mSeekingProgress > totalProgressTemp) {
            mSeekingProgress = totalProgressTemp;
          }
        }
        MtkLog.i(TAG, "seek progress calc:" + mSeekingProgress);

        return true;
      }

      // Pause audio
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
        onPauseOrPlay();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PLAY: {
        onPlayAudio();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PAUSE: {
        onPauseAudio();
        return true;
      }

      // Stop audio
      case KeyMap.KEYCODE_MTKIR_STOP: {
        mLogicManager.stopAudio();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PIPPOP:
      case KeyMap.KEYCODE_MTKIR_PIPPOS:
      case KeyMap.KEYCODE_MTKIR_PIPSIZE:
      case KeyMap.KEYCODE_MTKIR_CHDN:
      case KeyMap.KEYCODE_MTKIR_CHUP:
        return false;
      case KeyMap.KEYCODE_MTKIR_GUIDE:
        // Util.startEPGActivity(MediaMainActivity.this);
        break;
      /*
       * case KeyMap.KEYCODE_MENU:
       * Intent intent = new Intent(MmpConst.INTENT_NETSETTING);
       * startActivity(intent);
       * break;
       */
      default:
        MtkLog.i(TAG, "onKeyDown default");
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND
        || keyCode == KeyMap.KEYCODE_MTKIR_FASTFORWARD) {
      if (mLogicManager.isAudioFast() || !mLogicManager.canSeek()) {
        return true;
      }
      try {
        MtkLog.i(TAG, "seek progress:" + mSeekingProgress);
        mLogicManager.seekToCertainTime(mSeekingProgress);
        mHandler.sendEmptyMessageDelayed(MSG_RESET_SEEKIING, 100);
      } catch (Exception e) {
        MtkLog.i(TAG, "Seek exception");
        mIsSeeking = false;
        return true;
      }
    }
    return super.onKeyUp(keyCode, event);
  }

  private void onRepeat() {
    int repeatModel = mLogicManager.getRepeatModel(
        Const.FILTER_AUDIO);

    repeatModel = (repeatModel + 1) % 3;

    mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
        repeatModel);
  }

  private void onPauseOrPlay() {

    if (mLogicManager.getAudioPlaybackService() == null) {
      MtkLog.i(TAG, "mLogicManager.getAudioPlaybackService() == NULL");
      return;
    }
    if (mLogicManager.isAudioPause() || mLogicManager.isAudioFast()
        || mLogicManager.isAudioStoped()) {
      MtkLog.i(TAG, "onPauseOrPlay audio status= pasue | Fast | Stop");
      mLogicManager.playAudio();
    } else if (mLogicManager.isAudioPlaying()) {
      MtkLog.i(TAG, "onPauseOrPlay audio is playing");
      mLogicManager.pauseAudio();
    }
  }

  private void onPlayAudio() {
    MtkLog.i(TAG, "onPlayAudio");
    if (mLogicManager.getAudioPlaybackService() == null) {
      MtkLog.i(TAG, "mLogicManager.getAudioPlaybackService() == NULL");
      return;
    }
    if (mLogicManager.isAudioPause() || mLogicManager.isAudioFast()
        || mLogicManager.isAudioStoped()) {
      MtkLog.i(TAG, "onPlayAudio audio status= pasue | Fast | Stop");
      mLogicManager.playAudio();
    }
  }

  private void onPauseAudio() {
    MtkLog.i(TAG, "onPauseAudio");
    if (mLogicManager.getAudioPlaybackService() == null) {
      MtkLog.i(TAG, "mLogicManager.getAudioPlaybackService() == NULL");
      return;
    }

    if (mLogicManager.isAudioPlaying()) {
      MtkLog.i(TAG, "onPauseAudio audio is playing");
      mLogicManager.pauseAudio();
    }
  }

  private static long mLastKeyDownTime;

  public static boolean isValid() {
    boolean isValid = false;
    long currentTime = System.currentTimeMillis();
    if ((currentTime - mLastKeyDownTime) >= 1000) {
      mLastKeyDownTime = currentTime;
      isValid = true;
    } else {
      mLastKeyDownTime = currentTime;
    }
    return isValid;
  }

  /*
   * Used by other classes
   */
  public static boolean isValid(int millSeconds) {
    long currentTime = System.currentTimeMillis();
    boolean isValid = false;
    if ((currentTime - mLastKeyDownTime) >= millSeconds) {
      mLastKeyDownTime = currentTime;
      isValid = true;
    } else {
      mLastKeyDownTime = currentTime;
    }
    return isValid;
  }

  IRootMenuListener mRootMenuListener = new IRootMenuListener() {

    @Override
    public void handleRootMenu() {
      MtkLog.i(TAG, "handleRootMenu Received!");
      if (mIsDlnaAutoTest || mIsSambaAutoTest) {
        finish();
      }
    }
  };

  public static MediaMainActivity getInstance() {
    return mMediaMainActivity;
  }

  public TextToSpeechUtil getTTSUtil() {
    if (null == mTTSUtil) {
      mTTSUtil = new TextToSpeechUtil(mContext);
    }

    return mTTSUtil;
  }

  public void showNoDevicesTips() {
    if (mToast == null) {
      mToast = Toast.makeText(getApplicationContext(),
          getResources().getString(R.string.mmp_no_devices_tips),
          Toast.LENGTH_LONG);
    }
    mToast.show();
  }

  public void hideNoDevicesTips() {
    if (mToast != null) {
      mToast.cancel();
      mToast = null;
    }
  }

  private DevManager mDevManager = null;
  private MyDevListener mDevListener = null;
  private String mDevicePath;

  public class MyDevListener implements DevListener {
    public void onEvent(DeviceManagerEvent event) {
      Log.d(TAG, "Device Event : " + event.getType());
      int type = event.getType();
      String devicePath = event.getMountPointPath();
      String filePath = mLogicManager.getCurrentFilePath(Const.FILTER_AUDIO);
      mDevicePath = devicePath;
      switch (type) {
        case DeviceManagerEvent.ejecting:
          MtkLog.d(TAG, "Device Event ejecting!!");
          if (filePath != null && filePath.startsWith(devicePath)) {
            MtkLog.d(TAG, "Device Event : isAudioStarted " + mLogicManager.isAudioStarted());
            if (mLogicManager.isAudioStarted()) {
              mHandler.sendEmptyMessage(MSG_EJECTING);
            }
          }
          break;

        default:
          break;
      }
    }
  };

  private void onRegisterUsbEvent() {
    try {
      MtkLog.d(TAG, "onRegisterUsbEvent");
      if (mDevListener == null) {
        mDevListener = new MyDevListener();
      }
    } catch (ExceptionInInitializerError e) {
      mDevManager = null;
      mDevListener = null;
    }
  }

}
