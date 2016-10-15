package com.capubbs.lib;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;
import com.capubbs.lib.webconnection.WebConnection;

import java.util.ArrayList;

public class RequestingTask extends AsyncTask<ArrayList<Parameters>, String, Parameters> {
    ProgressDialog progressDialog;
    String requestString;
    int requestType;
    BaseActivity baseActivity;

    /**
     * ����һ��http���ã��������ʧ����ôֱ��Toast���ѣ��޷���
     *
     * @param msg  ��ʾ��Ϣ
     * @param url  �����ַ
     * @param type ��������
     */
    public RequestingTask(BaseActivity activity, String msg, String url, int type) {
        baseActivity = activity;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("��ʾ");
        progressDialog.setMessage(msg);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        requestString = url;
        requestType = type;
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected Parameters doInBackground(ArrayList<Parameters>... params) {
        return WebConnection.connect(requestString, params[0]);
    }

    @Override
    protected void onPostExecute(Parameters parameters) {
        progressDialog.dismiss();
        if (!"200".equals(parameters.name)) {
            if ("-1".equals(parameters.name)) {
                CustomToast.showInfoToast(baseActivity, "�޷���������(-1,-1)");
            } else
                CustomToast.showInfoToast(baseActivity, "�޷����ӵ������� (HTTP " + parameters.name + ")");
        } else baseActivity.finishRequest(requestType, parameters.value);
    }

}
