
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.dm.MountPoint;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.Lists;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import android.util.Log;

public final class MultiFilesManager extends FilesManager<FileAdapter> implements
    Observer {
  private static final String TAG = "MultiFilesManager";

  public static final String ROOT_PATH = "/";
  private static final String FIRST_EXTEND_DEVICE = "/storage/sdcard";
  private static final String FIRST_EXTEND_DEVICE_KEY = "sdcard";
  public static final String COMMON_EXTEND_DEVICE = "usbdisk";

  private static final String EXTEND_F = "mnt";
  private static final String EXTEND_S = "usb";
  private static final String EXTEND_F2 = "storage";

  private String mCurrentUsbdisBlock = null;
  public static final int SOURCE_ALL = FileConst.SRC_ALL;
  public static final int SOURCE_LOCAL = FileConst.SRC_USB;
  public static final int SOURCE_SMB = FileConst.SRC_SMB;
  public static final int SOURCE_DLNA = FileConst.SRC_DLNA;

  public static final int NO_DEVICES = 0;
  public static final int ONE_DEVICES = 1;
  public static final int MORE_DEVICES = 2;

  public static final int SUB_DIRCTORY = 1;
  private static final int MSG_MOUNTED = 4;
  private static final int MSG_EJECTING = 5;
  private static final int MSG_DISCONNECTED = 6;

  private static MultiFilesManager sInstance;

  private LocalFilesManager mLocalManager = null;
  private SmbFilesManager mSmbManager = null;
  private LenFilesManager mLenFilesManager = null;
  private DlnaFilesManager mDlnaManager = null;
  private DevManager mDevManager = null;

  private int mSourceType;

  private List<FileAdapter> mLocalDevices;
  private List<FileAdapter> mSmbDevices;
  private List<FileAdapter> mDlnaDevices;
  private List<FileAdapter> mAllDevices;

  private static boolean mSmbAvailable =  false;
  private static boolean mDlnaAvailable;

  private List<String> mLeftLocalDevices;
  private List<String> mFoundLocalDevices;
  private List<String> mVirtualLocalDevices;

  // Add by Dan for fix bug DTV00374299
  private int mMountedIsoCount = 0;
  private final Object mFilesLock = new Object();

  private MultiFilesManager(Context context) {
    super(context);
    mSourceType = SOURCE_ALL;

    initDevices();
    initFilesManager(context);
    initDevicesManager();
  }

  private void initDevices() {
    mLocalDevices = Lists.newArrayList();
    mSmbDevices = Lists.newArrayList();
    mDlnaDevices = Lists.newArrayList();
    mLeftLocalDevices = Lists.newArrayList();
    mFoundLocalDevices = Lists.newArrayList();
    mVirtualLocalDevices = Lists.newArrayList();
    mAllDevices = null;
  }

  public String getCurDevName() {
    if (mSourceType != SOURCE_ALL) {
      if (null != mLocalDevices) {
        String[] paths = mCurrentPath.split("/");
        MtkLog.i(TAG, " path len :" + paths.length);
        for (String path : paths) {
          MtkLog.i(TAG, " path  :" + path);
        }
        String devName = null;
        if (paths.length > 2) {
          if (COMMON_EXTEND_DEVICE.equals(paths[2]) || FIRST_EXTEND_DEVICE_KEY.equals(paths[2])) {
            devName = getCurrentUsbdisBlock();
          } else if (EXTEND_S.equals(paths[2])) {
            devName = paths[3];
          } else if (EXTEND_F.equals(paths[1]) || EXTEND_F2.equals(paths[1])) {
            // Virtual Devices
            devName = paths[2];
          }
          return devName;
        }
      }
    }
    return null;
  }

  private void initFilesManager(Context context) {
    mLocalManager = LocalFilesManager.getInstance(context);

    if (mSmbAvailable) {
      if (true == Feature.isSupportCnSamba()) {
        mLenFilesManager = LenFilesManager.getInstance(context);
        mLenFilesManager.setRootPath("smb://");
      } else {
        mSmbManager = SmbFilesManager.getInstance(context);
        mSmbManager.setRootPath("smb://");
      }
    }

    if (mDlnaAvailable) {
      mDlnaManager = DlnaFilesManager.getInstance(context);
    }
  }

  private String mDevicePath;
  private final DevListener mDevListener = new DevListener() {
    public void onEvent(DeviceManagerEvent event) {
      Log.d(TAG, "Device Event : " + event.getType() + "  :" + event.getMountPointPath()
          + "   :" + event.getDevicePath());
      int type = event.getType();
      String devicePath = event.getMountPointPath();
      mDevicePath = devicePath;
      if (type != DeviceManagerEvent.umounted) {
        mLeftLocalDevices.clear();
      }

      switch (type) {
        case DeviceManagerEvent.connected:
          Log.d(TAG, "Device Event Connected!!");
          break;
        case DeviceManagerEvent.disconnected:
          Log.d(TAG, "Device Event Disconnected!!");
          mHandler.sendEmptyMessage(MSG_DISCONNECTED);
          // onLocalDevicesStateChanged();
          break;
        case DeviceManagerEvent.mounted:
          Log.d(TAG, "Device Event Mounted!!");
          if (FIRST_EXTEND_DEVICE.equals(devicePath)) {
            try {
              Thread.sleep(500);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
          mHandler.sendEmptyMessage(MSG_MOUNTED);
          break;
        case DeviceManagerEvent.umounted:
          Log.d(TAG, "Device Event Unmounted!!");
          //LogicManager.getInstance(mContext).onDevUnMount(devicePath);
          //stopDecode();
          //onLocalDevicesStateChanged(devicePath);
          break;
        case DeviceManagerEvent.ejecting:
          Log.d(TAG, "Device Event ejecting!!");
          mHandler.sendEmptyMessage(MSG_EJECTING);
          break;
        case DeviceManagerEvent.unsupported:
          Log.d(TAG, "Device Event Unsupported!!");
          break;
        default:
          break;
      }
    }
  };

  public void netWorkStateChange(boolean connected) {
    if (!connected) {
      onNetDevicesStateChanged();
    }
  }

  private void stopDecode() {
    Log.d(TAG, "  stopdecode() begin -----------");
    LogicManager.getInstance(mContext).stopDecode();
    Log.d(TAG, "stopdecode() finish ------------");

  }

  /**
   * Close auido only.
   */
  private void setAuidoOnlyOff() {
    LogicManager logicManager = LogicManager.getInstance(mContext);

    if (logicManager.isAudioOnly()) {
      logicManager.setAudioOnly(false);
    }
  }

  public void initDevicesManager() {
    try {
      mDevManager = DevManager.getInstance();
      mDevManager.removeDevListeners();
      mDevManager.addDevListener(mDevListener);
    } catch (ExceptionInInitializerError e) {
      mDevManager = null;
    }
  }

  public static synchronized MultiFilesManager getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new MultiFilesManager(context.getApplicationContext());
      sInstance.setRootPath(ROOT_PATH);
    }

    return sInstance;
  }

  public static boolean hasInstance() {
    return sInstance != null;
  }


  public static void setSmbAndDlnaAvailable(boolean smbAvailable, boolean dlnaAvailable) {
      mSmbAvailable = smbAvailable;
      mDlnaAvailable = dlnaAvailable;
  }

  public boolean login(String path, String userName, String userPwd) {
    if (true == Feature.isSupportCnSamba()) {
      return mLenFilesManager.login(path, userName, userPwd);
    }
    return mSmbManager.login(path, userName, userPwd);
  }

  @Override
  public List<FileAdapter> listAllFiles(String path) {
    MtkLog.d(TAG, "Source Type : " + mSourceType + "--List Path : " + path
        + "--listAllFiles start = " + mFiles.size() + "  mAllDevices:" + mAllDevices);
    synchronized (mFilesLock) {
      mFiles.clear();
      if (mSourceType == SOURCE_ALL) {
        if (mAllDevices != null) {
          mAllDevices.clear();
          if (MediaMainActivity.mIsDlnaAutoTest) {
            mAllDevices.addAll(mDlnaDevices);
          } else {
            mAllDevices.addAll(mLocalDevices);
            if (true == Feature.isSupportCnSamba()) {
              mLenFilesManager.backToRoot();          
            }
            mAllDevices.addAll(mSmbDevices);
            mAllDevices.addAll(mDlnaDevices);
          }
          mFiles.addAll(mAllDevices);
          if (!mSmbAvailable && !mDlnaAvailable
              && getAllDevicesNum() == ONE_DEVICES) {
            mHandler.sendEmptyMessage(SUB_DIRCTORY);
          }

        } else {
          mLeftLocalDevices.clear();
          getLocalDevices();

          if (mSmbAvailable) {
            getSmbDevices();
          }
          MtkLog.d(TAG, "listAllFiles mAllDevices: " + mAllDevices
              + "  mSmbDevices:" + mSmbDevices.size());
          if (mAllDevices == null) {
            mAllDevices = Lists.newArrayList();
            if (!MediaMainActivity.mIsDlnaAutoTest) {
              mAllDevices.addAll(mLocalDevices);
              mAllDevices.addAll(mSmbDevices);
            }
          } else {
            mAllDevices.clear();
          }
          mFiles.addAll(mAllDevices);
          // getDlnaDevices will trigger mFiles.addAll(mAllDevices) again in update().
          if (mDlnaAvailable) {
            getDlnaDevices();
          }
          if (!mSmbAvailable && !mDlnaAvailable
              && getAllDevicesNum() == ONE_DEVICES) {
            mHandler.sendEmptyMessage(SUB_DIRCTORY);
          }

          // mFiles.addAll(mAllDevices);
        }

      } else {
        if (mSourceType == SOURCE_LOCAL) {
          //if (VendorProperties.allmedia().orElse(0) != 0) {
          //  mFiles = mLocalManager.listAllFiles(path, FileConst.MMP_TYPE_ALL);
          //} else {
          mFiles = mLocalManager.listAllFiles(path);
          //}
        } else if (mSourceType == SOURCE_SMB) {
          if (true == Feature.isSupportCnSamba()) {
            mFiles = mLenFilesManager.listAllFiles(path);
          } else {
            mFiles = mSmbManager.listAllFiles(path);
          }
        } else if (mSourceType == SOURCE_DLNA) {
          mDlnaManager.listAllFiles(path);
        }
      }
      MtkLog.d(TAG, "listAllFiles end:" + mFiles.size());

      logFiles(TAG);

      return mFiles;
    }
  }

  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      switch (msg.what) {
        case SUB_DIRCTORY:
          if (getAllDevicesNum() == ONE_DEVICES) {
            setChanged();
            notifyObservers(REQUEST_SUB_DIRECTORY);
          }
          break;

        case MSG_EJECTING:
            stopDecode();
            mLeftLocalDevices.add(getDevNameByPath(mDevicePath));
            onLocalDevicesStateChanged(mDevicePath);
            LogicManager.getInstance(mContext).onDevUnMount(mDevicePath);
            setAuidoOnlyOff();
            break;

        case MSG_MOUNTED:
            onLocalDevicesStateChanged(mDevicePath);
            break;

        case MSG_DISCONNECTED:
            stopDecode();
            break;

        default:
          break;
      }
    }
  };

  private void onLocalDevicesStateChanged(String devicePath) {
    Log.d(TAG, "onLocalDevicesStateChanged 1 devicePath:" + devicePath);
    if (mAllDevices != null) {
      MtkLog.d(TAG, "onLocalDevicesStateChanged 2 ");
      //findLocalLeftDevices();
      findLocalAddDevices();
      getLocalDevices();
      mAllDevices.clear();
      if (MediaMainActivity.mIsDlnaAutoTest) {
        mAllDevices.addAll(mDlnaDevices);
      } else {
        mAllDevices.addAll(mLocalDevices);
        mAllDevices.addAll(mSmbDevices);
        mAllDevices.addAll(mDlnaDevices);
      }
    } else {
      MtkLog.d(TAG, "onLocalDevicesStateChanged 3 ");
      return;
    }
    MtkLog.d(TAG, "onLocalDevicesStateChanged 4 ");
    if (mSourceType == SOURCE_ALL || mSourceType == SOURCE_LOCAL) {
      checkDevicesStateChanged(devicePath);
    }
  }

  private void onNetDevicesStateChanged() {
    MtkLog.d(TAG, "onNetDevicesStateChanged 1 ");
    if (mAllDevices != null) {
      MtkLog.d(TAG, "onNetDevicesStateChanged 2 ");
      // findLocalLeftDevices();
      // findLocalAddDevices();
      // getLocalDevices();
      mAllDevices.clear();
      mSmbDevices.clear();
      mDlnaDevices.clear();
      if (!MediaMainActivity.mIsDlnaAutoTest) {
        mAllDevices.addAll(mLocalDevices);
      }
    } else {
      MtkLog.d(TAG, "onNetDevicesStateChanged 3 ");
      return;
    }
    MtkLog.d(TAG, "onNetDevicesStateChanged 4 ");
    // network status change,not affect local operation
    if (mSourceType == SOURCE_LOCAL) {
      return;
    }
    setChanged();
    notifyObservers(REQUEST_BACK_TO_ROOT);

  }

