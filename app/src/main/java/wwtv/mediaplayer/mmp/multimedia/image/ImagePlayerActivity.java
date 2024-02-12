package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.media.MediaPlayer;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.EffectView;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoCompletedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoDecodeListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager.ImageLoad;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;

import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.GifDecoder;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.MyInputStream;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.util.Constants;
import com.mediatek.wwtv.mediaplayer.R;

public class ImagePlayerActivity extends MediaPlayActivity {
    private static final String TAG = "ImgPlayerActivity";

    private static final String ACTION_CHANGE_SOURCE = "source.switch.from.storage";
    private static final String ACTION_MCAST_STATE_CHANGED = "com.mstar.android.mcast.MCAST_STATE_CHANGED";

    private static final int NEXT_IMAGE_PRE = -1;
    private static final int NEXT_IMAGE_NEX = 1;

    // Set transfer news value，Pictures are playing
    private static final int PPT_PLAYER = 1;

    private static final int PHOTO_DECODE_PROPRESS = 2;

    private static final int PHOTO_DECODE_FINISH = 4;

    private static final int PHOTO_NAME_UPDATE = 5;

    private static final int SHOW_TOAST = 6;

    private static final int MESSAGE_POPHIDE = 7;

    private static final int HANDLE_MESSAGE_PLAYER_EXIT = 8;

    // Article control default disappear time 3s
    private static final int DEFAULT_TIMEOUT = 3000;

    private static final int IMAGE_VIEW = 0;

    private static final int GIF_VIEW = 3;

    // the largest size of file can be decode
    // private static final long LARGEST_FILE_SIZE = 30 * 1024 * 1024;
    // the largest pix of photo can be decode successful
    private static final long UPPER_BOUND_PIX = 1920 * 8 * 1080 * 8;

    private static final double UPPER_BOUND_WIDTH_PIX = 1920.0f;

    private static final double UPPER_BOUND_HEIGHT_PIX = 1080.0f;

    private final int DURATION_500MS = 500;

    private static final int mStep = 60;

    // Picture player all control container
    private ImagePlayerViewHolder mPhotoPlayerHolder;

    // Video buffer progress bar
    private ProgressDialog mProgressDialog;

    private FileInputStream mFileInputStream = null;

    private InputStream is = null;

    public int mDisplayFormat ;

    private boolean mCanSetWallpaper = true;

    // Key shielding switch
    private boolean mCanResponse = true;

    // Whether in the playing mode
    private boolean mPPTPlayer = true;

    private boolean mIsPlaying = false;

    // Picture enlarge or reduce The Times
    private float mZoomTimes = 1.0f;
    public float mMaxZoomInTimes = 2.4f;
    public float mMinZoomOutTimes = 0.4f;

    private static float mRotateAngle = 0f;

    // screen resolution
    private int mWindowResolutionWidth = 0;
    private int mWindowResolutionHeight = 0;

    private int mCurrentView = IMAGE_VIEW;

    private int mPreCurrentView = IMAGE_VIEW;

    // The current broadcast document source, local or network
    private int mSourceFrom;

    private Thread mZoomThread = new Thread();

    private Thread mRotateThread = new Thread();

    private static BitmapFactory.Options options;

    // private boolean isExit = false;
    private int detectNum = 0;

    private boolean isOnPause = false;

    // Only MPO format images can switch 3d
    private boolean isMPO = false;

    private boolean isAnimationOpened = false;

    private static int mSlideTime = DELAYED_SHORT;

    // 4K2K photo decode so slow, so define slide4K2KTime for 4K2K photo play
    private int slide4K2KTime = 5000;

    private boolean fristShowPicture = true;

    protected boolean isDefaultPhoto = false;

    private boolean isSettingWallPaper = false;

    private String str4K2KPhotoPath = "";

    private boolean mWillExit = false;

    public boolean mIsSourceChange = false;

    private boolean mIsStartingSetting = false;

    private Toast mToast = null;

    private AlertDialog mErrorDialog = null;

