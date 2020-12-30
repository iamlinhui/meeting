package cn.promptness.meeting.tool.task;

import cn.promptness.meeting.tool.utils.ChromeCookie;
import cn.promptness.meeting.tool.utils.ChromeDecryptHelper;
import cn.promptness.meeting.tool.utils.OpenUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Slf4j
public class MeetingTask implements Runnable {

    public MeetingTask(MeetingTaskProperties meetingTaskProperties) {
        this.meetingTaskProperties = meetingTaskProperties;
    }

    private final MeetingTaskProperties meetingTaskProperties;

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    @SneakyThrows
    public void meeting() {
        Set<ChromeCookie> chromeCookies = this.getChromeCookies();
        Header header = this.getHeader(chromeCookies);
        URIBuilder builder = this.getUriBuilder(chromeCookies);
        HttpGet httpGet = this.getHttpGet(header);
        log.info("---开始发送请求---");
        for (String roomId : meetingTaskProperties.getRoomIdList()) {
            builder.setParameter("room_id", roomId);
            httpGet.setURI(builder.build());

            try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(USER_AGENT).build().execute(httpGet)) {
                String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                if (this.isSuccess(content)) {
                    break;
                }
            }
        }
        log.info("---结束发送请求---");
    }

    private boolean isSuccess(String content) throws JSONException {
        JSONObject jsonObject = new JSONObject(content);
        int code = jsonObject.getInt("retcode");
        if (code == 0) {
            return true;
        }
        log.error(jsonObject.getString("retmsg"));
        OpenUtil.open(code);
        return false;
    }


    private HttpGet getHttpGet(Header header) {
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader(header);
        return httpGet;
    }

    private Header getHeader(Set<ChromeCookie> chromeCookieList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (ChromeCookie chromeCookie : chromeCookieList) {
            stringBuilder.append(chromeCookie.getName()).append("=").append(chromeCookie.getValue()).append(";");
        }
        return new BasicHeader("Cookie", stringBuilder.toString());
    }

    private URIBuilder getUriBuilder(Set<ChromeCookie> chromeCookieList) throws URISyntaxException {
        URIBuilder builder = new URIBuilder("https://m.oa.fenqile.com/meeting/main/due_meeting.json");
        builder.setParameter("meeting_type_id", "1");
        builder.setParameter("meeting_name", "工作汇报");
        builder.setParameter("city", "深圳市");
        builder.setParameter("address", "中国储能大厦");
        builder.setParameter("meeting_date", LocalDate.now().plusDays(meetingTaskProperties.getPlusDays()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        builder.setParameter("start_time", meetingTaskProperties.getStartTime());
        builder.setParameter("end_time", meetingTaskProperties.getEndTime());
        for (ChromeCookie chromeCookie : chromeCookieList) {
            if ("Hm_cv_6617828cdccae4d04f1557b9a67df803".equals(chromeCookie.getName())) {
                String uid = chromeCookie.getValue().split("\\*")[2];
                builder.setParameter("meeting_person", uid);
                break;
            }
        }
        return builder;
    }

    private Set<ChromeCookie> getChromeCookies() {
        log.info("---开始解析Chrome浏览器Cookie文件---");
        String sql = "SELECT * FROM cookies WHERE host_key = '.oa.fenqile.com'";
        Set<ChromeCookie> decryptedCookies = ChromeDecryptHelper.getWindowsDecryptedCookies(sql);
        log.info("---成功解析Chrome浏览器Cookie文件---");
        return decryptedCookies;
    }

    @Override
    public void run() {
        meeting();
    }
}
