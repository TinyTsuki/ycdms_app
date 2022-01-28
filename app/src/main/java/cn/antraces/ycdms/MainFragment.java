package cn.antraces.ycdms;

import static android.app.Activity.RESULT_OK;
import static cn.antraces.ycdms.utils.Constants.PopPageEdit;
import static cn.antraces.ycdms.utils.Constants.PopPageInfo;
import static cn.antraces.ycdms.utils.Constants.UnknownErrorJsonString;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.antraces.ycdms.adapter.MemberAdapter;
import cn.antraces.ycdms.entity.Member;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.fns.Function;
import cn.antraces.ycdms.utils.fns.Http;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressLint({"NotifyDataSetChanged", "NonConstantResourceId"})
public class MainFragment extends Fragment {

    protected Activity mActivity;
    private final String TAG = "MainFragment";
    View view;
    SwipeRefreshLayout swipeRefresh;
    MemberAdapter adapter;
    RecyclerView recyclerView;
    List<Member> memberList = new ArrayList<>();
    IntentFilter intentFilter;
    PopActivityReceiver popActivityReceiver;
    LocalBroadcastManager localBroadcastManager;
    String filter = "";

    private static final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttach(@NonNull Context context) {
        LogUtil.v(TAG, "onAttach");
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        LogUtil.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogUtil.v(TAG, "onCreateView");
        view = inflater.inflate(R.layout.view_main_page_main, container, false);

        // 注册监本地广播听器
        intentFilter = new IntentFilter();
        intentFilter.addAction(Function.getPackageName() + ".OPEN_POP_OPERATION_MEMBER_ACTIVITY");
        popActivityReceiver = new PopActivityReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(mActivity);
        localBroadcastManager.registerReceiver(popActivityReceiver, intentFilter);

        // 设置下拉刷新监听器
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeColors(Color.parseColor("#38ACFF"), Color.parseColor("#FF4081"));
        swipeRefresh.setOnRefreshListener(this::refreshMembers);
        swipeRefresh.setRefreshing(true);

        // 列表控件
        recyclerView = (RecyclerView) view.findViewById(R.id.recycle_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MemberAdapter(memberList);
        recyclerView.setAdapter(adapter);

        // 初始化会员列表
        initMembers();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        LogUtil.v(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        LogUtil.v(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        LogUtil.v(TAG, "onResume");
        super.onResume();
    }

    // PopOperation 监听器
    class PopActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.v(TAG, "PopActivityReceiver: " + intent.getIntExtra("pop", 0));
            if (intent.getIntExtra("pop", 0) == 0) return;
            Intent intent1 = new Intent(mActivity, PopOperationActivity.class);
            intent1.putExtra("status", null != adapter.selectedMember ? adapter.selectedMember.getPhoto() : 0);
            intent1.putExtra("type", intent.getIntExtra("pop", 0));
            ((MainActivity) mActivity).openPop(intent1);
        }
    }

    // PopOperation 返回结果
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                int position = adapter.selectedMember.getPosition();
                switch (data != null ? data.getIntExtra("selected", R.id.btn_cancel) : R.id.btn_cancel) {
                    case R.id.btn_change_submit:
                        int photo = adapter.selectedMember.getPhoto() == 0 ? 1 : 0;
                        Http.changePhoto(adapter.selectedMember.getId(), photo, new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                LogUtil.e(TAG, e.getMessage());
                                showSnackBar(mActivity.getString(R.string.modified_failure));
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String back = Objects.requireNonNull(response.body()).string();
                                LogUtil.v(TAG, back);
                                JSONObject json;
                                try {
                                    json = JSONObject.parseObject(back);
                                } catch (JSONException e) {
                                    json = JSONObject.parseObject(UnknownErrorJsonString);
                                }
                                if (json.getInteger("code") == 1) {
                                    String msg = mActivity.getString(R.string.modified_successful);
                                    showSnackBar(msg);
                                    handler.post(() -> {
                                        adapter.mMemberList.get(position).setPhoto(photo);
                                        adapter.selectedMember.setPhoto(photo);
                                        adapter.notifyItemChanged(position);
                                    });
                                    Http.getMembers(null, null, filter);
                                } else {
                                    String msg = json.getString("msg");
                                    showSnackBar(msg);
                                }
                            }
                        });
                        break;
                    case R.id.btn_update_qq:
                        Intent intent = new Intent(mActivity, PopOperationActivity.class);
                        intent.putExtra("status", adapter.selectedMember.getPhoto());
                        intent.putExtra("type", PopPageEdit);
                        ((MainActivity) mActivity).openPop(intent);
                        break;
                    case R.id.btn_view_details:
                        Intent intent1 = new Intent(mActivity, PopOperationActivity.class);
                        intent1.putExtra("status", adapter.selectedMember.getPhoto());
                        intent1.putExtra("type", PopPageInfo);
                        intent1.putExtra("data", adapter.selectedMember);
                        ((MainActivity) mActivity).openPop(intent1);
                        break;
                    case R.id.btn_okay:
                        long qq;
                        if (data.getIntExtra("mode", 0) == PopPageEdit) {
                            qq = Long.parseLong(data.getStringExtra("data"));
                            if (qq > 10000 && qq < 10000000000L) {
                                Http.changeQQ(adapter.selectedMember.getId(), qq, new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        LogUtil.e(TAG, e.getMessage());
                                        showSnackBar(mActivity.getString(R.string.delete_failed));
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                        String back = Objects.requireNonNull(response.body()).string();
                                        LogUtil.v(TAG, back);
                                        JSONObject json;
                                        try {
                                            json = JSONObject.parseObject(back);
                                        } catch (JSONException e) {
                                            json = JSONObject.parseObject(UnknownErrorJsonString);
                                        }
                                        if (json.getInteger("code") == 1) {
                                            String msg = mActivity.getString(R.string.modified_successful);
                                            showSnackBar(msg);
                                            handler.post(() -> {
                                                adapter.mMemberList.get(position).setQq(qq);
                                                adapter.selectedMember.setQq(qq);
                                                adapter.notifyItemChanged(position);
                                            });
                                            Http.getMembers(null, null, filter);
                                        } else {
                                            String msg = json.getString("msg");
                                            showSnackBar(msg);
                                        }

                                    }
                                });
                            } else {
                                showSnackBar(qq + mActivity.getString(R.string.invalid_username));
                            }
                        }
                        break;
                    case R.id.btn_delete_member:
                        Http.delMember(adapter.selectedMember.getId(), new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                LogUtil.e(TAG, e.getMessage());
                                showSnackBar(mActivity.getString(R.string.delete_failed));
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String back = Objects.requireNonNull(response.body()).string();
                                LogUtil.v(TAG, back);
                                JSONObject json;
                                try {
                                    json = JSONObject.parseObject(back);
                                } catch (JSONException e) {
                                    json = JSONObject.parseObject(UnknownErrorJsonString);
                                }
                                if (json.getInteger("code") == 1) {
                                    String msg = mActivity.getString(R.string.delete_successful);
                                    showSnackBar(msg);
                                    handler.post(() -> {
                                        adapter.mMemberList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                    });
                                    Http.getMembers(null, null, filter);
                                } else {
                                    String msg = json.getString("msg");
                                    showSnackBar(msg);
                                }
                            }
                        });
                        break;
                }
            }
        }
    }

    public void refreshMembers() {
        try {
            if (!swipeRefresh.isRefreshing()) swipeRefresh.setRefreshing(true);
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initMembers();
                handler.post(() -> {
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                });
            }).start();
        } catch (Exception e) {
            LogUtil.e(TAG, e.getMessage());
            showSnackBar(MyApplication.getContext().getString(R.string.restart_please));
        }
    }

    public void initMembers() {
        Http.getMembers(memberList, () -> handler.post(() -> {
            adapter.notifyDataSetChanged();
            swipeRefresh.setRefreshing(false);
        }), filter);
    }

    private static void showSnackBar(String msg) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());
        Intent intent = new Intent(Function.getPackageName() + ".SHOW_SNACK_BAR_ACTIVITY");
        intent.putExtra("msg", msg);
        localBroadcastManager.sendBroadcast(intent);
    }

}
