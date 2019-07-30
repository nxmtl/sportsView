package com.nxmtl.android.sportsview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.DrawableRes;

public class Utils {
    public static Bitmap getBitmap(Resources resources, @DrawableRes int id, int width,int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, id, options);
        options.inJustDecodeBounds = false;
        if(width/height > options.outWidth/options.outHeight){
            options.inDensity = options.outWidth;
            options.inTargetDensity = width;
        }else {
            options.inDensity = options.outHeight;
            options.inTargetDensity = height;
        }
        return BitmapFactory.decodeResource(resources, id, options);
    }
}
