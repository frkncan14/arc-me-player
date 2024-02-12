/**
 *
 */
package com.mediatek.wwtv.tvcenter.nav.view.common;

import android.view.KeyEvent;

/**
 * @author MTK40707
 *
 */
public interface NavBasic {
    int NAV_COMP_ID_MASK        = 0xF000000;
    //For Android Component ID
    int NAV_COMP_ID_BASIC       = 0x1000000;
    int NAV_COMP_ID_EAS         = NAV_COMP_ID_BASIC + 1;
    int NAV_COMP_ID_BANNER      = NAV_COMP_ID_BASIC + 2;
    int NAV_COMP_ID_MUTE        = NAV_COMP_ID_BASIC + 3;
    int NAV_COMP_ID_CEC         = NAV_COMP_ID_BASIC + 4;
    int NAV_COMP_ID_CH_LIST     = NAV_COMP_ID_BASIC + 5;
    int NAV_COMP_ID_SUNDRY      = NAV_COMP_ID_BASIC + 6;
    int NAV_COMP_ID_ZOOM_PAN    = NAV_COMP_ID_BASIC + 7;
    int NAV_COMP_ID_DIALOG_MSG  = NAV_COMP_ID_BASIC + 8;
    int NAV_COMP_ID_EWS         = NAV_COMP_ID_BASIC + 9;
    int NAV_COMP_ID_FAV_LIST    = NAV_COMP_ID_BASIC + 10;
    int NAV_COMP_ID_VOL_CTRL    = NAV_COMP_ID_BASIC + 11;
    int NAV_COMP_ID_GINGA_TV    = NAV_COMP_ID_BASIC + 12;
    int NAV_COMP_ID_INFO_BAR    = NAV_COMP_ID_BASIC + 13;
    int NAV_COMP_ID_INPUT_SRC   = NAV_COMP_ID_BASIC + 14;
    int NAV_COMP_ID_UPDATER     = NAV_COMP_ID_BASIC + 15;
    int NAV_COMP_ID_TWINKLE_MSG = NAV_COMP_ID_BASIC + 16;
    int NAV_COMP_ID_TELETEXT    = NAV_COMP_ID_BASIC + 17;
    int NAV_COMP_ID_PWD_DLG     = NAV_COMP_ID_BASIC + 18;
    int NAV_COMP_ID_POP         = NAV_COMP_ID_BASIC + 19;
    int NAV_COMP_ID_CI          = NAV_COMP_ID_BASIC + 20;
    int NAV_COMP_ID_OAD         = NAV_COMP_ID_BASIC + 21;
    int NAV_COMP_ID_SAT_SEL     = NAV_COMP_ID_BASIC + 22;
    int NAV_COMP_ID_SCART_MONITOR= NAV_COMP_ID_BASIC + 23;
    int NAV_COMP_ID_MISC        = NAV_COMP_ID_BASIC + 24;
    int NAV_COMP_ID_PVR_TIMESHIFT = NAV_COMP_ID_BASIC + 25;
    int NAV_COMP_ID_POWER_OFF   = NAV_COMP_ID_BASIC + 26;
    int NAV_COMP_ID_SUNDRY_DIALOG   = NAV_COMP_ID_BASIC + 27;
    int NAV_COMP_ID_MENU_OPTION_DIALOG = NAV_COMP_ID_BASIC + 28;
    int NAV_COMP_ID_CI_DIALOG   = NAV_COMP_ID_BASIC + 29;
    int NAV_COMP_ID_EPOP_VIEW  = NAV_COMP_ID_BASIC + 30;

