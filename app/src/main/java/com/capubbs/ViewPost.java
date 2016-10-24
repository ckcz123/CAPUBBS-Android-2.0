package com.capubbs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.capubbs.lib.Constants;
import com.capubbs.lib.Editor;
import com.capubbs.lib.MyBitmapFactory;
import com.capubbs.lib.MyCalendar;
import com.capubbs.lib.MyFile;
import com.capubbs.lib.RequestingTask;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.XML2Json;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.webconnection.Parameters;

import com.capubbs.lib.json.JSONArray;
import com.capubbs.lib.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ViewPost {
    ArrayList<PostInfo> postInfos = new ArrayList<PostInfo>();
    int page;
    int tmpPage;
    int totalPage;
    String tmpThreadid;
    String title;
    int index;
    String tmpBoard;
    int selectNum;
    int selection = 0;

    ViewActivity viewActivity;
    public ViewPost(ViewActivity activity) {viewActivity=activity;}

    @SuppressWarnings("unchecked")
    public void getPosts(String threadid, int page) {
        ArrayList<Parameters> arrayList = new ArrayList<Parameters>();
        arrayList.add(new Parameters("ask", "show"));
        arrayList.add(new Parameters("p", page + ""));
        arrayList.add(new Parameters("bid", viewActivity.board));
        arrayList.add(new Parameters("tid", threadid));
        arrayList.add(new Parameters("token", Userinfo.token));
        new RequestingTask(viewActivity, "���ڻ�ȡ����...",
                Constants.bbs_url, Constants.REQUEST_BBS_GET_POST)
                .execute(arrayList);
        tmpPage = page;
        tmpThreadid = threadid;
    }

    void finishRequest(String string) {
        try {
            int tmpNum = selectNum;
            selectNum = 0;
            selection = 0;

            JSONArray jsonArray=new JSONArray(string);
            int code = jsonArray.getJSONObject(0).getInt("code");
            if (code != 0 && code != -1) {
                CustomToast.showErrorToast(viewActivity,
                        jsonArray.getJSONObject(0).optString("msg", XML2Json.getErrorMessage(code)));
                viewActivity.setContentView(R.layout.bbs_thread_listview);
                viewActivity.showingPage = ViewActivity.PAGE_THREAD;
                return;
            }
            totalPage = jsonArray.getJSONObject(0).getInt("pages");
            title = jsonArray.getJSONObject(0).optString("title");
            postInfos.clear();
            int len = jsonArray.length();
            for (int i = 0; i < len; i++) {
                JSONObject post = jsonArray.getJSONObject(i);
                PostInfo postInfo = new PostInfo(post.optString("author"), post.getInt("floor"),
                        post.getInt("fid"), post.optString("time"), post.optInt("lzl"), post.optString("text"), post.optString("sig"));
                postInfos.add(postInfo);
                int number = post.getInt("floor");
                if (tmpNum == number)
                    selection = i;
            }

            page = tmpPage;
            viewActivity.threadid = tmpThreadid;
            viewPosts();
        } catch (Exception e) {
            postInfos.clear();
            CustomToast.showErrorToast(viewActivity, "��ȡʧ��");
        }
    }

    public void viewPosts() {
        viewActivity.setContentView(R.layout.bbs_post_listview);
        viewActivity.showingPage = ViewActivity.PAGE_POST;
        viewActivity.invalidateOptionsMenu();
        viewActivity.setTitle("(" + page + "/" + totalPage + ") " + title);
        final ListView listView = (ListView) viewActivity.findViewById(R.id.bbs_post_listview);
        listView.setAdapter(new BaseAdapter() {

            @SuppressLint("ViewHolder")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                convertView = viewActivity.getLayoutInflater().inflate(R.layout.bbs_post_item,
                        parent, false);
                final PostInfo postInfo = postInfos.get(position);

                ViewSetting.setTextView(convertView, R.id.bbs_post_item_text,
                        Html.fromHtml(postInfo.content+(!"".equals(new String(postInfo.sig).trim())?("<br><br>--<br>"+postInfo.sig):"")+"<br>", source -> {
                                    if (source == null) return null;
                                    if (source.startsWith("/"))
                                        source = "http://www.chexie.net" + source;
                                    else if (source.startsWith("../"))
                                        source = "http://www.chexie.net/bbs"+source.substring(2);
                                    final File file = MyFile.getCache(viewActivity, Util.getHash(source));
                                    if (file.exists()) {
                                        Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
                                        if (bitmap != null) {
                                            Drawable drawable = new BitmapDrawable(viewActivity.getResources(), bitmap);
                                            int width=drawable.getIntrinsicWidth(), height=drawable.getIntrinsicHeight();
                                            width*=1.8;height*=1.8;
                                            if (width>ViewActivity.listWidth)
                                            {
                                                height=(ViewActivity.listWidth*height)/width;
                                                width=ViewActivity.listWidth;
                                            }
                                            drawable.setBounds(0, 0, width, height);
                                            return drawable;
                                        }
                                    }
                                    if (Editor.getBoolean(viewActivity, "picture", true)) {
                                        final String url = source;
                                        new Thread(() -> {
                                            if (MyFile.urlToFile(url, file))
                                                viewActivity.handler.sendMessage(
                                                        Message.obtain(viewActivity.handler, Constants.MESSAGE_IMAGE_REQUEST_FINISHED, postInfo.pid, 0));
                                        }).start();
                                    }
                                    return null;
                                }, null));

                ViewSetting.setTextView(convertView, R.id.bbs_post_item_floor, postInfo.pid + "");
                ViewSetting.setTextView(convertView, R.id.bbs_post_item_author,
                        postInfo.author);
                ViewSetting.setTextView(convertView, R.id.bbs_post_item_time,
                        MyCalendar.format(postInfo.timestamp));
                ViewSetting.setTextView(convertView, R.id.bbs_post_item_lzl, "["+postInfo.lzl+"]");
                convertView.setTag(position);
                return convertView;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public int getCount() {
                return postInfos.size();
            }
        });
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            index = position;
            return false;
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {

            if (!Editor.getBoolean(viewActivity, "hint_post", false)) {
                new android.support.v7.app.AlertDialog.Builder(viewActivity)
                        .setTitle("ʹ����ʾ").setMessage("������Ӳ�����¥��¥���򵥻����Ӻͳ������Ӿ���" +
                        "�����˵���\n������Ӵ���¥��¥���򵥻����Ӳ鿴¥��¥���������Ӻ����˵���\n\n����ʾ��������ʾ��")
                        .setPositiveButton("��֪����",null).show();
                Editor.putBoolean(viewActivity, "hint_post", true);
                return;
            }

            index = position;
            PostInfo postInfo=postInfos.get(position);
            if (postInfo.lzl!=0) {
                Intent intent=new Intent(viewActivity, LZLActivity.class);
                intent.putExtra("fid", postInfo.fid);
                viewActivity.startActivityForResult(intent,4);
                return;
            }
            listView.showContextMenu();
        });
        listView.setSelection(selection);
        selection = 0;
        viewActivity.registerForContextMenu(listView);
    }

    public void jump() {
        AlertDialog.Builder builder = new AlertDialog.Builder(viewActivity);
        builder.setTitle("��ҳ");
        builder.setNegativeButton("ȡ��", null);
        final Spinner spinner = new Spinner(builder.getContext());
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 1; i <= totalPage; i++)
            arrayList.add(i + "");
        spinner.setAdapter(new ArrayAdapter<String>(builder.getContext(),
                android.R.layout.simple_spinner_item, arrayList));
        builder.setView(spinner);
        spinner.setSelection(page - 1);
        builder.setPositiveButton("ȷ��", (dialog, which) -> {
            getPosts(viewActivity.threadid, spinner.getSelectedItemPosition() + 1);
        });
        builder.show();
    }

    public void share() {
        CustomToast.showInfoToast(viewActivity, "�������ݻ����ߣ�");
    }

}
