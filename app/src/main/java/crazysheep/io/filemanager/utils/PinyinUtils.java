package crazysheep.io.filemanager.utils;

import android.content.Context;

import com.pinyin4android.PinyinUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * see{http://913.iteye.com/blog/2047826}
 * */
public class PinyinUtils {
    /**
     * 使用pinyin4android.jar将汉字转换为拼音
     * https://code.google.com/p/pinyin4android/downloads/detail?name=pinyin4android1.0.jar&can=2&q=
     *
     * @param chineseStr
     * @return
     */
    public static String chineneToSpell(Context context, String chineseStr) {
        if (isChinese(chineseStr)) {
            String pinying = PinyinUtil.toPinyin(context, chineseStr);
            return pinying.toUpperCase();
        }
        return chineseStr;
    }

    /**
     * 判断字符串中是否包含有中文
     *
     * @param str
     * @return
     */
    public static boolean isChinese(String str) {
        String regex = "[\\u4e00-\\u9fa5]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    /**
     * 判断是否是a-z
     *
     * @param str
     * @return
     */
    public static boolean isA2Z(String str) {
        String regex = "[A-Za-z]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    //获得汉语拼音首字母
    public static String getAlpha(String spellStr) {
        if (spellStr == null) {
            return "#";
        }

        if (spellStr.trim().length() == 0) {
            return "#";
        }

        char c = spellStr.trim().substring(0, 1).charAt(0);
        // 正则表达式，判断首字母是否是英文字母
        Pattern pattern = Pattern.compile("^[A-Za-z]{1}");
        if (pattern.matcher(c + "").matches()) {
            return (c + "");
        } else {
            return "#";
        }
    }

}
