package cn.promptness.meeting.tool.utils;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MeetingUtil {

    public static void main(String[] args) {
        File account = new File("account.dat");
        if (account.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(account))) {
                Object object;
                while ((object = ois.readObject()) != null) {
                    Cookie cookie = (Cookie) object;
                    HEADER_MAP.put(cookie.getName(), cookie.getValue());
                }
            } catch (Exception ignored) {

            }
        }
    }

    private static final Map<String, String> HEADER_MAP = new ConcurrentHashMap<>();

    public static void readCache() {
        File account = new File("account.dat");
        if (account.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(account))) {
                Object object;
                while ((object = ois.readObject()) != null) {
                    Cookie cookie = (Cookie) object;
                    HEADER_MAP.put(cookie.getName(), cookie.getValue());
                }
            } catch (Exception ignored) {

            }
            account.delete();
        }
    }

    public static void cache() {
        File account = new File("account.dat");
        if (account.exists()) {
            account.delete();
        }
        if (CollectionUtils.isEmpty(HEADER_MAP)) {
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(account))) {
            for (Map.Entry<String, String> entry : HEADER_MAP.entrySet()) {
                Cookie cookie = new BasicClientCookie2(entry.getKey(), entry.getValue());
                oos.writeObject(cookie);
            }
            oos.writeObject(null);
            oos.flush();
        } catch (Exception ignored) {

        }
    }

    /**
     * Set-Cookie: oa_session=9ldk347t38apb9ksgfs5nalk95; expires=Sun, 10-Jan-2021 08:59:36 GMT; path=/; domain=.oa.fenqile.com; httponly
     * Set-Cookie: oa_token_id=z0HpnPL%2FgXMwDXuKCd8G8KEG9MAGGCoaKZnctblq7s3TeTQXF3DwbuQQL2CcpQrlwn9ubrj0537SgQ31o7ndCQ%3D%3D; path=/; domain=.oa.fenqile.com
     * Set-Cookie: mid=31412; path=/; domain=.oa.fenqile.com
     *
     * @author lynn
     * @date 2021/1/9 21:00
     * @since v1.0.0
     */
    public static void flashHeader(List<Header> headers) {
        HEADER_MAP.clear();
        addHeader(headers);
    }

    public static void addHeader(List<Header> headers) {
        for (Header header : headers) {
            String value = header.getValue();
            String cookieString = value.split(";")[0];
            HEADER_MAP.put(cookieString.split("=")[0], cookieString.split("=")[1]);
        }
    }

    public static boolean haveAccount() {
        return !CollectionUtils.isEmpty(HEADER_MAP);
    }

    public static void logout() {
        HEADER_MAP.clear();
    }

    public static String getUid() {
        return HEADER_MAP.getOrDefault("mid", "");
    }

    public static List<Cookie> getHeaderList() {
        List<Cookie> cookieList = new ArrayList<>();
        for (Map.Entry<String, String> entry : HEADER_MAP.entrySet()) {
            Cookie cookie = new BasicClientCookie2(entry.getKey(), entry.getValue());
            cookieList.add(cookie);
        }
        return cookieList;
    }

    public static boolean checkCode(int code) {
        // 未登录或登录超时，请重新登录
        return 90001002 == code || 19002028 == code;
    }

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

}
