package cn.antraces.ycdms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.antraces.ycdms.utils.fns.Http;

@SuppressLint("NonConstantResourceId")
public class SettingsActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = "SettingsActivity";
    @BindView(R.id.btn_set_download_excel)
    Button download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        // 标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);

        // 绑定标题栏按钮
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        download.setOnClickListener(this);

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

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_set_download_excel) {
            if (requestMyPermissions()) {
                showSnackBar(getString(R.string.download_start));
                Http.downloadWorkerInfo(() -> showSnackBar(getString(R.string.download_failed)), () -> showSnackBar(getString(R.string.download_successful)));
            }
        }
    }

    private boolean requestMyPermissions() {
        boolean a;
        boolean b = false;

        //检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
        if ((a = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
                (b = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            //用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showSnackBar(getString(R.string.rw_permissions));
            }

            if (a) {
                //没有授权，编写申请权限代码
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }

            if (b) {
                //没有授权，编写申请权限代码
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }

        } else {
            return true;
        }
        return false;
    }

}
