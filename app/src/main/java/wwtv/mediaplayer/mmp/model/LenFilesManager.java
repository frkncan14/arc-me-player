
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoInfo;
import com.mediatek.wwtv.mediaplayer.netcm.samba.LoginInfo;
import com.mediatek.wwtv.mediaplayer.netcm.samba.lmm.BaseData;
import com.mediatek.wwtv.mediaplayer.netcm.samba.lmm.SambaDataBrowser;
import com.mediatek.wwtv.mediaplayer.util.Constants;

public class LenFilesManager extends FilesManager<FileAdapter> {
  private static final String TAG = "SmbLenFilesManager";

  private static LenFilesManager sInstance;

  private AudioInfo mAudioInfo;

  private VideoInfo mFileInfo;

  private SambaDataBrowser mSambaDataBrowser = null;

  private List<BaseData> mSourceData = new ArrayList<BaseData>();

  private Handler mHandler = new Handler() {
      public void handleMessage(Message msg) {
        Log.w(TAG, "mHandler.handleMessage() msg.what = " + msg.what);
        final int nSize = mSourceData.size();
        switch (msg.what){
          case Constants.UPDATE_ALL_SAMBA_DATA: {
              Log.w(TAG, "UPDATE_ALL_SAMBA_DATA, nSize = " + nSize);
              if (1 == nSize) {
                // device root
                BaseData bd = mSourceData.get(0);
                if (null == bd.getPath()) {
                  if (!bd.getName().isEmpty()) {
                    return;
                  }
                }
              }

              mFiles.clear();
              String path = "/";
              for(int nIdx = 0; nIdx < nSize; nIdx++) {
                BaseData bd = mSourceData.get(nIdx);
                //Log.w(TAG, "bd.getName() = " + bd.getName());
                //Log.w(TAG, "bd.getPath() = " + bd.getPath());
                //Log.w(TAG, "bd.getParentPath() = " + bd.getParentPath());
                path = bd.getPath();
                //Log.d(TAG, "path = " + path);
                if (null == path) {
                  path = bd.getParentPath();
                }
                final boolean isDir = (bd.getType() == Constants.FILE_TYPE_DIR) ? true : false;
                FileAdapter fa = new LenFileAdapter("" + path, mAudioInfo, mFileInfo, isDir, !isDir, bd.getSizeEx());
                mFiles.add(fa);
              }
              sortFile();
              setChanged();
              notifyObservers(REQUEST_SAMBA_UPDATE_ALL_SAMBA_DATA);
            }
            break;
          case Constants.UPDATE_SCAN_DEVICE_COMPLETED: {
              Log.w(TAG, "UPDATE_SCAN_DEVICE_COMPLETED");
              mFiles.clear();
              String path = "/";
              for(int nIdx = 0; nIdx < nSize; nIdx++) {
                BaseData bd = mSourceData.get(nIdx);
                final String currPath = getCurrentPath();
                final String rootPath = getRootPath();
                path = rootPath;
                if (null != currPath && !currPath.isEmpty()) {
                  if(rootPath.equals("smb://")) {
                    path += bd.getName();
                  } else {
                    path += "/"+ bd.getName();
                  }
                } else {
                    path += bd.getName();
                }
                Log.d(TAG, "path = " + path);
                FileAdapter fa = new LenFileAdapter(path, mAudioInfo, mFileInfo, true, false, 0);
                mFiles.add(fa);
              }
              setChanged();
              notifyObservers(REQUEST_SAMBA_SCAN_DEVICE_COMPLETED);
            }
            break;
          case Constants.UPDATE_PROGRESS_INFO:
            Log.d(TAG, "msg.arg1 = " + msg.arg1);
            switch (msg.arg1) {
              case Constants.LOGIN_STATUS_LOGIN_SAMBA:
                  break;
              case Constants.LOGIN_STATUS_LOGIN_SUCCESS:
                  break;
              case Constants.LOGIN_STATUS_LOGOUT_SAMBA:
                  break;
              case Constants.LOGIN_STATUS_LOAD_SAMBA_DEVICE:
                  break;
              case Constants.LOGIN_STATUS_LOAD_SAMBA_SOURCE:
                  break;
              case Constants.LOGIN_STATUS_LOGOUT_DONE:
                  break;
              case Constants.LOGIN_STATUS_LOGIN_CANCEL:
                  setChanged();
                  notifyObservers(REQUEST_SAMBA_LOGIN_CANCELED);
                  break;
              default:
                  return;
            }

            break;
          case Constants.UPDATE_SAMBA_DATA:
            break;
          case Constants.UPDATE_SHOW_LOGIN_DIALOG:
            setChanged();
            notifyObservers(REQUEST_SAMBA_DISMISS_DIALOG);
            break;
          case Constants.UPDATE_EXCEPTION_INFO:
            setChanged();
            notifyObservers(REQUEST_SAMBA_LOGIN_FAILED);
            break;
          case Constants.UPDATE_SAMBA_BACK_TO_ROOT:
            notifyObservers(REQUEST_SAMBA_UPDATE_ALL_SAMBA_DATA);
            break;
          default:
            break;
        }
      };
  };

