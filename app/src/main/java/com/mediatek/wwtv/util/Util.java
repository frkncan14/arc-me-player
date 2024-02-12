/**
 * @Description: TODO()
 *
 */

package com.mediatek.wwtv.util;

//import java.io.ByteArrayOutputStream;
//import java.util.HashMap;
//import java.util.Map;

import java.util.List;
//import java.util.zip.Adler32;
import java.util.Locale;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.UserHandle;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;
import android.text.TextUtils;
import android.view.View;
import android.content.ActivityNotFoundException;
import android.media.AudioManager;
import mediatek.sysprop.VendorProperties;

import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.KeyMap;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.PhotoRender.PhotoRender;

/**
 *
 */
public class Util {

    public static boolean isSupportDolbyAtmos(Context mContext) {
        return true;

    }

    public interface IDrmlistener {
    void listenTo(boolean isSure, boolean isContinue, int index);
  };

  public static final String STRICTMODE = "com.mediatek.wwtv.mediaplayer.debug";

  public static final String SOURCEACTION="mtk.intent.input.source";

  public static final String STARTACTION="mtk.intent.activity.start.name";

  public static final String EXO_PLAYER_PROP = "use.exoplayer.in.videoview";

  public static final String TALKBACK_SERVICE = "com.google.android.marvin.talkback/.TalkBackService";
  public static final String ACTION_MEDIA_RESOURCE_GRANTED =
        "android.intent.action.MEDIA_RESOURCE_GRANTED";
  public static final String EXTRA_PACKAGES =
        "android.intent.extra.PACKAGES";
  public static final String EXTRA_MEDIA_RESOURCE_TYPE =
        "android.intent.extra.MEDIA_RESOURCE_TYPE";
  public static final int EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC = 0;
  public static final String RECEIVE_MEDIA_RESOURCE_USAGE =
        "android.permission.RECEIVE_MEDIA_RESOURCE_USAGE";
  public static boolean mIsUseEXOPlayer = true;

  private static boolean isPvrPlaying = false;

  public static boolean mIsEnterPip = false;

  public static boolean mIsMmpFlag;

  public static boolean mIsDolbyVision = false;
  public static boolean mIsFirstPlayVideoListMode = true;
  public static boolean mInAppPipAction = true;
  private static boolean mIsInitPhotoPlay = false;

  public static void setPvrPlaying(boolean pvr) {
    isPvrPlaying = pvr;
  }

  public static boolean getPvrPlaying() {
    if (isPvrPlaying) {
      isPvrPlaying = false;
      return true;
    }
    return false;
  }

  /*
   * public static String mapKeyCodeToStr(int keyCode) { String mStr = ""; char _ch; switch
   * (keyCode) { case KeyMap.KEYCODE_0: _ch = '0'; mStr = "0"; break; case KeyMap.KEYCODE_1: _ch =
   * '1'; mStr = "1"; break; case KeyMap.KEYCODE_2: _ch = '2'; mStr = "2"; break; case
   * KeyMap.KEYCODE_3: _ch = '3'; mStr = "3"; break; case KeyMap.KEYCODE_4: _ch = '4'; mStr = "4";
   * break; case KeyMap.KEYCODE_5: _ch = '5'; mStr = "5"; break; case KeyMap.KEYCODE_6: _ch = '6';
   * mStr = "6"; break; case KeyMap.KEYCODE_7: _ch = '7'; mStr = "7"; break; case
   * KeyMap.KEYCODE_8: _ch = '8'; mStr = "8"; break; case KeyMap.KEYCODE_9: _ch = '9'; mStr = "9";
   * break; default: break; } return mStr; }
   */

  public static String TAG = "Util";
  public static final boolean PHOTO_4K2K_ON = true;
  public static final int PHOTO_8K4K2K = 1;
//  private static Activity mActivity;
//  private static boolean isMMP;

