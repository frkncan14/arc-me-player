package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import com.mediatek.wwtv.mediaplayer.util.Constants;

public class SambaDataBrowser {

    private static final String TAG = "SmbDataBrowser";

    /** Are landing */
    protected static final int LOGIN_SAMBA = 1;

    /** Successful landing samba */
    protected static final int LOGIN_SUCCESS = 2;

    /** Are logout samba */
    protected static final int LOGOUT_SAMBA = 3;

    /** Is loading samba equipment */
    protected static final int LOAD_SAMBA_DEVICE = 4;

    /** Is loading samba resources */
    protected static final int LOAD_SAMBA_SOURCE = 5;

    /** Has published samba */
    protected static final int LOGOUT_DONE = 6;

    /** Mount samba failure */
    protected static final int MOUNT_FAILED = 7;

    /** Cancel the landing */
    protected static final int LOGIN_CANCEL = 8;

    private SambaDataManager sambaDataManager = null;

    private Handler handler;

    // File list data source
    private List<BaseData> dataList;

    // The current focus position
    private int focusPosition;

    // Currently browsing media types
    private int browserType = Constants.OPTION_STATE_ALL;

    // Click confirm focus position media types
    private int mediaType;

    private Context mContext = null;

    private RefreshUIListener refreshUIListener = new RefreshUIListener() {

        @Override
        public void onFinish(List<BaseData> data, int currentPage,
                int totalPage, int position) {
            Log.d(TAG, "onFinish(), currentPage : " + currentPage
                    + " totalPage : " + totalPage + " position : " + position + ", data.size = " + data.size());

            // Focus position
            focusPosition = position;

            // Empty before the data
            dataList.clear();
            dataList.addAll(data);

            // Send refresh UI event
            Message msg = handler.obtainMessage();
            msg.what = Constants.UPDATE_ALL_SAMBA_DATA;

            Bundle bundle = new Bundle();
            // The current page number
            bundle.putInt(Constants.BUNDLE_PAGE, currentPage);
            // Total page
            bundle.putInt(Constants.BUNDLE_TPAGE, totalPage);
            // The current focus position
            bundle.putInt(Constants.BUNDLE_INDEX, position);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }

        @Override
        public void onOneItemAdd(List<BaseData> data, int currentPage,
                int totalPage, int position) {
            Log.d(TAG, "onOneItemAdd, currentPage : " + currentPage
                + " totalPage : " + totalPage + " position : " + position);

            // Focus position
            focusPosition = position;

            // Empty before the data
            dataList.clear();
            dataList.addAll(data);

            // Send refresh UI event
            Message msg = handler.obtainMessage();
            msg.what = Constants.UPDATE_SAMBA_DATA;

            Bundle bundle = new Bundle();
            // The current page number
            bundle.putInt(Constants.BUNDLE_PAGE, currentPage);
            // Total page
            bundle.putInt(Constants.BUNDLE_TPAGE, totalPage);
            // The current focus position
            bundle.putInt(Constants.BUNDLE_INDEX, position);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }

        @Override
        public void onScanDeviceCompleted() {
            Log.d(TAG, "onScanDeviceCompleted()");
            Message msg = handler.obtainMessage();
            msg.what = Constants.UPDATE_SCAN_DEVICE_COMPLETED;
            handler.sendMessage(msg);
        }

        @Override
        public void onFailed(int code) {
            Log.d(TAG, "onFailed, code : " + code);

            Message msg = handler.obtainMessage();
            msg.what = Constants.UPDATE_EXCEPTION_INFO;

            if (code == Constants.FAILED_TIME_OUT) {
                // Release the samba related resources
                release();
                msg.arg1 = Constants.FAILED_TIME_OUT;
            } else if (code == Constants.FAILED_WRONG_PASSWD) {
                msg.arg1 = Constants.FAILED_WRONG_PASSWD;
            } else if (code == Constants.FAILED_LOGIN_FAILED) {
                msg.arg1 = Constants.FAILED_LOGIN_FAILED;
            } else if (code == Constants.FAILED_LOGIN_OTHER_FAILED) {
                msg.arg1 = Constants.FAILED_LOGIN_OTHER_FAILED;
            }
            handler.sendMessage(msg);
        }

        @Override
        public void onShowLoginDialog() {
            Log.d(TAG, "onShowLoginDialog()");
            Message msg = handler.obtainMessage();
            msg.what = Constants.UPDATE_SHOW_LOGIN_DIALOG;
            handler.sendMessage(msg);
        }

        @Override
        public void onSambaEquipment() {
            Log.d(TAG, "onSambaEquipment()");
            Message msg = handler.obtainMessage();
            msg.what = Constants.UPDATE_SAMBA_BACK_TO_ROOT;
            handler.sendMessage(msg);
        }
    };

