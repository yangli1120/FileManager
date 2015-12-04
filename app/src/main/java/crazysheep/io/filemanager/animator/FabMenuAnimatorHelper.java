package crazysheep.io.filemanager.animator;

import android.animation.Animator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.SparseArray;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.concurrent.TimeUnit;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * helper for fab menu animator
 *
 * Created by crazysheep on 15/12/4.
 */
public class FabMenuAnimatorHelper {

    public static Builder wrap(View revealRoot, View menuParent, FloatingActionButton mainFab) {
        return new Builder(revealRoot, menuParent, mainFab);
    }

    public interface OnFabItemClickListener {
        void onFabItemClick(FloatingActionButton fab, int id);
    }

    public static class Builder implements View.OnClickListener {

        private View mRevealRoot;
        private View mMenuParent;
        private FloatingActionButton mMainFab;
        private SparseArray<FloatingActionButton> mFabs;
        private OnFabItemClickListener mFabMenuItemClickListener;

        private static final int MENU_CLOSED = 0;
        private static final int MENU_ANIMATING = 1;
        private static final int MENU_EXPANDED = 2;
        private int mMenuState = MENU_CLOSED;

        public Builder(@NonNull View root, @NonNull View menuParent,
                       @NonNull FloatingActionButton mainFab) {
            mRevealRoot = root;
            mMenuParent = menuParent;
            mMainFab = mainFab;

            mFabs = new SparseArray<>();

            mMenuParent.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    closed();
                }
            });
        }

        public Builder setOnFabMenuItemClickListener(OnFabItemClickListener listener) {
            mFabMenuItemClickListener = listener;

            return this;
        }

        @Override
        public void onClick(final View v) {
            // rx java! yes, you got it!
            Observable.just(v)
                    .doOnSubscribe(new Action0() {
                        @Override
                        public void call() {
                            closed();
                        }
                    })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .delay(300, TimeUnit.MILLISECONDS) // closed animation use 300ms
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<View>() {
                        @Override
                        public void call(View view) {
                            if (mFabMenuItemClickListener != null) {
                                FloatingActionButton fab = (FloatingActionButton) v;
                                mFabMenuItemClickListener.onFabItemClick(fab,
                                        mFabs.keyAt(mFabs.indexOfValue(fab)));
                            }
                        }
                    });
        }

        public Builder addFab(@NonNull FloatingActionButton fab, int id) {
            mFabs.put(id, fab);
            fab.setOnClickListener(this);

            return this;
        }

        public boolean isAnimating() {
            return mMenuState == MENU_ANIMATING;
        }

        public boolean isExpanded() {
            return mMenuState == MENU_EXPANDED;
        }

        // expanded fab menu
        public void expanded() {
            mMenuState = MENU_ANIMATING;

            // scale out main fab
            mMainFab.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(150)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mMainFab.setVisibility(View.GONE);

                            revealMenu();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();
        }

        private void revealMenu() {
            // reveal animation for menu parent
            SupportAnimator menuRevealAnimator = ViewAnimationUtils.createCircularReveal(
                    mMenuParent,
                    mMenuParent.getWidth(), mMenuParent.getHeight(),
                    0,
                    (int) Math.hypot(mMenuParent.getWidth(), mMenuParent.getHeight()));
            menuRevealAnimator.setInterpolator(new AccelerateInterpolator());
            menuRevealAnimator.setDuration(150);
            menuRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                    mRevealRoot.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd() {
                    mMainFab.setVisibility(View.GONE);

                    mMenuState = MENU_EXPANDED;
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
                }
            });
            menuRevealAnimator.start();

            // scale animation for fab
            for(int i = 0; i < mFabs.size(); i++) {
                FloatingActionButton fab = mFabs.get(mFabs.keyAt(i));

                fab.setScaleX(0f);
                fab.setScaleY(0f);
                fab.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setInterpolator(new OvershootInterpolator())
                        .setDuration(150)
                        .start();
            }
        }

        // closed fab menu
        public void closed() {
            mMenuState = MENU_ANIMATING;

            // reveal animator for menu
            SupportAnimator menuRevealAnimator = ViewAnimationUtils.createCircularReveal(
                    mMenuParent,
                    mMenuParent.getWidth(), mMenuParent.getHeight(),
                    (int) Math.hypot(mMenuParent.getWidth(), mMenuParent.getHeight()),
                    0);
            menuRevealAnimator.setInterpolator(new AccelerateInterpolator());
            menuRevealAnimator.setDuration(150);
            menuRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
                @Override
                public void onAnimationStart() {
                }

                @Override
                public void onAnimationEnd() {
                    mRevealRoot.setVisibility(View.GONE);
                    mMainFab.setVisibility(View.VISIBLE);

                    mMainFab.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setInterpolator(new OvershootInterpolator())
                            .setDuration(150)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    mMenuState = MENU_CLOSED;
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {
                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {
                                }
                            })
                            .start();
                }

                @Override
                public void onAnimationCancel() {
                }

                @Override
                public void onAnimationRepeat() {
                }
            });
            menuRevealAnimator.start();

            // scale out for fabs
            for(int i = 0; i < mFabs.size(); i++) {
                FloatingActionButton fab = mFabs.get(mFabs.keyAt(i));

                fab.setScaleX(1f);
                fab.setScaleY(1f);
                fab.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(150)
                        .start();
            }
        }

    }

}
