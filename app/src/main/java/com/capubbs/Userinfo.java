package com.capubbs;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.View;

import com.capubbs.lib.Constants;
import com.capubbs.lib.Editor;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.XML2Json;
import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.view.BadgeView;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;
import com.capubbs.lib.webconnection.WebConnection;

import com.capubbs.lib.json.JSONObject;

import java.util.ArrayList;

public class Userinfo {
    static String username, password, token;
    static Dialog dialog;
    static boolean givehint = false;

    public static void load(BBSActivity bbsActivity) {
        username = Editor.getString(bbsActivity, "username");
        password = Editor.getString(bbsActivity, "password");
        token="";
        if (Editor.getBoolean(bbsActivity,"autologin",true))
            autoLogin(bbsActivity);
    }

    public static void autoLogin(BBSActivity bbsActivity) {
        if ("".equals(username)) return;
        new Thread(() -> {
            ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
            arrayList.add(new Parameters("ask", "login"));
            arrayList.add(new Parameters("username", username));
            arrayList.add(new Parameters("password", Util.md5(password)));
            arrayList.add(new Parameters("os","android"));
            arrayList.add(new Parameters("device", android.os.Build.MODEL));
            arrayList.add(new Parameters("version", android.os.Build.VERSION.RELEASE));
            Parameters parameters = WebConnection.connect(Constants.bbs_url, arrayList);
            if ("200".equals(parameters.name)) {
                bbsActivity.handler.sendMessage(Message.obtain(
                        bbsActivity.handler, Constants.MESSAGE_BBS_LOGIN, parameters.value));
            }
        }).start();
        givehint = false;
    }

    @SuppressWarnings("unchecked")
    public static void login(BBSActivity bbsActivity) {
        if ("".equals(username)) {
            showLoginView(bbsActivity);
            return;
        }
        ArrayList<Parameters> arrayList = new ArrayList<>();
        arrayList.add(new Parameters("ask", "login"));
        arrayList.add(new Parameters("username", username));
        arrayList.add(new Parameters("password", Util.md5(password)));
        arrayList.add(new Parameters("os","android"));
        arrayList.add(new Parameters("device", android.os.Build.MODEL));
        arrayList.add(new Parameters("version", android.os.Build.VERSION.RELEASE));
        new RequestingTask(bbsActivity, "���ڵ�¼ ...", Constants.bbs_url,
                Constants.REQUEST_BBS_LOGIN).execute(arrayList);
        givehint = true;
    }

    public static void showLoginView(BBSActivity bbsActivity) {
        username = Editor.getString(bbsActivity, "username");
        password = Editor.getString(bbsActivity, "password");

        dialog = new Dialog(bbsActivity);
        dialog.setContentView(R.layout.bbs_login_view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle("��¼CAPUBBS");
        ViewSetting.setEditTextValue(dialog, R.id.username, username);
        ViewSetting.setEditTextValue(dialog, R.id.password, password);
        ViewSetting.setOnClickListener(dialog, R.id.bbs_login, v -> {
            String u = ViewSetting.getEditTextValue(dialog, R.id.username).trim(),
                    p = ViewSetting.getEditTextValue(dialog, R.id.password).trim();
            if ("".equals(u) || "".equals(p)) {
                CustomToast.showInfoToast(bbsActivity, "�˺Ż����벻��Ϊ�գ�", 1500);
                return;
            }
            username = u;
            password = p;
            login(bbsActivity);
        });
        ViewSetting.setOnClickListener(dialog, R.id.bbs_cancel, v -> dialog.dismiss());
        dialog.show();
    }

    public static void finishLogin(BBSActivity bbsActivity, String string) {
        try {
            JSONObject jsonObject = new JSONArray(string).getJSONObject(0);
            int code = jsonObject.getInt("code");
            if (code != 0) {
                CustomToast.showErrorToast(bbsActivity, jsonObject.optString("msg", XML2Json.getErrorMessage(code)));
                return;
            }
            Editor.putString(bbsActivity, "username", username);
            Editor.putString(bbsActivity, "password", password);
            token = jsonObject.optString("token");
            try {
                dialog.dismiss();
            } catch (Exception e) {
            }
            if (givehint)
                CustomToast.showSuccessToast(bbsActivity, "��¼�ɹ���", 1000);
            givehint = false;
            //	BadgeView.show(BBSActivity.bbsActivity, BBSActivity.bbsActivity.findViewById(R.id.bbs_bottom_img_me)
            //			, "new");
            SettingsFragment.set(bbsActivity);
        } catch (Exception e) {
            CustomToast.showErrorToast(bbsActivity, "��¼ʧ��");
        }

    }

    public static void logout(BBSActivity bbsActivity) {
        Editor.putString(bbsActivity, "username", "");
        Editor.putString(bbsActivity, "password", "");
        username = password = token ="";
        SettingsFragment.set(bbsActivity);
        BadgeView.show(bbsActivity,
                bbsActivity.findViewById(R.id.bbs_bottom_img_settings), "");
    }

}