    private LoginSambaListener loginSambaListener = new LoginSambaListener() {

        @Override
        public void onEnd(int code) {
            Log.d(TAG, "onEnd(), code = " + code);

            Message message = handler.obtainMessage();
            message.what = Constants.UPDATE_PROGRESS_INFO;

            switch (code) {
              case LOGIN_SAMBA:
                  Log.d(TAG, "LOGIN_SAMBA ");
                  message.arg1 = Constants.LOGIN_STATUS_LOGIN_SAMBA;
                  break;
              case LOGIN_SUCCESS:
                  Log.d(TAG, "LOGIN_SUCCESS ");
                  message.arg1 = Constants.LOGIN_STATUS_LOGIN_SUCCESS;
                  break;
              case LOGOUT_SAMBA:
                  Log.d(TAG, "LOGOUT_SAMBA");
                  message.arg1 = Constants.LOGIN_STATUS_LOGOUT_SAMBA;
                  break;
              case LOAD_SAMBA_DEVICE:
                  Log.d(TAG, "LOAD_SAMBA_DEVICE");
                  message.arg1 = Constants.LOGIN_STATUS_LOAD_SAMBA_DEVICE;
                  break;
              case LOAD_SAMBA_SOURCE:
                  Log.d(TAG, "LOAD_SAMBA_SOURCE");
                  message.arg1 = Constants.LOGIN_STATUS_LOAD_SAMBA_SOURCE;
                  break;
              case LOGOUT_DONE:
                  Log.d(TAG, "LOGOUT_DONE");
                  message.arg1 = Constants.LOGIN_STATUS_LOGOUT_DONE;
                  break;
              case MOUNT_FAILED:
                  Log.d(TAG, "MOUNT_FAILED");
                  message.what = Constants.UPDATE_EXCEPTION_INFO;
                  message.arg1 = Constants.LOGIN_STATUS_MOUNT_FAILED;
                  break;
              case LOGIN_CANCEL: {
                  Log.d(TAG, "LOGIN_CANCEL");
                  message.arg1 = Constants.LOGIN_STATUS_LOGIN_CANCEL;
                  break;
              }
              default:
                  return;
            }
            // Send a message to interface update
            handler.sendMessage(message);
        }
    };

    /**
     * @param activity
     *            {@link Activity}.
     * @param handler
     *            Send refresh interface news{@link Handler}.
     * @param data
     *            File list data source.
     */
    public SambaDataBrowser(
        Context ctx, Handler handler, List<BaseData> data) {
        this.mContext = ctx;
        this.handler = handler;
        this.dataList = data;
    }

    /**
     * Enter the folder for data.
     *
     * @param index
     *            .
     * @param type
     *            Media types.
     */
    public void browser(int index, int type, final String path) {
        this.browserType = type;
        Log.d(TAG, "browser(), index : " + index + " type : " + type);

        if (sambaDataManager == null) {
            sambaDataManager = new SambaDataManager(
                    mContext, loginSambaListener, refreshUIListener);
        }

        sambaDataManager.browser(index, type, path);
    }

    protected void stopBrowser() {
         if (sambaDataManager != null) {
             Log.i(TAG, "stop samba browser...");
             sambaDataManager.stopBrowser();
         }
    }

    protected boolean isUpdating() {
         if (sambaDataManager != null) {
             return sambaDataManager.isUpdating();
         }
         return false;
    }

    /**
     * Release the samba related resources.
     */
    protected void release() {
        Log.d(TAG, "release()");
        if (sambaDataManager != null) {
            sambaDataManager.release();
        }
    }

    /**
     * Unmount off all of the mount SAMBA file.
     */
    public void unmount() {
        Log.d(TAG, "unmount()");
        if (isUseHttpSambaModeOn()) {
            return;
        }
        if (sambaDataManager != null) {
            sambaDataManager.unmount();
        }
    }

    public void stopHttpServer() {
      Log.d(TAG, "stopHttpServer()");

        if (!isUseHttpSambaModeOn()) {
            return;
        }
        if (sambaDataManager != null) {
            sambaDataManager.stopHttpServer();
        }
    }

    protected int getBrowserSambaDataState() {
        if (null == sambaDataManager)
            return -1;
        int tmpState = sambaDataManager.getBrowserSambaDataState();
        return tmpState;
    }

    private static boolean isUseHttpSambaModeOn() {
        return true;
    }

    private int getLiveViewMode() {
        return Constants.GRIDVIEW_MODE;
    }

    private boolean isMediaFile(final int type) {
        if (Constants.FILE_TYPE_PICTURE == type
                || Constants.FILE_TYPE_SONG == type
                || Constants.FILE_TYPE_VIDEO == type) {
            return true;
        } else {
            return false;
        }
    }

    public String getCurrPath() {
        if (null == sambaDataManager) {
          return "";
        }
      return sambaDataManager.getCurrPath();
    }

    public void backToRoot() {
      if (null != sambaDataManager) {
        sambaDataManager.backToRoot();
      }
    }

    public void setCurrentPos(final int index) {
      if (null != sambaDataManager) {
        sambaDataManager.setCurrentPos(index);
      }
    }
}
