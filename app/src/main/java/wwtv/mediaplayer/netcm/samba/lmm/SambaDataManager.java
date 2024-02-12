package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import java.io.IOException;
import java.io.File;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;

import com.mstar.android.samba.HttpBean;
import com.mstar.android.samba.NanoHTTPD;
import com.mstar.android.samba.OnRecvMsg;
import com.mstar.android.samba.OnRecvMsgListener;
import com.mstar.android.samba.SambaStorageManager;
import com.mstar.android.samba.SmbAuthentication;
import com.mstar.android.samba.SmbClient;
import com.mstar.android.samba.SmbDevice;
import com.mstar.android.samba.SmbShareFolder;

import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.util.Constants;

public class SambaDataManager extends BaseDataManager {

    private static final String TAG = "SmbDataManager";

    private static final int NET_STATUS_NOT_SUPPORT = -1073741637;

    // Is scanning the SAMBA device list
    protected static final int SCAN_HOST = 1;

    // Are viewing a SAMBA equipment resources
    protected static final int SCAN_FILE = 2;

    private static final int CNT_PER_PAGE = 65535;

    private static final int CNT_GRID_PER_PAGE = 65535;

    private Context mContext = null;

    // return to SambaDataBrowser data list
    private List<BaseData> uiData = new ArrayList<BaseData>();

    // The current equipment pages, total pages and the focus position
    private int currentPage = 1, totalPage = 1, position = 0;

    // The current state of browsing
    private int state = SCAN_HOST;

    // current browse media type
    private int browseType = Constants.OPTION_STATE_ALL;

    // username and password
    private String usr, pwd;

    // Is currently scanning path
    private String currentPath = "";

    // used on newSamba in enterDirectory function
    private String currentDirectoryName = "";

    private SmbClient smbClient;

    private SmbDevice smbDevice = null;

    private NanoHTTPD nanohttp;

    // Scanning SAMBA device list thread
    private Thread findHostThread = null;

    // all SAMBA device list
    private List<SmbDevice> deviceList = new ArrayList<SmbDevice>();

    private List<SmbShareFolder> mSmbShareFolderLists = new ArrayList<SmbShareFolder>();

    private PingDeviceListener pingDeviceListener;

    private UnMountListener unmountListener;

    private LoginSambaListener loginSambaListener;

    private RefreshUIListener refreshUIListener;

    private SambaStorageManager mStorageManager;

    // Memory into the directory location to return to its original position
    private ReturnStack returnStack;

    LoginSambaDialog mLoginDialog;

    private boolean mIsConnectingHttpServer = false;

    private String mSambaName = "";

    private String mSambaPassword = "";

    public SambaDataManager(
            Context ctx,
            LoginSambaListener loginSambaListener,
            RefreshUIListener refreshUIListener) {
        super();

        this.mContext = ctx;
        this.loginSambaListener = loginSambaListener;
        this.refreshUIListener = refreshUIListener;

        this.returnStack = new ReturnStack();

        // Initialization samba search must parameters
        SmbClient.initSamba();
        this.mStorageManager = SambaStorageManager.getInstance(ctx);
    }

    protected void browser(int index, int type, final String path) {
        this.browseType = type;

        // First browse samba data
        if (index == -1) {
            findSambaDevice();
            // return to pre
        } else if (index == 0) {
            enterParentDirectory();
            // Into the next level directory
        } else {
            enterDirectory(path);
        }
    }

    protected void stopBrowser() {
        Log.d(TAG, "stopBrowser()");
        if (smbClient != null && smbClient.isUpdating()) {
            Log.i(TAG, "stop update...");
            smbClient.StopUpdate();
        }
    }

    public boolean isUpdating() {
        Log.d(TAG, "isUpdating()");
        if (smbClient != null) {
            return smbClient.isUpdating();
        }
        return false;
    }

    private void updateCurrentPage(int increase, int type) {
        Log.d(TAG, "updateCurrentPage, increase : " + increase
                + " current state : " + state + " type : " + type);
        // Flip over operation
        if (type == 0) {
            // page down
            if (increase == 1) {
                if (currentPage < totalPage) {
                    currentPage++;
                } else {
                    return;
                }
                position = Constants.POSITION_0;
            } else if (increase == 0) {
                // page up
            } else if (increase == -1) {
                if (currentPage > 1) {
                    currentPage--;
                } else {
                    return;
                }
                if (Constants.GRIDVIEW_MODE== isListOrGrid())
                    position=Constants.GRID_MODE_DISPLAY_NUM;
                else
                    position = 9;
            }
        } else {
            // Switching media type operation
            if (browseType != type) {
                browseType = type;
                position = Constants.POSITION_0;
                currentPage = 1;
            }
        }
    }
    /**
     * @param increase
     *            Turn the page index. </br> <li>1 : Said back flip over. <li>0
     *            : Said the first page. <li>-1 : Said forward turn the page.
     * @param type
     *            Media types, 0 indicates no practical significance.
     */
    protected void getCurrentPage(int increase, int type) {
        Log.d(TAG, "getCurrentPage, increase : " + increase
                + " current state : " + state + " type : " + type);

        updateCurrentPage(increase, type);
        // Structure UI display data
        //packageUIData();
        if (Constants.GRIDVIEW_MODE== isListOrGrid()) {
            packageUIDataInGrid();
        } else if (Constants.LISTVIEW_MODE== isListOrGrid()) {
            packageUIData();
        }
        // Callback interface notice UI refresh
        refreshUIListener.onFinish(uiData, currentPage, totalPage, position);
    }

