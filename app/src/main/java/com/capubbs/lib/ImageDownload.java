package com.capubbs.lib;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.ImageView;


import com.capubbs.R;
import com.capubbs.lib.webconnection.WebConnection;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageDownload {
    public static final int IMAGE_REQUEST_FINISHED = 0;
    public static final int IMAGE_REQUEST_FAILED = 1;

    ArrayList<ImageView> imageViews = new ArrayList<ImageView>();
    ArrayList<String> urls = new ArrayList<String>();
    Activity activity;
    EventHandler eventHandler;
    @SuppressLint("UseSparseArrays")
    HashMap<Integer, Drawable> hashMap = new HashMap<Integer, Drawable>();

    public ImageDownload(Activity _activity, ImageView imageView,
                         String url) {
        activity = _activity;
        eventHandler = new EventHandler(activity.getMainLooper());
        imageViews.add(imageView);
        urls.add(url);
    }

    public ImageDownload(Activity _activity, ArrayList<ImageView> _imageViews,
                         ArrayList<String> _urls) {
        activity = _activity;
        imageViews = new ArrayList<ImageView>(_imageViews);
        urls = new ArrayList<String>(_urls);
        eventHandler = new EventHandler(activity.getMainLooper());
    }

    /**
     * �������ڹ��캯���д���Activity��ImageViewʱ�ſɱ�����
     * ����˺�����ʲô������
     */
    public void requestAndSetImages() {
        if (activity == null || imageViews == null) return;
        int len = imageViews.size();
        for (int i = 0; i < len; i++) {
            final int j = i;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        InputStream inputStream = WebConnection.connect(urls.get(j));
                        Drawable drawable = Drawable.createFromStream(inputStream, j + ".png");
                        eventHandler.sendMessage(Message.obtain(
                                eventHandler, IMAGE_REQUEST_FINISHED, j, 0, drawable));
                    } catch (Exception e) {
                        eventHandler.sendMessage(Message.obtain(
                                eventHandler, IMAGE_REQUEST_FAILED, j, 0));
                    }
                }
            }).start();
        }
    }

    class EventHandler extends Handler {
        public EventHandler() {
            super();
        }

        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            try {
                int id = message.arg1;
                ImageView imageView = imageViews.get(id);
                Drawable drawable;
                switch (message.what) {
                    case IMAGE_REQUEST_FAILED:
                        imageView.setImageResource(R.drawable.failure);
                        break;
                    case IMAGE_REQUEST_FINISHED:
                        drawable = (Drawable) message.obj;
                        imageView.setImageDrawable(drawable);
                        break;
                }
            } catch (Exception e) {
            }
        }
    }

}
