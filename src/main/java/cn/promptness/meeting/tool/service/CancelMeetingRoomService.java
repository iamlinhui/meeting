package cn.promptness.meeting.tool.service;

import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.utils.MeetingUtil;
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


    private String meetingId;

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {

                URIBuilder builder = new URIBuilder(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_cancel_meeting.json?meeting_id=%s", getMeetingId()));
                HttpGet httpGet = new HttpGet();
                httpGet.setHeader(MeetingUtil.getHeader());
                httpGet.setURI(builder.build());

                try (CloseableHttpResponse ignored = HttpClients.custom().setUserAgent(Constant.USER_AGENT).build().execute(httpGet)) {
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
