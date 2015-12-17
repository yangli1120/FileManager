package crazysheep.io.filemanager.utils;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

/**
 * string utils
 *
 * Created by crazysheep on 15/12/16.
 */
public class StringUtils {

    /**
     * highlight a string contains by target string
     *
     * @return if dad contains child
     * */
    public static SpannableString highlight(String child,
                                            @NonNull String dad,
                                            @ColorInt int color) {
        SpannableString ssb = new SpannableString(dad);
        if(dad.contains(child)) {
            int startIndex = dad.toLowerCase().indexOf(child.toString());
            ssb.setSpan(new ForegroundColorSpan(color), startIndex, startIndex + child.length(),
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return ssb;
    }

}
