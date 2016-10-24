package com.capubbs.lib.subactivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

import com.capubbs.R;
import com.capubbs.lib.BaseActivity;
import com.capubbs.lib.Constants;
import com.capubbs.lib.ViewSetting;
import com.capubbs.lib.view.CustomToast;

import java.util.Locale;

public class SubActivity extends BaseActivity {

    WebView webView;
    Picture picture;
    GifView gifView;
    MyWebView myWebView;
    SwipeRefreshLayout swipeRefreshLayout;

    int type;
    String url;
    String html;
    String decodeString = "";

    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == Constants.MESSAGE_SUBACTIVITY_DECODE_PICTURE) {
                decodeString = (String) msg.obj;
                if (!"".equals(decodeString)) {
                    SubActivity.this.closeContextMenu();
                    CustomToast.showInfoToast(SubActivity.this, "����ͼƬ����ʶ���ά��", 1200);
                }
                return true;
            }
            if (msg.what == Constants.MESSAGE_SLEEP_FINISHED) {
                try {
                    swipeRefreshLayout.setRefreshing(false);
                } catch (Exception e) {
                }
                return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getInt("type");
        url = bundle.getString("url", "");

        // �ж��ǲ���gif��ʽͼƬ
        if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE
                && bundle.getString("title", "�鿴ͼƬ")
                .toLowerCase(Locale.getDefault()).endsWith(".gif")) {
            type = Constants.SUBACTIVITY_TYPE_PICTURE_GIF;
        }

        if (type == Constants.SUBACTIVITY_TYPE_ABOUT)
            viewAbout();
        else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_RESOURCE)
            picture = new Picture(this).showPicture(bundle.getInt("resid"),
                    bundle.getString("title", "�鿴ͼƬ"));
        else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
            picture = new Picture(this).showPicture(bundle.getString("file"),
                    bundle.getString("title", "�鿴ͼƬ"));
        } else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_URL) {
            picture = new Picture(this).showPicture(bundle.getString("url"));
        } else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_GIF)
            gifView = new GifView(this).showGif(bundle.getString("file")
                    , bundle.getString("title", "�鿴ͼƬ"));
        else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW)
            myWebView = new MyWebView(this, bundle.getInt("sid"))
                    .showWebView(bundle.getString("title", ""), url);
        else if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML)
            myWebView = new MyWebView(this).showWebHtml(bundle.getString("title", "�鿴��ҳ"),
                    bundle.getString("html"));
        else {
            wantToExit();
            return;
        }
        try {
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.subactivity_swipeRefreshLayout);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
                        android.R.color.holo_green_light,
                        android.R.color.holo_blue_bright,
                        android.R.color.holo_orange_light);
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    public void onRefresh() {
                        setRefresh();
                    }
                });
            }
        } catch (Exception e) {
        }
    }

    void setRefresh() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                handler.sendEmptyMessage(Constants.MESSAGE_SLEEP_FINISHED);
            }
        }).start();
    }

    public void viewAbout() {
        setContentView(R.layout.about);
        setTitle("���ڱ����");
        ViewSetting.setTextView(this, R.id.about_version, Constants.version);
        ViewSetting.setTextView(this, R.id.about_time, Constants.update_time);
    }

    public void finishRequest(int type, String string) {}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW && webView.canGoBack()) {
                webView.goBack();
                return true;
            }

            wantToExit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW
                || type == Constants.SUBACTIVITY_TYPE_WEBVIEW_HTML) {
            if (myWebView != null && !myWebView.loading && !"".equals(url)) {
                menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_OPEN_IN_BROWSER, Constants.MENU_SUBACTIVITY_OPEN_IN_BROWSER, "")
                        .setIcon(R.drawable.ic_open_in_new_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SHARE, Constants.MENU_SUBACTIVITY_SHARE, "")
                        .setIcon(R.drawable.ic_share_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }
        if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
            menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SHARE, Constants.MENU_SUBACTIVITY_SHARE, "")
                    .setIcon(R.drawable.ic_share_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, "")
                    .setIcon(R.drawable.ic_save_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        }
        if (type == Constants.SUBACTIVITY_TYPE_PICTURE_GIF) {
            menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, Constants.MENU_SUBACTIVITY_SAVE_PICTURE, "")
                    .setIcon(R.drawable.ic_save_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        menu.add(Menu.NONE, Constants.MENU_SUBACTIVITY_CLOSE, Constants.MENU_SUBACTIVITY_CLOSE, "")
                .setIcon(R.drawable.ic_close_white_36dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == Constants.MENU_SUBACTIVITY_OPEN_IN_BROWSER) {
            String url = webView.getUrl();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }
        if (id == Constants.MENU_SUBACTIVITY_SHARE) {
/*			if (webView != null) {
				String url = webView.getUrl();
				if (url == null || "".equals(url)) {
					url = this.url;
					if (url == null || "".equals(url))
						return true;
				}
				String title = getTitle().toString();
				String content = html;
				if (content == null || "".equals(content))
					content = getIntent().getStringExtra("content");
				if (content == null) content = "���������鿴";
				Bitmap bitmap = null;
				if (getIntent().getBooleanExtra("hasBitmap", false)) {
					bitmap = (Bitmap) DataObject.getInstance().getObject();
				}
				Share.readyToShareURL(this, "������ҳ", url, title, content, bitmap);
			} else if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
				picture.sharePicture();
			}
*/
            CustomToast.showInfoToast(this,"�������ݻ����ߣ������ڴ�~");
            return true;
        }
        if (id == Constants.MENU_SUBACTIVITY_SAVE_PICTURE
                && type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
            try {
                picture.savePicture();
            } catch (Exception e) {
                CustomToast.showErrorToast(this, "����ʧ��", 1500);
            }
            return true;
        }
        if (id == Constants.MENU_SUBACTIVITY_SAVE_PICTURE
                && type == Constants.SUBACTIVITY_TYPE_PICTURE_GIF) {
            try {
                gifView.savePicture();
            } catch (Exception e) {
                CustomToast.showErrorToast(this, "����ʧ��", 1500);
            }
            return true;
        }
        if (id == Constants.MENU_SUBACTIVITY_CLOSE) {
            wantToExit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
            if (picture == null) return;
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE,
                    Constants.CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE, "����");
            menu.add(Menu.NONE, Constants.CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE,
                    Constants.CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE, "���浽�ֻ�");
            if (!"".equals(decodeString)) {
                menu.add(Menu.NONE, Constants.CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE,
                        Constants.CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE, "ʶ���ά��");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == Constants.CONTEXT_MENU_SUBACTIVITY_SAVE_PICTURE &&
                type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
            try {
                picture.savePicture();
            } catch (Exception e) {
                CustomToast.showErrorToast(this, "����ʧ��", 1500);
            }
            return true;
        }
        if (id == Constants.CONTEXT_MENU_SUBACTIVITY_SHARE_PICTURE &&
                type == Constants.SUBACTIVITY_TYPE_PICTURE_FILE) {
            picture.sharePicture();
            return true;
        }
        if (id == Constants.CONTEXT_MENU_SUBACTIVITY_DECODE_PICTURE) {
            picture.decodePicture(decodeString);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    protected void wantToExit() {
        if (type == Constants.SUBACTIVITY_TYPE_WEBVIEW && webView != null) {
            try {
                webView.stopLoading();
            } catch (Exception e) {
            }
        }
        if (gifView != null) gifView.stop();
        System.gc();
        super.wantToExit();
    }

}
