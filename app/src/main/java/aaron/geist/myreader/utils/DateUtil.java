package aaron.geist.myreader.utils;

import org.jsoup.helper.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {

    private static final List<String> delimiters = Arrays.asList(".", "/", "-");

    public static long find(String text) {
        if (!StringUtil.isBlank(text)) {
            for (String delimiter : delimiters) {
                Matcher m = Pattern.compile(String.format("(\\d{4}%s\\d{2}%s\\d{2})", delimiter, delimiter)).matcher(text);
                if (m.find()) {
                    String ts = m.group(1);
                    try {
                        return new SimpleDateFormat(String.format("yyyy%sMM%sdd", delimiter, delimiter), Locale.CHINA).parse(ts).getTime();
                    } catch (Exception e) {
                    }
                }
            }
        }

        return System.currentTimeMillis();
    }
}