//  public void onNetSuccessDevicesStateChanged() {
//    MtkLog.d(TAG, "onNetSuccessDevicesStateChanged 1 ");
//    if (mAllDevices != null) {
//      MtkLog.d(TAG, "onNetSuccessDevicesStateChanged 2 ");
//      setChanged();
//      notifyObservers(REQUEST_BACK_TO_ROOT);
//    } else {
//      MtkLog.d(TAG, "onNetSuccessDevicesStateChanged 3 ");
//      return;
//    }
//    MtkLog.d(TAG, "onNetSuccessDevicesStateChanged 4 ");
//
//  }
//
//  private void closePlayer() {
//
//    LogicManager.getInstance(mContext).onDevUnMount();
//    //close should in activity
////    ((MmpApp) mContext.getApplicationContext())
////        .finishMediaPlayActivity();
//    // add by 3d gamekit start
////    close3DBrowse();
//    // add by 3d gamekit end
//  }

//  // add by 3d gamekit.
//  private void close3DBrowse() {
//    ((MmpApp) mContext.getApplicationContext()).finish3DBrowseActivity();
//  }

  private void checkDevicesStateChanged(String devicePath) {
    Log.d(TAG, "checkDevicesStateChanged~~~~~~~~~~,devicePath=="+devicePath);
    int deviceNum = getAllDevicesNum();
    List<String> leftLDevs = getLocalLeftDevices();
    List<String> foundLDevs = getLocalAddDevices();
    String curDevName = getCurDevName();
    String curPath = getCurrentPath();
    String curPlayDev = null;
    if (LogicManager.getInstance(mContext).isAudioStarted() && LogicManager.getInstance(mContext).getAudioFilePath() != null) {
      curPlayDev = getDevNameByPath(LogicManager.getInstance(mContext).getAudioFilePath());
    }
    Log.d(TAG, "checkDevicesStateChanged curPlayDev : " + curPlayDev + "curPath:"
        + getCurrentPath() + "curDevName:" + getCurDevName());

    if (checkVirtualDevice()) {
//      closePlayer();
//      LogicManager.getInstance(mContext).onDevUnMount(devicePath);
      setChanged();
      notifyObservers(REQUEST_BACK_TO_ROOT);
      return;
    }
    MtkLog.d(TAG, "checkDevicesStateChanged  deviceNum Type=" + deviceNum);
    switch (deviceNum) {
      case MORE_DEVICES: {

        if (!leftLDevs.isEmpty()) {
          int leftDevType = 0;
          for (String devName : leftLDevs) {
            MtkLog.d(TAG, "MORE_DEVICES  leftLDevs devName:" + devName);
            if (devName.equals(curPlayDev)) {
              leftDevType += 1;
              break;
            }
            if (devName.equals(curDevName)) {
              leftDevType += 2;
              break;
            }
          }
          switch (leftDevType) {
            case 1:
//              LogicManager.getInstance(mContext).onDevUnMount(devicePath);
//              closePlayer();
              break;
            case 2:
              setChanged();
              notifyObservers(REQUEST_BACK_TO_ROOT);
              break;
//            case 3:
////              LogicManager.getInstance(mContext).onDevUnMount(devicePath);
////              closePlayer();
//              setChanged();
//              notifyObservers(REQUEST_BACK_TO_ROOT);
//              break;
			default:
				break;
          }

        }

        if (curPath != null && curPath.equals(ROOT_PATH)) {
          setChanged();
          notifyObservers(REQUEST_BACK_TO_ROOT);
        }
      }
        break;
      case NO_DEVICES: {
//        LogicManager.getInstance(mContext).onDevUnMount(devicePath);
//        closePlayer();
        setChanged();
        notifyObservers(REQUEST_BACK_TO_ROOT);

      }
        break;
      case ONE_DEVICES: {
        if (!leftLDevs.isEmpty()) {
          int leftDevType = 0;
          for (String devName : leftLDevs) {

            MtkLog.d(TAG, "checkDevicesStateChanged ONE_DEVICES Left devName  " + devName);

            if (devName.equals(curPlayDev)) {
              leftDevType += 1;
            }

            if (devName.equals(curDevName)) {
              leftDevType += 2;
            }
          }
          MtkLog.d(TAG, "checkDevicesStateChanged leftDevType : " + leftDevType);

          switch (leftDevType) {
            case 1:
//              LogicManager.getInstance(mContext).onDevUnMount(devicePath);
//              closePlayer();
              break;
            case 2:
              setChanged();
              notifyObservers(REQUEST_SUB_DIRECTORY);
              break;
            case 3:
//              LogicManager.getInstance(mContext).onDevUnMount(devicePath);
//              closePlayer();
              setChanged();
              notifyObservers(REQUEST_SUB_DIRECTORY);
              break;
			 default:
			 	break;
          }

          if (curPath.equals(ROOT_PATH)) {
            setChanged();
            notifyObservers(REQUEST_SUB_DIRECTORY);
            break;

          }
        } else if (foundLDevs.size() == 1) {
          setChanged();
          notifyObservers(REQUEST_SUB_DIRECTORY);
        }
      }
        break;
      default:
        break;
    }
  }

  private String getDevNameByPath(String path) {
    String[] paths = path.split("/");
    String devName = null;
    if (paths.length > 2) {
      if (COMMON_EXTEND_DEVICE.equals(paths[2]) || FIRST_EXTEND_DEVICE_KEY.equals(paths[2])) {
        MtkLog.d(TAG, "mCurrentUsbdisBlock:" + mCurrentUsbdisBlock);
        devName = mCurrentUsbdisBlock;
      } else if (EXTEND_S.equals(paths[2])) {
        devName = paths[3];
      } else if (EXTEND_F.equals(paths[1]) || EXTEND_F2.equals(paths[1])) {
        // Virtual Devices
        devName = paths[2];
      }
      return devName;
    }

    return null;

  }

  public void updateSambaDevice() {
    synchronized (mFilesLock) {
      if (true == Feature.isSupportCnSamba()) {
        mSmbDevices.clear();
        mSmbDevices.addAll(mLenFilesManager.listAllDevices());
        mAllDevices.addAll(mSmbDevices);
        final String curPath = getCurrentPath();
        if (curPath != null && curPath.equals(ROOT_PATH)) {
          mFiles.addAll(mSmbDevices);
        }
      }
    }
  }
  
  public int getAllDevicesNum() {
    if (mLocalDevices.size() == 1 && mSmbDevices.isEmpty()
        && mDlnaDevices.isEmpty()) {
      MtkLog.d(TAG, "getAllDevicesNum 1");
      return ONE_DEVICES;
    } else if (mLocalDevices.isEmpty() && mSmbDevices.isEmpty()
        && mDlnaDevices.isEmpty()) {
      MtkLog.d(TAG, "getAllDevicesNum 0");
      return NO_DEVICES;
    } else {
      return MORE_DEVICES;
    }

  }

  // Add by Dan for fix bug DTV00374299
  public int getMountedIsoCount() {
    return mMountedIsoCount;
  }

  public String getFirstDeviceMountPointPath() {
    if (mLocalDevices == null || mLocalDevices.isEmpty()) {
      return null;
    }
    return mLocalDevices.get(0).getPath();
  }

  public List<FileAdapter> getLocalDviceAdapter() {
    return mLocalDevices;
  }

  public boolean isContainMountPoint(String path) {
    boolean isContained = false;
    MtkLog.i(TAG, "isContainMountPoint Path:" + path);
    for (FileAdapter ad : mLocalDevices) {
      MtkLog.i(TAG, "isContainMountPoint ad.getPath:" + ad.getPath());
      if (ad.getPath().equals(path)) {
        isContained = true;
        break;
      }
    }
    return isContained;
  }

  public List<String> getLocalLeftDevices() {
    return mLeftLocalDevices;
  }

  public List<String> getLocalAddDevices() {
    return mFoundLocalDevices;
  }

  /**
   * check virtual device.
   *
   * @return if true, checked. else unchecked.
   */
  public boolean checkVirtualDevice() {
    List<String> devices = mVirtualLocalDevices;
    if (mDevManager != null && devices != null && !devices.isEmpty()) {
      for (String devName : devices) {
        if (mDevManager.isVirtualDev(devName)) {
          MtkLog.d(TAG, "Get Mount List :  true");
          return true;
        }
      }
    }
    MtkLog.d(TAG, "Get Mount List :  false");
    return false;
  }

  private void findLocalAddDevices() {
    if (mFoundLocalDevices == null || mVirtualLocalDevices == null) {
      return;
    }
    mFoundLocalDevices.clear();
    mVirtualLocalDevices.clear();
    if (mDevManager != null) {
      List<MountPoint> devices = mDevManager.getMountList();

      if (null != devices && !devices.isEmpty()) {
        for (MountPoint mountPoint : devices) {
          int i = 0;
          for (FileAdapter localD : mLocalDevices) {
            if (localD.getName().equals(mountPoint.mDeviceName)) {
              break;
            }
            i++;
          }

          if (i >= mLocalDevices.size()) {
            mFoundLocalDevices.add(mountPoint.mDeviceName);
            if (mDevManager.isVirtualDev(mountPoint.mMountPoint)) {
              mVirtualLocalDevices.add(mountPoint.mMountPoint);
            }
          }
        }
      }
    }
  }

