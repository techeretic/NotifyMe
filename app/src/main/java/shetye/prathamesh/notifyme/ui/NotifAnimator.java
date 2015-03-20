
package shetye.prathamesh.notifyme.ui;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import shetye.prathamesh.notifyme.R;

public class NotifAnimator {

    public static final String TOP = "TOP";

    public static final String BOTTOM = "BOTTOM";

    public static final String IN = "IN";

    public static final String OUT = "OUT";

    public static void animateFAB(Context context, FloatingActionButton fab, String directionTo,
                                  String fromWhere) {
        if (fromWhere.equals(TOP)) {
            if (directionTo.equals(IN)) {
                Animation slideInTop = AnimationUtils.loadAnimation(context,
                        R.anim.abc_slide_in_top);
                slideInTop.setDuration(750);
                fab.setAnimation(slideInTop);
                fab.animate();
                fab.setVisibility(View.VISIBLE);
            } else {
                Animation slideOutTop = AnimationUtils.loadAnimation(context,
                        R.anim.abc_slide_out_top);
                slideOutTop.setDuration(750);
                fab.setAnimation(slideOutTop);
                fab.animate();
                fab.setVisibility(View.GONE);
            }
        } else {
            if (directionTo.equals(IN)) {
                Animation slideInBottom = AnimationUtils.loadAnimation(context,
                        R.anim.abc_slide_in_bottom);
                slideInBottom.setDuration(750);
                fab.setAnimation(slideInBottom);
                fab.animate();
                fab.setVisibility(View.VISIBLE);
            } else {
                Animation slideOutBottom = AnimationUtils.loadAnimation(context,
                        R.anim.abc_slide_out_bottom);
                slideOutBottom.setDuration(750);
                fab.setAnimation(slideOutBottom);
                fab.animate();
                fab.setVisibility(View.GONE);
            }
        }
    }

}
