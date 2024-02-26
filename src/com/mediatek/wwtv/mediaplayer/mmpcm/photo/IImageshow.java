package com.mediatek.wwtv.mediaplayer.mmpcm.photo;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.util.HashMap;

public interface IImageshow {

	// Iphotoinfo
	int getOrientation();

	int getPheight();

	int getPwidth();

	String getName();

	String getSize();

	String getMake();

	String getModel();

	String getFlash();

	String getWhiteBalance();

	HashMap<String, String> getAllExifInterfaceInfo();
//	public String setPhotoFrameImage();

	// Iplay

	void setDuration(int interval);

	int getDuration();


	// Izoom
	int getZoomOutSize();

	float getZoomInSize();

	Bitmap rightRotate(Bitmap bitmap);

	Bitmap leftRotate(Bitmap bitmap);

	void zoom(ImageView image, int inOrOut, Bitmap bitmap, float size);

	// Imove
	Bitmap moveImage();
}
