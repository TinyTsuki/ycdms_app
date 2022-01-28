package cn.antraces.ycdms;

import static cn.antraces.ycdms.utils.Constants.UpdateHostName;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import cn.antraces.ycdms.entity.Version;
import cn.antraces.ycdms.utils.C2RoundAngleImageView;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.fns.Function;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView logs, bubble;
    private LinearLayout layout;
    private Version version;
    private C2RoundAngleImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        version = (Version) getIntent().getSerializableExtra("version");

        // 标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_about);
        setSupportActionBar(toolbar);

        // 绑定标题栏按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        TextView versionText = (TextView) findViewById(R.id.about_version_text);
        versionText.setText(Function.getAppVersionName());

        logs = (TextView) findViewById(R.id.update_logs);
        logs.setText(R.string.update_logs);

        bubble = (TextView) findViewById(R.id.about_version_bubble);
        layout = (LinearLayout) findViewById(R.id.about_version);

        imageView = (C2RoundAngleImageView) findViewById(R.id.about_image);

        layout.setOnClickListener(this);
        imageView.setOnClickListener(this);

        if (version.getVn() > Function.getAppVersion()) {
            bubble.setVisibility(View.VISIBLE);
            if (null != version.getContent()) {
                logs.setText(String.format("%s", "--" + version.getVer() + "\n" + version.getContent() + "\n\n" + logs.getText().toString()));
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.about_version || view.getId() == R.id.about_image) {
            if (version.getVn() > Function.getAppVersion()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(UpdateHostName + "/" + Function.getPackageName() + "_" + version.getVer() + ".apk"));
                startActivity(intent);
            } else {
                Function.getLatestVersion(new Callback() {
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String back = response.body().string();
                        LogUtil.d("Version", back);
                        JSONObject json;
                        try {
                            json = JSONObject.parseObject(back);
                        } catch (JSONException e) {
                            json = JSONObject.parseObject("{'code':0}");
                        }
                        if (json.getInteger("code") == 1) {
                            version = JSONObject.toJavaObject(json.getJSONObject("data"), Version.class);
                            if (version.getVn() > Function.getAppVersion()) {
                                AboutActivity.this.runOnUiThread(() -> {
                                    bubble.setVisibility(View.VISIBLE);
                                    showSnackBar(getString(R.string.new_release) + " " + version.getVer());
                                    logs.setText(String.format("%s", version.getVer() + "\n" + version.getContent() + "\n\n" + logs.getText().toString()));
                                });
                            } else
                                showSnackBar(getString(R.string.latest_now));
                        } else
                            showSnackBar(getString(R.string.no_new));
                    }

                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        LogUtil.d("AboutActivity", "" + e.getMessage());
                        showSnackBar(getString(R.string.latest_now));
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.anim_l_to_r_in, R.anim.anim_l_to_r_out);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.anim_l_to_r_in, R.anim.anim_l_to_r_out);
    }

    /**
     * 展示一个SnackBar
     */
    public void showSnackBar(String message) {
        runOnUiThread(() -> {
            //去掉虚拟按键
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //隐藏虚拟按键栏
                    | View.SYSTEM_UI_FLAG_IMMERSIVE //防止点击屏幕时,隐藏虚拟按键栏又弹了出来
            );
            final Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            });
            snackbar.show();
        });
    }
}
