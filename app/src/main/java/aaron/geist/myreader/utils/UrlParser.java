package aaron.geist.myreader.utils;

import org.jsoup.helper.StringUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        }

        return result;
    }

    public static String getMd5Digest(String url) {
        String result = "";
        if (!StringUtil.isBlank(url)) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(url.getBytes());
                result = new String(md.digest());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
