
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.wwtv.mediaplayer.mmp.util.ThreadUtil;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmp.util.EffectViewNative;
import com.mediatek.wwtv.mediaplayer.mmp.util.EffectViewNative.ImagePlay;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoCompletedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoDecodeListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.SleepDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.EffectViewNative.ICompleteListener;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager.ImageLoad;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.PhotoRender.PhotoRender;

//import com.mediatek.wwtv.mediaplayer.capturelogo.CaptureLogoActivity;

public class Photo4K2KPlayActivity extends MediaPlayActivity {

  private static final String TAG = "Photo4K2KPlayActivity";

  private static final int MESSAGE_PLAY = 0;

  private static final int MESSAGE_POPHIDE = 1;

  private static final int MESSAGE_PHOTOMODE = 2;

  private static final int MESSAGE_HIDDLE_MESSAGE = 3;

  private static final int MESSAGE_HIDDLE_FRAME = 4;

  private static final int MESSAGE_NO_PHOTO_FRAME = 5;
  private static final int MESSAGE_DECODE_FAILURE = 6;
  private static final int FRAME_ONEPHOTO_MODE = 7;
  private static final int FRAME_MODE_HIDE = 8;

  private static final int MESSAGE_POPSHOWDEL = 10000;

  public static final int DELAYED_FRAME = 1000;
  private boolean isPhotoActivityLiving = true;

  public static int mDelayedTime = DELAYED_SHORT;

  private static int oriention = 0;

  private static int newOriention = 0;

  private EffectViewNative vShowView;

  private LinearLayout vLayout;

  private MenuListView menuDialog;

  private MenuListView menuDialogSleepTime;

  private ImageManager mImageManager;

  private Resources mResources;

  private int playMode;

  private PhotoUtil mCurBitmap;

  private int isRepeatMode = 0;

  private int menu_repeatmode = 0;// add by haixia

  //private SharedPreferences mPreferences;

  public static final String PHOTO_FRAME_PATH = "photoframe";

  public static final String PHOTO_FRAME_KEY = "photo";
  public static final String PLAY_MODE = "PlayMode";

  private int mImageSource = 0;

  private boolean isStop = false;
  private boolean isPause = false;

  public static final int NORMAL_MODE = 0;
  public static final int FRAME_ONE_PHOTO_MODE = 1;
  public static final int FRAME_ALL_PHOTO_MODE = 2;

  private boolean is4K2KFlag;

  private PhotoRender photoRender;

  private SleepDialog mSleepdialog;

  private Thread mRotateThread;

  private final ICompleteListener mCompleteListener = new ICompleteListener() {

    @Override
    public void drawEnd() {
      // TODO Auto-generated method stub
      handBack();
      handlePhotoPlayEnd();
    }

  };

