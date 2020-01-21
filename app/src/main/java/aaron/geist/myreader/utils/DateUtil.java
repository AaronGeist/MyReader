package aaron.geist.myreader.utils;

import org.jsoup.helper.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateUtil {

    private static final List<String> delimiters = Arrays.asList(".", "/", "-");
    private static final Map<String, Integer> units = new HashMap<>();

    static {
        units.put("天", Calendar.DATE);
        units.put("小时", Calendar.HOUR);
        units.put("分钟", Calendar.MINUTE);
        units.put("秒", Calendar.SECOND);

    }

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

            // still no matched pattern
            for (String unit : units.keySet()) {
                Matcher m = Pattern.compile(String.format("(\\d+)%s前", unit)).matcher(text);
                if (m.find()) {
                    int delta = Integer.valueOf(m.group(1));

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(units.get(unit), -delta);

                    return calendar.getTimeInMillis();
                }
            }

        }

        return System.currentTimeMillis();
    }
}