    protected int getBrowserSambaDataState(){
        return state;
    }

    private void notifyScanCompleted() {
        refreshUIListener.onScanDeviceCompleted();
    }

    private void updataDeviceListItem() {
        Log.d(TAG, "updataDeviceListItem"+ " current state : " + state );

        state = SCAN_HOST;

        if (smbClient == null) {
            return;
        }
        if(!smbClient.isUpdating()) {
            return;
        }
        deviceList.clear();
        List<SmbDevice> local = smbClient.getSmbDeviceList();
        if (local != null) {
            deviceList.addAll(local);
            int size = deviceList.size();
            // recalculate Page Breaks
            int numPerPage=-100;
            if (Constants.GRIDVIEW_MODE== isListOrGrid())
                numPerPage =CNT_GRID_PER_PAGE;
            else
                numPerPage =CNT_PER_PAGE;
            if (size != 0) {
                totalPage = size / numPerPage;
                totalPage += size % numPerPage == 0 ? 0 : 1;
            } else {
                Log.d(TAG, "load samba device failed");
            }
            Log.d(TAG, "updataDeviceListItem, totalPage : " + totalPage);

            updateCurrentPage(0, 0);
            // Structure UI display data
            if (Constants.GRIDVIEW_MODE== isListOrGrid()) {
                packageUIDataInGrid();
            } else if (Constants.LISTVIEW_MODE== isListOrGrid()) {
                packageUIData();
            }
            // Callback interface notice UI refresh
            position =0;
            Log.d(TAG, "update UI data  currentPage:"+currentPage+"  totalPage:"+totalPage+" position:"+position);
            refreshUIListener.onOneItemAdd(uiData, currentPage, totalPage, position);
        } else {
            Log.d(TAG, "smbDevice list is null");
        }
    }

    /**
     * Release the SAMBA related resources and clean up the local cache data.
     */
    protected void release() {
        Log.d(TAG, "release()");

        if (smbClient != null) {
            state = SCAN_HOST;

            // Samba device list page number replacement.
            currentPage = 1;
            totalPage = 1;
            position = 0;

            // Interrupt search samba equipment operation
            if (smbClient.isUpdating()) {
                smbClient.StopUpdate();
            }

            // Thread resources release
            if (findHostThread != null && findHostThread.isAlive()) {
                findHostThread.interrupt();
                findHostThread = null;
            }

            // Release stack data
            if (returnStack != null && returnStack.getTankage() > 0) {
                returnStack.clear();
            }

            // unmountSamba();
        }
    }

    /**
     * Get the current directory all designated type of media files.
     *
     * @param type
     *            ,media type.
     * @param position
     *            focus position.
     * @return The current players play index.
     */
    protected int getMediaFile(int type, int position) {
        // get all meida file
        ArrayList<BaseData> mediaFiles = new ArrayList<BaseData>();
        // get all data
        List<BaseData> allFiles = new ArrayList<BaseData>();

        // File type switch in all types mode
        if (type < 0) {
            allFiles.addAll(getUIDataList(Constants.OPTION_STATE_ALL));
            mediaFiles.addAll(getMediaFileList(-type));

            // File type switch in pictures, music, or video mode
        } else {
            allFiles.addAll(getUIDataList(type));
            mediaFiles.addAll(getMediaFileList(type));
        }

        // index switch
        int index = -100;
        if (Constants.GRIDVIEW_MODE== isListOrGrid())
            index = (currentPage - 1) * CNT_GRID_PER_PAGE+ position - 1;
        else
            index = (currentPage - 1) * CNT_PER_PAGE+ position - 1;
        int size = allFiles.size();
        Log.d(TAG, "all media file size : " + mediaFiles.size()
                + " currentPage : " + currentPage + " position : " + position);
        if (index >= 0 && index < size) {
            BaseData bd = allFiles.get(index);
            String path = bd.getPath();

            index = 0;
            for (BaseData item : mediaFiles) {
                if (path.equals(item.getPath())) {
                    return index;
                } else {
                    index++;
                }
            }
        }

        return 0;
    }