  private final OnItemClickListener mListener = new OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {

      isRepeatMode = parent.getChildCount();
      TextView tvTextView = (TextView) view
          .findViewById(R.id.mmp_menulist_tv);
      String content = tvTextView.getText().toString();
      controlState(content);
    }
  };

  private class SleepingDialog extends Dialog {

    private TextView mSleepTime;
    private Context msContext;
    private Timer timer;
    private TimerTask task;
    private int timeInt = 5;

    private SleepingDialog(Context context, int theme) {
      super(context, theme);
      this.msContext = context;
    }

    public SleepingDialog(Context context) {
      this(context, R.style.dialog);
      this.msContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.mmp_framephoto_sleep);
      setDialogPosition();
      initData();
      timerTask();
    }

    private void initData() {
      mSleepTime = (TextView) findViewById(R.id.mmp_framephoto_sleeptime);

    }

    private void timerTask() {
      timer = new Timer("Chang");
      task = new TimerTask() {
        @Override
        public void run() {
          if (timeInt <= 0) {
            SleepingDialog.this.dismiss();
          }
          timeInt--;
        }
      };
      timer.schedule(task, 10, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      MtkLog.i(TAG, "onKeyDown keycode:" + keyCode);
      keyCode = KeyMap.getKeyCode(keyCode, event);
      MtkLog.i(TAG, "onKeyDown later keycode:" + keyCode);

      switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_SLEEP:
          Log.i(TAG, "SLEEP");
          if (isValid()) {
            Log.i(TAG, "SLEEP VALIDE");
            setSleepTime();
          }
          break;
        case KeyMap.KEYCODE_BACK:
          // case KeyMap.KEYCODE_MTKIR_ANGLE:
        case KeyMap.KEYCODE_MTKIR_GUIDE:
          if ( msContext instanceof Photo4K2KPlayActivity) {
            ((Photo4K2KPlayActivity) msContext).onKeyDown(keyCode, event);
          }
          this.dismiss();
          break;
        case KeyMap.KEYCODE_MENU:
          if (msContext instanceof Photo4K2KPlayActivity) {
            ((Photo4K2KPlayActivity) msContext).onKeyDown(keyCode, event);
          }
		  break;
        default:
          break;
      }
	   return true;
    }

    private void setDialogPosition() {
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.x = -(int) (ScreenConstant.SCREEN_WIDTH * 0.35);
      lp.y = (int) (ScreenConstant.SCREEN_HEIGHT * 0.25);
      window.setAttributes(lp);

    }

  }

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      // fix CR DTV00375799
      if (isPause == true) {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
        isPause = false;
      } else
      {
        mHandler.sendEmptyMessage(MESSAGE_PLAY);
      }
    }

    @Override
    public void pause() {
      isPause = true;
      mHandler.removeMessages(MESSAGE_PLAY);
    }

  };
  private final ImageLoad mLoad = new ImageLoad() {

    @Override
    public void imageLoad(PhotoUtil bitmap) {
          loadImageDone(bitmap);
    }
  };

  private final ImagePlay mPlay = new ImagePlay() {

    @Override
    public void playDone() {
      MtkLog.d(TAG, "ImagePlay playDone ~");
      isNotSupport = false;
      if (vShowView != null) {
        vShowView.recycleLastBitmap();
      }
      Log.i(TAG, "isStop:" + isStop + "--mControlView:" + mControlView
          + "--isNotSupport:" + isNotSupport);
      if (mControlView != null) {
        Log.i(TAG, "--isPlaying:" + mControlView.isPlaying());
      }
      if (!isStop && mControlView != null && mControlView.isPlaying()) {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
      }
    }

    @Override
    public void playError() {

      MtkLog.d(TAG, "ImagePlay playError ~");
      isNotSupport = true;
      if (isStop) {
        return;
      }
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);

    }

  };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_mediaplay);
    Const.isTransfBitmaping = false;
    getScreenWH();
    // mPreferences = getSharedPreferences(PHOTO_FRAME_PATH, MODE_PRIVATE);
    findView();
    getIntentData();
    // add by keke for fix DTV00380644
    mControlView.setRepeatVisibility(Const.FILTER_IMAGE);

    String dataStr = getIntent().getDataString();
    if ((dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      if (autoTest(Const.FILTER_IMAGE, MultiFilesManager.CONTENT_PHOTO)) {
        mImageSource = ConstPhoto.LOCAL;
        playMode = 0;

      }
    }

    initShowPhoto();
    int repeatMode = getIntent().getIntExtra("repeatMode", 0);
    MtkLog.d(TAG,"onCreate,repeatMode=="+repeatMode);
    if (1 == repeatMode){
        mLogicManager.setRepeatMode(Const.FILTER_IMAGE,
            Const.REPEAT_ONE);
    }
    setRepeatMode();
    Util.logLife(TAG, "onCreate");
  }

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case MESSAGE_PLAY:
          mCurBitmap = null;
          if (playMode == FRAME_ONE_PHOTO_MODE) {
            mImageManager.load(Const.CURRENTPLAY);
          }
          else {
            // vShowView.setInterrupted(true);
            mImageManager.load(Const.AUTOPLAY);
          }
          break;
        case MESSAGE_POPHIDE:
          MtkLog.d(TAG, "MESSAGE_POPHIDE:" + msg.what);
          if (menuDialog != null && menuDialog.isShowing()) {
            if (mHandler.hasMessages(MESSAGE_POPHIDE)) {
              mHandler.removeMessages(MESSAGE_POPHIDE);
            }
            sendEmptyMessageDelayed(MESSAGE_POPHIDE, 3000);
            break;
          }
          hideController();
          break;
        case MESSAGE_PHOTOMODE:
          break;
        case MESSAGE_HIDDLE_MESSAGE: {
          dismissNotSupprot();
          break;
        }
        case MESSAGE_HIDDLE_FRAME: {
          mResources = Photo4K2KPlayActivity.this.getResources();
          String photoFrame = mResources
              .getString(R.string.mmp_menu_photo_frame);
          if (mTipsDialog != null && mTipsDialog.isShowing()
              && mTipsDialog.getTitle().equals(photoFrame)) {
            mTipsDialog.dismiss();
          }
          dismissNotSupprot();
          if (isNotSupport == true && isPhotoActivityLiving == true) {
            try {
				if(photoRender != null) {
					photoRender.deinitPhotoPlay();
					photoRender.initPhotoPlay();
				}
              featureNotWork(Photo4K2KPlayActivity.this.getResources()
                  .getString(R.string.mmp_photo_type_notsupport));
            } catch (Exception ex) {
              ex.printStackTrace();
            }

            vShowView.clearScreen();
          }
          break;
        }
        case MESSAGE_NO_PHOTO_FRAME: {
          if (mTipsDialog != null && mTipsDialog.isShowing()) {
            mTipsDialog.dismiss();
          }
          if (isPhotoActivityLiving == true) {
			  if(photoRender != null) {
				photoRender.deinitPhotoPlay();
				photoRender.initPhotoPlay();
			}
            featureNotWork(Photo4K2KPlayActivity.this.getResources().getString(
                R.string.mmp_photo_type_notsupport));
          }
          break;
        }
        case MESSAGE_DECODE_FAILURE:
			if(photoRender != null) {
				photoRender.deinitPhotoPlay();
				photoRender.initPhotoPlay();
            }
          featureNotWork(Photo4K2KPlayActivity.this.getResources()
              .getString(R.string.mmp_photo_type_notsupport));
          vShowView.clearScreen();
          break;
        case FRAME_ONEPHOTO_MODE:
          if (null == mCurBitmap) {
            showPhotoFrameInfo(getString(R.string.mmp_toast_no_photoframe));
            MtkLog.d(TAG,"FRAME_ONEPHOTO_MODE, mPhotoFramePath =="+mPhotoFramePath);
            if (null != mPhotoFramePath && mPhotoFramePath.length() > 0) {
              mHandler.sendEmptyMessageDelayed(MESSAGE_NO_PHOTO_FRAME,
                  DELAYED_FRAME);
            }
            return;
          }
          loadImageDone(mCurBitmap);
          return;
        case FRAME_MODE_HIDE:
          if (mSleepDialog != null) {
            if (mSleepDialog.isShowing()) {
              mSleepDialog.dismiss();
            }
          }
          return;
        default:
          break;
      }
    }

  };

  private SleepDialog mSleepDialog;

  private void setSleepTime() {
    // TODO Auto-generated method stub
    if (mSleepDialog == null) {
      Log.i(TAG, "mSleepDialog == NULL");
      mSleepDialog = new SleepDialog(this);
    }
    mHandler.removeMessages(FRAME_MODE_HIDE);
    mSleepDialog.show();
    mHandler.sendEmptyMessageDelayed(FRAME_MODE_HIDE, 5000);
    mSleepDialog.updateValue(true);
  }

  /**
   *  find view
   */
  private void findView() {
    vShowView = new EffectViewNative();
    vShowView.setPlayLisenter(mPlay);
    vLayout = (LinearLayout) findViewById(R.id.mmp_mediaplay);
    vLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    getPopView(R.layout.mmp_popupphoto, MultiMediaConstant.PHOTO,
        mControlImp);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();

    if (isBackFromCapture) {
      if (mControlView != null && !mControlView.isShowed()) {
        reSetController();
      }
      isBackFromCapture = false;
    } else {
      if (mImageManager != null && playMode != FRAME_ONE_PHOTO_MODE) {
        mImageManager.load(Const.CURRENTPLAY);
      }
    }

    if(photoRender != null && isDeinit) {
      photoRender.initPhotoPlay();
    }
    isStop = false;
    MtkLog.d(TAG, "onResume");
  }

  @Override
  protected void onPause() {
    //remove for DTV01930298
    /*
        if (isBackFlag) {
          Util.LogResRelease("onPause 1 deinitPhotoPlay");
          photoRender.deinitPhotoPlay();
          is4K2KFlag = false;
        }
        Log.i(TAG, "xiuqin test");
        if (Util.isNeedEndPhotoPlayWhenPause() && !isBackFlag) {
          Util.LogResRelease("onPause 2 deinitPhotoPlay");
          photoRender.deinitPhotoPlay();
          is4K2KFlag = false;
        } else {
          Util.setEndPhotoPlayWhenPause(true);
        }
        */

    if (playMode == FRAME_ALL_PHOTO_MODE){
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, menu_repeatmode);
    	}
    super.onPause();
    MtkLog.d(TAG, "onPause");
  }

  private void getIntentData() {
    Bundle bundle = getIntent().getExtras();
    if (null != bundle) {
      playMode = bundle.getInt(PLAY_MODE);
    }
    mImageSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();

    switch (mImageSource) {
      case MultiFilesManager.SOURCE_LOCAL:
        mImageSource = ConstPhoto.LOCAL;
        break;
      case MultiFilesManager.SOURCE_SMB:
        mImageSource = ConstPhoto.SAMBA;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        mImageSource = ConstPhoto.DLNA;
        break;
      default:
        break;
    }
  }

  private final OnPhotoCompletedListener mPhotoCompleteListener = new OnPhotoCompletedListener() {

    @Override
    public void onComplete() {
      playToEnd();
    }
  };

  private void playToEnd() {
    if (null != mImageManager) {
      mImageManager.finish();
    }
    vShowView.setCompleteListener(mCompleteListener);
    vShowView.setInterrupted(true);
  }

  private final OnPhotoDecodeListener mPhotoDecodeListener = new OnPhotoDecodeListener() {

    @Override
    public void onDecodeFailure() {
      Log.i(TAG, "onDecodeFailure isNotSupport:" + isNotSupport + "--isStop:" + isStop,new Throwable());
      vShowView.resetType();
      isNotSupport = true;
      if (isStop) {
        return;
      }
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);

    }

    @Override
    public void onDecodeSuccess() {
      MtkLog.i(TAG,"onDecodeSuccess");
      vShowView.resetType();
      isNotSupport = false;
    }
  };

  /**
   * Initialize photo play
   */
  private void initShowPhoto() {
    mResources = this.getResources();
    mLogicManager = LogicManager.getInstance(this);

    vShowView.setRotate(0);

    Display display = getWindowManager().getDefaultDisplay();
    mLogicManager.initPhotoFor4K2K(display, vShowView);
    mLogicManager.initRotate();
    photoRender = new PhotoRender(0);

    vShowView.setRender(photoRender);
    if (photoRender.initPhotoPlay() != 0)
    {
      MtkLog.d(TAG, "initShowPhoto 4k2k native init fail~");
      isNotSupport = false;
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);
      if (null != mImageManager) {
        mImageManager.finish();
      }
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          finish();
        }
      }, 1000);

      return;
    }

    int dw = 1920;
    int dh = 1080;

    if (Util.PHOTO_8K4K2K == 3) {
        dw = 7680;
        dh = 4320;
    } else if (Util.PHOTO_8K4K2K == 2){
        dw = 3840;
        dh = 2160;
    } else if(Util.PHOTO_8K4K2K == 4 ){ // 1366*768  CR:DTV03456804
      dw = ConstPhoto.MAX_HD_WIDTH;
      dh = ConstPhoto.MAX_HD_HEIGHT;
    }
    vShowView.setWindow(dw, dh);

    is4K2KFlag = true;
    mLogicManager.setPhotoCompleteListener(mPhotoCompleteListener);
    if (playMode != FRAME_ONE_PHOTO_MODE) {
      mLogicManager.setPhotoDecodeListener(mPhotoDecodeListener);
    }
    mImageManager = ImageManager.getInstance();
    // add by xiaojie fix cr DTV00390950
    if (playMode != FRAME_ONE_PHOTO_MODE) {
      mImageManager.setImageLoad(mLoad, mLogicManager);
    }

    mControlView.setPhotoAnimationEffect(vShowView.getEffectValue());
    mCurBitmap = null;

    if (playMode == NORMAL_MODE) {
      showPopUpWindow(vLayout);
      hideControllerDelay();
      setControlView();
    } else if (playMode == FRAME_ONE_PHOTO_MODE) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          // TODO Auto-generated method stub
          /*
           * String playPath = getSharedPreferences(PHOTO_FRAME_PATH,
           * MODE_PRIVATE).getString(PHOTO_FRAME_KEY, "");
           */

          mCurBitmap = mLogicManager.transfBitmap(mPhotoFramePath, mPhotoFrameSource);
          mHandler.sendEmptyMessage(FRAME_ONEPHOTO_MODE);
        }
      }).start();
      return;
    } else {
      mImageSource = ConstPhoto.LOCAL;
      menu_repeatmode = mLogicManager.getRepeatModel(Const.FILTER_IMAGE);// add by haixia for fix CR
                                                                         // DTV00379219
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ALL);
      mLogicManager.setShuffle(Const.FILTER_IMAGE, Const.SHUFFLE_OFF);
    }
    mLogicManager.setImageSource(mImageSource);

  }

  /**
   * Set control bar info
   */
  private void setControlView() {
    if (mControlView != null) {
      // TODO remove
      if (isNotSupport) {
        if (null != mControlView) {
          mControlView.setZoomEmpty();
        }
      } else {
        if (null != mControlView) {
          mControlView.setPhotoZoomSize();
        }
      }
      if (null != mControlView) {
        mControlView.setRepeat(Const.FILTER_IMAGE);
      }
      if (null != mControlView) {
        mControlView.setFileName(mLogicManager
            .getCurrentFileName(Const.FILTER_IMAGE));
      }
      if (null != mControlView) {
        mControlView.setFilePosition(mLogicManager.getImagePageSize());
      }
    }
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setPhotoView();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.d(TAG, "onKeyDown keyCode:" + keyCode);
    keyCode = KeyMap.getKeyCode(keyCode, event);
    MtkLog.i(TAG, "onKeyDown later keycode:" + keyCode);

    if (playMode == FRAME_ALL_PHOTO_MODE
          || playMode == FRAME_ONE_PHOTO_MODE/* ||isNotSupport */) {

      if (keyCode == KeyMap.KEYCODE_BACK) {
        finish();
      } else if (keyCode != KeyMap.KEYCODE_MTKIR_SLEEP) {
        return true;
      }
    }

    if (mRotateThread != null && mRotateThread.isAlive()) {
      // toast rotate is alive.
      Util.showToast(getApplicationContext(), "Rotate is alive, please wait.");
      return true;
    }

    switch (keyCode) {
      case KeyMap.KEYCODE_MENU: {
        reSetController();
        showDialog();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_CHDN: {
        if (isValid()) {
          isNotSupport = false;
          reSetController();
          mHandler.removeMessages(MESSAGE_PLAY);
          mCurBitmap = null;
          vShowView.setNotifyOnce();
          mImageManager.load(Const.MANUALPRE);
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_CHUP: {
        if (isValid()) {
          MtkLog.i(TAG, "KEYCODE_MTKIR_CHUP START");
          isNotSupport = false;
          reSetController();
          mHandler.removeMessages(MESSAGE_PLAY);
          mCurBitmap = null;
          vShowView.setNotifyOnce();
          mImageManager.load(Const.MANUALNEXT);
          MtkLog.i(TAG, "KEYCODE_MTKIR_CHUP END");
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        reSetController();
        onRepeat();
        updateInfoView();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_RECORD: {

        featureNotWork(getString(R.string.mmp_featue_notsupport));
        return true;
        /*
         * if (isNotSupport){ break; } if (mCurBitmap == null){
         * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } //
         * mLogicManager.setCapturer(vShowView); if(null != mControlView){
         * if(mControlView.isPlaying()){ mControlView.onCapture();
         * mControlView.setPlayIcon(View.INVISIBLE); photoPlayStatus=true; }else{
         * mControlView.setPauseIcon(View.INVISIBLE); photoPlayStatus=false; } } hideController();
         * Intent intent = new Intent(this, CaptureLogoActivity.class);
         * intent.putExtra(CaptureLogoActivity.FROM_MMP, CaptureLogoActivity.MMP_PHOTO);
         * startActivity(intent); isBackFromCapture = true; return true;
         */
      }

      case KeyMap.KEYCODE_DPAD_RIGHT:
        if (!isValid()) {
          return true;
        }
        if (vShowView.getZoomScale() <= 1.0f) {
          return true;
        }

        vShowView.setType(ConstPhoto.MOVE_RIGHT);
        new Thread(vShowView).start();

        return true;

      case KeyMap.KEYCODE_DPAD_LEFT:
        if (!isValid()) {
          return true;
        }
        if (vShowView.getZoomScale() <= 1.0f) {
          return true;
        }

        vShowView.setType(ConstPhoto.MOVE_LEFT);
        new Thread(vShowView).start();

        return true;

      case KeyMap.KEYCODE_DPAD_UP:
        if (!isValid()) {
          return true;
        }
        if (vShowView.getZoomScale() <= 1.0f) {
          return true;
        }

        vShowView.setType(ConstPhoto.MOVE_UP);
        new Thread(vShowView).start();

        return true;

      case KeyMap.KEYCODE_DPAD_DOWN:
        if (!isValid()) {
          return true;
        }
        if (vShowView.getZoomScale() <= 1.0f) {
          return true;
        }

        vShowView.setType(ConstPhoto.MOVE_DOWN);
        new Thread(vShowView).start();

        return true;

      case KeyMap.KEYCODE_MTKIR_YELLOW: {
        if (mRotateThread != null && mRotateThread.isAlive()) {
            // toast rotate is alive.
            Util.showToast(getApplicationContext(), "Rotate is alive, please wait.");
            return true;
        }
        MtkLog.i(TAG, "YELLOWKEYEVENT");
        if (!isValid()) {
          return true;
        }
        if (null != mControlView && mControlView.isPlaying()) {
          reSetController();
          switchEffect();
        } else {
          MtkLog.i(TAG, "YELLOWKEYEVENT PAUSE");
          /* fix cr DTV00385117 add by lei */
          // Modified by Dan for fix bug DTV00390943
          if (isNotSupport || mCurBitmap == null) {
            return true;
          }

          reSetController();

          int size = vShowView.getMultiple();
          size = size * 2;
          if (size > ConstPhoto.ZOOM_4X) {
            size = ConstPhoto.ZOOM_1X;
          }
          int zoom = R.string.mmp_menu_1x;
          switch (size) {
            case ConstPhoto.ZOOM_1X:
              zoom = R.string.mmp_menu_1x;
              break;
            case ConstPhoto.ZOOM_2X:
              zoom = R.string.mmp_menu_2x;
              break;
            case ConstPhoto.ZOOM_4X:
              zoom = R.string.mmp_menu_4x;
              break;
            default:
              break;
          }
          if (null != mControlView) {
            mControlView.setPhotoZoom(mResources.getString(zoom));
          }
          vShowView.setMultiple(size);
          MtkLog.i(TAG, "multisize:" + size);
          vShowView.setType(ConstPhoto.ZOOMOUT);
          new Thread(vShowView).start();
        }

        return true;
      }
      case KeyMap.KEYCODE_MTKIR_GREEN: {
        if (!isValid()) {
          return true;
        }

        if (null != mControlView && mControlView.isPlaying()) {
          reSetController();
          switchDuration();
        } else {
          MtkLog.i(TAG, "isNotSupport" + isNotSupport + "---mCurBitmap:" + mCurBitmap);
          if (isNotSupport || mCurBitmap == null) {
            return true;
          }

          reSetController();
          mLogicManager.incRotate();
          if (null != mCurBitmap && null != mCurBitmap.getMovie()) {
            oriention = vShowView.getRotate();
            if (oriention >= 360) {
              oriention = 0;
            }

            newOriention = vShowView.getRotate();
            MtkLog.i(TAG, "Photo oriention change gif:" + oriention);
            vShowView.setRotate(oriention + 90);
            mRotateThread = new Thread(vShowView);
            mRotateThread.start();
          } else {
//            oriention = mLogicManager.getPhotoOrientation();
//            MtkLog.d(TAG, "Rotate set Bitmap start: " + System.currentTimeMillis());
            mCurBitmap.setBitmap(mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
//            MtkLog.d(TAG, "Rotate set Bitmap end: " + System.currentTimeMillis());
//            newOriention = mLogicManager.getPhotoOrientation();
//
//            MtkLog.i(TAG, "Photo oriention change :" + oriention
//                + "-->" + newOriention);
            vShowView.setRes(mCurBitmap);
//            if (newOriention != oriention) {
//              // Bitmap thumb=Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),
//              // MultiMediaConstant.LARGE_THUMBNAIL_SIZE,
//              // MultiMediaConstant.LARGE_THUMBNAIL_SIZE, true);
//              // BitmapCache.createCache(false).put(mLogicManager
//              // .getCurrentPath(Const.FILTER_IMAGE), thumb);
//              MtkLog.d(TAG, "Rotate create cache start: " + System.currentTimeMillis());
//              BitmapCache.createCache(false).del(mLogicManager.getCurrentPath(Const.FILTER_IMAGE));
//              MtkLog.d(TAG, "Rotate create cache end: " + System.currentTimeMillis());
//              mLogicManager.setRotationChanged();
//
//            }
            vShowView.setType(ConstPhoto.ROTATE_R);
            mRotateThread = new Thread(vShowView);
            mRotateThread.start();
          }
          updateInfoView();
        }

        return true;
      }
      case KeyMap.KEYCODE_VOLUME_DOWN:
      case KeyMap.KEYCODE_VOLUME_UP:
      case KeyMap.KEYCODE_MTKIR_MUTE: {
        if (null != mLogicManager.getAudioPlaybackService()) {
          currentVolume = mLogicManager.getVolume();
          maxVolume = mLogicManager.getMaxVolume();
          break;
        } else {
          return true;
        }
      }
      case KeyMap.KEYCODE_MTKIR_PREVIOUS:
      case KeyMap.KEYCODE_MTKIR_NEXT: {
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_SLEEP: {
        // if (playMode == FRAME_ONE_PHOTO_MODE || playMode == FRAME_ALL_PHOTO_MODE) {
        // SleepDialog dialog = new SleepDialog(this);
        // dialog.show();
        // return true;
        // }

        if (isValid()) {
          if (playMode == FRAME_ONE_PHOTO_MODE || playMode == FRAME_ALL_PHOTO_MODE) {
            setSleepTime();
          }
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_INFO:
        if (playMode == FRAME_ONE_PHOTO_MODE) {
          return true;
        }
        break;
      case KeyMap.KEYCODE_BACK:
        // handlePhotoPlayEnd();
        playToEnd();
        break;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void handlePhotoPlayEnd() {
    if (null != vShowView) {
      vShowView.bitmapRecycle();
    }
    finishSetting();
    finish();
  }

  /**
   * Switch photo play effective
   */
  private void switchEffect() {
    int value = vShowView.getEffectValue();
    if (value == ConstPhoto.DEFAULT) {
      value = ConstPhoto.dissolve;
    } else if (value < ConstPhoto.RADNOM) {
      value++;
    } else {
      value = ConstPhoto.DEFAULT;
    }
    vShowView.setType(value);
    mControlView.setPhotoAnimationEffect(value);
  }

  /**
   * Switch photo play duration
   */
  private void switchDuration() {
    if (mDelayedTime == DELAYED_SHORT) {
      mDelayedTime = DELAYED_MIDDLE;
      mControlView.setPhotoTimeType(getString(R.string.mmp_menu_medium));
    } else if (mDelayedTime == DELAYED_MIDDLE) {
      mDelayedTime = DELAYED_LONG;
      mControlView.setPhotoTimeType(getString(R.string.mmp_menu_long));
    } else {
      mDelayedTime = DELAYED_SHORT;
      mControlView.setPhotoTimeType(getString(R.string.mmp_menu_short));
    }
  }

  /**
   * Show menu dialog
   */
  private void showDialog() {

    //if (playMode == FRAME_ONE_PHOTO_MODE) {

    //  showSleepMenuDialog();
   //   return;
   // }
    mHandler.removeMessages(MESSAGE_POPHIDE);
    menuDialog = new MenuListView(this, GetDataImp
        .getInstance().getComMenu(this,
            R.array.mmp_menu_photoplaylist,
            R.array.mmp_menu_photoplaylist_enable,
            R.array.mmp_menu_photoplaylist_hasnext), mListener,
        mCallBack);

    if (null != mControlView) {

      if (mControlView.isPlaying()) {
        menuDialog
            .setList(0, mResources
                .getString(R.string.mmp_menu_pause), false, 3,
                mResources
                    .getString(R.string.mmp_menu_duration),
                true, 4, mResources
                    .getString(R.string.mmp_menu_effect),
                true);
      } else {
        menuDialog.setList(0, mResources
            .getString(R.string.mmp_menu_play), false, 3,
            mResources.getString(R.string.mmp_menu_rotate), false,
            4, mResources.getString(R.string.mmp_menu_zoom), true);

        if (isNotSupport || mCurBitmap == null) {

          menuDialog.setItemEnabled(3, false);
          menuDialog.setItemEnabled(4, false);
        }
      }

        if (null != mLogicManager) {
            boolean isShuffle = mLogicManager
              .getShuffleMode(Const.FILTER_IMAGE);
            if (isShuffle) {
                menuDialog.setItem(2, mResources
                    .getString(R.string.mmp_menu_shuffleoff));
            }
        }
    }
    Util.logLife(TAG, "isTransfBitmaping:" + Const.isTransfBitmaping);
    if(Const.isTransfBitmaping) {
      menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_showinfo), false);
    }else{
      menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_showinfo), true);
    }
    menuDialog.setMediaType(sMediaType);
    menuDialog.show();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void hideControllerDelay() {
    mHandler.removeMessages(MESSAGE_POPHIDE);
    mHandler.sendEmptyMessageDelayed(MESSAGE_POPHIDE, MESSAGE_POPSHOWDEL);
  }

  /**
   * Menu right handler
   */
  private final MenuListView.MenuDismissCallBack
  mCallBack = new MenuListView.MenuDismissCallBack() {

    @Override
    public void onDismiss() {
      if (menuDialog != null && menuDialog.isShowing()) {
        if (mHandler.hasMessages(MESSAGE_POPHIDE)) {
          mHandler.removeMessages(MESSAGE_POPHIDE);
        }
        mHandler.sendEmptyMessageDelayed(MESSAGE_POPHIDE, 3000);
      } else {
        hideController();
      }
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

  private boolean isDeinit = false;
  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStop() {
    if (menuDialog != null && menuDialog.isShowing()) {
      menuDialog.dismiss();
    }
    removeMessage();
    isStop = true;
    if(photoRender != null) {
      photoRender.deinitPhotoPlay();
      isDeinit = true;
    }
    super.onStop();
    Util.logLife(TAG, "onStop");

  };

  /**
   * Remove handler message
   */
  private void removeMessage() {
    mHandler.removeMessages(MESSAGE_PHOTOMODE);
    mHandler.removeMessages(MESSAGE_PLAY);
    mHandler.removeMessages(MESSAGE_POPHIDE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    Const.isTransfBitmaping = false;
    if(mLogicManager != null) {
      mLogicManager.setPhotoCompleteListener(null);
      mLogicManager.setPhotoDecodeListener(null);
    }
    if(mCurBitmap != null) {
       mCurBitmap.setBitmap(null);
    }
    if(vShowView != null) {
      vShowView.setPlayLisenter(null);
      vShowView.setInterrupted(true);
      vShowView.bitmapRecycle();
      vShowView.removeAllMessage();
    }

    isPhotoActivityLiving = false;
    if (is4K2KFlag) {
      Util.logResRelease("deinitPhotoPlay");
      photoRender.deinitPhotoPlay();
      is4K2KFlag = false;
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
    if (mControlView != null) {
      mControlView.dismiss();
    }
    if (mTipsDialog != null) {
      mTipsDialog.dismiss();
    }
    mRotateThread = null;
    if(mImageManager != null) {
      mImageManager.setImageLoad(null, null);
    }
    super.onDestroy();
    Util.logLife(TAG, "onDestroy");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBackPressed() {
    if (null != mImageManager) {
      mImageManager.finish();
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
    if (null != vShowView) {
      vShowView.setInterrupted(true);
      vShowView.bitmapRecycle();
    }
    super.onBackPressed();
  }

  /**
   * show information when photo frame is selected in menu
   * @param menuSetupPhotoFrame
   * add by haixia for fix bug DTV00383200
   */
  private void showPhotoFrameInfo(String menuSetupPhotoFrame) {
    if (null == mTipsDialog) {
      mTipsDialog = new TipsDialog(this);
      mTipsDialog.setText(menuSetupPhotoFrame);
      if (isPhotoActivityLiving == true) {
        mTipsDialog.show();
        mTipsDialog.setBackground(R.drawable.toolbar_playerbar_test_bg);
        Drawable drawable = this.getResources().getDrawable(
            R.drawable.toolbar_playerbar_test_bg);

        int weight = (int) (drawable.getIntrinsicWidth() * 0.6);
        int height = drawable.getIntrinsicHeight();
        // mTipsDialog.setDialogParams(weight, height);

        int x = -((ScreenConstant.SCREEN_WIDTH / 2) - weight / 2)
            + (ScreenConstant.SCREEN_WIDTH / 10);
        int y = (int) (ScreenConstant.SCREEN_HEIGHT * 3 / 8
            - ScreenConstant.SCREEN_HEIGHT * 0.16 - height / 2);
        mTipsDialog.setWindowPosition(x, y);
      }
    } else {
      mTipsDialog.setText(menuSetupPhotoFrame);
      mTipsDialog.show();
    }

  }

  /**
   * Menu item click callback
   * @param content the click item content value
   */
  private void controlState(String content) {

    if (content.equals(mResources
        .getString(R.string.mmp_frame_photo_sleeptime))) {
      if (null != menuDialogSleepTime && menuDialogSleepTime.isShowing()) {
        menuDialogSleepTime.dismiss();
      }
      if (mSleepdialog != null && mSleepdialog.isShowing()) {
        // mSleepdialog.setSleepTime();
        return;
      } else {
        mSleepdialog = new SleepDialog(this);
        mSleepdialog.show();
        mSleepdialog.updateValue(true);
        return;
      }

    }

    if (content.equals(mResources.getString(R.string.mmp_menu_pause))) {
      showController();
      hideControllerDelay();
      mControlView.setMediaPlayState();
      MtkLog.d(TAG, "content:-----" + content);
      menuDialog.setList(0, mResources.getString(R.string.mmp_menu_play),
          false, 3, mResources.getString(R.string.mmp_menu_rotate),
          false, 4, mResources.getString(R.string.mmp_menu_zoom),
          true);
      // Added by Dan for fix bug DTV00384878& DTV00389285
      if (isNotSupport || mCurBitmap == null) {
        menuDialog.setItemEnabled(3, false);
        menuDialog.setItemEnabled(4, false);
      }else {
        menuDialog.setItemEnabled(3, true);
        menuDialog.setItemEnabled(4, true);
      }
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play))) {
      showController();
      hideControllerDelay();
      mControlView.setMediaPlayState();
      menuDialog.setList(0,
          mResources.getString(R.string.mmp_menu_pause), false, 3,
          mResources.getString(R.string.mmp_menu_duration), true, 4,
          mResources.getString(R.string.mmp_menu_effect), true);
      // add by keke 12.2.27 for DTV00399637
      if (isNotSupport) {
        menuDialog.setItemEnabled(3, true);
        menuDialog.setItemEnabled(4, true);
      }
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_none))
        && (isRepeatMode == 3)) {
      // Util.setMediaRepeatMode(getApplicationContext(), MultiMediaConstant.PHOTO,Util.NONE);
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_NONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatone))) {
      // Util.setMediaRepeatMode(getApplicationContext(),
      // MultiMediaConstant.PHOTO,Util.REPEATE_ONE);
      mControlView.setRepeatSingle();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatall))) {
      // Util.setMediaRepeatMode(getApplicationContext(),
      // MultiMediaConstant.PHOTO,Util.REPEATE_ALL);
      mControlView.setRepeatAll();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ALL);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleon))) {
      mControlView.setShuffleVisble(View.VISIBLE);
      menuDialog.initItem(2, mResources
          .getString(R.string.mmp_menu_shuffleoff));
      mLogicManager.setShuffle(Const.FILTER_IMAGE, Const.SHUFFLE_ON);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleoff))) {
      mControlView.setShuffleVisble(View.INVISIBLE);
      menuDialog.initItem(2, mResources
              .getString(R.string.mmp_menu_shuffleon));
      mLogicManager.setShuffle(Const.FILTER_IMAGE, Const.SHUFFLE_OFF);
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_short))) {
      mDelayedTime = DELAYED_SHORT;
      mControlView.setPhotoTimeType(content);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_medium))) {
      mDelayedTime = DELAYED_MIDDLE;
      mControlView.setPhotoTimeType(content);
    } else if (content.equals(mResources.getString(R.string.mmp_menu_long))) {
      mDelayedTime = DELAYED_LONG;
      mControlView.setPhotoTimeType(content);
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.DEFAULT);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_dissolve))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.dissolve);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wiperight))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_right);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wipeleft))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_left);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wipeup))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_top);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wipedown))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_bottom);
    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_boxin))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.box_in);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_boxout))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.box_out);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_random))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.RADNOM);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showinfo))) {
      menuDialog.dismiss();
      showinfoview(MultiMediaConstant.PHOTO);
    } else if (content.equals(mResources.getString(R.string.mmp_menu_1x))
        || content.equals(mResources.getString(R.string.mmp_menu_2x))
        || content.equals(mResources.getString(R.string.mmp_menu_4x))) {

      menuDialog.hideMenuDelay();

      mControlView.setPhotoZoom(content);

      int size = Integer.parseInt(content.substring(0, 1));
      // vShowView.setRes(mCurBitmap);
      vShowView.setMultiple(size);
      vShowView.setType(ConstPhoto.ZOOMOUT);
      new Thread(vShowView).start();
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_rotate))) {

      menuDialog.hideMenuDelay();

      mLogicManager.incRotate();
      // if (mLogicManager.getCurrentPath(Const.FILTER_IMAGE).endsWith(".gif")){
      if (null != mCurBitmap.getMovie()) {
        MtkLog.i(TAG, " gif file");
        oriention = vShowView.getRotate();
        MtkLog.i(TAG, " gif file");
        if (oriention >= 360) {
          oriention = 0;
        }

        newOriention = mLogicManager.getPhotoOrientation();
        newOriention = vShowView.getRotate();

        vShowView.setRotate(oriention + 90);
        MtkLog.i(TAG, "Photo oriention change gif:" + oriention
            + "-->" + newOriention);
        // new Thread(vShowView).start();
      } else {
//        oriention = mLogicManager.getPhotoOrientation();
        mCurBitmap.setBitmap(mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
//        newOriention = mLogicManager.getPhotoOrientation();
//
//        MtkLog.i(TAG, "Photo oriention change :" + oriention
//            + "-->" + newOriention);
        vShowView.setRes(mCurBitmap);
        // if (newOriention != oriention) {
        // Bitmap thumb=Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),
        // MultiMediaConstant.LARGE_THUMBNAIL_SIZE,
        // MultiMediaConstant.LARGE_THUMBNAIL_SIZE, true);
        // BitmapCache.createCache(false).put(mLogicManager
        // .getCurrentPath(Const.FILTER_IMAGE), thumb);
        // }
//        if (newOriention != oriention) {
//          // Bitmap thumb=Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),100,100, true);
//          BitmapCache.createCache(false).del(mLogicManager.getCurrentPath(Const.FILTER_IMAGE));
//          // .put(mLogicManager.getCurrentPath(Const.FILTER_IMAGE), thumb);
//          mLogicManager.setRotationChanged();
//        }
        vShowView.setType(ConstPhoto.ROTATE_R);
        new Thread(vShowView).start();

      }
      updateInfoView();

    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_frame))) {
      /*
       * String path = mLogicManager.getCurrentPath(Const.FILTER_IMAGE); Editor editor =
       * mPreferences.edit(); editor.putString(PHOTO_FRAME_KEY, path); editor.commit();
       */
      mPhotoFramePath = mLogicManager.getCurrentPath(Const.FILTER_IMAGE);
      MtkLog.d(TAG,"set mPhotoFramePath == "+mPhotoFramePath);
      mPhotoFrameSource = mImageSource;
      if (menuDialog != null && menuDialog.isShowing()) {
        menuDialog.dismiss();
      }
      this.showPhotoFrameInfo(mResources
          .getString(R.string.mmp_menu_photo_frame));

      mHandler.sendEmptyMessageDelayed(MESSAGE_HIDDLE_FRAME, DELAYED_FRAME);
    } else if (content.equals(mResources
          .getString(R.string.mmp_menu_pic_setting))) {
        if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.dismiss();
        }
        if (mControlView != null && mControlView.isShowed()) {
          hideController();
          //removeControlView();
        }
        showPictureSetting();
        isBackFromCapture = true;
     }
  }

  /**
   * Show the bitmap with EffectViewNative
   * @param bitmap
   */
  private void loadImageDone(PhotoUtil bitmap) {
    updateIndex();
    mLogicManager.initRotate();
    setControlView();
    if (null == bitmap) {
      return;
    }

    mCurBitmap = bitmap;
    if (null == mControlView) {

      MtkLog.i(TAG, "loadImageDone()  photoPlayActivity has finished");
      return;
    }

    if (null != mTipsDialog && mTipsDialog.isShowing()) {
      mTipsDialog.dismiss();
    }

    if(vShowView == null) {
      return;
    }

    int value = vShowView.getEffectValue();
    vShowView.setInterrupted(false);

    if ((bitmap.getBitmap() == null && bitmap.getMovie() == null) || isNotSupport) {
      vShowView.setInterrupted(true);
      mControlView.setPhotoZoom("");
      return;

    } else {
      int size = vShowView.getMultiple();
      int rotate = vShowView.getRotate();
      mControlView.setPhotoZoom(mResources
          .getString(R.string.mmp_menu_1x));
      // vShowView.setMultiple(1);
      mPhotoParams[0] = 1;
      // vShowView.setPreMultiple(size);
      mPhotoParams[1] = size;
      // vShowView.setRotate(0);
      mPhotoParams[2] = 0;
      // vShowView.setPreRotate(rotate);
      mPhotoParams[3] = rotate;
      //zoom scale
      mPhotoParams[4] = 1;
    }

    if (null != vShowView) {
      if (bitmap.getMovie() != null) {
        vShowView.setRes(bitmap, mPhotoParams);
        vShowView.setType(value);
        vShowView.run();
      } else {
        MtkLog.i(TAG, "setBitmap:" + bitmap.getBitmap());
        // vShowView.setEffectRes(bitmap.getBitmap(),mPhotoParams);
        vShowView.syncSetEffectResToRun(bitmap.getBitmap(),
            mPhotoParams, value);
      }
    }
  }

  int mPhotoParams[] = {
      0, 0, 0, 0, 0
  };

  /**
   * Get photo play duration
   * @return int duration
   */
  public static int getDelayedTime() {
    return mDelayedTime;
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    if (is4K2KFlag) {
      Util.logResRelease("deinitPhotoPlay");
      if (photoRender != null){
        photoRender.deinitPhotoPlay();
		}
      is4K2KFlag = false;
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
  }
}
