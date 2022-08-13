package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.utils.TooltipUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CancelRoomService extends BaseService<Void> {

    @Resource
    private HttpClientUtil httpClientUtil;

    private List<String> cancelList;

    public CancelRoomService setCancelList(List<String> cancelList) {
        this.cancelList = cancelList;
        return this;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (String meetingId : cancelList) {
                    httpClientUtil.doGet(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_cancel_meeting.json?meeting_id=%s", meetingId), AccountCache.getHeaderList());
                }
                Platform.runLater(() -> TooltipUtil.show("退订完成!"));
                return null;
            }
        };
    }
}
