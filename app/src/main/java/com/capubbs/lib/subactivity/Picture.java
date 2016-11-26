package com.capubbs.lib.subactivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import com.capubbs.R;
import com.capubbs.lib.*;
import com.capubbs.lib.view.CustomToast;
import com.capubbs.lib.view.MyNotification;
import com.capubbs.lib.view.TouchImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Picture {
    SubActivity subActivity;
    String title;
    TouchImageView touchImageView;
    Bitmap bitmap = null;
    String filepath = null;

    public Picture(SubActivity s) {
        subActivity = s;
    }

    public Picture showPicture(Integer res, String _title) {
        init(_title);
        touchImageView.setImageResource(res);
        return this;
    }

    public Picture showPicture(String url) {
        File file = MyFile.getCache(subActivity, Util.getHash(url));
        if (MyFile.urlToFile(url, file)) {
            return showPicture(file.getAbsolutePath(), "�鿴ͼƬ");
        } else {
            CustomToast.showErrorToast(subActivity, "ͼƬ����ʧ��");
            subActivity.wantToExit();
            return this;
        }
    }

    public Picture showPicture(String filepath, String _title) {
        init(_title);
        this.filepath = filepath;
        bitmap = MyBitmapFactory.getCompressedBitmap(filepath, -1);
        touchImageView.setImageBitmap(bitmap);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (bitmap != null) {
                    // TODO: 16/9/5 fix bug qr code
                    subActivity.handler.sendMessage(Message.obtain(subActivity.handler,
                            Constants.MESSAGE_SUBACTIVITY_DECODE_PICTURE,
                            MyBitmapFactory.decodeQRCode(bitmap)));
                }
            }
        }).start();
        return this;
    }

    void init(String _title) {
        title = _title;
        title = title.trim();
        if (title.endsWith(".jpg") || title.endsWith(".jpeg") || title.endsWith(".bmp")
                || title.endsWith(".png") || title.endsWith(".gif")) {
            title = title.substring(0, title.lastIndexOf("."));
        }
        if ("".equals(title)) title = "�鿴ͼƬ";
        subActivity.setTitle(title);
        subActivity.setContentView(R.layout.subactivity_imageview);
        touchImageView = (TouchImageView) subActivity.findViewById(R.id.subactivity_imageview);
        touchImageView.setScaleType(ScaleType.FIT_CENTER);
        touchImageView.setLongClickable(true);
        subActivity.registerForContextMenu(touchImageView);
    }

    public void savePicture() throws Exception {
        if (this.filepath == null || "".equals(this.filepath)) {
            CustomToast.showErrorToast(subActivity, "��ͼƬ�޷����浽����");
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault());
        String time = simpleDateFormat.format(new Date());
        new File(Environment.getExternalStorageDirectory() + "/Pictures/capubbs/").mkdirs();
        String t = new String(title);
        if (t.length() >= 15) t = t.substring(0, 13) + "...";
        String filepath = Environment.getExternalStorageDirectory() + "/Pictures/capubbs/"
                + t + "__" + time + ".png";
        if (MyFile.copyFile(this.filepath, filepath)) {
            if (android.os.Build.VERSION.SDK_INT < 16)
                CustomToast.showSuccessToast(subActivity, "ͼƬ������\n" + filepath, 3500);
            else {
                MyNotification.sendNotificationToOpenfile("ͼƬ�ѱ���",
                        "ͼƬ������" + filepath, "ͼƬ������" + filepath, subActivity,
                        new File(filepath));
            }
        } else
            CustomToast.showErrorToast(subActivity, "����ʧ��");
		/*
		try {
			Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
			FileOutputStream fileOutputStream=new FileOutputStream(filepath);
			bitmap.compress(CompressFormat.PNG, 100, fileOutputStream);
			fileOutputStream.flush();
			fileOutputStream.close();
			CustomToast.showSuccessToast(subActivity, "ͼƬ������ "+filepath);
		}
		catch (Exception e) {e.printStackTrace();}
		*/
    }

    public void sharePicture() {
        if (bitmap == null) {
            CustomToast.showErrorToast(subActivity, "��ͼƬ���ɱ�����");
            return;
        }
        Share.readyToShareImage(subActivity, "����ͼƬ", bitmap);
        //CustomToast.showInfoToast(subActivity,"��������ʱ��δ���ߣ�");
    }

    public void decodePicture(final String string) {
        Log.w("qrcode", string);
        if (string.startsWith("http://")
                || string.startsWith("https://")) {
            if (string.startsWith("http://weixin.qq.com/")) {
                new AlertDialog.Builder(subActivity).setTitle("��ʾ")
                        .setMessage("΢���û���΢��Ⱥ�Ķ�ά���޷���ȷʶ��"
                                + "�����΢�ź�����΢�Ž���ʶ��")
                        .setPositiveButton("ȷ��", null).show();
                return;
            }
            Intent intent = new Intent(subActivity, SubActivity.class);
            intent.putExtra("type", Constants.SUBACTIVITY_TYPE_WEBVIEW);
            intent.putExtra("url", string);
            intent.putExtra("title", title);
            subActivity.startActivity(intent);
        } else {
            new AlertDialog.Builder(subActivity).setTitle("ʶ���ά��")
                    .setMessage(string).setPositiveButton("ȷ��", null).
                    setNegativeButton("����", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ClipboardManager clipboardManager = (ClipboardManager) subActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                            clipboardManager.setPrimaryClip(ClipData.newPlainText("text", string));
                            CustomToast.showSuccessToast(subActivity, "�Ѹ��Ƶ����а壡", 1500);
                        }
                    }).show();
        }
    }
}
