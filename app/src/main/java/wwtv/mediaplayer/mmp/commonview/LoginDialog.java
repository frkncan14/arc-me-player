package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.EditText;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.DisplayUtil;
import com.mediatek.wwtv.tvcenter.util.KeyMap;

public class LoginDialog extends Dialog implements OnFocusChangeListener {
	private EditText vUserName;
	private EditText vPassword;
	private Context context;
	private OnKeyClickListener mListener;

	public LoginDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public LoginDialog(Context context, int theme) {
		super(context, theme);
	}

	public LoginDialog(Context context) {
		this(context, R.style.dialog);
	}


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mmp_login_dialog);

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		Context context = getContext();

		lp.gravity = Gravity.START | Gravity.TOP;

		lp.width = DisplayUtil.getWidthPixels(context, 0.3f);
		lp.height = DisplayUtil.getHeightPixels(context, 0.29f);

		lp.x = DisplayUtil.getWidthPixels(context, 0.1f);
		lp.y = DisplayUtil.getHeightPixels(context, 0.2f);

		getWindow().setAttributes(lp);

		vUserName = (EditText) findViewById(R.id.mmp_user_name);
		vPassword = (EditText) findViewById(R.id.mmp_password);
		vUserName.setOnFocusChangeListener(this);
		vPassword.setOnFocusChangeListener(this);
	}


	public boolean dispatchKeyEvent(KeyEvent event) {
		int key = event.getKeyCode();
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (key) {
			case KeyMap.KEYCODE_VOLUME_UP:
			case KeyMap.KEYCODE_VOLUME_DOWN:
				return true;
			case KeyMap.KEYCODE_DPAD_RIGHT:
				if (mListener != null) {
					mListener.onInputClicked();
				}
				return true;
			case KeyMap.KEYCODE_DPAD_CENTER:
				if (mListener != null) {
					mListener.onConfirmClicked(getUserName(), getPassword());
				}
				return true;
			case KeyMap.KEYCODE_BACK:
				MultiFilesManager.getInstance(context).popOpenedHistory();
				break;
			default:
				break;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	public void setOnKeyClickListener(OnKeyClickListener listener) {
		mListener = listener;
	}

	public boolean isPassword() {
		return getCurrentFocus().getId() == R.id.mmp_password;
	}

	public String getPrefill() {
		String prefill = null;
		int id = getCurrentFocus().getId();
		switch (id) {
		case R.id.mmp_user_name:
			prefill = getUserName();
			break;
		case R.id.mmp_password:
			prefill = getPassword();
			break;
		default:
			break;
		}

		return prefill;
	}

	public void setInput(String input) {
		int id = getCurrentFocus().getId();
		switch (id) {
		case R.id.mmp_user_name:
			setUserName(input);
			break;
		case R.id.mmp_password:
			setPassword(input);
			break;
		default:
			break;
		}
	}

	private String getUserName() {
		return vUserName.getText().toString().trim();
	}

	private String getPassword() {
		return vPassword.getText().toString().trim();
	}

	private void setUserName(String input) {
		vUserName.setText(input);
		vUserName.setSelection(0);
	}

	private void setPassword(String input) {
		vPassword.setText(input);
		vPassword.setSelection(0);
	}

	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			if (v.getId() == R.id.mmp_user_name) {
				vUserName.setSelection(0);
			} else if (v.getId() == R.id.mmp_password) {
				vPassword.setSelection(0);
			}
		}
	}

	public interface OnKeyClickListener {
		void onInputClicked();

		void onConfirmClicked(String userName, String password);
	}

	public void init() {
		setUserName("");
		setPassword("");
		vUserName.requestFocus();
	}
}
