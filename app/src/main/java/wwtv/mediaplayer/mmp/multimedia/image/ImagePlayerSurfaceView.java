package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;
import android.util.Log;
import android.net.Uri;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.util.DisplayMetrics;

import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.GifDecoder;
import com.mediatek.wwtv.mediaplayer.util.Constants;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.Closeable;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.Handler;
import android.os.Message;

import com.mstar.android.media.MMediaPlayer;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.R;

public class ImagePlayerSurfaceView extends SurfaceView implements
        SurfaceHolder.Callback {

    private static final String TAG = "ImgPlayerSurfaceView";
    private static final int MEDIA_PLAYER_STATE_IDLE = 0;
    private static final int MEDIA_PLAYER_STATE_PREPARING = MEDIA_PLAYER_STATE_IDLE + 1;
    private static final int MEDIA_PLAYER_STATE_PREPARED = MEDIA_PLAYER_STATE_IDLE + 2;
    private static final int MEDIA_PLAYER_STATE_STARTED = MEDIA_PLAYER_STATE_IDLE + 3;
    private static final int MEDIA_PLAYER_STATE_ERROR = MEDIA_PLAYER_STATE_IDLE + 4;
    private int mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_IDLE;
    private Bitmap bitmap;
    private InputStream is;
    private boolean isStop = false;
    private int mDelta = 1;
    private ImagePlayerActivity mImagePlayerActivity;
    private GifDecoder gDecoder;
    private MMediaPlayer mMMediaPlayer=null;
    private boolean isSurfaceCreated = false;
    //private boolean mNextFilePrepared = false;
    //private boolean mPrevFilePrepared = false;
    private int mPrevPrepareState = MEDIA_PLAYER_STATE_IDLE;
    private int mNextPrepareState = MEDIA_PLAYER_STATE_IDLE;
    private String sPath = "";
    private String imgPath2SeamlessPlayback = null;
    private SurfaceHolder sfholder=null;
    private int mSurfaceWidth = 1920;
    private int mSurfaceHeight = 1080;
    private int mPanelWidth;
    private int mPanelHeight;
    private float imgDecodedWidth;
    private float imgDecodedHeight;
    private int dstWidthAfterScale;
    private int dstHeightAfterScale;
    private int cropStartX = 0;
    private int cropStartY = 0;
    private Thread updateTimer;
    private android.graphics.Rect dst = new android.graphics.Rect();
    private Thread mStartImagePlayerThread;
    private double mScaleFactor = 1.0f;
    private boolean mIsOnErr = false;
    private boolean mKeepBkgBlank = false;
    
    // if < 0, means preparing previous photo,
    // else if > 0, means preparing next photo.
    // else if = 0. means not preparing photo.
    private int mPrepareDelta = 0;
    private Handler mHandler = null;
    private PhotoTaskExecutor mTaskExecutor = null;

    public ImagePlayerSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        getContext();
    }

    public ImagePlayerSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        getHolder().addCallback(this);
    }

    public ImagePlayerSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        mSurfaceHeight = height;
        mSurfaceWidth = width;
        if ((mPanelHeight > mSurfaceHeight) || (mPanelWidth > mSurfaceWidth)) {
            mSurfaceHeight = mPanelHeight;
            mSurfaceWidth = mPanelWidth;
        }
        if (mSurfaceWidth < 1920) {
            setSurfaceSize();
        }
        Log.i(TAG, "surfaceChanged--mSurfaceWidth:"+mSurfaceWidth+"---mSurfaceHeight:"+mSurfaceHeight);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        sfholder = holder;

        adjustSurfaceSize();

        Log.i(TAG, "surfaceCreated--mSurfaceWidth:"+mSurfaceWidth+"---mSurfaceHeight:"+mSurfaceHeight);
        if(!sPath.equals(""))
            setImageSurface();
        if (bitmap != null) {
            drawImage();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "********surfaceDestroyed******");
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        stopPlayback(true);
        sfholder = null;
    }

    public void setImagePath(String imagePath,ImagePlayerActivity ppa) {
        if (Tools.isSambaPlaybackUrl(imagePath)) {
            sPath = Tools.convertToHttpUrl(imagePath);
        } else {
            sPath = imagePath;
        }
        mImagePlayerActivity = ppa;
        Log.i(TAG, "the photo path is: " + sPath);
        openImagePlayer();
    }

    public int getPlayTimeEachFrame(){
        if(gDecoder != null){
            return Math.max(100, gDecoder.nextDelay()) / mDelta;
        }
        return 0;
    }

    public int getPlayTime() {
        int allDelay = 0;
        if(gDecoder != null) {
            int n = gDecoder.getFrameCount();
            for(int i = 0; i < n; i++) {
                allDelay += Math.max(100, gDecoder.getDelay(i));
            }
        }
        Log.i(TAG,"getPlayTime: allDelay = " + allDelay);
        return allDelay;
    }

    public int getFrameCount(){
        return gDecoder.getFrameCount();
    }

    private void frameDelay() {
        try {
            Thread.sleep(getPlayTimeEachFrame());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setStart(GifDecoder.IGifCallBack mGifCallBack) {
        if (updateTimer != null && updateTimer.isAlive()) {
            isStop = true;
            frameDelay();
        }
        gDecoder.setOnGifListener(mGifCallBack);
        updateTimer = new Thread(new Runnable() {
            @Override
            public void run() {
                frameDelay(); // the first frame delay
                while (true) {
                    if (isStop) {
                        return;
                    }
                    drawImage();
                    frameDelay();
                }
            }
        });
        isStop = false;
        updateTimer.start();
    }

    public void resetMediaPlayer() {
        Log.i(TAG, "resetMediaPlayer mMMediaPlayer:" + mMMediaPlayer + " mCurrentMediaPlayerState:" + mCurrentMediaPlayerState);
            if (mMMediaPlayer != null) {
                try {
                    mMMediaPlayer.reset();
                } catch (Exception ex) {
                    Log.e(TAG, "Exception:" + ex);
                }
                mMMediaPlayer = null;
                mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_IDLE;
            }
    }

    public void stopPlayback(boolean bActivityExit) {
        Log.i(TAG, "------stopPlayback ------- bActivityExit:" + bActivityExit);
        isStop = true;
        synchronized(this) {
            if ((mCurrentMediaPlayerState == MEDIA_PLAYER_STATE_IDLE)
                || (Constants.bReleasingPlayer)) {
                return;
            }
            stopTaskExecutor();
            if (bActivityExit) {
                // When abnormal stop play.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "mMMediaPlayer:" + mMMediaPlayer + " mCurrentMediaPlayerState:" + mCurrentMediaPlayerState);
                        if (mMMediaPlayer != null ) {

                            Constants.bReleasingPlayer = true;
                            /*
                            while (mCurrentMediaPlayerState < MEDIA_PLAYER_STATE_STARTED) {
                                try {
                                    Log.w(TAG,"player state is not prepared,wait 0.6 second!");
                                    Thread.sleep(600);
                                } catch (InterruptedException e) {
                                 Log.e(TAG,"thread interrupt exception");
                                }
                            }*/
                            try {
                                 if ((mMMediaPlayer !=null) && mMMediaPlayer.isPlaying()) {
                                     if (mMMediaPlayer !=null) {
                                        Log.i(TAG, "*****stop start*****");
                                        mMMediaPlayer.stop();
                                        Log.i(TAG, "*****stop end*****");
                                     }
                                 }
                                 // Stop is not necessary but release is must.
                                 if (mMMediaPlayer != null) {
                                    Log.i(TAG, "*****release start*****");
                                    mMMediaPlayer.release();
                                    Log.i(TAG, "*****release end*****");
                                 }
                                 mMMediaPlayer = null;

                                 Constants.bReleasingPlayer = false;
                            } catch (IllegalStateException ex) {
                                 Log.e(TAG, "IllegalStateException");
                            }
                        }
                    }
                }).start();
            } else {
                // call before play next.
                if (mMMediaPlayer != null ) {
                    Log.i(TAG, "mMMediaPlayer.stop()");
                    mMMediaPlayer.stop();
                    Log.i(TAG, "mMMediaPlayer.stop() end");
                    Log.i(TAG, "mMMediaPlayer.release()");
                    mMMediaPlayer.release();
                    Log.i(TAG, "mMMediaPlayer.release() end");
                    Log.i(TAG, "mMMediaPlayer= null");
                    mMMediaPlayer = null;
                    mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_IDLE;
                }
            }
        }
    }

    public void setStop() {
        isStop = true;
    }

    public boolean startNextVideo(String sPath, ImagePlayerActivity ppa) {
        Log.i(TAG, "startNextVideo  sPath:"+sPath+"  mImagePlayerActivity:"+mImagePlayerActivity+ " mCurrentMediaPlayerState:" + mCurrentMediaPlayerState);
        if (mCurrentMediaPlayerState == MEDIA_PLAYER_STATE_PREPARED) {
            return false;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        drawImage();
        stopPlayback(false);
        ppa.startShowProgress();
        setImagePath(sPath, ppa);
        setImageSurface();

        mPrevPrepareState = MEDIA_PLAYER_STATE_IDLE;
        mNextPrepareState = MEDIA_PLAYER_STATE_IDLE;
        return true;
    }

    private void setDstImageSize(float fAngle,float fRatio) {
        Log.i(TAG, "setDstImageSize -------------- begin " + "fAngle:" + fAngle + " fRatio:" + fRatio);
        double radian = fAngle * Math.PI / 180;
        int dstWidth = (int)((int)imgDecodedWidth * fRatio);
        int dstHeight = (int)((int)imgDecodedHeight * fRatio);
        dstWidthAfterScale = (int)(dstWidth * Math.abs(Math.cos(radian)) +
                dstHeight * Math.abs(Math.sin(radian))) - 1;
        dstHeightAfterScale = (int)(dstHeight * Math.abs(Math.cos(radian)) +
                dstWidth * Math.abs(Math.sin(radian))) - 1;
        if (dstHeightAfterScale>mSurfaceHeight) {
            cropStartY = (int)(dstHeightAfterScale-mSurfaceHeight)/2;
        } else {
            cropStartY = 0;
        }
        if (dstWidthAfterScale>mSurfaceWidth) {
            cropStartX = (int)(dstWidthAfterScale-mSurfaceWidth)/2;
        } else {
            cropStartX = 0;
        }
        Log.i(TAG, "after setDstImageSize cropStartY:" + cropStartY + " cropStartX:" + cropStartX);
        Log.i(TAG, "After setDstImageSize dstWidthAfterScale:"+dstWidthAfterScale+" dstHeightAfterScale:"+dstHeightAfterScale);
    }

    protected void rotateImage(float fAngle,float fRatio) {
        Log.i (TAG, "rotateImage, fRatio:" + fRatio + ", mScaleFactor:" + mScaleFactor + ", fAngle:" + fAngle);
        if (mMMediaPlayer != null) {
            if ((fRatio == 1.0f) && (fAngle == 90 || fAngle == 270 || fAngle == -90 || fAngle == -270)) {
                int tmpWidth =  (int)(imgDecodedWidth + 1.5);
                if (tmpWidth > mSurfaceWidth) {
                    fRatio = Math.min((float)mSurfaceHeight/(float)imgDecodedWidth,(float)mSurfaceWidth/(float)imgDecodedHeight);
                }
            }
            if ((dstWidthAfterScale > 3840) && (dstHeightAfterScale > 2160)) {
                mMMediaPlayer.ImageRotateAndScale(fAngle, fRatio * (float)mScaleFactor, fRatio * (float)mScaleFactor, true);
            } else {
                mMMediaPlayer.ImageRotateAndScale(fAngle, fRatio * (float)mScaleFactor, fRatio * (float)mScaleFactor, false);
                setDstImageSize(fAngle, fRatio);
            }
        }
    }

    protected void scaleImage(float fAngle, float fRatio) {
        Log.i (TAG, "scaleImage, fRatio:" + fRatio + ", mScaleFactor:" + mScaleFactor);
        if (mMMediaPlayer != null) {
            mMMediaPlayer.ImageRotateAndScale(fAngle, fRatio * (float)mScaleFactor, fRatio * (float)mScaleFactor, true);
            setDstImageSize(fAngle, fRatio);
        }
    }

    // if panRight > 0 means move Right direction
    // if panRight < 0 means move Left direction
    // if panDown > 0 means move Down direction
    // if panDown < 0 means move Up direction
    public void moveDirection(int panRight,int panDown) {
        Log.i(TAG, "------- moveDirection pRight:" + panRight + " pDown:" + panDown);
        if (panRight != 0) {
            if (mSurfaceWidth > dstWidthAfterScale) {
                return;
            }
            cropStartX += panRight;
            if (cropStartX < 0){
                cropStartX = 0;
                return;
            } else if ((cropStartX+mSurfaceWidth) > dstWidthAfterScale) {
                cropStartX = dstWidthAfterScale - mSurfaceWidth - 1;
                return;
            }
        } else if (panDown != 0) {
            if (mSurfaceHeight > dstHeightAfterScale) {
                return;
            }
            cropStartY += panDown;
            if (cropStartY < 0){
                cropStartY = 0;
                return;
            } else if ((cropStartY+mSurfaceHeight) > dstHeightAfterScale) {
                cropStartY = dstHeightAfterScale - mSurfaceHeight - 1;
                return;
            }
        }
        if(mMMediaPlayer != null) {
            Log.i(TAG, "dstWidthAfterScale:" + dstWidthAfterScale + " dstHeightAfterScale:" + dstHeightAfterScale);
            Log.i(TAG, "cropStartX:" + cropStartX + " cropStartY:" + cropStartY);
            int cropWidth = Math.min(mSurfaceWidth, dstWidthAfterScale);
            int cropHeight = Math.min(mSurfaceHeight,dstHeightAfterScale);
            Log.i(TAG, "cropWidth:" + cropWidth);
            Log.i(TAG, "cropHeight:" + cropHeight);
            // ImageCropRect API's Parameter should follow:
            // 0 <= cropStartX < dstWidthAfterScale
            // 0 <= cropStartY < dstHeightAfterScale
            // (cropStartX + cropWidth) <= dstWidthAfterScale
            // (cropStartY + cropHeight) <= dstHeightAfterScale

            if (cropStartX >= 0 && cropStartY >= 0 && cropStartX < dstWidthAfterScale && cropStartY < dstHeightAfterScale &&
                    cropStartX + cropWidth <= dstWidthAfterScale && cropStartY + cropHeight <= dstHeightAfterScale) {
                Log.i(TAG, "ImageCropRect parameter is valid");
                boolean bImagePanSuccess = mMMediaPlayer.ImageCropRect(cropStartX, cropStartY, cropWidth, cropHeight);
                showToast(bImagePanSuccess ? getResources().getString(R.string.mmp_img_player_pan_success) : getResources().getString(R.string.mmp_img_player_pan_failed), Gravity.CENTER, Toast.LENGTH_SHORT);
            } else {
                Log.i(TAG, "ImageCropRect parameter is not valid");
                showToast(getResources().getString(R.string.mmp_img_player_pan_parameter_invalid), Gravity.CENTER, Toast.LENGTH_SHORT);
            }
        }
    }

    private void setImageSampleSize() {
        // update surface size first
        adjustSurfaceSize();

        int imgOriginalWidth = mMMediaPlayer.getVideoWidth();
        int imgOriginalHeight = mMMediaPlayer.getVideoHeight();
        Log.i(TAG, "imgOriginalWidth:" + imgOriginalWidth + " imgOriginalHeight:" + imgOriginalHeight);

        int sampleSize = 1;
        double initialSize = Math.max((double)imgOriginalWidth/(double)mSurfaceWidth,(double)imgOriginalHeight/(double)mSurfaceHeight);
        double scaleFactor = 1.0f;
        Log.i(TAG,"initialSize1:"+initialSize);
        if ((initialSize == 1.0f) || (initialSize == 2.0f) || (initialSize == 4.0f) || (initialSize == 8.0f)) {
            scaleFactor = 1.0f;
            sampleSize = (int)initialSize;
        } else if(initialSize < 1.0f) {
            scaleFactor = 1.0f;
            sampleSize = 1;
        } else {
            if (initialSize < 2.0f) {
                sampleSize = (int)Math.ceil(initialSize);
            } else if (initialSize < 4.0f) {
                sampleSize = 2;
            } else if (initialSize < 8.0f) {
                sampleSize = 4;
            } else {
                sampleSize = 8;
            }
            scaleFactor = sampleSize / initialSize;
        }
        mScaleFactor = scaleFactor;
        imgDecodedWidth = (int)(imgOriginalWidth/sampleSize * scaleFactor);
        imgDecodedHeight = (int)(imgOriginalHeight/sampleSize * scaleFactor);
        dstWidthAfterScale = (int)imgDecodedWidth;
        dstHeightAfterScale = (int)imgDecodedHeight;
        MMediaPlayer.InitParameter  initParameter = mMMediaPlayer.new InitParameter();
        initParameter.degrees = 0;
        initParameter.scaleX = (float)scaleFactor;
        initParameter.scaleY = (float)scaleFactor;
        initParameter.cropX = 0;
        initParameter.cropY = 0;
        initParameter.cropWidth = 0;
        initParameter.cropHeight = 0;
        Log.i(TAG, "imgDecodedWidth:" + imgDecodedWidth + " imgDecodedHeight:" + imgDecodedHeight + " sampleSize:" + sampleSize + " scaleFactor:" + initParameter.scaleX);
        mMMediaPlayer.SetImageSampleSize(sampleSize, mSurfaceWidth, mSurfaceHeight, initParameter);
        initParameter = null;
    }

    private void startImagePlayer() {
        if (mMMediaPlayer == null
            || isSurfaceCreated == false
            || mCurrentMediaPlayerState != MEDIA_PLAYER_STATE_PREPARED)
            return;

                    Log.i(TAG, "startImagePlayer");
        if (mImagePlayerActivity.mIsSourceChange == false) {
            _startImagePlayer();
        }
    }

    private void _startImagePlayer() {
        mStartImagePlayerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMMediaPlayer.start();
                    if (Constants.bPhotoSeamlessEnable) {
                        mPrepareDelta = 0;
                    }
                } catch (Exception e){
                    mImagePlayerActivity.stopShowingProgress();
                    /*Toast toast = ToastFactory.getToast(mImagePlayerActivity,
                            getResources().getString(R.string.mmp_img_player_low_memory), Gravity.CENTER);
                    toast.show();*/
                    if (mImagePlayerActivity != null) {
                        mImagePlayerActivity.finish();
                    }
                }
                    Log.i(TAG, "mStartImagePlayerThread end!!!");

            }
        });
        mStartImagePlayerThread.start();
    }

    public Thread getImagePlayerThread() {
        return mStartImagePlayerThread;
    }

    private void closeFileInputStream(final Closeable c) {
       if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Throwable t) {
        }
    }

    private void prepareNextImage(final int playMode) {
        if (getTaskExecutor().getTaskSize() == PhotoTaskExecutor.MAX_TASK_SIZE) {
            Log.w(TAG, "prepareNextImage: the task queue is full, this task may block here!");
        }
        getTaskExecutor().queueEvent(new Runnable() {
            @Override
            public void run() {
                prepareNextPhotoThread(playMode);
            }
        });
    }

    protected void prepareNextPhotoThread(int playMode) {
        BitmapFactory.Options  options = new BitmapFactory.Options();
        if (mImagePlayerActivity != null) {
            imgPath2SeamlessPlayback = mImagePlayerActivity.getNextImagePath(playMode);
        }

        FileDescriptor fileDescriptor = null;
        Log.i(TAG,"prepareNextPhotoThread imgPath:"+imgPath2SeamlessPlayback);
        setPrepareState(playMode,MEDIA_PLAYER_STATE_IDLE);
        if (imgPath2SeamlessPlayback == null)
            return;
        int imgWidth = 0;
        int imgHeight = 0;
        boolean bSuccess = false;
        Log.i(TAG,"call ImageDecodeNext with null parameter first time");
        try {
            if (mMMediaPlayer != null) {
                int index = (playMode < 0) ? 0 : 1;
                if(Tools.isSambaPlaybackUrl(imgPath2SeamlessPlayback)) {
                    String httpUrl = Tools.convertToHttpUrl(imgPath2SeamlessPlayback);
                    bSuccess = mMMediaPlayer.ImageDecodeNext(httpUrl,0,mSurfaceWidth,mSurfaceHeight,null,index);
                } else if (Tools.isNetPlayback(imgPath2SeamlessPlayback)) {
                    bSuccess = mMMediaPlayer.ImageDecodeNext(imgPath2SeamlessPlayback,0,mSurfaceWidth,mSurfaceHeight,null,index);
                } else {
                    String fileUriScheme = "file://";
                    if (imgPath2SeamlessPlayback.startsWith(fileUriScheme)) {
                        imgPath2SeamlessPlayback = imgPath2SeamlessPlayback.substring(fileUriScheme.length());
                    }
                    FileInputStream fileInputStream = null;
                    try {
                        Log.i(TAG, "prepareNextPhotoThread: path: " + imgPath2SeamlessPlayback);
                        fileInputStream = new FileInputStream(imgPath2SeamlessPlayback);
                        fileDescriptor = fileInputStream.getFD();

                        bSuccess = mMMediaPlayer.ImageDecodeNext(fileDescriptor,0,mSurfaceWidth,mSurfaceHeight,null,index);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "FileInputStream.getFD() throws IOException");
                    } finally {
                        try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                Log.i(TAG, "Couldn't close file: " + e);
                            }
                    }

                }
            }
        } catch (Exception e) {
            Log.i(TAG,"image decode next exception: "+ e);
            bSuccess = false;
        }
        if (!bSuccess) {
            setPrepareState(mPrepareDelta,MEDIA_PLAYER_STATE_ERROR);
        }
    }

    private void setPrepareState(int delta, int state) {
        Log.i(TAG,"setPrepareState delta: "+String.valueOf(delta)+", state: "+state);
        if (delta < 0) {
            mPrevPrepareState = state;
            mPrepareDelta = delta;
        } else {
            mNextPrepareState = state;
            mPrepareDelta = delta;
        }
    }

    public boolean showNextPhoto(final int delta) {
        int state;

        if (mPrevPrepareState < MEDIA_PLAYER_STATE_STARTED) {
            // middleware design limitation
            Log.w(TAG,"showNextPhoto(), wait for previous image decoding...(" + mPrevPrepareState + ")");

            String sMessage = "The photo is decoding, please try again later...";
            showToast(sMessage, Gravity.CENTER, Toast.LENGTH_SHORT);
            return false;
        }

        if (delta < 0) {
            state = mPrevPrepareState;
        } else {
            state = mNextPrepareState;
        }

        Log.i(TAG,"showNextPhoto state: " + state);
        if (mMMediaPlayer != null
            && state == MEDIA_PLAYER_STATE_STARTED
            && getQueueLengthOfSeamlessPlayback() <= minimumQueueLengthCanShowSeamlessPhoto) {
            int index = (delta < 0) ? 0 : 1;
            boolean isSuccess = mMMediaPlayer.ImageShowNext(index);
            if (isSuccess) {
                // update the present photo position by order ascend in the photo set. mantis: 1182403
                mImagePlayerActivity.setCurrentPos(delta);
            } else {
                Log.i(TAG, "showNextPhoto: failed");
                mPrevPrepareState= MEDIA_PLAYER_STATE_IDLE;
                mNextPrepareState= MEDIA_PLAYER_STATE_IDLE;
                prepareNextImage(Const.MANUALNEXT);
                return false;
            }
        } else if (state == MEDIA_PLAYER_STATE_ERROR) {
            mImagePlayerActivity.setCurrentPos(delta);
            stopPlayback(false);
            mImagePlayerActivity.onNextPhtoError();
            return false;
        }

        mPrevPrepareState= MEDIA_PLAYER_STATE_IDLE;
        mNextPrepareState= MEDIA_PLAYER_STATE_IDLE;
        prepareNextImage(Const.MANUALNEXT);
        return true;
    }

    protected void openImagePlayer(){
        try {
            Log.i(TAG, "openImagePlayer");
            // Use imgPath2SeamlessPlayback to diff the callback "onVideoSizeChanged" is from "openImagePlayer"
            // or from "ImageDecodeNext(imgPath2SeamlessPlayback,0,mSurfaceWidth,mSurfaceHeight,null,index)"
            mIsOnErr = false;
            mKeepBkgBlank = false;
            imgPath2SeamlessPlayback = null;
            mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_IDLE;
            resetMediaPlayer();
            mMMediaPlayer = new MMediaPlayer();
            mMMediaPlayer.reset();
            Log.i(TAG,"the photo path is: " + sPath);
            Uri mUri= Uri.parse(sPath);
            Log.i(TAG, "mMMediaPlayer.setDataSource: " + mUri);
            mMMediaPlayer.setOnErrorListener(mErrorListener);
            mMMediaPlayer.setOnInfoListener(mInfoListener);
            mMMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMMediaPlayer.setDataSource(this.getContext(), mUri);
            Log.i(TAG, "mMMediaPlayer.setDataSource end");
            mMMediaPlayer.setOnPreparedListener(new MMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // The process of getting the width or height of photos is just like video
                    // flow as the image flow
                    Log.i(TAG, "onPrepared");
                    mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_PREPARED;
                    setImageSampleSize();
                    startImagePlayer();
                } });
            Log.i(TAG, "mMMediaPlayer.prepareAsync()");
            mMMediaPlayer.prepareAsync();
            mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_PREPARING;
        } catch (Exception e){
            Log.i(TAG, "Exception:" + e);
            mImagePlayerActivity.showNotSupportDialog(mImagePlayerActivity.getResources().getString(R.string.mmp_img_player_file_not_support));
        }
    }

    private void setImageSurface() {
        Log.i(TAG, "mMMediaPlayer.setDisplay()");
        if (sfholder == null) {
            return;
        }

        try {
            mMMediaPlayer.setDisplay(sfholder);
            isSurfaceCreated = true;
            startImagePlayer();
        } catch (Exception e){
            Log.i(TAG, "Exception:" + e);
            mImagePlayerActivity.showNotSupportDialog(mImagePlayerActivity.getResources().getString(R.string.mmp_img_player_file_not_support));
        }
    }

    private void initQueueLengthOfSeamlessPlayback(){
        queueLengthOfSeamlessPlayback = 0;
    }

    private int getQueueLengthOfSeamlessPlayback(){
        Log.i(TAG,"getQueueLengthOfSeamlessPlayback():"+queueLengthOfSeamlessPlayback);
        return queueLengthOfSeamlessPlayback;
    }

    private void enqueueSeamLessPlayback(){
        if (Tools.isPhotoStreamlessModeOn()) {
            queueLengthOfSeamlessPlayback++;
        }
    }

    private void dequeueSeamLessPlayback(){
        if (Tools.isPhotoStreamlessModeOn() && queueLengthOfSeamlessPlayback>0) {
            queueLengthOfSeamlessPlayback--;
        }
    }

    private int queueLengthOfSeamlessPlayback = 0;
    private int minimumQueueLengthCanShowSeamlessPhoto = 1;

    private MMediaPlayer.OnInfoListener mInfoListener = new MMediaPlayer.OnInfoListener() {
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.i(TAG, "onInfo what:" + what  + " extra:" + extra);
            switch (what) {
                case MediaPlayer.MEDIA_INFO_STARTED_AS_NEXT:
                    dequeueSeamLessPlayback();
                    break;
                case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    dequeueSeamLessPlayback();
                    Log.i(TAG, "onInfo MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START mPrepareDelta = " + mPrepareDelta);
                    if (Constants.bPhotoSeamlessEnable) {
                        if (mPrepareDelta < 0) {
                            setPrepareState(-1, MEDIA_PLAYER_STATE_STARTED);
                        } else if (mPrepareDelta > 0) {
                          Log.i(TAG, "mPrepareDelta > 0");
                            if (getQueueLengthOfSeamlessPlayback() == 0) {
                                setPrepareState(1, MEDIA_PLAYER_STATE_STARTED);
                                prepareNextImage(Const.MANUALPRE);
                            }
                        } else if (mPrepareDelta == 0) {
                          Log.i(TAG, "omPrepareDelta == 0");
                            mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_STARTED;
                            mImagePlayerActivity.stopShowingProgress();
                            //mImagePlayerActivity.hideControlDelay();
                            mImagePlayerActivity.startPPT_Player();
                            // decode next one and then decode pre one
                            prepareNextImage(Const.MANUALNEXT);
                        }
                    } else {
                        mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_STARTED;
                        mImagePlayerActivity.stopShowingProgress();
                        //mImagePlayerActivity.hideControlDelay();
                        mImagePlayerActivity.startPPT_Player();
                    }
                    break;
                case MMediaPlayer.MEDIA_INFO_PHOTO_DECODE_NEXT_ERR:
                    Log.d (TAG, "MEDIA_INFO_PHOTO_DECODE_NEXT_ERR, mPrepareDelta "
                            + mPrepareDelta + " offset image resolution not supported");
                    dequeueSeamLessPlayback();
                     if (mPrepareDelta < 0) {
                        setPrepareState(-1, MEDIA_PLAYER_STATE_ERROR);
                    } else if (mPrepareDelta > 0) {
                        setPrepareState(1, MEDIA_PLAYER_STATE_ERROR);
                        prepareNextImage(Const.MANUALPRE);
                    }
                    break;

            }
            return false;
        }
    };

    // The following is a series of the player listener in callback
    MMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MMediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            if (imgPath2SeamlessPlayback == null) {
                return;
            }
            int delta = mPrepareDelta;
            int imgWidth = mp.getVideoWidth();
            int imgHeight = mp.getVideoHeight();
            Log.i(TAG, "MediaPlayer: "+mp+"    Video Size Changed: (" + imgWidth + "," + imgHeight+")");

            setPrepareState(delta, MEDIA_PLAYER_STATE_PREPARING);
            int sampleSize = 1;
            boolean bSuccess = false;
            Log.i(TAG,"the decoded next photo w:"+imgWidth+" ;h:"+imgHeight);

            double initialSize = Math.max((double)imgWidth/(double)mSurfaceWidth,(double)imgHeight/(double)mSurfaceHeight);
            double scaleFactor = 1.0f;
            if ((initialSize == 1.0f) || (initialSize == 2.0f) || (initialSize == 4.0f) || (initialSize == 8.0f)) {
                scaleFactor = 1.0f;
                sampleSize = (int)initialSize;
            } else if(initialSize < 1.0f) {
                scaleFactor = 1.0f;
                sampleSize = 1;
            } else {
                if (initialSize < 2.0f) {
                     sampleSize = (int)Math.ceil(initialSize);
                } else if (initialSize < 4.0f) {
                     sampleSize = 2;
                } else if (initialSize < 8.0f) {
                     sampleSize = 4;
                } else {
                     sampleSize = 8;
                }
                scaleFactor = sampleSize / initialSize;
            }

            MMediaPlayer.InitParameter  initParameter = mMMediaPlayer.new InitParameter();
            initParameter.degrees = 0;
            initParameter.scaleX = (float)scaleFactor;
            initParameter.scaleY = (float)scaleFactor;
            initParameter.cropX = 0;
            initParameter.cropY = 0;
            initParameter.cropWidth = 0;
            initParameter.cropHeight = 0;
            int index = (delta < 0) ? 0 : 1;
            try {

                if(Tools.isSambaPlaybackUrl(imgPath2SeamlessPlayback)) {
                    String httpUrl = Tools.convertToHttpUrl(imgPath2SeamlessPlayback);
                    bSuccess = mMMediaPlayer.ImageDecodeNext(httpUrl,sampleSize,mSurfaceWidth,mSurfaceHeight,initParameter,index);
                } else if (Tools.isNetPlayback(imgPath2SeamlessPlayback)) {
                    bSuccess = mMMediaPlayer.ImageDecodeNext(imgPath2SeamlessPlayback,sampleSize,mSurfaceWidth,mSurfaceHeight,initParameter,index);
                } else {

                    FileInputStream fileInputStream = null;
                    FileDescriptor fileDescriptor = null;
                    try {
                        fileInputStream = new FileInputStream(imgPath2SeamlessPlayback);
                        fileDescriptor = fileInputStream.getFD();

                        bSuccess = mMMediaPlayer.ImageDecodeNext(fileDescriptor,sampleSize,mSurfaceWidth,mSurfaceHeight,initParameter,index);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "FileInputStream.getFD() throws IOException");
                    } finally {
                        try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                Log.i(TAG, "Couldn't close file: " + e);
                            }
                    }

                }
            } catch (Exception e) {
              Log.i(TAG,"image decode next exception");
              bSuccess = false;
            }
            if (bSuccess == false) {
                setPrepareState(mPrepareDelta,MEDIA_PLAYER_STATE_ERROR);
            }
            enqueueSeamLessPlayback();

        }
    };

    public boolean isOnError() {
      return mIsOnErr;
    }

    private MMediaPlayer.OnErrorListener mErrorListener = new MMediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.e(TAG, "onError: " + framework_err + "," + impl_err);
            mIsOnErr = true;
            dequeueSeamLessPlayback();
            mCurrentMediaPlayerState = MEDIA_PLAYER_STATE_ERROR;
            /* If an error handler has been supplied, use it and finish. */
            stopPlayback(true);
            mPrevPrepareState = MEDIA_PLAYER_STATE_IDLE;
            mNextPrepareState = MEDIA_PLAYER_STATE_IDLE;
            mImagePlayerActivity.onError(mp, framework_err, impl_err);
            return true;
        }
    };

    public boolean decodeBitmapFromNet(String path, ImagePlayerActivity player){
        Log.i(TAG,"decodeBitmapFromNet");
        mImagePlayerActivity = player;
        if (bitmap!= null) {
            bitmap.recycle();
            bitmap = null;
        }
        if (gDecoder == null) {
        } else {
            gDecoder.reset();
            //gDecoder.resetFrame();
        }
        gDecoder = new GifDecoder();
        try {
            is = new URL(path).openStream();
            if (gDecoder != null) {
                gDecoder.read(is);
                if (gDecoder.err()) {
                    bitmap = player.decodeBitmap(path);
                } else {
                    bitmap = gDecoder.getImage();// first
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        drawImage();
        return true;
    }

    protected void setBkgBlank(final boolean keepBkgBlank) {
        Log.i(TAG,"keepBkgBlank = " + keepBkgBlank);
        mKeepBkgBlank = keepBkgBlank;

        if (true == mKeepBkgBlank) {
          // flash blank background
          drawImage();
        }
    }

    public boolean setSrc(String path,ImagePlayerActivity player) {
        mImagePlayerActivity = player;
        // isStop = false;
        if (!Tools.checkPath(path)) {
            Log.e(TAG, "File Path Error!");
            return false;
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        if (gDecoder == null){
        } else {
            gDecoder.reset();
        }
        gDecoder = new GifDecoder();
        try {
            //if (Tools.isNetPlayback(path))
            is = new FileInputStream(path);
            if (gDecoder != null) {
                gDecoder.read(is);
                if (gDecoder.err()) {
                    bitmap = player.decodeBitmap(path);
                } else {
                    bitmap = gDecoder.getImage();// first
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        drawImage();
        return true;
    }

    protected void drawImage() {
        Log.i(TAG,"drawImage(), postInvalidate()");
        ImagePlayerSurfaceView.this.postInvalidate();
    }

    protected void onDraw(Canvas canvas) {
         if (bitmap != null && canvas != null) {
             if (true == mKeepBkgBlank) {
                 // keep the background blank
                 Log.w(TAG, "onDraw(), mKeepBkgBlank is true, ignored!");
                 return;
             }
             int srcWidth = bitmap.getWidth();
             int srcHeight = bitmap.getHeight();
             // Some GIF photo's size is Larger than Screen Size, So need to be scaled to adapt to ScreenSize..
             if (srcWidth > this.getWidth() && srcHeight > this.getHeight() && this.getWidth() > 0 && this.getHeight() > 0) {
                 float widthScale = (float) this.getHeight() / srcWidth;
                 float heightScale = (float) this.getHeight() / srcHeight;
                 int width, height;
                 if (widthScale > heightScale) {
                     width = Math.round(bitmap.getWidth() * heightScale);
                     height = Math.round(bitmap.getHeight() * heightScale);
                 } else {
                     width = Math.round(bitmap.getWidth() * widthScale);
                     height = Math.round(bitmap.getHeight() * widthScale);
                 }
                 bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
             }
            Paint paint = new Paint();
            android.graphics.Rect src = new android.graphics.Rect();
            src.left = 0;
            src.top = 0;
            src.bottom = bitmap.getHeight();
            src.right = bitmap.getWidth();
            dst.left = 0;
            dst.top = 0;
            dst.bottom = this.getHeight();
            dst.right = this.getWidth();
            if(!bitmap.isRecycled()){
                paint.setColor(Color.BLACK);
                canvas.drawRect(dst, paint);
                center(src,dst);
                canvas.drawBitmap(bitmap, src, dst, paint);
                if (gDecoder.getFrameCount() > 0) {
                    bitmap = gDecoder.nextBitmap();
                }
            }
        }else {
            if (canvas != null) {
                dst.left = 0;
                dst.top = 0;
                dst.bottom = this.getHeight();
                dst.right = this.getWidth();
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setAlpha(0);
                canvas.drawRect(dst, paint);
            }
        }

    }

    protected void center(android.graphics.Rect src,android.graphics.Rect dst) {
        //bmp = resizeDownIfTooBig(bmp, true);
        float height = bitmap.getHeight();
        float width = bitmap.getWidth();
        float deltaX = 0, deltaY = 0;
        int viewHeight = getHeight();
        if (height <= viewHeight) {
            deltaY = (viewHeight - height) / 2 - src.top;
        }  else if (src.top > 0) {
            deltaY = -src.top;
        } else if (src.bottom < viewHeight) {
            deltaY = getHeight() - src.bottom;
        }
        int viewWidth = getWidth();
        if (width <= viewWidth) {
            deltaX = (viewWidth - width) / 2 - src.left;
        } else if (src.left > 0) {
            deltaX = -src.left;
        } else if (src.right < viewWidth) {
            deltaX = viewWidth - src.right;
        }
        dst.top = src.top + (int)deltaY;
        dst.left = src.left + (int)deltaX;
        dst.bottom = bitmap.getHeight() + (int)deltaY;
        dst.right = bitmap.getWidth() + (int)deltaX;
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
            //canvas.save(1);
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

    private void setSurfaceSize() {
        DisplayMetrics dm = new DisplayMetrics();
        mImagePlayerActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mSurfaceWidth = (int)(dm.widthPixels);
        mSurfaceHeight = (int)(dm.heightPixels);
    }

    private void showToast(final String text, int gravity, int duration) {
      mImagePlayerActivity.showErrorDialog(text);
    }

    private void adjustSurfaceSize() {
        int[] config = Tools.getPanelSize();
        mPanelWidth = config[0];
        mPanelHeight = config[1];
        Log.i(TAG,"adjustSurfaceSize:"+String.valueOf(mPanelWidth)+" "+String.valueOf(mPanelHeight));
        if (mPanelWidth != 0 && mPanelHeight != 0) {
            Log.i(TAG, "getPanelConfig true");
        } else {
            Log.i(TAG, "getPanelConfig false");
            DisplayMetrics  dm = new DisplayMetrics();
            mImagePlayerActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            float density = dm.density;
            int tmpPanelWidth = (int)(dm.widthPixels * density);
            int tmpPanelHeight = (int)(dm.heightPixels * density);
            String strPM = "SurfaceSize:" +tmpPanelWidth +" * "+tmpPanelHeight;
            Log.i(TAG,strPM);
            mPanelWidth = tmpPanelWidth;
            mPanelHeight = tmpPanelHeight;
        }

        if ((mPanelHeight > mSurfaceHeight) || (mPanelWidth > mSurfaceWidth)) {
            mSurfaceHeight = mPanelHeight;
            mSurfaceWidth = mPanelWidth;
        }
    }

    private PhotoTaskExecutor getTaskExecutor() {
        if (mTaskExecutor == null || !mTaskExecutor.isAlive()) {
            mTaskExecutor = new PhotoTaskExecutor();
            mTaskExecutor.start();
        }
        return mTaskExecutor;
    }

    private void stopTaskExecutor() {
        if (mTaskExecutor != null && mTaskExecutor.isAlive()) {
            mTaskExecutor.clearEvents();
            mTaskExecutor.requestExit();
            mTaskExecutor.interrupt();
            mTaskExecutor = null;
        }
    }
}
