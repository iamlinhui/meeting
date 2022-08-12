package cn.promptness.meeting.tool.service;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import cn.promptness.meeting.tool.utils.TooltipUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ConfirmRoomService extends BaseService<Void> {

    @Resource
    private HttpClientUtil httpClientUtil;

    private List<String> timeList;
    private String meetingDate;
    private String roomId;

    public ConfirmRoomService param(String roomId, String meetingDate, List<String> timeList) {
        this.roomId = roomId;
        this.meetingDate = meetingDate;
        this.timeList = timeList;
        return this;
    }

    private Map<String, String> getParamMap(String startTime, String endTime) {
        Map<String, String> paramMap = new HashMap<>(16);
        paramMap.put("meeting_type_id", "1");
        paramMap.put("meeting_name", "工作汇报");
        paramMap.put("city", "深圳市");
        paramMap.put("address", "中国储能大厦");
        paramMap.put("meeting_date", meetingDate);
        paramMap.put("start_time", startTime);
        paramMap.put("end_time", endTime);
        paramMap.put("meeting_person", AccountCache.getUid());
        paramMap.put("room_id", roomId);
        return paramMap;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (String time : timeList) {
                    String[] split = time.split("\\|");
                    Map<String, String> paramMap = getParamMap(split[0], split[1]);
                    HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/due_meeting.json", paramMap, AccountCache.getHeaderList());
                    Response<?> response = httpResult.getContent(Response.class);
                    if (response.isSuccess()) {
                        SystemTrayUtil.displayMessage(String.format("预定%s会议室成功%s(%s~%s)", Constant.ROOM_INFO_MAP.get(roomId), meetingDate, split[0], split[1]));
                        continue;
                    }
                    String message = response.getMessage();
                    final String conflict = "预定的会议室冲突";
                    if (message.contains(conflict)) {
                        Platform.runLater(() -> TooltipUtil.show(conflict));
                        continue;
                    }
                    final String future = "只能预定未来7天内的会议室";
                    if (message.contains(future)) {
                        Platform.runLater(() -> TooltipUtil.show(future));
                    }
                }
                return null;
            }
        };
    }
}