  public static final String ISLISTACTIVITY = "islistactivity";
  public static final String MEDIASETTINGS = "mediasettings";

//  private static Handler mEpgHandler;
//
//  public static void setHandler(Handler handler) {
//    mEpgHandler = handler;
//  }

  static {
    // if(0!=PhotoRender.is4KPanel() ){
    // PHOTO_4K2K_ON = PhotoRender.is4KPanel();
    // }
    MtkLog.i("MMPUtil", "is4K2K:" + PHOTO_4K2K_ON);
    MtkLog.i("MMPUtil", "PHOTO_8K4K2K:" + PHOTO_8K4K2K);
    //initPhotoPlay();
  }



  public static boolean isSupport4K8K() {
      return mIsInitPhotoPlay;
  }


  public static boolean isUseExoPlayer() {
    Log.i(TAG, "isUseExoPlayer mIsUseEXOPlayer:" + mIsUseEXOPlayer);
    return mIsUseEXOPlayer;
  }

//  public static boolean startEPGActivity(Activity actvity) {
//    if (mEpgHandler == null) {
//      return false;
//    }
//    mActivity = actvity;
//    boolean success = false;
//    /*
//     * if
//     * (CommonIntegration.getInstanceWithContext(mActivity.getApplicationContext())
//     * .isCurrentSourceTv
//     * () ) { if (CommonIntegration.getInstance().isMenuInputTvBlock()) { if
//     * (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_US) { success = true; }
//     * } else { if (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_US &&
//     * CommonIntegration.getInstance().getAllEPGChannelLength() <= 0) { success = true; } } String
//     * country = MtkTvConfig.getInstance().getCountry(); if
//     * (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA) &&
//     * country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) { success = true; } } else {
//     * success = true; } if (!success) { LogicManager.getInstance(mActivity).restoreVideoResource();
//     * LogicManager.getInstance(mActivity).finishAudioService();
//     * MultiFilesManager.getInstance(mActivity).destroy(); ((MmpApp)
//     * (mActivity).getApplication()).finishAll(); MtkFilesBaseListActivity.reSetModel();
//     * mHandler.sendEmptyMessageDelayed(MeidaMainActivity.MSG_START_EPG_DELAY, 2000); }
//     */
//    return success;
//  }

  public static void exitMmpActivity(Context context) {
    if (((MmpApp) (context).getApplicationContext()).isFirstFinishAll()) {
      LogicManager.getInstance(context).stopAudio();
      if (VideoPlayActivity.getInstance() != null
          && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
          Log.i(TAG, "exitMmpActivity,isInPictureInPictureMode do nothing");
      } else {
        LogicManager.getInstance(context).finishVideo();
        if (Thumbnail.getInstance() != null) {
          Thumbnail.getInstance().setRestRigionFlag(false);
        }
      }
      DevManager.getInstance().destroy();
      if (MultiFilesManager.hasInstance()) {
        MultiFilesManager.getInstance(context).destroy();
      }
      BitmapCache.createCache(true);
      ((MmpApp) (context).getApplicationContext()).setEnterMMP(false);
      ((MmpApp) (context).getApplicationContext()).finishAll();
    } else {
      new Exception().printStackTrace();
    }
  }

  public static void goToMainActivity(Context context) {
      Log.i(TAG, "goToMainActivity");
      try {
          Intent newIntent = new Intent();
          newIntent.setClass(context, MediaMainActivity.class);
          newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          context.startActivity(newIntent);
          LogicManager.getInstance(context).stopAudio();
          MtkLog.d(TAG,"launch MediaMainActivity");
      } catch (ActivityNotFoundException e){
          Log.d(TAG, "Activity launch failed", e);
      }
  }

