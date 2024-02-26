
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.PlaybackService;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.UsbFileOperater;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.UsbFileOperater.OnUsbCopyProgressListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ConfirmDeleteDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ConfirmDeleteDialog.OnConfirmDeleteListener;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ConfirmReplaceDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ConfirmReplaceDialog.OnConfirmReplaceListener;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.KeyboardDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.KeyboardDialog.OnPressedListener;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.LoginDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.LoginDialog.OnKeyClickListener;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ProgressDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.ImagePlayerActivity;
import com.mediatek.wwtv.mediaplayer.mmp.model.DlnaFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader.LoadWork;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager.UnmountLisenter;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.IRootMenuListener;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.dm.MountPoint;
import com.mediatek.wwtv.util.TVContent;


import android.net.RouteInfo;
//import android.media.AudioManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;

public class MtkFilesBaseListActivity extends FilesListActivity<FileAdapter> {

  /**
   * Log tag
   */
  private static final String TAG = "MtkFilesBaseListActivity";

  public static final String INTENT_NAME_PATH = "Path";

  public static final String INTENT_NAME_SELECTION = "Position";

  public static final String INTENT_NAME_COPYED_FILES = "CopyedFiles";
  public static final String INTENT_NAME_SELECTED_FILES = "SelectedFiles";
  public static final String USBDISK = "/mnt/usbdisk";

  public static final int MODE_NORMAL = 1;

  public static final int MODE_RECURSIVE = 2;

  protected static final int DIALOG_EMPTY_TIP = 0;

  protected static final int DIALOG_MENU = 1;

  protected static final int DIALOG_REU_TIP = 2;

  protected static final int DIALOG_LOGIN = 3;

  protected static final int DIALOG_KEYBOARD = 4;

  protected static final int DIALOG_LOGIN_FAILED_TIP = 5;

  protected static final int DIALOG_PROGRESS = 6;

  protected static final int DIALOG_CONFIRM_DELETE = 7;

  protected static final int DIALOG_CONFIRM_REPLACE = 8;

  protected static final int DIALOG_LOADING = 9;

  private static final int MSG_RECURSIVE_PARSER = 0;

  private static final int MSG_PHOTO_FRAME = 1;

  private static final int MSG_NOSUPPORT_SHOW = 10;

  private static final int MSG_NOSUPPORT_DISMISS = 11;

  public static final int MSG_LOGIN_RESULT = 301;

  public static final int MSG_DLNA_AUTO_TEST_UPDATE_FILE = 359;

  public static final int MSG_COPY_FILE_FAIL = 360;

  public static final int KEY_DURATION = 400;

  protected long mLastKeyDownTime;

  private ProgressDialog vProgressDialog;

  private LoginDialog vLoginDialog;

  private TipsDialog reuTip;
  private TipsDialog loadTip;

  private ConfirmDeleteDialog vConfirmDeleteDialog;

  protected Resources mResources;

  // Added by Dan for fix bug DTV00379203
  protected List<String> mSelectedFiles;

  protected List<String> mCopyedFiles;

  protected String[] mContentTypeNames;

  protected Drawable[] mContentTypeIcons;

  public int mThumbnailSize;

  protected int mPlayMode;

  protected int mCurrentPage;

  protected int mPageSize;

  protected int mPageCount;

  private boolean mPasteCanceled;
  private boolean mPasteReplaceNo;

  private long mPasteSizeCount;

  protected PopupWindow mPopView;

  protected MusicPlayInfoView vMusicView;

  protected List<FileAdapter> mRecursiveFiles;

  private boolean mYes;

  private boolean mYesToAll;

  public static int mMode = MODE_NORMAL;

  private boolean isCancel = false;

  private Thread mRecursThread;

  public static boolean mFlag = false;

  protected static boolean isFromDmr = false;

  protected TipsDialog mTipsDialog;
  protected AsyncLoader<String> mInfoLoader;
  protected ConcurrentHashMap<String, LoadInfo> mLoadInfoWorks;

  public int currentPosition = 0;
  protected String mCurrentSelectedPath;
  public boolean isFirstRequest = true;
  // private MtkNetWorkRecevier netRecevier;
  // private BaseNetworkObserver mBaseNetworkObserver;
  // private INetworkManagementService mINetworkManagementService;
  public static int mViewMode;
  public static final int VIEW_MODE_GRID = 1;
  public static final int VIEW_MODE_LIST = 2;

  private Toast mToast = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (true == Feature.isSupportCnSamba()) {
      MultiFilesManager.getInstance(MtkFilesBaseListActivity.this).initSamba(MtkFilesBaseListActivity.this);
    }
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    // try {
    // mBaseNetworkObserver = new BaseNetworkObserver();
    // mINetworkManagementService =
    // INetworkManagementService.Stub.asInterface(ServiceManager
    // .getService(Context.NETWORKMANAGEMENT_SERVICE));
    // if (mINetworkManagementService != null && mBaseNetworkObserver != null) {
    // mINetworkManagementService.registerObserver(mBaseNetworkObserver);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }

