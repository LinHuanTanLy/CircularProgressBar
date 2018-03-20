package com.ly.circularprogressbar.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


import com.ly.circularprogressbar.R;

import java.math.BigDecimal;


/**
 * Created by Ly on 2018/3/19.
 */

public class CircularProgressBar extends View {
    //    定义 宽 高 半径 开始角度 滑动角度
    private int mW, mH, mRadius, mStartAngle = 0, mSweepAngle = 0;
    //    默认弧形的颜色，进度条颜色，暂停杠杠的颜色，字体颜色
    private int mDefColor, mProgressColor, mTabColor, mFontColor;
    //   画进度弧度，画中间暂停斜杠，画默认弧度，画文字
    private Paint mPaintForCircle, mPaintForTag, mPaintForDefCircle, mPaintForText;
    //   是否画进度条
    private boolean isDrawProgressBar = false;
    //   由圆形进度条 进化到  圆角矩形 需要增长的半径的倍数以及最大倍数
    private final float sMultiple = 1f, sMaxMultiple = 2.5f;
    //   可变的倍数
    private float mMultiple = sMaxMultiple;
    //   进度条的宽度,圆角矩形的宽度
    private float mStrokeWidthCircle = 12, mRectWidth = 6;
    //   圆角矩形中的提示文字
    private String mTipsStr = "下载", mTipsInstallStr = "安装";
    //   圆角矩形中的提示文字字号大小
    private float mTipsStrFontSize = 40;
    //   下载监听
    private OnProgressListener mOnProgressListener;

    public CircularProgressBar(Context context) {
        super(context);
    }

