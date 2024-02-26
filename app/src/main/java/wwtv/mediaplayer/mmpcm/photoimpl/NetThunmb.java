package com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import jcifs.smb.SmbException;

import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNADataSource;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;

public class NetThunmb {
	private static final int BUFFER_SIZE = 1024 * 128;
    private static final String TAG = "NetThunmb";

	private int computeSampleSize(InputStream in, int requiredSize) {
		int sampleSize = -1;

		BitmapFactory.Options opts = new BitmapFactory.Options();
		decodeInJustBounds(in, opts);

		if (opts.outHeight > requiredSize || opts.outWidth > requiredSize) {
			sampleSize = (int) Math.pow(2.0, (int) Math.round(Math
					.log(requiredSize
							/ (double) Math.max(opts.outHeight, opts.outWidth))
					/ Math.log(0.5)));
		} else {
			sampleSize = 1;
		}

		MmpTool.logDbg( "Sample Size : " + sampleSize);
		return sampleSize;
	}

	private File saveAsTemp(InputStream in) throws IOException,
			FileNotFoundException {
		File temp = File.createTempFile("image_", null, sTempFolder);
		// BufferedOutputStream out = new BufferedOutputStream(
		// new FileOutputStream(temp), BUFFER_SIZE);
		FileOutputStream out = new FileOutputStream(temp);
		byte[] bytes = new byte[BUFFER_SIZE];

        try {
			int read = in.read(bytes);
			while (read >= 0) {
            	out.write(bytes);
            	read = in.read(bytes);
         	}
          out.flush();
          out.close();
          in.close();
        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if (null != out) {
            try {
              out.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }

		return temp;
	}

	private InputStream getInputStream() {
		InputStream input = null;
		if (LocOrNet == ConstPhoto.SAMBA) {
			try {
				input = SambaManager.getInstance().getSambaDataSource(netPath)
						.newInputStream();
			} catch (SmbException e) {
				// TODO Auto-generated catch block
				MtkLog.d(TAG,"SmbException");
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				MtkLog.d(TAG,"MalformedURLException");
				e.printStackTrace();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				MtkLog.d(TAG,"UnknownHostException");
				e.printStackTrace();
			}
		} else if (LocOrNet == ConstPhoto.DLNA) {
			DLNADataSource dlnaDataSource = DLNAManager.getInstance().getDLNADataSource(netPath);
			if(dlnaDataSource != null) {
				input = dlnaDataSource.newContentInputStream();
			}
		}
		return input;
	}

	private Bitmap decodeBitmap(InputStream in, int width, int height) {
		Bitmap bmp = null;
		if (in != null) {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = computeSampleSize(in, width);
			InputStream inputStream = null;
			Bitmap original = null;
			try {
				inputStream = getInputStream();
				original = decodeBitmap(inputStream, o);
				if (original != null) {
					bmp = ThumbnailUtils.extractThumbnail(original, width, height,
							ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
					return bmp;
				}
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				in = null;
			}
		}

		return null;
	}
	/**
	 * Get thumbnail to sepcified width and height.
	 * @param width
	 * @param height
	 * @return
	 */
	public Bitmap getThumbnail(int width, int height) {
		Bitmap bmp = null;
		InputStream input = null;
        try {
            input = getInputStream();
			if(input != null) {
				bmp = decodeBitmap(input, width, height);
				input.close();
				return bmp;
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally {
          if (null != input) {
            try {
              input.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }

		return null;
	}

	public static File sTempFolder;

	static {
		sTempFolder = new File(ConstPhoto.TempFolderPath);

		if (!sTempFolder.exists()) {
			sTempFolder.mkdir();
		}
	}

	private Bitmap decodeBitmap(InputStream in, Options opts) {
		Bitmap bitmap = null;

		if (in != null) {
			bitmap = BitmapFactory.decodeStream(in, null, opts);

			if (bitmap == null) {
				try {
					in.close();
					in = getInputStream();
					if(in != null) {
						File temp = saveAsTemp(in);
						// bitmap = BitmapFactory.decodeStream(new FileInputStream(
						// temp), null, opts);
						bitmap = BitmapFactory.decodeFile(temp.getAbsolutePath(),
								opts);
						temp.delete();
						return bitmap;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try{
						in.close();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	private int width;
	private int height;

	private void decodeInJustBounds(InputStream in, Options opts) {
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, opts);
		width = opts.outWidth;
		height = opts.outHeight;

		if (opts.outWidth <= 0) {
			try {
				if(in != null) {
					in.close();
				}
				in = getInputStream();
				if(in != null) {
					File temp = saveAsTemp(in);
					// BitmapFactory.decodeStream(new FileInputStream(temp),
					// null,opts);
					BitmapFactory.decodeFile(temp.getAbsolutePath(), opts);
					width = opts.outWidth;
					height = opts.outHeight;
					temp.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Get resolution.
	 * @return
	 */
	public String getResolution() {
		String resolution = null;
		if (width == 0 || height == 0) {
            InputStream in = null;
            try {
                in = getInputStream();
    			if (in != null) {
    				BitmapFactory.Options opts = new BitmapFactory.Options();
    				decodeInJustBounds(in, opts);

    				resolution = new StringBuffer().append(opts.outWidth).append("*")
    						.append(opts.outHeight).toString();
					in.close();
					return resolution;
    			}
			} catch(Exception e){
				e.printStackTrace();
			} finally {
              if (null != in) {
                try {
                  in.close();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }
            }
		}else {
			resolution = new StringBuffer().append(width).append("*")
			.append(height).toString();
			return resolution;
		}
		return null;
	}

	private int LocOrNet;
	/**
	 * Set net or local mode.
	 * @param i
	 */
	public void setLocOrNet(int i) {
		LocOrNet = i;
	}

	private String netPath;
	/**
	 * Set play path when net mode.
	 * @param path
	 */
	public void setNetPath(String path) {
		netPath = path;

		MmpTool.logInfo(netPath);
	}
}