    /**
     * Open thread finish ping network equipment operation.
     */
    protected void pingDevice(final PingDeviceListener listener) {
        Log.d(TAG, "pingDevice()");
        // Ensure examples for null further follow-up operations
        if (smbDevice != null) {

            Runnable localRunnable = new Runnable() {

                @Override
                public void run() {
                    boolean result = false;
                    try {
                        String ip = smbDevice.getAddress();
                        InetAddress localnetAddress = InetAddress.getByName(ip);
                        result = localnetAddress.isReachable(1000);
                        HttpBean.setmIpAddress(ip);
                        //callSambaSetmIpAddress(ip);
                        Log.d(TAG, "host ip : " + ip + " result : " + result);
                    } catch (Exception e) {
                        result = false;
                    }
                    // Not a network segment or domain name, it is always false
                    // Application layer executive ping command word, many are
                    // no authority problem
                    listener.onFinish(result);
                }
            };
            // The network operation must not in the main thread
            Thread localThread = new Thread(localRunnable);
            localThread.start();

        } else {
            Log.d(TAG, "smbDevice is null!");
            listener.onFinish(false);
        }
    }

    public  void callSambaSetmIpAddress(String ip) {
        Log.i(TAG, "callSambaSetmIpAddress");

        try {
             Class clz = Class.forName("com.mstar.android.samba.HttpBean");
             Method setmIpAddress = clz.getDeclaredMethod("setmIpAddress",String.class);
             setmIpAddress.invoke(null,ip);
        } catch (Exception e) {
              e.printStackTrace();
        }
    }

    /**
     * unmount the data source which had mount to /mnt/samba/.
     */
    protected void unmount() {
        if (isUseHttpSambaModeOn()) {
            return;
        }

        // monitor
        unmountListener = new UnMountListener() {
            @Override
            public void onFinish(int code) {
                smbDevice = null;
                if (code == OnRecvMsg.NT_STATUS_UMOUNT_SUCCESSFUL) {
                    Log.d(TAG, "unmount success");
                } else if (code == OnRecvMsg.NT_STATUS_UMOUNT_FAILURE) {
                    Log.d(TAG, "unmount failed");
                }
            }
        };

        // Unloading operation more time-consuming, so start a thread to
        // complete the operation
        if (smbDevice != null && smbDevice.isMounted()) {
            Runnable localRunnable = new Runnable() {

                @Override
                public void run() {
                    int code = smbDevice.unmount();
                    Log.d(TAG, "unmount code : " + code);
                    unmountListener.onFinish(code);
                }
            };
            Thread localThread = new Thread(localRunnable);
            localThread.start();
        }
    }

    protected void closeDialogIfneeded() {
        if (mLoginDialog != null && mLoginDialog.isShowing()) {
            mLoginDialog.dismiss();
        }
    }

    /************************************************************************
     * Rewrite interface method area
     ************************************************************************/
    @Override
    public void onFinish() {
        Log.d(TAG, "onFinish()");

        getCurrentPage(0, browseType);
    }

    /************************************************************************
     * Private method area
     ************************************************************************/

    /*
     * Loading samba equipment data and calculation paging.
     */
    private void loadDeviceData() {
        state = SCAN_HOST;

        if (smbClient == null) {
            return;
        }
        deviceList.clear();
        List<SmbDevice> local = smbClient.getSmbDeviceList();
        if (local != null) {
            deviceList.addAll(local);
/*
            int size = deviceList.size();
            // recalculate Page Breaks
            int numPerPage = -100;
            if (Constants.GRIDVIEW_MODE== isListOrGrid())
                numPerPage = CNT_GRID_PER_PAGE;
            else
                numPerPage = CNT_PER_PAGE;
            if (size != 0) {
                totalPage = size / numPerPage;
                totalPage += size % numPerPage == 0 ? 0 : 1;
            } else {
                Log.d(TAG, "load samba device failed");
            }
            Log.d(TAG, "loadDeviceData, totalPage : " + totalPage);

            // Get the current page samba equipment
            //getCurrentPage(0, 0);
*/
            packageUIData();
        } else {
            Log.d(TAG, "smbDevice list is null");
        }
    }

    /*
     * Packaging UI display use SAMBA data list.
     */
    private void packageUIDataInGrid() {
    }
    
