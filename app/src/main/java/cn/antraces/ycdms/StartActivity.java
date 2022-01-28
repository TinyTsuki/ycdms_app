package cn.antraces.ycdms;

import static cn.antraces.ycdms.utils.Constants.UnknownErrorJsonString;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.antraces.ycdms.utils.AESUtil;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.MD5Util;
import cn.antraces.ycdms.utils.fns.Http;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.Response;

@SuppressLint("NonConstantResourceId")
public class StartActivity extends BaseActivity {
    private static final String TAG = "StartActivity";
    private SharedPreferences.Editor editor;
    @BindView(R.id.starting)
    LinearLayout bg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        //全屏且修改状态栏颜色为透明
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        LogUtil.v(TAG, String.valueOf(h));
        if (h >= 7 && h < 19) {
            bg.setBackground(getDrawable(R.drawable.starting_light));
        }

        //获取本地Cookies
        SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(StartActivity.this);
        List<Cookie> cookies = sharedPrefsCookiePersistor.loadAll();
        LogUtil.d(TAG, Arrays.toString(cookies.toArray()));

        Http.checkPermission(cookies, () -> runOnUiThread(() -> {
            SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
            String token = pref.getString("token", ""),
                    qq = pref.getString("qq", ""),
                    psw = pref.getString("psw", "");

            psw = AESUtil.decrypt(psw, MD5Util.getMD5Code_16(token) + MD5Util.getMD5Code_16(qq));

            if (!("".equals(token) || "".equals(qq) || "".equals(psw))) {
                Http.login(token, qq, psw, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        LogUtil.v(TAG, e.getMessage());

                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(new Intent(StartActivity.this, LoginActivity.class));
                            }
                        };
                        timer.schedule(task, 500);
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            String back = response.body().string();
                            String backHeaders = response.headers().toMultimap().toString();

                            LogUtil.v(TAG, backHeaders);
                            LogUtil.v(TAG, back);

                            JSONObject json;
                            try {
                                json = JSONObject.parseObject(back);
                            } catch (JSONException e) {
                                json = JSONObject.parseObject(UnknownErrorJsonString);
                            }
                            Intent intent;
                            if (json.getInteger("code") > 0) {
                                intent = new Intent(StartActivity.this, MainActivity.class);
                            } else {
                                editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                                editor.remove("qq");
                                editor.remove("psw");
                                intent = new Intent(StartActivity.this, LoginActivity.class);
                            }

                            Timer timer = new Timer();
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    startActivity(intent);
                                }
                            };
                            timer.schedule(task, 500);

                        } catch (IOException e) {
                            LogUtil.e(TAG, e.getMessage());

                            Timer timer = new Timer();
                            TimerTask task = new TimerTask() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(StartActivity.this, LoginActivity.class));
                                }
                            };
                            timer.schedule(task, 500);
                        }
                    }
                });
            } else
                startActivity(new Intent(this, LoginActivity.class));

        }));
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.finish();
    }
}
