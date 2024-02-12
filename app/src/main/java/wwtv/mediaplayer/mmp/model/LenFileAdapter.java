package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Locale;

import jcifs.smb.SmbException;
import android.graphics.Bitmap;

import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.CorverPic;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoInfo;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;
import com.mediatek.wwtv.util.Util;

import android.util.Log;

public class LenFileAdapter extends FileAdapter {
  private static final String TAG = "LenFileAdapter";
  private final String mPath;
  private static SambaManager sManager = SambaManager.getInstance();
  private AudioInfo mAudioInfo;
  private VideoInfo mFileInfo;
  private boolean mIsDir;
  private boolean mIsFile;
  private long mFileLength;

  public LenFileAdapter(String path) {
    mPath = path;
  }

  public LenFileAdapter(String path, AudioInfo audioInfo, VideoInfo fileInfo,
      boolean isDir, boolean isFile, long fileLength) {
    this(path);
    mAudioInfo = audioInfo;
    mFileInfo = fileInfo;
    mIsDir = isDir;
    mIsFile = isFile;
    mFileLength = fileLength;
  }

  public long getFileSize() {
    long lSize;
    if (this.isFile()) {
      lSize = this.length();
    } else {
      lSize = 0;
    }
    return lSize;
  }

  @Override
  public String getSize() {

    return getSize(getFileSize());
  }

  @Override
  public String getTextSize() {
    return getTextSize(getFileSize());
  }

  @Override
  public boolean delete() {
    return false;
  }

  @Override
  public String getAbsolutePath() {
    return mPath;
  }

  @Override
  public String getPath() {
    return mPath;
  }

  @Override
  public String getName() {
    return sManager.getFileName(mPath);
  }

  @Override
  public boolean isDirectory() {
    return mIsDir;
  }

  @Override
  public boolean isFile() {
    return mIsFile;
  }

  @Override
  public String getLastModified() {
    return "-";
  }

  @Override
  public long lastModified() {
    return 0;
  }

  @Override
  public long length() {
    return mFileLength;
  }

  @Override
  protected InputStream getInputStream() {
    try {
      return sManager.getFileStream(mPath);
    } catch (MalformedURLException | SmbException | UnknownHostException e) {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public String getPreviewBuf() {
    Log.d(TAG, "getPreviewBuf mpath = " + mPath);
    InputStream is = null;
    try {

      is = sManager.getFileStream(mPath);

      return super.getPreviewBuf(is);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      try{
        is.close();
      } catch (Exception e) {
      }
    }

    return "";

  }

  @Override
  public Bitmap getThumbnail(int width, int height, boolean isThumbnail) {
    Bitmap bitmap = null;
    if (isVideoFile()) {
      Thumbnail vThumb = Thumbnail.getInstance();
      bitmap = vThumb.getVideoThumbnail(FileConst.SRC_SMB,
          this.getPath(), width, height);
      if (!vThumb.hasResetRigion() && LogicManager.getInstance(null) != null
          && !Util.mIsEnterPip && Util.mIsMmpFlag) {
        if (vThumb.getContext() != null) {
          Util.exitPIP(vThumb.getContext());
        }
        if (LogicManager.getInstance(null) != null
            && LogicManager.getInstance(null).getThreadHandler() != null) {
          vThumb.setRestRigionFlag(true);
          LogicManager.getInstance(null).getThreadHandler().postDelayed(new Runnable() {

            @Override
            public void run() {
              LogicManager.getInstance(null).setDisplayRegionToFullScreen();
            }
          }, 1500);
        }
      }
    } else if ((isPhotoFile() || isThrdPhotoFile()) && isValidSizePhoto()) {
      InputStream is = null;
      try{
        is = getInputStream();
        bitmap = decodeBitmap(is, width, height);
      } catch (Exception e) {
      } finally {
        try{
          is.close();
        }catch (Exception e){
        }
      }
    } else if (isAudioFile()) {
      CorverPic aCorver = CorverPic.getInstance();
      bitmap = aCorver.getAudioCorverPic(FileConst.SRC_SMB,
          this.getPath(), width, height);
    }

    return bitmap;
  }

  @Override
  public boolean isValidSizePhoto() {
    long size = length();
    return size >= 0 && size < FileConst.MAX_PHOTO_SIZE;

  }

  @Override
  public void stopThumbnail() {
    if (isVideoFile()) {
      Thumbnail vThumb = Thumbnail.getInstance();
      vThumb.stopThumbnail();

    } else if (isPhotoFile()) {
      stopDecode();

    } else if (isAudioFile()) {
      CorverPic aCorver = CorverPic.getInstance();
      aCorver.stopThumbnail();
    }
  }

  @Override
  public String getInfo() {
    return getInfoByCache(false);
  }

  private String getInfoByCache(boolean isCache) {

    String info;
    MetaData data = null;

    if (isPhotoFile() || isThrdPhotoFile()) {
      if (isValidSizePhoto()) {
        info = assemblyInfos(getName(), getResolution(), getSize());
      } else {
        info = assemblyInfos(getName(), "", getSize());

      }

    } else if (isAudioFile()) {
      if (isCache) {
        data = Info.getCacheMetaData();
        if (null == data) {
          return null;
        }
      } else {
        data = mAudioInfo
            .getMetaDataInfo(mPath, FileConst.SRC_SMB);
      }

      if (data == null) {
        info = assemblyInfos(getName(), "", "", "", getSize());

      } else {
        String title = data.getTitle();
        if (title == null || title.length() <= 0) {
          title = getName();
        }
        info = assemblyInfos(title, data.getAlbum(), data.getGenre(),
            data.getYear(), getSize());
      }
    } else if (isVideoFile()) {
      if (isCache) {
        data = Info.getCacheMetaData();
        if (null == data) {
          return null;
        }
      } else {
        data = mFileInfo.getMetaDataInfo(mPath, FileConst.SRC_SMB);
      }

      if (data == null) {
        info = assemblyInfos(getName(), "", "", getSize());

      } else {

        String title = data.getTitle();
        if (title == null || title.length() <= 0) {
          title = getName();
        }
        info = assemblyInfos(title, data.getYear(),
            setTime(data.getDuration()), getSize());
      }

    } else {
      info = assemblyInfos(getName(), getLastModified(), getTextSize());
    }

    return info;
  }

  @Override
  public String getResolution() {
    return getResolution(getInputStream());
  }

  @Override
  public String getSuffix() {

    String name = getName();
    Log.i("sort", "getSuffix:name:" + name);
    if (name == null) {
      return "";
    }
    if (name.lastIndexOf('.') == -1) {
      return "";
    } else {
      return name.substring(name.lastIndexOf('.')).toLowerCase(Locale.ROOT);
    }
  }

  @Override
  public String getCacheInfo() {
    return getInfoByCache(true);
  }
}