    /*
     * Packaging UI display use SAMBA data list while Icon's mode
     */
    private void packageUIData() {
        int tail = 0;
        int size = 0;

        Log.d(TAG, "packageUIData() state = " +state);
        // loading SAMBA device list
        if (state == SCAN_HOST) {
            List<SmbDevice> deviceList_local = new ArrayList<SmbDevice>();
            deviceList_local.addAll(deviceList);
            size = deviceList_local.size();


            if (size > currentPage * CNT_PER_PAGE) {
                tail = currentPage * CNT_PER_PAGE;
            } else {
                tail = size;
            }
            Log.d(TAG, "device size : " + size + " tail : " + tail);
            // Empty before the data
            uiData.clear();
            // Loading the current page need to display equipment data
            for (int i = (currentPage - 1) * CNT_PER_PAGE; i < tail; i++) {
                 SmbDevice sd = deviceList_local.get(i);
                 BaseData item = new BaseData(Constants.FILE_TYPE_DIR);
                 if (sd != null) {
                     item.setName(sd.getAddress());
                     item.idxOfArr = i;
                     uiData.add(item);
                 }
            }
            deviceList_local.clear();
            // Structure resources data list
        } else if (state == SCAN_FILE) {
            // Empty before the data
            uiData.clear();
            List<BaseData> localUiData = new ArrayList<BaseData>();
            localUiData.addAll(getUIDataList(browseType));
            size = localUiData.size();
            if (size > currentPage * CNT_PER_PAGE) {
                tail = currentPage * CNT_PER_PAGE;
            } else {
                tail = size;
            }
            Log.d(TAG, "size : " + size + " tail : " + tail);

            // Loading the current page need to display resources data
            for (int i = (currentPage - 1) * CNT_PER_PAGE; i < tail; i++) {
                BaseData item = localUiData.get(i);
                uiData.add(item);
            }
        }

        // Computing distribution number
        if (size != 0) {
            totalPage = size / CNT_PER_PAGE;
            totalPage += size % CNT_PER_PAGE == 0 ? 0 : 1;
        } else {
            totalPage = 1;
        }
    }

    /*
     * In the file list first position press ok button finished previous catalog
     * operation.
     */
    private void enterParentDirectory() {
        Log.d(TAG, "enterParentDirectory(), state = " + state);
        Log.d(TAG, "returnStack, top = " + returnStack.getTankage() + ", ret stack size =" + returnStack.size());

        // There are also Stack data, return the first level directory
        if (returnStack.getTankage() > 0) {
            ReturnData rd = returnStack.pop();
            String id = rd.getId();
            currentPage = rd.getPage();
            position = rd.getPosition();
            currentPath = rd.getId();
            currentDirectoryName = rd.getDiractoryName();
            int returnViewMode = rd.getViewMode();
            Log.d(TAG, "pop stack, page : " + currentPage + " position : "
                    + position + " path : " + currentPath + ", id = " + id);
            int tmp=0;
            if (1==returnViewMode) {
                tmp = (currentPage - 1) * Constants.GRID_MODE_DISPLAY_NUM + position- 1;
            } else {
                tmp = (currentPage - 1) * Constants.LIST_MODE_DISPLAY_NUM + position- 1;
            }
            int tmpPos = position;
            if (0==returnViewMode && Constants.GRIDVIEW_MODE== isListOrGrid()) {
                // changeReturnDataList2Grid
                // tmp+1 is the true digit without counting "back" per page
                int mod =(tmp+1)%(Constants.GRID_MODE_DISPLAY_NUM);
                currentPage =(tmp+1)/(Constants.GRID_MODE_DISPLAY_NUM);
                if (mod>0)
                    currentPage++;
                if (mod>0)
                    position=mod;
                else if (0==mod&&tmpPos!=0)
                    position=Constants.GRID_MODE_DISPLAY_NUM;
            } else if (1==returnViewMode&& Constants.LISTVIEW_MODE== isListOrGrid()) {
                // changeReturnDataGrid2List
                int mod =(tmp+1)%(Constants.LIST_MODE_DISPLAY_NUM);
                currentPage =(tmp+1)/(Constants.LIST_MODE_DISPLAY_NUM);
                if (mod>0)
                    currentPage++;
                if (mod>0)
                    position=mod;
                else if (0==mod)
                    position=Constants.LIST_MODE_DISPLAY_NUM;
            }
            // Returns to the equipment list
            if (Constants.RETURN_SAMBA.equals(id)) {
                state = SCAN_HOST;
                // Loading all equipment data
                loadDeviceData();

                refreshUIListener.onSambaEquipment();

                // Returns the SAMBA equipment at the next higher level
                // directory
            } else {
                state = SCAN_FILE;

                pingDeviceListener = new PingDeviceListener() {

                    @Override
                    public void onFinish(boolean flag) {
                        if (flag) {
                            // start scanning data of the specified directory
                            if (isUseHttpSambaModeOn()) {
                                Log.i("andrew", "startScanSmbShareFolder enterParentDirectory:"+currentDirectoryName);
                                //startScanSmbShareFolder(smbDevice.enterDirectory(currentDirectoryName));
                                startScanSmbShareFolder(smbDevice.enterParent());
                            } else {
                                startScan(currentPath);
                            }

                            // can't SAMBA equipment Ping to record
                        } else {
                            refreshUIListener.onFailed(Constants.FAILED_TIME_OUT);
                        }
                    }
                };

                pingDevice(pingDeviceListener);
            }
        }
    }

