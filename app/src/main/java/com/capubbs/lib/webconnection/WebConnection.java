package com.capubbs.lib.webconnection;

import android.util.Log;

import com.capubbs.lib.XML2Json;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WebConnection {

    /**
     * 向服务器发送请求，并且接收到返回的数据
     *
     * @param url    请求地址
     * @param params 请求参数；如果参数为空使用get请求，如果不为空使用post请求
     * @return 一个 Parameters；name 存放 HTTP code（无法连接时为-1）；如果 code==200 那么 value
     * 存放的是返回的网页内容
     */
    public static Parameters connect(String url, ArrayList<Parameters> params) {
        if (params == null || params.size() == 0) {
            return connectWithGet(url);
        }
        try {
            url = url.trim();
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 4000);
            HttpConnectionParams.setSoTimeout(httpParams, 13000);
//			DefaultHttpClient httpClient=new DefaultHttpClient(httpParams);
            DefaultHttpClient httpClient = HTTPSClient.getHttpClient(httpParams);
            HttpPost httpPost = new HttpPost(url);

            Log.w("URL", url);
            List<NameValuePair> paramsList = new ArrayList<NameValuePair>();
            if (params != null) {
                for (Parameters paraItem : params) {
                    String string = paraItem.value;
                    if (string == null || "".equals(string)) continue;
                    paramsList.add(new BasicNameValuePair(paraItem.name, paraItem.value));
                    if (string.length() >= 300)
                        string = string.substring(0, 299);
                    Log.w(paraItem.name, string);
                }
            }
            Cookies.addCookie(httpPost);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
            if (paramsList.size() != 0)
                httpPost.setEntity(new UrlEncodedFormEntity(paramsList, "utf-8"));

            HttpResponse httpResponse = httpClient.execute(httpPost);

            Parameters parameters = new Parameters("", "");
            int returncode = httpResponse.getStatusLine().getStatusCode();

            boolean isGbk = false;
            String typeString = httpResponse.getFirstHeader("Content-type").getValue()
                    .toLowerCase(Locale.getDefault());
            if (typeString.contains("gbk") || typeString.contains("gb2312"))
                isGbk = true;
            Cookies.setCookie(httpResponse, url);

            parameters.name = returncode + "";

            if (returncode == 200) {
                BufferedReader bf;
                if (!isGbk)
                    bf = new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity().getContent()));
                else
                    bf = new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity().getContent(), "gbk"));
                String string = "";
                String line = bf.readLine();
                while (line != null) {
                    string = string + line + "\n";
                    line = bf.readLine();
                }
                string = string.trim();
                parameters.value = XML2Json.toJson(string);
            }
            return parameters;
        } catch (Exception e) {
            e.printStackTrace();
            return new Parameters("-1", "");
        }
    }

    private static Parameters connectWithGet(String url) {
        try {
            url = url.trim();
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 4000);
            HttpConnectionParams.setSoTimeout(httpParams, 13000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpGet httpGet = new HttpGet(url);
            Cookies.addCookie(httpGet);
            HttpResponse httpResponse = httpClient.execute(httpGet);

            Parameters parameters = new Parameters("", "");
            int returncode = httpResponse.getStatusLine().getStatusCode();

            boolean isGbk = false;
            String typeString = httpResponse.getFirstHeader("Content-type").getValue()
                    .toLowerCase(Locale.getDefault());
            if (typeString.contains("gbk") || typeString.contains("gb2312"))
                isGbk = true;
            Cookies.setCookie(httpResponse, url);

            parameters.name = returncode + "";

            if (returncode == 200) {
                BufferedReader bf;
                if (!isGbk)
                    bf = new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity().getContent()));
                else
                    bf = new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity().getContent(), "gbk"));
                String string = "";
                String line = bf.readLine();
                while (line != null) {
                    string = string + line + "\n";
                    line = bf.readLine();
                }
                parameters.value = XML2Json.toJson(string);
            }
            return parameters;
        } catch (Exception e) {
            return new Parameters("-1", "");
        }
    }

    /**
     * Return a inputstream for binary request.
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static InputStream connect(String url) throws IOException {
        url = url.trim();
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 4000);
        HttpConnectionParams.setSoTimeout(httpParams, 13000);
        HttpClient httpClient = new DefaultHttpClient(httpParams);
        Log.w("URL", url);
        HttpGet httpGet = new HttpGet(url);
        Cookies.addCookie(httpGet);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Cookies.setCookie(httpResponse, url);
        return httpResponse.getEntity().getContent();
    }

}