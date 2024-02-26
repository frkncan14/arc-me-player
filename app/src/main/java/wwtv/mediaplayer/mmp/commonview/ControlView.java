
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.DolbylogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;

import com.mediatek.twoworlds.tv.MtkTvVolCtrl;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;

/**
 * {@link PopupWindow}
 *
 * @author hs_weihuiluo
 *
 */
public class ControlView extends PopupWindow {

  private static final String TAG = "ControlView";

  private Context mContext;

  private View vControlView;

  private ImageView vPStatePlay;

  private ImageView vPStatePause;

  private TextView vVideoSpeed;

  private TextView vPMediaType;

  private TextView vPZoomSize;

  private TextView vPRepeatTv;

  private ImageView vPShuffle;

  private TextView vPTimeLong;

  private TextView vPOrder;

  private TextView vPView;

  private TextView vPFileName;

  private TextView vMStartTime;

  private TextView vMEndTime;

  private ProgressBar vMProgressBar;

  private TextView vMVolumeTv;

  private ProgressBar vMVolumeBar;

  private TextView mVideoTrackNumber;
  private TextView mMusicTrackNumber;

  private TextView mAudioTypeIcon;
  private TextView mVideoTypeIcon;

  private ImageView mRepeatLogo;

  private TextView mVideoSubtitle;

  private ImageView mVideoSubtitleIcon;

  private LinearLayout mVolumeProgressBg;

  private ControlPlayState mControlImp;

  private Drawable repeatAll;

  private Drawable repeatOne;

  private LogicManager mLogicManager;

  private RelativeLayout mPlayPauseLayout;
  private RelativeLayout mVideoLayout;
  private LinearLayout mControlbottom;

  private short subtitleIndex = -1;
  private short audioTrackIndex;

  // Added by Dan for fix bug DTV00376577
  private boolean mIsPlaying = true;

  private int mediaType;

  private int type;
  private int mDubiIconWidth;
  private int mDubiIconHeight;
  private int mDubiVisionIconWidth;
  private int mDubiVisionIconHeight;
  // private int mDtsIconWidth;
  // private int mDtsIconHeight;

  // arcelik customized
  private ImageView mVideoTrackNumberIcon;
  private ImageView vVideoSpeedIcon;

  public interface ControlPlayState {
    void play();

    void pause();
  }

  public ControlView(View contentView) {
    super(contentView);
  }

  public ControlView(Context context, int mediatype,
      ControlPlayState statecontrol, View contentView, int width,
      int height) {
    super(contentView, width, height);
    mediaType = mediatype;
    vControlView = contentView;
    mControlImp = statecontrol;
    mContext = context;
    mLogicManager = LogicManager.getInstance(context);

    findCommonView();
    initDrawable();
    switchView(mediatype);

    setOnDismissListener(mDismissListener);
    if (sHdrType != 0) {
      updateHdrShow(sHdrType);
    }
  }

  // add by shuming for fix CR: DTV00407914

  private void setTopLayoutVisible(int visibility) {
    if (mVideoLayout != null) {
      mVideoLayout.setVisibility(visibility);
    } else {
      setPlayPauseLayoutVisible(visibility);
    }
  }

  private void setPlayPauseLayoutVisible(int visibility) {
    if (mPlayPauseLayout != null) {
      mPlayPauseLayout.setVisibility(visibility);
    }
  }

  private void setControlBottomLayoutVisible(int visibility) {
    if (mControlbottom != null) {
      mControlbottom.setVisibility(visibility);
    }
  }

  public void hiddlen(int visibility) {
    // new Exception().printStackTrace();
    setTopLayoutVisible(visibility);
    setControlBottomLayoutVisible(visibility);
    affectRepeatLogo(visibility);
  }

  private void affectRepeatLogo(int visibility) {
    if (visibility == View.VISIBLE) {
      if (null != mRepeatLogo
          && (mRepeatLogo.getVisibility() != View.VISIBLE)) {
        setRepeatLogoVisible(View.VISIBLE);
      }
    } else {
      if (null != mRepeatLogo) {
        setRepeatLogoVisible(View.GONE);
      }
    }
  }

  private void setRepeatLogoVisible(int visibility) {
    if (null != mRepeatLogo) {
      mRepeatLogo.setVisibility(visibility);
    }
  }

  public boolean isShowed() {
    int isShowed = 0;
    if (vControlView != null) {
      MtkLog.i(TAG, "isShowed vControlView != null");
      if (mControlbottom != null && mControlbottom.getVisibility() == View.VISIBLE) {
        isShowed += 1;
      }
      if (mVideoLayout != null) {
        if (mVideoLayout.getVisibility() == View.VISIBLE) {
          isShowed += 4;
        }
      } else {
        if (mPlayPauseLayout != null && mPlayPauseLayout.getVisibility() == View.VISIBLE) {
          isShowed += 2;
        }
      }
    }
    MtkLog.i(TAG, "isShowing:" + isShowed);
    return isShowed > 0;
  }

  private final OnDismissListener mDismissListener = new OnDismissListener() {

    @Override
    public void onDismiss() {
      // vControlView = null;
      // mContext = null;
    }
  };

  public void initDrawable() {
    repeatOne = mContext.getResources().getDrawable(
        R.drawable.mmp_toolbar_icon_repeat_one);
    repeatOne.setBounds(0, 0, repeatOne.getMinimumWidth(), repeatOne
        .getMinimumHeight());
    repeatAll = mContext.getResources().getDrawable(
        R.drawable.mmp_toolbar_typeicon_repeat);
    repeatAll.setBounds(0, 0, repeatOne.getMinimumWidth(), repeatOne
        .getMinimumHeight());

    mDubiIconWidth = (int) mContext.getResources().getDimension(R.dimen.dubi_icon_width);
    mDubiIconHeight = (int) mContext.getResources().getDimension(R.dimen.dubi_icon_height);
    mDubiVisionIconWidth = (int) mContext.getResources().getDimension(R.dimen.dubi_vision_icon_width);
    mDubiVisionIconHeight = (int) mContext.getResources().getDimension(R.dimen.dubi_vision_icon_height);
    // mDtsIconWidth =
    // (int)mContext.getResources().getDimension(R.dimen.dts_icon_width);
    // mDtsIconHeight =
    // (int)mContext.getResources().getDimension(R.dimen.dts_icon_height);
  }

