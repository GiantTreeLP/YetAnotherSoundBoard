package org.gtlp.yasb;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {

    public static final float SPLASH_IMG_SCALE_FACTOR = 16.0f;
    public static final float SPLASH_IMG_ALPHA = 0.0f;
    public static final long SPLASH_IMG_ANIM_DURATION = 750;
    public static final String NAME = "SplashActivity";
    public static final float TENSION = 0.3f;
    public static final AnticipateOvershootInterpolator INTERPOLATOR = new AnticipateOvershootInterpolator(TENSION);
    private ImageView splashImg;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        splashImg = (ImageView) findViewById(R.id.splashImg);
    }

    @Override
    protected final void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            splashImg.setScaleX(SPLASH_IMG_SCALE_FACTOR);
            splashImg.setScaleY(SPLASH_IMG_SCALE_FACTOR);
            splashImg.setAlpha(SPLASH_IMG_ALPHA);
            splashImg.animate().scaleX(1).scaleY(1).alpha(1).setDuration(SPLASH_IMG_ANIM_DURATION).setInterpolator(INTERPOLATOR).setListener(new AnimatorAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startActivity(new Intent(SplashActivity.this, SoundActivity.class).putExtra(SoundActivity.CALLER_KEY, NAME));
                    finish();
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
