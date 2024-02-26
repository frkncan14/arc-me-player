
package com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmc.IPlayList;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;

/**
 *
 * This class represents manager play list.
 *
 */
public final class PlayList implements IPlayList {
  private static final String TAG = "PlayList";

  // music play list
  private final List<String> backFileList;

  // music shuffle list
  private final List<String> backShuffleFileList;

  // video play list
  private final List<String> foreVideoFileList;

  // video shuffle list
  private final List<String> foreShuffleVideoFileList;

  // image text play list
  private final List<String> foreFileList;

  // image text shuffle list
  private final List<String> foreShuffleFileList;

  private final List<Boolean> markFileList;

  private static PlayList pl = null;

  private final int curBackVideoIdx;
  private int curForeVideoIdx;

  private int curBackIdx;
  private int curForeIdx;

  private int vidRepeatMode;
  private int audRepeatMode;
  private int textRepeatMode;
  private int picRepeatMode;

  private boolean vidShuffleMode;
  private boolean audShuffleMode;
  private boolean textShuffleMode;
  private boolean picShuffleMode;

  // private boolean audMarkMode;

  private int PlayFileType = Const.FILE_TYPE_USB;

  public int getSource() {
    return PlayFileType;
  }

  private PlayList() {
    backFileList = new ArrayList<String>();
    foreFileList = new ArrayList<String>();
    foreVideoFileList = new ArrayList<String>();
    backShuffleFileList = new ArrayList<String>();
    foreShuffleFileList = new ArrayList<String>();
    foreShuffleVideoFileList = new ArrayList<String>();
    markFileList = new ArrayList<Boolean>();

    curBackIdx = -1;
    curForeIdx = -1;
    curBackVideoIdx = -1;
    curForeVideoIdx = -1;

    vidShuffleMode = Const.SHUFFLE_OFF;
    audShuffleMode = Const.SHUFFLE_OFF;
    textShuffleMode = Const.SHUFFLE_OFF;
    picShuffleMode = Const.SHUFFLE_OFF;

    vidRepeatMode = Const.REPEAT_NONE;
    audRepeatMode = Const.REPEAT_NONE;
    textRepeatMode = Const.REPEAT_NONE;
    picRepeatMode = Const.REPEAT_NONE;
  }

  /**
   * Get the playlist instance.
   *
   * @return
   */
  public static synchronized PlayList getPlayList() {
    if (pl == null) {
        pl = new PlayList();
    }
    return pl;
  }

  /**
   * Add files into playlist by specified filter type and file type.
   *
   * @param filterType
   *            filter type.
   * @param fileType
   *            , file type. photo, text, audio, video.
   * @param files
   *            , source files.
   */
  @Override
  public boolean addFiles(int filterType, int fileType, List<String> files) {
    // TODO Auto-generated method stub
    boolean retVal = true;

    if (files.isEmpty()) {
      return false;
    }

    PlayFileType = fileType;

    cleanList(filterType);

    switch (filterType) {
      case Const.FILTER_AUDIO:

        backFileList.addAll(0, files);
        markFileList.clear();
        for (int i = 0; i < backFileList.size(); i++) {
          markFileList.add(i, false);
        }

        if (curBackIdx < 0 || curBackIdx > backFileList.size() - 1) {
          curBackIdx = 0;
        }
        break;

      case Const.FILTER_VIDEO:
        foreVideoFileList.addAll(0, files);
        if (curForeVideoIdx < 0 || curForeVideoIdx > foreVideoFileList.size() - 1) {
          curForeVideoIdx = 0;
        }
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:

        foreFileList.addAll(0, files);

        if (curForeIdx < 0 || curForeIdx > foreFileList.size() - 1) {
          curForeIdx = 0;
        }
        break;
      default:
        retVal = false;
        break;
    }

    setShuffleMode(filterType);

    return retVal;
  }

