
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.media.MediaPlayer;

import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Util;

public class MtkVideoView extends SurfaceView {
  private static final String TAG = "MtkVideoView";
  private VideoManager mVideoManager;
  private final Context mContext;
  // Added by Dan for fix bug DTV00375890
  private boolean mIsStop;

  public MtkVideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    init();
  }

  public MtkVideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    init();
  }

  public MtkVideoView(Context context) {
    this(context, null, 0);
    init();
  }

  public void init() {
    MultiFilesManager filesManager = MultiFilesManager
        .getInstance(mContext);
    int source = filesManager.getCurrentSourceType();
    switch (source) {
      case MultiFilesManager.SOURCE_LOCAL:
        source = VideoConst.PLAYER_MODE_MMP;
        break;
      case MultiFilesManager.SOURCE_SMB:
        source = VideoConst.PLAYER_MODE_SAMBA;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        source = VideoConst.PLAYER_MODE_DLNA;
        break;
      default:
        break;
    }

    mVideoManager = VideoManager.getInstance(this, source);
    mVideoManager.setPreviewMode(true);
    mVideoManager.setOnCompletionListener(mCompleteListener);
    mVideoManager.setOnPreparedListener(preparedListener);
  }

  public int getVideoPlayStatus() {
    if (null == mVideoManager) {
      return VideoConst.PLAY_STATUS_INITED;
    }
    return mVideoManager.getPlayStatus();
  }

  private final MediaPlayer.OnCompletionListener
  mCompleteListener = new MediaPlayer.OnCompletionListener() {

    @Override
    public void onCompletion(MediaPlayer mp) {
      Util.logListener(TAG + "--MediaPlayer onCompletion");
      handleCompletion();
    }

  };

  // MEDIAPLAYER
  private final MediaPlayer.OnPreparedListener
  preparedListener = new MediaPlayer.OnPreparedListener() {

    @Override
    public void onPrepared(MediaPlayer mp) {
      Util.logListener(TAG + "--MediaPlayer onPrepared");
      handlePrepare();
    }
  };

  private void handleCompletion() {
    if (mIsStop) {
      return;
    }

    mVideoManager.autoNext();

  }

  protected void handlePrepare() {
    // TODO Auto-generated method stub
    if (mVideoManager != null) {
        mVideoManager.startVideoFromDrm();
    }
  }

  public void setPreviewMode(boolean model) {
    mVideoManager.setPreviewMode(model);
  }

  public boolean isVideoPlaybackInit() {
    return mVideoManager == null ? false : true;
  }

  public void play(String path) {
    try {
      // Added by Dan
      mVideoManager.setDataSource(path);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Added by Dan for fix bug DTV00375890
    mIsStop = false;
  }

  public void stop() {
    if (mVideoManager != null) {
      try {
        // Added by Dan for fix bug DTV00375890
        mIsStop = true;
        mVideoManager.stopVideo();

      } catch (IllegalStateException e) {
        MtkLog.w(TAG, e.getMessage());
      }
    }
  }

  public void reset() {
    if (mVideoManager != null) {
      try {
        // Added by Dan for fix bug DTV00375890

        if (Util.mIsUseEXOPlayer) {
          mVideoManager.stopVideo();
          mVideoManager.reset();
//          mVideoManager.onRelease();
        } else {
          mVideoManager.reset();
        }
        mVideoManager.setContext(mContext);

      } catch (IllegalStateException e) {
        MtkLog.w(TAG, e.getMessage());
      }
    }
  }

  public void onRelease() {

    if (mVideoManager != null) {
      try {
        stop();
        mVideoManager.setPreviewMode(false);
        mVideoManager.onRelease();
        mVideoManager = null;
      } catch (IllegalStateException e) {
        MtkLog.w(TAG, e.getMessage());
      }
    }

    /* video had been close and send broadcast tell it. */
    // LogicManager.getInstance(mContext).videoZoomReset();

    // LogicManager.getInstance(mContext).sendCloseBroadCast();
  }
}