  private LenFilesManager(Context context) {
    super(context);

    mAudioInfo = AudioInfo.getInstance();
    mFileInfo = VideoInfo.getInstance();
  }

  public static synchronized LenFilesManager getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new LenFilesManager(context);
    }

    return sInstance;
  }

  public boolean login(String path, String userName, String userPwd) {
    boolean success = false;

    return success;
  }

  public boolean login(String path, String domain, String userName, String userPwd) {
    boolean success = false;

    return success;
  }

  private boolean isSmbRoute(String path) {
    if (path == null) {
      return false;
    }

    if (path.equals(getRootPath())) {
      return true;
    }

    return false;
  }

  public List<FileAdapter> listAllDevices() {
      return mFiles;
  }

  @Override
  public List<FileAdapter> listAllFiles(String path) {
    Log.d(TAG, "listAllFiles(), path = " + path);

      mFiles.clear();
      int type = Constants.OPTION_STATE_ALL;

      switch (mContentType) {
        case CONTENT_PHOTO:
          type = Constants.OPTION_STATE_PICTURE;
          break;
        case CONTENT_AUDIO:
          type = Constants.OPTION_STATE_SONG;
          break;
        case CONTENT_VIDEO:
          type = Constants.OPTION_STATE_VIDEO;
          break;
        case CONTENT_TEXT:
        case CONTENT_THRDPHOTO:
        default:
          return mFiles;
      }

      final String parent = retriveParentPath(path);
      boolean browseDevice = false;
      if (true == isSmbRoute(path) || true == isSmbRoute("/")) {
        browseDevice = true;
      }
      Log.d(TAG, "parent = " + parent);
      Log.d(TAG, "browseDevice = " + browseDevice);

      boolean bEnterParentDir = true;
      final String newPath = path;
      final String currPath = mSambaDataBrowser.getCurrPath();
      final String tmpPath = (null == currPath) ? "" : currPath;
      boolean backToTop = false;
      if (!tmpPath.isEmpty()) {
        final String smbDevParent = retriveParentPath(tmpPath);
        backToTop = isSmbRoute(smbDevParent);
      }
      if ((null == currPath) || true == backToTop) {
        // broser 1st dir
        bEnterParentDir = false;
      } else if ((true == currPath.equals(newPath))
          && (false == browseDevice)) {
        Log.i(TAG, "No need to query data for the current path: " + newPath);
        return mFiles;
      } else if(currPath.length() > newPath.length()) {
        // enter parent dir
        bEnterParentDir = true;
      } else {
        // enter sub dir
        bEnterParentDir = false;
      }

      if (true == browseDevice) {
        // list device
        mSambaDataBrowser.browser(-1, 0, path);
      } else if(true == bEnterParentDir) {
        // enter parent dir
        mSambaDataBrowser.browser(0, type, path);
      } else {
        // enter sub dir
        mSambaDataBrowser.browser(1, type, path);
      }
    return mFiles;
  }

  @Override
  protected String retriveParentPath(String path) {
    String parent;
    String newPath = path.substring(0, path.length() - 1);

    if (true == isSmbRoute(path)) {
      return path;
    }
    if (newPath.indexOf("/") + 1 == newPath.lastIndexOf("/")) {
      parent = newPath.substring(0, newPath.lastIndexOf("/") + 1);
    } else {
      parent = super.retriveParentPath(newPath) + "/";
    }

    //Log.d(TAG, "RetriveParentPath : " + parent);
    return parent;
  }

  @Override
  protected FileAdapter newWrapFile(Object originalFile) {
    return null;
  }

  public interface AuthListener {
    void onNeedAuth();
  }

  @Override
  public void destroy() {
    if (null != mSambaDataBrowser) {
      mSambaDataBrowser.unmount();
      mSambaDataBrowser = null;
    }
    destroyManager();
  }

  @Override
  public List<FileAdapter> listRecursiveFiles(int contentType) {
    return null;
  }

  @Override
  public void destroyManager() {
    if (mAudioInfo != null) {
      mAudioInfo.destroyInfo();
      mAudioInfo = null;
    }
    if (mFileInfo != null) {
      mFileInfo.destroyInfo();
      mFileInfo = null;
    }
    sInstance = null;
  }

  protected void sortFile() {
    /*
    Log.i(TAG, "sortFile");
    int type = MultiFilesManager.getInstance(mContext).getSortType();
    if (FilesManager.SORT_BY_NAME == type) {
      new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getName", null);
    } else if (FilesManager.SORT_BY_DATE == type) {
      int source = MultiFilesManager.getInstance(mContext).getCurrentSourceType();
      if (MultiFilesManager.SOURCE_DLNA != source) {
        new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getSize", null);
      }
    } else if (FilesManager.SORT_BY_TYPE == type) {
      new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getSuffix", "getName");
    }
    */
  }

  public void init(final Activity act) {
    mSambaDataBrowser = new SambaDataBrowser(act, mHandler, mSourceData);
  }

  public void backToRoot() {
    //mFiles.clear();
    mSambaDataBrowser.backToRoot();
  }

  public void setCurrentPos(final int index) {
    if (null != mSambaDataBrowser) {
      mSambaDataBrowser.setCurrentPos(index);
    }
  }
}
