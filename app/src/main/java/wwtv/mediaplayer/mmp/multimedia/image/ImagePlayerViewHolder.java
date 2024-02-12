package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;

public class ImagePlayerViewHolder {
    private ImagePlayerActivity mImagePlayerActivity;

    protected ImagePlayerSurfaceView mSurfaceView;

    public ImagePlayerViewHolder(ImagePlayerActivity activity) {
        this.mImagePlayerActivity = activity;
    }

    void findViews() {
        mSurfaceView = (ImagePlayerSurfaceView) mImagePlayerActivity.findViewById(R.id.imageSurfaceView);
    }
}
