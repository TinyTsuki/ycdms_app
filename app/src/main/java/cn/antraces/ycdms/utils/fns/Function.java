package cn.antraces.ycdms.utils.fns;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static cn.antraces.ycdms.utils.fns.Pref.getS;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import androidx.core.app.NotificationCompat;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cn.antraces.ycdms.MyApplication;
import cn.antraces.ycdms.R;
import cn.antraces.ycdms.utils.HttpUtil;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.MD5Util;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

public class Function {

    public static String getQQHeadUrl() {
        return "https://q.qlogo.cn/headimg_dl?bs=qq&spec=140&dst_uin=" + getS("data", "qq");
    }

    public static String getQQHeadUrl(String qq) {
        return "https://q.qlogo.cn/headimg_dl?bs=qq&spec=140&dst_uin=" + qq;
    }

    public static String getQQHeadUrl(long qq) {
        return "https://q.qlogo.cn/headimg_dl?bs=qq&spec=140&dst_uin=" + qq;
    }

    public static String getPackageName() {
        String packageName;
        try {
            PackageManager pm = MyApplication.getContext().getPackageManager();
            packageName = MyApplication.getContext().getPackageName();
            if (packageName == null || packageName.length() <= 0) {
                packageName = "null";
            }
        } catch (Exception e) {
            LogUtil.e("getPackageName", e.getMessage());
            packageName = "null";
        }
        return packageName;
    }

    /**
     * 获取最新版本
     *
     * @param callback 回调函数
     */
    public static void getLatestVersion(Callback callback) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String id = Function.getPackageName();
        String token = MD5Util.getMD5Code(id + "." + sdf.format(new Date()));
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("token", token.substring(0, 25))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient.Builder().cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(MyApplication.getContext()))).build();
        HttpUtil.HTTP_POST(okHttpClient, "https://app.antraces.cn", new HashMap<String, List<String>>(), body, callback);
    }

    public static String getAppVersionName() {
        String versionName;
        try {
            PackageManager pm = MyApplication.getContext().getPackageManager();
            PackageInfo pi = pm.getPackageInfo(MyApplication.getContext().getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                versionName = "null";
            }
        } catch (Exception e) {
            LogUtil.e("getAppVersionName", e.getMessage());
            versionName = "null";
        }
        return "V" + versionName;
    }

    public static void saveFile(String name, String data) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = MyApplication.getContext().openFileOutput(name, MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (IOException e) {
            LogUtil.e("saveFile", e.getMessage());
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                LogUtil.e("saveFile", e.getMessage());
            }
        }
    }

    public static String readFile(String name) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = MyApplication.getContext().openFileInput(name);
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null)
                content.append(line);
        } catch (IOException e) {
            LogUtil.e("readFile", e.getMessage());
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    LogUtil.e("readFile", e.getMessage());
                }
        }
        return content.toString();
    }

    public static int getAppVersion() {
        int version;
        PackageManager pm = MyApplication.getContext().getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(MyApplication.getContext().getPackageName(), 0);
            version = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            version = 0;
            LogUtil.e("getAppVersion", e.getMessage());
        }
        return version;
    }

    /**
     * 隐藏软键盘，要防止报空指针
     */
    public static void hintKeyBoard(Activity activity) {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && activity.getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (activity.getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static boolean checkAppInstalled(String pkgName) {
        if (pkgName == null || pkgName.isEmpty()) {
            return false;
        }
        String packageName;
        try {
            packageName = MyApplication.getContext().getPackageManager().getPackageInfo(pkgName, PackageManager.GET_GIDS).packageName;
        } catch (PackageManager.NameNotFoundException e) {
            packageName = null;
            e.printStackTrace();
        }
        return packageName != null;
    }

    // 取得通知对象
    public static Notification getNotification(String title, String text, PendingIntent pi) {
        NotificationManager manager = (NotificationManager) MyApplication.getContext().getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //当大于Android8.0
            String id = "channel_1";
            String description = "143";
            NotificationChannel channel = new NotificationChannel(id, description, NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(MyApplication.getContext(), id)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.icon))
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .build();
            return notification;
        } else {
            //当小于Android8.0
            Notification notification = new NotificationCompat.Builder(MyApplication.getContext())
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(MyApplication.getContext().getResources(), R.drawable.icon))
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .build();
            return notification;
        }
    }
}
