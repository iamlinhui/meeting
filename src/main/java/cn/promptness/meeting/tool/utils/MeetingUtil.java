package cn.promptness.meeting.tool.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class MeetingUtil {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    public static String dateToWeek(String datetime) {

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        Date date;
        try {
            date = f.parse(datetime);
            cal.setTime(date);
        } catch (Exception ignored) {
        }
        // 一周的第几天
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    public static Integer next() {
        return ATOMIC_INTEGER.getAndIncrement();
    }


}
