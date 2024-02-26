package com.mediatek.wwtv.mediaplayer.setting.presenter;

import com.mediatek.wwtv.mediaplayer.R;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.Log;

public class SettingItemPresenter extends Presenter{

  private static class SettingItemViewHolder extends ViewHolder {
    public final ImageView mIconView;
    public final TextView mTitleView;
    public final RelativeLayout mLayout;

    SettingItemViewHolder(View v) {
      super(v);
      mIconView = (ImageView) v.findViewById(R.id.icon);
      mTitleView = (TextView) v.findViewById(R.id.title);
      mLayout = (RelativeLayout) v.findViewById(R.id.item_layout);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent) {
    LayoutInflater inflater =
        (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.browse_item, parent, false);
    return new SettingItemViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, Object item) {
    if (item instanceof SettingItem && viewHolder instanceof SettingItemViewHolder) {
      final SettingItem menuItem = (SettingItem) item;
      SettingItemViewHolder mItemViewHolder = (SettingItemViewHolder) viewHolder;
      prepareImageView(mItemViewHolder, menuItem.getIcon(), menuItem.isCircle());
      mItemViewHolder.mTitleView.setText(menuItem.getTitle());
      mItemViewHolder.mLayout.setBackground(menuItem.getBackground());
    }
  }

  @Override
  public void onUnbindViewHolder(ViewHolder viewHolder) {
    if (viewHolder instanceof SettingItemViewHolder) {
      SettingItemViewHolder menuItemViewHolder = (SettingItemViewHolder) viewHolder;
      menuItemViewHolder.mIconView.setImageBitmap(null);
    }
  }

  private void prepareImageView(
      final SettingItemViewHolder menuItemViewHolder, Drawable d, boolean isCircle) {
    Log.i("prepareImageView", "isCircle:" + isCircle);
    menuItemViewHolder.mIconView.setVisibility(View.VISIBLE);
    menuItemViewHolder.mIconView.setBackgroundDrawable(d);
    fadeIn(menuItemViewHolder.mIconView);
  }

  private void fadeIn(View v) {
    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(v, "alpha", 1f);
    alphaAnimator.setDuration(
        v.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
    alphaAnimator.start();
  }
}
