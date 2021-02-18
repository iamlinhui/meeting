package cn.promptness.meeting.tool.utils;

import cn.promptness.meeting.tool.data.Cookie;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MeetingUtil {

    private static final List<Cookie> HEADER_LIST = new CopyOnWriteArrayList<>();

    public static void readCache() {
        File account = new File("account.dat");
        if (account.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(account))) {
                Object object;
                while ((object = ois.readObject()) != null) {
                    HEADER_LIST.add((Cookie) object);
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
        if (CollectionUtils.isEmpty(HEADER_LIST)) {
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(account))) {
            for (int i = 0; i < HEADER_LIST.size(); i++) {
                oos.writeObject(HEADER_LIST.get(i));
                if (i == HEADER_LIST.size() - 1) {
                    oos.writeObject(null);
                    oos.flush();
                }
            }
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
    public static void flashHeader(Header[] headers) {
        HEADER_LIST.clear();
        for (Header header : headers) {
            String value = header.getValue();
            String cookieString = value.split(";")[0];
            Cookie cookie = new Cookie(cookieString.split("=")[0], cookieString.split("=")[1]);
            HEADER_LIST.add(cookie);
        }
    }

    public static boolean haveAccount() {
        return !CollectionUtils.isEmpty(HEADER_LIST);
    }

    public static void logout() {
        HEADER_LIST.clear();
    }

    public static String getUid() {
        for (Cookie cookie : HEADER_LIST) {
            if ("mid".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return "";
    }

    public static Header getHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Cookie cookie : HEADER_LIST) {
            stringBuilder.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
        }
        return new BasicHeader("Cookie", stringBuilder.toString());
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
