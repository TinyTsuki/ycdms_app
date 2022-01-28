package cn.antraces.ycdms;

import static cn.antraces.ycdms.utils.Constants.PopPageDel;
import static cn.antraces.ycdms.utils.Constants.PopPageEdit;
import static cn.antraces.ycdms.utils.Constants.PopPageInfo;
import static cn.antraces.ycdms.utils.Constants.PopPageNavHeader;
import static cn.antraces.ycdms.utils.Constants.PopPageSelect;
import static cn.antraces.ycdms.utils.Constants.PopPageTips;
import static cn.antraces.ycdms.utils.Constants.UnknownErrorJsonString;
import static cn.antraces.ycdms.utils.fns.Function.hintKeyBoard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.antraces.ycdms.entity.Member;
import cn.antraces.ycdms.entity.QrInfo;
import cn.antraces.ycdms.utils.LogUtil;
import cn.antraces.ycdms.utils.fns.Http;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

@SuppressLint("NonConstantResourceId")
public class PopOperationActivity extends Activity implements View.OnClickListener {
    private final String TAG = "PopOperationActivity";

    LinearLayout layout;
    int mode;

    List<Button> buttons = new ArrayList<>();
    List<EditText> editTexts = new ArrayList<>();
    List<TextView> textViews = new ArrayList<>();

    int[] bids;
    int[] eids;
    int[] tids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getIntExtra("type", 0);
        switch (mode) {
            case PopPageSelect:
                setContentView(R.layout.view_pop_select);
                layout = (LinearLayout) findViewById(R.id.pop_layout);
                bids = new int[]{R.id.btn_change_submit, R.id.btn_update_qq, R.id.btn_view_details, R.id.btn_cancel};
                for (int i : bids) {
                    Button button = (Button) findViewById(i);
                    button.setOnClickListener(this);
                    buttons.add(button);
                }
                //判断监听状态, 改变按钮显示
                if (getIntent().getIntExtra("status", 0) > 0) {
                    buttons.get(0).setText(R.string.set_not_submitted);
                } else {
                    buttons.get(0).setText(R.string.set_submitted);
                }
                break;
            case PopPageEdit:
                setContentView(R.layout.view_pop_edit);
                layout = (LinearLayout) findViewById(R.id.pop_layout);
                eids = new int[]{R.id.edit_qq};
                bids = new int[]{R.id.btn_cancel, R.id.btn_okay};
                for (int i : eids) {
                    EditText editText = (EditText) findViewById(i);
                    editTexts.add(editText);
                }
                for (int i : bids) {
                    Button button = (Button) findViewById(i);
                    button.setOnClickListener(this);
                    buttons.add(button);
                }
                break;
            case PopPageInfo:
                setContentView(R.layout.view_pop_info);
                layout = (LinearLayout) findViewById(R.id.pop_layout);
                tids = new int[]{R.id.info};
                bids = new int[]{R.id.btn_okay};
                for (int i : bids) {
                    Button button = (Button) findViewById(i);
                    button.setOnClickListener(this);
                    buttons.add(button);
                }
                for (int i : tids) {
                    TextView textView = findViewById(i);
                    textViews.add(textView);
                }
                Member member = (Member) getIntent().getSerializableExtra("data");
                textViews.get(0).setText(member.formatString(getString(R.string.loading), getString(R.string.loading), getString(R.string.loading)));
                Http.getQrInfo(member.getQrid(), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        LogUtil.e(TAG, e.getMessage());
                        runOnUiThread(() -> textViews.get(0).setText(getString(R.string.request_failed))
                        );
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String back = response.body().string();
                        LogUtil.v(TAG, back);
                        JSONObject json;
                        try {
                            json = JSONObject.parseObject(back);
                        } catch (JSONException e) {
                            json = JSONObject.parseObject(UnknownErrorJsonString);
                        }
                        if (json.getInteger("code") > 0) {
                            QrInfo qrInfo = JSONObject.toJavaObject(json.getJSONObject("data"), QrInfo.class);
                            String from = qrInfo.getJob() + " - " + qrInfo.getName();
                            String token = qrInfo.getToken();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time = sdf.format(qrInfo.getUsetime());
                            runOnUiThread(() -> textViews.get(0).setText(member.formatString(from, token, time)));
                        } else {
                            String msg = json.getString("msg");
                            runOnUiThread(() -> textViews.get(0).setText(msg)
                            );
                        }
                    }
                });
                break;
            case PopPageDel:
                setContentView(R.layout.view_pop_select_2);
                layout = (LinearLayout) findViewById(R.id.pop_layout);
                bids = new int[]{R.id.btn_delete_member, R.id.btn_cancel};
                for (int i : bids) {
                    Button button = (Button) findViewById(i);
                    button.setOnClickListener(this);
                    buttons.add(button);
                }
                break;
            case PopPageTips:
                setContentView(R.layout.view_pop_info);
                layout = (LinearLayout) findViewById(R.id.pop_layout);
                tids = new int[]{R.id.title, R.id.info};
                bids = new int[]{R.id.btn_okay};
                for (int i : bids) {
                    Button button = (Button) findViewById(i);
                    button.setOnClickListener(this);
                    buttons.add(button);
                }
                for (int i : tids) {
                    TextView textView = findViewById(i);
                    textViews.add(textView);
                }
                textViews.get(0).setText(getIntent().getStringExtra("title"));
                textViews.get(1).setText(getIntent().getStringExtra("content"));
                break;
            case PopPageNavHeader:
                setContentView(R.layout.view_pop_select);
                layout = (LinearLayout) findViewById(R.id.pop_layout);
                bids = new int[]{R.id.btn_change_submit, R.id.btn_update_qq, R.id.btn_view_details, R.id.btn_cancel};
                for (int i : bids) {
                    Button button = (Button) findViewById(i);
                    button.setOnClickListener(this);
                    buttons.add(button);
                }

                buttons.get(0).setText(getText(R.string.logout));
                buttons.get(1).setVisibility(View.GONE);
                buttons.get(2).setVisibility(View.GONE);

                break;
            default:
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (mode) {
            case PopPageEdit:
                hintKeyBoard(this);
                for (EditText i : editTexts) {
                    i.clearFocus();
                }
                break;
            case PopPageInfo:
                break;
            case PopPageNavHeader:
            case PopPageDel:
            case PopPageSelect:
                finish();
                overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        intent.putExtra("selected", view.getId());
        intent.putExtra("mode", mode);
        switch (view.getId()) {
            case R.id.pop_layout:
                return;
            case R.id.btn_okay:
                if (mode == PopPageEdit)
                    intent.putExtra("data", editTexts.get(0).getText().toString());
            case R.id.btn_update_qq:
            case R.id.btn_view_details:
            case R.id.btn_change_submit:
            case R.id.btn_delete_member:
                setResult(RESULT_OK, intent);
                break;
            case R.id.btn_cancel:
                setResult(RESULT_CANCELED, intent);
                break;
            default:
                break;
        }
        finish();
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out);
    }

}
