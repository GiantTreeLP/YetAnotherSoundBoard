package org.gtlp.yasb;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ProgressBar;

public class SmoothProgressBar extends ProgressBar {

    private static final Interpolator DEFAULT_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private ValueAnimator animator;
    private ValueAnimator animatorSecondary;
    private boolean animate = true;

    public SmoothProgressBar(Context context) {
        super(context);
    }

    public SmoothProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SmoothProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public synchronized void setProgress(int progress) {
        if (!animate) {
            super.setProgress(progress);
            return;
        }
        if (animator != null) animator.cancel();
        if (animator == null) {
            animator = ValueAnimator.ofInt(getProgress(), progress);
            animator.setInterpolator(DEFAULT_INTERPOLATOR);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    SmoothProgressBar.super.setProgress((Integer) animation.getAnimatedValue());
                }
            });
        } else
            animator.setIntValues(getProgress(), progress);
        animator.start();

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        if (!animate) {
            super.setSecondaryProgress(secondaryProgress);
            return;
        }
        if (animatorSecondary != null) animatorSecondary.cancel();
        if (animatorSecondary == null) {
            animatorSecondary = ValueAnimator.ofInt(getProgress(), secondaryProgress);
            animatorSecondary.setInterpolator(DEFAULT_INTERPOLATOR);
            animatorSecondary.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    SmoothProgressBar.super.setSecondaryProgress((Integer) animation.getAnimatedValue());
                }
            });
        } else
            animatorSecondary.setIntValues(getProgress(), secondaryProgress);
        animatorSecondary.start();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
        if (animatorSecondary != null) animatorSecondary.cancel();
    }
}