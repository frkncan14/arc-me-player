package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ImageSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback {

    private static final String TAG = "ImageSurfaceView";

    private Bitmap bitmap;

    public ImageSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        getContext();
    }

    public ImageSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        getHolder().addCallback(this);
    }

    public ImageSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawImage();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "********surfaceDestroyed******");
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    // ////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////

    /**
     * Picture reading and loading start.
     */
    protected void drawImage() {
        Canvas canvas = getHolder().lockCanvas();
        if (bitmap != null && canvas != null) {
            Paint paint = new Paint();
            android.graphics.Rect src = new android.graphics.Rect();
            android.graphics.Rect dst = new android.graphics.Rect();
            src.left = 0;
            src.top = 0;
            src.bottom = bitmap.getHeight();
            src.right = bitmap.getWidth();
            dst.left = 0;
            dst.top = 0;
            dst.bottom = this.getHeight();
            dst.right = this.getWidth();
            canvas.drawBitmap(bitmap, src, dst, paint);
        }

        if (canvas != null) {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    protected void cleanView(int width, int height) {
        Canvas canvas = getHolder().lockCanvas();
        if (bitmap != null && canvas != null) {
            Paint paint = new Paint();
            android.graphics.Rect src = new android.graphics.Rect();
            paint.setColor(Color.BLACK);
            src.left = 0;
            src.top = 0;
            src.bottom = height;
            src.right = width;
            canvas.drawRect(src, paint);
            canvas.save();
        }
        if (canvas != null) {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    protected void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    protected boolean updateView() {
        if (this.bitmap != null) {
            invalidate();
            return true;
        } else {
            return false;
        }
    }
}
