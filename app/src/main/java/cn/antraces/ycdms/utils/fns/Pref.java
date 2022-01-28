package cn.antraces.ycdms.utils.fns;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import cn.antraces.ycdms.MyApplication;

public class Pref {

    public static void set(String f, String key, int i) {
        SharedPreferences.Editor editor;
        editor = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE).edit();
        editor.putInt(key, i);
        editor.apply();
    }

    public static void set(String f, String key, Boolean b) {
        SharedPreferences.Editor editor;
        editor = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE).edit();
        editor.putBoolean(key, b);
        editor.apply();
    }

    public static void set(String f, String key, String s) {
        SharedPreferences.Editor editor;
        editor = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE).edit();
        editor.putString(key, s);
        editor.apply();
    }

    public static void set(String f, String key, long l) {
        SharedPreferences.Editor editor;
        editor = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE).edit();
        editor.putLong(key, l);
        editor.apply();
    }

    public static void clear(String f) {
        SharedPreferences.Editor editor;
        editor = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE).edit();
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE);
        for (String key : pref.getAll().keySet()) {
            editor.remove(key);
        }
        editor.apply();
    }

    public static int getI(String f, String key) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE);
        return pref.getInt(key, 0);
    }

    public static boolean getB(String f, String key) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }

    public static String getS(String f, String key) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE);
        return pref.getString(key, "");
    }

    public static long getL(String f, String key) {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences(f, MODE_PRIVATE);
        return pref.getLong(key, 0);
    }

    /**
     * 是否显示注册页
     *
     * @return 是/否, 默认为是
     */
    public static boolean isRegView() {
        SharedPreferences pref = MyApplication.getContext().getSharedPreferences("data", MODE_PRIVATE);
        return pref.getBoolean("isRegView", true);
    }

    /**
     * 设置首页是否显示注册页
     *
     * @param b 是/否
     */
    public static void setRegView(Boolean b) {
        SharedPreferences.Editor editor;
        editor = MyApplication.getContext().getSharedPreferences("data", MODE_PRIVATE).edit();
        editor.putBoolean("isRegView", b);
        editor.apply();
    }
}
