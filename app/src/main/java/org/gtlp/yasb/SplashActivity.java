package org.gtlp.yasb;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.DecelerateInterpolator;

public class SplashActivity extends AppCompatActivity {

    public static final int SPLASH_IMG_SCALE_FACTOR = 50;
    public static final float SPLASH_IMG_ALPHA_GOAL = 0.06125f;
    public static final int SPLASH_IMG_ANIM_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            findViewById(R.id.splashImg).setScaleX(SPLASH_IMG_SCALE_FACTOR);
            findViewById(R.id.splashImg).setScaleY(SPLASH_IMG_SCALE_FACTOR);
            findViewById(R.id.splashImg).setAlpha(SPLASH_IMG_ALPHA_GOAL);
            findViewById(R.id.splashImg).animate().scaleX(1).scaleY(1).alpha(1).setDuration(SPLASH_IMG_ANIM_DURATION).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finish();
                    startActivityIfNeeded(new Intent(SplashActivity.this, SoundActivity.class), 0);
                }
            }).start();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class AnimatorAdapter implements Animator.AnimatorListener {

        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {
        }

        @Override
        public void onAnimationCancel(Animator animation) {
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }
}
