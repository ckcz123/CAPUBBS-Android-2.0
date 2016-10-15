package com.capubbs;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

import com.capubbs.lib.BaseActivity;
import com.capubbs.lib.Constants;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.XML2Json;
import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;

import com.capubbs.lib.json.JSONObject;

import java.util.ArrayList;

public class PostActivity extends BaseActivity {
    String type = "";
    String board = "";
    String threadid = "";
    String postid = "";
    String quote = "";
    String text = "";

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ("".equals(Userinfo.token)) {
            CustomToast.showInfoToast(this, "���ȵ�¼��");
            super.wantToExit();
            return;
        }

        Bundle bundle = getIntent().getExtras();

        type = bundle.getString("type", "post");

        board = bundle.getString("bid", "");
        threadid = bundle.getString("tid", "");
        String title = bundle.getString("title", "").trim();
        postid = bundle.getString("pid", "");
        quote = bundle.getString("quote","");
        text = bundle.getString("text","");

        setContentView(R.layout.bbs_postpage);

        String tt = "��������";
        if ("reply".equals(type)) tt = "�ظ�����";
        if ("edit".equals(type)) tt = "�༭����";
        setTitle(tt);
        if (!"".equals(title)) {
            ViewSetting.setEditTextValue(this, R.id.bbs_postpage_title, "Re: " + title);
        }
        Button button = (Button) findViewById(R.id.bbs_postpage_button);
        String hint = "����";
        if ("reply".equals(type)) hint = "�ظ�";
        if ("edit".equals(type)) hint = "�༭";
        button.setText(hint);
        button.setOnClickListener(v -> post());
        if ("reply".equals(type)) {
            if (!"".equals(quote)) {
                ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, quote+"\n\n");
            }
        }
        if ("edit".equals(type)) {
            ViewSetting.setEditTextValue(this,R.id.bbs_postpage_text, text);
        }
        if (!"".equals(title))
            findViewById(R.id.bbs_postpage_text).requestFocus();

    }

    protected void finishRequest(int type, String string) {
        if (type == Constants.REQUEST_BBS_POST)
            finishPost(string);
    }

    @SuppressWarnings("unchecked")
    void post() {

        if ("edit".equals(type)) {
            edit();
            return;
        }

        String title = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_title).trim();
        String text = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_text);
        if ("".equals(title)) {
            CustomToast.showErrorToast(this, "���ⲻ��Ϊ�գ�", 1500);
            return;
        }
        //if (!text.contains("���� CAPUBBS"))
        //    text += "\n\n--\n���� CAPUBBS (Android " + Constants.version + ")\n";
        //CheckBox checkBox = (CheckBox) findViewById(R.id.bbs_postpage_anonymous);
        //String anonymous = checkBox.isChecked() ? "1" : "0";

        String sig="0";
        if (((RadioButton)findViewById(R.id.sig1)).isChecked()) sig="1";
        if (((RadioButton)findViewById(R.id.sig2)).isChecked()) sig="2";
        if (((RadioButton)findViewById(R.id.sig3)).isChecked()) sig="3";

        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("ask", "post"));
        arrayList.add(new Parameters("token", Userinfo.token));
        arrayList.add(new Parameters("bid", board));
        arrayList.add(new Parameters("title", title));
        arrayList.add(new Parameters("text", text));
        arrayList.add(new Parameters("sig", sig));
        arrayList.add(new Parameters("os","android"));
        if ("reply".equals(type)) {
            arrayList.add(new Parameters("tid", threadid));
        }
        new RequestingTask(this, "���ڷ���...", Constants.bbs_url,
                Constants.REQUEST_BBS_POST).execute(arrayList);

    }

    @SuppressWarnings("unchecked")
    void edit() {
        String title = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_title).trim();
        String text = ViewSetting.getEditTextValue(this, R.id.bbs_postpage_text);
        if ("".equals(title)) {
            CustomToast.showErrorToast(this, "���ⲻ��Ϊ�գ�", 1500);
            return;
        }
        //if (!text.contains("���� CAPUBBS"))
        //    text += "\n\n--\n���� CAPUBBS (Android " + Constants.version + ")\n";
        String sig="0";
        if (((RadioButton)findViewById(R.id.sig1)).isChecked()) sig="1";
        if (((RadioButton)findViewById(R.id.sig2)).isChecked()) sig="2";
        if (((RadioButton)findViewById(R.id.sig3)).isChecked()) sig="3";

        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("ask", "post"));
        arrayList.add(new Parameters("token", Userinfo.token));
        arrayList.add(new Parameters("bid", board));
        arrayList.add(new Parameters("title", title));
        arrayList.add(new Parameters("text", text));
        arrayList.add(new Parameters("sig", sig));
        arrayList.add(new Parameters("tid", threadid));
        arrayList.add(new Parameters("pid", postid));
        new RequestingTask(this, "�����޸�...", Constants.bbs_url,
                Constants.REQUEST_BBS_POST).execute(arrayList);

    }

    void finishPost(String string) {
        try {
            JSONObject jsonObject = new JSONArray(string).getJSONObject(0);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                CustomToast.showErrorToast(this, jsonObject.optString("msg", XML2Json.getErrorMessage(code)), 1500);
                return;
            }
            setResult(RESULT_OK);
            CustomToast.showSuccessToast(this, "����ɹ���");
            finish();
        } catch (Exception e) {
            CustomToast.showErrorToast(this, "����ʧ��", 1500);
        }
    }

    public void finishGetEdit(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                CustomToast.showErrorToast(this,
                        jsonObject.optString("msg", "������ȡʧ��"), 1300);
                super.wantToExit();
                return;
            }
            String title = jsonObject.optString("title");
            String text = jsonObject.optString("text");

            ViewSetting.setEditTextValue(this, R.id.bbs_postpage_title, title);
            ViewSetting.setEditTextValue(this, R.id.bbs_postpage_text, "\n\n" + text + "\n");
            findViewById(R.id.bbs_postpage_text).requestFocus();
            ((EditText) findViewById(R.id.bbs_postpage_text)).setSelection(0);

        } catch (Exception e) {
            CustomToast.showErrorToast(this, "���ݻ�ȡʧ��", 1300);
            super.wantToExit();
        }
    }

}