    /*
     * complete landing or loading a catalogue of data operation.
     */
    private void enterDirectory(final String path) {
        Log.d(TAG, "enterDirectory(), state = " + state + ", page = " + currentPage + ", total page = " + totalPage + ", pos = " + position + ", path = " + path);
        // are display samba device list data
        if (state == SCAN_HOST) {
          /*
            int tmp = -100;
            if (Constants.GRIDVIEW_MODE== isListOrGrid()) {
                tmp = (currentPage - 1) * CNT_GRID_PER_PAGE+ index- 1;
            } else if (Constants.LISTVIEW_MODE== isListOrGrid()) {
                tmp = (currentPage - 1) * CNT_PER_PAGE+ index- 1;
            }*/
            //if (tmp >= 0 && tmp < deviceList.size()) {
            //    Log.d(TAG, "enterDirectory index : " + index + " tmp index : "
            //            + tmp);
                int devArrIdx = 0;
                List<SmbDevice> deviceList_local = new ArrayList<SmbDevice>();
                deviceList_local.addAll(deviceList);
                final int size = deviceList_local.size();

                //Log.d(TAG, "sub = " + "smb://".length());
                //Log.d(TAG, "path len = "+ path.length());
                final String ip = path.substring("smb://".length(), path.length());

                for (int i =0; i < size; i++) {
                   SmbDevice sd = deviceList_local.get(i);
                   final String addr = sd.getAddress();
                   if(addr.equals(ip)) {
                    devArrIdx = i;
                    break;
                   }
                }
                deviceList_local.clear();
                Log.d(TAG, "IP: " + ip + ", device index = " + devArrIdx);

                final SmbDevice localSmbDevice = deviceList.get(devArrIdx);

                // if have loaded samba equipment is prior to discharge
                if (!isUseHttpSambaModeOn() && smbDevice != null && smbDevice.isMounted()) {
                    // unloading after the completion of display landing box
                    unmountListener = new UnMountListener() {

                        @Override
                        public void onFinish(int code) {
                            Log.d(TAG, "unmount code : " + code);
                            //activity.runOnUiThread(new Runnable() {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                public void run() {
                                    loginSambaListener
                                            .onEnd(SambaDataBrowser.LOGOUT_DONE);

                                    smbDevice = localSmbDevice;
                                    // No matter whether the logout success
                                    // relog
                                    // showLogin();
                                    mLoginDialog = new LoginSambaDialog(
                                            mContext, mLoginDlgHandler, smbDevice);
                                    mLoginDialog.show();
                                }
                            });
                        }
                    };

                    // unloading operation more time-consuming, so open a thread
                    Runnable localRunnable = new Runnable() {

                        @Override
                        public void run() {
                            // because exit landing there may be a need for a
                            // long time, so given exit landing tips let users
                            // wait
                            loginSambaListener
                                    .onEnd(SambaDataBrowser.LOGOUT_SAMBA);

                            int code = smbDevice.unmount();
                            unmountListener.onFinish(code);
                        }
                    };
                    Thread localThread = new Thread(localRunnable);
                    localThread.start();

