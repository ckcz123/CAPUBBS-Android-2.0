package com.capubbs.lib;

import android.util.Log;

import com.capubbs.lib.json.*;

/**
 * Created by oc on 2016/10/13.
 */
public class XML2Json {

    public static String toJson(String xml) {
        try {
            //String string=XML.toJSONObject(xml).optJSONObject("capu").optJSONArray("info").toString();
            JSONObject jsonObject=XML.toJSONObject(xml).optJSONObject("capu");
            String string;
            if (jsonObject.optJSONArray("info")!=null)
                string=jsonObject.optJSONArray("info").toString();
            else {
                JSONArray jsonArray=new JSONArray();
                jsonArray.put(jsonObject.optJSONObject("info")==null?jsonObject:jsonObject.optJSONObject("info"));
                string=jsonArray.toString();
            }
            Log.w("json", string);
            return string;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code", 20);
            jsonObject.put("msg", "XML����ʧ�ܣ�����ϵ����Ա�Ի�ȡ������");
            JSONArray jsonArray=new JSONArray();
            jsonArray.put(jsonObject);
            return jsonArray.toString();
        }
        catch (Exception e) {e.printStackTrace();}
        return "";
    }

    public static String getErrorMessage(int code) {
        switch (code) {
            case -25:return "��ʱ�������µ�¼ (errorcode: -1)";
            case 1:return "������� (errorcode: 1)";
            case 2:return "�û������� (errorcode: 2)";
            case 3:return "���ѱ���� (errorcode: 3)";
            case 4:return "���η���ʱ��������15s (errorcode: 4)";
            case 5:return "�����ѱ����� (errorcode: 5)";
            case 6:return "�ڲ����� (errorcode: 6)";
            case 7:return "ֻ�ܱ༭�Լ������� (errorcode: 7)";
            case 8:return "�û����зǷ��ַ� (errorcode: 8)";
            case 9:return "�û����Ѵ��� (errorcode: 9)";
            case 10:return "Ȩ�޲��� (errorcode: 10)";
            case 11:return "��ֱ��ɾ������ (errorcode: 11)";
            default:break;
        }
        return "�����ڲ����� (errorcode: "+code+")������ϵ����Ա��ȡ������";
    }


}