    public CircularProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.circular);
        mDefColor = typedArray.getColor(R.styleable.circular_defColor, Color.parseColor("#9E9E9E"));
        mProgressColor = typedArray.getColor(R.styleable.circular_progressColor, Color.parseColor("#FF34A350"));
        mTabColor = typedArray.getColor(R.styleable.circular_tabColor, Color.parseColor("#FF34A350"));
        mFontColor = typedArray.getColor(R.styleable.circular_fontColor, Color.parseColor("#FF34A350"));
        mTipsStr = typedArray.getString(R.styleable.circular_tipsStr);
        mStrokeWidthCircle = typedArray.getDimension(R.styleable.circular_progressWidth, 12);
        mRectWidth = typedArray.getDimension(R.styleable.circular_rectWidth, 6);
        if (TextUtils.isEmpty(mTipsStr)) {
            mTipsStr = "下载";
        }
        mTipsInstallStr = typedArray.getString(R.styleable.circular_finishStr);
        if (TextUtils.isEmpty(mTipsInstallStr)) {
            mTipsInstallStr = "安装";
        }
        mTipsStrFontSize = typedArray.getDimension(R.styleable.circular_fontSize, 40);
        typedArray.recycle();

        mPaintForCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintForCircle.setStyle(Paint.Style.STROKE);
        mPaintForTag = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintForDefCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintForDefCircle.setStyle(Paint.Style.STROKE);
        mPaintForText = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintForText.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mW = w;
        mH = h;
        mRadius = (int) (Math.min(w, h) / 2 * 0.7);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mW / 2, mH / 2);
        if (isDrawProgressBar) {
            drawPauseTag(canvas);
            drawDefCircle(canvas);
            drawBigCircle(canvas);
        } else {
            drawRoundRect(canvas);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取宽-测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // 获取高-测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理
        int mWidth = 200;
        int mHeight = 100;
        // 当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, mHeight);
            // 宽 / 高任意一个布局参数为= wrap_content时，都设置默认值
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mHeight);
        }
    }

    /**
     * 画矩形框中的文本
     *
     * @param rectF
     * @param canvas
     */
    private void drawTipsText(RectF rectF, Canvas canvas) {
        mPaintForText.setTextSize(mTipsStrFontSize);
        mPaintForText.setColor(mFontColor);
        Paint.FontMetricsInt fontMetrics = mPaintForText.getFontMetricsInt();
        float baseline2 = rectF.bottom - ((rectF.bottom - rectF.top - fontMetrics.bottom + fontMetrics.top) / 2 + fontMetrics.bottom);
        canvas.drawText(mTipsStr, rectF.centerX(), baseline2, mPaintForText);
    }

    /**
     * 画圆角矩形
     *
     * @param canvas
     */
    private void drawRoundRect(Canvas canvas) {
        mPaintForCircle.setStrokeWidth(mRectWidth);
        mPaintForCircle.setColor(mProgressColor);
        RectF rectF = new RectF(-mRadius * mMultiple, -mRadius, mRadius * mMultiple, mRadius);
        canvas.drawRoundRect(rectF, 60, 60, mPaintForCircle);
        mPaintForCircle.setStrokeWidth(mStrokeWidthCircle);
        if (mMultiple == sMaxMultiple) {
            drawTipsText(rectF, canvas);
        }
    }

    /**
     * 画基础的背景圆
     *
     * @param canvas
     */
    private void drawDefCircle(Canvas canvas) {
        mPaintForDefCircle.setStrokeWidth(mStrokeWidthCircle);
        mPaintForDefCircle.setColor(mDefColor);
        RectF rectF = new RectF(-mRadius, -mRadius, mRadius, mRadius);
        canvas.drawArc(rectF, mStartAngle, 360, false, mPaintForDefCircle);
    }

    /**
     * 画中间的暂停按钮
     *
     * @param canvas
     */
    private void drawPauseTag(Canvas canvas) {
        mPaintForTag.setColor(mTabColor);
        RectF rectFLeft = new RectF(-mRadius / 2.6f, -mRadius / 2.4f, -mRadius / 8, mRadius / 2.4f);
        RectF rectFRight = new RectF(mRadius / 8, -mRadius / 2.4f, mRadius / 2.6f, mRadius / 2.4f);
        canvas.drawRect(rectFLeft, mPaintForTag);
        canvas.drawRect(rectFRight, mPaintForTag);
    }

    /**
     * 画进度弧形
     *
     * @param canvas
     */
    private void drawBigCircle(Canvas canvas) {
        mPaintForCircle.setColor(mProgressColor);
        mPaintForCircle.setStrokeWidth(mStrokeWidthCircle);
        RectF rectF = new RectF(-mRadius, -mRadius, mRadius, mRadius);
        canvas.drawArc(rectF, mStartAngle, mSweepAngle, false, mPaintForCircle);
    }

    /**
     * 用属性动画绘制组件  开始画圆角矩形
     */
    private ValueAnimator mValueAnimatorRect;
    private ValueAnimator mValueAnimatorProgress;

    private void doDrawRect() {
        if (mValueAnimatorProgress!=null){
            mValueAnimatorProgress.cancel();
        }
        if (mValueAnimatorRect != null) {
            mValueAnimatorRect.start();
        } else {
            mValueAnimatorRect = ValueAnimator.ofFloat(sMultiple, sMaxMultiple);
            mValueAnimatorRect.setDuration(500);
            mValueAnimatorRect.addUpdateListener(animation -> {
                mMultiple = (float) animation.getAnimatedValue();
                invalidate();
            });
            mValueAnimatorRect.start();
        }
    }

    /**
     * 用属性动画绘制组件  开始画进度条
     */
    private void doDrawProgress() {
        if (mValueAnimatorRect!=null){
            mValueAnimatorRect.cancel();
        }
        if (mValueAnimatorProgress != null) {
            mValueAnimatorProgress.start();
        } else {
            mValueAnimatorProgress = ValueAnimator.ofFloat(sMaxMultiple, sMultiple);
            mValueAnimatorProgress.setDuration(500);
            mValueAnimatorProgress.addUpdateListener(animation -> {
                float rate = (float) animation.getAnimatedValue();
                mMultiple = rate;
                invalidate();
                if (rate == sMultiple) {
                    isDrawProgressBar = true;
                    invalidate();
                }
            });
            mValueAnimatorProgress.start();
        }
    }


    /**
     * 是不是正在显示进度中
     *
     * @return
     */
    public boolean isInProgress() {
        return isDrawProgressBar;
    }

    /**
     * 开始显示下载进度
     */
    public void doStartProgress() {
        doDrawProgress();
    }

    /**
     * 暂停下载进度
     */
    public void doPauseProgress() {
        isDrawProgressBar = false;
        doDrawRect();
    }

    /**
     * 完成下载进度
     */
    public void doFinishProgress() {
        mSweepAngle = 360;
        isDrawProgressBar = false;
        mTipsStr = mTipsInstallStr;
        doDrawRect();
        if (mOnProgressListener != null) {
            mOnProgressListener.onProgressFinish();
            mSweepAngle = 0;
        }
    }


    /**
     * 更新进度
     *
     * @param curProgress 当前进度
     * @param maxProgress 全部进度
     */
    public void setProgress(int curProgress, int maxProgress) {
        if (curProgress < maxProgress) {
            BigDecimal result = new BigDecimal(curProgress).divide(new BigDecimal(maxProgress), 2, BigDecimal.ROUND_HALF_DOWN).multiply(new BigDecimal(360));
            mSweepAngle = result.intValue();
        } else {
            mSweepAngle = 360;
        }
        invalidate();
    }


    /**
     * 默认弧形的颜色
     *
     * @param colorRgb
     */
    public void setDefColor(int colorRgb) {
        mDefColor = colorRgb;
    }

    /**
     * 进度条颜色
     *
     * @param colorRgb
     */
    public void setProgressColor(int colorRgb) {
        mProgressColor = colorRgb;
    }

    /**
     * 暂停杠杠的颜色
     *
     * @param colorRgb
     */
    public void setTabColor(int colorRgb) {
        mTabColor = colorRgb;
    }

    /**
     * 设置进度条宽度
     *
     * @param progressWidth
     */
    public void setProgressWidth(int progressWidth) {
        mStrokeWidthCircle = (float) progressWidth;
    }

    /**
     * 设置圆角矩形的宽度
     *
     * @param rectWidth
     */
    public void setRectWidth(int rectWidth) {
        mRectWidth = rectWidth;
    }

    /**
     * 设置矩形方框里面的信息文字
     *
     * @param msg
     */
    public void setTipsMessage(String msg) {
        mTipsStr = msg;
    }


    /**
     * 设置进度走完后显示的信息
     *
     * @param msg
     */
    public void setTipsFinish(String msg) {
        mTipsInstallStr = msg;
    }

    /**
     * 设置字体大小
     *
     * @param fontSize
     */
    public void setTipsMessageSize(int fontSize) {
        mTipsStrFontSize = fontSize;
    }

    /**
     * 设置字体颜色
     *
     * @param colorRgb
     */
    public void setFontColor(int colorRgb) {
        mFontColor = colorRgb;
    }


    /**
     * 監聽回調
     *
     * @param onProgressListener
     */
    public void setOnProgressListener(OnProgressListener onProgressListener) {
        mOnProgressListener = onProgressListener;
    }

    public interface OnProgressListener {
        void onProgressFinish();
    }
}
