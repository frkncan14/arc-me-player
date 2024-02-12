package com.mediatek.wwtv.mediaplayer.netcm.wifi;

/**
 * This class use to notify the dongle plug-in and out action which called
 * by WifiDevManager.
 *
 */
public interface WifiDevListener {

	/**
	 * Notify the application wifi dongle found.
	 *
	 */
    void notifyWifiDevFound(WifiDevEvent event);

	/**
	 * Notify the application wifi dongle left.
	 *
	 */
    void notifyWifiDevLeft(WifiDevEvent event);

}

