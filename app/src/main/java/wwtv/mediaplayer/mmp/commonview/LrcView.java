package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.LyricTimeContentInfo;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

public class LrcView extends View {

	private static final String TAG = "LrcView";
	private static List<LyricTimeContentInfo> lrcmap;
	private Paint mWhitePaint;

	private Paint mRedPaint;
	private int[] lrcWidth;
	private int lrcHeight = 0;
	private int viewWidth = 0;
	private List<LyricTimeContentInfo> lrcarr;
	private int mCurrentLine = 0;
	private int mLrcLine;
	private boolean isLineNeedUpdate = true;
	private int Lines = 8;
    private int lyricSize;
	//private String noLrc;
    private String mPreString;
    private String mLastString;
    private int mPreWidth;
    private int mLastWidth;
    private int mCurrentLines = Lines;
    private static final int mMinLrcWidth = 20;

	public LrcView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//noLrc = context.getString(R.string.mmp_info_nolrc);
		mWhitePaint = new Paint();
		mRedPaint = new Paint();
		mWhitePaint.setAntiAlias(true);
		mRedPaint.setAntiAlias(true);
        lyricSize = (int)context.getResources().getDimension(R.dimen.music_lyric_txt_size);
        MtkLog.d(TAG,"LrcView,lyricSize=="+lyricSize);
	}


	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

        MtkLog.d(TAG,"onDraw");

		if (lrcarr == null) {
			// canvas.drawText(noLrc, viewWidth / 3, lrcHeight / 2,
			// mWhitePaint);
			return;
		}

        int line = 0;
        int hilightLine = 0;
		for (int i = 0; i < lrcarr.size(); i++) {
            if (i == mCurrentLine){
                hilightLine = line;
            }
            if (lrcWidth[i] < mMinLrcWidth){
                changeLongLineToTwoLines(i);
                canvas.drawText(mPreString, mPreWidth, lrcHeight * (0.5f + line) , mWhitePaint);
                line++;
                canvas.drawText(mLastString, mLastWidth, lrcHeight * (0.5f + line), mWhitePaint);
                line++;
            } else {
			    canvas.drawText(lrcarr.get(i).getLyricContent(), lrcWidth[i], lrcHeight * (0.5f + line), mWhitePaint);
                line++;
            }
		}
		// show current music word

		mRedPaint.setColor(Color.GREEN);
        MtkLog.d(TAG,"onDraw,mCurrentLine=="+mCurrentLine);
        MtkLog.d(TAG,"onDraw,lrcWidth[mCurrentLine]=="+lrcWidth[mCurrentLine]);
		if (mCurrentLine < lrcarr.size()) {
            if (lrcWidth[mCurrentLine] < mMinLrcWidth){
                changeLongLineToTwoLines(mCurrentLine);
                canvas.drawText(mPreString, mPreWidth, lrcHeight * (0.5f + hilightLine), mRedPaint);
                canvas.drawText(mLastString, mLastWidth, lrcHeight * (1.5f + hilightLine), mRedPaint);
            } else {
    			canvas.drawText(lrcarr.get(mCurrentLine).getLyricContent(),
    					lrcWidth[mCurrentLine], lrcHeight * (0.5f + hilightLine),
    					mRedPaint);
            }
		}
	}

    public void changeLongLineToTwoLines(int i){
        String lrcString = lrcarr.get(i).getLyricContent();
        int index = lrcString.length() / 2;
        mLastString = lrcString.substring(index - 1);
        MtkLog.d(TAG,"changeLongLineToTwoLines,length=="+lrcString.length());
        MtkLog.d(TAG,"changeLongLineToTwoLines,index=="+index);
        int firstSpaceIndex = mLastString.indexOf(" ");
        index = index + firstSpaceIndex;
        mLastString = lrcString.substring(index);
        MtkLog.d(TAG,"changeLongLineToTwoLines,firstSpaceIndex=="+firstSpaceIndex);
        mPreString = lrcString.substring(0,index - 1);

        MtkLog.d(TAG,"changeLongLineToTwoLines,lrcString=="+lrcString);
        MtkLog.d(TAG,"changeLongLineToTwoLines,mPreString=="+mPreString);
        MtkLog.d(TAG,"changeLongLineToTwoLines,mLastString=="+mLastString);
        mPreWidth = (viewWidth - (int) mWhitePaint.measureText(mPreString)) / 2;
        mLastWidth = (viewWidth - (int) mWhitePaint.measureText(mLastString)) / 2;
    }

	public void noLrc(String lrc) {
        MtkLog.d(TAG,"noLrc,lrc=="+lrc);
		lrcarr = null;
		//noLrc = lrc;
		viewWidth = this.getWidth();
		lrcHeight = this.getHeight();
		mWhitePaint.setTextSize(lyricSize);

		mWhitePaint.setColor(Color.WHITE);
		this.invalidate();
	}

	public void init(List<LyricTimeContentInfo> lrc, int line) {
        MtkLog.d(TAG,"init,line=="+line);
		isLineNeedUpdate = true;
		mCurrentLine = 0;
		lrcmap = lrc;
		// Modified by Dan for fix hide lrc bug
		if (line == 0) {
			Lines = 8;
			setVisibility(View.INVISIBLE);
		} else {
			Lines = line;
			setVisibility(View.VISIBLE);
		}

		lrcarr = new ArrayList<LyricTimeContentInfo>(Lines);
		lrcWidth = new int[Lines];
		if (mLrcLine >= lrcmap.size() - 1) {
			mLrcLine = 0;
		}
		if (mLrcLine + Lines < lrcmap.size()) {
			lrcarr = lrcmap.subList(mLrcLine, mLrcLine + Lines);
		} else {
			lrcarr = lrcmap.subList(mLrcLine, lrcmap.size());
		}

		mWhitePaint.setTextSize(lyricSize);
		mRedPaint.setTextSize(lyricSize);

		mWhitePaint.setColor(Color.WHITE);

		viewWidth = this.getWidth();
		for (int i = 0; i < lrcarr.size(); i++) {
			lrcWidth[i] = (viewWidth - (int) mWhitePaint.measureText(lrcarr
					.get(i).getLyricContent())) / 2;
		}
		lrcHeight = this.getHeight() / Lines;
	}

	public void setLines(int line){
        MtkLog.d(TAG,"setLines,line=="+line);
		Lines =line;
        lrcWidth = new int[Lines];
	}

	public void setlrc(int currentline,boolean isRefreshImmediately) {
        MtkLog.d(TAG,"setlrc,currentline=="+currentline);

		if (lrcmap == null) {
			return;
		}
		if (currentline > lrcmap.size() - 1) {
			return;
		}
		if (!isLineNeedUpdate) {
			if (currentline != mLrcLine||isRefreshImmediately) {
				isLineNeedUpdate = true;
			}
		}
		if (isLineNeedUpdate) {
			MtkLog.i(TAG, "--------  mLrcLine  :" + mLrcLine
					+ "    currentline:" + currentline);
			mLrcLine = currentline;
			int endline;
			if (currentline % Lines == 0) {
				mCurrentLine = 0;
			} else {
				mCurrentLine = currentline % Lines;
				currentline = (currentline / Lines) * Lines;
			}

			if ((currentline + Lines) >= lrcmap.size()) {
				endline = lrcmap.size();
			} else {
				endline = currentline + Lines;
			}
			lrcarr = lrcmap.subList(currentline, endline);
            mCurrentLines = Lines;
			for (int i = 0; i < lrcarr.size(); i++) {
				lrcWidth[i] = (viewWidth - (int) mWhitePaint.measureText(lrcarr
						.get(i).getLyricContent())) / 2;
                if (lrcWidth[i] < mMinLrcWidth){
                    mCurrentLines++;
                }
			}
            if (Lines == 1){
                lrcHeight = this.getHeight() / 8 ;
            } else {
                lrcHeight = this.getHeight() / mCurrentLines ;
            }
			isLineNeedUpdate = false;
			this.invalidate();
		} else {
			MtkLog.i(TAG, "--------  mLrcLine  :" + mLrcLine
					+ "    currentline:" + currentline + ", no need update");
		}
	}

}
