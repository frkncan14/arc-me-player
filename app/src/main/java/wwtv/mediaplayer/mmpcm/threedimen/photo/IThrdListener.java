package com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photo;

public interface IThrdListener {
	/**
	 * complete while play all files
	 */
	void onCompleted();
	/**
	 * failed while play a failed.
	 */
	void onPlayFailed();
	/**
	 * connect vdp failed.
	 */
	void onOpenFailed();
	/**
	 * disconnect vdp failed.
	 */
	void onCloseFailed();
//add by shuming
	void playDone();

	void decodeSuccess();
}
