package com.nxmtl.android.sportsview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SportsView extends View {


    /** 控件默认宽度 dp **/
    static final int DEFAULT_WIDTH = 400;
    /** 控件默认高度 dp **/
    static final int DEFAULT_HEIGHT = 300;

    /** 圆环半径大小 dp （画笔大小）**/
    private static final int BIG_CIRCLE_SIZE = 16;
    /** 圆环光晕效果大小 dp **/
    private static final float CIRCLE_BLUR_SIZE = 24;

    private static final int LINE_CIRCLE_NUM = 8;


    /** 虚线画笔大小 dp **/
    private static final float DOTTED_CIRCLE_WIDTH = 2f;
    /** 虚线间隔大小 dp **/
    private static final float DOTTED_CIRCLE_GAG = 1f;
    /** 实线画笔大小 dp **/
    private static final float SOLID_CIRCLE_WIDTH = 2f;
    /** 实线头的圆点大小 dp **/
    private static final float DOT_SIZE = 8f;

    /** 手表图标偏移 dp **/
    private static final int WATCH_OFFSET_DP = 84;
    /** 手表图标大小 dp **/
    private static final int WATCH_SIZE = 24;

    /** 外部接口相关 **/
    private SportsData sportsData = new SportsData();
    private int width;
    private int height;
    private float centerX;
    private float centerY;
    private int rotateDegree = 0;
    private Random mRandom = new Random();
    private RectF rectF = new RectF(0, 0, 0, 0);
    private RectF lineRectF = new RectF(0, 0, 0, 0);

    private Paint lineCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint bigCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /** 步数画笔 **/
    private Paint mainTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /** 副标题画笔 **/
    private Paint subTitlePaint =new Paint(Paint.ANTI_ALIAS_FLAG);
    /** 虚线画笔 **/
    private Paint dottedCirclePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    /** 实线画笔 **/
    private Paint solidCirclePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
    /** 点画笔 **/
    private Paint dotPaint= new Paint(Paint.ANTI_ALIAS_FLAG);



    private ParticleSystem particleSystem;
    private ArrayList<Particle> mParticles;
    private ArrayList<LineCircle> mLineCircles;
    private boolean isLoading = false;
    private float mainTitleOffsetY;
    private float subTitleOffsetY;
    private String subTitleSeparator;
    private String mainTitleString;
    private String subTitleString;
    private float subTitleOffsetX;
    private RectF solidCircleRectF;
    Shader bigCircleLinearGradient;
    Shader blurLinearGradient;
    private Bitmap backgroundBitmap;
    private Bitmap watchBitmap;
    private float bigCircleRadiusFactor=1;
    private ObjectAnimator objectAnimator;
    private boolean drawProgrees = false;


    public SportsView(Context context) {
        super(context);
        init();
    }

    public SportsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SportsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setParticles(ArrayList<Particle> particles) {
        mParticles = particles;
    }

    public void setSportsData(SportsData sportsData){
        this.sportsData = new SportsData(sportsData);
        refreshData();
    }

    void init(){
        initText();
        initProgressCircle();
        initBigCircle();
        initParticle();
        initLineCircle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(resolveSize(DEFAULT_WIDTH, widthMeasureSpec),
                resolveSize(DEFAULT_HEIGHT, heightMeasureSpec));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;
        centerX = width/2f;
        centerY = height/2f;

        int endColor = Color.parseColor("#33FFFFFF");
        int startColor = Color.WHITE;
        SweepGradient lineSweepGradient = new SweepGradient(centerX, centerY, Color.TRANSPARENT,startColor);
        lineCirclePaint.setShader(lineSweepGradient);
        mLineCircles = new ArrayList<>();
        for (int i = 0; i < LINE_CIRCLE_NUM; i++) {
            LineCircle circle = new LineCircle();
            circle.degrees = mRandom.nextFloat()*DensityUtils.dp2px(5);
            circle.radius = width * 0.38f+mRandom.nextFloat()*DensityUtils.dp2px(BIG_CIRCLE_SIZE);
            circle.centerX = mRandom.nextFloat()*DensityUtils.dp2px(5);
            circle.centerY = mRandom.nextFloat()*DensityUtils.dp2px(5);
            mLineCircles.add(circle);
        }


        int xmin = (int)(centerX+width*0.38);
        int xmax = (int)(xmin + DensityUtils.dp2px(BIG_CIRCLE_SIZE));
        int[] emitter = { xmin,xmax,(int)centerY,(int)centerY};
        particleSystem.prepareEmitting(50,emitter);

        bigCircleLinearGradient = new LinearGradient(
                centerX, centerY - width*0.38f,
                centerX , centerY+ width*0.38f,
                startColor,
                endColor,
                Shader.TileMode.CLAMP);

        blurLinearGradient = new LinearGradient(
                centerX, centerY- width*0.38f,
                centerX , centerY,
                startColor,
                endColor,
                Shader.TileMode.CLAMP);
        backgroundBitmap = Utils.getBitmap(getResources(),R.drawable.bg_step_law,width,height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(backgroundBitmap!=null){
            canvas.drawBitmap(backgroundBitmap,0,height-backgroundBitmap.getHeight(),null);
        }
        if(isLoading){
            drawLineCircles(canvas);
            drawParticle(canvas);
        }else {
            drawBigCircle(canvas);
            drawProgressCircle(canvas);
        }
        drawText(canvas);
    }



    public void setIsLoading(boolean isLoading){
        this.isLoading = isLoading;
        if(!isLoading){
            getObjectAnimator().start();
        }else {
            invalidate();
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 0, TIMER_TASK_INTERVAL);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
    }

    private void initText(){
        watchBitmap = Utils.getBitmap(getResources(),R.drawable.icon_headview_watch, (int) DensityUtils.dp2px(WATCH_SIZE),(int)DensityUtils.dp2px(WATCH_SIZE));
        mainTitlePaint.setColor(Color.WHITE);
        mainTitlePaint.setTextAlign(Paint.Align.CENTER);
        mainTitlePaint.setTextSize(DensityUtils.sp2px(64));
        mainTitleOffsetY = -(mainTitlePaint.getFontMetrics().ascent +
                mainTitlePaint.getFontMetrics().descent) / 2;

        subTitlePaint.setColor(Color.parseColor("#CCFFFFFF"));
        subTitlePaint.setTextSize(DensityUtils.sp2px( 14));
        subTitleOffsetY = DensityUtils.sp2px( 50);
        refreshData();
    }

    private void refreshData() {
        // 字块
        mainTitleString = Integer.toString(sportsData.step);
        String format = getResources().getString(R.string.sub_title_format);
        subTitleSeparator = getResources().getString(R.string.sub_title_separator);
        subTitleString = String.format(format, sportsData.distance / 1000, sportsData.calories);
        // 副标题文字居中
        float indexBefore = subTitlePaint.measureText(subTitleString, 0, subTitleString.indexOf(subTitleSeparator));
        float indexAfter = subTitlePaint.measureText(subTitleString, 0, subTitleString.indexOf(subTitleSeparator) + 1);
        subTitleOffsetX = -(indexBefore + indexAfter) / 2;
    }

    private void drawText(Canvas canvas){
        canvas.drawText(mainTitleString, centerX, centerY + mainTitleOffsetY, mainTitlePaint);
        canvas.drawText(subTitleString, centerX + subTitleOffsetX, centerY + subTitleOffsetY, subTitlePaint);
        canvas.drawBitmap(watchBitmap, centerX - watchBitmap.getWidth() / 2f,
                centerY - watchBitmap.getHeight() / 2f + DensityUtils.dp2px(WATCH_OFFSET_DP), null);
    }


    private void initProgressCircle(){
        solidCircleRectF = new RectF();
        dottedCirclePaint = new Paint();
        dottedCirclePaint.setStrokeWidth(DensityUtils.dp2px(DOTTED_CIRCLE_WIDTH));
        dottedCirclePaint.setColor(Color.parseColor("#CCFFFFFF"));
        dottedCirclePaint.setStyle(Paint.Style.STROKE);
        float gagPx = DensityUtils.dp2px( DOTTED_CIRCLE_GAG);
        dottedCirclePaint.setPathEffect(new DashPathEffect(new float[]{gagPx, gagPx}, 0));
        dottedCirclePaint.setAntiAlias(true);

        solidCirclePaint = new Paint();
        solidCirclePaint.setStrokeWidth(DensityUtils.dp2px( SOLID_CIRCLE_WIDTH));
        solidCirclePaint.setColor(Color.WHITE);
        solidCirclePaint.setStyle(Paint.Style.STROKE);
        solidCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        solidCirclePaint.setAntiAlias(true);

        dotPaint = new Paint();
        dotPaint.setStrokeWidth(DensityUtils.dp2px( DOT_SIZE));
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        dotPaint.setColor(Color.WHITE);
        dotPaint.setAntiAlias(true);
    }

    private void drawProgressCircle(Canvas canvas){
        if(!drawProgrees){
            return;
        }
        float dottedCircleRadius = width * 0.32f;

        solidCircleRectF.set(centerX - dottedCircleRadius, centerY - dottedCircleRadius, centerX + dottedCircleRadius, centerY + dottedCircleRadius);
        canvas.drawCircle(centerX, centerY, dottedCircleRadius, dottedCirclePaint);
        canvas.drawArc(solidCircleRectF, -90, 3.6f * sportsData.progress, false, solidCirclePaint);
        // 计算进度点位置
        canvas.drawPoint(centerX + dottedCircleRadius * (float)Math.cos((3.6f * sportsData.progress - 90)* Math.PI / 180),
                centerY + dottedCircleRadius * (float)Math.sin((3.6f * sportsData.progress - 90) * Math.PI / 180),
                dotPaint);
    }


    private void initBigCircle(){
        bigCirclePaint = new Paint();
        bigCirclePaint.setStrokeWidth(DensityUtils.dp2px(BIG_CIRCLE_SIZE));
        bigCirclePaint.setStyle(Paint.Style.STROKE);
    }

    private void drawBigCircle(Canvas canvas){
        float bigCircleRadius = width * 0.38f*bigCircleRadiusFactor;
        canvas.save();
        canvas.rotate(rotateDegree, centerX, centerY);

        // 光晕
        bigCirclePaint.setShader(blurLinearGradient);
        for (int i = 0; i < 4; i++) {
            bigCirclePaint.setAlpha(0xff * (4 - i) / (4 * 3));
            rectF.set(centerX - bigCircleRadius, centerY - bigCircleRadius-i*DensityUtils.dp2px(CIRCLE_BLUR_SIZE)/4,
                    centerX + bigCircleRadius , centerY + bigCircleRadius);
            canvas.drawArc(rectF, 0,360,false,bigCirclePaint);
        }
        bigCirclePaint.setShader(bigCircleLinearGradient);
        bigCirclePaint.setAlpha(0xff);
        canvas.drawCircle(centerX, centerY, bigCircleRadius, bigCirclePaint);
        canvas.restore();
    }

    public float getBigCircleRadiusFactor() {
        return bigCircleRadiusFactor;
    }

    public void setBigCircleRadiusFactor(float bigCircleRadiusFactor) {
        this.bigCircleRadiusFactor = bigCircleRadiusFactor;
        invalidate();
    }
    private ObjectAnimator getObjectAnimator(){
        if(objectAnimator==null){
            objectAnimator = ObjectAnimator.ofFloat(this,"bigCircleRadiusFactor",0.6f,1).setDuration(500);
            objectAnimator.setInterpolator(new OvershootInterpolator(5f));
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    drawProgrees = true;
                    invalidate();
                }
            });
        }
        drawProgrees = false;
        return objectAnimator;
    }


    public class LineCircle{
         float degrees;
         float centerX;
         float centerY;
         float radius;
    }

    private void initLineCircle(){
        lineCirclePaint.setStyle(Paint.Style.STROKE);
        lineCirclePaint.setStrokeCap(Paint.Cap.ROUND);
        lineCirclePaint.setStrokeWidth(DensityUtils.dp2px(1f));
    }


    private void drawLineCircles(Canvas canvas){
        for (int i = 0; i < LINE_CIRCLE_NUM; i++) {
            canvas.save();
            LineCircle circle = mLineCircles.get(i);
            canvas.rotate(rotateDegree-circle.degrees ,centerX,centerY);
            lineRectF.set(centerX+circle.centerX-circle.radius,centerY+circle.centerY-circle.radius,centerX+circle.centerX+circle.radius,centerY+circle.centerY+circle.radius);
            canvas.drawArc(lineRectF,0,360,false,lineCirclePaint);
            canvas.restore();
        }
    }


    private void initParticle(){
        particleSystem = new ParticleSystem(this,100,getResources().getDrawable(R.drawable.dot),800);
        particleSystem.setScaleRange(0.7f, 1.3f);
//        particleSystem.setSpeedRange(0.05f, 0.1f);
        particleSystem.setSpeedModuleAndAngleRange(0.07f, 0.16f, -120, -80);
        particleSystem.setRotationSpeedRange(90, 180);
        particleSystem.setFadeOut(200, new AccelerateInterpolator());

    }

    private void drawParticle(Canvas canvas){
        //particle
        if(mParticles==null){
            return;
        }
        canvas.save();
        canvas.rotate(rotateDegree,centerX,centerY);
        for (int i = 0; i < mParticles.size(); i++) {
            mParticles.get(i).draw(canvas);
        }
        canvas.restore();
    }

    private Timer mTimer;
    private final ParticleTimerTask mTimerTask = new ParticleTimerTask(this);
    private long mCurrentTime = 0;

    private static long TIMER_TASK_INTERVAL = 33; // Default 30fps

    private static class ParticleTimerTask extends TimerTask {

        private final WeakReference<SportsView> mSportsView;

        public ParticleTimerTask(SportsView sportsView) {
            mSportsView = new WeakReference<>(sportsView);
        }

        @Override
        public void run() {
            if(mSportsView.get() != null) {
                SportsView sportsView = mSportsView.get();
                sportsView.particleSystem.setStartTime(sportsView.mCurrentTime);
                sportsView.particleSystem.onUpdate(sportsView.mCurrentTime);
                sportsView.mCurrentTime += TIMER_TASK_INTERVAL;//todo 采用插值器
                sportsView.particleSystem.setStartTime(sportsView.mCurrentTime);
                if(sportsView.isLoading){
                    sportsView.rotateDegree = (sportsView.rotateDegree + 5)%360;
                }else {
                    sportsView.rotateDegree = (sportsView.rotateDegree + 1)%360;
                }

            }
        }
    }
}
