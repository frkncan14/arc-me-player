package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.util.List;
import android.util.Log;

public class MenuFatherObject {
  public static final int MENU_INVALID_ID = -1;
	public String content;
	public boolean enable;
	public boolean right;
	public boolean enter;
	public boolean hasnext;
  private int menuId = MENU_INVALID_ID;

	@Override
	public boolean equals(Object o) {
		return content.equals(o.toString());
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

  public void setId(final int id) {
    menuId = id;
  }

  public int getId() {
    return menuId;    
  }
}
