
package com.mediatek.wwtv.mediaplayer.mmp.util;

public interface IDmrListener {
  void notifyNewEvent(int state);

  void notifyNewEventWithParam(int state, int param);

  long getProgress();

  long getDuration();

}