    // processing images play and pause
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_POPHIDE:
                  Log.i(TAG, "MESSAGE_POPHIDE");
                  dismissMenuDialog();
                  hideController();
                  break;
                case PPT_PLAYER: // Slide mode play pictures
                    if (mPPTPlayer) {
                        if (Const.REPEAT_ONE == mLogicManager.getRepeatModel(Const.FILTER_IMAGE)) {
                          startPPT_Player();
                          return;
                        }
                        moveNextOrPrevious(Const.MANUALNEXT);
                    }
                    break;
                case PHOTO_DECODE_PROPRESS:
                    dismissProgressDialog();
                    if (isSettingWallPaper) {
                        showProgressDialog(R.string.mmp_img_player_setting_wallpaper);
                    } else {
                        showProgressDialog(R.string.mmp_img_player_decoding);
                    }
                    break;
                case PHOTO_DECODE_FINISH:
                    dismissProgressDialog();
                    break;
                case HANDLE_MESSAGE_PLAYER_EXIT:
                    ImagePlayerActivity.this.finish();
                    break;
                case PHOTO_NAME_UPDATE:
                    setControlView();
                    break;
                case SHOW_TOAST:
                    showToast(getString(msg.arg1), Gravity.CENTER, Toast.LENGTH_SHORT);
                    break;
                default:
                    break;
            }
        }
    };

    private boolean bDecodeRet = false;

    private WallpaperManager mWpm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mHandler.sendEmptyMessage(PHOTO_DECODE_PROPRESS);
        setContentView(R.layout.mmp_imageplayer_main);

        mSourceFrom = getIntent().getIntExtra(Constants.SOURCE_FROM, 0);
        String IntentPath = Tools.parseUri(getIntent().getData());


        mLogicManager.setImageSource(ConstPhoto.LOCAL);

        //default
        mRotateAngle = 0f;
        mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ALL);

        findView();

        if (DELAYED_SHORT == mSlideTime) {
          mControlView.setPhotoTimeType(getString(R.string.mmp_menu_short));
        } else if (DELAYED_MIDDLE == mSlideTime) {
          mControlView.setPhotoTimeType(getString(R.string.mmp_menu_medium));
        } else {
          mControlView.setPhotoTimeType(getString(R.string.mmp_menu_long));
        }

        mSourceFrom = getIntent().getIntExtra(Constants.SOURCE_FROM, 0);

        WindowManager windowManager = getWindowManager();

        Display display = windowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        mWindowResolutionWidth = point.x; // display.getWidth();
        mWindowResolutionHeight = point.y;
        Log.i(TAG, "mWindowResolutionWidth:" + mWindowResolutionWidth + " mWindowResolutionHeight:" + mWindowResolutionHeight);

        mWpm = (WallpaperManager) getSystemService(Context.WALLPAPER_SERVICE);
        // switch source monitor
        IntentFilter sourceChange = new IntentFilter(ACTION_CHANGE_SOURCE);
        this.registerReceiver(mSourceChangeReceiver, sourceChange);
        IntentFilter castStateChangeIntentFilter = new IntentFilter(ACTION_MCAST_STATE_CHANGED);
        this.registerReceiver(mCastStateChangeReceiver, castStateChangeIntentFilter);
        Constants.bPhotoSeamlessEnable = Tools.isPhotoStreamlessModeOn();
        if(Tools.isTotalMemLowEnd()) {
            Constants.bSupportPhotoScale = false;
        }
        SharedPreferences mShared = getSharedPreferences("photoPlayerInfo", Context.MODE_PRIVATE);
        isAnimationOpened = mShared.getBoolean("isAnimationOpened", false);
        startToShowPhoto();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "********onDestroy*******");
        dismissProgressDialog();
        stopPPTPlayer();
        unregisterReceiver(mSourceChangeReceiver);
        // unregisterReceiver(homeKeyEventBroadCastReceiver);
        unregisterReceiver(mCastStateChangeReceiver);
        Constants.isExit = true;

        if (mErrorDialog != null) {
            mErrorDialog.dismiss();
            mErrorDialog = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "---------- onStop ---------");
        mWillExit = true;
        if (mCurrentView == IMAGE_VIEW) {
            mPhotoPlayerHolder.mSurfaceView.stopPlayback(true);
        }
        stopPPTPlayer();
        super.onStop();
        finish();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "********onPause*******" + Constants.isExit);
        isOnPause = true;

        // Began to disk scan
        new Thread(new Runnable() {
            @Override
            public void run() {
                Tools.startMediascanner(ImagePlayerActivity.this);
            }
        }).start();
        unregisterReceiver(mNetDisconnectReceiver);
        // Close file resources
        closeSilently(mFileInputStream);
        closeSilently(is);

        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "********onResume*******" + Constants.isExit);
        isOnPause = false;
        super.onResume();
        IntentFilter networkIntentFilter = new IntentFilter("com.mstar.localmm.network.disconnect");
        registerReceiver(mNetDisconnectReceiver, networkIntentFilter);
        // registerReceiver(homeKeyEventBroadCastReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        mIsStartingSetting = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Tools.stopMediascanner(ImagePlayerActivity.this);
            }
        }).start();
        if (fristShowPicture) {
            fristShowPicture = false;
            int count = mLogicManager.getImageNumber();
            if (count == 0) {
                finish();
                return;
            }
            Constants.isExit = false;
        } else {
            showControllerEx();
        }
        // animationNum = animationArray.length;
    }

    public void startShowProgress() {
        //mHandler.sendEmptyMessage(PHOTO_DECODE_PROPRESS);
    }

    public void stopShowingProgress() {
        mHandler.sendEmptyMessage(PHOTO_DECODE_FINISH);
    }

    private void startToShowPhoto() {
        String url = mLogicManager.getCurrentFilePath(Const.FILTER_IMAGE);
        boolean bgif = url.substring(url.lastIndexOf(".") + 1).equalsIgnoreCase(Constants.GIF);
        if (bgif) {
            decodeGif(url);
            mCurrentView = GIF_VIEW;
        } else {
            mCurrentView = IMAGE_VIEW;
            mPhotoPlayerHolder.mSurfaceView.setHandler(mHandler);
            mPhotoPlayerHolder.mSurfaceView.setImagePath(url, ImagePlayerActivity.this);
        }
    }

    public void onNextPhtoError() {
        if (!mWillExit) {
            showToastAtCenter(getString(R.string.mmp_img_player_decode_failed));
            dismissProgressDialog();
        }

        stopPPTPlayer();

        final int count = mLogicManager.getImageNumber();

        if (count <= 1) {
            stopPPTPlayer();
            this.finish();
        } else {
            showPlayErrorDialog();
        }
    }

    public void onError(MediaPlayer mp, int framework_err, int impl_err) {
        if (!mWillExit) {
            showToastAtCenter(getString(R.string.mmp_img_player_decode_failed));
            dismissProgressDialog();
        }

        stopPPTPlayer();

        final int count = mLogicManager.getImageNumber();

        if (count <= 1) {
            stopPPTPlayer();
            this.finish();
        } else {
            reSetController();
            showPlayErrorDialog();
        }
    }

    // Pop up display an error dialog box
    public void showErrorDialog(final String strMessage) {
      featureNotWork(strMessage);
    }

    public void showNotSupportDialog(final String strMessage) {
      mHandler.removeMessages(PHOTO_DECODE_PROPRESS);
      dismissProgressDialog();

      final int count = mLogicManager.getImageNumber();

      if (count <= 1) {
        stopPPTPlayer();
        showToastAtCenter(getString(R.string.mmp_img_player_decode_failed));
        this.finish();
      } else {
        showPlayErrorDialog();
      }
    }

    private void showPlayErrorDialog() {
        dismissProgressDialog();
        // Prevent activity died when the popup menu
        if (!isFinishing()) {
            if (mErrorDialog != null) {
                mErrorDialog.dismiss();
                mErrorDialog = null;
            }

            reSetController();

            /*
             * Gif is decoed in a AN Runnable, when showing error dialog, it needs to
             * avoid Gif showing in background.
             */
            mPhotoPlayerHolder.mSurfaceView.setBkgBlank(true);

            mErrorDialog = new AlertDialog.Builder(ImagePlayerActivity.this)
                    .setTitle(getResources().getString(R.string.mmp_img_player_play_error))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage(getResources().getString(R.string.mmp_img_player_play_error_message))
                    .setPositiveButton(getResources().getString(R.string.mmp_img_player_ok),
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                  mPhotoPlayerHolder.mSurfaceView.setBkgBlank(false);
                                  reSetController();
                                  moveNextOrPrevious(Const.MANUALNEXT);
                                  startPPT_PlayerDelayed();
                                }
                            })
                    .setNegativeButton(getResources().getString(R.string.mmp_img_player_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ImagePlayerActivity.this.finish();
                                }
                            }).setCancelable(false).show();
        }
    }

    private final ControlPlayState mControlImp = new ControlPlayState() {
      @Override
      public void play() {
      }

      @Override
      public void pause() {
      }
    };

    private void findView() {
        mPhotoPlayerHolder = new ImagePlayerViewHolder(this);
        mPhotoPlayerHolder.findViews();

        getPopView(R.layout.mmp_popupphoto, MultiMediaConstant.PHOTO, mControlImp);

        showPopUpWindow((LinearLayout) findViewById(R.id.mmp_imageplayer_display));
        hideControllerDelay();
        setControlView();

        reSetController();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      Log.i(TAG, "onKeyDown ----------- event:" + event);
      switch(keyCode) {
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
          if (isPhotoParsing()) {
              return true;
          }
          stopPPTPlayer();
          if (Constants.bPhotoSeamlessEnable
              && !checkIfItIsGifPhoto(Const.MANUALPRE)
              && (false == mPhotoPlayerHolder.mSurfaceView.isOnError())
              ) {
              initParameterBeforeShowNextPhoto();
              mPhotoPlayerHolder.mSurfaceView.showNextPhoto(NEXT_IMAGE_PRE);
          } else {
              moveNextOrPrevious(Const.MANUALPRE);
          }
          return true;
        case KeyEvent.KEYCODE_MEDIA_NEXT:
          if (isPhotoParsing()) {
              return true;
          }
          stopPPTPlayer();
          if (Constants.bPhotoSeamlessEnable
              && !checkIfItIsGifPhoto(Const.MANUALNEXT)
              && (false == mPhotoPlayerHolder.mSurfaceView.isOnError())
              ) {
              initParameterBeforeShowNextPhoto();
              mPhotoPlayerHolder.mSurfaceView.showNextPhoto(NEXT_IMAGE_NEX);
          } else {
              moveNextOrPrevious(Const.MANUALNEXT);
          }
          return true;
        case KeyEvent.KEYCODE_DPAD_UP:
        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
          if (!isContorllerShowed()) {
            if (1.0f != mZoomTimes)
            {
              switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                  Log.d(TAG, "move up");
                  mPhotoPlayerHolder.mSurfaceView.moveDirection(0, 0-mStep);
                  return true;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                  Log.d(TAG, "move down");
                  mPhotoPlayerHolder.mSurfaceView.moveDirection(0, mStep);
                  return true;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                  Log.d(TAG, "move left");
                  mPhotoPlayerHolder.mSurfaceView.moveDirection(0-mStep, 0);
                  return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                  Log.d(TAG, "move right");
                  mPhotoPlayerHolder.mSurfaceView.moveDirection(mStep, 0);
                  return true;
              }
            }
          }
          return true;
        case KeyEvent.KEYCODE_DPAD_CENTER:
        case KeyEvent.KEYCODE_ENTER:
          Log.i(TAG, "onKeyDown: KEYCODE_ENTER");
          if (isPhotoParsing()) {
            return true;
          }
          if (false == isContorllerShowed()) {
            reSetController();
            return true;
          }

          reSetController();

          if (true == mPPTPlayer) {
              stopPPTPlayer();
          } else {
              mPPTPlayer = true;
              startPPT_Player();
          }
          return true;

        case KeyEvent.KEYCODE_BACK:
          if (isPhotoParsing()) {
              return true;
          }

          mWillExit = true;

          mHandler.removeMessages(HANDLE_MESSAGE_PLAYER_EXIT);
          ImagePlayerActivity.this.finish();
          return true;

        case KeyMap.KEYCODE_MTKIR_PLAY:
          startPPT_PlayerDelayed();
          break;

        case KeyEvent.KEYCODE_MEDIA_PAUSE:
        case KeyEvent.KEYCODE_MEDIA_STOP:
          stopPPTPlayer();
          break;
        case KeyMap.KEYCODE_MENU:
          stopPPTPlayer();
          showControllerEx();
          break;

        case KeyMap.KEYCODE_MTKIR_ANGLE:
        case KeyMap.KEYCODE_MTKIR_REPEAT:
        case KeyMap.KEYCODE_MTKIR_GUIDE:
        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        case KeyMap.KEYCODE_MTKIR_INFO:
        default:
          return true;
      }
      return super.onKeyDown(keyCode, event);
    }

    private void zoomIn() {
        if (GIF_VIEW == mCurrentView) {
          showToastAtBottom(getString(R.string.mmp_img_player_gif_not_allow));
          return;
        }

        stopPPTPlayer();
        if (mZoomTimes >= mMaxZoomInTimes) {
            showToast(getString(R.string.mmp_img_player_max_tip), Gravity.CENTER, DURATION_500MS);
            return;
        }
        if (mZoomThread.isAlive()) {
            showToast(getString(R.string.mmp_img_player_zooming), Gravity.CENTER, DURATION_500MS);
            return;
        }
        mZoomTimes += 0.2;
        mZoomThread = new Thread(new Runnable() {
            public void run() {
                mPhotoPlayerHolder.mSurfaceView.scaleImage(mRotateAngle, mZoomTimes);
            }
        });
        mZoomThread.start();
    }

    private void zoomOut() {
        if (GIF_VIEW == mCurrentView) {
          showToastAtBottom(getString(R.string.mmp_img_player_gif_not_allow));
          return;
        }

        stopPPTPlayer();
        if (mZoomTimes < mMinZoomOutTimes) {
            showToast(getString(R.string.mmp_img_player_min_tip), Gravity.CENTER, DURATION_500MS);
            return;
        }
        if (mZoomThread.isAlive()) {
            showToast(getString(R.string.mmp_img_player_zooming), Gravity.CENTER, DURATION_500MS);
            return;
        }
        mZoomTimes -= 0.2;
        mZoomThread = new Thread(new Runnable() {
            public void run() {
                mPhotoPlayerHolder.mSurfaceView.scaleImage(mRotateAngle, mZoomTimes);
            }
        });
        mZoomThread.start();
    }

    private void rotateImage(final float fAngle) {
        if (GIF_VIEW == mCurrentView) {
          showToastAtBottom(getString(R.string.mmp_img_player_gif_not_allow));
          return;
        }

        stopPPTPlayer();
        if (mRotateThread.isAlive()) {
            showToast(getString(R.string.mmp_img_player_rotating), Gravity.CENTER, DURATION_500MS);
            return;
        }
        mRotateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mPhotoPlayerHolder.mSurfaceView.rotateImage(fAngle, mZoomTimes);
            }
        });
        mRotateThread.start();
    }

    private void rotateImageLeft() {
      if (GIF_VIEW == mCurrentView) {
        showToastAtBottom(getString(R.string.mmp_img_player_gif_not_allow));
        return;
      }
      if (mRotateAngle == -360) {
          mRotateAngle = 0;
      }
      mRotateAngle -= 90;
      rotateImage(mRotateAngle);
    }

    private void rotateImageRight() {
      if (GIF_VIEW == mCurrentView) {
        showToastAtBottom(getString(R.string.mmp_img_player_gif_not_allow));
        return;
      }
      if (mRotateAngle == 360) {
          mRotateAngle = 0;
      }
      mRotateAngle += 90;
      rotateImage(mRotateAngle);
    }

    private void setPhoto2Wallpaper() {
        stopPPTPlayer();
        new AlertDialog.Builder(this)
          .setIconAttribute(android.R.attr.alertDialogIcon)
          .setTitle(getString(R.string.mmp_img_player_photo_wallpaper))
          .setMessage(getString(R.string.mmp_img_player_photo_wallpaper))
          .setNegativeButton(android.R.string.cancel, null)
          .setPositiveButton(getString(android.R.string.ok),
                  new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int which) {
                          new Thread(new Runnable() {
                              @Override
                              public void run() {
                                  mCanSetWallpaper = false;
                                  String fullPath = mLogicManager.getCurrentFilePath(Const.FILTER_IMAGE);
                                  isSettingWallPaper = true;
                                  mHandler.sendEmptyMessage(PHOTO_DECODE_PROPRESS);
                                  if (Tools.isSambaPlaybackUrl(fullPath)) {
                                      fullPath = Tools.convertToHttpUrl(fullPath);
                                  }
                                  final Bitmap bitmap = decodeBitmap(fullPath);
                                  if (bitmap == null) {
                                      Log.e(TAG, "Couldn't get bitmap for path!!");
                                  } else {
                                      try {
                                          mWpm.suggestDesiredDimensions(mWindowResolutionWidth,mWindowResolutionHeight);
                                          mWpm.setBitmap(bitmap);
                                          try {
                                              Thread.sleep(DURATION_500MS);
                                          } catch (Exception e) {
                                              e.printStackTrace();
                                          }
                                          isSettingWallPaper = false;
                                          mHandler.sendEmptyMessage(PHOTO_DECODE_FINISH);
                                      } catch (Exception e) {
                                          isSettingWallPaper = false;
                                          e.printStackTrace();
                                          Log.e(TAG, "Failed to set wallpaper.");
                                      }
                                  }
                                  mCanSetWallpaper = true;
                              }
                          }).start();
                      }
                  }).show();
    }

    private GifDecoder.IGifCallBack mGifCallBack = new GifDecoder.IGifCallBack() {
        public void onFrameIndexChanged(int index) {
            //Log.d(TAG, "GifDecoder.IGifCallBack: onFrameIndexChanged index = " + index);
        }

        public void onFinalFrame() {
            Log.d(TAG, "GifDecoder.IGifCallBack: onFinalFrame!!!");
            if (mPPTPlayer) {
                mHandler.sendEmptyMessageDelayed(PPT_PLAYER, 100);
            }
        }
    };

    private boolean isLargerThanLimit(BitmapFactory.Options options) {
        long pixSize = ((long)options.outWidth) * ((long)options.outHeight);
        // largest pix is 1920 * 8 * 1080 * 8
        if (pixSize <= UPPER_BOUND_PIX) {
            return false;
        }
        return true;
    }

    private boolean isErrorPix(BitmapFactory.Options options) {
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return true;
        }
        return false;
    }

    private void decodeGif(final String url) {

        mPhotoPlayerHolder.mSurfaceView.setBkgBlank(false);

        String tmpPath = null;
        if (Tools.isSambaPlaybackUrl(url)) {
            tmpPath = Tools.convertToHttpUrl(url);
        } else {
            tmpPath = url;
        }
        final String realPath = tmpPath;
        Log.i(TAG,"Gif decode real path:"+realPath);
        if (!Tools.checkPath(realPath)) {
            Log.e(TAG, "File Path Error!");
            return;
        }
        File f = new File(realPath);
        if((!f.exists()) && (!Tools.isNetPlayback(realPath))) {
            Log.e(TAG,"file not exists or not http url");
            return;
        }
        ImagePlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (null != mControlView) {
                mControlView.setFileName(mLogicManager.getCurrentFileName(Const.FILTER_IMAGE));
              }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isDecodeSuccess = true;
                if (!Tools.isNetPlayback(realPath)) {
                    isDecodeSuccess = mPhotoPlayerHolder.mSurfaceView.setSrc(realPath,
                            ImagePlayerActivity.this);
                } else {
                    isDecodeSuccess = mPhotoPlayerHolder.mSurfaceView.decodeBitmapFromNet(realPath,
                            ImagePlayerActivity.this);
                }

                if (isDecodeSuccess) {
                    mHandler.sendEmptyMessage(PHOTO_DECODE_FINISH);

                    if (mPhotoPlayerHolder.mSurfaceView.getFrameCount() > 1) {
                        mPhotoPlayerHolder.mSurfaceView.setStart(mGifCallBack);
                    } else {
                        if (mPPTPlayer) {
                            mHandler.sendEmptyMessageDelayed(PPT_PLAYER, mSlideTime);
                        }
                    }
                }
            }
        }).start();
    }

    private Bitmap decodeBitmapFromLocal(final String imagePath) {
        // file no found
        if(!Tools.checkPath(imagePath)) {
            Log.e(TAG, "File Path Error!");
            mCanResponse = true;
            return null;
        }
        if (!Tools.isFileExist(imagePath)) {
            mCanResponse = true;
            return null;
        }
        Bitmap bitmap = null;
        /* BitmapFactory.Options */options = new BitmapFactory.Options();
        try {
            closeSilently(mFileInputStream);
            mFileInputStream = new FileInputStream(imagePath);
            FileDescriptor fd = mFileInputStream.getFD();
            if (fd == null) {
                closeSilently(mFileInputStream);
                decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
                return null;
            }
            // Plug disk, the following must be set to false.
            options.inPurgeable = false;
            options.inInputShareable = true;
            options.inJustDecodeBounds = true;
            if (!imagePath.substring(imagePath.lastIndexOf(".") + 1)
                    .equalsIgnoreCase(Constants.MPO)) {
                // options.forceNoHWDoecode = true;
            } else {
                isMPO = true;
            }
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            Log.d(TAG, "options " + options.outHeight + " " + options.outWidth);

            if (isLargerThanLimit(options)) {
                closeSilently(mFileInputStream);
                mCanResponse = true;
                Log.d(TAG, "**show default photo**");
                return setDefaultPhoto();
            }
            if (isErrorPix(options)) {
                closeSilently(mFileInputStream);
                mCanResponse = true;
                if (!isOnPause)
                    decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
                return null;
            }
            // options.forceNoHWDoecode = false;
            // According to the 1920 * 1080 high-definition format picture as
            // the restriction condition
            options.inSampleSize = computeSampleSizeLarger(options.outWidth, options.outHeight);

            Log.d(TAG, "options.inSampleSize : " + options.inSampleSize);
            options.inJustDecodeBounds = false;
            if (fd != null) {
                bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
                // jpeg png gif use the open source third-party library，bmp is
                // decoded by skia
                // Open source third-party library have default exception
                // handling methods（In the exit will interrupt analytic，return
                // null
                if (bitmap != null) {
                    bitmap = resizeDownIfTooBig(bitmap, true);
                } else {
                    if (!isOnPause)
                        decodeBitmapFailed(R.string.mmp_img_player_can_not_decode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (Constants.isExit) {
                return null;
            }
            try {
                closeSilently(mFileInputStream);
                mFileInputStream = new FileInputStream(imagePath);
                bitmap = BitmapFactory.decodeStream(mFileInputStream, null, options);
                if (bitmap == null) {
                    decodeBitmapFailed(R.string.mmp_img_player_can_not_decode);
                    return setDefaultPhoto();
                }
            } catch (Exception error) {
                error.printStackTrace();
                decodeBitmapFailed(R.string.mmp_img_player_can_not_decode);
                return setDefaultPhoto();
            } finally {
                closeSilently(mFileInputStream);
            }
        } finally {
            closeSilently(mFileInputStream);
        }
        mCanResponse = true;
        // ARGB_8888 is flexible and offers the best quality
        if (options != null && options.inPreferredConfig != Config.ARGB_8888) {
            return ensureGLCompatibleBitmap(bitmap);
        }
        return bitmap;
    }

    private Bitmap decodeBitmapFromNet(final String imagePath) {
        Bitmap bitmap = null;
        InputStream is = null;
        MyInputStream mIs = null;
        try {

            closeSilently(is);
            closeSilently(mIs);
            is = new URL(imagePath).openStream();
            if (is == null) {
                decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
                return null;
            }
            mIs = new MyInputStream(is, imagePath);
            /* BitmapFactory.Options */options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = true;
            if (!imagePath.substring(imagePath.lastIndexOf(".") + 1)
                    .equalsIgnoreCase(Constants.MPO)) {
                // options.forceNoHWDoecode = true; // Get the original
                // resolution
            } else {
                isMPO = true;
            }
            BitmapFactory.decodeStream(mIs, null, options);
            Log.d(TAG, "options " + options.outHeight + " " + options.outWidth);
            if (Constants.isExit) {
                return null;
            }
            // Test the image's resolution.
            if (isLargerThanLimit(options)) {
                closeSilently(is);
                closeSilently(mIs);
                mCanResponse = true;
                return setDefaultPhoto();
            }
            if (isErrorPix(options)) {
                closeSilently(is);
                closeSilently(mIs);
                mCanResponse = true;
                if (!isOnPause)
                    decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
                return null;
            }
            // options.inSampleSize = 4;
            options.inSampleSize = computeSampleSizeLarger(options.outWidth, options.outHeight);
            options.inJustDecodeBounds = false;
            // options.forceNoHWDoecode = false;
            closeSilently(mIs);
            is = new URL(imagePath).openStream();
            if (is == null) {
                decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
                return null;
            }
            mIs = new MyInputStream(is, imagePath);
            Log.d(TAG, "mIs : " + mIs);
            Log.i(TAG, "*****mIs*******" + mIs.markSupported());
            bitmap = BitmapFactory.decodeStream(mIs, null, options);
            if (bitmap == null) {
                Log.d(TAG, "BitmapFactory.decodeStream return null");
                if (isOnPause) {
                    return null;
                }
                decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
            }
            closeSilently(is);
            closeSilently(mIs);
            mCanResponse = true;
            return bitmap;
        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException in decodeBitmap");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "IOException in decodeBitmap");
            e.printStackTrace();
        } finally {
            closeSilently(is);
            closeSilently(mIs);
        }
        if (Constants.isExit) {
            return null;
        }
        decodeBitmapFailed(R.string.mmp_img_player_decode_failed);
        return null;
    }

    public Bitmap decodeBitmap(final String url) {
        Log.d(TAG, "decodeBitmap, url : " + url);
        mCanResponse = false;
        isDefaultPhoto = false;
        if (Tools.isNetPlayback(url)) {
            return decodeBitmapFromNet(url);
        } else {
            return decodeBitmapFromLocal(url);
        }
    }

    public Bitmap setDefaultPhoto() {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // Obtain resources pictures
        try {
            InputStream is = getResources().openRawResource(R.drawable.mmp_thumbnail_loading_failed_big);
            /*
            * InputStream in = null; try { in = getAssets().open("default_bg.jpg");
            * } catch (IOException e) { // TODO Auto-generated catch block
            * e.printStackTrace(); }
            */
            if (is == null) {
                return null;
            }
            /*
            * return ensureGLCompatibleBitmap(BitmapFactory.decodeStream(is, null,
            * opt));
            */
            isDefaultPhoto = true;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, opt);
            is.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showToastAtCenter(String text) {
        showToast(text, Gravity.CENTER, Toast.LENGTH_SHORT);
    }

    private void showToastAtBottom(String text) {
        showToast(text, Gravity.BOTTOM, Toast.LENGTH_SHORT);
    }

    private void showToast(final String text, int gravity, int duration) {
      if (null != mToast) {
        mToast.cancel();
        mToast = null;
      }
      mToast = Toast.makeText(ImagePlayerActivity.this, text, Toast.LENGTH_SHORT);
          mToast.setGravity(gravity, 0, 0);
      mToast.show();
    }

    private void closeSilently(final Closeable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Throwable t) {
        }
    }

    // Resize the bitmap if each side is >= targetSize * 2
    private Bitmap resizeDownIfTooBig(Bitmap bitmap, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float widthScale = (float) mWindowResolutionWidth / srcWidth;
        float heightScale = (float) mWindowResolutionHeight / srcHeight;
        Log.d(TAG, "srcWidth : " + srcWidth + " srcHeight : " + srcHeight + " widthScale : " + widthScale + " heightScale:" + heightScale);

        return resizeBitmapByScale(bitmap, widthScale, heightScale, recycle);
    }

    private Bitmap resizeBitmapByScale(Bitmap bitmap, float widthScale, float heightScale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * widthScale);
        int height = Math.round(bitmap.getHeight() * heightScale);
        if (width == bitmap.getWidth() && height == bitmap.getHeight()) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(widthScale, heightScale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    private Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    // This function should not be called directly from
    // DecodeUtils.requestDecode(...), since we don't have the knowledge
    // if the bitmap will be uploaded to GL.
    private Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            Log.i(TAG, "bitmap == null");
            return bitmap;
        } else if (bitmap.getConfig() != null) {
            Log.i(TAG, "bitmap.getConfig() != null");
            return bitmap;
        }
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        bitmap.recycle();
        System.gc();
        Log.i(TAG, "***bitmap**" + (bitmap == null) + " " + (newBitmap == null));
        return newBitmap;
    }

    // This computes a sample size which makes the longer side at least
    // minSideLength long. If that's not possible, return 1.
    private int computeSampleSizeLarger(double w, double h) {
        double initialSize = Math.max(w / UPPER_BOUND_WIDTH_PIX, h / UPPER_BOUND_HEIGHT_PIX);
        if (initialSize <= 2.0f) {
            return 1;
        } else if (initialSize < 4.0f) {
            return 2;
        } else if (initialSize < 8.0f) {
            return 4;
        } else {
            return 8;
        }
    }

    // Returns the previous power of two.
    // Returns the input if it is already power of 2.
    // Throws IllegalArgumentException if the input is <= 0
    @SuppressWarnings("unused")
    private int prevPowerOf2(int n) {
        if (n <= 0)
            throw new IllegalArgumentException();
        return Integer.highestOneBit(n);
    }

    public void setCurrentPos(int delta) {
        int nCurrIdx = mLogicManager.getCurrentIndex();
        int nTotal = mLogicManager.getImageNumber();

        final int nextIdx = mLogicManager.getNextIndex(Const.FILTER_IMAGE
          , (NEXT_IMAGE_NEX == delta) ? Const.MANUALNEXT : Const.MANUALPRE);

        mLogicManager.setCurrentIndex(Const.FILTER_IMAGE, nextIdx);

        mHandler.sendEmptyMessage(PHOTO_NAME_UPDATE);
    }

    public String getNextImagePath(int playMode) {
        final int idx = mLogicManager.getNextIndex(Const.FILTER_IMAGE, playMode);
        return mLogicManager.getFilePathByIndex(Const.FILTER_IMAGE, idx);
    }

    public boolean checkIfItIsGifPhoto(final int playMode){

        String currUrl = mLogicManager.getCurrentFilePath(Const.FILTER_IMAGE);

        String nextUrl = getNextImagePath(playMode);

        if (currUrl.substring(currUrl.lastIndexOf(".") + 1).equalsIgnoreCase(Constants.GIF)) {
            return true;
        }
        if (null != nextUrl) {
          if (nextUrl.substring(nextUrl.lastIndexOf(".") + 1).equalsIgnoreCase(Constants.GIF)) {
              return true;
          }
        }
        return false;
    }

    // Init relative parameter before call "showNextPhoto" function in photo seamless playback .
    public void initParameterBeforeShowNextPhoto(){
        mZoomTimes = 1.0f;
        mRotateAngle = 0f;
    }

    public boolean moveNextOrPrevious(final int playMode) {
        mPhotoPlayerHolder.mSurfaceView.setStop();

        // mZoomTimes should be initialized as 1.0f,
        // mZoomTimes>1.0f for zoomIn, mZoomTimes<1.0f for zoomOut.
        mZoomTimes = 1.0f;
        mRotateAngle = 0f;
        final int idx = mLogicManager.getNextIndex(Const.FILTER_IMAGE, playMode);
        if ((Const.MANUALNEXT == playMode)/* && (Const.REPEAT_NONE == mLogicManager.getRepeatMode(Const.FILTER_IMAGE)) */) {
          int nCurrIdx = mLogicManager.getCurrentIndex();
          if (idx == nCurrIdx) {
            Log.i(TAG, "moveNextOrPrevious() -> no change!");
            return false;
          }
        }
        mLogicManager.setCurrentIndex(Const.FILTER_IMAGE, idx);
        String url = mLogicManager.getCurrentFilePath(Const.FILTER_IMAGE);

        mPhotoPlayerHolder.mSurfaceView.setBkgBlank(false);

        if (url.substring(url.lastIndexOf(".") + 1).equalsIgnoreCase(Constants.GIF)) {
            mPhotoPlayerHolder.mSurfaceView.stopPlayback(false); // to clean background
            decodeGif(url);
            mCurrentView = GIF_VIEW;
        } else {
            mCurrentView = IMAGE_VIEW;
            if (mPhotoPlayerHolder.mSurfaceView.startNextVideo(url, ImagePlayerActivity.this)) {
            } else {
                //showToastAtCenter(getString(R.string.busy_tip));
                Log.w(TAG, "The system is busy,Please retry later!");
            }
        }
        /* if (mPPTPlayer) {
            mHandler.sendEmptyMessageDelayed(PPT_PLAYER, slideTime);
        } */
        setControlView();
        return true;
    }

    private void startPPT_PlayerDelayed() {
      dismissMenuDialog();
      if (null != mControlView) {
        mControlView.play();
      }
      mPPTPlayer = true;
      mHandler.removeMessages(PPT_PLAYER);
      mHandler.sendEmptyMessageDelayed(PPT_PLAYER, mSlideTime);
    }

    public void startPPT_Player() {
        mHandler.removeMessages(PPT_PLAYER);
        if (true == mPPTPlayer) {
          mHandler.sendEmptyMessageDelayed(PPT_PLAYER, mSlideTime);
          if (null != mControlView) {
            mControlView.play();
          }
        }
    }

    private void stopPPTPlayer() {
        mHandler.removeMessages(PPT_PLAYER);
        if (null != mControlView) {
          mControlView.pause();
        }

        mPPTPlayer = false;
    }

    private void showProgressDialog(int id) {
        Log.i(TAG, "showProgressDialog()");
        if (!isFinishing()) {
            mProgressDialog = new ProgressDialog(this);
            //mProgressDialog.setCancelable(false);
            //mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(getString(id));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            ColorDrawable cd = new ColorDrawable(android.graphics.Color.WHITE);
            cd.setAlpha(40);
            mProgressDialog.getWindow().setBackgroundDrawable(cd);
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            Log.i(TAG, "dismissProgressDialog");
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void showControllerEx() {
      setControlView();
      reSetController();
    }

    // Network disconnection radio treatment
    BroadcastReceiver mNetDisconnectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "receive net disconnect msg...");
            if (mSourceFrom == Constants.SOURCE_FROM_SAMBA) {
                showToastAtCenter(getString(R.string.mmp_img_player_net_disconnect));
                stopPPTPlayer();
                closeSilently(mFileInputStream);
                ImagePlayerActivity.this.finish();
            }
        }
    };
    private BroadcastReceiver mSourceChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"*******BroadcastReceiver**********" + intent.getAction());
            mIsSourceChange = true;
            mPhotoPlayerHolder.mSurfaceView.stopPlayback(false);
            ImagePlayerActivity.this.finish();
        }
    };

    private BroadcastReceiver mCastStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "------mcastStateChangeReceiver ---intent.getExtras(extraArg):" + intent.getStringExtra("extraArg"));
            if ("mairplay_playphoto".equalsIgnoreCase(intent.getStringExtra("extraArg"))) {
                mPhotoPlayerHolder.mSurfaceView.stopPlayback(true);
                ImagePlayerActivity.this.finish();
                Constants.isExit = true;
            }
        }
    };

    private void decodeBitmapFailed(final int id) {
        mCanResponse = true;
        ImagePlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToastAtCenter(getString(id));
            }
        });
    }

    private boolean hasScaleOrRotate() {
        Log.i(TAG, "---- hasScaleOrRotate ---- mRotateAngle:" + mRotateAngle);
        if ((360 == mRotateAngle) || (-360 == mRotateAngle)) {
            mRotateAngle = 0;
        }
        if ((mRotateAngle == 0) && (mZoomTimes == 1.0f)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isPhotoParsing() {
        if (mPhotoPlayerHolder.mSurfaceView.getImagePlayerThread() != null) {
            if (mPhotoPlayerHolder.mSurfaceView.getImagePlayerThread().isAlive()) {
                if (mProgressDialog != null && !mProgressDialog.isShowing()) {
                    mProgressDialog.show();
                } else if (mProgressDialog == null) {
                    showProgressDialog(R.string.mmp_img_player_decoding);
                }
                return true;
            }
        }
        return false;
    }

  private void setControlView() {
    if (mControlView != null) {
      mControlView.setRepeat(Const.FILTER_IMAGE);
      mControlView.setFileName(mLogicManager.getCurrentFileName(Const.FILTER_IMAGE));
      mControlView.setFilePosition(mLogicManager.getImagePageSize());
    }
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setPhotoView();
    }
  }

  @Override
  protected void hideControllerDelay() {
    Log.i(TAG, "hideControllerDelay");
    mHandler.removeMessages(MESSAGE_POPHIDE);
    mHandler.sendEmptyMessageDelayed(MESSAGE_POPHIDE, 10000);
  }

  private boolean isContorllerShowed() {
    if (mControlView != null && (true == mControlView.isShowed())) {
      return true;
    } else {
      return false;
    }
  }

  public static int getDelayedTime() {
    return mSlideTime;
  }

  public static int getRotateMode() {
    if (0f == mRotateAngle) {
    } else if (90f == mRotateAngle) {
      return 1;
    } else if (180f == mRotateAngle) {
      return 2;
    } else if (270f == mRotateAngle) {
      return 3;
    }
    return 0;
  }

  @Override
  protected void controlStateImage(String content) {
    final Resources res = getResources();

    /* repeat */
    if (content.equals(res.getString(R.string.mmp_menu_none))) {
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE,
          Const.REPEAT_NONE);
    } else if (content.equals(res
        .getString(R.string.mmp_menu_repeatone))) {
      mControlView.setRepeatSingle();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE,
          Const.REPEAT_ONE);
    } else if (content.equals(res
        .getString(R.string.mmp_menu_repeatall))) {
      mControlView.setRepeatAll();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE,
          Const.REPEAT_ALL);

    /* play/pause */
    } else if (content.equals(res.getString(R.string.mmp_menu_pause))) {
      menuDialog.initItem(0, res.getString(R.string.mmp_menu_play));
      menuDialog.dismiss();
      stopPPTPlayer();
    } else if (content.equals(res.getString(R.string.mmp_menu_play))) {
      menuDialog.initItem(0, res.getString(R.string.mmp_menu_pause));
      startPPT_PlayerDelayed();

    /* child: duration */
    } else if (content.equals(res.getString(R.string.mmp_menu_short))) {
      mSlideTime = DELAYED_SHORT;
      mControlView.setPhotoTimeType(content);
    } else if (content.equals(res.getString(R.string.mmp_menu_medium))) {
      mSlideTime = DELAYED_MIDDLE;
      mControlView.setPhotoTimeType(content);
    } else if (content.equals(res.getString(R.string.mmp_menu_long))) {
      mSlideTime = DELAYED_LONG;
      mControlView.setPhotoTimeType(content);
    } else {
      return;
    }
    if (mControlView != null && mControlView.isShowed()) {
      reSetController();
    }
  }

  private void showDecodeFailedMsg() {
    final Resources res = getResources();
    menuDialog.dismiss();
    featureNotWork(res.getString(R.string.mmp_img_player_decode_failed));
  }

  @Override
  protected boolean controlStateImageById(final int mnuId) {
    final Resources res = getResources();
    boolean ret = true;

    switch (mnuId) {
      /* show info */
      case R.string.mmp_menu_showinfo:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        menuDialog.dismiss();
        showinfoview(MultiMediaConstant.PHOTO);
        break;

      /* image setting */
      case R.string.mmp_menu_pic_setting:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        dismissMenuDialog();
        if (mControlView != null && mControlView.isShowed()) {
          hideController();
        }
        showPictureSetting();
        break;

      /* zoom */
      case R.string.mmp_menu_img_zoom_in:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        zoomIn();
        break;
      case R.string.mmp_menu_img_zoom_out:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        zoomOut();
        break;

      /* rotate */
      case R.string.mmp_menu_rotate_0:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        if (GIF_VIEW == mCurrentView) {
          break;
        }
        mRotateAngle = 0f;
        rotateImage(0);
        break;
      case R.string.mmp_menu_rotate_90:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        mRotateAngle = 90f;
        rotateImage(90);
        break;
      case R.string.mmp_menu_rotate_180:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        mRotateAngle = 180f;
        rotateImage(180);
        break;
      case R.string.mmp_menu_rotate_270:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        mRotateAngle = 270f;
        rotateImage(270);
        break;

      case R.string.mmp_menu_wallpaper:
        if (mPhotoPlayerHolder.mSurfaceView.isOnError()) {
          showDecodeFailedMsg();
          break;
        }
        if (mWpm == null) {
          showToastAtBottom(getString(R.string.mmp_img_player_wallpaper_not_supported));
        } else if (mCurrentView == GIF_VIEW) {
          showToastAtBottom(getString(R.string.mmp_img_player_gif_not_allow));
        } else {
          if (mCanSetWallpaper) {
            setPhoto2Wallpaper();
          } else {
            showToastAtBottom(getString(R.string.mmp_img_player_setting_wallpaper_plz_wait));
          }
        }
        break;

      default:
        ret = false;
        break;
    }
    if (true == ret) {
      if (mControlView != null && mControlView.isShowed()) {
        reSetController();
      }
    }
    return ret;
  }
}
