package com.boom.music.player.TagEditor;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.boom.music.player.Utils.Logger;
import com.boom.music.player.Utils.MusicUtils;

class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final float MAX_ZOOM = 0.8f;

    private static final float PCT = 300f;
    private float delta;

    private ValueAnimator valueAnimator;
    private boolean mFirstEvent = true;
    private ImageView imageView;
    private View mView;

    public MyGestureListener(View scrollView, ImageView imageView) {
        this.imageView = imageView;
        mView = scrollView;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }

        if (mFirstEvent) {
            mFirstEvent = false;
            return false;
        }

        delta += distanceY;
        float pct = getPct(delta);
        imageView.setScaleX(1.0f + pct);
        imageView.setScaleY(1.0f + pct);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mView.getLayoutParams();
        layoutParams.topMargin = MusicUtils.getDPFromPixel((int) (pct * 35f));
        mView.setLayoutParams(layoutParams);

        return false;
    }

    void upDetected() {

        mFirstEvent = true;
        float pct = getPct(delta);

        valueAnimator = new ValueAnimator();
        valueAnimator.setFloatValues(pct, 0.0f);
        valueAnimator.addUpdateListener(animation -> {
            imageView.setScaleX(1.0f + (Float) animation.getAnimatedValue());
            imageView.setScaleY(1.0f + (Float) animation.getAnimatedValue());
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mView.getLayoutParams();
            layoutParams.topMargin = MusicUtils.getDPFromPixel((int) ((Float) animation.getAnimatedValue() * 35f));
            mView.setLayoutParams(layoutParams);


        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                delta = 0f;
                imageView.setScaleX(1.0f);
                imageView.setScaleY(1.0f);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mView.getLayoutParams();
                layoutParams.bottomMargin = 0;
                mView.setLayoutParams(layoutParams);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        valueAnimator.start();
    }

    private float getPct(float delta) {
        float pct = -delta / PCT;
        if (pct >= MAX_ZOOM) {
            pct = MAX_ZOOM;
        } else if (pct <= 0) {
            pct = 0;
        }
        return pct;
    }
}