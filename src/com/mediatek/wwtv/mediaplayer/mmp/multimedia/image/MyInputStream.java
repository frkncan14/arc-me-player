package com.mediatek.wwtv.mediaplayer.mmp.multimedia.image;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.util.Log;

public class MyInputStream extends BufferedInputStream {
    private String imagePath;

    public MyInputStream(InputStream in, String Path) {
        super(in);
        imagePath = Path;
    }

    @Override
    public synchronized void reset() throws IOException {
        InputStream is = new URL(imagePath).openStream();
        if (is != null) {
            this.close();

            in = is;

            count = 0;
            marklimit = 0;
            markpos = -1;
            pos = 0;
            buf = new byte[8192];

            int result = in.read(buf);
            if (result > 0) {
                markpos = -1;
                pos = 0;
                count = result;
            } else if (0 == result) {
                throw new IOException("The network is not available!");
            } else if (-1 == result) {
                throw new IOException("EOF!");
            } else {
                throw new IOException("Unknown error!");
            }
        } else {
            throw new IOException("Initialize inputStream  fail!");
        }
    }
}
