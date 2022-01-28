package cn.antraces.ycdms.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {

    /**
     * 为HttpGet 的 url 方便的添加多个name value 参数。
     *
     * @param url    URL
     * @param params BODY
     * @return String
     */
    public static String attachHttpGetParams(String url, LinkedHashMap<String, String> params) {

        Iterator<String> keys = params.keySet().iterator();
        Iterator<String> values = params.values().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("?");

        for (int i = 0; i < params.size(); i++) {
            String value = null;
            try {
                value = URLEncoder.encode(values.next(), "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            stringBuffer.append(keys.next() + "=" + value);
            if (i != params.size() - 1) {
                stringBuffer.append("&");
            }
        }

        return url + stringBuffer.toString();
    }

    public static void HTTP_GET(OkHttpClient okHttpClient, String url, HashMap<String, List<String>> headers, Callback callback) {
        //1.创建OkHttpClient对象
        // OkHttpClient okHttpClient = new OkHttpClient();
        //2.创建Request对象，设置一个url地址（百度地址）,设置请求方式。
        LogUtil.i("HttpUtils HTTP_GET", url);
        Request.Builder builder = new Request.Builder();
        addDefaultHeaders(headers);
        for (String key : headers.keySet())
            for (int i = 0; i < headers.get(key).size(); i++)
                builder.addHeader(key, headers.get(key).get(i));

        Request request = builder.url(url).get().build();
        //3.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(callback);
    }

    public static Response HTTP_GET(OkHttpClient okHttpClient, String url, HashMap<String, List<String>> headers) {
        LogUtil.i("HttpUtils HTTP_GET", url);
        Request.Builder builder = new Request.Builder();
        addDefaultHeaders(headers);
        if (headers.isEmpty())
            LogUtil.i("HttpUtils", "请求头依旧为空");
        for (String key : headers.keySet())
            for (int i = 0; i < headers.get(key).size(); i++)
                builder.addHeader(key, headers.get(key).get(i));
        Response back;
        try {
            back = okHttpClient.newCall(builder.url(url).get().build()).execute();
        } catch (IOException e) {
            back = null;
            LogUtil.e("HttpUtils HTTP_GET", e.getMessage());
        }
        return back;
    }

    public static void HTTP_POST(OkHttpClient okHttpClient, String url, HashMap<String, List<String>> headers, RequestBody requestBody, Callback callback) {
        //1.创建OkHttpClient对象
        // OkHttpClient okHttpClient = new OkHttpClient();
        //2.通过new FormBody()调用build方法,创建一个RequestBody,可以用add添加键值对
        //3.创建Request对象，设置URL地址，将RequestBody作为post方法的参数传入
        LogUtil.i("HttpUtils HTTP_POST", url);
        Request.Builder builder = new Request.Builder();
        addDefaultHeaders(headers);
        for (String key : headers.keySet())
            for (int i = 0; i < headers.get(key).size(); i++)
                builder.addHeader(key, headers.get(key).get(i));

        Request request = builder.url(url).post(requestBody).build();

        //4.创建一个call对象,参数就是Request请求对象
        Call call = okHttpClient.newCall(request);
        //5.请求加入调度,重写回调方法
        call.enqueue(callback);
    }

    public static Response HTTP_POST(OkHttpClient okHttpClient, String url, HashMap<String, List<String>> headers, RequestBody requestBody) {
        LogUtil.i("HttpUtils HTTP_POST", url);
        Request.Builder builder = new Request.Builder();
        addDefaultHeaders(headers);
        for (String key : headers.keySet())
            for (int i = 0; i < headers.get(key).size(); i++)
                builder.addHeader(key, headers.get(key).get(i));

        Response back;
        try {
            back = okHttpClient.newCall(builder.url(url).post(requestBody).build()).execute();
        } catch (IOException e) {
            back = null;
            LogUtil.e("HttpUtils HTTP_POST", e.getMessage());
        }
        return back;
    }

    private static void addDefaultHeaders(HashMap<String, List<String>> headers) {
        if (headers.isEmpty()) {
            LogUtil.v("HttpUtils", "请求头为空");
            ArrayList<String> header1 = new ArrayList<>();
            header1.add("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            headers.put("Accept", header1);
            ArrayList<String> header3 = new ArrayList<>();
            header3.add("zh-CN,zh;q=0.9");
            headers.put("Accept-Language", header3);
            ArrayList<String> header4 = new ArrayList<>();
            header4.add("keep-alive");
            headers.put("Connection", header4);
            ArrayList<String> header5 = new ArrayList<>();
            header5.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) AppleWebKit/537.36 Chrome");
            headers.put("User-Agent", header5);
        }
    }
}
