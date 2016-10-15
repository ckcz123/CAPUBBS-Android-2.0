package com.capubbs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.capubbs.lib.BaseActivity;
import com.capubbs.lib.Constants;
import com.capubbs.lib.MyBitmapFactory;
import com.capubbs.lib.MyFile;
import com.capubbs.lib.Util;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.subactivity.SubActivity;
import com.capubbs.lib.view.CustomToast;

import org.jsoup.examples.HtmlToPlainText;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewActivity extends BaseActivity {

    public static final int PAGESIZE = 12;

    static final int PAGE_NONE = 0;
    static final int PAGE_THREAD = 1;
    static final int PAGE_POST = 2;
    int showingPage;
    int urlNum;
    String board;
    String boardName;
    String threadid;
    boolean startFromParent = false;
    public static int listWidth;

    ViewThread viewThread;
    ViewPost viewPost;

    Handler handler = new Handler(msg -> {
        if (msg.what == Constants.MESSAGE_IMAGE_REQUEST_FINISHED) {
            updateImage(msg.arg1);
        }
        return false;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        listWidth=(int)(displayMetrics.widthPixels-(getResources().getDisplayMetrics().density*20+0.5f));

        init();
        String type = getIntent().getStringExtra("type");
        viewThread=new ViewThread(this);
        viewPost=new ViewPost(this);
        showingPage = PAGE_NONE;
        if ("board".equals(type)) {
            startFromParent = false;
            viewThread.getThreads(1);
        }
        if ("thread".equals(type)) {
            startFromParent = true;
            String threadid = getIntent().getStringExtra("tid");
            if (threadid == null) {
                CustomToast.showErrorToast(this, "û��tid��");
                super.wantToExit();
                return;
            }
            viewPost.selectNum = getIntent().getIntExtra("pid", 1);
            viewPost.getPosts(threadid, (viewPost.selectNum-1) / PAGESIZE + 1);
        }
    }

    void init() {
        board = getIntent().getStringExtra("bid");
        if (board == null || "".equals(board)) {
            CustomToast.showErrorToast(this, "û��������棡");
            super.wantToExit();
            return;
        }

        Board bd = Board.boards.get(board);
        if (bd == null) {
            CustomToast.showErrorToast(this, "û��������棡");
            super.wantToExit();
            return;
        }
        boardName = bd.name;
    }

    public void finishRequest(int type, String string) {
        if (type == Constants.REQUEST_BBS_GET_LIST)
            viewThread.finishRequest(string);
        if (type == Constants.REQUEST_BBS_GET_POST)
            viewPost.finishRequest(string);
    }

    void updateImage(int postid) {
        if (showingPage == PAGE_POST) {
            ListView listView = (ListView) findViewById(R.id.bbs_post_listview);
            int size = viewPost.postInfos.size();
            for (int i = 0; i < size; i++) {
                PostInfo postInfo = viewPost.postInfos.get(i);
                if (postInfo.pid == postid) {
                    int cnt = listView.getChildCount();
                    for (int j = 0; j < cnt; j++) {
                        try {
                            View view = listView.getChildAt(j);
                            if (view == null) continue;
                            if ((Integer) view.getTag() != i) continue;
                            ViewSetting.setTextView(view, R.id.bbs_post_item_text,
                                    Html.fromHtml(postInfo.content, source -> {
                                        if (source.startsWith("/"))
                                            source = "http://www.chexie.net" + source;
                                        else if (source.startsWith("../"))
                                            source = "http://www.chexie.net/bbs"+source.substring(2);
                                        final File file = MyFile.getCache(ViewActivity.this, Util.getHash(source));
                                        if (file.exists()) {
                                            Bitmap bitmap = MyBitmapFactory.getCompressedBitmap(file.getAbsolutePath(), 2.5);
                                            if (bitmap != null) {
                                                Drawable drawable = new BitmapDrawable(ViewActivity.this.getResources(), bitmap);
                                                int width=drawable.getIntrinsicWidth(), height=drawable.getIntrinsicHeight();
                                                if (width>ViewActivity.listWidth)
                                                {
                                                    height=(ViewActivity.listWidth*height)/width;
                                                    width=ViewActivity.listWidth;
                                                }
                                                drawable.setBounds(0, 0, width, height);
                                                return drawable;
                                            }
                                        }
                                        return null;
                                    }, null));
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (showingPage == PAGE_THREAD) {
            if (viewThread.page > 1)
                menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_PREVIOUS, Constants.MENU_BBS_VIEW_PREVIOUS, "")
                        .setIcon(R.drawable.ic_menu_back).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (viewThread.page < viewThread.totalPage)
                menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_NEXT, Constants.MENU_BBS_VIEW_NEXT, "")
                        .setIcon(R.drawable.ic_menu_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_POST, Constants.MENU_BBS_VIEW_POST, "��������");
        }
        if (showingPage == PAGE_POST) {
            if (viewPost.page > 1)
                menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_PREVIOUS, Constants.MENU_BBS_VIEW_PREVIOUS, "")
                        .setIcon(R.drawable.ic_menu_back).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            if (viewPost.page < viewPost.totalPage)
                menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_NEXT, Constants.MENU_BBS_VIEW_NEXT, "")
                        .setIcon(R.drawable.ic_menu_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, Constants.MENU_BBS_FAVORITE, Constants.MENU_BBS_FAVORITE,
                    "�ղش�������");
            menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_POST, Constants.MENU_BBS_VIEW_POST, "�ظ�");
            menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_SHARE, Constants.MENU_BBS_VIEW_SHARE, "����");
        }
        menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_JUMP, Constants.MENU_BBS_VIEW_JUMP, "��ҳ");
        menu.add(Menu.NONE, Constants.MENU_BBS_VIEW_EXIT, Constants.MENU_BBS_VIEW_EXIT, "������ҳ");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == Constants.MENU_BBS_VIEW_PREVIOUS) {
            if (showingPage == PAGE_THREAD)
                viewThread.getThreads(viewThread.page - 1);
            else if (showingPage == PAGE_POST)
                viewPost.getPosts(threadid, viewPost.page - 1);
            return true;
        }
        if (id == Constants.MENU_BBS_VIEW_NEXT) {
            if (showingPage == PAGE_THREAD)
                viewThread.getThreads(viewThread.page + 1);
            else if (showingPage == PAGE_POST)
                viewPost.getPosts(threadid, viewPost.page + 1);
            return true;
        }
        if (id == Constants.MENU_BBS_FAVORITE) {
            if (FavoriteActivity.addFavorite(this, board, Integer.parseInt(threadid), viewPost.title,
                    viewPost.postInfos.get(0).author)) {
                CustomToast.showSuccessToast(this, "�ղسɹ���");
            }
            else CustomToast.showInfoToast(this, "�����ղش���������");
            return true;
        }
        if (id == Constants.MENU_BBS_VIEW_SHARE && showingPage == PAGE_POST) {
            viewPost.share();
            return true;
        }
        if (id == Constants.MENU_BBS_VIEW_POST) {
            if (showingPage == PAGE_THREAD) {
                Intent intent = new Intent(this, PostActivity.class);
                intent.putExtra("type", "post");
                intent.putExtra("bid", board);
                startActivityForResult(intent, PAGE_THREAD);
            } else if (showingPage == PAGE_POST) {
                Intent intent = new Intent(this, PostActivity.class);
                intent.putExtra("type", "reply");
                intent.putExtra("bid", board);
                intent.putExtra("tid", threadid);
                intent.putExtra("title", viewPost.title);
                startActivityForResult(intent, PAGE_POST);
            }
            return true;
        }
        if (id == Constants.MENU_BBS_VIEW_JUMP) {
            if (showingPage == PAGE_THREAD) {
                viewThread.jump();
            } else if (showingPage == PAGE_POST) {
                viewPost.jump();
            }
            return true;
        }
        if (id == Constants.MENU_BBS_VIEW_EXIT) {
            super.wantToExit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (showingPage == PAGE_POST) {
            PostInfo postInfo = viewPost.postInfos.get(viewPost.index);
            String content = postInfo.content;

            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_POST_LZL,
                    Constants.CONTEXT_MENU_BBS_POST_LZL, "�ظ�¥��¥");
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_POST,
                    Constants.CONTEXT_MENU_BBS_POST, "����");
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_EDIT,
                    Constants.CONTEXT_MENU_BBS_EDIT, "�༭");

            urlNum = 0;
            Matcher matcher = Patterns.WEB_URL.matcher(content);
            ArrayList<String> urls=new ArrayList<>();
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String url = content.substring(start, end);
                String tmp = url.toLowerCase(Locale.getDefault());
                if (tmp.startsWith("http://") || tmp.startsWith("https://")) {
                    if (url.endsWith("'") || url.endsWith("\"")
                            || url.endsWith(")") || url.endsWith("]")
                            || url.endsWith("}"))
                        url = url.substring(0, url.length() - 1);
                    url=url.replace("&amp;","&");
                    if (urls.contains(url)) continue;
                    urls.add(url);

                    if (url.matches("http(s)?://(www\\.)?chexie.net/bbs/main(.+?)+")) {
                        String bid=Util.getValue(url, "bid");
                        if (Board.containBoard(bid)) {
                            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                    "���ӣ�"+Board.getNameById(bid)+"��");
                        }
                        else
                            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                    url);
                    }
                    else if (url.matches("http(s)?://(www\\.)?chexie.net/bbs/content(.+?)+")) {
                        String bid=Util.getValue(url, "bid");
                        if (Board.containBoard(bid)) {
                            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                    "���ӣ�"+Board.getNameById(bid)+"��"+Util.getValue(url,"tid")+"��");
                        }
                        else menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                url);
                    }
                    else if (url.startsWith("http://www.chexie.net/cgi-bin/bbs.pl?")) {
                        String bid=Util.getValue(url,"b");
                        if ("".equals(bid))
                        {
                            String id=Util.getValue(url,"id");
                            if ("act".equals(id)) bid="1";
                            if ("capu".equals(id)) bid="2";
                            if ("bike".equals(id)) bid="3";
                            if ("water".equals(id)) bid="4";
                            if ("acad".equals(id)) bid="5";
                            if ("asso".equals(id)) bid="6";
                            if ("skill".equals(id)) bid="7";
                            if ("race".equals(id)) bid="9";
                            if ("web".equals(id)) bid="28";
                        }
                        String str=Util.getValue(url,"see");
                        String tid="";
                        if (str.length()==4) {
                            tid=((str.charAt(0)-'a')*26*26*26+(str.charAt(1)-'a')*26*26
                                    +(str.charAt(2)-'a')*26+(str.charAt(3)-'a')+1)+"";
                        }
                        if (Board.containBoard(bid) && !"".equals(tid)) {
                            menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                    "���ӣ�"+Board.getNameById(bid)+"��"+tid+"��");
                        }
                        else menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                url);

                    }
                    else menu.add(Menu.NONE, Constants.CONTEXT_MENU_BBS_URL + urlNum, Constants.CONTEXT_MENU_BBS_URL + urlNum,
                                url);

                    urlNum++;
                }
            }

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==Constants.CONTEXT_MENU_BBS_POST_LZL) {
            PostInfo postInfo=viewPost.postInfos.get(viewPost.index);
            Intent intent=new Intent(this, LZLActivity.class);
            intent.putExtra("fid", postInfo.fid);
            intent.putExtra("add",true);
            startActivityForResult(intent,4);
            return true;
        }

        if (id == Constants.CONTEXT_MENU_BBS_POST
                && showingPage == PAGE_POST) {
            PostInfo postInfo = viewPost.postInfos.get(viewPost.index);
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("type", "reply");
            intent.putExtra("bid", board);
            intent.putExtra("tid", threadid);
            intent.putExtra("title", viewPost.title);
            String quote=Html.fromHtml(postInfo.content).toString();
            if (quote.length()>100) quote=quote.substring(0,95)+"...";
            intent.putExtra("quote","[quote="+postInfo.author+"]"+quote+"[/quote]");
            startActivityForResult(intent, PAGE_POST);
            return true;
        }
        if (id == Constants.CONTEXT_MENU_BBS_EDIT) {
            PostInfo postInfo = viewPost.postInfos.get(viewPost.index);
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("type", "edit");
            intent.putExtra("bid", board);
            intent.putExtra("tid", threadid);
            intent.putExtra("pid",postInfo.pid+"");
            intent.putExtra("title", viewPost.title);
            intent.putExtra("text", Html.fromHtml(postInfo.content).toString());
            startActivityForResult(intent, 3);
            return true;
        }
        if (id >= Constants.CONTEXT_MENU_BBS_URL && id < Constants.CONTEXT_MENU_BBS_URL + urlNum) {
            String url = item.getTitle().toString();

            Pattern pattern=Pattern.compile("([\\u4e00-\\u9fa5]+)(\\d+)?");
            Matcher matcher=pattern.matcher(url.replace("���ӣ�","").replace("��","").replace("��",""));
            if (matcher.find()) {
                String text=matcher.group(1);
                String tid=matcher.group(2);
                Log.w("tid-text",tid+"-"+text);
                if (!"0".equals(Board.getIdByName(text))) {
                    Intent intent=new Intent(this, ViewActivity.class);
                    intent.putExtra("bid",Board.getIdByName(text));
                    if (tid!=null && !"".equals(tid)) {
                        intent.putExtra("type", "thread");
                        intent.putExtra("tid",tid);
                    }
                    else intent.putExtra("type","board");
                    startActivity(intent);
                    return true;
                }
            }
            Intent intent = new Intent(this, SubActivity.class);
            intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
            intent.putExtra("url", url);
            startActivity(intent);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    protected void wantToExit() {
        if (showingPage == PAGE_POST && !startFromParent) {
            viewThread.viewThreads();
            return;
        }
        super.wantToExit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == PAGE_THREAD) {
            viewThread.getThreads(1);
        } else if (requestCode == PAGE_POST) {
            viewPost.getPosts(threadid, viewPost.totalPage);
        } else if (requestCode == 3) {
            viewPost.getPosts(threadid, viewPost.page);
        } else if (requestCode == 4 && showingPage==PAGE_POST) {
            int lzlnum=data.getIntExtra("lzl",-1);
            if (lzlnum!=-1) {
                viewPost.postInfos.get(viewPost.index).lzl=lzlnum;
                ListView listView = (ListView) findViewById(R.id.bbs_post_listview);
                ((BaseAdapter)listView.getAdapter()).notifyDataSetChanged();
            }
        }
    }

}
