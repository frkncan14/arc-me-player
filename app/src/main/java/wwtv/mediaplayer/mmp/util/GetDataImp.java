
package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.TypedArray;
import android.provider.Settings;
import android.util.Log;

import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.util.Feature;
import com.mediatek.wwtv.util.Util;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.LrcObject;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;

public final class GetDataImp {
  private static final String TAG = "GetDataImp";
  // private static final Random RNG = new Random();
  int[] rawArray = new int[4];
  private static List<LrcObject> lrcmap;

  private boolean mIsInLrcOffsetMenu = false;
  private boolean mIsInEncodingMenu;

  private static GetDataImp imp = new GetDataImp();

  private GetDataImp() {
  };

  public static GetDataImp getInstance() {
    return imp;
  }

  /*
   * public int[] randArr() { int[] rawArray = new int[8]; for (int i = 0; i < rawArray.length; i++)
   * { rawArray[i] = RNG.nextInt(7); } return rawArray; }
   */

  public List<MenuFatherObject> getComMenu(Context context,
      int contentresid, int enableresid, int hasnextresid) {
    mIsInLrcOffsetMenu = false;
    mIsInEncodingMenu = false;
    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();

    String[] menucontent = context.getResources().getStringArray(
        contentresid);
    int[] menuenable = context.getResources().getIntArray(enableresid);
    int[] menuhasnext = context.getResources().getIntArray(hasnextresid);
    int picture_film_maker_mode = Settings.Global.getInt(context.getContentResolver(), "picture_film_maker_mode", 0);
    for (int i = 0; i < menuenable.length; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = menucontent[i];
      Log.e(TAG, "object.content: " + object.content + ";picture_film_maker_mode:" + picture_film_maker_mode);
      if(menucontent[i].equals(context.getString(R.string.mmp_menu_screenmode)) && picture_film_maker_mode != 0) {     
        object.enable = false;
        object.hasnext = false;
      }else {
        object.enable = menuenable[i] == 1 ? true : false;
        object.hasnext = menuhasnext[i] == 1 ? true : false;
      }
      MtkLog.d(TAG, "object.hasnext" + object.hasnext + " menuhasnext[i]" + menuhasnext[i]);
      menuList.add(object);
    }
    return menuList;
  }

  public List<MenuFatherObject> getComMenuEx(Context context,
      int contentresid, int enableresid, int hasnextresid) {
    mIsInLrcOffsetMenu = false;
    mIsInEncodingMenu = false;
    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();

    TypedArray tArr = context.getResources().obtainTypedArray(contentresid);
    final int len = tArr.length();
    int[] menuenable =  new int[1];
    int[] menuhasnext = new int[1];
    try {
      menuenable = context.getResources().getIntArray(enableresid);
      menuhasnext = context.getResources().getIntArray(hasnextresid);
    } catch (NotFoundException e) {
      tArr.recycle();
      MtkLog.e(TAG, e.toString());
      return menuList;
    }
    for (int i = 0; i < len; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = new String(tArr.getString(i));
      object.enable = menuenable[i] == 1 ? true : false;
      object.hasnext = menuhasnext[i] == 1 ? true : false;
      object.setId(tArr.getResourceId(i, MenuFatherObject.MENU_INVALID_ID));
      MtkLog.d(TAG, "object.hasnext" + object.hasnext + " menuhasnext[i]" + menuhasnext[i]);
      menuList.add(object);
    }
    tArr.recycle();

    return menuList;
  }

