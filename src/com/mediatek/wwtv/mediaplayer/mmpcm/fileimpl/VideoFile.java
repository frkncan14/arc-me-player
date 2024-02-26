package com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl;

import java.io.File;
import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;

import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.util.Util;

public class VideoFile extends MtkFile {
    static private VideoInfo info = null;
    static private Thumbnail vThumb = null;

    private static final long serialVersionUID = 1L;

    public static void openFileInfo(Context context) {
        info = VideoInfo.getInstance();
        vThumb = Thumbnail.getInstance();
    }

    public VideoFile(MtkFile f) {
        super(f.getPath());
    }

    public VideoFile(URI uri) {
        super(uri);
    }

    public VideoFile(String dirPath, String name) {
        super(dirPath, name);
    }

    public VideoFile(String path) {
        super(path);
    }

    public VideoFile(File dir, String name) {
        super(dir, name);
    }

    @Override
    public Bitmap getThumbnail(int width, int height,boolean isThumbnail) {
        if (vThumb != null) {
            Bitmap bp = null;
            bp =  vThumb.getVideoThumbnail(FileConst.SRC_USB, this.getAbsolutePath(), width, height);
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
                    //LogicManager.getInstance().setDisplayRegionToFullScreen();
                  }
                }, 1500);
              }
            }
            return bp;
        }
        return null;
    }

    @Override
    public void stopThumbnail(){
        if (vThumb == null){
            return;
        }

        vThumb.stopThumbnail();
    }

    public MetaData getMetaDataInfo(){
        if (null == info) {
            return null;
        }

        return info.getMetaDataInfo(this.getAbsolutePath(),FileConst.SRC_USB);
    }

    public void stopMetaDataInfo(){
    	if (null == info){
    		return;
    	}

    	info.stopMetaData();
    }

    public Bitmap[] getMultiSpecificThumbnail(URI uri[]) {
        return new Bitmap[0];
    }

    public Bitmap getFrameThumbnail(int frame) {
        return null;
    }

}
