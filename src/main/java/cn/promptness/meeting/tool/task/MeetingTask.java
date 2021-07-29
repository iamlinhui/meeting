package cn.promptness.meeting.tool.task;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.exception.MeetingException;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Room;
import cn.promptness.meeting.tool.utils.MeetingUtil;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class MeetingTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MeetingTask.class);

    public MeetingTask(MeetingTaskProperties meetingTaskProperties, ConfigurableApplicationContext applicationContext) {
        this.meetingTaskProperties = meetingTaskProperties;
        this.applicationContext = applicationContext;
    }

    private final MeetingTaskProperties meetingTaskProperties;
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void run() {
        if (MeetingUtil.haveAccount()) {
            try {
                this.meeting();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void meeting() throws Exception {
        HttpClientUtil httpClientUtil = applicationContext.getBean(HttpClientUtil.class);
        if (Objects.equals(Boolean.FALSE, meetingTaskProperties.isEnable())) {
            return;
        }
        if (Objects.equals(Boolean.FALSE, meetingTaskProperties.checkTimeIsOk())) {
            Platform.runLater(() -> applicationContext.publishEvent(false));
            return;
        }
        Map<String, String> paramMap = this.getParamMap();
        // 获取菜单 很容易获取不到数据加载重试机制
        List<String> roomIdList = this.getRetryTemplate().execute(retryContext -> this.filterRoomIdList(httpClientUtil));
        for (String roomId : roomIdList) {
            boolean success = this.getRetryTemplate().execute(retryContext -> this.handle(httpClientUtil, paramMap, roomId));
            if (success) {
                Platform.runLater(() -> applicationContext.publishEvent(true));
                return;
            }
        }
    }

    private Map<String, String> getParamMap() {
        Map<String, String> paramMap = new HashMap<>(16);
        paramMap.put("meeting_type_id", "1");
        paramMap.put("meeting_name", "工作汇报");
        paramMap.put("city", "深圳市");
        paramMap.put("address", "中国储能大厦");
        paramMap.put("meeting_date", meetingTaskProperties.getMeetingDateString());
        paramMap.put("start_time", meetingTaskProperties.getStartTime());
        paramMap.put("end_time", meetingTaskProperties.getEndTime());
        paramMap.put("meeting_person", MeetingUtil.getUid());
        return paramMap;
    }

    private List<String> filterRoomIdList(HttpClientUtil httpClientUtil) throws Exception {
        // 过滤被屏蔽的会议室
        List<String> roomIdList = new ArrayList<>();
        HttpResult httpResult = httpClientUtil.doGet(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_address_room.json?address=中国储能大厦&meeting_date=%s", meetingTaskProperties.getMeetingDateString()), MeetingUtil.getHeaderList());
        Response<Room> response = httpResult.getContent(new TypeToken<Response<Room>>() {}.getType());
        if (!response.isSuccess()) {
            throw new MeetingException(response.getMessage());
        }
        List<Integer> menuRoomList = response.getResult().stream().map(Room::getRoomId).collect(Collectors.toList());
        for (String roomId : meetingTaskProperties.getRoomIdList()) {
            if (menuRoomList.contains(Integer.valueOf(roomId))) {
                roomIdList.add(roomId);
            }
        }
        return roomIdList;
    }

    private boolean handle(HttpClientUtil httpClientUtil, Map<String, String> paramMap, String roomId) throws Exception {
        log.info("---开始预定{}会议室---", Constant.ROOM_INFO_MAP.get(roomId));
        paramMap.put("room_id", roomId);
        HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/due_meeting.json", paramMap, MeetingUtil.getHeaderList());
        Response<?> response = httpResult.getContent(Response.class);
        if (response.isSuccess()) {
            log.info("---结束发送请求---");
            SystemTrayUtil.displayMessage(String.format("预定%s会议室成功%s(%s~%s)", Constant.ROOM_INFO_MAP.get(roomId), meetingTaskProperties.getMeetingDateString(), meetingTaskProperties.getStartTime(), meetingTaskProperties.getEndTime()));
            return true;
        }
        String message = response.getMessage();
        final String conflict = "预定的会议室冲突";
        if (message.contains(conflict)) {
            log.error(conflict);
            return false;
        }
        final String future = "只能预定未来7天内的会议室";
        if (message.contains(future)) {
            log.error(future);
            // 重试
            throw new MeetingException(future);
        }
        log.error(message);
        return false;
    }

    private RetryTemplate getRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        // 设置重试策略，主要设置重试次数
        SimpleRetryPolicy policy = new SimpleRetryPolicy(3, Collections.singletonMap(MeetingException.class, true));
        // 设置重试回退操作策略，主要设置重试间隔时间
        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000);
        retryTemplate.setRetryPolicy(policy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }
}