  public List<MenuFatherObject> getChildList(Context mContext,
      String content) {
    mIsInLrcOffsetMenu = false;
    mIsInEncodingMenu = false;
    List<MenuFatherObject> mList = new ArrayList<MenuFatherObject>();
    int contentresid = R.array.mmp_menu_sortlist_photoortext;
    int enableresid = R.array.mmp_menu_sortlist_enable;
    MtkLog.d(TAG, "content:" + content);
    if (content.equals(mContext.getString(R.string.mmp_menu_sort))) {

      enableresid = R.array.mmp_menu_sortlist_enable;
      int type = MultiFilesManager.getInstance(mContext).getContentType();
      switch (type) {
        case MultiMediaConstant.PHOTO:
          contentresid = R.array.mmp_menu_sortlist_video;
          break;
        case MultiMediaConstant.TEXT: {
          contentresid = R.array.mmp_menu_sortlist_photoortext;
          break;
        }
        case MultiMediaConstant.AUDIO: {
          contentresid = R.array.mmp_menu_sortlist_audio;
          break;
        }
        case MultiMediaConstant.VIDEO: {
          contentresid = R.array.mmp_menu_sortlist_video;
          break;
        }

        default:
          break;
      }

    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_mediatype))) {
      contentresid = R.array.mmp_menu_mediatypelist;
      if (false == Feature.isAospCnPlatform()) {
        enableresid = R.array.mmp_menu_mediatypelist_enable;
      } else {
        enableresid = R.array.mmp_menu_mediatypelist_enable_cnsamba;
      }
    } else if (content.equals(mContext.getString(R.string.mmp_menu_thumb))) {
      contentresid = R.array.mmp_menu_thumblist;
      enableresid = R.array.mmp_menu_thumblist_enable;

    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_photoframe))) {
      contentresid = R.array.mmp_menu_photoframelist;
      enableresid = R.array.mmp_menu_photoframelist_enable;
      mList = getChildComMenu(mContext, contentresid, enableresid);
      String name = MultiFilesManager.getInstance(mContext)
          .getCurDevName();
      if (null != name) {
        MenuFatherObject object = new MenuFatherObject();
        object.content = name;
        object.enable = true;
        mList.add(object);
      }
      return mList;

    } else if (content.equals(mContext.getString(R.string.mmp_menu_repeat))) {
      contentresid = R.array.mmp_menu_repeatlist;
      enableresid = R.array.mmp_menu_repeatlist_enable;
      if (true == Feature.isAospCnPlatform()) {
        final int type = MultiFilesManager.getInstance(mContext).getContentType();
        switch (type) {
          case MultiMediaConstant.PHOTO:
            enableresid = R.array.mmp_menu_repeatlist_img_enable;
            break;
          default:
            break;
        }
      }
    } else if (content
        .equals(mContext.getString(R.string.mmp_menu_shuffle))) {
      contentresid = R.array.mmp_menu_shufflelist;
      enableresid = R.array.mmp_menu_shufflelist_enable;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_duration))) {
      contentresid = R.array.mmp_menu_durationlist;
      enableresid = R.array.mmp_menu_durationlist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_effect))) {
      contentresid = R.array.mmp_menu_effectlist;
      enableresid = R.array.mmp_menu_effectlist_enable;
    } else if (content
        .equals(mContext.getString(R.string.mmp_menu_display))) {
      contentresid = R.array.mmp_menu_displaylinelist;
      enableresid = R.array.mmp_menu_displaylinelist_enable;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_timeoffset))) {
      contentresid = R.array.mmp_menu_timeoffsetlist;
      enableresid = R.array.mmp_menu_timeoffsetlist_enable;
      // Added by Dan for fix bug DTV00389330
      mIsInLrcOffsetMenu = true;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_encoding))) {
      contentresid = R.array.mmp_menu_encodinglist;
      enableresid = R.array.mmp_menu_encodinglist_enable;
      mIsInEncodingMenu = true;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_picturemode))) {
      contentresid = R.array.mmp_menu_picturemodelist;
      enableresid = R.array.mmp_menu_picturemodelist_enable;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_ts_program))) {
      String[] programs = LogicManager.getInstance(mContext)
          .getTSVideoProgramList();
      for (String program : programs) {
        MenuFatherObject object = new MenuFatherObject();
        object.content = program;
        object.enable = true;
        mList.add(object);
      }
      return mList;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_screenmode))) {
      // TODO screen mode
      //int[] modes = LogicManager.getInstance(mContext)
      //    .getAvailableScreenMode();
      String[] strModes = mContext.getResources().getStringArray(
          R.array.mmp_menu_screenmodelist);
      int arrLen = strModes.length;
      if (true != Feature.isAospCnPlatform()) {
        // Over scan mode is only supported in CN
        arrLen -= 1;
      }
      for (int i = 0; i < arrLen; i++) {
          MenuFatherObject object = new MenuFatherObject();
          object.content = strModes[i];
          object.enable = true;
          mList.add(object);
      }
      return mList;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_size))) {
      contentresid = R.array.mmp_menu_sizelist;
      enableresid = R.array.mmp_menu_sizelist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_style))) {
      contentresid = R.array.mmp_menu_stylelist;
      enableresid = R.array.mmp_menu_stylelist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_color))) {
      contentresid = R.array.mmp_menu_colorlist;
      enableresid = R.array.mmp_menu_colorlist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_zoom))) {
      contentresid = R.array.mmp_menu_zoomlist;
      enableresid = R.array.mmp_menu_zoomlist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_last_memory))) {
      if (Util.isUseExoPlayer()){
          contentresid = R.array.mmp_lastmemory_exo_array;
          enableresid = R.array.mmp_lastmemory_exo_array_enable;
      } else {
          contentresid = R.array.mmp_lastmemory_array;
          enableresid = R.array.mmp_lastmemory_array_enable;
      }
      MtkLog.i(TAG, "content: Last Memory contentresid:" + contentresid + " enableresid:"
          + enableresid);
    } else if (content.equals(mContext.getString(R.string.mmp_menu_play_speed))
        && (true == Feature.isAospCnPlatform())) {
      contentresid = R.array.mmp_menu_play_speed_list;
      enableresid = R.array.mmp_menu_play_speed_list_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_subtitle_encoding))) {
      contentresid = R.array.mmp_subtitle_encoding_array;
      enableresid = R.array.mmp_subtitle_encoding_array_enable;
    } else if (content.equals(mContext
        .getString(R.string.menu_audio_sound_tracks))) {
        LogicManager logicManager = LogicManager.getInstance(mContext);
        int soundListsize = logicManager.getAudioTranckNumber();
        MtkLog.d(TAG, "loadSoundTrack,soundListsize:" + soundListsize);
        String[] soundTracksList = new String[soundListsize];
        for (int i = 0;i< soundListsize;i++) {
            String itemName = logicManager.getCurrentAudioTranckType(i);
            String soundString = logicManager.getCurrentAudioTranckMimeType(i);
            MtkLog.d(TAG, "loadSoundTrack,itemName:" + itemName);
            MtkLog.d(TAG, "loadSoundTrack,soundString:" + soundString);
            soundTracksList[i] = (i+1) + ": " + soundString;
        }
      for (String soundTrack : soundTracksList) {
        MenuFatherObject object = new MenuFatherObject();
        object.content = soundTrack;
        object.enable = true;
        mList.add(object);
      }
      return mList;
    } else if (content.equals(mContext.getString(R.string.mmp_seamless_mode))) {
      contentresid = R.array.mmp_menu_seamless_modelist;
      enableresid = R.array.mmp_menu_seamless_modelist_enable;
    } else if ((content.equals(mContext.getString(R.string.mmp_menu_rotate)))
      && (true == Feature.isAospCnPlatform())) {
      contentresid = R.array.mmp_menu_rotatelist;
      enableresid = R.array.mmp_menu_rotatelist_enable;
    } else {
      if (content.equals(mContext.getString(R.string.mmp_divx_title))
          || content.equals(mContext.getString(R.string.mmp_divx_edition))
          || content.equals(mContext.getString(R.string.mmp_divx_chapter))) {
        mList = getChildComMenu(mContext, content);
        return mList;
      }
    }
    mList = getChildComMenu(mContext, contentresid, enableresid);

    return mList;
  }

  public List<MenuFatherObject> getChildListEx(Context mContext,
      final String content, final int mnuId) {
    mIsInLrcOffsetMenu = false;
    mIsInEncodingMenu = false;
    List<MenuFatherObject> mList = new ArrayList<MenuFatherObject>();
    int contentresid = R.array.mmp_menu_sortlist_photoortext;
    int enableresid = R.array.mmp_menu_sortlist_enable;
    MtkLog.d(TAG, "content:" + content);

    switch (mnuId) {
      case R.string.mmp_menu_rotate:
        contentresid = R.array.mmp_menu_rotatelist;
        enableresid = R.array.mmp_menu_rotatelist_enable;
        break;
      case R.string.mmp_menu_zoom:
        if (true == Feature.isAospCnPlatform()) {
          contentresid = R.array.mmp_menu_img_zoomlist;
          enableresid = R.array.mmp_menu_img_zoomlist_enable;
        }
        break;
      default:
        return getChildList(mContext, content);
    }
    mList = getChildComMenuEx(mContext, contentresid, enableresid);

    return mList;
  }

  private List<MenuFatherObject> getChildComMenu(Context context, String content) {
    List<MenuFatherObject> menuList;
    int size = 0;
    menuList = createChildList(content, size);
    return menuList;
  }

  private List<MenuFatherObject> createChildList(String content, int size) {

    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();
    for (int i = 0; i < size; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = content + "_" + String.valueOf(i + 1);
      object.enable = true;
      menuList.add(object);
    }
    return menuList;
  }

  private List<MenuFatherObject> getChildComMenu(Context context,
      int contentresid, int enableresid) {
    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();
    String[] menucontent = context.getResources().getStringArray(
        contentresid);
    int[] menuenable = context.getResources().getIntArray(enableresid);
    for (int i = 0; i < menucontent.length; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = menucontent[i];
      object.enable = menuenable[i] == 1 ? true : false;
      menuList.add(object);
    }
    return menuList;
  }

  private List<MenuFatherObject> getChildComMenuEx(Context context,
      int contentresid, int enableresid) {
    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();
    TypedArray tArr = context.getResources().obtainTypedArray(contentresid);
    final int len = tArr.length();
    int[] menuenable = new int[1];
    try {
      menuenable = context.getResources().getIntArray(enableresid);
    } catch (NotFoundException e) {
      tArr.recycle();
      MtkLog.e(TAG, e.toString());
      return menuList;
    }
    for (int i = 0; i < len; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = new String(tArr.getString(i));
      object.enable = menuenable[i] == 1 ? true : false;
      object.setId(tArr.getResourceId(i, MenuFatherObject.MENU_INVALID_ID));
      menuList.add(object);
    }
    tArr.recycle();

    return menuList;
  }
  public List<LrcObject> read(String file, Context context) {
    lrcmap = new Vector<LrcObject>();
    Vector<LrcObject> lrcread = new Vector<LrcObject>();
    String data;
    InputStreamReader stream = null;
    BufferedReader br = null;
    try {
      // File saveFile = new File(file);
      // FileInputStream stream = new FileInputStream(saveFile);
      stream = new InputStreamReader(context
          .getResources().getAssets().open(file), "GB2312");
      // context.getResources().getAssets().open(file);
      br = new BufferedReader(stream);
      while ((data = br.readLine()) != null) {
        data = data.replace("[", "");
        data = data.replace("]", "@");

        String splitdata[] = data.split("@");
        String lrcContenet = splitdata[splitdata.length - 1];
        for (int j = 0; j < splitdata.length - 1; j++) {
          String tmpstr = splitdata[j];

          tmpstr = tmpstr.replace(":", ".");
          tmpstr = tmpstr.replace(".", "@");
          String timedata[] = tmpstr.split("@");

          Long m = Long.parseLong(timedata[0]); //
          Long s = Long.parseLong(timedata[1]); //
          Long ms = Long.parseLong(timedata[2]); //
          int currTime = (int) ((m * 60 + s) * 1000 + ms * 10);
          LrcObject item1 = new LrcObject();

          item1.begintime = currTime;
          item1.lrc = lrcContenet;
          // lrc_read.put(currTime, item1);//
          lrcread.add(item1);
        }

      }
      stream.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if(null != stream){
          try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
      }

      if(null != br){
        try {
            br.close();
        } catch (IOException e){
            e.printStackTrace();
        }
      }
    }

    lrcmap.clear();
    // data = "";
    LrcObject oldval = null;
    int i = 0;
    StringBuffer sb = new StringBuffer();
    for (int j = 0; j < lrcread.size(); j++) {
      LrcObject val = lrcread.get(i);
      if (oldval == null){
        oldval = val;
          }
      else {
        LrcObject item1 = new LrcObject();
        item1 = oldval;
        item1.timeline = val.begintime - oldval.begintime;
        lrcmap.add(item1);
        sb.append(String.format("[%04d]-[%04d]-%s\n", item1.begintime,
            item1.timeline, item1.lrc));
        i++;
        oldval = val;
      }
    }
    // data = sb.toString();
    return lrcmap;
  }

  // Added by Dan for fix bug DTV00389330
  public boolean isInLrcOffsetMenu() {
    return mIsInLrcOffsetMenu;
  }

  public boolean isInEncodingMenu() {
    return mIsInEncodingMenu;
  }
}
