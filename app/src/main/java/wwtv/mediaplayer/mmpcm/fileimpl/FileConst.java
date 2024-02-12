
package com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl;

public class FileConst {

  public static final int SMALL = 1;
  public static final int MEDIUM = 2;
  public static final int LARGE = 3;

  public static final int SORT_NAME = 1;
  public static final int SORT_DATE = 2;
  public static final int SORT_GENRE = 3;
  public static final int SORT_ARTIST = 4;
  public static final int SORT_ALBUM = 5;
  public static final int SORT_TYPE = 6;

  public static final int FILE_TYPE_UNKNOW = 0;
  public static final int FILE_TYPE_DIRECTORY = 1;
  public static final int FILE_TYPE_VIDEO = 2;
  public static final int FILE_TYPE_AUDIO = 3;
  public static final int FILE_TYPE_IMAGE = 4;
  public static final int FILE_TYPE_TEXT = 5;
  public static final int FILE_TYPE_DEVICE = 6;

  public static final int SRC_ALL = 0;
  public static final int SRC_USB = 1;
  public static final int SRC_DLNA = 2;
  public static final int SRC_SMB = 3;
  public static final int SRC_HTTP = 4;

  public static final int MMP_FF_ALL = 0;
  public static final int MMP_FF_VIDEO = 1;
  public static final int MMP_FF_PHOTO = 2;
  public static final int MMP_FF_AUDIO = 3;
  public static final int MMP_FF_TEXT = 4;
  public static final int MMP_TYPE_ALL = 6;
  /*
   * add
   */
  public static final int MMP_FF_ISO = 5;
  public static final int MMP_FF_ISOVIDEO = 6;
  public static final int MMP_FF_THRDPHOTO = 7; // by lei add

  public static final int RECURSIVE_PARSE_COUNT_PER_TIIME = 100;

  public static final int MSG_DEVICE_PARSED = 1;
  public static final int MSG_FILE_PARSEING = 2;
  public static final int MSG_FILE_PARSED = 3;
  public static final int MSG_FILE_EMPTY = 4;
  public static final int MSG_FILE_REMOVE = 5;
  public static final int MSG_SMB_NEED_AUTH = 6;
  public static final int MSG_SMB_UNKNOW_HOST = 7;

  //goolge sw decoder limit is 32737*32767(width*height)
  public static final long MAX_PHOTO_SIZE = 1000 * 1024 * 1024;
  public static final long MAX_PHOTO_SIZE_NET = 1000 * 1024 * 1024;

  public static final String photoSuffix[] = {
      ".png", ".bmp", ".jpg",
      ".PNG", ".jpeg", ".gif", ".webp", ".heif", ".heic"
  };
  public static final String thrdPhotoSuffix[] = {
      ".mpo", ".jps", ".pns"
  };
  public static final String textSuffix[] = {
    ".txt"
  };
  public static final String audioSuffix[] = {
      ".mp3", ".wma",
      ".m3u8", ".wav", ".m4a", ".aac", ".ac3", ".ec3",
      ".wpl", ".ogg", ".mp2", ".ra", ".flac", ".ape", ".amr",
      ".awb",".rm",".dtshd",".dtsx",".dts",".mid",".xmf",".mxmf",".rtttl",
      ".rtx",".ota",".imy",".opus", ".mka"
  };
  public static final String videoSuffix[] = {
      ".rmvb", ".avi", ".mkv",
      ".mp4", ".3gp", ".flv", ".mpg", ".ts", ".wmv", ".vob", ".rm",
      ".mov", ".avs", ".asf", ".mpe", ".mpeg", ".dat", ".asx",
      ".m4v", ".tp", ".trp", ".tts", ".m2ts", ".mts", ".m1v", ".m2v",
      ".m4v", ".264", ".vc1", ".flv", ".hlv", ".ogm", ".webm",
      ".ram", ".iso", ".ssif", ".264", ".265", ".avs2", ".heic", ".ivf",
      ".m2t",".mod",".3g2"
  };
  public static final String isovideoSuffix[] = {
    ".ssif"
  };

  public static int MEDIA_TYPE_UNKNOWN = 0; // /< Unknown format type
  public static int MEDIA_TYPE_AVI = 1; // /< AVI file
  public static int MEDIA_TYPE_MPEG2_PS = 2; // /< Mpeg2 program stream, or Mpeg1 system stream
  public static int MEDIA_TYPE_MPEG2_TS = 3; // /< Mpeg2 transport stream
  public static int MEDIA_TYPE_ASF = 4; // /< WMV and ASF
  public static int MEDIA_TYPE_MKV = 5; // /< MKV file
  public static int MEDIA_TYPE_OGG = 6; // /< Ogg file
  public static int MEDIA_TYPE_FLAC = 7; // /< the FLAC file format, which contains the FLAC audio
                                         // codec (::IMTK_PB_CTRL_AUD_ENC_FLAC)
  public static int MEDIA_TYPE_APE = 8; // /< the APE file format, which contains Monkey's Audio
                                        // (::IMTK_PB_CTRL_AUD_ENC_MONKEY)
  public static int MEDIA_TYPE_VIDEO_ES = 9; // /< Video elementary stream
  public static int MEDIA_TYPE_AUDIO_ES = 10; // /< Audio elementary stream
  public static int MEDIA_TYPE_MP4 = 11; // /< MP4 file (Lib Master only, for
                                         // IMtkPb_Ctrl_GetMediaInfo())
  public static int MEDIA_TYPE_WAV = 12; // /< WAV file (Lib Master only, for
                                         // IMtkPb_Ctrl_GetMediaInfo())
  public static int MEDIA_TYPE_RM = 13; // /< Real Media file (Lib Master only, for
                                        // IMtkPb_Ctrl_GetMediaInfo())
  public static int MEDIA_TYPE_MTK_P0 = 14; // /< MTK Private format 0 (App Master only, described
                                            // in @ref MTKP0page)
  public static int MEDIA_TYPE_MKA = 15; // /< MKV file

  public String toString() {
     return "FileConst";
  }
}
