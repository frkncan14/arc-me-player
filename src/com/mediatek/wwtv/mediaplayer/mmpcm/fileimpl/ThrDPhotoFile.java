package com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ProcessPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;


public class ThrDPhotoFile extends MtkFile{

	/**
	 *
	 */
	private static final String TAG = "ThrDPhotoFile";
	private static final long serialVersionUID = 1L;

	public ThrDPhotoFile(MtkFile f) {
		super(f.getPath());

	}
	public ThrDPhotoFile(URI uri) {
		super(uri);
	}

	public ThrDPhotoFile(String dirPath, String name) {
		super(dirPath, name);
	}

	public ThrDPhotoFile(String path) {
		super(path);
	}

	public ThrDPhotoFile(File dir, String name) {
		super(dir, name);
	}

	public String getResolution(){

		/*if (!isValidPhoto()){
			return null;
		}*/
		if (width == 0 || height == 0) {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			o.inSampleSize = 1;
			BitmapFactory.decodeFile(this.getAbsolutePath(), o);
			width = o.outWidth;
			height = o.outHeight;
		}

		return new StringBuffer().append(width).append("*").append(height)
				.toString();
	}

	private int width;
	private int height;

	/**
	 * if photo larger than 20M, unvalid.
	 * @return if true, valide. else unvalid
	 */
	/*private boolean isValidPhoto(){
		if (this.getFileSize() > 20*1024*1024){
			return false;
		}
		return true;
	}*/


	public Bitmap getThumbnail(int width, int height,boolean isThumbnail){

		Bitmap bmp = null;

		try {

			MmpTool.logInfo("starting--------");
			/*if (!isValidPhoto()){
				return null;
			}*/
			bmp = decodeBitmap(this, width);
//			bmp = ThumbnailUtils.extractThumbnail(bmp, width, height,
//					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		} catch (OutOfMemoryError e) {
			MmpTool.logError("OutOfMemoryError!!!");
		}
		return bmp;
	}

	private static BitmapFactory.Options opt = new BitmapFactory.Options();
	private Bitmap decodeBitmap(ThrDPhotoFile thrDPhotoFile, int requiredSize) {
		Bitmap bmp = null;

		opt.mCancel = false;
		Log.d(TAG, "opt.mCancel" + opt.mCancel);
		if (thrDPhotoFile != null) {
			// Decode image size
			if (width == 0 || height == 0) {
				opt.inJustDecodeBounds = true;
				opt.inSampleSize = 1;
				MmpTool.logInfo("thumbnial_decoding 2222222222222222222");
				Log.d(TAG, thrDPhotoFile.getAbsolutePath());
				BitmapFactory.decodeFile(this.getAbsolutePath(), opt);
				width = opt.outWidth;
				height = opt.outHeight;
				MmpTool.logInfo("width = " + opt.outWidth +";" + " height = "+ opt.outHeight);
			}
			// Find the correct scale value. It should be the power of 2.
			if (height > requiredSize || width > requiredSize) {
				int scale = (int) Math.pow(2.0, (int) Math.round(Math
						.log(requiredSize / (double) Math.max(height, width))
						/ Math.log(0.5)));
				// Decode with inSampleSize
				opt.inJustDecodeBounds = false;
				opt.inSampleSize = scale;
				MmpTool.logInfo("thumbnial_decoding 3333333333333333");
				MmpTool.logInfo(thrDPhotoFile.getAbsolutePath());
				bmp = BitmapFactory.decodeFile(thrDPhotoFile.getAbsolutePath(), opt);
			} else {
				opt.inJustDecodeBounds = false;
				opt.inSampleSize = 1;
				MmpTool.logInfo("thumbnial_decoding 4444444444444444444");
				MmpTool.logInfo(thrDPhotoFile.getAbsolutePath());
				bmp = BitmapFactory.decodeFile(thrDPhotoFile.getAbsolutePath(), opt);
			}
		}

		if (getOrientation() != -1) {

			if (getOrientation() == 1) {
				return bmp;
			} else {
				int orientation = getOrientation();
				if (null != bmp) {
					bmp = new ProcessPhoto().rotate(bmp,Const.ORIENTATION_ARRAY[orientation]);//rotate(bmp, orientation);
				}
			}
		}

		return bmp;
	}

	public void stopDecode() {
		MmpTool.logInfo("STOP Entered 000000000000000");
	if (null != opt) {
		MmpTool.logInfo("STOP Starting:1111111111111");
		opt.requestCancelDecode();
		MmpTool.logInfo("STOP Ending:2222222222222");

		opt.mCancel = false;
		MmpTool.logInfo("opt.mCancel" + opt.mCancel);

		}
	}

	public void stopThumbnail(){
		//TODO
		stopDecode();
	}
	//private int degree = 0;

	private int getOrientation() {
		//degree = 0;
		ExifInterface exif = null;

		if (null == this.getAbsoluteFile()) {
			Log.d(TAG, "curPath is null!!!");
		}

		try {

			exif = new ExifInterface(this.getAbsolutePath());

			MmpTool.logInfo("curPath = " + this.getAbsolutePath());
		} catch (IOException ex) {
			Log.e(TAG, "cannot read exif", ex);
			return -1;
		}
		int orientation = -1;
		if (exif != null) {
				 orientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION, -1);
			/*if (orientation != -1) {
				// We only recognize a subset of orientation tag values.
				switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
				}

		}*/
		}

		return orientation;
	}
    /*
	private Bitmap rotate(Bitmap bm, int dg) {

		int widthOrig = bm.getWidth();
		int heightOrig = bm.getHeight();

		int newWidth = widthOrig;
		int newHeight = heightOrig;
		float scaleWidth = ((float) newWidth) / widthOrig;
		float scaleHeight = ((float) newHeight) / heightOrig;

		Matrix rotatematrix = new Matrix();
		rotatematrix.postScale(scaleWidth, scaleHeight);

		rotatematrix.setRotate(dg);

		try {
			Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, widthOrig,
					heightOrig, rotatematrix, true);
			if (!resizedBitmap.equals(bm)) {
				bm.recycle();
				bm = resizedBitmap;

			}
		} catch (OutOfMemoryError e) {
			MmpTool.logError("OutOfMemoryError");
		}

		return bm;
	}
	*/


}
