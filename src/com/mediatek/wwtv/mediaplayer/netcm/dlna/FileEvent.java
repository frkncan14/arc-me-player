
package com.mediatek.wwtv.mediaplayer.netcm.dlna;

import java.util.LinkedList;
import java.util.List;

import com.mediatek.dlna.FoundDeviceEvent;
import com.mediatek.dlna.LeftDeviceEvent;
import com.mediatek.dlna.object.Content;
import com.mediatek.dlna.object.ContentType;
import com.mediatek.dlna.object.DLNADevice;
import com.mediatek.dlna.object.FailedContentEvent;
import com.mediatek.dlna.object.NormalContentEvent;
import com.mediatek.wwtv.mediaplayer.netcm.util.NetLog;

/**
 * Auxiliary class for DLNA manager.
 *
 */
public class FileEvent {
  public static final int FILTER_TYPE_ALL = 0;
  public static final int FILTER_TYPE_VIDEO = 1;
  public static final int FILTER_TYPE_AUDIO = 2;
  public static final int FILTER_TYPE_IMAGE = 3;
  public static final int FILTER_TYPE_TEXT = 4;

  private final static String TAG = "FileEvent";
  // public static final int EVENT_TYPE_UNKNOW = 0;
  // public static final int EVENT_TYPE_FOUND_DEVICE = 1;
  // public static final int EVENT_TYPE_LEFT_DEVICE = 2;
  // public static final int EVENT_TYPE_NORMAL_CONTENT = 3;
  // public static final int EVENT_TYPE_FAILED_CONTENT = 4;

  // private int mFileType = EVENT_TYPE_UNKNOW;
  //private FoundDeviceEvent mFileFoundEvent;
  private LeftDeviceEvent mFileLeftEvent;
  //private NormalContentEvent mFileNormalEvent;
  //private FailedContentEvent mFileFailEvent;
  private List<DLNAFile> mFileNameList = null;
  private List<DLNAFile> mContentList = null;

  public FileEvent(FoundDeviceEvent fileFoundEvent,
      List<DLNAFile> nameList) {
    NetLog.d(TAG, "new FoundDeviceEvent,fileFoundEvent=="+fileFoundEvent);
    //this.mFileFoundEvent = fileFoundEvent;
    // this.mFileType = EVENT_TYPE_FOUND_DEVICE;
    this.mFileNameList = new LinkedList<DLNAFile>();
    this.mFileNameList = nameList;
  }

  public FileEvent(LeftDeviceEvent fileLeftEvent,
      List<DLNAFile> nameList) {
    NetLog.d(TAG, "new LeftDeviceEvent");
    this.mFileLeftEvent = fileLeftEvent;
    // this.mFileType = EVENT_TYPE_LEFT_DEVICE;
    this.mFileNameList = new LinkedList<DLNAFile>();
    this.mFileNameList = nameList;
  }

  public FileEvent(NormalContentEvent fileNormalEvent,
      List<DLNAFile> contentList) {
    NetLog.d(TAG, "new NormalContentEvent,fileNormalEvent=="+fileNormalEvent);
    //this.mFileNormalEvent = fileNormalEvent;
    // this.mFileType = EVENT_TYPE_NORMAL_CONTENT;
    this.mFileNameList = new LinkedList<DLNAFile>();
    this.mContentList = contentList;
    // for (int i = 0; i < fileNormalEvent.getList().size(); i++){
    // mFileNameList.add(fileNormalEvent.getList().get(i).getTitle());
    // }
    for (DLNAFile dlnaFile : contentList) {
      this.mFileNameList.add(dlnaFile);
    }
  }

  public FileEvent(FailedContentEvent fileFailEvent) {
    NetLog.d(TAG, "new FailedContentEvent,fileFailEvent=="+fileFailEvent);
    //this.mFileFailEvent = fileFailEvent;
    // this.mFileType = EVENT_TYPE_FAILED_CONTENT;
  }

  // public int getEventType(){
  // return this.mFileType;
  // }

  // public DLNAEvent getEvent(int type){
  // switch(type){
  // case EVENT_TYPE_UNKNOW:
  // return null;
  // case EVENT_TYPE_FOUND_DEVICE:
  // return mFileFoundEvent;
  // case EVENT_TYPE_LEFT_DEVICE:
  // return mFileLeftEvent;
  // case EVENT_TYPE_NORMAL_CONTENT:
  // return mFileNormalEvent;
  // case EVENT_TYPE_FAILED_CONTENT:
  // return mFileFailEvent;
  // default:
  // break;
  // }
  // return null;
  // }

  /**
   * Get the file list of current path.
   *
   * @return the file list of current path.
   */
  public List<DLNAFile> getFileList() {
    NetLog.d(TAG, "getFileList");
    return this.mFileNameList;
  }

  private void multiMediaFilter() {
    NetLog.d(TAG, "multiMediaFilter");
    this.mFileNameList.clear();
    for (DLNAFile dlnaFile : mContentList) {
      Content cnt = dlnaFile.getContent();
      ContentType type = cnt.getType();
      if (cnt.isDirectory() || type == ContentType.Video
          || type == ContentType.Audio || type == ContentType.Photo
          || type == ContentType.Playlist){
        mFileNameList.add(dlnaFile);
      }
    }
  }

  private void videoFilter() {
    NetLog.d(TAG, "videoFilter");
    this.mFileNameList.clear();
    for (DLNAFile dlnaFile : mContentList) {
      if (dlnaFile != null) {
        Content cnt = dlnaFile.getContent();
        if (cnt.isDirectory() || cnt.getType() == ContentType.Video){
          mFileNameList.add(dlnaFile);
        }
      }
    }
  }

  private void audioFilter() {
    NetLog.d(TAG, "audioFilter");
    this.mFileNameList.clear();
    for (DLNAFile dlnaFile : mContentList) {
      Content cnt = dlnaFile.getContent();
      if (cnt.isDirectory() || cnt.getType() == ContentType.Audio){
        mFileNameList.add(dlnaFile);
      }
    }
  }

  private void imageFilter() {
    NetLog.d(TAG, "imageFilter");
    this.mFileNameList.clear();
    for (DLNAFile dlnaFile : mContentList) {
      Content cnt = dlnaFile.getContent();
      if (cnt.isDirectory() || cnt.getType() == ContentType.Photo){
        mFileNameList.add(dlnaFile);
      }
    }
  }

  private void textFilter() {
    NetLog.d(TAG, "textFilter");
    this.mFileNameList.clear();
    for (DLNAFile dlnaFile : mContentList) {
      Content cnt = dlnaFile.getContent();
      if (cnt.isDirectory() || cnt.getType() == ContentType.Playlist){
        mFileNameList.add(dlnaFile);
      }
    }
  }

  /**
   * Get the file list of current path, include a filter.
   *
   * @param filterType  the type of filter.
   * @return the file list of current path, filter by parameter.
   */
  public List<DLNAFile> getFileList(int filterType) {
    NetLog.d(TAG, "getFileList & filter");
    if (mContentList != null) {
      switch (filterType) {
        case FILTER_TYPE_VIDEO:
          videoFilter();
          break;
        case FILTER_TYPE_AUDIO:
          audioFilter();
          break;
        case FILTER_TYPE_IMAGE:
          imageFilter();
          break;
        case FILTER_TYPE_TEXT:
          textFilter();
          break;
        case FILTER_TYPE_ALL:
        default:
          multiMediaFilter();
          break;
      }
    }

    return this.mFileNameList;
  }

  public DLNADevice getLeftDevice() {
    if (mFileLeftEvent == null) {
      return null;
    }

    return mFileLeftEvent.getDevice();
  }

}
