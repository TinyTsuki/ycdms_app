package cn.antraces.ycdms;

import static cn.antraces.ycdms.utils.Constants.RegUrl;
import static cn.antraces.ycdms.utils.Constants.UnknownErrorJsonString;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.Objects;

import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.QRCodeUtil;
import cn.antraces.ycdms.utils.fns.Http;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class QrcodeFragment extends Fragment implements View.OnClickListener {

    protected Activity mActivity;
    private final String TAG = "QrcodeFragment";
    View view;
    ImageView qrcodeView;
    Button button;
    int qid;
    String qrurl;

    private static final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.view_main_page_qrcode, container, false);
        qrcodeView = (ImageView) view.findViewById(R.id.qrcode);

        button = (Button) view.findViewById(R.id.refresh_qrcode);
        button.setOnClickListener(this);
        qrcodeView.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.refresh_qrcode) {
            refreshQrcode();
        }
        if (view.getId() == R.id.qrcode && null != qrurl) {
            ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("RegURL", qrurl);
            clipboard.setPrimaryClip(clip);
            handler.post(() -> ((MainActivity) mActivity).showSnackBar(getString(R.string.copied_to_clipboard)));
        }
    }

    private void refreshQrcode() {
        Http.getRegToken(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, e.getMessage());
                handler.post(() -> ((MainActivity) mActivity).showSnackBar(getString(R.string.request_failed)));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String back = Objects.requireNonNull(response.body()).string();
                LogUtil.v("getMembers", back);
                JSONObject json;
                try {
                    json = JSONObject.parseObject(back);
                } catch (JSONException e) {
                    json = JSONObject.parseObject(UnknownErrorJsonString);
                }
                if (json.getInteger("code") > 0) {
                    qid = json.getJSONObject("data").getInteger("id");
                    String token = json.getJSONObject("data").getString("token");
                    qrurl = RegUrl + "?token=" + token;
                    Bitmap qrcode = QRCodeUtil.createQRCodeBitmap(qrurl, 480,
                            "UTF-8", "H", "1",
                            Color.BLACK, Color.WHITE, null,
                            BitmapFactory.decodeResource(getResources(), R.drawable.qrlogo), 0.24F);
                    handler.post(() -> qrcodeView.setImageBitmap(qrcode));
                } else {
                    String msg = json.getString("msg");
                    handler.post(() -> ((MainActivity) mActivity).showSnackBar(msg));
                }
            }
        });
    }
}
