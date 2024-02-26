package com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;
import java.util.List;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.image.Tools;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.ILyric;
import com.mediatek.wwtv.tvcenter.util.MtkLog;

public class Lyric implements ILyric {

	private String lyricAlbum;
	private String lyricArtist;
	private String lyricEditor;
	private String lyricTitle;
	private long lyricTimeOffset;
	private List<LyricTimeContentInfo> lyricTimeContentInfo;
	private static final String TAG = "Lyric";
   /**
    * Construction method
    * @param path
    */
	public Lyric(String path) {
		boolean result = false;

		lyricAlbum = null;
		lyricArtist = null;
		lyricEditor = null;
		lyricTitle = null;
		lyricTimeOffset = 0;
		lyricTimeContentInfo = null;

		result = parseLyricFile(path);
		if (result == false) {
			lyricAlbum = null;
			lyricArtist = null;
			lyricEditor = null;
			lyricTitle = null;
			lyricTimeOffset = 0;
			lyricTimeContentInfo = null;
		}
	}

	private int parser(String str, List<String> lyricContent,
			List<Long> lyricTime, int result) {
		String subStr = null;
		int bPos = 0;
		int mPos = 0;
		int ePos = 0;
		int index = 0;
		int count = 0;
		int step = 0;

		str = str.trim();
		while (str.length() != 0) {
			bPos = str.indexOf("[", index);
			mPos = str.indexOf(":", index);
			ePos = str.indexOf("]", index);

			if (bPos != -1 && bPos < mPos && mPos < ePos) {
				step += ePos - bPos + 1;
				subStr = str.substring(bPos + 1, mPos);
				if ("ti".equals(subStr)) {
					lyricTitle = str.substring(mPos + 1, ePos);
				} else if ("ar".equals(subStr)) {
					lyricArtist = str.substring(mPos + 1, ePos);
				} else if ("al".equals(subStr)) {
					lyricAlbum = str.substring(mPos + 1, ePos);
				} else if ("by".equals(subStr)) {
					lyricEditor = str.substring(mPos + 1, ePos);
				} else if ("offset".equals(subStr)) {
					String offsetSubString = str.substring(mPos + 1, ePos);
					try {
						lyricTimeOffset = Long.parseLong(offsetSubString);
					} catch (NumberFormatException ex) {
					    MtkLog.e(TAG, "Parser offsetSubString: " + offsetSubString);
					    ex.printStackTrace();
					}
				} else if ("la".equals(subStr) || "ver".equals(subStr)) {
				    MtkLog.d(TAG, "do nothing");
                    // do not anything.
				} else {
					long time = 0;
					long min = 0;
					long sec = 0;
					long msec = 0;
					int pos = 0;

					try {
						min = Long.parseLong(subStr);
						subStr = str.substring(mPos + 1, ePos);

						pos = subStr.indexOf(".");
						if (pos > 0) {
							sec = Long.parseLong(subStr.substring(0, 2));
							msec = Long.parseLong(subStr.substring(pos + 1,
									pos + 3));
						} else {
							sec = Long.parseLong(subStr.substring(0, 2));
						}

						time = (min * 60 + sec) * 1000 + msec + lyricTimeOffset;
						((Vector)lyricTime).addElement(time);
						count++;

					} catch (NumberFormatException ex) {
					    MtkLog.e(TAG, "Parser subStr: " + subStr);
					    ex.printStackTrace();
						// return -1;
					}
				}

				index = ePos + 1;
			} else {
				if (step > 0) {
					break;
				}
				else {
					if (result > 0) {
						int size = lyricContent.size();
						String temp = ((Vector)lyricContent).lastElement() + str;
						index = size - result;

						for (int i = 0; i < result; i++) {
							lyricContent.set(index + i, temp);
						}

						return result;
					}
					else {
						return -1;
					}

				}

			}
		}

		if (count > 0) {
			if (str.length() > step) {
				subStr = str.substring(step, str.length());
			} else {
				subStr = "";
			}

			for (int i = 0; i < count; i++) {
				((Vector)lyricContent).addElement(subStr);
			}
		}

		return count;
	}