  public static void exitPIP(Context context) {
    //noral case, should close other android PIP.
    String packageName = context.getPackageName()+"_app";
    MtkLog.d(TAG, "send broadcast exit pip in util packageName:" + packageName
        + "  " + GetCurrentTask.getInstance(context).getCurRunningClassName());
    if (packageName == null || packageName.equals("")) {
      packageName = "com.mediatek.wwtv.mediaplayer_app";
    }
    Intent intent = new Intent(ACTION_MEDIA_RESOURCE_GRANTED);
    intent.putExtra(EXTRA_PACKAGES,
        new String[]{packageName});
    intent.putExtra(EXTRA_MEDIA_RESOURCE_TYPE,
        EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);

  }

//  public static void setMMPFlag(boolean flag) {
//    isMMP = flag;
//  }
//
//  public static boolean getMMPFlag() {
//    return isMMP;
//  }

  public static void reset3D(Context context) {
    Log.i("UTIL", "reset3d");
    MenuConfigManager.getInstance(context).setValue(MenuConfigManager.VIDEO_3D_MODE, 0, null);
  }

  static boolean isEndPhotoPlay = true;

  /*
   * if 4k2kactivity pause,no need to end play, you need to set false;
   */
  public static void setEndPhotoPlayWhenPause(boolean isNeedEndPhotoPlay) {
    isEndPhotoPlay = isNeedEndPhotoPlay;
  }

  public static boolean isNeedEndPhotoPlayWhenPause() {
    return isEndPhotoPlay;
  }

  public static void onStop(Context context) {
    if (!isMMpActivity(context)) {
      exitMmpActivity(context);
    }
  }

  public static boolean isMMpActivity(Context context) {
    String topClassName = GetCurrentTask.getInstance(context)
        .getCurRunningClassName();
    boolean isMmpActiity = false;
    String packageName = context.getPackageName();
    if (topClassName != null && topClassName.startsWith(packageName)) {
      isMmpActiity = true;
    }
    Log.i(TAG, "packageName:" + packageName
        + "--topClassName:" + topClassName + "--isMmpActiity:" + isMmpActiity);
    return isMmpActiity;

  }

  public static boolean isTopMMpActivity(Context context) {
    String topClassName = GetCurrentTask.getInstance(context)
        .getCurRunningPackageName();
    boolean isMmpActivity = false;
    String packageName = context.getPackageName();
    if (topClassName != null && topClassName.startsWith(packageName)) {
      isMmpActivity = true;
    }
    Log.i(TAG, "packageName:" + packageName
        + "--topClassName:" + topClassName + "--isMmpActivity:" + isMmpActivity);
    return isMmpActivity;

  }

  public static boolean isGridActivity(Context context) {
    String topClassName = GetCurrentTask.getInstance(context)
        .getCurRunningClassName();
    MtkLog.i(TAG, "isGridActivity:" + topClassName);
    boolean is = false;
    if (topClassName != null
        && topClassName
           .equalsIgnoreCase("com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesGridActivity")) {
      is = true;
    }
    return is;
  }

  public static void logResRelease(String res) {
    //new Exception().printStackTrace();
    Log.i("RESOURCE", "---" + res);
  }

  public static void logListener(String res) {
    MtkLog.i("LISTENER", "---" + res);
  }

  public static void logLife(String tag, String info) {
    MtkLog.i(tag, "logLife---" + info);
  }

/*
  private static long getHashValue(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] arraystream = stream.toByteArray();
    Adler32 hash = new Adler32();
    hash.update(arraystream);
    return hash.getValue();
  }
  */

