package com.ldybob.ac3korea;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
    private final String TAG = "LoginActivity";

    Util util;
    Button btn_login;
    Context mContext;
    EditText mET_ID;
    EditText mET_PW;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = (Button)findViewById(R.id.btn_login);
        util = new Util(this);
        mContext = this;
        mET_ID = (EditText)findViewById(R.id.member_id);
        mET_PW = (EditText)findViewById(R.id.member_pw);
        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        String id = pref.getString("id", "");
        String pw = pref.getString("pw", "");
        if (!pw.isEmpty()) {
            pw = util.decrypt(pw);
        }
        mET_ID.setText(id);
        mET_PW.setText(pw);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean auto_login = pref.getBoolean("auto", false);
        if (auto_login) {
            //Log.d(TAG, "Auto Login");
            Login(true);
        }
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!util.NetworkCheck()) {
                    Toast.makeText(mContext, getString(R.string.network_fail_msg), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mET_ID.getText().toString().isEmpty() || mET_PW.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, getString(R.string.login_empty_msg), Toast.LENGTH_SHORT).show();
                    return;
                }
                Login(false);
            }
        });
    }

    private void Login(final boolean isAuto) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                SharedPreferences.Editor editor = pref.edit();
                String id, pw;
                if (isAuto) {
                    id = pref.getString("id", "");
                    pw = util.decrypt(pref.getString("pw", ""));
                } else {
                    id = mET_ID.getText().toString();
                    pw = mET_PW.getText().toString();
                }
                //Log.d(TAG, "id = " + id + ", pw = " + pw);
                boolean login_success = util.Login(id, pw);
                if (login_success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, getString(R.string.login_success_msg), Toast.LENGTH_SHORT).show();
                        }
                    });
                    editor.putBoolean("auto", true);
                    editor.putString("id", mET_ID.getText().toString());
                    editor.putString("pw", util.encrypt(mET_PW.getText().toString()));
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("bbs", BoardID.FREE);
                    startActivity(intent);
                    finish();
                } else {
                    editor.putBoolean("auto", false);
                    editor.putString("id", "");
                    editor.putString("pw", util.encrypt(""));
                    editor.commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mContext, getString(R.string.login_fail_msg), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        thread.start();
    }
}
