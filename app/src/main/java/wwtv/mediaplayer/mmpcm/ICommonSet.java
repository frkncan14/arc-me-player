package com.mediatek.wwtv.mediaplayer.mmpcm;

public interface ICommonSet {
    /**
     * get current volume
     *
     * @param mAudioManager
     *            current mAudioManager
     * @param null
     *
     * @return volume value
     */
    int getVolume();
    /**
     * get max volume
     *
     * @param mAudioManager
     *            current mAudioManager
     * @param null
     *
     * @return max volume value
     */
    int getMaxVolume();

    int getMinVolume();
    /**
     * set current volume
     *
     * @param mAudioManager
     *            current mAudioManager
     * @param volume
     *            the volume want to set
     * @return void
     */
    void setVolume(int volume);
    /**
     * set mute
     *
     * @param mAudioManager
     *            current mAudioManager
     * @param null
     *
     * @return void
     */
    void setMute();
    /**
     * get the mute or not
     *
     * @param null
     *
     * @param null
     *
     * @return boolean,true mean mute;false mean unmute
     */
    boolean isMute();

    int getPictureModeMin();
    int getPictureModeMax();
    /**
     * get current picture mode
     *
     * @param void
     *
     * @param void
     *
     * @return int
     */
    int getCurPictureMode();

    /**
     * change picture mode
     *
     * @param type
     *          the picture type user want to set
     * @param void
     *
     * @return void
     */
    void setPictureMode(int type);

    int getAudioEffectMin();
    int getAudioEffectMax();
    /**
     * get current audio mode
     *
     * @param void
     *
     * @param void
     *
     * @return int
     */
    int getCurAudioEffect();

    /**
     * change audio mode
     *
     * @param type
     *          the audio type user want to set
     * @param void
     *
     * @return void
     */
    void setAudioEffect(int type);

    int getScreenModeMin();
    int getScreenModeMax();

    int[] getAvailableScreenMode();

    /**
     * get current screen mode
     *
     * @param void
     *
     * @param void
     *
     * @return int
     */
    int getCurScreenMode();

    /**
     * change screen mode
     *
     * @param type
     *          the screen type user want to set
     * @param void
     *
     * @return void
     */
    void setScreenMode(int type);

    void setAudOnly(boolean on);

    boolean getAudOnly();

    void mmpFreeVideoResource();

    void mmpRestoreVideoResource();
}
