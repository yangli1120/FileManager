package crazysheep.io.filemanager.animator;

import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;

import com.nineoldandroids.animation.Animator;

import crazysheep.io.filemanager.utils.ViewUtils;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import io.codetail.animation.arcanimator.ArcAnimator;
import io.codetail.animation.arcanimator.Side;
import io.codetail.widget.RevealFrameLayout;

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

        private FloatingActionButton mFab;
        private View mRevealV;
        private RevealFrameLayout mRevealParentV;
        private ArcAnimator mArcAnimator;
        private SupportAnimator mRevealAnimator;

        private AnimatorListener mExpandedListener;
        private AnimatorListener mClosedListener;

        private static final int STATE_CLOSED = 0;
        private static final int STATE_ANIMATING = 1;
        private static final int STATE_EXPANDED = 2;
        private int mCurState = STATE_CLOSED;

        public Builder(@NonNull FloatingActionButton fab, @NonNull View revealView,
                       @NonNull RevealFrameLayout revealParent) {
            mFab = fab;
            mRevealV = revealView;
            mRevealParentV = revealParent;
        }

        public Builder setExpandedListener(AnimatorListener listener) {
            mExpandedListener = listener;

            return this;
        }

        public Builder setClosedListener(AnimatorListener listener) {
            mClosedListener = listener;

            return this;
        }

        public void hideFab() {
            mRevealParentV.setVisibility(View.GONE);
            mFab.setVisibility(View.GONE);
        }

        public void resetAndShowFab() {
            mRevealParentV.setVisibility(View.GONE);
            mFab.setVisibility(View.VISIBLE);
            mFab.setTranslationX(0);
            mFab.setTranslationY(0);
            mFab.setScaleX(0f);
            mFab.setScaleY(0f);
            mFab.animate()
                    .setInterpolator(new BounceInterpolator())
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(DEFAULT_ANIMATOR_DURATION)
                    .start();
        }

        // expanded
        public void expanded() {
            mCurState = STATE_ANIMATING;

            mArcAnimator = ArcAnimator.createArcAnimator(mFab,
                    ViewUtils.getRelativeLeft(mRevealV) + mRevealV.getWidth() / 2,
                    ViewUtils.getRelativeTop(mRevealV) + mRevealV.getHeight() / 2,
                    270f, Side.RIGHT);
            mArcAnimator.setDuration(DEFAULT_ANIMATOR_DURATION);
            mArcAnimator.setInterpolator(new AccelerateInterpolator());
            mArcAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if(mExpandedListener != null)
                        mExpandedListener.onAnimationStart();
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mFab.setVisibility(View.GONE);
                    mRevealParentV.setVisibility(View.VISIBLE);

                    mRevealAnimator = ViewAnimationUtils.createCircularReveal(mRevealV,
                            mRevealV.getLeft() + mRevealV.getWidth() / 2,
                            mRevealV.getTop() + mRevealV.getHeight() / 2,
                            (float) Math.hypot(mFab.getWidth(), mFab.getHeight()),
                            (float) Math.hypot(mRevealV.getWidth(), mRevealV.getHeight()));
                    mRevealAnimator.setDuration((int) DEFAULT_ANIMATOR_DURATION);
                    mRevealAnimator.setInterpolator(new AccelerateInterpolator());
                    mRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                        @Override
                        public void onAnimationStart() {
                        }

                        @Override
                        public void onAnimationEnd() {
                            mCurState = STATE_EXPANDED;

                            if(mExpandedListener != null)
                                mExpandedListener.onAnimationEnd();
                        }

                        @Override
                        public void onAnimationCancel() {
                        }

                        @Override
                        public void onAnimationRepeat() {
                        }
                    });
                    mRevealAnimator.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            mArcAnimator.start();
        }

        public void closed() {
            mCurState = STATE_ANIMATING;

            mRevealAnimator = ViewAnimationUtils.createCircularReveal(mRevealV,
                    mRevealV.getLeft() + mRevealV.getWidth() / 2,
                    mRevealV.getTop() + mRevealV.getHeight() / 2,
                    (float) Math.hypot(mRevealV.getWidth(), mRevealV.getHeight()),
                    (float) Math.hypot(mFab.getWidth(), mFab.getHeight()));
            mRevealAnimator.setDuration((int) DEFAULT_ANIMATOR_DURATION);
            mRevealAnimator.setInterpolator(new AccelerateInterpolator());
            mRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                    if(mClosedListener != null)
                        mClosedListener.onAnimationStart();
                }

                @Override
                public void onAnimationEnd() {
                    mRevealParentV.setVisibility(View.GONE);
                    mFab.setVisibility(View.VISIBLE);

                    mArcAnimator = ArcAnimator.createArcAnimator(mFab,
                            mFab.getX() - mFab.getTranslationX() + mFab.getWidth() / 2,
                            mFab.getY() - mFab.getTranslationY() + mFab.getHeight() / 2,
                            270, Side.RIGHT);
                    mArcAnimator.setDuration(DEFAULT_ANIMATOR_DURATION);
                    mArcAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    mArcAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCurState = STATE_CLOSED;

                            if(mClosedListener != null)
                                mClosedListener.onAnimationEnd();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    mArcAnimator.start();
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
                }
            });
            mRevealAnimator.start();
        }

        public boolean isExpanded() {
            return mCurState == STATE_EXPANDED;
        }

        public boolean isAnimating() {
            return mCurState == STATE_ANIMATING;
        }
    }

    public static Builder wrap(@NonNull FloatingActionButton fab, @NonNull View revealView,
                               @NonNull RevealFrameLayout revealParent) {
        return new Builder(fab, revealView, revealParent);
    }

}
