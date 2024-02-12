package com.mediatek.wwtv.mediaplayer.mmp.util;

/**
 * MetaDataInfo Class can be used to get meta data info 
 * 
 * 
 */
public class MetaDataInfo
{
    private int mAudioTrackNum;
    private int mBitRate;
    private int mTotalPlayTime;
    private int mSampleRate;
    private String mYear;
    private String mTitle;
    private String mAlbum;
    private String mArtist;

    private int mChannelNumber;
    private String mGenre;
    private String mDirector;
    private String mCopyright;
    /**
     * This Contrustor used for MtkMediaPlayer classes. Please avoid to use this class directly.
     */
    public MetaDataInfo(int audioTrackNum, int bitRate, int totalPlayTime,
        int sampleRate, String year, String title, String album, String artist,
        int channelNum, String genre, String director, String copyRight)
    {
        super();
        this.mAudioTrackNum = audioTrackNum;
        this.mBitRate = bitRate;
        this.mTotalPlayTime = totalPlayTime;
        this.mSampleRate = sampleRate;
        this.mYear = year;
        this.mTitle = title;
        this.mAlbum = album;
        this.mArtist = artist;
        this.mChannelNumber = channelNum;
        this.mGenre = genre;
        this.mDirector = director;
        this.mCopyright = copyRight;
    }
    
    /**
     *   This API will get audio track mAudioTrackNum
     *   @return mAudioTrackNum 
     */
    public int getAudioTrackNum()
    {
        return mAudioTrackNum;
    }
    
    /**
     *   This API will get audio track mTotalPlayTime
     *   @return mTotalPlayTime 
     */
    public int getTotalPlayTime()
    {
        return mTotalPlayTime;
    }
    
    /**
     *   This API will get audio track mBitRate
     *   @return mBitRate 
     */
    public int getBiteRate()
    {
        return mBitRate;
    }
    
    /**
     *   This API will get audio track mYear
     *   @return mYear 
     */
    public String getYear()
    {
        return mYear;
    }
    
    /**
     *   This API will get audio track mSampleRate
     *   @return mSampleRate 
     */
    public int getSampleRate()
    {
        return mSampleRate;
    }
    
    /**
     *   This API will get audio track mTitle
     *   @return mTitle 
     */
    public String getTitle()
    {
         return mTitle;
    }
    
    /**
     *   This API will get audio track mAlbum
     *   @return mAlbum 
     */
    public String getAlbum()
    {
        return mAlbum;
    }
    
    /**
     *   This API will get audio track mArtist
     *   @return mArtist 
     */
    public String getArtist()
    {
        return mArtist;
    }
    
    /**
     *   This API will get audio track mChannelNumber
     *   @return mChannelNumber 
     */
    public int getChannelNumber()
    {
        return mChannelNumber;
    }
    
    /**
     *   This API will get audio track mGenre
     *   @return mGenre 
     */
    public String getGenre()
    {
        return mGenre;
    }
    
    /**
     *   This API will get audio track mDirector
     *   @return mDirector 
     */
    public String getDirector()
    {
        return mDirector;
    }
    
    /**
     *   This API will get audio track mCopyright
     *   @return mCopyright 
     */
    public String getCopyright()
    {
        return mCopyright;
    }
}
