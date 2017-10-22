package com.boom.music.player.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.boom.music.player.R;

import java.util.Random;

/**
 * A music visualizer sort of animation (with mRandom data)
 */
public class MusicVisualizer extends View {
    Random mRandom;
    Paint mPaint;
    int height;
    int width;
    private Runnable animateView = new Runnable() {
        @Override
        public void run() {
            postDelayed(this, 150);
            invalidate();
        }
    };

    public MusicVisualizer(Context context) {
        super(context);
        new MusicVisualizer(context, null);
    }

    public MusicVisualizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mRandom = new Random();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(getResources().getColor(R.color.white));
        removeCallbacks(animateView);
        post(animateView);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawRect(getDimensionInPixel(((width / 5) * 1) + 0),
                getDimensionInPixel(mRandom.nextInt((int) (getHeight() / 1.5f))),
                getDimensionInPixel(((width / 5) * 2) + 0),
                getDimensionInPixel(height),
                mPaint);

        canvas.drawRect(getDimensionInPixel(((width / 5) * 2) + 5),
                getDimensionInPixel(mRandom.nextInt((int) (getHeight() / 1.5f))),
                getDimensionInPixel(((width / 5) * 3) + 5)
                , getDimensionInPixel(height),
                mPaint);

        canvas.drawRect(getDimensionInPixel(((width / 5) * 3) + 10),
                getDimensionInPixel(mRandom.nextInt((int) (getHeight() / 1.5f))),
                getDimensionInPixel(((width / 5) * 4) + 10),
                getDimensionInPixel(height),
                mPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = getDimensionInPixel(h);
        width = getDimensionInPixel(w);
    }


    public void setColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    private int getDimensionInPixel(int dp) {
        return (int) TypedValue.applyDimension(0, dp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == VISIBLE) {
            removeCallbacks(animateView);
            post(animateView);
        } else if (visibility == GONE) {
            removeCallbacks(animateView);
        }
    }
}