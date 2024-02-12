package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import com.mediatek.wwtv.mediaplayer.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ConfirmReplaceDialog extends AlertDialog implements
		View.OnClickListener, OnShowListener {
	private Button vYes;
	private OnConfirmReplaceListener mListener;

	public ConfirmReplaceDialog(Context context, int theme) {
		super(context, theme);
	}

	public ConfirmReplaceDialog(Context context) {
		super(context);
	}


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		vYes = (Button) findViewById(R.id.mmp_yes);
		Button vYesToAll = (Button) findViewById(R.id.mmp_yes_to_all);
		Button vNo = (Button) findViewById(R.id.mmp_no);
		Button vCancel = (Button) findViewById(R.id.mmp_cancel);

		vYes.setOnClickListener(this);
		vYesToAll.setOnClickListener(this);
		vNo.setOnClickListener(this);
		vCancel.setOnClickListener(this);

		setOnShowListener(this);
	}


	protected int getContentResId() {
		return R.layout.mmp_confirm_paste_dialog;
	}

	public void setOnConfirmReplaceListener(OnConfirmReplaceListener listener) {
		mListener = listener;
	}

	public interface OnConfirmReplaceListener {
		void onYesPressed();

		void onYesToAllPressed();

		void onNoPressed();

		void onCancelPressed();
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.mmp_yes:
			if (mListener != null) {
				mListener.onYesPressed();
			}
			break;
		case R.id.mmp_yes_to_all:
			if (mListener != null) {
				mListener.onYesToAllPressed();
			}
			break;
		case R.id.mmp_no:
			if (mListener != null) {
				mListener.onNoPressed();
			}
			break;
		case R.id.mmp_cancel:
			if (mListener != null) {
				mListener.onCancelPressed();
			}
			break;
		default:
			break;
		}
	}

	public void onShow(DialogInterface dialog) {
		vYes.requestFocus();
	}
}
