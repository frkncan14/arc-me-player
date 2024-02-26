package com.mstar.android.samba;

public class SambaFile {

    public SambaFile() {
    };
    
    public SambaFile(String filename) {
    }

    public String getPath() {
      return "";
    }

    public boolean isFile() {
      return false;
    }

    public boolean isDirectory(){
      return false;
    }

    public String getFileName() {
      return "";
    }

    public long getLength() {
      return 0;
    }
    
    public boolean canRead() {
      return false;
    }

    public boolean canWrite() {
      return false;
    }

    public boolean isExists() {
        return false;
    }
}
