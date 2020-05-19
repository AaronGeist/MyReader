package aaron.geist.myreader.utils;

import com.google.common.base.Charsets;

import org.jsoup.helper.StringUtil;

import java.nio.charset.Charset;
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
        StringBuilder result = new StringBuilder();
        if (!StringUtil.isBlank(url)) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(url.getBytes(Charsets.UTF_8));
                byte[] s = md.digest();
                for (byte b : s) {
                    result.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6));
                }
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }
}
