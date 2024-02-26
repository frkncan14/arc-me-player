
package com.mediatek.wwtv.mediaplayer.util;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.IRootMenuListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.StrictMode;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.app.ActivityManager;
import android.content.ComponentName;

public class MmpApp extends Application {
  private static final String TAG = "MMPAPP";

  private static List<Activity> mainActivities = new ArrayList<Activity>();
  private static boolean isTopTask = false;
  private boolean mHasEnterMMP;

  public void setEnterMMP(boolean isEnterMMP) {
    Log.d(TAG,"setEnterMMP,isEnterMMP=="+isEnterMMP);
    Util.mIsMmpFlag = isEnterMMP;
    mHasEnterMMP = isEnterMMP;
  }

  public boolean isEnterMMP() {
    return mHasEnterMMP;
  }

  public static boolean isTopTask() {
    return isTopTask;
  }

  public static void setTopTask(boolean isTopTask) {
    MmpApp.isTopTask = isTopTask;
  }

  public synchronized void add(Activity act) {
    mainActivities.add(0, act);
  }

  public static Activity getTopActivity() {
    if (mainActivities != null) {
      return mainActivities.get(mainActivities.size() - 1);
    } else {
      return null;
    }
  }

  private boolean isFirst = true;

  public boolean isFirstFinishAll() {
    return isFirst;
  }

  public void setIsFirst(boolean first) {
    isFirst = first;
  }

  // close all Activity
  public synchronized void finishAll() {
    isFirst = false;
    MtkLog.i(TAG, "finishAll");
    boolean isDLNAInPip = false;
    if (!LogicManager.getInstance(getApplicationContext()).isMMPLocalSource()
        && VideoPlayActivity.getInstance() != null
        && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
      MtkLog.d(TAG, "finishAll is pip, no need stop dlna");
      isDLNAInPip = true;
    }
    try {
      // fix cr DTV00416665
      DLNAManager.getInstance().stopDlna(isDLNAInPip);
    } catch (Exception ex) {
      MtkLog.d(TAG, "finishAll: " + ex);
    }
    for (Activity activity : mainActivities) {
      if (!activity.isFinishing()) {
        if (activity instanceof VideoPlayActivity
            && VideoPlayActivity.getInstance() != null
            && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
          MtkLog.d(TAG, "finishAll is pip, no need finish");
        } else {
          MtkLog.d(TAG, "finishAll is not pip, finish it");
          activity.finish();
        }
      }
    }
    mainActivities.clear();
  }

  public void resetDlna() {
    MtkLog.i(TAG, "resetDlan");
    try {
      DLNAManager.getInstance().resetDlna();
    } catch (Exception ex) {
      MtkLog.d(TAG, "resetDlna: " + ex);
    }

  }

  private synchronized boolean finishPlayActivity() {
    boolean hasMediaPlayActivity = false;
    if (mainActivities != null) {
      for (Activity activity:mainActivities) {
        if (activity instanceof MediaPlayActivity) {
          if (activity instanceof VideoPlayActivity
              && VideoPlayActivity.getInstance() != null
              && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
            MtkLog.d(TAG, "finishPlayActivity is pip, no need finish");
            hasMediaPlayActivity = true;
          } else {
            MtkLog.d(TAG, "finishPlayActivity is not pip, finish it");
            if (!activity.isFinishing()) {
              ((MediaPlayActivity) activity).resetResource();
              activity.finish();
              hasMediaPlayActivity = true;
            }
          }
        }
      }
    }
    return hasMediaPlayActivity;
  }

  public void finishMediaPlayActivity() {
    if (mainActivities != null) {
      for (Activity activity:mainActivities) {
        if (activity instanceof MediaPlayActivity) {
          MtkLog.d(TAG, "finishMediaPlayActivity:");
          activity.finish();
        }
      }
    }
  }

  //
  // //add by 3d gamekit.
  public void finish3DBrowseActivity() {
  	  Log.i(TAG, "finish3DBrowseActivity");
  }

  public synchronized void remove(Activity activity) {
    if (!mainActivities.isEmpty()) {
      mainActivities.remove(activity);
    }
  }

  boolean registed = false;

  public void register() {
    Log.i(TAG, "register:" + registed);
    if (!registed) {
      registed = true;
      ifilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
      ifilter.addAction(Util.SOURCEACTION);
      ifilter.addAction(Util.STARTACTION);
      ifilter.addAction(Intent.ACTION_SCREEN_OFF);
      ifilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
      registerReceiver(mReceiver, ifilter);
    }
  }

  public void unregister() {
    Log.i(TAG, "unregister registed:" + registed);
    if (registed){
      unregisterReceiver(mReceiver);
    }
    registed = false;
  }

  private List<IRootMenuListener> mRootMenuListenerList = new ArrayList<IRootMenuListener>();
//  private final IRootMenuListener mRootMenuListener = null;

  public void registerRootMenu(IRootMenuListener listener) {
    if (mRootMenuListenerList == null) {
      mRootMenuListenerList = new ArrayList<IRootMenuListener>();
    }
    Log.d(TAG, "1 mRootMenuListenerList size():" + mRootMenuListenerList.size());
    if (!mRootMenuListenerList.contains(listener)) {
      mRootMenuListenerList.add(listener);
    }
    Log.d(TAG, "2 mRootMenuListenerList size():" + mRootMenuListenerList.size());
//    mRootMenuListener = listener;
    if (!registed) {
      register();
    }
  }

