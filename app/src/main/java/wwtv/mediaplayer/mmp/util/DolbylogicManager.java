
package com.mediatek.wwtv.mediaplayer.mmp.util;

import android.text.TextUtils;
import android.util.Log;
import android.media.MediaFormat;
import java.util.Locale;

import com.mediatek.twoworlds.tv.MtkTvVolCtrl;

public class DolbylogicManager {
  private static final String TAG = "DolbylogicManager";
  private static DolbylogicManager mInstance;

  public static synchronized DolbylogicManager getInstance() {
    if (mInstance == null) {
      mInstance = new DolbylogicManager();
    }
    return mInstance;
  }

  public String getDubiDisplayInfo(String mime) {
    String result = "";
    Log.d(TAG, "getDubiDisplayInfo getDubiInfo mime = " + mime);
    if (TextUtils.isEmpty(mime)) {
        Log.d(TAG, "getDubiDisplayInfo mime is empty");
    } else if (mime.toLowerCase(Locale.ROOT).contains("eac3-dual")
        || mime.toLowerCase(Locale.ROOT).contains("ac3-dual")) {
      MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
      Log.d(TAG, "getDubiDisplayInfo getDubiInfo speakerType = " + speakerType);
      switch (speakerType) {
        case AUDDEC_SPK_MODE_LR:
          result = "DOLBY AUDIO STEREO";
          break;
        case AUDDEC_SPK_MODE_LL:
          result = "DOLBY AUDIO DUAL1";
          break;
        case AUDDEC_SPK_MODE_RR:
          result = "DOLBY AUDIO DUAL2";
          break;
        default:
          break;
      }
    } else if (mime.toLowerCase(Locale.ROOT).contains("ac3-surround")) {
      result = "DOLBY AUDIO SURROUND";
    } else if (mime.toLowerCase(Locale.ROOT).contains("ac3-mono")) {
      result = "DOLBY AUDIO MONO";
    } else if (mime.toLowerCase(Locale.ROOT).contains("ac3-stereo")) {
      result = "DOLBY AUDIO STEREO";
    } else if (mime.toLowerCase(Locale.ROOT).contains("heaacv2-dual")) {
      MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
      Log.d(TAG, "getDubiDisplayInfo getDubiInfo speakerType = " + speakerType);
      switch (speakerType) {
        case AUDDEC_SPK_MODE_LR:
          result = "Heaacv2 Dual Stereo";
          break;
        case AUDDEC_SPK_MODE_LL:
          result = "Heaacv2 Dual1";
          break;
        case AUDDEC_SPK_MODE_RR:
          result = "Heaacv2 Dual2";
          break;
        default:
          break;
      }
    } else if (mime.toLowerCase(Locale.ROOT).contains("heaacv2")) {
      result = "HEAACV2";
    } else if (mime.toLowerCase(Locale.ROOT).contains("heaac-dual")) {
      MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
      Log.d(TAG, "getDubiDisplayInfo getDubiInfo speakerType = " + speakerType);
      switch (speakerType) {
        case AUDDEC_SPK_MODE_LR:
          result = "Heaac Dual Stereo";
          break;
        case AUDDEC_SPK_MODE_LL:
          result = "Heaac Dual1";
          break;
        case AUDDEC_SPK_MODE_RR:
          result = "Heaac Dual2";
          break;
        default:
          break;
      }
    } else if (mime.toLowerCase(Locale.ROOT).contains("heaac")) {
      result = "HEAAC";
    } else if (mime.toLowerCase(Locale.ROOT).contains("x-dts-surround")) {
      result = "DTS Surround";
    } else if (mime.toLowerCase(Locale.ROOT).contains("x-dts")) {
      result = "DTS Stereo";
    } else if (mime.toLowerCase(Locale.ROOT).contains("mpeg2")) {
      result = "MPEG2";
    } else if (mime.toLowerCase(Locale.ROOT).contains("mpeg")) {
      result = "MP3";
    } else if (mime.toLowerCase(Locale.ROOT).contains("mp4a-latm")) {
      result = "AAC";
    } else if (mime.toLowerCase(Locale.ROOT).contains("x-ms-wmapro")) {
      result = "WMA Pro";
    } else if (mime.toLowerCase(Locale.ROOT).contains("x-ms-wma")) {
      result = "WMA";
    } else if (mime.toLowerCase(Locale.ROOT).contains("x-adpcm-ms")) {
      result = "LPCM";
    } else if (mime.toLowerCase(Locale.ROOT).contains("flac")) {
      result = "FLAC";
    } else if (mime.toLowerCase(Locale.ROOT).contains("vorbis")) {
      result = "Vorbis";
    } else if (mime.toLowerCase(Locale.ROOT).contains("3gpp")) {
      result = "AMR-NB";
    } else if (mime.toLowerCase(Locale.ROOT).contains("amr-wb")) {
      result = "AMR-WB";
    } else if (mime.toLowerCase(Locale.ROOT).contains("ape")) {
      result = "APE";
    } else if (mime.toLowerCase(Locale.ROOT).contains("vnd.rn-realaudio")) {
      result = "COOK";
    } else if (mime.toLowerCase(Locale.ROOT).contains("vnd.dts.hd;profile=lbr")) {
      result = "";
      //result = "DTS Express";
    } else if (mime.toLowerCase(Locale.ROOT).contains("vnd.dts.hd")) {
      result = "";
      //result = "DTS-HD Master Audio";
    } else if (mime.toLowerCase(Locale.ROOT).contains("vnd.dts")) {
      result = "";
      //result = "DTS";
    } else if (mime.toLowerCase(Locale.ROOT).contains("eac3")
                || mime.toLowerCase(Locale.ROOT).contains("ac3")
                || mime.toLowerCase(Locale.ROOT).contains("ac4")) {
        Log.d(TAG, "getDubiDisplayInfo do nothing");
        //do nothing
    } else {
      if (mime.contains("audio/")) {
        result = mime.substring(6);
      } else {
        result = mime;
      }
    }
    Log.d(TAG, "getDubiDisplayInfo getDubiInfo result =  " + result);
    return result;
  }

