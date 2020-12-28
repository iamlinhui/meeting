package cn.promptness.meeting.tool.utils;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OpenUtil {

    public static Header getHeader() {
        String sql = "SELECT * FROM cookies WHERE host_key = '.oa.fenqile.com'";
        Set<ChromeCookie> decryptedCookies = ChromeDecryptHelper.getWindowsDecryptedCookies(sql);

        StringBuilder stringBuilder = new StringBuilder();
        for (ChromeCookie chromeCookie : decryptedCookies) {
            stringBuilder.append(chromeCookie.getName()).append("=").append(chromeCookie.getValue()).append(";");
        }
        return new BasicHeader("Cookie", stringBuilder.toString());
    }

    public static boolean open(int code) {
        // 未登录或登录超时，请重新登录
        if (90001002 == code || 19002028 == code) {
            try {
                String path = "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe";
                String target = "http://oa.fenqile.com/";
                List<String> cmd = new ArrayList<>();
                cmd.add(path);
                cmd.add("--start-maximized");
                cmd.add(target);
                ProcessBuilder process = new ProcessBuilder(cmd);
                process.start();
            } catch (Exception e) {
                try {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://oa.fenqile.com/");
                } catch (IOException ignored) {
                }
            }
            return true;
        }
        return false;
    }

    public static String dateToWeek(String datetime) {

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        String[] weekDays = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
        Calendar cal = Calendar.getInstance();
        Date date;
        try {
            date = f.parse(datetime);
            cal.setTime(date);
        } catch (Exception ignored) {}
        // 一周的第几天
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }
}