  private static long getPixal(Bitmap bitmap) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    long checksum = 0;
    int a = 0;
	int r = 0;
	int g = 0;
	int b = 0;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        int pixal = bitmap.getPixel(i, j);
        a = (pixal & 0xff);
        r = ((pixal >> 8) & 0xff);
        g = ((pixal >> 16) & 0xff);
        b = ((pixal >> 24) & 0xff);
        checksum += (a + r + g + b);
      }

    }
    return checksum;
  }

  public static final String AUTO_TEST_PROPERTY = "vendor.mtk.auto_test";

  public static void printAutoTestImage(String str, Bitmap bitmap) {
    if (0 != VendorProperties.mtk_auto_test().orElse(0)
        || MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
      int length = str.length();
      int start = str.lastIndexOf(".") + 1;
      if (start < length && bitmap != null) {
        Log.i(
            "AUTO_TEST",
            "image_format:" + str.substring(start) + " checkSum: 0x"
                + Long.toHexString(getPixal(bitmap)));
        // +"--checkHash:0x"+Long.toHexString(getHashValue(bitmap)));
      } else {
        if (bitmap == null) {
          Log.i("AUTO_TEST", "bitmap is null fail ");
        } else {
          Log.i("AUTO_TEST", "bitmap is null can't get suffix fail");
        }

      }
    }
  }

  public static void printAutoTestImageResult(String result) {
    if (0 != VendorProperties.mtk_auto_test().orElse(0)
        || MediaMainActivity.mIsDlnaAutoTest) {
      Log.i("AUTO_TEST", " play result: " + result);
    }
  }

  //
  public static void printAutoTestImage3D(String str, String process) {
    if (0 != VendorProperties.mtk_auto_test().orElse(0)
        || MediaMainActivity.mIsDlnaAutoTest) {
      if (str != null) {
        int length = str.length();
        int start = str.lastIndexOf(".") + 1;
        if (start < length) {
          Log.i(
              "AUTO_TEST",
              "image_format:" + str.substring(start) + " file:"
                  + str.substring(str.lastIndexOf("/") + 1) + " checkSum: " + process);
        } else {
          Log.i("AUTO_TEST", "image_format:" + str + " checkSum: " + process);
        }
      } else {
        Log.i("AUTO_TEST", "image_format:" + str + " checkSum: " + process);
      }
    }
  }

  public static void printAutoTestImageGif(String str, String process) {
    if (0 != VendorProperties.mtk_auto_test().orElse(0)
        || MediaMainActivity.mIsDlnaAutoTest) {

      if (str != null) {
        int length = str.length();
        int start = str.lastIndexOf(".") + 1;
        if (start < length) {
          Log.i(
              "AUTO_TEST",
              "image_format:" + str.substring(start) + " file:"
                  + str.substring(str.lastIndexOf("/") + 1) + " checkSum:" + process);
        } else {
          Log.i("AUTO_TEST", "image_format:" + str + " checkSum: " + process);
        }
      }
    }
  }

  public static void enterMmp(int status, Context context) {
    // TODO Auto-generated method stub
    MtkLog.i(TAG, "enterMmp before status:" + status);
    if (1 == status) {
      MtkLog.i(TAG, "enterMmp 1 == status ");
      MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, status);
      MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
    }
    MtkLog.i(TAG, "enterMmp after status: " + status);
  }

  public static boolean isTTSEnabled(Context context) {
      AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
      List<AccessibilityServiceInfo> enableServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
      for (AccessibilityServiceInfo enableService : enableServices) {
          if (enableService.getId().contains(TALKBACK_SERVICE)) {
              return true;
          }
      }

      return false;
  }

  public static void showToast(Context context, String msg) {
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
  }


  public static boolean isDolbyVision(Context context){
      int cur = TVContent.getInstance(context).getConfigValue(MenuConfigManager.PICTURE_MODE);
      MtkLog.d(TAG, "isDolbyVision cur: " + cur);
      if (5 == cur || 6 == cur || 13 == cur){
          mIsDolbyVision = true;
      } else {
          mIsDolbyVision = false;
      }

      return mIsDolbyVision;
  }



  public static boolean isRtl(){
      int layoutDirection = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
      MtkLog.d(TAG, "isRtl,layoutDirection=="+layoutDirection);
      return View.LAYOUT_DIRECTION_RTL == layoutDirection;
  }

    public static int changeKeycodeToRtl(int keyCode){
        if (!isRtl()){
            return keyCode;
        }

        if (keyCode == KeyMap.KEYCODE_DPAD_LEFT){
            keyCode = KeyMap.KEYCODE_DPAD_RIGHT;
        } else if (keyCode == KeyMap.KEYCODE_DPAD_RIGHT){
            keyCode = KeyMap.KEYCODE_DPAD_LEFT;
        }

        return keyCode;
    }

  public static void showDoViToast(Context context) {
      MtkLog.d(TAG, "MMP showDoViToast");
      //no need
      //Intent intent = new Intent("mtk.intent.action.dolby.version");
      //context.sendBroadcast(intent);
  }

  public static void exitAndroidSetting(Context context) {
      MtkLog.d(TAG, "MMP exitAndroidSetting");
      Intent intent = new Intent("mtk.intent.action.exit.android.setting");
      context.sendBroadcast(intent);
  }


  public static boolean isInAppPipAction(Context context){
      mInAppPipAction = context.getPackageManager().hasSystemFeature(
            android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE);

      MtkLog.d(TAG, "isInAppPipAction,mInAppPipAction=="+mInAppPipAction);
      return mInAppPipAction;
  }

    public static void printStackTrace() {
        //if (true) {
        	Throwable tr = new Throwable();
        	Log.getStackTraceString(tr);
        	tr.printStackTrace();
        //}
    }

	public static String mapKeyCodeToStr(int keyCode){
		String mStr = "";
		switch (keyCode) {
		case KeyMap.KEYCODE_0:
			mStr = "0";
			break;
		case KeyMap.KEYCODE_1:
			mStr = "1";
			break;
		case KeyMap.KEYCODE_2:
			mStr = "2";
			break;
		case KeyMap.KEYCODE_3:
			mStr = "3";
			break;
		case KeyMap.KEYCODE_4:
			mStr = "4";
			break;
		case KeyMap.KEYCODE_5:
			mStr = "5";
			break;
		case KeyMap.KEYCODE_6:
			mStr = "6";
			break;
		case KeyMap.KEYCODE_7:
			mStr = "7";
			break;
		case KeyMap.KEYCODE_8:
			mStr = "8";
			break;
		case KeyMap.KEYCODE_9:
			mStr = "9";
			break;
		default:
			break;
		}

		return mStr;
	}

	public static byte [] stringToByte(String s){
		byte[] b = new byte[3];
		if (s != null && s.length() == 5) {
			byte[] bytes = s.getBytes();
			for (int i = 0; i < bytes.length; i++) {
				System.out.println(i + "  = "
						+ Integer.toBinaryString(bytes[i] - 48));
			}
			b[0] = (byte) (((bytes[0] - 48) * 16) | (bytes[1] - 48));
			b[1] = (byte) (((bytes[2] - 48) * 16) | (bytes[3] - 48));
			b[2] = (byte) (((bytes[4] - 48) * 16) | (0x0F));
		}
		return b;
	}

    public static int mDolbyType = 0;
    /*
      //0 is mean not dolby
      //1 is mean dolby vision
      //2 is mean dolby audio
      //3 is mean dolby atmos
      //4 is mean dolby vision and audio
      //5 is mean dolby vision and atmos

      //11 is mean dts
      //12 is mean dts hd
      //13 is mean dts express
      //14 is mean dts hd master
      //15 is mean dts x
       */
    public static void setDolbyType(int dolbyType){
        MtkLog.d(TAG,"setDolbyType,dolbyType=="+dolbyType);
        mDolbyType = dolbyType;
    }

    public static int getDolbyType(){
        return mDolbyType;
    }

    public String toString() {
        return "Util";
    }

    public static Bitmap getScaledBitmap(Bitmap bmp) {
      if (bmp == null) {
        return bmp;
      } else {
        int max = Math.max(bmp.getWidth(), bmp.getHeight());
        if (max > 1024)
        {
          float scale = (float) 1024/max;
          Bitmap scaled = Bitmap.createScaledBitmap(bmp, (int)(bmp.getWidth() * scale), (int)(bmp.getHeight() * scale), true);
          return scaled;
        } else {
          return bmp;
        }
      }
    }
}