  /**
   * Get current shuffle index by sepcifed type.
   *
   * @param filterType
   *            filter type, photo, text, video, audio.
   * @return index.
   */
  public int getCurShuffleIndex(int filterType) {
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (audShuffleMode){
          return curBackIdx;
        }
        break;
      case Const.FILTER_VIDEO:
        if (vidShuffleMode){
          return curForeIdx;
        }
        break;
      case Const.FILTER_IMAGE:
        if (picShuffleMode){
          return curForeIdx;
        }
        break;
      case Const.FILTER_TEXT:
        if (textShuffleMode){
          return curForeIdx;
        }
        break;
      default:
        break;
    }
    return -1;
  }

  /**
   * Get current normal index by sepcified type.
   *
   * @param filterType
   *            filter type, photo, text, video, audio.
   * @return index.
   */
  @Override
  public int getCurrentIndex(int filterType) {
    // TODO Auto-generated method stub
    int fileCurrent = -1;
    String curFileName;

    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (audShuffleMode) {
          curFileName = backShuffleFileList.get(curBackIdx);
          for (int i = 0; i < backShuffleFileList.size(); i++) {
            if (0 == curFileName.compareTo(backFileList.get(i))) {
              fileCurrent = i;
              break;
            }
          }
        } else {
          fileCurrent = curBackIdx;
        }
        break;

      case Const.FILTER_VIDEO:
        if (vidShuffleMode) {
          curFileName = foreShuffleVideoFileList.get(curForeVideoIdx);
          for (int i = 0; i < foreShuffleVideoFileList.size(); i++) {
            if (0 == curFileName.compareTo(foreVideoFileList.get(i))) {
              fileCurrent = i;
              break;
            }
          }
        } else {
          fileCurrent = curForeVideoIdx;
        }
        break;

      case Const.FILTER_IMAGE:
        if (picShuffleMode) {
          curFileName = foreShuffleFileList.get(curForeIdx);
          for (int i = 0; i < foreShuffleFileList.size(); i++) {
            if (0 == curFileName.compareTo(foreFileList.get(i))) {
              fileCurrent = i;
              break;
            }
          }
        } else {
          fileCurrent = curForeIdx;
        }
        break;

      case Const.FILTER_TEXT:
        if (textShuffleMode) {
          curFileName = foreShuffleFileList.get(curForeIdx);
          for (int i = 0; i < foreShuffleFileList.size(); i++) {
            if (0 == curFileName.compareTo(foreFileList.get(i))) {
              fileCurrent = i;
              break;
            }
          }
        } else {
          fileCurrent = curForeIdx;
        }
        break;

      default:
        return -1;
    }
    return fileCurrent;
  }

  /**
   * Get next index by sepcified type.
   *
   * @param filterType
   *            filter type, photo, text, video, audio.
   * @return index.
   */
  public int getNextIndex(final int filterType,final int playMode) {
    int tmpIndex = curForeIdx;

    switch (filterType) {
      case Const.FILTER_AUDIO:
        break;

      case Const.FILTER_VIDEO:
        break;

      case Const.FILTER_IMAGE:
        if (picRepeatMode == Const.REPEAT_ONE) {
          break;
        }
        if (Const.MANUALPRE == playMode) {
          if (picShuffleMode == Const.SHUFFLE_ON) {
            // Const.SHUFFLE_ON
            tmpIndex--;
            if (0 > tmpIndex) {
              if (picRepeatMode == Const.REPEAT_ALL) {
                tmpIndex = foreShuffleFileList.size() - 1;
              } else {
                tmpIndex = 0;
              }
            }
          } else {
            // Const.SHUFFLE_OFF
            tmpIndex--;
            if (0 > tmpIndex) {
              if (picRepeatMode == Const.REPEAT_ALL) {
                tmpIndex = foreFileList.size() - 1;
              } else {
                tmpIndex = 0;
              }
            }
          }
        } else if (Const.MANUALNEXT == playMode) {
          if (picShuffleMode == Const.SHUFFLE_ON) {
            // Const.SHUFFLE_ON
            tmpIndex++;
            if (tmpIndex >= foreShuffleFileList.size()) {
              if (picRepeatMode == Const.REPEAT_ALL) {
                tmpIndex = 0;
              } else {
                tmpIndex = foreShuffleFileList.size() - 1;
              }
            }
          } else {
            // Const.SHUFFLE_OFF
            tmpIndex++;
            if (tmpIndex >= foreFileList.size()) {
              if (picRepeatMode == Const.REPEAT_ALL) {
                tmpIndex = 0;
              } else {
                tmpIndex = foreFileList.size() - 1;
              }
            }
          }
        } // if (Const.MANUALPRE == playMode)
        break;

      case Const.FILTER_TEXT:
        break;

      default:
    }

    return tmpIndex;
  }

  /**
   * Set current normal index by sepcified type.
   *
   * @param filterType
   *            filter type, photo, text, video, audio.
   * @param index
   *            .
   * @return true to successful, false to failed.
   */
  @Override
  public boolean setCurrentIndex(int filterType, int index) {
    boolean retVal = true;
    if (index < 0) {
      return false;
    }
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (index >= backFileList.size()) {
          retVal = false;
          break;
        }
        if (audShuffleMode == Const.SHUFFLE_ON) {
          for (int i = 0; i < backFileList.size(); i++) {
            if (backFileList.get(index).equals(
                backShuffleFileList.get(i))) {
              backShuffleFileList.set(i, backShuffleFileList.get(0));
              backShuffleFileList.set(0, backFileList.get(index));
              curBackIdx = 0;
              break;
            }
          }
        } else {
          curBackIdx = index;
        }
        break;
      case Const.FILTER_VIDEO:
        if (index >= foreVideoFileList.size()) {
          retVal = false;
          break;
        }
        if (vidShuffleMode == Const.SHUFFLE_ON) {
          for (int i = 0; i < foreVideoFileList.size(); i++) {
            if (foreVideoFileList.get(index).equals(
                foreShuffleVideoFileList.get(i))) {
              foreShuffleVideoFileList.set(i, foreShuffleVideoFileList.get(0));
              foreShuffleVideoFileList.set(0, foreVideoFileList.get(index));
              curForeVideoIdx = 0;
              break;
            }
          }
        } else {
          curForeVideoIdx = index;
        }
        break;
      case Const.FILTER_IMAGE:
        if (index >= foreFileList.size()) {
          retVal = false;
          break;
        }
        if (picShuffleMode == Const.SHUFFLE_ON) {
          for (int i = 0; i < foreFileList.size(); i++) {
            if (foreFileList.get(index).equals(
                foreShuffleFileList.get(i))) {
              foreShuffleFileList.set(i, foreShuffleFileList.get(0));
              foreShuffleFileList.set(0, foreFileList.get(index));
              curForeIdx = 0;
              break;
            }
          }
        } else {
          curForeIdx = index;
        }
        break;
      case Const.FILTER_TEXT:
        if (index >= foreFileList.size()) {
          retVal = false;
          break;
        }
        if (textShuffleMode == Const.SHUFFLE_ON) {
          for (int i = 0; i < foreFileList.size(); i++) {
            if (foreFileList.get(index).equals(
                foreShuffleFileList.get(i))) {
              foreShuffleFileList.set(i, foreShuffleFileList.get(0));
              foreShuffleFileList.set(0, foreFileList.get(index));
              curForeIdx = 0;
              break;
            }
          }
        } else {
          curForeIdx = index;
        }
        break;
      default:
        retVal = false;
    }
    return retVal;
  }

  /**
   * Get file number in current playlist by sepcified type.
   *
   * @param filterType
   *            filter type, photo, text, video, audio.
   * @return the files number
   */
  @Override
  public int getFileNum(int filterType) {
    // TODO Auto-generated method stub
    int num;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        num = backFileList.size();
        break;

      case Const.FILTER_VIDEO:
        num = foreVideoFileList.size();
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        num = foreFileList.size();
        break;

      default:
        num = -1;
        break;
    }
    return num;
  }

  private int markCyclePre(List<String> fl, int currentIndex) {
    int retVal = -1;
    int tempIndex;

    if (PlayFileType != Const.FILE_TYPE_USB) {
      return retVal;
    }

    if (currentIndex <= 0) {
      tempIndex = fl.size() - 1;
    } else {
      tempIndex = currentIndex - 1;
    }

    if (fl.equals(backFileList) /* fl == backFileList */) {
      while (tempIndex != currentIndex) {
        if (markFileList.get(tempIndex)) {
          retVal = tempIndex;
          break;
        }
        if (tempIndex <= 0) {
          tempIndex = fl.size() - 1;
        } else {
          --tempIndex;
        }
      }
    } else if (fl.equals(foreFileList)
        || fl.equals(foreVideoFileList) /* fl == foreFileList */) {
      while (tempIndex != currentIndex) {
        if (markFileList.get(tempIndex)) {
          retVal = tempIndex;
          break;
        }
        if (tempIndex <= 0) {
          tempIndex = fl.size() - 1;
        } else {
          --tempIndex;
        }
      }
    }
    return retVal;
  }

  private int markCycleNext(List<String> fl, int currentIndex) {
    int retVal = -1;
    int tempIndex;

    if (PlayFileType != Const.FILE_TYPE_USB) {
      return retVal;
    }

    if (currentIndex >= fl.size() - 1) {
      tempIndex = 0;
    } else {
      tempIndex = currentIndex + 1;
    }

    if (fl.equals(backFileList) /* fl == backFileList */) {
      while (tempIndex != currentIndex) {
        if (markFileList.get(tempIndex)) {
          retVal = tempIndex;
          break;
        }
        if (tempIndex >= fl.size() - 1) {
          tempIndex = 0;
        } else {
          ++tempIndex;
        }
      }
    } else if (fl.equals(foreFileList)
        || fl.equals(foreVideoFileList) /* fl == foreFileList */) {
      while (tempIndex != currentIndex) {
        if (markFileList.get(tempIndex)) {
          retVal = tempIndex;
          break;
        }
        if (tempIndex >= fl.size() - 1) {
          tempIndex = 0;
        } else {
          ++tempIndex;
        }
      }
    } else if (fl.equals(foreShuffleFileList)
        || fl.equals(foreShuffleVideoFileList)/* fl == foreShuffleFileList */) {
      while (tempIndex != currentIndex) {
        if (markFileList.get(tempIndex)) {
          retVal = tempIndex;
          break;
        }
        if (tempIndex >= fl.size() - 1) {
          tempIndex = 0;
        } else {
          ++tempIndex;
        }
      }
    } else if (fl.equals(backShuffleFileList) /* fl == backShuffleFileList */) {
      while (tempIndex != currentIndex) {
        if (markFileList.get(tempIndex)) {
          retVal = tempIndex;
          break;
        }
        if (tempIndex >= fl.size() - 1) {
          tempIndex = 0;
        } else {
          ++tempIndex;
        }
      }
    }
    return retVal;
  }

  private int getNextFile(List<String> fl, int mode, int currentIndex) {
    int num;
    if (currentIndex < 0 || currentIndex > fl.size() - 1) {
      return -1;
    }
    switch (mode) {
      case Const.REPEAT_ALL:
        if (currentIndex >= fl.size() - 1) {
          num = 0;
        } else {
          num = currentIndex + 1;
        }
        break;
      case Const.REPEAT_ONE:
        num = currentIndex;
        break;

      case Const.REPEAT_MARKED:
        // add code later
        num = markCycleNext(fl, currentIndex);
        break;
      case Const.REPEAT_NONE:
        if (currentIndex >= fl.size() - 1) {
          num = -1;
        } else {
          num = currentIndex + 1;
        }
        break;
      default:
        num = -1;
        break;
    }
    return num;
  }

  private String autoPlay(int filterType) {
    String retFile = null;
    int retIndex;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (audShuffleMode == Const.SHUFFLE_ON) {
          if (curBackIdx < backShuffleFileList.size() - 1) {
            if (audRepeatMode != Const.REPEAT_ONE){
              curBackIdx++;
            }
          } else {
            if (audRepeatMode == Const.REPEAT_ALL) {
              curBackIdx = 0;
            } else if (audRepeatMode == Const.REPEAT_NONE
                || audRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = backShuffleFileList.get(curBackIdx);
        } else {
          retIndex = getNextFile(backFileList, audRepeatMode, curBackIdx);

          if (retIndex == -1) {
            break;
          }

          curBackIdx = retIndex;

          retFile = backFileList.get(curBackIdx);
        }
        break;
      case Const.FILTER_VIDEO:
        if (vidShuffleMode == Const.SHUFFLE_ON) {
          if (curForeVideoIdx < foreShuffleVideoFileList.size() - 1) {
            if (vidRepeatMode != Const.REPEAT_ONE){
              curForeVideoIdx++;
            }
          } else {
            if (vidRepeatMode == Const.REPEAT_ALL) {
              curForeVideoIdx = 0;
            } else if (vidRepeatMode == Const.REPEAT_NONE
                || vidRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = foreShuffleVideoFileList.get(curForeVideoIdx);
        } else {
          retIndex = getNextFile(foreVideoFileList, vidRepeatMode, curForeVideoIdx);

          if (retIndex == -1) {

            break;

          }

          curForeVideoIdx = retIndex;

          retFile = foreVideoFileList.get(curForeVideoIdx);
        }
        break;
      case Const.FILTER_IMAGE:
        if (picShuffleMode == Const.SHUFFLE_ON) {
          if (curForeIdx < foreShuffleFileList.size() - 1) {
            if (picRepeatMode != Const.REPEAT_ONE){
              curForeIdx++;
            }
          } else {
            if (picRepeatMode == Const.REPEAT_ALL) {
              curForeIdx = 0;
            } else if (picRepeatMode == Const.REPEAT_NONE
                || picRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = foreShuffleFileList.get(curForeIdx);
        } else {
          retIndex = getNextFile(foreFileList, picRepeatMode, curForeIdx);

          if (retIndex == -1) {

            break;

          }

          curForeIdx = retIndex;

          retFile = foreFileList.get(curForeIdx);
        }
        break;
      case Const.FILTER_TEXT:
        if (textShuffleMode == Const.SHUFFLE_ON) {
          if (curForeIdx < foreShuffleFileList.size() - 1) {
            if (textRepeatMode != Const.REPEAT_ONE){
              curForeIdx++;
            }
          } else {
            if (textRepeatMode == Const.REPEAT_ALL) {
              curForeIdx = 0;
            } else if (textRepeatMode == Const.REPEAT_NONE
                || textRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = foreShuffleFileList.get(curForeIdx);
        } else {
          retIndex = getNextFile(foreFileList, textRepeatMode, curForeIdx);

          if (retIndex == -1) {

            break;

          }

          curForeIdx = retIndex;

          retFile = foreFileList.get(curForeIdx);
        }
        break;
      default:
        break;
    }
    return retFile;
  }

  private void setShuffleMode(int filterType) {

    switch (filterType) {
      case Const.FILTER_AUDIO:
        backShuffleFileList.clear();
        backShuffleFileList.addAll(0, backFileList);
        Collections.shuffle(backShuffleFileList);

        for (int i = 0; i < backFileList.size(); i++) {
          if (backFileList.get(curBackIdx).equals(
              backShuffleFileList.get(i))) {
            backShuffleFileList.set(i, backShuffleFileList.get(0));
            backShuffleFileList.set(0, backFileList.get(curBackIdx));
            curBackIdx = 0;
            break;
          }
        }

        break;

      case Const.FILTER_VIDEO:
        foreShuffleVideoFileList.clear();
        foreShuffleVideoFileList.addAll(0, foreVideoFileList);
        Collections.shuffle(foreShuffleVideoFileList);

        String path1 = foreVideoFileList.get(curForeVideoIdx);
        int index1 = foreShuffleVideoFileList.indexOf(path1);
        if (index1 >= 0)
        {
          try {
            foreShuffleVideoFileList.set(index1, foreShuffleVideoFileList.get(curForeVideoIdx));
            foreShuffleVideoFileList.set(curForeVideoIdx, path1);
          } catch (Exception e) {
            Log.e("PlayList", "setShuffleMode:" + e.getMessage());
          }
        }
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        foreShuffleFileList.clear();
        foreShuffleFileList.addAll(0, foreFileList);
        Collections.shuffle(foreShuffleFileList);

        String path = foreFileList.get(curForeIdx);
        int index = foreShuffleFileList.indexOf(path);
        if (index >= 0)
        {
          try {
            foreShuffleFileList.set(index, foreShuffleFileList.get(curForeIdx));
            foreShuffleFileList.set(curForeIdx, path);
          } catch (Exception e) {
            Log.e("PlayList", "setShuffleMode:" + e.getMessage());
          }
        }
        break;

      default:
        break;
    }

    // if (filterType == Const.FILTER_AUDIO) {
    // for (int i = 0; i < backFileList.size(); i++) {
    // if (backFileList.get(curBackIdx).equals(
    // backShuffleFileList.get(i))) {
    // backShuffleFileList.set(i, backShuffleFileList.get(0));
    // backShuffleFileList.set(0, backFileList.get(curBackIdx));
    // curBackIdx = 0;
    // break;
    // }
    // }
    // }
  }

  /**
   * Get next file path in current playlist by sepcified filter type and play
   * mode.
   *
   * @param filterType
   * @param playMode
   * @return path
   */
  @Override
  public String getNext(int filterType, int playMode) {
    // TODO Auto-generated method stub
    String retFile = null;
    switch (playMode) {
      case Const.AUTOPLAY:
        retFile = autoPlay(filterType);
        break;
      case Const.MANUALPRE:
        retFile = playPre(filterType);
        break;
      case Const.MANUALNEXT:
        retFile = playNext(filterType);
        break;
      default:
        break;
    }
    return retFile;
  }

  private void shuffle(String path, List<String> list) {

    if (list.size() <= 1) {
      return;
    }
    do {
      Collections.shuffle(list);
    } while (path.equals(list.get(0)));
  }

  /**
   * according to the parameters play next file
   *
   * @param filterType
   * @return string play next
   */
  public String playNext(int filterType) {
    // TODO Auto-generated method stub
    int temp;
    String retFile = null;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (audShuffleMode == Const.SHUFFLE_ON) {
          if (curBackIdx < backShuffleFileList.size() - 1) {
            if (audRepeatMode != Const.REPEAT_ONE){
              curBackIdx++;
            }
          } else {
            if (audRepeatMode == Const.REPEAT_ALL) {
              shuffle(backShuffleFileList.get(curBackIdx),
                  backShuffleFileList);
              curBackIdx = 0;
            } else if (audRepeatMode == Const.REPEAT_NONE
                || audRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = backShuffleFileList.get(curBackIdx);
          break;
        } else if (audRepeatMode == Const.REPEAT_MARKED) {
          temp = markCycleNext(backFileList, curBackIdx);
          if (temp == -1) {
            retFile = null;
          } else {
            retFile = backFileList.get(temp);
            curBackIdx = temp;
          }
          break;
        } else if (audRepeatMode == Const.REPEAT_ONE) {
          retFile = backFileList.get(curBackIdx);
          break;
        }

        if (curBackIdx >= backFileList.size() - 1) {
          if (audRepeatMode == Const.REPEAT_ALL) {
            curBackIdx = 0;
          } else {
            break;
          }
        } else {
          curBackIdx++;
        }
        retFile = backFileList.get(curBackIdx);
        break;
      case Const.FILTER_VIDEO:
        if (vidShuffleMode == Const.SHUFFLE_ON) {

          if (curForeVideoIdx < foreShuffleVideoFileList.size() - 1) {
            if (vidRepeatMode != Const.REPEAT_ONE){
              curForeVideoIdx++;
            }
          } else {
            if (vidRepeatMode == Const.REPEAT_ALL) {
              shuffle(foreShuffleVideoFileList.get(curBackVideoIdx),
                  foreShuffleVideoFileList);
              curForeVideoIdx = 0;
            } else if (vidRepeatMode == Const.REPEAT_NONE
                || vidRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = foreShuffleVideoFileList.get(curForeVideoIdx);
          break;
        } else if (vidRepeatMode == Const.REPEAT_ONE) {
          retFile = foreVideoFileList.get(curForeVideoIdx);
          break;
        }

        if (curForeVideoIdx >= foreVideoFileList.size() - 1) {
          if (vidRepeatMode == Const.REPEAT_ALL) {
            curForeVideoIdx = 0;
          } else {
            break;
          }
        } else {
          curForeVideoIdx++;
        }
        retFile = foreVideoFileList.get(curForeVideoIdx);
        break;
      case Const.FILTER_TEXT:
        if (textShuffleMode == Const.SHUFFLE_ON) {
          if (curForeIdx < foreShuffleFileList.size() - 1) {
            if (textRepeatMode != Const.REPEAT_ONE){
              curForeIdx++;
            }
          } else {
            if (textRepeatMode == Const.REPEAT_ALL) {
              shuffle(foreShuffleFileList.get(curForeIdx),
                  foreShuffleFileList);
              curForeIdx = 0;
            } else if (textRepeatMode == Const.REPEAT_NONE
                || textRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = foreShuffleFileList.get(curForeIdx);
          break;
        } else if (textRepeatMode == Const.REPEAT_ONE) {
          retFile = foreFileList.get(curForeIdx);
          break;
        }
        if (curForeIdx >= foreFileList.size() - 1) {
          if (textRepeatMode == Const.REPEAT_ALL) {
            curForeIdx = 0;
          } else {
            break;
          }
        } else {
          curForeIdx++;
        }
        retFile = foreFileList.get(curForeIdx);
        break;
      case Const.FILTER_IMAGE:
        if (picShuffleMode == Const.SHUFFLE_ON) {
          if (curForeIdx < foreShuffleFileList.size() - 1) {
            if (picRepeatMode != Const.REPEAT_ONE){
              curForeIdx++;
            }
          } else {
            if (picRepeatMode == Const.REPEAT_ALL) {
              shuffle(foreShuffleFileList.get(curForeIdx),
                  foreShuffleFileList);
              curForeIdx = 0;
            } else if (picRepeatMode == Const.REPEAT_NONE
                || picRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }

          retFile = foreShuffleFileList.get(curForeIdx);
          break;
        } else if (picRepeatMode == Const.REPEAT_MARKED) {
          temp = markCycleNext(foreFileList, curForeIdx);
          if (temp == -1) {
            retFile = null;
          } else {
            retFile = foreFileList.get(temp);
            curForeIdx = temp;
          }
          break;
        } else if (picRepeatMode == Const.REPEAT_ONE) {
          retFile = foreFileList.get(curForeIdx);
          break;
        }
        if (curForeIdx >= foreFileList.size() - 1) {
          if (picRepeatMode == Const.REPEAT_ALL) {
            curForeIdx = 0;
          } else {
            break;
          }
        } else {
          curForeIdx++;
        }
        retFile = foreFileList.get(curForeIdx);
        break;
      default:
        break;
    }
    return retFile;
  }

  private String playPre(int filterType) {
    // TODO Auto-generated method stub
    int temp;
    String retFile = null;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (audShuffleMode == Const.SHUFFLE_ON) {

          if (curBackIdx < backShuffleFileList.size() - 1) {
            if (audRepeatMode != Const.REPEAT_ONE){
              curBackIdx++;
            }
          } else {
            if (audRepeatMode == Const.REPEAT_ALL) {
              // fix bug DTV00405809
              shuffle(backShuffleFileList.get(curBackIdx),
                  backShuffleFileList);
              curBackIdx = 0;
            } else if (audRepeatMode == Const.REPEAT_NONE
                || audRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }
          retFile = backShuffleFileList.get(curBackIdx);
          break;
        } else if (audRepeatMode == Const.REPEAT_MARKED) {
          temp = markCyclePre(backFileList, curBackIdx);
          if (temp == -1) {
            retFile = null;
          } else {
            retFile = backFileList.get(temp);
            curBackIdx = temp;
          }
          break;
        } else if (audRepeatMode == Const.REPEAT_ONE) {
          retFile = backFileList.get(curBackIdx);
          break;
        }
        if (curBackIdx <= 0) {
          if (audRepeatMode == Const.REPEAT_ALL) {
            curBackIdx = backFileList.size() - 1;
          } else {
            break;
          }
        } else {
          curBackIdx--;
        }
        retFile = backFileList.get(curBackIdx);
        break;
      case Const.FILTER_VIDEO:
        if (vidShuffleMode == Const.SHUFFLE_ON) {
          if (curForeVideoIdx < foreShuffleVideoFileList.size() - 1) {
            if (vidRepeatMode != Const.REPEAT_ONE){
              curForeVideoIdx++;
            }
          } else {
            if (vidRepeatMode == Const.REPEAT_ALL) {
              shuffle(foreShuffleVideoFileList.get(curForeVideoIdx),
                  foreShuffleVideoFileList);
              curForeVideoIdx = 0;
            } else if (vidRepeatMode == Const.REPEAT_NONE
                || vidRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }

          retFile = foreShuffleVideoFileList.get(curForeVideoIdx);
          break;
        } else if (vidRepeatMode == Const.REPEAT_ONE) {
          retFile = foreVideoFileList.get(curForeVideoIdx);
          break;
        }
        if (curForeVideoIdx <= 0) {
          if (vidRepeatMode == Const.REPEAT_ALL) {
            curForeVideoIdx = foreVideoFileList.size() - 1;
          } else {
            break;
          }
        } else {
          curForeVideoIdx--;
        }
        retFile = foreVideoFileList.get(curForeVideoIdx);
        break;
      case Const.FILTER_TEXT:
        if (textShuffleMode == Const.SHUFFLE_ON) {
          if (curForeIdx < foreShuffleFileList.size() - 1) {
            if (textRepeatMode != Const.REPEAT_ONE){
              curForeIdx++;
            }
          } else {
            if (textRepeatMode == Const.REPEAT_ALL) {
              shuffle(foreShuffleFileList.get(curForeIdx),
                  foreShuffleFileList);
              curForeIdx = 0;
            } else if (textRepeatMode == Const.REPEAT_NONE
                || textRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }

          retFile = foreShuffleFileList.get(curForeIdx);
          break;
        } else if (textRepeatMode == Const.REPEAT_ONE) {
          retFile = foreFileList.get(curForeIdx);
          break;
        }
        if (curForeIdx <= 0) {
          if (textRepeatMode == Const.REPEAT_ALL) {
            curForeIdx = foreFileList.size() - 1;
          } else {
            break;
          }
        } else {
          curForeIdx--;
        }
        retFile = foreFileList.get(curForeIdx);
        break;
      case Const.FILTER_IMAGE:
        if (picShuffleMode == Const.SHUFFLE_ON) {
          if (curForeIdx < foreShuffleFileList.size() - 1) {
            if (picRepeatMode != Const.REPEAT_ONE){
              curForeIdx++;
            }
          } else {
            if (picRepeatMode == Const.REPEAT_ALL) {
              shuffle(foreShuffleFileList.get(curForeIdx),
                  foreShuffleFileList);
              curForeIdx = 0;
            } else if (picRepeatMode == Const.REPEAT_NONE
                || picRepeatMode == Const.REPEAT_MARKED) {
              break;
            }
          }

          retFile = foreShuffleFileList.get(curForeIdx);

          break;
        } else if (picRepeatMode == Const.REPEAT_MARKED) {
          temp = markCyclePre(foreFileList, curForeIdx);
          if (temp == -1) {
            retFile = null;
          } else {
            retFile = foreFileList.get(temp);
            curForeIdx = temp;
          }
          break;
        } else if (picRepeatMode == Const.REPEAT_ONE) {
          retFile = foreFileList.get(curForeIdx);
          break;
        }
        if (curForeIdx <= 0) {
          if (picRepeatMode == Const.REPEAT_ALL) {
            curForeIdx = foreFileList.size() - 1;
          } else {
            break;
          }
        } else {
          curForeIdx--;
        }
        retFile = foreFileList.get(curForeIdx);
        break;
      default:
        break;
    }
    return retFile;
  }

  /**
   * Remove a file from playlist by sepcified type and index.
   *
   * @param
   * @param
   */
  @Override
  public String removeFile(int filterType, int index) {
    String retFile = null;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (index < 0 || index > backFileList.size() - 1) {
          break;
        }
        retFile = backFileList.remove(index);
        if (curBackIdx >= index) {
          curBackIdx--;
        }
        break;
      case Const.FILTER_VIDEO:
        if (index < 0 || index > foreVideoFileList.size() - 1) {
          break;
        }
        retFile = foreVideoFileList.remove(index);
        if (curForeVideoIdx >= index) {
          curForeVideoIdx--;
        }
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        if (index < 0 || index > foreFileList.size() - 1) {
          break;
        }
        retFile = foreFileList.remove(index);
        if (curForeIdx >= index) {
          curForeIdx--;
        }
        break;
      default:
        break;
    }
    return retFile;
  }

  /**
   * set repeat mode
   *
   * @param fileType
   *            file type
   * @param mode
   *            repeat mode
   * @return return true if success,return false if fail
   */
  @Override
  public boolean setRepeatMode(int filterType, int mode) {
    switch (filterType) {
      case Const.FILTER_AUDIO:
        audRepeatMode = mode;
        break;
      case Const.FILTER_VIDEO:
        vidRepeatMode = mode;
        break;
      case Const.FILTER_TEXT:
        textRepeatMode = mode;
        break;
      case Const.FILTER_IMAGE:
        picRepeatMode = mode;
        break;
      default:
        return false;
    }

    return true;
  }

  /**
   * get repeat mode
   *
   * @param fileType
   *            file type
   * @return play mode ,return -1 if failed
   */
  @Override
  public int getRepeatMode(int filterType) {
    // TODO Auto-generated method stub
    int ret;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        ret = audRepeatMode;
        break;
      case Const.FILTER_VIDEO:
        ret = vidRepeatMode;
        break;
      case Const.FILTER_TEXT:
        ret = textRepeatMode;
        break;
      case Const.FILTER_IMAGE:
        ret = picRepeatMode;
        break;
      default:
        ret = -1;
        break;
    }
    return ret;
  }

  /**
   * Set shuffle play mode.
   *
   * @param filterType
   *            , filter type.
   * @param mode
   *            , true to shuffle, false to normal.
   */
  @Override
  public void setShuffleMode(int filterType, boolean mode) {
    switch (filterType) {
      case Const.FILTER_AUDIO:
        audShuffleMode = mode;
        if (mode == Const.SHUFFLE_OFF) {
          for (int i = 0; i < backShuffleFileList.size(); i++) {
            if (backShuffleFileList.get(curBackIdx).equals(
                backFileList.get(i))) {
              curBackIdx = i;
              break;
            }
          }
        }
        break;
      case Const.FILTER_VIDEO:
        vidShuffleMode = mode;
        break;
      case Const.FILTER_TEXT:
        textShuffleMode = mode;
        break;
      case Const.FILTER_IMAGE:
        picShuffleMode = mode;
        break;
      default:
        break;
    }
    if (filterType != Const.FILTER_AUDIO) {
      if (mode == Const.SHUFFLE_OFF) {
        for (int i = 0; i < foreShuffleFileList.size(); i++) {
          if (foreShuffleFileList.get(curForeIdx).equals(
              foreFileList.get(i))) {
            curForeIdx = i;
            break;
          }
        }
      }
    }
    if (mode == Const.SHUFFLE_ON) {
      setShuffleMode(filterType);
    }
  }

  /**
   * Get shuffle play mode.
   *
   * @param filterType
   *            , filter type.
   * @return mode, true to shuffle, false to normal.
   */
  @Override
  public boolean getShuffleMode(int filterType) {
    // TODO Auto-generated method stub
    boolean ret;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        ret = audShuffleMode;
        break;
      case Const.FILTER_VIDEO:
        ret = vidShuffleMode;
        break;
      case Const.FILTER_TEXT:
        ret = textShuffleMode;
        break;
      case Const.FILTER_IMAGE:
        ret = picShuffleMode;
        break;
      default:
        ret = false;
        break;
    }
    return ret;
  }

  /*
   * public void setMarkMode(int filterType, boolean mode) { switch (filterType) { case
   * Const.FILTER_AUDIO: audMarkMode = mode; break; case Const.FILTER_IMAGE: default: break; } }
   * public boolean getMarkMode(int filterType) { switch (filterType) { case Const.FILTER_AUDIO:
   * return audMarkMode; case Const.FILTER_IMAGE: default: break; } return false; }
   */
  /**
   * Clean playlist by sepcified type.
   *
   * @param filter
   *            type.
   */
  @Override
  public void cleanList(int filterType) {
    switch (filterType) {
      case Const.FILTER_AUDIO:
        backFileList.clear();
        curBackIdx = -1;
        break;
      case Const.FILTER_VIDEO:
        foreVideoFileList.clear();
        curForeVideoIdx = -1;
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        foreFileList.clear();
        curForeIdx = -1;
        break;
      default:
        break;
    }
  }

  /**
   * Get playlist by sepcified type.
   *
   * @param filter
   *            type.
   */
  @Override
  public List<String> getList(int filterType) {
    switch (filterType) {
      case Const.FILTER_AUDIO:
        return backFileList;
      case Const.FILTER_VIDEO:
        return foreVideoFileList;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        return foreFileList;
      default:
        break;
    }
    return null;
  }

  /**
   * get file in playlist accroding to index
   *
   * @param fileType
   *            file type
   * @param index
   *            index in playlist
   * @return file,return null if failed
   * @deprecated
   */
  @Deprecated
  @Override
  public String getFile(int filterType, int index) {
    // TODO Auto-generated method stub
    String retFile = null;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (index < 0 || index > backFileList.size() - 1) {
          break;
        }
        retFile = backFileList.get(index);
        break;
      case Const.FILTER_VIDEO:
        if (index < 0 || index > foreVideoFileList.size() - 1) {
          break;
        }
        retFile = foreVideoFileList.get(index);
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        if (index < 0 || index > foreFileList.size() - 1) {
          break;
        }
        retFile = foreFileList.get(index);
        break;
      default:
        break;
    }
    return retFile;
  }

  /**
   * judge some file is marded or not??only support audio and photo file
   *
   * @param fileType
   *            file type
   * @param index
   *            file index
   * @return true means has marded??false means unmarked
   */
  @Override
  public boolean getMark(int filterType, int index) {
    // TODO Auto-generated method stub
    boolean retVal = false;

    if (PlayFileType != Const.FILE_TYPE_USB) {
      return retVal;
    }

    switch (filterType) {
      case Const.FILTER_AUDIO:
        retVal = markFileList.get(index);
        break;
      case Const.FILTER_IMAGE:
      default:
        break;
    }
    return retVal;
  }

  /**
   * mark file in REPEAT_MARKED mode??only support audio and photo file
   *
   * @param fileType
   *            file type
   * @param index
   *            index
   * @param mark
   *            is need to mark. true means mark??false means unmark
   * @return return true if success,return false if fail
   */
  @Override
  public boolean setMark(int filterType, int index, boolean mark) {
    // TODO Auto-generated method stub
    boolean retVal = false;

    if (PlayFileType != Const.FILE_TYPE_USB) {
      return retVal;
    }

    switch (filterType) {
      case Const.FILTER_AUDIO:
        markFileList.set(index, mark);
        retVal = true;
        break;
      case Const.FILTER_IMAGE:
      default:
        break;
    }
    return retVal;
  }

  /**
   * Get current play file path by sepcified type.
   *
   * @param filter
   *            type.
   * @return path.
   */
  @Override
  public String getCurrentPath(int filterType) {
    // TODO Auto-generated method stub
    String retFile = null;

    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (curBackIdx == -1){
          break;
        }
        if (audShuffleMode) {
          retFile = backShuffleFileList.get(curBackIdx);
        } else {
          retFile = backFileList.get(curBackIdx);
        }
        break;

      case Const.FILTER_VIDEO:
        if (curForeVideoIdx == -1){
          break;
        }
        if (vidShuffleMode) {
          retFile = foreShuffleVideoFileList.get(curForeVideoIdx);
        } else {
          retFile = foreVideoFileList.get(curForeVideoIdx);
        }
        break;

      case Const.FILTER_IMAGE:
        if (curForeIdx == -1){
          break;
        }
        if (picShuffleMode) {
          retFile = foreShuffleFileList.get(curForeIdx);
        } else {
          retFile = foreFileList.get(curForeIdx);
        }
        break;

      case Const.FILTER_TEXT:
        if (curForeIdx == -1){
          break;
        }
        if (textShuffleMode) {
          retFile = foreShuffleFileList.get(curForeIdx);
        } else {
          retFile = foreFileList.get(curForeIdx);
        }
        break;

      default:
        break;
    }
    return retFile;
  }

  /**
   * Get file path by sepcified type and index.
   *
   * @param filter
   *            type.
   * @return path.
   */
  public String getFilePathByIndex(final int filterType, final int index) {
    String retFile = null;

    switch (filterType) {
      case Const.FILTER_AUDIO:
        break;

      case Const.FILTER_VIDEO:
        break;

      case Const.FILTER_IMAGE:
        if (picShuffleMode == Const.SHUFFLE_ON) {
          retFile = foreShuffleFileList.get(index);
        } else {
          retFile = foreFileList.get(index);
        }
        break;

      case Const.FILTER_TEXT:
        break;

      default:
        break;
    }
    return retFile;
  }
  /**
   * Get current play file name by sepcified type.
   *
   * @param filter
   *            type.
   * @return file name.
   */
  @Override
  public String getCurrentFileName(int filterType) {

    String path = getCurrentPath(filterType);
    MmpTool.logError("getCurrentFileName---path:" + path);
    String suffix = "";

    // if(PlayFileType == Const.FILE_TYPE_DLNA){
    // suffix = DLNAManager.getInstance().getDLNAFileSuffix(path);
    //
    // }
    if (path == null) {
      return "";
    }
    String temp[] = path.replaceAll("\\\\", "/").split("/");

    String fileName = "-";

    if (temp.length > 1) {
      fileName = temp[temp.length - 1];
    } else {
      fileName = path;
    }

    return fileName + suffix;
  }

  /**
   * Get current play file name by sepcified type.
   *
   * @param filter
   *			  type.
   * @return file name.
   */
  public long getCurrentFileSize(int filterType) {

    String path = getCurrentPath(filterType);
    MmpTool.logError("getCurrentFileSize---path:" + path);

    if (null == path || path.length() <= 0) {
      return 0;
    }

    long length = 0;

    try {
      switch (PlayFileType) {
        case Const.FILE_TYPE_DLNA:
          DLNADataSource dlnaDataSource = DLNAManager.getInstance().getDLNADataSource(path);
          if(dlnaDataSource != null) {
            length = dlnaDataSource.getContent().getSize();
          }
          break;
        case Const.FILE_TYPE_USB:
          File file = new File(path);
          length = file.length();
          break;
        case Const.FILE_TYPE_SMB:
          length = SambaManager.getInstance().size(path);
          break;
        default:
          break;

      }
    } catch (Exception e) {
      e.printStackTrace();

    }
    Log.d(TAG, "getCurrentFileSize path = " + path + "length = " + length);
    return length;
  }

  /**
   * Get next play file name by sepcified type.
   *
   * @param filter
   *            type.
   * @return file name.
   */
  public String getNextFileName(int filterType) {
    // TODO Auto-generated method stub
    int temp;
    int tmpIndex;
    String retFile = null;
    tmpIndex = curForeIdx;

    switch (filterType) {
      case Const.FILTER_AUDIO:
        tmpIndex = curBackIdx;
        if (audShuffleMode == Const.SHUFFLE_ON) {
          if (audRepeatMode == Const.REPEAT_ONE) {
            retFile = backShuffleFileList.get(tmpIndex);
            break;
          }
          if (tmpIndex < backShuffleFileList.size() - 1) {
            tmpIndex++;
          } else {
            break;
          }
          retFile = backShuffleFileList.get(tmpIndex);
          break;
        } else if (audRepeatMode == Const.REPEAT_MARKED) {
          temp = markCycleNext(backFileList, tmpIndex);
          if (temp == -1) {
            retFile = null;
          } else {
            retFile = backFileList.get(temp);
          }
          break;
        } else if (audRepeatMode == Const.REPEAT_ONE) {
          retFile = backFileList.get(tmpIndex);
          break;
        }

        if (tmpIndex >= backFileList.size() - 1) {
          if (audRepeatMode == Const.REPEAT_ALL) {
            tmpIndex = 0;
          } else {
            break;
          }
        } else {
          tmpIndex++;
        }
        retFile = backFileList.get(tmpIndex);
        break;

      case Const.FILTER_VIDEO:
        tmpIndex = curForeVideoIdx;
        if (vidShuffleMode == Const.SHUFFLE_ON) {
          if (vidRepeatMode == Const.REPEAT_ONE) {
            retFile = foreShuffleVideoFileList.get(tmpIndex);
            break;
          }
          if (tmpIndex < foreShuffleVideoFileList.size() - 1) {
            tmpIndex++;
          } else {
            break;
          }
          retFile = foreShuffleVideoFileList.get(tmpIndex);
          break;
        } else if (vidRepeatMode == Const.REPEAT_ONE) {
          retFile = foreVideoFileList.get(tmpIndex);
          break;
        }

        if (tmpIndex >= foreVideoFileList.size() - 1) {
          if (vidRepeatMode == Const.REPEAT_ALL) {
            tmpIndex = 0;
          } else {
            break;
          }
        } else {
          tmpIndex++;
        }
        retFile = foreVideoFileList.get(tmpIndex);
        break;

      case Const.FILTER_TEXT:
        if (textShuffleMode == Const.SHUFFLE_ON) {
          if (textRepeatMode == Const.REPEAT_ONE) {
            retFile = foreShuffleFileList.get(tmpIndex);
            break;
          }
          if (tmpIndex < foreShuffleFileList.size() - 1) {
            tmpIndex++;
          } else {
            break;
          }
          retFile = foreShuffleFileList.get(tmpIndex);
          break;
        } else if (textRepeatMode == Const.REPEAT_ONE) {
          retFile = foreFileList.get(tmpIndex);
          break;
        }
        if (tmpIndex >= foreFileList.size() - 1) {
          if (textRepeatMode == Const.REPEAT_ALL) {
            tmpIndex = 0;
          } else {
            break;
          }
        } else {
          tmpIndex++;
        }
        retFile = foreFileList.get(tmpIndex);
        break;

      case Const.FILTER_IMAGE:
        Log.i(TAG, "FILTER_IMAGE  picShuffleMode:" + picShuffleMode + "--picRepeatMode:"
            + audRepeatMode);
        if (picShuffleMode == Const.SHUFFLE_ON) {
          if (picRepeatMode == Const.REPEAT_ONE) {
            retFile = foreShuffleFileList.get(tmpIndex);
            Log.i(TAG, "retFile:" + retFile);
            break;
          }
          if (tmpIndex < foreShuffleFileList.size() - 1) {
            tmpIndex++;
          } else {
              if (picRepeatMode == Const.REPEAT_ALL) {
                tmpIndex = 0;
              } else {
                break;
              }
          }

          retFile = foreShuffleFileList.get(tmpIndex);
          break;
        } else if (picRepeatMode == Const.REPEAT_MARKED) {
          temp = markCycleNext(foreFileList, tmpIndex);
          if (temp == -1) {
            retFile = null;
          } else {
            retFile = foreFileList.get(temp);
          }
          break;
        } else if (picRepeatMode == Const.REPEAT_ONE) {
          retFile = foreFileList.get(tmpIndex);
          break;
        }
        if (tmpIndex >= foreFileList.size() - 1) {
          if (picRepeatMode == Const.REPEAT_ALL) {
            tmpIndex = 0;
          } else {
            break;
          }
        } else {
          tmpIndex++;
        }
        retFile = foreFileList.get(tmpIndex);
        break;
      default:
        break;
    }

    if (retFile != null) {

      String tempPath[] = retFile.replaceAll("\\\\", "/").split("/");

      String fileName = "-";

      if (tempPath.length > 1) {
        fileName = tempPath[tempPath.length - 1];
      } else {
        fileName = retFile;
      }

      return fileName;
    } else {
      return retFile;
    }
  }

  /**
   * according to the parameters for the file
   *
   * @param filterType
   * @return return to file according to the parameters
   * @deprecated
   */
  @Deprecated
  @Override
  public File getCurrentFile(int filterType) {
    // TODO Auto-generated method stub
    File retFile = null;

    if (PlayFileType != Const.FILE_TYPE_USB) {
      return null /* retFile */;
    }

    switch (filterType) {
      case Const.FILTER_AUDIO:
        if (audShuffleMode) {
          retFile = new File(backShuffleFileList.get(curBackIdx));
        } else {
          retFile = new File(backFileList.get(curBackIdx));
        }
        break;

      case Const.FILTER_VIDEO:
        if (vidShuffleMode) {
          retFile = new File(foreShuffleVideoFileList.get(curForeVideoIdx));
        } else {
          retFile = new File(foreVideoFileList.get(curForeVideoIdx));
        }
        break;

      case Const.FILTER_IMAGE:
        if (picShuffleMode) {
          retFile = new File(foreShuffleFileList.get(curForeIdx));
        } else {
          retFile = new File(foreFileList.get(curForeIdx));
        }
        break;

      case Const.FILTER_TEXT:
        if (textShuffleMode) {
          retFile = new File(foreShuffleFileList.get(curForeIdx));
        } else {
          retFile = new File(foreFileList.get(curForeIdx));
        }
        break;

      default:
        break;
    }
    return retFile;
  }

  /**
   * Set current file
   *
   * @param filterType
   * @param path
   * @return return true if success,return false if fail
   * @deprecated
   */
  @Deprecated
  @Override
  public boolean setCurrentFile(int filterType, String path) {
    // TODO Auto-generated method stub
    boolean retVal = false;

    int num;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        num = backFileList.size();
        for (int i = 0; i < num; ++i) {
          if (path.equals(backFileList.get(i))) {
            curBackIdx = i;
            return true;
          }
        }
        break;
      case Const.FILTER_VIDEO:
        num = foreVideoFileList.size();
        for (int i = 0; i < num; ++i) {
          if (path.equals(foreVideoFileList.get(i))) {
            curForeVideoIdx = i;
            return true;
          }
        }
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        num = foreFileList.size();
        for (int i = 0; i < num; ++i) {
          if (path.equals(foreFileList.get(i))) {
            curForeIdx = i;
            return true;
          }
        }
        break;
      default:
        break;
    }
    return retVal;
  }

  public boolean isEnd(int filterType) {
    Log.d(TAG, "isEnd filterType = " + filterType);
    if (getRepeatMode(filterType) != Const.REPEAT_NONE) {
      Log.d(TAG, "isEnd repeateMode not none return false");
      return false;
    }

    int curIndex = -1;
    int fileNum = -1;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        curIndex = curBackIdx;
        fileNum = backFileList.size();
        break;
      case Const.FILTER_VIDEO:
        curIndex = curForeVideoIdx;
        fileNum = foreVideoFileList.size();
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        curIndex = curForeIdx;
        fileNum = foreFileList.size();
        break;
      default:
        return false;
    }
    Log.d(TAG, "isEnd fileNum = " + fileNum + " curIndex = " + curIndex);

    return (fileNum <= 0 || curIndex >= fileNum - 1);

  }

  public boolean isBegin(int filterType) {
    Log.d(TAG, "isBegin filterType = " + filterType);
    if (getRepeatMode(filterType) != Const.REPEAT_NONE) {
      Log.d(TAG, "isBegin repeateMode not none return false");
      return false;
    }

    int curIndex = -1;
    switch (filterType) {
      case Const.FILTER_AUDIO:
        curIndex = curBackIdx;
        break;
      case Const.FILTER_VIDEO:
        curIndex = curForeVideoIdx;
        break;
      case Const.FILTER_IMAGE:
      case Const.FILTER_TEXT:
        curIndex = curForeIdx;
        break;
      default:
        return false;
    }
    Log.d(TAG, "isBegin curIndex = " + curIndex);

    return (curIndex <= 0);
  }

}
