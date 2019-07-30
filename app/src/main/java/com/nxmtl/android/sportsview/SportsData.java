package com.nxmtl.android.sportsview;

public class SportsData {
    /**
     * 进度 0-100
     */
    public int progress;
    /**
     * 步数
     */
    public int step;
    /**
     * 路程 米
     */
    public float distance;
    /**
     * 卡路里 千卡
     */
    public int calories;

    public SportsData() {
        progress = 0;
        step = 0;
        distance = 0;
        calories = 0;
    }

    public SportsData(SportsData sportsData) {
        this();
        if (sportsData != null) {
            this.progress = sportsData.progress;
            this.step = sportsData.step;
            this.distance = sportsData.distance;
            this.calories = sportsData.calories;
        }
    }
}