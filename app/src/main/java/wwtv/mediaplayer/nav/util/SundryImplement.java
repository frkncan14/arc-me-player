package com.mediatek.wwtv.mediaplayer.nav.util;

import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;

import android.content.Context;
import android.util.Log;

public final class SundryImplement {
    private static SundryImplement instance;
    private MtkTvAVMode navMtkTvAVMode;

    private SundryImplement(Context context) {
        navMtkTvAVMode = MtkTvAVMode.getInstance();
        Log.d("SundryImplement","context=="+context);
    }

    public static synchronized SundryImplement getInstanceNavSundryImplement(
            Context context) {
        if (instance == null) {
            instance = new SundryImplement(context);
        }
        return instance;
    }


    /**
     * get main output if freeze or not
     * @return
     */
    public boolean isFreeze(){
        int result = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN);

        if (0 == result) {
            return navMtkTvAVMode.isFreeze("main");
        } else {//1
            return navMtkTvAVMode.isFreeze("sub");
        }
    }

    /**
     * set freeze
     * @param isFreeze
     */
    public int setFreeze(boolean isFreeze){
        int result = MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN);

        if (0 == result) {
            return navMtkTvAVMode.setFreeze("main", isFreeze);
        } else {//1
            return navMtkTvAVMode.setFreeze("sub", isFreeze);
        }
    }

}