	private String getCharset(String filePath) {
		String charset = "GBK";
		byte[] first3Bytes = new byte[3];
		InputStream fis = null;
        BufferedInputStream bis = null;
		try {
			boolean checked = false;
			if (Tools.isNetPlayback(filePath)) {
				try {
                    fis = new URL(filePath).openStream();
                } catch (MalformedURLException e) {
                     e.printStackTrace();
                } catch (IOException e) {
                     e.printStackTrace();
                }
                if (fis == null) {
                    return null;
                }
                bis = new BufferedInputStream(fis);
			} else {
				fis = new FileInputStream(filePath);
				bis = new BufferedInputStream(fis);
			}
			bis.mark(1);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1){
				return charset;
				}
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
					&& first3Bytes[2] == (byte) 0xBF) {
				charset = "UTF-8";
				checked = true;
			}
			bis.reset();
			if (!checked) {
				while ((read = bis.read()) != -1) {
					if (read >= 0xF0){
						break;
						}
					if (0x80 <= read && read <= 0xBF){
						break;
						}
					if (0xC0 <= read && read <= 0xDF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF){
							continue;
							}
						else{
							break;
							}
					} else if (0xE0 <= read && read <= 0xEF) {
						read = bis.read();
						if (0x80 <= read && read <= 0xBF) {
							read = bis.read();
							if (0x80 <= read && read <= 0xBF) {
								charset = "UTF-8";
								break;
							} else{
								break;
								}
						} else{
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

            if(bis != null){
				try {
					bis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return charset;
	}

	private boolean parseLyricFile(String path) {
		int result = 0;
		InputStream fileByte = null;
		InputStreamReader fileChar = null;
		int readChar = 0;
		String str = "";
		Vector<String> lyricContent = new Vector<String>();
		Vector<Long> lyricTime = new Vector<Long>();
		try {
			if (Tools.isNetPlayback(path)) {
				fileByte = new URL(path).openStream();
			} else {
				fileByte = new FileInputStream(path);
			}
			fileChar = new InputStreamReader(fileByte, getCharset(path));

			while ((readChar = fileChar.read()) != -1
					|| str.length() != 0) {
				if (readChar != -1 && readChar != 13 && readChar != 10) {
					str = str.concat("" + (char) readChar);
				} else {
					if (str.length() != 0) {
						result = parser(str, lyricContent, lyricTime, result);
						if (result == -1) {
							return false;
						}

						str = "";
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (null!=fileChar) {
					fileChar.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (null!=fileByte) {
						fileByte.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		int lyricTimeSize = lyricTime.size();
		int lyricContentSize = lyricContent.size();
		if (lyricTimeSize > 0 && lyricTimeSize == lyricContentSize) {
			Vector<Long> lyricTimeClone = (Vector<Long>) lyricTime.clone();
			Collections.sort(lyricTimeClone);

			lyricTimeContentInfo = new Vector<LyricTimeContentInfo>();

			long currentLineStartTime = 0;
			String currentLineLyricContent = null;
			long nextLineStartTime = 0;

			for (int i = 0; i < lyricContentSize; i++) {
				currentLineStartTime = (lyricTime.get(i)).longValue();
				currentLineLyricContent = lyricContent.get(i);

				LyricTimeContentInfo timeContentInfo = new LyricTimeContentInfo(
						currentLineStartTime, currentLineLyricContent);
				lyricTimeContentInfo.add(timeContentInfo);
			}

			Collections.sort(lyricTimeContentInfo);

			int i = 0;
			for (; i < lyricContentSize - 1; i++) {
				nextLineStartTime = (lyricTimeClone.get(i + 1)).longValue();
				lyricTimeContentInfo.get(i).setLyricTimeContentInfo(
						nextLineStartTime);
			}

			lyricTimeContentInfo.get(i).setLyricTimeContentInfo(0xffffffffL);

		}

		return true;

	}
	/**
	 * Get lyrice album.
	 * @return
	 */
	public String getLyricAlbum() {
		// TODO Auto-generated method stub
		return lyricAlbum;
	}
	/**
	 * Get lyrice artist.
	 * @return
	 */
	public String getLyricArtist() {
		// TODO Auto-generated method stub
		return lyricArtist;
	}
	/**
	 * Get lyrice editor.
	 * @return
	 */
	public String getLyricEditor() {
		// TODO Auto-generated method stub
		return lyricEditor;
	}
	/**
	 * Get lyrice time offset.
	 * @return
	 */
	public long getLyricTimeOffset() {
		// TODO Auto-generated method stub
		return lyricTimeOffset;
	}
	/**
	 * Get lyrice title.
	 * @return
	 */
	public String getLyricTitle() {
		// TODO Auto-generated method stub
		return lyricTitle;
	}
	/**
	 * Get lyrice time content inforation.
	 * @return
	 */
	public List<LyricTimeContentInfo> getLyricTimeContentInfo() {
		return lyricTimeContentInfo;
	}
	/**
	 * According to the parameters set lyric timeOffset
	 * @param timeOffset
	 */
	public void setLyricTimeOffset(long timeOffset) {
		if (timeOffset != lyricTimeOffset) {
			lyricTimeOffset = timeOffset;

			if (lyricTimeContentInfo != null) {
				int index = 0;
				while (index < lyricTimeContentInfo.size()) {
					LyricTimeContentInfo tmp = lyricTimeContentInfo.get(index);
					tmp.setStartTime(tmp.getStartTime() + lyricTimeOffset);
					tmp.setEndTime(tmp.getEndTime() + lyricTimeOffset);

					index++;
				}
			}

		}
	}
	/**
	 * Get line by specified time.
	 * @param time
	 * @return int time line
	 */
	public int getLine(long time) {
		if (lyricTimeContentInfo != null) {
			int line = 0;
			long min = ((Vector<LyricTimeContentInfo>)lyricTimeContentInfo).firstElement().getStartTime();
			if (time < min) {
				return -1;
			} else {
				while (line < lyricTimeContentInfo.size()) {
					if (time >= lyricTimeContentInfo.get(line).getEndTime()) {
						line++;
					} else {
						break;
					}
				}

				return line;
			}
		} else {
			return -2;
		}
	}

}
