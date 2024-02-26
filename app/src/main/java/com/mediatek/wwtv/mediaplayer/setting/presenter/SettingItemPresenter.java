package com.mediatek.wwtv.mediaplayer.setting.presenter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.setting.presenter.SettingItem;

public class SettingItemPresenter extends Presenter {

  private static class SettingItemViewHolder extends ViewHolder {
    public final ImageView mIconView;
    public final TextView mTitleView;
    public final RelativeLayout mLayout;

    SettingItemViewHolder(View v) {
      super(v);
      mIconView = v.findViewById(R.id.icon);
      mTitleView = v.findViewById(R.id.title);
      mLayout = v.findViewById(R.id.item_layout);
    }
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    View v = inflater.inflate(R.layout.browse_item, parent, false);
    return new SettingItemViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder viewHolder, Object item) {
    if (item instanceof SettingItem && viewHolder instanceof SettingItemViewHolder) {
      final SettingItem menuItem = (SettingItem) item;
      final SettingItemViewHolder mItemViewHolder = (SettingItemViewHolder) viewHolder;
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
    menuItemViewHolder.mIconView.setVisibility(View.VISIBLE);
    menuItemViewHolder.mIconView.setBackgroundDrawable(d);
    fadeIn(menuItemViewHolder.mIconView);
  }

  private void fadeIn(View v) {
    // Your fade-in animation code
  }

  private void playVideo(VideoView videoView) {
    videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
      }
    });
  }

  private void stopVideo(final VideoView videoView) {
    if (videoView.isPlaying()) {
      videoView.stopPlayback();
    }
  }
}