                    //
                } else {
                    smbDevice = localSmbDevice;
                    // Popup landing box
                    // showLogin();
                    showLoginDialog();
                }

            //} else {
            //    Log.e(TAG, "invalid index when show login dialog");
            //}

            // Browse Samba resource file
        } else if (state == SCAN_FILE) {

            pingDeviceListener = new PingDeviceListener() {

                @Override
                public void onFinish(boolean flag) {
                    if (flag) {
                        String parentPath = "";
                        int tmp = position;
                        int viewMode = 0;
                        /*
                        if (Constants.GRIDVIEW_MODE== isListOrGrid()) {
                            tmp = (currentPage - 1) * CNT_GRID_PER_PAGE+ position- 1;
                            viewMode = 1;
                        } else if (Constants.LISTVIEW_MODE== isListOrGrid()) {
                            tmp = (currentPage - 1) * CNT_PER_PAGE+ position- 1;
                            viewMode = 0;
                        }
                        */
                        Log.d(TAG, "tmp index : " + tmp);
                        // Get the current directory all need to display data
                        // browsing Samba resource file
                        List<BaseData> list = new ArrayList<BaseData>();
                        list.addAll(getUIDataList(browseType));
                        if (tmp >= 0 && tmp < list.size()) {
                            BaseData bd = list.get(tmp);
                            currentPath = bd.getPath();
                            if (isUseHttpSambaModeOn()) {
                                parentPath = Constants.RETURN_NOT_SAMBA;
                            } else {
                                parentPath = bd.getParentPath();
                            }
                            currentDirectoryName = bd.getName();
                        } else {
                            Log.e(TAG, "invalied index on browser");
                        }
                        Log.i(TAG,"parentPath:"+parentPath);
                        ReturnData rd = new ReturnData(viewMode,parentPath, currentPage,
                                position,currentDirectoryName);
                        returnStack.push(rd);

                        Log.d(TAG, "scan file path : " + currentPath);
                        position = 0;
                        currentPage = 1;
                        totalPage = 1;

                        loginSambaListener
                                .onEnd(SambaDataBrowser.LOAD_SAMBA_SOURCE);
                        // start scanning data of the specified directory
                        if (isUseHttpSambaModeOn() && currentDirectoryName.length()>0) {
                            Log.i("andrew", "startScanSmbShareFolder enterDirectory:"+currentDirectoryName);
                            startScanSmbShareFolder(smbDevice.enterDirectory(currentDirectoryName));
                        } else {
                            startScan(currentPath);
                        }
                        // can't Ping to SAMBA equipment
                    } else {
                        refreshUIListener.onFailed(Constants.FAILED_TIME_OUT);
                    }
                }
            };
            pingDevice(pingDeviceListener);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mLoginDlgHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "handleMessage() msg.what = " +msg.what);
            switch (msg.what) {
            case LoginSambaDialog.DLG_ACT_CANCEL:
                // Cancel the landing
                loginSambaListener.onEnd(SambaDataBrowser.LOGIN_CANCEL);
                break;
            case LoginSambaDialog.DLG_ACT_LOGIN:
                Bundle data = msg.getData();
                // Retrieve the user input user name
                usr = data.getString("USERNAME");
                // Get the password of user input
                pwd = data.getString("PASSWORD");
                Log.i(TAG, "user: " + usr + " pass: " + pwd);
                pingDeviceListener = new PingDeviceListener() {
                    @Override
                    public void onFinish(boolean flag) {
                        if (flag) {
                            LoginStatus localLoginStatus = new LoginStatus();
                            // Landing samba equipment
                            login(usr, pwd, localLoginStatus);
                        } else {
                            // Equipment, not with
                            refreshUIListener.onFailed(Constants.FAILED_TIME_OUT);
                        }
                    }
                };

                // Before landing samba equipment ping the equipment
                pingDevice(pingDeviceListener);
                break;
            default:
                break;
            }
        }
    };

    /*
     * Call samba interface complete landing operation.
     */

    private void login(final String usr, final String pwd, LoginStatus onRecvMsg) {
        Log.d(TAG, "login() usr = " + usr + ", pwd = " + pwd);

        mSambaName = usr;
        mSambaPassword = pwd;

        if (usr.length()==0 && pwd.length()==0) {
            // Anonymous Logon
            smbDevice.setAuth(null);
        } else {
            // Must input the user name if it has username(Can no password)
            SmbAuthentication auth = new SmbAuthentication(usr, pwd);
            smbDevice.setAuth(auth);
        }
        smbDevice.setStorageManager(mStorageManager);
        smbDevice.setOnRecvMsg(onRecvMsg);
        // Tips are landing
        loginSambaListener.onEnd(SambaDataBrowser.LOGIN_SAMBA);
        mSmbShareFolderLists.clear();
        try {
            mSmbShareFolderLists = smbDevice.getSharefolderList();
            if (mSmbShareFolderLists != null && isUseHttpSambaModeOn()) {
                startScanAfterLoginDone();
            }
        }catch(Exception e){
            onRecvMsg.onRecvMsg(OnRecvMsg.NT_STATUS_LOGON_FAILURE);
        }
    }

    private void startScanAfterLoginDone(){
        Log.i(TAG, "startScanAfterLoginDone");

        // Reset focus and the current page numbe
        if (null != nanohttp) {
           Log.i(TAG, "Samba Name:" + mSambaName + ", Password:" + mSambaPassword);
           HttpBean.setmName(mSambaName);
           HttpBean.setmPassword(mSambaPassword);
        }
        position = 0;
        currentPage = 1;
        totalPage = 1;

        loginSambaListener.onEnd(SambaDataBrowser.LOAD_SAMBA_SOURCE);
        startScanSmbShareFolder(mSmbShareFolderLists);
    }

    /*
     * Initialization scanning samba equipment thread.
     */
    private void findSambaDevice() {
        Log.i(TAG,"findSambaDevice()");
        // Search samba equipment thread
        state = SCAN_HOST;
        smbDevice = null;

        if (returnStack != null && returnStack.getTankage() > 0) {
            returnStack.clear();
        }

        findHostThread = new Thread(new FindHostRunnable());
        findHostThread.start();
    }

    /************************************************************************
     * Search SAMBA equipment thread realize area
     ************************************************************************/
    private class FindHostRunnable implements Runnable {

        @Override
        public void run() {
            smbClient = new SmbClient();
            smbClient.SetPingTimeout(500);
            smbClient.setOnRecvMsgListener(new OnRecvMsgListener() {
                public void onRecvMsgListener(int msg) {
                    switch (msg) {
                    case OnRecvMsgListener.MSG_UPDATE_DEVLIST_CANCEL:
                        Log.d(TAG, "scan cancel!");
                        break;

                    case OnRecvMsgListener.MSG_UPDATE_DEVLIST_ADD:
                        Log.d(TAG, "scan add!");
                        //position = 0;
                        //currentPage = 1;
                        updataDeviceListItem();
                        break;

                    case OnRecvMsgListener.MSG_UPDATE_DEVLIST_DONE:
                        Log.d(TAG, "scan completed ");
                        notifyScanCompleted();
                        if (!mIsConnectingHttpServer) {

                            Log.i(TAG, "scan completed, http server start");
                            File wwwroot = new File(".").getAbsoluteFile();
                            int port = 8088;
                            try {
                                mIsConnectingHttpServer = true;
                                nanohttp = new NanoHTTPD(port, wwwroot);//start http server
                            } catch ( IOException ioe ) {
                                Log.i(TAG, "Couldn't start server:"+ioe);
                            }
                        }
                        break;
                    }
                }
            });
            loginSambaListener.onEnd(SambaDataBrowser.LOAD_SAMBA_DEVICE);

            // Call interface start searching samba equipment data
            smbClient.updateSmbDeviceList();
        }
    }
    public void stopHttpServer(){
        if (nanohttp!=null && mIsConnectingHttpServer) {
            mIsConnectingHttpServer = false;
            nanohttp.stop();
            nanohttp = null;
        }

    }
    /************************************************************************
     * Landing samba equipment state interface implementation area
     ************************************************************************/

    private class LoginStatus implements OnRecvMsg {
        @Override
        public void onRecvMsg(int arg0) {
            switch (arg0) {
            case OnRecvMsg.NT_STATUS_WRONG_PASSWORD:
                Log.d(TAG, "Wrong password");
                refreshUIListener.onFailed(Constants.FAILED_WRONG_PASSWD);
                break;

            case OnRecvMsg.NT_STATUS_LOGON_FAILURE:
                Log.d(TAG, "login failed ");
                refreshUIListener.onFailed(Constants.FAILED_LOGIN_FAILED);
                break;
            case OnRecvMsg.NT_STATUS_OK:
                Log.d(TAG, "login ok");

                state = SCAN_FILE;
                // After the success of the land will be the current page number
                // and focus information press in the stack
                int viewMode =-100;
                if (Constants.GRIDVIEW_MODE== isListOrGrid())
                    viewMode =1;
                else
                    viewMode =0;
                ReturnData rd = new ReturnData(viewMode,Constants.RETURN_SAMBA,
                        currentPage, position, "");
                returnStack.push(rd);

                SmbAuthentication localSmbAuth = smbDevice.getAuth();
                if (localSmbAuth != null) {
                    //save  username and password to database
                    SharedPreferences sharedPref = mContext.getSharedPreferences(
                        LoginSambaDialog.SAMBA_SETTINGS, Context.MODE_PRIVATE);
                    Editor edr = sharedPref.edit();
                    edr.putString(LoginSambaDialog.PREF_LOGIN_USER, localSmbAuth.getName());
                    edr.putString(LoginSambaDialog.PREF_LOGIN_PWD, localSmbAuth.getPassword());
                    edr.commit();
                    Log.i(TAG, "user : " + localSmbAuth.getName());
                    Log.i(TAG, "pwd : " + localSmbAuth.getPassword());
                }
                if (!isUseHttpSambaModeOn()) {
                    smbDevice.mount(localSmbAuth);
                }
                // Specify the root directories of the scanning
                currentPath = "/mnt/samba";
                Log.i(TAG, "scan device path : " + currentPath);
                if (isUseHttpSambaModeOn()) {
                    return;
                }
                if (smbDevice.isMounted()) {
                    // Reset focus and the current page numbe
                    position = 0;
                    currentPage = 1;
                    totalPage = 1;

                    loginSambaListener
                            .onEnd(SambaDataBrowser.LOAD_SAMBA_SOURCE);
                    // Landing successful,Start scanning data
                    startScan(currentPath);
                } else {
                    loginSambaListener.onEnd(SambaDataBrowser.MOUNT_FAILED);
                    // in order to avoid Mount failed  be caused by Device or resource busy.
                    // Therefore , the unmount should be execute.
                    smbDevice.unmount();
                    state = SCAN_HOST;
                    smbDevice = null;
                }

                break;
            case NET_STATUS_NOT_SUPPORT:
            case OnRecvMsg.NT_STATUS_UNSUCCESSFUL:
            default:
                refreshUIListener.onFailed(Constants.FAILED_LOGIN_OTHER_FAILED);
                break;
            }
        }
    }

    public final synchronized void startScanSmbShareFolder(final List<SmbShareFolder> tmpLists) {
        // Startup thread scanning
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "startScanSmbShareFolder");
                    getFileFromSmbShareFolder(tmpLists);
                } catch (Exception e) {
                    Log.e(TAG, "********scan**fail**********");
                }

                // Data scan complete
                onFinish();
            }
        }).start();
    }

    /*
     * Scanning the specified directory of all files or folders.
     */

    private void getFileFromSmbShareFolder(List<SmbShareFolder> tmpLists) {
        List<BaseData> localFile = new ArrayList<BaseData>();
        List<BaseData> localFolder = new ArrayList<BaseData>();
        List<BaseData> localPicture = new ArrayList<BaseData>();
        List<BaseData> localSong = new ArrayList<BaseData>();
        List<BaseData> localVideo = new ArrayList<BaseData>();

        try {
            for (SmbShareFolder f : tmpLists) {
                String name = f.getFileName();
                BaseData file = new BaseData();
                //Log.i(TAG,"getFileFromSmbShareFolder samba name:"+name);
                file.setName(name);
                file.setPath(f.getPath());
                //Log.i(TAG,"f.getPath():"+f.getPath());
                //file.setParentPath(Constants.RETURN_SAMBA);
                if (f.isDirectory()) {
                    file.setType(Constants.FILE_TYPE_DIR);
                    localFolder.add(file);
                } else {
                    int pos = name.lastIndexOf(".");
                    String extension = "";
                    if (pos > 0) {
                        extension = name.toLowerCase().substring(pos + 1);
                        file.setFormat(extension);
                    }

                    file.setSizeEx((long)(f.getLength()));

                    if (check(name, FileConst.photoSuffix)) {
                        file.setType(Constants.FILE_TYPE_PICTURE);
                        localPicture.add(file);
                    } else if (check(name, FileConst.audioSuffix)) {
                        file.setType(Constants.FILE_TYPE_SONG);
                        localSong.add(file);
                    } else if (check(name, FileConst.videoSuffix)) {
                        file.setType(Constants.FILE_TYPE_VIDEO);
                        localVideo.add(file);
//                    } else if (check(name, resource.getStringArray(R.array.playlist_filter))) {
//                        file.setType(Constants.FILE_TYPE_MPLAYLIST);
                    } else {
                        file.setType(Constants.FILE_TYPE_FILE);
                    }
                    localFile.add(file);
                }
            }
        } catch (Exception e) {

        }
        mediaContainer.clearAll();
        if (localFile.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_FILE, localFile);
        }
        if (localFolder.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_DIR, localFolder);
        }
        if (localPicture.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_PICTURE, localPicture);
        }
        if (localSong.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_SONG, localSong);
        }
        if (localVideo.size() > 0) {
            putAllToCache(Constants.FILE_TYPE_VIDEO, localVideo);
        }
    }

    private static boolean isUseHttpSambaModeOn() {
        return true;
    }

    private static int isListOrGrid() {
      return Constants.LISTVIEW_MODE;
    }

    private void showLoginDialog() {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
          @Override
          public void run() {
              refreshUIListener.onShowLoginDialog();
              mLoginDialog = new LoginSambaDialog(
                      mContext, mLoginDlgHandler, smbDevice);
              mLoginDialog.show();
          }
      });
    }

    public String getCurrPath() {
      if (null == smbDevice) {
        return "";
      }

      Log.d(TAG, "smbDevice.GetCurrentPath() = " + smbDevice.GetCurrentPath());

      return smbDevice.GetCurrentPath();
    }

    public void backToRoot() {
      Log.d(TAG, "backToRoot()");
      if (null != smbDevice) {
        enterParentDirectory();
      }
    }

    public void setCurrentPos(final int index) {
      Log.d(TAG, "setCurrentPos(), index = " + index);
      position = index;
    }
}
