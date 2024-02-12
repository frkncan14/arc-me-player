package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

import com.mediatek.wwtv.tvcenter.util.MtkLog;

public class MediaGridView extends GridView {

	public boolean isOnMeasure;

	public MediaGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MediaGridView(Context context) {
		this(context, null);

	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		isOnMeasure = true;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		MtkLog.i("onMeasure", getWidth() + ":" + getHeight());
	}

	public void setNumColumns(int numColumns) {
		super.setNumColumns(numColumns);
        MtkLog.i("MediaGridView","setNumColumns,numColumns=="+numColumns);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		isOnMeasure = false;
		super.onLayout(changed, l, t, r, b);
	}
}
