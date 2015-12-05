package crazysheep.io.filemanager.animator;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;

/**
 * fab arc and reveal animation helper, refer to {see#https://github.com/konifar/fab-transformation}
 *
 * Created by crazysheep on 15/11/29.
 */
public class FabAnimatorHelper {

    public interface AnimatorListener {
        void onAnimationStart();
        void onAnimationEnd();
    }

    public static class DefaultAnimatorListener implements AnimatorListener {
        @Override
        public void onAnimationStart() {}

        @Override
        public void onAnimationEnd() {}
    }

    public static class Builder {

        public static final long DEFAULT_ANIMATOR_DURATION = 150;

        public static final int TYPE_ARC_COMBO_REVEAL = 0;
        public static final int TYPE_REVEAL_COMBO_ARC = 1;

        private FloatingActionButton mFab;
        private View mRevealV;
        private ArcAnimator mArcAnimator;
        private SupportAnimator mRevealAnimator;

        private AnimatorListener mArcListener;
        private AnimatorListener mRevealListener;

        private int mComboType = TYPE_ARC_COMBO_REVEAL;

        public Builder(@NonNull FloatingActionButton fab, @NonNull View revealView) {
            mFab = fab;
            mRevealV = revealView;
        }

        public Builder arc(float endX, float endY, float degree, Side side) {
            mArcAnimator = ArcAnimator.createArcAnimator(mFab, endX, endY, degree, side);
            mArcAnimator.setDuration(DEFAULT_ANIMATOR_DURATION);
            mArcAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mArcListener != null)
                        mArcListener.onAnimationStart();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mArcListener != null)
                        mArcListener.onAnimationEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            return this;
        }

        public Builder setArcInterpolator(@NonNull Interpolator interpolator) {
            mArcAnimator.setInterpolator(interpolator);

            return this;
        }

        public Builder setArcDuration(long duration) {
            mArcAnimator.setDuration(duration);

            return this;
        }

        public Builder reveal(int centerX, int centerY, float startRadius, float endRadius) {
            mRevealAnimator = ViewAnimationUtils.createCircularReveal(mRevealV,
                    centerX, centerY, startRadius, endRadius);
            mRevealAnimator.setDuration((int) DEFAULT_ANIMATOR_DURATION);
            mRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                    if (mRevealListener != null)
                        mRevealListener.onAnimationStart();
                }

                @Override
                public void onAnimationEnd() {
                    if (mRevealListener != null)
                        mRevealListener.onAnimationEnd();
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
                }
            });

            return this;
        }

        public Builder setRevealInterpolator(@NonNull Interpolator interpolator) {
            mRevealAnimator.setInterpolator(interpolator);

            return this;
        }

        public Builder setRevealDuration(long duration) {
            mRevealAnimator.setDuration((int) duration);

            return this;
        }

        public Builder listenArc(@NonNull AnimatorListener listener) {
            mArcListener = listener;

            return this;
        }

        public Builder listenReveal(@NonNull AnimatorListener listener) {
            mRevealListener = listener;

            return this;
        }

        public Builder arcComboReveal() {
            mComboType = TYPE_ARC_COMBO_REVEAL;

            return this;
        }

        public Builder revealComboArc() {
            mComboType = TYPE_REVEAL_COMBO_ARC;

            return this;
        }

        public void animate() {
            /*
            * TYPE_ARC_COMBO_REVEAL: arc animator play -> reveal animator play
            *
            * TYPE_REVEAL_COMBO_ARC: reveal animator play -> arc animator play
            * **/
            mArcAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if(mArcListener != null)
                        mArcListener.onAnimationStart();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if(mArcListener != null)
                        mArcListener.onAnimationEnd();

                    if(mComboType == TYPE_ARC_COMBO_REVEAL)
                        mRevealAnimator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            mRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                    if(mRevealListener != null)
                        mRevealListener.onAnimationStart();
                }

                @Override
                public void onAnimationEnd() {
                    if(mRevealListener != null)
                        mRevealListener.onAnimationEnd();

                    if(mComboType == TYPE_REVEAL_COMBO_ARC)
                        mArcAnimator.start();
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
                }
            });

            if(mComboType == TYPE_ARC_COMBO_REVEAL)
                mArcAnimator.start();
            else
                mRevealAnimator.start();
        }
    }

    public static Builder wrap(@NonNull FloatingActionButton fab, @NonNull View revealView) {
        return new Builder(fab, revealView);
    }

}
