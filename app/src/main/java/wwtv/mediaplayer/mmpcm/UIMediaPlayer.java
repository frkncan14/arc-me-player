
package com.mediatek.wwtv.mediaplayer.mmpcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaPlayer.TrackInfo;
import android.media.MediaFormat;
import android.media.PlaybackParams;
//import android.media.Metadata;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.mediatek.ExoMediaPlayer;

import com.mediatek.mmp.PcmMediaInfo;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;
import com.mediatek.wwtv.mediaplayer.mmp.util.DolbylogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MetaDataInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.MtkTrackInfo;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mediatek.mmp.Utils.DataSource;
import com.mediatek.mmp.Utils.DataSourceType;

public class UIMediaPlayer implements DataSource {

  public static final int MODE_LOCAL = FileConst.SRC_USB;
  public static final int MODE_SAMBA = FileConst.SRC_SMB;
  public static final int MODE_DLNA = FileConst.SRC_DLNA;
  public static final int MODE_HTTP = FileConst.SRC_HTTP;
  public static final int IMTK_PB_ERROR_CODE_NEW_TRICK = -400; // /< new trick flow, has no index
                                                               // table, only support speed less
                                                               // then 8x, ap should reset to normal
                                                               // play(1x)
  public static final int IMTK_PB_BUFFER_NOT_ENOUGH = -101; // /< new trick flow, buffer not enough
  private static final String TAG = "UIMediaPlayer";
  private ExoMediaPlayer mtkPlayer; // Just DLNA case
  private ExoMediaPlayer mPlayer; // Android flow
  private final int sourceType;
  private String mPath;

  private InputStream mInputStream;
  private boolean mIsSeamlessModeOn = false;

  //Must align with
  public static final int PLAYER_ID_PV_PLAYER = 1;
  public static final int PLAYER_ID_SONIVOX_PLAYER = 2;
  public static final int PLAYER_ID_STAGEFRIGHT_PLAYER = 3;
  public static final int PLAYER_ID_NU_PLAYER = 4;
  public static final int PLAYER_ID_TEST_PLAYER = 5;
  public static final int PLAYER_ID_CMPB_PLAYER = 6;
  public static final int PLAYER_ID_MTK_STREAM_PLAYER = 7;
  public static final int PLAYER_ID_EXO_PLAYER = 8;

  public UIMediaPlayer(int sourceType) {
    this.sourceType = sourceType;
    Log.i(TAG, "sourceType:" + sourceType);
    mPlayer = new ExoMediaPlayer();
    Log.i(TAG, "sourceType after:" + sourceType);

  }

  public ExoMediaPlayer getMtkPlayer() {
    return mtkPlayer;
  }

  public ExoMediaPlayer getPlayer() {
    return mPlayer;
  }

  public int getAllSubtitleTrackInfo() {
    int textTrackNum = mPlayer.getAllSubtitleTrackCount();
    Log.d(TAG, "getAllSubtitleTrackInfo~~~ subTrackNum = " + textTrackNum);
    return textTrackNum;
  }

  public int getAudioTrackIndex() {
    int index = mPlayer.getCurrentAudioIndex();
    Log.i(TAG, "getAudioTrackIndex sourceType:" + sourceType + "  index:" + index);
    return index;
  }

  public boolean setAudioTrack(int track) {
    Log.d(TAG, "setAudioTrack  ~ sourceType =" + sourceType + "  track:" + track);
    return mPlayer.setAudioTrack(track);
  }

  public boolean step(int amount) {
    boolean stepSuccess = mPlayer.step(amount);
    Log.d(TAG, "step stepSuccess =" + stepSuccess);
    return stepSuccess;
  }

  public int updateRotation(final int degree) {
    return mPlayer.updateRotation(degree);
  }

  public boolean setVendCmd(final int cmd, final int arg1, final int arg2, final Object obj) {
    final Bundle para = new Bundle();
    switch (cmd) {
     case Const.VND_CMD_SCREEN_MODE:
        para.putInt("vendor.vdecomx.screenmode", arg1);
        break;
      default:
       return false;
    }
    return mPlayer.setVendCmd(para);
  }

