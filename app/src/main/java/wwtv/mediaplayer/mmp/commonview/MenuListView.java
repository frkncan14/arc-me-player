
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.ImagePlayerActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesBaseListActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MusicPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.mmp.util.TextUtils;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

import android.content.res.Resources;

public class MenuListView extends ComDialog implements OnItemSelectedListener,
    OnDismissListener {

  private static final String TAG = "MenuListView";

  private MenuListRightView mdialog;

  private final Context mContext;

  private Resources mResources;

  private final TextUtils mTextUtils;

  private List<MenuFatherObject> mDataList = new ArrayList<MenuFatherObject>();

  ListView.OnItemClickListener mClickListener;

  private final MenuDismissCallBack mCallback;

  private int mIndex = 0;

  public interface MenuDismissCallBack {
    void onDismiss();

    void sendMessage();

    void noDismissPannel();
  }

  private final MenuDismissCallBack mMenuRightCallback = new MenuDismissCallBack() {

    @Override
    public void onDismiss() {

      if (null != mCallback) {
        mCallback.onDismiss();
      }
      dismiss();
    }

    @Override
    public void sendMessage() {
      mHandler.sendEmptyMessageDelayed(MSG_DISMISS_MENU, HIDE_DELAYTIME);
    }

    @Override
    public void noDismissPannel() {
      dismiss();
    }
  };

  public class MyHandler extends Handler {
    @Override
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case MSG_DISMISS_MENU: {

          if (null != mCallback) {
            mCallback.onDismiss();
          }
          dismiss();
          break;
        }
        default:
          break;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public MenuListView(Context context, List<?> list,
      ListView.OnItemClickListener listener, MenuDismissCallBack callback) {
    super(context, list, R.layout.mmp_menulist_item, listener);
    mContext = context;
    mResources = mContext.getResources();
    mClickListener = listener;
    mDataList = (List<MenuFatherObject>) list;
    mTextUtils = TextUtils.getInstance(context);

    mCallback = callback;
    mHandler = new MyHandler();
  }

  public void showSort(boolean show) {
    int index = getItemIndex(mResources.getString(R.string.mmp_menu_sort));
    mDataList.get(mIndex + index).enable = show;
    initList(mDataList);
  }

/*   public void showDelete(boolean show) {
    int index = getItemIndex(mResources.getString(R.string.mmp_menu_delete));

    mDataList.get(mIndex + index).enable = show;
    initList(mDataList);
  }

  public void showCopy(boolean show) {
    int index = getItemIndex(mResources.getString(R.string.mmp_menu_copy));
    mDataList.get(mIndex + index).enable = show;
    initList(mDataList);
  }

  public void showPaste(boolean show) {
    int index = getItemIndex(mResources.getString(R.string.mmp_menu_paste));
    mDataList.get(mIndex + index).enable = show;
    initList(mDataList);
  } */

  public void updateItem(int index, String newText) {
    mDataList.get(mIndex + index).content = newText;
    initList(mDataList);
  }

  public void updateItem(int index, MenuFatherObject obj) {
    mDataList.set(mIndex + index, obj);
    initList(mDataList);
  }

  public MenuFatherObject getItem(int index) {
    return mDataList.get(mIndex + index);
  }

  public void setList(int position1, String content1, boolean hasnext1,
      int position2, String content2, boolean hasnext2, int position3,
      String content3, boolean hasnext3) {
    mDataList.get(position1).content = content1;
    mDataList.get(position2).content = content2;
    mDataList.get(position2).hasnext = hasnext2;

    mDataList.get(position3).content = content3;
    mDataList.get(position3).hasnext = hasnext3;
    initList(mDataList);
  }

  @Override
  public void dismiss() {
    if (null != mdialog && mdialog.isShowing()) {
      mdialog.dismiss();
    }
    super.dismiss();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (mLogicManager == null) {
      mLogicManager = LogicManager.getInstance(mContext);
    }
    super.onCreate(savedInstanceState);
    vListView.setOnKeyListener(new ListView.OnKeyListener() {

      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {

        MtkLog.d(TAG, "onKey");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyMap.KEYCODE_MTKIR_YELLOW:

              return onKeyDown(keyCode, event);

            default:
              mHandler.removeMessages(MSG_DISMISS_MENU);
              mHandler.sendEmptyMessageDelayed(MSG_DISMISS_MENU,
                  HIDE_DELAYTIME);
              break;
          }
        }
        return false;
      }
    });
    vListView.setAccessibilityDelegate(mAccDelegate);
    vListView.setFocusableInTouchMode(true);
    mHandler.sendEmptyMessageDelayed(MSG_DISMISS_MENU, HIDE_DELAYTIME);
  }

  private final int PAGE_SIZE = ListAdapterMenu.PAGE_SIZE;
  private AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {

    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
        AccessibilityEvent event) {
      MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
      do {
        if (!vListView.equals(host)) {
          MtkLog.d(TAG, ":" + vListView + "," + host);
          break;
        }

        List<CharSequence> texts = event.getText();
        if (texts == null) {
          MtkLog.d(TAG, ":" + texts);
          break;
        }

        MtkLog.d(TAG, "event.getEventType()==" + event.getEventType());
        // confirm which item is focus
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {// move focus
          int index = findSelectItem(texts.get(0).toString());
          MtkLog.d(TAG, "index==" + index + ",selectPosition==" + selectPosition);
          if (index >= 0) {
            int selectionIndex = index % PAGE_SIZE;
            if (mDataList.get(selectionIndex).enable) {
              vListView.setSelection(selectionIndex);
            }
            selectPosition = index;
          }
          if (null != mHandler) {
            mHandler.removeMessages(MSG_DISMISS_MENU);
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_MENU, HIDE_DELAYTIME);
          }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {// select item
          MtkLog.d(TAG, "click item selectPosition:" + selectPosition);
          if (mDataList.get(selectPosition).enable) {
            checkHasNext();
          }

        }

      } while (false);

      return true;// host.onRequestSendAccessibilityEventInternal(child, event);
    }

    private int findSelectItem(String text) {
      if (mDataList == null) {
        return -1;
      }

      for (int i = 0; i < mDataList.size(); i++) {
        if (mDataList.get(i).equals(text)) {
          return i;
        }
      }

      return -1;
    }
  };

  public void hideMenuDelay() {
    mHandler.removeMessages(MSG_DISMISS_MENU);
    mHandler.sendEmptyMessageDelayed(MSG_DISMISS_MENU, HIDE_DELAYTIME);
  }

  private LogicManager mLogicManager;

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {

    if (mLogicManager == null) {
      mLogicManager = LogicManager.getInstance(mContext);
    }
    if (mLogicManager.isAudioOnly()) {
      mLogicManager.setAudioOnly(false);
      return true;
    }
    int keyCode = event.getKeyCode();
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (keyCode) {
        case KeyMap.KEYCODE_VOLUME_DOWN:
        case KeyMap.KEYCODE_VOLUME_UP:
        case KeyMap.KEYCODE_MTKIR_MUTE: {
          if (mContext instanceof MediaPlayActivity) {
            ((MediaPlayActivity) mContext).onKeyDown(keyCode, event);
          } else if (mContext instanceof MtkFilesBaseListActivity) {
            ((MtkFilesBaseListActivity) mContext).onKeyDown(keyCode,
                event);
          }
          return true;
        }
        default:
          break;
      }
    }
    return super.dispatchKeyEvent(event);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.d(TAG, "onKeyDown");
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_RIGHT:
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
      case KeyMap.KEYCODE_MTKIR_YELLOW:

      {
        MtkLog.d(TAG, "onKeyDown" + keyCode);
        return checkHasNext();
      }
      case KeyMap.KEYCODE_MENU: {
        dismiss();
        break;
      }
      // case KeyMap.KEYCODE_MTKIR_MUTE:
      // return onMute();

      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  private boolean checkHasNext() {
    final MenuFatherObject mnuFObj = mDataList.get(selectPosition);
    if (mnuFObj.hasnext) {
      mHandler.removeMessages(MSG_DISMISS_MENU);
      selectView = vListView.getSelectedView();
      if (selectView != null) {
        selectView.findViewById(R.id.mmp_menulist_img).setVisibility(
            View.VISIBLE);
        ((TextView) selectView.findViewById(R.id.mmp_menulist_tv))
            .setTextColor(Color.GREEN);
      }
      String content = mnuFObj.content;
      int type = getMediaType();
      List<MenuFatherObject> menuList;
      if (MenuFatherObject.MENU_INVALID_ID == mnuFObj.getId()) {
        menuList = GetDataImp.getInstance()
            .getChildList(mContext, content);
      } else {
        menuList = GetDataImp.getInstance()
            .getChildListEx(mContext, content, mnuFObj.getId());
      }
      mdialog = new MenuListRightView(mContext, menuList,
          mClickListener, mMenuRightCallback);
      mdialog.setOnDismissListener(this);

      mdialog.setMediaType(type);

      mdialog.setSelection(getSelection(content, menuList));
      mdialog.show();
      mdialog.setWindowPosition();

      return true;
    } else {
      return false;
    }
  }

  boolean onMute() {
    if (mediaPlayActivity != null) {
      mediaPlayActivity.onMute();
      return true;
    }
    return false;
  }

  public int getSelection(String content, List<MenuFatherObject> menuList) {

    if (content.equals(mContext.getResources().getString(
        R.string.mmp_menu_picturemode))) {
      return mLogicManager.getCurPictureMode();
    } else if (content.equals(mContext.getResources().getString(
        R.string.mmp_menu_screenmode))) {
      return mLogicManager.getCurScreenMode();
      /*
       * int[] modes = mLogicManager.getAvailableScreenMode();
       * int curMode = mLogicManager.getCurScreenMode();
       * if (curMode == CommonSet.VID_SCREEN_MODE_CUSTOM_DEF_1) {
       * curMode = CommonSet.VID_SCREEN_MODE_NORMAL;
       * }
       * int position = -1;
       * for (int i = 0, length = modes.length; i < length; i++) {
       * if (modes[i] >= 0) {
       * position++;
       * if (modes[i] == curMode) {
       * return position;
       * }
       * }
       * }
       * return 0;
       */
    }

    // Fix DTV00352617 bug

    else if (content.equals(mContext.getResources().getString(
        R.string.mmp_menu_repeat))) {
      int mediaType = MediaPlayActivity.getMediaType();
      int filterType;
      switch (mediaType) {
        case MultiMediaConstant.AUDIO: {
          filterType = Const.FILTER_AUDIO;
          break;
        }
        case MultiMediaConstant.VIDEO: {
          filterType = Const.FILTER_VIDEO;
          break;
        }
        case MultiMediaConstant.PHOTO: {
          filterType = Const.FILTER_IMAGE;
          break;
        }
        case MultiMediaConstant.TEXT: {
          filterType = Const.FILTER_TEXT;
          break;
        }
        default:
          filterType = 0;
          break;
      }
      int repeat = mLogicManager.getRepeatModel(
          filterType);

      if (repeat == Const.REPEAT_NONE) {
        return 0;
      } else if (repeat == Const.REPEAT_ONE) {
        return 1;
      } else if (repeat == Const.REPEAT_ALL) {
        return 2;
      }
    }
    // add by xudong chen 20111204 fix DTV00379662
    else if (content.equals(mContext.getString(R.string.mmp_menu_display))) {
      int perLine = MediaPlayActivity.getPerLine();
      if (perLine == MusicPlayActivity.OFFLINE) {
        return 0;

      } else if (perLine == MusicPlayActivity.SINGLINE) {
        return 1;

      } else if (perLine == MusicPlayActivity.MULTILINE) {
        return 2;

      }
    }
    // end

    else if (content.equals(mContext.getString(R.string.mmp_menu_duration))) {
      int time = 0;
      if (mContext instanceof PhotoPlayActivity) {

        time = PhotoPlayActivity.getDelayedTime();

      } else if (mContext instanceof Photo4K2KPlayActivity) {
        time = Photo4K2KPlayActivity.getDelayedTime();
      } else if (mContext instanceof ImagePlayerActivity) {
        time = ImagePlayerActivity.getDelayedTime();
      }

      if (time == MediaPlayActivity.DELAYED_SHORT) {
        return 0;

      } else if (time == MediaPlayActivity.DELAYED_MIDDLE) {
        return 1;

      } else if (time == MediaPlayActivity.DELAYED_LONG) {
        return 2;

      }
    } else if (content.equals(mContext.getString(R.string.mmp_menu_rotate))) {
      if (mContext instanceof ImagePlayerActivity) {
        return ImagePlayerActivity.getRotateMode();
      }
    } else if (content.equals(mContext.getString(R.string.mmp_menu_effect))) {
      int effect = mLogicManager.getImageEffect();
      switch (effect) {
        case ConstPhoto.DEFAULT: {
          return 0;
        }
        case ConstPhoto.dissolve: {
          return 1;
        }
        case ConstPhoto.wipe_right: {
          return 2;
        }
        case ConstPhoto.wipe_left: {
          return 3;
        }
        case ConstPhoto.wipe_top: {
          return 4;
        }
        case ConstPhoto.wipe_bottom: {
          return 5;
        }
        case ConstPhoto.box_in: {
          return 6;
        }
        case ConstPhoto.box_out: {
          return 7;
        }
        case ConstPhoto.RADNOM: {
          return 8;
        }

        default:
          break;
      }
    }

    else if (content.equals(mContext.getString(R.string.mmp_menu_shuffle))) {
      int fileType;
      switch (mMediaType) {
        case MultiMediaConstant.VIDEO:
          fileType = Const.FILTER_VIDEO;
          break;
        case MultiMediaConstant.AUDIO:
          fileType = Const.FILTER_AUDIO;
          break;
        case MultiMediaConstant.PHOTO:
          fileType = Const.FILTER_IMAGE;
          break;
        case MultiMediaConstant.TEXT:
          fileType = Const.FILTER_TEXT;
          break;
        default:
          fileType = 0;
          break;
      }
      boolean isShuffle = mLogicManager.getShuffleMode(fileType);
      return isShuffle ? 1 : 0;
    }

    else if (content.equals(mContext.getString(R.string.mmp_menu_sort))) {
      int sortType = MultiFilesManager.getInstance(mContext)
          .getSortType();
      int contentType = MultiFilesManager.getInstance(mContext)
          .getContentType();

      switch (contentType) {
        case MultiMediaConstant.PHOTO:

          if (sortType == FilesManager.SORT_BY_NAME) {
            return 2;
          } else if (sortType == FilesManager.SORT_BY_TYPE) {
            return 0;
          } else if (sortType == FilesManager.SORT_BY_DATE) {
            return 1;
          }
          break;
        case MultiMediaConstant.TEXT: {
          if (sortType == FilesManager.SORT_BY_NAME) {
            return 1;
          } else if (sortType == FilesManager.SORT_BY_DATE) {
            return 0;
          }
          break;
        }
        case MultiMediaConstant.AUDIO: {
          if (sortType == FilesManager.SORT_BY_NAME) {
            return 2;
          } else if (sortType == FilesManager.SORT_BY_DATE) {
            return 1;
          } else if (sortType == FilesManager.SORT_BY_TYPE) {
            return 0;
          }
          break;
        }
        case MultiMediaConstant.VIDEO:
          if (sortType == FilesManager.SORT_BY_NAME) {
            return 2;
          } else if (sortType == FilesManager.SORT_BY_DATE) {
            return 1;
          } else {
            return 0;
          }
        case MultiMediaConstant.THRD_PHOTO:
          if (sortType == FilesManager.SORT_BY_NAME) {
            return 1;
          } else if (sortType == FilesManager.SORT_BY_DATE) {
            return 0;
          }
          break;
        default:
          break;
      }
    }

    else if (content
        .equals(mContext.getString(R.string.mmp_menu_mediatype))) {
      MtkLog.i(TAG, "----------mMediaType " + mMediaType);
      int mediaType = MultiFilesManager.getInstance(mContext)
          .getContentType();
      switch (mediaType) {
        case FilesManager.CONTENT_PHOTO:
          return 1;
        case FilesManager.CONTENT_AUDIO:
          return 2;
        case FilesManager.CONTENT_VIDEO:
          return 0;
        case FilesManager.CONTENT_TEXT:
          return 3;
        default:
          break;
      }
    }

    else if (content.equals(mContext.getString(R.string.mmp_menu_thumb))) {
      int size = ((MtkFilesBaseListActivity) mContext).mThumbnailSize;
      switch (size) {
        case MultiMediaConstant.SMALL:
          return 0;
        case MultiMediaConstant.MEDIUM:
          return 1;
        case MultiMediaConstant.LARGE:
          return 2;
        default:
          break;
      }
    } else if (content.equals(mContext.getString(R.string.mmp_menu_size))) {
      return mTextUtils.getFontSizeIndex();// LogicManager.getInstance(mContext).getFontSize();
    } else if (content.equals(mContext.getString(R.string.mmp_menu_style))) {
      return mTextUtils.getFontStyleIndex();// LogicManager.getInstance(mContext).getFontStyle();
    } else if (content.equals(mContext.getString(R.string.mmp_menu_color))) {
      return mTextUtils.getFontColorIndex();// LogicManager.getInstance(mContext).getFontColor();
    } else if (content.equals(mContext.getString(R.string.mmp_menu_zoom))) {
      int size = mLogicManager.getPicCurZoom();
      Log.i(TAG, "size:" + size);
      switch (size) {
        case ConstPhoto.ZOOM_1X:
          return 0;
        case ConstPhoto.ZOOM_2X:
          return 1;
        case ConstPhoto.ZOOM_4X:
          return 2;
        default:
          return 0;
      }
    }
    // Added by Dan for fix bug DTV00389362
    else if (content.equals(mContext.getString(R.string.mmp_menu_timeoffset))) {
      return mLogicManager.getLrcOffsetMode();
    } else if (content.equals(mContext.getString(R.string.mmp_menu_encoding))) {
      return mLogicManager.getLrcEncodingMode();
    } else if (content.equals(mContext.getString(R.string.mmp_last_memory))) {
      int type = SaveValue.getInstance(mContext).readValue(LastMemory.LASTMEMORY_ID);
      if (type == LastMemory.LASTMEMORY_OFF) {
        return 0;
      }
      if (type == LastMemory.LASTMEMORY_TIME) {
        return 1;
      }
      if (type == LastMemory.LASTMEMORY_POSITION) {
        return 2;
      }

    } else if (content.equals(mContext.getString(R.string.mmp_menu_ts_program))) {
      return mLogicManager.getTSProgramIndex();
    } else if (content.equals(mContext.getString(R.string.menu_audio_sound_tracks))) {
      int index = mLogicManager.getAudioTrackIndex();
      MtkLog.i(TAG, "soundtracks index:" + index);
      return index;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_subtitle_encoding))) {
      String encodingType = mLogicManager.getSubtitleEncodingType();
      String[] encodingArray = mContext.getResources().getStringArray(R.array.mmp_subtitle_encoding_array);
      MtkLog.i(TAG, "Subtitle Encoding: " + encodingType);
      for (int i = 0; i < encodingArray.length; i++) {
        if (encodingType.equals(encodingArray[i])) {
          return i;
        }
      }
      return 0;
    } else if (content.equals(mContext.getString(R.string.mmp_seamless_mode))) {
      if (mLogicManager.getSeamlessMode()) {
        return 1;
      } else {
        return 0;
      }
    }

    return 0;
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    MtkLog.d(TAG, "mdialog is dismiss");
    init();

  }

  public void removeDataItem(int index) {
    if (index < mDataList.size()) {
      mDataList.remove(index);
    }
  }

  public void removeItem(int index) {
    mDataList.remove(index);
    // mIndex--;
    initList(mDataList);
  }

  // Added by Dan for fix bug DTV00379191
  public void setFirstIndex(int index) {
    mIndex = index;
  }

  MediaPlayActivity mediaPlayActivity;

  public void mControlView(MediaPlayActivity mediaPlayActivity) {

    this.mediaPlayActivity = mediaPlayActivity;
  }

  // Added by Dan for fix bug DTV00389330
  public boolean isInLrcOffsetMenu() {
    return GetDataImp.getInstance().isInLrcOffsetMenu();
  }

  public boolean isInEncodingMenu() {
    return GetDataImp.getInstance().isInEncodingMenu();
  }
}
