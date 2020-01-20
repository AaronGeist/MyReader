package aaron.geist.myreader.utils;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yzhou7 on 2015/8/7.
 */
public class UrlParser {

    public static int getPostId(String url) {
        int result = -1;
        Pattern p = Pattern.compile("^.*/[^0-9]*(\\d+)[/.a-zA-Z]*");
        Matcher m = p.matcher(url);

        if (m.matches()) {
            result = Integer.valueOf(m.group(1));
            Log.d("", "parse post ID: " + result);
        }

        return result;
    }
}
