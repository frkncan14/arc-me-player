
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MediaGridView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.Tools;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader.LoadWork;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;

public class MtkFilesGridActivity extends MtkFilesBaseListActivity {

  private static final String TAG = "MtkFilesGridActivity";

  private ImageView vLeftTopImg;

  private TextView vLeftTopTv;

  private TextView vLeftMidTv;

  private TextView vTopPath;

  private TextView vTopRight;

  private final int mGridViewH = (ScreenConstant.SCREEN_HEIGHT * 11 / 13);

  private final int mGridViewW = (int) (ScreenConstant.SCREEN_WIDTH * 0.75);

  private final int mVerticalSpacing = ScreenConstant.SCREEN_HEIGHT / 38;

  private final int mHorizontalSpacing = ScreenConstant.SCREEN_WIDTH / 64;

  private int mItemWidth;

  private int mItemHeight;

  /*
   * if turn list mode from grid mode, true, else false, for clear info and
   * thumbnail array not in
   * onstop function, but in press bule key. avoid clear array item when add in
   * list mode Help me?
   */
  private boolean mIsListMode = false;

  /* if turn page, true, and will add Blank item, else false. */
  private boolean mTurnPage = false;

  private final AdapterCallback mCallBack = new AdapterCallback() {

    @Override
    public void onMethodCallback(int position, String info) {
      if (position == getListSelectedItemPosition()) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            vLeftMidTv.setText(info);
          }
        });
      }
    }
  };

  public interface AdapterCallback {
    void onMethodCallback(int position, String info);
  }

  public static final int SHOW_MUSIC = 102;

  private final Handler mGridHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case SHOW_MUSIC:
          onShowMusicView();
          break;
        default:
          break;
      }

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
          int index = findSelectItem(texts.get(0).toString());
          MtkLog.d(TAG, "onRequestSendAccessibilityEvent.,index==" + index);

          if (index >= 0) {
            vList.setSelection(index);
          }
        }

      } while (false);

      return true;// host.onRequestSendAccessibilityEventInternal(child, event);
    }

    private int findSelectItem(String text) {
      if (mLoadFiles == null) {
        return -1;
      }

      for (int i = 0; i < mLoadFiles.size(); i++) {
        if (mLoadFiles.get(i).getName().equals(text)) {
          return i;
        }
      }

      return -1;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mViewMode = VIEW_MODE_GRID;
    super.onCreate(savedInstanceState);
    // if(AnimationManager.getInstance().getIsAnimation()){
    // AnimationManager.getInstance().startActivityEnterAnimation(this,
    // findViewById(R.id.mmp_files_grid_layout), null);
    // }

    Util.logLife(TAG, "onCreate");
    RelativeLayout listLeft = findViewById(R.id.file_list_left);
    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) listLeft.getLayoutParams();
    lp.width = (int) (ScreenConstant.SCREEN_WIDTH * 0.25);
    listLeft.setLayoutParams(lp);
    vList.setAccessibilityDelegate(mAccDelegate);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if ((getListContentType() != MultiMediaConstant.VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      showMusicView();
      MtkLog.i(TAG, "onResume musicview status:" + LogicManager.getInstance(this).getAudioStatus());
    } else {
      MtkLog.i(TAG, "onResume status:" + LogicManager.getInstance(this).getAudioStatus());
    }

    mPlayMode = 0;
    Util.logLife(TAG, "onResume");
  }

  private void onShowMusicView() {
    if ((getListContentType() != MultiMediaConstant.VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      if (mPopView != null && !mPopView.isShowing()) {
        if (GetCurrentTask.getInstance(getApplicationContext()).isCurActivtyGridActivity()) {
          showMusicView();
        }
      }
    }
  }

  IAudioPlayListener mListener = new IAudioPlayListener() {

    @Override
    public void notify(int status) {
      // TODO Auto-generated method stub
      LogicManager.getInstance(MtkFilesGridActivity.this).registerAudioPlayListener(null);
      mGridHandler.sendEmptyMessage(SHOW_MUSIC);
    }
  };

  @Override
  protected void onRestart() {
    super.onRestart();
    int position = setCurrentSelection();
    vList.requestFocusFromTouch();
    vList.setSelection(position);
    mAdapter.notifyDataSetChanged();
    Util.logLife(TAG, "onRestart");
    // LogicManager.getInstance(this).registerAudioPlayListener(mListener);

  }

  @Override
  protected void onStart() {
    super.onStart();
    Util.logLife(TAG, "onStart");
    LogicManager.getInstance(this).registerAudioPlayListener(mListener);
    findViewById(R.id.layout_background).setBackground(getDrawable(R.drawable.mmp_main_bg));
  }

  @Override
  protected void initMusicView() {
    View contentView = LayoutInflater.from(this).inflate(
        R.layout.mmp_musicbackplay, null);
    // int width = getWindowManager().getDefaultDisplay().getRawWidth();
    // int height = getWindowManager().getDefaultDisplay().getRawHeight();
    mPopView = new PopupWindow(contentView, ScreenConstant.SCREEN_WIDTH / 4
        - (ScreenConstant.SCREEN_WIDTH / 64), ScreenConstant.SCREEN_HEIGHT / 2);
    vMusicView = new MusicPlayInfoView(this, contentView, 0,
        mPopView);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    MtkLog.d(TAG, "onNewIntent call mAdapter.clearWork");
    mAdapter.clearWork();
    Util.logLife(TAG, "onNewIntent");
  }

  @Override
  protected void showMusicView() {
    super.showMusicView();

    Looper.myQueue().addIdleHandler(new IdleHandler() {
      @Override
      public boolean queueIdle() {
        // todo
        int popViewLeftStart = (int) getResources().getDimension(R.dimen.list_left_bg_start);
        mPopView.showAsDropDown(vLeftMidTv, -popViewLeftStart, 0);
        vMusicView.init(MtkFilesGridActivity.this);
        return false;
      }
    });
  }

  @Override
  protected int setupContentView() {
    return R.layout.mmp_files_grid;
  }

  @Override
  protected FilesAdapter getAdapter() {
    return new MtkFilesGridAdapter(this, mCallBack);
  }

  @Override
  protected void findViews() {
    vLeftTopImg = (ImageView) findViewById(R.id.multimedia_showinfo_img);
    vLeftMidTv = (TextView) findViewById(R.id.multimedia_showinfo_left);
    vLeftTopTv = (TextView) findViewById(R.id.mmp_grid_toptv);
    vTopPath = (TextView) findViewById(R.id.mmp_grid_filepath);
    vTopRight = (TextView) findViewById(R.id.mmp_grid_pagesize);
  }

  @Override
  protected void setupHeader() {
    int contentType = getListContentType();

    if (contentType == FilesManager.CONTENT_ALL) {
      Log.d(TAG, "setupHeader,contentType = -1");
      return;
    }

    if (contentType == FilesManager.CONTENT_THRDPHOTO) {
      contentType = FilesManager.CONTENT_PHOTO;
    }
    vLeftTopImg.setImageDrawable(mContentTypeIcons[contentType]);
    vLeftTopTv.setText(mContentTypeNames[contentType]);

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
    vTopPath.setText(curPath);
    vTopRight.setText("");
    vLeftMidTv.setText("");
  }

  @Override
  protected void onPostResume() {
    super.onPostResume();
    TextView filePath = (TextView) findViewById(R.id.mmp_grid_filepath);
    if (filePath != null) {
      filePath.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }
  }

  @Override
  public void onBackPressed() {
    if (MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
      super.onBackPressed();
      return;
    }
    if (!MediaMainActivity.isValid(300)) {
      return;
    }
    LogicManager mLogicManager = LogicManager.getInstance(this);

    MultiFilesManager multiFileManager = MultiFilesManager
        .getInstance(this);
    String path = multiFileManager.getFirstDeviceMountPointPath();
    String parentPath = getListParentPath();
    String currentPath = getListCurrentPath();

    MtkLog.d(TAG, "onBackPressed ParentPath :" + parentPath + "---curpath = " + currentPath
        + "---mMode= " + mMode + "---deviceNum= " + multiFileManager.getAllDevicesNum() + "--path:"
        + path);

    if ((null != parentPath && parentPath.equals("/mnt"))
        || MultiFilesManager.ROOT_PATH.equals(currentPath)) {
      onReachRoot(0);
      return;
    }

    if (multiFileManager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES) {
      if (false == Feature.isSupportCnSamba()) {
        if (getListCurrentPath().equals(path) || mMode == MODE_RECURSIVE) {
          onReachRoot(0);
          return;
        }
      }
    } else if (multiFileManager.getAllDevicesNum() == MultiFilesManager.MORE_DEVICES) {
      if (MultiFilesManager.getInstance(getApplicationContext()).isContainMountPoint(currentPath)) {
        openDir(MultiFilesManager.ROOT_PATH, mFilesManager.popOpenedHistory());
        return;
      }
    }

    if (mMode == MODE_RECURSIVE) {
      openDir(MultiFilesManager.ROOT_PATH, 0);
    } else {
      openDir(getListParentPath(), mFilesManager.popOpenedHistory());
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
  }

  @Override
  protected void onReachRoot(int selection) {
    stopMusicView();
    if (selection == 0) {

      mAdapter.cancel();
      final Thumbnail thumbnail = Thumbnail.getInstance();
      if (thumbnail.isLoadThumanil()) {
        if (mThreadHandler != null) {
          mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
              thumbnail.stopThumbnail();
            }
          });
        }
      }
    }
    // Don't remove listener by jianfang.
    super.onReachRoot(selection);
    destroyManger();
    LogicManager mLogicManager = LogicManager.getInstance(this);
    if (mLogicManager.isAudioStarted()
        && mLogicManager.getPlayMode() != AudioConst.PLAYER_MODE_LOCAL) {
      mLogicManager.stopAudio();
    }
    finish();
    // exit();
  }

  @Override
  protected void playFile(String path) {
    if (mPopView != null && mPopView.isShowing()) {
      vMusicView.removeMessage();
      mPopView.dismiss();
    }

    super.playFile(path);
  }

  @Override
  protected void stopMusicView() {
    if (vMusicView != null) {
      vMusicView.removeMessage();
    }
    super.stopMusicView();
  }

  protected void moveTo(int selection) {
    // mAdapter.cancel();
    // setListAdapter(mAdapter);
    setListSelection(selection);
    mAdapter.notifyDataSetChanged();
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
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      int keyCode = event.getKeyCode();
      keyCode = Util.changeKeycodeToRtl(keyCode);
      int position = getListSelectedItemPosition();
      int count = getListItemsCount();
      int rowSize = getNumberColum();
      MtkLog.i(TAG, "dispatchKeyEvent keyCode= " + keyCode);
      switch (keyCode) {
        case KeyMap.KEYCODE_DPAD_UP: {
          mTurnPage = false;
          if (position >= rowSize) {
            position -= rowSize;
            mCurrentPage = computeCurrentPage(position);
          } else {
            position = 0;
            mCurrentPage = computeCurrentPage(position);
            setListSelection(position);
            return true;
          }
          break;
        }
        case KeyMap.KEYCODE_DPAD_DOWN: {
          mTurnPage = false;
          if ((position + rowSize) < count) {
            position += rowSize;
            mCurrentPage = computeCurrentPage(position);
          } else {
            position = count - 1;
            mCurrentPage = computeCurrentPage(position);
            setListSelection(position);
            vList.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN);
            return true;
          }
          break;
        }
        case KeyMap.KEYCODE_DPAD_LEFT: {
          if (position >= 1) {
            position -= 1;
            mCurrentPage = computeCurrentPage(position);
            setListSelection(position);
            vList.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
            return true;
          }
          return true;
        }
        case KeyMap.KEYCODE_DPAD_RIGHT: {
          if ((position + 1) < count) {
            position += 1;
            mCurrentPage = computeCurrentPage(position);
            setListSelection(position);
            vList.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
            return true;
          }
          return true;
        }
        case KeyMap.KEYCODE_MTKIR_CHUP: {
          MtkLog.d(TAG, "position==" + position + ",mPageSize==" + mPageSize);
          if (position == 0) {
            return true;
          }
          if ((position - mPageSize) >= 0) {
            position -= mPageSize;
          } else {
            position = 0;
          }
          mTurnPage = true;
          mCurrentPage = computeCurrentPage(position);
          setListSelection(position);
          return true;
        }
        case KeyMap.KEYCODE_MTKIR_CHDN: {
          MtkLog.d(TAG, "position==" + position + ",mPageSize==" + mPageSize + ",count==" + count);
          if (position == count - 1) {
            return true;
          }
          if ((position + mPageSize) < count) {
            position += mPageSize;
          } else {
            position = count - 1;
          }
          mCurrentPage = computeCurrentPage(position);
          if (mCurrentPage == mPageCount) {
            mTurnPage = true;
            // setListAdapter(mAdapter);
          }
          setListSelection(position);
          return true;
        }
        default:
          break;
      }
    }

    return super.dispatchKeyEvent(event);
  }

  void scrollTo(int count) {
    int scrollY;
    int marginRows = getMarginRows(count);
    if (marginRows > 0) {
      scrollY = marginRows * (mItemHeight + mVerticalSpacing)
          + mVerticalSpacing;
      vList.scrollTo(vList.getScrollX(), scrollY);
    }
  }

  private int getMarginRows(int count) {
    int marginRows = 0;
    if (count % mPageSize > 0) {
      marginRows = (mPageSize - count % mPageSize) / getNumberColum();
    }
    return marginRows;

  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_BLUE:
        if (!MediaMainActivity.isValid(400)) {
          break;
        }

        if (true == Feature.isSupportCnSamba()) {
          final String curPath = getListCurrentPath();
          if ((curPath != null && curPath.equals("/"))
              || (true == Tools.isSambaPlaybackUrl(curPath))) {
            MtkLog.d(TAG, "Blue key event is ignored in Sam path");
            return true;
          }
        }

        Util.exitPIP(this);
        LogicManager.getInstance(getApplicationContext()).finishVideo();
        if (VideoPlayActivity.getInstance() != null) {
          VideoPlayActivity.getInstance().finish();
        }
        // if(MultiFilesManager.getInstance(this).getCurrentSourceType() == 0){
        // return true;
        // }

        /* by lei add for stop video and audio get thumbnail */
        int contentType = getListContentType();
        // ((MtkFilesGridAdapter) mAdapter).stop();

        mAdapter.stop();
        if (contentType == FilesManager.CONTENT_AUDIO
            || contentType == FilesManager.CONTENT_VIDEO) {
          ((MtkFilesGridAdapter) mAdapter).stopFileDecode();
        }

        mIsListMode = true;
        if (null != mInfoLoader) {
          mInfoLoader.clearQueue();
        }

        Intent intent = new Intent(this, MtkFilesListActivity.class);
        intent.putExtra(INTENT_NAME_PATH, getListCurrentPath());
        intent.putExtra(INTENT_NAME_SELECTION,
            getListSelectedItemPosition() + 1);
        intent.putExtra("Mode", 0);
        intent.putStringArrayListExtra(INTENT_NAME_COPYED_FILES,
            new ArrayList<String>(mCopyedFiles));
        intent.putStringArrayListExtra(INTENT_NAME_SELECTED_FILES,
            new ArrayList<String>(mSelectedFiles));
        // add for ThumbnailSize bug
        intent.putExtra("mThumbnailSize", mThumbnailSize);

        startActivity(intent);
        finish();
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onListItemSelected(AbsListView l, View v, int position,
      long id) {
    MtkLog.d(TAG, "onListItemSelected~~~ position= " + position);
    setSelect();
  }

  private void setSelect() {
    int position = vList.getSelectedItemPosition();
    MtkLog.d(TAG, "setSelect~~~:" + position);
    FileAdapter file = getListItem(position);

    if (mPageCount == 0) {
      vTopRight.setText("");
    } else {
      mCurrentPage = computeCurrentPage(position);
      MtkLog.d(TAG, "onListItemSelected aft mCurrentPage" + mCurrentPage);
      vTopRight.setText(mCurrentPage + "/" + mPageCount);
    }

    if (file == null) {
      return;
    }
    if (file.isIsoFile()) {
      // TODO mount iso file
      return;
    }
    if (file.isFile()) {
      int type = mFilesManager.getContentType();
      String info = "";
      if (type == MultiMediaConstant.AUDIO || type == MultiMediaConstant.VIDEO
          || type == MultiMediaConstant.PHOTO || type == MultiMediaConstant.THRD_PHOTO) {
        if (MultiFilesManager.isSourceDLNA(getApplicationContext())) {
          info = mInforCache.get(file.getAbsolutePath() + file.getSuffix());
        } else {
          info = mInforCache.get(file.getAbsolutePath());
        }
        if (info != null) {
          vLeftMidTv.setText(info);
          return;
        } else {
          vLeftMidTv.setText("");
        }
        String suffix = file.getSuffix();
        if (null != suffix && suffix.startsWith(".")) {
          suffix = suffix.substring(1);
        }
        if ("pcm".equalsIgnoreCase(suffix)
            || "lpcm".equalsIgnoreCase(suffix)) {
          MtkLog.w(TAG, "pcm file:" + file.getAbsolutePath());
          vLeftMidTv.setText(file.getName() + file.getSuffix());
          return;
        }
        // fix
        // if (mInfoLoader != null) {
        // // mInfoLoader.clearQueue();
        // LoadInfo loadinfo = mLoadInfoWorks.get(file.getAbsolutePath());
        // if (null == loadinfo) {
        // loadinfo = new LoadInfo(file, vLeftMidTv);
        // }
        // mInfoLoader.addSelectedInfoWork(loadinfo);
        // } else {
        //
        // mInfoLoader = AsyncLoader.getInstance(1);
        // if (null == mLoadInfoWorks) {
        // mLoadInfoWorks = new ConcurrentHashMap<String, LoadInfo>();
        // }
        // LoadInfo loadinfo = new LoadInfo(file, vLeftMidTv);
        // mInfoLoader.addSelectedInfoWork(loadinfo);
        //
        // }
        // info = mInforCache.get(file.getAbsolutePath());
      } else {
        info = file.getInfo();
        vLeftMidTv.setText(info);
        return;
      }
    } else {
      vLeftMidTv.setText("");
    }

  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    // MtkLog.d(TAG,"onScrollStateChanged scrollState ="+scrollState);

  };

  private int lastFirstVisibleItem = 0;

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
      int totalItemCount) {

    if (firstVisibleItem != lastFirstVisibleItem) {
      // cancelLoadFiles();
      mAdapter.cancel();
      // Thumbnail.getInstance().stopThumbnail();
      lastFirstVisibleItem = firstVisibleItem;
      vList.invalidateViews();
    }

    MtkLog.d(TAG, "onScroll firstVisibleItem =" + firstVisibleItem + "visibleItemCount ="
        + visibleItemCount +
        "totalItemCount = " + totalItemCount);

  };

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    vLeftMidTv.setText("");
  }

  protected int computePageSize() {
    mPageSize = getRowNumber() * getNumberColum();
    mItemWidth = getColumnWidth();
    mItemHeight = getColumnHeight();
    int filesCount = getListItemsCount();
    mPageCount = ceilPage(filesCount - 1, mPageSize);
    return mPageSize;
  }

  public int ceilPage(int position, int pageSize) {

    if (pageSize == 0) {
      return 0;
    }
    // index begin from 0
    position += 1;
    if (position == 1) {
      return 1;
    } else if (position % pageSize == 0) {
      return position / pageSize;
    } else {
      return position / pageSize + 1;
    }

  }

  protected int computeCurrentPage(int position) {
    MtkLog.d(TAG, "computeCurrentPage  position: " + position);
    int page = mCurrentPage;
    if (mPageSize > 0 && position >= 0 && position < getListItemsCount()) {
      page = ceilPage(position, mPageSize);
    }

    return page;
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
    mPageSize = computePageSize();
    mCurrentPage = computeCurrentPage(getListSelectedItemPosition());
    if (mPageCount == 0) {
      vTopRight.setText("0/0");
    } else {
      vTopRight.setText(String.format("%d/%d", mCurrentPage, mPageCount));
    }
  }

  /*
   * mGridViewH = getColumnHeight * rowNumber + mVerticalSpacing * (rowNumber - 1)
   * => mGridViewH = (getColumnHeight + mVerticalSpacing) * rowNumber -
   * mVerticalSpacing
   * => mGridViewH + mVerticalSpacing = (getColumnHeight + mVerticalSpacing) *
   * rowNumber
   * => rowNumber = (mGridViewW + mVerticalSpacing) / (getColumnHeight +
   * mVerticalSpacing)
   */
  public int getRowNumber() {
    int rowNumber = (mGridViewH + mVerticalSpacing) / (getColumnHeight() + mVerticalSpacing);
    Log.i(TAG, "getRowNumber:" + rowNumber);
    return rowNumber;
  }

  public int getColumnHeight() {
    float dpiRate = getResources().getDisplayMetrics().density / 2;
    int height = (int) (getResources().getDimension(R.dimen.gridview_item_height_small) * dpiRate);
    switch (mThumbnailSize) {
      case MultiMediaConstant.SMALL: {
        height = (int) (getResources().getDimension(R.dimen.gridview_item_height_small) * dpiRate);
        break;
      }
      case MultiMediaConstant.MEDIUM: {
        height = (int) (getResources().getDimension(R.dimen.gridview_item_height_medium) * dpiRate);
        break;
      }
      case MultiMediaConstant.LARGE: {
        height = (int) (getResources().getDimension(R.dimen.gridview_item_height_large) * dpiRate);
        break;
      }
    }
    MtkLog.i(TAG, "getColumnHeight:" + height);
    return height;
  }

  /*
   * mGridViewW = getColumnWidth * columnNumber + mHorizontalSpacing *
   * (columnNumber - 1)
   * => mGridViewW = (getColumnWidth + mHorizontalSpacing) * columnNumber -
   * mHorizontalSpacing
   * => mGridViewW + mHorizontalSpacing = (getColumnWidth + mHorizontalSpacing) *
   * columnNumber
   * => columnNumber = (mGridViewW + mHorizontalSpacing) / (getColumnWidth +
   * mHorizontalSpacing)
   */

  public int getNumberColum() {
    int columnNumber = (mGridViewW + mHorizontalSpacing) / (getColumnWidth() +
        mHorizontalSpacing);
    Log.i(TAG, "getNumberColum:" + columnNumber);

    /*
     * return columnNumber;
     */
    return 4; // arcelik customized
  }

  public int getColumnWidth() {
    float dpiRate = getResources().getDisplayMetrics().density / 2;
    int width = (int) (getResources().getDimension(R.dimen.gridview_item_width_small) * dpiRate);
    switch (mThumbnailSize) {
      case MultiMediaConstant.SMALL: {
        width = (int) (getResources().getDimension(R.dimen.gridview_item_width_small) * dpiRate);
        break;
      }
      case MultiMediaConstant.MEDIUM: {
        width = (int) (getResources().getDimension(R.dimen.gridview_item_width_medium) * dpiRate);
        break;
      }
      case MultiMediaConstant.LARGE: {
        width = (int) (getResources().getDimension(R.dimen.gridview_item_width_large) * dpiRate);
        break;
      }
    }
    MtkLog.i(TAG, "getColumnWidth:" + width);
    return width;
  }

  @Override
  protected void refreshListView(List<FileAdapter> files) {
    ((MediaGridView) vList).setNumColumns(getNumberColum());

    super.refreshListView(files);

    if (files != null) {
      mPageSize = computePageSize();
      mCurrentPage = computeCurrentPage(getListSelectedItemPosition());
      if (mPageCount == 0) {
        vTopRight.setText("0/0");
      } else {
        vTopRight.setText(String.format("%d/%d", mCurrentPage, mPageCount));
      }
    }
  }

  @Override
  protected void openDevicePath() {
    List<FileAdapter> fileList = ((MultiFilesManager) mFilesManager).getCurrentFiles();
    MtkLog.d(TAG, "openDevicePath count:" + fileList.size());
    // FileAdapter file = null;
    if (MediaMainActivity.mAutoTestFileDirectorys != null
        && MediaMainActivity.mAutoTestFileDirectorys.size() > 0) {
      MtkLog.d(TAG, "openDevicePath mAutoTestFileDirectorys:"
          + MediaMainActivity.mAutoTestFileDirectorys.get(0));
      for (FileAdapter file : fileList) {
        if (file != null) {
          String name = file.getName();
          if (name.equals(MediaMainActivity.mAutoTestFileDirectorys.get(0))) {
            MediaMainActivity.mAutoTestFileDirectorys.remove(0);
            openDir(file.getAbsolutePath());
            return;
          }
        }
      }
    }
  }

  @Override
  protected void searchDesFile(boolean isDlna) {
    MtkFilesGridAdapter tempAdapter = (MtkFilesGridAdapter) getListAdapter();
    int count = tempAdapter.getCount();
    MtkLog.d(TAG, "searchDesFile count:" + count);
    tempFile = null;
    FileAdapter file = null;
    if (MediaMainActivity.mAutoTestFileDirectorys != null
        && MediaMainActivity.mAutoTestFileName != null
        && MediaMainActivity.mAutoTestFilePath != null) {
      if (MediaMainActivity.mAutoTestFileDirectorys.size() > 0) {
        for (int i = 0; i < count; i++) {
          file = getListItem(i);
          if (file != null) {
            String name = file.getName();
            if (name.equals(MediaMainActivity.mAutoTestFileDirectorys.get(0))) {
              MediaMainActivity.mAutoTestFileDirectorys.remove(0);
              openDir(file.getAbsolutePath());
              return;
            }
          }
        }
        MtkLog.d(TAG, "searchDesFile has no find directory:"
            + MediaMainActivity.mAutoTestFileDirectorys.get(0));
      } else {
        MtkLog.d(TAG, "searchDesFile mAutoTestFileName:" + MediaMainActivity.mAutoTestFileName);
        for (int i = 0; i < count; i++) {
          file = getListItem(i);
          if (file != null) {
            String name = file.getName();
            if (isDlna) {
              name = name + file.getSuffix();
            }
            // MtkLog.d(TAG, "searchDesFile name:" + name);
            if (name.equals(MediaMainActivity.mAutoTestFileName)) {
              mAdapter.cancel();
              if (mThreadHandler != null) {
                tempFile = file;
                mThreadHandler.post(new Runnable() {

                  @Override
                  public void run() {
                    try {
                      tempFile.stopThumbnail();
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                });
              }
              int contentType = getListContentType();
              PlayList.getPlayList().cleanList(contentType + 1);
              List<FileAdapter> files = new ArrayList<FileAdapter>();
              files.add(file);
              MtkLog.d(TAG, "searchDesFile start to play:");
              playFile(file.getAbsolutePath());
              return;
            }
          }
        }
        MtkLog.d(TAG, "searchDesFile has no find file:");
      }
    }
  }

  @Override
  protected void cancelLoadFiles() {
    if (!mIsListMode) {
      mAdapter.cancel();
    }
    mIsListMode = false;
    super.cancelLoadFiles();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Util.logLife(TAG, "onDestroy");
    vList.setAccessibilityDelegate(null);
  }

  @Override
  protected void onStop() {
    super.onStop();
    LogicManager.getInstance(this).registerAudioPlayListener(null);
    Util.logLife(TAG, "onStop");
    findViewById(R.id.layout_background).setBackground(null);
  }

  // public void exit() {
  // // if(AnimationManager.getInstance().getIsAnimation()){
  // // AnimationManager.getInstance().startActivityEndAnimation(this,
  // // findViewById(R.id.mmp_files_grid_layout), null);
  // // }else{
  // finish();
  // // }
  // }

  private class MtkFilesGridAdapter extends FilesAdapter {
    private static final String TAG = "MtkFilesGridAdapter";
    private static final int MAX_NUM_RNUUNABLE = 300;

    private Drawable mVideoDefault;
    private Drawable mAudioDefault;
    private Drawable mPhotoDefault;
    private Drawable mTextDefault;
    private Drawable mSmbFolder;
    private Drawable mDlnaFolder;
    private Drawable mFolder;
    private Drawable mFailed;
    private int mFolderResId;

    private final Handler mBindHandler;
    private final BitmapCache mCache;
    private final AsyncLoader<Bitmap> mLoader;
    private final ConcurrentHashMap<View, LoadBitmap> mWorks;
    private final ConcurrentHashMap<View, Runnable> mRunnables;
    private final AsyncLoader<String> mInfoLoader;
    private final ConcurrentHashMap<View, LoadInfo> mInfoWorks;

    private AdapterCallback mAdapterCallback;

    @Override
    public void clearWork() {
      // mWorks.clear();
      if (mRunnables.size() > MAX_NUM_RNUUNABLE) {
        mRunnables.clear();
      }
    }

    // private PhotoLoader mPhotoLoader;
    public MtkFilesGridAdapter(Context context, AdapterCallback adapterCallBack) {
      super(context);
      mBindHandler = new Handler();
      mCache = BitmapCache.createCache(false);
      // mLoader = new AsyncLoader<Bitmap>(1);
      mLoader = AsyncLoader.getInstance(1);
      mWorks = new ConcurrentHashMap<View, LoadBitmap>();
      mRunnables = new ConcurrentHashMap<View, Runnable>();
      mInfoLoader = AsyncLoader.getInstance(1);
      mInfoWorks = new ConcurrentHashMap<View, LoadInfo>();
      mAdapterCallback = adapterCallBack;

      // mPhotoLoader = new PhotoLoader(context,
      // R.drawable.mmp_thumbnail_loading_failed_samll);

      prepareDefaultThumbnails(mThumbnailSize);
    }

    private void prepareDefaultThumbnails(int size) {
      if (size == MultiMediaConstant.SMALL) {
        mVideoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_video_small);
        mAudioDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_audio_samll);
        mPhotoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_photo_samll);
        mTextDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_text_samll);
        mFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_middle);
        mSmbFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_smb);
        mDlnaFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_dlna);
        mFailed = mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_samll);
        mFolderResId = R.drawable.mmp_list_icon_selected;
      } else if (size == MultiMediaConstant.MEDIUM) {
        mVideoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_video_middle);
        mAudioDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_audio_middle);
        mPhotoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_photo_middle);
        mTextDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_text_middle);
        mFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_middle);
        mSmbFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_smb);
        mDlnaFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_dlna);
        mFailed = mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_mid);
        mFolderResId = R.drawable.mmp_list_medium_icon_selected;
      } else if (size == MultiMediaConstant.LARGE) {
        mVideoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_video_big);
        mAudioDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_audio_big);
        mPhotoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_photo_big);
        mTextDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_text_big);
        mFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_middle);
        mSmbFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_smb);
        mDlnaFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_dlna);
        mFailed = mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_big);
        mFolderResId = R.drawable.mmp_list_large_icon_selected;
      }
    }

    @Override
    public int getCount() {
      if (mDataList == null) {
        return 0;
      }
      // TODO fixed bug may be bug
      if (mTurnPage) {
        if (mPageSize > 0 && (getListItemsCount() > mPageSize)
            && getListItemsCount() % mPageSize > 0) {
          return super.getCount()
              + (mPageSize - getListItemsCount() % mPageSize);
        }
      }
      // MtkLog.i(TAG, "getCount count =files size");
      return super.getCount();
    }

    @Override
    public FileAdapter getItem(int position) {
      if (position >= getListItemsCount()) {
        return null;
      }

      return super.getItem(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      // return false;
      if (position >= getListItemsCount()) {
        return false;
      }

      return super.isEnabled(position);
    }

    @Override
    protected void updateThumbnail() {
      mCache.clear();
      prepareDefaultThumbnails(mThumbnailSize);
      computePageSize();
      int selection = getListSelectedItemPosition();
      mCurrentPage = computeCurrentPage(selection);
      if (mPageCount == 0) {
        vTopRight.setText("0/0");
      } else {
        vTopRight.setText(String.format("%d/%d", mCurrentPage, mPageCount));
      }
      ((MediaGridView) vList).setNumColumns(getNumberColum());
      // setListAdapter(mAdapter);
      notifyDataSetChanged();
      setListSelection(selection);

    }

    @Override
    protected int getItemLayout() {
      return R.layout.mmp_gridmode_item;
    }

    protected boolean isSamePath(View v, FileAdapter data) {
      ViewHolder holder = (ViewHolder) v.getTag();
      String currentPath = null;
      if (null != data) {
        currentPath = data.getAbsolutePath();
      }
      return null != currentPath && currentPath.equals(holder.path);

    }

    @Override
    protected void initView(View v, FileAdapter data, boolean flag) {
      Log.i(TAG, "initView mItemWidth:" + mItemWidth + "--mItemHeight:" + mItemHeight);
      if (!flag) {
        LayoutParams lp = new LayoutParams(mItemWidth, mItemHeight);
        ViewHolder holder = new ViewHolder();
        if (null != data) {
          holder.path = data.getAbsolutePath();
        }
        holder.img = (ImageView) v.findViewById(R.id.multimedia_gv_img);
        holder.tv = (TextView) v.findViewById(R.id.multimedia_gv_tv);
        holder.layout = (LinearLayout) v.findViewById(R.id.mmp_grid_highlight);
        holder.layout.setLayoutParams(lp);
        v.setTag(holder);
      } else {

        GridView.LayoutParams lp = (GridView.LayoutParams) v.getLayoutParams();
        lp.width = mItemWidth;
        lp.height = mItemHeight;
        v.setLayoutParams(lp);
      }
    }

    @Override
    protected void bindView(View v, FileAdapter data, int position) {
      MtkLog.i(TAG, "bindView start");
      ViewHolder holder = (ViewHolder) v.getTag();
      if (data == null) {
        v.setBackgroundDrawable(null);
        holder.path = null;
        holder.img.setImageDrawable(null);
        holder.tv.setText("");
        holder.layout.setBackgroundDrawable(null);
        return;
      }

      String path = data.getAbsolutePath();
      // MtkLog.d(TAG, "BindView : " + path);

      /*
       * Cancel task when load success, so move to loaded function and the task had
       * add queue, will
       * don't add.
       */
      // cancelLastWork(holder.img);

      MultiFilesManager manager = ((MultiFilesManager) mFilesManager);
      int source = manager.getSourceType(path);
      holder.tv.setText(data.getName());
      holder.tv.setVisibility(View.VISIBLE);
      if (data.isDirectory() ||
          (source == MultiFilesManager.SOURCE_LOCAL && data.isIsoFile())) {
        switch (source) {
          case MultiFilesManager.SOURCE_LOCAL:
            // holder.img.setImageDrawable(mFolder);
            holder.img.setImageResource(mFolderResId);
            break;
          case MultiFilesManager.SOURCE_SMB:
            holder.img.setImageDrawable(mSmbFolder);
            break;
          case MultiFilesManager.SOURCE_DLNA:
            holder.img.setImageDrawable(mDlnaFolder);
            break;
          default:
            break;
        }

        if (holder.tv != null) {
          MtkLog.i(TAG,
              "holder.tv:width:" + holder.tv.getWidth() + "--height:" + holder.tv.getHeight()
                  + "--content:" + holder.tv.getText().toString());
          if (holder.tv.getVisibility() != View.VISIBLE) {
            MtkLog.i(TAG, "holder.tv not visible");
          } else {
            MtkLog.i(TAG, "holder.tv visible");
          }
        }
        holder.layout.setBackgroundDrawable(null);
      } else {
        int type = mFilesManager.getContentType();
        if (type == MultiMediaConstant.TEXT) {
          holder.img.setImageDrawable(mTextDefault);
        } else if (type == MultiMediaConstant.AUDIO) {
          holder.img.setImageDrawable(mAudioDefault);
        } else if (type == MultiMediaConstant.PHOTO) {
          holder.img.setImageDrawable(mPhotoDefault);
        } else if (type == MultiMediaConstant.VIDEO) {
          holder.img.setImageDrawable(mVideoDefault);
        } else if (type == MultiMediaConstant.THRD_PHOTO) {
          holder.img.setImageDrawable(mPhotoDefault);
        }

        if (mCopyedFiles.size() > 0 && mCopyedFiles.contains(path)) {
          holder.layout
              .setBackgroundResource(R.drawable.mmp_gridview_copyed_9);
        } else if (mSelectedFiles.size() > 0 && mSelectedFiles.contains(path)) {
          holder.layout
              .setBackgroundResource(R.drawable.mmp_gridview_selected_9);
        } else {
          holder.layout.setBackgroundDrawable(null);
        }
        if (type == MultiMediaConstant.AUDIO) {
          String suffix = data.getSuffix();
          if (FileSuffixConst.DLNA_FILE_NAME_EXT_PCM.equalsIgnoreCase(suffix)) {
            return;
          }
        }
        // EXO DLNA MARK
        // if (!(Util.isUseExoPlayer()
        // && ((MultiFilesManager) mFilesManager).getCurrentSourceType()
        // != MultiFilesManager.SOURCE_LOCAL)) {
        bindThumbnail(data, holder.img, path, position);
        // }
      }
      MtkLog.i(TAG, "bindView end");
    }

    private void bindThumbnail(FileAdapter data, ImageView view, String path, int position) {

      // if(!Util.isGridActivity(getApplicationContext())){
      // return;
      // }
      Bitmap image = mCache.get(path);
      if (image != null) {
        view.setImageBitmap(Util.getScaledBitmap(image));
        String info = mInforCache.get(data.getAbsolutePath());
        if (info == null && mInfoWorks.get(view) == null) {
          LoadInfo work = new LoadInfo(data, position);
          mInfoWorks.put(view, work);
          mInfoLoader.addWork(work);
        }
      } else {
        MtkLog.i(TAG, "bindThumbnail LoadBitmap!!" + mWorks.get(view) + "  " + path);
        MtkLog.i(TAG, "bindThumbnail position == " + position);
        if (mWorks.get(view) == null) {
          LoadBitmap work = new LoadBitmap(data, view, position);
          mWorks.put(view, work);
          mLoader.addWork(work);
        }
      }
    }

    @Override
    public void stop() {
      // stopThumbnail();
      synchronized (mWorks) {
        mLoader.clearQueue();
        mWorks.clear();
      }
      synchronized (mInfoWorks) {
        mInfoLoader.clearQueue();
        mInfoWorks.clear();
      }
    }

    public void stopFileDecode() {
      // LoadBitmap work = null;
      // Enumeration<View> views = mWorks.keys();
      // ArrayList<View> viewList = Collections.list(views);
      // View view = null;
      //
      // for (int i = 0; i < mWorks.size(); i++) {
      // view = viewList.get(i);
      // work = mWorks.get(view);
      // if (work != null && work.getData() != null) {
      // FileAdapter file = work.getData();
      // if (!file.isDirectory()) {
      // file.stopThumbnail();
      // break;
      // }
      // }
      // }
    }

    private void logCaheSize() {
      ConcurrentHashMap<String, SoftReference<Bitmap>> map = mCache
          .getCache();
      Iterator<Entry<String, SoftReference<Bitmap>>> iterator = map
          .entrySet().iterator();
      int count = 0;
      int recycles = 0;
      while (iterator.hasNext()) {
        SoftReference<Bitmap> ref = iterator.next().getValue();
        if (null != ref) {
          Bitmap value = ref.get();
          if (null != value) {
            count++;
            if (!value.isRecycled()) {
              recycles++;
              value.recycle();
              value = null;
            }
          }
        }
      }

      MtkLog.i(TAG, " count:" + count + " recycles:" + recycles);

    }

    // private void stopThumbnail(){
    //
    // MtkLog.d(TAG, "stopThumbnail : ---------------" );
    // LoadBitmap work = null;
    // Enumeration<View> views= mWorks.keys();
    // ArrayList< View> viewList = Collections.list(views);
    // View view = null;
    // for (int i=0; i < mWorks.size(); i++){
    // view = viewList.get(i);
    // work = mWorks.get(view);
    // work.getData().stopThumbnail();
    // }
    // }

    @Override
    protected void cancel() {
      mLoader.clearQueue();
      mWorks.clear();
      mInfoLoader.clearQueue();
      mInfoWorks.clear();
    }

    private class ViewHolder {
      ImageView img;
      TextView tv;
      LinearLayout layout;
      String path;
    }

    private class LoadInfo implements LoadWork<String> {
      private final FileAdapter mFile;
      private int mPosition;

      public LoadInfo(FileAdapter file, int position) {
        mFile = file;
        mPosition = position;
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
        mAdapterCallback.onMethodCallback(mPosition, mInforCache.get(mFile.getAbsolutePath()));
      }

    }

    private class LoadBitmap implements LoadWork<Bitmap> {
      private final FileAdapter mData;
      private final ImageView vImage;
      private Bitmap mResult;
      private boolean mNeedCache = true;
      private int mPosition = 0;

      public LoadBitmap(FileAdapter data, ImageView iamge, int position) {
        mData = data;
        vImage = iamge;
        mPosition = position;
      }

      public FileAdapter getData() {
        return mData;
      }

      @Override
      public Bitmap load() {
        Bitmap bitmap = null;
        try {
          /*
           * MtkLog.i(TAG, "mThumbnailSize:" + mThumbnailSize);
           * int width = 120;
           * int height = 120; // arcelik customized
           * 
           * if (mThumbnailSize == MultiMediaConstant.MEDIUM) {
           * width = 160;
           * height = 160; // arcelik customized
           * 
           * } else if (mThumbnailSize == MultiMediaConstant.LARGE) {
           * width = 300; // arcelik customized
           * height = 160; // arcelik customized
           * 
           * }
           */
          MtkLog.i(TAG, "mThumbnailSize:" + mThumbnailSize);
          int width = 120;
          int height = 120; // arcelik customized

          if (mThumbnailSize == MultiMediaConstant.MEDIUM) {
            width = 160;
            height = 160;
          } else if (mThumbnailSize == MultiMediaConstant.LARGE) {
            width = 300;
            height = 168;
          }
          // Original
          bitmap = mData.getThumbnail(width, width, true);
          // EXO DLNA MARK
          // bitmap = null;
          if (bitmap == null) {
            if (getListContentType() == MultiMediaConstant.PHOTO) {
              bitmap = ((BitmapDrawable) mFailed).getBitmap();
            } else if (getListContentType() == MultiMediaConstant.THRD_PHOTO) {
              bitmap = ((BitmapDrawable) mFailed).getBitmap();
            }
          }
          /*
           * if (getListContentType() == MultiMediaConstant.PHOTO && bitmap == null) {
           * bitmap =
           * mData.getThumbnail(mThumbnailSize MultiMediaConstant.ZOOM, mThumbnailSize
           * MultiMediaConstant.ZOOM); if (getListContentType() ==
           * MultiMediaConstant.PHOTO &&
           * bitmap == null) { bitmap = ((BitmapDrawable) mFailed).getBitmap(); } }
           */
        } catch (OutOfMemoryError e) {
          MtkLog.e(TAG, "Get Image Thumbnail!!", e);
          bitmap = ((BitmapDrawable) mFailed).getBitmap();
          mNeedCache = false;
        }

        mResult = bitmap;
        MtkLog.d(TAG, "Decode Bitmap : " + mResult);
        return bitmap;
      }

      @Override
      public void loaded(Bitmap result) {
        /* by lei add for optimization */
        String cacheInfo = mData.getCacheInfo();
        if (cacheInfo == null) {
          cacheInfo = mData.getInfo();
        }
        if (cacheInfo != null && mInforCache != null) {
          mInforCache.put(mData.getAbsolutePath(), cacheInfo);
          mAdapterCallback.onMethodCallback(mPosition, cacheInfo);
        }
        if (result == null) {
          int cntType = getListContentType();
          switch (cntType) {
            case MultiMediaConstant.AUDIO:
              mCache.put(mData.getAbsolutePath(),
                  ((BitmapDrawable) mAudioDefault).getBitmap());
              break;
            case MultiMediaConstant.VIDEO:
              mCache.put(mData.getAbsolutePath(),
                  ((BitmapDrawable) mVideoDefault).getBitmap());
              break;
            /*
             * case MultiMediaConstant.PHOTO: mCache.put(mData.getAbsolutePath(),
             * ((BitmapDrawable)
             * mFailed).getBitmap()); break;
             */
            default:
              break;
          }

        } else if (result != null && mNeedCache) {
          mCache.put(mData.getAbsolutePath(), result);
          mWorks.remove(vImage);
        }

        Runnable r = new BindImage();
        mRunnables.put(vImage, r);
        mBindHandler.post(r);
        // loaded a bitmap sleep 100 ms
        // try {
        // Thread.sleep(10);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

      }

      private class BindImage implements Runnable {
        @Override
        public void run() {
          if (mResult != null) {
            // TODO null image
            if (null != vImage.getDrawable()) {
              vImage.setImageBitmap(Util.getScaledBitmap(mResult));
            }
          }
        }
      }
    }
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    LogicManager.getInstance(this).stopAudio();
    stopMusicView();
    if (MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
      finish();
    }
  }
}
