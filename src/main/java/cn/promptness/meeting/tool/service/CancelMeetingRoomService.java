package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CancelMeetingRoomService extends BaseService<HttpResult> {

    @Resource
    private HttpClientUtil httpClientUtil;

    private String meetingId;

    @Override
    protected Task<HttpResult> createTask() {
        return new Task<HttpResult>() {
            @Override
            protected HttpResult call() throws Exception {
                return httpClientUtil.doGet(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_cancel_meeting.json?meeting_id=%s", getMeetingId()), MeetingUtil.getHeaderList());
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

    @Override
    public Service<HttpResult> expect(Callback callback) {
        return this;
    }
}
