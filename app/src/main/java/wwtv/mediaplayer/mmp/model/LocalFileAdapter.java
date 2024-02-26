
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import android.graphics.Bitmap;
import java.io.FileInputStream;

import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.AudioFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.PhotoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.UsbFileOperater;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.VideoFile;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

public class LocalFileAdapter extends FileAdapter {
  private static final String TAG = "LocalFileAdapter";
  private final MtkFile mFile;
  private String mName;
  private static UsbFileOperater sOperator = UsbFileOperater.getInstance();
  private String mLabel;

  public LocalFileAdapter(MtkFile file) {
    mFile = file;
  }

  public LocalFileAdapter(String path) {
    mFile = new MtkFile(path);
  }

  public LocalFileAdapter(String path, String name) {
    mFile = new MtkFile(path);
    mName = name;
  }

  public LocalFileAdapter(String path, String name, String label) {
    mFile = new MtkFile(path);
    mName = name;
    mLabel = label;
  }

  public LocalFileAdapter(File file) {
    mFile = new MtkFile(file);
  }

  @Override
  public void stopDecode() {
    if (mFile instanceof PhotoFile) {
      MtkLog.i(TAG, " Bitmap  stopDecode --------------");
      ((PhotoFile) mFile).stopDecode();
    }
  }

  @Override
  public boolean isPhotoFile() {
    return mFile.isPhotoFile();
  }

  @Override
  public boolean isAudioFile() {
    return mFile.isAudioFile();
  }

  @Override
  public boolean isVideoFile() {
    return mFile.isVideoFile();
  }

  @Override
  public boolean isTextFile() {
    return mFile.isTextFile();
  }

  @Override
  public String getSize() {
    return mFile.getSize();
  }

  @Override
  public String getTextSize() {
    return getTextSize(mFile.getFileSize());
  }

  @Override
  public String getAbsolutePath() {
    return mFile.getAbsolutePath();
  }

  @Override
  public String getPath() {
    return mFile.getPath();
  }

  @Override
  public String getDeviceName() {
    if (mName != null && mName.length() > 0) {
      return mName;
    }

    return mFile.getName();

  }

  @Override
  public String getName() {

    if (mLabel != null && mLabel.length() > 0) {
      return mLabel;
    }

    if (mFile != null) {
        String path = mFile.getPath();
        String rootPath = mFile.getParent();
        if (path != null && path.length()>0) {
            MtkLog.i(TAG, " path:" + path + " rootPath:" + rootPath);
            if (rootPath != null && rootPath.equals("/storage")) {
                int rootIndex = path.indexOf("/storage/");
                if (rootIndex >= 0) {
                    path = path.substring(rootIndex + 9, path.length());
                    return path;
                }
            } else {
                if (mName != null && mName.length()>0) {
                    return mName;
                }
            }
        }
      return mFile.getName();
    } else {
        if (mName != null && mName.length()>0) {
            return mName;
        }
    }
    return "";
  }

  public String getFileName() {
    if (null != mFile){
        return mFile.getName();
    } else {
        return "";
    }
  }

  @Override
  public Bitmap getThumbnail(int width, int height, boolean isThumbnail) {
    if ((isPhotoFile() || isThrdPhotoFile()) && !isValidSizePhoto()) {
      return null;
    }
    return mFile.getThumbnail(width, height, isThumbnail);
  }

  @Override
  public boolean isDirectory() {
    return mFile.isDirectory();
  }

  @Override
  public boolean isFile() {
    return mFile.isFile();
  }

  @Override
  public long lastModified() {
    return mFile.lastModified();
  }

  @Override
  public void stopThumbnail() {

    mFile.stopThumbnail();
  }

  @Override
  public long length() {
    return mFile.length();
  }

  @Override
  public boolean delete() {
    try {
      sOperator.addFileToDeleteList(mFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    sOperator.deleteFiles();

    return true;
  }

  @Override
  public String getInfo() {
    return getInfoByCache(false);
  }

  private String getInfoByCache(boolean isCache) {
    String info = "";
    MetaData data = null;
    if (isPhotoFile() || isThrdPhotoFile()) {
      if (isValidSizePhoto()) {
        info = assemblyInfos(getName(), getResolution(), getSize());

      } else {
        info = assemblyInfos(getName(), "", getSize());

      }

    } else if (isAudioFile() && (mFile instanceof AudioFile)) {
      AudioFile file = (AudioFile) mFile;
      if (isCache) {
        data = Info.getCacheMetaData();
        if (null == data) {
          return null;
        }
      } else {
        data = file.getMetaDataInfo();
      }

      if (null == data) {

        info = assemblyInfos(getName(), "", "", getSize());
      } else {
        String title = data.getTitle();
        if (title == null || title.length() <= 0) {
          title = getName();
        }
        info = assemblyInfos(title, data.getAlbum(), data.getGenre(), data
            .getYear(), getSize());
      }

    } else if (isVideoFile() && (mFile instanceof VideoFile)) {
      VideoFile file = (VideoFile) mFile;
      if (isCache) {
        data = Info.getCacheMetaData();
        if (null == data) {
          return null;
        }
      } else {
        data = file.getMetaDataInfo();
      }

      if (null == data) {

        info = assemblyInfos(getName(), "", "", getSize());
      } else {

        String title = data.getTitle();
        if (title == null || title.length() <= 0) {
          title = getName();
        }
        int dur = data.getDuration();
        MtkLog.i(TAG, "$$$$$$$$$$$$time = " + dur);
        if (getName() != null && getName().toLowerCase(Locale.ROOT).endsWith(".pvr")) {
          info = assemblyInfos(title, data.getYear(), "", getSize());
        } else {
          info = assemblyInfos(title, data.getYear(), setTime(dur), getSize());
        }

      }

    } else if (isTextFile()) {
      info = assemblyInfos(getName(), getLastModified(), getTextSize());
    }

    return info;
  }

  @Override
  public String getPreviewBuf() {
    MtkLog.d(TAG, "getPreviewBuf getAbsolutePath =" + getAbsolutePath());
    FileInputStream fileIS = null;
    try {
      fileIS = new FileInputStream(getAbsolutePath());
      return super.getPreviewBuf(fileIS);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      try{
        fileIS.close();
      } catch (Exception e) {
      }
    }

    return "";

  }

  @Override
  public String getResolution() {
    // String resolution = null;
    // if (isPhotoFile()) {
    // resolution = mFile.getResolution();
    // }

    return mFile.getResolution();
  }

  @Override
  public String getSuffix() {
    return "";
  }

  @Override
  public String getCacheInfo() {

    return getInfoByCache(true);
  }
}
