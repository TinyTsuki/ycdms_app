package cn.antraces.ycdms.utils.fns;

import static android.content.Context.NOTIFICATION_SERVICE;
import static cn.antraces.ycdms.utils.Constants.BoundUrl;
import static cn.antraces.ycdms.utils.Constants.ChangePhotoUrl;
import static cn.antraces.ycdms.utils.Constants.ChangeQQUrl;
import static cn.antraces.ycdms.utils.Constants.CheckPermissionUrl;
import static cn.antraces.ycdms.utils.Constants.DelMemberUrl;
import static cn.antraces.ycdms.utils.Constants.DownloadWorkerInfo;
import static cn.antraces.ycdms.utils.Constants.GetMembersUrl;
import static cn.antraces.ycdms.utils.Constants.GetQrInfoUrl;
import static cn.antraces.ycdms.utils.Constants.GetRegTokenUrl;
import static cn.antraces.ycdms.utils.Constants.GetWorkerInfo;
import static cn.antraces.ycdms.utils.Constants.IsBoundUrl;
import static cn.antraces.ycdms.utils.Constants.IsRegTokenUsedUrl;
import static cn.antraces.ycdms.utils.Constants.LoginUrl;
import static cn.antraces.ycdms.utils.Constants.UnknownErrorJsonString;
import static cn.antraces.ycdms.utils.fns.Function.getNotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import cn.antraces.ycdms.LoginActivity;
import cn.antraces.ycdms.MainActivity;
import cn.antraces.ycdms.MyApplication;
import cn.antraces.ycdms.R;
import cn.antraces.ycdms.entity.Member;
import cn.antraces.ycdms.utils.HttpUtil;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.MD5Util;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Http {
    private static final String TAG = "Http";

    /**
     * 验证Token
     *
     * @param token    token
     * @param callback 回调函数
     */
    public static void isBound(String token, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();

        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, IsBoundUrl, new HashMap<>(), body, callback);
    }

    /**
     * 登陆
     *
     * @param token    token
     * @param qq       QQ
     * @param psw      密码
     * @param callback 回调函数
     */
    public static void login(String token, String qq, String psw, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .add("qq", qq)
                .add("psw", MD5Util.getMD5Code(psw))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, LoginUrl, new HashMap<>(), body, callback);
    }

    /**
     * 注册
     *
     * @param token    token
     * @param qq       QQ
     * @param psw      密码
     * @param callback 回调函数
     */
    public static void bound(String token, String qq, String psw, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .add("qq", qq)
                .add("psw", MD5Util.getMD5Code(psw))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, BoundUrl, new HashMap<>(), body, callback);
    }

    /**
     * 验证是否登陆
     *
     * @param cookies  cookies
     * @param runnable runnable
     */
    public static void checkPermission(List<Cookie> cookies, Runnable runnable) {
        RequestBody body = new FormBody.Builder()
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, CheckPermissionUrl, new HashMap<>(), body, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.v(TAG, e.getMessage());

                MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
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
                if (json.getInteger("code") == 1) {
                    boolean login = false;
                    for (Cookie c : cookies) {
                        if (c.name().equals("request_token") && c.expiresAt() * 1000 > System.currentTimeMillis() + 2 * 60 * 1000) {
                            login = true;
                            break;
                        }
                    }

                    if (!login) { //判断是否登陆, 登陆是否过期
                        runnable.run();
                    } else {
                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                MyApplication.getContext().startActivity(new Intent(MyApplication.getContext(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        };
                        timer.schedule(task, 500);
                    }
                } else {
                    runnable.run();
                }
            }
        });
    }

    /**
     * 获取会员信息
     *
     * @param members  对象
     * @param runnable runnable
     */
    public static void getMembers(List<Member> members, Runnable runnable, String filter) {
        String md5 = MD5Util.getMD5Code(Function.readFile("members.json"));
        LogUtil.v(TAG, md5);
        RequestBody body = new FormBody.Builder()
                .add("md5", md5)
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, GetMembersUrl, new HashMap<>(), body, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, e.getMessage());
                getLocalMember(members, runnable, filter);
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
                if (json.getInteger("code") == 1) {
                    Function.saveFile("members.json", back.replaceFirst(String.valueOf(json.getLong("date")), "0"));
                    List<Member> memberList = json.getJSONArray("data").toJavaList(Member.class);
                    if (null != members) {
                        members.clear();
                        if (!memberList.isEmpty()) {
                            if (!"".equals(filter)) {
                                for (Member member : memberList) {
                                    String id = String.valueOf(member.getId());
                                    String qq = String.valueOf(member.getQq());
                                    String phone = String.valueOf(member.getPhone());
                                    String name = member.getName();
                                    String cn = member.getCn();
                                    boolean b = false;
                                    if (id.equals(filter)) b = true;
                                    if (qq.contains(filter)) b = true;
                                    if (phone.contains(filter)) b = true;
                                    if (name.contains(filter)) b = true;
                                    if (cn.contains(filter)) b = true;
                                    if (!b) continue;
                                    members.add(member);
                                }
                            } else {
                                members.addAll(memberList);
                            }
                        }
                    }
                    if (null != runnable)
                        runnable.run();
                } else if (json.getString("msg").equals("ok") && json.getString("data").equals(md5)) {
                    if (null != members && members.size() == 0) {
                        String jsonStr = Function.readFile("members.json");
                        if (!"".equals(jsonStr)) {
                            JSONObject object;
                            try {
                                object = JSONObject.parseObject(jsonStr);
                            } catch (JSONException e) {
                                object = JSONObject.parseObject(UnknownErrorJsonString);
                            }
                            if (object.getInteger("code") == 0) {
                                String msg = object.getString("msg");
                                showSnackBar(msg);
                            } else {
                                List<Member> memberList = object.getJSONArray("data").toJavaList(Member.class);
                                if (!memberList.isEmpty()) {
                                    if (!"".equals(filter)) {
                                        for (Member member : memberList) {
                                            String id = String.valueOf(member.getId());
                                            String qq = String.valueOf(member.getQq());
                                            String phone = String.valueOf(member.getPhone());
                                            String name = member.getName();
                                            String cn = member.getCn();
                                            boolean b = false;
                                            if (id.equals(filter)) b = true;
                                            if (qq.contains(filter)) b = true;
                                            if (phone.contains(filter)) b = true;
                                            if (name.contains(filter)) b = true;
                                            if (cn.contains(filter)) b = true;
                                            if (!b) continue;
                                            members.add(member);
                                        }
                                    } else {
                                        members.addAll(memberList);
                                    }
                                }
                                if (null != runnable)
                                    runnable.run();
                            }
                        }
                    } else {
                        getLocalMember(members, runnable, filter);
                    }
                } else {
                    String msg = json.getString("msg");
                    showSnackBar(msg);
                }
            }
        });

    }

    private static void getLocalMember(List<Member> members, Runnable runnable, String filter) {
        if (null != members) {
            String jsonStr = Function.readFile("members.json");
            if (!"".equals(jsonStr)) {
                JSONObject object;
                try {
                    object = JSONObject.parseObject(jsonStr);
                } catch (Exception e) {
                    object = JSONObject.parseObject(UnknownErrorJsonString);
                }
                if (object.getInteger("code") == 0) {
                    String msg = object.getString("msg");
                    showSnackBar(msg);
                } else {
                    List<Member> memberList = object.getJSONArray("data").toJavaList(Member.class);
                    if (!memberList.isEmpty()) {
                        if (!"".equals(filter)) {
                            members.clear();
                            for (Member member : memberList) {
                                String id = String.valueOf(member.getId());
                                String qq = String.valueOf(member.getQq());
                                String phone = String.valueOf(member.getPhone());
                                String name = member.getName();
                                String cn = member.getCn();
                                boolean b = false;
                                if (id.equals(filter)) b = true;
                                if (qq.contains(filter)) b = true;
                                if (phone.contains(filter)) b = true;
                                if (name.contains(filter)) b = true;
                                if (cn.contains(filter)) b = true;
                                if (!b) continue;
                                members.add(member);
                            }
                        } else if (memberList.size() == members.size()) {
                            showSnackBar(MyApplication.getContext().getString(R.string.no_more));
                        } else {
                            members.clear();
                            members.addAll(memberList);
                        }
                    }
                    if (null != runnable)
                        runnable.run();
                }
            }
        }
    }

    /**
     * 获取会员注册信息
     *
     * @param qrid     二维码ID
     * @param callback 回调函数
     */
    public static void getQrInfo(int qrid, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(qrid))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, GetQrInfoUrl, new HashMap<>(), body, callback);
    }

    /**
     * 删除会员
     *
     * @param id       会员编号
     * @param callback 回调函数
     */
    public static void delMember(int id, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, DelMemberUrl, new HashMap<>(), body, callback);
    }


    /**
     * 修改会员照片状态
     *
     * @param id       会员编号
     * @param photo    照片状态
     * @param callback 回调函数
     */
    public static void changePhoto(int id, int photo, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("photo", String.valueOf(photo))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, ChangePhotoUrl, new HashMap<>(), body, callback);
    }

    /**
     * 修改会员QQ
     *
     * @param id       会员编号
     * @param qq       要修改的QQ
     * @param callback 回调函数
     */
    public static void changeQQ(int id, long qq, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .add("qq", String.valueOf(qq))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, ChangeQQUrl, new HashMap<>(), body, callback);
    }

    /**
     * 获取注册码
     *
     * @param callback 回调函数
     */
    public static void getRegToken(Callback callback) {
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, GetRegTokenUrl, new HashMap<>(), new FormBody.Builder().build(), callback);
    }

    /**
     * 获取注册码使用状态
     *
     * @param id       二维码ID
     * @param callback 回调函数
     */
    public static void isRegTokenUsed(int id, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("id", String.valueOf(id))
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, IsRegTokenUsedUrl, new HashMap<>(), body, callback);
    }

    /**
     * 获取管理员信息
     *
     * @param token    Token
     * @param callback 回调函数
     */
    public static void getWorkerInfo(String token, Callback callback) {
        RequestBody body = new FormBody.Builder()
                .add("token", token)
                .build();
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, GetWorkerInfo, new HashMap<>(), body, callback);

    }

    /**
     * 下载会员信息表
     */
    public static void downloadWorkerInfo(Runnable runnableFailed, Runnable runnableSuccess) {
        //初始化Cookie
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, DownloadWorkerInfo, new HashMap<>(), new FormBody.Builder().build(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, e.getMessage());
                runnableFailed.run();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    String fileName = response.header("Content-Disposition", "attachment;filename=1").split(";")[1].split("=")[1];

                    File file = new File(savePath, fileName);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        LogUtil.d(TAG, sum + "/" + total);
                    }
                    fos.flush();

                    runnableSuccess.run();

                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //设置intent的Action属性
                    intent.setAction(Intent.ACTION_VIEW);
                    //设置intent的data和Type属性。
                    intent.setDataAndType(Uri.parse(savePath + "/" + fileName), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                    PendingIntent pi = PendingIntent.getActivity(MyApplication.getContext(), 0, intent, 0);
                    NotificationManager manager = (NotificationManager) MyApplication.getContext().getSystemService(NOTIFICATION_SERVICE);
                    manager.notify(2, getNotification(MyApplication.getContext().getString(R.string.download_successful),
                            MyApplication.getContext().getString(R.string.click_to_open) + "『" + fileName + "』", pi));

                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtil.d(TAG, "download failed");
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException ignored) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        });
    }

    private static void showSnackBar(String msg) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyApplication.getContext());
        Intent intent = new Intent(Function.getPackageName() + ".SHOW_SNACK_BAR_ACTIVITY");
        intent.putExtra("msg", msg);
        localBroadcastManager.sendBroadcast(intent);
    }
}
