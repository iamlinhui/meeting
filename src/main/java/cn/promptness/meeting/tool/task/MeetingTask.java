package cn.promptness.meeting.tool.task;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class MeetingTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MeetingTask.class);

    public MeetingTask(MeetingTaskProperties meetingTaskProperties) {
        this.meetingTaskProperties = meetingTaskProperties;
    }

    private final MeetingTaskProperties meetingTaskProperties;

    public Boolean meeting() throws URISyntaxException, IOException, JSONException {
        if (Objects.equals(Boolean.FALSE, meetingTaskProperties.isEnable())) {
            return false;
        }
        Header header = MeetingUtil.getHeader();
        URIBuilder builder = this.getUriBuilder();
        HttpGet httpGet = this.getHttpGet(header);
        log.info("---开始发送请求---");
        for (String roomId : meetingTaskProperties.getRoomIdList()) {
            builder.setParameter("room_id", roomId);
            httpGet.setURI(builder.build());
            log.info("---预定{}会议室---", Constant.ROOM_INFO_LIST.get(roomId));
            try (CloseableHttpResponse closeableHttpResponse = HttpClients.custom().setUserAgent(Constant.USER_AGENT).build().execute(httpGet)) {
                String content = EntityUtils.toString(closeableHttpResponse.getEntity(), StandardCharsets.UTF_8);
                if (this.isSuccess(content) && !Objects.equals(Boolean.TRUE, meetingTaskProperties.getMultipleChoice())) {
                    log.info("---结束发送请求---");
                    return true;
                }
            }
        }
        log.info("---结束发送请求---");
        return false;
    }

    private boolean isSuccess(String content) throws JSONException {
        JSONObject jsonObject = new JSONObject(content);
        int code = jsonObject.getInt("retcode");
        if (code == 0) {
            return true;
        }
        log.error(jsonObject.getString("retmsg"));
        return false;
    }


    private HttpGet getHttpGet(Header header) {
        HttpGet httpGet = new HttpGet();
        httpGet.setHeader(header);
        return httpGet;
    }

    private URIBuilder getUriBuilder() throws URISyntaxException {
        URIBuilder builder = new URIBuilder("https://m.oa.fenqile.com/meeting/main/due_meeting.json");
        builder.setParameter("meeting_type_id", "1");
        builder.setParameter("meeting_name", "工作汇报");
        builder.setParameter("city", "深圳市");
        builder.setParameter("address", "中国储能大厦");
        builder.setParameter("meeting_date", LocalDate.now().plusDays(meetingTaskProperties.getPlusDays()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        builder.setParameter("start_time", meetingTaskProperties.getStartTime());
        builder.setParameter("end_time", meetingTaskProperties.getEndTime());
        builder.setParameter("meeting_person", MeetingUtil.getUid());
        return builder;
    }

    @Override
    public void run() {
        if (MeetingUtil.haveAccount()) {
            try {
                meeting();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
