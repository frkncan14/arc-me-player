
package com.mediatek.wwtv.mediaplayer.netcm.dlna;

import java.io.IOException;
import android.util.Log;

import com.mediatek.dlna.object.Content;
import com.mediatek.dlna.object.ContentInputStream;

/**
 * This Class is the Content object streaming, use to download the content resource.
 *
 */
public class DLNAInputStream extends ContentInputStream {

  public DLNAInputStream(Content content) {
    super(content);
  }

  public int read() throws IOException {
    Log.d("DLNAInputStream","read");
    return super.read();
  }

  public int read(byte[] b) throws IOException {
    Log.d("DLNAInputStream","read");
    return super.read(b);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    Log.d("DLNAInputStream","read");
    return super.read(b, off, len);
  }

  public long skip(long n) throws IOException {
    Log.d("DLNAInputStream","skip");
    return super.skip(n);
  }

  public int available() throws IOException {
    Log.d("DLNAInputStream","available");
    return super.available();
  }

  public void close() throws IOException {
    Log.d("DLNAInputStream","close");
    super.close();
  }

  public void reset() throws IOException {
    Log.d("DLNAInputStream","reset");
    super.reset();
  }

  public void mark(int readlimit) {
    Log.d("DLNAInputStream","mark");
    super.mark(readlimit);
  }

  public boolean markSupported() {
    Log.d("DLNAInputStream","markSupported");
    return super.markSupported();
  }

  public long size() {
    Log.d("DLNAInputStream","size");
    return super.size();
  }
}
