package cn.promptness.meeting.tool.task;

import cn.promptness.httpclient.HttpClientUtil;
import cn.promptness.httpclient.HttpResult;
import cn.promptness.meeting.tool.cache.AccountCache;
import cn.promptness.meeting.tool.config.MeetingTaskProperties;
import cn.promptness.meeting.tool.data.Constant;
import cn.promptness.meeting.tool.exception.MeetingException;
import cn.promptness.meeting.tool.pojo.Response;
import cn.promptness.meeting.tool.pojo.Room;
import cn.promptness.meeting.tool.pojo.RoomTime;
import cn.promptness.meeting.tool.pojo.TaskEvent;
import cn.promptness.meeting.tool.utils.SystemTrayUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MeetingTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MeetingTask.class);
    private static final String CACHE = "CACHE";

    public MeetingTask(MeetingTaskProperties meetingTaskProperties, ConfigurableApplicationContext applicationContext) {
        this.meetingTaskProperties = meetingTaskProperties;
        this.applicationContext = applicationContext;
        this.cache = this.buildCache();
    }

    private final MeetingTaskProperties meetingTaskProperties;
    private final ConfigurableApplicationContext applicationContext;
    private final Cache<String, List<String>> cache;

    @Override
    public void run() {
        if (AccountCache.haveAccount()) {
            try {
                this.meeting();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void meeting() throws Exception {
        HttpClientUtil httpClientUtil = applicationContext.getBean(HttpClientUtil.class);
        // 还未到预定时间
        if (Objects.equals(Boolean.FALSE, meetingTaskProperties.isEnable())) {
            // 提前准备
            this.getCache();
            return;
        }
        // 已经过了会议室开始时间
        if (Objects.equals(Boolean.FALSE, meetingTaskProperties.checkTimeIsOk())) {
            Platform.runLater(() -> applicationContext.publishEvent(new TaskEvent(meetingTaskProperties.getTarget(), false)));
            return;
        }
        Map<String, String> paramMap = this.getParamMap();
        // 获取菜单 很容易获取不到数据加载重试机制
        List<String> roomIdList = this.getCache();
        for (String roomId : roomIdList) {
            boolean success = this.getRetryTemplate().execute(retryContext -> this.handle(httpClientUtil, paramMap, roomId), recoveryCallback -> false);
            if (success) {
                Platform.runLater(() -> applicationContext.publishEvent(new TaskEvent(meetingTaskProperties.getTarget(), true)));
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
        paramMap.put("meeting_person", AccountCache.getUid());
        return paramMap;
    }

    private List<String> filterRoomIdList(HttpClientUtil httpClientUtil) throws Exception {
        log.info("---开始过滤{}会议室{}~{}---", meetingTaskProperties.getMeetingDate(), meetingTaskProperties.getStartTime(), meetingTaskProperties.getEndTime());
        HttpResult httpResult = httpClientUtil.doGet(String.format("https://m.oa.fenqile.com/restful/get/meeting/meeting_room_address_room.json?address=中国储能大厦&meeting_date=%s", meetingTaskProperties.getMeetingDateString()), AccountCache.getHeaderList());
        Response<Room> response = httpResult.getContent(new TypeToken<Response<Room>>() {}.getType());
        if (!response.isSuccess()) {
            throw new MeetingException(response.getMessage());
        }
        List<String> filterRoomIdList = response.getResult().stream().filter(this::filterRoom).filter(this::filterTime).map(Room::getRoomId).collect(Collectors.toList());
        if (filterRoomIdList.isEmpty()) {
            return filterRoomIdList;
        }
        if (meetingTaskProperties.getRoomIdList().size() == filterRoomIdList.size()) {
            return meetingTaskProperties.getRoomIdList();
        }
        // 保持原有的添加顺序
        List<String> roomIdList = new ArrayList<>();
        for (String roomId : meetingTaskProperties.getRoomIdList()) {
            if (filterRoomIdList.contains(roomId)) {
                roomIdList.add(roomId);
            }
        }
        return roomIdList;
    }

    private boolean filterTime(Room room) {
        List<RoomTime> roomTimeList = room.getTime();
        int startTimeIndex = Constant.TIME_LIST.indexOf(meetingTaskProperties.getStartTime());
        int endTimeIndex = Constant.TIME_LIST.indexOf(meetingTaskProperties.getEndTime());
        for (int i = startTimeIndex; i < endTimeIndex; i++) {
            RoomTime roomTime = roomTimeList.get(i);
            if (roomTime.getFlag() == 0) {
                return false;
            }
        }
        return true;
    }

    private boolean filterRoom(Room room) {
        // 过滤被屏蔽的会议室
        return meetingTaskProperties.getRoomIdList().contains(room.getRoomId());
    }

    private boolean handle(HttpClientUtil httpClientUtil, Map<String, String> paramMap, String roomId) throws Exception {
        log.info("---开始预定{}会议室{} {}~{}---", Constant.ROOM_INFO_MAP.get(roomId), meetingTaskProperties.getMeetingDate(), meetingTaskProperties.getStartTime(), meetingTaskProperties.getEndTime());
        paramMap.put("room_id", roomId);
        HttpResult httpResult = httpClientUtil.doGet("https://m.oa.fenqile.com/meeting/main/due_meeting.json", paramMap, AccountCache.getHeaderList());
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
        fixedBackOffPolicy.setBackOffPeriod(1000);
        retryTemplate.setRetryPolicy(policy);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        return retryTemplate;
    }

    private Cache<String, List<String>> buildCache() {
        Cache<String, List<String>> cache = Caffeine.newBuilder().expireAfterWrite(17, TimeUnit.MINUTES).maximumSize(1).build();
        try {
            HttpClientUtil httpClientUtil = applicationContext.getBean(HttpClientUtil.class);
            // 获取菜单 很容易获取不到数据加载重试机制
            List<String> roomIdList = this.getRetryTemplate().execute(retryContext -> this.filterRoomIdList(httpClientUtil));
            cache.put(CACHE, roomIdList);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return cache;
    }

    private List<String> getCache() throws Exception {
        List<String> roomIdList = cache.getIfPresent(CACHE);
        if (CollectionUtils.isEmpty(roomIdList)) {
            HttpClientUtil httpClientUtil = applicationContext.getBean(HttpClientUtil.class);
            roomIdList = this.getRetryTemplate().execute(retryContext -> this.filterRoomIdList(httpClientUtil));
            cache.put(CACHE, roomIdList);
        }
        return roomIdList;
    }
}
