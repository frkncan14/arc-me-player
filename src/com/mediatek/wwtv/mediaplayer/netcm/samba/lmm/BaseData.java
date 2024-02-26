package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.mediatek.wwtv.mediaplayer.util.Constants;

/**
 * Data provide foundation class（photo Music Video Other search）.
 */
public class BaseData implements Parcelable {
    //video's thumbnail(may be used to other,e.q music ,photo)
    private Drawable thumbnail;
    // file name
    private String name;

    // file AP(absolute path)
    private String path;

    // parent AP(absolute path)
    private String parentPath;

    // file size
    private String size;

    // file format
    private String format;

    // dile description
    private String description;

    // artist
    private String artist;

    // title
    private String title;

    // modifyTime
    private long modifyTime;

    // Music files in the database of the key word
    private long id = 0;

    // Album art id
    private long album;

    // file type
    private int type;

    // Image resources id
    private int icon;

    private int _duration;

    // Duration, in seconds for the unit
    private String duration = "";

    private long fileSize = 0;

    public int idxOfArr = 0;

    public BaseData() {
    }

    public BaseData(int type) {
        this.type = type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the parentPath
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * @param parentPath the parentPath to set
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * @return the size
     */
    public String getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * @return the size
     */
    public long getSizeEx() {
        return fileSize;
    }

    /**
     * @param size the size to set
     */
    public void setSizeEx(final long sz) {
        this.fileSize = sz;
    }


    /**
     * @return the format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    public void setDuration(int duration) {
        this._duration = duration;
    }

    public int getDuration() {
        return _duration;
    }

    public String getDuration2() {
        return duration;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the modifyTime
     */
    public long getModifyTime() {
        return modifyTime;
    }

    /**
     * @param modifyTime the modifyTime to set
     */
    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    /**
     * @return the icon
     */
    public int getIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(int icon) {
        this.icon = icon;
    }
    public Drawable getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(Drawable thumbnail) {
        this.thumbnail= thumbnail;
    }
    /**
     * @return the artist
     */
    public String getArtist() {
        return artist;
    }

    /**
     * @param artist the artist to set
     */
    public void setArtist(String artist) {
        this.artist = artist;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the album
     */
    public long getAlbum() {
        return album;
    }

    /**
     * @param album the album to set
     */
    public void setAlbum(long album) {
        this.album = album;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(size);
        dest.writeString(format);
        dest.writeString(artist);
        dest.writeLong(modifyTime);
        dest.writeLong(id);
        dest.writeLong(album);
        dest.writeInt(_duration);

    }

    public static final Parcelable.Creator<BaseData> CREATOR = new Parcelable.Creator<BaseData>() {
        @Override
        public BaseData createFromParcel(Parcel source) {
            BaseData file = new BaseData(Constants.FILE_TYPE_PICTURE);
            file.name = source.readString();
            file.path = source.readString();
            file.size = source.readString();
            file.format = source.readString();
            file.artist = source.readString();
            file.modifyTime = source.readLong();
            file.id = source.readLong();
            file.album = source.readLong();
            file._duration = source.readInt();

            return file;
        }

        @Override
        public BaseData[] newArray(int size) {
            return new BaseData[size];
        }
    };

}
