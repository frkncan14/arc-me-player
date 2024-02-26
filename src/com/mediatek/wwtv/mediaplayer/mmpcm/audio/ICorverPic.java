package com.mediatek.wwtv.mediaplayer.mmpcm.audio;

import android.graphics.Bitmap;

public interface ICorverPic {
	/**
	 * Get audio corver picture
	 * @param srcType
	 * @param filepath
	 * @param width
	 * @param height
	 * @return
	 * @throws IllegalArgumentException
	 */
    Bitmap getAudioCorverPic(int srcType, String filepath, int width,
            int height) throws IllegalArgumentException ;
    /**
     * Stop get meta data thumbnail
     */
    void stopThumbnail();
}
