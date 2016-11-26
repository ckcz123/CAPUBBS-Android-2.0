package com.capubbs.lib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.capubbs.R;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class Share {
    private static final String[] sharesTo = {"���͸�΢�ź���", "��������Ȧ"};
    private static final String WX_APP_ID = "wxb064bb9685c7b1a0";


    private static IWXAPI api;

    public static void readyToShareURL(final Context context, final String dialogTitle,
                                       final String url, final String title, final String content, final Bitmap bitmap) {
        new AlertDialog.Builder(context).setTitle(dialogTitle)
                .setItems(sharesTo, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                            urlToWx(context, url, title, content, bitmap, false);
                        else if (which == 1)
                            urlToWx(context, url, title, content, bitmap, true);
                    }
                }).setCancelable(true).show();
    }

    public static void readyToShareImage(final Context context, final String dialogTitle,
                                         final Bitmap bitmap) {
        if (bitmap == null) return;
        new AlertDialog.Builder(context).setTitle(dialogTitle)
                .setItems(sharesTo, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                            imgToWx(context, bitmap, false);
                        else if (which == 1)
                            imgToWx(context, bitmap, true);
                    }
                }).setCancelable(true).show();
    }

    public static void readyToShareText(final Context context, final String dialogTitle,
                                        final String text) {
        new AlertDialog.Builder(context).setTitle(dialogTitle)
                .setItems(sharesTo, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0)
                            textToWx(context, text, false);
                        else if (which == 1)
                            textToWx(context, text, true);
                    }
                }).setCancelable(true).show();
    }

    public static void urlToWx(Context context, String url, String title, String content,
                               Bitmap bitmap, boolean isToTimeline) {
        api = regAPI(context);
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        String _con = new String(content);
        if (_con.length() >= 40) _con = _con.substring(0, 38) + "...";
        msg.description = _con;
        if (bitmap == null)
            bitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.logo_share)).getBitmap();
        byte[] bts = MyBitmapFactory.bitmapToArray(bitmap, 31);
        if (bts.length >= 31 * 1024) {
            bitmap = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.logo_share)).getBitmap();
            bts = MyBitmapFactory.bitmapToArray(bitmap, 31);
        }
        msg.thumbData = bts;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Util.getHash("webpage" + System.currentTimeMillis());
        req.message = msg;
        req.scene = isToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        Log.w("success?", api.sendReq(req) + "");
    }

    public static void textToWx(Context context, String text, boolean isToTimeline) {
        api = regAPI(context);
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Util.getHash("text" + System.currentTimeMillis());
        req.message = msg;
        req.scene = isToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        Log.w("success?", api.sendReq(req) + "");
    }

    public static void imgToWx(Context context, Bitmap bitmap, boolean isToTimeline) {
        api = regAPI(context);

        WXImageObject imgObj = new WXImageObject(bitmap);

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        Bitmap thumbBmp = Bitmap.createScaledBitmap(bitmap, 150, 150, true);

        msg.thumbData = MyBitmapFactory.bitmapToArray(thumbBmp, 31);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = Util.getHash("image" + System.currentTimeMillis());
        req.message = msg;
        req.scene = isToTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    private static IWXAPI regAPI(Context context) {
        if (api == null) {
            api = WXAPIFactory.createWXAPI(context, WX_APP_ID, false);
            api.registerApp(WX_APP_ID);
        }

        return api;
    }
}
