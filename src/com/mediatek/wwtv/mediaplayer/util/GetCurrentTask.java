
package com.mediatek.wwtv.mediaplayer.util;

import java.util.List;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

public final class GetCurrentTask {

  private static final String TAG = "GetCurrentTask";

  public static final int START_TASK_TO_FRONT = 2;
  private Context mContext = null;
  private static GetCurrentTask curTask = null;

  private GetCurrentTask(Context context) {
    mContext = context;
  }

  public static synchronized GetCurrentTask getInstance(Context context) {
    if (curTask == null) {
      curTask = new GetCurrentTask(context.getApplicationContext());
    }
    return curTask;
  }

  private ComponentName getCurRunningCN() {
    ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
    return am.getRunningTasks(1).get(0).topActivity;
  }

  // public List<RunningTaskInfo> getRunningTasks(){
  // ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
  // List<RunningTaskInfo> mTaskList =am.getRunningTasks(12);
  // return mTaskList ;
  // }

  public String getCurRunningPackageName() {
    String className = null;
    ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
    if (appList != null && !appList.isEmpty()) {
      for (ActivityManager.RunningAppProcessInfo topAPP : appList) {
        if (topAPP != null) {
          className = topAPP.processName;
          MtkLog.d(TAG, "Current Running Activity xx Name: " + className);
          break;
        }
      }
    }
    MtkLog.d(TAG, "Current Running Activity Name: " + className);
    return className;
  }

  // public String getCurRunningPKG(){
  // String packageName = getCurRunningCN().getPackageName();
  // MtkLog.d(TAG," Current Running Package Name: " + packageName);
  // return packageName;
  // }

  public String getCurRunningClassName() {
    String className = getCurRunningCN().getClassName();
    MtkLog.d(TAG, "Current Running Activity Name: " + className);
    return className;
  }

  public boolean isCurTaskTKUI() {
    return getCurRunningCN().getPackageName().equals("com.mediatek.wwtv.tvcenter");
  }

  public boolean isCurActivtyGridActivity() {
    return getCurRunningClassName().contains(
        "com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesGridActivity");
  }

  public boolean isCurActivityTkuiMainActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.nav.TurnkeyUiMainActivity");
  }

  public boolean isCurMediaActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.mmp.MeidaMainActivity");
  }

  public boolean isCurMenuMainActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.setting.SettingActivity");
  }

  public boolean isCurMiracastActivity() {
    return getCurRunningClassName().contains("com.mediatek.miracast");
  }

  public boolean isCurPipActivity() {
    return getCurRunningClassName().contains("com.android.systemui.pip.tv.PipMenuActivity");
  }

  public boolean isCurGoogleSettingActivity() {
    return getCurRunningClassName().equals("com.android.tv.settings.MainSettings");
  }

  public boolean isCurNetflixActivity() {
    return getCurRunningClassName().equals("com.netflix.ninja.MainActivity");
  }

  public boolean isCurOADActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.oad.NavOADActivity");
  }

  public boolean isCurWizardActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.wizard.SetupWizardActivity");
  }

  public boolean isCurEPGActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.epg.us.EPGUsActivity")
        || getCurRunningClassName().equals("com.mediatek.ui.epg.eu.EPGEuActivity")
        || getCurRunningClassName().equals("com.mediatek.ui.epg.sa.EPGSaActivity");
  }

  public boolean isCurEUEPGActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.epg.eu.EPGEuActivity");
  }

}
