package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CiPinDialogFragment.ResultListener;
import com.mediatek.wwtv.tvcenter.util.MtkLog;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;

public final class CIPinCodeDialog extends Dialog {

	private static final String TAG = "CIPinCodeDialog";
	private Context mContext;
	private CIStateChangedCallBack mCIState;
	private static CIPinCodeDialog mDialog;
	private boolean isKeyShowDialog = false;

	private CIPinCodeDialog(Context context) {
		super(context, R.style.Theme_TurnkeyCommDialog);
		mContext = context;
	}

	public static synchronized CIPinCodeDialog getInstance(Context context) {
		if (mDialog == null) {
			mDialog = new CIPinCodeDialog(context);
		}
		return mDialog;
	}

	public void setCIStateChangedCallBack(CIStateChangedCallBack state) {
		mCIState = state;
		mCIState.setPinCodeDialog(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MtkLog.d(TAG, "onCreate");
		setContentView(getLayoutInflater().inflate(
				R.layout.menu_ci_pin_code_dialog, null));
		setWindowPosition();
		TextView mTitle = (TextView) findViewById(R.id.ci_input_pin_code_title);
		CiPinDialogFragment pinDialogFragment = (CiPinDialogFragment) (((MediaPlayActivity)mContext)
				.getFragmentManager()
				.findFragmentById(R.id.ci_input_pin_code_num));
		pinDialogFragment.setResultListener(new ResultListener() {

			@Override
			public void done(String pinCode) {
				MtkTvCI ci = mCIState.getCIHandle();
				if (ci != null) {
					dismiss();
				}
			}
		});
		mTitle.setText(R.string.menu_setup_ci_pin_code_input_tip);
	}

	@Override
	public void show() {
		MtkLog.d(TAG, "show");
		isKeyShowDialog = true;
		super.show();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		MtkLog.d(TAG, "dispatchKeyEvent");
		if (isKeyShowDialog) {
			isKeyShowDialog = false;
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	private void setWindowPosition() {
		WindowManager m = getWindow().getWindowManager();
		Display display = m.getDefaultDisplay();
		Window window = getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		int menuWidth = (int) (display.getWidth() * 0.52);
		int menuHeight = (int) (display.getHeight() * 0.56);
		lp.width = menuWidth;
		lp.height = menuHeight;
		lp.gravity = Gravity.CENTER;
		window.setAttributes(lp);
	}
}