  public void removeRootMenuListener(IRootMenuListener listener) {
    if (mRootMenuListenerList != null) {
      mRootMenuListenerList.remove(listener);
      Log.d(TAG, "3 mRootMenuListenerList size():" + mRootMenuListenerList.size());
    }
  }

  public boolean isEcoModeAppRunning(Context context) {
    boolean result = false;
    try {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        String packString = cn.getPackageName();
        if(packString.equalsIgnoreCase("com.arcelik.ecomode")) {
          result = true;
        }
    }
    catch (Exception ex) {
        ex.printStackTrace();
        result = false;
    }
    return result;
  }

  private final IntentFilter ifilter = new IntentFilter();

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      Log.i(TAG, "onReceiveintent.getAction():" + intent.getAction());
      if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent
          .getAction()) || Util.SOURCEACTION.equals(intent.getAction())) {
        Log.i(TAG, "received ACTION_CLOSE_SYSTEM_DIALOGS || SOURCEACTION");

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            if(!isEcoModeAppRunning(getApplicationContext())){
              if (LastMemory.getLastMemortyType(getApplicationContext())
                  == LastMemory.LASTMEMORY_TIME) {
                  LastMemory.saveLastMemory(getApplicationContext());
              }

              //add for CR:DTV00961372
              if (Util.SOURCEACTION.equals(intent.getAction())){
                  LogicManager.getInstance(getApplicationContext()).finishVideo();
              }

              if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                  MtkLog.d(TAG, "reason = " + intent.getExtra("reason"));
                  if (!"homekey".equals(intent.getExtra("reason"))) {
                      return;// only handle home key
                  }
              }
              finishAll();
              DevManager.getInstance().destroy();

              if (MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
                MediaMainActivity.mAutoTestFilePath = null;
                MediaMainActivity.mAutoTestFileDirectorys = null;
                MediaMainActivity.mAutoTestFileName = null;
              }
              if (isEnterMMP()) {
                setEnterMMP(false);
              }
              if (Thumbnail.getInstance() != null) {
                Thumbnail.getInstance().setRestRigionFlag(false);
              }
              if (mRootMenuListenerList != null) {
                for (IRootMenuListener tempListener:mRootMenuListenerList) {
                  tempListener.handleRootMenu();
                }
              }
              if (LogicManager.getInstance(getApplicationContext()).isAudioOnly()) {
                LogicManager.getInstance(getApplicationContext()).setAudioOnly(false);
              }
              if (!Util.mIsEnterPip) {
                if (!Util.mIsUseEXOPlayer){
                  AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
                }
              }
              finishPlayActivity();
            }
          }
        }, 2000);

      } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
        if (!Util.mIsUseEXOPlayer){
            AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
        }
        Util.exitAndroidSetting(context);
        finishPlayActivity();
        if (isEnterMMP()) {
          setEnterMMP(false);
        }
        unregister();
        if (Util.isTopMMpActivity(context)){
          Util.goToMainActivity(getApplicationContext());
        }
      } else if(Util.STARTACTION.equals(intent.getAction())) {
        String pkgName = intent.getStringExtra("pkg");
        Log.d(TAG, "pkg:" + pkgName);

        boolean isCurGoogleSettingActivity = GetCurrentTask.getInstance(getApplicationContext()).isCurGoogleSettingActivity();
        boolean isCurMiracastActivity = GetCurrentTask.getInstance(getApplicationContext()).isCurMiracastActivity();
        boolean isCurPipActivity = GetCurrentTask.getInstance(getApplicationContext()).isCurPipActivity();
        if(!Util.isMMpActivity(context) && !"com.android.tv.settings".equals(pkgName)
                && !"com.google.android.apps.tv.launcherx".equals(pkgName)
                && !"com.mediatek.wwtv.tvcenter".equals(pkgName)
                && !"com.mediatek.tv.agent".equals(pkgName)
                && !"com.mediatek.tv.service".equals(pkgName)
                && !isCurGoogleSettingActivity
                && !isCurMiracastActivity
                && !isCurPipActivity
                && !"com.google.android.katniss".equals(pkgName)
                && !"com.arcelik.ecomode".equals(pkgName)
                && !"com.google.android.apps.tv.dreamx".equals(pkgName)) {
            Log.d (TAG, "Calling exitMmpActivity, pkgName: " + pkgName
                + ", isCurGoogleSettingActivity: " + isCurGoogleSettingActivity
                + ", isCurMiracastActivity: " + isCurMiracastActivity);
            Util.logLife(TAG, "top is not mmp, go to finish.");
            Util.exitMmpActivity(getApplicationContext());
        }
      }
    }
  };

  boolean isStrict = false;

  @Override
  public void onCreate() {
    Log.i(TAG, "onCreate:");
    super.onCreate();
    if (isStrict) {
      StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
          .detectCustomSlowCalls()
          .detectDiskReads()
          .detectDiskWrites()
          .detectNetwork()
          .penaltyLog()
          .penaltyFlashScreen()
          .build());

      StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
          .detectLeakedSqlLiteObjects()
          .detectLeakedClosableObjects()
          .penaltyLog()
          .build());
    }

  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    Log.i(TAG, "onTerminate:");
    /* destroy global info */
    /*
     * (new MtkTvHighLevel()).stopTV(); CommonIntegration.getInstance().setOpacity(255);
     * KeyDispatch.remove(); TvCallbackHandler.getInstance().removeAll();
     * CommonIntegration.remove(); InputSourceManager.remove();
     */
  }
}