/*
  private void findLocalLeftDevices() {
    mLeftLocalDevices.clear();
    if (mDevManager != null) {
      ArrayList<MountPoint> devices = mDevManager.getMountList();

      if (null != devices && mLocalDevices.size() > 0) {
        for (FileAdapter localD : mLocalDevices) {
          LocalFileAdapter locDev = (LocalFileAdapter) localD;
          int i = 0;
          for (MountPoint mountPoint : devices) {
            if (locDev.getDeviceName().equals(mountPoint.mDeviceName)) {
              break;
            }
            i++;
          }

          if (i >= devices.size()) {
            mLeftLocalDevices.add(locDev.getFileName());
          }
        }
      }
    }
  }
  */

  public MountPoint getCurrentDevice(String name) {
    if (null == name){
        return null;
    }
    if (mDevManager != null) {
      List<MountPoint> devices = mDevManager.getMountList();

      if (null != devices && !mLocalDevices.isEmpty()) {
          for (MountPoint mountPoint : devices) {
            MtkLog.d(TAG,"mMountPoint=="+mountPoint.mMountPoint);
            if (mountPoint.mMountPoint.contains(name)) {
              return mountPoint;
            }
          }
      }
    }

    return null;
  }

  //remove synchronized
  public void getLocalDevices() {
    if (mDevManager != null) {
      // Add by Dan for fix bug DTV00374299
      mMountedIsoCount = 0;
      mLocalDevices.clear();
      List<MountPoint> devices = mDevManager.getMountList();
      if (null != devices && !devices.isEmpty()) {
        for (MountPoint mountPoint : devices) {
          Log.d(TAG, "mMountPoint : " + mountPoint.mMountPoint + ", mVolumeLabel:"
              + mountPoint.mVolumeLabel + ", mDeviceName = " + mountPoint.mDeviceName);

          boolean isLocalDevice = true;
          if (null != mLeftLocalDevices && !mLeftLocalDevices.isEmpty()){
              for (String leftDeviceName : mLeftLocalDevices) {
                Log.d(TAG, "leftDeviceName=="+leftDeviceName);
                if (leftDeviceName.equals(getDevNameByPath(mountPoint.mMountPoint))) {
                  isLocalDevice = false;
                  break;
                }
              }
          }

          if (isLocalDevice){
              // Add by Dan for fix bug DTV00374299
              if (mDevManager.isVirtualDev(mountPoint.mMountPoint)) {
                mMountedIsoCount++;
              }
              mLocalDevices.add(new LocalFileAdapter(
                  mountPoint.mMountPoint, mountPoint.mDeviceName, mountPoint.mVolumeLabel));
          }
        }
      } else {
        Log.d(TAG, "Get Mount List devices is null or size is 0.");
      }
    } else {
      mLocalDevices.add(new LocalFileAdapter(Environment
          .getExternalStorageDirectory()));
    }
  }

  private void getSmbDevices() {
    if (true == Feature.isSupportCnSamba()) {
      if (mLenFilesManager != null) {
        mSmbDevices.clear();
        mSmbDevices.addAll(mLenFilesManager.listAllFiles(mLenFilesManager.getRootPath()));
      }
    } else {
      if (mSmbManager != null) {
        mSmbDevices.clear();
        mSmbDevices.addAll(mSmbManager.listAllFiles(mSmbManager.getRootPath()));
      }
    }
  }

  private void getDlnaDevices() {
    if (mDlnaManager != null) {
      mDlnaManager.clearHistory();
      mDlnaManager.listAllFiles(null);
    }
  }

  private void addFiles(List<FileAdapter> files) {
    MtkLog.d(TAG, "Add Files : " + files.size() + ", source type = " + mSourceType);
    synchronized (mFilesLock) {
      mFiles.clear();
      if (mSourceType == SOURCE_ALL) {
        mDlnaDevices.clear();
        mDlnaDevices.addAll(files);
        if (mAllDevices == null) {
          mAllDevices = Lists.newArrayList();
        }
        mAllDevices.clear();
        if (MediaMainActivity.mIsDlnaAutoTest) {
          mAllDevices.addAll(mDlnaDevices);
        } else {
          mAllDevices.addAll(mLocalDevices);
          mAllDevices.addAll(mSmbDevices);
          mAllDevices.addAll(mDlnaDevices);
        }

        mFiles.addAll(mAllDevices);
      } else {
        mFiles.addAll(files);
      }

      logFiles(TAG);
    }
  }

  public int getCurrentSourceType() {
    return mSourceType;
  }

  public void setCurrentSourceType(int type) {

    mSourceType = type;
  }

  public int getSourceType(String path) {
    int source = mSourceType;
    List<FileAdapter> dlnaDevices = mDlnaDevices;
    if (path == null || ROOT_PATH.equals(path)) {
      source = SOURCE_ALL;
    } else if (source == SOURCE_ALL) {
      for (FileAdapter file : mLocalDevices) {
        if (file.getAbsolutePath().equals(path)) {
          source = SOURCE_LOCAL;
          mLocalManager.setRootPath(path);
          MtkLog.d(TAG, "Source : LOCAL!!");
          return source;
        }
      }

      for (FileAdapter file : mSmbDevices) {
        if (file.getAbsolutePath().equals(path)) {
          source = SOURCE_SMB;
          MtkLog.d(TAG, "Source : SMB!!");
          return source;
        }
      }

      synchronized (dlnaDevices) {
        for (FileAdapter dlnaDevice : dlnaDevices) {
          if (path != null
              && dlnaDevice != null
              && path.equals(dlnaDevice.getAbsolutePath())) {
            source = SOURCE_DLNA;
            MtkLog.d(TAG, "Source : DLNA!!");
            return source;
          }
        }
      }
    }

    return source;
  }

  // add by xudong
  private void setCurrentUsbdisBlock(String path) {
    String[] paths = path.split("/");
    if (paths.length > 2) {
      if (COMMON_EXTEND_DEVICE.equals(paths[2]) || (FIRST_EXTEND_DEVICE_KEY.equals(paths[2]))) {
        // devName = "sda1";
        if (mDevManager != null) {
          List<MountPoint> devices = mDevManager
              .getMountList();
          if (null != devices && !devices.isEmpty()) {
            mCurrentUsbdisBlock = devices.get(0).mDeviceName;
          }
        }
      }
    }
  }

  private String getCurrentUsbdisBlock() {
    return mCurrentUsbdisBlock;
  }

  // end
  @Override
  public void setCurrentPath(String path) {
    MtkLog.d(TAG, "Set Path : " + path);
    int oldsource = mSourceType;
    mSourceType = getSourceType(path);

    switch (mSourceType) {
      case SOURCE_LOCAL:
        mLocalManager.setCurrentPath(path);
        // add by xudong
        setCurrentUsbdisBlock(path);
        // end
        break;
      case SOURCE_SMB:
        if (true == Feature.isSupportCnSamba()) {
          mLenFilesManager.setCurrentPath(path);
        } else {
          mSmbManager.setCurrentPath(path);
        }
        break;
      case SOURCE_DLNA:
        mDlnaManager.setCurrentPath(path);
        break;
      default:
        break;
    }

    mCurrentPath = path;

    if (mCurrentPath == null || ROOT_PATH.equals(mCurrentPath)) {
      mCurrentPath = mRootPath;
      mParentPath = null;
    } else {
      switch (mSourceType) {
        case SOURCE_LOCAL:
          mParentPath = mLocalManager.getParentPath();
          break;
        case SOURCE_SMB:
          if (true == Feature.isSupportCnSamba()) {
            mParentPath = mLenFilesManager.getParentPath();
            if (null != mParentPath && mParentPath.equals(mLenFilesManager.getRootPath())) {
              mParentPath = ROOT_PATH;
            }
          } else {
            mParentPath = mSmbManager.getParentPath();
            if (null != mParentPath && mParentPath.equals(mSmbManager.getRootPath())) {
              mParentPath = ROOT_PATH;
            }
          }
          break;
        case SOURCE_DLNA:
          mParentPath = mDlnaManager.getParentPath();
          if (null != mParentPath && mParentPath.equals(mDlnaManager.getRootPath())) {
            mParentPath = ROOT_PATH;
            mDlnaManager.clearHistory();
          }
          break;
        default:
          break;
      }

      if (mParentPath == null) {
        mParentPath = ROOT_PATH;
      }
    }

    if (oldsource != mSourceType && oldsource != SOURCE_ALL
        && !(mSourceType == SOURCE_ALL && !mSmbAvailable && !mDlnaAvailable)) {
        setChanged();
        notifyObservers(REQUEST_SOURCE_CHANGED);
    }

    MtkLog.d(TAG, "Current Path : " + mCurrentPath);
    MtkLog.d(TAG, "Parent Path : " + mParentPath);
  }

  @Override
  public void setContentType(int contenType) {
    mLocalManager.setContentType(contenType);

    if (true == Feature.isSupportCnSamba()) {
      if (mLenFilesManager != null) {
        mLenFilesManager.setContentType(contenType);
      }
    }

    if (mSmbManager != null) {
      mSmbManager.setContentType(contenType);
    }

    if (mDlnaManager != null) {
      mDlnaManager.setContentType(contenType);
    }

    super.setContentType(contenType);
  }

  @Override
  public void setSortType(int sortType) {
    mLocalManager.setSortType(sortType);

    super.setSortType(sortType);
  }

  @Override
  public boolean canPaste(String file) {
    return mLocalManager.canPaste(file);
  }

  @Override
  public boolean isInSameFolder(String path1, String path2) {
    return mLocalManager.isInSameFolder(path1, path2);
  }

  @Override
  public void destroy() {
    Log.d(TAG, "MuiltiFileManager destroy");
    removeDeviceListener();
    destroyManager();
  }

  @Override
  public void destroyManager() {
    mLocalManager.destroy();
    if (true == Feature.isSupportCnSamba()) {
      if (mLenFilesManager != null) {
        mLenFilesManager.destroy();
      }
    }
    if (mSmbManager != null) {
      mSmbManager.destroy();
    }

    if (mDlnaManager != null) {
      mDlnaManager.deleteObserver(this);
      mDlnaManager.destroy();
    }

    sInstance = null;
  }

  public void removeDeviceListener() {
    if (mDevManager != null) {
      mDevManager.removeDevListener(mDevListener);
    }
  }

  public List<FileAdapter> listRecursiveFiles(int contentType,
      int sourceType, boolean bind) {
    List<FileAdapter> files = null;

    MtkLog.i(TAG, "sourceType:" + sourceType);
    switch (sourceType) {
      case SOURCE_LOCAL:
        files = mLocalManager.listRecursiveFiles(contentType);
        break;
      case SOURCE_SMB:
        if (true == Feature.isSupportCnSamba()) {
          files = mLenFilesManager.listRecursiveFiles(contentType);
        } else {
          files = mSmbManager.listRecursiveFiles(contentType);
        }
        break;
      case SOURCE_DLNA:
        files = mDlnaManager.listRecursiveFiles(contentType);
        break;
      default:
        break;
    }

    MtkLog.d(TAG, "List Recursive Files!!");
//    logFiles(TAG, files);
    if (files != null) {
      MtkLog.i(TAG, "files size::" + files.size());
    } else {
      MtkLog.i(TAG, "files !=null:");
    }

    return files;
  }

  public void setFiles(List<FileAdapter> files) {
    mFiles = files;
  }

  @Override
  public List<FileAdapter> listRecursiveFiles(int contentType) {
    return listRecursiveFiles(contentType, mSourceType, true);
  }

  public PlayList getPlayList(List<FileAdapter> originalFiles,
      int currentIndex, int contentType, int sourceType) {

    if (currentIndex < 0) {
      return null;
    }

    PlayList playlist = PlayList.getPlayList();

    int type = 0;
    switch (contentType) {
      case CONTENT_AUDIO:
        type = Const.FILTER_AUDIO;
        break;
      case CONTENT_PHOTO:
      case CONTENT_THRDPHOTO:
        type = Const.FILTER_IMAGE;
        break;
      case CONTENT_VIDEO:
        type = Const.FILTER_VIDEO;
        break;
      case CONTENT_TEXT:
        type = Const.FILTER_TEXT;
        break;
      default:
        break;
    }

    int source = 0;
    switch (sourceType) {
      case SOURCE_LOCAL:
        source = Const.FILE_TYPE_USB;
        break;
      case SOURCE_SMB:
        source = Const.FILE_TYPE_SMB;
        break;
      case SOURCE_DLNA:
        source = Const.FILE_TYPE_DLNA;
        break;
      default:
        break;
    }

    synchronized (originalFiles) {
      List<String> files = Lists.newArrayList();
      int index = 0;
      int count = 0;
      FileAdapter original = originalFiles.get(currentIndex);
      switch (sourceType) {
        case SOURCE_LOCAL:
        case SOURCE_SMB:
          for (FileAdapter file : originalFiles) {
            if (file.isFile()) {
              files.add(file.getAbsolutePath());
              if (file.equals(original)) {
                index = count;
              }
              count++;
            }
          }
          break;
        case SOURCE_DLNA:
          for (FileAdapter file : originalFiles) {
            if (file.isFile()) {
              files.add(file.getName() + file.getSuffix());
              /*
               * files.add("dlna."+file.getName()+file.getSuffix()); String originalName =
               * "dlna."+original.getName()+original.getSuffix(); MtkLog.e("chengcl",
               * "filename="+file.getName()+file.getSuffix()+"==oname="+originalName);
               * if(("dlna."+file.getName()+file.getSuffix()).equals(originalName)){ index = count;
               * }
               */
              if (file.equals(original)) {
                index = count;
              }
              count++;
            }
          }
          break;
        default:
          break;
      }

      MtkLog.d(TAG, "PlayList Index : " + index);

      playlist.addFiles(type, source, files);
      playlist.setCurrentIndex(type, index);
    }

    return playlist;
  }

  public PlayList getPlayList(int currentIndex) {
    return getPlayList(mFiles, currentIndex, mContentType, mSourceType);
  }

  @Override
  public void addObserver(Observer observer) {
    super.addObserver(observer);

    if (true == Feature.isSupportCnSamba()) {
      if (mLenFilesManager != null) {
        mLenFilesManager.addObserver(observer);
      }
    }

    if (mSmbManager != null) {
      mSmbManager.addObserver(observer);
    }

    if (mDlnaManager != null) {
      mDlnaManager.addObserver(this);
    }
  }

  @Override
  public void deleteObserver(Observer observer) {
    super.deleteObserver(observer);

    if (true == Feature.isSupportCnSamba()) {
      if (mLenFilesManager != null) {
        mLenFilesManager.deleteObserver(observer);
      }
    }

    if (mSmbManager != null) {
      mSmbManager.deleteObserver(observer);
    }

    if (mDlnaManager != null) {
      mDlnaManager.deleteObserver(this);
    }
  }

  @Override
  public void deleteObservers() {
    super.deleteObservers();

    if (true == Feature.isSupportCnSamba()) {
      if (mLenFilesManager != null) {
        mLenFilesManager.deleteObservers();
      }
    }
    if (mSmbManager != null) {
      mSmbManager.deleteObservers();
    }

    if (mDlnaManager != null) {
      mDlnaManager.deleteObservers();
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    int request = (Integer) data;

    if (request == REQUEST_BACK_DEVICE_LEFT) {
      mDlnaDevices.clear();
      mDlnaDevices.addAll(mDlnaManager.getDevices());

    } else if (request == REQUEST_REFRESH) {
      
      if (mSourceType == SOURCE_ALL || mSourceType == SOURCE_DLNA) {
        if (mDlnaAvailable) {
          List<FileAdapter> files = mDlnaManager.getCurrentFiles();

          if (mSourceType == SOURCE_DLNA && !files.isEmpty()) {
            if (files.get(0).isDevice()) {
              mDlnaDevices.clear();
              mDlnaDevices.addAll(files);
              if (mAllDevices == null) {
                mAllDevices = Lists.newArrayList();
              }
              mAllDevices.clear();
              if (MediaMainActivity.mIsDlnaAutoTest) {
                mAllDevices.addAll(mDlnaDevices);
              } else {
                mAllDevices.addAll(mLocalDevices);
                mAllDevices.addAll(mSmbDevices);
                mAllDevices.addAll(mDlnaDevices);
              }
              return;
            }
          }
          addFiles(files);
        }
        setChanged();
        notifyObservers(data);
      }
    } else if (request == REQUEST_DEVICE_LEFT) {

      mSourceType = SOURCE_ALL;
      mDlnaDevices.clear();
      mDlnaDevices.addAll(mDlnaManager.getCurrentFiles());
      setChanged();
      notifyObservers(data);
    }
  }

  public void cleanAllDevices() {
    if (mAllDevices != null) {
      mAllDevices.clear();
      mAllDevices = null;
    }
  }

  // Only Used in DLNA
  public boolean isGif(String path) {
    if (mSourceType != SOURCE_DLNA) {
      return false;
    }
    int i = 0;
    for (; i < mFiles.size(); i++) {
      if (mFiles.get(i).getAbsolutePath().equals(path) || mFiles.get(i).getName().equals(path)) {
        break;
      }
    }

    return i < mFiles.size()
        && mFiles.get(i).getSuffix().equals(FileSuffixConst.DLNA_FILE_NAME_EXT_GIF);

  }

  public static boolean isSourceDLNA(Context context) {
    return MultiFilesManager.getInstance(context).getCurrentSourceType()
        == MultiFilesManager.SOURCE_DLNA;
  }

  public static boolean isSourceLocal(Context context) {
    return MultiFilesManager.getInstance(context).getCurrentSourceType()
        == MultiFilesManager.SOURCE_LOCAL;
  }

  public void initSamba(final Activity act){
      if (true == Feature.isSupportCnSamba()) {
        if (null != mLenFilesManager) {
            mLenFilesManager.init(act);
        }
      }
  }

  public void setCurrentPos(final int index){
      if (true == Feature.isSupportCnSamba()) {
        if (null != mLenFilesManager) {
          mLenFilesManager.setCurrentPos(index);
        }
      }
  }
}
