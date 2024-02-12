package com.mediatek.wwtv.mediaplayer.setting;

import android.os.Bundle;

import com.mediatek.wwtv.mediaplayer.R;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.setting.base.BaseSettingsActivity;
import com.mediatek.wwtv.mediaplayer.setting.base.SpecialOptionDealer;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.ProgressBarFrag;
import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.util.Util;


public class NetSettingActivity extends BaseSettingsActivity implements SpecialOptionDealer {

	public static final String TAG = "SettingActivity";
	private Context mContext;
	CommonIntegration mCommonIntegration ;
	private SaveValue saveV;
	private static NetSettingActivity mInstance =  null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
		mCurrAction = new Action("Menu", "Menu",
				Action.DataType.TOPVIEW);
		mCommonIntegration = CommonIntegration.getInstance();
		mSpecialOptionDealer = this;
		saveV = SaveValue.getInstance(getApplicationContext());
        Util.mIsDolbyVision = false;
		super.onCreate(savedInstanceState);
		mInstance = this;

	}

	public static NetSettingActivity getInstance(){
		return mInstance;
	}

	@Override
	protected Object getInitialDataType() {
		return DataType.TOPVIEW;
	}

	@Override
	protected void updateView() {
		switch ((Action.DataType) mState) {
		case TOPVIEW:
		case HAVESUBCHILD:
		case OPTIONVIEW:
//			if(mCurrAction.mItemID.equals(MenuConfigManager.NETWORK)){
//				DynamicOptionFrag frag = new DynamicOptionFrag();
//				frag.setAction(mCurrAction);
//				mActionFragment = frag;
//				setViewWithActionFragment(
//						(mCurrAction == null ? "Menu Video"
//								: mCurrAction.getTitle()),
//						(mParentAction == null ? mContext.getResources().getString(
//								R.string.menu_header_name) : mParentAction
//								.getTitle()), "", R.drawable.play_logo);
//			}else{
				refreshActionList();
				setView((mCurrAction == null ? "Menu Video"
						: mCurrAction.getTitle()),
						(mParentAction == null ? mContext.getResources().getString(
								R.string.menu_header_name) : mParentAction
								.getTitle()), "", R.drawable.play_logo);
//			}

			break;
		case DIALOGPOP:
			MtkLog.d(TAG, "mCurrAction.mItemID=="+mCurrAction.mItemID);
    		break;
		case PROGRESSBAR:
		case POSITIONVIEW:
			ProgressBarFrag frag = new ProgressBarFrag();
			if(mCurrAction != null) {
				frag.setAction(mCurrAction);
			}
			mActionFragment = frag;
			setViewWithActionFragment(
					(mCurrAction == null ? "Menu Video"
							: mCurrAction.getTitle()),
					(mParentAction == null ? mContext.getResources().getString(
							R.string.menu_header_name) : mParentAction
							.getTitle()), "", R.drawable.play_logo);
			break;
		case LASTVIEW:
			break;
		default:
			break;
		}

	}



	@Override
	protected void refreshActionList() {
		mActions.clear();
    	switch((Action.DataType)mState){
    	//handle havesubchild view special
    	case HAVESUBCHILD:
    		if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_COLOR_TEMPERATURE)){
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}else if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_ADVANCED_VIDEO)){

    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}else if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_3D)){
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}else{
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}
    		break;
    	default:
    		super.refreshActionList();
            break;
    	}

	}

	@Override
	protected void setActionsForTopView() {
		this.loadNetSetting(mCurrAction);
		mActions.addAll(mCurrAction.mSubChildGroup);
	}

	@Override
	public void onActionClicked(Action action) {
		if(action.mItemID.equals(MenuConfigManager.DEMO)){
            Log.d(TAG,"onActionClicked demo do nothing");
		}else{
			super.onActionClicked(action);
		}
	}

	@Override
	protected void setProperty(boolean enable) {
        MtkLog.d(TAG, "setProperty");
	}

	protected void setViewWithActionFragment(String title, String breadcrumb,
			String description, int iconResId) {
		mContentFragment = ContentFragment.newInstance(title, breadcrumb,
				description, iconResId,
				getResources().getColor(R.color.icon_background));
		setContentAndActionFragments(mContentFragment, mActionFragment);
	}

	public void loadNetSetting(Action net){
		List<Action> mGroup = new ArrayList<Action>();
		Action network = new Action(MenuConfigManager.NETWORK,
				mContext.getString(R.string.menu_setup_network),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE, null,
				MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
		String[] netConnection = mContext.getResources().getStringArray(
				R.array.menu_setup_network_connection_array);
		network.mSubChildGroup =  new ArrayList<Action>();
		int initDLNAValue = 0;
        int initNetValue = 0;
        //int initExoValue = 0;
		initDLNAValue = saveV.readValue(MenuConfigManager.DLNA);
		initNetValue = saveV.readValue(MenuConfigManager.MY_NET_PLACE);
		//initExoValue = saveV.readValue(MenuConfigManager.EXO_PLAYER_SWITCHER);
		Action dlna = new Action(MenuConfigManager.DLNA,
				mContext.getString(R.string.menu_setup_dlna),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE, initDLNAValue, netConnection,
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

		Action myNetPlace = new Action(MenuConfigManager.MY_NET_PLACE,
				mContext.getString(R.string.menu_setup_my_net_place),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE, initNetValue, netConnection,
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
        /*
		Action exoPlayer = new Action(MenuConfigManager.EXO_PLAYER_SWITCHER,
        mContext.getString(R.string.menu_setup_exo_payer_switcher),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE, initExoValue, net_connection,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
        */

	network.mSubChildGroup.add(dlna);
	network.mSubChildGroup.add(myNetPlace);
	mGroup.add(network);
	//mGroup.add(exoPlayer);
	net.mSubChildGroup = mGroup;
	}


	/**
	 * deal special item
	 */
	@Override
	public void specialOptionClick(Action currAction) {
		if(currAction.mItemID.equals(MenuConfigManager.VIDEO_3D_NAV)){
			 if (currAction.mInitValue == 2) {
				 mConfigManager.setValue(MenuConfigManager.VIDEO_3D_MODE, 1, currAction);
	         } else {
	        	 mConfigManager.setValue(MenuConfigManager.VIDEO_3D_MODE, 0, currAction);
	         }
		}
	}

}
