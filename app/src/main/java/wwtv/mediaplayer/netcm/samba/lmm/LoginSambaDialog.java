package com.mediatek.wwtv.mediaplayer.netcm.samba.lmm;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mstar.android.samba.SmbDevice;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.mediatek.wwtv.mediaplayer.R;

public class LoginSambaDialog extends Dialog {
    private static final String TAG = "LoginSambaDialog";

    public static final String SAMBA_SETTINGS = "lmm_samba_settings";

    public static final String PREF_LOGIN_USER = "lmm_samba_login_user";

    public static final String PREF_LOGIN_PWD = "lmm_samba_login_pwd";

    public static final int DLG_ACT_CANCEL = 0;

    public static final int DLG_ACT_LOGIN = 1;

    private EditText user = null;
    private EditText password = null;
    private Button confirm = null;
    private Button cancel = null;
    private Handler mHandler = null;
    private Context mContext = null;
    private SmbDevice mSmbDevice = null;

    private Window mWindow = null;
    private WindowManager mWindowManager = null;

    public LoginSambaDialog(Context ctx, SmbDevice smbDevice) {
        super(ctx);
        mContext = ctx;
        mSmbDevice = smbDevice;
    }

    public LoginSambaDialog(Context ctx, Handler handler, SmbDevice smbDevice) {
        super(ctx);
        mHandler = handler;
        mContext = ctx;
        mSmbDevice = smbDevice;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Instantiation new window
        mWindow = getWindow();
        mWindow.requestFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.samba_input);

        mWindowManager = mWindow.getWindowManager();

        setCancelable(false);
        findViews();
    }

    @Override
    public void show() {
        super.show();

        Display display = mWindowManager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        // Definition of window width and height
        int width = (int) (point.x * 0.25);
        int height = (int) (point.y * 0.35);

        WindowManager.LayoutParams prams = mWindow.getAttributes();
        prams.width = width;
        prams.height = height;
        mWindowManager.updateViewLayout(mWindow.getDecorView(), prams);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (user.hasFocus()) {
                password.requestFocus();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void findViews() {
        user = (EditText) findViewById(R.id.samba_user_name);
        password = (EditText) findViewById(R.id.samba_user_pass);
        confirm = (Button) findViewById(R.id.samba_login);
        cancel = (Button) findViewById(R.id.samba_cancel);
        user.requestFocus();
        confirm.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);

        if(mSmbDevice != null){
            SharedPreferences sharedPref = mContext.getSharedPreferences(
                LoginSambaDialog.SAMBA_SETTINGS, Context.MODE_PRIVATE);
            user.setText(sharedPref.getString(LoginSambaDialog.PREF_LOGIN_USER, ""));
            password.setText(sharedPref.getString(LoginSambaDialog.PREF_LOGIN_PWD, ""));
        }
        setOnEditActionListener();
    }

    private void setOnEditActionListener() {
        password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.i(TAG, "onEditorAction ----------- actionId:" + actionId);
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return false;
            }

        });
    }

    private Button.OnClickListener onClickListener = new Button.OnClickListener(){
    @Override
        public void onClick(View arg0){
            responseButton(arg0.getId());
            cancel();
        }
    };

    private void responseButton(int id){
        Message msg = new Message();
        switch (id) {
        case R.id.samba_login:
            // Retrieve the user input user name
            String usr = user.getText().toString();
            // Get the password of user input
            String pwd = password.getText().toString();
            Log.i(TAG, "user: " + usr + " pass: " + pwd);
            Bundle b = new Bundle();
            b.putString("USERNAME", usr);
            b.putString("PASSWORD", pwd);
            msg.setData(b);
            msg.what = DLG_ACT_LOGIN;
            mHandler.sendMessage(msg);
            break;
        case R.id.samba_cancel:
            msg = new Message();
            msg.what = DLG_ACT_CANCEL;
            mHandler.sendMessage(msg);
            break;
        }
    }

    private static String stringFilter(String  str) throws PatternSyntaxException{
        String regEx="[^a-zA-Z0-9_@.]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }
}
