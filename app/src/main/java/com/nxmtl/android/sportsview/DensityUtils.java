package com.nxmtl.android.sportsview;

import android.content.res.Resources;

public class DensityUtils {

    private DensityUtils() {

    }

    /**
     * dp转换成px
     */
    public static float dp2px(float dpValue){
        float scale= Resources.getSystem().getDisplayMetrics().density;
        return dpValue * scale;
    }

    /**
     * px转换成dp
     */
    public static float px2dp(float pxValue){
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return pxValue / scale;
    }

    /**
     * sp转换成px
     */
    public static float sp2px(float spValue){
        float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return spValue * fontScale;
    }

    /**
     * px转换成sp
     */
    public static float px2sp(float pxValue){
        float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return pxValue / fontScale;
    }
}