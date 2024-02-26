package com.mstar.android.media;

import java.io.FileDescriptor;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;

public class MMediaPlayer {
    public static final int MEDIA_INFO_PHOTO_DECODE_NEXT_ERR = 1020;

    public class InitParameter {
        public float degrees = 0;
        public float scaleX = 1;
        public float scaleY = 1;
        public int cropX = 0;
        public int cropY = 0;
        public int cropWidth = 0;
        public int cropHeight = 0;      
    }
 
    public interface OnErrorListener {
      boolean onError(MediaPlayer mp, int what, int extra);
    }

    public interface OnVideoSizeChangedListener {
      public void onVideoSizeChanged(MediaPlayer mp, int width, int height);
    }
    
    public interface OnInfoListener {
      boolean onInfo(MediaPlayer mp, int what, int extra);
    }
    
    public interface OnPreparedListener {
      void onPrepared(MediaPlayer mp);
    }
    
    public void reset() {
    }

    public void release() {
    }

    public void start() {
    }

    public void stop() {
    }
 
    public boolean isPlaying() {
      return false;
    }

    public int getVideoWidth() {
      return 0;
    }
        
    public int getVideoHeight() {
      return 0;
    }

    public boolean ImageRotateAndScale(float degrees, float scaleX, float scaleY, boolean isAutoCrop) {
      return false;
    }
    
    public boolean ImageCropRect(int cropX, int cropY, int cropWidth, int cropHeight) {
      return false;
    }

    public boolean ImageDecodeNext(String path, int imageSampleSize, int surfaceWidth, int surfaceHeight, InitParameter parameter, int index) {
      return false;
    }
    
    public boolean ImageDecodeNext(FileDescriptor fd, int imageSampleSize, int surfaceWidth, int surfaceHeight, InitParameter parameter, int index) {
      return false;
    }
        
    public boolean ImageShowNext(int index) {
      return false;
    }

    public boolean SetImageSampleSize(int imageSampleSize, int surfaceWidth, int surfaceHeight, InitParameter parameter) {
      return false;
    }
    
    public void setOnErrorListener(OnErrorListener listener) {    
    }
    
    public void prepareAsync() {
    }
    
    public void setOnInfoListener(OnInfoListener listener) {
    }
    
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener) {
    }
    
    public void setOnPreparedListener(OnPreparedListener listener) {
    }
    
    public void setDataSource(Context ctx, Uri path) {
    }
    
    public void setDisplay(SurfaceHolder sh) {
    }    
}
