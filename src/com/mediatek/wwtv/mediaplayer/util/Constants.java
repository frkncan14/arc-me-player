package com.mediatek.wwtv.mediaplayer.util;

public class Constants {
    public static final int POSITION_0 = 0;
    public static final int POSITION_9 = 9;
    public static final int LIST_MODE_DISPLAY_NUM = 9;
    public static final int GRID_MODE_DISPLAY_NUM = 9;
    public static final int GRID_MODE_ONE_ROW_DISPLAY_NUM = 5;

    public static final int OPTION_STATE_ALL = 1;
    public static final int OPTION_STATE_PICTURE = 2;
    public static final int OPTION_STATE_SONG = 3;
    public static final int OPTION_STATE_VIDEO = 4;

    public static final int LISTVIEW_MODE = 0 ;
    public static final int GRIDVIEW_MODE = 1 ;

    public static final String RETURN_TOP = "top";
    public static final String RETURN_LOCAL = "local";
    public static final String RETURN_SAMBA = "samba";
    public static final String RETURN_NOT_SAMBA = "notsamba";
    public static final String BUNDLE_PAGE = "current_page";
    public static final String BUNDLE_TPAGE = "total_page";
    public static final String BUNDLE_INDEX = "current_index";

    public static final int FILE_TYPE_FILE = 1;
    public static final int FILE_TYPE_PICTURE = 2;
    public static final int FILE_TYPE_SONG = 3;
    public static final int FILE_TYPE_VIDEO = 4;
    public static final int FILE_TYPE_DIR = 5;
    public static final int FILE_TYPE_RETURN = 6;
    public static final int FILE_TYPE_MPLAYLIST = 7;
    public static final int FILE_TYPE_CHINADRM = 8;

    private static final int UPDATE_BASE = 1000;
    public static final int UPDATE_ALL_SAMBA_DATA = UPDATE_BASE + 1;
    public static final int UPDATE_SAMBA_DATA =  UPDATE_BASE +2;
    public static final int UPDATE_SCAN_DEVICE_COMPLETED =  UPDATE_BASE +3;
    public static final int UPDATE_EXCEPTION_INFO =  UPDATE_BASE + 4;
    public static final int UPDATE_PROGRESS_INFO =  UPDATE_BASE + 5;
    public static final int UPDATE_SHOW_LOGIN_DIALOG =  UPDATE_BASE + 6;
    public static final int UPDATE_SAMBA_BACK_TO_ROOT=  UPDATE_BASE + 7;

    private static final int LOGIN_STATUS_BASE = 1500;
    public static final int LOGIN_STATUS_LOGIN_SAMBA = LOGIN_STATUS_BASE + 1;
    public static final int LOGIN_STATUS_LOGIN_SUCCESS = LOGIN_STATUS_BASE + 2;
    public static final int LOGIN_STATUS_LOGOUT_SAMBA = LOGIN_STATUS_BASE + 3;
    public static final int LOGIN_STATUS_LOAD_SAMBA_DEVICE = LOGIN_STATUS_BASE + 4;
    public static final int LOGIN_STATUS_LOAD_SAMBA_SOURCE = LOGIN_STATUS_BASE + 5;
    public static final int LOGIN_STATUS_LOGOUT_DONE = LOGIN_STATUS_BASE + 6;
    public static final int LOGIN_STATUS_MOUNT_FAILED = LOGIN_STATUS_BASE + 7;
    public static final int LOGIN_STATUS_LOGIN_CANCEL = LOGIN_STATUS_BASE + 8;

    private static final int FAILED_BASE = 2000;
    public static final int FAILED_TIME_OUT = FAILED_BASE + 1;
    public static final int FAILED_WRONG_PASSWD = FAILED_BASE + 2;
    public static final int FAILED_LOGIN_FAILED = FAILED_BASE + 3;
    public static final int FAILED_LOGIN_OTHER_FAILED = FAILED_BASE + 4;
    public static final int MOUNT_FAILED = FAILED_BASE + 5;
    
    /* image player */    
    public static final int SOURCE_FROM_SAMBA = 0x11;
    public static final int SOURCE_FROM_LOCAL = 0x12;
    public static final String MPO = "mpo";
    public static final String GIF = "gif";
    public static final String SOURCE_FROM = "sourceFrom";
    public static boolean isExit = true;
    public static boolean bPhotoSeamlessEnable = false;
    public static boolean bReleasingPlayer = false;
    public static boolean bSupportPhotoScale = true;
}
