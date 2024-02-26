package com.mediatek.wwtv.mediaplayer.setting.presenter;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class SettingItem {
  private static final String TAG = "SettingItem";
  private String mTitle;

  private Drawable mIcon;

  private Drawable mBackground;

  private Intent mIntent;

  private boolean isCircle = false;
    private boolean isSelected;
  public SettingItem() {
    Log.d(TAG, "SettingItem");
  }

  public SettingItem(String title, Drawable icon, Drawable bg) {
    mTitle = title;
    mIcon = icon;
    mBackground = bg;
  }

  public String getTitle() {
    return mTitle;
  }

  public void setTitle(String mTitle) {
    this.mTitle = mTitle;
  }

  public Drawable getIcon() {
    return mIcon;
  }

  public void setIcon(Drawable mIcon) {
    this.mIcon = mIcon;
  }

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent mIntent) {
		this.mIntent = mIntent;
	}

	public boolean isCircle() {
		return isCircle;
	}

	public void setCircle(boolean isCircle) {
		this.isCircle = isCircle;
	}
  public Drawable getBackground() {
    return mBackground;
  }

  public void setBackground(Drawable bg) {
    this.mBackground = bg;
  }




    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
