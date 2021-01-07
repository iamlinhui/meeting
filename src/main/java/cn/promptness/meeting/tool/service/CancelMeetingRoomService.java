package cn.promptness.meeting.tool.service;

import cn.promptness.meeting.tool.utils.OpenUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CancelMeetingRoomService extends Service<Void> {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    private String meetingId;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                URIBuilder builder = new URIBuilder(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_cancel_meeting.json?meeting_id=%s", meetingId));
                HttpGet httpGet = new HttpGet();
                httpGet.setHeader(OpenUtil.getHeader());
                httpGet.setURI(builder.build());

                try (CloseableHttpResponse ignored = HttpClients.custom().setUserAgent(USER_AGENT).build().execute(httpGet)) {
                    return null;
                }
            }
        };
    }


    public String getMeetingId() {
        return meetingId;
    }

    public CancelMeetingRoomService setMeetingId(String meetingId) {
        this.meetingId = meetingId;
        return this;
    }
}
