package cn.antraces.ycdms;

import static cn.antraces.ycdms.utils.Constants.PopPageNavHeader;
import static cn.antraces.ycdms.utils.Constants.PopPageTips;
import static cn.antraces.ycdms.utils.Constants.UnknownErrorJsonString;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.antraces.ycdms.adapter.MainPagerAdapter;
import cn.antraces.ycdms.entity.Version;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.fns.Function;
import cn.antraces.ycdms.utils.fns.Http;
import cn.antraces.ycdms.utils.fns.Pref;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private final String TAG = "MainActivity";
    List<Fragment> fragments = new ArrayList<>();
    Version version = new Version();
    ViewPager viewPager;
    Menu optionMenu;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    ActionBar actionBar;
    View navHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // 绑定标题栏, 设置标题
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(Pref.isRegView() ? getString(R.string.toolbar_title_2) : getString(R.string.toolbar_title_1));

        // 设置侧滑监听器
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
        }

        // 初始化侧滑头部
        if (navigationView != null) {
            navHeader = navigationView.inflateHeaderView(R.layout.view_navigation_header);
            navHeader.findViewById(R.id.iv_head).setOnClickListener(view -> {
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                Intent intent = new Intent(Function.getPackageName() + ".OPEN_POP_OPERATION_MEMBER_ACTIVITY");
                intent.putExtra("pop", PopPageNavHeader);
                localBroadcastManager.sendBroadcast(intent);
            });
            initUserJob();
            Glide.with(MainActivity.this).load(Function.getQQHeadUrl())
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.icon).into((ImageView) navHeader.findViewById(R.id.iv_head));
        }

        // 绑定标题栏按钮
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_access);
        }

        // 绑定分页
        viewPager = findViewById(R.id.viewpager);
        fragments.add(new MainFragment());
        fragments.add(new QrcodeFragment());
        MainPagerAdapter mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(mainPagerAdapter);
        // 设置当前选中页
        viewPager.setCurrentItem(Pref.isRegView() ? 1 : 0);
        // 绑定页面改变事件
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (null != optionMenu && 0 == positionOffsetPixels && 0 == positionOffset) {
                    MenuItem item1 = optionMenu.findItem(R.id.changeView);
                    MenuItem item2 = optionMenu.findItem(R.id.search);
                    MenuItem item3 = optionMenu.findItem(R.id.doubt);
                    if (position == 0) {
                        Pref.setRegView(false);
                        item1.setTitle(R.string.toolbar_menu_change_to_reg);
                        item1.setIcon(R.drawable.ic_scan);
                        item2.setVisible(true);
                        item3.setVisible(false);
                        getSupportActionBar().setTitle(getString(R.string.toolbar_title_1));
                    } else {
                        Pref.setRegView(true);
                        item1.setTitle(R.string.toolbar_menu_change_to_list);
                        item1.setIcon(R.drawable.ic_list);
                        item2.setVisible(false);
                        item3.setVisible(true);
                        getSupportActionBar().setTitle(getString(R.string.toolbar_title_2));
                    }
                }
            }
        });

        // 注册监本地广播听器 - showSnackBar
        IntentFilter intentFilter = new IntentFilter();
        ShowSnackBarReceiver showSnackBarReceiver = new ShowSnackBarReceiver();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter.addAction(Function.getPackageName() + ".SHOW_SNACK_BAR_ACTIVITY");
        localBroadcastManager.registerReceiver(showSnackBarReceiver, intentFilter);

        // 获取更新信息
        Function.getLatestVersion(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String back = Objects.requireNonNull(response.body()).string();
                LogUtil.d("Version", back);
                JSONObject json;
                try {
                    json = JSONObject.parseObject(back);
                } catch (JSONException e) {
                    json = JSONObject.parseObject("{'code':0}");
                }
                if (json.getInteger("code") == 1)
                    version = JSONObject.toJavaObject(json.getJSONObject("data"), Version.class);
                initNavMenu();
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                initNavMenu();
            }
        });
    }

    // PopOperation 返回结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (null != data && data.getIntExtra("mode", 0) == PopPageNavHeader) {
            if (requestCode == 1 && resultCode == RESULT_OK && data.getIntExtra("selected", R.id.btn_cancel) == R.id.btn_change_submit) {
                Pref.clear("CookiePersistence");
                Pref.set("data", "psw", "");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        } else if (null != data && data.getIntExtra("mode", 0) == PopPageTips) {
            //不需要执行任何操作, 防止报错
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            MainFragment mainFragment = (MainFragment) fragments.get(0);
            try {
                mainFragment.onActivityResult(requestCode, resultCode, data);
            } catch (Exception e) {
                LogUtil.e(TAG, e.getMessage());
                showSnackBar(getString(R.string.restart_please));
            }
        }
    }

    // 标题栏菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionMenu = menu;
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(500);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);

        if (Pref.isRegView()) {
            menu.findItem(R.id.changeView).setTitle(R.string.toolbar_menu_change_to_list);
            menu.findItem(R.id.changeView).setIcon(R.drawable.ic_list);
            menu.findItem(R.id.search).setVisible(false);
        } else {
            menu.findItem(R.id.changeView).setTitle(R.string.toolbar_menu_change_to_reg);
            menu.findItem(R.id.changeView).setIcon(R.drawable.ic_scan);
            menu.findItem(R.id.doubt).setVisible(false);
        }
        return true;
    }

    // 标题栏菜单点击
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeView:
                viewPager.setCurrentItem(Pref.isRegView() ? 0 : 1);
                break;
            case R.id.search:
                break;
            case R.id.doubt:
                Intent intent = new Intent(this, PopOperationActivity.class);
                intent.putExtra("type", PopPageTips);
                intent.putExtra("title", "Tips");
                intent.putExtra("content", getString(R.string.qrcode_tips));
                openPop(intent);
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    // 侧滑菜单点击
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                overridePendingTransition(R.anim.anim_r_to_l_in, R.anim.anim_r_to_l_out);
                break;
            case 2:
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                intent.putExtra("version", version);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_r_to_l_in, R.anim.anim_r_to_l_out);
                break;
            case 3:
                ActivityCollector.finishAll();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        // 返回桌面但不结束活动
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }

    // 初始化侧滑菜单
    private void initNavMenu() {
        MainActivity.this.runOnUiThread(() -> {
            if (version.getVn() > Function.getAppVersion()) {
                SubMenu subMenu = navigationView.getMenu().addSubMenu("系统");
                subMenu.add(1, 1, 1, R.string.setting).setIcon(R.drawable.ic_setting);
                subMenu.add(1, 2, 1, R.string.about).setIcon(R.drawable.ic_detail).setActionView(R.layout.view_msg_bubble);
                subMenu.add(1, 3, 1, R.string.quit).setIcon(R.drawable.ic_exit);
                showSnackBar(getString(R.string.new_release));
            } else {
                SubMenu subMenu = navigationView.getMenu().addSubMenu("系统");
                subMenu.add(1, 1, 1, R.string.setting).setIcon(R.drawable.ic_setting);
                subMenu.add(1, 2, 1, R.string.about).setIcon(R.drawable.ic_detail);
                subMenu.add(1, 3, 1, R.string.quit).setIcon(R.drawable.ic_exit);
            }
        });
    }

    // 弹出Pop
    public void openPop(Intent intent) {
        startActivityForResult(intent, 1);
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
    }

    public void showSnack(String msg) {
        showSnackBar(msg);
    }

    // showSnackBar 监听器
    class ShowSnackBarReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.v(TAG, "ShowSnackBarReceiver: " + intent.getStringExtra("msg"));
            String msg = intent.getStringExtra("msg");
            if (null != msg && !"".equals(msg))
                showSnackBar(msg);
        }

    }

    // 标题栏搜索框事件
    @Override
    public boolean onQueryTextSubmit(String query) {
        MainFragment mainFragment = (MainFragment) fragments.get(0);
        mainFragment.filter = query;
        mainFragment.refreshMembers();
        return false;
    }

    // 标题栏搜索框事件
    @Override
    public boolean onQueryTextChange(String newText) {
        MainFragment mainFragment = (MainFragment) fragments.get(0);
        mainFragment.filter = newText;
        return false;
    }

    // 标题栏搜索框事件
    @Override
    public boolean onClose() {
        new Handler().postDelayed(() -> {
            MainFragment mainFragment = (MainFragment) fragments.get(0);
            mainFragment.refreshMembers();
        }, 1000);
        return false;
    }

    // 初始化侧滑头部标题
    private void initUserJob() {
        if ("".equals(Pref.getS("data", "job"))) {
            Http.getWorkerInfo(Pref.getS("data", "token"), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    JSONObject json;
                    String back;
                    try {
                        back = Objects.requireNonNull(response.body()).string();
                        LogUtil.v("getMembers", back);
                        json = JSONObject.parseObject(back);
                    } catch (Exception e) {
                        back = UnknownErrorJsonString;
                        json = JSONObject.parseObject(back);
                    }
                    if (json.getInteger("code") > 0) {
                        String job = json.getJSONObject("data").getString("job");
                        Pref.set("data", "job", job);
                        ((TextView) navHeader.findViewById(R.id.iv_title)).setText(job.isEmpty() ? getString(R.string.app_name) : job);
                    }
                }
            });
        } else {
            ((TextView) navHeader.findViewById(R.id.iv_title)).setText(Pref.getS("data", "job"));
        }
    }
}
