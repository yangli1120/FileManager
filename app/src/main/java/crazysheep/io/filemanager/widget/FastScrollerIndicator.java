package crazysheep.io.filemanager.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import crazysheep.io.filemanager.R;
import crazysheep.io.filemanager.utils.PinyinUtils;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;

/**
 * see{https://github.com/danoz73/RecyclerViewFastScroller/blob/master/Application/src/main/java/xyz/danoz/recyclerviewfastscroller/sample/ui/example/ColorGroupSectionTitleIndicator.java}
 *
 * Created by crazysheep on 15/12/24.
 */
public class FastScrollerIndicator extends SectionTitleIndicator<String> {

    public FastScrollerIndicator(Context context) {
        super(context);
    }

    public FastScrollerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setSection(@NonNull String object) {
        String indicatorStr;
        if(PinyinUtils.isChinese(object))
            indicatorStr = PinyinUtils.getAlpha(object);
        else
            indicatorStr = String.valueOf(object.trim().charAt(0));

        setTitleText(indicatorStr);
        setIndicatorBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        setIndicatorTextColor(Color.WHITE);
    }
}