  public void setSubtitleTrack(int track) {
    Log.d(TAG, "setSubtitleTrack track =" + track);
        MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
        int textTrackNum = 0;
        int trackNum = 0;
        if (tracks == null || tracks.length <= 0)
        {
          return;
        }
        for (MtkTrackInfo info : tracks)
        {
          if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT)
          {
            if (textTrackNum == track)
            {
              Log.d(TAG, "EXOPLAYER setSubtitleTrack trackNum =" + trackNum + "textTrackNum ="
                  + textTrackNum);
              mPlayer.selectTrack(trackNum);
              return;
            }
            else
            {
              textTrackNum++;
            }
          }
          trackNum++;
        }
  }

  public boolean onSubtitleTrack() {
    Log.d(TAG, "onSubtitleTrack ~");
    return mPlayer.onSubtitleTrack();
  }

  public boolean offSubtitleTrack() {
    Log.d(TAG, "offSubtitleTrack ~sourceType = " + sourceType + "mPlayer = " + mPlayer);
    return mPlayer.offSubtitleTrack();
  }

  public boolean canDoSeek() {
    boolean doSeek = mPlayer.canDoSeek();
    Log.d(TAG, "canDoSeek ~ doSeek = " + doSeek);
    return doSeek;
  }

  public boolean canDoTrick(int speed) {
    Log.d(TAG, "canDoTrick ~ speed = " + speed);
    boolean doTrick = mPlayer.canDoTrick(speed);
    Log.d(TAG, "canDoTrick ~ doTrick = " + doTrick);
    return doTrick;
  }

  public byte[] getEmbeddedPicture() {
    Log.d(TAG, "getEmbeddedPicture ~ local not handle");
    return new byte[0];
  }

  public int getCurrentPosition() {
    int position= mPlayer.getCurrentPosition();
    Log.d(TAG, "getCurrentPosition position:" + position);
    return position;
  }

  public int getCurrentBytePosition() {
    int position = 0;
//    position = mPlayer.getCurrentBytePosition();
    Log.d(TAG, "getCurrentBytePosition ~position = " + position);
    return position;
  }

  public int getDuration() {
    int dur = mPlayer.getDuration();
    Log.d(TAG, "getDuration ~dur = " + dur);
    return dur;
  }

  public void setPcmMediaInfo(PcmMediaInfo pcmMediaInfo) {
    Log.d(TAG, "setPcmMediaInfo ~ local not handle");
    Log.d(TAG, "setPcmMediaInfo ~ isUseExoPlayer");

  }

  private MetaDataInfo getMetaDataInfoByRetriever() {
    Log.d(TAG, "getMetaDataInfoByRetriever ~mPath:" + mPath);

    MetaDataInfo mMetaInfo = null;
    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    try {
      String keyRetrieverPlayer = "X-tv-Retriever-Player";
      String valueRetrieverPlayer = "CMPB_PLAYER";
      Map<String, String> headersT = new HashMap<String, String>();
      headersT.put(keyRetrieverPlayer, valueRetrieverPlayer);
      retriever.setDataSource(mPath);
    } catch (Exception e) {
      retriever.release();
      Log.d(TAG, "setdataSource fail ~");
      return null;
    }

    String mtitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);


    String mdirector = null;
    String mcopyright = null;

    String myear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
    String mgenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

    String martist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

    String malbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

    String mbitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);

    String mdur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    Log.e(TAG, "mbitrate:" + mbitrate + "  mgenre:" + mgenre + "_mdirector:"
        + mdirector + "_mcopyright:" + mcopyright + "  mdur:" + mdur);
    int dur = 0;
    try {
      dur = Integer.valueOf(mdur);

    } catch (Exception ex) {
      Log.d(TAG, "duration to int error~~");
    }

    int mbitratet = 200;
    try {
      mbitratet = Integer.valueOf(mbitrate);

    } catch (Exception ex) {
      Log.d(TAG, "mbitrate to int error~~");
    }

    retriever.release();

    Log.e(TAG, " getMetaDataInfoByRetriever  myear:" + myear + "_mtitle:" + mtitle + "_martist:"
        + martist + "_malbum:" + malbum + "_mgenre:" + mgenre);

    mMetaInfo = new MetaDataInfo(-1, mbitratet, dur,
        -1, myear, mtitle, malbum, martist,
        -1, mgenre, mdirector, mcopyright);

    return mMetaInfo;

  }

  public MetaDataInfo getMetaDataInfo() {
    Log.d(TAG, "getMetaDataInfo ~");
    return getMetaDataInfoByRetriever();

  }

  public int getAudioTrackInfoNum() {
    Log.d(TAG, "getAudioTrackInfoNum ~");
    int audioTrackNum = mPlayer.getAllAudioTrackCount();
    Log.d(TAG, "getAudioTrackInfoNum ~ " + audioTrackNum);
    return audioTrackNum;

  }
  public String getAudioTrackInfoMimeTypeByIndex(int position) {
    String mime = "und";
    int audioTrackNum = 0;
    MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
    if (tracks == null || tracks.length <= 0) {
      Log.d(TAG, "getAudioTrackInfoMimeTypeByIndex ~ tracks == null || tracks.length <=0");
      return mime;
    }
    Log.d(TAG, "getAudioTrackInfoMimeTypeByIndex ~ tracks.length:" + tracks.length + "  " + position);
    for (MtkTrackInfo info : tracks) {
      if (info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
        if (audioTrackNum == position) {
          MediaFormat mediaFormat = info.getFormat();
          if (mediaFormat != null) {
            mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            break;
          }
        }
        audioTrackNum++;
      }
    }
    return mime;
  }

  public String getVideoMimeType() {
    String mime = "und";
    MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
    if(tracks == null || tracks.length <=0){
      Log.d(TAG,"getVideoMimeType ~ tracks == null || tracks.length <=0" );
      return mime;
    }
    Log.d(TAG,"getVideoMimeType ~ tracks.length:" + tracks.length);
    for(MtkTrackInfo info: tracks){
      if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_VIDEO){
        MediaFormat mediaFormat = info.getFormat();
        if (mediaFormat != null){
          mime = mediaFormat.getString(MediaFormat.KEY_MIME);
          Log.d(TAG,"getVideoMimeType ~ mime==" + mime);
          break;
        }
      }
    }
    Log.d(TAG,"getVideoMimeType end mime==" + mime);
    return mime;
 }

  public String getAudioTrackInfoTypeByIndex(int position){
    Log.d(TAG,"getAudioTrackInfoByIndex ~",new Throwable());
    int audioTrackNum = 0;
    String audioTrackType = null;
    MtkTrackInfo[] tracks = mPlayer.mtkGetTrackInfo();
    if(tracks == null || tracks.length <=0){
      Log.d(TAG,"getAudioTrackInfoByIndex ~ tracks == null || tracks.length <=0");
      return "und";
    }
    Log.d(TAG,"getAudioTrackInfoByIndex ~ tracks.length:" + tracks.length + "  " + position);
    for(MtkTrackInfo info: tracks){
      if(info.getTrackType() == TrackInfo.MEDIA_TRACK_TYPE_AUDIO){
        if (audioTrackNum == position) {
          //audioTrackType = info.getLanguage();
          MediaFormat mediaFormat = info.getFormat();
          String mime = null;
          if (mediaFormat != null) {
            mime = mediaFormat.getString(MediaFormat.KEY_MIME);
          }
          Log.d(TAG,"getAudioTrackInfoByIndex ~ non exo mime:" + mime + "  audioTrackNum:" + audioTrackNum + "  audioTrackType:" + audioTrackType);
          if (TextUtils.isEmpty(audioTrackType)){
            audioTrackType = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
          } else {
            if ("und".equals(audioTrackType)){
              audioTrackType = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
            } else {
              String dubiString = DolbylogicManager.getInstance().getDubiDisplayInfo(mime);
              if ("und".equals(dubiString)){
                Log.d(TAG,"getAudioTrackInfoByIndex ~ und");
                // if audioTrackType is ok and can not replace it.
              } else {
                audioTrackType += " " +  dubiString;
              }
            }
          }
          boolean isHEACC = DolbylogicManager.getInstance().isHEACC(mediaFormat);
          if (isHEACC){
            audioTrackType = "HE_AAC";
          }
          break;
        }
        audioTrackNum++;
      }
    }
    return audioTrackType;
  }

  public int getVideoHeight() {
    int videoHeight = mPlayer.getVideoHeight();
    Log.d(TAG, "getVideoHeight ~ videoHeight = " + videoHeight + "  sourceType:" + sourceType);
    return videoHeight;// super.getVideoHeight();
  }

  public int getVideoWidth() {
    int videoWidth = mPlayer.getVideoWidth();
    Log.d(TAG, "getVideoWidth ~ videoWidth = " + videoWidth + "  sourceType:" + sourceType);
    return videoWidth;// super.getVideoWidth();
  }


  public boolean isLooping() {
    boolean isLooping = mPlayer.isLooping();
    Log.d(TAG, "isLooping ~ isLooping =" + isLooping);
    return isLooping;// super.isLooping();
  }

  public boolean isPlaying() {
    boolean isPlaying = mPlayer.isPlaying();
    Log.d(TAG, "isPlaying ~ isPlaying =" + isPlaying);
    return isPlaying;// super.isPlaying();
  }


  public void pause() throws IllegalStateException {
    Log.d(TAG, "pause sourceType =" + sourceType + "mPlayer =" + mPlayer + "mtkPlayer =" + mtkPlayer);
    mPlayer.pause();
  }

  public void prepare() throws IOException, IllegalStateException {
    Log.d(TAG, "prepare ~");
    mPlayer.prepareAsync();
  }

  public void prepareAsync() throws IllegalStateException {
    Log.d(TAG, "prepareAsync ~");
    mPlayer.prepareAsync();
  }

  public void release() {
    Log.d(TAG, "release ~");
    mPlayer.release();
  }

  public void reset() {
    mPlayer.reset();
  }

  public void seekTo(int arg0) throws IllegalStateException {
    Log.d(TAG, "seekTo ~ arg0 = " + arg0);
    mPlayer.seekTo(arg0);
  }

  public void setSubtitleDataSource(String path) {
    Log.d(TAG, "setSubtitleDataSource ~path = " + path + " sourceType = " + sourceType);
    mPlayer.setSubtitleDataSource(path);
  }

  private final List<String> subTitleList = new ArrayList<String>();

  private void setDLNASubtitle(String path) {
    Log.d(TAG, "setDLNASubtitle  path = " + path);
    subTitleList.clear();
    subTitleList.addAll(DLNAManager.getInstance().getSubTitleList(path));
  }

  public void setFilePath(String path) {
    Log.d(TAG, "setFilePath ~ path =" + path);
    mPath = path;
  }

  public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    Log.d(TAG, "setDataSource ~ path =" + path);
    mPath = path;
    mPlayer.setDataSource(mPath, null);
  }

  public void setDataSource(String path, Context context) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    Log.d(TAG, "setDataSource ~ path =" + path + " context:" + context);
    mPath = path;
    Util.setDolbyType(0);
      if (sourceType == MODE_LOCAL){
          mPlayer.setDataSource(context,  Uri.fromFile(new File(path)));
      } else {
          ((ExoMediaPlayer)mPlayer).setDataSource(this, context);
      }
  }

  public void setDisplay(SurfaceHolder arg0) {
    mPlayer.setDisplay(arg0);
  }

  public void setLooping(boolean arg0) {
    Log.d(TAG, "setLooping ~ arg0 = " + arg0);
      mPlayer.setLooping(arg0);
    // super.setLooping(arg0);
  }

  /*
   * public int setMetadataFilter(Set<Integer> arg0, Set<Integer> arg1) { return
   * super.setMetadataFilter(arg0, arg1); }
   */



  public void setOnBufferingUpdateListener(Object listener) {
    Log.d(TAG, "setOnBufferingUpdateListener ~");
    mPlayer.setOnBufferingUpdateListener((OnBufferingUpdateListener) listener);
    // super.setOnBufferingUpdateListener(listener);
  }

  public void setOnCompletionListener(Object listener) {
    Log.d(TAG, "setOnCompletionListener ~");
    mPlayer.setOnCompletionListener((OnCompletionListener) listener);
  }

  public void setOnErrorListener(Object listener) {
    Log.d(TAG, "setOnErrorListener ~");
    mPlayer.setOnErrorListener((OnErrorListener) listener);
  }

  public void setOnInfoListener(Object listener) {
    Log.d(TAG, "setOnInfoListener ~");
    mPlayer.setOnInfoListener((OnInfoListener) listener);
  }

  public void setOnPreparedListener(Object listener) {
    Log.d(TAG, "setOnPreparedListener ~");
    mPlayer.setOnPreparedListener((OnPreparedListener) listener);
  }

  public void setOnSeekCompleteListener(Object listener) {
    Log.d(TAG, "setOnSeekCompleteListener ~");
    mPlayer.setOnSeekCompleteListener((OnSeekCompleteListener) listener);
  }

  public void setOnTimedTextListener(Object listener) {
    Log.d(TAG, "setOnTimedTextListener ~");
    mPlayer.setOnTimedTextListener((OnTimedTextListener) listener);
  }

  public void setOnVideoSizeChangedListener(Object listener) {
    Log.d(TAG, "setOnVideoSizeChangedListener ~");
    mPlayer.setOnVideoSizeChangedListener((OnVideoSizeChangedListener) listener);
  }


  public void setPlayerRole(int playerRole) {
    Log.d(TAG, "setPlayerRole ~ playerRole =" + playerRole);
//    mPlayer.setPlayerRole(playerRole);
  }

  public void setScreenOnWhilePlaying(boolean screenOn) {
    Log.d(TAG, "setScreenOnWhilePlaying ~screenOn:" + screenOn);
    mPlayer.setScreenOnWhilePlaying(screenOn);
  }

  public void setSurface(Surface surface) {
    Log.d(TAG, "setSurface ~surface:" + surface);
   mPlayer.setSurface(surface);
  }

  public int setPlayModeEx(int speed) {
    Log.d(TAG, "setPlayModeEx ~speed:" + speed);
    return mPlayer.setPlayMode(speed);
  }

  public boolean setSeamlessMode(boolean seamlessMode) {
    Log.d(TAG,"setSeamlessMode:" + seamlessMode);
    if(true == Feature.isAospCnPlatform() && Util.isUseExoPlayer()) {
      mIsSeamlessModeOn = seamlessMode;
      //add setSeamlessMode
      return mPlayer.setSeamlessMode(seamlessMode);
    }
    return false;
  }

  public boolean getSeamlessMode(){
    return mIsSeamlessModeOn;
  }

  public void setPlaybackParams(PlaybackParams params) {
    mPlayer.setPlaybackParams(params);
  }

  public PlaybackParams getPlaybackParams() {
    PlaybackParams pata = mPlayer.getPlaybackParams();
    if (null == pata) {
      pata = new PlaybackParams();
      pata.setSpeed(1.0f);
    }
    return pata;
  }

  public void start() throws IllegalStateException {
    Log.d(TAG, "start ~");
    mPlayer.start();
  }

  public void stop() throws IllegalStateException {
    Log.d(TAG, "stop ~",new Throwable());
    mPlayer.stop();
  }

  /**
   * closeStream.
   * Needn't close here, will handle in
   *
   */
  public void closeStream() {
    Log.d(TAG, "closeStream ~mInputStream:" + mInputStream);
    if (null != mInputStream) {
      try {
        mInputStream.close();
      } catch (IOException e) {
        Log.d(TAG, "video closeStream() fail" + e.toString());
      }
    } else {
      Log.d(TAG, "video closeStream()  stream is null");
    }
    mInputStream = null;
  }

  @Override
  public long getSourceSize() {
    Log.d(TAG, "getSourceSize ~:sourceType:" + sourceType);
    long fileSize = 0;
    switch (sourceType) {
      case MODE_DLNA: {
        DLNADataSource dlnaSource = DLNAManager.getInstance()
            .getDLNADataSource(mPath);
        if (dlnaSource != null) {
          fileSize = dlnaSource.getContent().getSize();
          Log.d(TAG, "getVideoFileSize = dlna" + fileSize);
        }
      }
        break;
      case MODE_SAMBA: {
        SambaManager sambaManager = SambaManager.getInstance();
        try {
          fileSize = sambaManager.size(mPath);
          Log.d(TAG, "getVideoFileSize = samba" + fileSize);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
        break;
      case MODE_LOCAL: {
        MtkFile mFile = null;
        if (mPath != null) {
          mFile = new MtkFile(mPath);
        }
        if (mFile == null) {
          fileSize = 0;
          break;
        }
        fileSize = mFile.getFileSize();
        Log.d(TAG, "getVideoFileSize = fileSize" + fileSize);
      }
        break;
      case MODE_HTTP:
        DLNADataSource dlnaSource = DLNAManager.getInstance()
            .getDLNADataSource(DmrHelper.getObject());
        if (dlnaSource != null) {
          fileSize = dlnaSource.getContent().getSize();
          Log.d(TAG, "getVideoFileSize = MODE_HTTP fileSize:" + fileSize);
        }
        break;
      default:
        break;
    }

    return fileSize;
  }

    @Override
    public DataSourceType getSourceType() {
        Log.d(TAG, "getSourceType ~sourceType:" + sourceType);
        DataSourceType eSourceType = null;
        switch (sourceType) {
            case MODE_DLNA:
            case MODE_HTTP:
                eSourceType = DataSourceType.SOURCE_TYPE_DLNA;
                break;
            case MODE_SAMBA:
                eSourceType = DataSourceType.SOURCE_TYPE_SAMBA;
                break;
            case MODE_LOCAL:
                eSourceType = DataSourceType.SOURCE_TYPE_LOCAL;
                break;
            default:
                break;
        }
        return eSourceType;
    }
    @Override
  public boolean isSeekable() {
    boolean canSeek = true;
    switch (sourceType) {
      case MODE_DLNA:
        DLNADataSource dlnaSource = DLNAManager.getInstance().getDLNADataSource(mPath);
        if(dlnaSource != null) {
          canSeek = dlnaSource.getContent().canSeek();
        }
        break;
      case MODE_SAMBA:
        break;
      case MODE_LOCAL:
        break;
      case MODE_HTTP:
        DLNADataSource dlna = DLNAManager.getInstance().getDLNADataSource(DmrHelper.getObject());
        if (dlna != null) {
          canSeek = dlna.getContent().canSeek();
        }
        break;
      default:
        break;
    }
    Log.d(TAG, "isSeekable ~sourceType:" + sourceType + " canSeek:" + canSeek);
    return canSeek;
  }

    @Override
  public InputStream newInputStream() {
    Log.d(TAG, "newInputStream mCurrentPath:" + mPath + "sourceType:" + sourceType);
    if (mPath == null) {
      return null;
    } else {
      if (sourceType == MODE_LOCAL) {
        try {
          mInputStream = new FileInputStream(mPath);
        } catch (FileNotFoundException e) {
          e.printStackTrace();
        }
      } else if (sourceType == MODE_SAMBA) {
        try {
          mInputStream = SambaManager.getInstance()
              .getSambaDataSource(mPath).newInputStream();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else if (sourceType == MODE_DLNA) {
        DLNADataSource source = DLNAManager.getInstance()
            .getDLNADataSource(mPath);
        Log.d(TAG, "PLAYER_MODE_DLNA mCurrentPath:" + mPath);
        if (source == null) {
          mInputStream = null;

        } else {
          mInputStream = source.newInputStream();
          Log.d(TAG, "PLAYER_MODE_DLNA mInputStream:" + mInputStream);
        }
      } else if (sourceType == MODE_HTTP) {
        DLNADataSource source = DLNAManager.getInstance()
            .getDLNADataSource(DmrHelper.getObject());
        Log.d(TAG, "dmr source:" + source);
        if (source == null) {
          mInputStream = null;
        } else {
          mInputStream = source.newInputStream();
          Log.d(TAG, "dmr mInputStream:" + mInputStream);
        }
      }
      return mInputStream;
    }

  }

  public void setUnLockPin(int pin) {
//    if (null != mPlayer) {
//      mPlayer.setUnLockPin(pin);
//    }
  }


  public int getSubtitleIndex() {
    int index = mPlayer.getCurrentSubtitleIndex();
    Log.i(TAG, "getSubtitleIndex sourceType:" + sourceType + "  index:" + index);
    return index;
  }

  public void setTunnelingMode(int tunnelingMode) {
    Log.i(TAG,"setTunnelingMode,tunnelingMode == "+tunnelingMode);
    if (null != mPlayer && Util.isUseExoPlayer()) {
        ((ExoMediaPlayer)mPlayer).setMediaTunnelingMode(tunnelingMode);
    }
  }

  public String getFileDate() {
    MtkFile mFile = null;
    String lastModified = null;
    if (mPath != null) {
      mFile = new MtkFile(mPath);
    }
    if (mFile != null) {
      lastModified = new SimpleDateFormat("yyyy-MM-dd")
                        .format(new Date(mFile.lastModified()));
    }
    Log.d(TAG, "getFileDate, lastModified == "+lastModified);

    return lastModified;
  }
}