  public boolean isDolbyAudio(String mime) {
    if (null == mime){
        return false;
    }
    return mime.toLowerCase(Locale.ROOT).contains("ac3")
        || mime.toLowerCase(Locale.ROOT).contains("ac4");

  }

  public boolean isDolbyDualAudio(String mime) {
    if (null == mime){
        return false;
    }
    return mime.toLowerCase(Locale.ROOT).contains("eac3-dual")
        || mime.toLowerCase(Locale.ROOT).contains("ac3-dual");
  }

  public boolean isDolbyAtmos(String mime) {
    if (null == mime){
        return false;
    }
    return mime.toLowerCase(Locale.ROOT).contains("eac3-joc")
        || mime.toLowerCase(Locale.ROOT).contains("ac4-joc");
  }

  public boolean isDolbyVisionType(String mime) {
    if (null == mime){
        return false;
    }
    return mime.toLowerCase(Locale.ROOT).contains("video/dolby-vision");

  }

  public boolean isDTSAudio(String mime) {
    if (null == mime){
        return false;
    }

    return mime.toLowerCase(Locale.ROOT).contains("dts");

  }

  public boolean isHEACC(MediaFormat mediaFormat) {
    if (null == mediaFormat){
        return false;
    }

    try {
        boolean isContainsAACKey = mediaFormat.containsKey(MediaFormat.KEY_AAC_PROFILE);
        Log.d(TAG,"isHEACC isContainsAACKey=="+isContainsAACKey);
        if (isContainsAACKey){
            int aacKey = mediaFormat.getInteger(MediaFormat.KEY_AAC_PROFILE);
            Log.d(TAG,"isHEACC aacKey=="+aacKey);
            if (5 == aacKey || 29 == aacKey){
                return true;
            }
        } else {
            return false;
        }
    }  catch (Exception e) {
        Log.d(TAG, "isHEACC: " + e.toString());
    }

    return false;
  }

}
