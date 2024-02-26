package com.mediatek.wwtv.mediaplayer.mmpcm.device;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.mediatek.dm.Device;
import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.dm.DeviceManagerListener;
import com.mediatek.dm.MountPoint;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity.MyDevListener;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import android.util.Log;

/**
 *
 *This class represents manager local device.
 *
 */

public final class DevManager {
    private static DevManager dman = null;

    private final DeviceManager dm = null;
    private final List<DevListener> onDevListener;
    static private final String TAG = "DevManager";
    /** the isMounted is used about refresh listView in MtkFilesBaseListActivity after power off to on. */
    private boolean isMounted = false;

    private DevManager(){
        onDevListener = new ArrayList<DevListener>();
        Log.d(TAG,"DevManager");
        dm.addListener(dmListener);
    }
    /**
     * Get device manager instance.
     * @return
     */
    public static synchronized DevManager getInstance(){
        if (dman == null) {
            Log.d(TAG,"dman == null");
            dman = new DevManager();
        }
        return dman;
    }
    /**
     * Get mount point count.
     * @return
     */
    public int getMountCount(){
        return dm.getMountPointCount();
    }
    /**
     * Get mount point list.
     * @return
     */
    public List<MountPoint> getMountList(){
        return dm.getMountPointList();
    }
    /**
     * Get mount point info by specified path.
     * @param path
     * @return
     */
    public MountPoint getPointInfo(String path){
        return dm.getMountPoint(path);
    }
    /**
     * Add a device notify listenr.
     * @param devListener
     */
    public void addDevListener(DevListener devListener){
        Log.d(TAG,"addDevListener,devListener=="+devListener);
        onDevListener.add(devListener);
    }

    /**
     * Remove a device listener by specified device listener.
     * @param devListener
     */
    public void removeDevListener(DevListener devListener){
        Log.d(TAG,"removeDevListener,devListener=="+devListener);
        onDevListener.remove(devListener);
    }

    public void removeDevListeners(){
      Log.d(TAG,"removeDevListeners");
      if (VideoPlayActivity.getInstance() != null
          && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
//        Util.LogResRelease("DevManager removeDevListeners is pip:" + onDevListener.size());
        for (int i = 0; i < onDevListener.size(); i++) {
          if (!(onDevListener.get(i) instanceof MyDevListener)) {
            onDevListener.remove(i);
            i--;
          }
        }
//        Util.LogResRelease("DevManager removeDevListeners is pip t: " + onDevListener.size());
      } else {
        onDevListener.clear();
      }
    }
	/**
	 * Mount a iso file by specified iso file path.
	 * @param isoFilePath
	 */
    public void mountISOFile( String isoFilePath)
    {
    	dm.mountISO(isoFilePath);
    }
    /**
     * Unmount a iso file by specified iso file path.
     * @param isoMountPath
     */
    public void umoutISOFile(String isoMountPath)
    {
    	dm.umountISO(isoMountPath);
    }
    /**
     * Check the device whether is virtual device by specified path.
     * @param isMountPath
     * @return
     */
    public boolean isVirtualDev(String isMountPath)
    {
    	return dm.isVirtualDevice(isMountPath);
    }
    //end ISO
    /**
     * Unmount device by specified mount point
     * @param mountPoint
     */
    public void unMountDevice(MountPoint mountPoint){
    	unMountDevice(getDeviceName(mountPoint));
    }


    private void unMountDevice(String devName){
    	if (devName != null){
    		dm.umountDevice(devName);
    	}

    }

    private String getDeviceName(MountPoint mountPoint){
    	Device dv = dm.getParentDevice(mountPoint);
    	return dv != null ? dv.mDeviceName : null;
    }
    /**
     * destroy device manager and remove devices listenr.
     */
    public void destroy(){
    	Log.d(TAG,"destroy");
    	if (VideoPlayActivity.getInstance() != null
    	    && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
//    	  Util.LogResRelease("DevManager Destroy is pip");
//    	  Util.LogResRelease("DevManager destroy is pip:" + onDevListener.size());
    	  for (int i = 0; i < onDevListener.size(); i++) {
    	    if (!(onDevListener.get(i) instanceof MyDevListener)) {
    	      onDevListener.remove(i);
    	      i--;
    	    }
        }
//    	  Util.LogResRelease("DevManager destroy is piptt:" + onDevListener.size());
    	} else {
//    	  Util.LogResRelease("DevManager Destroy is not pip");
    	  dm.removeListener(dmListener);
    	  onDevListener.clear();
          synchronized(DevManager.class) {
              dman = null;
          }
    	}
    }

   private final DeviceManagerListener dmListener = new DeviceManagerListener(){

        public void onEvent(DeviceManagerEvent arg0) {
           Log.d(TAG, "DeviceManagerListener-->onEvent,Type==" + arg0.getType());

           // Örnek bir düzeltme
           int eventType = arg0.getType();

           if (eventType == DeviceManagerEvent.mounted) {
               MtkLog.d(TAG, "OnEvent-->mounted");
               isMounted = true;
           }

           Log.d(TAG, "OnEvent,onDevListener.size==" + onDevListener.size());
           if (!onDevListener.isEmpty()) {
               Iterator<DevListener> it = onDevListener.iterator();

               while (it.hasNext()) {
                   DevListener lis = it.next();
                   Log.d(TAG, "OnEvent,DevListener==" + lis);
                   lis.onEvent(arg0);
                   Log.d(TAG, "onEvent end");
               }
           }
       }
    };

    public void setMount(boolean mounted) {
        this.isMounted = mounted;
    }

    public boolean getMount() {
        return isMounted;
    }
}