    //For Native Component ID
    int NAV_NATIVE_COMP_ID_BASIC        = (2 * NAV_COMP_ID_BASIC);
    int NAV_NATIVE_COMP_ID_MHEG5        = NAV_NATIVE_COMP_ID_BASIC + 1;
    int NAV_NATIVE_COMP_ID_HBBTV        = NAV_NATIVE_COMP_ID_BASIC + 2;
    int NAV_NATIVE_COMP_ID_GINGA        = NAV_NATIVE_COMP_ID_BASIC + 3;
    int NAV_NATIVE_COMP_ID_MHP          = NAV_NATIVE_COMP_ID_BASIC + 4;
    int NAV_NATIVE_COMP_ID_SUBTITLE_INFO= NAV_NATIVE_COMP_ID_BASIC + 5;

    //code for communicate between Activities
    int NAV_REQUEST_CODE                = (3 * NAV_COMP_ID_BASIC);
    int NAV_RESULT_CODE_MENU            = NAV_REQUEST_CODE + 1;

    //For comp show/hide flag
    String NAV_COMPONENT_HIDE_FLAG = "NavComponentHide";
    String NAV_COMPONENT_SHOW_FLAG = "NavComponentShow";

    //Common time out
    int NAV_TIMEOUT_1   = 1 * 1000;//1s
    int NAV_TIMEOUT_2   = 2 * 1000;//2S
    int NAV_TIMEOUT_3   = 3 * 1000;//3S
    int NAV_TIMEOUT_5   = 5 * 1000;//5s
    int NAV_TIMEOUT_10  = 2 * NAV_TIMEOUT_5;//10s
    int NAV_TIMEOUT_120 = 12* NAV_TIMEOUT_10;//120s

    //Priority
    int NAV_PRIORITY_DEFAULT    = 10;
    int NAV_PRIORITY_HIGH_1     = 11;
    int NAV_PRIORITY_HIGH_2     = 12;
    int NAV_PRIORITY_HIGH_3     = 13;
    int NAV_PRIORITY_LOW_1      = 9;
    int NAV_PRIORITY_LOW_2      = 8;
    int NAV_PRIORITY_LOW_3      = 7;

    //Common Methods

    /**
     * this method is used to check the status(visible or invisible) of this components
     *
     * @return true if the component is visible, false if invisible
     */
    boolean isVisible();

    /**
     * this method is used to check whether the specific key is the hot key of this component
     * <pre>
     * For example, the hot key of input source component is KEYCODE_MTKIR_SOURCE, when keyCode
     * is KEYCODE_MTKIR_SOURCE, the returned value is true, otherwise, false will be returned.
     * <pre>
     * @param keyCode, key code
     *
     * @return true if it's hot key of this component, others is false.
     */
    boolean isKeyHandler(int keyCode);

    /**
     * this method is used to get the component id
     *
     * @return the component id
     */
    int getComponentID();

    /**
     * this method is used to get the priority of the component
     *
     * @return the priority
     */
    int getPriority();

    /**
     * this method is used to judge whether the specific component id is conexist with this component
     *
     * @param componentID, the component id
     *
     * @return true if they are coexist, others is false.
     */
    boolean isCoExist(int componentID);

    /**
     * this method is used to handler key when the conponent is visible
     *
     * @param keyCode, key code
     * @param event
     * @param fromNative, true if key is from native app, false if key is from android ap
     *
     * @return true if key is handled and other other component should not handle it again, others is false.
     */
    boolean keyHandler(int keyCode, KeyEvent event, boolean fromNative);

    /**
     * this method is used to handler key when the conponent is visible
     *
     * @param keyCode, key code
     * @param event
     *
     * @return true if key is handled and other other component should not handle it again, others is false.
     */
    boolean keyHandler(int keyCode, KeyEvent event);

    /**
     * this method is used to initial the view resource of the component
     *
     * @return true if the flow is success, others is false.
     */
    boolean initView();

    /**
     * this method is used to start component, , should be called only when the TurnkeyUiMainActivity is resumed
     *
     * @return true if the flow is success, others is false.
     */
    boolean startComponent();

    /**
     * this method is used to destroy the view resource of the component
     *
     * @return true if the flow is success, others is false.
     */
    boolean deinitView();
}
