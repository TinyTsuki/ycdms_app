package cn.antraces.ycdms;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.antraces.ycdms.utils.AESUtil;
import cn.antraces.ycdms.utils.Constants;
import cn.antraces.ycdms.utils.EditTextUtil;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.MD5Util;
import cn.antraces.ycdms.utils.fns.Http;
import cn.antraces.ycdms.utils.fns.Pref;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressLint("NonConstantResourceId")
public class LoginActivity extends BaseActivity implements Constants {

    @BindView(R.id.usertoken)
    EditText usertoken;
    @BindView(R.id.usertoken_x)
    Button usertoken_x;

    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.username_x)
    Button username_x;

    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.password_x)
    Button password_x;

    @BindView(R.id.login)
    Button submit;

    private SharedPreferences.Editor editor;
    private final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //修改状态栏颜色为透明
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        EditTextUtil.clearButtonListener(usertoken, usertoken_x);
        EditTextUtil.clearButtonListener(username, username_x);
        EditTextUtil.clearButtonListener(password, password_x);

        String token = Pref.getS("data", "token");
        if (!"".equals(token)) {
            usertoken.setText(token);
            username.setText(Pref.getS("data", "qq"));
            isBound(token);
        } else {
            username.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
            submit.setText(R.string.action_verify);
        }
    }

    @OnClick(R.id.login)
    public void onViewClicked(View view) {
        String token = usertoken.getText().toString();
        String qq = username.getText().toString();
        String psw = password.getText().toString();
        if ("".equals(token)) {
            showSnackBar("请输入Token");
            return;
        }

        if (submit.getText().equals(getString(R.string.action_verify))) {
            isBound(token);
            return;
        }

        if ("".equals(qq)) {
            showSnackBar(getString(R.string.enter_qq_please));
            return;
        }
        if ("".equals(psw)) {
            showSnackBar(getString(R.string.enter_psw_please));
            return;
        }

        editor = getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putString("token", token);
        editor.putString("qq", qq);
        editor.putString("psw", AESUtil.encrypt(psw, MD5Util.getMD5Code_16(token) + MD5Util.getMD5Code_16(qq)));

        Callback loginCallback = new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, e.getMessage());
                new Thread(() -> {
                    Looper.prepare();
                    showSnackBar("请求失败");
                    Looper.loop();
                }).start();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String back = response.body().string();
                    LogUtil.v(TAG, back);
                    editor.apply();

                    JSONObject json;
                    try {
                        json = JSONObject.parseObject(back);
                    } catch (Exception e) {
                        json = JSONObject.parseObject(UnknownErrorJsonString);
                    }

                    if (json.getInteger("code") > 0) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        LoginActivity.this.finish();
                    } else {
                        editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                        editor.remove("phone");
                        editor.remove("psw");

                        String msg = json.getString("msg");
                        new Thread(() -> {
                            Looper.prepare();
                            showSnackBar(msg);
                            Looper.loop();
                        }).start();
                    }
                } catch (Exception e) {
                    LogUtil.e(TAG, e.getMessage());
                    editor.clear();
                    new Thread(() -> {
                        Looper.prepare();
                        showSnackBar("" + MyApplication.getContext().getString(R.string.unknown_error) + "");
                        Looper.loop();
                    }).start();
                }
            }
        };

        if (submit.getText().equals(getString(R.string.action_reg))) {
            Http.bound(token, qq, psw, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    LogUtil.e(TAG, e.getMessage());
                    new Thread(() -> {
                        Looper.prepare();
                        showSnackBar("请求失败");
                        Looper.loop();
                    }).start();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    editor.apply();

                    JSONObject json;
                    try {
                        String back = response.body().string();
                        LogUtil.v(TAG, back);
                        json = JSONObject.parseObject(back);
                    } catch (Exception e) {
                        json = JSONObject.parseObject(UnknownErrorJsonString);
                    }

                    if (json.getInteger("code") > 0) {
                        runOnUiThread(() -> {
                            submit.setText(R.string.action_sign_in);
                        });
                        Http.login(token, qq, psw, loginCallback);
                    } else {
                        String msg = json.getString("msg");
                        new Thread(() -> {
                            Looper.prepare();
                            showSnackBar(msg);
                            Looper.loop();
                        }).start();
                    }
                }
            });
        } else {
            Http.login(token, qq, psw, loginCallback);
        }

    }

    private void isBound(String token) {
        Http.isBound(token, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, e.getMessage());
                showSnackBar(getString(R.string.request_failed));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                JSONObject json;
                try {
                    String back = response.body().string();
                    LogUtil.v(TAG, back);
                    json = JSONObject.parseObject(back);
                } catch (Exception e) {
                    json = JSONObject.parseObject(UnknownErrorJsonString);
                }
                if (json.getInteger("code") == 1) {
                    runOnUiThread(() -> {
                        username.setVisibility(View.VISIBLE);
                        password.setVisibility(View.VISIBLE);
                        username.setHint(getString(R.string.prompt_set_qq));
                        password.setHint(getString(R.string.prompt_set_password));
                        submit.setText(R.string.action_reg);
                    });
                } else if (json.getInteger("code") < 0 && "bound".equals(json.getString("msg"))) {
                    runOnUiThread(() -> {
                        username.setVisibility(View.VISIBLE);
                        password.setVisibility(View.VISIBLE);
                        username.setHint(getString(R.string.prompt_qq));
                        password.setHint(getString(R.string.prompt_password));
                        submit.setText(R.string.action_sign_in);
                    });
                } else {
                    String msg = json.getString("msg");
                    new Thread(() -> {
                        Looper.prepare();
                        showSnackBar(msg);
                        Looper.loop();
                    }).start();
                }
            }
        });
    }
}