  private void switchView(int mediatype) {
    if (mediatype == MultiMediaConstant.PHOTO) {
      findPhotoView();
      vPMediaType.setText(mContext.getString(R.string.mmp_photo));
    } /* else if (mediatype == MultiMediaConstant.THRD_PHOTO) {
      findThrdPhotoView();
      vPMediaType.setText(mContext.getString(R.string.mmp_photo));
    } */ else if (mediatype == MultiMediaConstant.AUDIO) {
      findMusicView();
      vPMediaType.setText(mContext.getString(R.string.mmp_audio));
    } else if (mediatype == MultiMediaConstant.VIDEO) {
      findVideoView();
      vPMediaType.setText(mContext.getString(R.string.mmp_video));
    } else if (mediatype == MultiMediaConstant.TEXT) {
      findTextView();
      vPMediaType.setText(mContext.getString(R.string.mmp_text));
    }
    // Add by keke 1215 for fix cr DTV00383194
    setInforbarTransparent();
  }

  public void initMusicTrackNumber() {
    MtkLog.d(TAG, "initMusicTrackNumber");
    if (null == mMusicTrackNumber) {
      return;
    }
    int number = mLogicManager.getMusicAudioTrackNumber();
    MtkLog.d(TAG, "initMusicTrackNumber,number==" + number);
    if (number == 0) {
      mMusicTrackNumber.setText("");
      // arcelik customized
      if (mVideoTrackNumberIcon != null) {
        mVideoTrackNumberIcon.setVisibility(View.GONE);
      }
    } else if (number > 0) {
      int index = mLogicManager.getMusicAudioTrackIndex();
      if (index >= number || index < 0) {
        index = 0;
      }
      String type = mLogicManager.getCurrentMusicAudioTrackType(index);
      Log.d(TAG, "initMusicTrackNumber,type==" + type);

      // arcelik customized
      // Drawable musicIcon =
      // mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
      // musicIcon.setBounds(0, 0, musicIcon.getMinimumWidth(),
      // musicIcon.getMinimumHeight());
      // mMusicTrackNumber.setCompoundDrawables(musicIcon, null, null, null);
      if (mVideoTrackNumberIcon != null) {
        mVideoTrackNumberIcon.setVisibility(View.VISIBLE);
      }

      if (number > 1) {
        mMusicTrackNumber.setText(String.format("%s(%d)", type, index + 1));// + "/" + number
      } else {
        mMusicTrackNumber.setText(type);
      }
    }

    initDolbyAndDtsIcon();
  }

  public void initVideoTrackNumber() {
    if (null == mVideoTrackNumber) {
      return;
    }
    int number = mLogicManager.getAudioTranckNumber();
    MtkLog.d(TAG, "initVideoTrackNumber,number==" + number);
    if (number == 0) {
      mVideoTrackNumber.setText("");
      audioTrackIndex = 0;

      // arcelik customized
      if (mVideoTrackNumberIcon != null) {
        mVideoTrackNumberIcon.setVisibility(View.GONE);
      }
    } else if (number > 0) {
      int index = mLogicManager.getAudioTrackIndex();
      if (index >= number || index < 0) {
        index = 0;
        audioTrackIndex = 0;
        mLogicManager.setAudioTranckNumber((short) index);
      }
      String type = mLogicManager.getCurrentAudioTranckType(index);
      // String audioMimeType = mLogicManager.getCurrentAudioTranckMimeType(index);
      // arcelik customized
      // Drawable musicIcon =
      // mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
      // musicIcon.setBounds(0, 0, musicIcon.getMinimumWidth(),
      // musicIcon.getMinimumHeight());
      // mVideoTrackNumber.setCompoundDrawables(musicIcon, null, null, null);
      if (mVideoTrackNumberIcon != null) {
        mVideoTrackNumberIcon.setVisibility(View.VISIBLE);
      }

      if (number > 1) {
        mVideoTrackNumber.setText(String.format("%s(%d)", type, index + 1));// + "/" + number
      } else {
        mVideoTrackNumber.setText(type);
      }
    }

    initDolbyAndDtsIcon();
  }