    // mInfoLoader = new AsyncLoader<String>(1);
    // netRecevier = new MtkNetWorkRecevier();
    // IntentFilter intentFilter = new
    // IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    // registerReceiver(netRecevier, intentFilter);
    mInfoLoader = AsyncLoader.getInstance(1);
    mLoadInfoWorks = new ConcurrentHashMap<String, LoadInfo>();
    LogicManager.getInstance(this.getApplicationContext()).registerUnMountLisenter(unmountLisenter);
    // dmr_2014 ,clos dmr;
    // DmrUtil.closeDmr(getApplicationContext());
    MtkLog.d(TAG, "onCreate");
    mHandlerThead = new HandlerThread(TAG);
    mHandlerThead.start();
    mThreadHandler = new Handler(mHandlerThead.getLooper());
  }

  private final UnmountLisenter unmountLisenter = new UnmountLisenter() {
    @Override
    public void onUnmount(String devicePath) {
      String path = LogicManager.getInstance(MtkFilesBaseListActivity.this)
          .getCurrentFilePath(Const.FILTER_AUDIO);
      MtkLog.d(TAG, "onUnmount path:" + path);
      if (path != null && path.startsWith(devicePath)) {
        MtkLog.d(TAG, "onUnmount go stop and remove music");
        PlayList.getPlayList().cleanList(Const.FILTER_AUDIO);
        LogicManager.getInstance(MtkFilesBaseListActivity.this).stopAudio();
        stopMusicView();
      }
    }
  };

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      try {
        dismissDialog(DIALOG_REU_TIP);
      } catch (IllegalArgumentException e) {
        // e.printStackTrace();
        MtkLog.d(TAG, "dismissDialog DIALOG_REU_TIP exception~~");
      }
      switch (msg.what) {
        case MSG_DLNA_AUTO_TEST_UPDATE_FILE:
          if (((MultiFilesManager) mFilesManager).getCurrentPath()
              .equals(MultiFilesManager.ROOT_PATH)) {
            openDevicePath();
          } else {
            searchDesFile(msg.arg1 == 0 ? true : false);
          }
          break;
        case MSG_LOGIN_RESULT:
          if (msg.arg1 == 1) {// login success
            mFilesManager.popOpenedHistory();
            openDir(getListCurrentPath());
          } else {
            MtkLog.w(TAG, "Login Failed!!");
            showDialog(DIALOG_LOGIN_FAILED_TIP);
          }
          break;
        case MSG_RECURSIVE_PARSER:
          try {
            dismissDialog(DIALOG_EMPTY_TIP);
          } catch (IllegalArgumentException e) {
            // e.printStackTrace();
            MtkLog.d(TAG, "dismissDialog DIALOG_EMPTY_TIP exception~~");
          }
          if (mRecursiveFiles != null && !mRecursiveFiles.isEmpty()) {
            MultiFilesManager
                .getInstance(MtkFilesBaseListActivity.this)
                .setFiles(mRecursiveFiles);
            mMode = MODE_RECURSIVE;
            mFilesManager.setPositionInParent(0);
            refreshListView(mRecursiveFiles);
            // setCurrentSelection();
            // setListSelection(mFilesManager.getPositionInParent());
          } else {
            mMode = MODE_RECURSIVE;
            refreshListView(null);
            showDialog(DIALOG_EMPTY_TIP);

          }
          break;
        case MSG_PHOTO_FRAME:
          if (mRecursiveFiles != null && !mRecursiveFiles.isEmpty()) {
            ((MultiFilesManager) mFilesManager).getPlayList(
                mRecursiveFiles, 0, FilesManager.CONTENT_PHOTO,
                MultiFilesManager.SOURCE_LOCAL);

            Intent intent = null;
            if (Util.isSupport4K8K()) {
              intent = new Intent(MtkFilesBaseListActivity.this,
                  Photo4K2KPlayActivity.class);
            } else {
              intent = new Intent(MtkFilesBaseListActivity.this,
                  PhotoPlayActivity.class);
            }
            intent.putExtra("PlayMode", mPlayMode);
            startActivity(intent);
            // finish();
          }
          break;
        case MSG_NOSUPPORT_SHOW:
          if (mHandler != null) {
            onNotSuppsort(getString(R.string.mmp_featue_notsupport));
            // mHandler.sendEmptyMessageDelayed(MSG_NOSUPPORT_DISMISS, 1000);
          }
          break;
        case MSG_NOSUPPORT_DISMISS:
          dismissNotSupprot();
          break;

        case MSG_COPY_FILE_FAIL:
          String msg1 = "copy " + mCurrentCopyFileName + " fail";
          Toast.makeText(getApplicationContext(), msg1, Toast.LENGTH_SHORT).show();
          break;
        default:
          break;
      }
    }
  };
  private HandlerThread mHandlerThead;
  protected Handler mThreadHandler;
  protected FileAdapter tempFile;
  protected ConcurrentHashMap<String, String> mInforCache = new ConcurrentHashMap<String, String>();

  protected class LoadInfo implements LoadWork<String> {
    private final FileAdapter mFile;

    private final TextView mView;

    public LoadInfo(FileAdapter file, TextView view) {
      mFile = file;
      mView = view;
    }

    @Override
    public String load() {
      if (null == mFile) {
        return "";
      }

      String path = mFile.getAbsolutePath();
      if (MultiFilesManager.isSourceDLNA(getApplicationContext())) {
        path = mFile.getAbsolutePath() + mFile.getSuffix();
      }
      String info = mFile.getInfo();
      MtkLog.d(TAG,
          "LoadInfo load mFile =" + mFile.getAbsolutePath() + "----name = " + mFile.getName()
              + "---info:" + info);
      if (null != info) {
        mInforCache.put(path, info);
      }
      return info;
    }

    @Override
    public void loaded(final String result) {
      mView.post(new Runnable() {

        @Override
        public void run() {
          mView.setText(result);
        }
      });
    }

  }

  private final ListView.OnItemClickListener mListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {
      TextView tvTextView = (TextView) view
          .findViewById(R.id.mmp_menulist_tv);
      String content = tvTextView.getText().toString();
      controlState(content);
    }
  };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void init() {
    mResources = getResources();
    mSelectedFiles = new ArrayList<String>();
    mCopyedFiles = new ArrayList<String>();

    // arcelik customized. always use LARGE thumbnail.

    /*
     * Intent intent = getIntent();
     * 
     * // add for ThumbnailSize bug
     * 
     * int value = intent.getIntExtra("mThumbnailSize", 1);
     * try {
     * String m = TVStorage.getInstance(this).get(
     * MultiMediaConstant.THUMBNAIL_SIZE);
     * if (null != m && m.length() > 0) {
     * value = Integer.parseInt(m);
     * }
     * } catch (Exception e) {
     * MtkLog.i(TAG, "init exception");
     * e.printStackTrace();
     * }
     * 
     * if (value == MultiMediaConstant.MEDIUM) {
     * mThumbnailSize = MultiMediaConstant.MEDIUM;
     * }
     * else if (value == MultiMediaConstant.LARGE) {
     * mThumbnailSize = MultiMediaConstant.LARGE;
     * }
     * else {
     * mThumbnailSize = MultiMediaConstant.SMALL;
     * }
     */
    mThumbnailSize = MultiMediaConstant.LARGE;
    findViews();
    prepareResources();

    MmpApp des = (MmpApp) this.getApplication();
    des.add(this);
  }

  List<Integer> mPosition = new ArrayList<Integer>();

  protected boolean isCurrentScreenHaveVideoFile() {
    MtkLog.d(TAG, "isCurrentScreenHaveVideoFile:mAdapter.getCount()==" + mAdapter.getCount());
    for (int position = 0; position < mAdapter.getCount(); position++) {
      FileAdapter adapter = mAdapter.getItem(position);
      if (null != adapter && adapter.isVideoFile()) {
        MtkLog.d(TAG, "adapter.getAbsolutePath():" + adapter.getAbsolutePath());
        return true;
      }
    }

    return false;
  }

  /**
   * Set current selection focus
   *
   * @return the selected item position
   */
  protected int setCurrentSelection() {
    MtkLog.d(TAG, "setCurrentSelection mAdapter:"
        + (mAdapter == null ? mAdapter : mAdapter.getCount()));
    if (null == mAdapter || mAdapter.getCount() == 0) {
      return 0;
    }

    mPosition.clear();
    int sourceType = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();
    MtkLog.i(TAG, "sourceType:" + sourceType);
    if (sourceType == MultiFilesManager.SOURCE_ALL) {
      int pos = getListView().getSelectedItemPosition();
      MtkLog.i(TAG, "sourceType pos:" + pos);

      if (pos < 0) {
        pos = 0;
      }
      return pos;
    }
    // if Connection mode is SMB or DLNA path choosing the name else path is
    // the absolute path
    if (sourceType == MultiFilesManager.SOURCE_DLNA) {
      String path = LogicManager.getInstance(this)
          .getCurrentPath(getMediaFilter());
      if (null == path) {
        path = mFilesManager.getCurrentPath();
      }
      int oldposition = getListSelectedItemPosition();
      MtkLog.i(TAG, "oldposition:" + oldposition);
      if (this instanceof MtkFilesListActivity) {
        oldposition = oldposition + 1;
      }
      int position = 0;
      for (; position < mAdapter.getCount(); position++) {
        FileAdapter adapter = mAdapter.getItem(position);
        if (null != adapter) {
          // MtkLog.i(TAG, "path:" + path + "getName:" + adapter.getName() +
          // "getAbsolute():"
          // + adapter.getAbsolutePath());
          if (null != path && path.equals(adapter.getName() + adapter.getSuffix())) {
            Log.i(TAG, "position:" + position);
            // setListSelection(position);
            // return position;
            mPosition.add(position);
          } else if (!mPosition.isEmpty()) {
            break;
          }
        }
      }

      if (!mPosition.isEmpty()) {
        if (mPosition.contains(oldposition)) {
          Log.i(TAG, "oldposition:" + oldposition);
          position = oldposition;
        } else {
          position = mPosition.get(0);
        }
      }
      setListSelection(position);
      return position;
    } else {
      for (int position = 0; position < mAdapter.getCount(); position++) {
        FileAdapter adapter = mAdapter.getItem(position);
        if (null != adapter) {
          String path = LogicManager.getInstance(this)
              .getCurrentPath(getMediaFilter());
          MtkLog.d(TAG, "setCurrentSelection path:" + path
              + "  adapter.getAbsolutePath():" + adapter.getAbsolutePath());
          if (null == path) {
            path = mCurrentSelectedPath;
            MtkLog.d(TAG, "setCurrentSelection getCurrentPath:" + path
                + "  mCurrentSelectedPath:" + mCurrentSelectedPath);
            if (path == null) {
              path = mFilesManager.getCurrentPath();
            }
          }
          if (null != path && path.equals(adapter.getAbsolutePath())) {
            setListSelection(position);
            return position;
          }
        }
      }
      return 0;
    }
  }

  protected int getMediaFilter() {
    switch (getListContentType()) {
      case MultiMediaConstant.AUDIO:
        return Const.FILTER_AUDIO;
      case MultiMediaConstant.THRD_PHOTO:
      case MultiMediaConstant.PHOTO:
        return Const.FILTER_IMAGE;
      case MultiMediaConstant.VIDEO:
        return Const.FILTER_VIDEO;
      case MultiMediaConstant.TEXT:
        return Const.FILTER_TEXT;
      default:
        return -1;
    }
  }

  protected void initMusicView() {
    MtkLog.d(TAG, "initMusicView");

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!((MmpApp) getApplication()).isEnterMMP()) {
      MtkLog.d(TAG, "onResume() Util.mIsEnterPip:" + Util.mIsEnterPip);

      if (!Util.mIsEnterPip && isCurrentScreenHaveVideoFile()) {
        Util.exitPIP(this);
      }

      ((MmpApp) getApplication()).setEnterMMP(true);

      // remove set MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE with 1, and update
      // others.
      // Util.enterMmp(1, getApplicationContext());
      updateTvConfig(1);
      if (mViewMode == VIEW_MODE_LIST && !Util.mIsUseEXOPlayer) {
        // LogicManager.getInstance(getApplicationContext()).freeVideoResource();
        AudioBTManager.getInstance(getApplicationContext()).creatAudioPatch();
      }

      MultiFilesManager.getInstance(this).initDevicesManager();
    }

    if (DevManager.getInstance().getMount()) {
      refreshListView();
      DevManager.getInstance().setMount(false);
    }
  }

  private void updateTvConfig(int status) {
    int cur = LogicManager.getInstance(getApplicationContext()).getCurPictureMode();
    Log.i(TAG, "cur:--" + cur);
    LogicManager.getInstance(getApplicationContext()).setPictureMode(cur);

    if (1 == status) {
      MtkLog.i(TAG, "enterMmp 1 == status ");
      MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
    }
    MtkLog.i(TAG, "enterMmp after status: " + status);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPause() {
    super.onPause();
    if (null != vMusicView) {
      vMusicView.removeMessage();
    }
    Util.logLife(TAG, "onPause");
  }

  public static void reSetModel() {
    mMode = MODE_NORMAL;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    // mFilesManager.deleteObserver(this);
    // try {
    // if (mINetworkManagementService != null && mBaseNetworkObserver != null) {
    // mINetworkManagementService.unregisterObserver(mBaseNetworkObserver);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // this.unregisterReceiver(netRecevier);
    cancelRecursive();
    try {
      dismissDialog(DIALOG_MENU);
      dismissDialog(DIALOG_EMPTY_TIP);
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      MtkLog.d(TAG, "dismissDialog DIALOG_MENU&&DIALOG_EMPTY_TIP exception~~");
    }

    if (null != mPopView && mPopView.isShowing()) {
      mPopView.dismiss();
    }
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
      mHandler = null;
    }
    tempFile = null;
    if (mThreadHandler != null) {
      mThreadHandler.removeCallbacksAndMessages(null);
      mThreadHandler.getLooper().quit();
      mThreadHandler = null;
      mHandlerThead = null;
    }
    ((MmpApp) getApplication()).removeRootMenuListener(mRootMenuListener);
    ((MmpApp) getApplication()).remove(this);

    // exit mmp when enter other app
    // if (!Util.isMMpActivity(this)){
    // Util.exitMmpActivity(getApplicationContext());
    // }
    LogicManager.getInstance(this.getApplicationContext()).registerUnMountLisenter(null);
    super.onDestroy();
    Util.logLife(TAG, "onDestory");
  }

  /**
   * Show music play view
   */
  protected void showMusicView() {
    if (vMusicView != null) {
      vMusicView.initControl();
    } else {
      initMusicView();
    }

    if (null != mPopView && null != vMusicView) {
      vMusicView.init(this);
      return;
    }
  }

  protected void stopMusicView() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (null != mPopView) {
          if (mPopView.isShowing()) {
            mPopView.dismiss();
          }
        }
      }
    });

  }

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
   * {@inheritDoc}
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    Log.i(TAG, "Key :" + keyCode);
    switch (keyCode) {
      // case KeyMap.KEYCODE_VOLUME_UP:
      // case KeyMap.KEYCODE_VOLUME_DOWN:
      case KeyMap.KEYCODE_MTKIR_CHDN:
      case KeyMap.KEYCODE_MTKIR_CHUP:
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_CENTER:
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          onKeyDown(keyCode, event);
        }
        return true;
      default:
        break;
    }

    return super.dispatchKeyEvent(event);
  }

  /**
   * show not support tips dialog
   *
   * @param title
   *              the tips dialog content
   */
  protected void onNotSuppsort(String title) {

    MtkLog.i(TAG, "onNotSuppsort  :" + title);
    if (null == mTipsDialog) {
      mTipsDialog = new TipsDialog(this);
      mTipsDialog.setText(title);
      mTipsDialog.show();
      mTipsDialog.setBackground(R.drawable.toolbar_playerbar_test_bg);
      Drawable drawable = this.getResources().getDrawable(
          R.drawable.toolbar_playerbar_test_bg);

      int weight = (int) (drawable.getIntrinsicWidth() * 0.6);
      int height = drawable.getIntrinsicHeight();
      /*
       * mTipsDialog.setDialogParams(weight, height);
       */

      int x = -((ScreenConstant.SCREEN_WIDTH / 2) - weight / 2)
          + (ScreenConstant.SCREEN_WIDTH / 10);
      int y = (ScreenConstant.SCREEN_HEIGHT * 3 / 8) - 114 - height / 2;
      mTipsDialog.setWindowPosition(x, y);

    } else {
      mTipsDialog.setText(title);
      mTipsDialog.show();
    }

  }

  /**
   * Dismiss not support tips dialog
   */
  protected void dismissNotSupprot() {

    if (null != mTipsDialog && mTipsDialog.isShowing()) {
      mTipsDialog.dismiss();
    }
  }

  private boolean isAudioSeekable() {
    if (!LogicManager.getInstance(this).canSeek()) {
      MtkLog.d(TAG, "isAudioSeekable false: canseek is false");
      return false;
    }

    int totalDuration = LogicManager.getInstance(this).getTotalPlaybackTime();
    MtkLog.d(TAG, "isAudioSeekable totalDuration==" + totalDuration);
    if (totalDuration <= 0) {
      MtkLog.d(TAG, "isAudioSeekable false: audio duration issue");
      return false;
    }

    return true;
  }

  private boolean seek(int keyCode) {
    if (null == vMusicView) {
      return true;
    }

    if (!isAudioSeekable()) {
      MtkLog.i(TAG, "!mLogicManager.canSeek()");
      if (mHandler != null) {
        if (mHandler.hasMessages(MSG_NOSUPPORT_DISMISS)) {
          mHandler.removeMessages(MSG_NOSUPPORT_DISMISS);
        }
        mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
        mHandler.sendEmptyMessageDelayed(MSG_NOSUPPORT_DISMISS, 1000);
      }
      return true;
    }

    if (LogicManager.getInstance(this).isAudioPlaying()) {
      LogicManager.getInstance(this).pauseAudio();
    }
    vMusicView.removeProgressUpdateMsg();
    if (!vMusicView.isSeeking()) {
      vMusicView.setSeeking(true);
      int progressTemp = LogicManager.getInstance(this).getPlaybackProgress();
      vMusicView.setSeekingprogress(progressTemp);// progressTemp & 0xffffffffL;
    }
    int tempSeeking = 0;
    if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND) {
      tempSeeking = vMusicView.getSeekingprogress() - MusicPlayInfoView.SEEK_DURATION;
      if (tempSeeking < 0) {
        tempSeeking = 0;
      }
    } else {
      tempSeeking = vMusicView.getSeekingprogress() + MusicPlayInfoView.SEEK_DURATION;
      int totalProgressTemp = LogicManager.getInstance(this).getTotalPlaybackTime();
      // long totalProgress = totalProgressTemp & 0xffffffffL;
      if (tempSeeking > totalProgressTemp) {
        tempSeeking = totalProgressTemp;
      }
    }
    vMusicView.setSeekingprogress(tempSeeking);
    MtkLog.i(TAG, "seek progress calc:" + tempSeeking);
    if (LogicManager.getInstance(this).getAudioStatus() != AudioConst.PLAB_STATUS_SEEKING) {
      vMusicView.setSeekingBar();
    }
    return true;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "onkeyup keyCode:" + keyCode);
    if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND
        || keyCode == KeyMap.KEYCODE_MTKIR_FASTFORWARD) {
      if (LogicManager.getInstance(this).isAudioFast()) {
        return true;
      }
      // // add by keke 2.1 for DTV00393701
      // if (mLogicManager.getPlayStatus() == AudioConst.PLAY_STATUS_PAUSED) {
      // removeScorePause();
      // }
      if (vMusicView != null && vMusicView.isSeeking()) {
        try {
          MtkLog.i(TAG, "seek progress:" + vMusicView.getSeekingprogress());
          LogicManager.getInstance(this).seekToCertainTime(vMusicView.getSeekingprogress());
          // mControlView.setCurrentTime(progress);
          // mControlView.setProgress((int) progress);
        } catch (Exception e) {
          MtkLog.i(TAG, "Seek exception");
          vMusicView.setSeeking(false);
          if (mHandler != null) {
            if (mHandler.hasMessages(MSG_NOSUPPORT_DISMISS)) {
              mHandler.removeMessages(MSG_NOSUPPORT_DISMISS);
            }
            mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
            mHandler.sendEmptyMessageDelayed(MSG_NOSUPPORT_DISMISS, 1000);
          }
          return true;
        }
      }
    }

    return super.onKeyUp(keyCode, event);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    keyCode = KeyMap.getKeyCode(keyCode, event);
    MtkLog.i(TAG, "onKeyDown later keycode:" + keyCode);

    switch (keyCode) {
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_CENTER:
        if (getListView().isFocused()) {
          int pos = getListView().getSelectedItemPosition();
          onListItemClick(getListView(), null, pos, pos);
        }
        return true;
      case KeyMap.KEYCODE_MENU:
        if (reuTip != null && reuTip.isShowing()) {
          return true;
        }

        if (loadTip != null && loadTip.isShowing()) {
          return true;
        }

        try {
          dismissDialog(DIALOG_EMPTY_TIP);
        } catch (Exception e) {
          e.printStackTrace();
        }
        showDialog(DIALOG_MENU);
        break;
      case KeyMap.KEYCODE_MTKIR_ANGLE:

        break;

      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        if (mPopView != null && mPopView.isShowing()) {
          vMusicView.onRepeatClick();
        }
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
        if (mPopView != null && mPopView.isShowing()) {
          vMusicView.onPauseOrPlay();
        }
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PLAY: {
        if (mPopView != null && mPopView.isShowing()) {
          vMusicView.onPlay();
        }
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PAUSE: {
        if (mPopView != null && mPopView.isShowing()) {
          vMusicView.onPause();
        }
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_STOP: {
        if (mPopView == null || !mPopView.isShowing()) {
          return true;
        }
        vMusicView.onStop();
        if (mPopView.isShowing()) {
          mPopView.dismiss();
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_MUTE: {
        if (mPopView != null && mPopView.isShowing()) {
          vMusicView.setMute();
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_NEXT: {
        if (mPopView != null && mPopView.isShowing()) {
          LogicManager.getInstance(this).playNextAudio();
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
        if (mPopView != null && mPopView.isShowing()) {
          LogicManager.getInstance(this).playPrevAudio();
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_REWIND:
      case KeyMap.KEYCODE_MTKIR_FASTFORWARD: {
        if (null == mPopView || !mPopView.isShowing()
            || LogicManager.getInstance(this).isAudioStoped()) {
          return true;
        }
        if ((vMusicView != null && vMusicView.isNotSupport())
            || LogicManager.getInstance(this).isAudioFast()) {// || mIsSeeking
          return true;
        }
        // try {
        // if (MultiFilesManager.isSourceDLNA(this)
        // && !LogicManager.getInstance(this).canSeek()) {
        // MtkLog.i(TAG, "can not seek");
        // if (mHandler != null) {
        // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
        // }
        // return true;
        // }
        // LogicManager.getInstance(this).fastForwardAudio();
        // vMusicView.onFast();
        // } catch (IllegalStateException e) {
        // if (mHandler != null) {
        // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
        // }
        // } catch (Exception e) {
        // if (LogicManager.getInstance(this).getPlayStatus() ==
        // AudioConst.PLAY_STATUS_FF) {
        // try {
        // LogicManager.getInstance(this).fastForwardAudioNormal();
        // vMusicView.onFast();
        // } catch (Exception ex) {
        // if (mHandler != null) {
        // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
        // }
        // }
        // } else {
        // if (mHandler != null) {
        // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
        // }
        // }
        // }

        return seek(keyCode);
      }
      // case KeyMap.KEYCODE_MTKIR_REWIND: {
      // if (null == mPopView || !mPopView.isShowing()
      // || LogicManager.getInstance(this).isAudioStoped()) {
      // return true;
      // }
      // try {
      // if (MultiFilesManager.isSourceDLNA(this)
      // && !LogicManager.getInstance(this).canSeek()) {
      // if (mHandler != null) {
      // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
      // }
      // return true;
      // }
      // LogicManager.getInstance(this).fastRewindAudio();
      // vMusicView.onFast();
      // } catch (IllegalStateException e) {
      // if (mHandler != null) {
      // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
      // }
      // } catch (RuntimeException e) {
      // if (mHandler != null) {
      // mHandler.sendEmptyMessage(MSG_NOSUPPORT_SHOW);
      // }
      // }
      // return true;
      // }
      case KeyMap.KEYCODE_MTKIR_CHDN:
      case KeyMap.KEYCODE_MTKIR_CHUP:
        return true;
      case KeyMap.KEYCODE_MTKIR_GUIDE: {
        // if (isValid()) {
        // mAdapter.cancel();
        // Thumbnail thumbnail = Thumbnail.getInstance();
        // if (thumbnail.isLoadThumanil()) {
        // thumbnail.stopThumbnail();
        // }
        // Util.startEPGActivity(this);
        // }
        break;
      }
      // Added by Dan for fix bug DTV00379203
      case KeyMap.KEYCODE_MTKIR_EJECT:
        select();
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  // Added by Dan for fix bug DTV00379203
  private void select() {
    FileAdapter file = getListSelectedItem();
    if (null != file) {
      if (file.isDirectory()) {
        return;
      }
      String selectedFile = file.getAbsolutePath();

      // remove for DTV00889683
      /*
       * if (mSelectedFiles.size() > 0) {
       * String oneSelectedFile = mSelectedFiles.get(0);
       * if (!mFilesManager.isInSameFolder(oneSelectedFile, selectedFile)) {
       * mSelectedFiles.clear();
       * }
       * }
       */

      if (!mSelectedFiles.contains(selectedFile)) {
        if (mCopyedFiles.contains(selectedFile)) {
          mCopyedFiles.remove(selectedFile);
        } else {
          mSelectedFiles.add(selectedFile);
        }
      } else {
        mSelectedFiles.remove(selectedFile);
      }

      cancelLoadFiles();
      mAdapter.notifyDataSetChanged();

    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Dialog onCreateDialog(int id) {
    Dialog dialog = null;

    switch (id) {
      case DIALOG_EMPTY_TIP:
        TipsDialog emptyTip = new TipsDialog(this);
        emptyTip.setWindowPosition();
        emptyTip.setOnKeyListener(new OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode,
              KeyEvent event) {
            switch (keyCode) {
              case KeyMap.KEYCODE_MENU:
                dismissDialog(DIALOG_EMPTY_TIP);
                showDialog(DIALOG_MENU);
                return true;
              case KeyMap.KEYCODE_BACK:
                dismissDialog(DIALOG_EMPTY_TIP);
                onBackPressed();
                return true;
              default:
                break;
            }
            return false;
          }
        });
        dialog = emptyTip;
        break;
      case DIALOG_LOGIN_FAILED_TIP:
        TipsDialog failedTip = new TipsDialog(this);
        failedTip.setText(mResources
            .getString(R.string.mmp_login_failed_tip));
        failedTip.setWindowPosition();
        MtkLog.d(TAG, "dismiss tip dialog."
            + mResources.getString(R.string.mmp_login_failed_tip));
        dialog = failedTip;
        if (mHandler != null) {
          mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
              MtkLog.d(TAG, "dismiss tip dialog.");
              dismissDialog(DIALOG_LOGIN_FAILED_TIP);
              Bundle data = new Bundle();
              data.putBoolean("Init", true);
              showDialog(DIALOG_LOGIN, data);
              // showDialog(DIALOG_LOGIN);
            }
          }, 2000);
        }
        break;
      case DIALOG_MENU:
        final MenuListView menu = new MenuListView(this, GetDataImp
            .getInstance().getComMenu(this, R.array.mmp_menu_filelist,
                R.array.mmp_menu_filelist_enable,
                R.array.mmp_menu_filelist_hasnext),
            mListener, null);

        menu.setOnDismissListener(new OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            if (menu.isContain(mResources
                .getString(R.string.mmp_menu_mount_iso))) {
              menu.removeItem(menu.getCount() - 1);
            }

            if (menu.isContain(mResources
                .getString(R.string.mmp_menu_unmount_iso))) {
              menu.removeItem(menu.getCount() - 1);
            }

            if (getListItemsCount() == 0) {
              onListEmpty();
            }
          }
        });
        customMenu(menu);

        dialog = menu;
        break;
      case DIALOG_REU_TIP:
        reuTip = new TipsDialog(this);
        reuTip.setText(mResources.getString(R.string.mmp_reutip));
        reuTip.setFocusNeeded();
        reuTip.setWindowPosition();

        dialog = reuTip;
        reuTip.setOnKeyListener(new OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode,
              KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
              if (keyCode == KeyMap.KEYCODE_BACK) {
                isCancel = true;
                mMode = MODE_RECURSIVE;
                if (null != mRecursThread) {
                  try {
                    MtkLog.i(TAG, "thread isAlive:"
                        + mRecursThread.isAlive());
                    if (mRecursThread.isAlive()) {
                      mRecursThread.interrupt();
                    }
                  } catch (Exception e) {
                    MtkLog.d(TAG, e.getMessage());
                  }
                }
                finish();
              }
            }
            return false;
          }
        });
        break;
      case DIALOG_LOGIN:
        final LoginDialog login = new LoginDialog(this);
        login.setOnKeyClickListener(new OnKeyClickListener() {
          @Override
          public void onInputClicked() {
            String prefill = login.getPrefill();
            boolean isPassword = login.isPassword();
            Bundle data = new Bundle();
            data.putString("Prefill", prefill);
            data.putBoolean("IsPassword", isPassword);
            dismissDialog(DIALOG_LOGIN);
            showDialog(DIALOG_KEYBOARD, data);
          }

          @Override
          public void onConfirmClicked(final String userName, final String password) {
            dismissDialog(DIALOG_LOGIN);
            MtkLog.d(TAG, "UserName : " + userName);
            MtkLog.d(TAG, "Password : " + password);

            new Thread(new Runnable() {

              @Override
              public void run() {
                boolean success = ((MultiFilesManager) mFilesManager)
                    .login(getListCurrentPath(), userName, password);
                Message msg = Message.obtain();
                msg.what = MSG_LOGIN_RESULT;
                msg.arg1 = success ? 1 : 0;
                if (mHandler != null) {
                  mHandler.sendMessage(msg);
                }
              }
            }).start();
          }
        });
        dialog = login;
        vLoginDialog = login;
        break;
      case DIALOG_KEYBOARD:
        KeyboardDialog keyboard = new KeyboardDialog(this);
        keyboard.setOnPressedListener(new OnPressedListener() {
          @Override
          public void onPositivePressed(String input) {
            Bundle data = new Bundle();
            data.putString("Input", input);
            dismissDialog(DIALOG_KEYBOARD);
            showDialog(DIALOG_LOGIN, data);
          }

          @Override
          public void onNegativePressed() {
            dismissDialog(DIALOG_KEYBOARD);
            showDialog(DIALOG_LOGIN);
          }
        });

        keyboard.setOnDismissListener(new OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            showDialog(DIALOG_LOGIN);
          }
        });

        dialog = keyboard;
        break;
      case DIALOG_PROGRESS:
        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(R.string.mmp_title_copy);
        progress.setCancelable(false);
        dialog = progress;
        vProgressDialog = progress;
        break;
      case DIALOG_CONFIRM_DELETE:
        ConfirmDeleteDialog confirmDelete = new ConfirmDeleteDialog(this);
        confirmDelete.setTitle(R.string.mmp_title_confirm_delete);
        confirmDelete.setMessage(R.string.mmp_message_confirm_delete);
        dialog = confirmDelete;
        vConfirmDeleteDialog = confirmDelete;
        break;
      case DIALOG_LOADING:
        loadTip = new TipsDialog(this);
        loadTip.setText(mResources.getString(R.string.mmp_loading_tip));
        loadTip.setWindowPosition();
        loadTip.setOnKeyListener(new OnKeyListener() {
          @Override
          public boolean onKey(DialogInterface dialog, int keyCode,
              KeyEvent event) {
            if (keyCode == KeyMap.KEYCODE_BACK) {
              dismissDialog(DIALOG_LOADING);
              onBackPressed();
            }

            return true;
          }
        });

        dialog = loadTip;
        break;
      case DIALOG_CONFIRM_REPLACE:
        ConfirmReplaceDialog replaceDialog = new ConfirmReplaceDialog(this);
        replaceDialog.setCancelable(false);
        replaceDialog.setTitle(R.string.mmp_title_confirm_replace);
        replaceDialog
            .setOnConfirmReplaceListener(new OnConfirmReplaceListener() {
              @Override
              public void onYesToAllPressed() {
                MtkLog.d(TAG, "Replace All!!");
                dismissDialog(DIALOG_CONFIRM_REPLACE);
                showDialog(DIALOG_PROGRESS);
                mYesToAll = true;
                mPasteCanceled = false;
                synchronized (mCopyedFiles) {
                  mCopyedFiles.notifyAll();
                }
              }

              @Override
              public void onYesPressed() {
                dismissDialog(DIALOG_CONFIRM_REPLACE);
                showDialog(DIALOG_PROGRESS);
                MtkLog.d(TAG, "Replace One!!");
                mYes = true;
                mPasteCanceled = false;
                synchronized (mCopyedFiles) {
                  mCopyedFiles.notifyAll();
                }
              }

              @Override
              public void onNoPressed() {
                dismissDialog(DIALOG_CONFIRM_REPLACE);
                // showDialog(DIALOG_PROGRESS);
                MtkLog.d(TAG, "Replace No!!");
                synchronized (mCopyedFiles) {
                  mPasteReplaceNo = true;
                  mCopyedFiles.notifyAll();
                }
              }

              @Override
              public void onCancelPressed() {
                dismissDialog(DIALOG_CONFIRM_REPLACE);
                MtkLog.d(TAG, "Replace Cancel!!");
                UsbFileOperater.getInstance().setCopyAbort(true);
                mPasteCanceled = true;
                synchronized (mCopyedFiles) {
                  mCopyedFiles.notifyAll();
                }
              }
            });

        dialog = replaceDialog;
        break;
      default:
        break;
    }

    return dialog;
  }

  protected void customMenu(MenuListView menu) {
    MtkLog.d(TAG, "customMenu");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {

    switch (id) {
      case DIALOG_MENU:
        MtkLog.i("dialog", "onPrepareDialog");
        MenuListView menu = (MenuListView) dialog;

        menu.setMediaType(getListContentType());
        FileAdapter file = getListSelectedItem();
        // for 3d browse start
        if (MultiFilesManager.ROOT_PATH.equals(getListCurrentPath())) {
          menu.setItemEnabled(0, false);
        } else {
          menu.setItemEnabled(0, true);
        }
        int sType = 0;
        if (mFilesManager instanceof MultiFilesManager) {
          sType = ((MultiFilesManager) mFilesManager).getCurrentSourceType();
        }
        if (MultiFilesManager.SOURCE_DLNA == sType) {
          if (menu.isContain(mResources.getString(R.string.mmp_menu_3d_photo))) {
            menu.setItemEnabled(menu.getCount() - 1, false);
          }
        } else {
          if (menu.isContain(mResources.getString(R.string.mmp_menu_3d_photo))) {
            menu.setItemEnabled(menu.getCount() - 1, true);
          }

        }

        if (null != file
            && DevManager.getInstance().isVirtualDev(
                file.getAbsolutePath())) {
          // TODO unmount
          String menuContent = mResources
              .getString(R.string.mmp_menu_unmount_iso);
          if (!menu.isContain(menuContent)) {
            MenuFatherObject obj = new MenuFatherObject();
            obj.content = menuContent;
            obj.enable = true;
            obj.hasnext = false;
            obj.enter = false;
            menu.addItem(menu.getCount(), obj);
          }

        } else if (file == null || file.isDirectory()) {
          //menu.showCopy(false);
          //menu.showDelete(false);
        } else if (file.isIsoFile()) {
          // TODO ISO mount menu
          String menuContent = mResources
              .getString(R.string.mmp_menu_mount_iso);
          if (!menu.isContain(menuContent)) {
            MenuFatherObject obj = new MenuFatherObject();
            obj.content = menuContent;
            obj.enable = true;
            // Add by Dan for fix bug DTV00374299
            if (((MultiFilesManager) mFilesManager)
                .getMountedIsoCount() >= 8) {
              obj.enable = false;
            }
            obj.hasnext = false;
            obj.enter = false;
            menu.addItem(menu.getCount(), obj);
          }
        } else {
          //menu.showCopy(false); // arcelik customized
          //menu.showDelete(false); // arcelik customized
        }

        boolean isNTFS = false;
        if (null != file) {
          String curDevName = MultiFilesManager.getInstance(this).getCurDevName();
          MtkLog.d(TAG, "curDevName==" + curDevName);
          MountPoint mountPoint = MultiFilesManager.getInstance(this).getCurrentDevice(curDevName);
          if (null != mountPoint) {
            MtkLog.d(TAG, "mFsType==" + mountPoint.mFsType);
            if (mountPoint.mFsType == MountPoint.FS_TYPE.FS_TYPE_NTFS) {
              isNTFS = true;
            }
          }
        }
        MtkLog.d(TAG, "isNTFS == " + isNTFS);
        if (isNTFS) {
          //menu.showCopy(false);
          //menu.showDelete(false);
        }

        String path = null;
        if (file != null) {
          path = file.getAbsolutePath();
        }
        MtkLog.d(TAG, "onPrepareDialog show menu select path:" + path);
        if (path != null && path.toLowerCase(Locale.ENGLISH).endsWith(".pvr")) {
          //menu.showCopy(false);
          //menu.showDelete(false);
        }
        if (!mCopyedFiles.isEmpty()) {
          String oneCopyedFile = mCopyedFiles.get(0);
          if (mFilesManager.canPaste(oneCopyedFile)) {
            //menu.showPaste(false); // arcelik customized
          } else {
            //menu.showPaste(false);
          }
        } else {
          //menu.showPaste(false);
        }

        int sourceType = MultiFilesManager.getInstance(this)
            .getCurrentSourceType();

        int index = menu.getItemIndex(mResources.getString(R.string.mmp_menu_recur));
        if (index == -1) {
          index = menu.getItemIndex(mResources.getString(R.string.mmp_menu_not_recur));
        }

        MenuFatherObject obj = menu.getItem(index);
        if (sourceType == MultiFilesManager.SOURCE_LOCAL) {
          obj.enable = true;
        } else {
          obj.enable = false;
        }

        menu.updateItem(index, obj);

        if (mMode == MODE_NORMAL) {
          menu.updateItem(index,
              getResources().getString(R.string.mmp_menu_recur));
        } else if (mMode == MODE_RECURSIVE) {
          menu.updateItem(index,
              getResources().getString(R.string.mmp_menu_not_recur));
          // Added by Dan for fix bug DTV00376269
          //menu.showCopy(false);
          //menu.showPaste(false);
        }
        if (sourceType == MultiFilesManager.SOURCE_DLNA
            || sourceType == MultiFilesManager.SOURCE_SMB) {
          //menu.showCopy(false);
          //menu.showPaste(false);
          //menu.showDelete(false);
        }
        if (true == Feature.isSupportCnSamba()) {
          if (((MultiFilesManager) mFilesManager).getCurrentSourceType() == MultiFilesManager.SOURCE_SMB) {
            menu.showSort(false);
          }
        }
        break;
      case DIALOG_LOGIN:
        if (args != null) {
          String input = args.getString("Input");
          if (input != null) {
            ((LoginDialog) dialog).setInput(input);
          }

          boolean init = args.getBoolean("Init");
          if (init) {
            ((LoginDialog) dialog).init();
          }
        }
        break;
      case DIALOG_KEYBOARD:
        if (args != null) {
          String prefill = args.getString("Prefill");
          boolean isPassword = args.getBoolean("IsPassword");
          ((KeyboardDialog) dialog).setPrefill(prefill);
          ((KeyboardDialog) dialog).isPassword(isPassword);
        } else {
          ((KeyboardDialog) dialog).setPrefill("");
          ((KeyboardDialog) dialog).isPassword(false);
        }
        break;
      case DIALOG_PROGRESS:
        if (args != null) {
          final ProgressDialog progress = (ProgressDialog) dialog;
          int max = args.getInt("Max");
          if (max > 0) {
            progress.setMax(max);
          }
          progress.setProgress(0);

          String message = args.getString("Message");
          if (message != null) {
            progress.setMessage(message);
          }
        }

        break;
      case DIALOG_CONFIRM_REPLACE:
        if (args != null) {
          ConfirmReplaceDialog confirmReplace = (ConfirmReplaceDialog) dialog;
          String message = args.getString("Message");
          if (message != null) {
            confirmReplace.setMessage(message);
          }
        }
        break;
      case DIALOG_EMPTY_TIP: {
        int deviceNum = MultiFilesManager.getInstance(this).getAllDevicesNum();
        MtkLog.d(TAG, "onPrepareDialog,DIALOG_EMPTY_TIP,deviceNum =" + deviceNum);
        if (deviceNum == 0) {
          ((TipsDialog) dialog).setText(getString(R.string.mmp_nodevice));
          removeDialog(DIALOG_MENU);
        } else {
          ((TipsDialog) dialog)
              .setText(getString(R.string.mmp_emptyfile));

        }
        mFlag = false;
        break;
      }
      default:
        break;
    }
  }

  private void cancelRecursive() {
    isCancel = true;
    try {
      dismissDialog(DIALOG_REU_TIP);
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      MtkLog.d(TAG, "dismissDialog DIALOG_REU_TIP exception~~");
    }
    if (null != mRecursThread) {
      try {
        MtkLog.i(TAG, "thread isAlive:"
            + mRecursThread.isAlive());
        if (mRecursThread.isAlive()) {
          mRecursThread.interrupt();
        }
      } catch (Exception e) {
        MtkLog.d(TAG, e.getMessage());
      }
    }

  }

  private void controlState(String content) {
    if (mResources.getString(R.string.mmp_menu_name).equals(content)) {
      setListSortType(FilesManager.SORT_BY_NAME);
      openDir(getListCurrentPath());
    } else if (mResources.getString(R.string.mmp_menu_date).equals(content)) {
      setListSortType(FilesManager.SORT_BY_DATE);
      openDir(getListCurrentPath());
    } else if (mResources.getString(R.string.mmp_menu_album)
        .equals(content)) {
      setListSortType(FilesManager.SORT_BY_ALBUM);
      openDir(getListCurrentPath());
    } else if (mResources.getString(R.string.mmp_menu_artist).equals(
        content)) {
      setListSortType(FilesManager.SORT_BY_ARTIST);
      openDir(getListCurrentPath());
    } else if (mResources.getString(R.string.mmp_menu_genre)
        .equals(content)) {
      setListSortType(FilesManager.SORT_BY_GENRE);
      openDir(getListCurrentPath());
    } else if (mResources.getString(R.string.mmp_menu_type).equals(content)) {
      setListSortType(FilesManager.SORT_BY_TYPE);
      openDir(getListCurrentPath());
    }

    else if (mResources.getString(R.string.mmp_menu_small).equals(content)) {
      setThumbnailSize(MultiMediaConstant.SMALL);
    } else if (mResources.getString(R.string.mmp_menu_medium).equals(
        content)) {
      MtkLog.d(TAG, "Medium!!");
      setThumbnailSize(MultiMediaConstant.MEDIUM);
    } else if (mResources.getString(R.string.mmp_menu_large)
        .equals(content)) {
      MtkLog.d(TAG, "Large!!");
      setThumbnailSize(MultiMediaConstant.LARGE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_phototext))) {
      setListContentType(FilesManager.CONTENT_PHOTO);
      openDir(getListCurrentPath());
    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_music))) {
      setListContentType(FilesManager.CONTENT_AUDIO);
      openDir(getListCurrentPath());
    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_video))) {
      setListContentType(FilesManager.CONTENT_VIDEO);
      LogicManager.getInstance(this).stopAudio();
      stopMusicView();
      openDir(getListCurrentPath());
    } else if (content.equals(mResources.getString(R.string.mmp_menu_text))) {
      setListContentType(FilesManager.CONTENT_TEXT);
      openDir(getListCurrentPath());
    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_recur))) {
      mMode = MODE_RECURSIVE;
      recursive();

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_not_recur))) {
      cancelRecursive();
      mMode = MODE_NORMAL;
      openDir(getListCurrentPath());
    } else if (mResources.getString(R.string.mmp_menu_copy).equals(content)) {
      copy();
    } else if (mResources.getString(R.string.mmp_menu_paste)
        .equals(content)) {
      if (!mCopyedFiles.isEmpty()) {
        paste();
      }
    } else if (mResources.getString(R.string.mmp_menu_delete).equals(
        content)) {
      delete();
    } else if (mResources.getString(R.string.mmp_menu_onephotomode).equals(
        content)) {
      framePhoto(ConstPhoto.PHOTO_FRAME_ONE);

    } else if (content.equals(MultiFilesManager.getInstance(this).getCurDevName())) {
      framePhoto(ConstPhoto.PHOTO_FRAME_DEV);
    }
    // TODO mount
    else if (mResources.getString(R.string.mmp_menu_mount_iso).equals(
        content)) {
      if (getListSelectedItem() != null) {
        DevManager.getInstance().mountISOFile(
            getListSelectedItem().getAbsolutePath());
      }
      // Log.v("mout", getListSelectedItem().getAbsolutePath());
    } else if (mResources.getString(R.string.mmp_menu_unmount_iso).equals(
        content)) {
      if (getListSelectedItem() != null) {
        DevManager.getInstance().umoutISOFile(
            getListSelectedItem().getAbsolutePath());
      }
    } else if (mResources.getString(R.string.mmp_menu_3d_photo).equals(
        content)) {
      setListContentType(FilesManager.CONTENT_THRDPHOTO);
      openDir(getListCurrentPath());
    } else {
      return;
    }

    dismissDialog(DIALOG_MENU);
  }

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context content, Intent intent) {
      // TODO Auto-generated method stub
      /*
       * if (intent.getAction().equals(
       * AudioManager.VOLUME_CHANGED_ACTION)) {
       * Log.i(TAG, "register receiver VOLUME_CHANGED_ACTION");
       * if (mPopView != null && mPopView.isShowing()) {
       * if (vMusicView != null) {
       * vMusicView.setVolume();
       * } else {
       * Log.i(TAG, "CHANGE vMusicView == null");
       * }
       * }
       * } else if
       * (intent.getAction().equals(AudioManager.STREAM_MUTE_CHANGED_ACTION)) {
       * if (mPopView != null && mPopView.isShowing()) {
       * if (vMusicView != null) {
       * vMusicView.setMute();
       * } else {
       * Log.i(TAG, "MUTE vMusicView == null");
       * 
       * }
       * }
       * 
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
            if (mPopView != null && mPopView.isShowing() && vMusicView != null) {
              if (LogicManager.getInstance(getApplicationContext()).isAudioPlaying()) {
                vMusicView.onPauseOrPlay();
              }
            }
            break;
          case BluetoothAdapter.STATE_CONNECTED:
            Log.d(TAG, "BLUETOOTH_STATE_CONNECTED");
            if (mPopView != null && mPopView.isShowing() && vMusicView != null) {
              if (LogicManager.getInstance(getApplicationContext()).isAudioPause()) {
                vMusicView.onPauseOrPlay();
              }
            }
            break;
          default:
            Log.d(TAG, "unhandled bluetooth state");
            break;
        }
      } else if (intent.getAction().equals("mtk.intent.input.source")) {
        if (Util.mIsEnterPip) {
          LogicManager.getInstance(getApplicationContext()).finishVideo();
          if (VideoPlayActivity.getInstance() != null) {
            VideoPlayActivity.getInstance().finish();
          }
        }
      }
    }

  };

  @Override
  protected void onStart() {
    super.onStart();
    MtkLog.d(TAG, "onStart~~~~");
    IntentFilter filter = new IntentFilter();
    // filter.addAction(AudioManager.STREAM_MUTE_CHANGED_ACTION);
    // filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
    filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
    filter.addAction("mtk.intent.input.source");
    registerReceiver(mReceiver, filter);
    // registerReceiver(mRootReceiver,ifilter);
    MmpApp des = (MmpApp) this.getApplication();
    des.registerRootMenu(mRootMenuListener);
  }

  @Override
  protected void onStop() {
    super.onStop();
    MtkLog.d(TAG, "onStop");
    unregisterReceiver(mReceiver);
    // if (!Util.isMMpActivity(this)){
    // handleRootMenuEvent();
    // ((MmpApp) getApplication()).unregister();
    // this.finish();
    // }
  }

  protected void stopVideoListMode() {
    MtkLog.d(TAG, "stopVideoListMode");
  }

  private void recursive() {
    MtkLog.d(TAG, "recursive~~~");
    showDialog(DIALOG_REU_TIP);

    try {
      dismissDialog(DIALOG_EMPTY_TIP);
    } catch (IllegalArgumentException e) {
      // e.printStackTrace();
      MtkLog.d(TAG, "dismissDialog DIALOG_EMPTY_TIP exception~~");
    }
    mRecursThread = new Thread() {
      @Override
      public void run() {
        isCancel = false;
        mRecursiveFiles = ((MultiFilesManager) mFilesManager)
            .listRecursiveFiles(getListContentType());
        if (isCancel) {
          mRecursiveFiles.clear();
        } else {
          if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_RECURSIVE_PARSER);
          }
        }
      }
    };
    mRecursThread.start();
  }

  private void framePhoto(int model) {
    MtkLog.d(TAG, "framePhoto  model = " + model);
    Util.exitPIP(this);
    if (model == ConstPhoto.PHOTO_FRAME_ONE) {
      mPlayMode = ConstPhoto.PHOTO_FRAME_ONE;

      if (mPopView != null && mPopView.isShowing()) {
        vMusicView.removeMessage();
        mPopView.dismiss();
      }
      Intent intent = new Intent();
      Bundle bundle = new Bundle();
      if (Util.isSupport4K8K()) {
        intent.setClass(this, Photo4K2KPlayActivity.class);
      } else {
        intent.setClass(this, PhotoPlayActivity.class);
      }
      bundle.putInt("PlayMode", mPlayMode);
      intent.putExtras(bundle);
      startActivity(intent);
      // finish();
    } else if (model == ConstPhoto.PHOTO_FRAME_DEV) {
      mPlayMode = ConstPhoto.PHOTO_FRAME_DEV;
      showDialog(DIALOG_REU_TIP);
      new Thread() {
        @Override
        public void run() {
          mRecursiveFiles = ((MultiFilesManager) mFilesManager)
              .listRecursiveFiles(FilesManager.CONTENT_PHOTO,
                  MultiFilesManager.SOURCE_LOCAL, false);
          if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_PHOTO_FRAME);
          }
        }
      }.start();
    }

  }

  private void setThumbnailSize(int newSize) {
    if (mThumbnailSize != newSize) {
      mPageSize = 0;
      mThumbnailSize = newSize;
      mAdapter.updateThumbnail();
      // openDir(getListCurrentPath(), getListSelectedItemPosition());
      TVStorage.getInstance(this).set(MultiMediaConstant.THUMBNAIL_SIZE,
          newSize + "");
    }
  }

  private void copy() {
    // Modified by Dan for fix bug DTV00379203
    FileAdapter file = getListSelectedItem();
    if (null != file) {
      String copyedFile = file.getAbsolutePath();

      // remove for DTV00889683
      /*
       * if (mCopyedFiles.size() > 0) {
       * String oneCopyedFile = mCopyedFiles.get(0);
       * if (!mFilesManager.isInSameFolder(oneCopyedFile, copyedFile)) {
       * mSelectedFiles.clear();
       * mCopyedFiles.clear();
       * }
       * }
       */

      if (!mSelectedFiles.isEmpty()) {
        for (String path : mSelectedFiles) {
          if (!mCopyedFiles.contains(path)) {
            mCopyedFiles.add(path);
          }
        }
        mSelectedFiles.clear();
      }

      if (!mCopyedFiles.contains(copyedFile)) {
        mCopyedFiles.add(copyedFile);
      }

      cancelLoadFiles();
      // vList.setAdapter(mAdapter);
      mAdapter.notifyDataSetChanged();
    }
  }

  private void delete() {
    showDialog(DIALOG_CONFIRM_DELETE);
    if (vConfirmDeleteDialog != null) {
      vConfirmDeleteDialog
          .setOnConfirmDeleteListener(new OnConfirmDeleteListener() {
            @Override
            public void onPositivePressed() {
              new Thread(new Runnable() {
                @Override
                public void run() {
                  deleteSelectedFiles();
                  runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      // Modified by Dan for fix bug DTV00379203
                      openDir(getListCurrentPath());
                    }
                  });
                  dismissDialog(DIALOG_CONFIRM_DELETE);
                }
              }).start();
            }

            @Override
            public void onNegativePressed() {
              dismissDialog(DIALOG_CONFIRM_DELETE);
            }
          });
    }
  }

  private void deleteSelectedFiles() {
    // Modified by Dan for fix bug DTV00379203
    FileAdapter file = getListSelectedItem();
    if (null != file) {
      String selectedFile = file.getAbsolutePath();
      if (!mSelectedFiles.contains(selectedFile)) {
        mSelectedFiles.add(selectedFile);
      }

      for (String deleteFile : mSelectedFiles) {
        MtkLog.i(TAG, "deleteSelectedFile,deleteFile=" + deleteFile);
        if ((getListContentType() == MultiMediaConstant.AUDIO)) {
          LogicManager logicManager = LogicManager.getInstance(this);
          String path = logicManager.getCurrentPath(Const.FILTER_AUDIO);
          MtkLog.i(TAG, "deleteSelectedFile,logicManager.getCurrentPath:" + path);
          if (null != path && path.equals(deleteFile)) {
            if (null != logicManager.getAudioPlaybackService()) {
              logicManager.stopAudio();
              stopMusicView();
            }
          }
        }

        // change /storage/ to /mnt/media_rw/, /storage/ can not write
        String subPath = deleteFile.substring(9, deleteFile.length());
        String changeDeleteFile = "/mnt/media_rw/" + subPath;
        MtkLog.i(TAG, "deleteSelectedFile,changeDeleteFile=" + changeDeleteFile);

        new MtkFile(deleteFile).delete();

        cancelCopyIfDeleted(deleteFile);
      }

      mSelectedFiles.clear();
    }
  }

  private void cancelCopyIfDeleted(String file) {
    if (!mCopyedFiles.isEmpty() && mCopyedFiles.contains(file)) {
      mCopyedFiles.remove(file);
    }
  }

  private void paste() {
    MtkLog.d(TAG, "paste");
    long totalSize = 0;
    for (String path : mCopyedFiles) {
      MtkFile file = new MtkFile(path);
      totalSize += file.length();
    }

    final MtkFile dst = new MtkFile(getListCurrentPath());
    Bundle data = new Bundle();
    int max = (int) (totalSize / 2048);
    data.putInt("Max", max);
    showDialog(DIALOG_PROGRESS, data);

    OnUsbCopyProgressListener listener = new OnUsbCopyProgressListener() {
      @Override
      public void onSetProgress(long len) {
        if (vProgressDialog != null) {
          long size;
          synchronized (mCopyedFiles) {
            size = mPasteSizeCount + len;
          }
          int progress = (int) (size / 2048);
          // MtkLog.d(TAG, "OnUsbCopyProgressListener progress =" + progress);
          vProgressDialog.setProgress(progress);
        }
      }
    };

    final UsbFileOperater operator = UsbFileOperater.getInstance();
    operator.setOnUsbCopyProgressListener(listener);

    if (vProgressDialog != null) {
      vProgressDialog.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          MtkLog.d(TAG, "Paste Cancel!!");
          operator.setCopyAbort(true);
          mPasteCanceled = true;
        }
      });
    }

    new Thread(new Runnable() {
      @Override
      public void run() {
        synchronized (mCopyedFiles) {
          try {
            for (String path : mCopyedFiles) {
              MtkFile file = new MtkFile(path);
              if (vProgressDialog != null) {
                String msg = mResources.getString(
                    R.string.mmp_message_copy, file.getName(),
                    file.getParent(), getListCurrentPath());
                vProgressDialog.setMessage(msg);
              }
              setListItems(mFilesManager.getCurrentFiles());
              List<FileAdapter> items = mLoadFiles;
              for (final FileAdapter item : items) {
                if (item.getName().equals(new File(path).getName())) {
                  MtkLog.d(TAG, "Replace !!");
                  if (!mYesToAll) {
                    runOnUiThread(new Runnable() {
                      @Override
                      public void run() {
                        try {
                          dismissDialog(DIALOG_PROGRESS);
                        } catch (IllegalArgumentException e) {
                          // e.printStackTrace();
                          MtkLog.d(TAG, "dismissDialog DIALOG_PROGRESS exception~~");
                        }
                        Bundle data = new Bundle();
                        String msg = mResources
                            .getString(
                                R.string.mmp_message_confirm_replace,
                                item.getName());
                        data.putString("Message", msg);
                        showDialog(DIALOG_CONFIRM_REPLACE,
                            data);
                      }
                    });

                    if (!mYes) {
                      MtkLog.d(TAG, "mCopyedFiles.wait start");
                      mCopyedFiles.wait();
                      MtkLog.d(TAG, "mCopyedFiles.wait end");
                    }
                  }

                  if (mPasteCanceled) {
                    break;
                  }

                  if (mYesToAll || mYes) {
                    item.delete();
                    mYes = false;
                  }
                }
              }

              if (mPasteCanceled) {
                break;
              }

              if (mPasteReplaceNo) {
                mPasteReplaceNo = false;
                continue;
              }

              mCurrentCopyFileName = file.getName();
              mIsCopySuccess = operator.copyFile2Dir(file, dst);

              if (!mIsCopySuccess && mHandler != null) {
                if (mHandler.hasMessages(MSG_COPY_FILE_FAIL)) {
                  mHandler.removeMessages(MSG_COPY_FILE_FAIL);
                }
                mHandler.sendEmptyMessage(MSG_COPY_FILE_FAIL);
              }

              MtkLog.d(TAG, "paste isCopySuccess==" + mIsCopySuccess);
              MtkLog.d(TAG, "paste mCurrentCopyFileName==" + mCurrentCopyFileName);
              mPasteSizeCount += file.length();
            }
          } catch (Exception e) {
            MtkLog.d(TAG, "Paste File Error!!" + e.toString());
          }

          // dismissDialog(DIALOG_PROGRESS);
          removeDialog(DIALOG_PROGRESS);
          mCopyedFiles.clear();
          mPasteCanceled = false;
          mYesToAll = false;
          mPasteReplaceNo = false;
          mPasteSizeCount = 0;
        }

        openDir(getListCurrentPath());
      }
    }).start();

  }

  boolean mIsCopySuccess = false;
  String mCurrentCopyFileName = null;

  private void prepareResources() {
    if (TVContent.getInstance(this).isCNRegion()) {
      mContentTypeNames = mResources
          .getStringArray(R.array.mmp_main_names_cn);
    } else {
      mContentTypeNames = mResources
          .getStringArray(R.array.mmp_main_names);
    }
    TypedArray iocns = mResources
        .obtainTypedArray(R.array.mmp_main_resources);

    mContentTypeIcons = new Drawable[mContentTypeNames.length];
    for (int i = 0; i < mContentTypeIcons.length; i++) {
      mContentTypeIcons[i] = iocns.getDrawable(i);
    }
    iocns.recycle();
  }

  protected void findViews() {
    MtkLog.d(TAG, "findViews");
  }

  @Override
  protected void bindData(Intent intent) {
    getIntentData(intent);
    setupHeader();
    refreshListView(null);
    int sourceType = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();
    if (mMode == MODE_RECURSIVE && !"/".equals(getListCurrentPath())
        && sourceType == MultiFilesManager.SOURCE_LOCAL) {
      cancelLoadFiles();
      recursive();
      return;
    }
    MtkLog.i(TAG, "binddata:getListCurrentPath:" + getListCurrentPath());
    loadFiles(getListCurrentPath());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onPreLoadFiles() {
    showDialog(DIALOG_LOADING);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onLoadedFiles() {
    try {
      dismissDialog(DIALOG_LOADING);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListEmpty() {
    super.onListEmpty();
    if ((vLoginDialog == null || !vLoginDialog.isShowing())
        && (vProgressDialog == null || !vProgressDialog.isShowing())
        && (reuTip == null || !reuTip.isShowing())) {
      try {
        showDialog(DIALOG_EMPTY_TIP);
      } catch (Exception e) {
        MtkLog.d(TAG, "dismissDialog DIALOG_EMPTY_TIP exception~~");
      }
    }
    // stopMusicView();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListNotEmpty() {
    super.onListNotEmpty();
    try {
      dismissDialog(DIALOG_EMPTY_TIP);
    } catch (IllegalArgumentException e) {
      MtkLog.w(TAG, "No Such Dialog!!");
    }
    if (MediaMainActivity.mIsSambaAutoTest && mHandler != null) {
      mHandler.removeMessages(MSG_DLNA_AUTO_TEST_UPDATE_FILE);
      Message msg = Message.obtain();
      msg.what = MSG_DLNA_AUTO_TEST_UPDATE_FILE;
      msg.arg1 = 1;
      mHandler.sendMessageDelayed(msg, 1500);
    }
  }

  /**
   * {@inheritDoc}
   */

  @Override
  protected FilesManager<FileAdapter> setupFilesManager() {
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

    MultiFilesManager.setSmbAndDlnaAvailable(smbAvailable, dlnaAvailable);
    MultiFilesManager mManager = MultiFilesManager.getInstance(this);
    // manager.initDevices();
    // manager.initFilesManager(this);
    mManager.deleteObservers();
    mManager.addObserver(this);
    // manager.initDevicesManager();

    return mManager;
  }

  protected void getIntentData(Intent intent) {
    MtkLog.i(TAG, "base getIntentData");
    if (intent != null) {
      String path = intent.getStringExtra(INTENT_NAME_PATH);
      if (path == null || path.equals(mFilesManager.getParentPath())) {
        mFilesManager.setRefresh(true);
      } else {
        mFilesManager.setRefresh(false);
      }
      setListCurrentPath(path);

      int contentType = intent.getIntExtra(MultiMediaConstant.MEDIAKEY,
          -1);
      MtkLog.i(TAG, "base getIntentData contentType:" + contentType);
      if (contentType >= 0) {
        setListContentType(contentType);
        if (contentType == MultiMediaConstant.VIDEO) {
          LogicManager.getInstance(this).stopAudio();
        }
      }

      int currentSelection = intent
          .getIntExtra(INTENT_NAME_SELECTION, -1);
      MtkLog.i(TAG, "base getIntentData  currentSelection:" + currentSelection
          + "  path:" + path);
      if (currentSelection != -1) {
        mFilesManager.setPositionInParent(currentSelection);
        // getListView().requestFocusFromTouch();
        getListView().setSelection(currentSelection);
        MtkLog.i(TAG, "setSelection:" + getListView().getSelectedItemPosition());
      }
      ArrayList<String> copyedFiles = intent.getStringArrayListExtra(INTENT_NAME_COPYED_FILES);
      if (copyedFiles != null) {
        MtkLog.i(TAG, "base list path:" + path + "   size:" + copyedFiles.size());
      } else {
        MtkLog.i(TAG, "base list path:" + path);
      }
      if (copyedFiles != null && !copyedFiles.isEmpty()) {
        for (String copyedPath : copyedFiles) {
          mCopyedFiles.add(copyedPath);
        }
      }
      ArrayList<String> selectedFiles = intent.getStringArrayListExtra(INTENT_NAME_SELECTED_FILES);
      if (selectedFiles != null) {
        MtkLog.i(TAG, "selected base list path:" + path + "   size:" + selectedFiles.size());
        if (!selectedFiles.isEmpty()) {
          for (String selectpath : selectedFiles) {
            mSelectedFiles.add(selectpath);
          }
        }
      } else {
        MtkLog.i(TAG, "base list path:" + path);
      }

    }
  }

  protected void setupHeader() {
    MtkLog.i(TAG, "setupHeader");

  }

  @Override
  protected void onListItemSelected(AbsListView l, View v, int position,
      long id) {
    MtkLog.i(TAG, "onListItemSelected");
  }

  @Override
  protected void onListItemClick(AbsListView l, View v, int position, long id) {
    final FileAdapter file = getListItem(position);
    getListView().requestFocusFromTouch();
    // getListView().setSelection(position);
    mAdapter.cancel();
    if (null != file && file.isDirectory()) {
      // open directory
      mFilesManager.pushOpenedHistory(position);
      currentPosition = position;
      isFirstRequest = true;
      if (file instanceof DlnaFileAdapter
          && getListContentType() == FilesManager.CONTENT_THRDPHOTO) {
        setListContentType(FilesManager.CONTENT_PHOTO);
      }
      if (true == Feature.isSupportCnSamba()) {
        MultiFilesManager.getInstance(MtkFilesBaseListActivity.this).setCurrentPos(position);
      }
      openDir(file.getAbsolutePath(), 0);

    } else if (null != file) {
      // play multi media file
      // int pos = 0;
      /*
       * pos = getListSelectedItemPosition(); if (pos < 0) { return; }
       */
      int pos = position;
      if (this instanceof MtkFilesListActivity) {
        pos -= 1;
      } else {
        try {
          file.stopThumbnail();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
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

  /**
   * {@inheritDoc}
   */
  @Override
  protected void openDir(String path, int selection) {
    super.openDir(path, selection);
    if (path != null) {
      Intent intent = new Intent(this, this.getClass());
      intent.putExtra(INTENT_NAME_PATH, path);
      intent.putExtra(INTENT_NAME_SELECTION, selection);
      intent.putExtra(MultiMediaConstant.MEDIAKEY, getListContentType());
      startActivity(intent);
    } else {
      onReachRoot(selection);
    }
  };

  /**
   * Reach root
   *
   * @param selection
   */
  protected void onReachRoot(int selection) {
    mFilesManager.deleteObserver(this);
    mFilesManager.destroy();
  }

  protected void destroyManger() {
    mFilesManager.destroyManager();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void playFile(String path) {
    super.playFile(path);
    Intent intent = new Intent();
    Bundle bundle = new Bundle();

    int contentType = getListContentType();

    MtkLog.d(TAG, "playFile,mIsEnterPip==" + Util.mIsEnterPip);

    if (contentType == FilesManager.CONTENT_VIDEO
        || contentType == FilesManager.CONTENT_AUDIO
        || contentType == FilesManager.CONTENT_PHOTO
        || contentType == FilesManager.CONTENT_THRDPHOTO) {
      Util.exitPIP(this);
    }

    if (contentType == FilesManager.CONTENT_VIDEO || Util.mIsEnterPip) {
      LogicManager.getInstance(getApplicationContext()).finishVideo();
      if (VideoPlayActivity.getInstance() != null) {
        VideoPlayActivity.getInstance().finish();
      }
    }
    MtkLog.d(TAG, "playFile contentType:" + contentType);
    // || (contentType == FilesManager.CONTENT_THRDPHOTO && MultiFilesManager
    // .isSourceDLNA(getApplication()))
    if (contentType == FilesManager.CONTENT_PHOTO) {
      MtkLog.i(TAG, "FilesManager.CONTENT_PHOTO");
      if (true == Feature.isAospCnPlatform()) {
        intent.setClass(this, ImagePlayerActivity.class);
      } else {
        Util.reset3D(this);
        if (Util.isSupport4K8K()) {
          MtkLog.i(TAG, "4k2k on");
          intent.setClass(this, Photo4K2KPlayActivity.class);
        } else {
          MtkLog.i(TAG, "4k2k off");
          intent.setClass(this, PhotoPlayActivity.class);
        }
      }
      bundle.putInt("PlayMode", mPlayMode);
    } else if (contentType == FilesManager.CONTENT_AUDIO) {
      MtkLog.i(TAG, "FilesManager.CONTENT_AUDIO");
      intent.setClass(this, MusicPlayActivity.class);
      intent.putExtras(bundle);
      startActivityForResult(intent, 0);
      return;

    } else if (contentType == FilesManager.CONTENT_VIDEO) {
      int videoSource = MultiFilesManager.getInstance(this)
          .getCurrentSourceType();
      LogicManager.getInstance(this).initVideo(videoSource, this.getApplicationContext());
      LogicManager.getInstance(this).setSeamlessMode(true);
      LogicManager.getInstance(this).initDataSource();
      intent.putExtra("initDataSource", true);

      if (this instanceof MtkFilesListActivity) {
        intent.putExtra(Util.ISLISTACTIVITY, true);
      }
      intent.setClass(this, VideoPlayActivity.class);
    } else if (contentType == FilesManager.CONTENT_TEXT) {
      intent.setClass(this, TextPlayActivity.class);
    }

    intent.putExtras(bundle);
    startActivity(intent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void refreshListView(List<FileAdapter> files) {
    super.refreshListView(files);

    // mCurrentPage = 1;
    // mPageCount = 1;

    setListSelection(mFilesManager.getPositionInParent());

    MtkLog.d(TAG, "refreshListView");
    if (!Util.mIsEnterPip && isCurrentScreenHaveVideoFile()) {
      Util.exitPIP(this);
    }

  }

  protected void openDevicePath() {
    MtkLog.i(TAG, "openDevicePath");

  }

  protected void searchDesFile(boolean isDlna) {
    MtkLog.i(TAG, "searchDesFile" + isDlna);

  }

  /**
   * Observer change,
   */
  @Override
  public void update(Observable observable, Object data) {
    final int request = (Integer) data;
    MtkLog.d(TAG, "update ~~ " + " request =" + request);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        switch (request) {
          case FilesManager.REQUEST_SAMBA_LOGIN_CANCELED:
            showDialog(DIALOG_EMPTY_TIP);
            break;
          case FilesManager.REQUEST_SAMBA_LOGIN_FAILED:
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                if (null != mToast) {
                  mToast.cancel();
                  mToast = null;
                }
                mToast = Toast.makeText(
                    getApplicationContext(), mResources
                        .getString(R.string.mmp_login_failed_tip),
                    Toast.LENGTH_LONG);
                mToast.show();
              }
            });
            showDialog(DIALOG_EMPTY_TIP);
            break;
          case FilesManager.REQUEST_SAMBA_DISMISS_DIALOG:
            try {
              dismissDialog(DIALOG_EMPTY_TIP);
            } catch (IllegalArgumentException e) {
            }
            break;
          case FilesManager.REQUEST_SAMBA_UPDATE_ALL_SAMBA_DATA:
            MtkLog.d(TAG, "REQUEST_SAMBA_UPDATE_ALL_SAMBA_DATA,  mFilesManager.getFilesCount() = "
                + mFilesManager.getFilesCount());
            if (0 < mFilesManager.getFilesCount()) {
              try {
                dismissDialog(DIALOG_EMPTY_TIP);
              } catch (IllegalArgumentException e) {
              }
            }
            refreshListView();
            break;
          case FilesManager.REQUEST_SAMBA_SCAN_DEVICE_COMPLETED:
            MtkLog.d(TAG, "REQUEST_SAMBA_SCAN_DEVICE_COMPLETED,  mFilesManager.getFilesCount() = "
                + mFilesManager.getFilesCount());
            ((MultiFilesManager) mFilesManager).updateSambaDevice();
            refreshListView();
            break;
          case FilesManager.REQUEST_REFRESH: {
            if ((mFilesManager instanceof MultiFilesManager)
                && (((MultiFilesManager) mFilesManager)
                    .getCurrentSourceType() == MultiFilesManager.SOURCE_DLNA)
                && (mFilesManager.getFilesCount() == 0)) {
              onListEmpty();

            } else {

              try {
                dismissDialog(DIALOG_EMPTY_TIP);
              } catch (IllegalArgumentException e) {
                MtkLog.w(TAG, "No Such Dialog!!");
              }
            }
            refreshListView();
            if (MediaMainActivity.mIsDlnaAutoTest && mHandler != null) {
              mHandler.removeMessages(MSG_DLNA_AUTO_TEST_UPDATE_FILE);
              Message msg = Message.obtain();
              msg.what = MSG_DLNA_AUTO_TEST_UPDATE_FILE;
              msg.arg1 = 0;
              mHandler.sendMessageDelayed(msg, 1500);
            }
            break;
          }

          case FilesManager.REQUEST_SUB_DIRECTORY: {
            MultiFilesManager multiFileManager = MultiFilesManager
                .getInstance(MtkFilesBaseListActivity.this);
            String path = multiFileManager.getFirstDeviceMountPointPath();
            MtkLog.d("USB", path);
            MtkLog.d("USB", mFilesManager.getCurrentPath());
            if (!mFilesManager.getCurrentPath().equals(path)) {
              mFilesManager.pushOpenedHistory(0);
              openDir(path, 0);
            }

            break;
          }
          case FilesManager.REQUEST_DEVICE_LEFT: {
            try {
              dismissDialog(DIALOG_EMPTY_TIP);
            } catch (IllegalArgumentException e) {
              MtkLog.w(TAG, "No Such Dialog!!");
            }
            Toast.makeText(getApplicationContext(), mResources
                .getString(R.string.mmp_dlnaserver_disconnected), Toast.LENGTH_LONG).show();
            LogicManager.getInstance(MtkFilesBaseListActivity.this).stopAudio();
            ((MmpApp) getApplication()).finishMediaPlayActivity();
            Intent intent = new Intent();
            intent.putExtra(INTENT_NAME_PATH, "/");
            intent.putExtra(MultiMediaConstant.MEDIAKEY,
                mFilesManager.getContentType());
            bindData(intent);

            break;
          }
          case FilesManager.REQUEST_MMP_MENU: {
            openDir(null, 0);
            break;
          }
          case FilesManager.REQUEST_LOGIN:
            Looper.myQueue().addIdleHandler(new IdleHandler() {
              @Override
              public boolean queueIdle() {
                try {
                  dismissDialog(DIALOG_EMPTY_TIP);
                } catch (IllegalArgumentException e) {
                  MtkLog.w(TAG, "No Such Dialog!!");
                }
                if (!isFirstRequest) {
                  MtkLog.e("chengcl", "currentPosition==" + currentPosition);
                  mFilesManager.pushOpenedHistory(currentPosition);
                }
                MtkLog.d(TAG, "REQUEST_LOGIN= isFirstRequest=" + isFirstRequest);
                if (isFirstRequest) {
                  isFirstRequest = false;
                } else {
                  MtkLog.d(TAG, "REQUEST_LOGIN= show DIALOG_LOGIN_FAILED_TIP=");
                  Toast.makeText(getApplicationContext(), mResources
                      .getString(R.string.mmp_login_failed_tip), Toast.LENGTH_SHORT).show();
                  // showDialog(DIALOG_LOGIN_FAILED_TIP);
                }
                Bundle data = new Bundle();
                data.putBoolean("Init", true);
                showDialog(DIALOG_LOGIN, data);
                return false;
              }
            });

            break;

          case FilesManager.REQUEST_BACK_TO_ROOT: {
            mFlag = true;
            openDir(getListRootPath());
            break;
          }
          case FilesManager.REQUEST_SOURCE_CHANGED: {
            resetSourceChange();
            break;
          }
          default:
            break;
        }
      }
    });
  }

  protected void resetSourceChange() {
    mMode = MODE_NORMAL;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
      switch (resultCode) {
        case 100:
          PlaybackService mAudioPlayback = LogicManager.getInstance(this).getAudioPlaybackService();
          if (null != mAudioPlayback) {
            int playStatus = mAudioPlayback.getPlayStatus();
            if (playStatus == AudioConst.PLAY_STATUS_FF
                || playStatus == AudioConst.PLAY_STATUS_FR
                || playStatus == AudioConst.PLAY_STATUS_SF) {
              mAudioPlayback.play();
            }
          }
          break;

        default:
          break;
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  // class MtkNetWorkRecevier extends BroadcastReceiver{
  //
  // @Override
  // public void onReceive(Context context, Intent intent) {
  // ConnectivityManager connectivityManager = (ConnectivityManager)
  // context.getSystemService(Context.CONNECTIVITY_SERVICE);
  // if(null != connectivityManager){
  // NetworkInfo [] netWorkInfos = connectivityManager.getAllNetworkInfo();
  // for(int i = 0; i<netWorkInfos.length;i++){
  // State state = netWorkInfos[i].getState();
  // if(NetworkInfo.State.CONNECTED==state){
  // MtkLog.e(TAG, "network ok.....");
  // return ;
  // }
  // }
  // }
  //
  // MtkLog.e(TAG, "network error.....");
  // MultiFilesManager.getInstance(MtkFilesBaseListActivity.this).netWorkError();
  //
  // }
  //
  //
  // }

  // private class BaseNetworkObserver extends
  // INetworkManagementEventObserver.Stub {
  // @Override
  // public void interfaceStatusChanged(String iface, boolean up) {
  // // default no-op
  // MtkLog.i(TAG, " interfaceStatusChanged");
  // }
  //
  // @Override
  // public void interfaceRemoved(String iface) {
  // // default no-op
  // MtkLog.i(TAG, " interfaceRemoved");
  // }
  //
  // @Override
  // public void addressUpdated(String iface, LinkAddress address) {
  // // default no-op
  // Log.i(TAG, " addressUpdated:" + iface + " address:" + address.getAddress()
  // + " getListCurrentPath():" + getListCurrentPath());
  // if (getListCurrentPath() != null
  // && !getListCurrentPath().equals(mFilesManager.getRootPath())) {
  // return;
  // }
  // int sourceType = MultiFilesManager.getInstance(getBaseContext())
  // .getCurrentSourceType();
  // if (mMode == MODE_RECURSIVE && !"/".equals(getListCurrentPath())
  // && sourceType == MultiFilesManager.SOURCE_LOCAL) {
  // cancelLoadFiles();
  // recursive();
  // return;
  // }
  // MtkLog.i(TAG, "addressUpdated binddata:getListCurrentPath:" +
  // getListCurrentPath());
  // MultiFilesManager.getInstance(MtkFilesBaseListActivity.this).cleanAllDevices();
  // Log.i(TAG, "addressUpdated reset dlan");
  // ((MmpApp) MtkFilesBaseListActivity.this.getApplication()).resetDlna();
  // Log.i(TAG, "addressUpdated reset dlan done");
  // loadFiles(getListCurrentPath());
  // }
  //
  // @Override
  // public void addressRemoved(String iface, LinkAddress address) {
  // // default no-op
  // MtkLog.i(TAG, " addressRemoved");
  // MultiFilesManager.getInstance(MtkFilesBaseListActivity.this).netWorkStateChange(false);
  // DLNAManager.getInstance().deleteDlnaFile("goldendms");
  // }
  //
  // @Override
  // public void interfaceLinkStateChanged(String iface, boolean up) {
  // // default no-op
  // MtkLog.i(TAG, " interfaceLinkStateChanged");
  //
  // }
  //
  // @Override
  // public void interfaceAdded(String iface) {
  // // default no-op
  // MtkLog.i(TAG, " interfaceAdded");
  // }
  //
  // @Override
  // public void interfaceClassDataActivityChanged(int transportType, boolean
  // active, long tsNanos,
  // int uid) {
  // // default no-op
  // MtkLog.i(TAG, "base interfaceClassDataActivityChanged");
  // }
  //
  // @Override
  // public void limitReached(String limitName, String iface) {
  // // default no-op
  // MtkLog.i(TAG, "base limitReached");
  // }
  //
  // @Override
  // public void interfaceDnsServerInfo(String iface, long lifetime, String[]
  // servers) {
  // // default no-op
  // MtkLog.i(TAG, "base interfaceDnsServerInfo");
  // }
  //
  // @Override
  // public void routeUpdated(RouteInfo route) {
  // // default no-op
  // MtkLog.i(TAG, "base routeUpdated");
  // }
  //
  // @Override
  // public void routeRemoved(RouteInfo route) {
  // // default no-op
  // MtkLog.i(TAG, "base routeRemoved");
  //
  // }
  // }

  protected void removeVideoView() {
    MtkLog.i(TAG, "removeVideoView");
  }

  // private IntentFilter ifilter = new
  // IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
  //
  //
  // private BroadcastReceiver mRootReceiver = new BroadcastReceiver(){
  //
  // @Override
  // public void onReceive(Context context, Intent intent) {
  // if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent
  // .getAction())) {
  // Log.i(TAG,"received ACTION_CLOSE_SYSTEM_DIALOGS");
  // if(LogicManager.getInstance(context).isAudioPlaying()){
  // LogicManager.getInstance(context).stopAudio();
  // stopMusicView();
  // return;
  // }
  // if(LogicManager.getInstance(context).isPlaying()){
  // removeVideoView();
  // }
  //
  // }
  // }
  // };

  public static void setFromDmr() {
    isFromDmr = true;
  }

  public void handleRootMenuEvent() {
    if (vList != null) {
      int index = vList.getSelectedItemPosition();
      if (index > 0 && mAdapter != null) {
        FileAdapter adapter = mAdapter.getItem(index);
        if (adapter != null) {
          mCurrentSelectedPath = adapter.getAbsolutePath();
          MtkLog.d(TAG, "handleRootMenuEvent mCurrentSelectedPath:" + mCurrentSelectedPath);
        }
      }
    }
    Util.enterMmp(0, getApplicationContext());
  }

  IRootMenuListener mRootMenuListener = new IRootMenuListener() {

    @Override
    public void handleRootMenu() {
      // TODO Auto-generated method stub
      MtkLog.i(TAG, "handleRootMenu Received!");
      handleRootMenuEvent();
    }
  };
}
