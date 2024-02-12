package com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.media.ExifInterface;

import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ProcessPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;

import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
/**
 * This class defines Photo file
 *
  */
public class PhotoFile extends MtkFile {

	private static final String TAG = "PhotoFile";
	private static final long serialVersionUID = 111212L;



	/**
	 * Get the photo file with MtkFile
	 * @param f
	 *        MtkFile
	 */
	public PhotoFile(MtkFile f) {
		super(f.getPath());
	}

	/**
	 * Get the photo file with URI
	 * @param uri
	 */
	public PhotoFile(URI uri) {
		super(uri);
	}

	/**
	 * Get the photo file with directory path and name of the file
	 * @param dirPath
	 * @param name
	 */
	public PhotoFile(String dirPath, String name) {
		super(dirPath, name);
	}

	/**
	 * Get the photo file with path of the file
	 * @param path
	 */
	public PhotoFile(String path) {
		super(path);
	}

	/**
	 * Get the photo file with directory path and name of the file
	 * @param dir
	 * @param name
	 */
	public PhotoFile(File dir, String name) {
		super(dir, name);
	}

	/**
	 * Get resolution of the photo
	 */
	@Override
  public String getResolution() {
        int width;
        int height;

		/*if (!isValidPhoto()) {
			return null;
		}*/
		//if (width == 0 || height == 0)
		{
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

	/**
	 * If photo larger than 100M, unvalid.
	 * @return if true, valide. else unvalid
	 */
	private boolean isValidPhoto() {
		long size = this.getFileSize();

		return !(size > FileConst.MAX_PHOTO_SIZE || size <= 0);
	}

	@Override
  public Bitmap getThumbnail(int width, int height,boolean isThumbnail) {
		if(!isValidPhoto()){
			return null;
		}
		Bitmap bmp = null;
		//Bitmap smallBmp =null;
		try {
			MmpTool.logInfo("starting--------");
			if(width<height){
				width= height;
			}
			bmp = decodeBitmap(this, width,isThumbnail);
			MmpTool.logInfo("end-------- bmp = " + bmp);
		} catch (OutOfMemoryError e) {
			MmpTool.logError("OutOfMemoryError!!!");
		}
		return bmp;
	}

	private static BitmapFactory.Options opt = new BitmapFactory.Options();

	private Bitmap decodeBitmap(File file, int requiredSize,boolean isThumbnail) {
		if (null == file) {
			return null;
		}
		Bitmap bmp = null;
		try {
//			ExifInterface exif = new ExifInterface(file.getAbsolutePath());
//			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
//            MtkLog.d(TAG,"decodeBitmap,exif.hasThumbnail()=="+exif.hasThumbnail());
//			if(exif.hasThumbnail() && isThumbnail){
//				byte[] thumbnail = exif.getThumbnail();
//                MtkLog.d(TAG,"decodeBitmap,thumbnail=="+thumbnail);
//                if (null != thumbnail){
//				    bmp = BitmapFactory.decodeByteArray(thumbnail, 0, thumbnail.length);
//                }
//
//                MtkLog.d(TAG,"decodeBitmap,bmp=="+bmp);
//			}

//            if (bmp == null){
    			opt.mCancel = false;
    			opt.inJustDecodeBounds = true;
				opt.inPreferredConfig = Config.RGB_565;
				opt.inDither = true;
    			opt.inSampleSize = 1;
    			BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
    			int width = opt.outWidth;
    			int height = opt.outHeight;
    			MmpTool.logInfo( "base Width = "+ width +"Height = "+ height);
                if (width > ConstPhoto.MAX_8K_WIDTH){
                    //thumbnail will not very clear
    			    opt.inSampleSize = computeSampleSize((width>height ? width :height),requiredSize);
                }
    			MmpTool.logInfo( "inSampleSize = "+ opt.inSampleSize);
    			opt.inJustDecodeBounds = false;
//    			opt.inPreferQualityOverSpeed = true;
    			//opt.inScaled = true;
    			//opt.inDensity = width/opt.inSampleSize;
                int inDensity = width/opt.inSampleSize;
                int inTargetDensity = 0;
    			if((width*1000 / requiredSize) > (height*1000 /requiredSize)){
    				//opt.inTargetDensity = requiredSize;
    				inTargetDensity = requiredSize;
    			}else{
    				//opt.inTargetDensity = width*requiredSize /height;
    				inTargetDensity = width*requiredSize /height;
    			}
                MmpTool.logInfo( "inSampleSize = "+opt.inSampleSize+",inTargetDensity="+inTargetDensity);
    			bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
                if (null == bmp) {
                    MmpTool.logError( "bmp is null");
                    opt.requestCancelDecode();
                    return null;
                }
                float scale = 0;
                if( inDensity != 0 )
                {
                    scale = (float)inTargetDensity/inDensity;
                    MmpTool.logInfo("scale = "+scale);
                    if( scale != 0 )
                    {
                        Matrix matrix = new Matrix();
                        matrix.preScale(scale,scale);
                        Bitmap scalebmp = Bitmap.createBitmap(bmp,0,0,opt.outWidth,opt.outHeight,matrix,false);
                        MmpTool.logInfo( "outWidth = "+opt.outWidth+",outHeight="+opt.outHeight);
                        if (null == scalebmp)
                        {
                            MmpTool.logError( "scalebmp is null");
                            opt.requestCancelDecode();
                            return null;
                        }
                        MmpTool.logInfo("use scalebmp");
                        bmp = scalebmp;
                    }
                }

//    		}
//			if (null == bmp) {
//				MmpTool.logError( "Thumbnailimage is null");
//				opt.requestCancelDecode();
//				return null;
//			}
//			if (orientation > 1 && orientation <=8) {
//			  //rotate(bmp, orientation);
//				bmp = new ProcessPhoto().rotate(bmp,Const.ORIENTATION_ARRAY[orientation]);
//			}
			MmpTool.logInfo( "Thumbnailimage Wid = "+ bmp.getWidth() +" height = "+ bmp.getHeight());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return bmp;
	}


	private int computeSampleSize(int maxbmp,int dsize){
		if(maxbmp <= dsize ){
			return 1;
		}
		int initialSize = maxbmp/dsize+ (maxbmp%dsize>0? 1:0);
		int roundedSize;
        if (initialSize <= 8 ) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
	}

	/**
	 * Create a 2>>N pixels target bitmap from source bitmap.
	 *
	 * @param b
	 *            source bitmap
	 * @param size
	 *            eg: 128,256,512
	 * @return target bitmap
	 */
	public static Bitmap scaleBitmap(Bitmap source, int size) {
		if (source == null) {
			return source;
		}
		Matrix matrix = new Matrix();
		int width = source.getWidth();
		int height = source.getHeight();
		float sx = (float) size / height;

		if (width > height) {
			sx = (float) size / width;
		}
		matrix.setScale(sx, sx);
		try {
			Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source
					.getHeight(), matrix, false);

			if (!bitmap.equals(source)) {
				source.recycle();
				MmpTool.logInfo("source recycle ? "+source.isRecycled());
			}

			Bitmap bitmap1 = Bitmap.createBitmap(size, size,
					Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap1);
			float left = 0;
			float right = 0;
			int width1 = bitmap.getWidth();
			int height1 = bitmap.getHeight();
			if (width1 < size) {
				left = (size - width1) / 2;
			}
			if (height1 < size) {
				right = (size - height1) / 2;
			}
			canvas.drawBitmap(bitmap, left, right, null);

			bitmap.recycle();
			MmpTool.logInfo("bitmap recycle ? "+bitmap.isRecycled());

			return bitmap1;
		} catch (OutOfMemoryError e) {
			MmpTool.logError("OutOfMemoryError!!!");
		}
		return null;
	}

	public void stopDecode() {
		// MmpTool.logInfo("STOP Entered 000000000000000");
		// if (null != opt) {
		// MmpTool.logInfo("STOP Starting:1111111111111");
		//
		// //add by shuming for fix bug DTV00380813
		//
		// StopTask stopDecodePhoto = new StopTask();
		// stopDecodePhoto.execute();
		//
		// MmpTool.logInfo("STOP Ending:2222222222222");
		//
		// opt.mCancel = false;
		// MmpTool.logInfo("opt.mCancel" + opt.mCancel);
		//
		// }
	}

	/**
	 * Stop to decode thumbnail
	 */
	@Override
  public void stopThumbnail(){
		//TODO
		stopDecode();
	}

	//private int degree = 0;


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
			if ( !resizedBitmap.equals(bm)) {
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
