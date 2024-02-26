
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MtkVideoView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.Tools;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader.LoadWork;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.mmp.model.SmbFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.LenFileAdapter;

public class MtkFilesListActivity extends MtkFilesBaseListActivity {

  private static final String TAG = "MtkFilesListActivity";

  private static final int MODE_FILES_LIST = 0;

  private static final int MODE_CONTENT_TYPES_LIST = 1;

  private ImageView vTopImage;

  private TextView vTopTV;

  private TextView vTopFilePath;

  private TextView vTopPageSize;

  private ImageView vThumbnail;

  private TextView vDetailInfo;

  private static MtkVideoView vVideoView;

  private TextView vTextShow;

  private RelativeLayout vMidLayout;

  private int mPlayPosition = -1;

  private final int mListItemHeght = ScreenConstant.SCREEN_HEIGHT / 12; // arcelik customized

  private int mMode = MODE_CONTENT_TYPES_LIST;

  public static boolean LOCALlogV = true;

  private AsyncLoader<Bitmap> mLoader;

  private AudioManager mAudioManager;

  private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

    @Override
    public void onAudioFocusChange(int focusChange) {
      MtkLog.d(TAG, "onAudioFocusChange." + focusChange);
    }
  };

  private final AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {

    @Override
    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
        AccessibilityEvent event) {
      MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
      do {
        if (vList != host) {
          MtkLog.d(TAG, ":" + vList + "," + host);
          break;
        }

        List<CharSequence> texts = event.getText();
        if (texts == null) {
          MtkLog.d(TAG, ":" + texts);
          break;
        }

        // confirm which item is focus
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move focus
          // test code
          myHandler.removeMessages(TTS_FOCUS);
          Message msg = new Message();
          msg.what = TTS_FOCUS;
          msg.obj = texts.get(0).toString();
          MtkLog.d(TAG, "text name:" + texts.get(0).toString());
          myHandler.sendMessageDelayed(msg, 100);
        }
      } while (false);

      return true;// host.onRequestSendAccessibilityEventInternal(child, event);
    }
  };

  private int findSelectItem(String text) {
    if (mMode == MODE_CONTENT_TYPES_LIST) {
      if (mContentTypeNames == null) {
        return -1;
      }
      for (int i = 0; i < mContentTypeNames.length; i++) {
        if (mContentTypeNames[i].equals(text)) {
          return i;
        }
      }
    } else if (mMode == MODE_FILES_LIST) {
      if (mLoadFiles == null) {
        return -1;
      }

      for (int i = 0; i < mLoadFiles.size(); i++) {
        if (mLoadFiles.get(i).getName().equals(text)) {
          return i + 1;
        } else if ("[..]".equals(text)) {
          return 0;
        }
      }
    }

    return -1;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mViewMode = VIEW_MODE_LIST;
    super.onCreate(savedInstanceState);
    // mLoader = new AsyncLoader<Bitmap>(1);
    mLoader = AsyncLoader.getInstance(1);
    Util.logLife(TAG, "onCreate");
    RelativeLayout listLeft = findViewById(R.id.mmp_listmode_rl);
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) listLeft.getLayoutParams();
    lp.width = (int) (ScreenConstant.SCREEN_WIDTH * 0.25);
    listLeft.setLayoutParams(lp);
    vList.setAccessibilityDelegate(mAccDelegate);
    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    // Toast if TalkbackService enabled
    if (Util.isTTSEnabled(getApplicationContext())) {
      Util.showToast(this, "TalkBackService is enabled. \nPlease use Yellow key to change browse mode.");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void initMusicView() {
    View contentView = LayoutInflater.from(this).inflate(
        R.layout.mmp_musicbackplay, null);
    contentView.findViewById(R.id.mmp_music_playback_spectrum)
        .setVisibility(View.GONE);
    int popViewWidth = (int) getResources().getDimension(R.dimen.list_left_bg_width);
    mPopView = new PopupWindow(contentView, popViewWidth, ScreenConstant.SCREEN_HEIGHT / 3);
    vMusicView = new MusicPlayInfoView(this, contentView, 1, mPopView);
  }

  /**
   * Play video completion listener
   */
  /*
   * private OnCompletionListener mCompletionListener = new OnCompletionListener()
   * { public void
   * onCompletion(CHMtkMediaPlayer arg0) { // flag==1 play all files finish
   * ,flag==0 play a file
   * finish //if (flag == 1) { vMusicView.removeMessage(); mPopView.dismiss(); //}
   * //return false; }
   * };
   */

  /**
   * {@inheritDoc}
   */
  @Override
  protected void showMusicView() {
    super.showMusicView();

    Looper.myQueue().addIdleHandler(new IdleHandler() {
      @Override
      public boolean queueIdle() {
        MtkLog.d(TAG, "showMusicView");
        int popViewLeftStart = (int) getResources().getDimension(R.dimen.list_left_bg_start);
        int popViewTopStart = (int) getResources().getDimension(R.dimen.list_music_view_top);
        mPopView.showAsDropDown(vMidLayout, popViewLeftStart, 0);
        vMusicView.init(MtkFilesListActivity.this);
        return false;
      }
    });
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (LogicManager.getInstance(this).isAudioOnly()) {
      /* by lei add for fix cr DTV00390970 */
      if (event.getAction() == KeyEvent.ACTION_UP) {
        LogicManager.getInstance(this).setAudioOnly(false);
      }
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  int position = 0;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    position = setCurrentSelection();
    MtkLog.i(TAG, "onResume  position:" + position);
    String currentPath = MultiFilesManager.getInstance(this).getCurrentPath();
    MtkLog.d(TAG, "onResume currentPath:" + currentPath);
    if (((currentPath != null && currentPath.equals("/"))
        || getListContentType() != FilesManager.CONTENT_VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      showMusicView();
    } else {
      // modified by keke for fix DTV00380564
      LogicManager.getInstance(this).setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
      stopMusicView();

    }

    mPlayMode = 0;
    if (getListContentType() == FilesManager.CONTENT_VIDEO) {
      Log.i(TAG, "currentPlay :" + currentPlay);
      if (currentPlay == position) {
        LogicManager.getInstance(this).videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
        myHandler.sendEmptyMessage(PLAY);
      } else {
        setListSelection(position);
      }
    }
    Util.logLife(TAG, "onResume");
  }

  @Override
  protected void onStop() {
    super.onStop();
    LogicManager.getInstance(this).registerAudioPlayListener(null);
    Util.logLife(TAG, "onStop");
  }

  private final static int PLAY = 100;
  private final static int MUSIC_PLAYING = 101;
  private final static int TTS_FOCUS = 102;
  private final Handler myHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case PLAY:
          FileAdapter file = getListItem(position);
          if (null != file) {
            if (file.isFile()
                && (getListContentType() == FilesManager.CONTENT_VIDEO)) {
              if (vVideoView == null) {
                vVideoView = (MtkVideoView) findViewById(R.id.mmp_video_videoview);
                vVideoView.setVisibility(View.VISIBLE);
              }
              // vVideoView.init();

              if (file instanceof LocalFileAdapter
                  || file instanceof SmbFileAdapter
                  || file instanceof LenFileAdapter) {
                playSelectionItem(file.getAbsolutePath());
              } else {
                playSelectionItem(file.getName() + file.getSuffix());
              }

            }
          }
          break;
        case MUSIC_PLAYING:
          onShowMusicView();
          break;
        case TTS_FOCUS:
          int index = findSelectItem(msg.obj.toString());
          if (index >= 0) {
            setListSelection(index);
          }
          break;
        default:
          break;

      }
    }
  };

  // modified by keke for fix DTV00380564
  @Override
  protected void onPause() {
    if (getListContentType() == FilesManager.CONTENT_VIDEO) {
      String m = TVStorage.getInstance(this).get(
          "SCREENMODE_FILELIST");
      try {
        if (null != m && m.length() > 0) {
          int u = Integer.parseInt(m);
          if (u != 1) {
            LogicManager.getInstance(this).setScreenMode(u);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    super.onPause();
    removeView();
    Util.logLife(TAG, "onPause");
  }

  /*
   * if from list mode to grid mode, true, else false, for clear info and
   * thumbnail array not in
   * onstop function, but in press bule key. avoid clear array item when add in
   * grid mode Help me?
   */
  private boolean mIsGridMode = false;

  /* For close preview mode video add by lei */
  @Override
  protected void onDestroy() {
    if (vVideoView != null) {
      vVideoView.onRelease();
    }
    if (!mIsGridMode) {
      if (null != mInfoLoader) {
        mInfoLoader.clearQueue();
      }

      if (null != mLoader) {
        mLoader.clearQueue();
      }
    }
    mIsGridMode = false;

    mAudioManager.abandonAudioFocus(mAudioFocusListener);
    mAudioManager = null;
    vList.setAccessibilityDelegate(null);
    super.onDestroy();
    Util.logLife(TAG, "onDestroy");
  }

  IAudioPlayListener mListListener = new IAudioPlayListener() {

    @Override
    public void notify(int status) {
      // TODO Auto-generated method stub
      myHandler.sendEmptyMessage(MUSIC_PLAYING);
      LogicManager.getInstance(MtkFilesListActivity.this).registerAudioPlayListener(null);
    }

  };

  private void onShowMusicView() {
    // TODO Auto-generated method stub
    if ((getListContentType() != FilesManager.CONTENT_VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      if (mPopView != null && !mPopView.isShowing()) {
        if (GetCurrentTask.getInstance(this)
            .getCurRunningClassName()
            .equals("com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesListActivity")) {
          showMusicView();
        }
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Util.logLife(TAG, "onStart");
    LogicManager.getInstance(this).registerAudioPlayListener(mListListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onRestart() {
    super.onRestart();
    // int position = setCurrentSelection();
    // FileAdapter file = getListItem(position);
    // if (null != file) {
    // if (file.isFile()
    // && (getListContentType() == FilesManager.CONTENT_VIDEO)&&vVideoView!=null){
    // modified by keke for DTV00383229
    // vVideoView.init();
    // playSelectionItem(file.getAbsolutePath());
    // }
    // }
    Util.logLife(TAG, "onRestart");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int setupContentView() {
    return R.layout.mmp_files_list;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void bindData(Intent intent) {
    int mode = intent.getIntExtra("Mode", -1);
    if (mode > -1) {
      mMode = mode;
    }

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      getIntentData(intent);
      setupHeader();
      refreshListView(null);
    } else {
      super.bindData(intent);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected FilesAdapter getAdapter() {
    return new MtkFilesListAdapter(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void findViews() {
    vTopImage = (ImageView) findViewById(R.id.mmp_list_topimg);
    vTopTV = (TextView) findViewById(R.id.mmp_list_toptv);
    vTopFilePath = (TextView) findViewById(R.id.mmp_list_filepath);
    vTopPageSize = (TextView) findViewById(R.id.mmp_list_pagesize);
    vThumbnail = (ImageView) findViewById(R.id.mmp_listmode_thumnail);
    vDetailInfo = (TextView) findViewById(R.id.mmp_list_detailinfo);

    vVideoView = (MtkVideoView) findViewById(R.id.mmp_video_videoview);
    vTextShow = (TextView) findViewById(R.id.mmp_listmode_textshow);

    vMidLayout = (RelativeLayout) findViewById(R.id.mmp_listmode_rl);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setupHeader() {
    vThumbnail.setVisibility(View.GONE);
    if (vVideoView != null) {
      vVideoView.setVisibility(View.GONE);
    }
    vTextShow.setVisibility(View.GONE);

    int contentType = getListContentType();
    if (contentType >= 0) {
      if (contentType == FilesManager.CONTENT_PHOTO
          || contentType == FilesManager.CONTENT_AUDIO) {
        vThumbnail.setVisibility(View.VISIBLE);
      } else if (contentType == FilesManager.CONTENT_VIDEO) {

        if (vVideoView != null) {
          if (mMode == MODE_FILES_LIST) {
            vVideoView.setVisibility(View.VISIBLE);
          } else {
            vVideoView.setVisibility(View.GONE);
          }
        }
      } else if (contentType == FilesManager.CONTENT_TEXT) {
        vTextShow.setVisibility(View.VISIBLE);
      } else if (contentType == FilesManager.CONTENT_THRDPHOTO) {
        // DMR,THRDPHOT BOTH==4
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          contentType = FilesManager.CONTENT_THRDPHOTO;
        } else {
          vThumbnail.setVisibility(View.VISIBLE);
          contentType = FilesManager.CONTENT_PHOTO;
        }
      }

      vTopImage.setImageDrawable(mContentTypeIcons[contentType]);
      vTopTV.setText(mContentTypeNames[contentType]);
    }

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      vTopFilePath.setText("");
    } else if (mMode == MODE_FILES_LIST) {
      String curPath = getListCurrentPath();
      // if (curPath != null && curPath.startsWith("/storage")) {
      // MultiFilesManager multiFileManager = MultiFilesManager
      // .getInstance(this);
      // List<FileAdapter> deviceList = multiFileManager.getLocalDviceAdapter();
      // if (deviceList != null && deviceList.size() > 0) {
      // for (int i = 0; i < deviceList.size(); i++) {
      // if (curPath.contains(deviceList.get(i).getPath())) {
      // curPath = curPath.substring(deviceList.get(i).getPath().length());
      // curPath = "/storage/" + deviceList.get(i).getName() + curPath;
      // break;
      // }
      // }
      // }
      // }
      vTopFilePath.setText(curPath);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void customMenu(MenuListView menu) {
    /*
     * menu.removeItem(2); //arcelik customized
     */ }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if ((Util.isTTSEnabled(getApplicationContext()) &&
        KeyMap.KEYCODE_MTKIR_BLUE == keyCode)
        || (!Util.isTTSEnabled(getApplicationContext()) &&
            KeyMap.KEYCODE_MTKIR_YELLOW == keyCode)) {
      MtkLog.i(TAG, "onKeyDown keycode: " + keyCode);
      return super.onKeyDown(keyCode, event);
    }

    switch (keyCode) {
      case KeyMap.KEYCODE_MENU:
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          return true;
        }
        break;
      case KeyMap.KEYCODE_MTKIR_YELLOW:
      case KeyMap.KEYCODE_MTKIR_BLUE:
        if (!MediaMainActivity.isValid(400)) {
          break;
        }

        if (true == Feature.isSupportCnSamba()) {
          final String curPath = getListCurrentPath();
          if ((curPath != null && curPath.equals("/"))
              || (true == Tools.isSambaPlaybackUrl(curPath))) {
            if ((mMode != MODE_CONTENT_TYPES_LIST)) {
              MtkLog.d(TAG, "Yellow/Blue key event is ignored in Sam path");
              return true;
            }
          }
        }

        Intent intent = new Intent();
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          // mFilesManager.destroy();
          // mFilesManager.destroyManager();
          // mFilesManager.deleteObservers();
          stopMusicView();
          // intent.setClass(this, MediaMainActivity.class);
          // MediaMainActivity.mSelection=getListSelectedItemPosition() + 1;
          // intent.putExtra("selection", getListSelectedItemPosition() + 1);
          // intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          finish();
          if (MultiFilesManager.hasInstance()) {
            MultiFilesManager.getInstance(this).destroy();
          }
          return true;
        } else if (mMode == MODE_FILES_LIST) {
          removeView();
          mLoader.clearQueue();
          mIsGridMode = true;
          intent.setClass(this, MtkFilesGridActivity.class);
          intent.putExtra(INTENT_NAME_PATH, getListCurrentPath());
          intent.putExtra(INTENT_NAME_SELECTION, getListSelectedItemPosition());
          intent.putStringArrayListExtra(INTENT_NAME_COPYED_FILES, new ArrayList<String>(mCopyedFiles));
          intent.putStringArrayListExtra(INTENT_NAME_SELECTED_FILES, new ArrayList<String>(mSelectedFiles));
          // add for ThumbnailSize bug
          intent.putExtra("mThumbnailSize", mThumbnailSize);

          // MediaMainActivity.mSelection=getListSelectedItemPosition();
        }
        startActivity(intent);
        finish();
        break;
      case KeyMap.KEYCODE_MTKIR_ANGLE:
        // removeView();
        // Util.exitMmpActivity(getApplicationContext());
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * {@inheritDoc}
   */
  private int currentPlay = -1;

  @Override
  protected void onListItemClick(AbsListView l, View v, int position, long id) {
    MtkLog.i(TAG, "onListItemClick position:" + position);

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      if (4 == position) {
        Util.exitPIP(this);
        LogicManager.getInstance(this).finishVideo();
        if (VideoPlayActivity.getInstance() != null) {
          VideoPlayActivity.getInstance().finish();
        }
        LogicManager.getInstance(this).stopAudio();
        Intent intent = new Intent(MmpConst.INTENT_DMR);
        intent.putExtra("TKUI", true);
        intent.putExtra("FILE_LIST", true);
        this.startActivity(intent);
      } else {

        SaveValue pref = SaveValue.getInstance(this);
        boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false
            : true;

        if (true == Feature.isSupportCnSamba()) {
          smbAvailable = true;
        }

        MtkLog.d(TAG, "Samba Available : " + smbAvailable);
        boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false
            : true;
        MtkLog.d(TAG, "Dlna Available : " + dlnaAvailable);

        MultiFilesManager.getInstance(this).getLocalDevices();
        int deviceNum = MultiFilesManager.getInstance(this).getAllDevicesNum();
        if (deviceNum == MultiFilesManager.NO_DEVICES && !dlnaAvailable && !smbAvailable) {
          return;
        }

        mMode = MODE_FILES_LIST;

        mFilesManager.pushOpenedHistory(position);
        openDir(getListRootPath());

        if (getListContentType() == FilesManager.CONTENT_VIDEO) {
          LogicManager.getInstance(this).stopAudio();
          stopMusicView();
        }
      }
      return;

    }

    if (position == 0) {
      if (super.mMode == MODE_NORMAL) {
        MultiFilesManager multiFileManager = MultiFilesManager
            .getInstance(this);
        String path = multiFileManager.getFirstDeviceMountPointPath();
        if (multiFileManager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES) {
          if (getListCurrentPath().equals(path)) {
            onReachRoot(getListContentType());
            return;
          }
        }
        int cur = mFilesManager.popOpenedHistory();
        if (null != getListParentPath()) {
          cur = cur + 1;
        }
        openDir(getListParentPath(), cur);
      } else if (super.mMode == MODE_RECURSIVE) {
        MultiFilesManager multiFileManager = MultiFilesManager
            .getInstance(this);
        if (MultiFilesManager.ROOT_PATH.equals(getListCurrentPath())) {
          onReachRoot(getListContentType());

        } else {
          if (multiFileManager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES) {
            onReachRoot(getListContentType());
          } else {
            openDir(MultiFilesManager.ROOT_PATH, 0);
          }
        }

      }
    } else {
      if (getListContentType() == FilesManager.CONTENT_VIDEO) {
        removeView();
      }
      // super.onListItemClick(l, v, position, id);

      FileAdapter file = getListItem(position);
      mAdapter.cancel();
      if (null != file && file.isDirectory()) {
        // open directory
        mFilesManager.pushOpenedHistory(position - 1);
        currentPosition = position;
        isFirstRequest = true;
        if (true == Feature.isSupportCnSamba()) {
          MultiFilesManager.getInstance(this).setCurrentPos(position - 1);
        }
        openDir(file.getAbsolutePath(), 0);

      } else if (null != file) {
        currentPlay = position;
        // play multi media file
        // int pos = 0;
        /*
         * pos = getListSelectedItemPosition(); if (pos < 0) { return; }
         */
        int pos = position;
        if (this instanceof MtkFilesListActivity) {
          pos -= 1;
        }
        if (pos < 0) {
          return;
        } // support mouse click by lei modif.
        ((MultiFilesManager) mFilesManager).getPlayList(pos);
        // int contentType = getListContentType();
        // if (contentType == FilesManager.CONTENT_PHOTO ||
        // contentType == FilesManager.CONTENT_THRDPHOTO) {
        // file.stopDecode();

        // file.stopThumbnail();
        // }
        playFile(file.getAbsolutePath());
      }
    }
  }

  @Override
  protected void playFile(String path) {
    LogicManager.getInstance(this).registerAudioPlayListener(null);
    super.playFile(path);
  }

  /**
   * Count page size
   */
  protected void computePageSize() {
    if (mPageSize == 0) {
      MtkLog.d(TAG, "H : " + vList.getHeight());
      MtkLog.d(TAG, "W : " + vList.getWidth());

      int h = vList.getHeight();
      int size = mListItemHeght;
      MtkLog.d(TAG, "Size : " + size);

      int row = h / size;
      mPageSize = row;

      MtkLog.d(TAG, "PageSize : " + mPageSize);
    }

    if (mPageSize > 0) {
      int filesCount = getListItemsCount();
      mPageCount = filesCount / mPageSize;
      if (filesCount % mPageSize > 0 || filesCount == 0) {
        mPageCount += 1;
      }

      MtkLog.d(TAG, "ItemCount : " + filesCount);
      MtkLog.d(TAG, "PageCount : " + mPageCount);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onReachRoot(int selection) {
    // super.onReachRoot(selection);

    mMode = MODE_CONTENT_TYPES_LIST;

    Intent intent = new Intent(this, this.getClass());
    intent.putExtra(INTENT_NAME_PATH, getListParentPath());
    intent.putExtra(INTENT_NAME_SELECTION, selection);
    startActivity(intent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListItemSelected(AbsListView l, View v, int position,
      long id) {
    MtkLog.i(TAG, "onListItemSelected position:" + position + "  mMode:" + mMode
        + "  pip:" + Util.mIsEnterPip);
    currentPlay = -1;
    if (myVideoPlayDelay != null && vVideoView != null) {
      vVideoView.removeCallbacks(myVideoPlayDelay);
    }
    if (mInfoLoader != null && mInfoLoader.getTaskSize() > 0) {
      mInfoLoader.clearQueue();
    }
    if (mLoader != null && mLoader.getTaskSize() > 0) {
      mLoader.clearQueue();
    }

    int fileCount = getListItemsCount();
    int listCount = getListView().getCount();
    if (mMode != MODE_CONTENT_TYPES_LIST) {
      if (position == 0) {
        vTopPageSize.setText("");
      } else {
        vTopPageSize.setText(String.format("%d/%d", getListSelectedItemPosition() + 1, fileCount));
      }
    } else {
      vTopPageSize.setText(String.format("%d/%d", getListSelectedItemPosition() + 2, listCount));
    }

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      setListContentType(position);
      setupHeader();
      vDetailInfo.setText("");
      if (vVideoView != null && !Util.mIsEnterPip) {
        vVideoView.reset();
      }
      vThumbnail.setImageBitmap(null);
      vTextShow.setText("");
    } else {
      if (position == 0) {
        vTopPageSize.setText("");
        vThumbnail.setImageBitmap(null);
        vDetailInfo.setText("");
        vTextShow.setText("");
        if (vVideoView != null && !Util.mIsEnterPip) {
          vVideoView.reset();
        }
      } else if (position > 0) {
        FileAdapter file = getListItem(position);
        int contentType = getListContentType();
        // ((MultiFilesManager) mFilesManager).getPlayList(position-1);
        if (file != null
            && (file.isDirectory() || file.isIsoFile())) {
          if (contentType == FilesManager.CONTENT_TEXT) {
            vTextShow.setText("");
          } else if (contentType == FilesManager.CONTENT_VIDEO && vVideoView != null) {
            if (!Util.mIsEnterPip) {
              vVideoView.reset();
            }
          } else {
            vThumbnail.setImageBitmap(null);
          }
          vDetailInfo.setText("");
        } else {
          if (contentType != FilesManager.CONTENT_TEXT
              && contentType != FilesManager.CONTENT_AUDIO) {
            Util.exitPIP(this);
            LogicManager.getInstance(getApplicationContext()).finishVideo();
            if (VideoPlayActivity.getInstance() != null) {
              VideoPlayActivity.getInstance().finish();
            }
            if (Util.mIsEnterPip) {
              Util.mIsEnterPip = false;
              if (vVideoView != null) {
                vVideoView.reset();
              }
            }
          }

          if (mFilesManager.getContentType() == MultiMediaConstant.VIDEO) {
            if (!Util.mIsUseEXOPlayer) {
              AudioBTManager.getInstance(getApplicationContext()).creatAudioPatch();
            }
            mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
          }

          if (contentType == FilesManager.CONTENT_TEXT) {
            NetworkFileOperation playText = new NetworkFileOperation();
            playText.execute(file);
          } else if (contentType == FilesManager.CONTENT_VIDEO) {
            // TODO paly video
            currentPlay = position;
            MultiFilesManager filesManager = MultiFilesManager
                .getInstance(this);
            int source = filesManager.getCurrentSourceType();
            myVideoPlayDelay = new VideoPlayDelay(file, source);

            if (vVideoView == null) {
              MtkLog.d(TAG, "onListItemSelected~~vVideoView == null~");

              vVideoView = (MtkVideoView) findViewById(R.id.mmp_video_videoview);
              vVideoView.setVisibility(View.VISIBLE);
            }
            if (vVideoView != null) {
              MtkLog.d(TAG, "onListItemSelected~~mIsFirstPlayVideoListMode==" + Util.mIsFirstPlayVideoListMode);
              if (Util.mIsFirstPlayVideoListMode) {
                // add for DTV00928707
                vVideoView.postDelayed(myVideoPlayDelay, 2000);
                Util.mIsFirstPlayVideoListMode = false;
              } else {
                vVideoView.postDelayed(myVideoPlayDelay, 1000);
              }
            }
          } else {
            vThumbnail.setImageBitmap(null);
            // mLoader.clearQueue();
            if (mLoader != null) {
              mLoader.addWork(new LoadCorverPic(file));
            }
          }

          int type = mFilesManager.getContentType();
          String info = "";
          if (type == MultiMediaConstant.AUDIO || type == MultiMediaConstant.VIDEO
              || type == MultiMediaConstant.PHOTO || type == MultiMediaConstant.THRD_PHOTO) {
            if (file != null) {
              info = mInforCache.get(file.getAbsolutePath());
            }
            if (info != null) {
              vDetailInfo.setText(info);
              return;
            } else {
              vDetailInfo.setText("");
            }
            String suffix = file.getSuffix();
            if (!FileSuffixConst.DLNA_FILE_NAME_EXT_PCM
                .equalsIgnoreCase(suffix)) {
              // mInfoLoader.clearQueue();
              if (mInfoLoader != null) {
                mInfoLoader.addSelectedInfoWork(new LoadInfo(file, vDetailInfo));
              }
            } else {
              vDetailInfo.setText(String.format("%s%s", file.getName(), file.getSuffix()));
            }
          } else {
            info = file.getInfo();
            vDetailInfo.setText(info);
            return;
          }
        }
      }
    }

  }

  class NetworkFileOperation extends AsyncTask<FileAdapter, String, String> {

    @Override
    protected String doInBackground(FileAdapter... params) {
      MtkLog.d(TAG, "NetworkFileOperation doInBackground params[0]: " + params[0]);
      if (params[0] != null) {
        return params[0].getPreviewBuf();
      }
      return null;
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      MtkLog.d(TAG, "NetworkFileOperation onPostExecute result: " + result);
      if (result != null) {
        vTextShow.setText(result);
      }
    }
  }

  private class LoadCorverPic implements LoadWork<Bitmap> {

    private final FileAdapter mFile;

    public LoadCorverPic(FileAdapter file) {
      mFile = file;
    }

    @Override
    public Bitmap load() {

      Bitmap thumbnail = null;
      try {
        // Original
        thumbnail = mFile.getThumbnail(vThumbnail.getWidth(),
            vThumbnail.getHeight(), false);
        // EXO DLNA MARK
        // thumbnail = null;
      } catch (OutOfMemoryError e) {
        thumbnail = ((BitmapDrawable) mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_big))
            .getBitmap();
      }

      return thumbnail;
    }

    @Override
    public void loaded(final Bitmap result) {
      if (null != result) {
        vThumbnail.post(new Runnable() {

          @Override
          public void run() {
            vThumbnail.setImageBitmap(Util.getScaledBitmap(result));
          }
        });
      }

    }

  }

  private static VideoPlayDelay myVideoPlayDelay;

  private class VideoPlayDelay implements Runnable {
    private final FileAdapter file;
    private final int sourceType;

    public VideoPlayDelay(FileAdapter file, int sourceType) {
      this.file = file;
      this.sourceType = sourceType;
    }

    @Override
    public void run() {
      if (file instanceof LocalFileAdapter) {
        playSelectionItem(file.getAbsolutePath());
      } else if (sourceType == MultiFilesManager.SOURCE_SMB) {
        playSelectionItem(file.getAbsolutePath());
      } else {
        playSelectionItem(file.getName() + file.getSuffix());
      }

      myVideoPlayDelay = null;
    }
  }

  private void playSelectionItem(String path) {
    if (isValid() && vVideoView != null) {
      MtkLog.d(TAG, "playSelectionItem path:" + path + "  vVideoView.isVideoPlaybackInit():"
          + vVideoView.isVideoPlaybackInit());
      if (!vVideoView.isVideoPlaybackInit()) {
        vVideoView.init();
      }
      vVideoView.reset();
      vVideoView.setPreviewMode(true);
      vVideoView.play(path);
      // Canvas canvas = vVideoView.getHolder().lockCanvas(null);
      // vVideoView.getHolder().unlockCanvasAndPost(canvas);
    }
  }

  @Override
  public FileAdapter getListItem(int position) {
    return super.getListItem(position - 1);
  }

  @Override
  public FileAdapter getListSelectedItem() {
    return getListItem(super.getListSelectedItemPosition());
  }

  @Override
  public int getListSelectedItemPosition() {
    return super.getListSelectedItemPosition() - 1;
  }

  @Override
  protected void refreshListView(List<FileAdapter> files) {
    super.refreshListView(files);

    if (mMode == MODE_FILES_LIST) {
      vTopPageSize.setText(String.format("%d/%d", mCurrentPage, mPageCount));
    }
  }

  @Override
  protected void refreshListView() {
    super.refreshListView();
    if (mFilesManager.isRefresh()) {
      setListSelection(mFilesManager.getPositionInParent());
      MtkLog.i(TAG, "is same path, need set position");
    } else {
      MtkLog.i(TAG, "is not same path, no need set position");
    }
  }

  private class MtkFilesListAdapter extends FilesAdapter {
    private Drawable mFolder;
    private Drawable mFolderBack; //arcelik customized
    private Drawable mFilePhotoIcon;
    private Drawable mFileVideoIcon;
    private Drawable mFileMusicIcon;

    private Drawable mPlaying;
    private final String[] mContentTypes;

    public MtkFilesListAdapter(Context context) {
      super(context);

      prepareDefaultThumbnails();

      mContentTypes = mContentTypeNames;
    }

    private void prepareDefaultThumbnails() {
      Resources resources = getResources();

      mFolder = resources.getDrawable(R.drawable.mmp_list_icon_selected);

      mPlaying = resources
          .getDrawable(R.drawable.mmp_toolbar_typeicon_paly);

      mFolderBack = resources.getDrawable(R.drawable.folder_back); //arcelik customized
      mFilePhotoIcon = resources.getDrawable(R.drawable.mmp_thumbnail_icon_photo_samll);
      mFileVideoIcon = resources.getDrawable(R.drawable.mmp_thumbnail_icon_video_small);
      mFileMusicIcon = resources.getDrawable(R.drawable.mmp_thumbnail_icon_audio_samll);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
        return mContentTypes.length;
      }

      return super.getCount() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileAdapter getItem(int position) {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
        return null;
      }

      if (position == 0) {
        return null;
      }

      return super.getItem(position - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
        return position;
      }

      if (position == 0) {
        return 0;
      }

      return super.getItemId(position - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getItemLayout() {
      return R.layout.mmp_listmode_item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initView(View v, FileAdapter data, boolean flag) {
      ViewHolder holder = new ViewHolder();

      holder.folderIcon = (ImageView) v
          .findViewById(R.id.mmp_listmode_icon);
      holder.folderName = (TextView) v
          .findViewById(R.id.mmp_listmode_foldername);

      holder.copyIcon = (ImageView) v
          .findViewById(R.id.mmp_listmode_copyicon);

      v.setLayoutParams(new ListView.LayoutParams(
          LayoutParams.MATCH_PARENT, mListItemHeght));
      v.setTag(holder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void bindView(View v, FileAdapter data, int position) {
      ViewHolder holder = (ViewHolder) v.getTag();
      v.setBackgroundDrawable(null);

      if (mMode == MODE_CONTENT_TYPES_LIST) {
        holder.folderIcon.setVisibility(View.VISIBLE);
        holder.folderName.setText(mContentTypes[position]);
        holder.copyIcon.setImageDrawable(null);
        return;
      }

      if (position == 0) {
        holder.folderIcon.setVisibility(View.VISIBLE);
        holder.folderName.setText("[..]");
        holder.folderIcon.setImageDrawable(mFolderBack); //arcelik customized
        holder.copyIcon.setImageDrawable(null);
      } else {
        if (data == null) {

          holder.folderIcon.setImageDrawable(null);
          holder.folderName.setText("");
          holder.copyIcon.setImageDrawable(null);
          return;
        }

        if (data.isDirectory()) {
          holder.folderIcon.setVisibility(View.VISIBLE);
          holder.folderIcon.setImageDrawable(mFolder);
          holder.folderName.setText(data.getName());
          holder.copyIcon.setImageDrawable(null);
        } else {
          if (position == mPlayPosition) {
            holder.folderIcon.setVisibility(View.VISIBLE);
            holder.folderIcon.setImageDrawable(mPlaying);
          } else {

            //arcelik customized
            int contentType = getListContentType();
            if (contentType == FilesManager.CONTENT_PHOTO){
              holder.folderIcon.setVisibility(View.VISIBLE);
              holder.folderIcon.setImageDrawable(mFilePhotoIcon);
            } else if (contentType == FilesManager.CONTENT_AUDIO){
              holder.folderIcon.setVisibility(View.VISIBLE);
              holder.folderIcon.setImageDrawable(mFileMusicIcon);
            } else if (contentType == FilesManager.CONTENT_VIDEO){
              holder.folderIcon.setVisibility(View.VISIBLE);
              holder.folderIcon.setImageDrawable(mFileVideoIcon);
            } else {
              holder.folderIcon.setVisibility(View.INVISIBLE);
            }
          }
          // arcelik customized

          //arcelik customized
          /*
          String path = data.getAbsolutePath();
          if (mCopyedFiles.size() > 0 && mCopyedFiles.contains(path)) {
            holder.copyIcon
                .setImageResource(R.drawable.mmp_listmode_icon_copy);
          } else if (mSelectedFiles.size() > 0 && mSelectedFiles.contains(path)) {
            holder.copyIcon
                .setImageResource(R.drawable.mmp_listmode_icon_select);
          } else {
            holder.copyIcon.setImageDrawable(null);
          }*/
          holder.copyIcon.setImageDrawable(null);

          holder.folderName
              .setText(String.format("%s%s", data.getName(), data.getSuffix()));
        }

      }
    }

    private class ViewHolder {
      ImageView folderIcon;
      TextView folderName;
      ImageView copyIcon;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListEmpty() {

    MultiFilesManager multiFileManager = MultiFilesManager
        .getInstance(this);
    if (multiFileManager.getAllDevicesNum() == MultiFilesManager.NO_DEVICES) {
      super.onListEmpty();
      vList.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListNotEmpty() {
    super.onListNotEmpty();
    vList.setVisibility(View.VISIBLE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBackPressed() {
    Log.i(TAG, "onBackPressed");
    String currentPath = getListCurrentPath();
    if (MultiFilesManager.ROOT_PATH.equals(currentPath)) {
      setListContentType(FilesManager.CONTENT_VIDEO);
    }

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      removeView();
      mFilesManager.deleteObserver(this);
      mFilesManager.destroy();
      MmpApp destroyApp = (MmpApp) getApplicationContext();
      destroyApp.finishAll();
    } else if (mMode == MODE_FILES_LIST) {
      vList.setVisibility(View.VISIBLE);
      onListItemClick(null, null, 0, 0);
    }
  }

  @Override
  protected void removeVideoView() {
    removeView();
  }

  protected static synchronized void removeView() {
    if (vVideoView != null) {
      vVideoView.removeCallbacks(myVideoPlayDelay);
      vVideoView.setVisibility(View.GONE);
      vVideoView.onRelease();
      vVideoView = null;
    }
  }

  @Override
  protected void stopMusicView() {
    if (vMusicView != null) {
      vMusicView.removeMessage();
    }
    super.stopMusicView();
  }

  @Override
  public void openDir(String path, int selection) {
    if (mMode == MODE_CONTENT_TYPES_LIST && mFlag) {
      if ((getListContentType() != FilesManager.CONTENT_VIDEO)
          && LogicManager.getInstance(this).isAudioStarted()) {
        showMusicView();
      } else {
        LogicManager.getInstance(this).setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
        stopMusicView();
      }
      return;
    }
    super.openDir(path, selection);
  }

  @Override
  protected void stopVideoListMode() {
    if (null != vVideoView && !Util.mIsEnterPip) {
      vVideoView.reset();
    }
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    removeView();
    LogicManager.getInstance(this).stopAudio();
    stopMusicView();
  }
}
