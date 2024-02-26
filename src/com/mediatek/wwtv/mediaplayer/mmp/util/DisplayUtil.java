package com.mediatek.wwtv.mediaplayer.mmp.util;

import com.mediatek.wwtv.tvcenter.util.MtkLog;

import android.content.Context;
import android.util.DisplayMetrics;

public final class DisplayUtil {
	private static final String TAG = "DisplayUtil";
	private static int sWidthPixels = -1;
	private static int sHeightPixels = -1;

    private DisplayUtil() {
        MtkLog.v(TAG, "DisplayUtil");
    }

	private static void getDisplayMetrics(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		sWidthPixels = dm.widthPixels;
		sHeightPixels = dm.heightPixels;
		MtkLog.v(TAG, "Display Metrics : " + dm);
	}

	public static int getWidthPixels(Context context, float ratio) {
		if (sWidthPixels == -1) {
			getDisplayMetrics(context);
		}

		return (int) (sWidthPixels * ratio);
	}

	public static int getHeightPixels(Context context, float ratio) {
		if (sHeightPixels == -1) {
			getDisplayMetrics(context);
		}

		return (int) (sHeightPixels * ratio);
	}
}