  public void initDolbyAndDtsIcon() {
    int dolbyType = Util.getDolbyType();
    MtkLog.d(TAG, "initDolbyAndDtsIcon,dolbyType==" + dolbyType);

    if (mAudioTypeIcon == null) {
      MtkLog.d(TAG, "initDolbyAndDtsIcon,mAudioTypeIcon == null");
      return;
    }

    if (dolbyType == 3 || dolbyType == 5) {
      mAudioTypeIcon.setText("");
      Drawable duiIcon;
      if (Util.isSupportDolbyAtmos(mContext)) {
        duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_atmos_icon);
      } else {
        duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_audio_icon);
      }
      duiIcon.setBounds(0, 0, mDubiIconWidth, mDubiIconHeight);
      mAudioTypeIcon.setVisibility(View.VISIBLE);
      mAudioTypeIcon.setCompoundDrawables(duiIcon, null, null, null);
    } else if (dolbyType == 2 || dolbyType == 4) {
      mAudioTypeIcon.setText("");
      Drawable duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_audio_icon);
      duiIcon.setBounds(0, 0, mDubiIconWidth, mDubiIconHeight);
      mAudioTypeIcon.setVisibility(View.VISIBLE);
      mAudioTypeIcon.setCompoundDrawables(duiIcon, null, null, null);
    } else if (dolbyType == 11) {
      // Drawable dtsIcon = mContext.getResources().getDrawable(R.drawable.dts_icon);
      // dtsIcon.setBounds(0, 0, mDtsIconWidth, mDtsIconHeight);
      // mAudioTypeIcon.setCompoundDrawables(dtsIcon, null, null, null);
      mAudioTypeIcon.setCompoundDrawables(null, null, null, null);

      mAudioTypeIcon.setVisibility(View.VISIBLE);
      // mAudioTypeIcon.setCompoundDrawablePadding(5);
      mAudioTypeIcon.setText(R.string.mmp_dts);
    } else if (dolbyType == 12) {
      mAudioTypeIcon.setCompoundDrawables(null, null, null, null);

      mAudioTypeIcon.setVisibility(View.VISIBLE);
      mAudioTypeIcon.setText(R.string.mmp_dts_hd);
    } else if (dolbyType == 13) {
      mAudioTypeIcon.setCompoundDrawables(null, null, null, null);

      mAudioTypeIcon.setVisibility(View.VISIBLE);
      mAudioTypeIcon.setText(R.string.mmp_dts_express);
    } else if (dolbyType == 14) {
      mAudioTypeIcon.setCompoundDrawables(null, null, null, null);

      mAudioTypeIcon.setVisibility(View.VISIBLE);
      mAudioTypeIcon.setText(R.string.mmp_dts_hd_master_audio);
    } else if (dolbyType == 15) {
      mAudioTypeIcon.setCompoundDrawables(null, null, null, null);

      mAudioTypeIcon.setVisibility(View.VISIBLE);
      mAudioTypeIcon.setText(R.string.mmp_dts_x);
    } else {
      mAudioTypeIcon.setVisibility(View.INVISIBLE);
    }

  }

  public void updateDolbyVisionIcon(boolean isDolbyVision) {
    MtkLog.d(TAG, "updateDolbyVisionIcon,isDolbyVision==" + isDolbyVision);
    if (isDolbyVision) {
        Drawable duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_vision_icon);
        duiIcon.setBounds(0, 0, mDubiVisionIconWidth, mDubiVisionIconHeight);
        mVideoTypeIcon.setVisibility(View.VISIBLE);
        mVideoTypeIcon.setCompoundDrawables(duiIcon, null, null, null);
    } else {
        mVideoTypeIcon.setVisibility(View.INVISIBLE);
    };
  }

  public static int sHdrType = 0;

  public void updateHdrShow(int dolbyType) {
    if (mVideoTypeIcon == null) {
      return;
    }
    sHdrType = dolbyType;
    Log.d(TAG, "sHdrType " + dolbyType);
    mVideoTypeIcon.setVisibility(View.VISIBLE);
    switch (dolbyType) {
      case 17:
        mVideoTypeIcon.setCompoundDrawables(null, null, null, null);
        mVideoTypeIcon.setText(R.string.mmp_hdr10);
        break;
      case 18:
        mVideoTypeIcon.setCompoundDrawables(null, null, null, null);
        mVideoTypeIcon.setText(R.string.mmp_hlg);
        break;
      case 19:
        Drawable duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_vision_icon);
        duiIcon.setBounds(0, 0, mDubiVisionIconWidth, mDubiVisionIconHeight);
        mVideoTypeIcon.setCompoundDrawables(duiIcon, null, null, null);
        mVideoTypeIcon.setText("");
        break;
      case 20:
        mVideoTypeIcon.setCompoundDrawables(null, null, null, null);
        mVideoTypeIcon.setText(R.string.mmp_technt);
        break;
      case 21:
        mVideoTypeIcon.setCompoundDrawables(null, null, null, null);
        mVideoTypeIcon.setText(R.string.mmp_hdr10_);
        break;
      case 16:
      default:
        mVideoTypeIcon.setCompoundDrawables(null, null, null, null);
        mVideoTypeIcon.setText("");
        break;
    }
  }

  public boolean changeVideoTrackNumber() {

    if (null == mVideoTrackNumber) {
      return false;
    }

    int tranckNumber = mLogicManager.getAudioTranckNumber();
    int currentTrack = mLogicManager.getAudioTrackIndex();
    String audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
    String audioMimeType = mLogicManager.getCurrentAudioTranckMimeType((short) (currentTrack));
    MtkLog.d(TAG, "--- changeVideoTrackNumber tranckNumber :" + tranckNumber + "currentTrack:"
        + currentTrack + "  audioType:" + audioType);
    // modif by lei
    if (tranckNumber < 2 && !(DolbylogicManager.getInstance().isDolbyDualAudio(audioMimeType))) {
      MtkLog.d(TAG, "--- changeVideoTrackNumber tranckNumber < 2:tranckNumber:" + tranckNumber);
      return false;
    }
    if (currentTrack < 0) {
      currentTrack = audioTrackIndex;
    }
    try {
      // String value = mVideoTrackNumber.getText().toString();
      audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
      audioMimeType = mLogicManager.getCurrentAudioTranckMimeType((short) (currentTrack));
      if (DolbylogicManager.getInstance().isDolbyDualAudio(audioMimeType)) {
        switch (CommonSet.getInstance(mContext).getAudioSpeakerMode()) {
          case AUDDEC_SPK_MODE_LR:
            CommonSet.getInstance(mContext).setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LL);
            audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
            mVideoTrackNumber.setText(String.format("%s(%d)", audioType, currentTrack + 1));
            return true;
          case AUDDEC_SPK_MODE_LL:
            CommonSet.getInstance(mContext).setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_RR);
            audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
            mVideoTrackNumber.setText(String.format("%s(%d)", audioType, currentTrack + 1));
            return true;
          case AUDDEC_SPK_MODE_RR:
            CommonSet.getInstance(mContext).setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LR);
            break;
          default:
            break;
        }

      }
      currentTrack++;
      if (currentTrack >= tranckNumber) {
        currentTrack = 1;
      } else {
        currentTrack++;
      }
      MtkLog.d(TAG, "--- changeVideoTrackNumber --currentTrack:" + currentTrack + "--number:"
          + tranckNumber);
      audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack - 1));
      audioMimeType = mLogicManager.getCurrentAudioTranckMimeType((short) (currentTrack - 1));
      if (mLogicManager.setAudioTranckNumber((short) (currentTrack - 1))) {
        audioTrackIndex = (short) (currentTrack - 1);

        // arcelik customized
        // Drawable musicIcon =
        // mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
        // musicIcon.setBounds(0, 0, musicIcon.getMinimumWidth(),
        // musicIcon.getMinimumHeight());
        // mVideoTrackNumber.setCompoundDrawables(musicIcon, null, null, null);
        if (mVideoTrackNumberIcon != null) {
          mVideoTrackNumberIcon.setVisibility(View.VISIBLE);
        }

        mVideoTrackNumber.setText(String.format("%s(%d)", audioType, currentTrack));
        return true;
      } else {
        MtkLog.d(TAG,
            "--- changeVideoTrackNumber --setAudioTranckNumber return false, set fail!!!!");
      }
    } catch (Exception e) {
      MtkLog.d(TAG, "--- changeVideoTrackNumber --:" + e.getMessage());
    }

    initDolbyAndDtsIcon();

    return false;
  }

  public void setRepeat(int type) {
    int casetype = mLogicManager.getRepeatModel(type);
    MtkLog.i(TAG, "casetype:" + casetype);
    switch (casetype) {
      case Const.REPEAT_ALL:
        setRepeatAll();
        break;
      case Const.REPEAT_NONE:
        setRepeatNone();
        break;
      case Const.REPEAT_ONE:
        setRepeatSingle();
        break;
      default:
        break;
    }

  }

  private void findCommonView() {
    mPlayPauseLayout = (RelativeLayout) vControlView.findViewById(R.id.mmp_pop_playstatus_layout);
    vPStatePlay = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_playstateplay);
    vPStatePause = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_playstatepause);

    vPMediaType = (TextView) vControlView
        .findViewById(R.id.mmp_pop_mediatype);
    vPOrder = (TextView) vControlView.findViewById(R.id.mmp_pop_order_tv);
    vPFileName = (TextView) vControlView
        .findViewById(R.id.mmp_pop_filename_tv);

    mControlbottom = (LinearLayout) vControlView.findViewById(R.id.mmp_control_bottom);
  }

  private void findPhotoView() {
    vPZoomSize = (TextView) vControlView
        .findViewById(R.id.mmp_pop_zoomsize);
    if (true == Feature.isAospCnPlatform()) {
      vPZoomSize.setVisibility(View.GONE);
    }
    // vPRepeat = (LinearLayout) vControlView
    // .findViewById(R.id.mmp_pop_repeat);
    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);
    vPTimeLong = (TextView) vControlView.findViewById(R.id.mmp_pop_time_tv);
    vPView = (TextView) vControlView.findViewById(R.id.mmp_pop_view_tv);

    setRepeat(Const.FILTER_IMAGE);
    setShuffle(Const.FILTER_IMAGE);
    setPhotoDuration();

  }

  private void setPhotoDuration() {
    if (Util.isSupport4K8K()) {

      type = Photo4K2KPlayActivity.mDelayedTime;
      if (type == Photo4K2KPlayActivity.DELAYED_SHORT) {
        vPTimeLong.setText(R.string.mmp_menu_short);
      } else if (type == Photo4K2KPlayActivity.DELAYED_MIDDLE) {
        vPTimeLong.setText(R.string.mmp_menu_medium);
      } else if (type == Photo4K2KPlayActivity.DELAYED_LONG) {
        vPTimeLong.setText(R.string.mmp_menu_long);
      }
    } else {
      type = PhotoPlayActivity.mDelayedTime;
      if (type == PhotoPlayActivity.DELAYED_SHORT) {
        vPTimeLong.setText(R.string.mmp_menu_short);
      } else if (type == PhotoPlayActivity.DELAYED_MIDDLE) {
        vPTimeLong.setText(R.string.mmp_menu_medium);
      } else if (type == PhotoPlayActivity.DELAYED_LONG) {
        vPTimeLong.setText(R.string.mmp_menu_long);
      }
    }
  }

  private void setShuffle(int type) {
    boolean isShuffle = mLogicManager.getShuffleMode(type);
    if (isShuffle) {
      setShuffleVisble(View.VISIBLE);
    } else {
      setShuffleVisble(View.GONE);
      // arcelik customized
      /*
       * setShuffleVisble(View.INVISIBLE);
       */ }
  }

  private void findMusicView() {
    vVideoSpeed = (TextView) vControlView
        .findViewById(R.id.mmp_video_repeata);
    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    // vPRepeat = (LinearLayout) vControlView
    // .findViewById(R.id.mmp_pop_repeat);
    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);
    vMStartTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_starttime);
    vMEndTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_endtime);
    vMProgressBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_music_progress);
    vMVolumeTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_volume);
    vMVolumeBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_musicvolume_progress);
    mVolumeProgressBg = (LinearLayout) vControlView
        .findViewById(R.id.mmp_volume_progress_bg);
    mAudioTypeIcon = (TextView) vControlView
        .findViewById(R.id.mmp_pop_audio_type_icon);
    mMusicTrackNumber = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_order);

    // arcelik customized
    mVideoTrackNumberIcon = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_video_order_icon);
    vVideoSpeedIcon = (ImageView) vControlView
        .findViewById(R.id.mmp_video_repeata_icon);

    setRepeat(Const.FILTER_AUDIO);
    setShuffle(Const.FILTER_AUDIO);
    // mute
  }

  /**
   * if true show video display current playback time and duration in control
   * panel
   * fix DTV00367339
   * 
   * @param flag
   */
  // public void setVisibility(boolean flag)
  // {
  // if(flag)
  // {
  // setStartTimeVisible(View.VISIBLE);
  // setEndTimeVisible(View.VISIBLE);
  // }else
  // {
  // setStartTimeVisible(View.INVISIBLE);
  // setEndTimeVisible(View.INVISIBLE);
  // }
  // }
  public void setTimeViewVisibility(boolean flag) {
    if (flag) {
      setStartTimeVisible(View.VISIBLE);
      setEndTimeVisible(View.VISIBLE);
    } else {
      setStartTimeVisible(View.INVISIBLE);
      setEndTimeVisible(View.INVISIBLE);
    }
  }

  private void setStartTimeVisible(int visible) {
    Log.i(TAG, "setStartTimeVisible visible:" + visible);
    // new Exception("setStartTimeVisible").printStackTrace();
    if (vMStartTime != null) {
      vMStartTime.setVisibility(visible);
    }
  }

  private void setEndTimeVisible(int visible) {
    Log.i(TAG, "setEndTimeVisible visible:" + visible);
    // new Exception("setEndTimeVisible").printStackTrace();
    if (vMEndTime != null) {
      vMEndTime.setVisibility(visible);
    }
  }

  /**
   * fix reStart play 2D,3D photo , music and video repeatOne reserves problem
   */
  public void setRepeatVisibility(int type) {
    setRepeat(mediaType);
  }

  /**
   * if true, display start time and end time, else dont.
   * 
   * @return
   */
  public boolean isTimeViewVisiable() {
    return vMStartTime.getVisibility() == View.VISIBLE;
  }

  private void findVideoView() {

    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);

    mRepeatLogo = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_repeat_logo);

    vVideoSpeed = (TextView) vControlView
        .findViewById(R.id.mmp_video_repeata);

    vPZoomSize = (TextView) vControlView
        .findViewById(R.id.mmp_pop_zoomsize);
    if (Util.isUseExoPlayer()) {
      // exo not support zoom function
      vPZoomSize.setVisibility(View.GONE);
    }

    vMStartTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_starttime);

    vMEndTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_endtime);

    vMProgressBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_music_progress);

    vMVolumeTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_volume);

    vMVolumeBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_musicvolume_progress);

    mVideoTrackNumber = (TextView) vControlView
        .findViewById(R.id.mmp_pop_video_order);

    mAudioTypeIcon = (TextView) vControlView
        .findViewById(R.id.mmp_pop_audio_type_icon);

    mVideoTypeIcon = (TextView) vControlView
        .findViewById(R.id.mmp_pop_video_type_icon);

    mVolumeProgressBg = (LinearLayout) vControlView
        .findViewById(R.id.mmp_volume_progress_bg);

    mVideoSubtitle = (TextView) vControlView
        .findViewById(R.id.mmp_pop_subtitle_number);
    mVideoSubtitleIcon = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_subtitle_icon);
    mVideoLayout = (RelativeLayout) vControlView.findViewById(R.id.mmp_video_rl);

    // arcelik customized
    mVideoTrackNumberIcon = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_video_order_icon);
    vVideoSpeedIcon = (ImageView) vControlView
        .findViewById(R.id.mmp_video_repeata_icon);

    setRepeat(Const.FILTER_VIDEO);
  }

  // Added by Dan for fix bug DTV00380300
  public void setZoomEmpty() {
    vPZoomSize.setText("");
  }

  public void setPhotoZoomSize() {
    int size = mLogicManager.getCurrentZoomSize();
    if (size == 0) {
      size = 1;
    } else {
      size = 2 << (size - 1);
    }
    vPZoomSize.setText(String.format("%dX", size));
  }

  public void setZoomSize() {

    // if(mLogicManager.getVideoWidth() <= 0
    // ||mLogicManager.getVideoHeight() <= 0){
    //
    // vPZoomSize.setText("");
    //
    // }else{

    int size = mLogicManager.getCurZomm();
    if (size == 0) {
      size = 1;
    } else {
      size = 2 << (size - 1);
    }
    vPZoomSize.setText(String.format("%dX", size));
    // }
  }

  private void findTextView() {

    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);

    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);

    vPTimeLong = (TextView) vControlView.findViewById(R.id.mmp_pop_time_tv);

    setRepeat(Const.FILTER_TEXT);
    setShuffle(Const.FILTER_TEXT);
  }

  public void setMediaPlayState() {
    if (isPlaying()) {
      pause();
    } else {
      play();
    }

    setPlaybackState();

    if (null != mRepeatLogo && (mRepeatLogo.getVisibility() != View.VISIBLE)) {
      setRepeatLogoVisible(View.VISIBLE);
    }

    if (null != vVideoSpeed) {
      Log.i(TAG, "vVideoSpeed!=NULL VISIBLE:" + vVideoSpeed.getVisibility() + "---shown:"
          + vVideoSpeed.isShown());
      if (vVideoSpeed.getVisibility() == View.VISIBLE) {
        setVideoSpeedVisible(View.INVISIBLE);
      }
    }
  }

  public void stopKeyPause() {
    // mLogicManager.pauseVideoWhenStopKey();
    if (null != mRepeatLogo && (mRepeatLogo.getVisibility() != View.VISIBLE)) {
      setRepeatLogoVisible(View.VISIBLE);
    }

    if (null != vVideoSpeed) {
      Log.i(TAG, "vVideoSpeed!=NULL VISIBLE:" + vVideoSpeed.getVisibility() + "---shown:"
          + vVideoSpeed.isShown());
      if (vVideoSpeed.getVisibility() == View.VISIBLE) {
        setVideoSpeedVisible(View.INVISIBLE);
      }
    }
  }

  public void setVideoSpeedVisible(int visible) {
    MtkLog.i(TAG, "setVideoSpeedVisible visible:" + visible);
    // new Exception("setVideoSpeedVisible").printStackTrace();
    if (vVideoSpeed != null) {
      /*
       * vVideoSpeed.setVisibility(visible);
       */ // arcelik customized
      if (visible == View.VISIBLE) {
        vVideoSpeed.setVisibility(visible);
        if (vVideoSpeedIcon != null) {
          vVideoSpeedIcon.setVisibility(visible);
        }
      } else {
        vVideoSpeed.setVisibility(View.GONE);
        if (vVideoSpeedIcon != null) {
          vVideoSpeedIcon.setVisibility(View.GONE);
        }
      }
    }
  }

  public void setPlayIcon() {
    setPauseVisiblity(View.GONE);
    setPlayVisibilty(View.VISIBLE);
    mIsPlaying = true;
  }

  public void play() {
    // Moved to bottom by keke 1.18
    try {
      mControlImp.play();
      setPauseVisiblity(View.GONE);
      setPlayVisibilty(View.VISIBLE);
      // Added by Dan for fix bug DTV00376577
      mIsPlaying = true;
      setPlaybackState();
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
    }
  }

  public void pause() {
    try {
      mControlImp.pause();
      if (isPause() || (mediaType != MultiMediaConstant.VIDEO && mediaType != MultiMediaConstant.AUDIO)) {
        setPauseVisiblity(View.VISIBLE);
        setPlayVisibilty(View.GONE);
        // end
        // Added by Dan for fix bug DTV00376577
        mIsPlaying = false;
        setPlaybackState();
      }
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
    }
  }

  private void setPlayVisibilty(int visible) {
    Log.i(TAG, "setPlayVisibilty vPStatePlay:" + vPStatePlay + "--visible:" + visible);
    // new Exception().printStackTrace();
    if (vPStatePlay != null) {
      vPStatePlay.setVisibility(visible);
    }
    if (visible == View.VISIBLE) {
      setVideoSpeedVisible(View.GONE);
    }
  }

  private void setPauseVisiblity(int visible) {
    Log.i(TAG, "setPauseVisiblity vPStatePause:" + vPStatePause + "--visible:" + visible);
    // new Exception().printStackTrace();
    if (vPStatePause != null) {
      vPStatePause.setVisibility(visible);
    }
    if (visible == View.VISIBLE) {
      setVideoSpeedVisible(View.GONE);
    }
  }

  public void onCapture() {
    try {
      mControlImp.pause();
      mIsPlaying = false;
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
    }
  }

  // add by shuming for fix CR: DTV00407914
  public void setPauseIcon(int visibility) {
    setPauseVisiblity(visibility);
    setPlayVisibilty(View.GONE);
  }

  // add by shuming for fix CR: DTV00407914
  public void setPlayIcon(int visibility) {
    setPlayVisibilty(visibility);
    setPauseVisiblity(View.GONE);
  }

  public void setPauseIconGone() {
    setPauseVisiblity(View.GONE);
    setPlayVisibilty(View.GONE);
  }

  public void reSetVideo() {
    setPauseVisiblity(View.GONE); // arcelik customized
    vPStatePause.setBackgroundResource(R.drawable.toolbar_top_pause);
    setPlayVisibilty(View.VISIBLE);
    mIsPlaying = true;
    setVideoSpeedVisible(View.INVISIBLE);
  }

  public short getSubtitleIndex() {
    return subtitleIndex;
  }

  public void setVideoSubtitle(short number, short index) {
    MtkLog.d(TAG, "setVideoSubtitle number:" + number + "  index:" + index);
    if (number <= 0) {
      setVideoSubtitleVisible(View.INVISIBLE);
    } else {
      setVideoSubtitleVisible(View.VISIBLE);
      if (index < 0) {
        mLogicManager.setSubOnOff(false);
        mVideoSubtitle.setText(R.string.mmp_menu_off);
      } else {
        mLogicManager.setSubtitleTrack(index);
        mVideoSubtitle.setText(String.format("%d/%s", index + 1, number));
      }
      subtitleIndex = index;
    }
  }

  public void setVideoSubtitleVisible(int visible) {
    if (mVideoSubtitle != null) {
      mVideoSubtitle.setVisibility(visible);
    }
    if (mVideoSubtitleIcon != null) {
      mVideoSubtitleIcon.setVisibility(visible);
    }

  }

  /**
   * Initialize subtitle,set subtitle off
   *
   * @param number
   */
  public void initSubtitle(short trackNum) {
    MtkLog.i(TAG, "initSubtitle: trackNum:" + trackNum);
    // mLogicManager.setSubtitleTrack((short)255);
    short number = (mLogicManager.getSubtitleTrackNumber());
    if (number <= 0) {
      mLogicManager.setSubtitleTrack((short) 255);
      setVideoSubtitleVisible(View.INVISIBLE);
    } else {
      short index = (short) (mLogicManager.getSubtitleIndex());
      MtkLog.d(TAG, "initSubtitle index :" + index);
      setVideoSubtitleVisible(View.VISIBLE);
      // modified by keke for DTV00384824
      int lastMemoryValue = SaveValue.getInstance(mContext.getApplicationContext())
          .readValue(LastMemory.LASTMEMORY_ID);
      if (lastMemoryValue == LastMemory.LASTMEMORY_OFF) {
        mLogicManager.setSubtitleTrack((short) 255);
        mVideoSubtitle.setText(R.string.mmp_menu_off);
        subtitleIndex = -1;
      } else if (lastMemoryValue == LastMemory.LASTMEMORY_POSITION
          || lastMemoryValue == LastMemory.LASTMEMORY_TIME) {
        if (index < 0) {
          mLogicManager.setSubtitleTrack((short) 255);
          mVideoSubtitle.setText(R.string.mmp_menu_off);
          subtitleIndex = -1;
        } else {
          mLogicManager.setSubtitleTrack(index);
          mVideoSubtitle.setText(String.format("%d/%s", index + 1, number));
          subtitleIndex = index;
        }
      }
    }
  }

  public void initSubtitle() {
    MtkLog.i(TAG, "initSubtitle");
    short number = (mLogicManager.getSubtitleTrackNumber());
    if (number <= 0) {
      mLogicManager.setSubtitleTrack((short) 255);
      setVideoSubtitleVisible(View.INVISIBLE);
    } else {
      short index = (short) (mLogicManager.getSubtitleIndex());
      MtkLog.d(TAG, "initSubtitle index :" + index);
      setVideoSubtitleVisible(View.VISIBLE);
      if (index < 0) {
        mLogicManager.setSubtitleTrack((short) 255);
        mVideoSubtitle.setText(R.string.mmp_menu_off);
        subtitleIndex = -1;
      } else {
        mLogicManager.setSubtitleTrack(index);
        mVideoSubtitle.setText(String.format("%d/%s", index + 1, number));
        subtitleIndex = index;
      }
    }
  }

  /**
   * Initialize subtitle,set subtitle off
   *
   * @param number
   */
  public void reinitSubtitle(short number) {
    int index = mLogicManager.getSubtitleIndex();
    MtkLog.i(TAG, "reinitSubtitle:" + index + " number:" + number);
    int videoSource = MultiFilesManager.getInstance(mContext)
        .getCurrentSourceType();
    if (videoSource != MultiFilesManager.SOURCE_LOCAL) {
      index = subtitleIndex;
    }
    if (index >= 0 && index < number) {
      setVideoSubtitle(number, (short) index);
      subtitleIndex = (short) index;
    } else {
      initSubtitle(number);
    }
  }

  public void reSetAudio() {
    // setPauseVisiblity(View.INVISIBLE);
    setPauseVisiblity(View.GONE); // arcelik customized

    vPStatePause.setBackgroundResource(R.drawable.mmp_top_pause);
    setPlayVisibilty(View.VISIBLE);
    mIsPlaying = true;
    if (null != vVideoSpeed) {
      setVideoSpeedVisible(View.INVISIBLE);
    }
  }

  public void stop() {
    if (null != vVideoSpeed && (vVideoSpeed.getVisibility() == View.VISIBLE)) {
      setVideoSpeedVisible(View.INVISIBLE);
    }
    setPauseVisiblity(View.VISIBLE);
    vPStatePause.setBackgroundResource(R.drawable.toolbar_top_stop);
    setPlayVisibilty(View.GONE);
    // Added by Dan for fix bug DTV00376577
    mIsPlaying = false;
  }

  public void onFast(int speed, int status, int type) {
    MtkLog.d(TAG, "onFast speed:" + speed + "  status:" + status + "  type:" + type);
    /*
     * setPlayVisibilty(View.INVISIBLE);
     */ setPlayVisibilty(View.GONE); // arcelik customized
    mIsPlaying = false;
    /*
     * setPauseVisiblity(View.INVISIBLE);
     */
    setPauseVisiblity(View.GONE); // arcelik customized

    setVideoSpeedVisible(View.VISIBLE);

    if (speed == 1) {
      setPlayVisibilty(View.VISIBLE);
      mIsPlaying = true;
      setVideoSpeedVisible(View.INVISIBLE);
      return;
    }

    vVideoSpeed.setText(String.format("%dx", speed));
    Drawable imgleft = null;
    if (type == Const.FILTER_VIDEO) {
      if (null != mRepeatLogo && (mRepeatLogo.getVisibility() == View.VISIBLE)) {
        setRepeatLogoVisible(View.INVISIBLE);
      }
      // vVideoSpeed.setTextColor(Color.WHITE); //arcelik customized - no need
    }

    if (status == 0) {
      if (type == Const.FILTER_VIDEO) {
        imgleft = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_ff_video);
      } else {
        imgleft = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_ff);
      }
    } else if (status == 1) {
      if (type == Const.FILTER_VIDEO) {
        imgleft = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_rew_video);
      } else {
        imgleft = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_rew);

      }
    } else if (status == 2) {
      imgleft = mContext.getResources().getDrawable(
          R.drawable.toolbar_typeicon_ff);

      vVideoSpeed.setText(String.format("1/%dx", speed));

    } else if (status == 3) {

      imgleft = mContext.getResources().getDrawable(
          R.drawable.toolbar_typeicon_rew);

      vVideoSpeed.setText(String.format("1/%dx", speed));
    }

    if (imgleft != null) {
      imgleft.setBounds(0, 0, imgleft.getMinimumWidth(), imgleft
          .getMinimumHeight());
    }

    // arcelik customized
    if (vVideoSpeedIcon != null) {
      vVideoSpeedIcon.setImageDrawable(imgleft);
    } else {
      vVideoSpeed.setCompoundDrawables(imgleft, null, null, null);
    }
  }

  // public void setSlow() {

  // }

  public boolean isPlaying() {
    switch (mediaType) {
      case MultiMediaConstant.VIDEO:
        return mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STARTED;
      case MultiMediaConstant.AUDIO:
        return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STARTED;
      default:
        break;
    }
    return mIsPlaying;
  }

  public boolean isPause() {
    switch (mediaType) {
      case MultiMediaConstant.VIDEO:
        return mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED;
      case MultiMediaConstant.AUDIO:
        return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_PAUSED;
      default:
        break;
    }
    return !mIsPlaying;
  }

  public void setFileName(String name) {
    // if(MultiFilesManager.isSourceDLNA(mContext)){
    // int index = name.lastIndexOf(".");
    // if(index > 0){
    // name = name.substring(0,index);
    // }
    // }
    vPFileName.setText(name);
  }

  public void setFilePosition(String pagesize) {
    vPOrder.setText(pagesize);
  }

  public void setRepeatAll() {
    setRepeatModeVisible(View.VISIBLE);
    vPRepeatTv.setCompoundDrawables(repeatAll, null, null, null);
    vPRepeatTv.setText(mContext.getString(R.string.mmp_menu_repeatall));
  }

  private void setRepeatModeVisible(int visible) {
    if (vPRepeatTv != null) {
      vPRepeatTv.setVisibility(visible);
    }
  }

  public void setRepeatSingle() {
    setRepeatModeVisible(View.VISIBLE);
    vPRepeatTv.setCompoundDrawables(repeatOne, null, null, null);
    vPRepeatTv.setText(mContext.getString(R.string.mmp_pop_repeat_tvsingle));
  }

  public void setRepeatNone() {
    setRepeatModeVisible(View.INVISIBLE);
    // add by yongzhengwei for fix bug DTV00379498
    vPRepeatTv.setCompoundDrawables(null, null, null, null);
    vPRepeatTv.setText(mContext.getString(R.string.mmp_pop_repeat_tvnone));
  }

  public void setShuffleVisble(int visibility) {
    if (vPShuffle != null) {
      vPShuffle.setVisibility(visibility);
    }

  }

  public void setPhotoZoom(String scale) {
    vPZoomSize.setText(scale);
  }

  public void setPhotoTimeType(String type) {
    vPTimeLong.setText(type);
  }

  public void setPhotoAnimationEffect(String animation) {
    vPView.setText(animation);
  }

  public void setPhotoAnimationEffect(int type) {
    String value = "";
    switch (type) {
      case ConstPhoto.DEFAULT:
        value = mContext.getString(R.string.mmp_menu_none);
        break;
      case ConstPhoto.dissolve:
        value = mContext.getString(R.string.mmp_menu_dissolve);
        break;

      case ConstPhoto.wipe_right:
        value = mContext.getString(R.string.mmp_menu_wiperight);
        break;
      case ConstPhoto.wipe_left:
        value = mContext.getString(R.string.mmp_menu_wipeleft);
        break;
      case ConstPhoto.wipe_top:
        value = mContext.getString(R.string.mmp_menu_wipeup);
        break;
      case ConstPhoto.wipe_bottom:
        value = mContext.getString(R.string.mmp_menu_wipedown);
        break;
      case ConstPhoto.box_in:
        value = mContext.getString(R.string.mmp_menu_boxin);
        break;
      case ConstPhoto.box_out:
        value = mContext.getString(R.string.mmp_menu_boxout);
        break;
      case ConstPhoto.RADNOM:
        value = mContext.getString(R.string.mmp_menu_random);
        break;
      default:
        break;
    }
    vPView.setText(value);
  }

  public void setCurrentTime(long mills) {
    // mills = mills+500;
    mills /= 1000;
    long minute = mills / 60;
    long hour = minute / 60;
    long second = mills % 60;
    minute %= 60;
    vMStartTime.setText(String.format("%02d:%02d:%02d", hour, minute,
        second));
    Log.i(TAG, "setCurrentTime starttime:" + vMStartTime.getText());
  }

  public void setEndtime(int mills) {
    setProgressMax(mills);
    mills /= 1000;
    int minute = mills / 60;
    int hour = minute / 60;
    int second = mills % 60;
    minute %= 60;
    // String text = "";
    try {
      vMEndTime.setText(String.format("%02d:%02d:%02d", hour, minute, second));
      // text = String.format("%02d:%02d:%02d", hour, minute, second);
    } catch (Exception e) {
      vMEndTime.setText("");
    }

  }

  public void setProgressMax(int max) {
    MtkLog.d(TAG, "setProgressMax max:" + max);
    // MtkLog.d(TAG, "setProgressMax max:" + max + " " + Log.getStackTraceString(new
    // Throwable()));
    vMProgressBar.setMax(max);
  }

  public int getProgressMax() {
    return vMProgressBar.getMax();
  }

  public int getCurrentProgress() {
    return vMProgressBar.getProgress();
  }

  public void setProgress(int progress) {
    MtkLog.d(TAG, "setProgress progress:" + progress);
    // MtkLog.d(TAG, "setProgress progress:" + progress
    // + " " + Log.getStackTraceString(new Throwable()));
    vMProgressBar.setProgress(progress);
  }

  public void setVolumeMax(int max) {
    vMVolumeBar.setMax(max);
  }

  public void setCurrentVolume(int volume) {
    // new Exception("setCurrentVolume").printStackTrace();
    Log.i(TAG, "setCurrentVolume: volume:" + volume);
    if (null == vMVolumeBar) {
      return;
    }

    vMVolumeBar.setProgress(volume);

  }

  private void setVolumeProgressBgVisible(int visible) {
    if (mVolumeProgressBg != null) {
      mVolumeProgressBg.setVisibility(visible);
    }
  }

  public void setMute(boolean isMute) {
    if (null == vMVolumeBar) {
      return;
    }
    Log.i(TAG, "setMute isMute:" + isMute);
    if (!isMute) {
      setVolumeMax(mLogicManager.getMaxVolume());
      setCurrentVolume(mLogicManager.getVolume());
    }

    if (null != mVolumeProgressBg) {
      if (isMute) {
        Log.i(TAG, "mVolumeProgressBg INVISIBLE isMute:" + isMute);
        setVolumeProgressBgVisible(View.INVISIBLE);
      } else {
        Log.i(TAG, "mVolumeProgressBg VISIBLE isMute:" + isMute);
        setVolumeProgressBgVisible(View.VISIBLE);
      }

    }

    if (null != vMVolumeTv) {
      if (isMute) {
        Log.i(TAG, "vMVolumeTv isMute true:" + isMute);
        Drawable imgleft = mContext.getResources().getDrawable(
            R.drawable.mmp_toolbar_icon_mute);
        imgleft.setBounds(0, 0, imgleft.getMinimumWidth(), imgleft
            .getMinimumHeight());
        vMVolumeTv.setCompoundDrawables(imgleft, null, null, null);
        vMVolumeTv.setTag(Boolean.TRUE);
      } else {
        Log.i(TAG, "vMVolumeTv isMute false :" + isMute);
        Drawable imgleft = mContext.getResources().getDrawable(
            R.drawable.mmp_toolbar_icon_volume);
        imgleft.setBounds(0, 0, imgleft.getMinimumWidth(), imgleft
            .getMinimumHeight());
        vMVolumeTv.setCompoundDrawables(imgleft, null, null, null);
        vMVolumeTv.setTag(Boolean.FALSE);
      }
    }

  }

  public void reSetPause() {
    vPStatePause.setBackgroundResource(R.drawable.toolbar_top_pause);
  }

  // Added by keke 1202 for fix bug DTV00379478
  public void setInforbarNull() {
    setStartTimeVisible(View.INVISIBLE);
    vMStartTime.setText("");
    Log.i(TAG, "vMStartTime.setTex NULL");
    setEndTimeVisible(View.INVISIBLE);
    vMEndTime.setText("");
    vMProgressBar.setProgress(0);
    vPZoomSize.setText("");
    mVideoTrackNumber.setText("");
    setVideoSubtitleVisible(View.INVISIBLE);
    vPOrder.setText("");
    vPFileName.setText("");
    setVideoSpeedVisible(View.INVISIBLE);
    mAudioTypeIcon.setVisibility(View.INVISIBLE);
    mVideoTypeIcon.setVisibility(View.INVISIBLE);

    // arcelik customized
    if (mVideoTrackNumberIcon != null) {
      mVideoTrackNumberIcon.setVisibility(View.GONE);
    }
  }

  // Add by keke 1215 for fix cr DTV00383194
  private void setInforbarTransparent() {
    try {
      // LinearLayout m = (LinearLayout)
      // vControlView.findViewById(R.id.mmp_control_bottom);
      // m.getBackground().setAlpha(220);
      // LinearLayout m2 = (LinearLayout)
      // vControlView.findViewById(R.id.mmp_popwindow_Operator_Message);
      // m2.getBackground().setAlpha(220);
      // LinearLayout m = (LinearLayout)
      // vControlView.findViewById(R.id.mmp_popwindow);
      // m.getBackground().setAlpha(220);
      // LinearLayout m2 = (LinearLayout)
      // vControlView.findViewById(R.id.mmp_popwindow_Operator_Message);
      // m2.getBackground().setAlpha(220);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setVMProgressBarVisible(int visible) {
    if (null != vMProgressBar) {
      vMProgressBar.setVisibility(visible);
    }
  }

  public void hideProgress() {
    setVMProgressBarVisible(View.INVISIBLE);
    setStartTimeVisible(View.INVISIBLE);
    setEndTimeVisible(View.INVISIBLE);
  }

  public void showProgress() {
    setStartTimeVisible(View.VISIBLE);
    setEndTimeVisible(View.VISIBLE);
    setVMProgressBarVisible(View.VISIBLE);
  }

  public void showPausePlayIcon(boolean isPlaying) {

    if (isPlaying == true) {
      setPlayVisibilty(View.VISIBLE);
    }

    setPauseVisiblity(View.GONE);

    mIsPlaying = isPlaying;
  }

  public void resetSpeepView() {

    if (vVideoSpeed != null) {
      // if (mediaType == MultiMediaConstant.VIDEO
      // && 1 < mLogicManager.getVideoSpeed()) {
      // setVideoSpeedVisible(View.INVISIBLE);
      // } else {
      setVideoSpeedVisible(View.INVISIBLE);
      // }
    }

  }

  public void hideOrder() {
    vPOrder.setVisibility(View.INVISIBLE);
  }

  private MediaSession mMediaSession;
  private PlaybackState.Builder mPb;

  public void setMediaSession(MediaSession mediaSession, PlaybackState.Builder pb) {
    mMediaSession = mediaSession;
    mPb = pb;
  }

  public void setPlaybackState() {
    MtkLog.d(TAG, "MediaSession setPlaybackState");
    if (mediaType == MultiMediaConstant.VIDEO || mediaType == MultiMediaConstant.AUDIO) {
      if (mMediaSession == null || mPb == null) {
        Log.d(TAG, "mMediaSession == null || mPb == null");
        return;
      }

      if (isPlaying()) {
        MtkLog.d(TAG, "Received PlaybackState.STATE_PLAYING");
        mPb.setState(PlaybackState.STATE_PLAYING, 0, 1);
      } else {
        MtkLog.d(TAG, "Received PlaybackState.STATE_PAUSED");
        mPb.setState(PlaybackState.STATE_PAUSED, 0, 1);
      }
      mMediaSession.setPlaybackState(mPb.build());
    }
  }
}
